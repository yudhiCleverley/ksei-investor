package akses.ksei.co.id.investor.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.service.BatchOrchestratorService;
import akses.ksei.co.id.investor.service.JobHealthServiceImpl;
import akses.ksei.co.id.investor.service.ParameterImpl;
import akses.ksei.co.id.investor.service.SchedulerMgmtServiceImpl;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DynamicBatchScheduler {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());
	private final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Jakarta");

	// private String SCHEDULER_CODE = Constants.PULL_DATA_INVESTOR_CODE;

	private final ParameterImpl _parameterImpl;

	private final JobHealthServiceImpl _jobHealthServiceImpl;

	private final SchedulerMgmtServiceImpl _schedulerMgmtServiceImpl;

	private final BatchOrchestratorService _batchOrchestratorService;

	private final TaskScheduler _taskScheduler;

	// Menyimpan cron per schedulerCode
	private final Map<String, String> schedulerCronMap = new ConcurrentHashMap<>();

	// Menyimpan scheduled task per schedulerCode
	private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

	// Menyimpan status "sedang running" per schedulerCode
	private final Map<String, AtomicBoolean> jobRunningMap = new ConcurrentHashMap<>();

	// Menyimpan tanggal schedule per schedulerCode
	private final Map<String, String> lastScheduledDateMap = new ConcurrentHashMap<>();

	private final Map<String, ReentrantLock> schedulerLocks = new ConcurrentHashMap<>();

	private ReentrantLock getLock(String schedulerCode) {
		return schedulerLocks.computeIfAbsent(schedulerCode, k -> new ReentrantLock());
	}

	// Update the cron expression dynamically
	public void updateCronAndSchedule(String schedulerCode, String regularDate, String newCronExpression) {
		ReentrantLock lock = getLock(schedulerCode);
		lock.lock();

		try {
			if (_log.isInfoEnabled()) {
				_log.info("Updating cron for [{}]. Previous: {}, New: {}", schedulerCode,
						schedulerCronMap.get(schedulerCode), newCronExpression);
			}

			if (!isValidCron(newCronExpression)) {
				_log.warn("Invalid cron expression: {}", newCronExpression);
				return;
			}

			// stop existing schedule (if any)
			stopScheduler(schedulerCode);

			// set new cron
			schedulerCronMap.put(schedulerCode, newCronExpression);

			if (Validator.isNotNull(regularDate)) {
				lastScheduledDateMap.put(schedulerCode, regularDate);
			} else {
				lastScheduledDateMap.remove(schedulerCode);
			}

			// start schedule with the (possibly) updated cron & regular date
			startScheduler(schedulerCode);
		} finally {
			lock.unlock();
		}
	}

	// Schedule the task with the current cron expression
	public void startScheduler(String schedulerCode) {
		ReentrantLock lock = getLock(schedulerCode);
		lock.lock();

		try {
			String cron = schedulerCronMap.getOrDefault(schedulerCode, _parameterImpl.getCronExpression());
			String date = lastScheduledDateMap.getOrDefault(schedulerCode, _parameterImpl.getDate());

			if (_log.isInfoEnabled()) {
				_log.info("{} Starting scheduler with cron: {} and date: {}", logContext(schedulerCode), cron, date);
			}

			// stop any existing scheduled future
			stopScheduler(schedulerCode);

			boolean isActive = _schedulerMgmtServiceImpl.isActive(schedulerCode);
			_log.info("Scheduler status for [{}]: {}", schedulerCode, isActive ? "ACTIVE" : "INACTIVE");

			ScheduledFuture<?> future = _taskScheduler.schedule(() -> runTask(schedulerCode, date),
					new CronTrigger(cron, TIME_ZONE));
			scheduledTasks.put(schedulerCode, future);
		} finally {
			lock.unlock();
		}
	}

	public void stopScheduler(String schedulerCode) {
		ScheduledFuture<?> future = scheduledTasks.get(schedulerCode);

		if (future != null && !future.isCancelled()) {
			future.cancel(true);

			if (_log.isInfoEnabled()) {
				_log.info("Stopped job [{}], cron: {}, date: {}", schedulerCode, schedulerCronMap.get(schedulerCode),
						lastScheduledDateMap.get(schedulerCode));
			}

			scheduledTasks.remove(schedulerCode);
		}
	}

	public void stopAllSchedulers() {
		scheduledTasks.keySet().forEach(this::stopScheduler);
	}

	@Async
	public void runNow(String schedulerCode, String date) {
		String execDate = Validator.isNotNull(date) ? date : _parameterImpl.getDate();
		if (_log.isInfoEnabled()) {
			_log.info("{} runNow invoked with execDate: {}", logContext(schedulerCode), execDate);
		}

		runTask(schedulerCode, execDate);
	}

	public void runTask(String schedulerCode, String date) {
		if (_log.isInfoEnabled()) {
			_log.info("{} Job started at {}", logContext(schedulerCode), LocalDateTime.now());
		}

		AtomicBoolean isRunning = jobRunningMap.computeIfAbsent(schedulerCode, k -> new AtomicBoolean(false));

		if (!isRunning.compareAndSet(false, true)) {
			_log.warn("Job [{}] is already running. Skipping execution.", schedulerCode);
			return;
		}

		try {
			executeBatchJob(schedulerCode, date);
		} catch (Exception e) {
			_log.error("{} Batch job failed: {}", logContext(schedulerCode), e.getMessage(), e);
		} finally {
			isRunning.set(false);

			if (_log.isInfoEnabled()) {
				_log.info("{} Job completed at {}", logContext(schedulerCode), LocalDateTime.now());
			}
		}

		// Log all job health status
		_jobHealthServiceImpl.getAllJobStatus().forEach((jobName, lastRunTime) -> {
			if (_log.isInfoEnabled()) {
				_log.info("{} Job: {}, Last Run Time: {}", logContext(schedulerCode), jobName, lastRunTime);
			}
		});
	}

	private void executeBatchJob(String schedulerCode, String date) {
		// Check scheduler active status here
		if (!_schedulerMgmtServiceImpl.isActive(schedulerCode)) {
			if (_log.isWarnEnabled()) {
				_log.warn("{} Scheduler is not active. Skipping execution.", logContext(schedulerCode));
			}
			return;
		}

		// Check job health
		if (!_jobHealthServiceImpl.isJobHealthy(schedulerCode, Duration.ofHours(24)) && _log.isWarnEnabled()) {
			_log.warn("Job [{}] did not run within expected time frame (24h)", schedulerCode);
		}

		// Update last run time
		_jobHealthServiceImpl.updateLastRunTime(schedulerCode);
		if (_log.isInfoEnabled()) {
			_log.info("{} Successfully updated last run time.", logContext(schedulerCode));
		}

		// execute batch jobs
		if (Validator.isNotNull(date)) {
			_log.info("Executing job [{}] for date: {}", schedulerCode, date);
			_batchOrchestratorService.runBatch(schedulerCode, date);
		}
	}

	private boolean isValidCron(String cron) {
		try {
			new CronTrigger(cron);
			return true;
		} catch (Exception e) {
			_log.error("Invalid cron expression: {}", cron);
			return false;
		}
	}

	private String logContext(String schedulerCode) {
		return String.format("[jobName=%s][schedulerCode=%s]", Constants.JOBS_NAME, schedulerCode);
	}

}

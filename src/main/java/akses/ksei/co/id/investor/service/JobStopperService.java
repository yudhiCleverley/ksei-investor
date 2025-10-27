package akses.ksei.co.id.investor.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobStopperService {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private static final int PAGE_SIZE = 50;

	private final JobExplorer jobExplorer;

	private final JobRepository jobRepository;

	@Async("batchStopExecutor")
	public void stopRunningJobAsync(String schedulerCode) {
		_log.info("Attempting to stop job for schedulerCode: {}", schedulerCode);

		String jobName = mapSchedulerCodeToJobName(schedulerCode);
		if (Validator.isNull(jobName)) {
			_log.warn("Unknown schedulerCode: {}", schedulerCode);
			return;
		}

		int start = 0;
		List<JobInstance> jobInstances;

		do {
			jobInstances = jobExplorer.getJobInstances(jobName, start, PAGE_SIZE);

			for (JobInstance instance : jobInstances) {
				stopRunningExecutions(instance, jobName);
			}

			start += PAGE_SIZE;
		} while (!jobInstances.isEmpty());

		_log.info("Completed job stop check for jobName: {}", jobName);
	}

	private void stopRunningExecutions(JobInstance instance, String jobName) {
		List<JobExecution> executions = jobExplorer.getJobExecutions(instance);

		for (JobExecution execution : executions) {
			BatchStatus status = execution.getStatus();

			if (status.isRunning()) {
				_log.info("Found RUNNING JobExecution ID {} for job {}. Marking as STOPPING...", execution.getId(),
						jobName);

				execution.setStatus(BatchStatus.STOPPING);
				execution.setEndTime(new Date());
				execution.getExecutionContext().putString("stoppedBy", "USER");
				execution.getExecutionContext().putString("stopTime", LocalDateTime.now().toString());

				jobRepository.update(execution);

				_log.info("Successfully updated JobExecution ID {} to STOPPING", execution.getId());
			}
		}
	}

	private String mapSchedulerCodeToJobName(String schedulerCode) {
		if (Constants.PULL_DATA_INVESTOR_CODE.equals(schedulerCode)) {
			return Constants.JOBS_NAME;
		}

		throw new IllegalArgumentException("Unknown schedulerCode: " + schedulerCode);
	}

}

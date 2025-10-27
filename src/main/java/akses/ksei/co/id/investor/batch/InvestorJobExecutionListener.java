package akses.ksei.co.id.investor.batch;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import akses.ksei.co.id.investor.enumeration.AuditType;
import akses.ksei.co.id.investor.model.AuditRequest;
import akses.ksei.co.id.investor.service.AuditTrailImpl;
import akses.ksei.co.id.investor.service.JobStopFlagService;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.ServerInfoUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InvestorJobExecutionListener implements JobExecutionListener {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final AuditTrailImpl _auditTrailImpl;

	private final ServerInfoUtil _serverInfoUtil;

	private final InvestorItemReader _reader;

	private final JobResultHolder _jobResultHolder;

	private final JobStopFlagService _jobStopFlagService;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		_jobStopFlagService.reset();

		try {
			JobParameters params = jobExecution.getJobParameters();

			String date = params.getString("date");

			// configure reader
			_reader.setQuery(date);

			// configure server info
			String serverIp = _serverInfoUtil.getServerIp();

			String serverPort = _serverInfoUtil.getHostName();

			ExecutionContext executionContext = jobExecution.getExecutionContext();
			executionContext.put(Constants.CLIENT_IP_KEY, serverIp);
			executionContext.put(Constants.SERVER_PORT_KEY, serverPort);

			_log.info("Audit data added to ExecutionContext: clientIp={}, serverPort={}", serverIp, serverPort);
		} catch (Exception e) {
			// _log.error("Error configuring reader: " + e.getMessage(), e);
			throw new IllegalStateException("Job configuration failed: ", e);
		}
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		String stoppedBy = jobExecution.getExecutionContext().containsKey("stoppedBy")
				? jobExecution.getExecutionContext().getString("stoppedBy")
				: "";
		boolean manuallyStopped = stoppedBy.equalsIgnoreCase("USER");

		if (manuallyStopped) {
			String stopTime = jobExecution.getExecutionContext().containsKey("stopTime")
					? jobExecution.getExecutionContext().getString("stopTime")
					: "unknown";

			_log.warn("Job was manually stopped by user at {}", stopTime);

			if (jobExecution.getStatus() != BatchStatus.STOPPED) {
				jobExecution.setStatus(BatchStatus.STOPPED);
			}
		}

		String jobName = jobExecution.getJobInstance().getJobName();
		String status = manuallyStopped ? "STOPPED" : jobExecution.getStatus().toString();
		// Map<String, JobParameter> jobParameters =
		// jobExecution.getJobParameters().getParameters();
		long durationMillis = Optional.ofNullable(jobExecution.getStartTime())
				.flatMap(start -> Optional.ofNullable(jobExecution.getEndTime())
						.map(end -> Duration.between(start.toInstant(), end.toInstant()).toMillis()))
				.orElse(0L);

		int totalInsertCount = 0;
		int totalUpdateCount = 0;
		int totalDuplicateCount = 0;
		int totalReadCount = 0;

		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			ExecutionContext stepContext = stepExecution.getExecutionContext();
			totalInsertCount += stepContext.getInt("insertCount", 0);
			totalUpdateCount += stepContext.getInt("updateCount", 0);
			totalDuplicateCount += stepContext.getInt("duplicateCount", 0);
			totalReadCount += stepExecution.getReadCount();
		}

		// Format log
		String logMessage = String.format(
				"Job: [%s] completed with status: [%s], duration: %dms, Pulled: %d, Inserted: %d, Updated: %d, Duplicate: %d",
				jobName, status, durationMillis, totalReadCount, totalInsertCount, totalUpdateCount,
				totalDuplicateCount);

		// String logMessage = String.format(
		// "Job: [%s] completed with the following parameters: [%s] and the following
		// status: [%s] in %dms",
		// jobName, jobParameters, status, durationMillis);

		String jobId = String.valueOf(jobExecution.getJobId());
		_jobResultHolder.setSuccessMessage(jobId, logMessage);

		if (jobExecution.getStatus() == BatchStatus.FAILED) {
			_log.error("JOB GAGAL: {} status: {}", jobExecution.getJobInstance().getJobName(),
					jobExecution.getStatus());

			List<Throwable> allExceptions = jobExecution.getAllFailureExceptions();

			for (int i = 0; i < allExceptions.size(); i++) {
				Throwable t = allExceptions.get(i);
				_log.error("Job failure #{}: {} - {}", i + 1, t.getClass().getSimpleName(), t.getMessage(), t);
			}
		}

		// add audit trail
		try {
			ExecutionContext executionContext = jobExecution.getExecutionContext();

			String clientIp = (String) executionContext.get(Constants.CLIENT_IP_KEY);
			String serverPort = (String) executionContext.get(Constants.SERVER_PORT_KEY);

			clientIp = (clientIp == null) ? "unknown" : clientIp;
			serverPort = (serverPort == null) ? "unknown" : serverPort;

			_log.info("_clientIp: {}", clientIp);
			_log.info("_serverPort: {}", serverPort);

			AuditRequest request = new AuditRequest(AuditType.SCHEDULER_AUDIT.getId(), Constants.MODULE_NAME,
					this.getClass().getName(), Constants.PULL_INVESTOR_DATA, new Date(), clientIp, serverPort,
					logMessage);
			_auditTrailImpl.createAuditTrail(request);
		} catch (Exception e) {
			_log.error("failed add audit trail : " + e.getMessage(), e);
		}

	}

}

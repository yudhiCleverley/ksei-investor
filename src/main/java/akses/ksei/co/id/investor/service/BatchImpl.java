package akses.ksei.co.id.investor.service;

import java.util.function.IntSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.batch.ChunkSizeCalculator;
import akses.ksei.co.id.investor.batch.JobResultHolder;
import akses.ksei.co.id.investor.exception.BatchJobExecutionException;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchImpl {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final JobLauncher jobLauncher;

	private final JobLocator jobLocator;

	private final JobRegistry jobRegistry;

	private final ChunkSizeCalculator _chunkSizeCalculator;

	private final JobResultHolder _jobResultHolder;

	public String executeBatchJob(String schedulerCode, String date, IntSupplier totalItem, int threadPoolSize) {
		String jobName = Constants.JOBS_NAME;

		try {
			if (!jobRegistry.getJobNames().contains(schedulerCode)) {
				throw new IllegalStateException("Job not registered: " + schedulerCode);
			}

			int totalDataCount = totalItem.getAsInt();
			_log.info("Total data count: {}", totalDataCount);

			if (totalDataCount == 0) {
				_log.info("No data to process. Skipping batch job.");
				return null;
			}

			int chunkSize = _chunkSizeCalculator.calculateChunkSize(totalDataCount);
			_log.info("Chunk size: {}", chunkSize);

			JobParameters jobParameters = new JobParametersBuilder().addString("schedulerCode", schedulerCode)
					.addString("date", date).addLong("chunkSize", (long) chunkSize)
					.addLong("totalDataCount", (long) totalDataCount).addLong("threadPoolSize", (long) threadPoolSize)
					.addLong("startAt", System.currentTimeMillis()).toJobParameters();

			Job job = jobLocator.getJob(schedulerCode);
			long start = System.currentTimeMillis();
			JobExecution execution = jobLauncher.run(job, jobParameters);
			// _log.info("Batch job started successfully. Status: {}",
			// execution.getStatus());

			String jobId = String.valueOf(execution.getJobId());
			String status = execution.getStatus().toString();

			_jobResultHolder.setTotalCount(jobId, totalDataCount);

			// Tunggu job selesai
			while (execution.isRunning()) {
				if (sleepSafely(500)) {
					return "Job execution interrupted";
				}
			}

			long duration = System.currentTimeMillis() - start;
			String message = getLogMessage(jobId, jobName, status, duration);
			if (Validator.isNull(message)) {
				message = String.format("Job: [%s] completed with status: [%s]", jobName, status);
			}
			_jobResultHolder.setSuccessMessage(jobId, message);
			_jobResultHolder.clear(jobId);

			return message;
		} catch (RuntimeException e) {
			// log.error("Batch job failed: {}", e.getMessage(), e);
			throw new BatchJobExecutionException(String.format("Batch job failed: %s", e.getMessage()), e);
		} catch (Exception e) {
			// log.error("Unexpected error: {}", e.getMessage(), e);
			throw new BatchJobExecutionException(
					String.format("Unexpected error during batch job execution: %s", e.getMessage()), e);
		}
	}

	private boolean sleepSafely(long millis) {
		try {
			Thread.sleep(millis);
			return false;
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			_log.warn("Job execution interrupted: {}", ie.getMessage());
			return true; // Indicate interruption
		}
	}

	private String getLogMessage(String jobId, String jobName, String status, long duration) {

		return _jobResultHolder.formatSuccessMessage(jobId, jobName, status, duration);
	}

	public String formatNoDataMessage(String jobName, String date, String result) {

		return String.format("Job: [%s] completed with status: [COMPLETED], Date: %s => %s", jobName, date, result);
	}

	public String formatFailedMessage(String jobName, String date, String result) {

		return String.format("Job: [%s] completed with status: [FAILED], Date: %s => %s", jobName, date, result);
	}

}

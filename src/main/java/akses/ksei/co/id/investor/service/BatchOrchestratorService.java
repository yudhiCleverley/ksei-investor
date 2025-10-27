package akses.ksei.co.id.investor.service;

import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.batch.BatchLoadBalancer;
import akses.ksei.co.id.investor.config.DataService;
import akses.ksei.co.id.investor.enumeration.JobStatusEnum;
import akses.ksei.co.id.investor.mail.MailServiceImpl;
import akses.ksei.co.id.investor.model.EmailNotificationModel;
import akses.ksei.co.id.investor.util.CommonServiceUtil;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.StringPool;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatchOrchestratorService {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private String jobName = Constants.JOBS_NAME;

	private final JobStatusServiceImpl _jobStatusImpl;

	private final SchedulerMgmtServiceImpl _schedulerMgmtServiceImpl;

	private final NotificationImpl _notificationImpl;

	private final MailServiceImpl _mailServiceImpl;

	private final BatchImpl _batchImpl;

	private final DataService _dataService;

	private final CommonServiceUtil _commonUtil;

	private final BatchLoadBalancer _batchLoadBalancer;

	public void runBatch(String schedulerCode, String date) {
		String parameterInput = date;

		// Mark job status as STARTED
		_log.info("Starting batch job: {}", jobName);
		_jobStatusImpl.upsert(jobName, BatchStatus.STARTED.name());

		long schedulerId = _schedulerMgmtServiceImpl.getSchedulerId(schedulerCode);
		Date startTime = new Date();

		String failedMessage = StringPool.BLANK;
		String successMessage = StringPool.BLANK;
		String logResult = "No data to process";
		JobStatusEnum finalStatus = JobStatusEnum.SUCCESS;

		long historyId = _schedulerMgmtServiceImpl.insertSchedulerHistory(schedulerId, startTime, null,
				BatchStatus.STARTED.name(), failedMessage, successMessage, parameterInput);

		try {
			int totalRecord = _dataService.getTotalInvestor(date);

			if (totalRecord == 0) {
				_log.warn("No investor records found for date: {}", date);
				successMessage = _batchImpl.formatNoDataMessage(Constants.JOBS_NAME, date, logResult);

				_jobStatusImpl.upsert(jobName, BatchStatus.COMPLETED.name());

				_schedulerMgmtServiceImpl.updateSchedulerHistoryOnEnd(historyId, startTime, new Date(),
						BatchStatus.COMPLETED.name(), failedMessage, successMessage);
				return;
			}

			Pair<Integer, Integer> config = _batchLoadBalancer.computeThreadAndChunk(totalRecord);
			int thread = config.getLeft();
			int chunkSize = config.getRight();
			_batchLoadBalancer.logBatchConfig(totalRecord, thread, chunkSize);
			logResult = _batchImpl.executeBatchJob(schedulerCode, date, () -> totalRecord, thread);

			// Mark job status as COMPLETED
			Date endTime = new Date();
			successMessage = logResult;
			finalStatus = JobStatusEnum.SUCCESS;

			_log.info("Batch job {} completed successfully", jobName);
			_jobStatusImpl.upsert(jobName, BatchStatus.COMPLETED.name());

			_schedulerMgmtServiceImpl.updateSchedulerHistoryOnEnd(historyId, startTime, endTime,
					BatchStatus.COMPLETED.name(), failedMessage, successMessage);
		} catch (Exception e) {
			failedMessage = "Error during " + jobName + " execution: " + e.getMessage();
			_log.error(failedMessage, e);
			finalStatus = JobStatusEnum.FAILED;

			_jobStatusImpl.upsert(jobName, BatchStatus.FAILED.name());

			_schedulerMgmtServiceImpl.updateSchedulerHistoryOnEnd(historyId, startTime, new Date(),
					BatchStatus.FAILED.name(), failedMessage, successMessage);
		}

		int jobStatus = _schedulerMgmtServiceImpl.getJobStatus(schedulerCode);

		if (JobStatusEnum.STOPPED.getId() != jobStatus && finalStatus != JobStatusEnum.STOPPED) {
			EmailNotificationModel notifModel = _schedulerMgmtServiceImpl.getEmailNotification(schedulerCode);
			String logMsg = finalStatus == JobStatusEnum.SUCCESS ? successMessage : failedMessage;
			sendNotificationEmail(schedulerCode, date, logMsg, finalStatus, notifModel);
		} else {
			_log.info("Job [{}] was stopped. Notification email will not be sent.", schedulerCode);
		}
	}

	private void sendNotificationEmail(String schedulerCode, String date, String logMsg, JobStatusEnum status,
			EmailNotificationModel notifModel) {
		if (notifModel == null) {
			_log.warn("No email notification config found for scheduler: {}", schedulerCode);
			return;
		}

		boolean isSentEmail = (status == JobStatusEnum.SUCCESS && notifModel.isSentEmailSuccess())
				|| (status == JobStatusEnum.FAILED && notifModel.isSentEmailFailed());

		if (!isSentEmail) {
			return;
		}

		String recipients = (status == JobStatusEnum.SUCCESS) ? notifModel.getEmailSuccess()
				: notifModel.getEmailFailed();

		if (Validator.isNull(recipients)) {
			_log.warn("Recipient email is empty for scheduler: {}", schedulerCode);
			return;
		}

		Date createdDate = _commonUtil.toCreatedDate(date);
		long notifRelationId = _schedulerMgmtServiceImpl.getSchedulerId(schedulerCode);
		long notificationId = _notificationImpl.upsertNotification(String.valueOf(notifRelationId), null, null, null,
				null, createdDate, Constants.SCHEDULER_MANAGEMENT_TABLE_NAME);

		_mailServiceImpl.sendJobNotificationEmail(status, notificationId, jobName, date, logMsg, recipients);
	}

}

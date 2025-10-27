package akses.ksei.co.id.investor.controller;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import akses.ksei.co.id.investor.i18n.I18nUtil;
import akses.ksei.co.id.investor.service.BatchImpl;
import akses.ksei.co.id.investor.service.BatchOrchestratorService;
import akses.ksei.co.id.investor.service.BatchStopHistoryLogger;
import akses.ksei.co.id.investor.service.JobStopFlagService;
import akses.ksei.co.id.investor.service.JobStopperService;
import akses.ksei.co.id.investor.service.ParameterImpl;
import akses.ksei.co.id.investor.service.SchedulerMgmtServiceImpl;
import akses.ksei.co.id.investor.task.DynamicBatchScheduler;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.DateUtil;
import akses.ksei.co.id.investor.util.ResponseUtil;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/investor")
public class BatchController {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	// private String SCHEDULER_CODE = Constants.PULL_DATA_INVESTOR_CODE;

	private final ParameterImpl _parameterImpl;

	private final SchedulerMgmtServiceImpl _schedulerMgmtServiceImpl;

	private final DynamicBatchScheduler _dynamicBatchScheduler;

	private final BatchOrchestratorService _batchOrchestratorService;

	private final BatchStopHistoryLogger _batchStopHistoryLogger;

	private final JobStopperService _jobStopperService;

	private final JobStopFlagService _jobStopFlagService;

	private final BatchImpl _batchImpl;

	private final I18nUtil _i18nUtil;

	private final ResponseUtil _responseUtil;

	@PostMapping("/pull_data")
	public ResponseEntity<Map<String, Object>> pullData(@RequestBody Map<String, Object> payload) {
		String schedulerCode = (String) payload.get("schedulerCode");
		String date = (String) payload.get("date");

		try {
			if (Validator.isNull(schedulerCode)) {
				return _responseUtil.badRequest(_i18nUtil.getMessage("missing.code", ""));
			}

			_batchOrchestratorService.runBatch(schedulerCode, date);

			return _responseUtil.success(_i18nUtil.getMessage("success.executed.job", ""));
		} catch (Exception e) {
			_log.error("Unexpected error: ", e);
			return _responseUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					_i18nUtil.getMessage("failed.running.job", ""));
		}
	}

	@PostMapping("/run_batch_job")
	public ResponseEntity<Map<String, Object>> runBatchJobByDate(@RequestBody Map<String, Object> payload) {
		String jobName = Constants.JOBS_NAME;
		String schedulerCode = (String) payload.get("schedulerCode");
		String date = (String) payload.get("date");
		boolean runNow = Optional.ofNullable(payload.get("runNow")).map(Object::toString).map(Boolean::parseBoolean)
				.orElse(false);

		try {
			if (Validator.isNull(schedulerCode)) {
				return _responseUtil.badRequest(_i18nUtil.getMessage("missing.code", ""));
			}

			if (Validator.isNull(date)) {
				date = _parameterImpl.getDate();
			}

			// validate date format
			if (!DateUtil.isValid(date, "dd/MM/yyyy")) {
				String failedMessage = _batchImpl.formatFailedMessage(jobName, date, "Invalid date format");
				_log.warn("[schedulerCode={}] Invalid date format: {}", schedulerCode, date);

				_schedulerMgmtServiceImpl.logInactiveSchedulerHistory(schedulerCode, date, failedMessage);
				return _responseUtil.badRequest(_i18nUtil.getMessage("invalid.date.format", ""));
			}

			if (!_schedulerMgmtServiceImpl.isRegistered(schedulerCode)) {
				_log.warn("Job with name '{}' not found in registry", schedulerCode);

				return _responseUtil.badRequest(_i18nUtil.getMessage("scheduler.not.registered", "") + schedulerCode);
			}

			if (!_schedulerMgmtServiceImpl.isActive(schedulerCode)) {
				_log.warn("[schedulerCode={}] Scheduler is not active", schedulerCode);

				String failedMessage = _i18nUtil.getMessage("scheduler.not.active");
				_schedulerMgmtServiceImpl.logInactiveSchedulerHistory(schedulerCode, date, failedMessage);

				return _responseUtil.badRequest(failedMessage);
			}

			String cronExpression = _schedulerMgmtServiceImpl.getCronExpression(schedulerCode);
			String regularDate = _parameterImpl.getDate();

			// update and reschedule
			_dynamicBatchScheduler.updateCronAndSchedule(schedulerCode, regularDate, cronExpression);

			if (runNow) {
				String execDate = Validator.isNotNull(date) ? date : regularDate;

				_log.info("[schedulerCode={}] Running batch immediately", schedulerCode);
				_dynamicBatchScheduler.runNow(schedulerCode, execDate);
			}

			String message = runNow ? _i18nUtil.getMessage("batch.job.triggered.and.scheduled", "")
					: _i18nUtil.getMessage("batch.job.scheduled");

			return _responseUtil.success(message);
		} catch (Exception e) {
			_log.error("[schedulerCode={}] Unexpected error while running batch job: {}", schedulerCode, e.getMessage(),
					e);

			try {
				String failedMessage = _batchImpl.formatFailedMessage(jobName, date, e.getMessage());

				_schedulerMgmtServiceImpl.logInactiveSchedulerHistory(schedulerCode, date, failedMessage);
			} catch (Exception logEx) {
				_log.error("[schedulerCode={}] Failed to log scheduler history: {}", schedulerCode, logEx.getMessage());
			}

			return _responseUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					_i18nUtil.getMessage("failed.running.job", ""));
		}
	}

	@PostMapping("/stop_batch_job")
	public ResponseEntity<Map<String, Object>> stopScheduler(@RequestBody Map<String, Object> payload) {
		_jobStopFlagService.setStop();

		String schedulerCode = (String) payload.get("schedulerCode");
		String date = (String) payload.get("date");

		if (Validator.isNull(schedulerCode)) {
			return _responseUtil.badRequest(_i18nUtil.getMessage("missing.code", ""));
		}

		if (Validator.isNull(date)) {
			date = _parameterImpl.getDate();
		}

		try {
			_log.info("[schedulerCode={}] Stopping scheduled batch job", schedulerCode);
			_dynamicBatchScheduler.stopScheduler(schedulerCode);

			// Stop Spring Batch job yang sedang berjalan
			if (!_schedulerMgmtServiceImpl.isRegistered(schedulerCode)) {
				_log.warn("Job with name '{}' not found in registry", schedulerCode);

				return _responseUtil.badRequest(_i18nUtil.getMessage("scheduler.not.registered", "") + schedulerCode);
			}

			new Thread(() -> {
				try {
					_jobStopperService.stopRunningJobAsync(schedulerCode);
				} catch (Exception e) {
					_log.error("error when stop running job: {}", e.getMessage(), e);
				}
			}).start();

			long schedulerId = _schedulerMgmtServiceImpl.getSchedulerId(schedulerCode);
			_batchStopHistoryLogger.logStopHistory(schedulerId, date);

			return _responseUtil
					.success(_i18nUtil.getMessage("success.stop.batch.job", "") + " [Stopping in background]");
		} catch (Exception e) {
			_log.error("[schedulerCode={}] Unexpected error while stopping batch job: {}", schedulerCode,
					e.getMessage(), e);

			return _responseUtil.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
					_i18nUtil.getMessage("failed.stop.batch.job", e.getMessage()));
		}
	}

}

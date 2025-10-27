package akses.ksei.co.id.investor.service;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.enumeration.JobStatusEnum;
import akses.ksei.co.id.investor.model.EmailNotificationModel;
import akses.ksei.co.id.investor.model.SchedulerHistoryRequest;
import akses.ksei.co.id.investor.util.CommonServiceUtil;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.DateUtil;
import akses.ksei.co.id.investor.util.JdbcUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulerMgmtServiceImpl {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final ParameterImpl _parameterImpl;

	private final SequenceServiceImpl _sequenceServiceImpl;

	private final CommonServiceUtil _commonUtil;

	private final JdbcTemplate jdbcTemplate;

	public long getSchedulerId(String code) {
		String sql = sqlFindSchedulerId();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("code", code);

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		try {
			Long schedulerId = namedJdbcTemplate.queryForObject(sql, params, Long.class);
			return Optional.ofNullable(schedulerId).orElse(0L);
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No schedulerId found for code: {}", code);

			return 0L;
		} catch (DataAccessException e) {
			_log.error("Database error while fetching schedulerId: {}", e.getMessage(), e);

			return 0L;
		}
	}

	public int getJobStatus(String code) {
		String sql = sqlFindJobStatus();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("code", code);

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		try {
			Integer jobStatus = namedJdbcTemplate.queryForObject(sql, params, Integer.class);
			return Optional.ofNullable(jobStatus).orElse(0);
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No jobStatus found for code: {}", code);

			return -1;
		} catch (DataAccessException e) {
			_log.error("Database error while fetching jobStatus: {}", e.getMessage(), e);

			return -1;
		}
	}

	public boolean isActive(String code) {
		try {
			String sql = sqlFindIsActive();

			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("code", code);

			NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

			Integer result = namedJdbcTemplate.queryForObject(sql, params, Integer.class);
			return result != null && result == 1;
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No active scheduler found for code: {}", code);
			return Boolean.TRUE;
		} catch (DataAccessException e) {
			_log.error("Database error while fetching active scheduler: {}", e.getMessage(), e);
			return Boolean.TRUE;
		} catch (Exception e) {
			_log.error(e.getMessage(), e);
			return Boolean.TRUE;
		}
	}

	public boolean isRegistered(String code) {

		return getSchedulerId(code) > 0;
	}

	public String getCronExpression(String code) {
		String sql = sqlFindCronExpression();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("code", code);

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		try {
			return namedJdbcTemplate.queryForObject(sql, params, String.class);
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No cronExpression found for code: {}", code);

			return _parameterImpl.getCronExpression();
		} catch (DataAccessException e) {
			_log.error("Database error while fetching cronExpression: {}", e.getMessage(), e);

			return _parameterImpl.getCronExpression();
		}
	}

	public EmailNotificationModel getEmailNotification(String code) {
		String sql = sqlEmailNotification();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("code", code);

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		try {
			Map<String, Object> result = namedJdbcTemplate.queryForMap(sql, params);

			boolean sentSuccess = _commonUtil.toBoolean(result.get("SENT_EMAIL_SUCCESS"), false);
			boolean sentFailed = _commonUtil.toBoolean(result.get("SENT_EMAIL_FAILED"), false);
			String emailSuccess = _commonUtil.toString(result.get("EMAIL_SUCCESS"));
			String emailFailed = _commonUtil.toString(result.get("EMAIL_FAILED"));

			return new EmailNotificationModel(sentSuccess, sentFailed, emailSuccess, emailFailed);
		} catch (EmptyResultDataAccessException e) {
			return new EmailNotificationModel(false, false, null, null);
		}
	}

	public long getHistoryIdByKey(long schedulerId, Date createdDate) {
		if (jdbcTemplate == null) {
			_log.error("JdbcTemplate is null in getHistoryIdByKey");
			return 0L;
		}

		java.sql.Date sqlDate = DateUtil.safeDate(createdDate);

		if (sqlDate == null) {
			_log.warn("getHistoryIdByKey called with null or invalid date for schedulerId: {}", schedulerId);
			return 0L;
		}

		String sql = sqlFindHistoryIdByKey();

		return JdbcUtil.safeQueryForLong(jdbcTemplate, sql, ps -> {
			ps.setLong(1, schedulerId);
			ps.setDate(2, sqlDate);
		}, String.format("getHistoryIdByKey(schedulerId=%d, createdDate=%s)", schedulerId, sqlDate));
	}

	public int getStatus(String batchStatus) {

		return JobStatusEnum.fromBatchStatus(batchStatus);
	}

	public long upsertSchedulerHistory(SchedulerHistoryRequest request) {
		try {
			Timestamp modifiedDate = new Timestamp(System.currentTimeMillis());

			long historyId = getHistoryIdByKey(request.schedulerId(), request.createdDate());

			if (historyId > 0) {
				// record exists, do update
				String updateSql = sqlUpdateSchedulerHistory();

				jdbcTemplate.update(updateSql, ps -> {
					int i = 1;

					_commonUtil.setNullableField(ps, i++, request.startProcess(), Types.TIMESTAMP); // start_process
					_commonUtil.setNullableField(ps, i++, request.endProcess(), Types.TIMESTAMP); // end_process
					ps.setInt(i++, getStatus(request.batchStatus())); // status
					_commonUtil.setNullableField(ps, i++, request.failedMessage(), Types.VARCHAR); // failed_message
					_commonUtil.setNullableField(ps, i++, request.successMessage(), Types.VARCHAR); // success_message
					ps.setLong(i++, 0);// modified_by
					_commonUtil.setNullableField(ps, i++, modifiedDate, Types.TIMESTAMP); // modified_date

					ps.setLong(i++, historyId); // history_id
				});

				return historyId;
			} else {
				// insert baru
				return insertSchedulerHistory(request.schedulerId(), request.startProcess(), request.endProcess(),
						request.batchStatus(), request.failedMessage(), request.successMessage(),
						request.parameterInput());
			}
		} catch (Exception e) {
			// _log.error("Failed to upsert scheduler history. Error: {}", e.getMessage(),
			// e);
			throw new IllegalStateException("Failed to upsert scheduler history. Error:", e);
		}
	}

	public long insertSchedulerHistory(long schedulerId, Date startProcess, Date endProcess, String batchStatus,
			String failedMessage, String successMessage, String parameterInput) {
		try {
			String sql = sqlInsertSchedulerHistory();

			Timestamp now = new Timestamp(new Date().getTime());

			long historyId = _sequenceServiceImpl.getNextId(Constants.SCHEDULER_HISTORY_SEQUENCE_NAME);

			jdbcTemplate.update(sql, ps -> {
				int i = 1;

				ps.setLong(i++, historyId); // id
				ps.setLong(i++, schedulerId); // scheduler_id
				_commonUtil.setNullableField(ps, i++, startProcess, Types.TIMESTAMP); // start_process
				_commonUtil.setNullableField(ps, i++, endProcess, Types.TIMESTAMP); // end_process
				ps.setInt(i++, getStatus(batchStatus)); // status
				_commonUtil.setNullableField(ps, i++, failedMessage, Types.VARCHAR); // failed_message
				_commonUtil.setNullableField(ps, i++, successMessage, Types.VARCHAR); // success_message
				_commonUtil.setNullableField(ps, i++, parameterInput, Types.VARCHAR); // parameter_input
				ps.setLong(i++, 0);// created_by
				_commonUtil.setNullableField(ps, i++, now, Types.TIMESTAMP); // created_date
				ps.setLong(i++, 0);// modified_by
				_commonUtil.setNullableField(ps, i++, now, Types.TIMESTAMP); // modified_date
			});

			_log.info("Inserted scheduler history with ID: {}", historyId);
			return historyId;
		} catch (Exception e) {
			// _log.error("Failed to insert scheduler history. Error: {}", e.getMessage(),
			// e);
			throw new IllegalStateException("Failed to insert scheduler history. Error:", e);
		}
	}

	public void updateSchedulerHistoryOnEnd(long historyId, Date startProcess, Date endProcess, String batchStatus,
			String failedMessage, String successMessage) {

		Timestamp modifiedDate = new Timestamp(System.currentTimeMillis());

		int status = getStatus(batchStatus);

		String updateSql = sqlUpdateSchedulerHistory();

		jdbcTemplate.update(updateSql, ps -> {
			int i = 1;

			_commonUtil.setNullableField(ps, i++, startProcess, Types.TIMESTAMP); // start_process
			_commonUtil.setNullableField(ps, i++, endProcess, Types.TIMESTAMP); // end_process
			ps.setInt(i++, status); // status
			_commonUtil.setNullableField(ps, i++, failedMessage, Types.VARCHAR); // failed_message
			_commonUtil.setNullableField(ps, i++, successMessage, Types.VARCHAR); // success_message
			ps.setLong(i++, 0);// modified_by
			_commonUtil.setNullableField(ps, i++, modifiedDate, Types.TIMESTAMP); // modified_date

			ps.setLong(i++, historyId); // history_id
		});

		_log.info("Updated scheduler history {} with status: {}", historyId, status);
	}

	public void logInactiveSchedulerHistory(String code, String date, String message) {
		long schedulerId = getSchedulerId(code);
		Date startTime = new Date();
		insertSchedulerHistory(schedulerId, startTime, startTime, BatchStatus.FAILED.name(), message, null, date);
	}

	public static String sqlFindSchedulerId() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT SCHEDULER_ID ");
		query.append(" FROM ksei_scheduler_management ");
		query.append(" WHERE ");
		query.append(" CODE = :code ");
		query.append(" FETCH FIRST 1 ROWS ONLY ");

		return query.toString();
	}

	public static String sqlFindIsActive() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" STATUS AS IS_ACTIVE ");
		query.append(" FROM ksei_scheduler_management ");
		query.append(" WHERE ");
		query.append(" CODE = :code ");
		query.append(" FETCH FIRST 1 ROWS ONLY ");

		return query.toString();
	}

	public static String sqlFindCronExpression() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT CRON_EXPRESSION ");
		query.append(" FROM ksei_scheduler_management ");
		query.append(" WHERE ");
		query.append(" CODE = :code ");
		query.append(" FETCH FIRST 1 ROWS ONLY ");

		return query.toString();
	}

	public static String sqlFindHistoryIdByKey() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT HISTORY_ID ");
		query.append(" FROM ksei_scheduler_history ");
		query.append(" WHERE ");
		query.append(" SCHEDULER_ID = ? ");
		query.append(" AND TRUNC(CREATED_DATE) = ? ");
		query.append(" ORDER BY CREATED_DATE DESC ");
		query.append(" FETCH FIRST 1 ROWS ONLY ");

		return query.toString();
	}

	public static String sqlInsertSchedulerHistory() {
		StringBuilder query = new StringBuilder();
		query.append(" insert into ksei_scheduler_history (");
		query.append(" history_id, ");
		query.append(" scheduler_id, ");
		query.append(" start_process, ");
		query.append(" end_process, ");
		query.append(" status, ");
		query.append(" failed_message, ");
		query.append(" success_message, ");
		query.append(" parameter_input, ");
		query.append(" created_by, ");
		query.append(" created_date, ");
		query.append(" modified_by, ");
		query.append(" modified_date ");
		query.append(" ) VALUES ( ");
		query.append("?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ");
		query.append(" ) ");

		return query.toString();
	}

	public static String sqlUpdateSchedulerHistory() {
		StringBuilder query = new StringBuilder();
		query.append("UPDATE ksei_scheduler_history SET ");
		query.append("start_process = ?, ");
		query.append("end_process = ?, ");
		query.append("status = ?, ");
		query.append("failed_message = ?, ");
		query.append("success_message = ?, ");
		query.append("modified_by = ?, ");
		query.append("modified_date = ? ");
		query.append("WHERE history_id = ?");

		return query.toString();
	}

	public static String sqlEmailNotification() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" IS_SUCCESS_NOTIF AS SENT_EMAIL_SUCCESS, ");
		query.append(" IS_FAILED_NOTIF AS SENT_EMAIL_FAILED, ");
		query.append(" EMAIL_SUCCESS, ");
		query.append(" EMAIL_FAILED ");
		query.append(" FROM ksei_scheduler_management ");
		query.append(" WHERE ");
		query.append(" CODE = :code ");
		query.append(" FETCH FIRST 1 ROWS ONLY ");

		return query.toString();
	}

	public static String sqlFindJobStatus() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" recent_hist.STATUS AS JOB_STATUS ");
		query.append(" FROM ksei_scheduler_management mgt ");
		query.append(" LEFT JOIN ( ");
		query.append(" SELECT h1.* ");
		query.append(" FROM ksei_scheduler_history h1 ");
		query.append(" JOIN ( ");
		query.append(" SELECT SCHEDULER_ID, MAX(START_PROCESS) AS MAX_START ");
		query.append(" FROM ksei_scheduler_history ");
		query.append(" GROUP BY SCHEDULER_ID ");
		query.append(" ) h2 ON h1.SCHEDULER_ID = h2.SCHEDULER_ID AND h1.START_PROCESS = h2.MAX_START ");
		query.append(" ) recent_hist ON recent_hist.SCHEDULER_ID = mgt.SCHEDULER_ID ");
		query.append(" WHERE ");
		query.append(" mgt.CODE = :code ");
		query.append(" FETCH FIRST 1 ROWS ONLY ");

		return query.toString();
	}

}

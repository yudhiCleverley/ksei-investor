package akses.ksei.co.id.investor.service;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.util.CommonServiceUtil;
import akses.ksei.co.id.investor.util.Constants;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationImpl {

	// private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final CommonServiceUtil _commonUtil;

	private final SequenceServiceImpl _sequenceServiceImpl;

	private final JdbcTemplate jdbcTemplate;

	public long upsertNotification(String notifRelationId, Date sendEmailTime, Date sendInboxTime, String emailStatus,
			String inboxStatus, Date createdDate, String sourceTable) {

		try {
			String selectSql = sqlSelectNotificationByKey();

			List<Long> existing = jdbcTemplate.query(selectSql, ps -> {
				int i = 1;
				ps.setString(i++, notifRelationId);
				ps.setString(i++, sourceTable);
				ps.setDate(i++, new java.sql.Date(DateUtils.truncate(createdDate, Calendar.DATE).getTime()));
			}, (rs, rowNum) -> rs.getLong("notification_id"));

			if (!existing.isEmpty()) {
				// record exists, do update
				String updateSql = sqlUpdateNotification();

				jdbcTemplate.update(updateSql, ps -> {
					int i = 1;

					_commonUtil.setNullableField(ps, i++, sendEmailTime, Types.TIMESTAMP); // sending_email_time
					_commonUtil.setNullableField(ps, i++, null, Types.TIMESTAMP); // sending_wa_time
					_commonUtil.setNullableField(ps, i++, sendInboxTime, Types.TIMESTAMP); // send_inbox_time
					_commonUtil.setNullableField(ps, i++, emailStatus, Types.VARCHAR); // status_email
					_commonUtil.setNullableField(ps, i++, null, Types.VARCHAR); // status_wa
					_commonUtil.setNullableField(ps, i++, inboxStatus, Types.VARCHAR); // status_inbox

					_commonUtil.setNullableField(ps, i++, notifRelationId, Types.VARCHAR); // notification_relation_id
					_commonUtil.setNullableField(ps, i++, sourceTable, Types.VARCHAR); // source_table
					ps.setDate(i++, new java.sql.Date(DateUtils.truncate(createdDate, Calendar.DATE).getTime())); // created_date
				});

				return existing.get(0);
			} else {
				return insertNotification(notifRelationId, sendEmailTime, sendInboxTime, emailStatus, inboxStatus,
						sourceTable);
			}
		} catch (Exception e) {
			// _log.error("Failed to upsert notification. Error: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to upsert notification. Error:", e);
		}
	}

	public int updateEmailNotification(long notificationId, Date sendEmailTime, String emailStatus) {
		try {
			String updateSql = sqlUpdateEmailNotificationById();

			return jdbcTemplate.update(updateSql, ps -> {
				int i = 1;

				_commonUtil.setNullableField(ps, i++, sendEmailTime, Types.TIMESTAMP); // sending_email_time
				_commonUtil.setNullableField(ps, i++, emailStatus, Types.VARCHAR); // status_email

				ps.setLong(i++, notificationId); // id
			});
		} catch (Exception e) {
			// _log.error("Failed to update email notification. Error: {}", e.getMessage(),
			// e);
			throw new IllegalStateException("Failed to update email notification. Error:", e);
		}
	}

	public long insertNotification(String notifRelationId, Date sendEmailTime, Date sendInboxTime, String emailStatus,
			String inboxStatus, String sourceTable) {
		try {
			String sql = sqlInsertNotification();

			Timestamp now = new Timestamp(new Date().getTime());

			long id = _sequenceServiceImpl.getNextId(Constants.NOTIFICATION_SEQUENCE_NAME);

			jdbcTemplate.update(sql, ps -> {
				int i = 1;

				ps.setLong(i++, id); // id
				_commonUtil.setNullableField(ps, i++, notifRelationId, Types.VARCHAR); // notification_relation_id
				_commonUtil.setNullableField(ps, i++, sendEmailTime, Types.TIMESTAMP); // sending_email_time
				_commonUtil.setNullableField(ps, i++, null, Types.TIMESTAMP); // sending_wa_time
				_commonUtil.setNullableField(ps, i++, sendInboxTime, Types.TIMESTAMP); // send_inbox_time
				_commonUtil.setNullableField(ps, i++, emailStatus, Types.VARCHAR); // status_email
				_commonUtil.setNullableField(ps, i++, null, Types.VARCHAR); // status_wa
				_commonUtil.setNullableField(ps, i++, inboxStatus, Types.VARCHAR); // status_inbox
				_commonUtil.setNullableField(ps, i++, now, Types.TIMESTAMP); // created_date
				_commonUtil.setNullableField(ps, i++, sourceTable, Types.VARCHAR); // source_table
			});

			return id;
		} catch (Exception e) {
			// _log.error("Failed to insert notification. Error: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to insert notification. Error:", e);
		}
	}

	public int upsertEmailHistory(long notificationId, String from, String to, String cc, String bcc, String subject,
			String body) {

		try {
			String selectSql = sqlSelectHistoryEmailByKey();

			List<Long> existing = jdbcTemplate.query(selectSql, ps -> {
				int i = 1;
				ps.setLong(i++, notificationId);
			}, (rs, rowNum) -> rs.getLong("email_code"));

			if (!existing.isEmpty()) {
				// record exists, do update
				String updateSql = sqlUpdateHistoryEmail();

				return jdbcTemplate.update(updateSql, ps -> {
					int i = 1;

					_commonUtil.setNullableField(ps, i++, from, Types.VARCHAR); // from_address
					_commonUtil.setNullableField(ps, i++, to, Types.VARCHAR); // to_address
					_commonUtil.setNullableField(ps, i++, cc, Types.VARCHAR); // cc
					_commonUtil.setNullableField(ps, i++, bcc, Types.VARCHAR); // bcc
					_commonUtil.setNullableField(ps, i++, subject, Types.VARCHAR); // subject
					_commonUtil.setNullableField(ps, i++, body, Types.VARCHAR); // body

					ps.setLong(i++, notificationId); // notification_id
				});
			} else {
				return insertEmailHistory(notificationId, from, to, cc, bcc, subject, body);
			}
		} catch (Exception e) {
			// _log.error("Failed to upsert email history. Error: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to upsert email history. Error:", e);
		}
	}

	public int insertEmailHistory(long notificationId, String from, String to, String cc, String bcc, String subject,
			String body) {
		try {
			String sql = sqlInsertHistoryEmail();

			long id = _sequenceServiceImpl.getNextId(Constants.HISTORY_EMAIL_SEQUENCE_NAME);

			return jdbcTemplate.update(sql, ps -> {
				int i = 1;

				ps.setLong(i++, id); // id
				ps.setLong(i++, notificationId); // notification_id
				_commonUtil.setNullableField(ps, i++, from, Types.VARCHAR); // from_address
				_commonUtil.setNullableField(ps, i++, to, Types.VARCHAR); // to_address
				_commonUtil.setNullableField(ps, i++, cc, Types.VARCHAR); // cc
				_commonUtil.setNullableField(ps, i++, bcc, Types.VARCHAR); // bcc
				_commonUtil.setNullableField(ps, i++, subject, Types.VARCHAR); // subject
				_commonUtil.setNullableField(ps, i++, body, Types.VARCHAR); // body
			});
		} catch (Exception e) {
			// _log.error("Failed to insert email history. Error: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to insert email history. Error:", e);
		}
	}

	public Long getNextNotificationId() {
		String sql = sqlNextNotificationId();

		return jdbcTemplate.queryForObject(sql, Long.class);
	}

	// ksei_notification
	public static String sqlNextNotificationId() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT NVL(MAX(NOTIFICATION_ID), 0) + 1 FROM ksei_notification ");
		return query.toString();
	}

	public static String sqlSelectNotificationByKey() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT notification_id ");
		query.append(" FROM ksei_notification ");
		query.append(" WHERE ");
		query.append(" notification_relation_id = ? ");
		query.append(" AND source_table = ? ");
		query.append(" AND TRUNC(created_date) = ? ");

		return query.toString();
	}

	public String sqlInsertNotification() {
		StringBuilder query = new StringBuilder();
		query.append(" INSERT INTO ksei_notification ( ");
		query.append(" notification_id, ");
		query.append(" notification_relation_id, ");
		query.append(" sending_email_time, ");
		query.append(" sending_wa_time, ");
		query.append(" sending_inbox_time, ");
		query.append(" status_email, ");
		query.append(" status_wa, ");
		query.append(" status_inbox, ");
		query.append(" created_date, ");
		query.append(" source_table ");
		query.append(" ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

		return query.toString();
	}

	public String sqlUpdateNotification() {
		StringBuilder query = new StringBuilder();
		query.append(" UPDATE ksei_notification SET ");
		query.append(" sending_email_time = ?, ");
		query.append(" sending_wa_time = ?, ");
		query.append(" sending_inbox_time = ?, ");
		query.append(" status_email = ?, ");
		query.append(" status_wa = ?, ");
		query.append(" status_inbox = ? ");
		query.append(" WHERE ");
		query.append(" notification_relation_id = ? ");
		query.append(" AND source_table = ? ");
		query.append(" AND TRUNC(created_date) = ? ");

		return query.toString();
	}

	public String sqlUpdateEmailNotificationById() {
		StringBuilder query = new StringBuilder();
		query.append(" UPDATE ksei_notification SET ");
		query.append(" sending_email_time = ?, ");
		query.append(" status_email = ? ");
		query.append(" WHERE ");
		query.append(" notification_id = ? ");

		return query.toString();
	}

	// ksei_history_email
	public static String sqlSelectHistoryEmailByKey() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT email_code ");
		query.append(" FROM ksei_history_email ");
		query.append(" WHERE notification_id = ? ");

		return query.toString();
	}

	public String sqlInsertHistoryEmail() {
		StringBuilder query = new StringBuilder();
		query.append(" INSERT INTO ksei_history_email ( ");
		query.append(" email_code, ");
		query.append(" notification_id, ");
		query.append(" from_address, ");
		query.append(" to_address, ");
		query.append(" cc, ");
		query.append(" bcc, ");
		query.append(" subject, ");
		query.append(" body ");
		query.append(" ) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ");

		return query.toString();
	}

	public String sqlUpdateHistoryEmail() {
		StringBuilder query = new StringBuilder();
		query.append(" UPDATE ksei_history_email SET ");
		query.append(" from_address = ?, ");
		query.append(" to_address = ?, ");
		query.append(" cc = ?, ");
		query.append(" bcc = ?, ");
		query.append(" subject = ?, ");
		query.append(" body = ? ");
		query.append(" WHERE notification_id = ? ");

		return query.toString();
	}

}

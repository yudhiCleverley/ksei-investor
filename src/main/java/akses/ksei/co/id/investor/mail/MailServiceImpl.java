package akses.ksei.co.id.investor.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import akses.ksei.co.id.investor.enumeration.JobStatusEnum;
import akses.ksei.co.id.investor.enumeration.Status;
import akses.ksei.co.id.investor.service.NotificationImpl;
import akses.ksei.co.id.investor.service.ParameterImpl;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailServiceImpl {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private static final String _templateEmailSuccessRuningJob = "email.success.running.job";

	private static final String _templateEmailFailedRuningJob = "failed.success.running.job";

	private String LOGO = "logo";

	private final MailUtil _mailUtil;

	private final MailProperties _mailProperties;

	private final ParameterImpl _parameterImpl;

	private final NotificationImpl _notificationImpl;

	private final JdbcTemplate jdbcTemplate;

	public List<MailTemplateModel> getMailTemplates(String code) {

		String sqlQuery = MailTemplateQueryUtil.sqlMailTemplates();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("code", code);

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		try {
			return namedJdbcTemplate.query(sqlQuery, params, (rs, rowNum) -> {
				MailTemplateModel model = new MailTemplateModel();
				model.setCode(rs.getString("code"));
				model.setSubject(rs.getString("subject"));
				model.setContent(rs.getString("content"));
				return model;
			});
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No mail template found for code: {}", code);
			return Collections.emptyList();
		} catch (DataAccessException e) {
			// _log.error("DB error fetching mail template for code {}: {}", code,
			// e.getMessage(), e);
			throw e;
		}
	}

	public Map<String, String> getMailTemplateMap(String code) {
		String sql = MailTemplateQueryUtil.sqlMailTemplate();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("code", code);

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		try {
			return namedJdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
				Map<String, String> result = new HashMap<>();
				result.put("subject", rs.getString("subject"));
				result.put("content", rs.getString("content"));
				return result;
			});
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No template found for code: {}", code);
			return Collections.emptyMap();
		} catch (Exception e) {
			_log.error("Failed to get mail template for code {}: {}", code, e.getMessage(), e);
			return Collections.emptyMap();
		}
	}

	public String getSubject(String code) {
		try {
			Map<String, String> templateMap = getMailTemplateMap(code);
			return templateMap.getOrDefault("subject", null);
		} catch (Exception e) {
			_log.error("Error getting subject for code {}: {}", code, e.getMessage(), e);
			return null;
		}
	}

	public String getContent(String code) {
		try {
			Map<String, String> templateMap = getMailTemplateMap(code);
			return templateMap.getOrDefault("content", null);
		} catch (Exception e) {
			_log.error("Error getting content for code {}: {}", code, e.getMessage(), e);
			return null;
		}
	}

	public String getMimeType(String base64File) throws IOException {
		// Decode base64 string to bytes
		byte[] fileBytes = Base64.getDecoder().decode(base64File);

		// Use Apache Tika to detect the file type
		Tika tika = new Tika();
		return tika.detect(new ByteArrayInputStream(fileBytes));
	}

	public String[] getRecipients(String recipients) {
		try {

			if (Validator.isNotNull(recipients)) {
				return _mailUtil.cleanEmailList(recipients);
			}

			_log.info("recipients : {}", recipients);
		} catch (Exception e) {
			_log.error("Error while fetching email recipients: {}", e.getMessage(), e);
		}

		return new String[0];
	}

	public MailModel getMailRequest(String recipients, String subject, String body, Boolean isHtml, String base64Logo,
			String fileExtention, MultipartFile[] attachments) {

		return new MailModel(getRecipients(recipients), subject, body, isHtml, base64Logo, fileExtention, attachments);
	}

	public void sendJobNotificationEmail(JobStatusEnum status, long notificationId, String jobName, String date,
			String logMsg, String recipients) {
		try {
			String subject;
			String content;

			if (status == JobStatusEnum.SUCCESS) {
				subject = getSubject(_templateEmailSuccessRuningJob);
				content = getContent(_templateEmailSuccessRuningJob);
			} else {
				subject = getSubject(_templateEmailFailedRuningJob);
				content = getContent(_templateEmailFailedRuningJob);
			}

			if (Validator.isNull(subject)) {
				throw new IllegalArgumentException(
						"Email template subject is missing for " + status.name().toLowerCase() + " job notification.");
			}

			if (Validator.isNull(content)) {
				throw new IllegalArgumentException(
						"Email template content is missing for " + status.name().toLowerCase() + " job notification.");
			}

			subject = subject + " " + jobName + " - " + date;

			Map<String, Object> variables = new HashMap<>();
			variables.put("jobName", jobName);
			variables.put("date", date);
			variables.put("logMsg", logMsg);
			variables.put(LOGO, "cid:" + LOGO);

			// render HTML from template
			String body = TemplateRenderer.render(content, variables);

			MailModel mailRequest = getMailRequest(recipients, subject, body, Boolean.TRUE, _parameterImpl.getLogo(),
					getMimeType(_parameterImpl.getLogo()), null);
			_mailUtil.sendMail(mailRequest);

			// upsert email history
			if (notificationId > 0) {
				_notificationImpl.upsertEmailHistory(notificationId, _mailProperties.getSender(), recipients, null,
						null, subject, body);
				_notificationImpl.updateEmailNotification(notificationId, new Date(), Status.SENT.name());
			}
		} catch (Exception e) {
			_log.error("Failed to send {} job notification email for job '{}', date '{}': {}",
					status.name().toLowerCase(), jobName, date, e.getMessage(), e);

			if (notificationId > 0) {
				_notificationImpl.updateEmailNotification(notificationId, new Date(), Status.FAILED.name());
			}
		}
	}

}

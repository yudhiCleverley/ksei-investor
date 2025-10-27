package akses.ksei.co.id.investor.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.model.ParameterModel;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.QueryUtil;
import akses.ksei.co.id.investor.util.StringPool;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParameterImpl {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private static String dfPattern = "dd/MM/yyyy";
	private static final DateTimeFormatter df = DateTimeFormatter.ofPattern(dfPattern);

	@Value("${task.schedule.cron}")
	private String cronExpression;

	private final JdbcTemplate jdbcTemplate;

	public List<ParameterModel> getParameters(String groupCode, String paramCode) throws Exception {

		String sqlQuery = QueryUtil.sqlParameter(groupCode, paramCode);

		NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		return namedTemplate.query(sqlQuery, (rs, rowNum) -> {
			return new ParameterModel(rs.getString("code"), rs.getString("param_value"));
		});

	}

	public String getParamValue(String groupCode, String paramCode) throws Exception {
		List<ParameterModel> parameters = getParameters(groupCode, paramCode);

		if (!parameters.isEmpty()) {

			return parameters.get(0).getParamValue();
		}

		return StringPool.BLANK;
	}

	// Retrieve the current cron expression (for verification purposes)
	public String getCronExpression() {
		try {
			String cronExp = getParamValue(Constants.SCHEDULER_SETTINGS_GROUP_CODE,
					Constants.TRANSFER_DATA_CRON_EXPRESSION_CODE);

			return Validator.isNotNull(cronExp) ? cronExp : cronExpression;
		} catch (Exception e) {
			_log.error("failed get cron expression from parameter: {}", e.getMessage(), e);

			return cronExpression;
		}
	}

	public String getDate() {
		String date = LocalDate.now().format(df);

		try {
			String paramDate = getParamValue(Constants.SCHEDULER_SETTINGS_GROUP_CODE,
					Constants.TRANSFER_DATA_IVESTOR_DATE_CODE);

			if (Validator.isNotNull(paramDate) && !paramDate.equals(Constants.TRANSFER_DATA_IVESTOR_DATE_DEFAULT_VALUE)
					&& isValidDate(paramDate)) {
				date = paramDate;
			}
		} catch (Exception e) {
			_log.error("Error while fetching parameter value: {}", e.getMessage(), e);
		}

		return date;
	}

	public String getLogo() {
		try {
			return getParamValue(Constants.EMAIL_SETTINGS_GROUP_CODE, Constants.EMAIL_LOGO_CODE);
		} catch (Exception e) {
			_log.error("error while fetching getLogo: {}", e.getMessage(), e);
			return null;
		}
	}

	public String getPortalWeb() {
		try {
			return getParamValue(Constants.SITE_SETTINGS_GROUP_CODE, Constants.DEFAULT_PORTAL_WEB_CODE);
		} catch (Exception e) {
			_log.error("error while fetching getPortalWeb: {}", e.getMessage(), e);
			return null;
		}
	}

	public boolean isValidDate(String dateStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dfPattern);

		try {
			LocalDate.parse(dateStr, formatter);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

}

package akses.ksei.co.id.investor.service;

import java.sql.Timestamp;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.model.AuditRequest;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.QueryUtil;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditTrailImpl {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final JdbcTemplate jdbcTemplate;

	public Long getNextIdFromSequence(String sequenceName) {
		if (!Constants.VALID_SEQUENCES.contains(sequenceName)) {
			throw new IllegalArgumentException("Invalid sequence name: " + sequenceName);
		}

		try {
			String sql = String.format("SELECT %s.NEXTVAL FROM DUAL", sequenceName.toUpperCase());

			return jdbcTemplate.queryForObject(sql, Long.class);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to fetch next value from sequence: " + e.getMessage(), e);
		}
	}

	public boolean isAuditIdExists(Long auditId) {
		try {
			String sql = "SELECT COUNT(1) FROM ksei_audittrail WHERE audit_id = :auditId";

			NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("auditId", auditId);

			Integer count = namedTemplate.queryForObject(sql, parameters, Integer.class);

			return count != null && count > 0;
		} catch (DataAccessException e) {
			_log.error("Error executing query to check auditId existence: {}", e.getMessage(), e);
			return false;
		}
	}

	public void createAuditTrail(AuditRequest request) throws Exception {
		String sql = QueryUtil.sqlInserAuditTrail();

		DefaultLobHandler lobHandler = new DefaultLobHandler();

		NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		Long auditId = getNextIdFromSequence(Constants.AUDIT_SEQUENCE_NAME);
		// _log.info("Generated auditId: " + auditId);

		if (isAuditIdExists(auditId)) {
			_log.warn("Audit ID {} already exists. Skipping insert.", auditId);
			return;
		}

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("auditId", auditId, Types.NUMERIC);
		params.addValue("auditType", request.auditType(), Types.INTEGER);
		params.addValue("moduleName", request.moduleName(), Types.VARCHAR);
		params.addValue("className", request.className(), Types.VARCHAR);
		params.addValue("primKey", "0", Types.VARCHAR);
		params.addValue("name", null, Types.VARCHAR);
		params.addValue("loginId", null, Types.VARCHAR);
		params.addValue("email", null, Types.VARCHAR);
		params.addValue("linkEmail", null, Types.VARCHAR);
		params.addValue("version", 0d, Types.DOUBLE);
		params.addValue("auditAction", request.auditAction(), Types.VARCHAR);
		params.addValue("auditActionBy", "0", Types.VARCHAR);
		params.addValue("auditActionDate", new Timestamp(request.auditActionDate().getTime()), Types.TIMESTAMP);
		params.addValue("clientIp", request.clientIp(), Types.VARCHAR);
		params.addValue("otpNumber", null, Types.VARCHAR);
		params.addValue("mobileNumber", null, Types.VARCHAR);
		params.addValue("sessionId", null, Types.VARCHAR);
		params.addValue("serverPort", request.serverPort(), Types.VARCHAR);
		if (Validator.isNotNull(request.additionalInfo())) {
			params.addValue("additionalInfo", new SqlLobValue(request.additionalInfo(), lobHandler), Types.CLOB);
		} else {
			params.addValue("additionalInfo", null, Types.CLOB);
		}
		params.addValue("createdBy", "0", Types.VARCHAR);
		params.addValue("createdDate", new Timestamp(System.currentTimeMillis()), Types.TIMESTAMP);
		params.addValue("modifiedBy", "0", Types.VARCHAR);
		params.addValue("modifiedDate", new Timestamp(System.currentTimeMillis()), Types.TIMESTAMP);

		try {
			namedTemplate.update(sql, params);
			// _log.info("Audit trail with ID {} inserted successfully.", auditId);
		} catch (Exception e) {
			_log.error("Failed to insert audit trail: {}", e.getMessage(), e);
		}
	}

}

package akses.ksei.co.id.investor.util;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class JdbcUtil {

	private static final Logger _log = LoggerFactory.getLogger(JdbcUtil.class);

	private JdbcUtil() {
		// prevent instantiation
	}

	// ============================================================
	// GENERIC SAFE QUERY METHOD
	// ============================================================
	/**
	 * Safely executes a queryForObject expecting a specific type.
	 *
	 * @param <T>          the return type
	 * @param jdbcTemplate the NamedParameterJdbcTemplate instance
	 * @param sql          the SQL query string
	 * @param params       the query parameters
	 * @param type         the expected return type
	 * @param defaultValue the value to return if query yields no result or fails
	 * @return the result value or defaultValue when not found or error
	 */
	public static <T> T safeQueryForObject(NamedParameterJdbcTemplate jdbcTemplate, String sql,
			MapSqlParameterSource params, Class<T> type, T defaultValue) {

		try {
			T result = jdbcTemplate.queryForObject(sql, params, type);
			return result != null ? result : defaultValue;

		} catch (EmptyResultDataAccessException e) {
			_log.warn("No result found for SQL [{}] with params: {}", sql, params);
			return defaultValue;

		} catch (DataAccessException e) {
			_log.error("Database error executing SQL [{}]: {}", sql, e.getMessage(), e);
			return defaultValue;

		} catch (Exception e) {
			_log.error("Unexpected error executing SQL [{}]: {}", sql, e.getMessage(), e);
			return defaultValue;
		}
	}

	// ============================================================
	// LONG VARIANT
	// ============================================================
	/**
	 * Safely executes a queryForObject expecting a Long result.
	 */
	public static long safeQueryForLong(NamedParameterJdbcTemplate jdbcTemplate, String sql,
			MapSqlParameterSource params, long defaultValue) {

		try {
			Long result = jdbcTemplate.queryForObject(sql, params, Long.class);
			return result != null ? result : defaultValue;
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No result found for SQL [{}] with params: {}", sql, params);
			return defaultValue;

		} catch (DataAccessException e) {
			_log.error("Database error executing SQL [{}]: {}", sql, e.getMessage(), e);
			return defaultValue;

		} catch (Exception e) {
			_log.error("Unexpected error executing SQL [{}]: {}", sql, e.getMessage(), e);
			return defaultValue;
		}
	}

	/**
	 * Safely execute a query that returns a single long value.
	 */
	public static long safeQueryForLong(JdbcTemplate jdbcTemplate, String sql, PreparedStatementSetter pss,
			String contextInfo) {

		if (jdbcTemplate == null) {
			_log.error("JdbcTemplate is null in safeQueryForLong()");
			return 0L;
		}

		if (sql == null || sql.isEmpty()) {
			_log.error("SQL is null or empty in safeQueryForLong()");
			return 0L;
		}

		if (pss == null) {
			_log.warn("PreparedStatementSetter is null in safeQueryForLong ({})", contextInfo);
			return 0L;
		}

		try {
			Long result = jdbcTemplate.query(sql, pss, rs -> {
				if (rs.next()) {
					return rs.getLong(1);
				}
				return 0L;
			});

			return Objects.requireNonNullElse(result, 0L);
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No result found for query [{}] - {}", sql, contextInfo);
			return 0L;
		} catch (DataAccessException e) {
			_log.error("Database error in safeQueryForLong ({}): {}", contextInfo, e.getMessage(), e);
			return 0L;
		} catch (Exception e) {
			_log.error("Unexpected error in safeQueryForLong ({}): {}", contextInfo, e.getMessage(), e);
			return 0L;
		}
	}

	// ============================================================
	// INT VARIANT
	// ============================================================
	/**
	 * Safely executes a queryForObject expecting an Integer result.
	 */
	public static int safeQueryForInteger(NamedParameterJdbcTemplate jdbcTemplate, String sql,
			MapSqlParameterSource params, int defaultValue) {

		try {
			Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
			return result != null ? result : defaultValue;
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No result found for SQL [{}] with params: {}", sql, params);
			return defaultValue;

		} catch (DataAccessException e) {
			_log.error("Database error executing SQL [{}]: {}", sql, e.getMessage(), e);
			return defaultValue;

		} catch (Exception e) {
			_log.error("Unexpected error executing SQL [{}]: {}", sql, e.getMessage(), e);
			return defaultValue;
		}
	}

	// ============================================================
	// STRING VARIANT
	// ============================================================
	/**
	 * Safely executes a queryForObject expecting a String result.
	 */
	public static String safeQueryForString(NamedParameterJdbcTemplate jdbcTemplate, String sql,
			MapSqlParameterSource params, String defaultValue) {

		try {
			String result = jdbcTemplate.queryForObject(sql, params, String.class);
			return result != null ? result : defaultValue;

		} catch (EmptyResultDataAccessException e) {
			_log.warn("No result found for SQL [{}] with params: {}", sql, params);
			return defaultValue;

		} catch (DataAccessException e) {
			_log.error("Database error executing SQL [{}]: {}", sql, e.getMessage(), e);
			return defaultValue;

		} catch (Exception e) {
			_log.error("Unexpected error executing SQL [{}]: {}", sql, e.getMessage(), e);
			return defaultValue;
		}
	}

}

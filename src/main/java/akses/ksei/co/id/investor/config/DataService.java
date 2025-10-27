package akses.ksei.co.id.investor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.QueryUtil;

@Service
public class DataService {

	Logger _log = LoggerFactory.getLogger(this.getClass());

	private final NamedParameterJdbcTemplate sidgenJdbcTemplate;

	@Autowired
	public DataService(@Qualifier("sidgenJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {

		this.sidgenJdbcTemplate = jdbcTemplate;
	}

	public int getTotalInvestor() {
		try {
			String sql = QueryUtil._sqlCountInvestor(null);

			MapSqlParameterSource parameters = new MapSqlParameterSource();

			Integer totalRows = sidgenJdbcTemplate.queryForObject(sql, parameters, Integer.class);

			return (totalRows != null) ? totalRows : 0;
		} catch (Exception e) {
			_log.error("Error executing query for count investors: {}", e.getMessage(), e);

			return 0;
		}
	}

	public int getTotalInvestor(String date) {
		try {
			String sql = QueryUtil._sqlCountInvestor(date);

			MapSqlParameterSource parameters = new MapSqlParameterSource();
			parameters.addValue("date", date);
			parameters.addValue("fmt", Constants.TRANSFER_DATA_DATE_FORMAT);

			Integer totalRows = sidgenJdbcTemplate.queryForObject(sql, parameters, Integer.class);

			return (totalRows != null) ? totalRows : 0;
		} catch (Exception e) {
			_log.error("Error executing query for count investors by date: {}", e.getMessage(), e);

			return 0;
		}
	}

}

package akses.ksei.co.id.investor.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.util.Constants;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SequenceServiceImpl {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final JdbcTemplate jdbcTemplate;

	public void createSequence(String tableName, String columnId, String sequenceName) {
		// Validasi nama tabel
		if (!Constants.VALID_TABLES.contains(tableName)) {
			throw new IllegalArgumentException("Invalid table name: " + tableName);
		}
		// Validasi nama kolom
		if (!Constants.VALID_COLUMNS.contains(columnId)) {
			throw new IllegalArgumentException("Invalid column name: " + columnId);
		}

		// get the maximum value from the audit_id column
		String maxIdQuery = String.format("SELECT MAX(%s) FROM %s", columnId, tableName);
		Optional<Integer> maxId = Optional.ofNullable(jdbcTemplate.queryForObject(maxIdQuery, Integer.class));

		// determine the initial value of the sequence
		int startWith = maxId.orElse(0) + 1;

		// query to create sequence
		if (!isSequenceExists(sequenceName)) {
			// String createSequenceQuery = String.format(
			// "CREATE SEQUENCE " + Constants.AUDIT_SEQUENCE_NAME + " START WITH %d
			// INCREMENT BY 1 NOCACHE",
			// startWith);

			String createSequenceQuery = String.format("CREATE SEQUENCE %s START WITH %d INCREMENT BY 1 NOCACHE",
					sequenceName.toUpperCase(), startWith);

			try {
				// execute query to create sequence
				jdbcTemplate.execute(createSequenceQuery);
				_log.info("Sequence {} successfully created with START WITH: {}", sequenceName, startWith);
			} catch (Exception e) {
				_log.error("failed to create sequence: {}", e.getMessage());
			}
		} else {
			_log.info("Sequence {} already exist.", sequenceName);
		}
	}

	@SuppressWarnings("deprecation")
	public boolean isSequenceExists(String sequenceName) {
		String query = "SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME = ?";
		Integer count = jdbcTemplate.queryForObject(query, new Object[] { sequenceName.toUpperCase() }, Integer.class);
		return count != null && count > 0;
	}

	public Long getNextId(String sequenceName) {
		if (!Constants.VALID_SEQUENCES.contains(sequenceName)) {
			throw new IllegalArgumentException("Invalid sequence name: " + sequenceName);
		}

		try {
			String sql = String.format("SELECT %s.NEXTVAL FROM DUAL", sequenceName);
			return jdbcTemplate.queryForObject(sql, Long.class);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to fetch next value from sequence: " + e.getMessage(), e);
		}
	}

}

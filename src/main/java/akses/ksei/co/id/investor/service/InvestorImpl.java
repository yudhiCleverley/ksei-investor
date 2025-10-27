package akses.ksei.co.id.investor.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import akses.ksei.co.id.investor.entity.Investor;
import akses.ksei.co.id.investor.repository.InvestorRepository;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.QueryUtil;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvestorImpl {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final InvestorRepository _investorRepository;

	private final EntityManager _entityManager;

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

	@SuppressWarnings("deprecation")
	public List<Long> getNextIdsFromSequence(String sequenceName, int batchSize) {
		String sql = String.format("SELECT %s.NEXTVAL FROM DUAL CONNECT BY ROWNUM <= ?", sequenceName);
		return jdbcTemplate.query(sql, new Object[] { batchSize }, (rs, rowNum) -> rs.getLong(1));
	}

	public Investor findByPrimaryKey(long id) {

		return _investorRepository.findById(id);
	}

	public Investor fetchBySid(String sid) {

		return _investorRepository.findBySid(sid);
	}

	public List<Investor> findAll() {

		return _investorRepository.findAll();
	}

	public List<String> fetchAllSids() {
		List<Investor> investors = findAll();
		List<String> list = new ArrayList<>();

		if (!investors.isEmpty()) {
			for (Investor investor : investors) {
				list.add(investor.getSid());
			}
		}

		return list;
	}

	public int getTotalInvestors() {
		try {
			String sql = QueryUtil.sqlCountExistingInvestor();

			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
			MapSqlParameterSource parameters = new MapSqlParameterSource();

			Integer totalRows = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);

			return (totalRows != null) ? totalRows : 0;
		} catch (Exception e) {
			_log.error("Error executing query for count existing investors: {}", e.getMessage(), e);

			return 0;
		}
	}

	public Investor save(Investor investor) {

		return _investorRepository.save(investor);
	}

	public void remove(long id) {
		if (id > 0) {

			_investorRepository.deleteById(id);
		}
	}

	public int addInvestors(List<Investor> items) {
		int inserted = 0;
		int duplicated = 0;

		if (!items.isEmpty()) {
			for (Investor item : items) {
				try {
					Investor result = addInvestor(item.getSid(), item.getName(), item.getLastUpdate());

					if (result != null) {
						inserted++;
					} else {
						duplicated++;
					}
				} catch (Exception exc) {
					_log.error("Failed to insert record: {}", item.getSid(), exc);
				}
			}
		}

		_log.info("Total successfully inserted investor records: {}, and duplicated: {}", inserted, duplicated);
		return inserted;
	}

	public Investor addInvestor(String sid, String name, Date lastUpdate) {

		Investor investor = fetchBySid(sid);

		if (investor == null) {
			investor = new Investor();
			investor.setSid(sid);
			investor.setName(name);
			investor.setLastUpdate(lastUpdate);

			investor = save(investor);
		} else {
			return null;
		}

		return investor;
	}

	public Investor addUpdateInvestor(String sid, String name, Date lastUpdate) {

		Investor investor = fetchBySid(sid);

		if (investor == null) {
			investor = new Investor();
			investor.setSid(sid);
			investor.setName(name);
			investor.setLastUpdate(lastUpdate);

			investor = save(investor);
		} else {
			investor.setName(name);
			investor.setLastUpdate(lastUpdate);

			investor = save(investor);
		}

		return investor;
	}

	public Investor updateInvestor(String sid, String name, Date lastUpdate) {

		Investor investor = fetchBySid(sid);

		if (investor != null) {
			investor.setName(name);
			investor.setLastUpdate(lastUpdate);

			investor = save(investor);
		}

		return investor;
	}

	public String generateKey(String sid) {
		return String.join("|", sid);
	}

	public Map<String, Investor> fetchExistingSids(Set<String> sids) {
		if (sids == null || sids.isEmpty()) {
			return Collections.emptyMap();
		}

		try {
			List<Investor> existingInvestorsList = fetchBySids(sids);
			_log.info("Fetched {} investors from the database.", existingInvestorsList.size());

			return existingInvestorsList.stream().collect(Collectors.toMap(Investor::getSid, Function.identity()));
		} catch (Exception e) {
			// _log.error("Error fetching investors by SIDs: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to fetch investors by SIDs: " + sids, e);
		}
	}

	public List<Investor> fetchBySids(Set<String> sids) {
		// validate input
		if (sids == null || sids.isEmpty()) {
			throw new IllegalArgumentException("SIDs list cannot be null or empty");
		}

		try {
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
			List<Investor> investors = new ArrayList<>();

			// Query template
			String sql = QueryUtil.sqlExistingInvestors();

			// Chunk the list into batches of 1000 or less
			int batchSize = 1000;
			for (int i = 0; i < sids.size(); i += batchSize) {
				List<String> batch = new ArrayList<>(sids).subList(i, Math.min(i + batchSize, sids.size()));

				// Use MapSqlParameterSource for list parameters
				MapSqlParameterSource params = new MapSqlParameterSource();
				params.addValue("sids", batch);

				// Execute the query for this batch and add the results to the final list
				List<Investor> batchInvestors = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
					Investor investor = new Investor();
					investor.setSid(rs.getString("sid"));
					investor.setName(rs.getString("name"));
					investor.setLastUpdate(rs.getDate("last_update"));
					return investor;
				});

				// Add results to final list
				investors.addAll(batchInvestors);
			}

			// Check if the result is empty
			if (investors.isEmpty()) {
				_log.warn("No exist investors found in db for the given SIDs.");

				return Collections.emptyList();
			}

			return investors;
		} catch (Exception e) {
			// _log.error("Error fetching investors by SIDs: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to fetch investors by SIDs", e);
		}
	}

	public Map<String, Investor> fetchBySids(List<? extends Investor> items) {
		_entityManager.clear();

		if (items.isEmpty()) {
			return Collections.emptyMap();
		}

		Set<String> sids = items.stream().map(Investor::getSid).filter(Validator::isNotNull)
				.collect(Collectors.toSet());
		// _log.info("Input sids: {}", sids);

		if (sids.isEmpty()) {
			_log.warn("Skipping DB fetch due to empty sids");
			return Collections.emptyMap();
		}

		try {
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

			// Query template
			String sql = QueryUtil.sqlExistingInvestors();

			// Use MapSqlParameterSource for list parameters
			MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue("sids", sids);

			// Execute the query for this batch and add the results to the final list
			List<Investor> existingData = namedParameterJdbcTemplate.query(sql, params, (rs, rowNum) -> {
				Investor investor = new Investor();
				investor.setSid(rs.getString("SID"));
				investor.setName(rs.getString("NAME"));
				investor.setLastUpdate(rs.getDate("LAST_UPDATE"));
				return investor;
			});

			// Check if the result is empty
			if (existingData.isEmpty()) {
				// _log.warn("No exist members found in db for the given memberCodes.");

				return Collections.emptyMap();
			}

			return existingData.stream().collect(Collectors.toMap(item -> generateKey(item.getSid()),
					Function.identity(), (existing, replacement) -> existing));
		} catch (Exception e) {
			// _log.error("Error fetching investors by sids: {}", e.getMessage(), e);
			throw new IllegalStateException("Failed to fetch investors by sids", e);
		}
	}

	@Transactional
	public int insertBatch(List<Investor> items) {
		if (items == null || items.isEmpty()) {
			_log.warn("No data to insert. Skipping batch insert.");
			return 0;
		}

		try {
			return batchInsert(items);
		} catch (Exception e) {
			// _log.error("Failed to insert investors. Error: {}", e.getMessage(), e);
			throw new IllegalStateException("Batch insert failed", e);
		}
	}

	public int batchInsert(List<Investor> items) {
		try {
			// Filter only investor with valid sid (not null)
			// List<Investor> validItems = items.stream()
			// .filter(investor -> investor.getSid() != null &&
			// !investor.getSid().isEmpty()).toList();

			// _log.info("Total investors: {}, valid investors: {}, invalid investors: {}",
			// investors.size(),
			// validInvestors.size(), investors.size() - validInvestors.size());

			// if (validItems.isEmpty()) {
			// _log.warn("No valid investors to insert.");
			// return;
			// }

			// get ID for all valid investor
			List<Long> generatedIds = getNextIdsFromSequence(Constants.INVESTOR_SEQUENCE_NAME, items.size());
			if (generatedIds.size() != items.size()) {
				throw new IllegalStateException("Mismatch between IDs and valid investors.");
			}

			String sql = QueryUtil.sqlInsertInvestor();

			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Investor item = items.get(i);
					ps.setLong(1, generatedIds.get(i));
					setNullableField(ps, 2, item.getSid(), Types.VARCHAR); // sid
					setNullableField(ps, 3, item.getName(), Types.VARCHAR); // name
					setNullableField(ps, 4, item.getLastUpdate(), Types.DATE); // last_update
				}

				@Override
				public int getBatchSize() {
					return items.size();
				}
			});

			// int successCount = (int) Arrays.stream(results).filter(result -> result >=
			// 1).count();
			// _log.info("Batch insert completed. Total valid investors: {}, Successfully
			// inserted: {}, Failed: {}",
			// items.size(), successCount, items.size() - successCount);
			return items.size();
		} catch (Exception e) {
			// _log.error("Failed to insert batch. Error: {}", e.getMessage(), e);
			throw new IllegalStateException("Batch insert failed", e);
		}
	}

	@Transactional
	public int updateBatch(List<Investor> items) {
		if (items.isEmpty()) {
			_log.warn("No investors to update.");
			return 0;
		}

		try {
			// List<Investor> validItems = items.stream()
			// .filter(investor -> investor.getSid() != null &&
			// !investor.getSid().isEmpty()).toList();

			// _log.info("Total investors: {}, valid investors: {}, invalid investors: {}",
			// investors.size(),
			// validInvestors.size(), investors.size() - validInvestors.size());

			// if (validItems.isEmpty()) {
			// _log.warn("No valid investors to update.");
			// return;
			// }

			String sql = QueryUtil.sqlUpdateInvestor();

			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Investor item = items.get(i);

					setNullableField(ps, 1, item.getName(), Types.VARCHAR); // name
					setNullableField(ps, 2, item.getLastUpdate(), Types.DATE); // last_update
					setNullableField(ps, 3, item.getSid(), Types.VARCHAR); // sid
				}

				@Override
				public int getBatchSize() {
					return items.size();
				}
			});

			// int successCount = (int) Arrays.stream(results).filter(result -> result >=
			// 1).count();
			// _log.info("Batch update completed. Total valid investors: {}, Successfully
			// updated: {}, Failed: {}",
			// items.size(), successCount, items.size() - successCount);
			return items.size();
		} catch (Exception e) {
			// _log.error("Failed to update batch. Error: {}", e.getMessage(), e);
			throw new IllegalStateException("Batch update failed", e);
		}
	}

	public boolean isValidData(Investor data) {

		return data != null && Validator.isNotNull(data.getSid());
	}

	public boolean isDuplicate(Investor item, Investor existing) {

		return item.getName().equals(existing.getName());
	}

	public boolean hasChanged(Investor newData, Investor existing) {
		return !equalsString(newData.getName(), existing.getName());
	}

	private boolean equalsString(String a, String b) {
		return normalize(a).equalsIgnoreCase(normalize(b));
	}

	private String normalize(String s) {
		if (s == null || s.trim().isEmpty() || s.trim().equals("-") || s.trim().equals("--")) {
			return "";
		}
		return s.trim().toLowerCase();
	}

	public void setNullableField(PreparedStatement ps, int index, Object value, int sqlType) throws SQLException {
		if (value != null) {
			ps.setObject(index, value, sqlType);
		} else {
			ps.setNull(index, sqlType);
		}
	}

}

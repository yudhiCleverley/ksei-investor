package akses.ksei.co.id.investor.service;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import akses.ksei.co.id.investor.enumeration.JobStatusEnum;
import akses.ksei.co.id.investor.model.JobStatusModel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobStatusServiceImpl {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final JdbcTemplate jdbcTemplate;

	public JobStatusModel upsert(String jobName, String batchStatus) {
		Date now = new Date();

		try {
			int status = JobStatusEnum.fromBatchStatus(batchStatus);

			long jobId = getJobIdByName(jobName);
			JobStatusModel jobStatus = jobId > 0 ? findById(jobId) : null;

			if (jobStatus != null) {
				// _log.info("update job status");

				jobStatus.setStatus(status);
				jobStatus.setUpdatedAt(now);
			} else {
				// _log.info("create job status");

				jobStatus = new JobStatusModel();
				jobStatus.setJobName(jobName);
				jobStatus.setStatus(status);
				jobStatus.setUpdatedAt(now);
			}

			jobStatus = save(jobStatus);

			// _log.info("Job status saved: {}", jobStatus.getId());

			return jobStatus;
		} catch (Exception e) {
			_log.error("Failed to upsert job status for jobName={}, batchStatus={}. Reason: {}", jobName, batchStatus,
					e.getMessage(), e);
			return null;
		}
	}

	public long getJobIdByName(String jobName) {
		String sql = sqlFindJobIdByName();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("jobName", jobName);

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		try {
			Long jobId = namedJdbcTemplate.queryForObject(sql, params, Long.class);
			return Optional.ofNullable(jobId).orElse(0L);
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No jobId found for jobName: {}", jobName);
			return 0L;
		} catch (DataAccessException e) {
			_log.error("Database error while fetching jobId: {}", e.getMessage(), e);
			return 0L;
		}
	}

	public JobStatusModel findById(long jobId) {
		String sql = sqlFindJobStatusById();

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("jobId", jobId);

		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

		try {
			return namedJdbcTemplate.queryForObject(sql, params, (rs, rowNum) -> {
				JobStatusModel js = new JobStatusModel();
				js.setJobId(rs.getLong("JOB_ID"));
				js.setJobName(rs.getString("JOB_NAME"));
				js.setStatus(rs.getInt("STATUS"));
				js.setUpdatedAt(rs.getDate("UPDATED_AT"));

				return js;
			});
		} catch (EmptyResultDataAccessException e) {
			_log.warn("No jobStatus found for jobId: {}", jobId);
			return null;
		} catch (DataAccessException e) {
			_log.error("Database error while fetching jobStatus: {}", e.getMessage(), e);
			return null;
		}
	}

	public JobStatusModel save(JobStatusModel js) {
		try {
			if (js.getJobId() > 0) {
				// update
				String sql = sqlUpdateJobStatus();
				jdbcTemplate.update(sql, js.getStatus(), js.getUpdatedAt(), js.getJobId());
			} else {
				// insert
				String sql = sqlInsertJobStatus();
				long id = getNextId();
				jdbcTemplate.update(sql, id, js.getJobName(), js.getStatus(), js.getUpdatedAt());
				js.setJobId(id);
			}

			return js;
		} catch (Exception e) {
			_log.error("Failed to save JobStatus for jobName={}, id={}. Reason: {}", js.getJobName(), js.getJobId(),
					e.getMessage(), e);
			return null;
		}
	}

	public Long getNextId() {
		String sql = sqlNextId();

		return jdbcTemplate.queryForObject(sql, Long.class);
	}

	public static String sqlNextId() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT NVL(MAX(JOB_ID), 0) + 1 FROM ksei_job_status ");
		return query.toString();
	}

	public static String sqlFindJobIdByName() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT JOB_ID ");
		query.append(" FROM ksei_job_status ");
		query.append(" WHERE ");
		query.append(" JOB_NAME = :jobName ");
		query.append(" FETCH FIRST 1 ROWS ONLY ");

		return query.toString();
	}

	public static String sqlFindJobStatusById() {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append(" JOB_ID, ");
		query.append(" JOB_NAME, ");
		query.append(" STATUS, ");
		query.append(" UPDATED_AT ");
		query.append(" FROM ksei_job_status ");
		query.append(" WHERE ");
		query.append(" JOB_ID = :jobId ");
		query.append(" FETCH FIRST 1 ROWS ONLY ");

		return query.toString();
	}

	public static String sqlInsertJobStatus() {
		StringBuilder query = new StringBuilder();
		query.append(" INSERT INTO ksei_job_status ( ");
		query.append(" JOB_ID, ");
		query.append(" JOB_NAME, ");
		query.append(" STATUS, ");
		query.append(" UPDATED_AT)");
		query.append(" VALUES (?, ?, ?, ?) ");

		return query.toString();
	}

	public static String sqlUpdateJobStatus() {
		StringBuilder query = new StringBuilder();
		query.append(" UPDATE ksei_job_status SET ");
		query.append(" STATUS = ?, ");
		query.append(" UPDATED_AT = ? ");
		query.append(" WHERE JOB_ID = ? ");

		return query.toString();
	}

}

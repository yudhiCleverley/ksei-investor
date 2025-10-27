package akses.ksei.co.id.investor.startup;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import akses.ksei.co.id.investor.service.BatchOrchestratorService;
import akses.ksei.co.id.investor.service.InvestorImpl;
import akses.ksei.co.id.investor.service.ParameterImpl;
import akses.ksei.co.id.investor.service.SchedulerMgmtServiceImpl;
import akses.ksei.co.id.investor.service.SequenceServiceImpl;
import akses.ksei.co.id.investor.task.DynamicBatchScheduler;
import akses.ksei.co.id.investor.util.Constants;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AppStartupRunner implements ApplicationRunner {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private String SCHEDULER_CODE = Constants.PULL_DATA_INVESTOR_CODE;

	private final JdbcTemplate jdbcTemplate;

	private final SchedulerMgmtServiceImpl _schedulerMgmtServiceImpl;

	private final ParameterImpl _parameterImpl;

	private final InvestorImpl _investorImpl;

	private final SequenceServiceImpl _sequenceServiceImpl;

	private final BatchOrchestratorService _batchOrchestratorService;

	private final DynamicBatchScheduler _dynamicBatchScheduler;

	private final TaskScheduler taskScheduler;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		upsertSequence();

		int totalExistingData = _investorImpl.getTotalInvestors();
		_log.info("current data investors: {}", totalExistingData);

		if (totalExistingData <= 0) {
			// execute batch jobs
			_batchOrchestratorService.runBatch(SCHEDULER_CODE, null);
		}

		createIndexes();

		// scheduler to update daily
		try {
			String regularDate = _parameterImpl.getDate();
			// String date = "02/05/2025";

			scheduleJob(regularDate, SCHEDULER_CODE);
		} catch (Exception e) {
			_log.error("Startup process failed: {}", e.getMessage(), e);
		}
	}

	public void upsertSequence() {
		// create investor seqeunce
		_sequenceServiceImpl.createSequence(Constants.INVESTOR_TABLE_NAME, Constants.INVESTOR_COLUMN_ID,
				Constants.INVESTOR_SEQUENCE_NAME);

		// create audit seqeunce
		_sequenceServiceImpl.createSequence(Constants.AUDIT_TABLE_NAME, Constants.AUDIT_COLUMN_ID,
				Constants.AUDIT_SEQUENCE_NAME);

		// scheduler history seqeunce
		_sequenceServiceImpl.createSequence(Constants.SCHEDULER_HISTORY_TABLE_NAME,
				Constants.SCHEDULER_HISTORY_COLUMN_ID, Constants.SCHEDULER_HISTORY_SEQUENCE_NAME);

		// notification seqeunce
		_sequenceServiceImpl.createSequence(Constants.NOTIFICATION_TABLE_NAME, Constants.NOTIFICATION_COLUMN_ID,
				Constants.NOTIFICATION_SEQUENCE_NAME);
	}

	private void createIndexes() {
		String checkIndexExistsSql = "SELECT COUNT(*) FROM user_indexes WHERE table_name = 'KSEI_INVESTOR' AND index_name = 'IDX_KSEI_INVESTOR_SID'";
		String createIndexSql = "CREATE INDEX IDX_KSEI_INVESTOR_SID ON KSEI_INVESTOR(SID)";
		try {
			Integer count = jdbcTemplate.queryForObject(checkIndexExistsSql, Integer.class);
			if (count != null && count == 0) {
				jdbcTemplate.execute(createIndexSql);
				_log.info("Index on 'KSEI_INVESTOR(SID)' created successfully.");
			} else {
				_log.info("Index 'IDX_KSEI_INVESTOR_SID' already exists.");
			}
		} catch (Exception e) {
			_log.error("Failed to create index on KSEI_INVESTOR (SID): {}", e.getMessage());
		}
	}

	private void scheduleJob(String regularDate, String schedulerCode) {
		try {
			String cron = _schedulerMgmtServiceImpl.getCronExpression(schedulerCode);
			_log.info("Scheduling job for [{}] with cron [{}] and regularDate [{}]", schedulerCode, cron, regularDate);

			taskScheduler.schedule(() -> {
				_log.info("Running dynamic scheduler update for job [{}]", schedulerCode);
				_dynamicBatchScheduler.updateCronAndSchedule(regularDate, schedulerCode, cron);
			}, new Date(System.currentTimeMillis() + 5000));
		} catch (Exception e) {
			_log.error("Failed to schedule batch job [{}]: {}", schedulerCode, e.getMessage(), e);
		}
	}

}

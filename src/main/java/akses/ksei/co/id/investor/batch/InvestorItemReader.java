package akses.ksei.co.id.investor.batch;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import akses.ksei.co.id.investor.entity.Investor;
import akses.ksei.co.id.investor.entity.InvestorRowMapper;
import akses.ksei.co.id.investor.service.JobStopFlagService;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.QueryUtil;

@Component
public class InvestorItemReader extends JdbcCursorItemReader<Investor> implements StepExecutionListener {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final JobStopFlagService jobStopFlagService;

	private final DataSource dataSource;

	private StepExecution stepExecution;

	@Autowired
	public InvestorItemReader(JobStopFlagService jobStopFlagService,
			@Qualifier("sidgenDataSource") DataSource dataSource) {
		this.jobStopFlagService = jobStopFlagService;
		this.dataSource = dataSource;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		jobStopFlagService.reset();
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return ExitStatus.COMPLETED;
	}

	@Override
	public Investor read() throws Exception {
		if (Thread.currentThread().isInterrupted()) {
			_log.warn("Thread interrupted in reader. Throwing JobInterruptedException...");
			throw new JobInterruptedException("Thread interrupted in reader");
		}

		if (jobStopFlagService.shouldStop()) {
			_log.warn("Job stop flag detected in reader. Throwing JobInterruptedException...");
			throw new JobInterruptedException("Job stop flag set in reader");
		}

		if (stepExecution != null && stepExecution.getJobExecution().isStopping()) {
			_log.warn("JobExecution isStopping() detected. Returning null...");
			return null;
		}

		return super.read();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setDataSource(dataSource);
		setRowMapper(new InvestorRowMapper());
		setSaveState(true);
	}

	public void setQuery(String date) throws Exception {
		String dateFormat = Constants.TRANSFER_DATA_DATE_FORMAT;

		String rawSql = QueryUtil._sqlInvestor(date);
		String sql = rawSql.replace(":date", "'" + date + "'").replace(":fmt", "'" + dateFormat + "'");

		setSql(sql);
	}

}

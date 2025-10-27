package akses.ksei.co.id.investor.batch;

import java.net.SocketTimeoutException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import akses.ksei.co.id.investor.entity.Investor;
import akses.ksei.co.id.investor.util.Constants;
import akses.ksei.co.id.investor.util.Validator;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing
public class BatchConfig {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final JobBuilderFactory jobBuilderFactory;

	private final StepBuilderFactory stepBuilderFactory;

	private final InvestorItemReader reader;

	private final InvestorItemWriter writer;

	private final InvestorJobExecutionListener jobListener;

	private final InvestorChunkListener chunkListener;

	private final InvestorStepExecutionListener stepListener;

	private final JobFailureListener jobFailureListener;

	@Qualifier("sidgenDataSource")
	private final DataSource sidgenDataSource;

	@Bean
	public JobRegistry jobRegistry() {
		return new MapJobRegistry();
	}

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
		JobRegistryBeanPostProcessor processor = new JobRegistryBeanPostProcessor();
		processor.setJobRegistry(jobRegistry);
		return processor;
	}

	// Job configuration
	@Bean(name = Constants.PULL_DATA_INVESTOR_CODE)
	public Job job() {

		return jobBuilderFactory.get(Constants.PULL_DATA_INVESTOR_CODE).incrementer(new RunIdIncrementer())
				.preventRestart().listener(jobListener).start(step(null, null, null, null)).build();
	}

	// Step configuration
	@Bean
	@JobScope
	public Step step(@Value("#{jobParameters['date']}") String date,
			@Value("#{jobParameters['chunkSize']}") Integer chunkSize,
			@Value("#{jobParameters['threadPoolSize']}") Long threadPoolSizeParam,
			@Value("#{jobParameters['totalDataCount']}") Long totalDataCountParam) {

		boolean isDatePresent = Validator.isNotNull(date);

		int calculatedChunkSize = calculateChunkSize(chunkSize, isDatePresent);

		// _log.info("Calculated Chunk Size: {}", calculatedChunkSize);

		TaskExecutor taskExecutor = dynamicThreadPoolExecutor(chunkSize, threadPoolSizeParam, totalDataCountParam);

		return stepBuilderFactory.get("step").<Investor, Investor>chunk(calculatedChunkSize)
				.reader(synchronizedReader()).writer(writer).faultTolerant().skip(JobInterruptedException.class)
				.skipLimit(Integer.MAX_VALUE).retry(DataAccessException.class).retry(SocketTimeoutException.class)
				.retryLimit(3).listener(chunkListener).listener(stepListener).listener(jobFailureListener)
				.listener(reader).listener(writer).listener(new StepExecutionListener() {
					@Override
					public void beforeStep(StepExecution stepExecution) {
						// No-op
					}

					@Override
					public ExitStatus afterStep(StepExecution stepExecution) {
						// Kalau ditemukan JobInterruptedException → ExitStatus.STOPPED
						boolean wasInterrupted = stepExecution.getFailureExceptions().stream()
								.anyMatch(JobInterruptedException.class::isInstance);
						if (wasInterrupted) {
							_log.warn("Step [{}] terhenti oleh JobInterruptedException", stepExecution.getStepName());
							return ExitStatus.STOPPED;
						}
						return null;
					}
				}).taskExecutor(taskExecutor).allowStartIfComplete(true).startLimit(1).build();
	}

	@Bean
	public SynchronizedItemStreamReader<Investor> synchronizedReader() {
		SynchronizedItemStreamReader<Investor> synchronizedReader = new SynchronizedItemStreamReader<>();
		synchronizedReader.setDelegate(reader);
		return synchronizedReader;
	}

	// Task Executor
	public TaskExecutor dynamicThreadPoolExecutor(Integer chunkSize, Long threadPoolSizeParam,
			Long totalDataCountParam) {
		int threadPoolSize = (threadPoolSizeParam != null) ? threadPoolSizeParam.intValue() : 5;
		int queueCapacity = calculateQueueCapacity(threadPoolSize, chunkSize, totalDataCountParam.intValue());

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(threadPoolSize);
		executor.setMaxPoolSize(threadPoolSize * 2);
		executor.setQueueCapacity(queueCapacity);
		executor.setThreadNamePrefix("BatchThread-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);
		executor.initialize();

		_log.info("Auto-tuned ThreadPool Executor - Threads: {}, Queue Capacity: {}", threadPoolSize, queueCapacity);

		return executor;
	}

	public int calculateChunkSize(Integer chunkSize, boolean isDatePresent) {
		int defaultSize = isDatePresent ? 100 : 1000;
		return (chunkSize == null || chunkSize <= 0) ? defaultSize : chunkSize;
	}

	public int calculateQueueCapacity(int threadPoolSize, int chunkSize, int dataCount) {
		int totalChunks = (int) Math.ceil((double) dataCount / chunkSize);
		int minQueue = Math.max(10, threadPoolSize * 2);
		int estimatedQueue = totalChunks - threadPoolSize;
		return Math.max(minQueue, estimatedQueue);
	}

}

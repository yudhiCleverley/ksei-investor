package akses.ksei.co.id.investor.config;

import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableAsync
public class AsyncConfig {

	private static final Logger _log = LoggerFactory.getLogger(AsyncConfig.class);

	private final DataSource dataSource;

	@Bean(name = "batchStopExecutor")
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(10);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("batchStop-");
		executor.setTaskDecorator(new ClearTransactionContextDecorator(dataSource));
		executor.initialize();
		return executor;
	}

	@Bean(name = "taskExecutor")
	public Executor defaultTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(3);
		executor.setMaxPoolSize(6);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("defaultAsync-");
		executor.initialize();
		return executor;
	}

	public static class ClearTransactionContextDecorator implements TaskDecorator {
		private final DataSource dataSource;

		public ClearTransactionContextDecorator(DataSource dataSource) {
			this.dataSource = dataSource;
		}

		@Override
		public Runnable decorate(Runnable runnable) {
			return () -> {
				try {
					// Unbind DataSource (secara eksplisit)
					if (TransactionSynchronizationManager.hasResource(dataSource)) {
						TransactionSynchronizationManager.unbindResource(dataSource);
						_log.info("Manually unbound datasource from thread: {}", dataSource);
					}

					if (TransactionSynchronizationManager.isSynchronizationActive()) {
						TransactionSynchronizationManager.clearSynchronization();
						_log.info("Transaction synchronization cleared");
					}
				} catch (Exception e) {
					_log.error("Failed clearing transaction context", e);
				}

				// _log.info("Before run, cleared resources.");
				runnable.run();
				// _log.info("After run completed.");
			};
		}
	}

}

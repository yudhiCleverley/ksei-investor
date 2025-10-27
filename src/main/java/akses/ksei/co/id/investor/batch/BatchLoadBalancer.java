package akses.ksei.co.id.investor.batch;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BatchLoadBalancer {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private static final int MAX_THREAD_POOL_SIZE = 64;
	private static final int DEFAULT_DB_LOAD_FACTOR = 2;

	private final DataSource dataSource;

	private final ChunkSizeCalculator chunkSizeCalculator;

	@Value("${batch.thread-pool-size:0}")
	private int overrideThreadPoolSize;

	@PostConstruct
	public void logPoolSize() {
		if (dataSource instanceof HikariDataSource hikari) {
			_log.info("HikariCP - Pool Name: {}", hikari.getPoolName());
			_log.info("HikariCP - Max Pool Size: {}", hikari.getMaximumPoolSize());
			_log.info("HikariCP - Minimum Idle: {}", hikari.getMinimumIdle());
		} else {
			_log.info("DataSource is not HikariCP: {}", dataSource.getClass().getName());
		}
	}

	public Pair<Integer, Integer> computeThreadAndChunk(int totalData) {
		int threadPoolSize = calculateThreadPoolSize(dataSource);
		int chunkSize = chunkSizeCalculator.calculateChunkSize(totalData);

		_log.info("Calculated threadPoolSize: {}, chunkSize: {} based on totalData: {}", threadPoolSize, chunkSize,
				totalData);

		return Pair.of(threadPoolSize, chunkSize);
	}

	private int calculateThreadPoolSize(DataSource dataSource) {
		if (overrideThreadPoolSize > 0) {
			_log.info("Override threadPoolSize applied: {}", overrideThreadPoolSize);
			return overrideThreadPoolSize;
		}

		int dbLoadFactor = getDbLoadFactor(dataSource);
		int availableCores = Runtime.getRuntime().availableProcessors();
		int poolSize = Math.max(4, availableCores * dbLoadFactor);

		if (poolSize > MAX_THREAD_POOL_SIZE) {
			_log.warn("Thread pool size terlalu besar: {}, diset ke {}", poolSize, MAX_THREAD_POOL_SIZE);
			poolSize = MAX_THREAD_POOL_SIZE;
		}

		_log.info("Available cores: {}, DB Load Factor: {}, Calculated threadPoolSize: {}", availableCores,
				dbLoadFactor, poolSize);
		return poolSize;
	}

	// private int getDbLoadFactor(DataSource dataSource) {
	// String sql = "SELECT value FROM v$parameter WHERE name = 'sessions'";
	//
	// try (Connection conn = dataSource.getConnection();
	// PreparedStatement ps = conn.prepareStatement(sql);
	// ResultSet rs = ps.executeQuery()) {
	//
	// if (rs.next()) {
	// int maxSessions = rs.getInt(1);
	// int availableCores = Runtime.getRuntime().availableProcessors();
	// return Math.max(1, (maxSessions * 30 / 100) / availableCores);
	// }
	// } catch (SQLException e) {
	// _log.warn("Failed to get DB max sessions, using default dbLoadFactor={}",
	// DEFAULT_DB_LOAD_FACTOR, e);
	// }
	//
	// return DEFAULT_DB_LOAD_FACTOR;
	// }

	private int getDbLoadFactor(DataSource dataSource) {
		int availableCores = Runtime.getRuntime().availableProcessors();

		if (dataSource instanceof HikariDataSource hikari) {
			int maxPoolSize = hikari.getMaximumPoolSize();
			int computed = (int) Math.ceil((maxPoolSize * 0.3) / availableCores);
			_log.info("Hikari maxPoolSize: {}, computed DB load factor: {}", maxPoolSize, computed);
			return Math.max(DEFAULT_DB_LOAD_FACTOR, computed);
		}

		int fallback = (int) Math.ceil(availableCores * 0.3);
		_log.info("Non-Hikari fallback DB load factor: {}", fallback);
		return Math.max(DEFAULT_DB_LOAD_FACTOR, fallback);
	}

	public void logBatchConfig(int totalData, int thread, int chunkSize) {
		_log.info("Total: {}, Chunk Size: {}, Thread Pool Size: {}", totalData, chunkSize, thread);
	}

}

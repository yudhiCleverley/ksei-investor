package akses.ksei.co.id.investor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.datasource.hikari")
public class DataSourceHikariProperties {

	private int minimumIdle;
	private int maximumPoolSize;
	private int idleTimeout;
	private String poolName;
	private int maxLifetime;
	private int connectionTimeout;
	private int initializationFailTimeout;
	private String connectionInitSql;

	public int getMinimumIdle() {
		return minimumIdle;
	}

	public void setMinimumIdle(int minimumIdle) {
		this.minimumIdle = minimumIdle;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public int getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(int idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public int getMaxLifetime() {
		return maxLifetime;
	}

	public void setMaxLifetime(int maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getInitializationFailTimeout() {
		return initializationFailTimeout;
	}

	public void setInitializationFailTimeout(int initializationFailTimeout) {
		this.initializationFailTimeout = initializationFailTimeout;
	}

	public String getConnectionInitSql() {
		return connectionInitSql;
	}

	public void setConnectionInitSql(String connectionInitSql) {
		this.connectionInitSql = connectionInitSql;
	}

}

package akses.ksei.co.id.investor.config;

import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	private final FullDataSourceProperties fullProps;

	private final MultipleDataSourceProperties multipleProps;

	public DataSourceConfig(FullDataSourceProperties fullProps, MultipleDataSourceProperties multipleProps) {
		this.fullProps = fullProps;
		this.multipleProps = multipleProps;
	}

	// LOCAL CONFIG
	@Primary
	@Bean(name = "dataSource")
	public DataSource dataSource() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(fullProps.getUrl());
		config.setUsername(fullProps.getUsername());
		config.setPassword(fullProps.getPassword());
		config.setDriverClassName(fullProps.getDriverClassName());

		FullDataSourceProperties.DataSourceHikariProperties hikari = fullProps.getHikari();
		config.setMinimumIdle(hikari.getMinimumIdle());
		config.setMaximumPoolSize(hikari.getMaximumPoolSize());
		config.setIdleTimeout(hikari.getIdleTimeout());
		config.setPoolName(hikari.getPoolName());
		config.setMaxLifetime(hikari.getMaxLifetime());
		config.setConnectionTimeout(hikari.getConnectionTimeout());
		config.setInitializationFailTimeout(hikari.getInitializationFailTimeout());
		config.setConnectionInitSql(hikari.getConnectionInitSql());

		return new HikariDataSource(config);
	}

	@Bean(name = "jdbcTemplate")
	public NamedParameterJdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource ds) {
		_log.info("Creating NamedParameterJdbcTemplate for dataSource");

		return new NamedParameterJdbcTemplate(ds);
	}

	@Primary
	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(@Qualifier("dataSource") DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}

	// SIDGEN CONFIG
	@Bean(name = "sidgenDataSource")
	public DataSource sidgenDataSource() {

		return createHikariDataSource("sidgen");
	}

	@Bean(name = "sidgenJdbcTemplate")
	public NamedParameterJdbcTemplate sidgenJdbcTemplate(@Qualifier("sidgenDataSource") DataSource ds) {
		return new NamedParameterJdbcTemplate(ds);
	}

	@Bean(name = "sidgenTransactionManager")
	public PlatformTransactionManager sidgenTransactionManager(@Qualifier("sidgenDataSource") DataSource ds) {
		return new DataSourceTransactionManager(ds);
	}

	private DataSource createHikariDataSource(String key) {
		Map<String, DataSourcePropertiesWrapper> map = multipleProps.getDatasource();
		if (map == null) {
			throw new IllegalStateException("Datasource map is null! Check YAML or binding configuration.");
		}

		DataSourcePropertiesWrapper props = map.get(key);
		if (props == null) {
			throw new IllegalStateException("No datasource config found for key: " + key);
		}

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(props.getUrl());
		config.setUsername(props.getUsername());
		config.setPassword(props.getPassword());
		config.setDriverClassName(props.getDriverClassName());

		return new HikariDataSource(config);
	}

}

package akses.ksei.co.id.investor.config;

import java.util.HashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
//@EnableJpaRepositories(basePackages = "akses.ksei.co.id.investor.repository", entityManagerFactoryRef = "entityManager", transactionManagerRef = "transactionManager")
public class OracleJpaConfig {

	private final Logger _log = LoggerFactory.getLogger(this.getClass());

	@Value("${spring.datasource.driver-class-name}")
	public String driverClassName;

	@Value("${spring.datasource.url}")
	public String url;

	@Value("${spring.datasource.username}")
	public String username;

	@Value("${spring.datasource.password}")
	public String password;

	@Value("${spring.jpa.hibernate.ddl-auto}")
	public String hibernateDdlAuto;

	@Value("${spring.jpa.properties.hibernate.dialect}")
	public String hibernateDialect;

	@Primary
	@Bean(name = "dataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource dataSource() {
		_log.info("driverClassName: {}", driverClassName);

		return DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username)
				.password(password).build();
	}

	@Primary
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManager() {
		final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource());
		em.setPackagesToScan("akses.ksei.co.id.investor.entity");

		final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);
		final HashMap<String, Object> properties = new HashMap<>();
		properties.put("hibernate.hbm2ddl.auto", hibernateDdlAuto);
		properties.put("hibernate.dialect", hibernateDialect);
		properties.put("hibernate.physical_naming_strategy",
				"akses.ksei.co.id.investor.config.UppercasePhysicalNamingStrategy");
		em.setJpaPropertyMap(properties);

		return em;
	}

	@Primary
	@Bean
	public PlatformTransactionManager transactionManager() {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManager().getObject());
		return transactionManager;
	}

}

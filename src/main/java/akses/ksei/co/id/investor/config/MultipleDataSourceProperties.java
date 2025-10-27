package akses.ksei.co.id.investor.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class MultipleDataSourceProperties {

	private Map<String, DataSourcePropertiesWrapper> datasource;

	public Map<String, DataSourcePropertiesWrapper> getDatasource() {
		return datasource;
	}

	public void setDatasource(Map<String, DataSourcePropertiesWrapper> datasource) {
		this.datasource = datasource;
	}

}

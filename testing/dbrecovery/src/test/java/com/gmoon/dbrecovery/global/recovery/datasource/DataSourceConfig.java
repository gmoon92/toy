package com.gmoon.dbrecovery.global.recovery.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

	// todo proxy jdbc driver crud query detected.
	@Bean
	@Primary
	public DataSource dataSource(DataSourceProperties properties) throws Exception {
		DataSource dataSource = properties.initializeDataSourceBuilder()
			 .type(HikariDataSource.class)
			 .build();

		return new DataSourceProxy(dataSource);
	}
}

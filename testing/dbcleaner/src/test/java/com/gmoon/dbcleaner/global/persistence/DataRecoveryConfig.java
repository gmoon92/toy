package com.gmoon.dbcleaner.global.persistence;

import com.gmoon.dbcleaner.global.persistence.datasource.DataSourceProxy;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataRecoveryConfig {

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

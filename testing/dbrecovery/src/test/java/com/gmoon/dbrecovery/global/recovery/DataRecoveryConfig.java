package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.DataSourceProxy;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "service.dbrecovery.enable", matchIfMissing = false)
public class DataRecoveryConfig {

	@Bean
	@Primary
	public DataSource dataSource(DataSourceProperties properties) throws Exception {
		DataSource dataSource = properties.initializeDataSourceBuilder()
			 .type(HikariDataSource.class)
			 .build();

		return new DataSourceProxy(dataSource);
	}

	@Bean
	public RecoveryDatabaseInitialization recoveryDatabaseInitialization(DataSource dataSource, RecoveryDatabaseProperties properties) {
		return new RecoveryDatabaseInitialization(dataSource, properties);
	}

	@Bean
	public DataRecoveryHelper dataRecoveryHelper(RecoveryDatabaseInitialization recoveryDatabaseInitialization, RecoveryDatabaseProperties properties, DataSource dataSource) {
		return new DataRecoveryHelper(recoveryDatabaseInitialization, properties, dataSource);
	}
}

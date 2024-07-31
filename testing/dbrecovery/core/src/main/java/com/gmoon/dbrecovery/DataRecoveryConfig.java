package com.gmoon.dbrecovery;

import com.gmoon.dbrecovery.datasource.RecoveryDatabaseInitialization;
import com.gmoon.dbrecovery.datasource.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.datasource.Table;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(RecoveryDatabaseProperties.class)
public class DataRecoveryConfig {

	@Bean
	public Table recoveryTable(DataSource dataSource, RecoveryDatabaseProperties properties) {
		return new Table(dataSource, properties);
	}

	@Bean
	public DataRecoveryHelper dataRecoveryHelper(
		 DataSource dataSource,
		 Table recoveryTable,
		 RecoveryDatabaseProperties properties
	) {
		return new DataRecoveryHelper(dataSource, recoveryTable, properties);
	}

	@Bean
	public RecoveryDatabaseInitialization recoveryDatabaseInitialization(
		 DataSource dataSource,
		 Table recoveryTable,
		 RecoveryDatabaseProperties properties
	) {
		return new RecoveryDatabaseInitialization(dataSource, recoveryTable, properties);
	}
}

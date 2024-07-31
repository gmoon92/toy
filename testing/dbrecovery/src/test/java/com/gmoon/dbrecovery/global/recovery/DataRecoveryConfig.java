package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.RecoveryDatabaseInitialization;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(RecoveryDatabaseProperties.class)
public class DataRecoveryConfig {

	@Bean
	public RecoveryTable recoveryTable(DataSource dataSource, RecoveryDatabaseProperties properties) {
		return new RecoveryTable(dataSource, properties);
	}

	@Bean
	public DataRecoveryHelper dataRecoveryHelper(
		 DataSource dataSource,
		 RecoveryTable recoveryTable,
		 RecoveryDatabaseProperties properties
	) {
		return new DataRecoveryHelper(dataSource, recoveryTable, properties);
	}

	@Bean
	public RecoveryDatabaseInitialization recoveryDatabaseInitialization(
		 DataSource dataSource,
		 RecoveryTable recoveryTable,
		 RecoveryDatabaseProperties properties
	) {
		return new RecoveryDatabaseInitialization(dataSource, recoveryTable, properties);
	}
}

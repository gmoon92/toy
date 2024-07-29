package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "service.dbrecovery.enable", matchIfMissing = false)
@RequiredArgsConstructor
public class DataRecoveryConfig {

	@Bean
	public RecoveryTable recoveryTable(DataSource dataSource, RecoveryDatabaseProperties properties) {
		return new RecoveryTable(dataSource, properties);
	}

	@Bean
	public DataRecoveryHelper dataRecoveryHelper(RecoveryTable recoveryTable, RecoveryDatabaseProperties properties, DataSource dataSource) {
		return new DataRecoveryHelper(recoveryTable, properties, dataSource);
	}

	@Bean
	public RecoveryDatabaseInitialization recoveryDatabaseInitialization(
		 RecoveryTable recoveryTable,
		 DataSource dataSource,
		 RecoveryDatabaseProperties properties
	) {
		return new RecoveryDatabaseInitialization(recoveryTable, dataSource, properties);
	}
}

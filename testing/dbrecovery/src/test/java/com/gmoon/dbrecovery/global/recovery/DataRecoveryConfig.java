package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "service.dbrecovery.enable", matchIfMissing = false)
public class DataRecoveryConfig {

	@Bean
	public RecoveryDatabaseInitialization recoveryDatabaseInitialization(DataSource dataSource, RecoveryDatabaseProperties properties) {
		return new RecoveryDatabaseInitialization(dataSource, properties);
	}

	@Bean
	public DataRecoveryHelper dataRecoveryHelper(RecoveryDatabaseInitialization recoveryDatabaseInitialization, RecoveryDatabaseProperties properties, DataSource dataSource) {
		return new DataRecoveryHelper(recoveryDatabaseInitialization, properties, dataSource);
	}
}

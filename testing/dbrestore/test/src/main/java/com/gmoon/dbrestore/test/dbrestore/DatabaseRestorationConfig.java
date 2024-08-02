package com.gmoon.dbrestore.test.dbrestore;

import com.gmoon.dbrestore.test.dbrestore.datasource.BackupDatabaseInitialization;
import com.gmoon.dbrestore.test.dbrestore.datasource.DatabaseRestoreProperties;
import com.gmoon.dbrestore.test.dbrestore.datasource.ReferenceTable;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EnableConfigurationProperties(DatabaseRestoreProperties.class)
@RequiredArgsConstructor
public class DatabaseRestorationConfig {

	@Bean
	public ReferenceTable referenceTable(
		 DataSource dataSource,
		 DatabaseRestoreProperties properties
	) {
		return new ReferenceTable(dataSource, properties);
	}

	@Bean
	public DataRestorationHelper databaseRestorationHelper(
		 DataSource dataSource,
		 ReferenceTable referenceTable,
		 DatabaseRestoreProperties properties
	) {
		return new DataRestorationHelper(dataSource, referenceTable, properties);
	}

	@Bean
	public BackupDatabaseInitialization backupDatabaseInitialization(
		 DataSource dataSource,
		 ReferenceTable referenceTable,
		 DatabaseRestoreProperties properties
	) {
		return new BackupDatabaseInitialization(dataSource, referenceTable, properties);
	}
}

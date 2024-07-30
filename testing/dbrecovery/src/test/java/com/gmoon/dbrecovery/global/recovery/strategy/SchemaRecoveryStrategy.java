package com.gmoon.dbrecovery.global.recovery.strategy;

import com.gmoon.dbrecovery.global.recovery.RecoveryTable;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class SchemaRecoveryStrategy extends AbstractRecoveryStrategy implements RecoveryStrategy {

	private final DataSource dataSource;
	private final RecoveryTable recoveryTable;
	private final RecoveryDatabaseProperties properties;

	@Override
	public void recovery() {
		Set<String> tableNames = recoveryTable.getTableAll();
		recovery(tableNames);
	}

	@Override
	public Connection getConnection() throws Exception {
		return dataSource.getConnection();
	}

	@Override
	public String getSchema() {
		return properties.getSchema();
	}

	@Override
	public String getRecoverySchema() {
		return properties.getRecoverySchema();
	}
}

package com.gmoon.dbrecovery.global.recovery.strategy;

import com.gmoon.dbrecovery.global.recovery.RecoveryTable;
import com.gmoon.dbrecovery.global.recovery.datasource.SqlStatementCallStack;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Stack;

@Slf4j
@RequiredArgsConstructor
public class RecordRecoveryStrategy extends AbstractRecoveryStrategy implements RecoveryStrategy {

	private final DataSource dataSource;
	private final RecoveryTable recoveryTable;
	private final RecoveryDatabaseProperties properties;
	private final SqlStatementCallStack callStack;

	@Override
	public void recovery() {
		String sql = "";
		String recoverySql = "";
		try (Connection connection = getConnection()) {
			Stack<String> queries = callStack.getValue();

			while (queries.isEmpty()) {
				sql = queries.pop();
				recoverySql = "";
				executeQuery(connection, recoverySql);
			}
		} catch (Exception e) {
			throw new RuntimeException(String.format("It`s Wrong sql. %s -> recovery: %s", sql, recoverySql), e);
		}
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

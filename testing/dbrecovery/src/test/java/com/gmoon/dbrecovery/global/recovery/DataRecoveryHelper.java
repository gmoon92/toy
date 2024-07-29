package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.DmlStatementCallStack;
import com.gmoon.dbrecovery.global.recovery.datasource.SqlParser;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

@Slf4j
@RequiredArgsConstructor
public class DataRecoveryHelper {

	private final RecoveryTable recoveryTable;
	private final RecoveryDatabaseProperties properties;
	private final DataSource dataSource;

	private void executeQuery(String queryString) {
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(queryString)) {

			int result = statement.executeUpdate();
			log.debug("[EXECUTE][{}]: {}", result, queryString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void recovery(DmlStatementCallStack dmlStatementCallStack) {
		log.debug("Start data recovery.");
		executeQuery("SET FOREIGN_KEY_CHECKS = 0");

		Set<String> tableNames = obtainRecoveryTables(dmlStatementCallStack);
		for (String tableName : tableNames) {
			truncateTable(tableName);
			recoveryTable(tableName);
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
		log.debug("Data recovery successful.");
	}

	private Set<String> obtainRecoveryTables(DmlStatementCallStack dmlStatementCallStack) {
		Set<String> result = new HashSet<>();
		Stack<String> callStack = dmlStatementCallStack.getValue();
		while (!callStack.empty()) {
			String sql = callStack.pop();

			Statement statement = SqlParser.getStatement(sql);
			Table table = SqlParser.getTable(statement);
			String tableName = table.getName();
			result.add(tableName);
			if (statement instanceof Delete) {
				Set<String> deleteTables = recoveryTable.getDeleteTables(tableName);
				result.addAll(deleteTables);
			}
		}
		return result;
	}

	private void truncateTable(String tableName) {
		String originTable = properties.getSchema() + "." + tableName;
		executeQuery("TRUNCATE TABLE " + originTable);
		log.debug("[TRUNCATE] {}", originTable);
	}

	private void recoveryTable(String tableName) {
		String originTable = properties.getSchema() + "." + tableName;
		String backupTable = properties.getBackupSchema() + "." + tableName;
		executeQuery(String.format("INSERT INTO %s SELECT * FROM %s", originTable, backupTable));
		log.debug("[RECOVERY] {}", originTable);
	}
}

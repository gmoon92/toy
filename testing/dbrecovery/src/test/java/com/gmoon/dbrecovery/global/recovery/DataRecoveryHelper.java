package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.SqlParser;
import com.gmoon.dbrecovery.global.recovery.datasource.SqlStatementCallStack;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.global.recovery.strategy.RecoveryStrategy;
import com.gmoon.javacore.util.CollectionUtils;
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

	private final DataSource dataSource;
	private final RecoveryTable recoveryTable;
	private final RecoveryDatabaseProperties properties;

	public void recovery(SqlStatementCallStack sqlStatementCallStack) {
		RecoveryStrategy strategy = properties.getStrategy();
		strategy.recovery();
		recoveryOfTableStrategy(sqlStatementCallStack);
	}

	private void recoveryOfTableStrategy(SqlStatementCallStack sqlStatementCallStack) {
		Set<String> tableNames = obtainRecoveryTables(sqlStatementCallStack);
		if (CollectionUtils.isNotEmpty(tableNames)) {
			try (Connection connection = dataSource.getConnection()) {
				log.trace("Start data recovery.");
				executeQuery(connection, "SET FOREIGN_KEY_CHECKS = 0");

				for (String tableName : tableNames) {
					truncateTable(connection, tableName);
					recoveryTable(connection, tableName);
				}
				executeQuery(connection, "SET FOREIGN_KEY_CHECKS = 1");
				log.trace("Data recovery successful.");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void executeQuery(Connection connection, String queryString) {
		try (PreparedStatement statement = connection.prepareStatement(queryString)) {

			int result = statement.executeUpdate();
			log.trace("[EXECUTE][{}]: {}", result, queryString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> obtainRecoveryTables(SqlStatementCallStack sqlStatementCallStack) {
		Set<String> result = new HashSet<>();
		Stack<String> callStack = sqlStatementCallStack.getValue();
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

	private void truncateTable(Connection connection, String tableName) {
		String originTable = properties.getSchema() + "." + tableName;
		executeQuery(connection, "TRUNCATE TABLE " + originTable);
		log.trace("[TRUNCATE] {}", originTable);
	}

	private void recoveryTable(Connection connection, String tableName) {
		String originTable = properties.getSchema() + "." + tableName;
		String backupTable = properties.getRecoverySchema() + "." + tableName;
		executeQuery(connection, String.format("INSERT INTO %s SELECT * FROM %s", originTable, backupTable));
		log.trace("[RECOVERY] {}", originTable);
	}
}

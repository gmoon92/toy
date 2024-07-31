package com.gmoon.dbrecovery;

import com.gmoon.dbrecovery.datasource.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.datasource.SqlParser;
import com.gmoon.dbrecovery.datasource.SqlStatementCallStack;
import com.gmoon.dbrecovery.datasource.Table;
import com.gmoon.javacore.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

@Slf4j
@RequiredArgsConstructor
public class DataRecoveryHelper {

	private final DataSource dataSource;
	private final Table recoveryTable;
	private final RecoveryDatabaseProperties properties;

	public void recovery(SqlStatementCallStack callStack) {
		Set<String> tableNames = obtainRecoveryTables(callStack);
		recovery(tableNames);
	}

	private Set<String> obtainRecoveryTables(SqlStatementCallStack callStack) {
		Set<String> result = new HashSet<>();
		Stack<String> queries = callStack.getValue();
		while (!queries.empty()) {
			String sql = queries.pop();
			log.trace("stack sql: {}", sql);

			Statement statement = SqlParser.getStatement(sql);
			net.sf.jsqlparser.schema.Table table = SqlParser.getTable(statement);
			String tableName = table.getName();
			result.add(tableName);
			if (statement instanceof Delete) {
				Set<String> deleteTables = recoveryTable.getDeleteTables(tableName);
				result.addAll(deleteTables);
			}
		}
		return result;
	}

	private void recovery(Collection<String> tableNames) {
		if (CollectionUtils.isNotEmpty(tableNames)) {
			try (Connection connection = dataSource.getConnection()) {
				log.trace("Start data recovery.");
				executeQuery(connection, "SET FOREIGN_KEY_CHECKS = 0");
				executeQuery(connection, "SET AUTOCOMMIT = 0");

				for (String tableName : tableNames) {
					truncateTable(connection, tableName);
					recoveryTable(connection, tableName);
				}
				executeQuery(connection, "SET FOREIGN_KEY_CHECKS = 1");
				executeQuery(connection, "COMMIT");
				executeQuery(connection, "SET AUTOCOMMIT = 1");
				log.trace("Data recovery successful.");
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
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

	private void executeQuery(Connection connection, String sql) {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

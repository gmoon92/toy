package com.gmoon.dbrecovery.global.recovery.strategy;

import com.gmoon.dbrecovery.global.recovery.RecoveryTable;
import com.gmoon.dbrecovery.global.recovery.datasource.SqlParser;
import com.gmoon.dbrecovery.global.recovery.datasource.SqlStatementCallStack;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

@Slf4j
@RequiredArgsConstructor
public class TableRecoveryStrategy extends AbstractRecoveryStrategy implements RecoveryStrategy {

	private final DataSource dataSource;
	private final RecoveryTable recoveryTable;
	private final RecoveryDatabaseProperties properties;
	private final SqlStatementCallStack callStack;

	@Override
	public void recovery() {
		Set<String> tableNames = obtainRecoveryTables(callStack);
		recovery(tableNames);
	}

	private Set<String> obtainRecoveryTables(SqlStatementCallStack callStack) {
		Set<String> result = new HashSet<>();
		Stack<String> queries = callStack.getValue();
		while (!queries.empty()) {
			String sql = queries.pop();
			log.debug("sql: {}", sql);

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

package com.gmoon.dbrestore.test.dbrestore;

import com.gmoon.dbrestore.test.dbrestore.datasource.DatabaseRestoreProperties;
import com.gmoon.dbrestore.test.dbrestore.datasource.ReferenceTable;
import com.gmoon.dbrestore.test.dbrestore.datasource.SqlParser;
import com.gmoon.dbrestore.test.dbrestore.datasource.event.SqlStatementCallStack;
import com.gmoon.javacore.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

@Slf4j
@RequiredArgsConstructor
public class DatabaseRestoreHelper {

	private final DataSource dataSource;
	private final ReferenceTable referenceTable;
	private final DatabaseRestoreProperties properties;

	public void snapshot(SqlStatementCallStack sqlCallStack) {
		try (Connection connection = dataSource.getConnection()) {
			if (isBrokenTablePresent(connection)) {
				Set<String> allTables = referenceTable.getAllTables();
				restore(allTables);
			}

			executeUpdate(connection, String.format("INSERT INTO %s (status) value ('WAIT');", referenceTable.getSystemTableName()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			sqlCallStack.clear();
		}

	}

	private boolean isBrokenTablePresent(Connection connection) {
		String sql = String.format("SELECT 1 FROM %s LIMIT 1 ", referenceTable.getSystemTableName());
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.execute();
			try (ResultSet resultSet = statement.getResultSet()) {
				return resultSet.next();
			}
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void restore(SqlStatementCallStack callStack) {
		Set<String> tableNames = obtainRestoreTables(callStack);
		restore(tableNames);
	}

	private Set<String> obtainRestoreTables(SqlStatementCallStack callStack) {
		Set<String> result = new HashSet<>();
		Stack<String> queries = callStack.getValue();
		while (!queries.empty()) {
			String sql = queries.pop();
			log.trace("stack sql: {}", sql);

			Statement statement = SqlParser.getStatement(sql);
			Table table = SqlParser.getTable(statement);
			String tableName = table.getName();

			result.add(tableName);
			if (statement instanceof Delete) {
				Set<String> deleteTables = referenceTable.getDeleteTables(tableName);
				result.addAll(deleteTables);
			}
		}
		return result;
	}

	private void restore(Collection<String> tableNames) {
		try (Connection connection = dataSource.getConnection()) {
			if (CollectionUtils.isNotEmpty(tableNames)) {
				log.trace("[Start] data restoration.");
				executeUpdate(connection, "SET FOREIGN_KEY_CHECKS = 0");
				executeUpdate(connection, "SET AUTOCOMMIT = 0");

				for (String tableName : tableNames) {
					truncateTable(connection, tableName);
					restoreTable(connection, tableName);
				}

				executeUpdate(connection, "SET FOREIGN_KEY_CHECKS = 1");
				executeUpdate(connection, "COMMIT");
				executeUpdate(connection, "SET AUTOCOMMIT = 1");
				log.trace("[END]  data restoration.");
			}

			executeUpdate(connection, String.format("DELETE FROM %s", referenceTable.getSystemTableName()));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void truncateTable(Connection connection, String tableName) {
		String originTable = properties.getSchema() + "." + tableName;
		executeUpdate(connection, "TRUNCATE TABLE " + originTable);
		log.trace("[TRUNCATE] {}", originTable);
	}

	private void restoreTable(Connection connection, String tableName) {
		String originTable = properties.getSchema() + "." + tableName;
		String backupTable = properties.getBackupSchema() + "." + tableName;
		executeUpdate(connection, String.format("INSERT INTO %s SELECT * FROM %s", originTable, backupTable));
		log.trace("[RESTORATION] {}", originTable);
	}

	private void executeUpdate(Connection connection, String sql) {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

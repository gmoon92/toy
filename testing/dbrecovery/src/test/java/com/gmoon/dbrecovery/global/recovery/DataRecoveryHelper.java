package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.DataSourceProxy;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.global.recovery.vo.RecoveryTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DataRecoveryHelper {

	private final RecoveryDatabaseInitialization recoveryDatabase;
	private final RecoveryDatabaseProperties properties;
	private final DataSource dataSource;

	private void executeQuery(String queryString) {
		try (Connection connection = dataSource.getConnection();
			 CallableStatement callableStatement = connection.prepareCall(queryString)) {

			int result = callableStatement.executeUpdate();
			log.debug("[EXECUTE][{}]: {}", result, queryString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void recovery() {
		executeQuery("SET FOREIGN_KEY_CHECKS = 0");
		Set<String> tableNames = obtainRecoveryTables();
		for (String tableName : tableNames) {
			truncateTable(tableName);
			recoveryTable(tableName);
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
	}

	private Set<String> obtainRecoveryTables() {
		Map<Class<? extends Statement>, Set<String>> modifiedTables = DataSourceProxy.detectedStatementThreadLocal.get();
		Set<String> recoveryTables = modifiedTables.values()
			 .stream()
			 .flatMap(Collection::stream)
			 .collect(Collectors.toSet());

		RecoveryTable recoveryTable = recoveryDatabase.getRecoveryTable();
		Set<String> onDeleteTables = Optional.ofNullable(modifiedTables.get(Delete.class))
			 .orElseGet(HashSet::new)
			 .stream()
			 .map(recoveryTable::getDeleteTables)
			 .flatMap(Collection::stream)
			 .collect(Collectors.toSet());
		recoveryTables.addAll(onDeleteTables);
		return recoveryTables;
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

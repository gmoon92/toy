package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.DataSourceProxy;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.global.recovery.vo.TableMetaData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import org.springframework.stereotype.Component;

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
@Component
@RequiredArgsConstructor
public class DataRecoveryHelper {

	private final RecoveryDatabaseInitialization recoveryDatabase;
	private final RecoveryDatabaseProperties properties;
	private final DataSource dataSource;

	private void executeQuery(String queryString) {
		try (Connection connection = dataSource.getConnection();
			 CallableStatement callableStatement = connection.prepareCall(queryString)) {

			log.debug("[START] execute query: {}", queryString);
			int result = callableStatement.executeUpdate();
			log.debug("[END]   execute query({}): {}", result, queryString);
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

		TableMetaData metadata = recoveryDatabase.getMetadata();
		Set<String> onDeleteTables = Optional.ofNullable(modifiedTables.get(Delete.class))
			 .orElseGet(HashSet::new)
			 .stream()
			 .map(metadata::getDeleteTables)
			 .flatMap(Collection::stream)
			 .collect(Collectors.toSet());
		recoveryTables.addAll(onDeleteTables);
		return recoveryTables;
	}

	private void truncateTable(String tableName) {
		String originTable = properties.schema + "." + tableName;
		log.debug("[TRUNCATE] START {}", originTable);
		executeQuery("TRUNCATE TABLE " + originTable);
		log.debug("[TRUNCATE] DONE {}", originTable);
	}

	private void recoveryTable(String tableName) {
		String originTable = properties.schema + "." + tableName;
		String backupTable = properties.recoverySchema + "." + tableName;
		log.debug("[RECOVERY] START {}", originTable);
		executeQuery(String.format("INSERT INTO %s SELECT * FROM %s", originTable, backupTable));
		log.debug("[RECOVERY] DONE  {}", originTable);
	}
}

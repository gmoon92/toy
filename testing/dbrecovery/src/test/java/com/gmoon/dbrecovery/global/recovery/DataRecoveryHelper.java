package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.DataSourceProxy;
import com.gmoon.dbrecovery.global.recovery.vo.TableMetaData;
import com.gmoon.javacore.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <pre>
 * 빈 순서 정의
 * @DependsOn(value = {"dataSourceScriptDatabaseInitializer", "entityManagerFactory"})
 * </pre>
 *
 * @link https://discourse.hibernate.org/t/get-table-name-in-hibernate-6-2/8601
 * @see SqlDataSourceScriptDatabaseInitializer
 * @see JpaBaseConfiguration
 */
@Slf4j
//@DependsOn(value = { "dataSourceScriptDatabaseInitializer", "entityManagerFactory" })
@Component
@RequiredArgsConstructor
public class DataRecoveryHelper {

	private final DataSource dataSource;
	private final RecoveryDatabaseInitialization recoveryDatabase;

	@Value("${service.db-schema}")
	private String schema;

	private String getBackupSchema() {
		return schema + "_test";
	}

	private String obtainBackupTableName(String tableName) {
		String backupSchema = getBackupSchema();
		return backupSchema + "." + tableName;
	}

	private String obtainOriginTableName(String tableName) {
		return schema + "." + tableName;
	}

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
		Set<String> rollbackTables = new HashSet<>();
		Map<Class<? extends Statement>, Set<String>> modifiedTables = DataSourceProxy.modifiedTables;
		Set<String> result = modifiedTables.values()
			 .stream()
			 .flatMap(Collection::stream)
			 .collect(Collectors.toSet());

		Set<String> deleteTables = modifiedTables.get(Delete.class);
		// todo refactoring Inject into delete table after db initialization.
		if (CollectionUtils.isNotEmpty(deleteTables)) {
			Set<String> deleteTablesRecursively = getDeleteTablesRecursively(deleteTables);
			rollbackTables.addAll(deleteTablesRecursively);
		}

		rollbackTables.addAll(result);
		return rollbackTables;
	}

	public Set<String> getDeleteTablesRecursively(Set<String> deleteTables) {
		Set<String> result = new HashSet<>();

		TableMetaData metadata = recoveryDatabase.getMetadata();
		for (String tableName : deleteTables) {
			Set<String> caseCadeTables = metadata.getDeleteTableNames(tableName);
			result.addAll(getDeleteTablesRecursively(caseCadeTables));
		}

		result.addAll(deleteTables);
		return result;
	}

	private void truncateTable(String tableName) {
		String originTable = obtainOriginTableName(tableName);
		log.debug("[TRUNCATE] START {}", originTable);
		executeQuery("TRUNCATE TABLE " + originTable);
		log.debug("[TRUNCATE] DONE {}", originTable);
	}

	private void recoveryTable(String tableName) {
		String originTable = obtainOriginTableName(tableName);
		String backupTable = obtainBackupTableName(tableName);
		log.debug("[RECOVERY] START {}", originTable);
		executeQuery(String.format("INSERT INTO %s SELECT * FROM %s", originTable, backupTable));
		log.debug("[RECOVERY] DONE  {}", originTable);
	}
}

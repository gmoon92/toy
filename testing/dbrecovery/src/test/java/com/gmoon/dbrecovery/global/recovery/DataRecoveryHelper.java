package com.gmoon.dbrecovery.global.recovery;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
@DependsOn(value = { "dataSourceScriptDatabaseInitializer", "entityManagerFactory" })
@Component
@RequiredArgsConstructor
public class DataRecoveryHelper {

	private static Set<String> tableNames;
	private final DataSource dataSource;

	@Value("${service.db-schema}")
	private String schema;

	@PostConstruct
	public void init() {
		log.debug("===========Initializing data cleaner===============");
		tableNames = getTableNames();
		createBackupSchema(tableNames);
		log.debug("===========Initializing data cleaner===============");
	}

	private void createBackupSchema(Set<String> tableNames) {
		String backupSchema = getBackupSchema();

		executeQuery("SET FOREIGN_KEY_CHECKS = 0");
		executeQuery(String.format("CREATE DATABASE IF NOT EXISTS %s", backupSchema));
		for (String tableName : tableNames) {
			String originTable = obtainOriginTableName(tableName);
			String backupTable = obtainBackupTableName(tableName);
			log.info("Copy table {} to {}", originTable, backupTable);
			executeQuery(String.format("DROP TABLE IF EXISTS %s", backupTable));
			executeQuery(String.format("CREATE TABLE %s AS SELECT * FROM %s", backupTable, originTable));
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
	}

	private String getBackupSchema() {
		return schema + "_testback";
	}

	private String obtainBackupTableName(String tableName) {
		String backupSchema = getBackupSchema();
		return backupSchema + "." + tableName;
	}

	private String obtainOriginTableName(String tableName) {
		return schema + "." + tableName;
	}

	private void executeQuery(String queryString) {
		try (Connection connection = dataSource.getConnection()) {
			int result = connection.prepareCall(queryString).executeUpdate();
			log.debug("[{}] Executing query: {}", result, queryString);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> getTableNames() {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet resultSet = databaseMetaData.getTables(
				 connection.getCatalog(),
				 null,
				 "%",
				 new String[]{ "TABLE" }
			);

			Set<String> tableNames = new HashSet<>();
			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME");
				log.debug("table name: {}", tableName);
				tableNames.add(tableName);
			}

			return Collections.unmodifiableSet(tableNames);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void recovery() {
		// todo cache UPDATE, INSERT, DELETE tables.
		executeQuery("SET FOREIGN_KEY_CHECKS = 0");
		for (String tableName : tableNames) {
			truncateTable(tableName);
			recoveryTable(tableName);
			// todo backup transfer date
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
	}

	private void truncateTable(String tableName) {
		String originTable = obtainOriginTableName(tableName);
		log.debug("TRUNCATE {}", originTable);
		executeQuery("TRUNCATE TABLE " + originTable);
	}

	private void recoveryTable(String tableName) {
		String originTable = obtainOriginTableName(tableName);
		String backupTable = obtainBackupTableName(tableName);
		log.debug("RECOVERY {}", originTable);
		executeQuery(String.format("INSERT INTO %s SELECT * FROM %s", originTable, backupTable));
	}
}

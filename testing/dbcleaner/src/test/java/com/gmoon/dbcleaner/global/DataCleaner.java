package com.gmoon.dbcleaner.global;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @link https://discourse.hibernate.org/t/get-table-name-in-hibernate-6-2/8601
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCleaner {

	private static Set<String> tableNames;

	private final DataSource dataSource;

	private String schema = "dbcleaner";
	private String backupSchema = schema + "_testback";

	@PostConstruct
	public void init() {
		tableNames = getTableNames();
		createBackupSchema(tableNames);
	}

	private void createBackupSchema(Set<String> tableNames) {
		executeQuery(String.format("CREATE DATABASE IF NOT EXISTS %s", backupSchema));
		for (String tableName : tableNames) {
			String originTable = obtainOriginTableName(tableName);
			String backupTable = obtainBackupTableName(tableName);
			executeQuery(String.format("DROP TABLE IF EXISTS %s", backupTable));
			executeQuery(String.format("CREATE TABLE %s AS SELECT * FROM %s", backupTable, originTable));
		}
	}

	private String obtainBackupTableName(String tableName) {
		return backupSchema + "." + tableName;
	}

	private String obtainOriginTableName(String tableName) {
		return schema + "." + tableName;
	}

	private void executeQuery(String queryString) {
		try (Connection connection = dataSource.getConnection()) {
			connection.prepareCall(queryString).executeUpdate();
		} catch (Exception e) {
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
				log.trace("table name: {}", tableName);
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

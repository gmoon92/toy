package com.gmoon.dbcleaner.global;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @link https://discourse.hibernate.org/t/get-table-name-in-hibernate-6-2/8601
 */
@Slf4j
public class DataCleaner implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback {

	private static DataSource dataSource;
	private static Set<String> tableNames;

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		dataSource = obtainBean(extensionContext, DataSource.class);
		tableNames = getTableNames();
		createBackupTables(tableNames);
	}

	private void createBackupTables(Set<String> tableNames) throws SQLException {
		Connection connection = dataSource.getConnection();
		String schema = connection.getCatalog();
		String backupSchema = schema + "_testback";

		executeQuery(String.format("CREATE DATABASE IF NOT EXISTS %s", backupSchema));
		for (String tableName : tableNames) {
			String originTable = schema + "." + tableName;
			String backupTable = backupSchema + "." + tableName;
			executeQuery(String.format("DROP TABLE IF EXISTS %s", backupTable));
			executeQuery(String.format("CREATE TABLE %s AS SELECT * FROM %s", backupTable, originTable));
		}
	}

	private void executeQuery(String queryString) {
		try (Connection connection = dataSource.getConnection()) {
			connection.prepareCall(queryString).executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Set<String> getTableNames() {
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

	private static <T> T obtainBean(ExtensionContext context, Class<T> clazz) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(clazz);
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		log.info("setup db rollback.");
	}

	@Override
	@Transactional
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		log.info("[START] truncate table all.");
		recoveryTable(tableNames);
		log.info("[END]   truncate table all.");
	}

	private void recoveryTable(Set<String> tableNames) {
		// todo cache UPDATE, INSERT, DELETE tables.
		truncateTable(tableNames);
	}

	private void truncateTable(Set<String> tableNames) {
		executeQuery("SET FOREIGN_KEY_CHECKS = 0");
		for (String tableName : tableNames) {
			executeQuery("TRUNCATE TABLE " + tableName);
			// todo backup transfer date
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
	}
}

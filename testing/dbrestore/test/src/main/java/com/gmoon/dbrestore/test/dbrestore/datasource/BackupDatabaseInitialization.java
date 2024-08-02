package com.gmoon.dbrestore.test.dbrestore.datasource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * <pre>
 * DB 초기화 이후 빈 주입을 위해, @DependsOnDatabaseInitialization 사용
 * @DependsOn 는 type safe 하지 않음.
 * </pre>
 *
 * @link https://discourse.hibernate.org/t/get-table-name-in-hibernate-6-2/8601
 * @see SqlDataSourceScriptDatabaseInitializer
 * @see JpaBaseConfiguration
 */
@Slf4j
@DependsOnDatabaseInitialization
@RequiredArgsConstructor
public class BackupDatabaseInitialization implements InitializingBean {

	private final DataSource dataSource;
	private final ReferenceTable referenceTable;
	private final DatabaseRestoreProperties properties;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (existsBackupSchema()) {
			return;
		}

		String backupSchema = properties.getBackupSchema();
		log.trace("===========Initializing {} database===============", backupSchema);
		createBackupDatabase();
		log.trace("===========Initializing {} database===============", backupSchema);
	}

	private boolean existsBackupSchema() {
		String sql = "SELECT SCHEMA_NAME "
			 + "FROM INFORMATION_SCHEMA.SCHEMATA "
			 + "WHERE SCHEMA_NAME = ? ";

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, properties.getBackupSchema());
			statement.execute();

			ResultSet resultSet = statement.getResultSet();
			return resultSet.next();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createBackupDatabase() {
		try (Connection connection = dataSource.getConnection()) {
			executeUpdate(connection, "SET FOREIGN_KEY_CHECKS = 0");
			executeUpdate(connection, String.format("CREATE DATABASE %s", properties.getBackupSchema()));
			for (String tableName : referenceTable.getAllTables()) {
				String sourceTable = properties.getSchema() + "." + tableName;
				String targetTable = properties.getBackupSchema() + "." + tableName;
				executeUpdate(connection, String.format("CREATE TABLE %s AS SELECT * FROM %s", targetTable, sourceTable));
				log.trace("Copy table {} to {}", sourceTable, targetTable);
			}

			createSystemTable(connection);
			executeUpdate(connection, "SET FOREIGN_KEY_CHECKS = 1");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createSystemTable(Connection connection) {
		String sql = String.format("CREATE TABLE %s (" +
			 "    status     VARCHAR(10) DEFAULT 'WAIT', " +
			 "    created_at DATETIME    DEFAULT NOW() " +
			 ")", referenceTable.getSystemTableName());
		executeUpdate(connection, sql);
	}

	private void executeUpdate(Connection connection, String sql) {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			int result = statement.executeUpdate();
			log.trace("[EXECUTE][{}]: {}", result, sql);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

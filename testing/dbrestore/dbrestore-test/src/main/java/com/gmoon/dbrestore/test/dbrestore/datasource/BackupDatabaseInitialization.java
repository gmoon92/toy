package com.gmoon.dbrestore.test.dbrestore.datasource;

import java.sql.SQLException;
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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.UncategorizedDataAccessException;

/**
 * <pre>
 * DB 초기화 이후 빈 주입을 위해, <coe>@DependsOnDatabaseInitialization</coe> 사용
 * <code>@DependsOn</code> 는 type safe 하지 않음.
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
	public void afterPropertiesSet() {
		if (existsBackupSchema()) {
			return;
		}

		String backupSchema = properties.getBackupSchema();
		log.trace("===========Initializing {} database===============", backupSchema);
		createBackupDatabase();
		log.trace("===========Initializing {} database===============", backupSchema);
	}

	private boolean existsBackupSchema() {
		String sql = String.format("SELECT SCHEMA_NAME "
			 + "FROM INFORMATION_SCHEMA.SCHEMATA "
			 + "WHERE SCHEMA_NAME = '%s' ", properties.getBackupSchema());

		try (
			 Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql);
			 ResultSet resultSet = statement.executeQuery()
		) {
			return resultSet.next();
		} catch (SQLException e) {
			throw new DataAccessResourceFailureException("The existence of a backup schema is unknown.", e);
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
		} catch (SQLException e) {
			throw new DataAccessResourceFailureException("Backup schema cannot be created.", e);
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
		} catch (SQLException e) {
			throw new DataAccessResourceFailureException("The SQL statement is invalid.", e);
		}
	}
}

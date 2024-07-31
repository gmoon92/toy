package com.gmoon.dbrecovery.datasource;

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
public class RecoveryDatabaseInitialization implements InitializingBean {

	private final DataSource dataSource;
	private final Table recoveryTable;
	private final RecoveryDatabaseProperties properties;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (existsRecoverySchema()) {
			return;
		}

		String backupSchema = properties.getRecoverySchema();
		log.trace("===========Initializing recovery {} database===============", backupSchema);
		createRecoverySchema();
		log.trace("===========Initializing recovery {} database===============", backupSchema);
	}

	private boolean existsRecoverySchema() {
		String sql = "SELECT SCHEMA_NAME "
			 + "FROM INFORMATION_SCHEMA.SCHEMATA "
			 + "WHERE SCHEMA_NAME = ? ";

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, properties.getRecoverySchema());
			statement.execute();

			ResultSet resultSet = statement.getResultSet();
			return resultSet.next();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createRecoverySchema() {
		try (Connection connection = dataSource.getConnection()) {
			executeQuery(connection, "SET FOREIGN_KEY_CHECKS = 0");
			executeQuery(connection, String.format("CREATE DATABASE %s", properties.getRecoverySchema()));
			for (String tableName : recoveryTable.getTableAll()) {
				String sourceTable = properties.getSchema() + "." + tableName;
				String targetTable = properties.getRecoverySchema() + "." + tableName;
				executeQuery(connection, String.format("CREATE TABLE %s AS SELECT * FROM %s", targetTable, sourceTable));
				log.trace("Copy table {} to {}", sourceTable, targetTable);
			}
			executeQuery(connection, "SET FOREIGN_KEY_CHECKS = 1");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void executeQuery(Connection connection, String sql) {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			int result = statement.executeUpdate();
			log.trace("[EXECUTE][{}]: {}", result, sql);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

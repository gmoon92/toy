package com.gmoon.dbrecovery.global.recovery.datasource;

import com.gmoon.dbrecovery.global.recovery.RecoveryTable;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;

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
// @DependsOn(value = {"dataSourceScriptDatabaseInitializer", "entityManagerFactory"})
@DependsOnDatabaseInitialization
@RequiredArgsConstructor
public class RecoveryDatabaseInitialization implements InitializingBean {

	private final DataSource dataSource;
	private final RecoveryTable recoveryTable;
	private final RecoveryDatabaseProperties properties;

	@Override
	public void afterPropertiesSet() throws Exception {
		String backupSchema = properties.getBackupSchema();
		log.debug("===========Initializing recovery {} database===============", backupSchema);
		createRecoverySchema();
		log.debug("===========Initializing recovery {} database===============", backupSchema);
	}

	private void createRecoverySchema() {
		executeQuery("SET FOREIGN_KEY_CHECKS = 0");
		executeQuery(String.format("CREATE DATABASE IF NOT EXISTS %s", properties.getBackupSchema()));
		for (String tableName : recoveryTable.getTableAll()) {
			String sourceTable = properties.getSchema() + "." + tableName;
			String targetTable = properties.getBackupSchema() + "." + tableName;
			executeQuery(String.format("DROP TABLE IF EXISTS %s", targetTable));
			executeQuery(String.format("CREATE TABLE %s AS SELECT * FROM %s", targetTable, sourceTable));
			log.debug("Copy table {} to {}", sourceTable, targetTable);
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
	}

	private void executeQuery(String queryString) {
		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(queryString)) {

			int result = statement.executeUpdate();
			log.debug("[EXECUTE][{}]: {}", result, queryString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

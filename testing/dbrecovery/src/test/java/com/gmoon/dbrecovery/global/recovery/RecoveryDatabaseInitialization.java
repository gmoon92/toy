package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.global.recovery.vo.RecoveryTable;
import com.gmoon.dbrecovery.global.recovery.vo.Table;
import com.gmoon.dbrecovery.global.recovery.vo.TableMetadata;
import lombok.Getter;
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
import java.util.ArrayList;
import java.util.List;

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
	private final RecoveryDatabaseProperties properties;

	@Getter
	private RecoveryTable recoveryTable;

	@Override
	public void afterPropertiesSet() throws Exception {
		String backupSchema = properties.getBackupSchema();
		log.debug("===========Initializing recovery {} database===============", backupSchema);
		recoveryTable = obtainRecoveryTable();
		createRecoverySchema(recoveryTable);
		log.debug("===========Initializing recovery {} database===============", backupSchema);
	}

	private RecoveryTable obtainRecoveryTable() {
		String queryString =
			 "SELECT kcu.TABLE_NAME             AS table_name, " +
				  "  kcu.COLUMN_NAME            AS table_key_column_name, " +
				  "  kcu.REFERENCED_TABLE_NAME  AS ref_table_name, " +
				  "  kcu.REFERENCED_COLUMN_NAME AS ref_column_name, " +
				  "  CASE WHEN rc.DELETE_RULE = 'CASCADE' " +
				  "      THEN 1 " +
				  "      ELSE 0 " +
				  "      END AS on_delete " +
				  "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu " +
				  "         LEFT JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc " +
				  "              ON kcu.CONSTRAINT_NAME = rc.CONSTRAINT_NAME " +
				  "                  AND kcu.CONSTRAINT_SCHEMA = rc.CONSTRAINT_SCHEMA " +
				  "WHERE kcu.TABLE_SCHEMA = ? ";

		try (Connection connection = dataSource.getConnection();
			 PreparedStatement statement = connection.prepareStatement(queryString)) {
			statement.setString(1, properties.getSchema());
			statement.execute();

			ResultSet resultSet = statement.getResultSet();
			List<TableMetadata> metadata = new ArrayList<>();
			while (resultSet.next()) {
				metadata.add(TableMetadata.builder()
					 .tableName(resultSet.getString("table_name"))
					 .tableKeyName(resultSet.getString("table_key_column_name"))
					 .referenceTableName(resultSet.getString("ref_table_name"))
					 .referenceColumnName(resultSet.getString("ref_column_name"))
					 .onDelete(resultSet.getInt("on_delete"))
					 .build());
			}
			return RecoveryTable.initialize(metadata);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	private void createRecoverySchema(RecoveryTable recoveryTable) {
		executeQuery("SET FOREIGN_KEY_CHECKS = 0");
		executeQuery(String.format("CREATE DATABASE IF NOT EXISTS %s", properties.getBackupSchema()));
		for (Table table : recoveryTable.getAll()) {
			String tableName = table.getName();
			String sourceTable = properties.getSchema() + "." + tableName;
			String targetTable = properties.getBackupSchema() + "." + tableName;
			executeQuery(String.format("DROP TABLE IF EXISTS %s", targetTable));
			executeQuery(String.format("CREATE TABLE %s AS SELECT * FROM %s", targetTable, sourceTable));
			log.debug("Copy table {} to {}", sourceTable, targetTable);
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
	}
}

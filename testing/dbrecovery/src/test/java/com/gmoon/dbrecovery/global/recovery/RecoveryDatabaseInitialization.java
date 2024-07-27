package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.global.recovery.vo.Table;
import com.gmoon.dbrecovery.global.recovery.vo.TableMetaData;
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
	private TableMetaData metadata;

	@Override
	public void afterPropertiesSet() throws Exception {
		String recoverySchema = properties.getRecoverySchema();
		log.debug("===========Initializing recovery {} database===============", recoverySchema);
		metadata = obtainTableMetaData();
		createRecoverySchema(metadata);
		log.debug("===========Initializing recovery {} database===============", recoverySchema);
	}

	private TableMetaData obtainTableMetaData() {
		String queryString =
			 "SELECT kcu.TABLE_NAME             AS table_name, " +
				  "  kcu.COLUMN_NAME            AS table_pk_column_name, " +
				  "  kcu.REFERENCED_TABLE_NAME  AS ref_table_name, " +
				  "  kcu.REFERENCED_COLUMN_NAME AS ref_table_pk_column_name, " +
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
			List<Table> tables = new ArrayList<>();
			while (resultSet.next()) {
				String tableName = resultSet.getString("table_name");
				String tablePkColumnName = resultSet.getString("table_pk_column_name");
				String refTableName = resultSet.getString("ref_table_name");
				String refTablePkColumnName = resultSet.getString("ref_table_pk_column_name");
				int onDelete = resultSet.getInt("on_delete");

				Table table = Table.builder()
					 .tableName(tableName)
					 .tablePKColumnName(tablePkColumnName)
					 .referenceTableName(refTableName)
					 .referenceTablePKColumnName(refTablePkColumnName)
					 .onDelete(onDelete)
					 .build();
				tables.add(table);
			}
			return TableMetaData.initialize(tables);
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

	private void createRecoverySchema(TableMetaData metadata) {
		executeQuery("SET FOREIGN_KEY_CHECKS = 0");
		executeQuery(String.format("CREATE DATABASE IF NOT EXISTS %s", properties.getRecoverySchema()));
		for (Table table : metadata.getValue().keySet()) {
			String tableName = table.getTableName();
			String sourceTable = properties.getSchema() + "." + tableName;
			String targetTable = properties.getRecoverySchema() + "." + tableName;
			executeQuery(String.format("DROP TABLE IF EXISTS %s", targetTable));
			executeQuery(String.format("CREATE TABLE %s AS SELECT * FROM %s", targetTable, sourceTable));
			log.debug("Copy table {} to {}", sourceTable, targetTable);
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
	}
}

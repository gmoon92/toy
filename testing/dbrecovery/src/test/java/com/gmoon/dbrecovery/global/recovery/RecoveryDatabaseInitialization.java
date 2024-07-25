package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.vo.Table;
import com.gmoon.dbrecovery.global.recovery.vo.TableMetaData;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 빈 순서 정의
 * @DependsOn(value = {"dataSourceScriptDatabaseInitializer", "entityManagerFactory"})
 *
 * DB 생성 이후 초기화 하기 위해, @DependsOnDatabaseInitialization 사용
 * </pre>
 *
 * @link https://discourse.hibernate.org/t/get-table-name-in-hibernate-6-2/8601
 * @see SqlDataSourceScriptDatabaseInitializer
 * @see JpaBaseConfiguration
 */
@Slf4j
@DependsOnDatabaseInitialization
@Component
@RequiredArgsConstructor
public class RecoveryDatabaseInitialization implements InitializingBean, DisposableBean {

	private final EntityManager entityManager;
	private final DataSource dataSource;

	@Getter
	private TableMetaData metadata;

	@Value("${service.dbrecovery.schema}")
	private String schema;

	@Value("${service.dbrecovery.backup-schema}")
	private String backupSchema;

	@Override
	public void afterPropertiesSet() throws Exception {
		log.debug("===========Initializing recovery database===============");
		metadata = obtainTableMetaData();
		createTargetSchema(metadata);
		log.debug("===========Initializing recovery database===============");
	}

	@Override
	public void destroy() throws Exception {
		log.debug("===========Destroying recovery database===============");
		executeQuery("DROP SCHEMA IF EXISTS " + backupSchema);
		log.debug("===========Destroying recovery database===============");
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	private TableMetaData obtainTableMetaData() {
		try (Connection connection = dataSource.getConnection()) {
			log.debug("[Start] table metadata info.");
			String queryString = "SELECT kcu.TABLE_NAME             AS table_name, " +
				 "       kcu.COLUMN_NAME            AS table_pk_column_name, " +
				 "       kcu.REFERENCED_TABLE_NAME  AS ref_table_name, " +
				 "       kcu.REFERENCED_COLUMN_NAME AS ref_table_pk_column_name, " +
				 "       CASE WHEN rc.DELETE_RULE = 'CASCADE' " +
				 "           THEN 1 " +
				 "           ELSE 0 " +
				 "           END AS on_delete " +
				 "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu " +
				 "         LEFT JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc " +
				 "              ON kcu.CONSTRAINT_NAME = rc.CONSTRAINT_NAME " +
				 "                  AND kcu.CONSTRAINT_SCHEMA = rc.CONSTRAINT_SCHEMA " +
				 "WHERE kcu.TABLE_SCHEMA = ? ";

			PreparedStatement preparedStatement = connection.prepareStatement(queryString);
			preparedStatement.setString(1, schema);
			preparedStatement.execute();

			ResultSet resultSet = preparedStatement.getResultSet();
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
				log.debug("{}", table);
			}
			log.debug("[END]   table metadata info.");
			return TableMetaData.initialize(tables);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void executeQuery(String queryString) {
		try (Connection connection = dataSource.getConnection();
			 CallableStatement callableStatement = connection.prepareCall(queryString)) {

			log.debug("[START] execute query: {}", queryString);
			int result = callableStatement.executeUpdate();
			log.debug("[END{}] execute query: {}", result, queryString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void createTargetSchema(TableMetaData metadata) {
		String sourceSchema = schema;
		String targetSchema = backupSchema;
		executeQuery("SET FOREIGN_KEY_CHECKS = 0");
		executeQuery(String.format("CREATE DATABASE IF NOT EXISTS %s", targetSchema));
		for (Table table : metadata.getValue().keySet()) {
			String tableName = table.getTableName();
			String sourceTable = sourceSchema + "." + tableName;
			String targetTable = targetSchema + "." + tableName;
			log.debug("Copy table {} to {}", sourceTable, targetTable);
			executeQuery(String.format("DROP TABLE IF EXISTS %s", targetTable));
			executeQuery(String.format("CREATE TABLE %s AS SELECT * FROM %s", targetTable, sourceTable));
		}
		executeQuery("SET FOREIGN_KEY_CHECKS = 1");
	}

}

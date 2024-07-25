package com.gmoon.dbrecovery.global.recovery.vo;

import com.gmoon.dbrecovery.global.recovery.DataRecovery;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static com.gmoon.dbrecovery.global.test.Fixtures.newOnDeleteTable;
import static com.gmoon.dbrecovery.global.test.Fixtures.newTable;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@DataRecovery
class TableMetaDataTest {

	@Autowired
	private DataSource datasource;

	@Value("${service.db-schema}")
	private String schema;

	@Test
	void test() {
		try (Connection connection = datasource.getConnection()) {
			log.trace("[Start] table metadata info.");
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
				log.trace("{}", table);
			}
			TableMetaData metaData = TableMetaData.initialize(tables);
			log.trace("metadata: {}", metaData);
			log.trace("[END]   table metadata info.");

			assertThat(metaData.getTotalCount()).isEqualTo(5);
			assertThat(metaData.getDeleteTables(newTable("tb_ticket_office")))
				 .containsExactly(
					  newOnDeleteTable("tb_movie"),
					  newOnDeleteTable("tb_ticket")
				 );
			assertThat(metaData.getDeleteTables(newTable("tb_ticket")))
				 .containsExactly(
					  newOnDeleteTable("tb_movie_ticket")
				 );
			assertThat(metaData.getDeleteTables(newTable("tb_movie")))
				 .containsExactly(
					  newOnDeleteTable("tb_coupon"),
					  newOnDeleteTable("tb_movie_ticket")
				 );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

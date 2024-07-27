package com.gmoon.dbrecovery.global.recovery.vo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RecoveryTableTest {

	/**
	 * +----------------+--------------------+----------------+------------------------+---------+
	 * |table_name      |table_pk_column_name|ref_table_name  |ref_table_pk_column_name|on_delete|
	 * +----------------+--------------------+----------------+------------------------+---------+
	 * |tb_ticket_office|id                  |null            |null                    |0        |
	 * |tb_ticket       |ticket_office_id    |tb_ticket_office|id                      |1        |
	 * |tb_ticket       |id                  |null            |null                    |0        |
	 * |tb_movie_ticket |ticket_id           |null            |null                    |0        |
	 * |tb_movie_ticket |movie_id            |tb_movie        |id                      |1        |
	 * |tb_movie_ticket |ticket_id           |tb_ticket       |id                      |1        |
	 * |tb_movie_ticket |movie_id            |null            |null                    |0        |
	 * |tb_movie        |ticket_office_id    |tb_ticket_office|id                      |1        |
	 * |tb_movie        |id                  |null            |null                    |0        |
	 * |tb_coupon       |movie_id            |tb_movie        |id                      |1        |
	 * |tb_coupon       |id                  |null            |null                    |0        |
	 * +----------------+--------------------+----------------+------------------------+---------+
	 */
	@Test
	void test() {
		List<TableMetadata> metadata = List.of(
			 TableMetadata.builder().tableName("tb_ticket_office").tablePKColumnName("id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_ticket").tablePKColumnName("ticket_office_id").referenceTableName("tb_ticket_office").referenceTablePKColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_ticket").tablePKColumnName("id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_movie_ticket").tablePKColumnName("ticket_id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_movie_ticket").tablePKColumnName("movie_id").referenceTableName("tb_movie").referenceTablePKColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_movie_ticket").tablePKColumnName("ticket_id").referenceTableName("tb_ticket").referenceTablePKColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_movie_ticket").tablePKColumnName("movie_id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_movie").tablePKColumnName("ticket_office_id").referenceTableName("tb_ticket_office").referenceTablePKColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_movie").tablePKColumnName("id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_coupon").tablePKColumnName("movie_id").referenceTableName("tb_movie").referenceTablePKColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_coupon").tablePKColumnName("id").onDelete(0).build()
		);

		RecoveryTable recoveryTable = RecoveryTable.initialize(metadata);

		assertThat(recoveryTable.getAll().size()).isEqualTo(5);
		assertThat(recoveryTable.getDeleteTables("tb_ticket_office")).containsOnly("tb_ticket", "tb_movie", "tb_movie_ticket", "tb_coupon");
		assertThat(recoveryTable.getDeleteTables("tb_ticket")).containsOnly("tb_movie_ticket");
		assertThat(recoveryTable.getDeleteTables("tb_movie")).containsOnly("tb_movie_ticket", "tb_coupon");
		assertThat(recoveryTable.getDeleteTables("tb_movie_ticket")).isEmpty();
		assertThat(recoveryTable.getDeleteTables("tb_coupon")).isEmpty();
	}

}

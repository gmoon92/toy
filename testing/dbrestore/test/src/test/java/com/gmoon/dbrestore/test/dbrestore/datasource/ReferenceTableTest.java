package com.gmoon.dbrestore.test.dbrestore.datasource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReferenceTableTest {


	/**
	 * <pre>
	 * +----------------+--------------------+----------------+------------------------+---------+
	 * |table_name      |table_key_column_name|ref_table_name  |ref_column_name		   |on_delete|
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
	 * </pre>
	 */
	@Test
	void test() {
		List<TableMetadata> metadata = List.of(
			 TableMetadata.builder().tableName("tb_ticket_office").tableKeyName("id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_ticket").tableKeyName("id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_ticket").tableKeyName("ticket_office_id").referenceTableName("tb_ticket_office").referenceColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_ticket").tableKeyName("parent_id").referenceTableName("tb_ticket").referenceColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_movie_ticket").tableKeyName("ticket_id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_movie_ticket").tableKeyName("movie_id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_movie_ticket").tableKeyName("ticket_id").referenceTableName("tb_ticket").referenceColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_movie_ticket").tableKeyName("movie_id").referenceTableName("tb_movie").referenceColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_movie").tableKeyName("id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_movie").tableKeyName("ticket_office_id").referenceTableName("tb_ticket_office").referenceColumnName("id").onDelete(1).build(),
			 TableMetadata.builder().tableName("tb_coupon").tableKeyName("id").onDelete(0).build(),
			 TableMetadata.builder().tableName("tb_coupon").tableKeyName("movie_id").referenceTableName("tb_movie").referenceColumnName("id").onDelete(1).build()
		);

		ReferenceTable referenceTable = new ReferenceTable(null, null);
		referenceTable.initialize(metadata);

		assertThat(referenceTable.getAllTables()).hasSize(5);
		assertThat(referenceTable.getDeleteTables("tb_ticket_office")).containsOnly("tb_ticket", "tb_movie", "tb_movie_ticket", "tb_coupon");
		assertThat(referenceTable.getDeleteTables("tb_ticket")).containsOnly("tb_movie_ticket");
		assertThat(referenceTable.getDeleteTables("tb_movie")).containsOnly("tb_movie_ticket", "tb_coupon");
		assertThat(referenceTable.getDeleteTables("tb_movie_ticket")).isEmpty();
		assertThat(referenceTable.getDeleteTables("tb_coupon")).isEmpty();
	}
}

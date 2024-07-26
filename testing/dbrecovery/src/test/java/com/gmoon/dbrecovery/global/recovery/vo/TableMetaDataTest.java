package com.gmoon.dbrecovery.global.recovery.vo;

import com.gmoon.dbrecovery.global.recovery.RecoveryDatabaseInitialization;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class TableMetaDataTest {

	@Autowired
	private RecoveryDatabaseInitialization initialization;

	/**
	 * +----------------+--------------------+----------------+------------------------+---------+
	 * |table_name      |table_pk_column_name|ref_table_name  |ref_table_pk_column_name|on_delete|
	 * +----------------+--------------------+----------------+------------------------+---------+
	 * |tb_movie_ticket |movie_id            |tb_movie        |id                      |1        |
	 * |tb_movie_ticket |ticket_id           |tb_ticket       |id                      |1        |
	 * |tb_ticket       |ticket_office_id    |tb_ticket_office|id                      |1        |
	 * |tb_movie        |ticket_office_id    |tb_ticket_office|id                      |1        |
	 * |tb_coupon       |movie_id            |tb_movie        |id                      |1        |
	 * |tb_movie_ticket |movie_id            |null            |null                    |0        |
	 * |tb_movie_ticket |ticket_id           |null            |null                    |0        |
	 * |tb_ticket       |id                  |null            |null                    |0        |
	 * |tb_ticket_office|id                  |null            |null                    |0        |
	 * |tb_movie        |id                  |null            |null                    |0        |
	 * |tb_coupon       |id                  |null            |null                    |0        |
	 * +----------------+--------------------+----------------+------------------------+---------+
	 */
	@Test
	void test() {
		TableMetaData metadata = initialization.getMetadata();

		log.info("tb_ticket_office  : {}", metadata.getDeleteTables("tb_ticket_office"));
		log.info("tb_movie_ticket   : {}", metadata.getDeleteTables("tb_movie_ticket"));
		log.info("tb_ticket         : {}", metadata.getDeleteTables("tb_ticket"));
		log.info("tb_movie          : {}", metadata.getDeleteTables("tb_movie"));
		log.info("tb_coupon         : {}", metadata.getDeleteTables("tb_coupon"));
	}
}

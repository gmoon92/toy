package com.gmoon.dbrecovery.global.recovery.datasource;

import com.gmoon.dbrecovery.global.recovery.datasource.vo.KeyValue;
import com.gmoon.dbrecovery.global.recovery.vo.TableKey;
import com.gmoon.dbrecovery.global.recovery.vo.TableMetadata;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SqlParserTest {

	/**
	 * insert -> delete
	 * update -> update
	 * delete -> insert with casecade on delete depenendt tables
	 */
	@Nested
	class RecoverySqlTest {

		private final String schema = "dbrecovery";
		private final String backup = "dbrecovery_backup";

		@DisplayName("insert -> delete")
		@Test
		void insert() {
			String sql = "insert into zt_revision_info (timestamp, updated_by, updated_by_username) values (1722315616046, NULL, NULL)";

			String recoverySql = SqlParser.getInsertRecoverySql(sql);
			log.info("sql         : {}", sql);
			log.info("recoverySql : {}", recoverySql);

			assertThat(recoverySql).isEqualTo(
				 "DELETE FROM zt_revision_info WHERE timestamp = 1722315616046 AND updated_by IS NULL AND updated_by_username IS NULL");
		}

		/**
		 * <pre>
		 * UPDATE schema.users
		 * 	 JOIN schema_backup.users_backup ON schema.id = schema_backup.id
		 *    SET schema.name 		= schema_backup.name,
		 * 	      schema.age 		= schema_backup.age,
		 * 	      schema.email 		= schema_backup.email,
		 * 	      schema.created_at = schema_backup.created_at
		 *  WHERE schema.users.id = <ID_OF_MODIFIED_RECORD>;
		 * </pre>
		 */
		@DisplayName("update -> update")
		@Test
		void update() {
			String sql = "update tb_ticket set status='1', timezone='Asia/Tokyo' where id = 100";
			Set<KeyValue> keyValues = obtainTableKeyValues(sql);

			String recoverySql = SqlParser.getUpdateRecoverySql(schema, backup, sql, keyValues);
			log.info("sql         : {}", sql);
			log.info("recoverySql : {}", recoverySql);
			assertThat(recoverySql).isEqualTo(
				 "UPDATE dbrecovery.tb_ticket INNER JOIN dbrecovery_backup.tb_ticket ON dbrecovery.tb_ticket.id = dbrecovery_backup.tb_ticket.id SET dbrecovery.tb_ticket.status = dbrecovery_backup.tb_ticket.status, dbrecovery.tb_ticket.timezone = dbrecovery_backup.tb_ticket.timezone WHERE dbrecovery.tb_ticket.id = 100");

		}

		private Set<KeyValue> obtainTableKeyValues(String sql) {
			Set<TableKey> keys = new HashSet<>();
			keys.add(TableKey.from(TableMetadata.builder()
				 .tableName("tb_ticket")
				 .tableKeyName("id")
				 .build()));

			PlainSelect plainSelect = SqlParser.selectRecordForUpdate(schema, sql);

			// datasource execute call id process...

			Set<KeyValue> keyValues = new HashSet<>();
			keyValues.add(new KeyValue("id", "100"));
			return keyValues;
		}

		/**
		 * <pre>
		 * INSERT INTO schema.tb_ticket
		 *      SELECT *
		 *      FROM schema_backup.tb_ticket
		 *      WHERE schema_backup.tb_ticket.id = 1;
		 * </pre>
		 */
		@DisplayName("delete -> insert")
		@Test
		void delete() {
			String sql = "delete from tb_ticket where id = 100";

			Set<KeyValue> keyValues = new HashSet<>();
			keyValues.add(new KeyValue("id", "100"));

			String recoverySql = SqlParser.getDeleteRecoverySql(schema, backup, sql, keyValues);
			log.info("sql         : {}", sql);
			log.info("recoverySql : {}", recoverySql);

			assertThat(recoverySql).isEqualTo("INSERT INTO dbrecovery.tb_ticket "
				 + "SELECT * FROM dbrecovery_backup.tb_ticket WHERE dbrecovery_backup.tb_ticket.id = 100");
		}
	}
}

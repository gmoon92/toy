package com.gmoon.springjooq.domain;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springjooq.accesslog.domain.AccessLog;
import com.gmoon.springjooq.accesslog.domain.vo.OperatingSystem;
import com.gmoon.springjooq.global.jooqschema.enums.TbAccessLogOs;
import com.gmoon.springjooq.global.jooqschema.tables.TbAccessLog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class JooqCRUDOperationsTest {

	@Autowired
	private DSLContext dsl;
	private TbAccessLog jAccessLog = TbAccessLog.TB_ACCESS_LOG;

	/***
	 * insert into
	 * "PUBLIC"."TB_ACCESS_LOG" ("ID", "IP", "OS", "USERNAME")
	 * values ('alr000001', '254.0.0.0', 'MAC', 'guest')
	 */
	@Test
	void create() {
		dsl.insertInto(jAccessLog)
			 .set(jAccessLog.ID, "alr000001")
			 .set(jAccessLog.USERNAME, "guest")
			 .set(jAccessLog.IP, "254.0.0.0")
			 .set(jAccessLog.OS, TbAccessLogOs.MAC)
			 .execute();

		// Caused by: org.h2.jdbc.JdbcSQLSyntaxErrorException: Syntax error in SQL statement
		// "insert into ""PUBLIC"".""TB_ACCESS_LOG"" (""ID"", ""USERNAME"", ""IP"", ""OS"") values (?, ?, ?, ?) [*]returning ""PUBLIC"".""TB_ACCESS_LOG"".""ID""";
		// SQL statement:
		// TbAccessLogRecord record = dsl.newRecord(jAccessLog);
		// record.setId("alr000001");
		// record.setUsername("guest");
		// record.setIp("254.0.0.0");
		// record.setOs(TbAccessLogOs.MAC);
		// record.store();
	}

	/***
	 * update "PUBLIC"."TB_ACCESS_LOG"
	 *    set "PUBLIC"."TB_ACCESS_LOG"."USERNAME" = ?,
	 *        "PUBLIC"."TB_ACCESS_LOG"."OS" = ?
	 *  where "PUBLIC"."TB_ACCESS_LOG"."ID" = ?
	 */
	@Test
	void update() {
		dsl.update(jAccessLog)
			 .set(jAccessLog.USERNAME, "test")
			 .set(jAccessLog.OS, TbAccessLogOs.LINUX)
			 .where(jAccessLog.ID.eq("none"))
			 .execute();
	}

	@Nested
	class Read {

		/**
		 * select "PUBLIC"."TB_ACCESS_LOG"."ID",
		 * "PUBLIC"."TB_ACCESS_LOG"."ATTEMPT_DT",
		 * "PUBLIC"."TB_ACCESS_LOG"."IP",
		 * "PUBLIC"."TB_ACCESS_LOG"."OS",
		 * "PUBLIC"."TB_ACCESS_LOG"."USERNAME"
		 * from "PUBLIC"."TB_ACCESS_LOG"
		 * +-----+-------------------+---------+------+--------+
		 * |ID   |ATTEMPT_DT         |IP       |OS    |USERNAME|
		 * +-----+-------------------+---------+------+--------+
		 * |tal01|2022-02-24T12:34:43|220.0.0.0|WINDOW|admin   |
		 * |tal02|2022-02-24T12:34:43|220.0.0.0|WINDOW|admin   |
		 * |tal03|2022-02-24T12:34:43|220.0.0.0|WINDOW|admin   |
		 * |tal04|2022-02-24T12:34:43|220.0.0.0|WINDOW|admin   |
		 * |tal05|2022-02-24T12:34:43|220.0.0.0|WINDOW|admin   |
		 * +-----+-------------------+---------+------+--------+
		 */
		@Nested
		class FetchTest {

			@Test
			void fetch() {
				Result<Record> fetch = dsl.select()
					 .from(jAccessLog)
					 .fetch();

				List<TbAccessLogOs> values1 = fetch.getValues(jAccessLog.OS);
				List<OperatingSystem> values2 = fetch.getValues(4, OperatingSystem.class);
				List<OperatingSystem> values3 = fetch.getValues(jAccessLog.OS, OperatingSystem.class);
				log.info("values1: {}", values1);
				log.info("values2: {}", values2);
				log.info("values3: {}", values3);
				assertThat(values1).isNotEmpty();
				assertThat(values2).isEqualTo(values3);

				fetch.forEach(accessLog -> {
					String username = accessLog.get(jAccessLog.USERNAME);
					String ip = accessLog.get(jAccessLog.IP);
					TbAccessLogOs os = accessLog.get(jAccessLog.OS);
					OperatingSystem osEnum = accessLog.get(jAccessLog.OS, OperatingSystem.class);
					LocalDateTime attemptDt = accessLog.get(jAccessLog.ATTEMPT_AT);

					assertThat(username).isNotBlank();
					assertThat(ip).isNotBlank();
					assertThat(os).isNotNull();
					assertThat(osEnum).isNotNull();
					assertThat(attemptDt).isNotNull();
				});
			}

			/***
			 * select "PUBLIC"."TB_ACCESS_LOG"."ID"
			 *   from "PUBLIC"."TB_ACCESS_LOG"
			 *  where ("PUBLIC"."TB_ACCESS_LOG"."ID" is not null
			 *   and "PUBLIC"."TB_ACCESS_LOG"."OS" = ?
			 *   and "PUBLIC"."TB_ACCESS_LOG"."IP" is not null)
			 * group by "PUBLIC"."TB_ACCESS_LOG"."ID"
			 * having count(*) > ?
			 * limit ? offset ?
			 * */
			@Test
			void fetchInto() {
				List<AccessLog> accessLogs = dsl.select(jAccessLog.ID)
					 .from(jAccessLog)
					 .where(
						  jAccessLog.ID.isNotNull(),
						  jAccessLog.OS.eq(TbAccessLogOs.WINDOW)
							   .and(jAccessLog.IP.isNotNull())
					 )
					 .groupBy(jAccessLog.ID)
					 .having(DSL.count().gt(0))
					 .limit(2)
					 .offset(0)
					 .fetchInto(AccessLog.class);

				assertThat(accessLogs).isNotEmpty()
					 .allMatch(row ->
						  StringUtils.isNotBlank(row.getId())
							   && StringUtils.isBlank(row.getIp())
							   && StringUtils.isBlank(row.getUsername())
							   && row.getOs() == null);
			}
		}

		@Nested
		class FindOneTest {

			/**
			 * select "PUBLIC"."TB_ACCESS_LOG"."OS"
			 * from "PUBLIC"."TB_ACCESS_LOG"
			 * where "PUBLIC"."TB_ACCESS_LOG"."OS" is null
			 * limit ?
			 */
			@Test
			void fetchOne() {
				TbAccessLogOs result = dsl.select(jAccessLog.OS)// 컬럼을 지정하자.
					 .from(jAccessLog)
					 .where(jAccessLog.OS.isNull())
					 .limit(1) // fetch one 반드시 지정
					 .fetchOne(jAccessLog.OS);

				assertThat(result).isNull();
			}

			/**
			 * select "PUBLIC"."TB_ACCESS_LOG"."ID",
			 * "PUBLIC"."TB_ACCESS_LOG"."ATTEMPT_DT",
			 * "PUBLIC"."TB_ACCESS_LOG"."IP",
			 * PUBLIC"."TB_ACCESS_LOG"."OS",
			 * "PUBLIC"."TB_ACCESS_LOG"."USERNAME"
			 * from "PUBLIC"."TB_ACCESS_LOG"
			 * where "PUBLIC"."TB_ACCESS_LOG"."OS" is null
			 * limit ?
			 */
			@Test
			void fetchOne2() {
				TbAccessLogOs result = dsl.select()
					 .from(jAccessLog)
					 .where(jAccessLog.OS.isNull())
					 .limit(1) // fetch one 반드시 지정
					 .fetchOne(jAccessLog.OS);

				assertThat(result).isNull();
			}
		}
	}

	/**
	 * delete from "PUBLIC"."TB_ACCESS_LOG"
	 *  where "PUBLIC"."TB_ACCESS_LOG"."ID" = 'alr000001'
	 * */
	@Test
	void delete() {
		dsl.delete(jAccessLog)
			 .where(jAccessLog.ID.eq("alr000001"))
			 .execute();
	}
}

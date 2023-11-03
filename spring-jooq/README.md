# Spring JOOQ

## Environment

- spring-boot-starter:spring-boot:2.7.8
- mariadb:10.3
- org.jooq:jooq:3.14.16
- jdk 1.8

```sh
cd docker
docker compose -p batchinsert up -d
```

## [Generated source](https://www.jooq.org/doc/3.18/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step3/)

### [Java config](https://www.jooq.org/doc/latest/manual/code-generation/codegen-programmatic/)

java config 설정시 GenerationTool jooq version 3.11.0 이상 지원

- org.jooq:jooq-meta:${jooq.version}
- org.jooq:jooq-meta:${jooq.version}
- org.jooq:jooq-meta:${jooq.version}

```java
import org.jooq.meta.jaxb.*;

@Configuration
public class JooqConfig {

	@PostConstruct
	public void generateSource() throws Exception {
		Configuration configuration = new Configuration()
		  // jdbc config
		  .withJdbc(new Jdbc()
			.withDriver("org.h2.Driver")
			.withUrl("jdbc:h2:mem:test")
			.withUser("sa")
			.withPassword(""))
		  .withGenerator(new Generator()
			// jooq meta database config
			.withDatabase(new Database()
			  .withSchemaVersionProvider("SELECT 'V1' AS version") // todo make custom schema version provider.
			  .withName("org.jooq.meta.h2.H2Database") // org.jooq.meta.*.*Database
			  .withInputSchema("PUBLIC")
			)
			// generated source target dir.
			.withTarget(
			  new Target()
				.withDirectory("spring-jooq/src/test/java")
				.withPackageName("com.gmoon.springjooq.global.jooqschema")
			)
		  );

		GenerationTool.generate(configuration);
	}
}
```

### [Maven plugin]((https://www.jooq.org/doc/latest/manual/code-generation/codegen-maven/))

- jooq-codegen-maven

```xml

<plugin>
    <!-- https://www.jooq.org/doc/latest/manual/code-generation/codegen-maven/ -->
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen-maven</artifactId>
    <version>${jooq.version}</version>
    <executions>
        <execution>
            <id>jooq-codegen</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <jdbc>
                    <driver>${jdbc.driver-class-name}</driver>
                    <url>${jdbc.url}</url>
                    <user>${jdbc.username}</user>
                    <password>${jdbc.password}</password>
                </jdbc>
                <generator>
                    <database>
                        <inputSchema>${jdbc.schema}</inputSchema>
                    </database>
                    <generate>
                        <!-- Never generate deprecated code -->
                        <deprecated>false</deprecated>
                        <instanceFields>true</instanceFields>
                        <pojos>false</pojos>
                        <records>false</records>
                    </generate>
                    <target>
                        <!--<directory>target/generated-sources/</directory>-->
                        <directory>src/main/java</directory>
                        <packageName>com.gmoon.springjooq.global.jooqschema</packageName>
                    </target>
                </generator>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 2. CRUD Operations Tests

```java
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
import com.gmoon.springjooq.global.jooqschema.tables.TbAccessLog;
import com.gmoon.springjooq.global.jooqschema.tables.records.TbAccessLogRecord;

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
		TbAccessLogRecord record = dsl.newRecord(jAccessLog);
		record.setId("alr000001");
		record.setUsername("guest");
		record.setIp("254.0.0.0");
		record.setOs(OperatingSystem.MAC.name());
		record.store();
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
			.set(jAccessLog.OS, OperatingSystem.LINUX.name())
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

				List<String> values1 = fetch.getValues(jAccessLog.OS);
				List<OperatingSystem> values2 = fetch.getValues(3, OperatingSystem.class);
				List<OperatingSystem> values3 = fetch.getValues(jAccessLog.OS, OperatingSystem.class);
				assertThat(values1).isNotEmpty();
				assertThat(values2).isEqualTo(values3);

				fetch.forEach(accessLog -> {
					String username = accessLog.get(jAccessLog.USERNAME);
					String ip = accessLog.get(jAccessLog.IP);
					String os = accessLog.get(jAccessLog.OS);
					OperatingSystem osEnum = accessLog.get(jAccessLog.OS, OperatingSystem.class);
					LocalDateTime attemptDt = accessLog.get(jAccessLog.ATTEMPT_DT);

					assertThat(username).isNotBlank();
					assertThat(ip).isNotBlank();
					assertThat(os).isNotBlank();
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
						jAccessLog.OS.eq(OperatingSystem.WINDOW.name())
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
				String result = dsl.select(jAccessLog.OS)// 컬럼을 지정하자.
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
				String result = dsl.select()
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

```

## Reference

- [www.jooq.org](https://www.jooq.org/)
  - [tutorials](https://www.jooq.org/doc/latest/manual/getting-started/tutorials/jooq-in-7-steps/#toc)
- [h2database - Using H2 with jOOQ](https://www.h2database.com/html/tutorial.html#using_jooq)
- [baeldung - Getting Started with jOOQ](https://www.baeldung.com/jooq-intro)

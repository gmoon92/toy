# Batch insert

## 1. Environment

- spring-boot:2.7.8
- mariadb:10.3
- jdk 1.8

```sh
cd docker
docker compose -p batchinsert up -d
```

### 2. Batch Insert 지원하는 라이브러리 및 프레임워크

- Spring Batch
- MyBatis
- Spring JDBC
- JDBC Batch Processing
- Hibernate Batch Processing
- Apache Camel
- Apache Nifi
- JOOQ(Java Object Oriented Querying)
- [Exposed](https://github.com/JetBrains/Exposed)
    - kotlin sql framework

### 2.1. Benchmarking

- hibernate batch insert/update
- jooq

> 선정 기준, Batch insert DSL, TypeSafe 지원

### 3. Multi Insert 동작 방식

- PreparedStatement.addBatch()
    - insert 할 쿼리 저장
- PreparedStatement.executeBatch()
    - addBatch 쿼리 재조합
    - DB Query 실행 요청

> 하이버네이트는 기본적으로 PreparedStatement#execute(String)으로 Query 실행 요청

### 4. Hibernate Batch Insert

- repository.saveAll()
- lazy write
- JDBC batching
    - [Hibernate jdbc batching guide](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#batch-jdbcbatch)
    - [MySql Performance Extensions](https://dev.mysql.com/doc/connectors/en/connector-j-connp-props-performance-extensions.html)

### 4.1. Properties

- properties
    - spring.jpa.properties
        - hibernate.jdbc.batch_size
        - hibernate.order_inserts
        - hibernate.order_updates
    - datasource
        - jdbc url options
            - [performance](https://dev.mysql.com/doc/connectors/en/connector-j-connp-props-performance-extensions.html)
                - `rewriteBatchedStatements`: 다중 쿼리 지원, 기본 값 false
            - [profiling](https://dev.mysql.com/doc/connectors/en/connector-j-connp-props-debugging-profiling.html)
                - `profileSQL`: Driver 에서 전송하는 쿼리 출력, 기본 값 false
                - `logger`: 로그 출력에 사용되는 로거 클래스, 기본 값 com.mysql.cj.log.StandardLogger
                - `maxQuerySizeToLog`: 출력할 쿼리 최대 길이

```yaml
spring:
  # rewriteBatchedStatements=true
  datasource: ${jdbc-url}?rewriteBatchedStatements=true&logger=Slf4JLogger&profileSQL=true&maxQuerySizeToLog=999999
  jpa:
    properties:
      hibernate:
        order_inserts: true
        order_updates: true
        "[jdbc.batch_size]": 1000
```

```text
Hibernate: insert into ex_access_log (attempt_dt, ip, os, username, id) values (?, ?, ?, ?, ?)
Hibernate: insert into ex_access_log (attempt_dt, ip, os, username, id) values (?, ?, ?, ?, ?)

2023-11-16 14:24:10.703  INFO 51239 --- [           main] MySQL                                    : [QUERY] insert into ex_access_log (attempt_dt, ip, os, username, id) values ('20220224', '220.0.0.0', 'WINDOW', 'admin', 'b9b62cdf-49d1-4326-93d2-03fc5c162ae6'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '6f956007-f0fc-4f73-b203-e1da8b59b4f8'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '2f96b421-c109-49a2-98e5-81ce2feb21ec'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '2c741cd1-ec68-4f6f-804f-0297f178c0bd'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '271323a7-b8dc-4833-843d-2c764380bf5e'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '3d65550d-520d-4c0e-a7b7-30d11d30bfe2'),('20220224', '220.0.0.0', 'WINDOW', 'admin', 'dfb902bf-899c-4ce6-90f5-290ad3b86162'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '1d04cf02-21a6-410d-8bc0-589feeb43443'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '19203cf2-1ee4-4d92-b701-8423160df002'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '73282790-6765-4167-b5fe-fde346cd7a4d'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '5b1953f4-c034-4c7d-9df1-346ca69e9e00'),('20220224', '220.0.0.0', 'WINDOW', 'admin', 'ce224722-d7f7-425d-942d-e1e770bf95b0'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '99f4f59a-2eec-483d-b0d5-09c4362ea49b'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '53c192e1-7586-4ded-a20f-acb606e0edd3'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '2737e8ed-6981-4ea1-8c36-5d870082724e'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '29b1f28a-5882-4f77-8176-59e269ee3697'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '6a7cf3e9-93ce-4abc-add5-f39e64d75b5c'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '22533a94-aeb8-4f04-8f88-7fe9d4223446'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '571fc011-e441-4453-8967-9c7d84551842'),('20220224', '220.0.0.0', 'WINDOW', 'admin', 'ffe328ca-2ab8-4697-8b94-64f264ea3ff3'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '3c7df815-8cfc-43a9-bcaa-101191df6aa8'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '06e8abe2-ce78-4167-bffd-d5ff29e1a566'),('20220224', '220.0.0.0', 'WINDOW', 'admin', '81e70eb4-694c-41ee-9a46-22ed532ca635'),('20220224', '220.0.0.0', ... (truncated) [Created on: Thu Nov 16 14:24:10 KST 2023, duration: 17, connection-id: 4227, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyStatement.executeBatch(ProxyStatement.java:127)]
2023-11-16 14:24:10.703  INFO 51239 --- [           main] MySQL                                    : [FETCH]  [Created on: Thu Nov 16 14:24:10 KST 2023, duration: 0, connection-id: 4227, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyStatement.executeBatch(ProxyStatement.java:127)]
Hibernate: insert into ex_access_log (attempt_dt, ip, os, username, id) values (?, ?, ?, ?, ?)
```

### 4.2. Hibernate Batch Insert 주의 사항

- @Id 생성 전략
- 연관 관계에 따른 Batch insert 미지원
    - hibernate.order_inserts
    - hibernate.order_update

### 4.2.1. @Id 생성 전략

- [하이버네이트 공식 문서](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#identifiers-generators-identity)
  에 따르면, GenerationType.IDENTITY 일 경우, 하이버네이트 일괄 배치 저장시 자동으로 비활성화 처리

```text
There is yet another important runtime impact of choosing IDENTITY generation: Hibernate will not be able to batch INSERT statements for the entities using the IDENTITY generation.

The importance of this depends on the application-specific use cases. If the application is not usually creating many new instances of a given entity type using the IDENTITY generator, then this limitation will be less important since batching would not have been very helpful anyway.
```

- MySQL 에선 insert 쿼리 실행 시점에, auto-increment 로 PK 값을 자동 증분해서 생성한다.
- 따라서 Transaction Write Behind를 할 수 없고, 결과적으로 Batch Insert 를 진행할 수 없다.

### 5. Hibernate Heap memory issued.

flush a batch of inserts and release memory

- 쓰지 지연 SQL 저장소 초기화
- 영속성 컨텍스트 1차 캐시 메모리 초기화

Session Heap memory clear

- [stackoverflow - Using StatelessSession for Batch processing](https://stackoverflow.com/questions/14174271/using-statelesssession-for-batch-processing)
- [javainnovations - batch insertion in hibernate](https://javainnovations.blogspot.com/2008/07/batch-insertion-in-hibernate.html)

### 6. [StatelessSession](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#_statelesssession)

StatelessSession 는 데이터 저장시 영속성 컨텍스트를 사용하지 않고, 더 낮은 수준으로 데이터베이스와 데이터를 스트리밍하는데 사용한다.

- 캐시 미지원(1차 캐시, 2차 캐시)
- 더티 체킹 미지원
- 지연로딩 미지원
- 영속성 전이 미지원
- Hibernate 의 Event model, interceptor 우회

```java
@RequiredArgsConstructor
public class AccessLogExcelDownloadRepositoryQueryImpl implements AccessLogExcelDownloadRepositoryQuery {

	private final EntityManager em;

	@Override
	public List<AccessLogExcelDownload> bulkSaveAllByStatelessSession(List<AccessLog> accessLogs) {
		Session session = em.unwrap(Session.class);
		SessionFactory sessionFactory = session.getSessionFactory();

		try (StatelessSession statelessSession = sessionFactory.openStatelessSession()) {
			Transaction tx = statelessSession.beginTransaction();

			List<AccessLogExcelDownload> result = new ArrayList<>(accessLogs.size());
			for (AccessLog accessLog : accessLogs) {
				AccessLogExcelDownload data = AccessLogExcelDownload.create(accessLog);

				statelessSession.insert(data);

				result.add(data);
			}

			tx.commit();
			return result;
		} catch (Exception e) {
			throw new RuntimeException("Not saved excel download data", e);
		}
	}
}
```

### 7. Global temporary table Bulk-id strategies

DB 성능을 고려하여 생성된 임시 테이블의 key 기준으로 대량 생성/수정/삭제를 하는 전략

1. [임시 테이블](https://dev.mysql.com/doc/refman/8.0/en/create-temporary-table.html) 생성(create temporary table tmp_account)
2. insert tmp_account (id, username) value ('uuid...', 'gmoon')
3. delete account where username = select username from tmp_account
4. delete account_detail where username = select username from tmp_account

> [Hibernate HHH-11262](https://hibernate.atlassian.net/browse/HHH-11262)

임시 테이블을 생성할 수 없을 경우

- InlineIdsInClauseBulkIdStrategy: PostgreSQL 만 지원
- InlineIdsSubSelectValueListBulkIdStrategy: PostgreSQL 만 지원
- InlineIdsOrClauseBulkIdStrategy: Oracle, SQL Server, MySQL, PostgreSQL
- CteValuesListBulkIdStrategy: CTE(Common Table Expressions) 지원해야 하며, PostgreSQL 만 지원

> [Hibernate - Non-temporary table bulk mutation strategies](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#batch-bulk-hql-strategies-non-temporary-table)
> [Hibernate -
Bulk-id strategies when you can’t use temporary tables](https://in.relation.to/2017/02/01/non-temporary-table-bulk-id-strategies/)

### 8. 결과

1000 건 데이터 기준으로 batch insert 성능 비교 결과.

- [X] hibernate batch option + StatelessSession 사용 = 117m 
- [ ] hibernate batch option 만 적용 = 2초
- [ ] jooq = 117m 

최종적으로 메모리와 쿼리 성능 최적화, 그리고 유지보수 관점으로 하이버네이트에서 지원하는 StatelessSession 를 사용하여 해결했다.

```java
@SpringBootTest
class AccessLogExcelDownloadRepositoryTest {

	private static List<AccessLog> accessLogs;

	@Autowired
	private AccessLogExcelDownloadRepository repository;


	@BeforeAll
	static void beforeAll(@Autowired AccessLogRepository accessLogRepository) {
		accessLogs = accessLogRepository.findAll();
	}

	@DisplayName("JPA 2 sec")
	@Test
	void saveAll() {
		List<AccessLogExcelDownload> registered = accessLogs.stream()
			.map(AccessLogExcelDownload::create)
			.toList();

		repository.saveAll(registered);
	}

	@DisplayName("JOOQ 117m")
	@Test
	void bulkSaveAllAtJooq() {
		repository.bulkSaveAllAtJooq(accessLogs);
	}

	@DisplayName("StatelessSession 117m")
	@Test
	void bulkSaveAllAtStatelessSession() {
		repository.bulkSaveAllAtStatelessSession(accessLogs);
	}

	@AfterEach
	void tearDown() {
		repository.flush();
	}
}

```

## Reference

- [Baeldung - JPA Hibernate batch insert update](https://www.baeldung.com/jpa-hibernate-batch-insert-update)
- [Spring boot support for jooq](https://www.baeldung.com/spring-boot-support-for-jooq)
- [Hibernate jooq a match mad in heaven](https://thorben-janssen.com/hibernate-jooq-a-match-made-in-heaven/)
- Hibernate StatelessSession
    - [Hibernate User Guide -_Stateless Session](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#_statelesssession)
    - [Hibernate User Guide - Batch](https://docs.jboss.org/hibernate/core/3.5/reference/en/html/batch.html)
    - [Hibernate: Improving application performance with StatelessSession](https://medium.com/javarevisited/hibernate-improving-application-performance-with-statelesssession-11536ebe9f80)
    - [Hibernate’s StatelessSession](https://thorben-janssen.com/hibernates-statelesssession/)
    - [SOLVED-BULK INSERT/UPDATE USING STATELESS SESSION - HIBERNATE-HIBERNATE](https://www.appsloveworld.com/hibernate/100/24/bulk-insert-update-using-stateless-session-hibernate)
- [MySQL 환경의 스프링부트에 하이버네이트 배치 설정 해보기](https://techblog.woowahan.com/2695/)
- [권남 - Hibernate Batch/Bulk Insert/Update](https://kwonnam.pe.kr/wiki/java/hibernate/batch)

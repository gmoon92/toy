Spring boot 3.5 version issued

@Query hibernate SyntaxException

- https://github.com/spring-projects/spring-boot/issues/45661
- https://github.com/spring-projects/spring-data-jpa/issues/3895

```java
public interface JpaTicketOfficeRepository extends JpaRepository<TicketOffice, Long> {

	@Query("SELECT m FROM Movie m WHERE m.id =:movieId AND m.ticketOffice.id = :id")
	Optional<Movie> findByIdAndMovieId(Long id, Long movieId);
}
```
```text
org.springframework.dao.InvalidDataAccessApiUsageException: org.hibernate.query.SyntaxException: At 1:55 and token ')', no viable alternative at input 'SELECT new com.gmoon.dbrestore.web.movies.domain.Movie(*) FROM Movie m WHERE m.id = :movieId AND m.ticketOffice.id = :id' [SELECT new com.gmoon.dbrestore.web.movies.domain.Movie() FROM Movie m WHERE m.id = :movieId AND m.ticketOffice.id = :id]
```

```java
// @Query("SELECT m FROM Movie m WHERE m.id =:movieId AND m.ticketOffice.id = :id")
@Query("FROM Movie m WHERE m.id =:movieId AND m.ticketOffice.id = :id")
```

----

## DB Lock

### dataSource.getConnection()

```java
@Slf4j
public class DataRecoveryExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        log.info("=======================afterEach");
        DataRecoveryHelper dataRecoveryHelper = obtainBean(extensionContext, DataRecoveryHelper.class);
        dataRecoveryHelper.recovery(); // dataSource.getConnection();
        DataSourceProxy.modifiedTables.clear();
    }
}
```

```text
# connection-id: 2463 - Test
[main] MySQL                                    (91) [QUERY] SET autocommit=0 [Created on: Thu Jul 25 20:39:21 KST 2024, duration: 0, connection-id: 2463, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 20:39:21 KST 2024, duration: 1, connection-id: 2463, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]
Hibernate: delete to1_0 from tb_ticket_office to1_0
[main] c.g.d.g.r.d.DataSourceProxy              (97) [DETECTED]   tb_ticket_office
[main] MySQL                                    (91) [QUERY] delete to1_0 from tb_ticket_office to1_0 [Created on: Thu Jul 25 20:39:21 KST 2024, duration: 2, connection-id: 2463, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 20:39:21 KST 2024, duration: 0, connection-id: 2463, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] c.g.d.g.r.DataRecoveryExtension          (19) =======================afterEach

# connection-id: 2464 - AfterEachCallback
[main] c.g.d.g.r.DataRecoveryHelper             (97) [START] execute query: SET FOREIGN_KEY_CHECKS = 0
[main] MySQL                                    (91) [QUERY] SET FOREIGN_KEY_CHECKS = 0 [Created on: Thu Jul 25 20:39:21 KST 2024, duration: 0, connection-id: 2464, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 20:39:21 KST 2024, duration: 0, connection-id: 2464, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] c.g.d.g.r.DataRecoveryHelper             (99) [END0] execute query: SET FOREIGN_KEY_CHECKS = 0
[main] c.g.d.g.r.DataRecoveryHelper             (155) recovery call: [tb_ticket, tb_movie, tb_movie_ticket, tb_ticket_office, tb_ticket]
[main] c.g.d.g.r.DataRecoveryHelper             (199) [TRUNCATE] START dbrecovery.tb_ticket
[main] c.g.d.g.r.DataRecoveryHelper             (97) [START] execute query: TRUNCATE TABLE dbrecovery.tb_ticket

```

1. 테스트 코드가 종료되고 AfterEach 코드에서 커넥션 id 가 변경.
2. hikari pool 사용으로 커넥션이 변경됌 확인.
   - `connection-id: 2463` -> `connection-id: 2464`
3. 이후 DB lock 으로 에러 발생.
   - "Waiting for table metadata lock"
    ```sql
    SHOW FULL PROCESSLIST; -- 현재 트랜잭션과 잠금 상태
    SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCKS; -- 현재 잠금 정보
    SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCK_WAITS; -- 잠금 대기 정보
    
    /**
    +----+----+------------------+----------+-------+----+-------------------------------+----------------------------------------------------------------+--------+
    |Id  |User|Host              |db        |Command|Time|State                          |Info                                                            |Progress|
    +----+----+------------------+----------+-------+----+-------------------------------+----------------------------------------------------------------+--------+
    |2903|root|192.168.65.1:36605|dbrecovery|Sleep  |107 |                               |null                                                            |0       |
    |2904|root|192.168.65.1:36606|dbrecovery|Query  |107 |Waiting for table metadata lock|TRUNCATE TABLE dbrecovery.tb_ticket                             |0       |
    |2905|root|192.168.65.1:36607|dbrecovery|Sleep  |108 |                               |null                                                            |0       |
    |2906|root|192.168.65.1:36608|dbrecovery|Sleep  |108 |                               |null                                                            |0       |
    |2907|root|192.168.65.1:36609|dbrecovery|Sleep  |108 |                               |null                                                            |0       |
    |2908|root|192.168.65.1:36610|dbrecovery|Sleep  |108 |                               |null                                                            |0       |
    |2909|root|192.168.65.1:36611|dbrecovery|Sleep  |107 |                               |null                                                            |0       |
    |2910|root|192.168.65.1:36612|dbrecovery|Sleep  |107 |                               |null                                                            |0       |
    |2911|root|192.168.65.1:36613|dbrecovery|Sleep  |107 |                               |null                                                            |0       |
    |2912|root|192.168.65.1:36614|dbrecovery|Sleep  |107 |                               |null                                                            |0       |
    |2913|root|192.168.65.1:36685|dbrecovery|Query  |0   |starting                       |/* ApplicationName=IntelliJ IDEA 2024.1 */ SHOW FULL PROCESSLIST|0       |
        +----+----+------------------+----------+-------+----+-------------------------------+----------------------------------------------------------------+--------+
    **/
    
    ```

### EntityManager

- 정상 호출 connection-id: 2924

```java
@Slf4j
public class DataRecoveryExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        log.info("=======================afterEach");
        EntityManager entityManager = obtainBean(extensionContext, EntityManager.class);
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE dbrecovery.tb_ticket_office").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO dbrecovery.tb_ticket_office SELECT * FROM dbrecovery_test.tb_ticket_office").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
    }
}
```

```text
# connection-id: 2924 - Test
[main] MySQL                                    (91) [QUERY] SET autocommit=0 [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 1, connection-id: 2924, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 0, connection-id: 2924, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]
Hibernate: delete to1_0 from tb_ticket_office to1_0
[main] c.g.d.g.r.d.DataSourceProxy              (99) [DETECTED]   tb_ticket_office
[main] MySQL                                    (91) [QUERY] delete to1_0 from tb_ticket_office to1_0 [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 2, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 0, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] c.g.d.g.r.DataRecoveryExtension          (16) =======================afterEach

# connection-id: 2924 - AfterEachCallback
Hibernate: SET FOREIGN_KEY_CHECKS = 0
[main] c.g.d.g.r.d.DataSourceProxy              (101) [UNDETECTED] SET FOREIGN_KEY_CHECKS = 0
[main] MySQL                                    (91) [QUERY] SET FOREIGN_KEY_CHECKS = 0 [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 1, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 0, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
Hibernate: TRUNCATE TABLE dbrecovery.tb_ticket_office
[main] c.g.d.g.r.d.DataSourceProxy              (101) [UNDETECTED] TRUNCATE TABLE dbrecovery.tb_ticket_office
[main] MySQL                                    (91) [QUERY] TRUNCATE TABLE dbrecovery.tb_ticket_office [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 7, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 0, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
Hibernate: INSERT INTO dbrecovery.tb_ticket_office SELECT * FROM dbrecovery_test.tb_ticket_office
[main] c.g.d.g.r.d.DataSourceProxy              (99) [DETECTED]   tb_ticket_office
[main] MySQL                                    (91) [QUERY] INSERT INTO dbrecovery.tb_ticket_office SELECT * FROM dbrecovery_test.tb_ticket_office [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 3, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 0, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
Hibernate: SET FOREIGN_KEY_CHECKS = 1
[main] c.g.d.g.r.d.DataSourceProxy              (101) [UNDETECTED] SET FOREIGN_KEY_CHECKS = 1
[main] MySQL                                    (91) [QUERY] SET FOREIGN_KEY_CHECKS = 1 [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 1, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 0, connection-id: 2924, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
[main] MySQL                                    (91) [QUERY] rollback [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 1, connection-id: 2924, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.rollback(ProxyConnection.java:386)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 0, connection-id: 2924, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.rollback(ProxyConnection.java:386)]
[main] MySQL                                    (91) [QUERY] SET autocommit=1 [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 1, connection-id: 2924, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]
[main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 22:38:41 KST 2024, duration: 0, connection-id: 2924, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]

```

### Connection

thread local 을 활용해서, Test 코드와 connection 를 동일하게 해야 하나 ?

- Unable to rollback against JDBC Connection
  - Connection is closed
  - DB socket timeout 으로 인한, connection is closed. 발생 가능성

```java
private Connection obtainConnection() throws SQLException {
    Connection connection = DataSourceProxy.connectionThreadLocal.get();
    if (connection == null || connection.isClosed()) {
        Connection reconnection = dataSource.getConnection();
        DataSourceProxy.connectionThreadLocal.set(reconnection);
        return reconnection;
    }
    return connection;
}
```
```text
org.springframework.orm.jpa.JpaSystemException: Unable to rollback against JDBC Connection

	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:341)
	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.translateExceptionIfPossible(HibernateJpaDialect.java:241)
	at org.springframework.orm.jpa.JpaTransactionManager.doRollback(JpaTransactionManager.java:593)
	...
	
Caused by: org.hibernate.TransactionException: Unable to rollback against JDBC Connection
	at org.springframework.orm.jpa.JpaTransactionManager.doRollback(JpaTransactionManager.java:589)
	... 10 more
Caused by: java.sql.SQLException: Connection is closed
	at com.zaxxer.hikari.pool.ProxyConnection$ClosedConnection.lambda$getClosedConnection$0(ProxyConnection.java:503)
```

### 생각해보니 새로 할당 받아도 되는거 아닌가?

굳이 테스트 코드에서 사용한 커넥션을 유지할 필요는 없잖아.

- Spring Transactional 은 rdmbs 트랜잭션은 ThreadLocal 을 통해 쓰레드 단위로 관리한다.
  - r2dbc 는 논외
- @Test 코드에서 선언된 @Transactional 에 대해 롤백 처리를 해야 하는데,
- @AfterEach 에서 수행하는 데이터 복구할 테이블과 해당 테이블의 레코드 롤백 시점 자체가 데이터 경합을 발생시켜 데드락 발생.
- 그렇다고 해서 테스트 코드에서 선언된 @Transactional 은 강제로 롤백 처리가 되어야 할텐데...
- 맞아. @Transactional 선언된 테스트 코드는 Spring JpaTransactionManager 에서 트랜잭션을 관리하게 된다.
  - 데이터에 대해 변경사항이 있다면, ThreadLocal 로 반환된 트랜잭션에 롤백 여부 마크를 선언.
  - 테스트 종료 시점에 자동 롤백 처리 되는 구조.
- @AfterEach 이후 테스트 코드에서 할당 받은 Connection 을 롤백해야 하는데 롤백을 할 수 없으니, 에러가 발생.

```java
@Slf4j
public class DataRecoveryExtension implements AfterEachCallback {
    
	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		forceTestCodeTransactionRollback();

		DataRecoveryHelper dataRecoveryHelper = obtainBean(extensionContext, DataRecoveryHelper.class);
		dataRecoveryHelper.recovery();
		DataSourceProxy.modifiedTables.clear();
	}

	private void forceTestCodeTransactionRollback() throws SQLException {
		Connection connection = DataSourceProxy.connectionThreadLocal.get();
		boolean activeTestConnection = connection != null && !connection.isClosed();
		if (activeTestConnection) {
			connection.rollback();
		}
	}
}
```
```text
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] SET autocommit=0 [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 1, connection-id: 3284, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3284, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]
Hibernate: delete to1_0 from tb_ticket_office to1_0
23:34:06 INFO  [main] c.g.d.g.r.d.DataSourceProxy              (99) [DETECTED]   tb_ticket_office
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] delete to1_0 from tb_ticket_office to1_0 [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 1, connection-id: 3284, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3284, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] rollback [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3284, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.rollback(ProxyConnection.java:386)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3284, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.rollback(ProxyConnection.java:386)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: SET FOREIGN_KEY_CHECKS = 0
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] SET FOREIGN_KEY_CHECKS = 0 [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(0): SET FOREIGN_KEY_CHECKS = 0
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (128) [TRUNCATE] START dbrecovery.tb_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: TRUNCATE TABLE dbrecovery.tb_ticket
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] TRUNCATE TABLE dbrecovery.tb_ticket [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 6, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(0): TRUNCATE TABLE dbrecovery.tb_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (130) [TRUNCATE] DONE dbrecovery.tb_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (136) [RECOVERY] START dbrecovery.tb_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: INSERT INTO dbrecovery.tb_ticket SELECT * FROM dbrecovery_test.tb_ticket
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] INSERT INTO dbrecovery.tb_ticket SELECT * FROM dbrecovery_test.tb_ticket [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 1, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(6): INSERT INTO dbrecovery.tb_ticket SELECT * FROM dbrecovery_test.tb_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (138) [RECOVERY] DONE  dbrecovery.tb_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (128) [TRUNCATE] START dbrecovery.tb_movie
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: TRUNCATE TABLE dbrecovery.tb_movie
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] TRUNCATE TABLE dbrecovery.tb_movie [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 4, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(0): TRUNCATE TABLE dbrecovery.tb_movie
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (130) [TRUNCATE] DONE dbrecovery.tb_movie
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (136) [RECOVERY] START dbrecovery.tb_movie
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: INSERT INTO dbrecovery.tb_movie SELECT * FROM dbrecovery_test.tb_movie
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] INSERT INTO dbrecovery.tb_movie SELECT * FROM dbrecovery_test.tb_movie [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 1, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(2): INSERT INTO dbrecovery.tb_movie SELECT * FROM dbrecovery_test.tb_movie
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (138) [RECOVERY] DONE  dbrecovery.tb_movie
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (128) [TRUNCATE] START dbrecovery.tb_movie_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: TRUNCATE TABLE dbrecovery.tb_movie_ticket
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] TRUNCATE TABLE dbrecovery.tb_movie_ticket [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 3, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(0): TRUNCATE TABLE dbrecovery.tb_movie_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (130) [TRUNCATE] DONE dbrecovery.tb_movie_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (136) [RECOVERY] START dbrecovery.tb_movie_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: INSERT INTO dbrecovery.tb_movie_ticket SELECT * FROM dbrecovery_test.tb_movie_ticket
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] INSERT INTO dbrecovery.tb_movie_ticket SELECT * FROM dbrecovery_test.tb_movie_ticket [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 1, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(6): INSERT INTO dbrecovery.tb_movie_ticket SELECT * FROM dbrecovery_test.tb_movie_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (138) [RECOVERY] DONE  dbrecovery.tb_movie_ticket
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (128) [TRUNCATE] START dbrecovery.tb_ticket_office
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: TRUNCATE TABLE dbrecovery.tb_ticket_office
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] TRUNCATE TABLE dbrecovery.tb_ticket_office [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 2, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(0): TRUNCATE TABLE dbrecovery.tb_ticket_office
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (130) [TRUNCATE] DONE dbrecovery.tb_ticket_office
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (136) [RECOVERY] START dbrecovery.tb_ticket_office
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: INSERT INTO dbrecovery.tb_ticket_office SELECT * FROM dbrecovery_test.tb_ticket_office
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] INSERT INTO dbrecovery.tb_ticket_office SELECT * FROM dbrecovery_test.tb_ticket_office [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 1, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(1): INSERT INTO dbrecovery.tb_ticket_office SELECT * FROM dbrecovery_test.tb_ticket_office
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (138) [RECOVERY] DONE  dbrecovery.tb_ticket_office
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (128) [TRUNCATE] START dbrecovery.tb_coupon
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: TRUNCATE TABLE dbrecovery.tb_coupon
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] TRUNCATE TABLE dbrecovery.tb_coupon [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 3, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(0): TRUNCATE TABLE dbrecovery.tb_coupon
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (130) [TRUNCATE] DONE dbrecovery.tb_coupon
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (136) [RECOVERY] START dbrecovery.tb_coupon
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: INSERT INTO dbrecovery.tb_coupon SELECT * FROM dbrecovery_test.tb_coupon
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] INSERT INTO dbrecovery.tb_coupon SELECT * FROM dbrecovery_test.tb_coupon [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 1, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(5): INSERT INTO dbrecovery.tb_coupon SELECT * FROM dbrecovery_test.tb_coupon
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (138) [RECOVERY] DONE  dbrecovery.tb_coupon
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (64) [START] execute query: SET FOREIGN_KEY_CHECKS = 1
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] SET FOREIGN_KEY_CHECKS = 1 [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3285, statement-id: 0, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyPreparedStatement.executeUpdate(ProxyPreparedStatement.java:61)]
23:34:06 DEBUG [main] c.g.d.g.r.DataRecoveryHelper             (66) [END]   execute query(0): SET FOREIGN_KEY_CHECKS = 1
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] rollback [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3284, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.rollback(ProxyConnection.java:386)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3284, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.rollback(ProxyConnection.java:386)]
23:34:06 INFO  [main] MySQL                                    (91) [QUERY] SET autocommit=1 [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3284, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]
23:34:06 INFO  [main] MySQL                                    (91) [FETCH]  [Created on: Thu Jul 25 23:34:06 KST 2024, duration: 0, connection-id: 3284, statement-id: -1, resultset-id: 0,	at com.zaxxer.hikari.pool.ProxyConnection.setAutoCommit(ProxyConnection.java:402)]

```

### 참고하면 좋은 글

- [baeldung - java socket connection read timeout](https://www.baeldung.com/java-socket-connection-read-timeout)
- [@DependsOnDatabaseInitialization](https://docs.openrewrite.org/recipes/java/spring/boot2/databasecomponentandbeaninitializationordering)
- [netmarble tech - 게임 서버 시스템을 위한 JDBC와 Timeout 이해하기
  ](https://netmarble.engineering/jdbc-timeout-for-game-server/)
- [woowahan - 응? 이게 왜 롤백되는거지?](https://techblog.woowahan.com/2606/)
- [Spring Batch에서 Chunk 작업이 길어지는 경우 주의할 점
  ](https://jaehun2841.github.io/2020/08/08/2020-08-08-spring-batch-db-connection-issue/#%EB%93%A4%EC%96%B4%EA%B0%80%EB%A9%B0)

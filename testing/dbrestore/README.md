# Integration testing without @Transactional

## Environment

- Spring boot 3
- MySQL 8
- com.github.jsqlparser:jsqlparser:4.9
- p6spy:p6spy:3.8.2
- org.codehaus.mojo:sql-maven-plugin

## 들어가기전

### @Transactional 테스트 검증의 한계

1. 의도치 않은 트랜잭션 적용
    - 의도되지 않는 "Dirty Checking" 또는 "Dirty Checking" 되지 않는 현상
    - 테스트 코드에서만 동작되는 비정상적인 검증
        - 트랜잭션 범위 밖에서 `save/update/delete` 동작되는 비정상적인 테스트 코드
        - 트랜잭션 범위 밖에서 `HibernateException` 예외 없이 통과되는 비정상적인 테스트 코드
            - `LazyInitializationException`
            - `EntityExistsException`, `NonUniqueObjectException`
                - "a different object with the same identifier value was already associated with the session"
            - `AbstractPersistentCollection`: Hibernate Collection 타입 관련 오류
                - "Illegal attempt to associate a collection with two open sessions"
                    - PersistentBag
                    - PersistentList
                    - PersistentMap
                    - PersistentSet
2. 트랜잭션 전파 속성 조절 실패
    - 테스트 코드에서 롤백 처리 되지 않는 현상
    - `@Transactional(propagation = Propagation.REQUIRES_NEW)`
        - 항상 새로운 트랜잭션을 시작함
3. 비동기 메소드 롤백 미적용
    - 테스트 코드에서 실행한 비동기 메소드에 대해 롤백이 적용되지 않는 현상
4. @EventListener
    - `@TransactionalEventListener`
    - Spring 트랜잭션은 `ThreadLocal`로 관리되며, 쓰레드 단위로 동작
        - JUnit 테스트 컨텍스트는 하나의 트랜잭션으로 관리되고 롤백 처리됨
        - 트랜잭션 커밋 이후의 비즈니스 로직에 대해 검증할 수 없음
        ```java
        // 커밋 이후 처리를 테스트할 수 없음
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handle(Event event) {
          // ...
        }
        ```

### @Transaction 없는 테스트 검증이 필요한 이유

1. 신뢰할 수 있는 테스트 코드
    - "의도치 않은 트랜잭션 적용"을 방지하여, 개발자 실수를 줄일 수 있다.
    - 더 나은 서비스를 만들기 위한 발판이 된다.
2. Slow 비즈니스 로직 발견 및 예방
    - 영속성 컨텍스트 1차 캐시
        - 하이버네이트는 데이터베이스 부하를 줄이기 위해 1차 캐시를 사용한다.
        - 1차 캐시는 동일한 @Transactional 범위 내에서 동작한다.
        - 하지만, @Transactional 선언된 테스트 코드에선 1차 캐시의 효과를 제대로 활용하고 있는지 파악하기 어렵다.
    - N+1 문제 및 성능 최적화
        - 양방향 관계를 단방향으로 변경하거나, Lazy 패치 타입으로 저장한 경우 성능을 개선할 수 있다.
        - 다만 이 과정에 발생할 수 있는 HibernateException 예외를 검증할 수 있는 신뢰할 수 있는 테스트 환경이 필요하다.

### 신뢰할 수 있는 테스트 환경

테스트 코드의 @Transactional은 "FIRST" 규칙 중 멱등성을 보장하기 위해, 변경된 데이터에 대한 롤백을 지원한다.

- [Test is FIRST](https://gmoon92.github.io/test/2018/08/24/test-driven-development.html)
    - Fast: 단위 테스트는 빨라야 한다.
    - Independent: 각각의 테스트 케이스는 독립적으로 동작한다.
    - Repeat: 멱등성을 보장하라. 어느 환경에서, 반복적으로 테스트 동일한 결과를 보장해야한다.
    - Self-validating: 비즈니스 로직에 대해 자체 검증 가능하도록 구현해라.
    - Timely: 미루지 마라. 제품 코드를 구현전에 테스트 코드 구성해라.

@Transactional 없이 신뢰된 테스트 환경을 만들기 위해선, 테스트 코드에서 변경된 데이터를 원래 상태로 복구하는 과정이 필요하다.

---

## DB Restore

### 백업 DB 를 통한 데이터 복구

테스트 코드에서 데이터가 변경되더라도, 검증 이후 백업 디비를 통해 변경된 데이터 복구한다.

1. 백업 DB 스키마 생성
2. run test case
3. 변경된 테이블 감지
4. @AfterEach 에서 변경된 데이터를 복구

### 1. 백업 DB schema 생성

백업 DB 스키마는 스키마와 테이블 구조, 그리고 샘플 데이터 생성을 완료한 후에 생성한다.

1. 빌드시 백업 DB schema drop
    - sql-maven-plugin 메이븐 플러그인을 활용해서 drop schema `db_back`
2. 테스트 코드 실행 시점에 최초 1번만 백업 DB 스키마 생성
    - `BackupDatabaseInitialization`
        - 백업 DB 스키마 생성
        - 여러 테스트를 수행하더라도, 백업 스키마 생성 작업은 한번만 실행되도록 보장.
        ```java
        public void afterPropertiesSet() throws Exception {
                // SELECT 1 
                // FROM INFORMATION_SCHEMA.SCHEMATA 
                // WHERE SCHEMA_NAME = db_back
            if (existsBackupSchema()) {
              return;
            }
                
            // 백업 스키마 생성
        }
        ```

### 2. 변경된 테이블 감지, DML 쿼리 감지

- `LoggingEventListenerProxy`
- p6spy 라이브러리를 활용해서 테스트 코드에서 실행된 SQL 문을 감지한다.
    - "INSERT | UPDATE | DELETE" 문만 감지

```java
public class LoggingEventListenerProxy extends LoggingEventListener {

    public static final SqlStatementCallStack CALL_STACK = new SqlStatementCallStack(
            DmlStatement.INSERT,
            DmlStatement.UPDATE,
            DmlStatement.DELETE
    );

    @Override
    protected void logElapsed(Loggable loggable, long timeElapsedNanos, Category category, SQLException e) {
        detectDMLStatement(loggable);
        super.logElapsed(loggable, timeElapsedNanos, category, e);
    }

    private void detectDMLStatement(Loggable loggable) {
        String sqlWithValues = loggable.getSqlWithValues();
        CALL_STACK.push(sqlWithValues);
    }
}
```

### 3. 데이터 복구, @DataRestore

- [**Junit5 Extension**](https://junit.org/junit5/docs/current/user-guide/#extensions) 확장
    - `DatabaseRestoreExtension`
        - `AfterEachCallback`
            - 테스트 코드 후처리
            - DataRestorationHelper#**restore()**
                1. `LoggingEventListener`에 적재된 DML 콜 스택으로 복구할 테이블 추출
                2. 데이터 복구
                    1. TRUNCATE TABLE db.table
                    2. INSERT INTO db.table SELECT * FROM db_back.table
                    3. DELETE FROM db_back.`sys_restore_table`
                        1. `sys_restore_table` 은 데이터 복구를 보장하기 위한 테이블
        - `BeforeEachCallback`
            - 테스트 코드 전처리
            - @Transaction 어노테이션 선언 유무 검증
                - 트랜잭션 없이 테스트 코드 수행 목적으로, 개발자 실수를 방지하기 위해 에러 처리
            - DataRestorationHelper#**snapshot()**
                1. 테스트 도중 크러시 이슈로 인해, 복원이 제대로 이뤄지지 않았는지 확인
                    - 예를 들어, 디버깅 중 테스트 코드가 중단된 경우
                    - 복원이 필요한 경우, 전체 테이블 복원 진행.
                2. 테스트 코드 실행 상태 추가
                    - [1] 단계 검증을 위한 (db_back.`sys_restore_table`) 상태 추가
                3. DML 콜스택 클리어
                    1. 테스트 코드에서만 수행한 DML 추출하기 위한 clear()
                ```java
                class DataRestorationHelper {
                
                	public void snapshot(SqlStatementCallStack callStack) {
                    try (Connection connection = dataSource.getConnection()) {
                      if (isBrokenTablePresent(connection)) {
                        restore(referenceTable.getAllTables());
                      }
                
                      executeUpdate(connection,
                          String.format("INSERT INTO %s (status) value ('WAIT');", getSystemTableName()));
                    } catch (Exception e1) {
                      throw new RuntimeException(e1);
                    } finally {
                      callStack.clear();
                    }
                  }
                }
                ```

### @IntegrationTest

- Tests run: 22, Failures: 0, Errors: 0, Skipped: 1
- Total time: 10.744 s -> 11.018 s

> 참고로, 실무에서 API 모듈에 적용한 경우, 540개의 테스트 코드 실행 시 빌드 시간이 약 1분 정도 증가했다. (09:05 min → 10:13 min)

## 주요 개발 항목

- @IntegrationTest : 통합 테스트 어노테이션, @SpringBootTest + @DataRestorer
- @DataRestorer: 테스트 실행후 테이블 복구, JUnit5 Extension 구현
    - 참고) DatabaseRestoreExtension 테이블 데이터 복구
- DatabaseRestorationConfig: db restore 관련 설정
- BackupDatabaseInitialization: 백업 스키마 생성 (테스트 코드 수행시 단 한번만 실행 보장)
- ReferenceTable: 테이블 정보
    - key=테이블명
    - value= 외래키 제약 조건 `on delete cascade` 옵션이 활성화된 **모든 테이블 명**
- LoggingEventListenerProxy
    - INSERT, UPDATE, DELETE SQL 문 감지
- DatabaseRestoreExtension
    - snapshot: 테스트 코드 실행전 초기화 (cleanup call stack, sys table)
    - restore: 테이블 복구

## Reference

- [Don’t Use @Transactional in Tests](https://dev.to/henrykeys/don-t-use-transactional-in-tests-40eb)
- [How to clear database in Spring Boot tests?](https://miensol.pl/clear-database-in-spring-boot-tests/)
- [vladmihalcea.com - The best way to handle the LazyInitializationException](https://vladmihalcea.com/the-best-way-to-handle-the-lazyinitializationexception/)
- [Martin Fowler - Eradicating Non-Determinism in Tests](https://martinfowler.com/articles/nonDeterminism.html)
- [Spring Transaction Management: @Transactional In-Depth](https://www.marcobehler.com/guides/spring-transaction-management-transactional-in-depth)
- [byte buddy](https://bytebuddy.net)
- p6spy
    - https://github.com/gavlyukovskiy/spring-boot-data-source-decorator
    - https://github.com/p6spy/p6spy

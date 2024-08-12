# Integration testing without @Transactional

@Transactional 없이 테스트 코드가 동작하기 위한 목적

1. 트랜잭션 범위 밖에 JPA 관련 예외 검증
    - 개발자 실수 방지
        - 의도하지 않은 dirty check 메커니즘으로 인해 데이터 수정
        - LazyInitializationException
            - 트랜잭션 범위 밖에서 엔티티 proxy 객체 touched
        - EntityExistsException
            - 트랜잭션 범위 밖에서 가공한 proxy 객체 save
2. Slow 비즈니스 로직 발견 및 예방
    - 영속성 컨텍스트 1차 캐시
        - 하이버네이트는 자체적으로 성능을 위해 컨텍스트 1차 캐시 활용.
        - @Transactional 테스트 코드에선 전체적으로 트랜잭션 범위가 잡히기 때문에, 1차 캐시를 제대로 활용하고 있는지 제대로 파악하기 힘듬.
    - N+1, 성능 최적화를 목적으로 변경된 코드에 대한 테스트 코드 검증 가능
        - 양방향 → 단방향
        - Lazy 패치 타입 변경

### 기존 @Transactional 테스트 검증 한계

1. 의도치 않은 트랜잭션 적용
    - 테스트 코드에서만 동작되는 정상적인 테스트.
        - 트랜잭션 범위 밖에서 save/update/delete 실행
        - 의도되지 않는 “dirty checking” 또는 “dirty checking” 되지 않는 현상
        - 트랜잭션 범위 밖에 예외를 못잡음
            - LazyInitializationException
            - Hibernate Error: a different object with the same identifier value was already associated with the session
2. 트랜잭션 전파 속성을 조절한 테스트 롤백 실패
    - 테스트 코드에서 롤백 처리 되지 않는 현상
    - @Transactional propagation = REQUIRES_NEW
        - 항상 새로운 트랜잭션을 시작
3. 비동기 메소드 롤백 미적용
    - 테스트 코드에서 실행한 비동기 메소드에 대해 롤백 안되는 현상
4. @EventListener
    - @TransactionalEventListener
    - Spring Transaction 은 트랜잭션을 ThreadLocal 로 관리하기 때문에, 쓰레드 단위로 동작
        - JUnit 테스트 컨텍스트는 하나의 트랜잭션으로 관리하고 Rollback 처리 함.
        - 트랜잭션 커밋 이후 처리되는 비즈니스 로직에 대해 검증할 수 없음.
        ```java
        // after commit 에 대한 테스트를 진행할 수 없음.
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handle(Event event) {
          // ...
        }
        ```


## [Test is FIRST](https://gmoon92.github.io/test/2018/08/24/test-driven-development.html)

- Fast: 단위 테스트는 빨라야 한다.
- Independent: 각각의 테스트 케이스는 독립적으로 동작한다.
- Repeat: 멱등성을 보장하라. 어느 환경에서, 반복적으로 테스트 동일한 결과를 보장해야한다.
- Self-validating: 비즈니스 로직에 대해 자체 검증 가능하도록 구현해라.
- Timely: 미루지 마라. 제품 코드를 구현전에 테스트 코드 구성해라.

### Environment

- com.github.jsqlparser:**jsqlparser**:4.9
- p6spy:**p6spy**:3.8.2
- org.codehaus.mojo:**sql-maven-plugin**
    - `execute drop-data-recovery-schema`

### 개발

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

---

## Result

- Tests run: 22, Failures: 0, Errors: 0, Skipped: 1
- Total time: 10.744 s -> 11.018 s

### Enable Total time:  11.348 s

- Tests run: 22, Failures: 0, Errors: 0, Skipped: 1
- 11.348 s

```text
[INFO] Results:
[WARNING] Tests run: 22, Failures: 0, Errors: 0, Skipped: 1
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.348 s
[INFO] Finished at: 2024-07-26T13:10:33+09:00
[INFO] ------------------------------------------------------------------------
```

### Disabled -> Total time:  10.744 s

- Tests run: 22, Failures: 0, Errors: 0, Skipped: 1
- Total time:  10.744 s

```text
[INFO] Results:
[WARNING] Tests run: 22, Failures: 0, Errors: 0, Skipped: 1
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  10.744 s
[INFO] Finished at: 2024-07-26T13:09:19+09:00
[INFO] ------------------------------------------------------------------------
```

## Reference

- https://dev.to/henrykeys/don-t-use-transactional-in-tests-40eb
- https://miensol.pl/clear-database-in-spring-boot-tests/
- https://vladmihalcea.com/the-best-way-to-handle-the-lazyinitializationexception/
- https://martinfowler.com/articles/nonDeterminism.html
- https://www.marcobehler.com/guides/spring-transaction-management-transactional-in-depth
- https://martinfowler.com/articles/nonDeterminism.html
- https://bytebuddy.net
- p6spy
  - https://github.com/gavlyukovskiy/spring-boot-data-source-decorator
  - https://github.com/p6spy/p6spy

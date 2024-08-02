# @IntegrationTest without @Transactional

## with @Transaction Problems

- 의도치 않은 트랜잭션 적용
    - 테스트 코드에서만 동작되는 정상적인 테스트.
        - 트랜잭션 범위 밖에서 save/update/delete 실행
        - 의도되지 않는 “dirty checking” 또는 “dirty checking” 되지 않는 현상
        - LazyInitializationException
- 트랜잭션 전파 속성을 조절한 테스트 롤백
    - 전파 속성에 대한 롤백 안되는 현상
    - @Transactional propagation = REQUIRES_NEW
        - 항상 새로운 트랜잭션을 시작
- 비동기 메소드 롤백 안되는 현상
- TransactionalEventListener 동작 실패
    - @TransactionalEventListener
    - Spring Transaction 은 쓰레드 단위로 동작함. (ThreadLocal 로 관리하기 때문)
        - JUnit 의 테스트 컨텍스트는 하나의 트랜잭션으로 관리하고 Rollback 처리 함.
        ```java
        // after commit 에 대한 테스트를 진행할 수 없음.
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void handle(Event event) {
          // ...
        }
        ```
- 테스트 코드의 신뢰 감소
- 수동 테스트를 시작하지 않는 한, 많은 버그가 눈치채지 못한 채 프로덕션에 들어가게 되는데, 수동 테스트를 시작하는 것은 오히려 역효과를 낳는다

## without @Transactional

- 성능 최적화에 활용할 수 있다.
  - API 병목 지점 발견 예방
  - 영속성 컨텍스트 1차 캐시 미활용
  - N+1 다중 발생 지점 발견
- Lazy loading
- False Negative

> 회귀방지(False Negative) <br>
테스트에서 회귀 는 테스트 코드는 정상동작 하더라도 제품코드가 비정상인 False Negative 상황을 뜻한다. </br> 
> 코드가 지속적으로 수정되고 늘어나면서 기능이 의도대로 작동하지 않는, 많은 회귀상황이 발생할 수 있다.

### Environment

- com.github.jsqlparser:**jsqlparser**:4.9
- p6spy:**p6spy**:3.8.2
- org.codehaus.mojo:**sql-maven-plugin**
    - `execute drop-data-recovery-schema`

### DB Recovery

1. 백업 DB 스키마 생성
2. Test Code 수행
   - INSERT, UPDATE, DELETE SQL 문 감지
3. 테스트 코드 종료되면 테이블 복구 수행
   - 감지된 SQL문을 통해 테이블 복구 진행
   - truncate table
   - insert ~ from schema_recovery

### DB backup schema

RecoveryDatabaseInitialization

- backup 스키마 생성 목적
- 테스트 실행시 db Spring test context 가 한번만 실행되도록 보장하기 위함.

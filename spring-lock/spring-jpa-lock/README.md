# JPA Lock

락이란 관계형 데이터베이스의 데이터의 일관성과 무결성을 보장하기 위해, 트랜잭션의 순서를 보장하는 장치다. 

## 1. Locking strategies

락 전략은 크게 낙관적 락(Optimistic Lock) 또는 비관적 락(Pessimistic Lock)이 있고, 하이버네이트는 다음 두 전략 메커니즘을 제공한다.

- Optimistic Lock
    - 여러 트랜잭션이 서로 영향을 미치지 않고 완료될 수 있다는 가정하에, 영향을 미치는 데이터를 DB 수준에서 잠그지 않고 트랜잭션을 진행하는 방식이다.
    - Application Level 에서 처리.
        - 단순히 버전 정보 하나만을 갖고 처리하기 때문에 애플리케이션에서 거는 락, Application Lock 이라고도 한다.
        - Version 컬럼으로 데이터 충돌 관리.
            1. 커밋하기 전에 각 트랜잭션은 다른 트랜잭션이 해당 데이터를 수정하지 않았는지 확인.
            2. 데이터 충돌이 감지되면, 커밋 중인 트랜잭션은 롤백 처리.
            3. 일반적으로 충돌이 발생하지 않는다고 가정하지만, 충돌 발생시 예외 발생 및 롤백 처리.
- Pessimistic Lock
    - 여러 트랜잭션이 서로 영향을 미치고 충돌한다고 가정한다.
    - DBMS Level 에서 처리.
        - 하이버네이트는 S-Lock, X-Lock 를 지원한다.
        - 동작 방식은 전적으로 DBMS 에서 지원하는 락 메커니즘을 따른다.
        - row-level-lock, record lock...
          - ex) `FOR SHARE(LOCK IN SHARE MODE)`, `FOR UPDATE`

## 2. Optimistic Lock

- @Version
- @OptimisticLock
- @OptimisticLocking

### 2.1. 동작 방식

1. 커밋하기 전에 각 트랜잭션은 다른 트랜잭션이 해당 데이터를 수정하지 않았는지 확인.
2. 데이터 충돌이 감지되면, 커밋 중인 트랜잭션은 롤백 처리.
3. 일반적으로 충돌이 발생하지 않는다고 가정하지만, 충돌 발생시 예외 발생 및 롤백 처리.

> Of course, if you are operating in a low-data-concurrency environment and do not require version checking, you may use this approach and just skip the version check. In that case, last commit wins will be the default strategy for your long application transactions. Keep in mind that this might confuse the users of the application, as they might experience lost updates without error messages or a chance to merge conflicting changes. - [Optimistic concurrency control - 4.4.1. Application version checking](https://access.redhat.com/documentation/pt-br/jboss_enterprise_application_platform/5/html/hibernate_entity_manager_reference_guide/transactions-optimistic#transactions-optimistic-manual)

하이버네이트 낙관적 락 전략은 `First Commit Wins` 따른다.

> [Here, the version property is mapped to the version column, and the entity manager uses it to detect conflicting updates, and prevent the loss of updates that would otherwise be overwritten by a last-commit-wins strategy.](https://docs.jboss.org/hibernate/orm/5.0/userguide/en-US/html/ch08.html)

- `First Commit Wins`: 낙관적 락을 활성화되면 첫번째 커밋만이 성공적으로 이루어지며, 두번째 커밋은 충돌로 간주하여 에러를 발생한다.
- `Last Commit Wins`: 낙관적 락을 비활성화하면 기본적으로 수행되는 유형으로 2개의 트랜잭션 모두 성공적으로 커밋된다. 마지막 커밋으로 덮어쓴다.
- `Merge`: 첫번째 커밋만이 성공적으로 이루어지며, 두번째 커밋 시에는 에러가 발생한다. 그러나 `First Commit Wins`와는 달리 두번째 커밋을 위한 작업을 처음부터 다시 하지 않고 개발자의 선택에 의해 선택적으로 변경될 수 있도록 한다. 가장 좋은 전략이나 변경 사항을 merge 할 수 있는 화면이나 방법을 직접 제공해 줄 수 있어야 한다.(추가 구현 필요)

### 2.2. @Version

JPA는 javax.persistence.Version 어노테이션을 통해 낙관적 락을 지원한다.

```java
import javax.persistence.*;

@Entity
public class Order {
    
	@Id
	private Long no;

	// ...

	@Version
	private Long version;
	
	// props...
}
```

- 반드시 엔티티의 @Version 컬럼은 하나만 지정해야 한다.
- [버전 타입은 숫자 또는 날짜 유형중 하나여야 한다.](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#locking-optimistic-mapping)
    - `int`, `Integer`
    - `long`, `Long`
    - `short`, `Short`
    - `java.sql.Timestamp`, `java.time.Instant`, `java.time.LocalDateTime`, [@CurrentTimestamp](https://docs.jboss.org/hibernate/orm/6.2/javadocs/org/hibernate/annotations/CurrentTimestamp.html)

### 2.3. @OptimisticLock

@OptimisticLock 는 Version 증가할 때 데이터 충돌 검사에서 제외할 컬럼을 지정할 때 사용하는 어노테이션이다.

```java
import javax.persistence.*;

@Entity
public class Order {

	// ...

	@ColumnDefault("0")
	@OptimisticLock(excluded = true)
	@Column(name = "issued_count")
	private Long issuedCount;
	
	// props...
}
```

```text
Hibernate: select order0_.`no` as no1_0_0_, order0_.`address` as address2_0_0_, order0_.`issued_count` as issued_c3_0_0_, order0_.`status` as status4_0_0_, order0_.`version` as version5_0_0_ from `tb_order` order0_ where order0_.`no`=?

Hibernate: update `tb_order` set `address`='Seoul', `issued_count`=1, `status`='WAIT', `version`=1 where `no`='order-no-001' and `version`='1'
Hibernate: update `tb_order` set `address`='Seoul', `issued_count`=1, `status`='WAIT', `version`=1 where `no`='order-no-001' and `version`='1'
Hibernate: update `tb_order` set `address`='Seoul', `issued_count`=1, `status`='WAIT', `version`=1 where `no`='order-no-001' and `version`='1'
```

- 실제 동시에 트랜잭션 경합은 발생하지 않지만, 동일한 버전으로 업데이트 쿼리가 실행되기 때문에 이후 요청된 업데이트 쿼리는 무시된다.
- 결과적으로 경합에서 제외를 하더라도 기본적으로 First Commit Wins 전략을 따른다.
- @Version 은 증가하지 않는다.

### 2.4. @OptimisticLocking

@OptimisticLocking 는 하이버네이트에서 지원하는 어노테이션으로, @Version 없는 낙관적 락을 사용할 때 사용한다.

- VERSION(default): @Version 를 기반으로 낙관적 잠금 수행
- NONE: @Version 컬럼이 있어도 낙관적 잠금 비활성화
- ALL: UPDATE/DELETE WHERE 절에 모든 필드를 기반으로 낙관적 잠금 수행
- DIRTY: UPDATE/DELETE WHERE 절에 더티 필드를 기반으로 낙관적 잠금 수행

```java
import org.hibernate.annotations.OptimisticLocking;

@OptimisticLocking(type = OptimisticLockType.ALL)
@Entity
@Table(name = "tb_order")
public class Order {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	@Column(length = 50)
	private String no;

	@Column(name = "address", length = 50)
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 50)
	private OrderStatus status;
} 
```

```sql
UPDATE order 
SET status = ?
WHERE id = ? AND address = ? AND status = ? -- OptimisticLockType.ALL
```

WHERE 절에 기준이 되는 데이터는 조회된 시점의 데이터를 사용하고, UPDATE/DELETE 시점에 해당 레코드가 없다면, StaleStateException, OptimisticLockException 예외가 발생한다.

## 3. Pessimistic Lock

JPA 에서 비관적 락을 지원하며, 비관적 락은 전적으로 [데이터베이스의 락 메커니즘](https://dev.mysql.com/doc/refman/5.7/en/innodb-locking.html)을 따른다.

- Shared Lock(공유 잠금, 읽기 잠금, S-Lock)
  - `SELECT ~ FOR SHARE(LOCK IN SHARE MODE)` 
  - 다른 트랜잭션에서 읽기와 S-Lock 을 획득할 수 있다. 또한 X-Lock 을 획득할 수 없다.
  - 읽는 동안 데이터 변경이 이뤄지지 않도록 않는 방식.
  - 여러 사용자가 동시에 데이터를 읽어도 데이터의 일관성에는 아무런 영향을 주지 않기 때문에, S-Lock 끼리는 동시에 접근 가능
- Exclusive Lock(베타적(독점) 자금, 쓰기 잠금, X-Lock)
  - `SELECT ~ FOR UPDATE`
  - 다른 트랜잭션에서 읽기와 쓰기가 불가능. 또한 추가적인 S, X Lock 획득 불가.
  - 하나의 레코드를 독점하여 수정하는 동안, 다른 트랜잭션에서 해당 자원에 대해 접근(ex, SELECT, INSERT..) 자체를 막아 데이터 일관성과 무결성을 보증

> [stackoverflow - What's the difference between an exclusive lock and a shared lock](https://stackoverflow.com/questions/11837428/whats-the-difference-between-an-exclusive-lock-and-a-shared-lock)

```sql
SELECT * FROM information_schema.INNODB_LOCK_WAITS; -- 현재 LOCK이 걸려 대기중인 정보
SELECT * FROM information_schema.INNODB_LOCKS; -- LOCK을 건 정보
SELECT * FROM information_schema.INNODB_TRX ; -- LOCK을 걸고 있는 프로세스 정보
SHOW ENGINE INNODB STATUS; -- InnoDB 상태
```

## 4. Hibernate LockModes

Lock Mode는 Query 초기화 시점에 설정된다.

javax.persistence.LockModeType 는 EntityManager, Query, TypeQuery 에 LockMode를 지정할 수 있다. setLockMode(LockModeType)

```java
package org.hibernate.internal;

public class SessionImpl
  extends AbstractSessionImpl
  implements EventSource, SessionImplementor, HibernateEntityManagerImplementor {

	@Override
	protected void initQueryFromNamedDefinition(Query query, NamedQueryDefinition namedQueryDefinition) {
		super.initQueryFromNamedDefinition(query, namedQueryDefinition);

		// Query 초기화시 지정된 Lock Mode가 있다면 Lock Mode를 주입한다.
		if (namedQueryDefinition.getLockOptions() != null) {
			if (namedQueryDefinition.getLockOptions().getLockMode() != null) {
				query.setLockMode(
				  LockModeTypeHelper.getLockModeType(namedQueryDefinition.getLockOptions().getLockMode())
				);
			}
		}
	}
}
```

### 4.1. Optimistic Lock Modes

엔티티 버전을 기준으로 데이터 무결성과 일관성을 보장한다.

- OPTIMISTIC(JPA 1.0 javax.persistence.LockModeType.READ)
    - 낙관적 락을 사용할 때, 기본적으로 설정되는 옵션이다.
    - 엔티티 변경 시점(UPDATE/DELETE)에 엔티티 버전을 증가시킨다.
    - 엔티티 버전은 트랜잭션이 끝나는 시점에 검증한다.
- OPTIMISTIC_FORCE_INCREMENT(JPA 1.0 LockModeType.WRITE)
    - 엔티티가 물리적으로 변경되지 않았지만, 논리적으로는 변경되었을 경우 버전을 증가하고 싶을 때 사용한다.
    - 트랜잭션을 커밋 시점에 버전 정보를 강제로 증가시킨다.
      - 엔티티가 직접적으로 수정되어 있지 않아도, 트랜잭션을 커밋 시점에 버전 정보를 강제로 증가시킨다. 
      - 커밋 시점에 엔티티 버전을 검증하고 일치하지 않으면 예외가 발생한다. 
      - OPTIMISTIC 모드와 마찬가지로 엔티티 변경이 일어날 때, 그리고 커밋 시점. 총 2번의 버전 증가가 발생한다.

### 4.2. Pessimistic Lock Modes

데이터베이스에서 지원하는 락 메커니즘을 기반으로 데이터 무결성과 일관성을 보장한다. 

- PESSIMISTIC_READ(javax.persistence.LockModeType.PESSIMISTIC_READ)
  - Shared Lock 방식으로 동작
- PESSIMISTIC_WRITE(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
  - Exclusive Lock 방식으로 동작
- PESSIMISTIC_FORCE_INCREMENT(javax.persistence.LockModeType.PESSIMISTIC_FORCE_INCREMENT): 
  - Exclusive Lock, with version update

### 4.3. [JPA Lock Mode -> Hibernate](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#locking-LockMode)

하이버네이트는 org.hibernate.internal.util.LockModeConverter 를 통해 JPA LockMode 와 호환성을 유지한다.

- LockModeConverter#convertToLockModeType(LockMode lockMode)
- LockModeConverter#convertToLockMode(LockModeType lockMode)

- NONE
    - NONE
- OPTIMISTIC
    - READ
    - OPTIMISTIC
- OPTIMISTIC_FORCE_INCREMENT
    - WRITE
    - OPTIMISTIC_FORCE_INCREMENT
- PESSIMISTIC_READ
    - PESSIMISTIC_READ
- PESSIMISTIC_WRITE
    - PESSIMISTIC_WRITE
    - ~~UPGRADE~~
    - UPGRADE_NOWAIT
    - UPGRADE_SKIPLOCKED
- PESSIMISTIC_FORCE_INCREMENT
    - PESSIMISTIC_FORCE_INCREMENT
    - ~~FORCE~~

## 5. 주의 사항

하이버네이트에서 DB Lock 설정을 하지 않더라도, DBMS의 락 메커니즘에 따라 데드락이 발생할 수 있다.

예를 들어 다음 코드는 데드락이 발생된다.

```text
OrderLineItem
- id:varchar(50) pk
- quantity:bigint
- menu_id:varchar(50) fk

Menu
- id:varchar(50) pk
- name:varchar(50)
- quantity:bigint
- price:bigint
```

```java
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final MenuRepository menuRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Order ordering(Order param, String menuId) {
		Menu menu = menuRepository.findById(menuId)
			.orElseThrow(EntityNotFoundException::new);

		// 1. 주문 생성
		List<OrderLineItem> orderLineItems = param.getOrderLineItems();
		for (OrderLineItem item : orderLineItems) {
			item.setMenu(menu);
		}

		// 2. 메뉴 재고 감소
		menu.decreaseQuantity();
		return orderRepository.saveAndFlush(param);
	}
}
```

MySql 같은 경우엔 FK 테이블에 제약 조건이 정의된 경우, 제약 조건을 확인(FK를 포함한)해야 하는 INSERT/UPDATE/DELETE 쿼리는 제약 조건을 확인하기 위해 S-Lock 을 설정한다. (InnoDB 동일한 락 메커니즘을 따른다) 또한, `UPDATE ~ WHERE`절을 사용할 경우 X-Lock 설정을 한다.

- [If a FOREIGN KEY constraint is defined on a table, any insert, update, or delete that requires the constraint condition to be checked sets shared record-level locks on the records that it looks at to check the constraint. InnoDB also sets these locks in the case where the constraint fails.](https://dev.mysql.com/doc/refman/8.0/en/innodb-locks-set.html)
- [UPDATE ... WHERE ... sets an exclusive next-key lock on every record the search encounters. However, only an index record lock is required for statements that lock rows using a unique index to search for a unique row.](https://dev.mysql.com/doc/refman/8.0/en/innodb-locks-set.html)

### 5.1. DB DeadLock 발생 원인

1. TX-A 주문 생성(INSERT)시 Menu ID 를 포함함으로, 해당 Menu 테이블의 ROW 에 대해 S-Lock 획득
2. TX-B 주문 생성(INSERT)시 Menu ID 를 포함함으로, 해당 Menu 테이블의 ROW 에 대해 S-Lock 획득
3. TX-A 메뉴 제고 감소 시도(UPDATE ~ WHERE), 해당 Menu 테이블의 ROW에 대해 X-Lock 획득 시도, TX-B 트랜잭션 종료 대기
4. TX-B 메뉴 제고 감소 시도(UPDATE ~ WHERE), 해당 Menu 테이블의 ROW에 대해 X-Lock 획득 시도, TX-A 트랜잭션 종료 대기
5. Deadlock!!!

### 6. 마무리

정리하자면 동시성 문제를 해결하기 위해선 우선적으로 애플리케이션 비즈니스에 대한 이해가 필요하다.

그 다음 낙관적/비관적 락의 장단점을 고려해야 하며, 데드락은 언제든 발생할 수 있다는점을 인지하고, 디비 환경에 따라 락 메커니즘을 이해하며 개발해야 한다.

- 낙관적 락
  - 실패에 대한 데이터 롤백 정책 고려 및 실패 이벤트 추가 개발 및 관리
- 비관적 락
  - 대량의 트래픽으로 디비 성능 저하 및 전체 시스템 성능 저하 우려
  - Redis Sorted Set, Lua Script
  - 메시지 큐 시스템 도입(Kafka, RabbitMQ)
  - 처리율 제한기 미들웨어 도입(API Gateway)
- 테이블 반정규화 고려
  - 특정 엔티티에서 대량의 데이터 수정사항 존재하고, 루트 엔티티의 변경이 필요한 경우.
- DB 분산 환경이라면 분산 락 고려(Distributed Lock)
  - 비관적 락으로 동시성을 해결할 수 없다.
  - [Redis lock](https://redis.com/glossary/redis-lock/) 고려
  - JTA Atomikos

## Reference

- Real MySQL 8.0
- [Hibernate User Guide - Locking](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#locking)
- [baeldung - Optimistic Locking in JPA](https://www.baeldung.com/jpa-optimistic-locking)
- [egovframe wiki - Optimistic Locking](https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte2:psl:orm:concurrency)
- [dev.mysql.com](https://dev.mysql.com/doc/)
  - [15.7.1 InnoDB Locking](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking.html)
  - [15.7.2.1 Transaction Isolation Levels](https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-isolation-levels.html)
  - [15.7.3 Locks Set by Different SQL Statements in InnoDB](https://dev.mysql.com/doc/refman/8.0/en/innodb-locks-set.html)
- [stackoverflow - How are locking mechanisms (Pessimistic/Optimistic) related to database transaction isolation levels?](https://stackoverflow.com/questions/22646226/how-are-locking-mechanisms-pessimistic-optimistic-related-to-database-transact)
- [[우아한테크토크] 선착순 이벤트 서버 생존기! 47만 RPM에서 살아남다?!](https://www.youtube.com/watch?v=MTSn93rNPPE&t=710s)

# Spring Transaction

Spring 트랜잭션은 추상화를 통해 일관성 있는 프로그래밍 모델을 제공한다.

기존 글로벌 트랜잭션(JTA)과 로컬 트랜잭션의 제약 사항으로 일관되지 않는 프로그래밍 모델을 구성했다.

Spring 트랜잭션 추상화의 핵심은 트랜잭션 관리의 전략 개념이다.

JDBC, JTA, JPA 등 각각의 트랜잭션 관리의 전략 구현체는 org.springframework.transaction.PlatformTransactionManager 인터페이스에 의해 정의된다.

- JdbcTransactionManager
- JtaTransactionManager
- JpaTransactionManager

## Transaction

데이터베이스 상태를 변경하기 위한 하나의 논리적인 작업 단위 또는 일련의 연산이다.

- 상태 변화: SQL 질의어(SELECT|INSERT|UPDATE|DELETE)를 통해 DB에 접근하여 데이터베이스 상태가 변화하는 것
- 작업 단위: 일련의 연산(SQL 명령문 집합). 사람이 정하는 기준(하나의 논리적 기능을 수행하기 위한)에 따라 정의함

## JDBC Transaction

```java
import java.sql.*;

import javax.sql.DataSource;

public class JDBCRepository {

	private final DataSource dataSource;

	public void executeQuery() {
		Connection conn = dataSource.getConnection(); // 1

		try (conn) {
			conn.setAutoCommit(false); // 2
			conn.setTransactionIsolation(Connection.TRANSACTION_NONE); // 3
			// execute query // 4
			conn.commit(); // 5
		} catch (SQLException e) {
			conn.rollback(); // 5
		}
	}
}
```

1. 데이터베이스 연결
2. 논리적인 작업 단위를 분리하기 위한 setAutoCommit 비활성화
3. 트랜잭션 격리 수준 설정
4. SQL Query 수행
5. commit or rollback

## TransactionInterceptor

Spring 트랜잭은은 AOP 를 활용하여 트랜잭션 생명 주기를 관리한다.

- org.springframework.transaction.interceptor.TransactionInterceptor
- 기본(getConnection(), setAutoCommit(false), commit())방식으로 접근.

```java
package org.springframework.transaction.interceptor;

public abstract class TransactionAspectSupport implements BeanFactoryAware, InitializingBean {

	@Nullable
	protected Object invokeWithinTransaction(
	  Method method,
	  @Nullable Class<?> targetClass,
	  final InvocationCallback invocation
	) throws Throwable {
		// 1. 트랜잭션 전파/격리 수준 속성 정보
		TransactionAttributeSource tas = getTransactionAttributeSource();
		final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);

		// 2. TransactionManager 
		final TransactionManager tm = determineTransactionManager(txAttr);
		PlatformTransactionManager ptm = asPlatformTransactionManager(tm);

		// 3. 트랜잭션 생성 getConnection(), setAuthCommit(false)
		TransactionInfo txInfo = createTransactionIfNecessary(ptm, txAttr, joinpointIdentification);

		Object retVal;
		try {
			// 4. 타깃 메서드 수행
			retVal = invocation.proceedWithInvocation();
		} catch (Throwable ex) {
			completeTransactionAfterThrowing(txInfo, ex);
			throw ex;
		} finally {
			// 5. TransactionHolder clean...
			cleanupTransactionInfo(txInfo);
		}

		if (retVal != null && vavrPresent && VavrDelegate.isVavrTry(retVal)) {
			// Set rollback-only in case of Vavr failure matching our rollback rules...
			TransactionStatus status = txInfo.getTransactionStatus();
			if (status != null && txAttr != null) {
				retVal = VavrDelegate.evaluateTryFailure(retVal, txAttr, status);
			}
		}

		// 6. 트랜잭션 커밋
		commitTransactionAfterReturning(txInfo);
		return retVal;

	}
}

```

- [PlatformTransactionManager](https://docs.spring.io/spring-framework/reference/data-access/transaction/strategies.html)
    - 트랜잭션 전략은 해당 인터페이스가 정의한다.
    - DB Connection 연결 및 commit / rollback 수행
    - AbstractPlatformTransactionManager
        - PlatformTransactionManager를 확장한 팩토리 메서드 패턴 구조의 추상 클래스.
- TransactionInterceptor
    - TransactionAspectSupport 를 구현한 클래스
        - TransactionAspectSupport
            - 트랜잭션 Aspect 동작의 근간이 되는 템플릿 추상 클래스
            - 논리적인 트랜잭션 경계를 설정하고 여기에서 주입된 PlatformTransactionManager 를 사용. (ref #invokeWithinTransaction)
- TransactionAttributeSource
    - 트랜잭션 속성 관련 인터페이스.
    - AnnotationTransactionAttributeSource: 어노테이션 기반
    - NameMatchTransactionAttributeSource: 각각의 메서드 명마다 트랜잭션 정의
- AbstractAutoProxyCreator
    - 프록시 객체가 생성되고 실제 클라이언트의 요청은 프록시를 통해 TransactionInterceptor를 사용해 트랜잭션의 생명 주기를 관리한다.
    - 타깃 메서드를 호출하고 커밋/롤백을 수행한다.

## 물리적 트랜잭션 vs 논리적 트랜잭션

- 물리적 트랜잭션: 실제 JDBC 트랜잭션
    - 실제 데이터베이스에 적용되는 트랜잭션으로, 커넥션을 통해 커밋/롤백하는 단위
- 논리적 트랜잭션: @Transactional 로 중첩된 메서드
    - Spring Transaction Manager 를 통한 트랜잭션 처리 단위
    - 모든 논리 트랜잭션이 커밋되어야 물리 트랜잭션이 커밋
    - 하나의 논리 트랜잭션이라도 롤백되면 물리 트랜잭션은 롤백

## Transaction Attributes

- [Propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html):
  트랜잭션 전파 옵션
- Isolation: DB 격리 수준

### Propagation

- org.springframework.transaction.annotation.Propagation: 트랜잭션 전파 옵션

| Propagation           | Description                                                                                                                                                    | 
|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **REQUIRED**(default) | 상위 트랜잭션 참여, 없으면 새로운 트랜잭션을 시작.<br/><br/>예외 발생 롤백, 상위 트랜잭션에 예외 발생 및 롤백 전파                                                                                        |
| REQUIRES_NEW          | 항상 새로운 트랜잭션을 시작. <br/>상위 트랜잭션이 존재한다면 상위 트랜잭션은 대기 후, 새로운 트랜잭션 시작<br/><br/>하위 트랜잭션 예외 발생 롤백, 롤백은 상위 트랜잭션에 전파하지 않는다.                                              |
| NESTED                | 상위 트랜잭션이 있다면 중첩하여 트랜잭션 진행.<br/><br/>상위 트랜잭션 예외 발생 -> 중첩 트랜잭션 롤백<br/> 중첩 트랜잭션 예외 발생 -> 상위 트랜잭션과 별개로 독립적으로 중첩 트랜잭션에서 발생한 예외 발생 부분 롤백 가능(savepoints, DB에서 지원해야함.) |
| MANDATORY             | 이미 시작된 트랜잭션이 있으면 참여, 없으면 IllegalTransactionStateException 예외 발생. 독립적으로 수행되면 안되는 경우에 사용.                                                                        |                                                                                                                          |
| NOT_SUPPORTED         | 트랜잭션을 사용하지 않고 처리. 이미 진행중인 트랜잭션이 있다면 잠시 보류.                                                                                                                     |
| NEVER                 | 트랜잭션을 사용하지 않도록 강제. 이미 진행중인 트랜잭션 또한 허용하지 않으며, 있다면 IllegalTransactionStateException 예외 발생.                                                                       |
| SUPPORTS              | 이미 시작된 트랜잭션이 있으면 참여, 없으면 트랜잭션 없이 처리.                                                                                                                           |

### Isolation

- org.springframework.transaction.annotation.Isolation: DB 격리 수준

| Isolation                 | Description                                                                                                                                                                               |
|---------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DEFAULT                   | 데이터 베이스에서 설정된 기본 격리 수준을 따른다.                                                                                                                                                              |
| READ_UNCOMMITTED(level 0) | 트랜잭션이 아직 커밋되지 않은 데이터(Dirty data)를 읽을 수 있다.                                                                                                                                                |
| READ_COMMITTED(level 1)   | `Dirty Read`를 방지하기 위해, 커밋된 데이터만 읽을 수 있다.                                                                                                                                                  |
| REPEATABLE_READ(level 2)  | 트랜잭션이 완료될 때까지 조회한 모든 데이터에 `s-lock`이 걸리므로, 트랜잭션이 종료될 때까지 다른 트랜잭션은 그 영역에 해당하는 데이터를 수정할 수 없다.<br/><br/>Phantom READ 발생, 한 트랜잭션에서 동일한 조회 쿼리 수행시 첫 번째 쿼리엔 레코드가 없다가 두번째 조회 쿼리 수행시 레코드를 응답하는 현상. |
| SERIALIZABLE(level 3)     | 가장 엄격한 트랜잭션 격리수준으로, 완벽한 읽기 일관성 모드 제공. <br/><br/>PHANTOM READ 상태가 발생하지 않지만 동시성 처리 성능이 급격히 떨어질 수 있다.                                                                                        |

## OSIV

Controller 계층 부터 트랜잭션 관리

- OpenEntityManagerInViewInterceptor
    - WebRequestInterceptor 구현한 인터셉터

> OSIV 설정과 엔티티 생명주기는 [다음 블로그 내용](https://perfectacle.github.io/2021/05/24/entity-manager-lifecycle/) 참고하자.

## REQUIRES_NEW

REQUIRES_NEW 전파 유형은 상위 트랜잭션과 별도로 항상 새로운 트랜잭션을 생성한다.

```java

@Service
@RequiredArgsConstructor
public class SpaceUserService {

	private final SpaceUserRepository spaceUserRepository;
	private final FavoriteService favoriteService;

	@Lazy
	@Autowired
	private SpaceUserService self;

	@Transactional
	public void delete(String spaceId, String userId) {
		SpaceUser spaceUser = getSpaceUser(spaceId, userId);

		spaceUserRepository.delete(spaceUser);
		self.deleteFavorites(userId);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deleteFavorites(String userId) {
		try {
			favoriteService.delete(userId);
			throw new RuntimeException("강제 예외 발생");
		} catch (Exception e) {
			log.warn("", e);
		}
	}
}
```

1. try-cache 예외를 잡지 않을 경우 상위 트랜잭션도 롤백된다.
2. 전파 옵션과 별개로 에러가 발생하면 콜스택을 제거하면서 최초 호출한 상위 메서드까지 예외가 전파된다.
3. 결국 상위 논리적인 트랜잭션도 예외가 발생하여 물리적인 트랜잭션 롤백 처리.

> REQUIRED 전파 유형은 중첩 트랜잭션에서 예외 발생하면 상위 트랜잭션에 예외 전파
> org.springframework.transaction.UnexpectedRollbackException

## @TransactionalEventListener

이벤트 어노테이션 구현 방식은 Spring framework 4.2 버전 부터 지원.

- BEFORE_COMMIT: 트랜잭션 커밋 전에 이벤트 호출
- AFTER_COMMIT(default): 트랜잭션이 커밋되었을 때 이벤트 호출
    - Propagation.REQUIRES_NEW (호출된 트랜잭션과 별도로 새로운 트랜잭션 필요.)
- AFTER_ROLLBACK: 트랜잭션이 롤백될 때 이벤트 호출
- AFTER_COMPLETION: 트랜잭션이 완료되었을 때 이벤트 호출(commit or rollback)

```java
import org.springframework.transaction.event.TransactionalEventListener;

import com.gmoon.springtx.global.event.DeleteFavoriteEvent;

@Component
@RequiredArgsConstructor
public class FavoriteEventHandler {

	private final FavoriteService favoriteService;

	@TransactionalEventListener(
	  phase = TransactionPhase.AFTER_COMMIT,
	  fallbackExecution = true
	)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleDelete(DeleteFavoriteEvent deleteFavoriteEvent) {
		String userId = deleteFavoriteEvent.getUserId();
		favoriteService.delete(userId);
	}
}


```

> @EventListener 은 publish 이후 바로 리스너가 동작.

호출한 상위 트랜잭션 커밋 후 별도의 트랜잭션이 진행되도록 TransactionPhase.AFTER_COMMIT 옵션 설정한다.

```java
package com.gmoon.springtx.spaces.application;

import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
public class SpaceUserService {

	private final ApplicationEventPublisher applicationEventPublisher;
	private final SpaceUserRepository spaceUserRepository;

	@Transactional
	public void delete(String spaceId, String userId) {
		SpaceUser spaceUser = getSpaceUser(spaceId, userId);

		spaceUserRepository.delete(spaceUser);
		deleteFavorites(userId);
	}

	private void deleteFavorites(String userId) {
		applicationEventPublisher.publishEvent(new DeleteFavoriteEvent(this, userId));
	}
}

```

## TransactionSynchronization

PlatformTransactionManager 는 트랜잭션 커밋 또는 롤백 전/후 시점에 TransactionSynchronization 를 통해 이벤트를 처리한다.

```java
package org.springframework.transaction.support;

public interface TransactionSynchronization extends Ordered, Flushable {

	int STATUS_COMMITTED = 0;
	int STATUS_ROLLED_BACK = 1;
	int STATUS_UNKNOWN = 2;

	default void beforeCommit(boolean readOnly) {
	}

	default void beforeCompletion() {
	}

	default void afterCommit() {
	}

	default void afterCompletion(int status) {
	}

}

```

예를 들어 키밋 이후 이벤트 호출은 다음과 같다.

1. `PlatformTransactionManager`#commit
2. `AbstractPlatformTransactionManager`#triggerAfterCompletion()
3. `TransactionSynchronizationUtils`#invokeAfterCompletion()
4. `TransactionSynchronization`#afterCompletion()

### TransactionSynchronization 예외 전파

@TransactionalEventListener 는 TransactionalApplicationListener 에 TransactionalApplicationListenerSynchronization 인스턴스를
`TransactionSynchronization` 추상화 타입으로 등록한다. `TransactionSynchronization`는 트랜잭션의 전 후에 등록된 이벤트를 호출한다.

- `beforeCommit`, `afterCommit`은 호출자에게 예외가 전파된다.
    - BEFORE_COMMIT
- `beforeCompletion`, `afterCompletion`은 예외가 전파되지 않는다.
    - AFTER_COMMIT
        - afterCompletion(TransactionSynchronization.STATUS_COMMITTED)
    - AFTER_ROLLBACK
        - afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK)
    - AFTER_COMPLETION

```java
package org.springframework.transaction.event;

class TransactionalApplicationListenerSynchronization<E extends ApplicationEvent>
  implements TransactionSynchronization {

	@Override
	public void beforeCommit(boolean readOnly) {
		if (this.listener.getTransactionPhase() == TransactionPhase.BEFORE_COMMIT) {
			processEventWithCallbacks();
		}
	}

	@Override
	public void afterCompletion(int status) {
		TransactionPhase phase = this.listener.getTransactionPhase();
		if (phase == TransactionPhase.AFTER_COMMIT && status == STATUS_COMMITTED) {
			processEventWithCallbacks();
		} else if (phase == TransactionPhase.AFTER_ROLLBACK && status == STATUS_ROLLED_BACK) {
			processEventWithCallbacks();
		} else if (phase == TransactionPhase.AFTER_COMPLETION) {
			processEventWithCallbacks();
		}
	}

	// default void beforeCompletion() {
	// }

	// default void afterCommit() {
	// }
}

```

리스너에서 afterCompletion 또는 beforeCompletion 는 예외가 발생하더라도 TransactionSynchronizationUtils 로깅 레벨이 디버그 설정되어 있기 때문에 별도로 try-cache 로 에러 로그를 설정해 줘야 한다.

> @Async 비동기 예외 처리는 SimpleAsyncUncaughtExceptionHandler 또는 AsyncUncaughtExceptionHandler를 구현한 커스텀 핸들러를 구현해야한다.

```java
package org.springframework.transaction.support;

public abstract class TransactionSynchronizationUtils {

	public static void invokeAfterCompletion(@Nullable List<TransactionSynchronization> synchronizations,
	  int completionStatus) {

		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : synchronizations) {
				try {
					synchronization.afterCompletion(completionStatus);
				} catch (Throwable ex) {
					// Spring boot 2.7.14 이하 버전, debug level.
					logger.debug("TransactionSynchronization.afterCompletion threw exception", ex);
				}
			}
		}
	}
}
```

다음 로그 레벨 문제([#30776](https://github.com/spring-projects/spring-framework/pull/30776))는 Spring framework 6.0.11 버전 이상(spring boot 2.7.14)에서 error 레벨로 개선됐다.

> [spring release note - Fix log level on error with @TransactionalEventListener](https://github.com/spring-projects/spring-framework/releases/tag/v6.0.11)

```java
package org.springframework.transaction.support;

public abstract class TransactionSynchronizationUtils {

	public static void invokeAfterCompletion(@Nullable List<TransactionSynchronization> synchronizations,
	  int completionStatus) {

		if (synchronizations != null) {
			for (TransactionSynchronization synchronization : synchronizations) {
				try {
					synchronization.afterCompletion(completionStatus);
				} catch (Throwable ex) {
					// Spring boot 2.7.14 버전에서 개선됌
					logger.error("TransactionSynchronization.afterCompletion threw exception", ex);
				}
			}
		}
	}
}
```

## Reference

- [docs.spring.io](https://docs.spring.io/spring-framework/reference/data-access.html)
    - [Transactional](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
    - [Transaction-bound Events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html#page-title)
- [www.marcobehler.com - Spring Transaction Management: @Transactional In-Depth](https://www.marcobehler.com/guides/spring-transaction-management-transactional-in-depth?fbclid=IwAR1PsHPKHyLGmiKORaTsvXV6EwIwe5f2RTCkz52QLZFnDdI7QzArXLil4PQ)
- [DZone - Transaction Synchronization and Spring Application Events: Understanding @TransactionalEventListener](https://dzone.com/articles/transaction-synchronization-and-spring-application)
- [perfectacle blog - entity manager lifecycle](https://perfectacle.github.io/2021/05/24/entity-manager-lifecycle/)
- [woodcock - Transactional REQUIRES_NEW에 대한 오해](https://woodcock.tistory.com/40)
- [spring boot release versions and schedule](https://jsession4d.com/list-of-spring-boot-and-spring-release-versions-and-schedule/)

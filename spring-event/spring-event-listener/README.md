# Spring @EventListener

이벤트는 시스템간 강결합 문제를 해결하기 위해 사용한다. 스프링에서 지원하는 이벤트 처리 방식에 대해 소개한다.

- [Event](#event)
- [Publisher](#publisher)
- [Listener](#listener)
- [Asynchronous Events Config](#Asynchronous-Events-Config)
- [Spring ApplicationContext Events](#asynchronous-events-config)
- [EventListener Annotation](#eventlistener-annotation)
- [Event Listener Order](#event-listener-order)
- [Generics Support](#generics-support)
- [TransactionalEventListener Annotation](#transactionaleventlistener-annotation)

### Event

- 이벤트 클래스 구성
- ApplicationEvent 구현

```java

@Getter
public class CustomEvent extends ApplicationEvent {

	private final String message;

	public CustomEvent(Object source, String message) {
		super(source);
		this.message = message;
	}
}
```

- org.springframework.context.ApplicationEvent
    - EventObject(Object source)

> Spring 4.2 이상 부터 ApplicationEvent 를 구현하지 않아도 내부적으로 PayloadApplicationEvent 래핑하여 처리한다.

### Publisher

- 이벤트 발행
    - ApplicationEventPublisher.publishEvent(Object event)

```java

@Component
@RequiredArgsConstructor
public class CustomEventPublisher {

	private final ApplicationEventPublisher publisher;

	public void raise(String message) {
		CustomEvent event = new CustomEvent(this, message);
		publisher.publishEvent(event);
	}
}
```

- org.springframework.context.ApplicationEventPublisher
    - publishEvent(Object event)

### Listener

- ApplicationListener 인터페이스 구현
- Spring Bean 지정

```java

@Slf4j
@Component
public class CustomEventListener implements ApplicationListener<CustomEvent> {

	// by default Spring events are synchronous
	@Override
	public void onApplicationEvent(CustomEvent event) {
		log.info("start...");
		log.info("Received event({}): {}", event.getSource(), event.getMessage());
		slowProcess();
		log.info("end...");
	}

	private void slowProcess() {
		try {
			sleep(Duration.ofMillis(500).toMillis());
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
```

- org.springframework.context.ApplicationListener
    - onApplicationEvent(E event)

### Asynchronous Events Config

- 비동기식 이벤트 생성
- Spring은 기본적으로 이벤트를 동기 방식으로 처리한다.
- 비동기 이벤트 방식 주의 사항
    - 비동기 이벤트는 리스너에서 예외가 발생하면 호출자에게 전파되지 않는다.
        - 별도의 비동기 예외 핸들러 구현 필요, (AsyncUncaughtExceptionHandler 참고)
    - 비동기 이벤트 리스너 메서드는 값을 반환하여 후속 이벤트를 발행할 수 없다.
        - 만약 처리 결과로 다른 이벤트를 발행해야 하는 경우 `ApplicationEventPublisher` 를 통해 수동으로 발행해야 한다.

```java

@Configuration
public class EventConfig {

	@Bean(name = "applicationEventMulticaster")
	public ApplicationEventMulticaster simpleApplicationEventMulticaster(Executor executor) {
		SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();

		eventMulticaster.setTaskExecutor(executor);
		return eventMulticaster;
	}

	@Bean
	public Executor simpleAsyncTaskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}
}
```

- org.springframework.context.event.`ApplicationEventMulticaster`
- org.springframework.context.event.`SimpleApplicationEventMulticaster`
- org.springframework.core.task.`SimpleAsyncTaskExecutor`

### Spring ApplicationContext Events

- 스프링은 기본적으로 `ApplicationContext` 관련된 다양한 이벤트를 제공한다.
- ApplicationContext
    - ApplicationContextEvent
        - ContextRefreshedEvent
        - ContextStartedEvent
        - RequestHandledEvent
        - ContextStoppedEvent
        - ContextClosedEvent
        - [그외 표준 이벤트... org.springframework.context.event...](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-events)

```java

@Slf4j
@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("Handling context refreshed event. ");
		ApplicationContext context = event.getApplicationContext();

		loggingForEventListenerBeans(context);
	}

	private void loggingForEventListenerBeans(ApplicationContext context) {
		log.info("==============ApplicationListener=====================");
		Map<String, ApplicationListener> listeners = context.getBeansOfType(ApplicationListener.class);
		for (String beanName : listeners.keySet()) {
			ApplicationListener listener = listeners.get(beanName);
			log.info("bean: {}, name: {}", listener.getClass().getSimpleName(), beanName);
		}
		log.info("======================================================");
	}
}
```

```text
Handling context refreshed event. 
==============ApplicationListener=====================
bean name: &org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory, type: org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer$SharedMetadataReaderFactoryBean@31d0e481
bean name: applicationAvailability, type: org.springframework.boot.availability.ApplicationAvailabilityBean@2f4854d6
bean name: customEventListener, type: com.gmoon.springeventlistener.simple.CustomEventListener@7a138fc5
bean name: contextRefreshedListener, type: com.gmoon.springeventlistener.context.ContextRefreshedListener@379ab47b ======================================================
```

### EventListener Annotation

- Spring 4.2부터 @EventListener 를 통해 이벤트 리스너를 구현할 수 있다.
- `EventListenerMethodProcessor`에서 @EventListener 빈을 등록한다.

```java
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

@Slf4j
@Component
public class OrderListener {

	@Async
	@EventListener(condition = "#event.productNo != null && !#event.productNo.isEmpty()")
	public void syncOrderLines(CompletedOrderEvent event) {
		log.info("Handling synchronized order lines ... {}", event);
	}
}
```

- @EventListener
    - `classes`: 리스너가 처리하는 이벤트 클래스 정의
    - `condition`: 이벤트 처리 조건부 선언, [SpEL 표현식 정의](https://www.baeldung.com/spring-expression-language)
    - `id`: 식별자 지정
        - 식별자는 이벤트 리스너를 등록, 제거하기 위한 목적으로 사용
        - 기본적으로 메서드 시그니처를 기반으로 이벤트 리스너의 식별자 생성
        - e.g. "com.gmoon.events.OrderListener.`syncOrderLines`(com.gmoon.events.`CompletedOrderEvent`)"

```java
package org.springframework.context.event;

public class ApplicationListenerMethodAdapter implements GenericApplicationListener {

	@Override
	public String getListenerId() {
		String id = this.listenerId;
		if (id == null) {
			id = getDefaultListenerId();
			this.listenerId = id;
		}
		return id;
	}

	protected String getDefaultListenerId() {
		Method method = getTargetMethod();
		StringJoiner sj = new StringJoiner(",", "(", ")");
		for (Class<?> paramType : method.getParameterTypes()) {
			sj.add(paramType.getName());
		}
		return ClassUtils.getQualifiedMethodName(method) + sj.toString();
	}

}
```

### Event Listener Order

- 이벤트 리스너 주석과 함께 @Order 사용
    - org.springframework.core.annotation.Order
- 리스너 실행 순서는 `Order`의 값이 클 수록 먼저 실행된다.

```java
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Slf4j
@Component
public class OrderListener {

	@Order(Ordered.LOWEST_PRECEDENCE)
	@Async
	@EventListener(condition = "#event.productNo != null && !#event.productNo.isEmpty()")
	public void syncOrderLines(CompletedOrderEvent event) {
		log.info("Handling synchronized order lines ... {}", event);
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Async
	@EventListener(condition = "#event.productNo != null && !#event.productNo.isEmpty()")
	public void sendMessage(CompletedOrderEvent event) {
		log.info("Handling send message... {}", event);
	}
}
```

```text
Handling synchronized order lines ... CompletedOrderEvent(productNo=0001, orderPrice=50000, productName=Clean Architecture, userName=gmoon, userEmail=gmoon0929@gmail.com)
Handling send message... CompletedOrderEvent(productNo=0001, orderPrice=50000, productName=Clean Architecture, userName=gmoon, userEmail=gmoon0929@gmail.com)
```

### Generics Support

제네릭 타입의 이벤트를 지원한다. 제네릭 타입을 활용하여 구조적으로 유연하게 설계할 수 있다.

```java
// Generic Event
@Getter
public class PostInsertEvent<T> extends ApplicationEvent
  implements ResolvableTypeProvider {

	private final ResolvableType entityType;

	public PostInsertEvent(T source) {
		super(source);
		entityType = ResolvableType.forInstance(getSource());
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(
		  getClass(),
		  entityType);
	}
}

// Generic Listener
@Component
@RequiredArgsConstructor
public class PersistenceHandler {

	private final EntityManager em;

	@EventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void save(PostInsertEvent<?> event) {
		Object entity = event.getSource();
		em.persist(entity);
	}
}

// Test
@SpringBootTest
class PersistenceHandlerTest {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired
	private JpaCartRepository repository;

	@Test
	void save() {
		UserId userId = new UserId(UUID.randomUUID().toString());
		ProductNo productNo = new ProductNo(UUID.randomUUID().toString());
		Cart cart = new Cart(userId, productNo);

		publisher.publishEvent(new PostInsertEvent<>(cart));
		Awaitility.await()
		  .pollDelay(Duration.ofSeconds(1))
		  .atMost(Duration.ofSeconds(3))
		  .untilAsserted(() -> assertThat(repository.exists(Example.of(cart)))
			.isTrue());
	}
}
```

### TransactionalEventListener Annotation

- Spring 4.2부터 `@TransactionalEventListener`을 제공한다.
- 이벤트의 리스너를 트랜잭션의 단계에 바인딩할 수 있다.
    - 스프링의 트랜잭션 정책은 서비스의 메서드 단위로 생성되고 관리되기 때문에, 프로덕션 코드의 트랜잭션과 이벤트 발생 시점의 트랜잭션이 불일치가 발생할 수 있다.
    - @EventListener 의 경우 이벤트 발행 시점에 리스닝되어 프로덕션 코드에서 예외가 발생되더라도 이벤트를 처리한다.
    - @TransactionalEventListener 의 경우 지정한 트랜잭션 전략에 의해 이벤트 리스닝을 하기 때문에 트랜잭션 불일치 문제를 해결할 수 있다.

```java

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EventListener // @EventListener 를 확장한 어노테이션 
public @interface TransactionalEventListener {
	// ...
}
```

- org.springframework.transaction.event.TransactionPhase
- org.springframework.transaction.event.TransactionalEventListener
    - fallbackExecution: 트랜잭션이 없을 경우 이벤트 처리 여부 (default false)
    - phase: 트랜잭션 이벤트 처리 단계
        - BEFORE_COMMIT: 트랜잭션 커밋 직전에 이벤트 발생
        - AFTER_COMMIT (default): 트랜잭션이 성공적으로 완료된 경우, 이벤트 발생
        - AFTER_ROLLBACK: 트랜잭션이 롤백된 경우
        - AFTER_COMPLETION: 트랜잭션이 완료된 경우

```java

@Component
public class TxEventHandler {

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void validation(PostInsertEvent<?> event) {
		log.info("Handling event inside a transaction BEFORE COMMIT.");
	}
	
	// ...
}
```

## Reference

- [Spring docs - Context Functionality Events](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-events)
- [Spring docs - Application Events and Listeners](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.application-events-and-listeners)
- [Spring docs - Generic Events](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-events-generics)
- [baeldung - Spring events](https://www.baeldung.com/spring-events)
- [baeldung - Spring context events](https://www.baeldung.com/spring-context-events)
- [baeldung - Spring Expression Language Guide](https://www.baeldung.com/spring-expression-language)

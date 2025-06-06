# Spring TestContext Framework

## TestContext Caching

[Spring TestContext Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-framework)
의 핵심은 테스트 간 `Spring IoC Container`의 캐시 관리다.

프로젝트 규모가 커질 수록 컨텍스트에서 관리하는 빈들이 많아지게 된다. 특히 Database, JPA, AMQP, Redis 설정과 같이 비교적 인스턴스 생성 비용이 큰 빈들은 ApplicationContext 로드 시간을 지연시키는 큰 빈들이다.

이처럼 ApplicationContext 는 각각의 테스트마다 컨텍스트를 로드하기엔 부담되기 때문에, **기본적으로 한 번 로드된 ApplicationContext 은 캐시하여 각 테스트마다 재사용한다.**

## Context 관리 캐시 관리

`TestContext`는 테스트 인스턴스에 대한 캐시 및 컨텍스트 관리를 제공한다.

- 기본적으로 테스트 인스턴스는 구성된 ApplicationContext 를 통해 자동으로 주입받지 않는다.
- 테스트 클래스가 ApplicationContextAware 인터페이스를 구현하는 경우 ApplicationContext 에 대한 참조가 테스트 인스턴스에 제공된다.
    - @ExtendWith(SpringExtension.class)
    - ApplicationContextAware
        - AbstractJUnit4SpringContextTests
            - DependencyInjectionTestExecutionListener
- Spring 5.0 이후 부터는 지원하는 다양한 테스트 어노테이션을 활용하면 구성된 ApplicationContext를 주입받을 수 있다.
  - @SpringBootTest
  - @SpringJUnitConfig
  - @SpringJUnitWebConfig
  - [그외 테스트 어노테이션 참고, docs.spring.io - Test auto configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/test-auto-configuration.html)

### Context Configuration with Component Classes

[TestContext](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-ctx-management-javaconfig) 를 구성하는 방법은 여러 있지만 Java 기반의 설정 방식을 소개한다.

```java
@Configuration
public class TestConfig {

  @Bean
  public EmailService emailService() {
    return new EmailService();
  }
}

@ExtendWith(SpringExtension.class)
// ApplicationContext will be loaded from TestConfig
@ContextConfiguration(classes = { TestConfig.class })
class UserServiceTest {
  // test
}
```

- @Configuration 를 활용하여 테스트 전용 설정 구성을 한다.
- 커스터한 테스트 ApplicationContext를 구성하기 위해, @ContextConfiguration 어노테이션을 지정한다.

### Context Caching

1. ApplicationContext(또는 WebApplicationContext) 로드
2. ApplicationContext 설정 값에 의해 식별 가능한 고유 키 생성, 내부 컨텍스트 캐시 저장
3. 테스트에서 구성한 컨텍스트 키가 동일한 경우 재사용

Spring TestContext 프레임워크는 아래 조건에 따라 컨텍스트 캐시 키를 생성한다.

- @ContextConfiguration
    - locations
    - classes
    - contextInitializerClasses
    - contextLoader
- contextCustomizers (from ContextCustomizerFactory)
    - @DynamicPropertySource 메서드
    - @MockBean
    - @SpyBean
    - ContextCustomizer
    - 그외 커스텀 컨텍스트 구현 클래스 참고
      - ContextCustomizer
        - MockitoContextCustomizer
      - ContextCustomizerFactory
        - MockitoContextCustomizerFactory
- @ContextHierarchy
    - parent
- @ActiveProfiles
- @TestPropertySource
    - propertySourceLocations
    - propertySourceProperties
- @WebAppConfiguration
    - resourceBasePath

컨텍스트의 빈이 오염될 경우 ApplicationContext 는 재로드 된다.

예를 들어 @MockBean 이나 @SpyBean 은 기존 빈을 재사용할 수 없음으로 내부적으로 재로드 하여 캐시로 관리하게 된다.

> `컨텍스트 캐시 관리` 이외 Spring TestContext Framework 의 주된 매커니즘은 `어노테이션 기반의 테스트`다.<br/>
> 참고) `org.springframework.boot.test` 패키지

```java
@Slf4j
class CachingApplicationContextTest {

	static final Map<Integer, ApplicationContext> CONTEXT_CACHE = new HashMap<>();

	@AfterAll
	static void afterAll() {
		log.info("cache context size: {}", CONTEXT_CACHE.size());
	}

	@Nested
	@SpringBootTest
	class Case1Test {

		@Autowired ApplicationContext context;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}

	@Nested
	@SpringBootTest
	class Case2Test {

		@Autowired ApplicationContext context;
		@MockBean UserService userService;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}

	@Nested
	@SpringBootTest
	class Case3Test {

		@Autowired ApplicationContext context;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}

	@Nested
	@SpringBootTest
	class Case4Test {

		@Autowired ApplicationContext context;
		@MockBean UserService userService;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}

	@Nested
	@SpringBootTest
	class Case5Test {

		@Autowired ApplicationContext context;
		@SpyBean UserService userService;

		@Test
		void case1() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}

		@Test
		void case2() {
			CONTEXT_CACHE.put(context.hashCode(), context);
		}
	}
}
```
```text
2022-09-10 18:14:35.314  INFO 88984 --- [           main] c.g.c.c.CachingApplicationContextTest    : cache context size: 3
```

다음 테스트 코드에서 Application 은 총 3번 로드된다.

- 기본 컨텍스트 구성
- @MockBean 사용으로 인한 커스텀 컨텍스트 구성 
- @SpyBean 사용으로 인한 커스텀 컨텍스트 구성 

이외에도 캐시 메커니즘의 이점을 얻으려면 모든 테스트가 동일한 프로세스 또는 테스트내에서 실행되어야 한다.

마찬가지로 Ant, Maven 또는 Gradle과 같은 빌드 프레임워크로 테스트를 실행할 때 빌드 프레임워크가 테스트 간에 분기되지 않도록 하는 것이 중요하다. 예를 들어 Maven Surefire 플러그인의 `forkMode`가 `always` 또는 `pertest` 로 설정된 경우 TestContext 프레임워크는 테스트 클래스 간에 애플리케이션 컨텍스트를 캐시할 수 없으며 결과적으로 빌드 프로세스가 훨씬 더 느리게 실행된다.

## TextContext Property

ApplicationContext 의 캐시 정책이 있다지만, 불필요하게 로드된 ApplicationContext 가 많다면 빌드 시간이 오래 걸릴 수 밖에 없다.

통합 테스트 속도를 개선하기 위해선 캐시된 컨텍스트 수를 아는 것이 중요하다. 컨텍스트 캐시에 대한 통계를 로깅할 수 있도록 프로퍼티 설정을 추가해줘야 한다.

```properties
logging.level.org.springframework.test.context.cache=DEBUG
```

컨텍스트 캐시에 대한 통계를 보면 다음과 같다.

```text
2022-09-10 14:12:41.945 DEBUG 79250 --- [           main] org.springframework.test.context.cache   : Spring test ApplicationContext cache statistics: [DefaultContextCache@1c32886a size = 1, maxSize = 32, parentContextCount = 0, hitCount = 63, missCount = 2]
```

- [DefaultContextCache](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/context/cache/DefaultContextCache.html)
    - size: 현재 캐시되어 관리되는 컨텍스트 수
    - maxSize: 관리할 수 있는 최대 캐시 컨텍스트 수
    - parentContextCount: 상위 컨텍스트 설정 수
    - hitCount: 캐시된 컨텍스트 호출 수
    - missCount: 새로 로드된 컨텍스트 수

```properties
# 32mb 기본값
spring.test.context.cache.maxSize=32
```

- `-Dspring.test.context.cache.maxSize=50`
    - 설정된 `maxSize` 프로퍼티 값은 기본적으로 `32`로 제한된다.
    - 캐싱된 컨텍스트 수(`size`)가 최대 크기(`maxSize`)에 도달할 때마다 LRU(Least recently used) 제거 캐시 정책에 따라 오래된 컨텍스트를 제거하고 닫는다.
    - JVM 옵션을 통해 `maxSize`를 설정할 수 있다.
        - `-Dspring.test.context.cache.maxSize=32`

## ApplicationContext Lifecycle

캐시된 테스트 ApplicationContext 는 다음 시나리오 중 하나일 경우 종료될 수 있다.

- 컨텍스트는 @DirtiesContext 의 정의된 전략에 의해 닫힌다.
    - BEFORE_CLASS
    - BEFORE_EACH_TEST_METHOD
    - BEFORE_METHOD
    - **`AFTER_METHOD (default)`**
    - AFTER_EACH_TEST_METHOD
    - AFTER_CLASS
- 컨텍스트는 LRU 캐시 정책에 따라 축출될 경우 닫힌다.
- 컨텍스트는 JVM이 종료될 때 JVM 종료 후크를 통해 닫힌다.

> Spring TestContext Framework 의 Context 캐시 전략으로 인해, 테스트에서 한번 로드된 ApplicationContext 는 재사용된다.</br>
> 캐시된 ApplicationContext에 등록된 빈의 속성 값이 바뀌었거나 제거, 또는 수정되거나, 기존과 달리 설정이 된다면 다음 테스트가 실패할 경우가 발생할 수 있다.<br/>
`@DirtiesContext`은 캐시된 `ApplicationContext`에 등록된 Bean 이 오염될 경우 컨텍스트를 다시 생성하기 위해 사용한다.<br/>

## ApplicationContext Hierarchies

Spring TestContext 는 컨텍스트 계층 구성을 지원한다.

부모 설정과 자식 설정을 분리하여 Application 설정을 확장시키는 방식으로 자세한 내용은 [Context Hierarchies](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-ctx-management-ctx-hierarchies) 를 참고하자.

## Reference

- [docs.spring.io - Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html)
    - [3.2.1. Context Management and Caching](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testing-ctx-management)
    - [3.5.6. Context Management](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-ctx-management)
    - [Context Caching](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#testcontext-ctx-management-caching)
- [docs.spring.io - Test auto configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/test-auto-configuration.html)
- [github - Spring boot test autoconfigure](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-test-autoconfigure/src/main/java/org/springframework/boot/test/autoconfigure)

# Awaitility

비동기 테스트 지원하는 테스팅 라이브러리로 다양한 DSL을 지원한다.

## Dependency

- [org.awaitility:awaitility](https://mvnrepository.com/artifact/org.awaitility/awaitility)

```xml

<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <version>4.2.0</version>
</dependency>
```

## Examples

자세한 사용법은 [github awaitility - Usage](https://github.com/awaitility/awaitility/wiki/Usage) 참고하자.

```java
class EventServiceTest {

  EventService eventService;

  @BeforeEach
  void setUp() {
    EventRepository repository = new EventRepository();
    eventService = new EventService(repository);
    eventService.deleteAll();

    // 500 millisecond delay
    ExecutorService executor = Executors.newCachedThreadPool();
    executor.submit(() -> eventService.save(new Event("hello gmoon.")));
  }

  @DisplayName("lambda 표현식")
  @Test
  void case1() {
    // then
    Awaitility.await("lambda 표현식")
      .pollDelay(Duration.ofSeconds(1)) // 1초 대기
      .pollInterval(Duration.ofSeconds(1)) // 1초 마다 확인
      .atMost(Duration.ofSeconds(5)) // 최대 대기 시간 설정, default 10 초
      // assertion
      // .until(() -> eventRepository.size() == 1)
      .until(eventService::count, CoreMatchers.equalTo(1));
  }

  @DisplayName("AssertJ 단언문 사용")
  @Test
  void withAssertJ() {
    // then
    Awaitility.await("AssertJ 단언문 사용")
      .pollDelay(Duration.ofSeconds(1)) // 1초 대기
      .pollInterval(Duration.ofSeconds(1)) // 1초 마다 확인
      .atMost(Duration.ofSeconds(5)) // 최대 대기 시간 설정, default 10 초
      .untilAsserted(
        () -> assertThat(eventService.count()).isNotZero()
      );
  }

  @DisplayName("Timeout 예외")
  @Test
  void error1() {
    // then
    assertThatThrownBy(() ->
        Awaitility.await("Timeout 예외")
          .atMost(Duration.ofMillis(200)) // 최대 대기 시간 설정, default 10 초
//					.timeout(Duration.ofMillis(200))
          .untilAsserted(() ->
            Assertions.assertThat(eventService.count())
              .isNotZero()
          )
    ).isInstanceOf(ConditionTimeoutException.class);
  }
}
```

## Reference

- [www.awaitility.org](http://www.awaitility.org/)
- [github - awaitility](https://github.com/awaitility/awaitility)
- [github - awaitility wiki](https://github.com/awaitility/awaitility/wiki/Usage)
- [maven - awaitility](https://search.maven.org/artifact/org.awaitility/awaitility/4.2.0/jar)

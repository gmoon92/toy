# Spring Boot Project Config

- Spring Boot 2.4.1 ver

## SLF4J log.debug() not work


![logging1](./img/logging1.png)

```
# Application Global Config
logging.level.root=info
```

1. ERROR
2. WARN
3. INFO
4. DEBUG
5. TRACE

## logging.properties
## org.springframework.boot.logging.logging.properties

![logging0](./img/logging0.png)

### LoggingApplicationListener.class

![logging2](./img/logging2.png)
![logging3](./img/logging3.png)

---

## TODO

- slf4j 사용, 구현은 log4j2 고려
    - [https://www.baeldung.com/spring-boot-testing-log-level](https://www.baeldung.com/spring-boot-testing-log-level)
    - [https://goddaehee.tistory.com/206](https://goddaehee.tistory.com/206)

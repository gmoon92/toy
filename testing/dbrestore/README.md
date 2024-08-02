# Integration testing without @Transactional

## [Test is FIRST](https://gmoon92.github.io/test/2018/08/24/test-driven-development.html)

- Fast: 단위 테스트는 빨라야 한다.
- Independent: 각각의 테스트 케이스는 독립적으로 동작한다.
- Repeat: 멱등성을 보장하라. 어느 환경에서, 반복적으로 테스트 동일한 결과를 보장해야한다.
- Self-validating: 비즈니스 로직에 대해 자체 검증 가능하도록 구현해라.
- Timely: 미루지 마라. 제품 코드를 구현전에 테스트 코드 구성해라.

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

# Integration testing without @Transactional

- Tests run: 22, Failures: 0, Errors: 0, Skipped: 1
- Total time: 10.744 s -> 11.018 s

## Enable Total time:  11.348 s

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
- https://bytebuddy.net

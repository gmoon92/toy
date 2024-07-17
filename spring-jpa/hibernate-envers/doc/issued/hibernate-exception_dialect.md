# Spring boot MySQL 연동 에러

### Caused by: org.hibernate.HibernateException: Unable to determine Dialect without JDBC metadata (please set 'jakarta.persistence.jdbc.url' for common cases or 'hibernate.dialect' when a custom Dialect implementation must be provided)

```text

2024-07-17T22:58:46.787+09:00  WARN 17775 --- [           main] o.h.e.j.e.i.JdbcEnvironmentInitiator     : HHH000339: Could not obtain connection metadata: java.sql.SQLSyntaxErrorException: Unknown column 'RESERVED' in 'where clause'
2024-07-17T22:58:46.787+09:00  WARN 17775 --- [           main] o.h.e.j.e.i.JdbcEnvironmentInitiator     : HHH000342: Could not obtain connection to query metadata

Caused by: org.hibernate.HibernateException: Unable to determine Dialect without JDBC metadata (please set 'jakarta.persistence.jdbc.url' for common cases or 'hibernate.dialect' when a custom Dialect implementation must be provided)
	at org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl.determineDialect(DialectFactoryImpl.java:191) ~[hibernate-core-6.5.2.Final.jar:6.5.2.Final]
	at org.hibernate.engine.jdbc.dialect.internal.DialectFactoryImpl.buildDialect(DialectFactoryImpl.java:87) ~[hibernate-core-6.5.2.Final.jar:6.5.2.Final]
	...
```

### 해결

dialect 설정

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```





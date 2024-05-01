# [Hibernate and Data.sql](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.5-Release-Notes#sql-script-datasource-initialization)

Spring boot 2.5.x 부터

SqlDataSourceScriptDatabaseInitializer 가 먼저 초기화가 진행되고, 이후 JPA 관련 빈 초기화 작업되도록 실행 순서 변경됌.

1. `spring.sql.init` 수행
    - schema.sql, data.sql
2. EntityManagerFactory 초기화 작업

```text
By default, data.sql scripts are now run before Hibernate is initialized. This aligns the behavior of basic script-based initialization with that of Flyway and Liquibase. If you want to use data.sql to populate a schema created by Hibernate, set spring.jpa.defer-datasource-initialization to true. While mixing database initialization technologies is not recommended, this will also allow you to use a schema.sql script to build upon a Hibernate-created schema before it’s populated via data.sql.

기본적으로 data.sql 스크립트는 이제 Hibernate가 초기화되기 전에 실행됩니다. 이는 기본 스크립트 기반 초기화 동작을 Flyway 및 Liquibase의 동작과 일치시킵니다. Hibernate에 의해 생성된 스키마를 채우기 위해 data.sql을 사용하려면 `spring.jpa.defer-datasource-initialization`을 true로 설정하십시오.

데이터베이스 초기화 기술을 혼합하는 것은 권장되지 않지만, 이는 data.sql을 통해 채워지기 전에 Hibernate에서 생성된 스키마를 기반으로 구축하기 위해 Schema.sql 스크립트를 사용할 수도 있습니다.
```

```properties
spring.sql.init.mode=always
spring.sql.init.continue-on-error=false
spring.jpa.defer-datasource-initialization=true
```

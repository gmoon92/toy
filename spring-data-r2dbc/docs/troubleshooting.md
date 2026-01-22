Spring Data JPA with R2DBC config

## Parameter 0 of method jdbcDataSource in com.gmoon.springdatar2dbc.global.JpaConfig required a bean of type 'org.springframework.boot.autoconfigure.jdbc.DataSourceProperties' that could not be found.

이 메시지는 DataSourceProperties 빈(bean)이 없어서 JpaConfig 클래스의 jdbcDataSource 메서드 파라미터를 채울 수 없다는 의미다.
즉, 스프링이 JDBC용 데이터소스 속성 빈을 찾지 못해 생긴 문제다.

- JPA와 R2DBC를 섞어쓰며 트랜잭션, 데이터소스, Repository 충돌 발생
- 빈 주입/설정에서 DataSource와 ConnectionFactory가 꼬이는 현상
- basePackage, Repository 어노테이션 관리가 불명확
- 서비스 레이어에서 동기/비동기 코드 혼용 → 예상치 못한 버그

```java

@Profile("jpa")
@Configuration
@EnableJpaRepositories(
        basePackages = {"com.gmoon.*"},
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ReactRepository.class)
)
public class JpaConfig {
}

@Profile("r2dbc")
@Configuration
@EnableR2dbcRepositories(
        basePackages = {"com.gmoon.*"},
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ReactRepository.class)
)
public class R2dbcConfig {

}
```

JPA와 R2DBC를 완전히 "구분"해서만 관리하면 둘 다 하나의 프로젝트에서 충분히 병행 사용 가능!

keyword: Spring Boot jpa r2dbc multi datasource

```text
Description:

Parameter 0 of method jdbcDataSource in com.gmoon.springdatar2dbc.global.JpaConfig required a bean of type 'org.springframework.boot.autoconfigure.jdbc.DataSourceProperties' that could not be found.


Action:

Consider defining a bean of type 'org.springframework.boot.autoconfigure.jdbc.DataSourceProperties' in your configuration.

2025-06-06T01:47:45.219+09:00  WARN 77459 --- [           main] o.s.test.context.TestContextManager      : Caught exception while allowing TestExecutionListener [org.springframework.test.context.support.DependencyInjectionTestExecutionListener] to prepare test instance [com.gmoon.springdatar2dbc.SpringDataR2dbcApplicationTests@4d9754a8]

```

---

## DataSource bean registered

- jpa datasource
- r2dbc datasource

## ScriptStatementFailedException

`spring.sql.init`

- SqlDataSourceScriptDatabaseInitializer
  - SettingsCreator
  - DataSourceInitializationConfiguration
  - `R2dbcInitializationConfiguration`

> (R2DBC version error.)(https://stackoverflow.com/questions/74972672/java-lang-integer-cannot-be-cast-to-class-java-lang-long-in-r2dbc-mysql-model-cl)

```text
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'r2dbcScriptDatabaseInitializer' defined in class path resource [org/springframework/boot/autoconfigure/sql/init/R2dbcInitializationConfiguration.class]: Invocation of init method failed; nested exception is org.springframework.r2dbc.connection.init.ScriptStatementFailedException: Failed to execute SQL script statement #1 of URL [file:/Users/moongyeom/IdeaProjects/private/toy/spring-data-r2dbc/target/classes/data.sql]: insert into tb_team (id, name) values ('team01', 'team01'); nested exception is java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.Integer

...

Caused by: org.springframework.r2dbc.connection.init.ScriptStatementFailedException: Failed to execute SQL script statement #1 of URL [file:/Users/moongyeom/IdeaProjects/private/toy/spring-data-r2dbc/target/classes/data.sql]: insert into tb_team (id, name) values ('team01', 'team01'); nested exception is java.lang.ClassCastException: java.lang.Long cannot be cast to java.lang.Integer
```

## org.springframework.dao.InvalidDataAccessApiUsageException

```text
Caused by: org.springframework.dao.InvalidDataAccessApiUsageException: Reactive Repositories are not supported by JPA; Offending repository is com.gmoon.springdatar2dbc.teams.team.domain.TeamReactorRepository
```

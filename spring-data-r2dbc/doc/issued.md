Spring Data JPA with R2DBC config

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

## Spring boot version up

2.7.14 -> 3.3.0

### org.thymeleaf.extras:thymeleaf-extras-springsecurity

- thymeleaf-extras-springsecurity5 -> thymeleaf-extras-springsecurity6

```text
thymeleaf-extras-springsecurity5 for integration with Spring Security 5.x
thymeleaf-extras-springsecurity6 for integration with Spring Security 6.x
```

[github - thymeleaf](https://github.com/thymeleaf/thymeleaf-extras-springsecurity)

```xml
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

### org.mockito:mockito-inline

Spring boot 3 without mockito inline version.

https://github.com/mockito/mockito/wiki/What%27s-new-in-Mockito-2#unmockable

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-inline</artifactId>
    <version>5.2.0</version>
    <scope>test</scope>
</dependency>
```

### hibernate version

5.6.15 Final -> 6.5.2.Final

- package 변경
    - javax.persistence.* -> jakarta.persistence.*
    - javax.annotation.* -> jakarta.annotation.*
- dialect 변경
    - MariaDB10Dialect -> org.hibernate.dialect.MariaDBDialect
    - MySQL8Dialect -> org.hibernate.dialect.MySQLDialect
- 의존성 변경
    - org.hibernate:hibernate-core
    - org.hibernate:hibernate-envers
    - org.hibernate:hibernate-entitymanager -> hibernate.core pom type으로 대체

```xml

<dependencis>
    <!-- https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-core -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.5.2.Final</version>
    </dependency>

    <!-- https://mvnrepository.com/artifact/org.hibernate.orm/hibernate-envers -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-envers</artifactId>
        <version>6.5.2.Final</version>
    </dependency>

    <!-- -->
    <!--
    org.hibernate:hibernate-core:6.5.2.Final
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-entitymanager</artifactId>
        <version>${hibernate.version}</version>
    </dependency>
    -->
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.5.2.Final</version>
        <type>pom</type>
    </dependency>
</dependencis>
```

### org.springframework.boot.autoconfigure.cache.CacheType EHCACHE deprecated

### Spring integration migration

https://github.com/spring-projects/spring-integration/wiki/Spring-Integration-5.x-to-6.0-Migration-Guide

- IntegrationFlows -> IntegrationFlow

### Spring data redis

spring.redis -> spring.data.redis

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      connect-timeout: 3000
```

- https://docs.spring.io/spring-data/redis/reference/redis/template.html

### org.springframework.http.HttpMethod static method

HttpMethod 클래스는 3버전 부터는 클래스 타입으로 변경됌. (2 버전에선 이넘 클래스였다.)


# Embedded Redis

- Redis Server 테스트 환경 구축
 - [kstyrc 라이브러리 방식](#1-embedded-redis-for-kstyrc)
 - [ozimov 라이브러리 방식](#2-embedded-redis-for-ozimov)

## Environment

- Spring boot 2.7.5
- com.github.kstyrc:embedded-redis:0.6
- it.ozimov:embedded-redis:0.7.3

## 1. Embedded Redis for kstyrc

### 1.1. Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
    <groupId>com.github.kstyrc</groupId>
    <artifactId>embedded-redis</artifactId>
    <version>0.6</version>
    <scope>test</scope>
</dependency>
```

### [1.2. Using](https://github.com/kstyrc/embedded-redis)

```java
RedisServer redisServer = new RedisServer(6379);

redisServer.start();
// do some work
redisServer.stop();
```

### 1.3. Java Config

````yaml
env: local
spring:
  redis:
    port: 6379
````

```java
import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedRedisConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnProperty(value = "env", havingValue = "local")
    public redis.embedded.RedisServer redisServer(RedisProperties redisProperties) throws IOException {
        int port = redisProperties.getPort();
        return new redis.embedded.RedisServer(port);
    }
}
```

## 2. Embedded Redis for ozimov

### 2.1. Dependency

```xml
<dependency>
  <groupId>it.ozimov</groupId>
  <artifactId>embedded-redis</artifactId>
  <version>0.7.3</version>
</dependency>
```

### [2.2. Using](https://github.com/ozimov/embedded-redis)

```java
RedisServer redisServer = new RedisServer(6379);

redisServer.start();
// do some work
redisServer.stop();
```

### 2.3. Java Config

```java
import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddedRedisConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
	@ConditionalOnProperty(value = "env", havingValue = "local")
    public redis.embedded.RedisServer redisServer(RedisProperties redisProperties) throws IOException {
        int port = redisProperties.getPort();
        return new redis.embedded.RedisServer(port);
    }
}
```

## 라이브러리 선택

[ozimov](https://github.com/ozimov/embedded-redis) 는 기존 [kstyrc](https://github.com/kstyrc/embedded-redis) 저장소를 fork 한 저장소다. 때문에 두 라이브러리에서 제공하는 `redis.embedded`.RedisServer 클래스는 패키지 경로가 동일함으로 테스트 환경에서 혼용해서 사용할 수 없다.

여러 기술 블로그에서 ozimov 라이브러리를 사용하여 로컬 환경의 임베디드 레디스 서버 설정하는 방식을 작성되어 있던데, 개인적으로 kstyrc 라이브러리를 선택하는게 맞다고 생각한다.

## Reference

- [baeldung - Spring Embedded Redis](https://www.baeldung.com/spring-embedded-redis)
- [kstyrc - Embedded Redis](https://github.com/kstyrc/embedded-redis)
- [ozimov - Embedded Redis](https://github.com/ozimov/embedded-redis)

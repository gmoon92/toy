# Spring Data Redis

- Spring Boot 2.6.2
- Redis Client Jedis 3.8.0
- Java 8

### Docker run redis

1. Docker run
    - `docker/docker-compose.yml`
    - `docker-run`
2. Redis 컨테이너 redis-cli(Redis command line interface) 접근
    - `docker ps -a`
    - `docker exec -it {CONTAINER ID} redis-cli --raw`

``` text
docker ps -a
CONTAINER ID    IMAGE    COMMAND                  CREATED          STATUS        PORTS                      NAMES
80c1d069007f    redis    "docker-entrypoint.s…"   10 minutes ago   Up 10 minutes 0.0.0.0:6379->6379/tcp     redis

docker exec -it 80c1d069007f redis-cli --raw
127.0.0.1:6379> set hello redis
OK
127.0.0.1:6379> get hello
"redis"
127.0.0.1:6379> del hello
(integer) 1
127.0.0.1:6379> get hello
(nil)
127.0.0.1:6379> FLUSHALL
OK
```

### WRONGTYPE Operation against a key holding the wrong kind of value

Redis 조회시 [Type](https://redis.io/commands/type) 을 확인하여, 조회할 Key의 타입에 맞게 명령어를 사용해야 한다.

```text
- if value is of type string -> GET <key>
- if value is of type hash -> HGETALL <key>
- if value is of type lists -> lrange <key> <start> <end>
- if value is of type sets -> smembers <key>
- if value is of type sorted sets -> ZRANGEBYSCORE <key> <min> <max>
- if value is of type stream -> xread count <count> streams <key> <ID>.
```

- [redis.io - commands](https://redis.io/commands)
- [redisgate.kr - redis-cli](http://redisgate.kr/redis/server/redis-cli.php)

### Maven Dependencies

```text
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
 </dependency>

<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>3.8.0</version>
</dependency>
```

### Jedis Properties config

```yaml
spring:
   redis:
      client-type:jedis
      client-name:gmoon-redis-client
      connect-timeout:10
      host:localhost
      port:6379

   jedis:
      pool:
         enabled:true
```

### RedisTemplate

기본적으로 RedisTemplate 은 Key/Value 에 `Serializer`를 설정해줘야 한다.

```java
@Configuration
public class RedisConfig {
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) { 
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(factory);
		redisTemplate.setKeySerializer(StringRedisSerializer.UTF_8);
		return redisTemplate;
	}
}
```

만약 `Serializer`를 지정하지 않았다면, 기본적으로 JdkSerializationRedisSerializer 로 설정된다.

> 참고) `org.springframework.data.redis.core.RedisTemplate#afterPropertiesSet()`

### `\xac` 문자열 -> JdkSerializationRedisSerializer

등록된 `org.springframework.data.redis.core.RedisTemplate` 빈을 활용하여 Redis 서버에 데이터를 저장하게 되면 `\xac` 문자열을 확인할 수 있다.

```text
127.0.0.1:6379> keys *
1) "\xac\xed\x00\x05t\x00\x04hello"
```

이러한 이유엔 Redis 서버에 JdkSerializationRedisSerializer 를 사용하여 데이터를 저장하고 있기 때문이다. `\xac` 문자는 Java 직렬화 과정에서 객체의 주소 값을 그대로 저장하고 있다고 생각하면 된다.

> 참고) [Stackoverflow - Get Set value from Redis using RedisTemplate](https://stackoverflow.com/questions/31608394/get-set-value-from-redis-using-redistemplate)

### SerializationException

JdkSerializationRedisSerializer 사용 시, Redis 서버에 저장된 데이터와 SerialNumber 버전 불 일치시 오류가 발생한다.

- 저장할 때 사용했던 커스텀 클래스의 프로퍼티 변경
- 커스텀 클래스의 새로운 프로퍼티 함수를 추가
- 커스텀 클래스의 버전이 저장되있어, 수정하면 버전 불일치

```
nested exception is org.springframework.data.redis.serializer.SerializationException: Cannot deserialize; 
    nested exception is org.springframework.core.serializer.support.SerializationFailedException: Failed to deserialize payload. 
    Is the byte array a result of corresponding serialization for DefaultDeserializer?
```

어쩌면 RedisTemplate 의 Value의 Serializer 를 JSon 타입을 지원하는 Serializer로 설정하는게 맞을 수 있다고 생각할 수 있다. 하지만 이 역시 운영 상의 이슈가 존재한다. [다음 글을 참고하자.](https://mongsil-jeong.tistory.com/25)

결과적으로 `org.springframework.data.redis.core.StringRedisTemplate` 와 ObjectMapper 를 활용하여, 데이터를 직접 serialize/deserialize 하는 편이 용이할 수 있다.

### Reference

- [Redis Documentation](https://redis.io/documentation)
- [lettuce.io - Reference](https://lettuce.io/core/release/reference)
- [Spring Reference - Spring Data Redis](https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#reference)
- [Spring Reference - Redis Serializer](https://docs.spring.io/spring-data/redis/docs/current/reference/html/#redis:serializer)
- [Baeldung - Spring Data Redis’s Property-Based Configuration](https://www.baeldung.com/spring-data-redis-properties)
- [Baeldung - Spring Boot Redis cache](https://www.baeldung.com/spring-boot-redis-cache)
- [Baeldung - An Introduction to Spring Data Redis Reactive](https://www.baeldung.com/spring-data-redis-reactive)
- [Baeldung - Embedded Redis Server with Spring Boot Test](https://www.baeldung.com/spring-embedded-redis)
- [Baeldung - List All Available Redis Keys](https://www.baeldung.com/redis-list-available-keys)
- [Baeldung - List All Redis Databases](https://www.baeldung.com/redis-list-all-databases)
- [Baeldung - Delete Everything in Redis](https://www.baeldung.com/redis-delete-data)
- [Baeldung - Memcached vs Redis](https://www.baeldung.com/memcached-vs-redis)
- [Baeldung - Redis vs MongoDB](https://www.baeldung.com/java-redis-mongodb)
- [Baeldung - Spring Boot Cache with Redis](https://www.baeldung.com/spring-boot-redis-cache)
- [Redisgate - 레디스 명령어 한글판](http://redisgate.com/redis/command/commands.php)
- [How to use Redis-Template in Java Spring Boot](https://medium.com/@hulunhao/how-to-use-redis-template-in-java-spring-boot-647a7eb8f8cc)
- [RedisTemplate 을 이용해서 Multi Pojo get/set 할 때 이슈사항](https://mongsil-jeong.tistory.com/25)
- [이슈 7. Redis에 여러 개의 데이터 추가 시 네트워크 문제 해결하기 : Redis PipeLining](https://velog.io/@meme2367/MindDiary-%EC%9D%B4%EC%8A%88-7.-Redis%EC%97%90-%EC%97%AC%EB%9F%AC-%EA%B0%9C%EC%9D%98-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%B6%94%EA%B0%80-%EC%8B%9C-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0%ED%95%98%EA%B8%B0-Redis-PipeLining)

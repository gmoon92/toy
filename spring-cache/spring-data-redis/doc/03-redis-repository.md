# Spring Data Redis

- Spring Boot 2.6.2
- Redis Client Jedis 3.8.0
- Java 8

## 학습 목표

- [Redis Repositories](#redis-repositories)

### Redis Repositories

- Redis Server 2.8.0 버전 이상
- `Transaction` 을 지원하지 않는다.
    - Transaction 미지원 설정된 RedisTemplate 을 요구한다.

### Annotations

- `org.springframework.data.redis.core.RedisHash`
- `org.springframework.data.annotation.Id`

RedisRepository 는 레디스 서버에 데이터를 저장할 때, 두 어노테이션을 식별자로 사용한다.

```java

@Getter
@ToString
@RedisHash(value = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CacheUser implements Serializable {
	static final long MINUTES_OF_TTL = 5;

	@org.springframework.data.annotation.Id
	private String id;

	@Indexed // 조회 목적
	@EqualsAndHashCode.Include
	private String username;
	private String email;
	private boolean enabled;

	// default ttl unit seconds
	@TimeToLive(unit = TimeUnit.MINUTES)
	private Long expiration;

	@Builder
	private CacheUser(String id, String username, String email, boolean enabled) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.enabled = enabled;
		this.expiration = MINUTES_OF_TTL;
	}
}
```

- @RedisHash
- @Id
- @Indexed
- @TimeToLive

### Java Config

Redis 설정 파일에 @EnableRedisRepositories 어노테이션을 선언해준다.

- org.springframework.data.redis.repository.configuration.EnableRedisRepositories
- spring.data.redis.repositories.enabled
    - @EnableRedisRepositories 선언이 안되어 있더라도 Spring boot 에선 기본적으로 활성화 해준다.

``` java
@Configuration
@EnableRedisRepositories // 1. Redis repository enabled
public class RedisConfig {

  @Bean // 2. Redis Connector config
  public RedisConnectionFactory connectionFactory() { 
    return new JedisConnectionFactory();
  }

  @Bean 3. Redis Template config
  public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<byte[], byte[]> template = new RedisTemplate<byte[], byte[]>();
    connectionFactory.setConnectionFactory(redisConnectionFactory);
    return template;
  }
}
```

### @EnableRedisRepositories 선언

```java

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableRedisRepositories.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@ConditionalOnProperty(prefix = "spring.data.redis.repositories", name = "enabled", havingValue = "true",
	matchIfMissing = true)
@ConditionalOnMissingBean(RedisRepositoryFactoryBean.class)
@Import(RedisRepositoriesRegistrar.class)
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class RedisRepositoriesAutoConfiguration {

}
```

- org.springframework.data.redis.repository.configuration.EnableRedisRepositories
- spring.data.redis.repositories.enabled
    - @EnableRedisRepositories 선언이 안되어 있더라도 Spring boot 에선 기본적으로 활성화 해준다.

### RedisRepository

```java
public interface CacheUserRepository extends CrudRepository<CacheUser, Long> {
	@Cacheable(value = CacheUser.KEY, unless = "#result==null")
	Optional<CacheUser> findByUsername(String username);
}
```

`unless = "#result==null"` 속성 값의 의미는

CacheManager 빈 설정을 보면 `disableCachingNullValues`를 활성화했다. 이 경우 값이 null인 경우는 캐시 대상에서 제외하게 된다.

하지만 CacheManager 빈을 등록하는 과정에서 캐시를 등록하게 되는데, 이때 기본적으로 Null로 값을 셋팅한다. 하지만 필자는 `disableCachingNullValues` 설정을 활성화했기에 null 을 주입할 수 없다는 에러가 발생한다. `unless = "#result==null"` 속성 값을 정의했다.

```java
public class RedisConfig {

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		// define all cache names
		Set<String> allCacheNames = CacheName.getAll();

		// cache ttl settings
		Map<String, RedisCacheConfiguration> cacheExpireConfigs = RedisCacheExpireConfigs.create()
			.putExpireMinutes(CacheName.USER, 1)
			.getValues();

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(createRedisCacheConfig(Duration.ofSeconds(DEFAULT_CACHE_EXPIRE_SECONDS)))
			.initialCacheNames(allCacheNames) // set default cache config
			.withInitialCacheConfigurations(cacheExpireConfigs)
			.transactionAware()
			.build();
	}

	private RedisCacheConfiguration createRedisCacheConfig(Duration expireTtl) {
		return RedisCacheConfiguration.defaultCacheConfig(contextClassLoader)
                .entryTtl(expireTtl)
                .disableCachingNullValues() // disabled null value
                .prefixCacheNameWith(CacheKeyPrefix.SEPARATOR)
                .computePrefixWith(CacheKeyPrefix.simple())
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(getKeySerializer()));
	}
}
```

### Test

```java
package com.gmoon.springdataredis.user;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import com.gmoon.springdataredis.test.EmbeddedRedisConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(EmbeddedRedisConfig.class)
@SpringBootTest
class CacheUserRepositoryTest {
	@Autowired CacheUserRepository repository;
	@Autowired CacheManager cacheManager;

	@BeforeEach
	void init() {
		CacheUser cacheUser = CacheUser.builder()
			.username("gmoon")
			.build();

		repository.save(cacheUser);
	}

	@Test
	void testSave() {
		// when
		Optional<CacheUser> actual = repository.findByUsername("gmoon");

		// then
		log.info("actual: {}", actual);
		assertThat(actual)
			.isNotEmpty()
			.isEqualTo(getCachedUser("gmoon"));
	}

	private Optional<CacheUser> getCachedUser(String username) {
		return Optional.ofNullable(cacheManager.getCache(CacheUser.KEY))
			.map(cache -> cache.get(username, CacheUser.class));
	}
}
```

## Reference

- [Spring Data Redis - Redis Repository](https://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#redis.repositories)
- [Spring Embedded Redis](https://www.baeldung.com/spring-embedded-redis)

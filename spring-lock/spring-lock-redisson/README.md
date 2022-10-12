# Redisson Distributed Lock

Redisson은 [Redis에서 지원하는 여러 클라이언트](https://redis.io/docs/stack/json/clients/) 중 분산 락을 지원하는 자바 전용 클라이언트다.

## Environment

- Spring boot 2.7.4
- Java 8
- Maven
- Redisson 3.17.7

## 무결성 확보를 위한 동시성 제어

동시성 문제는 2개 이상의 세션이 `공통 자원`에 대해 `read-write` 할 경우 발생한다.

선착순 결제 이벤트 방식이 대표적이다. 이와 같이 공유 데이터의 무결성과 정합성을 보장하기 위해 atomic 을 보장하면서 분산 처리를 해줘야 한다.

동시성 문제의 핵심은 보장된 데이터 품질을 클라이언트에게 제공하느냐에 따라 결정되는데, 데이터 품질은 크게 `무결성`과 `활동성`으로 나뉜다.

- 데이터 무결성
    - 데이터의 정확성과 일관성을 유지하고 보증하는 것
    - 데이터의 손실에 대한 부분을 얼마나 일관성 있게 처리하느냐
- 시스템 활동성
    - 공통 자원에 대한 데이터의 무결성을 지키긴 어렵다.
    - 동시성 문제를 해결하는 방법에 따라 시스템에 대한 활동성이 달라진다.
    - 대표적으로 비관적 락(ex. DB Lock) 같은 경우, 데이터 정확성을 높이기 위해 높은 격리 수준을 유지한다면 활동성을 보장할 수 없다.

이러한 이벤트 처리 방식은 시스템 활동성보단 데이터 무결성이 우선된다.

공통 자원의 데이터 무결성을 보장하기 위해선 비관적 락과 낙관적 락을 통해 해결할 수 있다.

기본적으로 비관적 락은 `Shared Lock` 또는 `Exclusive Lock`을 선점하는 방식이다. 이는 현재 애플리케이션의 RDBMS 에서 지원하는 격리 수준(`isolation level`) 정책에 따라 다르게 동작될 수 있다. 지원하는 데이터베이스 시스템에 강하게 의존될 경향이 있다. DB Lock 같은 경우엔 격리 수준이 높을 수록 일관성은 보장하지만, 처리 성능이 떨어지므로 활동성을 보장할 수 없다. 이 방법은 애플리케이션 시스템과 데이터베이스 시스템간의 강결합이 발생되기 때문에, 다양한 데이터베이스 시스템을 지원하는 환경이라면 적합하지 않다.

데이터 일관성을 보장하면서 활동성까지 보장하기 위해선 DB Lock 보단 애플리케이션에서 제어할 수 있는 Lock 을 기술해야 한다.

JPA 에서 지원하는 낙관적 락(Optimistic Lock)은 @Version 충돌로 손실 데이터가 비번하게 발생할 수 있으며, 자칫 막대한 `ObjectOptimisticLockingFailureException` 에러 로그를 직면하게 된다.

물론 손실 데이터에 대한 문제는 낙관적 락 뿐만 아니라 비관적 락도 트랜잭션 작업 단위에 외부 시스템을 사용할 경우 발생할 수 있다. 비교적 낙관적 락에서 발생할 가능성이 높을 뿐이다. 이는 전체적인 분산된 서버의 데이터 무결성과 데이터 정합성을 보장할 수 없다.

## Redisson

Redisson은 애플리케이션 수준에서 락을 제어하여 데이터 무결성과 시스템 활동성을 보장한다. pub/sub 방식의 분산 락 방식을 제공하기 때문에 `Spin Lock`의 단점인 Redis 서버의 부하를 보안한 방식이다.

Lettuce와 비슷하게 Netty를 사용하여 non-blocking I/O를 사용하며 Redisson의 특이한 점은 직접 레디스의 명령어를 제공하지 않고, Bucket이나 Map같은 자료구조나 Lock 같은 특정한 구현체의 형태로 제공한다.

> 그 외 Lettuce와 Redisson 을 비교한 자세한 문서 자료는 [Redisson docs - Feature Comparison: Redisson vs Lettuce](https://redisson.org/feature-comparison-redisson-vs-lettuce.html) 을 참고하자.

Redisson 은 Lock 선점에 있어 timeout 을 제공한다.

Lettuce 또는 Jedis 는 pub/sub 방식을 지원하지 않는다. 따라서 [Spin Lock](https://ko.wikipedia.org/wiki/%EC%8A%A4%ED%95%80%EB%9D%BD) 방식으로 동시성 제어를 해결해야 했다. 직접 Spin Lock 을 구현하려면 여러 문제를 고려해야 했다. 대표적으로 선점된 Lock 에 대해 `Lock 선점 여부`, `Lock 선점` operation 을 하나의 일관된 작업으로 처리하기 위해 `setnx` 명령어를 사용해야만 했는데, `setnx` 명령어엔 `expire timeout`를 지원하지 않기 때문에 해결하기에 Lock 에 대한 만료 처리에 대한 어려움이 따른다. 이외에도 Lock을 선점하기 위한 교착 문제, 무한 대기, Redis 서버 부하의 문제를 야기한다.

> Redisson 은 pub/sub 방식으로 락이 해제될 때마다 subscribe 하는 클라이언트에게 알림을 주어 Redis에 요청을 보내 락의 획득 가능 여부를 체크하지 않아도 되도록 개선했다.

[Redisson의 분산락 메커니즘](https://redis.io/docs/reference/patterns/distributed-locks/)은 다음과 같다.

- 상호 배제(Mutual exclusion)
    - 하나의 클라이언트만 Lock을 선점하도록 보장한다.
- 교착 상태(Deadlock free)
    - 공유 자원에 대해 Lock을 선점한 클라이언트가 충돌하거나 분할되더라도 항상 Lock 획득을 보장한다.
- 장애 허용 시스템(Fault tolerance)
    - Redis 서버 장애가 발생하지 않는 이상, Lock 을 선점한 애플리케이션 장애가 발생하더라도 다른 클라이언트에선 Lock을 획득/해제 할 수 있다.

## Dependency

```xml

<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.17.7</version>
</dependency>
```

- org.redisson:redisson-spring-boot-starter:3.17.7

## Configuration

```yaml
spring:
  redis:
    host: localhost
    port: 6379
#    connect-timeout: 3000
```

## RedissonAutoConfiguration

```java
import org.redisson.spring.starter;

@Configuration
@ConditionalOnClass({Redisson.class, RedisOperations.class})
@AutoConfigureBefore({RedisAutoConfiguration.class})
@EnableConfigurationProperties({RedissonProperties.class, RedisProperties.class})
public class RedissonAutoConfiguration {

	// ...
	@Bean(destroyMethod = "shutdown")
	@ConditionalOnMissingBean({RedissonClient.class})
	public RedissonClient redisson() throws IOException {
		// ...
		return Redisson.create(config);
	}
}
```

## RedissonClient Java Config

```java
import java.time.Duration;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

	@Bean
	public RedissonClient redissonClient(RedisProperties properties) {
		String host = properties.getHost();
		int port = properties.getPort();
		Duration connectTimeout = properties.getConnectTimeout();

		Config config = new Config();
		config.useSingleServer()
		  .setAddress(String.format("redis://%s:%d", host, port))
		  .setTimeout((int)connectTimeout.toMillis())
		  .setUsername(properties.getUsername())
		  .setPassword(properties.getPassword());
		return Redisson.create(config);
	}
}
```

## Example

자세한
코드는 [GitHub](https://github.com/gmoon92/toy/blob/master/spring-lock/spring-lock-redisson/src/test/java/com/gmoon/springlockredisson/redisson/RedissonClientTest.java)
를 참고하자.

```java
class RedissonClientTest {

	@Autowired
	private RedissonClient redisson;

	@RepeatedTest(5)
	void incrementAndGet() {
		String lockKey = "gmoon-" + UUID.randomUUID().toString();

		RLock lock = redisson.getLock(lockKey);
		String hitCountKey = lockKey + "-hitCount";
		RAtomicLong atomicLong = redisson.getAtomicLong(hitCountKey);

		Executor task = Executors.newFixedThreadPool(3);
		task.execute(() -> getIncrementAndGet(lock, hitCountKey));
		task.execute(() -> getIncrementAndGet(lock, hitCountKey));
		task.execute(() -> getIncrementAndGet(lock, hitCountKey));

		Awaitility.await()
		  .pollDelay(Duration.ofMillis(10))
		  .atMost(Duration.ofMillis(200))
		  .untilAsserted(() -> assertThat(atomicLong.get()).isEqualTo(3));
	}

	private long getIncrementAndGet(RLock lock, String hitCountKey) {
		try {
			// lock 획득
			if (lock.tryLock(10, 10, TimeUnit.SECONDS)) {
				RAtomicLong atomicLong = redisson.getAtomicLong(hitCountKey);
				return atomicLong.incrementAndGet();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			// lock 해제
			if (lock.isLocked()) {
				lock.unlock();
			}
		}

		throw new RuntimeException("lock try timeout...");
	}
}
```

## 주의 사항

- Lock Timeout 지정
    - Lock 설정시 TTL 설정은 필수다.
    - 이미 Lock 을 선점한 스레드에서 Lock 을 해제하기 전까지, 다른 스레드에선 Lock 을 점유할 수 없다.
- tryLock, try-finally
    - 반드시 선점한 락은 비즈니스 로직 후, 해제(unlock)해야 한다.
- JPA 와 `@Transaction` 처리
    - 데이터 동시성은 보장하지만 무결성은 보장할 수 없다.
        - lock 획득/해제를 고수준 계층에서 관리
        - entityManager.flush 시점 이후 lock 해제

> Spring 은 ThreadLocal 변수를 통해 트랜잭션 상태를 관리하기 때문에 다른 스레드에서 시작된 트랜잭션 (ex : @Async, Executors) 은 상위 스레드에 대해 관리되는 트랜잭션에 참여할 수 없으므로 롤백할 수 없다.

## Reference

- [redis docs](https://redis.io/docs/)
    - [redis - clients](https://redis.io/docs/stack/json/clients/)
    - [Distributed Locks with Redis](https://redis.io/docs/reference/patterns/distributed-locks/)
- [Github - Redisson](https://github.com/redisson/redisson)
    - [Redisson - Wiki](https://github.com/redisson/redisson/wiki/Table-of-Content)
    - [Redisson - Distributed locks and synchronizers](https://github.com/redisson/redisson/wiki/8.-distributed-locks-and-synchronizers/#89-spin-lock)
    - [Redisson - Quick start](https://github.com/redisson/redisson#quick-start)
- [레디스와 분산 락(1/2) - 레디스를 활용한 분산 락과 안전하고 빠른 락의 구현](https://hyperconnect.github.io/2019/11/15/redis-distributed-lock-1.html)
- [MySQL을 이용한 분산락으로 여러 서버에 걸친 동시성 관리](https://techblog.woowahan.com/2631/)

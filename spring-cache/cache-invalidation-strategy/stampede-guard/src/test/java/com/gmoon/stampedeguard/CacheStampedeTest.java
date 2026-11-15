package com.gmoon.stampedeguard;

import static com.gmoon.stampedeguard.ConcurrentReaders.*;
import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.BatchStrategies;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.gmoon.cacheinvalidation.test.IntegrationTest;

/**
 * {@code @Cacheable(sync = true)} 가 Redis 에서 실제로 무엇을 막는지, 캐시 writer 수준에서 고정한다.
 * <p>
 * {@code sync = true} 는 {@link Cache#get(Object, Callable)} 호출로, 아니면 {@code get} 후 {@code put} 으로 바뀐다
 * (spring-context {@code CacheAspectSupport}).
 */
@IntegrationTest
@DisplayName("무효화 직후 동시 MISS")
class CacheStampedeTest {

	private static final int CONCURRENT_READERS = 8;
	private static final Duration SLOW_LOAD = Duration.ofMillis(500);
	private static final Duration LOCK_WAIT_OBSERVATION = Duration.ofMillis(300);
	private static final String KEY = "user-1";

	@Autowired RedisConnectionFactory connectionFactory;

	private String cacheName;
	private AtomicInteger loads;

	@BeforeEach
	void setUp() {
		cacheName = "STAMPEDE-" + System.nanoTime();
		loads = new AtomicInteger();
	}

	@AfterEach
	void releaseLeftoverLock() {
		try (RedisConnection connection = connectionFactory.getConnection()) {
			connection.keyCommands().del(lockKey());
		}
	}

	@Nested
	@DisplayName("락 없는 writer 에서는")
	class WhenWriterDoesNotLock {

		@Test
		@DisplayName("적재를 캐시에 맡겨도 MISS 한 요청이 모두 원본을 읽는다")
		void everyReaderLoadsEvenWhenCacheLoads() {
			Cache cache = cacheOver(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory), "app:");

			readTogether(CONCURRENT_READERS, () -> cache.get(KEY, slowLoader()));

			assertThat(loads)
				 .as("sync = true 는 Cache#get(key, loader) 를 부를 뿐이고, 막는 것은 writer 의 락이다")
				 .hasValue(CONCURRENT_READERS);
		}
	}

	@Nested
	@DisplayName("락 writer 에서는")
	class WhenWriterLocks {

		@Test
		@DisplayName("조회 후 적재하면 여전히 모두 원본을 읽는다")
		void everyReaderLoadsWhenGetThenPut() {
			Cache cache = cacheOver(lockingWriter(), "app:");

			readTogether(CONCURRENT_READERS, () -> getThenPut(cache));

			assertThat(loads)
				 .as("락은 get(key, loader) 안에서만 걸린다. sync 가 없으면 조회와 적재 사이가 비어 있다")
				 .hasValue(CONCURRENT_READERS);
		}

		@Test
		@DisplayName("적재를 캐시에 맡기면 한 요청만 원본을 읽는다")
		void onlyOneReaderLoads() {
			Cache cache = cacheOver(lockingWriter(), "app:");

			readTogether(CONCURRENT_READERS, () -> cache.get(KEY, slowLoader()));

			assertThat(loads)
				 .as("락을 기다린 요청은 락이 풀린 뒤 이미 적재된 값을 읽는다")
				 .hasValue(1);
		}

		@Test
		@DisplayName("다른 키의 적재도 같은 락을 기다린다")
		void serializesLoadsOfDifferentKeys() {
			Cache cache = cacheOver(lockingWriter(), "app:");

			assertThat(startsLoadingWhileLockHeld(cache, cache, "user-2"))
				 .as("락 키가 <캐시 이름>~lock 이라, 상관없는 키의 MISS 도 앞선 적재가 끝날 때까지 줄 선다")
				 .isFalse();
		}

		@Test
		@DisplayName("키 접두사가 달라도 캐시 이름이 같으면 락을 공유한다")
		void sharesLockAcrossKeyPrefixes() {
			Cache appA = cacheOver(lockingWriter(), "app-a:");
			Cache appB = cacheOver(lockingWriter(), "app-b:");

			assertThat(startsLoadingWhileLockHeld(appA, appB, KEY))
				 .as("락 키에는 key-prefix 가 붙지 않는다. 같은 Redis 를 쓰는 다른 애플리케이션의 적재까지 막는다")
				 .isFalse();
		}

		@Test
		@DisplayName("기본 설정의 락에는 만료가 없다")
		void defaultLockNeverExpires() {
			Cache cache = cacheOver(RedisCacheWriter.lockingRedisCacheWriter(connectionFactory), "app:");
			CountDownLatch release = new CountDownLatch(1);
			CompletableFuture<Object> holder = holdLock(cache, release);

			Long lockTtlMillis = lockTtlMillis();
			release.countDown();
			holder.join();

			assertThat(lockTtlMillis)
				 .as("보유자가 적재 도중 죽으면 이 캐시의 적재가 영원히 멈춘다. 제품 설정은 락 만료를 직접 건다")
				 .isEqualTo(-1L);
		}

		private RedisCacheWriter lockingWriter() {
			return RedisCacheWriter.lockingRedisCacheWriter(
				 connectionFactory,
				 Duration.ofMillis(50),
				 RedisCacheWriter.TtlFunction.just(Duration.ofSeconds(5)),
				 BatchStrategies.keys()
			);
		}

		private boolean startsLoadingWhileLockHeld(Cache holderCache, Cache waiterCache, String waiterKey) {
			CountDownLatch release = new CountDownLatch(1);
			CompletableFuture<Object> holder = holdLock(holderCache, release);

			AtomicBoolean waiterStarted = new AtomicBoolean();
			CompletableFuture<Object> waiter = CompletableFuture.supplyAsync(() -> waiterCache.get(
				 waiterKey,
				 () -> {
					 waiterStarted.set(true);
					 return "waiter";
				 }
			));
			pause(LOCK_WAIT_OBSERVATION);
			boolean startedWhileLockHeld = waiterStarted.get();

			release.countDown();
			holder.join();
			waiter.join();
			return startedWhileLockHeld;
		}
	}

	private CompletableFuture<Object> holdLock(Cache cache, CountDownLatch release) {
		CompletableFuture<Object> holder = CompletableFuture.supplyAsync(() -> cache.get(
			 KEY,
			 () -> {
				 ConcurrentReaders.await(release);
				 return "holder";
			 }
		));
		await().atMost(Duration.ofSeconds(2)).until(this::lockHeld);
		return holder;
	}

	private Object getThenPut(Cache cache) {
		Cache.ValueWrapper cached = cache.get(KEY);
		if (cached != null) {
			return cached.get();
		}
		Object loaded = load();
		cache.put(KEY, loaded);
		return loaded;
	}

	private Callable<Object> slowLoader() {
		return this::load;
	}

	private Object load() {
		loads.incrementAndGet();
		pause(SLOW_LOAD);
		return "value";
	}

	private Cache cacheOver(RedisCacheWriter writer, String keyPrefix) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
			 .prefixCacheNameWith(keyPrefix)
			 .entryTtl(Duration.ofMinutes(1));
		RedisCacheManager cacheManager = RedisCacheManager.builder(writer)
			 .cacheDefaults(configuration)
			 .initialCacheNames(Set.of(cacheName))
			 .build();
		cacheManager.afterPropertiesSet();
		return cacheManager.getCache(cacheName);
	}

	private boolean lockHeld() {
		try (RedisConnection connection = connectionFactory.getConnection()) {
			return Boolean.TRUE.equals(connection.keyCommands().exists(lockKey()));
		}
	}

	private Long lockTtlMillis() {
		try (RedisConnection connection = connectionFactory.getConnection()) {
			return connection.keyCommands().pTtl(lockKey());
		}
	}

	private byte[] lockKey() {
		return (cacheName + "~lock").getBytes(StandardCharsets.UTF_8);
	}
}

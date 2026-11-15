package com.gmoon.cacheserializer.migration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.gmoon.cacheserializer.jdk.FilteredJdk;
import com.gmoon.cacheserializer.test.CachedOrder;
import com.gmoon.cacheserializer.test.LegacyOrder;
import com.gmoon.cacheserializer.typed.TypedJson;

/**
 * 롤링 배포 중 구 Pod(JDK)와 신 Pod(타입 고정 JSON)가 같은 Redis 를 쓰는 상황을 재현한다.
 *
 * <p>로컬 Redis({@code localhost:6379})가 없으면 건너뛴다.
 */
@DisplayName("JDK 에서 타입 고정 JSON 으로 롤링 이관")
class RollingMigrationTest {

	private static final String KEY_PREFIX = "cache-serializer-strategy:test:";
	private static final String ORDER = "ORDER";
	private static final String ORDER_V2 = "ORDER_V2";
	private static final long ORDER_ID = 1L;

	private static LettuceConnectionFactory connectionFactory;

	private final RedisSerializer<Object> legacy = FilteredJdk.allowing("com.gmoon.cacheserializer.test.LegacyOrder");
	private final RedisSerializer<CachedOrder> current = TypedJson.of(CachedOrder.class);

	private final Cache oldPodOrder = cacheOf(ORDER, legacy);
	private final Cache newPodOrderV2 = cacheOf(ORDER_V2, current);
	private final Cache newPodOrderSameName = cacheOf(ORDER, current);

	@BeforeAll
	static void connect() {
		connectionFactory = new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
		connectionFactory.afterPropertiesSet();
		connectionFactory.start();
		assumeTrue(isRedisReachable(), "local Redis (localhost:6379) is required");
	}

	@AfterAll
	static void disconnect() {
		connectionFactory.destroy();
	}

	@AfterEach
	void clean() {
		oldPodOrder.evict(ORDER_ID);
		newPodOrderV2.evict(ORDER_ID);
	}

	@Nested
	@DisplayName("직렬화기를 바꾸며 캐시 이름도 올리면")
	class WhenCacheNameVersioned {

		@Test
		@DisplayName("신 Pod 는 구 Pod 가 쓴 값을 읽지 않고 미스로 처리한다")
		void missesInsteadOfReadingLegacyBytes() {
			oldPodOrder.put(ORDER_ID, new LegacyOrder(ORDER_ID, "PAID"));

			assertThat(newPodOrderV2.get(ORDER_ID))
				 .as("구·신 Pod 가 서로 다른 키만 읽어 교차 역직렬화가 한 건도 일어나지 않는다")
				 .isNull();
		}
	}

	@Nested
	@DisplayName("캐시 이름은 두고 직렬화기만 바꾸면")
	class WhenOnlySerializerSwapped {

		@Test
		@DisplayName("신 Pod 가 구 Pod 가 쓴 값을 읽다 실패한다")
		void failsOnLegacyBytes() {
			oldPodOrder.put(ORDER_ID, new LegacyOrder(ORDER_ID, "PAID"));

			assertThatThrownBy(() -> newPodOrderSameName.get(ORDER_ID))
				 .as("CacheErrorHandler 가 이 예외를 삼키면 장애가 아니라 조용히 계속되는 미스로 보인다")
				 .isInstanceOf(SerializationException.class);
		}
	}

	@Nested
	@DisplayName("구 Pod 가 변경을 처리하며 캐시를 지우면")
	class WhenOldPodEvicts {

		@Test
		@DisplayName("신 이름의 키는 남아 낡은 값을 돌려준다")
		void leavesVersionedKeyStale() {
			newPodOrderV2.put(ORDER_ID, CachedOrder.sample());

			oldPodOrder.evict(ORDER_ID);

			assertThat(newPodOrderV2.get(ORDER_ID))
				 .as("전환 기간에는 신 캐시 TTL 을 짧게 두고, 신 Pod 의 무효화가 구·신 이름을 함께 지워야 한다")
				 .isNotNull();
		}
	}

	private static Cache cacheOf(String cacheName, RedisSerializer<?> valueSerializer) {
		RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
			 .prefixCacheNameWith(KEY_PREFIX)
			 .entryTtl(Duration.ofMinutes(1))
			 .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer));

		RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
			 .withCacheConfiguration(cacheName, configuration)
			 .disableCreateOnMissingCache()
			 .build();
		cacheManager.afterPropertiesSet();
		return cacheManager.getCache(cacheName);
	}

	private static boolean isRedisReachable() {
		try (RedisConnection connection = connectionFactory.getConnection()) {
			return "PONG".equals(connection.ping());
		} catch (RuntimeException e) {
			return false;
		}
	}
}

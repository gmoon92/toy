package com.gmoon.cacheinvalidation.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@DisplayName("주입된 캐시 관리자 확인")
class CacheManagerValidatorTest {

	@Nested
	@DisplayName("Redis 자동설정이 물러나 다른 관리자가 꽂히면")
	class WhenRedisAutoConfigurationBackedOff {

		@Test
		@DisplayName("기동을 막는다")
		void failsToStart() {
			CacheManagerValidator validator = new CacheManagerValidator(new ConcurrentMapCacheManager());

			assertThatIllegalStateException()
				 .as("그대로 기동하면 TTL 도 값 타입도 미등록 캐시 차단도 없이 캐시가 동작하는 것처럼 보인다")
				 .isThrownBy(validator::afterSingletonsInstantiated)
				 .withMessageContaining("ConcurrentMapCacheManager")
				 .withMessageContaining("RedisConnectionFactory");
		}
	}

	@Nested
	@DisplayName("설정한 Redis 관리자가 주입되면")
	class WhenRedisCacheManagerInjected {

		@Test
		@DisplayName("그대로 기동한다")
		void startsNormally() {
			RedisCacheManager cacheManager = RedisCacheManager.builder(mock(RedisConnectionFactory.class)).build();

			assertThatNoException()
				 .as("정상 배선까지 막으면 쓸 수 없다")
				 .isThrownBy(new CacheManagerValidator(cacheManager)::afterSingletonsInstantiated);
		}
	}
}

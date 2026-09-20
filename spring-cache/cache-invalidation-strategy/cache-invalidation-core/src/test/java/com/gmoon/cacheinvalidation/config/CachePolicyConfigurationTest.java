package com.gmoon.cacheinvalidation.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.gmoon.cacheinvalidation.CachePolicy;
import com.gmoon.cacheinvalidation.TestCachePolicy;
import com.gmoon.cacheinvalidation.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.serializer.SerializerFactory;

@DisplayName("정책을 캐시 설정으로 바꾸기")
class CachePolicyConfigurationTest {

	private static final TtlResolver PASS_THROUGH = declaredTtl -> (key, value) -> declaredTtl;

	@Nested
	@DisplayName("키 접두사를 쓰지 않기로 하면")
	class WhenKeyPrefixDisabled {

		@Test
		@DisplayName("캐시명을 키 앞에 붙이지 않는다")
		void writesKeyWithoutPrefix() {
			CacheProperties properties = new CacheProperties();
			properties.getRedis().setUseKeyPrefix(false);

			assertThat(registeredCacheOf(properties).usePrefix())
				 .as("getKeyPrefixFor 는 접두사를 쓴다면 무엇이 될지만 계산한다. 사용 여부는 usePrefix 가 쥔다")
				 .isFalse();
		}
	}

	@Nested
	@DisplayName("키 접두사를 지정하면")
	class WhenKeyPrefixGiven {

		@Test
		@DisplayName("지정한 값이 키 앞에 붙는다")
		void writesGivenPrefix() {
			CacheProperties properties = new CacheProperties();
			properties.getRedis().setKeyPrefix("svc:");

			assertThat(registeredCacheOf(properties).getKeyPrefixFor(TestCachePolicy.Name.USER))
				 .startsWith("svc:");
		}
	}

	@Nested
	@DisplayName("정책이 없는 캐시의 기본 설정은")
	class WhenCacheHasNoPolicy {

		@Test
		@DisplayName("spring.cache.redis.time-to-live 를 따른다")
		void followsDeclaredTimeToLive() {
			CacheProperties properties = new CacheProperties();
			properties.getRedis().setTimeToLive(Duration.ofMinutes(7));

			assertThat(unregisteredTtlOf(properties)).isEqualTo(Duration.ofMinutes(7));
		}

		@Test
		@DisplayName("설정이 비어 있으면 5분으로 버틴다")
		void fallsBackToFiveMinutes() {
			assertThat(unregisteredTtlOf(new CacheProperties()))
				 .as("무한 보관은 축출 대상이 없어 쓰기 실패로 이어지므로 기본값이 있어야 한다")
				 .isEqualTo(Duration.ofMinutes(5));
		}
	}

	private RedisCacheConfiguration registeredCacheOf(CacheProperties properties) {
		return builderWith(properties).getCacheConfigurationFor(TestCachePolicy.Name.USER)
			 .orElseThrow(() -> new AssertionError("정책이 등록한 캐시 설정이 없다"));
	}

	private Duration unregisteredTtlOf(CacheProperties properties) {
		return builderWith(properties).cacheDefaults().getTtlFunction().getTimeToLive("key", "value");
	}

	private RedisCacheManager.RedisCacheManagerBuilder builderWith(CacheProperties properties) {
		RedisCacheManager.RedisCacheManagerBuilder builder =
			 RedisCacheManager.builder(mock(RedisConnectionFactory.class));

		config().cachePolicyCustomizer(SerializerFactory.json(), PASS_THROUGH, properties)
			 .customize(builder);

		return builder;
	}

	private static AbstractRedisCacheConfig config() {
		return new AbstractRedisCacheConfig() {
			@Override
			protected List<CachePolicy> cachePolicies() {
				return List.of(TestCachePolicy.USER);
			}
		};
	}
}

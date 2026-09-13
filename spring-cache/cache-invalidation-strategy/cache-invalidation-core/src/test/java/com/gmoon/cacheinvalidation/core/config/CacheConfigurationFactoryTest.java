package com.gmoon.cacheinvalidation.core.config;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.CacheProperties;

import com.gmoon.cacheinvalidation.core.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.serialization.JsonSerializerFactory;

@DisplayName("정책을 캐시 설정으로 바꾸기")
class CacheConfigurationFactoryTest {

	private static final TtlResolver PASS_THROUGH = declaredTtl -> (key, value) -> declaredTtl;

	@Nested
	@DisplayName("키 접두사를 쓰지 않기로 하면")
	class WhenKeyPrefixDisabled {

		@Test
		@DisplayName("캐시명을 키 앞에 붙이지 않는다")
		void writesKeyWithoutPrefix() {
			CacheProperties.Redis redis = new CacheProperties().getRedis();
			redis.setUseKeyPrefix(false);

			assertThat(factoryOf(redis).configurationOf(TestCachePolicy.USER).usePrefix())
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
			CacheProperties.Redis redis = new CacheProperties().getRedis();
			redis.setKeyPrefix("svc:");

			assertThat(factoryOf(redis).configurationOf(TestCachePolicy.USER).getKeyPrefixFor("USER"))
				 .startsWith("svc:");
		}
	}

	@Nested
	@DisplayName("정책이 없는 캐시의 기본 설정은")
	class WhenCacheHasNoPolicy {

		@Test
		@DisplayName("spring.cache.redis.time-to-live 를 따른다")
		void followsDeclaredTimeToLive() {
			CacheProperties.Redis redis = new CacheProperties().getRedis();
			redis.setTimeToLive(Duration.ofMinutes(7));

			assertThat(ttlOf(factoryOf(redis)))
				 .isEqualTo(Duration.ofMinutes(7));
		}

		@Test
		@DisplayName("설정이 비어 있으면 5분으로 버틴다")
		void fallsBackToFiveMinutes() {
			assertThat(ttlOf(factoryOf(new CacheProperties().getRedis())))
				 .as("무한 보관은 축출 대상이 없어 쓰기 실패로 이어지므로 기본값이 있어야 한다")
				 .isEqualTo(Duration.ofMinutes(5));
		}
	}

	private Duration ttlOf(CacheConfigurationFactory factory) {
		return factory.unregisteredCacheConfiguration().getTtlFunction().getTimeToLive("key", "value");
	}

	private CacheConfigurationFactory factoryOf(CacheProperties.Redis redis) {
		return new CacheConfigurationFactory(new JsonSerializerFactory(), PASS_THROUGH, redis);
	}
}

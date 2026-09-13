package com.gmoon.cacheinvalidation.core.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.core.cache.expiration.JitteredTtlResolver;
import com.gmoon.cacheinvalidation.core.cache.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.cache.serialization.JsonSerializerFactory;
import com.gmoon.cacheinvalidation.core.cache.serialization.SerializerFactory;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.invalidation.EvictableEntityRule;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRule;

/**
 * 확장점은 모두 {@code public} 메서드이므로 스프링 컨텍스트 없이 직접 호출해 검증한다.
 * 다만 재정의한 전략이 실제 캐시 설정까지 닿는지는 {@code cachePolicyCustomizer} 를 거쳐 확인한다.
 */
@DisplayName("캐시 설정 확장점")
class AbstractRedisCacheConfigTest {

	private static final Duration FIXED_TTL = Duration.ofMinutes(42);
	private static final ServiceCacheProperties PROPERTIES = new ServiceCacheProperties(null, null);

	@Nested
	@DisplayName("전략을 재정의하지 않으면")
	class WhenStrategiesNotOverridden {

		private final AbstractRedisCacheConfig config = new TestCacheConfig();

		@Test
		@DisplayName("코어의 기본 구현이 쓰인다")
		void usesCoreDefaults() {
			assertThat(config.serializerFactory())
				 .as("직렬화 전략의 기본 구현")
				 .isInstanceOf(JsonSerializerFactory.class);
			assertThat(config.ttlResolver(PROPERTIES))
				 .as("만료 전략의 기본 구현")
				 .isInstanceOf(JitteredTtlResolver.class);
		}

		@Test
		@DisplayName("기본 구현은 값을 JSON 으로 쓴다")
		void writesJsonByDefault() {
			assertThat(serializedValueOf(config))
				 .as("재정의 결과와 기본 결과가 같으면 아래 테스트가 아무것도 증명하지 못한다")
				 .isEqualTo("\"hello\"");
		}
	}

	@Nested
	@DisplayName("직렬화 전략을 재정의하면")
	class WhenSerializationOverridden {

		private final AbstractRedisCacheConfig config = new TestCacheConfig() {
			@Override
			public SerializerFactory serializerFactory() {
				return new StringSerializerFactory();
			}
		};

		@Test
		@DisplayName("재정의한 구현이 반환된다")
		void returnsOverriddenStrategy() {
			assertThat(config.serializerFactory())
				 .as("상속으로 연 확장점이 기본 구현을 대체해야 한다")
				 .isInstanceOf(StringSerializerFactory.class);
		}

		@Test
		@DisplayName("캐시 설정이 재정의한 직렬화기로 값을 쓴다")
		void reachesCacheConfiguration() {
			assertThat(serializedValueOf(config))
				 .as("빈만 바뀌고 캐시 설정에 반영되지 않으면 확장점이 동작한 것이 아니다")
				 .isEqualTo("hello");
		}
	}

	@Nested
	@DisplayName("만료 전략을 재정의하면")
	class WhenExpirationOverridden {

		private final AbstractRedisCacheConfig config = new TestCacheConfig() {
			@Override
			public TtlResolver ttlResolver(ServiceCacheProperties properties) {
				return declaredTtl -> (key, value) -> FIXED_TTL;
			}
		};

		@Test
		@DisplayName("재정의한 TTL 이 캐시 설정에 반영된다")
		void reachesCacheConfiguration() {
			assertThat(ttlOf(config))
				 .as("정책이 선언한 TTL 대신 재정의한 만료 전략이 이겨야 한다")
				 .isEqualTo(FIXED_TTL);
		}

		@Test
		@DisplayName("정책이 선언한 TTL 과 다른 값으로 검증한다")
		void differsFromPolicyDeclaredTtl() {
			assertThat(TestCachePolicy.USER.ttl())
				 .as("정책 TTL 과 재정의 TTL 이 같으면 위 테스트가 아무것도 증명하지 못한다")
				 .isNotEqualTo(FIXED_TTL);
		}
	}

	private String serializedValueOf(AbstractRedisCacheConfig config) {
		ByteBuffer written = configurationOf(config).getValueSerializationPair().write("hello");
		return StandardCharsets.UTF_8.decode(written).toString();
	}

	private Duration ttlOf(AbstractRedisCacheConfig config) {
		return configurationOf(config).getTtlFunction().getTimeToLive("key", "value");
	}

	private RedisCacheConfiguration configurationOf(AbstractRedisCacheConfig config) {
		RedisCacheManager.RedisCacheManagerBuilder builder =
			 RedisCacheManager.builder(mock(RedisConnectionFactory.class));

		config.cachePolicyCustomizer(
			 new CachePolicyRegistry(config.cachePolicies()),
			 PROPERTIES,
			 new CacheProperties()
		).customize(builder);

		return builder.getCacheConfigurationFor(TestCachePolicy.Name.USER)
			 .orElseThrow(() -> new AssertionError("정책이 등록한 캐시 설정이 없다"));
	}

	static class TestCacheConfig extends AbstractRedisCacheConfig {

		@Override
		protected List<CachePolicy> cachePolicies() {
			return List.of(TestCachePolicy.USER);
		}

		@Override
		protected List<InvalidationRule> invalidationRules() {
			return List.of(EvictableEntityRule.owning(TestCachePolicy.USER));
		}
	}

	static class StringSerializerFactory implements SerializerFactory {

		@Override
		public RedisSerializer<?> valueSerializerFor(CachePolicy policy) {
			return RedisSerializer.string();
		}

		@Override
		public RedisSerializer<?> unregisteredCacheSerializer() {
			return RedisSerializer.string();
		}
	}
}

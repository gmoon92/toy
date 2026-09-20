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

import com.gmoon.cacheinvalidation.core.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;
import com.gmoon.cacheinvalidation.core.fixture.FailingCacheManager;
import com.gmoon.cacheinvalidation.core.invalidation.CacheEvictable;
import com.gmoon.cacheinvalidation.core.invalidation.CacheInvalidator;
import com.gmoon.cacheinvalidation.core.invalidation.CacheKey;
import com.gmoon.cacheinvalidation.core.invalidation.ChangeSource;
import com.gmoon.cacheinvalidation.core.invalidation.EntityChange;
import com.gmoon.cacheinvalidation.core.invalidation.EvictableEntityRule;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRecorder;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRule;
import com.gmoon.cacheinvalidation.core.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.serialization.SerializerFactory;

/**
 * 확장점은 {@code protected} 이므로 같은 패키지인 이 테스트가 스프링 컨텍스트 없이 직접 호출해 검증한다.
 * 다만 재정의한 전략이 실제 캐시 설정까지 닿는지는 {@code cachePolicyCustomizer} 를 거쳐 확인한다.
 */
@DisplayName("캐시 설정 확장점")
class AbstractRedisCacheConfigTest {

	private static final Duration FIXED_TTL = Duration.ofMinutes(42);
	private static final double DEFAULT_JITTER_RATIO = 0.1;
	private static final ServiceCacheProperties PROPERTIES = new ServiceCacheProperties(null, null);

	@Nested
	@DisplayName("전략을 재정의하지 않으면")
	class WhenStrategiesNotOverridden {

		private final AbstractRedisCacheConfig config = new TestCacheConfig();

		@Test
		@DisplayName("기본 만료 전략이 선언한 TTL 을 기준으로 흔들린 만료를 쓴다")
		void appliesJitteredTtlByDefault() {
			Duration declared = TestCachePolicy.USER.ttl();
			long bound = Math.max(1L, (long)(declared.toSeconds() * DEFAULT_JITTER_RATIO));

			assertThat(ttlOf(config))
				 .as("기본 만료 전략이 캐시 설정까지 닿지 않으면 동시 만료를 막지 못한다")
				 .isBetween(declared.minusSeconds(bound), declared.plusSeconds(bound));
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
			protected SerializerFactory serializerFactory() {
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
			protected TtlResolver ttlResolver(ServiceCacheProperties properties) {
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

	@Nested
	@DisplayName("무효화기를 조립하면")
	class WhenInvalidatorAssembled {

		@Test
		@DisplayName("캐시가 죽어도 예외를 호출자에게 돌려주지 않는다")
		void swallowsInvalidationFailure() {
			CacheInvalidator invalidator = new TestCacheConfig().cacheInvalidator(
				 FailingCacheManager.of("redis down"), InvalidationRecorder.NONE);

			assertThatNoException()
				 .as("리스너가 각자 try/catch 하던 것을 한 곳으로 모았으므로, 배선이 빠지면 보호가 사라진다")
				 .isThrownBy(() -> invalidator.invalidate(
					  EntityChange.deleted(new EvictableUser()), ChangeSource.JPA_ENTITY));
		}
	}

	@Nested
	@DisplayName("무효화 규칙을 선언하지 않으면")
	class WhenNoRuleDeclared {

		@Test
		@DisplayName("소유권을 따지지 않고 기동한다")
		void skipsOwnershipCheck() {
			AbstractRedisCacheConfig config = new AbstractRedisCacheConfig() {
				@Override
				protected List<CachePolicy> cachePolicies() {
					return List.of(TestCachePolicy.USER);
				}
			};

			assertThatNoException()
				 .as("무효화를 쓰지 않는 모듈은 TTL 이 유일한 수단이므로 주인을 물을 대상이 없다")
				 .isThrownBy(config::validateInvalidationOwnership);
		}
	}

	@Nested
	@DisplayName("무효화 규칙을 하나라도 선언하면")
	class WhenRuleDeclared {

		@Test
		@DisplayName("주인 없는 캐시를 기동에서 막는다")
		void rejectsPolicyWithoutOwner() {
			AbstractRedisCacheConfig config = new AbstractRedisCacheConfig() {
				@Override
				protected List<CachePolicy> cachePolicies() {
					return List.of(TestCachePolicy.USER, TestCachePolicy.USER_SUMMARY);
				}

				@Override
				protected List<InvalidationRule> invalidationRules() {
					return List.of(EvictableEntityRule.owning(TestCachePolicy.USER));
				}
			};

			assertThatIllegalStateException()
				 .as("규칙을 쓰기 시작하면 어떤 캐시를 지우고 어떤 캐시를 시간에 맡기는지 밝혀야 한다")
				 .isThrownBy(config::validateInvalidationOwnership)
				 .withMessageContaining(TestCachePolicy.Name.USER_SUMMARY);
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
			 config.serializerFactory(),
			 config.ttlResolver(PROPERTIES),
			 new CacheProperties()
		).customize(builder);

		return builder.getCacheConfigurationFor(TestCachePolicy.Name.USER)
			 .orElseThrow(() -> new AssertionError("정책이 등록한 캐시 설정이 없다"));
	}

	record EvictableUser() implements CacheEvictable {

		@Override
		public List<CacheKey> cacheEntriesToEvict() {
			return List.of(CacheKey.of(TestCachePolicy.USER, 1L));
		}
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

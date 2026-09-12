package com.gmoon.cacheinvalidation.core.config;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicy;
import com.gmoon.cacheinvalidation.core.cache.policy.CachePolicyRegistry;
import com.gmoon.cacheinvalidation.core.cache.expiration.TtlResolver;
import com.gmoon.cacheinvalidation.core.cache.expiration.JitteredTtlResolver;
import com.gmoon.cacheinvalidation.core.cache.serialization.SerializerFactory;
import com.gmoon.cacheinvalidation.core.cache.serialization.JsonSerializerFactory;
import com.gmoon.cacheinvalidation.core.invalidation.EvictableEntityRule;
import com.gmoon.cacheinvalidation.core.invalidation.InvalidationRule;
import com.gmoon.cacheinvalidation.core.fixture.TestCachePolicy;

@DisplayName("캐시 설정 확장점")
class AbstractRedisCacheConfigTest {

	private static final Duration FIXED_TTL = Duration.ofMinutes(42);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		 .withBean(RedisConnectionFactory.class, () -> mock(RedisConnectionFactory.class))
		 .withBean(CacheManager.class, ConcurrentMapCacheManager::new)
		 .withUserConfiguration(BootCachePropertiesConfig.class);

	@Nested
	@DisplayName("전략을 재정의하지 않으면")
	class WhenStrategiesNotOverridden {

		@Test
		@DisplayName("코어의 기본 구현이 쓰인다")
		void usesCoreDefaults() {
			contextRunner.withUserConfiguration(DefaultCacheConfig.class)
				 .run(context -> {
					 assertThat(context.getBean(SerializerFactory.class))
						  .isInstanceOf(JsonSerializerFactory.class);
					 assertThat(context.getBean(TtlResolver.class))
						  .isInstanceOf(JitteredTtlResolver.class);
				 });
		}
	}

	@Nested
	@DisplayName("직렬화 전략을 재정의하면")
	class WhenSerializationOverridden {

		@Test
		@DisplayName("재정의한 구현이 빈으로 등록된다")
		void registersOverriddenBean() {
			contextRunner.withUserConfiguration(OverriddenSerializationConfig.class)
				 .run(context -> assertThat(context.getBean(SerializerFactory.class))
					  .as("상속으로 연 확장점이 실제로 기본 구현을 대체해야 한다")
					  .isInstanceOf(FixedSerialization.class));
		}

		@Test
		@DisplayName("캐시 설정이 재정의한 직렬화기로 값을 쓴다")
		void writesValuesWithOverriddenSerializer() {
			contextRunner.withUserConfiguration(OverriddenSerializationConfig.class)
				 .run(context -> assertThat(serializedValueOf(context))
					  .as("빈만 바뀌고 캐시 설정에 반영되지 않으면 확장점이 동작한 것이 아니다")
					  .isEqualTo("hello"));
		}

		@Test
		@DisplayName("기본 구현은 JSON 으로 쓴다")
		void defaultWritesJson() {
			contextRunner.withUserConfiguration(DefaultCacheConfig.class)
				 .run(context -> assertThat(serializedValueOf(context))
					  .as("재정의 결과와 기본 결과가 같으면 위 테스트가 아무것도 증명하지 못한다")
					  .isEqualTo("\"hello\""));
		}
	}

	@Nested
	@DisplayName("만료 전략을 재정의하면")
	class WhenExpirationOverridden {

		@Test
		@DisplayName("재정의한 TTL 이 캐시 설정에 반영된다")
		void reachesCacheConfiguration() {
			contextRunner.withUserConfiguration(OverriddenExpirationConfig.class)
				 .run(context -> assertThat(ttlOf(context.getBean(RedisCacheSettings.class),
					  context.getBean(CachePolicyRegistry.class)))
					  .as("정책이 선언한 TTL 대신 재정의한 만료 전략이 이겨야 한다")
					  .isEqualTo(FIXED_TTL));
		}

		@Test
		@DisplayName("정책이 선언한 TTL 과 다른 값으로 검증한다")
		void differsFromPolicyDeclaredTtl() {
			assertThat(TestCachePolicy.USER.ttl())
				 .as("정책 TTL 과 재정의 TTL 이 같으면 위 테스트가 아무것도 증명하지 못한다")
				 .isNotEqualTo(FIXED_TTL);
		}
	}

	private String serializedValueOf(ApplicationContext context) {
		ByteBuffer written = configurationOf(context.getBean(RedisCacheSettings.class),
			 context.getBean(CachePolicyRegistry.class))
			 .getValueSerializationPair()
			 .write("hello");
		return StandardCharsets.UTF_8.decode(written).toString();
	}

	private Duration ttlOf(RedisCacheSettings config, CachePolicyRegistry registry) {
		return configurationOf(config, registry).getTtlFunction().getTimeToLive("key", "value");
	}

	private RedisCacheConfiguration configurationOf(
		 RedisCacheSettings config,
		 CachePolicyRegistry registry
	) {
		return config.byCacheName(registry).get(TestCachePolicy.Name.USER);
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(CacheProperties.class)
	static class BootCachePropertiesConfig {
	}

	@Configuration
	static class DefaultCacheConfig extends AbstractRedisCacheConfig {

		@Override
		protected List<CachePolicy> cachePolicies() {
			return List.of(TestCachePolicy.USER);
		}

		@Override
		protected List<InvalidationRule> invalidationRules() {
			return List.of(EvictableEntityRule.owning(TestCachePolicy.USER));
		}
	}

	@Configuration
	static class OverriddenSerializationConfig extends DefaultCacheConfig {

		@Bean
		@Override
		public SerializerFactory serializerFactory() {
			return new FixedSerialization();
		}
	}

	@Configuration
	static class OverriddenExpirationConfig extends DefaultCacheConfig {

		@Bean
		@Override
		public TtlResolver ttlResolver(ServiceCacheProperties properties) {
			return baseTtl -> (key, value) -> FIXED_TTL;
		}
	}

	static class FixedSerialization implements SerializerFactory {

		@Override
		public RedisSerializer<?> valueSerializerFor(CachePolicy policy) {
			return new FixedSerializer();
		}

		@Override
		public RedisSerializer<?> unregisteredCacheSerializer() {
			return new FixedSerializer();
		}
	}

	static class FixedSerializer implements RedisSerializer<Object> {

		@Override
		public byte[] serialize(Object value) {
			return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
		}

		@Override
		public Object deserialize(byte[] bytes) {
			return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
		}
	}
}

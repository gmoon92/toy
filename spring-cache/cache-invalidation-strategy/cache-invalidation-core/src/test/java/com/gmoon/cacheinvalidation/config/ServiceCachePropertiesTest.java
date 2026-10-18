package com.gmoon.cacheinvalidation.config;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

@DisplayName("캐시 전략 설정 바인딩")
class ServiceCachePropertiesTest {

	private static final Map<String, String> COMPLETE = Map.ofEntries(
		 Map.entry("service.cache.expiration.not-found-ttl", "30s"),
		 Map.entry("service.cache.expiration.jitter-ratio", "0.1"),
		 Map.entry("service.cache.clear.scan-batch-size", "1000")
	);

	@Nested
	@DisplayName("모든 값을 적으면")
	class WhenEverythingDeclared {

		@Test
		@DisplayName("적은 값 그대로 선다")
		void bindsAsDeclared() {
			ServiceCacheProperties properties = bind(COMPLETE);

			assertThat(properties.expiration().notFoundTtl()).isEqualTo(Duration.ofSeconds(30));
			assertThat(properties.expiration().jitterRatio()).isEqualTo(0.1);
			assertThat(properties.clear().scanBatchSize()).isEqualTo(1_000);
		}
	}

	@Nested
	@DisplayName("흔들림 비율을 0 으로 적으면")
	class WhenJitterRatioIsZero {

		@Test
		@DisplayName("0 이 그대로 남는다")
		void keepsZero() {
			ServiceCacheProperties properties = bind(with("service.cache.expiration.jitter-ratio", "0"));

			assertThat(properties.expiration().jitterRatio())
				 .as("0 을 다른 값으로 바꾸면 흔들림을 끌 방법이 없어 장애 재현이 막힌다")
				 .isZero();
		}
	}

	@Nested
	@DisplayName("설정을 아예 적지 않으면")
	class WhenNothingDeclared {

		@Test
		@DisplayName("기동을 막는다")
		void rejectsAtStartup() {
			assertThatThrownBy(() -> bind(Map.of()))
				 .as("기본값으로 메우면 구현 모듈이 정하지 않은 만료 정책으로 모르는 사이에 돈다")
				 .rootCause()
				 .hasMessageContaining("service.cache.expiration must be declared");
		}
	}

	@Nested
	@DisplayName("블록 하나가 빠지면")
	class WhenBlockMissing {

		@Test
		@DisplayName("빠진 블록을 알려 주며 기동을 막는다")
		void rejectsNamingMissingBlock() {
			assertThatThrownBy(() -> bind(without("service.cache.clear.scan-batch-size")))
				 .as("어느 블록이 빠졌는지 모르면 설정을 고칠 자리를 찾지 못한다")
				 .rootCause()
				 .hasMessageContaining("service.cache.clear must be declared");
		}
	}

	@Nested
	@DisplayName("블록 안의 값 하나가 빠지면")
	class WhenValueMissing {

		@Test
		@DisplayName("빠진 키를 알려 주며 기동을 막는다")
		void rejectsNamingMissingKey() {
			assertThatThrownBy(() -> bind(without("service.cache.expiration.jitter-ratio")))
				 .as("원시 타입이면 빠진 값이 0 으로 바인딩되어 모르는 사이에 흔들림이 꺼진다")
				 .rootCause()
				 .hasMessageContaining("service.cache.expiration.jitter-ratio must be declared");
		}
	}

	@Nested
	@DisplayName("비우기 배치 크기가 0 이하면")
	class WhenScanBatchSizeNotPositive {

		@Test
		@DisplayName("기동을 막는다")
		void rejectsAtStartup() {
			assertThatThrownBy(() -> bind(with("service.cache.clear.scan-batch-size", "0")))
				 .as("조용히 다른 값으로 바꾸면 잘못 적은 설정을 끝까지 모른다")
				 .rootCause()
				 .hasMessageContaining("scan batch size must be positive");
		}
	}

	private static Map<String, String> with(String key, String value) {
		Map<String, String> source = new HashMap<>(COMPLETE);
		source.put(key, value);
		return source;
	}

	private static Map<String, String> without(String key) {
		Map<String, String> source = new HashMap<>(COMPLETE);
		source.remove(key);
		return source;
	}

	private static ServiceCacheProperties bind(Map<String, String> source) {
		return new Binder(new MapConfigurationPropertySource(source))
			 .bindOrCreate("service.cache", ServiceCacheProperties.class);
	}
}

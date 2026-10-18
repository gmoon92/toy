package com.gmoon.cacheinvalidation.config;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

@DisplayName("캐시 전략 설정 바인딩")
class ServiceCachePropertiesTest {

	@Nested
	@DisplayName("흔들림 비율을 0 으로 적으면")
	class WhenJitterRatioIsZero {

		@Test
		@DisplayName("0 이 그대로 남는다")
		void keepsZero() {
			ServiceCacheProperties properties = bind(Map.of("service.cache.expiration.jitter-ratio", "0"));

			assertThat(properties.expiration().jitterRatio())
				 .as("0 을 기본값으로 되돌리면 흔들림을 끌 방법이 없어 장애 재현이 막힌다")
				 .isZero();
		}
	}

	@Nested
	@DisplayName("설정을 아예 적지 않으면")
	class WhenNothingDeclared {

		@Test
		@DisplayName("중첩 블록이 없어도 기본값으로 선다")
		void fallsBackToDefaults() {
			ServiceCacheProperties properties = bind(Map.of());

			assertThat(properties.expiration().notFoundTtl()).isEqualTo(Duration.ofSeconds(30));
			assertThat(properties.expiration().jitterRatio())
				 .as("블록 전체가 없을 때는 바깥 인자의 기본값이 없으면 null 로 바인딩된다")
				 .isEqualTo(0.1);
			assertThat(properties.clear().scanBatchSize()).isEqualTo(1_000);
		}
	}

	@Nested
	@DisplayName("블록 하나만 적으면")
	class WhenOnlyOneValueDeclared {

		@Test
		@DisplayName("같은 블록의 나머지도 기본값으로 선다")
		void fillsSiblingsWithDefaults() {
			ServiceCacheProperties properties =
				 bind(Map.of("service.cache.expiration.not-found-ttl", "5s"));

			assertThat(properties.expiration().notFoundTtl()).isEqualTo(Duration.ofSeconds(5));
			assertThat(properties.expiration().jitterRatio())
				 .as("적지 않은 값이 0 으로 바인딩되면 모르는 사이에 흔들림이 꺼진다")
				 .isEqualTo(0.1);
		}
	}

	@Nested
	@DisplayName("비우기 배치 크기가 0 이하면")
	class WhenScanBatchSizeNotPositive {

		@Test
		@DisplayName("기동을 막는다")
		void rejectsAtStartup() {
			assertThatThrownBy(() -> bind(Map.of("service.cache.clear.scan-batch-size", "0")))
				 .as("조용히 기본값으로 바꾸면 잘못 적은 설정을 끝까지 모른다")
				 .rootCause()
				 .hasMessageContaining("scan batch size must be positive");
		}
	}

	private static ServiceCacheProperties bind(Map<String, String> source) {
		return new Binder(new MapConfigurationPropertySource(source))
			 .bind("service.cache", ServiceCacheProperties.class)
			 .orElseGet(ServiceCacheProperties::defaults);
	}
}

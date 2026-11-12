package com.gmoon.aftercommitinvalidate.config;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

@DisplayName("캐시 설정 바인딩")
class ServiceCachePropertiesTest {

	@Nested
	@DisplayName("값을 적으면")
	class WhenDeclared {

		@Test
		@DisplayName("적은 값 그대로 선다")
		void bindsAsDeclared() {
			ServiceCacheProperties properties = bind(Map.of("service.cache.clear.scan-batch-size", "1000"));

			assertThat(properties.clear().scanBatchSize()).isEqualTo(1_000);
		}
	}

	@Nested
	@DisplayName("설정을 아예 적지 않으면")
	class WhenNothingDeclared {

		@Test
		@DisplayName("빠진 블록을 알려 주며 기동을 막는다")
		void rejectsNamingMissingBlock() {
			assertThatThrownBy(() -> bind(Map.of()))
				 .as("기본값으로 메우면 정하지 않은 배치 크기로 모르는 사이에 돈다")
				 .rootCause()
				 .hasMessageContaining("service.cache.clear must be declared");
		}
	}

	@Nested
	@DisplayName("비우기 배치 크기가 0 이하면")
	class WhenScanBatchSizeNotPositive {

		@Test
		@DisplayName("기동을 막는다")
		void rejectsAtStartup() {
			assertThatThrownBy(() -> bind(Map.of("service.cache.clear.scan-batch-size", "0")))
				 .as("조용히 다른 값으로 바꾸면 잘못 적은 설정을 끝까지 모른다")
				 .rootCause()
				 .hasMessageContaining("scan batch size must be positive");
		}
	}

	private static ServiceCacheProperties bind(Map<String, String> source) {
		return new Binder(new MapConfigurationPropertySource(source))
			 .bindOrCreate("service.cache", ServiceCacheProperties.class);
	}
}

package com.gmoon.outboxinvalidate.config;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

@DisplayName("캐시 설정 바인딩")
class ServiceCachePropertiesTest {

	private static final Map<String, String> DECLARED = Map.of(
		 "service.cache.clear.scan-batch-size", "1000",
		 "service.cache.outbox.queue", "evict",
		 "service.cache.outbox.relay-interval", "1s",
		 "service.cache.outbox.relay-batch-size", "100"
	);

	@Nested
	@DisplayName("값을 모두 적으면")
	class WhenDeclared {

		@Test
		@DisplayName("적은 값 그대로 선다")
		void bindsAsDeclared() {
			ServiceCacheProperties.Outbox outbox = bind(DECLARED).outbox();

			assertThat(outbox).isEqualTo(new ServiceCacheProperties.Outbox("evict", Duration.ofSeconds(1), 100));
		}
	}

	@Nested
	@DisplayName("outbox 블록을 적지 않으면")
	class WhenOutboxMissing {

		@Test
		@DisplayName("빠진 블록을 알려 주며 기동을 막는다")
		void rejectsNamingMissingBlock() {
			Map<String, String> source = Map.of("service.cache.clear.scan-batch-size", "1000");

			assertThatThrownBy(() -> bind(source))
				 .as("기본값으로 메우면 정하지 않은 주기와 큐로 모르는 사이에 돈다")
				 .rootCause()
				 .hasMessageContaining("service.cache.outbox must be declared");
		}
	}

	@Nested
	@DisplayName("릴레이 간격이 0 이면")
	class WhenRelayIntervalZero {

		@Test
		@DisplayName("기동을 막는다")
		void rejectsAtStartup() {
			assertThatThrownBy(() -> bind(with("service.cache.outbox.relay-interval", "0s")))
				 .as("간격 없이 돌면 대기 행이 없어도 DB 를 쉬지 않고 읽는다")
				 .rootCause()
				 .hasMessageContaining("relay interval must be positive");
		}
	}

	@Nested
	@DisplayName("릴레이 배치 크기가 0 이하면")
	class WhenRelayBatchSizeNotPositive {

		@Test
		@DisplayName("기동을 막는다")
		void rejectsAtStartup() {
			assertThatThrownBy(() -> bind(with("service.cache.outbox.relay-batch-size", "0")))
				 .as("한 행도 읽지 않는 릴레이는 지시를 영원히 쌓아 둔다")
				 .rootCause()
				 .hasMessageContaining("relay batch size must be positive");
		}
	}

	private static Map<String, String> with(String key, String value) {
		Map<String, String> source = new HashMap<>(DECLARED);
		source.put(key, value);
		return source;
	}

	private static ServiceCacheProperties bind(Map<String, String> source) {
		return new Binder(new MapConfigurationPropertySource(source))
			 .bindOrCreate("service.cache", ServiceCacheProperties.class);
	}
}

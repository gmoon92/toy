package com.gmoon.cacheinvalidation;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@DisplayName("무효화 실패 계측")
class InvalidationMetricsTest {

	private static final String FAILURE_COUNT = "cache.invalidation.failures";

	@Nested
	@DisplayName("레지스트리가 있으면")
	class WhenRegistryPresent {

		private final MeterRegistry registry = new SimpleMeterRegistry();
		private final InvalidationMetrics metrics = new InvalidationMetrics(registry);

		@Test
		@DisplayName("삭제 실패를 캐시 이름과 함께 센다")
		void countsEvictionFailureByCacheName() {
			metrics.evictionFailed(TestCachePolicy.Name.USER);
			metrics.evictionFailed(TestCachePolicy.Name.USER);

			assertThat(countOf("evict", TestCachePolicy.Name.USER))
				 .as("실패를 세지 않으면 TTL 이 만료될 때까지 틀린 값을 주면서도 신호가 남지 않는다")
				 .isEqualTo(2);
		}

		@Test
		@DisplayName("규칙 실패를 삭제 실패와 구분해 센다")
		void separatesResolutionFailureFromEviction() {
			metrics.resolutionFailed("BrokenRule");

			assertThat(countOf("resolve", "BrokenRule"))
				 .as("규칙이 깨진 것과 레디스가 죽은 것은 대응이 다르다")
				 .isEqualTo(1);
			assertThat(registry.find(FAILURE_COUNT).tag("phase", "evict").counter())
				 .as("한 지표에 섞여 들어가면 어느 쪽이 터졌는지 못 읽는다")
				 .isNull();
		}

		private double countOf(String phase, String target) {
			return registry.find(FAILURE_COUNT)
				 .tag("phase", phase)
				 .tag("target", target)
				 .counter()
				 .count();
		}
	}

	@Nested
	@DisplayName("레지스트리가 없으면")
	class WhenRegistryAbsent {

		@Test
		@DisplayName("세지 않고 그냥 넘어간다")
		void doesNothing() {
			InvalidationMetrics metrics = new InvalidationMetrics(null);

			assertThatNoException()
				 .as("코어가 관측 스택을 강제하면 actuator 없는 애플리케이션이 기동하지 못한다")
				 .isThrownBy(() -> metrics.evictionFailed(TestCachePolicy.Name.USER));
		}
	}
}

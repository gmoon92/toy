package com.gmoon.cacheinvalidation;

import org.springframework.lang.Nullable;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * 무효화가 실패한 횟수를 센다.
 * <p>
 * 무효화 실패는 호출자에게 전파하지 않으므로, 세지 않으면 해당 TTL 이 만료될 때까지
 * 틀린 값을 돌려주면서도 아무 신호가 남지 않는다. 로그는 집계해야 보이고 보통 집계되지 않는다.
 * <p>
 * {@link MeterRegistry} 빈이 없는 애플리케이션에서는 세지 않는다.
 * 코어가 관측 스택을 강제하지 않기 위해서다.
 */
final class InvalidationMetrics {

	private static final String FAILURE_COUNT = "cache.invalidation.failures";

	@Nullable
	private final MeterRegistry registry;

	InvalidationMetrics(@Nullable MeterRegistry registry) {
		this.registry = registry;
	}

	void evictionFailed(String cacheName) {
		count("evict", cacheName);
	}

	void resolutionFailed(String ruleName) {
		count("resolve", ruleName);
	}

	private void count(String phase, String target) {
		if (registry == null) {
			return;
		}
		registry.counter(FAILURE_COUNT, "phase", phase, "target", target).increment();
	}
}

package com.gmoon.cacheinvalidation.core.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Boot 표준 {@code spring.cache.redis.*} 로 표현할 수 없는 값만 정의한다.
 * 키 접두사는 {@code spring.cache.redis.key-prefix},
 * 기본 TTL 은 {@code spring.cache.redis.time-to-live},
 * 통계는 {@code spring.cache.redis.enable-statistics} 를 사용한다.
 */
@ConfigurationProperties(prefix = "cache-invalidation")
public record CacheInvalidationProperties(
	 Duration notFoundTtl,
	 double ttlJitterRatio,
	 int clearScanBatchSize
) {

	private static final Duration DEFAULT_NOT_FOUND_TTL = Duration.ofSeconds(30);
	private static final double DEFAULT_JITTER_RATIO = 0.1;
	private static final int DEFAULT_CLEAR_SCAN_BATCH_SIZE = 1_000;

	public CacheInvalidationProperties {
		notFoundTtl = notFoundTtl == null ? DEFAULT_NOT_FOUND_TTL : notFoundTtl;
		ttlJitterRatio = ttlJitterRatio <= 0 ? DEFAULT_JITTER_RATIO : ttlJitterRatio;
		clearScanBatchSize = clearScanBatchSize <= 0 ? DEFAULT_CLEAR_SCAN_BATCH_SIZE : clearScanBatchSize;
	}

	public static CacheInvalidationProperties withDefaults() {
		return new CacheInvalidationProperties(null, 0, 0);
	}
}

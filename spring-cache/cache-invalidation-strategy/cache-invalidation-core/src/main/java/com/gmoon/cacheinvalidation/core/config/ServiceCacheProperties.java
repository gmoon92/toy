package com.gmoon.cacheinvalidation.core.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 서비스의 캐시 전략 설정을 한 트리에 모은다.
 * <p>
 * 무효화·만료뿐 아니라 읽기 전략처럼 뒤에 추가될 축도 이 아래에 중첩한다.
 * 전략마다 최상위 프로퍼티를 따로 만들면 설정이 흩어진다.
 * <p>
 * Boot 표준 {@code spring.cache.redis.*} 로 표현 가능한 값은 여기에 두지 않는다.
 * 키 접두사는 {@code key-prefix}, 기본 TTL 은 {@code time-to-live},
 * 통계는 {@code enable-statistics} 를 그대로 쓴다.
 *
 * <pre>
 * service:
 *   cache:
 *     expiration:
 *       not-found-ttl: 30s
 *       jitter-ratio: 0.1
 *     invalidation:
 *       clear-scan-batch-size: 1000
 * </pre>
 */
@ConfigurationProperties(prefix = "service.cache")
public record ServiceCacheProperties(Expiration expiration, Invalidation invalidation) {

	public ServiceCacheProperties {
		expiration = expiration == null ? new Expiration(null, 0) : expiration;
		invalidation = invalidation == null ? new Invalidation(0) : invalidation;
	}


	/**
	 * 값이 만료되는 방식.
	 */
	public record Expiration(Duration notFoundTtl, double jitterRatio) {

		private static final Duration DEFAULT_NOT_FOUND_TTL = Duration.ofSeconds(30);
		private static final double DEFAULT_JITTER_RATIO = 0.1;

		public Expiration {
			notFoundTtl = notFoundTtl == null ? DEFAULT_NOT_FOUND_TTL : notFoundTtl;
			jitterRatio = jitterRatio <= 0 ? DEFAULT_JITTER_RATIO : jitterRatio;
		}

	}

	/**
	 * 무효화가 캐시를 다루는 방식.
	 */
	public record Invalidation(int clearScanBatchSize) {

		private static final int DEFAULT_CLEAR_SCAN_BATCH_SIZE = 1_000;

		public Invalidation {
			clearScanBatchSize = clearScanBatchSize <= 0 ? DEFAULT_CLEAR_SCAN_BATCH_SIZE : clearScanBatchSize;
		}

	}
}

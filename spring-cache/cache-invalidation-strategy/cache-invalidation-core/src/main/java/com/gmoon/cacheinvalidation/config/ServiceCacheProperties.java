package com.gmoon.cacheinvalidation.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * 서비스의 캐시 전략 설정을 한 트리에 모은다.
 * <p>
 * 무효화·만료뿐 아니라 읽기 전략처럼 뒤에 추가될 축도 이 아래에 중첩한다.
 * 전략마다 최상위 프로퍼티를 따로 만들면 설정이 흩어진다.
 * <p>
 * Boot 표준 {@code spring.cache.redis.*} 로 표현 가능한 값은 여기에 두지 않는다.
 * 키 접두사는 {@code key-prefix}, 기본 TTL 은 {@code time-to-live}, 통계는 {@code enable-statistics} 를 그대로 쓴다.
 * <p>
 * 모든 값은 필수다. 기본값을 두지 않고 구현 모듈이 반드시 정한다. 빠지면 기동을 막는다.
 *
 * <pre>
 * service:
 *   cache:
 *     expiration:
 *       not-found-ttl: 30s
 *       jitter-ratio: 0.1   # 0 이면 흔들지 않는다
 *     clear:
 *       scan-batch-size: 1000
 * </pre>
 */
@ConfigurationProperties(prefix = "service.cache")
public record ServiceCacheProperties(Expiration expiration, Clear clear) {

	public ServiceCacheProperties {
		Assert.notNull(expiration, "service.cache.expiration must be declared");
		Assert.notNull(clear, "service.cache.clear must be declared");
	}

	/**
	 * 값이 만료되는 방식.
	 */
	public record Expiration(Duration notFoundTtl, Double jitterRatio) {

		public Expiration {
			Assert.notNull(notFoundTtl, "service.cache.expiration.not-found-ttl must be declared");
			Assert.notNull(jitterRatio, "service.cache.expiration.jitter-ratio must be declared");
		}
	}

	/**
	 * 캐시 하나를 통째로 비울 때({@code Cache.clear()})의 방식.
	 */
	public record Clear(Integer scanBatchSize) {

		public Clear {
			Assert.notNull(scanBatchSize, "service.cache.clear.scan-batch-size must be declared");
			Assert.isTrue(scanBatchSize > 0, "scan batch size must be positive but was " + scanBatchSize);
		}
	}
}

package com.gmoon.stampedeguard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * 이 모듈이 쓰는 캐시 설정만 바인딩한다.
 * <p>
 * Boot 표준 {@code spring.cache.redis.*} 로 표현 가능한 값은 여기에 두지 않는다.
 * 키 접두사는 {@code key-prefix}, 통계는 {@code enable-statistics} 를 그대로 쓴다.
 * TTL 은 {@link CachePolicy} 가 정한다.
 * <p>
 * 모든 값은 필수다. 기본값을 두지 않는다. 빠지면 기동을 막는다.
 *
 * <pre>
 * service:
 *   cache:
 *     clear:
 *       scan-batch-size: 1000
 * </pre>
 */
@ConfigurationProperties(prefix = "service.cache")
public record ServiceCacheProperties(Clear clear) {

	public ServiceCacheProperties {
		Assert.notNull(clear, "service.cache.clear must be declared");
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

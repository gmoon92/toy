package com.gmoon.outboxinvalidate.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * 이 모듈이 쓰는 캐시 설정만 바인딩한다.
 * <p>
 * Boot 표준 {@code spring.cache.redis.*} 로 표현 가능한 값은 여기에 두지 않는다.
 * 모든 값은 필수다. 기본값을 두지 않는다. 빠지면 기동을 막는다.
 *
 * <pre>
 * service:
 *   cache:
 *     clear:
 *       scan-batch-size: 1000
 *     outbox:
 *       queue: cache-outbox-invalidate.evict
 *       relay-interval: 1s
 *       relay-batch-size: 100
 * </pre>
 */
@ConfigurationProperties(prefix = "service.cache")
public record ServiceCacheProperties(Clear clear, Outbox outbox) {

	public ServiceCacheProperties {
		Assert.notNull(clear, "service.cache.clear must be declared");
		Assert.notNull(outbox, "service.cache.outbox must be declared");
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

	/**
	 * 대기 중인 무효화 지시를 브로커로 옮기는 방식.
	 *
	 * @param queue          무효화 지시를 받을 큐
	 * @param relayInterval  릴레이가 대기 행을 읽는 간격. 커밋부터 캐시 삭제까지 걸리는 시간의 대부분이다
	 * @param relayBatchSize 한 번에 읽는 대기 행 수
	 */
	public record Outbox(String queue, Duration relayInterval, Integer relayBatchSize) {

		public Outbox {
			Assert.hasText(queue, "service.cache.outbox.queue must be declared");
			Assert.notNull(relayInterval, "service.cache.outbox.relay-interval must be declared");
			Assert.isTrue(relayInterval.isPositive(), "relay interval must be positive but was " + relayInterval);
			Assert.notNull(relayBatchSize, "service.cache.outbox.relay-batch-size must be declared");
			Assert.isTrue(relayBatchSize > 0, "relay batch size must be positive but was " + relayBatchSize);
		}
	}
}

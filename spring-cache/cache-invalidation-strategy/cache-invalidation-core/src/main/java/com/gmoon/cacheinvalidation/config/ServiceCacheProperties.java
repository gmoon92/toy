package com.gmoon.cacheinvalidation.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.Assert;

/**
 * 서비스의 캐시 전략 설정을 한 트리에 모은다.
 * <p>
 * 무효화·만료뿐 아니라 읽기 전략처럼 뒤에 추가될 축도 이 아래에 중첩한다.
 * 전략마다 최상위 프로퍼티를 따로 만들면 설정이 흩어진다.
 * <p>
 * Boot 표준 {@code spring.cache.redis.*} 로 표현 가능한 값은 여기에 두지 않는다.
 * 키 접두사는 {@code key-prefix}, 통계는 {@code enable-statistics} 를 그대로 쓴다.
 * <p>
 * 기본값은 {@link DefaultValue} 로 바인딩 시점에 넣는다.
 * 생성자에서 0 을 기본값으로 바꾸면 "끄기" 를 표현할 수 없다.
 * 중첩 블록 자체가 없을 때를 위해 바깥 인자에도 붙인다.
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
public record ServiceCacheProperties(
	 @DefaultValue Expiration expiration,
	 @DefaultValue Clear clear
) {

	public static ServiceCacheProperties defaults() {
		return new ServiceCacheProperties(
			 Expiration.DEFAULT,
			 Clear.DEFAULT
		);
	}

	/**
	 * 값이 만료되는 방식.
	 */
	public record Expiration(
		 @DefaultValue("30s") Duration notFoundTtl,
		 @DefaultValue("0.1") double jitterRatio
	) {

		static final Expiration DEFAULT = new Expiration(
			 Duration.ofSeconds(30),
			 0.1
		);

		public Expiration {
			Assert.notNull(notFoundTtl, "not found ttl must not be null");
		}
	}

	/**
	 * 캐시 하나를 통째로 비울 때({@code Cache.clear()})의 방식.
	 */
	public record Clear(@DefaultValue("1000") int scanBatchSize) {

		static final Clear DEFAULT = new Clear(1_000);

		public Clear {
			Assert.isTrue(scanBatchSize > 0, "scan batch size must be positive but was " + scanBatchSize);
		}
	}
}

package com.gmoon.springconfigserverclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Config Server에서 관리하는 애플리케이션 설정
 * {@code @RefreshScope}를 통해 /actuator/refresh 호출 시 설정이 갱신됩니다.
 * {@code @RefreshScope} 는 CGlib 방식으로 프록시 객체를 생성하기 때문에, java record 사용 불가.
 */
@RefreshScope
@ConfigurationProperties(prefix = "app")
@RequiredArgsConstructor
@Getter
@ToString
public class AppProperties {
	private final String message;
	private final String version;
	private final Feature feature;

	@RequiredArgsConstructor
	@Getter
	@ToString
	public static class Feature {
		private final boolean enabled;
		private final int maxRetry;
		private final int timeoutSecond;
	}
}

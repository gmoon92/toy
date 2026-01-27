package com.gmoon.springcloudbus.springcloudbusclient.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ConfigurationProperties(prefix = "app")
@RequiredArgsConstructor
@Getter
@ToString
public class AppProperties {
	private final String message;
	private final String version;
	private final Feature feature;
	private final int refreshCount;

	@RequiredArgsConstructor
	@Getter
	@ToString
	public static class Feature {
		private final boolean enabled;
		private final int maxRetry;
		private final Duration timeout;
	}
}

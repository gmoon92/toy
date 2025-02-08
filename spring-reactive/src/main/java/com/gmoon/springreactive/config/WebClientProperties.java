package com.gmoon.springreactive.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import lombok.Getter;
import lombok.ToString;

@ConfigurationProperties(prefix = "service.webclient")
@Getter
@ToString
public class WebClientProperties {

	private final DataSize maxInMemorySize;

	private final Duration connectTimeout;
	private final Duration responseTimeout;

	private final Duration readTimeout;
	private final Duration writeTimeout;

	private final Duration maxIdleTime;
	private final Duration maxLifeTime;

	public WebClientProperties(
		 @DataSizeUnit(DataUnit.BYTES) DataSize maxInMemorySize,
		 @DefaultValue("30s") Duration connectTimeout,
		 @DefaultValue("5s") Duration responseTimeout,
		 @DefaultValue("30s") Duration readTimeout,
		 @DefaultValue("30s") Duration writeTimeout,
		 @DefaultValue("18s") Duration maxIdleTime,
		 @DefaultValue("18s") Duration maxLifeTime
	) {
		this.maxInMemorySize = maxInMemorySize;

		this.connectTimeout = connectTimeout;
		this.responseTimeout = responseTimeout;

		this.readTimeout = readTimeout;
		this.writeTimeout = writeTimeout;

		this.maxIdleTime = maxIdleTime;
		this.maxLifeTime = maxLifeTime;
	}
}

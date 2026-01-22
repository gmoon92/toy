package com.gmoon.springconfigserverclient.config;

import java.io.Serializable;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "database")
public record DatabaseProperties(Pool pool) implements Serializable {
	public record Pool(
		 int minSize,
		 int maxSize,
		 int connectionTimeout
	) {
	}
}

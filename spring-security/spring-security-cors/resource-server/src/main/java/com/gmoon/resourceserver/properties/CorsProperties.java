package com.gmoon.resourceserver.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;

@Getter
@ConstructorBinding
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {
	private final boolean enabled;

	protected CorsProperties(Boolean enabled) {
		this.enabled = Boolean.TRUE.equals(enabled);
	}
}

package com.gmoon.resourceserver.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "cors")
@Getter
public class CorsProperties {
	private final boolean enabled;

	@ConstructorBinding
	public CorsProperties(boolean enabled) {
		this.enabled = enabled;
	}

	protected CorsProperties(Boolean enabled) {
		this.enabled = Boolean.TRUE.equals(enabled);
	}
}

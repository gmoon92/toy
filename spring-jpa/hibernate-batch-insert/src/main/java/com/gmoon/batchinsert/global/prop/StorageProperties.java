package com.gmoon.batchinsert.global.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "service.storage")
@Getter
public class StorageProperties {

	private final String absolutePath;
	private final String path;

	@ConstructorBinding
	public StorageProperties(String absolutePath, String path) {
		this.absolutePath = absolutePath;
		this.path = path;
	}
}

package com.gmoon.batchinsert.global.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConstructorBinding
@ConfigurationProperties(prefix = "service.storage")
@RequiredArgsConstructor
@Getter
public class StorageProperties {

	private final String absolutePath;
	private final String path;
}

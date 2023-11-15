package com.gmoon.batchinsert.global.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConstructorBinding
@ConfigurationProperties(prefix = "jooq.properties")
@RequiredArgsConstructor
@Getter
public class JooqProperties {

	private final String schema;
	private final String driverClassName;
	private final String targetDir;
}

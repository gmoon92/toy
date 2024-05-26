package com.gmoon.batchinsert.global.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@ConfigurationProperties(prefix = "jooq.properties")
@Getter
public class JooqProperties {

	private final String schema;
	private final String driverClassName;
	private final String targetDir;

	@ConstructorBinding
	public JooqProperties(String schema, String driverClassName, String targetDir) {
		this.schema = schema;
		this.driverClassName = driverClassName;
		this.targetDir = targetDir;
	}
}

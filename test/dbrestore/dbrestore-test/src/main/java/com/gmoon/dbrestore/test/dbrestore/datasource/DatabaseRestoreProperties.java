package com.gmoon.dbrestore.test.dbrestore.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "service.dbrestore")
//@ConstructorBinding
@RequiredArgsConstructor
@Getter
public class DatabaseRestoreProperties {

	private final String schema;
	private final String backupSchema;
}

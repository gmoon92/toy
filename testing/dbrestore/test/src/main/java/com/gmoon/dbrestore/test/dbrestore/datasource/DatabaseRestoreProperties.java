package com.gmoon.dbrestore.test.dbrestore.datasource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.dbrestore")
//@ConstructorBinding
@RequiredArgsConstructor
@Getter
@ToString
public class DatabaseRestoreProperties {

	private final String schema;
	private final String backupSchema;
}

package com.gmoon.dbrecovery.global.recovery.properties;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.dbrecovery")
//@ConstructorBinding
@RequiredArgsConstructor
@ToString
public class RecoveryDatabaseProperties {

	public final String schema;
	public final String recoverySchema;
}

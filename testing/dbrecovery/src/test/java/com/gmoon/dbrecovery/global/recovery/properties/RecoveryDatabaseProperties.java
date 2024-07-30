package com.gmoon.dbrecovery.global.recovery.properties;

import com.gmoon.dbrecovery.global.recovery.strategy.RecoveryStrategyType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.dbrecovery")
//@ConstructorBinding
@RequiredArgsConstructor
@Getter
@ToString
public class RecoveryDatabaseProperties {

	private final String schema;
	private final String recoverySchema;
	private final RecoveryStrategyType strategy;
}

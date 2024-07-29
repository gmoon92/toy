package com.gmoon.dbrecovery.global.recovery.properties;

import com.gmoon.dbrecovery.global.recovery.strategy.RecoveryRecord;
import com.gmoon.dbrecovery.global.recovery.strategy.RecoverySchema;
import com.gmoon.dbrecovery.global.recovery.strategy.RecoveryStrategy;
import com.gmoon.dbrecovery.global.recovery.strategy.RecoveryTable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "service.dbrecovery")
//@ConstructorBinding
@RequiredArgsConstructor
@ToString
public class RecoveryDatabaseProperties {

	private static final Map<RecoveryStrategyType, RecoveryStrategy> recoveryStrategies;

	static {
		recoveryStrategies = new HashMap<>();
		recoveryStrategies.put(RecoveryStrategyType.SCHEMA, new RecoverySchema());
		recoveryStrategies.put(RecoveryStrategyType.TABLE, new RecoveryTable());
		recoveryStrategies.put(RecoveryStrategyType.RECORD, new RecoveryRecord());
	}

	@Getter
	private final String schema;

	@Getter
	private final String recoverySchema;

	private final RecoveryStrategyType strategy;

	public RecoveryStrategy getStrategy() {
		return recoveryStrategies.get(strategy);
	}

	enum RecoveryStrategyType {
		SCHEMA, TABLE, RECORD
	}
}

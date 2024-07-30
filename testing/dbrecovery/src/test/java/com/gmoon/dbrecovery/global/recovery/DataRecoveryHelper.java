package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.SqlStatementCallStack;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import com.gmoon.dbrecovery.global.recovery.strategy.RecordRecoveryStrategy;
import com.gmoon.dbrecovery.global.recovery.strategy.RecoveryStrategy;
import com.gmoon.dbrecovery.global.recovery.strategy.RecoveryStrategyType;
import com.gmoon.dbrecovery.global.recovery.strategy.SchemaRecoveryStrategy;
import com.gmoon.dbrecovery.global.recovery.strategy.TableRecoveryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
public class DataRecoveryHelper {

	private final DataSource dataSource;
	private final RecoveryTable recoveryTable;
	private final RecoveryDatabaseProperties properties;

	public void recovery(SqlStatementCallStack callStack) {
		RecoveryStrategy recoveryStrategy = getRecoveryStrategy(callStack);
		recoveryStrategy.recovery();
	}

	private RecoveryStrategy getRecoveryStrategy(SqlStatementCallStack callStack) {
		RecoveryStrategyType type = properties.getStrategy();
		if (RecoveryStrategyType.SCHEMA == type) {
			return new SchemaRecoveryStrategy(dataSource, recoveryTable, properties);
		} else if (RecoveryStrategyType.RECORD == type) {
			return new RecordRecoveryStrategy(dataSource, recoveryTable, properties, callStack);
		}

		return new TableRecoveryStrategy(dataSource, recoveryTable, properties, callStack);
	}
}

package com.gmoon.dbrecovery.global.recovery.strategy;

import com.gmoon.javacore.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;

@Slf4j
public abstract class AbstractRecoveryStrategy implements RecoveryStrategy {

	protected void recovery(Collection<String> tableNames) {
		if (CollectionUtils.isNotEmpty(tableNames)) {
			try (Connection connection = getConnection()) {
				log.debug("Start data recovery.");
				executeQuery(connection, "SET FOREIGN_KEY_CHECKS = 0");
				executeQuery(connection, "SET AUTOCOMMIT = 0");

				for (String tableName : tableNames) {
					truncateTable(connection, tableName);
					recoveryTable(connection, tableName);
				}
				executeQuery(connection, "SET FOREIGN_KEY_CHECKS = 1");
				executeQuery(connection, "COMMIT");
				executeQuery(connection, "SET AUTOCOMMIT = 1");
				log.debug("Data recovery successful.");
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	void truncateTable(Connection connection, String tableName) {
		String originTable = getSchema() + "." + tableName;
		executeQuery(connection, "TRUNCATE TABLE " + originTable);
		log.debug("[TRUNCATE] {}", originTable);
	}

	void recoveryTable(Connection connection, String tableName) {
		String originTable = getSchema() + "." + tableName;
		String backupTable = getRecoverySchema() + "." + tableName;
		executeQuery(connection, String.format("INSERT INTO %s SELECT * FROM %s", originTable, backupTable));
		log.debug("[RECOVERY] {}", originTable);
	}

	void executeQuery(Connection connection, String sql) {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	abstract public Connection getConnection() throws Exception;

	abstract public String getSchema();

	abstract public String getRecoverySchema();
}

package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.DataSourceProxy;
import com.gmoon.dbrecovery.global.recovery.properties.RecoveryDatabaseProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class DataRecoveryExtension implements AfterEachCallback {

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		RecoveryDatabaseProperties recoveryDatabaseProperties = obtainBean(extensionContext, RecoveryDatabaseProperties.class);
		if (!recoveryDatabaseProperties.isEnabled()) {
			return;
		}

		registerRecoveryData(extensionContext);
	}

	/**
	 * https://stackoverflow.com/questions/15026142/creating-a-post-commit-when-using-transaction-in-spring
	 * https://stackoverflow.com/questions/18771296/spring-transactions-transactionsynchronizationmanager-isactualtransactionactive
	 */
	private void registerRecoveryData(ExtensionContext extensionContext) {
		boolean activeTransaction = TransactionSynchronizationManager.isSynchronizationActive();
		log.info("activeTransaction: {}", activeTransaction);
		if (!activeTransaction) {
			recovery(extensionContext);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(
			 new TransactionSynchronization() {
				 @Override
				 public void afterCompletion(int status) {
					 log.info("status: {}", status);
					 TransactionSynchronization.super.afterCompletion(status);
					 recovery(extensionContext);
				 }
			 }
		);
	}

	private void recovery(ExtensionContext extensionContext) {
		DataRecoveryHelper dataRecoveryHelper = obtainBean(extensionContext, DataRecoveryHelper.class);
		dataRecoveryHelper.recovery();
		DataSourceProxy.connectionThreadLocal.remove();
		DataSourceProxy.detectedStatementThreadLocal.remove();
	}

	private <T> T obtainBean(ExtensionContext context, Class<T> clazz) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(clazz);
	}
}

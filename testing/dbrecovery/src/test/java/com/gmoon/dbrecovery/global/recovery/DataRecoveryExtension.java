package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.LoggingEventListenerProxy;
import com.gmoon.dbrecovery.global.recovery.datasource.SqlStatementCallStack;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContextAnnotationUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class DataRecoveryExtension implements BeforeEachCallback, AfterEachCallback {

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		checkDeclaredTransactionalAnnotation(extensionContext);
		clearSqlCallStack();
	}

	private static void checkDeclaredTransactionalAnnotation(ExtensionContext extensionContext) {
		if (TestContextAnnotationUtils.hasAnnotation(extensionContext.getRequiredTestClass(), Transactional.class) ||
			 TestContextAnnotationUtils.hasAnnotation(extensionContext.getRequiredTestClass(), jakarta.transaction.Transactional.class) ||
			 AnnotatedElementUtils.hasAnnotation(extensionContext.getRequiredTestMethod(), Transactional.class) ||
			 AnnotatedElementUtils.hasAnnotation(extensionContext.getRequiredTestMethod(), jakarta.transaction.Transactional.class)) {
			Assertions.fail("Declared @Transactional or @jakarta.transaction.Transactional annotation.");
		}
	}

	private void clearSqlCallStack() {
		LoggingEventListenerProxy.sqlCallStack.clear();
	}


	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		registerRecoveryData(extensionContext);
	}

	/**
	 * https://stackoverflow.com/questions/15026142/creating-a-post-commit-when-using-transaction-in-spring
	 * https://stackoverflow.com/questions/18771296/spring-transactions-transactionsynchronizationmanager-isactualtransactionactive
	 */
	private void registerRecoveryData(ExtensionContext extensionContext) {
		boolean activeTransaction = TransactionSynchronizationManager.isSynchronizationActive();
		if (!activeTransaction) {
			recovery(extensionContext);
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(
			 new TransactionSynchronization() {
				 @Override
				 public void afterCompletion(int status) {
					 TransactionSynchronization.super.afterCompletion(status);
					 recovery(extensionContext);
				 }
			 }
		);
	}

	private void recovery(ExtensionContext extensionContext) {
		DataRecoveryHelper dataRecoveryHelper = obtainBean(extensionContext, DataRecoveryHelper.class);

		SqlStatementCallStack callStack = LoggingEventListenerProxy.sqlCallStack;
		dataRecoveryHelper.recovery(callStack);
		clearSqlCallStack();
	}

	private <T> T obtainBean(ExtensionContext context, Class<T> clazz) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(clazz);
	}
}

package com.gmoon.dbrecovery;

import com.gmoon.dbrecovery.datasource.event.LoggingEventListenerProxy;
import com.gmoon.dbrecovery.datasource.event.SqlStatementCallStack;
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
import org.springframework.util.StopWatch;

@Slf4j
public class DataRecoveryExtension implements BeforeEachCallback, AfterEachCallback {

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		checkDeclaredTransactionalAnnotation(extensionContext);

		DataRecoveryHelper dataRecoveryHelper = obtainBean(extensionContext, DataRecoveryHelper.class);
		dataRecoveryHelper.recoveryBrokenTable();
		clearSqlCallStack();
	}

	private void checkDeclaredTransactionalAnnotation(ExtensionContext extensionContext) {
		if (declaredTransactionalOnTestClass(extensionContext.getRequiredTestClass()) ||
			 AnnotatedElementUtils.hasAnnotation(extensionContext.getRequiredTestMethod(), Transactional.class) ||
			 AnnotatedElementUtils.hasAnnotation(extensionContext.getRequiredTestMethod(), jakarta.transaction.Transactional.class)) {
			Assertions.fail("Declared @Transactional or @jakarta.transaction.Transactional annotation.");
		}
	}

	private boolean declaredTransactionalOnTestClass(Class<?> requiredTestClass) {
		boolean declared = TestContextAnnotationUtils.hasAnnotation(requiredTestClass, Transactional.class) ||
			 TestContextAnnotationUtils.hasAnnotation(requiredTestClass, jakarta.transaction.Transactional.class);
		if (declared) {
			return true;
		}

		Class<?> superclass = requiredTestClass.getSuperclass();
		if (superclass != null) {
			return declaredTransactionalOnTestClass(superclass);
		}
		return false;
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
		if (activeTransaction) {
			TransactionSynchronizationManager.registerSynchronization(
				 new TransactionSynchronization() {
					 @Override
					 public void afterCompletion(int status) {
						 TransactionSynchronization.super.afterCompletion(status);
						 recovery(extensionContext);
					 }
				 }
			);
			return;
		}

		recovery(extensionContext);
	}

	private void recovery(ExtensionContext extensionContext) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		DataRecoveryHelper dataRecoveryHelper = obtainBean(extensionContext, DataRecoveryHelper.class);

		SqlStatementCallStack callStack = LoggingEventListenerProxy.sqlCallStack;
		dataRecoveryHelper.recovery(callStack);
		stopWatch.stop();
		log.warn("total recovery time ms: " + stopWatch.getTotalTimeMillis());
	}

	private <T> T obtainBean(ExtensionContext context, Class<T> clazz) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(clazz);
	}
}

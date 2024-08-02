package com.gmoon.dbrestore.test.dbrestore;

import com.gmoon.dbrestore.test.dbrestore.datasource.event.LoggingEventListenerProxy;
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

import java.lang.reflect.Method;

@Slf4j
public class DatabaseRestoreJunitExtension implements BeforeEachCallback, AfterEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		validateNoTransactionalAnnotation(context);

		DatabaseRestoreHelper helper = obtainBean(context, DatabaseRestoreHelper.class);
		helper.snapshot(LoggingEventListenerProxy.sqlCallStack);
	}

	private void validateNoTransactionalAnnotation(ExtensionContext context) {
		if (hasTransactionAnnotation(context)) {
			Assertions.fail("Declared @Transactional or @jakarta.transaction.Transactional annotation.");
		}
	}

	private boolean hasTransactionAnnotation(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		if (AnnotatedElementUtils.hasAnnotation(testMethod, Transactional.class) ||
			 AnnotatedElementUtils.hasAnnotation(testMethod, jakarta.transaction.Transactional.class)) {
			return true;
		}


		Class<?> testClass = context.getRequiredTestClass();
		while (testClass != null) {
			if (TestContextAnnotationUtils.hasAnnotation(testClass, Transactional.class) ||
				 TestContextAnnotationUtils.hasAnnotation(testClass, jakarta.transaction.Transactional.class)) {
				return true;
			}
			testClass = testClass.getSuperclass();
		}
		return false;
	}


	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(
				 new TransactionSynchronization() {
					 @Override
					 public void afterCompletion(int status) {
						 TransactionSynchronization.super.afterCompletion(status);
						 restore(extensionContext);
					 }
				 }
			);
		} else {
			restore(extensionContext);
		}
	}

	private void restore(ExtensionContext context) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		DatabaseRestoreHelper helper = obtainBean(context, DatabaseRestoreHelper.class);
		helper.restore(LoggingEventListenerProxy.sqlCallStack);

		stopWatch.stop();
		log.warn("total restoration time {} ms", stopWatch.getTotalTimeMillis());
	}

	private <T> T obtainBean(ExtensionContext context, Class<T> clazz) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(clazz);
	}
}

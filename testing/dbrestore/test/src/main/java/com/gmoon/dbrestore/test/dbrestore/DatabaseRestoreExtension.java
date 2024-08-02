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

@Slf4j
public class DatabaseRestoreExtension implements BeforeEachCallback, AfterEachCallback {

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		checkDeclaredTransactionalAnnotation(context);

		DataRestorationHelper helper = obtainBean(context, DataRestorationHelper.class);
		helper.snapshot(LoggingEventListenerProxy.sqlCallStack);
	}

	private void checkDeclaredTransactionalAnnotation(ExtensionContext context) {
		if (declaredTransactionalOnTestClass(context.getRequiredTestClass()) ||
			 AnnotatedElementUtils.hasAnnotation(context.getRequiredTestMethod(), Transactional.class) ||
			 AnnotatedElementUtils.hasAnnotation(context.getRequiredTestMethod(), jakarta.transaction.Transactional.class)) {
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

		DataRestorationHelper helper = obtainBean(context, DataRestorationHelper.class);
		helper.restore(LoggingEventListenerProxy.sqlCallStack);

		stopWatch.stop();
		log.warn("total restoration time {} ms", stopWatch.getTotalTimeMillis());
	}

	private <T> T obtainBean(ExtensionContext context, Class<T> clazz) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(clazz);
	}
}

package com.gmoon.timesorteduniqueidentifier.global.base.annotation;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
class JpaFlushClearAfterTestExtension implements AfterTestExecutionCallback {

	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		Class<?> requiredTestClass = context.getRequiredTestClass();
		JpaFlushAndClear annotation = getJpaFlushAndClear(requiredTestClass);
		if (annotation.enabled()) {
			flushAndClear(context);
		}
	}

	private JpaFlushAndClear getJpaFlushAndClear(Class<?> requiredTestClass) {
		JpaFlushAndClear annotation = AnnotatedElementUtils.findMergedAnnotation(requiredTestClass, JpaFlushAndClear.class);
		if (annotation == null) {
			Class<?> nestedClass = requiredTestClass.getEnclosingClass();
			return getJpaFlushAndClear(nestedClass);
		}
		return annotation;
	}

	private void flushAndClear(ExtensionContext context) {
		EntityManager entityManager = getEntityManager(context);
		entityManager.flush();
		entityManager.clear();
		log.info("EntityManager flushed and cleared.");
	}

	private EntityManager getEntityManager(ExtensionContext context) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(EntityManager.class);
	}
}

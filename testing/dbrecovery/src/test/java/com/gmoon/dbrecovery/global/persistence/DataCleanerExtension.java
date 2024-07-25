package com.gmoon.dbrecovery.global.persistence;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class DataCleanerExtension implements AfterEachCallback {

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		DataCleaner dataCleaner = obtainBean(extensionContext, DataCleaner.class);
		dataCleaner.recovery();
	}

	private <T> T obtainBean(ExtensionContext context, Class<T> clazz) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(clazz);
	}
}

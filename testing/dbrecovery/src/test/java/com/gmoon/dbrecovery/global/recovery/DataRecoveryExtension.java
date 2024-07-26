package com.gmoon.dbrecovery.global.recovery;

import com.gmoon.dbrecovery.global.recovery.datasource.DataSourceProxy;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class DataRecoveryExtension implements AfterEachCallback {

	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		forceTestCodeTransactionRollback();

		DataRecoveryHelper dataRecoveryHelper = obtainBean(extensionContext, DataRecoveryHelper.class);
		dataRecoveryHelper.recovery();
		DataSourceProxy.connectionThreadLocal.remove();
		DataSourceProxy.detectedStatementThreadLocal.remove();
	}

	private void forceTestCodeTransactionRollback() throws SQLException {
		Connection connection = DataSourceProxy.connectionThreadLocal.get();
		boolean activeTestConnection = connection != null && !connection.isClosed();
		if (activeTestConnection) {
			connection.rollback();
		}
	}

	private <T> T obtainBean(ExtensionContext context, Class<T> clazz) {
		ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
		return applicationContext.getBean(clazz);
	}
}

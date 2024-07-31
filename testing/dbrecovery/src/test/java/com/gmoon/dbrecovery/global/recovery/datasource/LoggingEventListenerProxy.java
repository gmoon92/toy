package com.gmoon.dbrecovery.global.recovery.datasource;

import com.p6spy.engine.common.Loggable;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.logging.LoggingEventListener;

import java.sql.SQLException;

/**
 * <code>src/main/resources/META-INF/services/com.p6spy.engine.logging.LoggingEventListener</code>
 *
 * @see com.p6spy.engine.logging.P6LogFactory
 * @see com.p6spy.engine.logging.LoggingEventListener
 */
public class LoggingEventListenerProxy extends LoggingEventListener {

	public static final SqlStatementCallStack sqlCallStack = new SqlStatementCallStack(
		 DmlStatement.INSERT,
		 DmlStatement.UPDATE,
		 DmlStatement.DELETE
	);

	@Override
	protected void logElapsed(Loggable loggable, long timeElapsedNanos, Category category, SQLException e) {
		detectDMLStatement(loggable);
		super.logElapsed(loggable, timeElapsedNanos, category, e);
	}

	private void detectDMLStatement(Loggable loggable) {
		String sqlWithValues = loggable.getSqlWithValues();
		sqlCallStack.push(sqlWithValues);
	}
}

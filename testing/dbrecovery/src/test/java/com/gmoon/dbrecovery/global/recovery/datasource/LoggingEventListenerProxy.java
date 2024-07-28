package com.gmoon.dbrecovery.global.recovery.datasource;

import com.p6spy.engine.common.Loggable;
import com.p6spy.engine.logging.Category;
import com.p6spy.engine.logging.LoggingEventListener;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

/**
 * <code>src/main/resources/META-INF/services/com.p6spy.engine.logging.LoggingEventListener</code>
 *
 * @see com.p6spy.engine.logging.P6LogFactory
 * @see com.p6spy.engine.logging.LoggingEventListener
 */
@Slf4j
public class LoggingEventListenerProxy extends LoggingEventListener {

	@Override
	protected void logElapsed(Loggable loggable, long timeElapsedNanos, Category category, SQLException e) {
		// todo detected dml sql statement
		super.logElapsed(loggable, timeElapsedNanos, category, e);
	}
}

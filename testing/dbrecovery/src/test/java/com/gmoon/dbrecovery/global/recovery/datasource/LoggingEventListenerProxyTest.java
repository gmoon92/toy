package com.gmoon.dbrecovery.global.recovery.datasource;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class LoggingEventListenerProxyTest {

	@Test
	void test() {
		assertThat(getCallStack().getValue()).isEmpty();

		SqlStatementCallStack callStack = getCallStack();
		callStack.push("select * from tb_ticket");
		assertThat(getCallStack().getValue()).isEmpty();

		callStack.push("insert into tb_ticket_office (id, name) values (2, 'guest')");
		assertThat(getCallStack().getValue()).isNotEmpty();

		clear();
		assertThat(getCallStack().getValue()).isEmpty();
	}

	private SqlStatementCallStack getCallStack() {
		return LoggingEventListenerProxy.sqlCallStack;
	}

	private void clear() {
		LoggingEventListenerProxy.sqlCallStack.clear();
	}
}

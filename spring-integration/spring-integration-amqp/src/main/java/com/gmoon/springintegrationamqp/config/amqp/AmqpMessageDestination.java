package com.gmoon.springintegrationamqp.config.amqp;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AmqpMessageDestination {

	SEND_EMAIL(Value.SEND_MAIL);

	public final String value;

	private static class Value {

		private static final String SEND_MAIL = "send_mail";
	}
}

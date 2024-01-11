package com.gmoon.springintegrationamqp.global.amqp;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum AmqpMessageDestination {

	SEND_MAIL(Value.SEND_MAIL),
	WELCOME_MAIL(Value.WELCOME_MAIL);

	public final String value;

	private static class Value {

		private static final String SEND_MAIL = "send_mail";
		private static final String WELCOME_MAIL = "welcome_mail";
	}
}

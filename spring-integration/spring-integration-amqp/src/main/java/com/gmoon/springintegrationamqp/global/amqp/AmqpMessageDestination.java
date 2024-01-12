package com.gmoon.springintegrationamqp.global.amqp;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum AmqpMessageDestination {

	SEND_MAIL(Value.SEND_MAIL),
	SAVE_MAIL_LOG(Value.SAVE_MAIL_LOG);

	public final String value;

	private static class Value {

		private static final String SEND_MAIL = "send_mail";
		private static final String SAVE_MAIL_LOG = "save_mail_log";
	}
}

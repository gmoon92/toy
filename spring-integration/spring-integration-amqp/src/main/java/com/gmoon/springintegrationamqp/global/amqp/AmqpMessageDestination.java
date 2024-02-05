package com.gmoon.springintegrationamqp.global.amqp;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AmqpMessageDestination {

	SEND_MAIL("send_mail"),
	SAVE_MAIL_LOG("save_mail_log");

	public final String value;

}

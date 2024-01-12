package com.gmoon.springintegrationamqp.mail.model;

import java.io.Serializable;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public class SendMailVO implements Serializable {

	private static final long serialVersionUID = 793592979833200752L;

	private String from;
	private String to;
	private String subject;
	private String content;

	@Builder
	private SendMailVO(String from, String to, String subject, String content) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.content = content;
	}

	public Message<SendMailVO> toMessage() {
		return MessageBuilder
			.withPayload(this)
			.build();
	}
}

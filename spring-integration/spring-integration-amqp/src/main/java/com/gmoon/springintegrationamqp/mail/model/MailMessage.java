package com.gmoon.springintegrationamqp.mail.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class MailMessage implements Serializable, Message<MailMessage.Payload> {

	private static final long serialVersionUID = 3765854360400542751L;

	private MessageHeaders headers;
	private Payload payload = new Payload();

	@NoArgsConstructor
	@Getter
	@EqualsAndHashCode
	@ToString
	public static class Payload implements Serializable {

		private static final long serialVersionUID = -2148390084128476064L;

		private String content;

		public Payload(String content) {
			this.content = content;
		}
	}

	@Builder
	public MailMessage(String from, String to, String subject, String content) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("from", from);
		metadata.put("to", to);
		metadata.put("subject", subject);
		headers = new MessageHeaders(metadata);
		payload.content = content;
	}
}

package com.gmoon.springasync.mail;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MailConfig {
	private static final String DEFAULT_MAIL_SUBJECT_FORMAT = "Hello %s.";
	private static final String DEFAULT_MAIL_TEXT_FORMAT = "I invite you to my github page. %s";

	@Bean
	public SimpleMailMessage templateSimpleMailMessage(MailProperties mailProperties) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(mailProperties.getUsername());
		message.setSubject(DEFAULT_MAIL_SUBJECT_FORMAT);
		message.setText(DEFAULT_MAIL_TEXT_FORMAT);
		return message;
	}
}

package com.gmoon.springintegrationamqp.mail.application;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Service;

import com.gmoon.springintegrationamqp.mail.model.MailMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailService {

	@ServiceActivator
	public MailMessage send(MailMessage message) {
		log.info("send mail: {}", message);
		return message;
	}

	@ServiceActivator
	public void welcome(MailMessage.Payload message) {
		log.info("welcome: {}", message);
	}
}

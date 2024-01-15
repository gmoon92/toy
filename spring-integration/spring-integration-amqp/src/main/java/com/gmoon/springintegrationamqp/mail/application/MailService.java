package com.gmoon.springintegrationamqp.mail.application;

import java.util.Map;

import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.gmoon.springintegrationamqp.mail.model.SaveMailLogVO;
import com.gmoon.springintegrationamqp.mail.model.SendMailVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailService {

	public SendMailVO send(SendMailVO message) {
		log.info("send mail: {}", message);
		return message;
	}

	public void saveLog(@Headers Map<Object, Object> headers, SaveMailLogVO message) {
		log.info("headers: {}, payload: {}", headers, message);
	}
}

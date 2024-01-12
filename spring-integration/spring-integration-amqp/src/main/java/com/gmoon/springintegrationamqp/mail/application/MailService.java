package com.gmoon.springintegrationamqp.mail.application;

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

	public void saveLog(SaveMailLogVO message) {
		log.info("logging: {}", message);
	}
}

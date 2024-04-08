package com.gmoon.springfcm.messaging;

import com.google.firebase.messaging.Message;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpFcmSender implements FcmClient {

	@Override
	public String push(Message message) {
		log.info("noop fcm push message.");
		return "noop";
	}
}

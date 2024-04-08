package com.gmoon.springfcm.messaging;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FcmSender implements FcmClient {

	private final FirebaseMessaging firebaseMessaging;

	@Override
	public String push(Message message) {
		try {
			log.debug("send message...");
			return firebaseMessaging.send(message);
		} catch (Exception e) {
			throw new RuntimeException("push message fail.", e);
		}
	}
}

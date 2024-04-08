package com.gmoon.springfcm.messaging;

import com.google.firebase.messaging.Message;

public interface FcmClient {
	String push(Message message);
}

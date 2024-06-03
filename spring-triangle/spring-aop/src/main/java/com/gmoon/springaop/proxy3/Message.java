package com.gmoon.springaop.proxy3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 팩토리 빈의 동작원리를 확인하기 위한 오브젝트
 * */
@RequiredArgsConstructor
@Getter
public class Message {
	public final String text;

	public static Message newMessage(String text) {
		return new Message(text);
	}
}

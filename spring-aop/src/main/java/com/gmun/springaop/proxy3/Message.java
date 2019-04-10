package com.gmun.springaop.proxy3;

/**
 * 팩토리 빈의 동작원리를 확인하기 위한 오브젝트
 * */
public class Message {
	
	public String text;

	private Message(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	public static Message newMessage(String text) {
		return new Message(text);
	}
}

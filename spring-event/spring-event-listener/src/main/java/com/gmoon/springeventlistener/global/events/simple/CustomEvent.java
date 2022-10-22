package com.gmoon.springeventlistener.global.events.simple;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class CustomEvent extends ApplicationEvent {

	private final String message;

	public CustomEvent(Object source, String message) {
		super(source);
		this.message = message;
	}
}

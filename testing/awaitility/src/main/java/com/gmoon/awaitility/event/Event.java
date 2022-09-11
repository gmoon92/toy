package com.gmoon.awaitility.event;

import java.util.UUID;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Event {

	private final UUID id;
	private final String message;

	public Event(String message) {
		this.id = UUID.randomUUID();
		this.message = message;
	}
}

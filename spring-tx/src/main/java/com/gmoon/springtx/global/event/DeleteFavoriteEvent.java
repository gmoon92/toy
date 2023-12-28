package com.gmoon.springtx.global.event;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class DeleteFavoriteEvent extends ApplicationEvent {

	private String eventId;
	private String userId;
	private LocalDateTime issuedDt;

	public DeleteFavoriteEvent(Object source, String userId) {
		super(source);
		this.eventId = UUID.randomUUID().toString();
		this.userId = userId;
		this.issuedDt = LocalDateTime.now();
	}
}

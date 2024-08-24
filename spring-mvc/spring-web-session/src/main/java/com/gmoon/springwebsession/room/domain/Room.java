package com.gmoon.springwebsession.room.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

	@EqualsAndHashCode.Include
	private String name;
	private Long price;
	private Boolean enabled;

	protected Room() {
		enabled = true;
	}

	public static Room create(String name, Long price) {
		Room room = new Room();
		room.name = name;
		room.price = price;
		return room;
	}

	public boolean isEnabled() {
		return enabled != null && enabled;
	}
}

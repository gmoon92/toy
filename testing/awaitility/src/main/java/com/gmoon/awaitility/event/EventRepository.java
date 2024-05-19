package com.gmoon.awaitility.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Repository;

@Repository
public class EventRepository {

	private static final Map<UUID, Event> MEMORY = new HashMap<>();

	public Event save(Event data) {
		MEMORY.put(data.getId(), data);
		return data;
	}

	public int count() {
		return MEMORY.size();
	}

	public void deleteAll() {
		MEMORY.clear();
	}
}

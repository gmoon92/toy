package com.gmoon.springwebsession.room.domain;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class InMemoryRoomRepository implements RoomRepository {

	private static final Map<String, Room> CACHES = Stream.of(
		 Room.create("A", 1_000l),
		 Room.create("B", 2_000l),
		 Room.create("C", 2_000l),
		 Room.create("D", 2_000l),
		 Room.create("E", 2_000l),
		 Room.create("F", 2_000l),
		 Room.create("G", 2_000l),
		 Room.create("H", 2_000l)
	).collect(collectingAndThen(
		 toMap(Room::getName, Function.identity()),
		 HashMap::new));

	@Override
	public boolean exists(String name) {
		return find(name) != null;
	}

	@Override
	public Room find(String name) {

		return CACHES.get(name);
	}

	@Override
	public List<Room> findAll() {
		return new ArrayList<>(CACHES.values());
	}

	@Override
	public Room save(Room room) {
		CACHES.put(room.getName(), room);
		return room;
	}

	@Override
	public List<Room> saveAll(List<Room> rooms) {
		for (Room room : rooms) {
			save(room);
		}

		return rooms;
	}

	@Override
	public void delete(Room room) {
		delete(room.getName());
	}

	@Override
	public void delete(String name) {
		CACHES.remove(name);
	}
}

package com.gmoon.springwebsession.room.domain;

import java.util.List;

public interface RoomRepository {

	boolean exists(String name);

	Room find(String name);

	List<Room> findAll();

	Room save(Room room);
	List<Room> saveAll(List<Room> rooms);

	void delete(Room room);

	void delete(String name);
}

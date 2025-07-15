package com.gmoon.springwebsession.room.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gmoon.springwebsession.room.domain.Room;
import com.gmoon.springwebsession.room.domain.RoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {

	private final RoomRepository repository;

	public Room save(Room room) {
		return repository.save(room);
	}

	public Room find(String name) {
		return repository.find(name);
	}

	public List<Room> findAll() {
		return repository.findAll().stream()
			 .filter(Room::isEnabled)
			 .toList();
	}

	public void delete(String name) {
		if (repository.exists(name)) {
			repository.delete(name);
		}
	}
}

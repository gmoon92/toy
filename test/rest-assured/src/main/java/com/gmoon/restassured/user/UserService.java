package com.gmoon.restassured.user;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository repository;

	public List<User> findAll() {
		return repository.findAll();
	}

	public User find(String username) {
		return repository.findByName(username);
	}

	public User save(String username) {
		User user = User.from(username);
		return repository.save(user);
	}

	public void delete(String username) {
		User user = User.from(username);
		repository.remove(user);
	}
}

package com.gmoon.restassured.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class UserRepository {
	private static final List<User> ALL = new ArrayList<>(Arrays.asList(
		User.from("gmoon"),
		User.from("guest").disabled()
	));

	public List<User> findAll() {
		return ALL;
	}

	public User findByName(String username) {
		return ALL.stream()
			.filter(user -> user.equals(User.from(username)))
			.findFirst()
			.orElseThrow(() -> new RuntimeException(String.format("User(%s) not found... ", username)));
	}

	public User save(User user) {
		ALL.add(user);
		log.debug("user: {}, ALL: {}", user, ALL);
		return user;
	}

	public void remove(User user) {
		ALL.remove(user);
		log.debug("user: {}, ALL: {}", user, ALL);
	}
}

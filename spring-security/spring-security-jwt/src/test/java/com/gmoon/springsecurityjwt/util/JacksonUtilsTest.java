package com.gmoon.springsecurityjwt.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gmoon.springsecurityjwt.user.Role;
import com.gmoon.springsecurityjwt.user.User;

class JacksonUtilsTest {

	@Test
	void testToString() {
		// given
		User user = User.create("gmoon", "123", Role.ADMIN);

		// when
		String jsonString = JacksonUtils.toString(user);

		// then
		assertThat(jsonString).contains("username", "gmoon", "authorities", "ADMIN");
	}

	@Test
	void testToObject() {
		// given
		String jsonString = JacksonUtils.toString(User.create("gmoon", "123", Role.ADMIN));

		// when
		User actual = JacksonUtils.toObject(jsonString, User.class);

		// then
		assertThat(actual).hasFieldOrPropertyWithValue("username", "gmoon");
	}

	@Test
	void testToObject_TypeReference() {
		// given
		List<User> users = Arrays.asList(
			User.create("gmoon", "123", Role.ADMIN),
			User.create("user", "123", Role.USER)
		);

		String jsonString = JacksonUtils.toString(users);

		// when
		List<User> actual = JacksonUtils.toObject(jsonString, new TypeReference<List<User>>() {});

		// then
		assertThat(actual)
			.hasSize(2)
			.containsExactly(
				User.create("gmoon", "123", Role.ADMIN),
				User.create("user", "123", Role.USER)
			);
	}
}

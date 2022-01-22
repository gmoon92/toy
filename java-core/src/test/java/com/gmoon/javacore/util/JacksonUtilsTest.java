package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

class JacksonUtilsTest {

	@Test
	void testToString() {
		// given
		User user = User.create("gmoon");

		// when
		String jsonString = JacksonUtils.toString(user);

		// then
		assertThat(jsonString).contains("username", "gmoon", "enabled", "true");
	}

	@Test
	void testToObject() {
		// given
		String jsonString = JacksonUtils.toString(User.create("gmoon"));

		// when
		User actual = JacksonUtils.toObject(jsonString, User.class);

		// then
		assertThat(actual).hasFieldOrPropertyWithValue("username", "gmoon");
	}

	@Test
	void testToObject_TypeReference() {
		// given
		List<User> users = Arrays.asList(
			User.create("gmoon"),
			User.create("anonymous", false)
		);

		String jsonString = JacksonUtils.toString(users);

		// when
		List<User> actual = JacksonUtils.toObject(jsonString, new TypeReference<List<User>>() {});

		// then
		assertThat(actual)
			.hasSize(2)
			.containsExactly(
				User.create("gmoon"),
				User.create("anonymous", false)
			);
	}

	@Getter
	@EqualsAndHashCode(of = "username")
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	static class User implements Serializable {
		private static final long serialVersionUID = -2696732525957590605L;

		private String username;
		private boolean enabled;

		User(String username, boolean enabled) {
			this.username = username;
			this.enabled = enabled;
		}

		static User create(String name) {
			return User.create(name, true);
		}

		static User create(String name, boolean enabled) {
			return new User(name, enabled);
		}
	}
}

package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonTest
class JacksonUtilsTest {
	@Autowired
	private JacksonTester<User> json;

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
	void testToString_jacksonTester() throws IOException {
		// given
		User user = User.create("gmoon");

		// when then
		assertThat(json.write(user)).hasJsonPath("@.enabled");
		assertThat(json.write(user)).extractingJsonPathStringValue("@.username")
			 .isEqualTo("gmoon");
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
		List<User> actual = JacksonUtils.toObject(jsonString, new TypeReference<List<User>>() {
		});

		// then
		assertThat(actual)
			 .hasSize(2)
			 .containsExactly(
				  User.create("gmoon"),
				  User.create("anonymous", false)
			 );
	}

	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Getter
	@EqualsAndHashCode(of = "username")
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

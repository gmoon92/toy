package com.gmoon.springsecurityjwt.util;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import com.fasterxml.jackson.core.type.TypeReference;

import com.gmoon.javacore.util.StringUtils;
import com.gmoon.springsecurityjwt.user.Role;
import com.gmoon.springsecurityjwt.user.User;

@JsonTest
class JacksonUtilsTest {
	@Autowired
	private JacksonTester<User> json;

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
	void testToString_jacksonTester() throws IOException {
		// given
		User user = User.create("gmoon", StringUtils.randomAlphabetic(10), Role.ADMIN);

		// when then
		assertThat(json.write(user)).hasJsonPathStringValue("@.password");
		assertThat(json.write(user)).extractingJsonPathStringValue("@.username")
			 .isEqualTo("gmoon");
		assertThat(json.write(user)).extractingJsonPathArrayValue("@.authorities")
			 .containsExactly("ADMIN");
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
		List<User> actual = JacksonUtils.toObject(jsonString, new TypeReference<List<User>>() {
		});

		// then
		assertThat(actual)
			 .hasSize(2)
			 .containsExactly(
				  User.create("gmoon", "123", Role.ADMIN),
				  User.create("user", "123", Role.USER)
			 );
	}
}

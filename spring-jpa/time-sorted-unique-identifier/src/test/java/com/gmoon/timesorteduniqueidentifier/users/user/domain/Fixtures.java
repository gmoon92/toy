package com.gmoon.timesorteduniqueidentifier.users.user.domain;

import com.gmoon.javacore.util.TimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Fixtures {

	private static Instant toInstant(String dateStr) {
		String pattern = "yyyy-MM-dd HH:mm:ss";
		return TimeUtils.of(pattern, dateStr);
	}

	public static class Users {
		public static User ADMIN = User.of("user01", "admin", "p@ssword", toInstant("2024-09-15 15:00:00"));
		public static User USER = User.of("user02", "gmoon", "p@ssword", toInstant("2024-09-15 15:00:00"));

		public static User.UserBuilder user(String username, String password) {
			return User.builder()
				 .username(username)
				 .password(password);
		}
	}
}

package com.gmoon.timesorteduniqueidentifier.users.user.domain;

import com.gmoon.javacore.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Slf4j
class UserTest {

	private User user;

	@BeforeEach
	void setUp() {
		user = Fixtures.Users.user("admin", "password")
			 .build();
	}

	@Nested
	class PasswordTest {

		@DisplayName("사용자 비밀번호를 업데이트한다.")
		@Test
		void success() {
			String newPassword = StringUtils.randomAlphabetic(8);
			assertThat(user.updatePassword(newPassword)).hasFieldOrPropertyWithValue("password", newPassword);
		}

		@DisplayName("사용자 비밀번호는 공백이거나 null 일 수 없다.")
		@ParameterizedTest
		@NullAndEmptySource
		void error1(String newPassword) {
			assertThatExceptionOfType(IllegalArgumentException.class)
				 .isThrownBy(() -> user.updatePassword(newPassword));
		}

		@DisplayName("사용자 비밀번호는 이전 패스워드와 동일할 수 없다.")
		@Test
		void error2() {
			String newPassword = user.getPassword();

			assertThatExceptionOfType(IllegalArgumentException.class)
				 .isThrownBy(() -> user.updatePassword(newPassword));
		}

		@DisplayName("사용자 비밀번호는 8글자 이상이어야 한다.")
		@Test
		void error3() {
			String newPassword = StringUtils.randomAlphabetic(7);

			assertThatExceptionOfType(IllegalArgumentException.class)
				 .isThrownBy(() -> user.updatePassword(newPassword));
		}
	}
}

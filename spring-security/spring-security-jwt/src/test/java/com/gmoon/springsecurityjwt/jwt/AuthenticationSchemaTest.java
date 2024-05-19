package com.gmoon.springsecurityjwt.jwt;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AuthenticationSchemaTest {
	@ParameterizedTest
	@MethodSource("getSchemaNames")
	@DisplayName("인증 스키마는 대소문자를 구별한다.")
	void testCheckValidSchema(String invalidSchema) {
		// then
		assertThatThrownBy(() -> AuthenticationSchema.checkValidSchema(invalidSchema))
			 .isInstanceOf(IllegalArgumentException.class)
			 .hasMessage("Invalid authentication schema name: %s", invalidSchema);
	}

	static Stream<Arguments> getSchemaNames() {
		return Stream.of(
			 Arguments.of(AuthenticationSchema.BASIC.getName().toUpperCase()),
			 Arguments.of(AuthenticationSchema.BEARER.getName().toLowerCase())
		);
	}
}

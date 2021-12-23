package com.gmoon.springsecurityjwt.jwt;

import static org.assertj.core.api.Assertions.*;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gmoon.springsecurityjwt.user.Role;
import com.gmoon.springsecurityjwt.user.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = JWTUtil.class)
class JWTUtilTest {
	@Autowired
	JWTUtil jwtUtil;

	String token;

	@BeforeEach
	void setUp() {
		User user = User.create("gmoon", "123", Role.ADMIN);
		token = jwtUtil.generate(user);
	}

	@Test
	@DisplayName("JWT 생성 및 구조 검증, header.payload.signature")
	void testGenerate() {
		// when
		String[] parts = token.split("\\.");

		String header = decodeToBase64(parts[0]);
		String payload = decodeToBase64(parts[1]);
		String signature = parts[2];

		// then
		assertThat(header).contains("typ", "JWT", "alg", "HS256");
		assertThat(payload).contains("iss", "v1", "username", "gmoon");
		assertThat(signature).isNotBlank();
	}

	private String decodeToBase64(String str) {
		Base64.Decoder decoder = Base64.getDecoder();
		return new String(decoder.decode(str));
	}

	@Test
	@DisplayName("JWT 복호화 검증")
	void testDecode() {
		// when
		User user = jwtUtil.decode(token);

		// then
		assertThat(user)
			.hasFieldOrPropertyWithValue("username", "gmoon")
			.hasFieldOrPropertyWithValue("role", Role.ADMIN)
			.hasFieldOrPropertyWithValue("password", "");
	}
}

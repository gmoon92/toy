package com.gmoon.resourceserver.test;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gmoon.resourceserver.jwt.JwtUtils;
import com.gmoon.resourceserver.user.User;
import com.gmoon.resourceserver.user.UserRepository;

import lombok.RequiredArgsConstructor;

@SpringBootTest
public abstract class BaseIntegrationTest {
	public static Token TOKEN_OF_ADMIN;

	protected MockMvc mockMvc;

	@Autowired
	WebApplicationContext context;

	@BeforeAll
	static void beforeAll(@Autowired UserRepository repository, @Autowired JwtUtils jwtUtils) {
		User admin = repository.findByUsername("admin");
		TOKEN_OF_ADMIN = new Token(jwtUtils.generate(admin));
	}

	@RequiredArgsConstructor
	static class Token {
		final String value;

		@Override
		public String toString() {
			return value;
		}
	}

	protected void setupMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.alwaysDo(print())
			.apply(springSecurity())
			.build();
	}
}

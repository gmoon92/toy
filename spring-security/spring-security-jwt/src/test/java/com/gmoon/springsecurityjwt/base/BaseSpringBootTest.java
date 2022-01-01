package com.gmoon.springsecurityjwt.base;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.HeaderResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.StatusResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
public abstract class BaseSpringBootTest {
	@Autowired
	WebApplicationContext context;

	protected MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.apply(springSecurity())
			.alwaysDo(print())
			.build();
	}

	public MockHttpServletRequestBuilder get(String urlTemplate, Object... uriVars) {
		return MockMvcRequestBuilders.get(urlTemplate, uriVars);
	}

	public MockHttpServletRequestBuilder post(String urlTemplate, Object... uriVars) {
		return MockMvcRequestBuilders.post(urlTemplate, uriVars);
	}

	public MockHttpServletRequestBuilder delete(String urlTemplate, Object... uriVars) {
		return MockMvcRequestBuilders.delete(urlTemplate, uriVars);
	}

	public MockHttpServletRequestBuilder put(String urlTemplate, Object... uriVars) {
		return MockMvcRequestBuilders.put(urlTemplate, uriVars);
	}

	public MockHttpServletRequestBuilder patch(String urlTemplate, Object... uriVars) {
		return MockMvcRequestBuilders.patch(urlTemplate, uriVars);
	}

	public SecurityMockMvcRequestBuilders.FormLoginRequestBuilder formLogin() {
		return SecurityMockMvcRequestBuilders.formLogin();
	}

	public StatusResultMatchers status() {
		return MockMvcResultMatchers.status();
	}

	public HeaderResultMatchers header() {
		return MockMvcResultMatchers.header();
	}

	public ResultMatcher authenticated() {
		return SecurityMockMvcResultMatchers.authenticated();
	}

	public ResultMatcher unauthenticated() {
		return SecurityMockMvcResultMatchers.unauthenticated();
	}
}

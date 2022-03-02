package com.gmoon.resourceclient.util;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class CookieUtilsTest {
	@Test
	void testCreateSessionCookie() {
		// given
		HttpServletResponse response = new MockHttpServletResponse();
		String name = "gmoon";
		String value = "123";

		// when
		CookieUtils.addHeaderCookie(response, name, value);

		// then
		String headerOfCookie = response.getHeader(HttpHeaders.SET_COOKIE);
		assertThat(headerOfCookie)
			.contains(name, value);
		log.info("Set-Cookie: {}", headerOfCookie);
	}

	@Test
	void testGetCookieValue() {
		// given
		String name = "gmoon";
		String value = "123";
		Cookie cookie = CookieUtils.create(name, value);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(cookie);

		// when then
		assertAll(
			() -> assertThat(CookieUtils.getCookieValue(request, name)).isEqualTo(value),
			() -> assertThrows(IllegalArgumentException.class, () -> CookieUtils.getCookieValue(request, UUID.randomUUID().toString()))
		);
	}

	@Test
	void testDelete() {
		// given
		String name = "gmoon";
		String value = "123";
		Cookie cookie = CookieUtils.create(name, value);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setCookies(cookie);

		// when
		MockHttpServletResponse response = new MockHttpServletResponse();
		CookieUtils.delete(request, name, response);

		// then
		assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
			.contains("Max-Age=0");
	}
}

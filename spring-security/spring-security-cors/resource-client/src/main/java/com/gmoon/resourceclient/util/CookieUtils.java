package com.gmoon.resourceclient.util;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.server.header.CacheControlServerHttpHeadersWriter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CookieUtils {
	private final Duration DEFAULT_MAX_AGE = Duration.ofDays(1);

	public Cookie create(String name, String value) {
		Cookie cookie = new Cookie(name, value);

		cookie.setHttpOnly(true);
		cookie.setPath("/");
		cookie.setMaxAge(Math.toIntExact(DEFAULT_MAX_AGE.getSeconds()));
		return cookie;
	}

	public void addHeaderCookie(HttpServletResponse response, String name, String value) {
		Cookie cookie = create(name, value);
		addHeaderCookie(response, cookie);
	}

	private void addHeaderCookie(HttpServletResponse response, Cookie cookie) {
		response.setHeader(HttpHeaders.CACHE_CONTROL, CacheControlServerHttpHeadersWriter.CACHE_CONTRTOL_VALUE);
		response.setHeader(HttpHeaders.PRAGMA, CacheControlServerHttpHeadersWriter.PRAGMA_VALUE);
		response.setHeader(HttpHeaders.EXPIRES, CacheControlServerHttpHeadersWriter.EXPIRES_VALUE);
		response.addCookie(cookie);
	}

	public String read(HttpServletRequest request, String name) {
		return getCookies(request)
			.stream()
			.filter(cookie -> StringUtils.equals(name, cookie.getName()))
			.map(Cookie::getValue)
			.findFirst()
			.orElseThrow(IllegalArgumentException::new);
	}

	private List<Cookie> getCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (ArrayUtils.isEmpty(cookies)) {
			return new ArrayList<>();
		}

		return Collections.unmodifiableList(Arrays.asList(cookies));
	}
}

package com.gmoon.resourceclient.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.web.server.header.CacheControlServerHttpHeadersWriter;

import com.gmoon.javacore.util.ArrayUtils;
import com.gmoon.javacore.util.StringUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CookieUtils {
	private final Duration DEFAULT_MAX_AGE = Duration.ofDays(1);
	private final String URI_PATH = "/";

	// An invalid character [32] was present in the Cookie value
	public Cookie create(String name, String value) {
		try {
			Cookie cookie = new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8.displayName()));

			cookie.setHttpOnly(true);
			cookie.setPath(URI_PATH);
			cookie.setMaxAge(Math.toIntExact(DEFAULT_MAX_AGE.getSeconds()));
			return cookie;
		} catch (Exception e) {
			throw new RuntimeException("A cookie could not be created.", e);
		}
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

	public String getCookieValue(HttpServletRequest request, String name) {
		Cookie cookie = get(request, name);
		return cookie.getValue();
	}

	public Cookie get(HttpServletRequest request, String name) {
		return getCookies(request)
			 .stream()
			 .filter(cookie -> StringUtils.equals(name, cookie.getName()))
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

	public void delete(HttpServletRequest request, String name, HttpServletResponse response) {
		try {
			Cookie cookie = get(request, name);
			cookie.setMaxAge(0);
			cookie.setPath(URI_PATH);
			response.addCookie(cookie);
		} catch (Exception e) {
			log.debug("Already deleted cookie.");
		}
	}
}

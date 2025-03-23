package com.rsupport.rv5x.springaccesslog.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestUtils {

	private static final Charset DEFAULT_CHARACTER_ENCODING = StandardCharsets.ISO_8859_1;

	public static String getRequestMethod(HttpServletRequest request) {
		return request.getMethod();
	}

	public static String getRequestBody(HttpServletRequest request) {
		boolean cachedContent = ContentCachingRequestWrapper.class.isInstance(request);
		if (!cachedContent) {
			log.warn("Request is not an instance of ContentCachingRequestWrapper. Unable to read body.");
			return "";
		}

		ContentCachingRequestWrapper wrappingRequest = (ContentCachingRequestWrapper) request;
		byte[] content = wrappingRequest.getContentAsByteArray();
		if (content.length > 0) {
			Charset charset = getCharset(wrappingRequest);
			return new String(content, charset);
		}

		return "";
	}

	public static String getRequestQueryString(HttpServletRequest request) {
		return request.getQueryString();
	}

	private static Charset getCharset(HttpServletRequest request) {
		try {
			String characterEncoding = request.getCharacterEncoding();
			return Charset.forName(characterEncoding);
		} catch (Exception e) {
			return DEFAULT_CHARACTER_ENCODING;
		}
	}

	public static String getRequestUri(HttpServletRequest request) {
		return request.getRequestURI();
	}

	public static String getRemoteHost(HttpServletRequest request) {
		return request.getRemoteHost();
	}

	public static int getRemotePort(HttpServletRequest request) {
		return request.getRemotePort();
	}

	public static String getRemoteIp(HttpServletRequest request) {
		String ip = getFirstNonEmpty(
			 request.getHeader("X-Forwarded-For"),
			 request.getHeader("X-Real-IP"),
			 request.getRemoteAddr()
		);

		// X-Forwarded-For는 여러 개의 IP가 있을 수 있으므로 첫 번째 값만 사용
		if (ip != null && ip.contains(",")) {
			return ip.split(",")[0].trim();
		}

		return ip;
	}

	private static String getFirstNonEmpty(String... values) {
		for (String value : values) {
			if (value != null && !value.isEmpty() && !"unknown".equalsIgnoreCase(value)) {
				return value;
			}
		}
		return null;
	}

	public static Map<String, String> getRequestHeaders(HttpServletRequest request) {
		return Collections.list(request.getHeaderNames())
			 .stream()
			 .collect(Collectors.toMap(name -> name, request::getHeader));
	}

	public static String getHeader(HttpServletRequest request, String headerName) {
		return request.getHeader(headerName);
	}

	public static Map<String, String[]> getQueryParams(HttpServletRequest request) {
		return request.getParameterMap();
	}

	public static String getQueryParam(HttpServletRequest request, String paramName) {
		return request.getParameter(paramName);
	}

	public static Map<String, String> getCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Collections.emptyMap();
		}

		return Map.ofEntries(
			 Arrays.stream(cookies)
				  .map(cookie -> Map.entry(cookie.getName(), cookie.getValue()))
				  .toArray(Map.Entry[]::new)
		);
	}

	public static String getCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	public static String getScheme(HttpServletRequest request) {
		return request.getScheme();
	}

	public static String getProtocol(HttpServletRequest request) {
		return request.getProtocol();
	}
}

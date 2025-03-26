package com.rsupport.rv5x.springaccesslog.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.unit.DataSize;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @apiNote <pre>
 * {@link RequestUtils#MAX_BODY_LENGTH}
 * 최대 요청 본문 길이 값
 * - 기본적으로 10KB (~10,240 bytes) 를 설정
 * - 대형 JSON 요청을 고려하면 50KB (~51,200 bytes) 로 확장 가능
 *
 * XML: 4KB (4,096 bytes) ~ 10KB (10,240 bytes)
 * JSON: 4KB ~ 10KB
 * form-data: 50KB (51,200 bytes)
 * 대형 데이터 요청: 100KB (102,400 bytes) ~ 1MB (1,048,576 bytes)
 *
 * 최대 요청 본문 길이 값은 시스템의 특성과 목적에 따라 다르지만, 일반적인 기준을 고려해 보면 다음과 같다.
 *
 * 1. 일반적인 텍스트 요청 (JSON, XML)
 *     XML: 4KB (4,096 bytes) ~ 10KB (10,240 bytes)
 *     JSON: 4KB ~ 10KB
 *     JSON API 요청에서 일반적인 크기
 *     대부분의 REST API 요청 본문은 4~10KB 이내
 *     예시: {"username":"moon","age":10,"email":"moon@example.com"}
 *          (100~300 bytes 수준)
 *
 * 2. 조금 더 큰 요청 (폼 데이터 포함)
 * 	  50KB (51,200 bytes)
 *    예를 들어, 로그인, 회원가입, 게시글 작성 등에서 사용
 * 	  JSON이 아닌, HTML 폼 데이터를 포함하는 경우
 *
 * 3. 대형 데이터 요청 (첨부 파일 제외)
 *    100KB (102,400 bytes) ~ 1MB (1,048,576 bytes)
 *    로그 전송, 상세한 JSON 객체 요청
 *
 *    클라이언트에서 텍스트 데이터를 많이 포함하는 경우
 *    하지만, 메모리 부담이 크기 때문에 파일/DB 저장 방식을 고려해야 함
 * </pre>
 **/
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestUtils {

	public static final DataSize MAX_BODY_LENGTH = DataSize.ofKilobytes(50);
	private static final Charset DEFAULT_CHARACTER_ENCODING = StandardCharsets.ISO_8859_1;

	public static String getRequestMethod(HttpServletRequest request) {
		return request.getMethod();
	}

	public static String getRequestBody(ContentCachingRequestWrapper request) {
		byte[] content = request.getContentAsByteArray();
		if (content.length > 0) {
			Charset charset = getCharset(request);
			String body = new String(content, charset);
			int maxSize = (int) MAX_BODY_LENGTH.toBytes();
			return body.length() > maxSize ? body.substring(0, maxSize) + "..." : body;
		}

		return null;
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

	public static String getRequestedSessionId(HttpServletRequest request) {
		return request.getRequestedSessionId();
	}
}

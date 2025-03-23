package com.rsupport.rv5x.springaccesslog.global.utils;

import io.restassured.internal.util.IOUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Slf4j
class RequestUtilsTest {

	private final HttpServletRequest request = getServletRequest();

	private HttpServletRequest getServletRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRemoteHost("localhost");
		request.setRequestURI("/v2/user");
		request.setRemotePort(8080);
		request.setCookies(new Cookie("session", "abc123"));

		request.setMethod(HttpMethod.GET.name());
		request.addHeader("User-Agent", "JUnit");
		request.setContentType(MediaType.APPLICATION_JSON_VALUE);
		request.setContent("{\"username\":\"gmoon\"}".getBytes());
		request.setQueryString("id=123");
		request.setParameter("id", "123");

		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		try {
			IOUtils.toByteArray(wrappedRequest.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return wrappedRequest;
	}

	@Test
	void getRequestBody() {
		Assertions.assertThat(RequestUtils.getRequestBody(request)).isEqualTo("{\"username\":\"gmoon\"}");
		Assertions.assertThat(RequestUtils.getRequestBody(request)).isEqualTo("{\"username\":\"gmoon\"}");
		Assertions.assertThat(RequestUtils.getRequestBody(request)).isEqualTo("{\"username\":\"gmoon\"}");
	}

	@Test
	void getRequestQueryString() {
		Assertions.assertThat(RequestUtils.getRequestQueryString(request)).isEqualTo("id=123");
	}

	@Test
	void getRequestMethod() {
		Assertions.assertThat(RequestUtils.getRequestMethod(request)).isEqualTo("GET");
	}

	@Test
	void getRequestUri() {
		Assertions.assertThat(RequestUtils.getRequestUri(request)).isEqualTo("/v2/user");
	}

	@Test
	void getRemoteIp() {
		Assertions.assertThat(RequestUtils.getRemoteIp(request)).isEqualTo("127.0.0.1");
	}

	@Test
	void getRequestHeaders() {
		Assertions.assertThat(RequestUtils.getRequestHeaders(request)).containsEntry("User-Agent", "JUnit");
	}

	@Test
	void getHeader() {
		Assertions.assertThat(RequestUtils.getHeader(request, "User-Agent")).isEqualTo("JUnit");
	}

	@Test
	void getQueryParams() {
		Assertions.assertThat(RequestUtils.getQueryParams(request)).containsKey("id");
		Assertions.assertThat(RequestUtils.getQueryParams(request).get("id")).containsExactly("123");
	}

	@Test
	void getQueryParam() {
		Assertions.assertThat(RequestUtils.getQueryParam(request, "id")).isEqualTo("123");
	}

	@Test
	void getCookies() {
		Assertions.assertThat(RequestUtils.getCookies(request)).containsEntry("session", "abc123");
	}

	@Test
	void getCookie() {
		Assertions.assertThat(RequestUtils.getCookie(request, "session")).isEqualTo("abc123");
	}

	@Test
	void getScheme() {
		Assertions.assertThat(RequestUtils.getScheme(request)).isEqualTo("http");
	}

	@Test
	void getProtocol() {
		Assertions.assertThat(RequestUtils.getProtocol(request)).isEqualTo("HTTP/1.1");
	}

	@Test
	void getRemoteHost() {
		Assertions.assertThat(RequestUtils.getRemoteHost(request)).isEqualTo("localhost");
	}

	@Test
	void getRemotePort() {
		Assertions.assertThat(RequestUtils.getRemotePort(request)).isEqualTo(8080);
	}
}

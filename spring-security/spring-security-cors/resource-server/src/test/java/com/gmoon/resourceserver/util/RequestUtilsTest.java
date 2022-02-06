package com.gmoon.resourceserver.util;

import static org.assertj.core.api.Assertions.*;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.gmoon.resourceserver.constants.HttpHeaders;

class RequestUtilsTest {
	final String CLIENT_ORIGIN = "https://gmoon.github.io:443";

	@Test
	@DisplayName("클라이언트 IP 주소를 반환한다."
		+ " X-Forwarded-For Http Header에 담긴 요청된 IP 값 중 "
		+ " 최초 클라이언트의 IP 주소(가장 왼쪽)를 반환한다.")
	void testGetClientIpAddress() {
		// given
		HttpServletRequest request = getHttpServletRequest();

		// when
		String actual = RequestUtils.getClientIpAddress(request);

		// then
		assertThat(actual).isEqualTo(CLIENT_ORIGIN);
	}

	@Test
	@DisplayName("Origin의 schema를 반환한다.")
	void testGetOriginSchema() {
		// given
		HttpServletRequest request = getHttpServletRequest();

		// when
		String schema = RequestUtils.getOriginSchema(request);

		// then
		assertThat(schema).isEqualTo("https");
	}

	@Test
	@DisplayName("Origin의 host를 반환한다.")
	void testGetOriginHost() {
		// given
		HttpServletRequest request = getHttpServletRequest();

		// when
		String host = RequestUtils.getOriginHost(request);

		// then
		assertThat(host).isEqualTo("gmoon.github.io");
	}

	@Test
	@DisplayName("Origin의 port를 반환한다.")
	void testGetOriginPort() {
		// given
		HttpServletRequest request = getHttpServletRequest();

		// when
		Integer port = RequestUtils.getOriginPort(request);

		// then
		assertThat(port).isEqualTo(443);
	}

	private MockHttpServletRequest getHttpServletRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.ORIGIN, CLIENT_ORIGIN);
		request.addHeader(HttpHeaders.X_FORWARDED_FOR, CLIENT_ORIGIN + ",proxy1,proxy2");
		return request;
	}
}

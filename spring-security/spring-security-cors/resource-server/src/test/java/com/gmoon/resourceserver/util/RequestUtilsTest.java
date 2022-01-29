package com.gmoon.resourceserver.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import constants.HttpHeaders;

class RequestUtilsTest {
	@Test
	@DisplayName("클라이언트 IP 주소를 반환한다."
		+ " X-Forwarded-For Http Header에 담긴 요청된 IP 값 중 "
		+ " 최초 클라이언트의 IP 주소(가장 왼쪽)를 반환한다.")
	void testGetClientIpAddress() {
		// given
		String clientIp = "190.0.10.1";

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.X_FORWARDED_FOR, clientIp + ",proxy1,proxy2");

		// when
		String actual = RequestUtils.getClientIpAddress(request);

		// then
		assertThat(actual).isEqualTo(clientIp);
	}
}

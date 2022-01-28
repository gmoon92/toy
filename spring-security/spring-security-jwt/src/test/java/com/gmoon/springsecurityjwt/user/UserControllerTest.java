package com.gmoon.springsecurityjwt.user;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import com.gmoon.springsecurityjwt.base.BaseSpringBootTest;
import com.gmoon.springsecurityjwt.jwt.JwtAuthenticationFilter;

class UserControllerTest extends BaseSpringBootTest {
	@Test
	@DisplayName("로그인 성공 후, 응답 헤더에 JWT 토큰을 포함한다.")
	void testLogin() throws Exception{
		// when
		ResultActions result = mockMvc.perform(formLogin()
			.user("admin")
			.password("123"));

		// then
		result.andExpect(status().isOk());
		result.andExpect(header().exists(JwtAuthenticationFilter.HEADER_NAME));
	}

	@Test
	@DisplayName("Stateless 환경, NullSecurityContextRepository 를 사용하기 때문에 폼 로그인 인증할 수 없다.")
	void testLogin_isNotUsedHttpSession() throws Exception {
		// when
		ResultActions result = mockMvc.perform(formLogin()
			.user("admin")
			.password("123"));

		// then
		result.andExpect(status().isOk());
		result.andExpect(unauthenticated());
	}

	@Test
	@DisplayName("인증되지 않는 사용자에 대해선 Unauthorized(401) 에러가 발생한다.")
	void testLogin_unauthorized() throws Exception {
		// when
		ResultActions result = mockMvc.perform(formLogin()
			.user("admin")
			.password(RandomStringUtils.randomAlphanumeric(10)));

		// then
		result.andExpect(status().is4xxClientError());
		result.andExpect(status().isUnauthorized());
	}
}

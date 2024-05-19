package com.gmoon.springsecurityjwt.team;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import javax.persistence.EntityNotFoundException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.gmoon.springsecurityjwt.base.BaseSpringBootTest;
import com.gmoon.springsecurityjwt.jwt.JwtAuthenticationFilter;
import com.gmoon.springsecurityjwt.jwt.JwtUtil;
import com.gmoon.springsecurityjwt.user.User;
import com.gmoon.springsecurityjwt.user.UserRepository;

class TeamControllerTest extends BaseSpringBootTest {
	@Autowired
	UserRepository userRepository;
	@Autowired
	JwtUtil jwtUtil;

	final long WEB_TEAM_ID = 0;
	final String URL_OF_TEAM = "/team/" + WEB_TEAM_ID;

	@Test
	@DisplayName("팀을 조회한다.")
	void testGet() throws Exception {
		// given
		User admin = getUserOrElseThrow("admin");

		// when
		ResultActions result = verify(get(URL_OF_TEAM), admin);

		// then
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").value(WEB_TEAM_ID));
	}

	@Test
	@DisplayName("팀을 삭제한다.")
	void testDelete() throws Exception {
		// given
		User admin = getUserOrElseThrow("admin");

		// when
		ResultActions result = verify(delete(URL_OF_TEAM), admin);

		// then
		result.andExpect(status().isOk());
	}

	@Test
	@DisplayName("팀 삭제는 어드민 계정만 가능하다. "
		 + "권한이 없는 계정이 접근할 경우 Forbidden(403) 에러가 발생한다.")
	void testDelete_forbidden() throws Exception {
		// given
		User user = getUserOrElseThrow("user1");

		// when
		ResultActions result = verify(delete(URL_OF_TEAM), user);

		// then
		result.andExpect(status().is4xxClientError());
		result.andExpect(status().isForbidden());
	}

	private User getUserOrElseThrow(String username) {
		return userRepository.findByUsername(username)
			 .orElseThrow(EntityNotFoundException::new);
	}

	private ResultActions verify(MockHttpServletRequestBuilder builder, User loginUser) throws Exception {
		String token = jwtUtil.generate(loginUser);

		return mockMvc.perform(builder
			 .header(JwtAuthenticationFilter.HEADER_NAME, token)
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON));
	}
}

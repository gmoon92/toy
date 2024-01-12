package com.gmoon.springintegrationamqp.users.user.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.gmoon.javacore.util.JacksonUtils;
import com.gmoon.springintegrationamqp.users.user.domain.Role;
import com.gmoon.springintegrationamqp.users.user.domain.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class UserRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void signup() throws Exception {
		User user = User.builder()
			.email("gmoon0929@gmail.com")
			.username("gmoon")
			.password("@password")
			.role(Role.ADMIN)
			.build();

		ResultActions result = mockMvc.perform(post("/user/signup")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.content(JacksonUtils.toString(user))
		);

		result.andExpect(status().isOk());
	}
}

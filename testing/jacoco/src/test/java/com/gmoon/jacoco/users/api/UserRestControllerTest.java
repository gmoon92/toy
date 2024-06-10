package com.gmoon.jacoco.users.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserRestControllerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp(@Autowired WebApplicationContext context) {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .build();
	}

	@Test
	@WithMockUser
	void updatePassword() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/users/password")
			 .param("password", "password")
			 .param("newPassword", "newPassord")
		);

		result.andExpect(status().isNoContent());
	}
}

package com.gmoon.jacoco.users.api;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gmoon.jacoco.global.CustomWithMockUser;
import com.gmoon.jacoco.users.domain.Role;

@SpringBootTest
class UserRestControllerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp(@Autowired WebApplicationContext context) {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .apply(springSecurity())
			 .build();
	}

	@DisplayName("/users/password")
	@Nested
	class UpdatePasswordTest {

		@CustomWithMockUser(value = "admin", role = Role.ADMIN)
		@Test
		void ok() throws Exception {
			ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/users/password")
				 .param("password", "password")
				 .param("newPassword", "newPassword")
			);

			result.andExpect(status().isNoContent());
		}

		@CustomWithMockUser(value = "user", role = Role.USER)
		@Test
		void deny() throws Exception {
			ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post("/users/password")
				 .param("password", "password")
				 .param("newPassword", "newPassword")
			);

			result.andExpect(status().isForbidden());
		}
	}
}

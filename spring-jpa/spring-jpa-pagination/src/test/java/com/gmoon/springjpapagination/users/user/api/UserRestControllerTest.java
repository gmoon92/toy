package com.gmoon.springjpapagination.users.user.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class UserRestControllerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp(@Autowired WebApplicationContext context) {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .alwaysDo(print())
			 .build();
	}

	@Test
	void findAll() throws Exception {
		ResultActions result = mockMvc.perform(get("/api/users")
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON)
			 .param("page", "1")
			 .param("size", "10")
		);

		result.andExpect(status().isOk());
		result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}
}

package com.gmoon.restassured.user;

import static org.hamcrest.CoreMatchers.*;
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
class UserControllerMockMvcTest {

	@Autowired
	WebApplicationContext context;

	MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .alwaysDo(print())
			 .build();
	}

	@Test
	void testFindAll() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/user/list")
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.*.name").value(hasItems("gmoon", "guest")));
		result.andExpect(jsonPath("$.[0].name").value("gmoon"));
		result.andExpect(jsonPath("$.[0].enabled").exists());
	}

	@Test
	void testGet() throws Exception {
		// given
		String username = "gmoon";

		// when
		ResultActions result = mockMvc.perform(get("/user")
			 .param("username", username)
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.name").value(username));
		result.andExpect(jsonPath("$.enabled").isBoolean());
	}

	@Test
	void testSave() throws Exception {
		// given
		String username = "newbie";

		// when
		ResultActions result = mockMvc.perform(post("/user/" + username)
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.name").value(username));
	}

	@Test
	void testDelete() throws Exception {
		// when
		ResultActions result = mockMvc.perform(delete("/user/guest")
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isNoContent());
	}
}

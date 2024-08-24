package com.gmoon.springjpapagination.users.user.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class UserRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void userContent() throws Exception {
		ResultActions result = mockMvc.perform(get("/api/users/content")
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON)
			 .param("page", "1")
			 .param("size", "10")
		);

		result.andExpect(status().isOk());
		result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	void findAll() throws Exception {
		ResultActions result = mockMvc.perform(get("/api/users")
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON)
			 .param("groupId", "ug000")
			 .param("page", "1")
			 .param("size", "10")
		);

		result.andExpect(status().isOk());
		result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
		result.andExpect(jsonPath("$.content").hasJsonPath());
		result.andExpect(jsonPath("$.page").hasJsonPath());
		result.andExpect(jsonPath("$.['page']['size']").exists());
		result.andExpect(jsonPath("$.['page']['number']").exists());
		result.andExpect(jsonPath("$.['page']['totalElements']").exists());
		result.andExpect(jsonPath("$.['page']['totalPages']").exists());
	}
}

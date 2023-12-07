package com.gmoon.springjpamultidatasource.users.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.gmoon.springjpamultidatasource.global.AbstractIntegrationTest;
import com.gmoon.springjpamultidatasource.global.Fixtures;

class GroupUserControllerTest extends AbstractIntegrationTest {

	@Test
	void find() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/group/user")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.param("groupId", Fixtures.GROUP_ID)
		);

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("*").exists());
	}

	@Test
	void delete() throws Exception {
		String groupId = Fixtures.GROUP_ID;
		String userId = Fixtures.USER_ID;

		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/group/user")
			.param("groupId", groupId)
			.param("userId", userId)
		);

		result.andExpect(status().isOk());
	}
}

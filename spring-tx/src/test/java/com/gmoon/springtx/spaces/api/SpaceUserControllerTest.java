package com.gmoon.springtx.spaces.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springtx.global.BaseIntegrationTest;
import com.gmoon.springtx.global.Fixtures;

class SpaceUserControllerTest extends BaseIntegrationTest {

	@Transactional
	@Test
	void delete() throws Exception {
		String groupId = Fixtures.SPACE_ID;
		String userId = Fixtures.USER_ID;

		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/space/user")
			 .param("spaceId", groupId)
			 .param("userId", userId)
		);

		result.andExpect(status().isOk());
	}
}

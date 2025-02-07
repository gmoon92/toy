package com.gmoon.payment.global.servicecheck;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.gmoon.payment.test.IntegrationTestCase;

@IntegrationTestCase
class VersionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void test() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/version.txt")
			 .contentType(MediaType.TEXT_PLAIN_VALUE));

		result.andExpect(MockMvcResultMatchers.status().isOk());
	}
}

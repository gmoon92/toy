package com.gmoon.timesorteduniqueidentifier.users.user.adapter.in.web;

import com.gmoon.javacore.util.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void view() throws Exception {
		ResultActions result = mockMvc.perform(get("/user")
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON)
		);

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.*.id").hasJsonPath());
		result.andExpect(jsonPath("$.*.username").hasJsonPath());
		result.andExpect(jsonPath("$.*.createdTime").hasJsonPath());
	}

	@Test
	void detail() throws Exception {
		ResultActions result = mockMvc.perform(get("/user/user01")
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON)
		);

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").hasJsonPath());
		result.andExpect(jsonPath("$.username").hasJsonPath());
		result.andExpect(jsonPath("$.createdTime").hasJsonPath());
	}

	@Test
	void updatePassword() throws Exception {
		ResultActions result = mockMvc.perform(put("/user/password/user01")
			 .accept(MediaType.APPLICATION_JSON)
			 .contentType(MediaType.APPLICATION_JSON)
			 .content(StringUtils.randomAlphabetic(8))
		);

		result.andExpect(status().isNoContent());
	}
}

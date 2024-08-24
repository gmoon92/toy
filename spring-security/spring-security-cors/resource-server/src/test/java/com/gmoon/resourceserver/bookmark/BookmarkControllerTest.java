package com.gmoon.resourceserver.bookmark;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import com.gmoon.resourceserver.test.BaseIntegrationTest;

class BookmarkControllerTest extends BaseIntegrationTest {
	private final String bookmarkName = "gmoon92.github.io";

	@Test
	@DisplayName("지정된 이름으로 북마크를 찾는다")
	void testGet() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/bookmark/" + bookmarkName)
			 .header(HttpHeaders.AUTHORIZATION, jwt)
			 .accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.name").value(bookmarkName));
	}

	@Test
	@DisplayName("북마크를 저장한다")
	void testSave() throws Exception {
		// given
		String bookmarkName = "new bookmark! " + Instant.now();

		// when
		ResultActions result = mockMvc.perform(post("/bookmark/" + bookmarkName)
			 .header(HttpHeaders.AUTHORIZATION, jwt)
			 .accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.name").value(bookmarkName));
	}

	@Test
	@DisplayName("북마크를 삭제한다")
	void testRemove() throws Exception {
		// when
		ResultActions result = mockMvc.perform(delete("/bookmark/" + bookmarkName)
			 .header(HttpHeaders.AUTHORIZATION, jwt)
			 .accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isNoContent());
	}
}

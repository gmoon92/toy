package com.gmoon.resourceserver.bookmark;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gmoon.resourceserver.test.SampleData;

@SpringBootTest
class BookmarkControllerTest {
	@Autowired WebApplicationContext context;

	MockMvc mockMvc;
	String bookmarkName;

	@BeforeEach
	void setUp() {
		bookmarkName = "gmoon92.github.io";

		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			.alwaysDo(print())
			.apply(springSecurity())
			.build();
	}

	@Test
	@DisplayName("지정된 이름으로 북마크를 찾는다")
	void testGet() throws Exception {
		// when
		ResultActions result = mockMvc.perform(get("/bookmark/" + bookmarkName)
			.header(HttpHeaders.AUTHORIZATION, SampleData.Token.ADMIN)
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.name").value(bookmarkName));
	}

	@Test
	@DisplayName("북마크를 저장한다")
	void testSave() throws Exception {
		// given
		String bookmarkName = "new bookmark! " + LocalDateTime.now();

		// when
		ResultActions result = mockMvc.perform(post("/bookmark/" + bookmarkName)
			.header(HttpHeaders.AUTHORIZATION, SampleData.Token.ADMIN)
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
			.header(HttpHeaders.AUTHORIZATION, SampleData.Token.ADMIN)
			.accept(MediaType.APPLICATION_JSON));

		// then
		result.andExpect(status().isNoContent());
	}
}

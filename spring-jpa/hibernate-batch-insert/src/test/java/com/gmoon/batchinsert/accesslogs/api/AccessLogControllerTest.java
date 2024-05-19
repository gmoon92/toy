package com.gmoon.batchinsert.accesslogs.api;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccessLogControllerTest {

	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;

	@BeforeEach
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .build();
	}

	@Disabled("build error.")
	@Test
	void download() throws Exception {
		// given
		String attemptDt = "20220201";

		// when
		ResultActions result = mockMvc.perform(get("/accesslog/download")
			 .queryParam("attemptDt", attemptDt));

		// then
		result.andExpectAll(status().isOk(),
			 header().exists(HttpHeaders.CONTENT_LENGTH),
			 header().string(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.APPLICATION_OCTET_STREAM_VALUE)),
			 header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")),
			 header().string(HttpHeaders.CONTENT_DISPOSITION, containsString(".xlsx")));
	}
}

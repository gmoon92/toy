package com.gmoon.dbcleaner.movies.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MovieControllerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp(@Autowired WebApplicationContext context) {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .alwaysDo(print())
			 .build();
	}

	@Test
	void findAllTicketOffices() throws Exception {
		ResultActions result = mockMvc.perform(
			 get("/movie/ticketoffices")
				  .accept(MediaType.APPLICATION_JSON)
				  .param("movieId", "1")
				  .param("page", "1")
				  .param("size", "10")
		);

		result.andExpect(status().isOk());
	}
}

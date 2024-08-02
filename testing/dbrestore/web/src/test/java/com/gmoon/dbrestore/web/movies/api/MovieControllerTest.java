package com.gmoon.dbrestore.web.movies.api;

import com.gmoon.dbrestore.test.dbrestore.annotation.IntegrationTest;
import com.gmoon.dbrestore.web.movies.dto.CouponRequestVO;
import com.gmoon.javacore.util.JacksonUtils;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
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

	@Test
	void issueCoupon() throws Exception {
		CouponRequestVO requestVO = new CouponRequestVO();
		requestVO.setOfficeId(1L);
		requestVO.setMovieId(1L);

		ResultActions result = mockMvc.perform(
			 MockMvcRequestBuilders.post("/movie/coupon")
				  .contentType(MediaType.APPLICATION_JSON)
				  .accept(MediaType.APPLICATION_JSON)
				  .content(JacksonUtils.toString(requestVO))
		);

		result.andExpect(status().isOk());
	}

	@Test
	void error() throws Exception {
		CouponRequestVO requestVO = new CouponRequestVO();
		requestVO.setOfficeId(1L);
		requestVO.setMovieId(1L);

		ResultActions result = mockMvc.perform(
			 MockMvcRequestBuilders.post("/movie/error")
				  .contentType(MediaType.APPLICATION_JSON)
				  .accept(MediaType.APPLICATION_JSON)
				  .content(JacksonUtils.toString(requestVO))
		);

		result.andExpect(status().is5xxServerError());
		result.andExpect(response -> assertTrue(response.getResolvedException() instanceof LazyInitializationException));
	}

	@Test
	void delete() throws Exception {
		ResultActions result = mockMvc.perform(
			 MockMvcRequestBuilders.delete("/movie")
				  .contentType(MediaType.APPLICATION_JSON)
				  .accept(MediaType.APPLICATION_JSON)
				  .param("movieId", "1")
		);

		result.andExpect(status().isNoContent());
	}
}

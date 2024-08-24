package com.gmoon.dbrestore.web.movies.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.gmoon.dbrestore.test.dbrestore.annotation.IntegrationTest;
import com.gmoon.dbrestore.web.movies.dto.CouponRequestVO;
import com.gmoon.javacore.util.JacksonUtils;

@IntegrationTest
class MovieControllerTest {

	@Autowired
	private MockMvc mockMvc;

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

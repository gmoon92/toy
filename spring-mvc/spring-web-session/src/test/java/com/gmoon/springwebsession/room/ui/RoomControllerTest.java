package com.gmoon.springwebsession.room.ui;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.gmoon.springwebsession.room.domain.Room;

@SpringBootTest
class RoomControllerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp(WebApplicationContext context) {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
			 .alwaysDo(print())
			 .build();
	}

	@Test
	void index() throws Exception {
		ResultActions result = mockMvc.perform(get("/room")
			 .accept(MediaType.TEXT_HTML));

		result.andExpect(status().isOk());
		result.andExpect(model().attributeExists("rooms"));
		result.andExpect(MockMvcResultMatchers.view().name("room/index"));
	}

	@Test
	void view() throws Exception {
		String roomName = "A";

		ResultActions result = mockMvc.perform(get("/room/" + roomName)
			 .accept(MediaType.APPLICATION_FORM_URLENCODED)
			 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
		);

		result.andExpect(status().isOk());
		result.andExpect(model().attributeExists("room"));
		MvcResult mvcResult = result.andReturn();
		Room room = (Room)mvcResult.getModelAndView().getModel().get("room");
		assertThat(room.getName()).isEqualTo(roomName);
	}

	@Test
	void save() throws Exception {
		ResultActions result = mockMvc.perform(post("/room")
			 .sessionAttr("room", Room.create("Z-909", 1000l))
			 .accept(MediaType.APPLICATION_FORM_URLENCODED)
			 .contentType(MediaType.APPLICATION_FORM_URLENCODED)
		);

		result.andExpect(status().is3xxRedirection());
	}

	@Test
	void delete() throws Exception {
		ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/room/B"));

		result.andExpect(status().isNoContent());
	}
}

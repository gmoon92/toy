package com.gmoon.restassured.user;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

	@MockBean
	UserService service;

	@BeforeAll
	static void beforeAll(@Autowired MockMvc mockMvc) {
		RestAssuredMockMvc.mockMvc(mockMvc);
	}

	@Test
	void testFindAll() {
		Mockito.when(service.findAll())
			.thenReturn(Arrays.asList(User.from("gmoon"), User.from("guest")));

		//@formatter:off
		RestAssuredMockMvc
			.given()
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
			.when()
				.get("/user/list")
			.then()
				.log().all()
				.statusCode(HttpStatus.OK.value())
				.body("name", hasItems("gmoon", "guest"))
				.body("[0].name", equalTo("gmoon"))
				.body("[0].enabled", notNullValue());
		//@formatter:on
	}

	@Test
	void testGet() {
		String username = "gmoon";
		Mockito.when(service.find(anyString()))
			.thenReturn(User.from(username));

		//@formatter:off
		RestAssuredMockMvc
			.given()
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
				.param("username", username)
			.when()
				.get("/user")
			.then()
				.log().all()
				.statusCode(HttpStatus.OK.value())
				.body("name", equalTo(username))
				.body("enabled", instanceOf(Boolean.TYPE));
		//@formatter:on
	}

	@Test
	void testSave() {
		//@formatter:off
		RestAssuredMockMvc
			.given()
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
			.when()
				.post("/user/newbie")
			.then()
				.log().all()
				.statusCode(HttpStatus.CREATED.value());
		//@formatter:on
	}

	@Test
	void testDelete() {
		//@formatter:off
		RestAssuredMockMvc
			.given()
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
			.when()
				.delete("/user/newbie")
			.then()
				.log().all()
				.statusCode(HttpStatus.NO_CONTENT.value());
		//@formatter:on
	}
}

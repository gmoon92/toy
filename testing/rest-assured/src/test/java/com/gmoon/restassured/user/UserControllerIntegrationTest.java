package com.gmoon.restassured.user;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

	@BeforeAll
	static void beforeAll(@LocalServerPort int port) {
		RestAssured.port = port;
	}

	@Test
	void testFindAll() {
		//@formatter:off
		RestAssured
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
		// given
		RequestSpecification spec = RestAssured.given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.param("username", "gmoon");

		// when
		Response response = RestAssured.given(spec)
			.when().get("/user")
			.thenReturn();

		// then
		//@formatter:off
		response.then()
			.log().all()
			.assertThat()
				.statusCode(HttpStatus.OK.value())
				.body("name", equalTo("gmoon"))
				.body("enabled", instanceOf(Boolean.class));
		//@formatter:on
	}

	@Test
	void testGet_with_assertj() {
		// given
		User user = User.from("gmoon");
		String username = user.getName();

		// when
		//@formatter:off
		User result = RestAssured
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
				.body("enabled", instanceOf(Boolean.class))
			.extract()
				.as(User.class);
		//@formatter:on

		// then
		assertThat(result).isEqualTo(user);
	}

	@Test
	void testSave() {
		//@formatter:off
		RestAssured
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
		RestAssured
			.given()
				.accept(ContentType.JSON)
				.contentType(ContentType.JSON)
			.when()
				.delete("/user/guest")
			.then()
				.log().all()
				.statusCode(HttpStatus.NO_CONTENT.value());
		//@formatter:on
	}
}

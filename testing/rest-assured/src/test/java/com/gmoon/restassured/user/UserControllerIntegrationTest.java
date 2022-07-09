package com.gmoon.restassured.user;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

	@BeforeAll
	static void beforeAll(@LocalServerPort int port) {
		RestAssured.port = port;
	}

	@Order(Integer.MIN_VALUE)
	@Test
	void testSave() {
		save("gmoon");
		save("guest");
	}

	private void save(String username) {
		//@formatter:off
		RestAssured
			.given()
			.accept(ContentType.JSON)
			.contentType(ContentType.JSON)
			.when()
			.post("/user/" + username)
			.then()
			.log().all()
			.statusCode(HttpStatus.CREATED.value());
		//@formatter:on
	}

	@Order(2)
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

	@Order(3)
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

	@Order(4)
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

	@Order(Integer.MAX_VALUE)
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

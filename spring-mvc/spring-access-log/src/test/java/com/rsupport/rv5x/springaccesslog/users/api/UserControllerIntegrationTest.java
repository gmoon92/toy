package com.rsupport.rv5x.springaccesslog.users.api;

import com.rsupport.rv5x.springaccesslog.users.model.UserForm;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class UserControllerIntegrationTest {

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.basePath = "/user";
		RestAssured.port = 8080;
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@DisplayName("사용자 목록을 요청하고, 사용자 정보를 수정 후 조회하는 시나리오")
	@Test
	void test() {
		var authCookie = 사용자_폼_로그인("admin", "123");
		사용자_목록을_요청한다(authCookie);

		var location = 사용자를_저장한다(authCookie, new UserForm("moon", 10))
			 .response()
			 .header(HttpHeaders.LOCATION);

		var id = getQueryParam(location, "id");
		사용자_정보를_요청한다(authCookie, id)
			 .statusCode(HttpStatus.OK.value())
			 .body(Matchers.containsString("<p><strong>나이:</strong> <span>10</span></p>"));

		사용자_정보를_수정한다(authCookie, id, new UserForm("moon", 20));
		사용자_정보를_요청한다(authCookie, id)
			 .statusCode(HttpStatus.OK.value())
			 .body(Matchers.containsString("<p><strong>나이:</strong> <span>20</span></p>"));

		사용자를_삭제한다(authCookie, id);
		사용자_정보를_요청한다(authCookie, id)
			 .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	private Cookie 사용자_폼_로그인(String username, String password) {
		//@formatter:off
		ExtractableResponse<Response> extract = RestAssured
			 .given()
			 .redirects().follow(false)
			 .accept(ContentType.URLENC)
			 .contentType(ContentType.URLENC)
			 .param("username", username)
			 .param("password", password)
			 .when()
			 .post("login")
			 .then()
			 .statusCode(HttpStatus.FOUND.value())
			 .extract();
		//@formatter:on
		return extract.detailedCookie("JSESSIONID");
	}

	private ExtractableResponse<Response> 사용자_목록을_요청한다(Cookie authCookie) {
		//@formatter:off
		return RestAssured
			 .given()
			 	.cookie(authCookie)
			 	.accept(ContentType.HTML)
			 	.contentType(ContentType.HTML)
			 .when()
			 	.get()
			 .then()
			 	.contentType(ContentType.HTML)
			 	.body(Matchers.containsString("사용자 목록"))
			 	.statusCode(HttpStatus.OK.value())
			 .extract();
		//@formatter:on
	}

	private void 사용자_정보를_수정한다(Cookie authCookie, String id, UserForm form) {
		//@formatter:off
		RestAssured
			 .given()
			 	.cookie(authCookie)
			 	.accept(ContentType.JSON)
			 	.contentType(ContentType.JSON)
			 	.param("id", id)
			 	.body(form)
			 .when()
			 	.patch()
			 .then()
			 	.statusCode(HttpStatus.NO_CONTENT.value());
		//@formatter:on
	}

	private ValidatableResponse 사용자_정보를_요청한다(Cookie authCookie, String id) {
		//@formatter:off
		return RestAssured
			 .given()
			 	.cookie(authCookie)
			 	.accept(ContentType.HTML)
			 	.contentType(ContentType.HTML)
			 	.param("id", id)
			 .when()
			 	.get("view")
			 .then()
			 	.contentType(ContentType.HTML);
		//@formatter:on
	}

	private String getQueryParam(String url, String param) {
		return UriComponentsBuilder.fromUriString(url)
			 .build()
			 .getQueryParams()
			 .getFirst(param);
	}

	private ExtractableResponse<Response> 사용자를_저장한다(Cookie authCookie, UserForm userForm) {
		//@formatter:off
		return RestAssured
			 .given()
			 	.cookie(authCookie)
			 	.accept(ContentType.TEXT)
			 	.contentType(ContentType.URLENC)
			 	.param("username", userForm.getUsername())
			 	.param("age", userForm.getAge())
			 .when()
				 .post()
			 .then()
			 	.statusCode(HttpStatus.FOUND.value())
			 	.header(HttpHeaders.LOCATION, CoreMatchers.containsString("/user/view"))
			 .extract();
		//@formatter:on
	}

	private void 사용자를_삭제한다(Cookie authCookie, String id) {
		//@formatter:off
		RestAssured
			 .given()
			 	.cookie(authCookie)
			 	.accept(ContentType.JSON)
			 	.contentType(ContentType.JSON)
			 	.param("id", id)
			 .when()
			 	.delete()
			 .then()
			 	.statusCode(HttpStatus.NO_CONTENT.value());
		//@formatter:on
	}
}

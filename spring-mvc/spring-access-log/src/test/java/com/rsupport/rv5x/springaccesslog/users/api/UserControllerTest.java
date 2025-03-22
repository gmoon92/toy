package com.rsupport.rv5x.springaccesslog.users.api;

import com.rsupport.rv5x.springaccesslog.users.model.UserForm;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import io.restassured.response.ExtractableResponse;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class UserControllerTest {

	@BeforeEach
	void setup(@Autowired MockMvc mockMvc) {
		RestAssuredMockMvc.basePath = "/user";
		RestAssuredMockMvc.mockMvc(mockMvc);
		RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();
	}


	@DisplayName("사용자 목록을 요청하고, 사용자 정보를 수정 후 조회하는 시나리오")
	@Test
	void test() {
		var authentication = getAuthentication("gmoon", "ADMIN");
		사용자_목록을_요청한다(authentication);

		var id = 사용자를_저장한다(authentication, new UserForm("moon", 10))
			 .response()
			 .getMvcResult()
			 .getModelAndView()
			 .getModel()
			 .get("id");

		사용자_정보를_요청한다(authentication, id)
			 .assertThat(result -> {
				 ModelAndView modelAndView = result.getModelAndView();
				 ModelMap modelMap = modelAndView.getModelMap();
				 UserForm userForm = (UserForm) modelMap.get("userForm");
				 Assertions.assertThat(userForm.getAge()).isEqualTo(10);
			 });

		사용자_정보를_수정한다(authentication, id, new UserForm("moon", 20));
		사용자_정보를_요청한다(authentication, id)
			 .assertThat(result -> {
				 ModelAndView modelAndView = result.getModelAndView();
				 ModelMap modelMap = modelAndView.getModelMap();
				 UserForm userForm = (UserForm) modelMap.get("userForm");
				 Assertions.assertThat(userForm.getAge()).isEqualTo(20);
			 });

		사용자를_삭제한다(authentication, id);
	}

	private Authentication getAuthentication(String username, String... authorities) {
		User user = new User(
			 username,
			 "123",
			 AuthorityUtils.createAuthorityList(authorities)
		);
		return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
	}

	private void 사용자_목록을_요청한다(Authentication authentication) {
		//@formatter:off
		RestAssuredMockMvc
			 .given()
			 .auth().principal(authentication)
				 .accept(ContentType.HTML)
				 .contentType(ContentType.HTML)
			 .when()
				 .get()
			 .then()
				 .contentType(ContentType.HTML)
				 .status(HttpStatus.OK);
		//@formatter:on
	}

	private ExtractableResponse<MockMvcResponse> 사용자를_저장한다(Authentication authentication, UserForm userForm) {
		//@formatter:off
		return RestAssuredMockMvc
			 .given()
			 	.auth().principal(authentication)
			 	.accept(ContentType.TEXT)
			 	.contentType(ContentType.URLENC)
			 	.param("username", userForm.getUsername())
			 	.param("age", userForm.getAge())
			 .when()
			 	.post()
			 .then()
			 	.status(HttpStatus.FOUND)
			 .extract();
		//@formatter:on
	}

	private ValidatableMockMvcResponse 사용자_정보를_요청한다(Authentication authentication, Object id) {
		//@formatter:off
		return RestAssuredMockMvc
			 .given()
			 	.auth().principal(authentication)
			 	.accept(ContentType.HTML)
			 	.contentType(ContentType.HTML)
			 	.param("id", id)
			 .when()
			 	.get("view")
			 .then()
			 	.contentType(ContentType.HTML)
			 	.status(HttpStatus.OK);
		//@formatter:on
	}

	private void 사용자_정보를_수정한다(Authentication authentication, Object id, UserForm userForm) {
		//@formatter:off
		RestAssuredMockMvc
			 .given()
			 	.auth().principal(authentication)
			 	.accept(ContentType.JSON)
			 	.contentType(ContentType.JSON)
			 	.param("id", id)
			 	.body(userForm)
			 .when()
			 	.patch()
			 .then()
			 	.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}

	private void 사용자를_삭제한다(Authentication authentication, Object id) {
		//@formatter:off
		RestAssuredMockMvc
			 .given()
			 	.auth().principal(authentication)
			 	.accept(ContentType.JSON)
			 	.contentType(ContentType.JSON)
			 	.param("id", id)
			 .when()
			 	.delete()
			 .then()
			 	.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}
}

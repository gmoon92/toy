package com.rsupport.rv5x.springaccesslog.users.api;

import com.rsupport.rv5x.springaccesslog.users.model.UserForm;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.response.ExtractableResponse;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ModelMap;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class UserControllerTest {

	@BeforeEach
	void setup(@Autowired MockMvc mockMvc) {
		RestAssuredMockMvc.basePath = "/user";
		RestAssuredMockMvc.mockMvc(mockMvc);
	}

	@WithMockUser(roles = "ADMIN")
	@Test
	void index() {
		//@formatter:off
		RestAssuredMockMvc
			 .given()
				 .accept(ContentType.HTML)
				 .contentType(ContentType.HTML)
			 .when()
				 .get()
			 .then()
				 .contentType(ContentType.HTML)
				 .body(Matchers.containsString("사용자 목록"))
				 .status(HttpStatus.OK);
		//@formatter:on
	}

	@WithMockUser(roles = "ADMIN")
	@Test
	void 사용자를_저장후_상세보기_페이지로_이동한다() {
		ModelMap modelMap = 사용자를_저장한다();
		log.info("modelMap: {}", modelMap);

		사용자_상세보기_페이지(modelMap.get("id"));
	}

	private ModelMap 사용자를_저장한다() {
		UserForm userForm = new UserForm();
		userForm.setUsername("newUser01");
		userForm.setAge(99);

		//@formatter:off
		return RestAssuredMockMvc
			 .given()
			 	.accept(ContentType.TEXT)
			 	.contentType(ContentType.URLENC)
			 	.param("username", userForm.getUsername())
				 .param("age", userForm.getAge())
			 .when()
				 .post()
			 .then()
				 .status(HttpStatus.FOUND)
			 .extract()
				 .response()
				 .getMvcResult()
				 .getModelAndView()
				 .getModelMap();
		//@formatter:on
	}

	private ExtractableResponse<MockMvcResponse> 사용자_상세보기_페이지(Object id) {
		//@formatter:off
		return RestAssuredMockMvc
			 .given()
			 	.accept(ContentType.HTML)
			 	.contentType(ContentType.HTML)
			 	.param("id", id)
			 .when()
			 	.get("view")
			 .then()
			 	.log().all()
			 	.contentType(ContentType.HTML)
			 	.body(Matchers.containsString("사용자 상세보기"))
			 	.status(HttpStatus.OK)
			 .extract();
		//@formatter:on
	}

	@WithMockUser(roles = "ADMIN")
	@Test
	void update() {
		UserForm form = new UserForm();
		form.setId("user01");
		form.setUsername("newUser01");
		form.setAge(99);

		//@formatter:off
		RestAssuredMockMvc
			 .given()
			 	.accept(ContentType.JSON)
			 	.contentType(ContentType.JSON)
			 	.body(form)
			 .when()
			 	.patch()
			 .then()
			 	.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}

	@WithMockUser(roles = "ADMIN")
	@Test
	void delete() {
		ModelMap userForm = 사용자를_저장한다();

		//@formatter:off
		RestAssuredMockMvc
			 .given()
			 	.accept(ContentType.JSON)
			 	.contentType(ContentType.JSON)
			 	.param("id", userForm.get("id"))
			 .when()
			 	.delete()
			 .then()
			 	.status(HttpStatus.NO_CONTENT);
		//@formatter:on
	}
}

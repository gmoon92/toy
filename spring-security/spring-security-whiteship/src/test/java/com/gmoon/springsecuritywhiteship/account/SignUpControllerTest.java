package com.gmoon.springsecuritywhiteship.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class SignUpControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Test
  @DisplayName("회원가입 폼에 csrf 태그가 포함되는지")
  void form() throws Exception {
    mockMvc.perform(get("/signup"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("_csrf")));
  }

  @Test
  @DisplayName("회원 가입 csrf 토큰 미포함시 forbidden 403 에러 확인")
  void save_403_error_when_non_csrf_token() throws Exception {
    mockMvc.perform(post("/signup")
            .param("username", "gmoon")
            .param("password", "123123"))
            .andDo(print())
            .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("회원 가입 csrf 토큰 포함 테스트")
  void save() throws Exception {
    mockMvc.perform(post("/signup")
            .param("username", "gmoon")
            .param("password", "123123")
            .with(csrf()))
            .andDo(print())
            .andExpect(status().is3xxRedirection());
  }
}
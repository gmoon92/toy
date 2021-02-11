package com.gmoon.springsecurity.login;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Test
  void index_anonymous() throws Exception {
    mockMvc.perform(get("/"))
            .andDo(print()) // 출력 응답
            .andExpect(status().isOk());
  }

  @Test
  void index_user() throws Exception {
    mockMvc.perform(get("/").with(user("gmoon").roles("USER"))) // mock already login user
            .andDo(print())
            .andExpect(status().isOk());
  }

  @Test
  void index_admin() throws Exception {
    mockMvc.perform(get("/").with(user("admin").roles("ADMIN"))) // mock already login user
            .andDo(print())
            .andExpect(status().isOk());
  }

  @Test
  void admin_user() throws Exception {
    mockMvc.perform(get("/admin").with(user("gmoon").roles("USER")))
            .andDo(print()) // 출력 응답
            .andExpect(status().isForbidden());
  }

  @Test
  void admin_admin() throws Exception {
    mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
            .andDo(print()) // 출력 응답
            .andExpect(status().isOk());
  }

  @Test
  @WithAnonymousUser
  void index_anonymous_with_test_annotation() throws Exception {
    mockMvc.perform(get("/"))
            .andDo(print())
            .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "gmoon", roles = { "USER" })
  void index_user_with_test_annotation() throws Exception {
    mockMvc.perform(get("/"))
            .andDo(print())
            .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "admin", roles = { "ADMIN" })
  void admin_admin_with_test_annotation() throws Exception {
    mockMvc.perform(get("/admin"))
            .andDo(print())
            .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "user", roles = { "USER" })
  void admin_user_with_test_annotation() throws Exception {
    mockMvc.perform(get("/admin"))
            .andDo(print())
            .andExpect(status().isForbidden());
  }


}
package com.gmoon.springsecurity.login;

import com.gmoon.springsecurity.account.AccountService;
import com.gmoon.springsecurity.account.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  AccountService accountService;

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

  @Test
  @DisplayName("Security formLogin test")
  @Transactional
  void form_login() throws Exception {
    String username = "gmoon";
    String password = "123";

    Member member = createUser(username, password);
    mockMvc.perform(formLogin()
              .user(member.getUsername())
              .password(password))
            .andExpect(authenticated());
  }

  @Test
  @DisplayName("Security formLogin test")
  @Transactional
  void form_login2() throws Exception {
    String username = "gmoon";
    String password = "123";

    Member member = createUser(username, password);
    mockMvc.perform(formLogin()
              .user(member.getUsername())
              .password("1234"))
            .andExpect(unauthenticated());
  }

  private Member createUser(String username, String password) {
    Member member = new Member();
    member.setUsername(username);
    member.setPassword(password);
    member.setRole("USER");
    return accountService.createNew(member);
  }


}
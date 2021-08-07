package com.gmoon.springsecurity.account;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class UserControllerTest {

  @Autowired
  private WebApplicationContext context;
  private MockMvc mockMvc;

  @Autowired
  private AccountRepository accountRepository;

  @Before
  public void setUp() throws Exception {
    DelegatingFilterProxy delegatingFilterProxy = new DelegatingFilterProxy();
    delegatingFilterProxy.init(new MockFilterConfig(context.getServletContext(), BeanIds.SPRING_SECURITY_FILTER_CHAIN));
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
//            .addFilter(delegatingFilterProxy)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .alwaysDo(print())
            .build();

    SecurityContext securityContext = SecurityContextHolder.getContext();
    Account account = accountRepository.save(Account.newUser("user", "123"));

    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(account.getUsername(), account.getPassword(), account.getAuthorities());
    token.setDetails(account);
    securityContext.setAuthentication(token);

    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("Security Chain이랑 같은 환경에서 도는건지")
  public void list() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/user/list")
                    .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
  }
}
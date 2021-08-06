package com.gmoon.springsecurity.account;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.security.config.BeanIds;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class UserControllerTest {

  @Autowired
  private WebApplicationContext context;
  private MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {
    DelegatingFilterProxy delegatingFilterProxy = new DelegatingFilterProxy();
    delegatingFilterProxy.init(new MockFilterConfig(context.getServletContext(), BeanIds.SPRING_SECURITY_FILTER_CHAIN));

    mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .addFilter(delegatingFilterProxy)
            .build();
  }

  @Test
  @DisplayName("Security Chain이랑 같은 환경에서 도는건지")
  public void list() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/user/list"))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
  }
}
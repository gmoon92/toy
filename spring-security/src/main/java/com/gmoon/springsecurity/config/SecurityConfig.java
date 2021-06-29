package com.gmoon.springsecurity.config;

import com.gmoon.springsecurity.account.MemberRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  // 히어러키 커스텀 1. AccessDecisionManager
  // 복잡.....
  // 계층형 Role 관리를 위한 AccessDecisionManager 추가

  // 커스텀2 expressionHandler 정의
  // 히어러키는 voter 핸들러를 통해 설정이 가능함으로. expressionHandler를 커스텀
  public AccessDecisionManager customAccessDecisionManager() {
    // handler 생성
    SecurityExpressionHandler handler = customExpressionHandler();

    // voter 생성
    WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
    // handler를 통해서 voter 히어러키 설정
    webExpressionVoter.setExpressionHandler(handler);

    // voter 설정
    return new AffirmativeBased(Arrays.asList(webExpressionVoter));
  }

  private SecurityExpressionHandler customExpressionHandler() {
    // 히어러키 설정은 expressionHandler를 통해서만 설정 가능
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    // Admin 계정은 User 계정도 볼 수 있다.
    roleHierarchy.setHierarchy(String.format("%s > %s", MemberRole.ADMIN.getName(), MemberRole.USER.getName()));

    DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  // Role Hierarchy
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests() // [1] 인가(Authorization) 설정, 어떤 요청들을 어떻게 설정 할지
            .mvcMatchers("/", "/info", "/account/**").permitAll()
            .mvcMatchers("/admin").hasRole(MemberRole.ADMIN.getValue())
            .mvcMatchers("/user").hasRole(MemberRole.USER.getValue())
            .anyRequest().authenticated() // etc... 나머지 요청은 인증처리를 하겠다.
//            .accessDecisionManager(customAccessDecisionManager()); // 커스텀 1. accessDecisionManager
            .expressionHandler(customExpressionHandler()); // 커스텀 2. expression handler

    // [2] form login 설정
    http.formLogin();

    http.httpBasic();
//    https://www.baeldung.com/spring-security-basic-authentication
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

}

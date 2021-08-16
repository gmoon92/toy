package com.gmoon.springsecurity.config;

import com.gmoon.springsecurity.account.AccountRole;
import com.gmoon.springsecurity.account.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  final AccountService accountService;

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
    roleHierarchy.setHierarchy(String.format("%s > %s", AccountRole.ADMIN.getAuthority(), AccountRole.USER.getAuthority()));

    DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  // Role Hierarchy
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests() // [1] 인가(Authorization) 설정, 어떤 요청들을 어떻게 설정 할지
            .mvcMatchers("/", "/signup", "/info", "/account/**").permitAll()
            .mvcMatchers("/admin").hasRole(AccountRole.ADMIN.getRole())
            .mvcMatchers("/user").hasRole(AccountRole.USER.getRole())
            .antMatchers("/user/**").hasRole(AccountRole.ADMIN.getRole())
            .anyRequest().authenticated() // etc... 나머지 요청은 인증처리를 하겠다.
//            .accessDecisionManager(customAccessDecisionManager()); // 커스텀 1. accessDecisionManager
            .expressionHandler(customExpressionHandler()); // 커스텀 2. expression handler

//    https://www.baeldung.com/spring-security-custom-access-denied-page
    // ExceptionTranslationFilter
    http.exceptionHandling()
//            .accessDeniedPage("/access-denied");
        .accessDeniedHandler((request, response, accessDeniedException) -> {
          UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
          String username = user.getUsername();
          log.info("user : {} dined to access {}", username, request.getRequestURI());

          response.sendRedirect("/access-denied");
        });

    // [2] form login 설정
    http.formLogin()
            .loginPage("/login")
            .usernameParameter("id")
            .passwordParameter("password")
            .permitAll();

    http.httpBasic();
//    https://www.baeldung.com/spring-security-basic-authentication

    // [3] logout
    http.logout()
        .logoutUrl("/logout") // logout page config
        .logoutSuccessUrl("/login") // logout success event after page
//      .logoutSuccessHandler() // custom logout success handler add
//      .addLogoutHandler() // custom logout handler add
//      .deleteCookies() // cookie login auth
    ;

    // [4] anonymous config
//    http.anonymous()
//            .principal("anonymousUser")
//            .authorities()
//            .key("anonymousUserKey1");

    // Session fixation
//    http.sessionManagement()
//            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//            .sessionFixation()
////            .newSession()
////            .changeSessionId()
////            .migrateSession()
////            .none()
//              .changeSessionId()
////            .invalidSessionUrl("/login")
//            .maximumSessions(1) // 동시성 제어
//              .maxSessionsPreventsLogin(true)
//    ;

    // remember me config
    http.rememberMe()
//            .useSecureCookie(true) // https 적용 후 true 권장
            .userDetailsService(accountService)
            .key("remember-me-sample");

    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

}

package com.gmoon.springsecuritywhiteship.config;

import com.gmoon.springsecuritywhiteship.account.AccountRole;
import com.gmoon.springsecuritywhiteship.account.AccountService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;

/**
 * <pre>
 * @see WebSecurityConfigurerAdapter
 * @link {https://spring.io/guides/topicals/spring-security-architecture}
 * </pre>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(proxyTargetClass = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final AccountService accountService;

	@PostConstruct
	public void init() {
		// 	https://github.com/spring-projects/spring-security/issues/6856
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}

	/**
	 * @implNote <pre>
	 * 히어러키
	 * 커스텀 1. AccessDecisionManager
	 * 복잡.....
	 * 계층형 Role 관리를 위한 AccessDecisionManager 추가
	 *
	 * 커스텀2 expressionHandler 정의
	 * 히어러키는 voter 핸들러를 통해 설정이 가능함으로 expressionHandler 를 커스텀
	 * </pre>
	 */
	public AccessDecisionManager customAccessDecisionManager() {
		// handler 생성
		SecurityExpressionHandler<FilterInvocation> handler = customExpressionHandler();

		// voter 생성
		WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
		// handler를 통해서 voter 히어러키 설정
		webExpressionVoter.setExpressionHandler(handler);

		// voter 설정
		return new AffirmativeBased(Arrays.asList(webExpressionVoter));
	}

	/**
	 * @link https://docs.spring.io/spring-security/reference/servlet/authorization/architecture.html#_the_authorizationmanager
	 * @see SecurityConfig#roleHierarchy
	 * @see SecurityConfig#methodSecurityExpressionHandler(RoleHierarchy)
	 */
	private SecurityExpressionHandler<FilterInvocation> customExpressionHandler() {
		// 히어러키 설정은 expressionHandler를 통해서만 설정 가능
		RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
		// Admin 계정은 User 계정도 볼 수 있다.
		roleHierarchy.setHierarchy(
			 String.format("%s > %s", AccountRole.ADMIN.getAuthority(), AccountRole.USER.getAuthority()));

		DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
		handler.setRoleHierarchy(roleHierarchy);
		return handler;
	}

	@Bean
	public RoleHierarchy roleHierarchy() {
		return RoleHierarchyImpl.withDefaultRolePrefix()
			 .role(AccountRole.ADMIN.getRole()).implies(AccountRole.USER.getRole())
			 .build();
	}

	@Bean
	static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setRoleHierarchy(roleHierarchy);
		return expressionHandler;
	}

	// Role Hierarchy
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			 .authenticationManager(authenticationManager())
			 // [1] 인가(Authorization) 설정, 어떤 요청들을 어떻게 설정 할지
			 .authorizeHttpRequests(request ->
					   request
							.requestMatchers(
								 AntPathRequestMatcher.antMatcher("/"),
								 AntPathRequestMatcher.antMatcher("/signup"),
								 AntPathRequestMatcher.antMatcher("/info"),
								 AntPathRequestMatcher.antMatcher("/account/**"),
								 AntPathRequestMatcher.antMatcher("/sample/**")
							).permitAll()
							.requestMatchers(AntPathRequestMatcher.antMatcher("/admin")).hasRole(AccountRole.ADMIN.getRole())
							.requestMatchers(AntPathRequestMatcher.antMatcher("/user")).hasRole(AccountRole.USER.getRole())
							.requestMatchers(AntPathRequestMatcher.antMatcher("/user/**")).hasRole(AccountRole.ADMIN.getRole())
							.anyRequest().authenticated() // etc... 나머지 요청은 인증처리를 하겠다.
				  //            .accessDecisionManager(customAccessDecisionManager()); // 커스텀 1. accessDecisionManager
				  // .expressionHandler(customExpressionHandler()) // 커스텀 2. expression handler
				  // .anyRequest().access(customExpressionHandler())
			 )

			 // https://www.baeldung.com/spring-security-custom-access-denied-page
			 // ExceptionTranslationFilter
			 .exceptionHandling(this::exceptionHandling)

			 // [2] form login 설정
			 .formLogin(config ->
				  config.loginPage("/login")
					   .usernameParameter("id")
					   .passwordParameter("password")
					   .permitAll()
			 )

			 // [3] logout
			 .logout(config ->
					   config.logoutUrl("/logout") // logout page config
							.logoutSuccessUrl("/login") // logout success event after page
				  // .logoutSuccessHandler() // custom logout success handler add
				  // .addLogoutHandler() // custom logout handler add
				  // .deleteCookies() // cookie login auth
			 )

			 // [4] anonymous config
			 // .anonymous(config ->
			 // 	config.principal("anonymousUser")
			 // 		.authorities()
			 // 		.key("anonymousUserKey1")
			 // )

			 /**
			  * @see SessionManagementConfigurer
			  * */
			 // Session fixation
			 // .sessionManagement(config ->
			 // 	config
			 // 		.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
			 // 		.sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::changeSessionId)
			 // 		// .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::migrateSession)
			 // 		// .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::newSession)
			 // 		// .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::none)
			 // 		// .invalidSessionUrl("/login")
			 // 		.maximumSessions(1) // 동시성 제어
			 // 		.maxSessionsPreventsLogin(true)
			 // )

			 // remember me config
			 .rememberMe(config ->
				  config
					   // .useSecureCookie(true) // https 적용 후 true 권장
					   .userDetailsService(accountService)
					   .key("remember-me-sample")
			 )

			 // https://www.baeldung.com/spring-security-basic-authentication
			 .httpBasic(Customizer.withDefaults())
			 .build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	/**
	 * @implNote <pre>
	 * @link {https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html#servlet-authentication-unpwd-storage}
	 * @link {https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html#publish-authentication-manager-bean}
	 * </pre>
	 */
	@Bean
	public AuthenticationManager authenticationManager() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setPasswordEncoder(passwordEncoder());
		provider.setUserDetailsService(accountService);
		return new ProviderManager(provider);
	}

	private void exceptionHandling(ExceptionHandlingConfigurer<HttpSecurity> configurer) {
		configurer
			 // .accessDeniedPage("/access-denied")
			 .accessDeniedHandler((request, response, accessDeniedException) -> {
				 SecurityContext context = SecurityContextHolder.getContext();
				 Authentication authentication = context.getAuthentication();
				 if (authentication != null) {
					 UserDetails user = (UserDetails)authentication.getPrincipal();
					 String username = user.getUsername();
					 log.info("user : {} dined to access {}", username, request.getRequestURI());
				 }
				 response.sendRedirect("/access-denied");
			 });
	}
}

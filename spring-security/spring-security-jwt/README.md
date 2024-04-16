# Spring Security JWT

## Environment

- org.springframework.boot 2.7.14
- JDK 8
- H2 Database

## JWT Classes 

- `JwtAuthenticationFilter`: 사용자 인증 관련 필터
- `JwtVerifyFilter`: JWT 검증 필터
- `JwtUtil`: JWT 생성 및 검증 유틸성 클래스
- `AuthenticationSchema`: 인증 토큰 유형 이넘 클래스

## Spring Security Filter Chain Config

```java
@Configurable
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private final MappingJackson2HttpMessageConverter converter;
	private final JwtUtil jwtUtil;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.headers(headers -> headers.frameOptions().sameOrigin())
			.csrf().disable()
			.cors().and()
			.httpBasic().disable()
			.formLogin().disable()
			.exceptionHandling(handling -> // [1] 인증, 인가 예외 Handler
				handling.accessDeniedHandler(jwtExceptionHandler())
					.authenticationEntryPoint(jwtExceptionHandler()))
			.sessionManagement(sessionManagement -> 
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // [2] Non HTTP Session ,STATELESS 설정
			.authorizeRequests(request ->
				request
					.antMatchers("/", "**/login**").permitAll()
					.antMatchers(HttpMethod.DELETE, "**").hasRole(Role.ADMIN.name())
					.anyRequest().authenticated())
			.addFilter(new JwtAuthenticationFilter(authenticationManager(), jwtUtil)) // [3] 사용자 인증 검증 및 JWT 발급 필터
			.addFilter(new JwtVerifyFilter(jwtExceptionHandler(), jwtUtil)); // [4] JWT 검증 필터
	}

	@Bean
	public JwtExceptionHandler jwtExceptionHandler() {
		return new JwtExceptionHandler(converter);
	}
	
	// ...
}
```

## Filters

1. WebAsyncManagerIntegrationFilter
2. SecurityContextPersistenceFilter
3. HeaderWriterFilter
4. CorsFilter
5. LogoutFilter
6. **`JwtAuthenticationFilter`**
7. **`JwtVerifyFilter`**
8. RequestCacheAwareFilter
9. SecurityContextHolderAwareRequestFilter
10. AnonymousAuthenticationFilter
11. SessionManagementFilter
12. ExceptionTranslationFilter
13. FilterSecurityInterceptor

## Reference

- [GitHub - Java JWT](https://github.com/auth0/java-jwt)
- [GitHub - jjwt](https://github.com/eugenp/tutorials/blob/10f32d31bb/jjwt/README.md)
- [Open naru - JWT](http://www.opennaru.com/opennaru-blog/jwt-json-web-token/)
  - [Open naru - JWT with Microservice](http://www.opennaru.com/opennaru-blog/jwt-json-web-token-with-microservice/)
  - [JWT 자바 가이드](https://medium.com/@OutOfBedlam/jwt-%EC%9E%90%EB%B0%94-%EA%B0%80%EC%9D%B4%EB%93%9C-53ccd7b2ba10)
  - [JWT(JSON Web Token)에 대해 알아보자](https://blog.hax0r.info/2017-12-29/about-jwt-json-web-token/)
  - [JSON Web Token 소개 및 구조](https://velopert.com/2389)
- [RFC 7519 - JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
  - [RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749)
  - [RFC 6750](https://datatracker.ietf.org/doc/html/rfc6750)
  - [RFC 7515 - JSON Web Signature (JWS)](https://datatracker.ietf.org/doc/html/rfc7515)
  - [RFC 7516 - JSON Web Encryption (JWE)](https://datatracker.ietf.org/doc/html/rfc7516)
- [broadcom - JWT Authentication scheme](https://techdocs.broadcom.com/us/en/symantec-security-software/identity-security/siteminder/12-8/configuring/policy-server-configuration/authentication-schemes/json-web-token-jwt-authentication-scheme.html)
- [www.iana.org - HTTP Auth Schemes](https://www.iana.org/assignments/http-authschemes/http-authschemes.xhtml)
- [stackoverflow.com - HMAC256 vs HMAC512](https://stackoverflow.com/questions/38472926/hmac-256-vs-hmac-512-jwt-signature-encryption)
- [jwt.io](https://jwt.io/)

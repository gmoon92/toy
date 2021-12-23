# Spring Security

**`Spring Security`** 는 Spring 기반의 애플리케이션의 보안을 담당하는 프레임워크다.

또한, 다양한 웹 보안 처리와 사용자의 '인증'과 '권한'에 대한 부분을 Filter 기반으로 처리한다는 특징을 지닌다.

- 인증 및 권한 부여에 대한 포괄적이고 확장 가능한 지원
- 다양한 웹 보안 공격으로 부터 보호
  - [Session fixation](https://en.wikipedia.org/wiki/Session_fixation)
  - [Clickjacking](https://en.wikipedia.org/wiki/Clickjacking)
  - [CORS](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing)
- Servlet API Integration
- Optional integration with Spring Web MVC

# Security Architecture

Spring Security 는 Servlet 을 기반으로 동작한다.

`Servlet Filter`를 확장한 여러 Security Filter 들은 Filter Chain 을 구성하여 순차적으로 단일 HTTP 요청에 대해 처리한다.

![multi-securityfilterchain](img/multi-securityfilterchain.png)

1. Servlet Container 에 요청이 들어온다.
2. `Spring Security` 엔 **`Servlet Filter`** 의 구현체 중 하나인 **`DelegatingFilterProxy`** 가 있다.
4. DelegatingFilterProxy 는 **필터 처리를 `FilterChainProxy` 에 위임한다.**
   1. `FilterChain doFilterInternal()` 메서드 안에서 gerFilters 를 사용하여 **`Chain의 목록(SecurityFilterChain)`** 을 가져온다.
   2. 순차적으로 등록된 filter 를 실행한다.
5. HttpSecurity 를 사용하여 WebSecurity 를 만들고 WebSecurity 가 최종적으로 `springSecurityFilterChain`라는 이름으로 FilterChain 을 빈으로 등록한다.
6. **`FilterChainProxy`** 는 RequestMatcher 인터페이스를 활용하여, 호출된 HttpServletRequest 정보를 판단한다.

## Filters

- ChannelProcessingFilter
- WebAsyncManagerIntegrationFilter
- SecurityContextPersistenceFilter
- HeaderWriterFilter
- CorsFilter
- CsrfFilter
- LogoutFilter
- OAuth2AuthorizationRequestRedirectFilter
- Saml2WebSsoAuthenticationRequestFilter
- X509AuthenticationFilter
- AbstractPreAuthenticatedProcessingFilter
- CasAuthenticationFilter
- OAuth2LoginAuthenticationFilter
- Saml2WebSsoAuthenticationFilter
- UsernamePasswordAuthenticationFilter
- OpenIDAuthenticationFilter
- DefaultLoginPageGeneratingFilter
- DefaultLogoutPageGeneratingFilter
- ConcurrentSessionFilter
- DigestAuthenticationFilter
- BearerTokenAuthenticationFilter
- BasicAuthenticationFilter
- RequestCacheAwareFilter
- SecurityContextHolderAwareRequestFilter
- JaasApiIntegrationFilter
- RememberMeAuthenticationFilter 
- AnonymousAuthenticationFilter
- OAuth2AuthorizationCodeGrantFilter 
- SessionManagementFilter
- ExceptionTranslationFilter 
- FilterSecurityInterceptor
- SwitchUserFilter

# Reference

- [Spring Security Project](https://spring.io/projects/spring-security)
- [Spring Security - Reference](https://docs.spring.io/spring-security/reference/index.html)
- [Spring Security - Architecture](https://docs.spring.io/spring-security/reference/servlet/architecture.html)

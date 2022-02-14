# Security Architecture

1. Servlet Container 에 요청이 들어오면,
2. Servlet Filter 중에 DelegatingFilterProxy 가 등록이 되어 있으면
3. 필터 처리를 FilterChainProxy 에 위임한다.
4. HttpSecurity 를 사용하여 WebSecurity 를 만들고 WebSecurity 가 최종적으로 FilterChain을 만든다.

SecurityContextHolder -> SecurityContext -> Authentication
-> Principal -> GrantedAuthorities

## 인증
AuthenticationManager

-> AuthenticationManager 구현체 ProviderManager

DaoAuthenticationProvider -> UserDetailsService


## 인가

FilterSecurityInterceptor 인가 처리하는 인터셉터를 통해서 AccessDecisionManager

AccessDecisionManager -> AffirmativeBase -> AccessDecisionVoter

WebExpressionVoter -> SecurityExpressionHandler (롤 히어리키 커스텀)



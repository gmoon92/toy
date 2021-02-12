# Spring Security

# Filter와 FilterChainProxy

스프링 시큐리티가 제공하는 필터들

1. WebAsyncManagerIntegrationFilter
2. SecurityContextPersistenceFilter
3. HeaderWriterFilter
4. CsrfFilter
5. LogoutFilter
6. UsernamePasswordAuthenticationFilter
7. DefaultLoginPageGeneratingFilter
8. DefaultLogoutPageGeneratingFilter
9. BasicAuthenticationFilter
10. RequestCacheAwareFilter
11. SecurityContextHolderAwareRequestFilter
12. AnonymousAuthenticationFilter
13. SessionManagementFilter
14. ExceptionTranslationFilter
15. FilterSecurityInterceptor

이 모든 필터는 FilterChainProxy가 호출한다.

![filter-chain](./img/filter-chain.png)

`FilterChainProxy.class`

1. doFilterInternal 메서드 안에서 gerFilters 를 사용하여 chain의 목록을 가져온다.
2. 순차적으로 filter를 실행한다.

[Spring security - servlet filter chain proxy](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#servlet-filterchainproxy)

```java
public class FilterChainProxy extends GenericFilterBean {

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    boolean clearContext = request.getAttribute(FILTER_APPLIED) == null;
    if (!clearContext) {
      doFilterInternal(request, response, chain);
      return;
    }
    try {
      request.setAttribute(FILTER_APPLIED, Boolean.TRUE);
      doFilterInternal(request, response, chain);
    } catch (RequestRejectedException ex) {
      this.requestRejectedHandler.handle((HttpServletRequest) request, (HttpServletResponse) response, ex);
    } finally {
      SecurityContextHolder.clearContext();
      request.removeAttribute(FILTER_APPLIED);
    }
  }

  private List<Filter> getFilters(HttpServletRequest request) {
    int count = 0;
    for (SecurityFilterChain chain : this.filterChains) {
      if (logger.isTraceEnabled()) {
        logger.trace(LogMessage.format("Trying to match request against %s (%d/%d)", chain, ++count,
                this.filterChains.size()));
      }
      if (chain.matches(request)) {
        return chain.getFilters();
      }
    }
    return null;
  }
}
```

아래 configure를 하나의 SecurityFilterChain이라 보면 된다.

```java
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests() 
            .anyRequest().authenticated();
    http.formLogin();
    http.httpBasic();
  }
}
```

antMatcher에 따라 @Order에 따라서 FilterChain이 순차적으로 필터를 실행한다.

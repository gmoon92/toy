# AnonymousAuthenticationFilter

익명 인증 필터

현재 SecurityContext에 Authentication이 null이면 `익명 Authentication`을 만들어 넣어주고, null이
아니면 아무일도 하지 않는다.

기본으로 만들어 사용할 `익명 Authentication` 객체를 설정할 수 있다.

``` text
http.anonymous()
    .principal()
    .authorities()
    .key()
```

```java
public final class AnonymousConfigurer<H extends HttpSecurityBuilder<H>>
        extends AbstractHttpConfigurer<AnonymousConfigurer<H>, H> {

  //...
  private Object principal = "anonymousUser";
  private List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS");

  //...
}
```

- anonymousUser
- ROLE_ANONYMOUS

```java
public class AnonymousAuthenticationFilter extends GenericFilterBean implements InitializingBean {

  //...
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {

    // [1] 현재 컨텍스트에 authentication이 없다면
    if (SecurityContextHolder.getContext().getAuthentication() == null) {

      // [2] set AnonymousAuthentication  
      SecurityContextHolder.getContext().setAuthentication(createAuthentication((HttpServletRequest) req));
      if (this.logger.isTraceEnabled()) {
        this.logger.trace(LogMessage.of(() -> "Set SecurityContextHolder to "
                + SecurityContextHolder.getContext().getAuthentication()));
      } else {
        this.logger.debug("Set SecurityContextHolder to anonymous SecurityContext");
      }
    } else {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace(LogMessage.of(() -> "Did not set SecurityContextHolder since already authenticated "
                + SecurityContextHolder.getContext().getAuthentication()));
      }
    }
    chain.doFilter(req, res);
  }
}
```

AnonymousAuthentication 객체는 [Null Object Pattern](https://en.wikipedia.org/wiki/Null_object_pattern) 을
사용했다.

코드에서 null을 체크하는것이 아닌 Null을 대변할 오브젝트를 사용한다.

```java
public class AnonymousAuthenticationFilter extends GenericFilterBean implements InitializingBean {

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    //...
  }
  
  protected Authentication createAuthentication(HttpServletRequest request) {
    AnonymousAuthenticationToken token = new AnonymousAuthenticationToken(this.key, this.principal,
            this.authorities);
    token.setDetails(this.authenticationDetailsSource.buildDetails(request));
    return token;
  }
}
```

SecurityFilter에서 종종 AnonymousAuthentication 인지 아닌지 판단하는 코드가 존재한다.

AnonymousAuthentication는 인증된 사용자가 없다. 즉 인증되지 않는 사용자, null 을 대변하는 클래스 타입이다.

따라서 인증된 사용자인지 아닌지 판단하는 `isAnonymous(...)` 메서드를 보면 Authentication 타입의 객체에 실제 객체를 비교하여 판단한다.

```java
public class AuthenticationTrustResolverImpl implements AuthenticationTrustResolver {
  //...
  @Override
  public boolean isAnonymous(Authentication authentication) {
    if ((this.anonymousClass == null) || (authentication == null)) {
      return false;
    }
    // type 비교
    return this.anonymousClass.isAssignableFrom(authentication.getClass());
  }
  
}
```

### 참고

- https://docs.spring.io/spring-security/site/docs/5.1.5.RELEASE/reference/htmlsingle/#anonymous
- https://en.wikipedia.org/wiki/Null_object_pattern

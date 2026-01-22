# BasicAuthenticationFilter

Basic 인증 처리 필터

Basic 인증이란?

- https://tools.ietf.org/html/rfc7617
- 요청 헤더에 username와 password를 실어 보내면 브라우저 또는 서버가 그 값을 읽어서 인증하는 방식. 예)
  Authorization: Basic QWxhZGRpbjpPcGVuU2VzYW1l (keesun:123 을 BASE 64)
- 보통, 브라우저 기반 요청이 클라이언트의 요청을 처리할 때 자주 사용.
- 보안에 취약하기 때문에 반드시 HTTPS를 사용할 것을 권장.

```text
curl -u gmoon:123 http://localhost:8080
```

[1] 인증이 되면 SecurityContextHolder에 인증된 정보를 담아준다.

```java
public class BasicAuthenticationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    try {
      UsernamePasswordAuthenticationToken authRequest = this.authenticationConverter.convert(request);
      if (authRequest == null) {
        chain.doFilter(request, response);
        return;
      }
      String username = authRequest.getName();
      if (authenticationIsRequired(username)) {
        Authentication authResult = this.authenticationManager.authenticate(authRequest);
        
        // [1] 인증이 되면 SecurityContextHolder에 인증된 정보를 담아준다.
        SecurityContextHolder.getContext().setAuthentication(authResult);
        
        //...
        onSuccessfulAuthentication(request, response, authResult);
      }
    } catch (AuthenticationException ex) {
      //...
    }

    chain.doFilter(request, response);
  }
  
}
```

`UsernamePasswordAuthenticationToken` 토큰을 사용하여 인증된 사용자를 얻는 방식이 마치 UsernamePasswordAuthenticationFilter 와 흡사하지만 차이가 있다.

- UsernamePasswordAuthenticationFilter : Form 인증
  - stateful 인증 후 SecurityContextRepository에 사용자 정보를 담고, 다음 요청부턴 SecurityContextPersistenceFilter 에선 다시 인증하는것이 아닌 세션 값을 읽어서 처리
- BasicAuthenticationFilter : Request Header 에서 사용자 인증
  - stateless. 계속 인증 요청 처리함 

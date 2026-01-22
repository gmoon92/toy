# SecurityContextHolderAwareRequestFilter

시큐리티 관련 서블릿3 스팩 구현 필터

시큐리티 관련 서블릿 API를 구현해주는 필터

- HttpServletRequest#authenticate(HttpServletResponse)
- HttpServletRequest#login(String, String)
- HttpServletRequest#logout()
- AsyncContext#start(Runnable)
  - WebAsyncManagerIntegrationFilter 와 비슷한 역할
  - 새로운 생성된 Thread에서 SecurityContextHolder에 인증된 사용자 데이터를 공유해주는 역할을 한다.

자바 서블릿 패키지(javax.servlet.http)에 포함되어 있는 HttpServletRequest를 사용하여 authenticate 를 호출하면 인증이 됐는지 안됐는지 판단하고, 안되어 있다면 로그인 페이지로 이동을 시키는 필터다.

```java
public interface HttpServletRequest extends ServletRequest {
  boolean authenticate(HttpServletResponse response) throws IOException, ServletException;  
}
```

![img](images/security-context-holder-aware-request-filter.png)

# ExceptionTranslationFilter

필터 체인에서 발생하는 AccessDeniedException과 AuthenticationException을 처리하는 필터

필터 체인에서 마지막에서 두 번째로 실행되는 필터
다음으로 오는 필터가 `FilterSecurityInterceptor`와 밀접한 관계이다.

이는 실행 우선 순위 때문이다.

ExceptionTranslationFilter -> FilterSecurityInterceptor (AccessDecisionManager, 인가 처리)

반드시 ExceptionTranslationFilter 필터는 FilterSecurityInterceptor 필터 보다 우선적으로 실행되어야 한다.

ExceptionTranslationFilter 가 FilterSecurityInterceptor 를 try-catch 블록으로 감싸고 처리를 한다. 

FilterSecurityInterceptor 엔 요청에 대한 권한 처리를 판단하다가 두 가지 인가 예외가 발생할 수 있다.

1. 인증 자체가 안되어 있다. -> AuthenticationException 예외 발생
2. 요청에 대한 권한이 부족하다 -> AccessDeniedException 예외 발생

### 1. AuthenticationException 발생 시

- AuthenticationEntryPoint 실행
  - 해당 유저를 인증을 할 수 있게끔 인증 요청을 할 수 있는 페이지로 이동 시키는 역할
- AbstractSecurityInterceptor 하위 클래스(예, FilterSecurityInterceptor)에서 발생하는 예외만 처리.
- 그렇다면 UsernamePasswordAuthenticationFilter에서 발생한 인증 에러는?

### 2. AccessDeniedException 발생 시

- 익명 사용자라면 AuthenticationEntryPoint 실행
- 익명 사용자가 아니면 AccessDeniedHandler에게 위임
  - ExceptionTranslationFilter 가 가지고 있는 AccessDeniedHandler 가 처리


### 2.1. AccessDeniedException Custom

```text
http.exceptionHandling()
    .accessDeniedPage("/"); // 권한 실패시 이동할 페이지 설정
```

페이지 이동 및 권한 실패한 사용자에 대한 데이터를 로그로 남기고 싶다면 AccessDeniedHandler 구현

```java
http.exceptionHandling()
        .accessDeniedHandler(new AccessDeniedHandler() {
          @Override
          public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
            UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = user.getUsername();
            log.info("user : {} dined to access {}", username, request.getRequestURI());

            response.sendRedirect("/access-denied");
          }
        });
```

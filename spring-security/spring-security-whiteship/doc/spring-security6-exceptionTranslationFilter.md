# 19. ExceptionTranslationFilter

필터 체인에서 발생하는 `AccessDeniedExcpetion`과 `AuthenticationException`을 처리하는 필터

> FilterSecurityInterceptor 에서 발생하는 예외를 처리하는 처리기다.
> - `AccessDecisionManager`를 사용하여 Access Control 또는 예외 처리하는 필터

정확히는 AbstractSecurityInterceptor 에서 발생하는 예외를 관리

- 인증 에러, AuthenticationException 발생 시
    - AuthenticationEntryPoint 실행
    - AbstractSecurityInterceptor 하위 클래스 (ex. FilterSecurityInterceptor)에서 발생하는 예외만 처리
    - 그렇다면 `UsernamePasswordAuthenticationFilter`에서 발생한 인증 에러는 ?
        - > UsernamePasswordAuthenticationFilter 에서 발생하는 에러는 관리하지 않는다.
          > 
          > 로그인을 관장하는 필터(AbstractAuthenticationProcessingFilter)로써, ExceptionTranslationFilter에선 관리르하지 않는다.
- 인가(권한) 에러, AccessDeniedException 발생 시
    - 익명 사용자라면 AuthenticationEntryPoint 실행
    - 익명 사용자가 아니면(이미 인증된 사용자) AccessDeniedHandler에게 위임


```java
class SimpleUrlAuthenticationFailureHandler {
//  ...
  protected final void saveException(HttpServletRequest request, AuthenticationException exception) {
    if (this.forwardToDestination) {
      request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
      return;
    }
    HttpSession session = request.getSession(false);
    if (session != null || this.allowSessionCreation) {
      request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
      // ^-- Http Session에다가 Exception Message를 담는다.
    }
  }
//  ...
}
```
Http Session 에다가 Exception Message를 담아둔다.
AuthenticationFailureHandler
# FilterSecurityInterceptor

AccessDecisionManager를 사용하여 Access Control 또는 예외 처리 하는 필터. 대부분의 경우
FilterChainProxy에 제일 마지막 필터로 들어있다.

최종적으로 인증 절차를 다 걸친 후, AccessDecisionManager를 통해 ConfigAttributes를 만족하는지 확인하는 필터.

```java
public abstract class AbstractSecurityInterceptor
        implements InitializingBean, ApplicationEventPublisherAware, MessageSourceAware {

  private void attemptAuthorization(Object object, Collection<ConfigAttribute> attributes,
                                    Authentication authenticated) {
    try {
      this.accessDecisionManager.decide(authenticated, object, attributes);
    } catch (AccessDeniedException ex) {
      if (this.logger.isTraceEnabled()) {
        this.logger.trace(LogMessage.format("Failed to authorize %s with attributes %s using %s", object,
                attributes, this.accessDecisionManager));
      } else if (this.logger.isDebugEnabled()) {
        this.logger.debug(LogMessage.format("Failed to authorize %s with attributes %s", object, attributes));
      }
      publishEvent(new AuthorizationFailureEvent(object, attributes, authenticated, ex));
      throw ex;
    }
  }
}
```

1. SecurityContextHolder가 Authentication (인증 정보)를 가지고 있다.
2. Authentication은 AuthenticationManager를 통해 인증 처리를 한다.
    1. UsernamePasswordAuthenticationFiler
      - 폼 인증 처리를 하는 시큐리티 필터
      - 인증된 Authentication을 SecurityContextHolder에 넣어주는 필터
    2. SecurityContextPersistenceFilter
      - SecurityContext를 Http session에 캐시(기본 전략)하여 여러 요청에서 Authentication을 공유하는 필터
      - SecurityContextReository를 교체하여 세션을 HTTP session이 아닌 다른곳에 저장하는 것도 가능
3. AuthenticationManager는 유요한 인증인지 확인하고 SecurityContextHolder에 넣어준다.
4. 여러 FilterChainProxy를 통해 묶여있고 순차적으로 등록된 필터들을 실행
5. 인가(authorization), AuthenticationManager를

어디서 AuthenticationManager를 사용하는 필터를 확인하고 싶다면, FilterChainProxy 클래스에 디버그를 통해 알 수 있다.




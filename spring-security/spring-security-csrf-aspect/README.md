# Spring Security CSRF Aspect

본 포스팅은 **Spring Security의 CsrfFilter에 대해 다루진 않는다.**

`Spring Security`가 도입된 프로젝트에선 `CsrfFilter`를 활성화하여 CSRF 보안 취약점을 간단히 해결할 수 있다. `Spring Security`에 의해 시큐리티 필터 체인에 `CsrfFilter`가 등록이 되어 동작하기 때문에 모든 요청에 전역적으로 적용된다.

모든 프로젝트가 간단히 흘러간다면 얼마나 좋을까. 어떤 프로젝트는 개발 환경과 비용 때문에 CsrfFilter 필터를 섣불리 적용시키기 어렵다.

[CSRF 공격](https://www.imperva.com/learn/application-security/csrf-cross-site-request-forgery/) 은 애플리케이션 보안에 치명적이므로 괄시할 수 없다.

대신, Spring AOP를 통해 CSRF 공격을 방어할 수 있는 방법을 제시한다.

1. [Custom Annotation](#custom-annotation)
    - CSRF Token 생성 어노테이션
    - CSRF Token 확인 어노테이션
2. [CSRF Token Repository](#csrf-token-aspect)
3. [CSRF Token Aspect](#csrf-token-aspect)

전체 코드는 [GitHub 코드를 참고하자.](https://github.com/gmoon92/Toy/blob/master/spring-security/spring-security-csrf-aspect/README.md)

## Custom Annotation

- `CSRFTokenGenerator`: CSRF Token 생성 어노테이션
- `CSRF`: CSRF Token 체크 어노테이션

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSRFTokenGenerator {
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CSRF {
}
```

## CSRF Token Repository

생성된 CSRF 토큰은 HttpSession 에서 관리할 예정이다.

- saveTokenOnHttpSession
  - 새로운 토큰을 생성하고 `HTTP Session`에 저장한다.
- getToken
  - `HTTP Session` 에 저장된 토큰을 가져온다.

> Repository 전체 코드는 [GitHub 코드를 참고하자.](https://github.com/gmoon92/Toy/blob/master/spring-security/spring-security-csrf-aspect/src/main/java/com/gmoon/springsecuritycsrfaspect/csrf/CsrfTokenRepository.java)

```java
public class CsrfTokenRepository {
  private static final String SESSION_ATTRIBUTE_NAME = "CSRF_TOKEN";

  public void saveTokenOnHttpSession(HttpServletRequest request) {
    BaseCsrfToken newToken = HttpSessionCsrfToken.generate();
    request.getSession().setAttribute(SESSION_ATTRIBUTE_NAME, newToken);
  }

  public BaseCsrfToken getToken(HttpServletRequest request) {
    return (BaseCsrfToken) request.getSession().getAttribute(SESSION_ATTRIBUTE_NAME);
  }
}
```

## CSRF Token Aspect

- @AfterReturning 어드바이스 메서드
  - CSRF 토큰을 생성한다.
- @Before 어드바이스 메서드
  - CSRF 토큰을 체크한다.

> Aspect 전체 코드는 [GitHub 코드를 참고하자.](https://github.com/gmoon92/Toy/blob/master/spring-security/spring-security-csrf-aspect/src/main/java/com/gmoon/springsecuritycsrfaspect/csrf/CsrfTokenAspect.java) 

```java
@Aspect
@RequiredArgsConstructor
public class CsrfTokenAspect {
  private final HttpServletRequest request;
  private final CsrfTokenRepository csrfTokenRepository;

  @AfterReturning("@annotation(com.gmoon.springsecuritycsrfaspect.csrf.annotation.CSRFTokenGenerator)")
  public void generator() {
    csrfTokenRepository.saveTokenOnHttpSession(request);
  }

  @Pointcut("within(@org.springframework.stereotype.Controller *)")
  public void controller() {
  }

  @Pointcut("@annotation(com.gmoon.springsecuritycsrfaspect.csrf.annotation.CSRF)")
  public void csrf() {
  }

  @Before("controller() && csrf()")
  public void checkCsrfToken() {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return;
    }

    BaseCsrfToken sessionToken = csrfTokenRepository.getToken(request);
    
    String headerName = sessionToken.getHeaderName();
    String parameterName = sessionToken.getParameterName();
    String requestToken = getRequestToken(headerName, parameterName);
    
    checkRequestCsrfToken(sessionToken, requestToken);
  }

  private String getRequestToken(String headerName, String parameterName) {
    String header = request.getHeader(headerName);
    String parameter = request.getParameter(parameterName);
    return StringUtils.defaultString(header, parameter);
  }

  private void checkRequestCsrfToken(CsrfToken sessionToken, String requestToken) {
    boolean isExistsCRSFToken = StringUtils.equals(sessionToken.getToken(), requestToken);
    if (!isExistsCRSFToken) {
      throw new AccessDeniedException("csrf attack is prevented");
    }
  }
}
```

## 참고

- [Baeldung - CSRF Protection with Spring MVC and Thymeleaf](https://www.baeldung.com/csrf-thymeleaf-with-spring-security)
- [Baeldung - A Guide to CSRF Protection in Spring Security](https://www.baeldung.com/spring-security-csrf)
- [Baeldung - CSRF With Stateless REST API](https://www.baeldung.com/csrf-stateless-rest-api)
- [imperva - Cross site request forgery (CSRF) attack](https://www.imperva.com/learn/application-security/csrf-cross-site-request-forgery/)


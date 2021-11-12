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

## Custom Annotation

## CSRF Token Repository

## CSRF Token Aspect

## 참고

- [Baeldung - CSRF Protection with Spring MVC and Thymeleaf](https://www.baeldung.com/csrf-thymeleaf-with-spring-security)
- [Baeldung - A Guide to CSRF Protection in Spring Security](https://www.baeldung.com/spring-security-csrf)
- [Baeldung - CSRF With Stateless REST API](https://www.baeldung.com/csrf-stateless-rest-api)
- [imperva - Cross site request forgery (CSRF) attack](https://www.imperva.com/learn/application-security/csrf-cross-site-request-forgery/)


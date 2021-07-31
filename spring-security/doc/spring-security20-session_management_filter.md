# SessionManagementFilter

세션 관리 필터

세션 변조 방지 전략 설정: sessionFixation

- 세션 변조: https://www.owasp.org/index.php/Session_fixation
- none
- newSession
- migrateSession (서블릿 3.0- 컨테이너 사용시 기본값)
- changeSessionId (서브릿 3.1+ 컨테이너 사용시 기본값)
- https://docs.spring.io/spring-security/site/docs/5.1.5.RELEASE/reference/htmlsingle/#nsa-session-management-attributes

유효하지 않은 세션을 리다이렉트 시킬 URL 설정

- invalidSessionUrl

동시성 제어: maximumSessions

- 추가 로그인을 막을지 여부 설정 (기본값, false)
- https://docs.spring.io/spring-security/site/docs/5.1.5.RELEASE/reference/htmlsingle/#nsa-concurrency-control

세션 생성 전략: sessionCreationPolicy
- IF_REQUIRED
- NEVER
- STATELESS
- ALWAYS

## 참고

- https://docs.spring.io/spring-security/site/docs/5.1.5.RELEASE/reference/htmlsingle/#session-mgmt
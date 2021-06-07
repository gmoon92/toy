# AccessDecisionManager
## Authorization 권한 부여(인가(Authorization))

이미 인증된 사용자가 특정한 서버의 리소스에 접근할 때(웹 요청) 또는 메서드 콜 요청이 올때 허용할 것인가? 해당 요청이 올바른지 판단이 필요하다. 인가(Authorization) 또는 권한 부여를 의미한다.

엑세스 컨트롤러 인터페이스 중점으로 다룬다.

- Access Control 결정을 내리는 인터페이스로, 구현체 3가지를 기본으로 제공한다.
    - **AffirmativeBased** : 여러 Voter중에 한명이라도 허용하면 허용. 기본 전략.
    - ConsensusBased : 다수결
    - UnanimousBased : 만장일치

- AccessDecisionVoter
    - 해당 Authentication이 특정한 Object에 접근할 때 필요한 ConfigAttributes를 만족하는지 확인한다.
    - **WebExpressionVoter** : 웹 시큐리티에서 사용하는 기본 구현체, ROLE_Xxxx가 매치하는지 확인.
    - RoleHierarchyVoter : 계층형 ROLE 지원. ADMIN > MANAGER > USER
    - ...

```java
public interface AccessDecisionManager {
  
  void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
          throws AccessDeniedException, InsufficientAuthenticationException;

  boolean supports(ConfigAttribute attribute);

  boolean supports(Class<?> clazz);
}
```

- Voter
  - Authentication : 인증된 사용자
  - Object : 요청 url 또는 메서드
  - ConfigAttribute : 접근 권한

```java
int result = voter.vote(authentication, object, configAttributes);
```

```java
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    
    http.authorizeRequests()
            .mvcMatchers("/", "/info", "/account/**")  // Object
              .permitAll() // ConfigAttribute
            .mvcMatchers("/admin") // Object
              .hasRole("ADMIN") // ConfigAttribute
            .anyRequest().authenticated();

    http.formLogin();
    http.httpBasic();
  }
}
```
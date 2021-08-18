# 메소드 시큐리티

서비스 객체를 직접 호출할 때 사용하는 보안 객체이다.

- https://docs.spring.io/spring-security/site/docs/5.1.5.RELEASE/reference/htmlsingle/#jc-method
- https://www.baeldung.com/spring-security-method-security

해당 기능은 웹에선 적합하지 않고, 오히려 데스크탑 애플리케이션에 적합하다.


@EnableGlobalMethodSecurity 

```text
@EnableGlobalMethodSecurity(securedEnabled = true,
                            prePostEnabled = true,
                            jsr250Enabled = true)
```

### @Secured와 @RollAllowed 

- 메소드 호출 이전에 권한을 확인한다.
- 스프링 EL(expression language)을 사용하지 못한다.

### @PreAuthorize와 @PostAuthorize

- 메소드 호출 이전 @있다.
- 스프링 EL(expression language) 사용 가능

파라미터나 리턴 값이 있을 경우에 권한 체크 가능 

### MethodSecurityConfig.java

```java
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

  @Override
  protected AccessDecisionManager accessDecisionManager() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
    AffirmativeBased accessDecisionManager = (AffirmativeBased) super.accessDecisionManager();
    accessDecisionManager.getDecisionVoters().add(new RoleHierarchyVoter(roleHierarchy));
    return accessDecisionManager;
  }
}
```

### RoleHierarchy

기본적으로 스프링 시큐리티에 설정된 accessDecisionManager 를 가져오지 못한다.

별도로 메서드 시큐리티에도 계층형 권한 설정을 할 수 있도록 설정이 필요.

```java
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true,
        prePostEnabled = true,
        jsr250Enabled = true)
public class MethodSecurity extends GlobalMethodSecurityConfiguration {

  @Override
  protected AccessDecisionManager accessDecisionManager() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");

    AffirmativeBased accessDecisionManager = (AffirmativeBased) super.accessDecisionManager();
    accessDecisionManager.getDecisionVoters().add(new RoleHierarchyVoter(roleHierarchy));
    return accessDecisionManager;
  }
}
```
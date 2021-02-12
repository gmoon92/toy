# Spring Security

# 아키텍처

# SecurityContextHolder와 Authentication

# SecurityContextHolder

- Authentication 객체가 어디에 있는지?
- Authentication 객체가 무엇을 담고 있는지?

![security-context-holder](./img/security-context-holder.png)

SecurityContextHolder

- SecurityContext 제공, 기본적으로 ThreadLocal을 사용한다.

> ThreadLocal : 한 쓰레드 내에서 공유하는 저장소라 생각하면 된다. 객체를 파라미터를 넘기지 않아도, 데이터를 접근할 수 있다.

SecurityContext

- Authentication 제공
- SecurityContextHolder
- SecurityContext
- Authentication

# Authentication

- Principal 과 GrantAuthority 제공

Principal (인증, 인증된 사용자 정보)

- "누구"에 해당하는 정보, (User 객체 타입)
- **UserDetailsService**에서 리턴한 그 객체
- 객체는 UserDetails 타입

GrantAuthority (권한)

- "ROLE_USER", "ROLE_ADMIN" 등 Principal이 가지고 있는 "권한"을 나타낸다.
- 인증 이후, 인가 미 권한 확인할 때 이 정보를 참조한다.

UserDetails

- 애플리케이션이 가지고 있는 유저 정보와 스프링 시큐리티가 사용하는 Authentication 객체 사이의 어댑터

UserDetailsService

- 유저 정보를 UserDetails 타입으로 가져오는 DAO (Data Access Object) 인터페이스
- 구현은 커스텀 가능 (Spring Data JPA 사용 가능)

SecurityContextHolder 인증된 정보만 담는다. Thread Local 를 사용하고 애플리케이션 어디서나 접근 가능하다.
Thread가 다를 경우, 제대로된 Authentication를 반환 받을 수 없기 때문에, 다른 전략을 고민해야한다.

인증이 유저라면 SecurityContextHolder를 통해 Authentication(인증) 객체를 반환하여, 사용자 정보를 받을 수 있다.

---

# AuthenticationManager와 Authentication

- 실제로 Authentication 객체를 만들고, 인증을 담당하는 곳은?

스프링 시큐리티에서 인증(Authentication)은 AuthenticationManager가 한다.

```java
Authentication authenticate(Authentication authentication)throws AuthenticationException;
```

- 인자로 받은 Authentication이 유효한 인증인지 확인하고 Authentication 객체를 반환한다.
- 인증을 확인하는 과정에서 비활성 계정, 잘못된 비번, 잠긴 계정 등의 에러를 던질 수 있다.

인자로 받은 Authentication

- 사용자가 입력한 인증에 필요한 (username, password)로 만든 객체. (폼 인증인 경우)
- Authentication
    - Principal : "gmoon"
    - Credentials : "123"

유효한 인증인지 확인

- 사용자가 입력한 password가 UserDetailsService를 통해 읽어온 UserDetails 객체에 들어있는 password와
  일치하는지 확인 해당 사용자 계정이 잠겨 있진 않은지, 비활성 계정은 아닌지 등 확인

Authentication 객체를 리턴

- Authentication
    - Principal : UserDetailsService에서 리턴한 그 객체 (User)
    - Credentials : ~~"123"~~ 사실 리턴 받을땐 비어 있다.
    - GrantedAuthorities

기본적인 AuthenticationManager 인터페이스 구현체는 ProviderManager를 사용하고 있다.

```text
org.springframework.security.authentication.ProviderManager
```

formLogin일 경우, Authentication 객체가 UsernamePasswordAuthenticationToken로 넘어오게 되는데,
여러 AuthenticationProvider의 구현체들을 통해 인증 처리가 되게 된다.

> 참고 formLogin DaoAuthenticationProvider

---

# ThreadLocal

Java.lang 패키지에서 기본으로 제공하는 쓰레드 범위 변수, 즉, 쓰레드 수준의 데이터 저장소,.

- 같은 쓰레드 내에서만 공유.
- 따라서 같은 쓰레드라면 해당 데이터를 메소드 매개변수로 넘겨줄 필요 없음.
- SecurityContextHolder의 기본적략

ThreadLocal를 이용하면, 한 쓰레드 내에서는 메서드 파라미터로 넘겨줄 필요가 없다.

```java
public class AccountContext {

  private static final ThreadLocal<Member> ACCOUNT_THREAD_LOCAL
          = new ThreadLocal<>();

  public static void setMember(Member member) {
    ACCOUNT_THREAD_LOCAL.set(member);
  }

  public static Member getMember() {
    return ACCOUNT_THREAD_LOCAL.get();
  }
}
```

---

# Authentication과 SecurityContextHolder

# 누가 인증된 Authentication 객체를 SecurityContextHolder에 담아 주는건가?

AuthenticationManager가 인증을 마친 뒤 리턴 받은 Authentication 객체의 행방은?

아래 2가지 필터가 Authentication 객체를 관리하고 있다.

- UsernamePasswordAuthenticationFilter
- SecurityContextPersistenceFilter

UsernamePasswordAuthenticationFilter

- 폼 인증을 처리하는 시큐리티 필터
- 인증된 Authentication 객체를 SecurityContextHolder에 넣어주는 필터 SecurityContextHolder
  getContext().setAuthentication(authentication)

SecurityContextPersistenceFilter (stateful)

- SecurityContext를 HTTP session에 캐시(기본 전략)하여 여러 요청에서 Authentication를 공유할 수 있는,
  공유하는 필터
- SecurityContextRepository를 교체하여 세션을 HTTP session이 아닌 다른 곳에 저장하는 것도 가능하다. 

한번 인증된 사용자에 대해선, 다시 인증하는것이 아닌 동일한 Authentication 객체 반환

```java
public class UsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
          throws AuthenticationException {
    // ...
    return this.getAuthenticationManager().authenticate(authRequest);
  }
}
```

UsernamePasswordAuthenticationFilter 필터는 AuthenticationManager 인터페이스를 통해 인증된 사용자인지 검증한다. 검증된 Authentication 객체를 반환된다. UsernamePasswordAuthenticationFilter 필터는 AbstractAuthenticationProcessingFilter의 doFilter 메서드를 거치게 된다.

AbstractAuthenticationProcessingFilter 템플릿 팩토리 메서드 패턴이고, 이 구현체가 바로
UsernamePasswordAuthenticationFilter 필터이다.

```java
public abstract class AbstractAuthenticationProcessingFilter extends GenericFilterBean
        implements ApplicationEventPublisherAware, MessageSourceAware {

  private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    // ...
    Authentication authenticationResult = attemptAuthentication(request, response);
    // ...
    successfulAuthentication(request, response, chain, authenticationResult);
  }
  // ...

  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                          Authentication authResult) throws IOException, ServletException {
    SecurityContextHolder.getContext().setAuthentication(authResult);
    // ...
  }
}
```

SecurityContextPersistenceFilter 필터 방식은 HTTP session에서 인증된 사용자를 가져온다. 이 말인즉슨 HTTP session이 바뀌면 사용자 인증 정보가 날라간다는 의미다. 즉 session이 유지되어야 한다는 의미이다.

HTTP session 방식을 사용하지 않는다는 의미는 stateless 하게 구현해야 된다는 의미, 매 요청마다 인증 정보에 대한 검증을 처리해야한다. 따라서 인증에 필요한 정보가 Header는 본문이든 어디든 들어 있어야한다.

그 요청이 들어 왔을때, SecurityContextHolder.getContext().setAuthentication(authentication) 에 넣어주는 로직을 구현해야 한다.





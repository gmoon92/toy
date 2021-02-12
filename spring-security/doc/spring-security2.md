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

SecurityContextHolder 인증된 정보만 담는다. Thread Local 를 사용하고 애플리케이션 어디서나 접근 가능하다. Thread가 다를 경우, 제대로된 Authentication를 반환 받을 수 없기 때문에, 다른 전략을 고민해야한다.

인증이 유저라면 SecurityContextHolder를 통해 Authentication(인증) 객체를 반환하여, 사용자 정보를 받을 수 있다. 

---

# AuthenticationManager와 Authentication

- 실제로 Authentication 객체를 만들고, 인증을 담당하는 곳은?

스프링 시큐리티에서 인증(Authentication)은 AuthenticationManager가 한다.

```java
Authentication authenticate(Authentication authentication) throws AuthenticationException;
```
- 인자로 받은 Authentication이 유효한 인증인지 확인하고 Authentication 객체를 반환한다.
- 인증을 확인하는 과정에서 비활성 계정, 잘못된 비번, 잠긴 계정 등의 에러를 던질 수 있다.

인자로 받은 Authentication
- 사용자가 입력한 인증에 필요한 (username, password)로 만든 객체. (폼 인증인 경우)
- Authentication
    - Principal : "gmoon"
    - Credentials : "123"

유효한 인증인지 확인
- 사용자가 입력한 password가 UserDetailsService를 통해 읽어온 UserDetails 객체에 들어있는 password와 일치하는지 확인
해당 사용자 계정이 잠겨 있진 않은지, 비활성 계정은 아닌지 등 확인
  
Authentication 객체를 리턴
- Authentication
    - Principal : UserDetailsService에서 리턴한 그 객체 (User)
    - Credentials : "123"

기본적인 AuthenticationManager 인터페이스 구현체는 ProviderManager를 사용하고 있다.

formLogin일 경우, Authentication 객체가 UsernamePasswordAuthenticationToken로 넘어오게 되는데, 여러 AuthenticationProvider의 구현체들을 통해 인증 처리가 되게 된다.

> 참고 DaoAuthenticationProvider





# Spring Security

Principal

## Dependency

스프링 부트 도움을 받아 추가하기

- `spring-boot-starter-*` 사용
- 버전 생략 - 스프링 부트의 의존성 관리 기능 사용

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

의존성 추가 후, 모든 요청은 인증을 필요로 한다. 기본 유저가 생성된다.

- Username : user
- Password : 콘솔에 출력된 문자열 확인

```text
Using generated security password: a4edae6b-993a-4fab-9f6c-c0324710c6d9
```

```java

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

}
```

ant 형식으로 표현 가능

## UserDetailsServiceAutoConfiguration

### inMemoryUserDetailsManager

SecurityProperties

```text
spring.security.user.name=admin
spring.security.user.password=123
spring.security.user.rols=ADMIN
```

{noop} 스프링 시큐리티 내부적으로 장착되어 있는 패스워드 인코더 반드시 앞에 prefix로 암호화 패스워드 인코더를 명시해야한다.
{noop}은 어떠한 암호화를 사용하지 않겠다는 의미이다.

```java

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  //...

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
            .withUser("gmoon").password("{noop}gmoon").roles("USER").and()
            .withUser("admin").password("{noop}admin").roles("ADMIN");
  }
}
```

시큐리티 내부적으로 어떠한 방식으로 인코딩으로 입력받은 값을 암호화해서 비교를 하여 값을 넣어준다.

java.lang.IllegalArgumentException: There is no PasswordEncoder mapped for the
id "null"

패스워드 인코딩 필요

```java

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  AccountService accountService;

  // 생략 가능
  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(accountService);
  }
}
```

UserDetailsService 구현체가 빈으로 등록되어 있으면 따로 설정 생략 가능하다. 마찬가지로 패스워드 인코더도 구현체가 빈으로
등록되어 있으면 된다.

---

# PasswordEncoder

비밀번호는 반드시 인코딩해서 저장해야한다. 단방향 암호화 알고리즘으로...

- 스프링 시큐리티가 제공하는 PasswordEncoder는 특정한 포맷으로 동작함.
- {id}encoderPassword
- 다양한 해싱 전략의 패스워드를 지원할 수 있다는 장점이 있다.

// 비추 : 비밀번호가 평문 그대로 저장된다. NoOpPasswordEncoder

// 추천 : 기본 전략인 bcrypt로 암호화 해서 저장하며 비교할 때는 {id}를 확인해서 다양한 인코딩을 지원한다.
PasswordEncoderFactories.createDelegatingPasswordEncoder();

패스워드 포맷 {noop} 패스워드 인코더 없애기

```java

@Entity
public class Member {
  // ...
  public void encodePassword() {
    this.password = "{noop}" + this.password;
  }
}
```

Spring 5 이전 버전 아무런 패스워드 인코딩이 없을때 사용했던 방식 현재는 권장하지 않는 인코딩 방식이기 때문에 @Deprecated
되었다.

```java
@Bean
public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
        }
```

왜 Spring 5 특정한 포맷이 생겼느냐?

버전에선 NoOpPasswordEncoder 이 기본 패스워드 기본 인코딩 전략에서
[Spring Security - Password history](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-history)

결론은 noop 기본 전략에서 bcrypt 방식으로 바꾸었다.

문제는 평문으로 저장되어 있을 수 있던데, 인증이 깨지는 문제가 발생한다.

동시 다양한 암호화 알고리즘을 지원해주기 위해 {id}encoderPassword을 채택했다.

PasswordEncoderFactories

## Example 19. Create Custom DelegatingPasswordEncoder

id 에 해당하는 해싱 알고리즘을 지정하고, value는 알고리즘을 해싱할 수 있는 패스워드 인코더를 지정해주면 된다.

```java
String idForEncode="bcrypt";
        Map encoders=new HashMap<>();
        encoders.put(idForEncode,new BCryptPasswordEncoder());
        encoders.put("noop",NoOpPasswordEncoder.getInstance());
        encoders.put("pbkdf2",new Pbkdf2PasswordEncoder());
        encoders.put("scrypt",new SCryptPasswordEncoder());
        encoders.put("sha256",new StandardPasswordEncoder());

        PasswordEncoder passwordEncoder=
        new DelegatingPasswordEncoder(idForEncode,encoders);
```

기본 포맷을 지원하는 패스워드 인코더로 변경해보자

스프링 5에서 권장하는 패스워드 인코더는 PasswordEncoderFactories 팩토리 클래스에서 받아올 수 있다.

DelegatingPasswordEncoder 이름에서 알 수 있듯이 다양한 패스워드 인코딩 방식을 지원해주고 있다.

```java
@Bean
public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
```

---

## 테스트

## 폼 인증 테스트

## dependency 추가

```xml

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

- mockMvc
- @WithMockUser

```java
class LoginControllerTest {
  
  @Test
  void admin_admin() throws Exception {
    mockMvc.perform(get("/admin").with(user("admin").roles("ADMIN")))
            .andDo(print()) // 출력 응답
            .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "user", roles = { "USER" })
  void admin_user_with_test_annotation() throws Exception {
    mockMvc.perform(get("/admin"))
            .andDo(print())
            .andExpect(status().isForbidden());
  }
}
```






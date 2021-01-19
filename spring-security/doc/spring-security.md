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

의존성 추가 후, 모든 요청은 인증을 필요로 한다.
기본 유저가 생성된다.
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

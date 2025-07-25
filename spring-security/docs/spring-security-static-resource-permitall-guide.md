## please use permitAll via HttpSecurity#authorizeHttpRequests instead.

- 경고는 "Spring Security에서 static 자원 등 ignore()을 사용하지 말고 permitAll을 사용하라"는 최신 정책 알림이다.
- 코드를 변경해도 기존 기능(정적 리소스 오픈)은 동일하지만, 보안성과 확장성이 향상된다.
- 향후 Spring 버전업 또는 정식 릴리즈 환경에서도 해당 경고가 발생하지 않도록 하기 위해 권장 방식으로 변경하는 것이 바람직하다.

```text
WARN 19446 --- [commons-apache-poi] [om-jmh-worker-1] o.s.s.c.a.web.builders.WebSecurity       : You are asking Spring Security to ignore org.springframework.boot.autoconfigure.security.servlet.StaticResourceRequest$StaticResourceRequestMatcher@2af21a93. This is not recommended -- please use permitAll via HttpSecurity#authorizeHttpRequests instead.
```

- 해당 경고는 프로젝트의 Security 설정(WebSecurity의 Ignore 방식 등)이 예전 방식(web.ignoring())을 사용하고 있음을 나타낸다.
- Spring의 공식 권장 방식은 `ignore()`이 아닌 `http.authorizeHttpRequests().requestMatchers(...).permitAll()` 방식으로 허용하는 것이다.

### Spring Security 5.7부터의 변화

- `WebSecurity.ignoring()` API는 Spring Security 5.7부터 deprecated 되었으며, 향후 지원 중단이 예고되어 있다.
- 이 방식은 보안상의 이유로 권장되지 않는다.

### ignoring() 사용 시 보안 이슈

- `ignoring()`을 사용하면 해당 경로는 필터 체인 자체에 포함되지 않아 모든 보안 필터링이 제외된다.
- 반면 `permitAll()`은 필터 체인에 들어오긴 하므로 후처리, 로깅, 감사 등 기능이 정상적으로 동작한다.

```java
// 구방식 (비권장)
@Override
public void configure(WebSecurity web) {
	web.ignoring().requestMatchers(StaticResourceRequest.toCommonLocations());
}
```

### 권장 방식

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(StaticResourceRequest.toCommonLocations()).permitAll()
            .anyRequest().authenticated()
        );
}
```

- `ignoring()` 방식은 필터 자체를 배제하므로 보안 로깅, 필터 후처리 등에서 누락이 발생할 수 있다.
- `permitAll()` 방식은 필터 체인에 그대로 포함되기 때문에 일관성과 안전성을 유지할 수 있다.

### Spring Boot 3.x / Security 6.x 기준 권장 설정

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(request -> request
             .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
             .requestMatchers("/login**").permitAll()
             .anyRequest().authenticated());
    return http.build();
}
```

### 커스텀 필터 주의사항

`@Component`, `@WebFilter`, `FilterRegistrationBean` 방식을 통해 등록된 필터는 서블릿 컨테이너(web.xml의 필터처럼) 레벨에서 전역으로 자동 등록된다.

예를 들어

```java
@Componet
class JwtVerifyFilter extends OncePerRequestFilter {
	
}

@Bean
public SecurityFilterChain filterChain(HttpSecurity http, JwtVerifyFilter jwtFilter) throws Exception {
	http
		 .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
		 .authorizeHttpRequests(req -> req.anyRequest().authenticated());
	return http.build();
}
```

이처럼 서블릿 컨텍스트에 자동 등록된 필터는 Spring Security에 다시 등록하면 중복 실행될 수 있다.

#### 반면, `@Bean`으로만 등록하면

Spring Bean 컨테이너에만 올라가고 SecurityFilterChain 내부에서만 사용하면 중복 없이 안전하게 동작한다.

의도치 않은 중복 실행을 방지하려면 명확한 필터 체인 등록 방식을 사용하는 것이 바람직하다.

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http, JwtVerifyFilter jwtFilter) throws Exception {
	http
		 .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
		 .authorizeHttpRequests(req -> req.anyRequest().authenticated());
	return http.build();
}

@Bean
public JwtVerifyFilter jwtVerifyFilter() {
	return new JwtVerifyFilter(...);
}
```

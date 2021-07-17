# 3부 웹 애플리케이션 시큐리티

## 21. 스프링 시큐리티 ignoring() 1부

WebSecurity의 ignoring()을 사용해서 시큐리티 필터 적용을 제외할 요청을 설정할 수 있다.

```java
@Override
public void configure(WebSecurity web)throws Exception{
  web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
}
```

- 스프링 부트가 제공하는 PathRequest를 사용해서 정적 자원 요청을 스프링 시큐리티 필터를 적용하지 않도록 설정.

로그인 페이지를 요청했는데, 3 가지 요청이 발생한다.

- localhost
- localhost/favicon.io
- localhost/login

브라우저는 자체적으로 `/favicon.ico` 요청을 보낸다.

파비콘에 대한 요청은 모든 시큐리티 필터에 의해 걸리게 되고 로그인 페이지로 리다이렉트하게 된다. 이처럼 스태틱 리소스 요청은 불필요한 서버
리소스를 사용하게 됨으로 이와 관련된 요청 처리들을 예외하기 위해 ignore 설정을 해보자.

1. FilterSecurityInterceptor에서 인증 에러, AuthenticationException 에러가 발생
2. ExceptionTranslationFilter
3. AuthenticationEntryPoint

```java
@Override
public void configure(WebSecurity web)throws Exception{
  web.ignoring().mvcMatchers("/favicon.ico");
}
```

ignore 할 스태틱 리소스에 대한 request uri 처리를 매번 정의하는게 귀찮다.

스프링 부트에선 `PathRequest.toStaticResources().atCommonLocations()` 를 사용하여 간단히 설정할 수 있다.

```java
@Override
public void configure(WebSecurity web) throws Exception {
  web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
}
```

```java
public enum StaticResourceLocation {
  CSS("/css/**"),
  JAVA_SCRIPT("/js/**"),
  IMAGES("/images/**"),
  WEB_JARS("/webjars/**"),
  FAVICON("/favicon.*", "/*/icon-*");

  // ...
}
```

`FilterChainProxy` 에서 확인해보면 ignore 할 요청에 대해선 filter 가 포함되어이지 않게된다.

WebSecurity 에서 ignore를 처리하지 않고 httpRequest chain에 설정을 하게 된다면 어떻게 될까?

```java
@Override
protected void configure(HttpSecurity http) throws Exception{
    http.authorizeRequests()
        .mvcMatchers("/","/info","/account/**").permitAll()
        .mvcMatchers("/admin").hasRole(MemberRole.ADMIN.getValue())
        .mvcMatchers("/user").hasRole(MemberRole.USER.getValue())
        // ...
        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
}
```

`.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();`

authorizeRequests 에 등록되어 있는 모든 필터에 거쳐가야 되기 때문에 결과는 같을 수 있지만, 전혀 다르다.

AnonymousAuthenticationFilter에서 익명(annoymous) Authentication 이 생성이 되고 permitAll 이 되어 있기 때문에 uri 에 대해 허용을 한다. 이 과정에서 시큐리티 필터에 등록되어 있는 모든 필터를 거치게 된다.

즉 정적인 uri 인 경우 WebSecurity 에서 설정으로 ignore 처리하는게 맞고
동적인 uri 인 경우엔 HttpSecurity 에서 필터 체인으로 설정하는게 바르다.
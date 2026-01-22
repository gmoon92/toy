# CORS 설정

- `Access-Control-Allow-Origin : *`
- `*`로 설정되어 있어, 공격자가 `http://attacker1.com`과 같은 악의적인 도메인에서 서버의 민감한 정보를 탈취할 가능성이 존재.

## 1. 개요

`CORS`는 브라우저의 보안 정책이다.

브라우저는 서버에 요청할 때, Origin 헤더에 현재 요청한 도메인 정보를 포함시킨다.

서버는 이 `Origin` 헤더와 설정된 `Access-Control-Allow-Origin` 값을 비교하여, 브라우저의 요청을 허용할지 거부할지 결정한다.

> 만약 **Access-Control-Allow-Origin**이 *로 설정되어 있으면, 모든 도메인에서 요청을 받아들이게 된다. 즉, 어떤 도메인에서든 자원에 접근할 수 있게 되어, 공격자가 악의적인 도메인에서
> 민감한 정보를 탈취할 수 있는 위험이 존재한다.

## 2. CORS Filter

스프링 프로젝트에선 CorsFilter 필터를 제공하고 있다.

우선 CORS 검증이 어떻게 이뤄지는지 doFilterInternal 메서드를 살펴보자.

```java
package org.springframework.web.filter;

public class CorsFilter extends OncePerRequestFilter {

	private final CorsConfigurationSource configSource;
	private CorsProcessor processor = new DefaultCorsProcessor();

	// ...

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
	  FilterChain filterChain) throws ServletException, IOException {

		CorsConfiguration corsConfiguration = this.configSource.getCorsConfiguration(request);
		boolean isValid = this.processor.processRequest(corsConfiguration, request, response);
		if (!isValid || CorsUtils.isPreFlightRequest(request)) {
			return;
		}
		filterChain.doFilter(request, response);
	}
}
```

- CorsConfigurationSource 는 CorsConfiguration 를 제공하는 인터페이스다.
    - CORS 설정을 제공하는 Functional 인터페이스인 `CorsConfiguration`을 반환한다.
- `CorsProcessor`가 주입되지 않았다면, 기본적으로 `DefaultCorsProcessor` 에 의해 검증된다.
    1. DefaultCorsProcessor 는 CorsConfiguration 의 설정 파일에 의해 브라우저 요청 `Access-Control-*` 헤더를 검증한다.
    2. 응답 [Vary](https://datatracker.ietf.org/doc/html/rfc7231#section-7.1.4) 헤더 추가
    3. CORS 요청 확인
    4. preflight 요청 확인
    5. 마지막으로 서블릿 요청의 CORS 검증에 대해 CorsConfiguration 에 위임

## 2. Spring 에서 CORS 설정 방법

스프링 기반 프로젝트에선 CorsFilter 필터를 제공하고 있다.
스프링은 CorsConfigurer.getCorsFilter 메서드를 통해 리턴된 CorsFilter 를 사용하게 되는데, 우선 순위는 다음과 같다.

1. CorsFilter 빈 등록
2. CorsConfigurationSource 빈 등록
3. Web MVC Config HandlerMappingIntrospector 빈 등록
    - WebMvcConfigurationSupport 구현하여 Spring MVC 웹설정을 했다면, 자동 등록 됌
    - HandlerMappingIntrospector 인터셉터는 CorsConfigurationSource 인터페이스를 구현한 매핑 핸들러임.

### 2.1. CorsConfigurer

CorsConfigurer 를 살펴보자.

```java
package org.springframework.security.config.annotation.web.configurers;

public class CorsConfigurer<H extends HttpSecurityBuilder<H>> extends AbstractHttpConfigurer<CorsConfigurer<H>, H> {

	private CorsFilter getCorsFilter(ApplicationContext context) {
		if (this.configurationSource != null) {
			return new CorsFilter(this.configurationSource);
		} else {
			// [1]
			boolean containsCorsFilter = context.containsBeanDefinition("corsFilter");
			if (containsCorsFilter) {
				return (CorsFilter)context.getBean("corsFilter", CorsFilter.class);
			} else {
				boolean containsCorsSource = context.containsBean("corsConfigurationSource");
				if (containsCorsSource) {
					// [2]
					CorsConfigurationSource configurationSource = (CorsConfigurationSource)context.getBean(
					  "corsConfigurationSource", CorsConfigurationSource.class);
					return new CorsFilter(configurationSource);
				} else {
					// [3]
					boolean mvcPresent = ClassUtils.isPresent(
					  "org.springframework.web.servlet.handler.HandlerMappingIntrospector", context.getClassLoader());
					return mvcPresent ? CorsConfigurer.MvcCorsFilter.getMvcCorsFilter(context) : null;
				}
			}
		}
	}
}

```

1. `corsFilter` 빈 이름을 지닌 CorsFilter 타입 확인
2. `corsConfigurationSource` 빈 확인
3. Spring Web MVC 설정 확인
    - `mvcHandlerMappingIntrospector` 빈 확인

## 3. Spring MVC 통합하기

Spring Web MVC 설정을 `WebMvcConfigurationSupport`를 사용하여 구현했다면, 자동으로 CORS 필터가 등록된다.

```java
package org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

	@Override
	@Bean
	@Lazy
	public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
		return new HandlerMappingIntrospector(); // default
	}
}
```

`HandlerMappingIntrospector` 핸들러 인터셉터는 `CorsConfigurationSource` 를 구현된걸 확인할 수 있다.

```java
package org.springframework.web.servlet.handler;

public class HandlerMappingIntrospector
  implements CorsConfigurationSource, // <-- cors 인터페이스 구현
  ApplicationContextAware, InitializingBean {

}
```

### 3.1. 서블릿 필터와 Spring Security

때때로 잘못된 설정으로 인해 **javax.servlet.Filter**를 빈으로 등록한 후, 시큐리티 필터 체인 설정에서도 동일한 필터를 추가하는 경우가 있다.

이 경우 중복 처리가 발생할 수 있기 때문에 주의가 필요하다.

**ServletContextInitializerBeans**에서 서블릿 인스턴스를 초기화하는 시점에, `javax.servlet.Filter` 타입의 구현체가 빈으로 등록되면 *
*FilterRegistrationBean**을 통해 서블릿 컨테이너(예: 톰캣)의 서블릿 필터 체인에 자동으로 포함된다.

`Spring Security`를 설정하면, **springSecurityFilterChain**이라는 이름의 단일 서블릿 필터가 **DelegatingFilterProxy**를 통해 서블릿 필터 체인에 포함된다.

**따라서 서블릿 필터 → springSecurityFilterChain (Spring Security 필터 체인) 순서로 동작하게 된다.**

이때 필터를 빈으로 등록하고 필터 체인에 등록하면 필터가 두 번 호출될 수 있다.

### 3.2. [Spring Security CORS 비활성화 처리](https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html)

CORS 필터는 특별히 주의가 필요하다.

**반드시 `CORS`는 `Spring Security`보다 먼저 처리되어야 한다.**

그 이유는 `preflight` 요청에는 쿠키(예: JSESSIONID)가 포함되지 않는다. 만약 `Spring Security`가 먼저 처리되면, 쿠키가 없기 때문에 인증되지 않은 사용자로 판단되어 요청이 거부된다. 따라서 `CORS`를 먼저 처리하도록 설정해야 한다.

가장 간단한 방법은 **CorsWebFilter**를 사용하여 `Spring Security`와 통합하는 것이다. 이를 통해 `CORS`가 먼저 처리되도록 할 수 있다.

```java
package org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

	@Bean
	public UrlBasedCorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("https://example.com"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
```

````java

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
		  // ...
		  .cors(AbstractHttpConfigurer::disable) // CORS 비활성화 설정
		  .build();
	}
}
````

## 4. 개발 범위

- [Access-Control-Allow-Credentials](#41-access-control-allow-credentials)
- [Access-Control-Expose-Headers](#42-access-control-expose-headers)

### 4.1. Access-Control-Allow-Credentials

CORS는 기본적으로 보안상의 이유로 `HTTP Cookie` 와 `HTTP Authentication` 헤더 정보를 요청할 수 없도록 제약한다.

- Access-Control-Allow-Credentials: true
- Access-Control-Allow-Origin: https://gmoon.com

만약 Credentials 옵션을 활성화하고, 허용 Origin 을 와일드 카드로 설정해놨다면 서버에선 와일드 카드를 사용할 수 없다는 에러가 발생한다.

```java
CorsConfiguration config = new CorsConfiguration();
config.setAllowCredentials(true);
config.setAllowedOrigins(Arrays.asList("gmoon.com"));

// 주의: 허용 Origin에 대해 패턴 설정으로 와일드 카드로 설정했다면, Credential 요청에 대한 검증은 제외된다.
// config.addAllowedOriginPattern("*");
```

추가적으로 Fetch API 나 XMLHttpRequest 에서도 요청에 Cookie 나 Authentication 헤더를 포함하도록 별도로 설정해줘야 한다.

- Fetch API
  ```javascript
  fetch("https://gmoon.com/v2", {
    credentials: "include",
  });
  ```
- AXIOS
    ```javascript
    axios.get("https://gmoon.com/v2", {
      withCredentials: true,
    });
    ```

### 4.2. [Access-Control-Expose-Headers](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Access-Control-Expose-Headers)

클라이언트는 `CORS-safelisted` 응답 헤더를 제외한 모든 응답 헤더에 접근할 수 없다.

- [CORS-safelisted 응답 헤더](https://developer.mozilla.org/en-US/docs/Glossary/CORS-safelisted_response_header)
  - By default, the safelist includes the following response headers:
    - Cache-Control
    - Content-Language
    - Content-Length
    - Content-Type
    - Expires
    - Last-Modified
    - Pragma

예를 들어 사용자 정의 헤더(`X-AUTH-TOKEN`)를 브라우저의 스크립트에서 접근할 수 있도록 하려면, 서버는 `Access-Control-Expose-Headers` 응답 헤더에 정의한다. 

```java
CorsConfiguration config = new CorsConfiguration();
config.addExposedHeader("X-Auth-Token");
```

## Reference

- [Spring Framework - CORS](https://docs.spring.io/spring-framework/reference/web/webflux-cors.html#webflux-cors-intro)

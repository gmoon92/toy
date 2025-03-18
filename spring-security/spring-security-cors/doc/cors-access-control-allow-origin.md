# CORS 설정 미흡

- `Access-Control-Allow-Origin : *`
- `*`로 설정되어 있어, 공격자가 `http://attacker1.com`과 같은 악의적인 도메인에서 서버의 민감한 정보를 탈취할 가능성이 존재.

## 1. 개요

`CORS`는 브라우저의 보안 정책이다.

브라우저는 서버에 요청할 때, Origin 헤더에 현재 요청한 도메인 정보를 포함시킨다.

서버는 이 `Origin` 헤더와 설정된 `Access-Control-Allow-Origin` 값을 비교하여, 브라우저의 요청을 허용할지 거부할지 결정한다.

> 만약 **Access-Control-Allow-Origin**이 *로 설정되어 있으면, 모든 도메인에서 요청을 받아들이게 된다. 즉, 어떤 도메인에서든 자원에 접근할 수 있게 되어, 공격자가 악의적인 도메인에서 민감한 정보를 탈취할 수 있는 위험이 존재한다.

## 2. CORS 설정 방식

스프링 기반 프로젝트에선 CorsFilter 필터를 제공하고 있다.
스프링은 CorsConfigurer.getCorsFilter 메서드를 통해 리턴된 CorsFilter 를 사용하게 된다.

1. CorsFilter 빈 등록
2. CorsConfigurationSource 빈 등록
3. Web MVC Config HandlerMappingIntrospector 빈 등록

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
                    CorsConfigurationSource configurationSource = (CorsConfigurationSource)context.getBean("corsConfigurationSource", CorsConfigurationSource.class);
                    return new CorsFilter(configurationSource);
                } else {
                    // [3]
                    boolean mvcPresent = ClassUtils.isPresent("org.springframework.web.servlet.handler.HandlerMappingIntrospector", context.getClassLoader());
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

```java
package org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

	@Override
	@Bean
	@Lazy
	public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
		return new HandlerMappingIntrospector();
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

## 3. [Spring Security CORS 비활성화 처리](https://docs.spring.io/spring-security/reference/reactive/integrations/cors.html)

### 3.1. 서블릿 필터와 Spring Security

때때로 잘못된 설정으로 **javax.servlet.Filter**를 빈으로 등록한 후, 시큐리티 필터 체인 설정에서도 동일한 필터를 설정하는 경우가 있다.

이 경우 중복 처리가 발생할 수 있기 때문에 주의가 필요하다.

**ServletContextInitializerBeans**에서 서블릿 인스턴스를 초기화하는 시점에, `javax.servlet.Filter` 타입의 구현체가 빈으로 등록되면 **FilterRegistrationBean**을 통해 서블릿 컨테이너(예: 톰캣)의 서블릿 필터 체인에 자동으로 포함된다.

`Spring Security`를 설정하면, **springSecurityFilterChain**이라는 이름의 단일 서블릿 필터가 **DelegatingFilterProxy**를 통해 서블릿 필터 체인에 포함된다.

**따라서 서블릿 필터 → springSecurityFilterChain (Spring Security 필터 체인) 순서로 동작하게 된다.**

이때 필터를 빈으로 등록하고 필터 체인에 등록하면 필터가 두 번 호출될 수 있다.

### 3.1. Spring Security CORS 비활성화 설정

CORS 필터는 특별히 주의가 필요하다.

**반드시 `CORS`는 `Spring Security`보다 먼저 처리되어야 한다.** 

그 이유는 `preflight 요청`에는 쿠키(예: JSESSIONID)가 포함되지 않기 때문이다. 만약 `Spring Security`가 먼저 처리되면, 쿠키가 없기 때문에 사용자가 인증되지 않았다고 판단하고 요청을 거부하게 된다. 따라서 `CORS`를 먼저 처리하도록 설정해야 한다. 

가장 간단한 방법은 **CorsWebFilter**를 사용하여 `Spring Security`와 통합하는 것이다. 이를 통해 `CORS`가 먼저 처리되도록 할 수 있다.

```java
package org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://example.com"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
	
	@Override
	@Bean
	@Lazy
	public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
		return new HandlerMappingIntrospector();
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

## 4. CORS 확장 포인트

CORS 설정을 수정할 수 있는 방법은 CorsFilter.doFilterInternal 메서드를 살펴보면 된다.

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

- CorsConfigurationSource 는 필수다.
- 설정이 없다면 기본적으로 DefaultCorsProcessor 에 의해 판단한다.
- DefaultCorsProcessor 는 CorsConfiguration 의 설정 파일에 의해 브라우저 요청 `Access-Control-*` 헤더를 검증한다.

## Reference

- [Spring Framework - CORS](https://docs.spring.io/spring-framework/reference/web/webflux-cors.html#webflux-cors-intro)

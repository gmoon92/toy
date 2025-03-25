# Web Access Log

- AbstractRequestLoggingFilter
- ContentCachingRequestWrapper
- ContentCachingResponseWrapper

## 1. ContentCachingRequestWrapper

`ServletRequest`의 `InputStream`은 한 번만 읽을 수 있기 때문에, 요청 본문을 여러 번 읽기가 어렵다.  
`ContentCachingRequestWrapper`는 요청 본문이 처음 읽힐 때 내부적으로 캐싱하여, 이후에도 다시 읽을 수 있도록 한다.

### 1.1. 데코레이터 패턴과 주의 사항

`ServletRequest`는 데코레이터 패턴을 활용해 확장 가능하도록 설계되어 있다.

여러 개의 커스텀 필터 또는 인터셉터에서 불필요하게 `ContentCachingRequestWrapper`를 중복 사용하여 래핑되지 않도록 주의해야 한다.

중첩된 래핑은 객체의 깊이를 증가시키고, 불필요한 메모리 사용을 초래할 수 있다. 데코레이터 패턴 개념을 정확히 이해하고 활용해야 한다.

### 1.2. 불필요한 래핑된 HttpServletRequestWrapper 객체

필터나 인터셉터에서 요청을 가로채면, 서블릿 요청이 래핑될 수 있다.

```java
package jakarta.servlet;

public class ServletRequestWrapper implements ServletRequest {
    private ServletRequest request;
}
```
```java
package jakarta.servlet.http;
public class HttpServletRequestWrapper
        extends ServletRequestWrapper
        implements HttpServletRequest
{
    public HttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }
}
```

이처럼 래핑된 객체가 사용되므로, `instanceof`를 이용한 직접 비교는 지양해야 한다.

```java
// 래핑된 요청 객체를 직접 비교하는 것은 의미 없음.
boolean cachedContent = ContentCachingRequestWrapper.class.isInstance(request);
```

서블릿 요청 객체(`ServletRequest`)는 데코레이터 패턴을 사용하여 기존 요청 객체를 참조(Composition)하는 방식으로 확장되기 때문에,  
**여러 필터/인터셉터에서 중복 래핑이 발생하면 불필요한 객체가 계속 생성**될 수 있다.

```text
ContentCachingRequestWrapper -> 불필요한 중복 발생
 └ CacheableRequestWrapper
    └ ContentCachingRequestWrapper -> 불필요한 중복 발생
        └ HttpServletRequestWrapper
            └ ServletRequestWrapper
                └ ServletRequest
```

1. **메모리 낭비**
    - 불필요한 래핑 객체가 계속 생성되면서 **객체 참조 체인이 길어짐**
    - 가비지 컬렉션(GC) 부담 증가 → 성능 저하 가능
2. **성능 저하**
    - `instanceof` 체크를 반복해야 해서 **메서드 호출 비용 증가**
    - 불필요한 객체 탐색 비용이 누적됨

## 2. 구현

### 요구사항: 특정 테이블에 데이터 변경 시점을 추적해주세요.

- **Filter**를 통해 클라이언트의 HTTP payload 정보를 로깅하고, 이를 **스레드 로컬 변수(ThreadLocal)**에 담아 요청 간에 데이터를 분리하여 관리한다.
- **AOP**를 활용하여 계층 간 제약을 없애고, 컨트롤러뿐만 아니라 서비스와 레파지토리 계층에서도 로깅된 데이터를 **변경 이력 테이블**에 저장한다.
    - 이는 **특정 비즈니스 로직**에 의해 변경된 데이터를 추적하기 위함이다.
    - 컨트롤러 계층에서 로깅 처리에 제약이 있을 수 있으므로, **중복된 비즈니스 로직은 서비스 계층에 배치**하여 로깅과 변경 사항 추적을 더 유연하게 처리한다.
- 로깅 데이터를 저장하는 시점에, 필터에서 수집한 클라이언트의 HTTP 요청 정보를 **스레드 로컬 변수**에서 꺼내어 해당 요청과 관련된 데이터를 함께 저장한다.
    - 이때, 요청이 여러 필터를 거칠 수 있기 때문에, **ThreadLocal**을 통해 요청 정보를 안전하게 추적하고, DB에 저장할 때 해당 정보를 활용한다.


> 클라이언트 요청 정보를 로깅하기 위해서는 Spring의 **Filter**, **Interceptor**, **AOP**를 활용할 수 있다. 요구사항에 맞게 구현 방식을 선택한다. [참고 Spring Filter vs Interceptor vs AOP](docs/spring-filter-interceptor-aop.md)

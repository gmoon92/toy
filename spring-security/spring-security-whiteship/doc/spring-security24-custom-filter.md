# Custom Filter

### 커스텀 필터 추가하기, LoggingFilter.java

커스텀 시큐리티 필터는 GenericFilterBean를 상속받아 구현하면 쉽게 구현할 수 있다.

```java
public class LoggingFilter extends GenericFilterBean {

  private Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
          throws IOException, ServletException {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start(( (HttpServletRequest) request ).getRequestURI());
    chain.doFilter(request, response); // 다음 필터 요청
    stopWatch.stop();
    logger.info(stopWatch.prettyPrint());
  }
}
```

### 커스텀 필터 추가 설정 

`http.addFilter*()`

```text
http.addFilterAfter(new LoggingFilter(), // 추가할 필터
                    UsernamePasswordAuthenticationFilter.class);  // 실행 순서의 기준이 될 필터 정의
```


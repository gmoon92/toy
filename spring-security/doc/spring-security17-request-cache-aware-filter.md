# RequestCacheAwareFilter

요청 캐시 필터: RequestCacheAwareFilter

현재 요청과 관련 있는 캐시된 요청이 있는지 찾아서 적용하는 필터.

- 캐시된 요청이 없다면, 현재 요청 처리
- 캐시된 요청이 있다면, 해당 캐시된 요청 처리

```java
public class RequestCacheAwareFilter extends GenericFilterBean {

  private RequestCache requestCache;

  public RequestCacheAwareFilter() {
    this(new HttpSessionRequestCache());
  }

  public RequestCacheAwareFilter(RequestCache requestCache) {
    Assert.notNull(requestCache, "requestCache cannot be null");
    this.requestCache = requestCache;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    HttpServletRequest wrappedSavedRequest = this.requestCache.getMatchingRequest((HttpServletRequest) request,
            (HttpServletResponse) response);
    chain.doFilter(( wrappedSavedRequest != null ) ? wrappedSavedRequest : request, response);
  }

}
```

request dashboard 요청 -> 권한 업음 -> 로그인 페이지
request 로그인 -> 캐싱된 dashboard 페이지로 이동


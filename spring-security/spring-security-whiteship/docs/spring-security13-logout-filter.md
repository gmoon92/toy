# 로그아웃 처리 필터: LogoutFilter

LogoutFilter 는 로그아웃과 관련된 필터이다.

```java
public class LogoutFilter extends GenericFilterBean {
  private RequestMatcher logoutRequestMatcher;
  private final LogoutHandler handler;
  private final LogoutSuccessHandler logoutSuccessHandler;
  // ... 생략
}
```

로그아웃 필터는 내부적으로 `LogoutHandler`, `LogoutSuccessHandler`를 통해 로그아웃 처리를 한다.

- LogoutHandler
- LogoutSuccessHandler

`LogoutHandler` 는 Composite Type으로 `LogoutFilter는 여러 `LogoutHandler`를 사용하여 로그아웃시 필요한 처리를 하며 이후에는 `LogoutSuccessHandler`를 사용하여 로그아웃 후처리를 한다.

> Composite Pattern 참고, DefaultLogoutPageGeneratingFilter

LogoutHandler

- CsrfLogoutHandler
- SecurityContextLogoutHandler

LogoutSuccessHandler
- SimpleUrlLogoutSuccessHandler

> `/logout` Get 요청시 응답하는 페이지는 DefaultLogoutPageGeneratingFilter 가 처리한다.


## 로그아웃 필터 설정

```java
http.logout()
    .logoutUrl("/logout")
    .logoutSuccessUrl("/")
    .logoutRequestMatcher()
    .invalidateHttpSession(true)
    .deleteCookies()
    .addLogoutHandler()
    .logoutSuccessHandler();
```

![img](images/logout-filter.png)


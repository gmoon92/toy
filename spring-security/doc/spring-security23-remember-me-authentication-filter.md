# RememberMeAuthenticationFilter

토큰 기반 인증 필터
세션이 사라지거나 만료가 되더라도 쿠키 또는 DB를 사용하여 저장된 토큰 기반으로 인증을 지원하는 필터

추가적인 필터로써 `rememberMe` 기능을 지원한다.

`rememberMe` 기능의 예시를 들어보자면, 웹 사이트에 로그인 기억하기 체크 박스 기능이 있고 이를 통해 직접적으로 로그인을 하여 인증을 받지 않고 인증을 할 수 있는 기능이다.

즉 세션이 종료 및 만료가 되어도 사용자 인증 처리를 할 수 있는데, 브라우저나 세션의 생명 주기가 보다 긴 쿠키 또는 DB에 저장된 토큰 기반으로 인증을 지원한다.

### RememberMe 설정

``` text
http.rememberMe()
    .userDetailsService(accountService)
    .key("remember-me-sample"); // parameter 
                                // default 'remember-me'
```

### Test RememberMe

참고로 크롬 `EditThisCookie` 플러그인을 사용하면 현재 웹 사이트에서 사용하고 있는 쿠키 정보를 볼 수 있다.

> https://chrome.google.com/webstore/detail/editthiscookie/fngmhnnpilhplaeedifhccceomclgfbg?hl=en

1. JSessionId 쿠키 제거 
   - JSessionId 는 Session id 를 담고 있는 쿠키이다.
   - 요청과 관련된 Session 정보를 얻어, 시큐리티는 세션의 정보로 인증 처리를 한다.
2. rememberMe 기능을 활성화 한다.
3. `remember-me` 쿠키 생성 확인
   - username: 사용자 정보
   - 기한: 쿠기 유효 날짜
4. JSessionId 쿠키를 제거된 상태에서 로그인이 유지가 되는지 확인한다.






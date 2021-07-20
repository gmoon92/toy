# HeaderWriterFilter
## 시큐리티 관련 헤더 추가하는 필터

응답 헤더에 시큐리티 관련 헤더를 추가해주는 필터

- XContentTypeOptionsHeaderWriter: 마임 타입 스니핑 방어.
  - 실행할 수 없는 MIME 타입이 아닌 Content-Type 인데도 서버의 요청을 하여 다운로드하거나 악의적인 스크립트를 실행할 수 있는걸 방지하기 위함
- XXssProtectionHeaderWriter: 브라우저에 내장된 XSS 필터 적용.
  - 브라우저에 내장된 XSS 필터로썬 모든 공격을 방지할 수 없기 때문에 lucy filter를 적용해볼만 하다.
- CacheControlHeadersWriter: 캐시 히스토리 취약점 방어.
  - 정적인 리소스일 경우엔 캐시 적용이 효율적이지만, 서버 사이드에서 무언갈 실행하고 할 때, 민감한 정보를 노출될 가능성이 높다. 브라우저 히스토리를 빼서 본다거나, 캐싱된 데이터를 탈취를 가능해서 캐시정보를 비어주는 역할을 하고 있다.
- HstsHeaderWriter: HTTPS로만 소통하도록 강제.
- XFrameOptionsHeaderWriter: clickjacking 방어.
    - 보이지 않는 영역에 iframe 이나 object http에 숨겨두고 나의 정보를 보내는걸 방지하는

```text
CacheControlHeadersWriter           Cache-Control: no-cache, no-store, max-age=0, must-revalidate
                                    Content-Language: en-US
                                    Content-Type: text/html;charset=UTF-8
                                    Date: Sun, 04 Aug 2019 16:25:10 GMT
CacheControlHeadersWriter           Expires: 0
CacheControlHeadersWriter           Pragma: no-cache
                                    Transfer-Encoding: chunked
XContentTypeOptionsHeaderWriter     X-Content-Type-Options: nosniff
XFrameOptionsHeaderWriter           X-Frame-Options: DENY
XXssProtectionHeaderWriter          X-XSS-Protection: 1; mode=block
```

## 참고
- X-Content-Type-Options:
    - https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Content-Type-Options
- Cache-Control:
    - https://www.owasp.org/index.php/Testing_for_Browser_cache_weakness_(OTG-AUTHN-006)
- X-XSS-Protection
    - https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-XSS-Protection
    - https://github.com/naver/lucy-xss-filter
- HSTS
    - https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Strict_Transport_Security_Cheat_Sheet.html
- X-Frame-Options
    - https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options
    - https://cyberx.tistory.com/171
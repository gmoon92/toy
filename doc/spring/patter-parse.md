# [PathPattern](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/util/pattern/PathPattern.html)

## PathPattern 개요

**PathPattern**은 URL 경로를 패턴 매칭하는 기능을 제공하는 클래스다. 
빠른 비교 및 매칭을 위해 경로 요소를 체인으로 유지하며, 특정 규칙을 따른다.

## 매칭 규칙

- `?` → 한 문자와 매칭
- `*` → 경로 세그먼트 내에서 0개 이상의 문자와 매칭
- `**` → 경로의 끝까지 0개 이상의 경로 세그먼트와 매칭 (**단, 패턴 끝에서만 사용 가능**)
- `{변수명}` → 해당 경로 세그먼트를 변수로 캡처
- `{변수명:정규식}` → 정규식을 적용하여 변수로 캡처
- `{*변수명}` → 경로의 끝까지 0개 이상의 세그먼트와 매칭 후 변수로 캡처 (**단, 패턴 끝에서만 사용 가능**)

---

## 사용 예시

| 패턴 | 매칭 예시 | 설명 |
|------|----------|------|
| `/pages/t?st.html` | `/pages/test.html`, `/pages/tXst.html` | `?`는 한 글자 매칭 |
| `/resources/*.png` | `/resources/logo.png`, `/resources/bg.png` | `*`는 세그먼트 내 여러 글자 매칭 |
| `/resources/**` | `/resources/img/logo.png`, `/resources/css/style.css` | `**`는 모든 하위 경로 매칭 |
| `/resources/{*path}` | `/resources/img/logo.png` → `"path"="/img/logo.png"` | `*path`는 끝까지 모든 경로를 변수로 저장 |
| `/resources/{filename:\\w+}.dat` | `/resources/spring.dat` → `"filename"="spring"` | 정규식 적용 가능 |

## 주의사항

**`**`와 `{*변수}`는 패턴 끝에서만 사용 가능**

- ✅ 올바른 예시: `/pages/{**}`
- ❌ 잘못된 예시: `/pages/{**}/details`

이러한 패턴을 통해 다양한 URL 매칭이 가능하며, **[AntPathMatcher](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html)보다 명확한 규칙을 적용**하여 
패턴 비교의 모호성을 줄이는 것이 목표다.

## Reference

- [PathPattern](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/util/pattern/PathPattern.html)
- [AntPathMatcher](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/AntPathMatcher.html)

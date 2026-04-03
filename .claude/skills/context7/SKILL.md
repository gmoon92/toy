---
name: context7
description: |
  라이브러리/프레임워크의 최신 공식 문서를 검색하고 가져옵니다.
  context7 MCP 대체 스킬 — WebSearch + WebFetch 기반.

  다음 상황에서 사용합니다:
  - "React 최신 문서 찾아줘"
  - "Next.js App Router 사용법 알려줘"
  - 특정 라이브러리의 API, 설정, 마이그레이션 가이드가 필요할 때
user-invocable: true
---

# context7 스킬

라이브러리 이름(과 선택적으로 토픽)을 받아 공식 문서를 실시간으로 가져옵니다.

## Workflow

### 1단계 — 문서 URL 탐색

`WebSearch`로 공식 문서 URL을 찾습니다.

검색 쿼리 패턴:
```
<library> official documentation site:docs.<library>.dev OR site:npmjs.com OR site:github.com
```

토픽이 있으면 쿼리에 포함:
```
<library> <topic> official documentation
```

### 2단계 — 문서 fetch

`WebFetch`로 검색 결과 상위 문서 페이지를 가져옵니다.

- 공식 docs 사이트 우선 (docs.*, *.dev, *.io/docs 등)
- GitHub README는 공식 docs가 없을 때만 사용
- 최대 2개 페이지 fetch (중복 방지)

### 3단계 — 핵심 내용 요약 제공

가져온 문서에서 사용자 질문에 관련된 부분만 추출해서 제공합니다.

- 코드 예제 우선 포함
- API 시그니처 / 옵션 테이블 포함
- 불필요한 마케팅 문구 제거

## 출력 형식

```
## [라이브러리명] — [토픽]
출처: <URL>

[핵심 내용 요약]

### 코드 예제
[예제 코드]
```

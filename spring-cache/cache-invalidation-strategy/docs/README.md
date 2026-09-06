# 문서 지도

## 질문 → 문서

| 이런 질문이라면 | 문서 |
|--------------|-----|
| 전체 그림부터 보고 싶다 | [concepts/overview.md](concepts/overview.md) |
| aside · through · around 가 무슨 뜻인가 | [concepts/overview.md](concepts/overview.md#이름이-말해주는-것) |
| 이 이름들이 어디서 나온 정식 용어인가 | [concepts/canonical-patterns.md](concepts/canonical-patterns.md) |
| 메모리가 꽉 차면 무엇이 밀려나나 | [eviction/](eviction/) |
| Redis가 죽으면 서비스는 어떻게 되나 | [concepts/cache-failure-modes.md](concepts/cache-failure-modes.md) |
| 캐시 값을 어떤 형식으로 저장하나 | [concepts/serialization.md](concepts/serialization.md) |
| 배포 중 캐시 스키마가 바뀌면 | [concepts/schema-evolution.md](concepts/schema-evolution.md) |
| 코어 모듈은 어떻게 구성되어 있나 | [core-design.md](core-design.md) |
| 조회 시 캐시를 어떻게 읽나 | [read/](read/) |
| 쓰기가 캐시를 거쳐야 하나 | [write/](write/) |
| DB가 바뀌면 옛 캐시를 언제 지우나 | [invalidation/](invalidation/) |
| 왜 갱신이 아니라 삭제인가 | [concepts/why-invalidate-not-update.md](concepts/why-invalidate-not-update.md) |
| TTL을 얼마로 잡아야 하나 | [concepts/ttl.md](concepts/ttl.md) |
| 어느 수준까지 정합성을 맞춰야 하나 | [concepts/consistency-levels.md](concepts/consistency-levels.md) |
| DB와 Redis를 한 트랜잭션으로 못 묶는데 | [concepts/dual-write.md](concepts/dual-write.md) |
| 무효화 직후 DB가 터진다 | [concepts/stampede.md](concepts/stampede.md) |
| 무엇을 측정해야 하나 | [concepts/metrics.md](concepts/metrics.md) |
| 지연 이중 삭제는 왜 안 쓰나 | [appendix/non-standard-techniques.md](appendix/non-standard-techniques.md) |
| 무엇을 실험할 것인가 | [poc-scenarios.md](poc-scenarios.md) |

## 구조

| 디렉토리 | 내용 |
|--------|-----|
| `concepts/` | 전략과 무관하게 항상 성립하는 공통 관점 |
| `read/` | 읽기 전략 |
| `write/` | 쓰기 전략 |
| `invalidation/` | 무효화 시점 전략 (전략당 1문서) |
| `eviction/` | 메모리 압박에 의한 축출 |
| `appendix/` | 비표준 기법과 잘못 알려진 주장 |

읽기·쓰기 전략, 무효화 시점, 메모리 축출은 **서로 직교하는 네 축**이다.
`Write Around`는 "캐시에 쓰지 않는다"만 정할 뿐, 옛 캐시를 언제 지울지는 정하지 않는다.
그리고 무효화를 잘 해도 메모리가 부족하면 **우리가 지우지 않은 키가 사라진다.**

## 전략 문서 템플릿

| 섹션 | 목적 | 적용 |
|-----|-----|-----|
| 서두 | 이름의 뜻과 그림이 그려지는 한 문단 | 전체 |
| 동작 | mermaid 다이어그램 1개 | 전체 |
| 구현 | 전략을 구분짓는 코드만 | 전체 |
| 장점 / 단점 | 데이터 흐름의 득실 | `read/`, `write/` |
| 보장하는 것 | 이 전략이 실제로 해결하는 것 | `invalidation/` |
| **보장하지 않는 것** | **과장을 막는 필수 섹션** | `invalidation/` |
| 선택 기준 | 적합 / 부적합 | 전체 |
| 검증 | 동작을 증명하는 테스트 명제 | `invalidation/` |

`invalidation/`은 "무엇을 보장하지 않는가"를 필수로 둔다.
무효화는 반드시 실패하는 경로가 있고, 그것을 명시하지 않으면 문서가 과장된다.

## 작성 규칙

| 규칙 | 이유 |
|-----|-----|
| 다이어그램은 mermaid | ASCII 아트는 폰트·화면에 따라 깨진다 |
| 같은 표를 두 문서에 넣지 않는다 | 단일 출처. 중복이 비대화의 원인 |
| 측정값은 실측 후 기입 | 추정치를 표에 넣지 않는다. 미측정은 `미측정`으로 명시 |
| 예시에 실제 조직·인물 없음 | `team`, `alice` 같은 일반 명칭 사용 |

# Cache Invalidation Strategy

DB를 Source of Truth로 두고 Redis를 앞단 캐시로 구성할 때,
**읽기·쓰기 전략**과 **무효화 시점**을 전략별 하위 모듈로 학습한다.

```text
READ   Cache hit → Redis 반환 / miss → DB 조회 후 Cache 저장
CUD    DB 변경 → 기존 Cache를 어떻게 처리할 것인가?
```

기준 구성은 `Look Aside + Write Around + 무효화 + TTL`이며,
규모가 커질 때 이 패턴을 버리는 것이 아니라 필요한 만큼 안전장치를 덧붙인다.

- Redis / MySQL: `_settings/docker/docker-compose.yml`
- 문서 지도: [docs/README.md](docs/README.md)

## 네 축

읽기·쓰기 전략, 무효화 시점, 메모리 축출은 **서로 직교한다.**
`Write Around`는 "캐시에 쓰지 않는다"만 정할 뿐, 옛 캐시를 언제 지울지는 정하지 않는다.

| 축 | 결정하는 것 | 문서 |
|---|-----------|-----|
| 읽기·적재 | 누가 DB를 읽는가 | [docs/read/](docs/read/) |
| 쓰기 | 쓰기가 캐시를 거치는가 | [docs/write/](docs/write/) |
| 무효화 | 옛 캐시를 언제 어떻게 지우는가 | [docs/invalidation/](docs/invalidation/) |
| 축출 | 메모리가 부족하면 무엇이 밀려나는가 | [docs/eviction/](docs/eviction/) |

이름의 출처와 공식 정의(Oracle Coherence 4대 전략, Microsoft Cache-Aside 패턴)는
[canonical-patterns](docs/concepts/canonical-patterns.md) 참조.

## 읽기 전략

| 전략 | DB 조회 주체 | 캐시 장애 시 | 문서 |
|-----|------------|------------|-----|
| Look Aside (Cache Aside) | 애플리케이션 | 서비스 유지 | [look-aside](docs/read/look-aside.md) |
| Read Through | 캐시 | 서비스 중단 위험 | [read-through](docs/read/read-through.md) |
| Refresh Ahead | 캐시 (만료 전 선갱신) | 서비스 중단 위험 | [refresh-ahead](docs/read/refresh-ahead.md) |

## 쓰기 전략

| 전략 | 쓰기 대상 | 유실 위험 | 문서 |
|-----|---------|---------|-----|
| Write Around | DB만 | 없음 | [write-around](docs/write/write-around.md) |
| Write Through | 캐시 + DB (동기) | 없음 | [write-through](docs/write/write-through.md) |
| Write Back | 캐시 → 배치로 DB | flush 전 유실 | [write-back](docs/write/write-back.md) |

## 무효화 전략

| 전략 | DB 변경 시 행동 | 구현 복잡도 | 장애 내성 | 문서 |
|-----|--------------|----------|---------|-----|
| TTL Only | 아무것도 안 함 | 매우 낮음 | 높음 | [ttl-only](docs/invalidation/ttl-only.md) |
| Write Invalidate | 즉시 `DEL` | 낮음 | 중간 | [write-invalidate](docs/invalidation/write-invalidate.md) |
| After Commit Invalidate | 커밋 후 `DEL` | 낮음~중간 | 중간 | [after-commit-invalidate](docs/invalidation/after-commit-invalidate.md) |
| Entity Event Invalidate | 엔티티 post-commit 이벤트로 `DEL` | 중간 | 중간 | [entity-event-invalidate](docs/invalidation/entity-event-invalidate.md) |
| Event Driven Invalidate | 이벤트 소비자가 `DEL` | 중간~높음 | 높음 | [event-driven-invalidate](docs/invalidation/event-driven-invalidate.md) |
| Outbox Invalidate | Outbox → MQ → `DEL` | 높음 | 높음 | [outbox-invalidate](docs/invalidation/outbox-invalidate.md) |
| CDC Invalidate | binlog → 이벤트 → `DEL` | 높음 | 높음 | [cdc-invalidate](docs/invalidation/cdc-invalidate.md) |
| Versioned Cache | 버전 변경으로 기존 키 폐기 | 높음 | 높음 | [versioned-cache](docs/invalidation/versioned-cache.md) |

## 전략 조합

| 조합 | 특징 |
|-----|-----|
| **Look Aside + Write Around** | **가장 일반적.** 이 저장소의 기준 구성 |
| Read Through + Write Around | 항상 DB에 쓰므로 정합성 안전장치가 강하다 |
| Read Through + Write Through | 쓸 때 캐시에 먼저 쓰므로 읽을 때 최신 보장 (예: AWS DAX) |
| Look Aside + Write Back | 쓰기 부하는 낮지만 유실 위험 — 로그성 데이터 한정 |

## 정합성 수준별 권장 구성

| 수준 | 요구사항 | 권장 구성 |
|-----|--------|---------|
| Level 0 | 몇 분 전 데이터여도 무방 | Look Aside + Write Around + TTL Only |
| Level 1 | 평소 거의 즉시, 장애 시 몇 분 stale 허용 | + After-Commit / Entity Event 무효화 |
| Level 2 | 무효화 유실까지 막아야 함 | + Outbox/CDC + Durable MQ + 멱등 무효화 |
| Level 3 | 본인 쓰기 직후 조회는 반드시 최신 | + 쓰기 주체 캐시 우회 |
| Level 4 | 모든 사용자에게 즉시 최신값 | 캐시 사용 자체를 재검토 |

상세는 [consistency-levels](docs/concepts/consistency-levels.md) 참조.

## 모듈

| 모듈 | 역할 |
|-----|-----|
| `cache-invalidation-core` | 공통 틀 — 캐시 정책, 무효화 규칙, 엔티티 이벤트 어댑터, 측정 하네스 |
| 전략별 모듈 | 코어를 사용해 각 전략의 세부 구현만 담당 |

시나리오와 측정 계획은 [poc-scenarios](docs/poc-scenarios.md) 참조.

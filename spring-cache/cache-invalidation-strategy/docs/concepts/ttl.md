# TTL

TTL은 메모리 관리 수단이 아니라 **정합성의 상한선**이다.

## 역할

무효화가 실패할 수 있는 경로는 항상 존재한다.

```mermaid
flowchart LR
    C[DB COMMIT 성공] --> X{{"애플리케이션 종료 · Redis 장애 · 이벤트 유실"}}
    X --> D[Cache DEL 미실행]
    D --> S[stale 잔존]
```

TTL을 10분으로 두었다는 것은 다음을 의미한다.

> 무효화가 완전히 실패해도 stale 데이터가 살아있을 수 있는 최대 시간을 10분으로 제한한다.

| 장치 | 역할 |
|-----|-----|
| Cache Evict | 정상 CUD 경로에서의 빠른 최신화 |
| TTL | 경합·유실·장애에 대한 최종 안전장치 |

Redis는 client-side caching 문서에서 이 원칙을 명시적으로 권고한다.

> "Putting a max TTL on every key is a good idea, **even if it has no TTL**.
> This protects against bugs or connection issues that would make the client have old data in the local copy."

무효화 메시지를 신뢰하는 구성에서도 TTL을 두라는 뜻이다.

## 만료는 즉시 일어나지 않는다

Redis의 키 만료는 두 방식으로 동작한다.

| 방식 | 동작 |
|-----|-----|
| Passive (lazy) | 클라이언트가 키에 접근할 때 만료 여부를 검사해 삭제 |
| Active | 만료 시간이 설정된 키 중 **일부를 무작위 샘플링**해 주기적으로 삭제 |

따라서 TTL이 0에 도달한 시점과 실제 삭제 시점이 다를 수 있다.

> "There are no guarantees that the Redis server will be able to generate the `expired` event
> at the time the key time to live reaches the value of zero."
> "If no command targets the key constantly, and there are many keys with a TTL associated,
> there can be a **significant delay**."

`expired` 이벤트는 **TTL이 0이 될 때가 아니라 Redis가 키를 실제로 삭제할 때** 발생한다.
만료 이벤트를 무효화 신호로 쓰려는 설계는 이 지연을 감안해야 한다.

## SET은 기존 TTL을 버린다

가장 자주 밟는 함정이다.

| 순서 | 명령 | 이후 TTL |
|-----|-----|---------|
| 1 | `SET mykey "Hello"` | 없음 |
| 2 | `EXPIRE mykey 10` | 10 |
| 3 | `SET mykey "Hello World"` | **-1 — 만료되지 않는 키가 된다** |

| 명령 | TTL 영향 |
|-----|---------|
| `SET` | 기존 TTL을 **버린다**. 유지하려면 `KEEPTTL` |
| `EXPIRE` | 값을 건드리지 않고 TTL만 갱신 |
| `INCR`, `LPUSH`, `HSET` 등 값 변경 | TTL 유지 |
| `DEL`, `GETSET`, `*STORE` | TTL 제거 |

캐시를 갱신하는 코드가 `SET`을 쓰면서 TTL을 다시 지정하지 않으면,
그 키는 **영구 키가 되어 무효화 유실 시 복구되지 않는다.**

## TTL Jitter

동일 TTL로 대량 생성된 캐시는 동시에 만료된다.

```mermaid
flowchart LR
    A["10만 개 캐시 생성 · TTL 10분"] --> B[10분 후 동시 만료]
    B --> C[DB 스파이크]
```

만료 시점을 분산시킨다.

```java
private static final Duration BASE_TTL = Duration.ofMinutes(10);
private static final Duration JITTER_BOUND = Duration.ofMinutes(1);

private Duration ttlWithJitter() {
    long bound = JITTER_BOUND.toSeconds();
    return BASE_TTL.plusSeconds(ThreadLocalRandom.current().nextLong(-bound, bound + 1));
}
```

만료가 `9m00 · 9m10 · 9m40 · 10m00 · 10m30` 처럼 흩어진다.
전략과 무관하게 거의 항상 적용할 가치가 있다.

## TTL 길이 결정 기준

| 질문 | TTL에 미치는 영향 |
|-----|----------------|
| 무효화가 실패하면 얼마나 오래 stale을 견딜 수 있는가 | **상한을 결정한다** |
| 데이터가 얼마나 자주 변경되는가 | 변경이 잦으면 짧게 (어차피 무효화됨) |
| MISS 시 DB 조회 비용이 얼마나 큰가 | 비싸면 길게 |
| 워킹셋이 메모리에 들어가는가 | 넘치면 짧게 |

TTL Only 전략에서는 TTL이 **stale window 그 자체**이지만,
무효화를 함께 쓰면 TTL은 **실패 경로에서만 의미를 갖는 상한**이 된다.
이 차이 때문에 무효화를 도입하면 TTL을 오히려 길게 가져갈 수 있다.

## 주의사항

| 항목 | 내용 |
|-----|-----|
| 벽시계 의존 | 만료는 절대 Unix timestamp로 저장된다. 시스템 시각을 앞당기면 즉시 만료된다 |
| 복제본 | 복제본은 독립적으로 만료시키지 않고 마스터의 `DEL` 전파를 기다린다 |
| 음수 TTL | `EXPIRE`에 0 이하를 주면 만료가 아니라 **삭제**된다 (이벤트도 `del`) |
| `GT` 옵션 | TTL 없는 키는 무한 TTL로 취급되어 `GT`로는 설정되지 않는다 (Redis 7.0+) |

## Reference

- [Redis - EXPIRE (How Redis expires keys)](https://redis.io/docs/latest/commands/expire/)
- [Redis - SET (KEEPTTL)](https://redis.io/docs/latest/commands/set/)
- [Redis - Keyspace notifications (Timing of expired events)](https://redis.io/docs/latest/develop/pubsub/keyspace-notifications/)
- [Redis - Client-side caching](https://redis.io/docs/latest/develop/clients/client-side-caching/)

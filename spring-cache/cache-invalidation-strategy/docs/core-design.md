# 코어 모듈 설계

`cache-invalidation-core`는 전략 모듈들이 공유하는 **틀만** 제공한다.
도메인 캐시명이나 전략별 구현은 담지 않는다.

## 원칙 — 프레임워크가 주는 것을 다시 만들지 않는다

| 필요한 것 | 사용하는 것 | 직접 만든 것 |
|---------|-----------|-----------|
| hit·miss·put·delete 집계 | `RedisCacheManagerBuilder.enableStatistics()` | — |
| Micrometer 연동 | Actuator `RedisCacheMeterBinderProvider` | — |
| 캐시 장애 폴백 | `CachingConfigurer.errorHandler()` | 폴백 처리기 구현체 (기본 제공 없음) |
| 엔트리별 TTL | `RedisCacheWriter.TtlFunction` (3.2+) | jitter 계산 |
| DB 문장 수 | Hibernate `Statistics` | 얇은 래퍼 |
| 무효화 규칙 | — | 전부 (프레임워크 영역 밖) |
| 미등록 캐시 차단 | `disableCreateOnMissingCache()` | 기본 도달 시 예외 던지는 직렬화기 |
| 캐시별 타입 직렬화 | `withInitialCacheConfigurations` | `CachePolicy.valueType()` |
| `clear()` 시 SCAN 사용 | `BatchStrategies.scan(n)` | — |

## 모듈 구성

| 모듈 | 소비 방식 | 담는 것 |
|-----|---------|-------|
| `cache-invalidation-core` | `implementation` | 캐시 정책, 무효화 규칙, 이벤트 어댑터, 회복력 |
| `cache-invalidation-test` | **`testImplementation`** | 컨테이너 설정, 프로파일 해석기, 통합 테스트 애노테이션 |

테스트 지원을 별도 모듈로 둔 이유는 전략 모듈이 7~8개로 늘어나기 때문이다.
각 모듈이 컨테이너 설정을 복사하면 같은 코드가 그만큼 중복된다.

## 구성 요소

```mermaid
flowchart TD
    subgraph 정책
        CP[CachePolicy] --> CPS[CachePolicies]
        CPS --> REG[CachePolicyRegistry]
    end
    subgraph 신호 소스 (모듈이 선택)
        HB[Hibernate POST_COMMIT] --> HL[JpaEntityChangeListener]
        SP[ApplicationEvent AFTER_COMMIT] --> SL[EntityChangeEventListener]
    end
    subgraph 무효화 파이프라인 (코어가 확정)
        HL --> SINK[CacheInvalidator]
        SL --> SINK
        SINK --> RU[InvalidationRules]
        RU --> EVI[CacheEvictor]
    end
    subgraph 회복력
        CEH[FallbackCacheErrorHandler]
        CFR[CacheFailureRecorder]
    end
    subgraph 관측
        IR[InvalidationRecorder]
    end
    REG --> CFG[AbstractRedisCacheConfig]
    CFG --> CEH
    CEH --> CFR
    EVI --> CFR
    EVI --> IR
```

회복력과 관측은 다른 책임이다.
회복력은 캐시가 죽어도 서비스를 살리고, 관측은 무효화가 **성공했든 실패했든** 결과를 남긴다.
한 패키지에 두면 패키지 순환이 생겨서 분리했다.

## 패키지 배치

```text
cache/          policy · eviction · serialization · expiration  (네 축) + RedisCacheConfig(조립)
invalidation/   무효화 규칙과 실행 + change/(변경 사실)
listener/       변경을 감지해 무효화를 호출하는 어댑터
metrics/        무효화 결과 기록
resilience/     캐시 장애 시 서비스 보호
config/         스프링 배선과 프로퍼티
```

의존은 한 방향으로만 흐른다. `cache.policy`, `cache.expiration`, `invalidation.change`, `resilience` 는
아무것도 참조하지 않는 말단이고, `config` 만 전부를 안다. 순환은 없다.

무효화가 필요 없는 모듈은 신호 소스를 선언하지 않는다.
`ttl-only` 가 그 경우이며, 리스너 빈이 하나도 등록되지 않는다는 것을 테스트로 고정한다.

## 캐시 정책 — 코어는 이름을 모른다

코어에 `USER` 같은 도메인 캐시명을 두면 전략 모듈이 늘 때마다 코어를 고쳐야 한다.
그래서 `CachePolicy`는 인터페이스이고, 각 모듈이 enum으로 구현한다.

```java
public interface CachePolicy {
    String cacheName();
    Duration ttl();
}
```

```java
public enum UserCachePolicy implements CachePolicy {
    USER(Name.USER, Duration.ofMinutes(10));
}
```

모듈은 `CachePolicies` 빈으로 자기 정책을 등록하고,
`CachePolicyRegistry`가 모아 캐시명 중복을 거부한다.

## 무효화 — Rule이 기본, 인터페이스는 단축키

```java
public interface InvalidationRule {
    boolean supports(EntityChange change);
    Collection<CacheEntryRef> resolve(EntityChange change);
}
```

| 방식 | 언제 |
|-----|-----|
| `CacheEvictable` 구현 | 엔티티가 자기 키만 알면 되는 단순한 경우 |
| `InvalidationRule` 빈 | 키 변경, 교차 엔티티, 목록 캐시 등 |

`EntityChange`가 **변경 전 상태**를 함께 실어 옛 키를 재구성할 수 있다.

```java
change.previousValueOf("username")   // 자연키가 바뀐 경우 옛 키 산출
```

### 왜 record 안에 배열을 두지 않았나

`EntityChange`가 `Object[] previousState`를 직접 가지면
record의 `equals`/`hashCode`가 **참조 동일성**으로 동작해 내용이 같아도 다르다고 판정한다.
접근자가 가변 배열을 그대로 노출하는 문제도 있다.

그래서 `EntityState` record가 불변 `List`로 감싸고 조회 책임까지 가진다.

## 회복력 — 캐시가 죽어도 서비스는 산다

```java
@EnableCaching
public abstract class AbstractRedisCacheConfig implements CachingConfigurer {

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new FallbackCacheErrorHandler(cacheFailureRecorder());
    }
}
```

`CachingConfigurer` 구현이 **필수**다. `@Bean CacheErrorHandler`만으로는 등록되지 않는다.
자세한 근거는 [cache-failure-modes.md](concepts/cache-failure-modes.md).

| 경로 | 처리 |
|-----|-----|
| 애노테이션 기반(`@Cacheable` 등) | `CacheErrorHandler`가 감싼다 |
| 프로그래밍 방식(`CacheEvictor`) | 직접 try/catch 후 `CacheFailureRecorder`에 기록 |

두 경로 모두 실패를 **같은 레코더**에 모아 관측 지점을 하나로 유지한다.

## 설정

**Boot 표준 프로퍼티로 표현되는 것은 직접 만들지 않는다.**

```yaml
spring:
  cache:
    redis:
      key-prefix: "cache-ttl-only:"   # 키 네임스페이스
      time-to-live: 5m                # 기본 TTL
      enable-statistics: true         # hit/miss/put/delete 집계

cache-invalidation:
  not-found-ttl: 30s                  # 표준에 없음 — null 값 전용 짧은 TTL
  ttl-jitter-ratio: 0.1               # 표준에 없음
  clear-scan-batch-size: 1000         # 표준에 없음
```

| 항목 | 출처 | 이유 |
|-----|-----|-----|
| `spring.cache.redis.key-prefix` | **Boot 표준** | 최종 키는 `{prefix}{cacheName}::{key}` |
| `spring.cache.redis.time-to-live` | **Boot 표준** | 정책에 없는 캐시의 기본값 |
| `spring.cache.redis.enable-statistics` | **Boot 표준** | 코드로 강제하지 않고 설정에 위임 |
| `cache-invalidation.not-found-ttl` | 자체 | Penetration 방어 — 표준에 대응 항목 없음 |
| `cache-invalidation.ttl-jitter-ratio` | 자체 | 동시 만료 방지 — 표준 미제공 |
| `cache-invalidation.clear-scan-batch-size` | 자체 | `clear()`가 `KEYS`+`DEL`을 쓰는 문제 회피 |

`spring.cache.redis.use-key-prefix: false` 도 그대로 존중한다.

### 왜 캐시명이 정책에 등록되어야만 하는가

Spring Data Redis는 미등록 캐시명을 **런타임에 조용히 생성**하고
Boot 기본 설정(**JDK 직렬화**)을 적용한다.

`disableCreateOnMissingCache()`로 이 경로를 막고,
`cacheDefaults`에는 예외를 던지는 직렬화기를 두어 기본값으로 흘러가지 않게 한다.
→ [serialization.md](concepts/serialization.md)
| `ttl-jitter-ratio` | 대량 키 동시 만료 방지 |
| `clear-scan-batch-size` | `RedisCache#clear()` 기본이 `KEYS`+`DEL`이라 프로덕션에서 위험 |

## 검증

코어가 보장하는 동작은 테스트로 고정되어 있다.

| 테스트 | 고정하는 명제 |
|------|------------|
| `EntityStateTest` | 내용이 같으면 동등하고, 원본 배열 변경에 영향받지 않는다 |
| `RuleBasedCacheInvalidatorTest` | 키가 바뀐 수정에서 옛 키도 무효화된다 |
| `InvalidationRulesTest` | 규칙 하나가 실패해도 나머지는 수행된다 |
| `FallbackCacheErrorHandlerTest` | 기본 처리기는 전파하고, 폴백 처리기는 삼키되 기록한다 |
| `JitteredTtlTest` | 흔들림이 기준 TTL 비율을 넘지 않고 최소 1초를 확보한다 |

## 관련

- [concepts/cache-failure-modes.md](concepts/cache-failure-modes.md)
- [concepts/metrics.md](concepts/metrics.md)
- [invalidation/entity-event-invalidate.md](invalidation/entity-event-invalidate.md)

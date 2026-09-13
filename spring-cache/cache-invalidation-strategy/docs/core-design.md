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
        CP[CachePolicy] --> REG[CacheCatalog]
    end
    subgraph 신호 소스 (모듈이 선택)
        HB[Hibernate POST_COMMIT] --> HL[JpaEntityChangeListener]
        SP[ApplicationEvent AFTER_COMMIT] --> SL[ApplicationEventChangeListener]
    end
    subgraph 무효화 파이프라인 (코어가 확정)
        HL --> SAFE[FailSafeCacheInvalidator]
        SL --> SAFE
        SAFE --> SINK[RuleBasedCacheInvalidator]
        SINK --> RU[InvalidationRuleSet]
        SINK --> EVI[CacheEvictor]
    end
    subgraph 회복력
        CEH[FallbackCacheErrorHandler] --> CFR[CacheFailureRecorder]
    end
    subgraph 관측
        IR[InvalidationRecorder]
    end
    REG --> CFG[AbstractRedisCacheConfig]
    CFG --> CEH
    SINK --> IR
    RU --> IR
    SAFE --> IR
```

회복력과 관측은 다른 책임이다.
회복력은 캐시가 죽어도 서비스를 살리고, 관측은 무효화가 **성공했든 실패했든** 결과를 남긴다.
기록기를 한 곳에 합치려면 `InvalidationRecorder` 가 참조하는 `ChangeSource` · `EvictionOutcome` 까지
끌고 나가야 하므로 패키지 순환이 생긴다. 각자 자기 관심사 옆에 둔다.

`CacheEvictor` 는 어디에도 기록하지 않는다. 결과만 돌려주고 기록은 호출자가 한다.
지우는 쪽과 세는 쪽이 모두 기록하면 같은 실패가 두 번 쌓인다.

## 패키지 배치

최상위를 열면 캐시를 다루는 축이 목차처럼 늘어선다.

```text
policy/             어떤 캐시가 있고 수명이 얼마인가
expiration/         언제 만료되나
serialization/      어떤 형식으로 저장하나
resilience/         캐시가 죽어도 서비스를 살린다
invalidation/       언제 무엇을 지우나
    change/         변경 사실
    listener/       변경을 감지하는 어댑터
config/             스프링 배선과 프로퍼티
```

모듈명이 이미 캐시를 말하므로 `cache/` 로 한 번 더 감싸지 않는다.
`eviction` 이라는 이름도 쓰지 않는다 — 이 저장소에서 그 말은
[메모리 압박에 의한 축출](eviction/)을 뜻하고, 여기서 하는 일은 명시적 삭제다.

의존은 한 방향으로만 흐른다.

| 패키지 | 참조 대상 |
|-----|---------|
| `policy` · `expiration` · `resilience` | 없음 (말단) |
| `serialization` | `policy` |
| `invalidation` | `policy` · `invalidation.change` |
| `invalidation.listener` | `invalidation` · `invalidation.change` |
| `config` | 전부 |

순환은 0이다. 특히 **`policy` 는 무효화를 모른다.**
정책이 `InvalidationOwner` 같은 무효화 어휘를 들고 있으면 import 그래프는 깨끗해 보여도
개념은 이미 역류한 상태다. 소유권은 정책의 속성이 아니라 정책과 규칙 사이의 관계이므로
[규칙 쪽에서만](#무효화-소유권) 선언한다.

무효화가 필요 없는 모듈은 신호 소스를 선언하지 않는다.
`ttl-only` 가 그 경우이며, 리스너 빈이 하나도 등록되지 않는다는 것을 테스트로 고정한다.

## 캐시 정책 — 코어는 이름을 모른다

코어에 `USER` 같은 도메인 캐시명을 두면 전략 모듈이 늘 때마다 코어를 고쳐야 한다.
그래서 `CachePolicy`는 인터페이스이고, 각 모듈이 enum으로 구현한다.

```java
public interface CachePolicy {
    Spec spec();

    record Spec(String cacheName, Duration ttl, Class<?> valueType) { }
}
```

```java
public enum UserCachePolicy implements CachePolicy {
    USER(new Spec(Name.USER, Duration.ofMinutes(10), CachedUser.class));
}
```

값을 `Spec` 에 모은 덕에 구현 enum 은 필드 하나만 들고,
무효화를 쓰지 않는 모듈은 무효화 어휘를 한 번도 만나지 않는다.

모듈은 `cachePolicies()` 를 구현해 자기 정책을 등록하고,
`CacheCatalog` 가 모아 캐시명 중복을 거부한다.

## 무효화 소유권

캐시를 등록해 놓고 아무도 지우지 않으면, 그 사실은 운영 중에만 드러난다.
그래서 규칙이 자기가 책임지는 캐시를 밝히고, 기동할 때 선언 목록과 대조한다.

```java
@Override
protected List<InvalidationRule> invalidationRules() {
    return List.of(
        EvictableEntityRule.owning(UserCachePolicy.USER),
        TtlOnlyRule.covering(ArticleCachePolicy.ARTICLE)
    );
}
```

`TtlOnlyRule` 은 아무것도 지우지 않지만 주인은 된다.
시간 만료에 맡기겠다는 판단과 그냥 빠뜨린 것을 구분하기 위해서다.

소유 대상은 캐시명 문자열이 아니라 `CachePolicy` 그 자체다.
문자열로 내리면 오타가 컴파일을 통과하고, 그 구멍을 메우려고 기동 검증이 더 필요해진다.

규칙을 하나도 선언하지 않은 모듈은 대조하지 않는다.
무효화를 쓰지 않으면 TTL 이 유일한 수단이므로 주인을 물을 대상이 없다.

## 무효화 — Rule이 기본, 인터페이스는 단축키

```java
public interface InvalidationRule {
    boolean supports(EntityChange change);
    Collection<CacheKey> resolve(EntityChange change);
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

| 경로 | 처리 | 기록 |
|-----|-----|-----|
| 애노테이션 기반(`@Cacheable` 등) | `CacheErrorHandler` 가 감싼다 | `CacheFailureRecorder` |
| 프로그래밍 방식(`CacheEvictor`) | 결과 코드로 돌려준다 | `InvalidationRecorder` |

기록처는 둘이고, 묻는 질문이 다르다.
앞은 "캐시 인프라가 성한가", 뒤는 "무효화가 목적을 이뤘는가"를 센다.

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

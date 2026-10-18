# 코어 모듈 설계

`cache-invalidation-core`는 전략 모듈들이 공유하는 **틀만** 제공한다.
도메인 캐시명이나 전략별 구현은 담지 않는다.

이 문서에서 **무효화**는 "DB가 바뀌어 낡아진 캐시 항목을 지우는 일"을 뜻한다.
TTL 만료나 메모리 압박에 의한 축출과는 다르다 — 그 둘은 Redis가 하고, 무효화는 우리가 한다.
구분은 [문서 지도](README.md#말부터) 참조.

## 원칙 — 프레임워크가 주는 것을 다시 만들지 않는다

| 필요한 것 | 사용하는 것 | 직접 만든 것 |
|---------|-----------|-----------|
| hit·miss·put·delete 집계 | `RedisCacheManagerBuilder.enableStatistics()` | — |
| 캐시 실패·무효화 결과 관측 | — | 쓰기 전용 포트 2종 (기본 구현은 무연산) |
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
| `cache-invalidation-core` | `implementation` | 캐시 정책, 무효화 규칙, 이벤트 어댑터, 캐시 장애 폴백 |
| `cache-invalidation-test` | **`testImplementation`** | 컨테이너 설정, 프로파일 해석기, 통합 테스트 애노테이션, 측정 구현 |

테스트 지원을 별도 모듈로 둔 이유는 전략 모듈이 7~8개로 늘어나기 때문이다.
각 모듈이 컨테이너 설정을 복사하면 같은 코드가 그만큼 중복된다.

## 구성 요소

```mermaid
flowchart TD
    subgraph 정책
        CP[CachePolicy]
    end
    subgraph 신호 소스 (모듈이 선택, 트랜잭션 안에서 수집)
        HB[Hibernate POST_INSERT·UPDATE·DELETE] --> HL[JpaEntityChangeListener]
        SP[ApplicationEvent BEFORE_COMMIT] --> SL[PublishedEntityChangeListener]
    end
    subgraph 무효화 파이프라인 (코어가 확정)
        HL --> SAFE[FailSafeCacheInvalidator]
        SL --> SAFE
        SAFE --> SINK[DefaultCacheInvalidator]
        SINK --> RU[규칙으로 대상 산출]
        RU --> PEND[PendingInvalidations<br/>트랜잭션 스코프 수집]
        PEND --> EVI[커밋 이후 한 번에 삭제]
    end
    subgraph 기록 포트 (코어는 세지 않는다)
        CFR[CacheFailureRecorder]
    end
    CP --> CFG[AbstractRedisCacheConfig]
    CFG --> CEH[FallbackCacheErrorHandler]
    CEH --> CFR
    SINK --> IR
    RU --> IR
    SAFE --> IR
```

무효화 결과는 코어가 세지 않는다.

한때 `InvalidationRecorder` 로 무효화 건수와 신호원을 기록했으나 걷어냈다.
운영 빈이 무연산이라 **제품 코드에서는 아무 일도 하지 않았고**, 읽는 쪽은 테스트뿐이었다.
그 하나를 위해 `Outcome` enum, `EntityChange.Source` enum, 수집기의 `Map<CacheKey, Source>`,
생성자 파라미터 세 자리, 테스트 모듈의 계수 구현이 딸려 있었다.

운영에서 남는 신호는 WARN 로그다. 부하 측정을 시작하면 손으로 만든 카운터가 아니라
Micrometer 로 넣는다 — 어차피 스크랩되지 않는 숫자는 지표가 아니다.

`CacheFailureRecorder` 는 남아 있다. 이쪽은 애노테이션 경로의 캐시 접근 실패를 다루고
`CacheErrorHandler` 계약에 묶여 있어 성격이 다르다.

규칙 산출과 삭제는 `DefaultCacheInvalidator` 하나가 맡는다.
`DefaultRedisCacheWriter` 가 연결·배치·통계를 혼자 처리하는 것과 같은 결이다.
쪼개 두면 결과 코드가 클래스 경계를 넘나들고, 지우는 쪽과 세는 쪽이 모두 기록해 같은 실패가 두 번 쌓인다.

## 패키지 배치

최상위를 열면 캐시를 다루는 축이 목차처럼 늘어선다.

```text
(루트)              무효화 계약과 그 구현 — 모듈의 주 책임
serializer/         어떤 형식으로 저장하나
expiration/         언제 만료되나
config/             스프링 배선과 프로퍼티, 캐시 접근 실패 시의 폴백
```

**계약과 구현을 같은 패키지에 둔다.** 자바 package-private 은 하위 패키지도 남이므로,
구현을 감추려면 그것을 만드는 정적 팩토리와 같은 패키지여야 한다.
`spring-data-redis` 의 `serializer` 가 `RedisSerializer` 계약과 구현 8종을 한자리에 두는 이유가 이것이다.
`connection` → `connection.lettuce` 같은 분할은 조직화일 뿐 접근 강제가 아니다 (Lettuce 구현은 public).

모듈명이 이미 무효화를 말하므로 루트 아래 `invalidation/` 을 한 번 더 두지 않는다.
`resilience/` 도 두지 않는다 — 축이 아니라 품질 속성 이름이고, 폴백이 도는 상황은
`CachingConfigurer.errorHandler()` 가 사는 `config/` 그대로다.

모듈명이 이미 캐시를 말하므로 `cache/` 로 한 번 더 감싸지 않는다.
`eviction` 이라는 이름도 쓰지 않는다 — 이 저장소에서 그 말은
[메모리 압박에 의한 축출](eviction/)을 뜻하고, 여기서 하는 일은 명시적 삭제다.

의존은 한 방향으로만 흐른다.

| 패키지 | 참조 대상 | 역방향이 막히는 근거 |
|-----|---------|----------------|
| (루트) | 없음 | — |
| `expiration` | 없음 | 구현이 package-private |
| `serializer` | 루트(`CachePolicy`) | 〃 |
| `config` | 루트 · `serializer` · `expiration` 의 **공개 계약만** | 구현체가 보이지 않아 `new` 자체가 컴파일되지 않는다 |

순환은 0이다. 특히 **`policy` 는 무효화를 모른다.**
정책이 `InvalidationOwner` 같은 무효화 어휘를 들고 있으면 import 그래프는 깨끗해 보여도
개념은 이미 역류한 상태다. 소유권은 정책의 속성이 아니라 정책과 규칙 사이의 관계이므로
[규칙 쪽에서만](#무효화-소유권) 선언한다.

무효화가 필요 없는 모듈은 신호 소스를 선언하지 않는다.
`ttl-only` 가 그 경우이며, 리스너 빈이 하나도 등록되지 않는다는 것을 테스트로 고정한다.

## 공개 표면 — 전략 모듈이 만나는 것만 public

코어 타입이 전부 `public` 이면 전략 모듈 개발자는 자동완성에서 무엇을 써야 할지 구분할 수 없다.
그래서 **밖에서 실제로 쓰는 것만 공개하고 나머지는 패키지 안에 둔다.**

| 공개 | 무엇에 쓰나 |
|-----|-----------|
| `AbstractRedisCacheConfig` | 상속해서 모듈 설정을 만든다 |
| `CachePolicy` | 캐시 이름·TTL·값 타입을 enum 으로 선언한다 |
| `JpaEntityChangeConfig` · `PublishedEntityChangeConfig` | 감지 방식을 `@Import` 로 켠다 |
| `InvalidationRule` · `EvictableEntityRule` · `TtlOnlyRule` · `CacheEvictable` · `CacheKey` | 무효화 대상을 선언한다 |
| `EntityChange` | JPA 밖 변경을 직접 발행한다. `State` · `Type` 은 그 안에 중첩 |
| `CacheFailureRecorder` | 캐시 접근 실패를 세는 구현을 끼운다 |
| `TtlResolver` · `SerializerFactory` | 만료·직렬화 전략을 재정의한다 |

조립은 계약 위 정적 팩토리가 맡는다. 그래야 구현체가 `config` 에 보일 필요가 없다.

```java
SerializerFactory.json()
TtlResolver.jittered(jitterRatio, notFoundTtl)
CacheInvalidator.failSafe(rules, cacheManager, recorder)
InvalidationRule.verifyOwnership(rules, declaredPolicies)
```

`DefaultCacheInvalidator` · `FailSafeCacheInvalidator` · `JpaEntityChangeListener` ·
`PublishedEntityChangeListener` · `JitteredTtlResolver` · `JsonSerializerFactory` ·
`FallbackCacheErrorHandler` 는 package-private 이다.

전략 전용 규칙은 코어에 두지 않는다. `CacheEvictable` 과 `EvictableEntityRule` 은
"엔티티가 자기 캐시 키를 안다" 는 특정 전략의 관례이므로 `write-invalidate` 가 소유한다.
코어는 계약 `InvalidationRule` 과 아무것도 지우지 않는 `TtlOnlyRule` 만 제공한다.

`AbstractRedisCacheConfig` 의 `@Bean` 메서드도 `protected` 다.
`CachingConfigurer` 구현인 `errorHandler()` 만 `public` 이어야 한다.

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
`CachePolicy.distinct()` 가 모아 캐시명 중복을 거부한다.
중복을 허용하면 어느 쪽 TTL 과 값 타입이 이겼는지 런타임에만 드러난다.

## 지우는 자리는 하나다

신호원마다 즉시 지우면 같은 변경을 두 경로로 알렸을 때 같은 키를 두 번 지운다.
정합성은 안전하지만(DEL 은 멱등) 왕복과 지표가 두 배가 된다.

그래서 **수집은 트랜잭션 안에서 하고 실행은 커밋 이후에 한다.**

```text
flush       JpaEntityChangeListener       ─┐
커밋 직전    PublishedEntityChangeListener ─┴→ PendingInvalidations (Set)
커밋 이후    afterCompletion(COMMITTED)      → 한 번에 삭제
```

`TransactionSynchronization` 은 트랜잭션이 살아 있을 때만 등록할 수 있다.
그래서 수집 시점만 커밋 전으로 당겼다. 실행 시점은 그대로 커밋 이후다.
롤백이면 모인 대상을 버린다 — 흩어져 있던 `requiresPostCommitHandling` ·
`onPost*CommitFailed` 처리가 여기 한곳으로 모였다.

트랜잭션 밖에서 들어온 변경은 모으지 않고 그대로 지운다.

신호원이 늘어도 지우는 자리는 하나다. 새 신호원은 수집기에 넣기만 하면 된다.

| | 신호원 N개일 때 |
|---|---|
| 신호원마다 즉시 삭제 | 삭제 경로 N개, 중복 가능 |
| 수집 후 일괄 삭제 | **삭제 경로 1개, 중복 없음** |

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

그래서 중첩 record `EntityChange.State` 가 불변 `List` 로 감싸고 조회 책임까지 가진다.

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

| 경로 | 처리 | 내보내는 포트 |
|-----|-----|-----|
| 애노테이션 기반(`@Cacheable` 등) | `CacheErrorHandler` 가 감싼다 | `CacheFailureRecorder` |
| 프로그래밍 방식(`DefaultCacheInvalidator`) | 삼키고 WARN 로그로 남긴다 | 없음 |

두 포트 모두 코어의 기본 구현은 무연산이다. [구성 요소](#구성-요소) 참조.

## 설정

**Boot 표준 프로퍼티로 표현되는 것은 직접 만들지 않는다.**

```yaml
spring:
  cache:
    redis:
      key-prefix: "cache-ttl-only:"   # 키 네임스페이스
      time-to-live: 5m                # 기본 TTL
      enable-statistics: true         # hit/miss/put/delete 집계

service:
  cache:
    expiration:
      not-found-ttl: 30s              # 표준에 없음 — null 값 전용 짧은 TTL
      jitter-ratio: 0.1               # 표준에 없음
    invalidation:
      clear-scan-batch-size: 1000     # 표준에 없음
```

자체 프로퍼티는 `service.cache` 한 트리에 모으고 축별로 중첩한다.
전략마다 최상위 키를 따로 만들면 설정이 흩어진다.

| 항목 | 출처 | 이유 |
|-----|-----|-----|
| `spring.cache.redis.key-prefix` | **Boot 표준** | 최종 키는 `{prefix}{cacheName}::{key}` |
| `spring.cache.redis.time-to-live` | **Boot 표준** | 정책에 없는 캐시의 기본값 |
| `spring.cache.redis.enable-statistics` | **Boot 표준** | 코드로 강제하지 않고 설정에 위임 |
| `service.cache.expiration.not-found-ttl` | 자체 | Penetration 방어 — 표준에 대응 항목 없음 |
| `service.cache.expiration.jitter-ratio` | 자체 | 동시 만료 방지 — 표준 미제공 |
| `service.cache.invalidation.clear-scan-batch-size` | 자체 | `clear()`가 `KEYS`+`DEL`을 쓰는 문제 회피 |

`spring.cache.redis.use-key-prefix: false` 도 그대로 존중한다.

### 왜 캐시명이 정책에 등록되어야만 하는가

Spring Data Redis는 미등록 캐시명을 **런타임에 조용히 생성**하고
Boot 기본 설정(**JDK 직렬화**)을 적용한다.

`disableCreateOnMissingCache()`로 이 경로를 막고,
`cacheDefaults`에는 예외를 던지는 직렬화기를 두어 기본값으로 흘러가지 않게 한다.
→ [serialization.md](concepts/serialization.md)

## 검증

코어가 보장하는 동작은 테스트로 고정되어 있다.

| 테스트 | 고정하는 명제 |
|------|------------|
| `EntityChangeStateTest` | 내용이 같으면 동등하고, 원본 배열 변경에 영향받지 않는다 |
| `DefaultCacheInvalidatorTest` | 키가 바뀐 수정에서 옛 키도 무효화되고, 규칙 하나가 실패해도 나머지는 수행된다 |
| `FallbackCacheErrorHandlerTest` | 기본 처리기는 전파하고, 폴백 처리기는 삼키되 기록한다 |
| `JitteredTtlResolverTest` | 흔들림이 기준 TTL 비율을 넘지 않고 최소 1초를 확보한다 |
| `AbstractRedisCacheConfigTest` | 재정의한 직렬화·만료 전략이 캐시 설정까지 닿는다 |

## 관련

- [concepts/cache-failure-modes.md](concepts/cache-failure-modes.md)
- [concepts/metrics.md](concepts/metrics.md)
- [invalidation/entity-event-invalidate.md](invalidation/entity-event-invalidate.md)

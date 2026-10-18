# 정책 없는 캐시의 직렬화기

| | |
|---|---|
| 상태 | **결정 대기** — 현재 구현은 깨져 있어 어느 쪽으로든 고쳐야 한다 |
| 일시 | 2026-10-18 |
| 대상 | `AbstractRedisCacheConfig` 의 `cacheDefaults` 값 직렬화기 |
| 대조 | Boot 3.5.0 · spring-data-redis 3.5.0 · jackson-databind 2.19.0 · spring-context 6.2.7 (전부 로컬 소스·바이트코드 직접 확인) |

## 무엇을 정하는가

`CachePolicy` 가 선언된 캐시는 `Jackson2JsonRedisSerializer<T>` 를 쓴다 — 값에 타입 정보가 없고 가젯 면이 0이다.
정하는 것은 **정책이 없어 값 타입을 모르는 캐시**의 기본 직렬화기뿐이다.

| 후보 | |
|---|---|
| **(a)** | `JdkSerializationRedisSerializer` — Boot 기본값 |
| **(b)** | `GenericJackson2JsonRedisSerializer` — `@class` 를 값에 실음 |
| **(c)** | 그런 캐시를 만들지 않음 (기동 또는 호출 시점 실패) |
| **(d)** | 격리 캐시 — 항상 miss, 저장하지 않음 |

## 현재 구현은 깨져 있다

```java
new GenericJackson2JsonRedisSerializer(defaultObjectMapper())
```

이 생성자는 private 4-인자 경로(`GenericJackson2JsonRedisSerializer.java:163`)로 가며
`setDefaultTyping` 도 `registerNullValueSerializer` 도 호출하지 않는다. 실측:

```
직렬화    record Cached("kim",30)  →  {"name":"kim","age":30}     @class 없음
역직렬화                           →  java.util.LinkedHashMap      원래 타입 소실
NullValue                          →  SerializationException
```

결과는 캐시 적중 시 호출부 `ClassCastException`, 그리고 null 캐싱 불가.
이를 통과시킨 테스트는 `.contains("kim")` 만 단언했다 — `@class` 유무와 무관하게 참이다.

## 측정 기록

전부 이 저장소에서 직접 실행하거나 소스·바이트코드를 읽어 확인했다.

| # | 측정 | 결과 |
|:-:|-----|-----|
| 1 | Boot 기본 캐시 값 직렬화기 | `JdkSerializationRedisSerializer` |
| 2 | `LaissezFaireSubTypeValidator` (javap) | `validateSubClassName`·`validateSubType` 모두 무조건 `ALLOWED` |
| 3 | 제대로 만든 (b) 왕복 | 원래 record 타입으로 복원 성공 |
| 4 | 제대로 만든 (b) `java.time` | 성공 (`"1970-01-01T00:00:00Z"`) |
| 5 | 제대로 만든 (b) 필드 추가분을 구버전이 읽기 | **성공** |
| 6 | 제대로 만든 (b) `NullValue` | `{"@class":"org.springframework.cache.support.NullValue"}` |
| 7 | 컬렉션 타입 힌트 | `java.util.ImmutableCollections$List12` 가 페이로드에 유출 (원소 수에 따라 `$ListN` 으로 바뀜) |
| 8 | PTV `allowIfBaseType(Object.class)` + `allowIfSubType` | `InitialContext`·`HashMap` **통과** |
| 9 | PTV `allowIfSubType` 만 | 셋 다 차단 |
| 10 | 거부된 클래스의 static initializer | **차단 전에 실행됨** (인스턴스는 생성 안 됨) |
| 11 | `RedisCacheManager.getMissingCache` 반환형 | `RedisCache` 로 좁혀짐 → `NoOpCache` 반환 불가 |
| 12 | `SpringCacheAnnotationParser:116` | `setCacheNames(cacheable.cacheNames())` — 리터럴, SpEL 없음 |
| 13 | `CompositeCacheManager.setFallbackToNoOpCache` | 존재 |

8번의 메커니즘은 `sec` 가 바이트코드로 밝혔다 —
`validateBaseType` 이 `ALLOWED` 를 반환하면 `StdTypeResolverBuilder.verifyBaseTypeValidity` 가
`Id.CLASS` 조합에서 `LaissezFaireSubTypeValidator.instance` 를 돌려준다. 허용 목록을 설정하고 즉시 버리는 코드다.

## 상충 지점

독립 조사 셋을 그대로 받지 않고 전부 재현했다. 네 곳에서 갈렸다.

| 주장 | 출처 | 실측 | 판정 |
|-----|-----|-----|-----|
| PTV allow-list 로 (b) 완화 가능 | `sec` 1차 | 8번 — 통과 | **무효.** 본인 철회 |
| 필드 추가 = 롤링 배포 장애 | `ops` | 5번 — 성공 | **무인자 생성자 한정** |
| `java.time` 불가 | `ops` | 4번 — 성공 | **무인자 생성자 한정** |
| (b) 는 아무도 의존하면 안 될 포맷 | `alt` | 3~6번 — 전부 동작 | **가능성은 반박, 타당성은 유효** |

`ops` 의 두 지적이 무인자 한정이라는 점이 중요하다 — **(b) 는 불가능하지 않다.**
문제는 가능성이 아니라 올바른 구성에 도달하기 어렵다는 것이다.

## 조용히 틀린 횟수 — 네 번

| # | 누가 | 무엇을 | 증상 |
|:-:|---|---|---|
| 1 | 구현 | `new GenericJackson2JsonRedisSerializer(mapper)` | `@class` 미생성 |
| 2 | 구현 | 같은 경로 | `NullValue` 실패 |
| 3 | 테스트 | `.contains("kim")` | 1·2 를 못 잡음 |
| 4 | 조사 | `allowIfBaseType(Object.class)` | 허용 목록 무력화 |

**네 번 모두 예외 없이 통과했다.** 올바른 (b) 는 서로 독립적인 다섯 설정을 전부 요구한다 —
빌더 사용 · `defaultTyping(true)` · 커스텀 `ObjectMapper` · `allowIfBaseType` 회피 · `FAIL_ON_UNKNOWN` 해제.
하나라도 틀리면 조용히 되돌아간다.

## 후보별 비용

| | (a) JDK | (b) Generic JSON | (c) 거부 | (d) 격리 |
|---|---|---|---|---|
| 우리 record 저장 | **불가** (`Serializable` 아님) | 가능 | 해당 없음 | 저장 안 함 |
| 가젯 면 | 클래스패스 전체 | PTV 로 좁힘, static init 은 남음 | **0** | **0** |
| 실패 시점 | 트래픽 중 PUT | 설정 오류는 무음 | 기동 또는 호출 | 없음 (무캐시) |
| 포맷 부채 | — | `@class` 에 FQCN·JDK 내부 클래스명 | — | — |
| 되돌리기 | — | 정책 전환 시 기존 엔트리 TTL 소진까지 읽기 실패 | — | — |
| 소비자 | 0 | 0 | — | — |

## 미검증

| 주장 | 출처 |
|---|---|
| `@class` 오버헤드 +83% (295B → 541B) | `ops` 실측. 유출 자체는 7번으로 재현했으나 비율은 재측정 안 함 |
| 4.0 deprecated · Jackson 3 와이어 비호환 | `ops` 가 공식 문서 인용. 저장소 `serialization.md` 와 일치하나 출처가 같을 수 있음 |
| static initializer 로 실제 RCE 가 닿는 가젯이 현 클래스패스에 있는지 | `sec` 미조사 — 이론적 면만 |
| (c) 기동 스캔의 사각지대 (JCache 애노테이션·커스텀 `cacheResolver`·프로그램적 `getCache`) | `alt` 가 열거, 미검증 |

## 권고

**(c) + (d).** 기동 시점에 정책 없는 `@Cacheable` 을 찾아 실패시키고, opt-in 으로만 격리 캐시를 허용한다.
12·13번이 둘 다 구현 가능함을 보인다.

근거는 보안이 아니라 **비용 대비 이익**이다 — 소비자가 0인 경로에,
함정 다섯 개짜리 포맷을 기본값으로 들이는 거래다.

`alt` 의 전제 분리가 이 결정의 축이다.
**"캐시가 동작해야 한다" 와 "앱이 죽지 않아야 한다" 는 다른 요구**이고, (d) 가 후자만 떼어 해결한다.

### (b) 를 택한다면 최소 조건

1. 빌더 + `defaultTyping(true)` + 커스텀 `ObjectMapper`
2. `allowIfBaseType` 없는 PTV — 또는 `validateSubClassName` 에서 `DENIED` 를 반환하는 직접 구현
3. PTV 미지정 시 캐시 생성 실패
4. `{"@class":"java.net.URL"}` 가 `SerializationException` 을 던지는 **적대적 테스트**를 회귀로 고정

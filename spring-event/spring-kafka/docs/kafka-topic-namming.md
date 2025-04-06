# Kafka Topic Naming Convention Guide

토픽 네이밍엔 표준은 없지만, 조직 내 일관성이 가장 중요하다.

## 1. 기본 제약 조건

- 허용 문자: 영문자(a-z), 숫자(0-9), `.`, `_`, `-`
- 금지 규칙:
    - `__` 접두어 사용 금지(카프카 내부 토픽 전용)
    - `.` 또는 `..` 단독 사용 금지
    - `.` 와 `_` 혼용하여 사용 금지
    - 중복된 구분자 사용 금지 (`order..request`, `order--request`)
    - 빈 문자열 불가
    - 249자 이상 불가

> Kafka 시스템 내부 토픽은 `__consumer_offsets`와 같이 `__`로 시작합니다. 일반 사용자 토픽과 혼동을 피하기 위해 금지되어야 한다.

## 2. 실무 가이드라인

- 토픽 구성 요소는 `.` 으로 구분 통일
- 영어 소문자만 사용 (`a-z`, `0-9`)
- 버전 명시
  - 트랜잭션 경계, DB 마이그레이션시 하위 호환성 유지하기 위함
- 환경 정보는 마지막에 `-env` 형식으로 명시
  - user.created.v1-alpha
  - user.created.v1-prod
- 금지 사항
  - 전체 토픽 길이는 255자 이내
  - 대문자 사용 금지 (`Order.Request.Result` ❌)
  - 특수문자 (`_`, `@`, `#`, 등) 사용 금지
  - 중복된 구분자 사용 금지 (`order..request`, `order--request` ❌)
  - 너무 일반적이거나 모호한 이름 지양 (`data`, `info` ❌)

### 2.1. 구분자

일반적으로 `.`, `_`는 구조나 단어의 **"구분자" 또는 "경계 표시" 역할**로 사용된다.

- `.` 또는 `_` 중 **하나만** 사용, **혼용 금지** (메트릭 시스템 충돌 가능성)
- 예: `team.event.order` 또는 `team_event_order`

| 구분자         | 설명                | 예시                          |
|-------------|-------------------|-----------------------------|
| `.` (마침표)   | 계층적 구조 표현에 적합     | `service.createduser.event` |
| `_` (언더스코어) | 단어 구분자로 사용        | `service_created-user_event` |
| `-` (하이픈)   | 사람이 읽기 쉬운 구분자로 사용 | `service.created-user.event` |

Kafka의 내부 시스템 토픽은 `__`(더블 언더스코어)로 시작하는 규칙이 있으며, Docker에서는 `.`를 `_`로 변환해 환경 변수를 구성할 수 있도록 제안하고 있다.

이러한 점을 고려할 때, `-`는 여러 단어를 연결하거나 문장 표현에서 **사람이 읽기 쉬운 구분자(human-readable separator)**로 사용하는 것이 바람직하며, 애플리케이션 수준에서는 `.`을 **계층적 구조 표현**에 사용하는 것이 좋다.

### 2.2. 명명 패턴 예시

`{서비스/팀}.{환경}.{도메인}.{이벤트/액션}.{포맷}.{버전}`

예: `payment.prod.order.created.avro.v1`

- 이벤트 기반: `user.signup.success`
- 파이프라인 기반: `data.raw.sales.us-east`
- 멀티 테넌트: `tenantA.inventory.update`
- 환경 포함: `commerce.payment.transaction.prod`

## 3. 대기업 사례 분석

### 카카오

- 형식: `{서비스}.{환경}.{데이터유형}.{버전}`
- 예: `kakaotalk.prod.message.v1`
- 특징:
    - 마침표(`.`)만 사용
    - 환경(`dev`/`stage`/`prod`) 필수
    - 버전(`v1`) 명시

### 네이버

- 형식: `{팀명}-{시스템명}-{이벤트명}-{환경}`
- 예: `commerce-payment-ordercreated-prod`
- 특징:
    - 하이픈(`-`)만 사용
    - 팀 오너십 명시
    - 필터링/검색에 용이한 형태

### 라인

- 형식: `{도메인}.{aggregate}.{action}.{format}`
- 예: `member.user.updated.avro`
- 특징:
    - DDD 기반 구조
    - 데이터 포맷 포함 (json, avro)
    - 모두 소문자 사용

### 쿠팡

- 형식: `{source}.{target}.{pipeline-stage}.{env}`
- 예: `oms.warehouse.transformation.prod`
- 특징:
    - 파이프라인 단계 포함 (`raw`, `transformed`, `aggregated`)

### AWS / 넷플릭스

- 형식: `{app}.{region}.{event}.{env}`
- 예: `recommendation.us-west-2.click.prod`
- 특징:
    - 리전(region) 명시로 모니터링 용이

### 3.1 공통 패턴 요약

| 기업          | 명명 구조                                      | 구분자 | 특징                     | 예시 토픽 명                              |
|---------------|--------------------------------------------|--------|--------------------------|-------------------------------------------|
| 카카오         | `{서비스}.{환경}.{데이터유형}.{버전}`                  | `.`    | 계층적 구조, 버전 관리 필수     | `order.prod.result.v1`                    |
| 네이버         | `{팀}-{시스템}-{이벤트}-{환경}`                     | `-`    | 팀 중심, CLI 편의성 고려     | `adplatform-user-login-prod`              |
| 라인          | `{domain}.{aggregate}.{action}.{format}`   | `.`    | DDD 기반, 포맷 명시        | `user.auth.login.json`                    |
| 쿠팡          | `{source}.{target}.{pipeline-stage}.{env}` | `.`    | ETL 단계 포함              | `db.s3.extract.dev`                       |
| AWS/넷플릭스   | `{app}.{region}.{event}.{env}`             | `.`    | 리전(region) 정보 포함     | `video.us-east-1.playback.prod`           |

1. **구분자 일관성 유지**: `.` 또는 `-` 중 선택
2. **환경 정보 포함**: `dev`, `stage`, `prod` 등
3. **오너십 명시**: 팀명 또는 서비스명
4. **데이터 특성 포함**: 데이터 포맷(json/avro), 버전(v1 등)
5. **소문자 강제 사용**: 대소문자 혼용 금지

## 5. Tips

- 카카오는 `.` 구분자, 네이버는 `-` 구분자 선호
- 버전 정보와 환경 정보는 **필수 포함**을 권장
- 큰 조직일수록 **명명 규칙 문서화 및 자동 검증 시스템 도입** 필요

## Reference

- [Confluent 공식 가이드](https://www.confluent.io/learn/kafka-topic-naming-convention/)
- [Kafka Topic Naming Convention - Data Engineer Tech](https://data-engineer-tech.tistory.com/36)
- [개기심사 블로그](https://devfoxstar.github.io/middleware/kafka-topic-naming/)
- [마루-IT 블로그](https://maru-itdeveloper.tistory.com/36)
- [Digital Bourgeois](https://digitalbourgeois.tistory.com/269)
- [네이버 카프카 활용 사례 (YouTube)](https://www.youtube.com/watch?v=OxMdru93E6k)
- [Velog 블로그 모음](https://velog.io/@parktaejung/10-토픽-작명-방법)
- [GC Tech 블로그: Kafka 기반 EDA 설계](https://techblog.gccompany.co.kr/apache-kafka%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%98%EC%97%AC-eda-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0-bf263c79efd0)
- [CallMeSmith 블로그](https://devboi.tistory.com/660)

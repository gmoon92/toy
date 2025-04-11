# Kafka Topic Naming Convention Guide

토픽 네이밍엔 표준은 없지만, 조직 내 일관성이 가장 중요하다.

- [1. 기본 제약 조건](#1-기본-제약-조건)
- [2. 실무 가이드라인](#2-실무-가이드라인)
- [3. 대기업 토픽 네이밍 사례](#3-대기업-토픽-네이밍-사례)
- [4. 실무 인사이트 요약](#4-실무-인사이트-요약)

## 1. 기본 제약 조건

Kafka 토픽 이름 크기는 249자 이하로 구성하고, 영문 소문자(a-z), 숫자(0-9), 구분자(`.`, `_`, `-`)만 허용한다.

금지 규칙

- 빈 문자열 불가
- `__`(더블 언더 스코어) 접두어 사용 금지(카프카 내부 토픽 전용)
- 구분자 단독 사용 금지
    - 예: `.`, `..` `-`, `_`, `..event`, `order..created`, `.created`, `crated.`
- 중복된 구분자 사용 금지
    - 예: `order..license`, `order--license`
- 메트릭 시스템 충돌 가능성으로 인한 `.` 와 `_` 혼용 사용 지양
    - 예: `order.license_created`

> Kafka 시스템 내부 토픽은 `__consumer_offsets`와 같이 `__`로 시작합니다. 일반 사용자 토픽과 혼동을 피하기 위해 금지되어야 한다.

## 2. 실무 가이드라인

고려 사항은 다음과 같다.

- 구성 요소는 `.`  구분자로 일관되게 사용한다.
- 버전 관리
    - 하위 호환성을 유지하기 위해 버전을 명시한다.
    - DB 마이그레이션이나 트랜잭션 경계, 이벤트 스키마 변경 시 특히 유용하다.
    - 예: `v1`, `v2-beta`
- 환경 정보
    - 운영 환경은 접미사로 명확히 구분한다.
    - 예: user.created.**v1-alpha**, user.created.**v1-prod**
- 구체적인 액션과 도메인을 조합하여 명확하게
    - 예: `order.cancelled`, `user.signup.success`
- 의미가 모호하거나 너무 일반적인 이름은 지양
    - 예: `data`, `info`

### 2.1. 구분자

`.`, `_`는 구조나 단어의 **"구분자" 또는 "경계 표시" 역할**로 사용된다.

- `.` 또는 `_` 중 **하나만** 선택하여 일관되게 사용한다. **혼용은 지양한다.** (메트릭 시스템 충돌 가능성)
- 예: `team.event.order` 또는 `team_event_order`

| 구분자         | 설명                | 예시                           |
|-------------|-------------------|------------------------------|
| `.` (마침표)   | 계층적 구조 표현에 적합     | `service.createduser.event`  |
| `_` (언더스코어) | 단어 구분자로 사용        | `service_created-user_event` |
| `-` (하이픈)   | 사람이 읽기 쉬운 구분자로 사용 | `service.created-user.event` |

Kafka의 내부 시스템 토픽은 `__`(더블 언더스코어)로 시작하는 규칙이 있으며, Docker에서는 `.`를 `_`로 변환해 환경 변수를 구성할 수 있도록 제안하고 있다.

이러한 점을 고려할 때, `-`는 여러 단어를 연결하거나 문장 표현에서 **사람이 읽기 쉬운 구분자(human-readable separator)**로 사용하는 것이 바람직하며, 애플리케이션 수준에서는 `.`을 **계층적 구조 표현**에 사용하는 것이 좋다.

### 2.2. 명명 패턴 예시

- `{서비스/팀}.{환경}.{도메인}.{이벤트/액션}.{포맷}.{버전}`: `payment.prod.order.created.avro.v1`
- 이벤트 기반: `user.signup.success`
- 파이프라인 기반: `data.raw.sales.us-east`
- 멀티 테넌트: `tenant-a.inventory.update`
- 환경 포함: `commerce.payment.transaction.prod`

## 3. 대기업 토픽 네이밍 사례

Kafka를 사용하는 주요 기업들은 운영 환경에 맞춰 각기 다른 네이밍 전략을 사용한다.

| 기업             | 명명 구조                                      | 구분자 | 주요 특징 요약                                                                   | 예시 토픽 명                         |
|----------------|--------------------------------------------|-----|----------------------------------------------------------------------------|---------------------------------|
| **카카오**        | `{서비스}.{환경}.{데이터유형}.{버전}`                  | `.` | 계층적 구조 + 환경(`dev`, `prod`) + 버전(`v1`) 명시<br/>Kafka 표준 스타일에 충실              | `order.prod.result.v1`          |
| **네이버**        | `{팀}-{시스템}-{이벤트}-{환경}`                     | `-` | 팀 오너십 강조<br/>CLI 친화적 구조<br/>환경 정보 필수                                       | `adplatform-user-login-prod`    |
| **라인**         | `{도메인}.{aggregate}.{action}.{format}`      | `.` | DDD 기반<br/>이벤트 포맷 포함(`json`, `avro`)<br/>전체 소문자 사용                         | `user.auth.login.json`          |
| **쿠팡**         | `{source}.{target}.{pipeline-stage}.{env}` | `.` | ETL 파이프라인 구조 반영<br/>단계(`raw`, `transformed`, `aggregated`) 명시<br/>환경 정보 포함 | `db.s3.extract.dev`             |
| **AWS / 넷플릭스** | `{app}.{region}.{event}.{env}`             | `.` | 글로벌 서비스 지향<br/>리전(region) 정보 포함<br/>모니터링 및 운영 편의성 고려                       | `video.us-east-1.playback.prod` |

1. **구분자 일관성 유지**: `.` 또는 `-` 중 선택
2. **환경 정보 포함**: `dev`, `stage`, `prod` 등
3. **오너십 명시**: 팀명 또는 서비스명
4. **데이터 특성 포함**: 데이터 포맷(json/avro), 버전(v1 등)
5. **소문자 강제 사용**: 대소문자 혼용 금지

## 4. 실무 인사이트 요약

Kafka 토픽 네이밍은 일관성, 표현력, 운영 효율성이 핵심이다.
대기업 사례를 통해 다음과 같은 실무적인 인사이트를 정리할 수 있다.

- 대다수 기업은 . 또는 - 중 하나의 구분자를 일관되게 사용
- 버전(v1)과 환경(dev, prod) 정보는 반드시 포함하는 것을 권장
- 팀명 또는 서비스명을 통해 오너십을 명확히 표현
- 필요 시 데이터 포맷(json/avro) 포함하여 이벤트 특성 강조
- 대소문자 혼용은 금지, 모두 소문자 사용
- 리전(region) 또는 테넌트 정보는 글로벌/멀티 테넌트 환경에서 유용
- CLI 친화성과 모니터링 편의성을 고려한 구조가 실무에서 도움이 됨
- 규모가 있는 조직일수록 네이밍 규칙 문서화 + 자동 검증 시스템 도입이 중요

## Reference

- [Confluent 공식 가이드](https://www.confluent.io/learn/kafka-topic-naming-convention/)
- [Apache Kafka - topic naming](https://kafka.apache.org/documentation/#multitenancy-topic-naming)
- [YouTube 네이버 D2 컨퍼런스- 네이버 스케일로 카프카 컨슈머 사용하기](https://www.youtube.com/watch?v=OxMdru93E6k)
- blogs
    - [여기어때 기술 블로그 - Apache Kafka를 사용하여 EDA 적용하기](https://techblog.gccompany.co.kr/apache-kafka%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%98%EC%97%AC-eda-%EC%A0%81%EC%9A%A9%ED%95%98%EA%B8%B0-bf263c79efd0)
    - [Data Engineer Tech - [🧙Kafka] 카프카 토픽 이름 짓는 법(Topic Naming Convention)](https://data-engineer-tech.tistory.com/36)
    - [개기심사 블로그 - 카프카 토픽 이름 정하기 (Kafka Topic Naming Conventions)](https://devfoxstar.github.io/middleware/kafka-topic-naming/)
    - [마루-IT 블로그 - [Apache Kafka] 4. 의미 있는 토픽 이름 작명 방법](https://maru-itdeveloper.tistory.com/36)
    - [Digital Bourgeois - Kafka 토픽 네이밍 규칙: 혼돈을 피하고 효율성을 극대화하는 방법](https://digitalbourgeois.tistory.com/269)
    - [@parktaejung - #10 토픽 작명 방법](https://velog.io/@parktaejung/10-%ED%86%A0%ED%94%BD-%EC%9E%91%EB%AA%85-%EB%B0%A9%EB%B2%95)

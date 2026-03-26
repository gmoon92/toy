# Reverse Engineering 방법론

코드에서 설계 문서로 끌어올리기 위한 6단계 프로세스.

---

## 전체 프로세스

```
1) Scope 정의
2) Reverse Engineering (미시 분석)
3) 흐름 단위 모델링
4) Architecture Recovery (거시 구조화)
5) 설계 문서화
6) 검증 및 보정
```

---

## Step 1: Scope 정의

분석 범위를 제한하지 않으면 레거시 분석은 끝이 없음.

### 결정 사항

| 항목 | 설명 |
|------|------|
| 대상 시스템 범위 | 전체 / 특정 도메인 / 특정 모듈 |
| 목적 | 유지보수 / 리팩토링 / 신규 기능 확장 |
| 분석 깊이 | Level 1 (아키텍처) / Level 2 (모듈) / Level 3 (코드 수준) |

### 산출물
- 분석 범위 정의서 (간단히라도 작성)

---

## Step 2: Reverse Engineering

"구조"가 아닌 "동작과 의미"를 뜯어내는 단계.

### 2-1 기능 단위 분석 (Feature 중심)

| 항목 | 설명 | 예시 |
|------|------|------|
| 기능명 | 분석할 기능의 이름 | 회원 가입 |
| 진입점 | Controller / API / Batch | POST /users |
| 처리 흐름 | 호출 체인 | Controller -> Service -> Validator -> Repository |
| 의존 서비스 | 외부/남부 호출 | EmailService, UserRepository |
| DB 접근 | 사용하는 테이블 | users, user_profile |

### 2-2 코드 구조 분석

- **패키지 구조**: 디렉토리 구성과 네이밍 규칙
- **레이어 구조**: Controller / Service / Repository 등 계층 구분
- **공통 모듈**: Utils, Config, Common 등 재사용 요소

### 2-3 데이터 흐름 분석

- **DTO -> Entity 변환**: 계층 간 데이터 변환
- **트랜잭션 경계**: 트랜잭션 시작/종료 지점
- **외부 시스템 호출**: API, MQ, 캐시 등

### 2-4 의존성 분석

- **모듈 간 호출 관계**: 누가 누구를 호출하는가
- **순환 의존 여부**: A->B->A 형태의 순환 참조
- **외부 라이브러리 의존**: 핵심 라이브러리와 버전

---

## Step 3: 흐름 모델링

코드를 "읽는 상태"에서 벗어나 흐름을 시각적으로 재구성.

**핵심 원칙**: "코드 -> 바로 아키텍처"로 가면 실패함. 반드시 중간에 흐름 모델링 필요.

### 핵심 산출물

| 산출물 | 목적 |
|--------|------|
| Sequence Diagram | 요청 -> 처리 흐름 |
| Data Flow Diagram | 데이터 이동 경로 |
| Use Case 정리 | 사용자 관점 기능 정의 |

---

## Step 4: Architecture Recovery

이해한 내용을 설계 문서 수준으로 끌어올리는 단계.

### 4-1 레이어 구조 정의

```
Presentation -> Application -> Domain -> Infrastructure
```

기존 코드 기준으로 "재분류" - 이상적인 구조가 아닌 실제 구조를 정리.

### 4-2 모듈 구조 정의

| 항목 | 내용 |
|------|------|
| 모듈 리스트 | 분석 대상 모듈 목록 |
| 책임 | 각 모듈의 역할과 경계 |
| 의존 관계 | 모듈 간 의존성 방향 |

### 4-3 컴포넌트 정의

| 항목 | 내용 |
|------|------|
| Component 이름 | 컴포넌트 식별자 |
| 역할 | Handler / Processor / Service 등 |
| 인터페이스 | 외부에 노출되는 메서드 |

### 4-4 시스템 경계 정의

- **외부 시스템**: API, MQ, Redis 등 외부 연동
- **낮부 vs 외부 경계**: 시스템 낮부와 외부의 경계선

### 4-5 아키텍처 스타일 도출

- **Layered**: 계층형 아키텍처
- **Hexagonal**: 포트-어댑터 패턴
- **Event-driven**: 이벤트 기반

대부분 혼합형이므로 명확히 정리할 것.

---

## Step 5: 설계 문서화

문서는 "설명"이 아닌 "의사결정 도구"여야 함. 개선 포인트 포함이 필수.

### 권장 문서 구조

| 섹션 | 내용 |
|------|------|
| 1. Overview | 시스템 목적, 주요 기능 요약 |
| 2. Architecture Overview | 전체 구조 다이어그램, 레이어 설명 |
| 3. Module Structure | 모듈 목록, 책임, 의존성 |
| 4. Component Detail | 각 컴포넌트별 역할, 메서드, 의존성, 입출력 |
| 5. Data Model | ERD, 주요 테이블 설명, 엔티티 관계 |
| 6. Flow | 주요 시나리오별 처리 흐름, Sequence Diagram |
| 7. Integration | 외부 시스템 연동, API/MQ/Cache |
| 8. Technical Decisions | 왜 이렇게 설계됐는지 추론, 문제점, 개선 포인트 |

---

## Step 6: 검증 및 보정

실무에서 가장 많이 빠지는 단계.

### 검증 방법

- **실제 실행 로그 비교**: 문서와 실제 동작이 일치하는가
- **테스트 코드 실행**: 기존 테스트가 문서화된 흐름과 맞는가
- **트래픽 따라가기**: 실제 요청이 문서화된 경로로 흐르는가

---

## 추천 도구

### 정적 분석

| 도구 | 용도 |
|------|------|
| IDE 탐색 기능 | Go to Definition, Find Usages, Call Hierarchy |
| IntelliJ Diagram | 자동 클래스 다이어그램 생성 |
| Dependency Analyzer | 모듈/패키지 의존성 분석 |
| cloc | 코드 라인 통계 |
| depgraph / madge | 의존성 시각화 |

### 런타임 분석

| 도구 | 용도 |
|------|------|
| APM (New Relic, Datadog, Pinpoint) | 성능 및 흐름 모니터링 |
| 로그 트레싱 (Zipkin, Jaeger) | 분산 추적 |

### 시각화/다이어그램

| 도구 | 특징 |
|------|------|
| PlantUML / Mermaid | 텍스트 기반 (버전 관리 가능) |
| Draw.io | GUI 기반 (묵집) |
| Lucidchart | 협업용 (유료) |

---

## 예상 산출물 구조

```
docs/
├── architecture/
│   ├── overview.md           # 전체 아키텍처 개요
│   ├── layers.md             # 레이어 구조
│   └── modules/
│       ├── module-a.md       # 모듈별 상세
│       └── module-b.md
├── data-flow/
│   ├── usecase-1.md          # 주요 유스케이스 흐름
│   └── usecase-2.md
├── analysis/
│   ├── findings.md           # 발견한 사항
│   └── recommendations.md    # 개선 제안
└── diagrams/
    ├── system-context.png    # 시스템 컨텍스트 다이어그램
    ├── container.png         # 컨테이너 다이어그램
    └── component.png         # 컴포넌트 다이어그램
```

---

## 핵심 팁

### 1. 코드 -> 바로 아키텍처 가면 실패함

중간에 "흐름 모델링" 단계가 필수. 코드를 읽은 상태에서 바로 구조를 그리면 핵심 흐름을 놓침.

### 2. 기능 단위로 쪼개라

클례스 단위 분석은 의미 없음. "회원 가입"처럼 하나의 기능(유스케이스) 단위로 접근. Entry point부터 Output까지 흐름을 따라갈 것.

### 3. 문서는 "설명"이 아닌 "의사결정 도구"

단순히 "이런 구조다"가 아니라 "이 문제가 있고, 이렇게 개선할 수 있다"까지 포함해야 가치 있음. Technical Decisions 섹션이 가장 중요.

### 4. 검증 단계를 생략하지 마라

문서와 실제 코드/동작이 일치하는지 반드시 확인. 시간이 없으면 최소한 주요 흐름 2-3개만이라도 검증.

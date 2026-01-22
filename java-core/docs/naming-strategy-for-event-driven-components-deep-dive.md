# Naming Strategy for Common and Domain-Specific Components

## V2 – 확장 네이밍 전략 가이드

_이 문서는 이벤트 기반 및 일반적인 서비스 아키텍처에서 사용되는 핵심 컴포넌트의 명명 규칙을 정의하고, 역할별 네이밍 기준 및 anti-pattern 사례를 제시한다._

*이 전략은 유지보수성, 의도 명확성, 도메인 가독성 향상을 목표로 한다.*

## 컴포넌트 유형별 정의 및 구분

| 유형              | 역할                                                                   | 트리거/호출 방식               | 대표 사용 사례                        |
|-----------------|----------------------------------------------------------------------|-------------------------|---------------------------------|
| **Factory**     | 객체 생성 책임을 분리하여 캡슐화하는 컴포넌트                                            | 명시적 호출<br/>주로 new 대신 사용 | DB 연결 팩토리, 도메인 엔티티 팩토리          |
| **Mapper**      | 데이터 변환·매핑                                                            | 명시적 호출                  | DTO <-> Entity 매핑, DB 레코드→객체 변환 |
| **Resolver**    | 조건에 따라 적절한 전략/객체/값 등을 선택해주는 컴포넌트<br/>값·전략·종속성 동적 해석 및 결정             | 명시적 호출, 런타임 평가          | 파라미터→객체 변환, 주입 대상 선택, 라우팅 등     |
| **Strategy**    | 특정 알고리즘을 캡슐화하여 주입 또는 선택적으로 사용하는 구조<br/>교체 가능한 단일 정책 알고리즘             | 전략 선택(플러그인, 주입)         | 할인 계산 방식, 라우팅 정책, 외부 API 연동     |
| **Coordinator** | 복수의 컴포넌트 흐름을 조정하고, 외부 호출이나 트랜잭션 제어 수행 책임<br/>프로세스·플로우·트랜잭션 등 오케스트레이션 | 복수 처리 흐름 조정             | 워크플로우 단계 실행, 분산 트랜잭션 조정         |
| **Executor**    | 명령 실행 또는 스케줄 기반 처리 책임<br/>명령/작업 실행 및 관리                              | 명시적 실행 호출(동기·비동기)       | 배치 작업 실행, 비동기 Task 처리           |
| **Policy**      | 정책/규칙 기반으로 처리되는 로직 캡슐화<br/>규칙/비즈니스 정책 정의 및 적용                        | 명시적/전략 패턴 또는 구성에 의한 적용  | 결제 정책, 인증 정책                    |

> 명시적 호출: "클라이언트 코드에서 직접 호출 (정적)"<br/>
> 전략 선택: "컨텍스트 또는 설정 기반으로 런타임 시 결정"<br/>
> 복수 처리 흐름 조정: "여러 컴포넌트 조합/호출을 오케스트레이션"

## 컴포넌트 유형별 가이드

### Factory

- **정의**: 복잡한 객체 생성 또는 초기화 책임 분리
- **적용 조건**:
    - new 키워드 대신 생성 로직 중앙화
    - 생성 방식 변경/확장 필요
- **예시**:
    - `UserFactory`
    - `ConnectionFactory`
    - `InvoiceFactory`

### Mapper

- **정의**: 데이터 구조 간 변환
- **적용 조건**:
    - Entity-DTO, Entity-Request 등 구조 변환
- **예시**:
    - `UserMapper`
    - `OrderToInvoiceMapper`

### Resolver

- **정의**: 동적 의사결정(값, 전략, 객체, 엔드포인트, 의존성 등)
- **적용 조건**:
    - 런타임 선택/추상화, 조건/주입 등
- **예시**:
    - `LocaleResolver`
    - `EndpointResolver`
    - `StrategyResolver`

### Strategy

- **정의**: 플러그인식 정책/알고리즘 패턴
- **적용 조건**:
    - 전략 교체/확장 가능해야 할 때
- **예시**:
    - `FeeCalculationStrategy`
    - `FallbackStrategy`
    - `DiscountStrategy`
    - `RetryStrategy`
    - `AuthenticationStrategy`

### Coordinator / Executor

- **Coordinator 정의**: 여러 작업/플로우 오케스트레이션(트랜잭션, 워크플로우 등)
    - 예시:
    - `WorkflowCoordinator`
    - `SagaCoordinator`
- **Executor 정의**: 명령, 잡, 작업 실행 및 스케줄 관리
    - 예시:
    - `EmailSendExecutor`
    - `SettlementExecutor`
    - `TaskExecutor`
    - `BatchJobExecutor`

### Policy

- **정의**: 도메인 비즈니스 룰, 규칙(실행 전략과 별도로 네이밍)
- **예시**:
    - `PaymentPolicy`
    - `ShippingPolicy`

## Policy와 Strategy의 차이

많은 팀들이 Policy, Strategy, Rule을 혼용합니다.

```pgsql
Rule ⊂ Policy ⊂ Strategy
```

- Rule: 단일 판단 로직
- Policy: 복수 `Rule`을 포함한 비즈니스 규치 묶음 (외부 설정에 의해 가능)
- Strategy: 정책을 포함한 실행 알고리즘 또는 플러그인 구조 (Policy + 실행 메커니즘 포함)

## 도메인 특화 명명 규칙

- 정책·계산·인증 등: **Strategy/Policy/Rule**
    - 예:
    - `PointAccumulationPolicy`
    - `OAuthStrategy`
    - `TaxCalculationRule`
    - 결제 도메인:
        - `PaymentPolicy`, `FraudCheckStrategy`, `CardTypeResolver`
    - 정산 도메인:
        - `SettlementCoordinator`, `FeeCalculationStrategy`
    - 계약 도메인:
        - `ContractUploadStrategy`, `ContractRuleEngine`
- 분기/동적 변환 등: **Resolver/Selector/Decider**
    - 예:
    - `RouteSelector`
    - `NextStepDecider`

## 네이밍 Anti-pattern 예시

| Anti-pattern                 | 문제점 설명                                               | 개선 예시                                                                 |
|------------------------------|------------------------------------------------------|-----------------------------------------------------------------------|
| `FooService`                 | 비즈니스 책임/역할 불명확<br/>역할이 모호하고 추상적임. 모든 역할을 떠맡는 경향 발생   | 역할 중심 이름<br/>`FooValidator`, `FooExporter`, `UserRegistrationService` |
| `Util`, `Helper`             | 범용성 및 코드베이스 오염 위험<br/>책임 범위가 불명확하고 테스트/의존성 분리가 어려워짐  | 명확한 역할 기반: `Converter`, `Formatter` 등 더 구체화<br/>필요시 모듈 기준별 Utils 분리   |
| `Manager`                    | 실제 역할이 불명확(관리/조정/실행 불분명)<br/>추상적이며 지나치게 광범위한 책임을 암시함 | 명확한 책임 기반: `Coordinator`, `Executor`, `Handler` 등 더 구체화               |
| 접두사 중복 (`ExcelExcelHandler`) | 불필요한 반복이거나, 컨텍스트 과잉 표현                               | → 컨텍스트를 공통 네임스페이스(패키지)로 위임                                            |

> **역할에 맞는 접미사만 사용하고, 명확한 책임/의도 없이 범용적·모호한 불분명한 이름(Helper/Service/Manager 등)은 의식적으로 지양하자.**

## 체크리스트

- 역할이 명확한가? (객체 생성인지, 흐름 제어인지, 변환인지)
- 고유 책임이 존재하는가? (하나의 목적에 집중?)
- 전략/정책/규칙과 관련이 있다면 구분 기준은 명확한가?
- 모듈/도메인 컨텍스트를 중복해서 표현하고 있진 않은가?
- 테스트 가능성과 대체 가능성(플러그인 구조)을 고려했는가?

## 마무리

이 확장된 네이밍 전략은 애플리케이션의 설계 수준을 한 단계 끌어올릴 수 있는 기반이 된다.

- 비즈니스/기술 책임 명확화
- 유지보수성, 코드 검색, 이해도 향상
- 서비스 계층과 도메인 계층 간의 경계 명료화

향후에는 프로젝트 특성과 도메인에 맞춘 명명 전략(예: `SettlementPolicy`, `VoucherResolver`, `ContractUploadStrategy` 등)을 도출함으로써 더 정교한 아키텍처 설계로 발전시킬 수 있다.

### 실천을 위한 권장 사항

또한 현재 애플리케이션의 네이밍 실태를 점검하고 표준화·문서화하는 과정은 팀 문화 향상에도 기여한다:

- 팀/조직 내부 네이밍 가이드 정책 지속적 문서화
- 정기적 리팩터링 주기에 네이밍 리뷰 포함 권장
- 핵심 클래스 및 컴포넌트의 일관된 네이밍 정비
- 리팩터링 시점마다 네이밍 리뷰 관행화

이러한 전략은 단순한 규칙을 넘어서 가독성, 일관성, 유지보수성이라는 소프트웨어 설계의 핵심 가치를 실현하는 데 중요한 역할을 한다.

> 정교한 명명 전략을 유지하고, 계속해서 개선함으로써 성장하는 개발 문화를 만들어가길 바랍니다.

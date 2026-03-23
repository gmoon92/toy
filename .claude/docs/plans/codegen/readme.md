# 코드 생성 에이전트 팀

기능 개발 및 수정을 위한 AI 코드 생성 에이전트 팀 가이드

### 철학

이 에이전트 팀은 다음 철학을 따릅니다:

- **Tidy First**: 구조 변경과 행위 변경을 분리
- **TDD**: 테스트 먼저 작성 (RED-GREEN-REFACTOR)
- **Simple Design**: "작동하는 가장 단순한 것"부터 시작
- **Small Steps**: 작은 단계로 자주 커밋

### 에이전트 호출 순서

1. 사용자 요청
2. requirements-analyst (요구사항 분석)
3. architecture-analyst (아키텍처 설계)
4. code-designer + testing-strategist (설계 + 테스트)
5. code-designer (구현)
6. quality-manager (검증)
7. 커밋 (structural → test → feat/fix 순)

## 핵심 원칙

| 원칙                          | 설명                            | 적용 예시                     |
|-----------------------------|-------------------------------|---------------------------|
| **Tidy First**              | 구조 변경과 행위 변경을 분리              | 먼저 구조를 바꾸고, 그 후에 기능을 추가   |
| **TDD**                     | 테스트 먼저 작성, RED-GREEN-REFACTOR | 기능 변경 시 테스트 선행            |
| **Simple Design**           | "작동하는 가장 단순한 것"부터 시작          | YAGNI - 필요할 때까지 복잡성 추가 금지 |
| **Small Steps**             | 작은 단계로 나누어 자주 커밋              | 한 번에 한 가지씩, 200줄 이하 커밋    |
| **Single Responsibility**   | 각 모듈/함수는 하나의 책임만              | 클래스/함수 분리 기준              |
| **Explicit over Implicit**  | 명시적이고 투명한 코드 선호               | 타입 추론보다 명시적 타입, 주석 필수     |
| **Fail Fast**               | 오류를 빠르게 발견하고 명시적 처리           | 검증 로직을 입력단에서 수행           |
| **Measure Twice, Cut Once** | 측정 기반 개선                      | 프로파일링 후 최적화, 벤치마크 검증      |

---

## 에이전트 팀 구조

### 역할별 책임

| 에이전트                       | 책임                          | 산출물                   |
|----------------------------|-----------------------------|-----------------------|
| **requirements-analyst**   | 요구사항 분석, 인수조건 정의, 도메인 용어 정의 | 요구사항 명세, 인수조건, 도메인 모델 |
| **architecture-analyst**   | 시스템 아키텍처 설계, 기술 선택, 패턴 적용   | 설계 문서, ADR, 인터페이스 명세  |
| **code-designer**          | 코드 구현, 인터페이스 설계, 리팩토링       | 소스 코드, 인터페이스 정의       |
| **testing-strategist**     | 테스트 전략, TDD 사이클 관리, 커버리지 관리 | 테스트 코드, 테스트 데이터       |
| **quality-manager**        | 코드 품질 검증, 정적 분석, 커밋 규칙 관리   | 품질 리포트, 린트 설정         |
| **security-reviewer** (선택) | 보안 취약점 검사, OWASP 준수 검토      | 보안 검토 리포트             |

### 확장 가능한 에이전트

| 에이전트                  | 적용 시나리오                                 |
|-----------------------|-----------------------------------------|
| performance-optimizer | 쿼리 최적화, 캐싱 전략, 비동기 처리 개선                |
| db-specialist         | 스키마 설계, 마이그레이션, 인덱스 최적화                 |
| api-designer          | RESTful API 설계, GraphQL 스키마, OpenAPI 명세 |
| devops-engineer       | CI/CD 파이프라인, 인프라 코드, 모니터링 설정            |

---

## 워크플로우

### Tidy First + TDD 통합 워크플로우

```
┌─────────────────────────────────────────────────────────────────┐
│                    Tidy First (메타-프로세스)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  1. STRUCTURAL (구조 변경)                                       │
│     ├── 동작 변경 없이 구조만 변경                                │
│     ├── 모든 테스트는 계속 통과해야 함                            │
│     └── 커밋: structural: ...                                   │
│                                                                 │
│  2. BEHAVIORAL (행위 변경) ← TDD 적용                            │
│     │                                                           │
│     │  ┌──────────┐    ┌──────────┐    ┌──────────┐            │
│     └──►│   RED    │───►│  GREEN   │───►│ REFACTOR │            │
│         │          │    │          │    │          │            │
│         │ 테스트 작성 │    │ 최소 구현  │    │ 코드 개선  │            │
│         │ (실패)    │    │ (테스트    │    │ (테스트    │            │
│         │          │    │  통과)    │    │  유지)    │            │
│         └──────────┘    └──────────┘    └──────────┘            │
│              │                                  │               │
│              └──────────────────────────────────┘               │
│                         (반복)                                  │
│                                                                 │
│     커밋: test: → feat/fix: → refactor:                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**중요 원칙:**

- **STRUCTURAL**은 동작 변경 없이 구조만 바꾼다
- 모든 테스트는 structural 커밋 후에도 반드시 통과해야 한다
- **BEHAVIORAL** 변경 시에만 TDD 사이클 적용
- 두 단계를 절대 섞지 않는다 (구조 변경 + 기능 구현 금지)

### TDD 단계별 체크리스트

**RED 단계:**

- [ ] 테스트가 명확한 실패 메시지를 제공하는가?
- [ ] 테스트가 하나의 행위만 검증하는가?
- [ ] 테스트 이름이 의도를 명확히 표현하는가?
- [ ] 테스트가 독립적으로 실행 가능한가?

**GREEN 단계:**

- [ ] 모든 테스트가 통과하는가?
- [ ] "작동하는 가장 단순한" 구현인가? (hardcode도 허용)
- [ ] 불필요한 복잡성을 추가하지 않았는가?
- [ ] 미래의 기능을 예측하지 않았는가? (YAGNI)

**REFACTOR 단계:**

- [ ] 테스트가 여전히 통과하는가?
- [ ] 코드 가독성이 향상되었는가?
- [ ] 중복이 제거되었는가?

---

## 아키텍처 원칙

### 1. Separation of Concerns (관심사 분리)

```
src/
├── domain/              # 도메인 로직 (변경 빈도: 낮음)
│   ├── entity/          # 도메인 엔티티
│   ├── valueobject/     # 값 객체
│   └── repository/      # 저장소 인터페이스
├── application/         # 애플리케이션 로직 (변경 빈도: 중간)
│   ├── service/         # 유스케이스
│   ├── dto/             # 데이터 전송 객체
│   └── mapper/          # 변환기
├── infrastructure/      # 인프라 (변경 빈도: 중간)
│   ├── persistence/     # 데이터 저장
│   ├── external/        # 외부 API
│   └── config/          # 설정
└── api/                 # 인터페이스 (변경 빈도: 높음)
    ├── controller/      # API 엔드포인트
    ├── middleware/      # 미들웨어
    └── validator/       # 입력 검증
```

### 2. Dependency Inversion (의존성 역전)

```java
// 인터페이스 (Domain 계층)
public interface PaymentGateway {
	PaymentResult process(PaymentRequest request);
}

// 구현체 (Infrastructure 계층)
@Service
public class TossPaymentsGateway implements PaymentGateway {
	@Override
	public PaymentResult process(PaymentRequest request) {
		// 토스페이먼츠 API 호출
	}
}

// 사용처 (Application 계층)
@Service
public class OrderService {
	private final PaymentGateway paymentGateway;  // 인터페이스 의존

	public OrderService(PaymentGateway paymentGateway) {
		this.paymentGateway = paymentGateway;
	}
}
```

### 3. 의존성 규칙

```
┌─────────────────────────────────────────┐
│  Layer 4: API / Presentation            │
│  - Controllers, Middleware, Validators  │
├─────────────────────────────────────────┤
│  Layer 3: Application                   │
│  - Services, DTOs, Mappers              │
├─────────────────────────────────────────┤
│  Layer 2: Domain                        │
│  - Entities, Value Objects, Repository  │
│    Interfaces                           │
├─────────────────────────────────────────┤
│  Layer 1: Infrastructure                │
│  - Persistence, External APIs, Config   │
└─────────────────────────────────────────┘
```

**규칙:**

1. **단방향 의존성**: 상위 레이어는 하위 레이어만 의존
2. **Domain 불변성**: Domain은 어떤 레이어에도 의존하지 않음
3. **순환 의존성 금지**: circular dependency 절대 불가

---

## 코드 작성 가이드

### 1. 파일 및 코드 크기 제한

| 요소         | 목표 크기 | 최대 크기 | 조치      |
|------------|-------|-------|---------|
| **파일**     | ~300줄 | 500줄  | 초과 시 분할 |
| **클래스**    | ~200줄 | 400줄  | 책임 분리   |
| **함수/메서드** | ~20줄  | 50줄   | 함수 추출   |
| **라인 길이**  | ~80자  | 120자  | 개행      |

### 2. 명명 규칙

| 요소         | Java        | Python      | TypeScript           |
|------------|-------------|-------------|----------------------|
| **클래스/타입** | PascalCase  | PascalCase  | PascalCase           |
| **함수/메서드** | camelCase   | snake_case  | camelCase            |
| **변수**     | camelCase   | snake_case  | camelCase            |
| **상수**     | UPPER_SNAKE | UPPER_SNAKE | UPPER_SNAKE          |
| **파일명**    | PascalCase  | snake_case  | camelCase/PascalCase |
| **패키지/모듈** | lowercase   | lowercase   | camelCase            |

### 3. 단순성 원칙 (YAGNI)

> **"Do the simplest thing that could possibly work"** - Kent Beck

- 필요할 때까지 기능을 추가하지 않는다
- 미래의 요구사항을 예측하지 않는다
- 현재의 테스트를 통과하는 가장 단순한 코드를 작성한다

```java
// ❌ 과도한 설계
public interface PaymentGateway {
	PaymentResult process(PaymentRequest request);

	PaymentResult refund(String transactionId);  // 아직 필요 없음

	List<PaymentHistory> getHistory(String customerId);  // 아직 필요 없음
}

// ✅ 단순한 설계
public interface PaymentGateway {
	PaymentResult process(PaymentRequest request);
}
```

---

## 테스트 전략

### 1. 테스트 디렉토리 구조

```
src/test/
├── java/
│   └── com/example/
│       ├── common/                   # 테스트 공용 코드
│       │   ├── fixtures/             # 테스트 픽스처
│       │   └── assertions/           # 커스텀 Assertion
│       ├── unit/                     # 단위 테스트 (60%)
│       │   ├── domain/
│       │   └── application/
│       ├── integration/              # 통합 테스트 (30%)
│       └── e2e/                      # E2E 테스트 (10%)
└── resources/
    └── application-test.yml
```

### 2. 테스트 유형별 비율

| 카테고리            | 비율  | 목표 커버리지   | 목적           |
|-----------------|-----|-----------|--------------|
| **Unit**        | 60% | 80%       | 개별 클래스/함수 검증 |
| **Integration** | 30% | 핵심 시나리오   | 모듈 간 상호작용 검증 |
| **E2E**         | 10% | 핵심 사용자 흐름 | 사용자 관점 흐름 검증 |

### 3. Given-When-Then 패턴

```java

@Test
@DisplayName("재고가 부족하면 주문 생성에 실패해야 한다")
void createOrder_WhenInsufficientStock_ShouldFail() {
	// Given
	CreateOrderRequest request = CreateOrderRequestFixture.valid();
	given(inventoryService.getStock(request.getProductId()))
	  .willReturn(0);

	// When & Then
	assertThatThrownBy(() -> orderService.createOrder(request))
	  .isInstanceOf(InsufficientStockException.class)
	  .hasMessageContaining("재고가 부족합니다");
}
```

---

## 커밋 규칙

### 1. Tidy First 커밋 순서

```
1. structural: 코드 구조 변경 (분리된 첫 번째 커밋)
2. test: 테스트 추가 (RED 단계)
3. feat/fix: 기능 구현 (GREEN 단계)
4. refactor: 리팩토링 (REFACTOR 단계)
```

**예시:**

```bash
# 1. 구조적 변경
structural: create auth module structure

# 2. 테스트 추가 (RED)
test(auth): add authentication interface tests

# 3. 기능 구현 (GREEN)
feat(auth): implement JWT token generation

# 4. 리팩토링 (REFACTOR)
refactor(auth): extract validation logic
```

### 2. 에이전트 특화 타입

| 타입           | 설명                      | 예시                                           |
|--------------|-------------------------|----------------------------------------------|
| `structural` | 코드 재조직, 리팩토링 (동작 변경 없음) | `structural: extract validation module`      |
| `feat`       | 새로운 기능 구현               | `feat: add user authentication flow`         |
| `fix`        | 버그 수정                   | `fix: resolve null pointer in order service` |
| `test`       | 테스트 추가/수정               | `test(order): add order creation tests`      |
| `docs`       | 문서 추가/수정                | `docs: document deployment process`          |
| `perf`       | 성능 최적화                  | `perf: optimize string allocation`           |
| `refactor`   | 코드 개선                   | `refactor: extract validation logic`         |

### 3. 커밋 크기 가이드라인

| 변경 유형        | 권장 크기   | 최대 크기 |
|--------------|---------|-------|
| `structural` | 20-80줄  | 200줄  |
| `test`       | 20-100줄 | 200줄  |
| `feat`/`fix` | 30-100줄 | 200줄  |
| `refactor`   | 20-80줄  | 200줄  |

---

## 품질 기준

### 1. 품질 게이트

| 메트릭      | 목표값                | 최소값  | 측정 도구      | 실패 시       |
|----------|--------------------|------|------------|------------|
| 라인 커버리지  | ≥ 80%              | 70%  | JaCoCo     | ❌ PR 병합 불가 |
| 브랜치 커버리지 | ≥ 70%              | 60%  | JaCoCo     | ⚠️ 경고      |
| 정적 분석 이슈 | 0 Critical/Blocker | 0    | SonarQube  | ❌ PR 병합 불가 |
| 코드 중복    | < 3%               | 5%   | SonarQube  | ⚠️ 경고      |
| 순환 복잡도   | < 10               | 15   | Checkstyle | ⚠️ 경고      |
| 함수 길이    | ≤ 50줄              | 100줄 | Checkstyle | ⚠️ 경고      |
| 파일 길이    | ≤ 500줄             | 800줄 | Checkstyle | ⚠️ 경고      |

### 2. 테스트 실행 시간 목표

| 테스트 유형       | 목표    | 최대  |
|--------------|-------|-----|
| 단위 테스트 (전체)  | < 30초 | 60초 |
| 통합 테스트 (전체)  | < 2분  | 5분  |
| E2E 테스트 (전체) | < 5분  | 10분 |
| 전체 테스트       | < 10분 | 15분 |

### 3. 금지 패턴 예시

```java
// ❌ 피할 것
public class BadExamples {
	// 1. 묵시적 null 처리
	public void process(String input) {
		System.out.println(input.length());  // NPE 위험
	}

	// 2. 매직 넘버
	public double calculatePrice(int quantity) {
		return quantity * 0.9;
	}

	// 3. 예외 무마
	public void save(Order order) {
		try {
			repository.save(order);
		} catch (Exception e) {
			// 아무것도 하지 않음
		}
	}
}

// ✅ 권장
public class GoodExamples {
	// 1. 명시적 null 처리
	public void process(String input) {
		Objects.requireNonNull(input, "input must not be null");
		System.out.println(input.length());
	}

	// 2. 상수화
	private static final double DISCOUNT_RATE = 0.9;

	public double calculatePrice(int quantity) {
		return quantity * DISCOUNT_RATE;
	}

	// 3. 예외 전파
	public void save(Order order) {
		try {
			repository.save(order);
		} catch (DataAccessException e) {
			throw new OrderPersistenceException("Failed to save order", e);
		}
	}
}
```

---

## 템플릿

### 1. 기능 개발 계획 (PLAN.md)

```markdown
# Plan: [기능명]

## 개요

- 목적: 무엇을 해결하는가?
- 범위: 무엇을 포함/제외하는가?

## 요구사항

### 기능적 요구사항

- [ ] 요구사항 1
- [ ] 요구사항 2

### 비기능적 요구사항

- 성능: 응답 시간 < 200ms
- 보안: 인증 필요

## 변경 분류

- [ ] 구조적 변경 (structural)
- [ ] 기능 추가 (feat)
- [ ] 버그 수정 (fix)

## 설계

### 인터페이스

```java
public interface OrderService {
    OrderResult createOrder(CreateOrderRequest request);
}
```

### 테스트 계획

- 단위 테스트: OrderService 로직
- 통합 테스트: API 엔드포인트

```

### 2. 아키텍처 결정 기록 (ADR.md)

```markdown
# ADR-XXX: [결정 제목]

## 상태
- 제안됨 / 수락됨 / 반려됨 / 대체됨

## 컨텍스트
- 어떤 문제를 해결해야 하는가?

## 결정
- 무엇을 결정했는가?

## 대안
| 대안 | 장점 | 단점 |
|------|------|------|
| 대안 1 | ... | ... |
| 대안 2 | ... | ... |

## 영향
- 이 결정의 영향은?
```

---

## 부록: 빠른 참조

### 에이전트 호출 순서

```
새로운 기능 요청
    ↓
requirements-analyst
    ↓
architecture-analyst
    ↓
code-designer + testing-strategist
    ↓
code-designer
    ↓
quality-manager
    ↓
security-reviewer (선택)
    ↓
커밋 (structural → test → feat/fix 순)
```

### TDD 사이클 체크리스트

**RED:**

- [ ] 테스트가 명확한 실패 메시지 제공
- [ ] 테스트가 하나의 행위만 검증
- [ ] 테스트 이름이 의도 명확히 표현

**GREEN:**

- [ ] 모든 테스트 통과
- [ ] "작동하는 가장 단순한" 구현 (YAGNI)
- [ ] 커밋: `feat: ...`

**REFACTOR:**

- [ ] 테스트 여전히 통과
- [ ] 코드 가독성 향상
- [ ] 중복 제거
- [ ] 커밋: `refactor: ...`

### 품질 체크리스트

- [ ] 단위 테스트 커버리지 ≥ 80%
- [ ] 모든 테스트 통과
- [ ] 정적 분석 이슈 0개 (Critical/Blocker)
- [ ] 함수 길이 ≤ 50줄
- [ ] 파일 길이 ≤ 500줄
- [ ] Tidy First 적용 (structural 먼저)

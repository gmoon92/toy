# 코드 생성 에이전트 팀

범용 기능 개발 및 수정을 위한 AI 코드 생성 에이전트 팀 가이드

> **철학적 기반**: 이 문서는 Kent Beck의 "Tidy First?", "Test-Driven Development: By Example", "Extreme Programming Explained"의 철학을 기반으로 합니다.

**버전:** 3.0
**작성일:** 2026-02-23
**지원 기술:** Java/Spring, Python, TypeScript, Go 등 (언어 중립적)
**지원 아키텍처:** 웹 애플리케이션, 마이크로서비스, 라이브러리

---

## 목차

1. [시작하기](#시작하기)
2. [핵심 원칙](#핵심-원칙)
3. [에이전트 팀 구조](#에이전트-팀-구조)
4. [통합 워크플로우](#통합-워크플로우)
5. [아키텍처 원칙](#아키텍처-원칙)
6. [코드 구조](#코드-구조)
7. [테스트 전략](#테스트-전략)
8. [커밋 규칙](#커밋-규칙)
9. [품질 기준](#품질-기준)
10. [템플릿](#템플릿)

---

## 시작하기

### 에이전트 팀 구조

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         코드 생성 에이전트 팀                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  requirements-analyst    →   architecture-analyst                      │
│       (요구사항 분석)            (아키텍처 설계)                          │
│                                                                         │
│              ↓                      ↓                                   │
│  code-designer  ←→  testing-strategist  ←→  quality-manager             │
│    (코드 설계)         (테스트 전략)           (품질 검증)                  │
│                                                                         │
│  [선택적] security-reviewer, performance-optimizer 등                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 빠른 시작

#### 1. 새로운 기능 개발

```
사용자 요청: "사용자 인증 기능을 추가해줘"
     │
     ▼
┌──────────────────────────────────────────────────────────────┐
│ 1. requirements-analyst                                      │
│    • 기능 요구사항 분석                                       │
│    • 인수 조건 작성 (Given-When-Then)                        │
└──────────────────────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────────────────────┐
│ 2. architecture-analyst                                      │
│    • 인증 아키텍처 설계 (JWT vs Session)                      │
│    • 패키지 구조 결정: auth/                                  │
│    • ADR 작성 (필요시)                                        │
└──────────────────────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────────────────────┐
│ 3. testing-strategist + code-designer 협업                   │
│    • 테스트 먼저 작성 (RED)                                   │
│    • 인터페이스 설계 (AuthService)                            │
└──────────────────────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────────────────────┐
│ 4. code-designer 구현 (GREEN → REFACTOR)                     │
│    • 최소한의 구현                                            │
│    • 코드 개선                                                │
└──────────────────────────────────────────────────────────────┘
     │
     ▼
┌──────────────────────────────────────────────────────────────┐
│ 5. quality-manager 검증                                      │
│    • 코드 커버리지 ≥ 80% 확인                                │
│    • 정적 분석 이슈 0개 확인                                  │
│    • 커밋 메시지 컨벤션 검사                                   │
└──────────────────────────────────────────────────────────────┘
```

#### 2. 커밋 순서 (Tidy First)

```bash
# 1. 구조적 변경
structural: create auth module structure

# 2. 테스트 추가 (RED)
test(auth): add authentication interface tests

# 3. 기능 구현 (GREEN)
feat(auth): implement JWT token generation

# 4. 추가 테스트
test(auth): add edge case tests for invalid credentials

# 5. 리팩토링 (REFACTOR)
refactor(auth): extract validation logic

# 6. 문서화
docs(auth): add API documentation
```

---

## 핵심 원칙

| 원칙 | 설명 | 적용 예시 |
|------|------|-----------|
| **Tidy First** | 구조적 변경과 행위적 변경을 완전히 분리 | 먼저 구조를 바꾸고, 그 후에 기능을 추가 |
| **TDD** | 테스트 먼저 작성, RED-GREEN-REFACTOR | 기능 변경 시 테스트 선행 |
| **Simple Design** | "작동하는 가장 단순한 것"부터 시작 | YAGNI - 필요할 때까지 복잡성 추가 금지 |
| **Small Steps** | 작은 단계로 나누어 자주 커밋 | 한 번에 한 가지씩, 200줄 이하 커밋 |
| **Single Responsibility** | 각 모듈/함수는 하나의 책임만 | 클래스/함수 분리 기준 |
| **Explicit over Implicit** | 명시적이고 투명한 코드 선호 | 타입 추론보다 명시적 타입, 주석 필수 |
| **Fail Fast** | 오류를 빠르게 발견하고 명시적 처리 | 검증 로직을 입력단에서 수행 |
| **Measure Twice, Cut Once** | 측정 기반 개선 | 프로파일링 후 최적화, 벤치마크 검증 |

---

## 에이전트 팀 구조

### 역할별 책임

| 에이전트 | 책임 | 산출물 |
|----------|------|--------|
| **requirements-analyst** | 요구사항 분석, 인수조건 정의, 도메인 용어 정의 | 요구사항 명세, 인수조건, 도메인 모델 |
| **architecture-analyst** | 시스템 아키텍처 설계, 기술 선택, 패턴 적용 | 설계 문서, ADR, 인터페이스 명세 |
| **code-designer** | 코드 구현, 인터페이스 설계, 리팩토링 | 소스 코드, 인터페이스 정의 |
| **testing-strategist** | 테스트 전략, TDD 사이클 관리, 커버리지 관리 | 테스트 코드, 테스트 데이터 |
| **quality-manager** | 코드 품질 검증, 정적 분석, 커밋 규칙 관리 | 품질 리포트, 린트 설정 |
| **security-reviewer** (선택) | 보안 취약점 검사, OWASP 준수 검토 | 보안 검토 리포트 |

### 확장 가능한 에이전트

| 에이전트 | 적용 시나리오 |
|----------|---------------|
| performance-optimizer | 쿼리 최적화, 캐싱 전략, 비동기 처리 개선 |
| db-specialist | 스키마 설계, 마이그레이션, 인덱스 최적화 |
| api-designer | RESTful API 설계, GraphQL 스키마, OpenAPI 명세 |
| devops-engineer | CI/CD 파이프라인, 인프라 코드, 모니터링 설정 |

---

## 통합 워크플로우

### TDD 사이클

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│     RED      │────►│    GREEN     │────►│   REFACTOR   │
│              │     │              │     │              │
│ 1. 테스트 작성 │     │ 2. 최소한의   │     │ 3. 코드 개선  │
│    (실패 확인)│     │    구현      │     │    (테스트    │
│              │     │    (테스트    │     │     유지)    │
│              │     │     통과)    │     │              │
└──────────────┘     └──────────────┘     └──────────────┘
      │                                            │
      └────────────────────────────────────────────┘
                        (반복)
```

**각 단계별 체크리스트:**

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

### Tidy First 워크플로우

> **핵심 원칙**: "구조(structure)"와 "행위(behavior)"를 완전히 분리하여 별도의 커밋으로 처리한다.

```
┌─────────────────────────────────────────────────────────────────┐
│                    Tidy First (메타-프로세스)                     │
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

**중요:**
- **STRUCTURAL**은 **동작 변경 없이** 구조만 바꾼다
- 모든 테스트는 structural 커밋 후에도 **반드시 통과**해야 한다
- **BEHAVIORAL** 변경 시에만 TDD 사이클 적용
- 두 단계를 절대 섞지 않는다 (구조 변경 + 기능 구현 금지)

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

**Java/Spring 예시:**

```java
// ✅ 관심사 분리
@RestController
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }
}

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public OrderResponse createOrder(OrderRequest request) {
        // 비즈니스 로직
    }
}

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
```

### 2. Dependency Inversion (의존성 역전)

```java
// ✅ 인터페이스에 의존
public interface PaymentGateway {
    PaymentResult process(PaymentRequest request);
}

// 구현체
@Service
public class TossPaymentsGateway implements PaymentGateway {
    @Override
    public PaymentResult process(PaymentRequest request) {
        // 토스페이먼츠 API 호출
    }
}

// 사용처
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

### 4. Repository Pattern

```java
// 인터페이스 (Domain 계층)
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    Order save(Order order);
    List<Order> findByStatus(OrderStatus status);
}

// 구현체 (Infrastructure 계층)
@Repository
public class JpaOrderRepository implements OrderRepository {
    private final JpaOrderRepositoryDelegate delegate;

    @Override
    public Optional<Order> findById(OrderId id) {
        return delegate.findById(id.value())
            .map(this::toDomain);
    }
}
```

### 5. Service Layer Pattern

```java
@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;

    public OrderResult createOrder(CreateOrderCommand command) {
        // 1. 도메인 객체 생성
        Order order = Order.create(command);

        // 2. 비즈니스 로직 실행
        PaymentResult payment = paymentGateway.process(order.toPaymentRequest());

        // 3. 결과 저장
        order.markAsPaid(payment.getTransactionId());
        Order savedOrder = orderRepository.save(order);

        // 4. 결과 반환
        return OrderResult.from(savedOrder);
    }
}
```

---

## 코드 구조

### 1. 프로젝트 디렉토리 구조

**Java/Spring 예시:**

```
src/
├── main/
│   ├── java/
│   │   └── com/example/
│   │       ├── domain/
│   │       │   ├── order/
│   │       │   │   ├── Order.java
│   │       │   │   ├── OrderId.java
│   │       │   │   ├── OrderStatus.java
│   │       │   │   └── OrderRepository.java
│   │       │   └── product/
│   │       ├── application/
│   │       │   ├── order/
│   │       │   │   ├── OrderService.java
│   │       │   │   ├── OrderDto.java
│   │       │   │   └── OrderMapper.java
│   │       ├── infrastructure/
│   │       │   ├── persistence/
│   │       │   │   ├── JpaOrderRepository.java
│   │       │   │   └── OrderEntity.java
│   │       │   └── config/
│   │       │       └── AppConfig.java
│   │       └── api/
│   │           ├── order/
│   │           │   ├── OrderController.java
│   │           │   ├── OrderRequest.java
│   │           │   └── OrderResponse.java
│   │           └── exception/
│   │               └── GlobalExceptionHandler.java
│   └── resources/
│       ├── application.yml
│       └── application-local.yml
└── test/
    ├── java/
    │   └── com/example/
    │       ├── domain/
    │       ├── application/
    │       ├── infrastructure/
    │       └── api/
    └── resources/
```

### 2. 파일 및 코드 크기 제한

| 요소 | 목표 크기 | 최대 크기 | 조치 |
|------|-----------|-----------|------|
| **파일** | ~300줄 | 500줄 | 초과 시 분할 |
| **클래스** | ~200줄 | 400줄 | 책임 분리 |
| **함수/메서드** | ~20줄 | 50줄 | 함수 추출 |
| **라인 길이** | ~80자 | 120자 | 개행 |

**Java 예시:**

```java
// ✅ 권장: 20-30줄 이내, 하나의 책임
public OrderResult processOrder(CreateOrderRequest request) {
    validateRequest(request);
    Order order = createOrder(request);
    Order savedOrder = orderRepository.save(order);
    return mapToResult(savedOrder);
}

// ❌ 피할 것: 100줄 이상, 여러 책임
public OrderResult processOrder(CreateOrderRequest request) {
    // 검증 로직 (20줄)
    // 변환 로직 (30줄)
    // 저장 로직 (20줄)
    // 알림 로직 (20줄)
    // 로깅 로직 (15줄)
    // 결과 반환 (10줄)
}
```

### 3. 명명 규칙

| 요소 | Java | Python | TypeScript |
|------|------|--------|------------|
| **클래스/타입** | PascalCase | PascalCase | PascalCase |
| **함수/메서드** | camelCase | snake_case | camelCase |
| **변수** | camelCase | snake_case | camelCase |
| **상수** | UPPER_SNAKE | UPPER_SNAKE | UPPER_SNAKE |
| **파일명** | PascalCase | snake_case | camelCase/PascalCase |
| **패키지/모듈** | lowercase | lowercase | camelCase |

**명명 가이드라인:**

```java
// ✅ 의도를 명확히 표현
public class OrderService { }
public class CustomerValidator { }
public OrderResult processOrder(OrderRequest request) { }

// ❌ 모호한 이름
public class Service { }  // 어떤 서비스?
public class Util { }     // 어떤 유틸?
public void process() { }  // 무엇을 처리?

// ✅ 타입 이름은 명사
public class Order { }
public class OrderRepository { }

// ✅ 메서드 이름은 동사/동사구
public Order createOrder() { }
public void cancelOrder() { }
public boolean isValid() { }
```

### 4. 단순성 원칙 (Simple Design / YAGNI)

> **"Do the simplest thing that could possibly work"** - Kent Beck

**YAGNI (You Aren't Gonna Need It):**
- 필요할 때까지 기능을 추가하지 않는다
- 미래의 요구사항을 예측하지 않는다
- 현재의 테스트를 통과하는 가장 단순한 코드를 작성한다

```java
// ❌ 과도한 설계 (미래의 필요를 예측)
public interface PaymentGateway {
    PaymentResult process(PaymentRequest request);
    PaymentResult refund(String transactionId);  // 아직 필요 없음
    PaymentResult partialRefund(String transactionId, BigDecimal amount);  // 아직 필요 없음
    List<PaymentHistory> getHistory(String customerId);  // 아직 필요 없음
}

// ✅ 단순한 설계 (현재 필요한 것만)
public interface PaymentGateway {
    PaymentResult process(PaymentRequest request);
}
```

**단순성을 위한 4가지 원칙:**
1. **테스트 통과**: 모든 테스트가 통과해야 한다
2. **의도 명확**: 코드가 하는 일을 명확히 드러내야 한다
3. **중복 없음**: 동일한 로직의 반복 금지 (DRY)
4. **요소 최소화**: 클래스/메서드/라인 수 최소화

### 5. 주석 및 문서화

**공개 API 문서화 (Java - Javadoc):**

```java
/**
 * 주문을 생성합니다.
 *
 * @param command 주문 생성 명령
 * @return 생성된 주문 결과
 * @throws IllegalArgumentException 유효하지 않은 입력인 경우
 * @throws PaymentFailedException 결제 실패 시
 *
 * @see CreateOrderCommand
 * @since 1.0
 */
public OrderResult createOrder(CreateOrderCommand command) {
    // 구현
}
```

**내부 구현 주석:**

```java
public OrderResult createOrder(CreateOrderCommand command) {
    // 입력 검증
    validateCommand(command);

    // 재고 확인 및 예약
    // Note: 재고 예약은 5분간 유효하며, 이후 자동 해제됨
    InventoryReservation reservation = inventoryService.reserve(
        command.getProductId(),
        command.getQuantity(),
        Duration.ofMinutes(5)
    );

    try {
        // 주문 생성 및 저장
        Order order = Order.create(command);
        Order savedOrder = orderRepository.save(order);

        return OrderResult.from(savedOrder);
    } catch (Exception e) {
        // 예약 실패 시 재고 해제
        inventoryService.release(reservation);
        throw new OrderCreationException("Failed to create order", e);
    }
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
│       │   │   ├── OrderFixture.java
│       │   │   └── UserFixture.java
│       │   └── assertions/           # 커스텀 Assertion
│       │       └── OrderAssert.java
│       ├── unit/                     # 단위 테스트 (60%)
│       │   ├── domain/
│       │   │   └── OrderTest.java
│       │   └── application/
│       │       └── OrderServiceTest.java
│       ├── integration/              # 통합 테스트 (30%)
│       │   ├── OrderRepositoryTest.java
│       │   └── OrderControllerTest.java
│       └── e2e/                      # E2E 테스트 (10%)
│           └── OrderE2ETest.java
└── resources/
    └── application-test.yml
```

### 2. 테스트 유형별 비율

| 카테고리 | 비율 | 목표 커버리지 | 목적 |
|----------|------|---------------|------|
| **Unit** | 60% | 80% | 개별 클래스/함수 검증 |
| **Integration** | 30% | 핵심 시나리오 | 모듈 간 상호작용 검증 |
| **E2E** | 10% | 핵심 사용자 흐름 | 사용자 관점 흐름 검증 |
| **Total** | 100% | 80%+ | 전체 신뢰성 확보 |

### 3. Given-When-Then 패턴

```java
@Test
@DisplayName("재고가 부족하면 주문 생성에 실패해야 한다")
void createOrder_WhenInsufficientStock_ShouldFail() {
    // Given
    CreateOrderRequest request = CreateOrderRequestFixture.valid();
    given(inventoryService.getStock(request.getProductId()))
        .willReturn(0);  // 재고 없음

    // When & Then
    assertThatThrownBy(() -> orderService.createOrder(request))
        .isInstanceOf(InsufficientStockException.class)
        .hasMessageContaining("재고가 부족합니다");
}
```

### 4. 테스트 픽스처

```java
public class OrderFixture {

    public static Order validOrder() {
        return Order.builder()
            .orderId("ORDER-001")
            .productId("PROD-001")
            .quantity(2)
            .customerId("CUST-001")
            .status(OrderStatus.CREATED)
            .build();
    }

    public static Order cancelledOrder() {
        Order order = validOrder();
        order.cancel();
        return order;
    }

    public static CreateOrderRequest validRequest() {
        return CreateOrderRequest.builder()
            .productId("PROD-001")
            .quantity(2)
            .customerId("CUST-001")
            .build();
    }
}
```

### 5. 단위 테스트 예시 (Java)

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // Given
        CreateOrderRequest request = OrderFixture.validRequest();
        given(inventoryService.getStock(any())).willReturn(10);
        given(orderRepository.save(any(Order.class)))
            .willAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResult result = orderService.createOrder(request);

        // Then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("재고 부족 시 예외 발생")
    void createOrder_InsufficientStock_ThrowsException() {
        // Given
        CreateOrderRequest request = OrderFixture.validRequest();
        given(inventoryService.getStock(any())).willReturn(0);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(InsufficientStockException.class);
    }
}
```

### 6. 통합 테스트 예시 (Spring)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문 생성 API 통합 테스트")
    void createOrder_API_Success() throws Exception {
        // Given
        CreateOrderRequest request = OrderFixture.validRequest();

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.status").value("CREATED"));

        assertThat(orderRepository.findAll()).hasSize(1);
    }
}
```

### 7. 테스트 커버리지

| 메트릭 | 목표값 | 최소값 | 측정 도구 (Java) |
|--------|--------|--------|------------------|
| 라인 커버리지 | 80% | 70% | JaCoCo |
| 브랜치 커버리지 | 70% | 60% | JaCoCo |
| 메서드 커버리지 | 85% | 75% | JaCoCo |

**CI 통합:**

```yaml
# .github/workflows/test.yml
name: Test

on: [pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run tests
        run: ./mvnw test jacoco:report

      - name: Check coverage
        run: |
          COVERAGE=$(cat target/site/jacoco/index.html | grep -oP 'Total[^%]+%' | head -1 | grep -oP '\d+')
          if [[ $COVERAGE -lt 70 ]]; then
            echo "Coverage $COVERAGE% is below 70%"
            exit 1
          fi
          echo "Coverage: $COVERAGE%"
```

---

## 커밋 규칙

### 1. 프로젝트 커밋 스킬 참조

코드 생성 에이전트 팀은 프로젝트의 `commit` 스킬을 사용합니다. 본 문서는 에이전트 팀 특화 내용(Tidy First)만 포함합니다.

> **참조**: 프로젝트 표준 커밋 규칙은 `commit` 스킬을 사용하세요.

### 2. Tidy First 커밋 순서

코드 생성 에이전트는 **Tidy First** 원칙에 따라 다음 순서로 커밋합니다:

```
1. structural: 코드 구조 변경 (분리된 첫 번째 커밋)
2. test: 테스트 추가 (RED 단계)
3. feat/fix: 기능 구현 (GREEN 단계)
4. refactor: 리팩토링 (REFACTOR 단계)
```

### 3. 에이전트 특화 타입

| 타입 | 설명 | 예시 |
|------|------|------|
| `structural` | 코드 재조직, 리팩토링 (동작 변경 없음) | `structural: extract validation module` |
| `feat` | 새로운 기능 구현 | `feat: add user authentication flow` |
| `fix` | 버그 수정 | `fix: resolve null pointer in order service` |
| `test` | 테스트 추가/수정 | `test(order): add order creation tests` |
| `docs` | 문서 추가/수정 | `docs: document deployment process` |
| `perf` | 성능 최적화 | `perf: optimize string allocation` |
| `refactor` | 코드 개선 | `refactor: extract validation logic` |

### 4. TDD 사이클과 커밋

```
┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐
│   RED   │────►│  GREEN  │────►│ REFACTOR│────►│  COMMIT │
│         │     │         │     │         │     │         │
│  test:  │     │ feat:   │     │refactor:│     │ 각 단계   │
│  추가    │     │  구현    │     │  개선    │     │ 완료 시   │
└─────────┘     └─────────┘     └─────────┘     └─────────┘
```

### 5. 커밋 크기 가이드라인

| 변경 유형 | 권장 크기 | 최대 크기 | 설명 |
|-----------|-----------|-----------|------|
| `structural` | 20-80줄 | 200줄 | 순수 구조 변경만 |
| `test` | 20-100줄 | 200줄 | 테스트 파일 단위 |
| `feat`/`fix` | 30-100줄 | 200줄 | 하나의 기능만 |
| `refactor` | 20-80줄 | 200줄 | 동작 변경 없음 |

### 6. 커밋 체크리스트

**Tidy First 원칙:**
```
□ 구조 변경(structural)과 기능 변경(feat/fix)을 섞지 않았는가?
□ structural 커밋 후 모든 테스트가 통과하는가?
□ behavioral 변경 시에만 TDD 사이클을 적용했는가?
```

**TDD 사이클:**
```
□ test 커밋에는 구현이 없는가? (RED)
□ feat 커밋에는 테스트 통과가 포함되는가? (GREEN)
□ refactor 커밋에는 동작 변경이 없는가?
□ 모든 테스트가 통과한 후에만 refactor 커밋했는가?
```

**일반:**
```
□ 커밋이 하나의 목적만 가지는가?
□ 커밋 크기가 200줄 이하인가?
□ 커밋 메시지가 프로젝트 컨벤션을 따르는가?
```

---

## 품질 기준

### 1. 품질 게이트

| 메트릭 | 목표값 | 최소값 | 측정 도구 (Java) | 실패 시 |
|--------|--------|--------|------------------|---------|
| 라인 커버리지 | ≥ 80% | 70% | JaCoCo | ❌ PR 병합 불가 |
| 브랜치 커버리지 | ≥ 70% | 60% | JaCoCo | ⚠️ 경고 |
| 메서드 커버리지 | ≥ 85% | 75% | JaCoCo | ⚠️ 경고 |
| 정적 분석 이슈 | 0 Critical/Blocker | 0 | SonarQube, Checkstyle | ❌ PR 병합 불가 |
| 코드 중복 | < 3% | 5% | SonarQube | ⚠️ 경고 |
| 순환 복잡도 | < 10 per method | 15 | Checkstyle | ⚠️ 경고 |
| 함수 길이 | ≤ 50줄 | 100줄 | Checkstyle | ⚠️ 경고 |
| 파일 길이 | ≤ 500줄 | 800줄 | Checkstyle | ⚠️ 경고 |

### 2. 금지 패턴

**Java 예시:**

```java
// ❌ 피할 것
public class BadExamples {
    // 1. 묵시적 null 처리
    public void process(String input) {
        System.out.println(input.length());  // NPE 위험
    }

    // 2. 매직 넘버
    public double calculatePrice(int quantity) {
        return quantity * 0.9;  // 0.9이 무엇인지 불분명
    }

    // 3. 거대한 클래스
    public class OrderService {
        // 500+ 줄, 20+ 메서드
    }

    // 4. 예외 무마
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

    // 3. 단일 책임
    public class OrderCreationService { /* 생성 책임 */ }
    public class OrderQueryService { /* 조회 책임 */ }

    // 4. 예외 전파
    public void save(Order order) {
        try {
            repository.save(order);
        } catch (DataAccessException e) {
            throw new OrderPersistenceException("Failed to save order", e);
        }
    }
}
```

### 3. 테스트 실행 시간 목표

| 테스트 유형 | 목표 | 최대 |
|-------------|------|------|
| 단위 테스트 (전체) | < 30초 | 60초 |
| 통합 테스트 (전체) | < 2분 | 5분 |
| E2E 테스트 (전체) | < 5분 | 10분 |
| 전체 테스트 | < 10분 | 15분 |

### 4. 코드 리뷰 체크리스트

**구조적 체크리스트:**
- [ ] 파일 크기가 500줄 이하인가?
- [ ] 클래스당 하나의 책임만 있는가?
- [ ] 메서드 크기가 50줄 이하인가?
- [ ] 패키지 구조가 일관성 있게 구성되었는가?
- [ ] 의존성이 단방향인가?
- [ ] 순환 의존성이 없는가?

**품질 체크리스트:**
- [ ] 요구사항 충족
- [ ] 테스트 충분성
- [ ] 에러 처리 적절성
- [ ] 보안 취약점 없음
- [ ] 성능 영향 고려
- [ ] 문서화 완료

### 5. CI/CD 게이트

```yaml
# .github/workflows/quality-gate.yml
name: Quality Gate

on: [pull_request]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run tests with coverage
        run: ./mvnw test jacoco:report

      - name: Check coverage threshold
        run: |
          COVERAGE=$(cat target/site/jacoco/index.html | grep -oP 'Total[^%]+%' | head -1)
          echo "Coverage: $COVERAGE"
          if [[ $(echo "$COVERAGE" | grep -oP '\d+') -lt 80 ]]; then
            echo "Coverage below 80%"
            exit 1
          fi

      - name: Run static analysis
        run: ./mvnw checkstyle:check

      - name: Run SonarQube scan
        run: sonar-scanner
```

---

## 템플릿

### 1. 기능 개발 계획 (PLAN.md)

```markdown
# Plan: [기능명]

## 개요
- 목적: 무엇을 해결하는가?
- 범위: 무엇을 포함/제외하는가?
- 예상 소요: 얼마나 걸리는가?

## 요구사항
### 기능적 요구사항
- [ ] 요구사항 1
- [ ] 요구사항 2

### 비기능적 요구사항
- 성능: 응답 시간 < 200ms
- 보안: 인증 필요
- 가용성: 99.9%

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
- E2E 테스트: 사용자 시나리오

## 리스크
- [ ] 리스크 1 및 대응책
- [ ] 리스크 2 및 대응책

## 검토 체크리스트
- [ ] 요구사항 명확성
- [ ] 테스트 계획 적절성
- [ ] 보안 고려사항
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

## 관련 문서
- 관련 문서 링크
```

---

## 부록: 빠른 참조 카드

### 에이전트 호출 순서

```
새로운 기능 요청
    ↓
requirements-analyst (요구사항 분석)
    ↓
architecture-analyst (아키텍처 설계)
    ↓
code-designer + testing-strategist (설계 + 테스트)
    ↓
code-designer (구현)
    ↓
quality-manager (검증)
    ↓
security-reviewer (선택적)
    ↓
커밋 (structural → test → feat/fix 순)
```

### TDD 사이클

**RED:**
- [ ] 테스트가 명확한 실패 메시지 제공
- [ ] 테스트가 하나의 행위만 검증
- [ ] 테스트 이름이 의도 명확히 표현

**GREEN:**
- [ ] 모든 테스트 통과
- [ ] "작동하는 가장 단순한" 구현 (YAGNI)
- [ ] 미래 예측 없음, 현재 테스트만 만족
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
- [ ] 커밋 메시지 컨벤션 준수
- [ ] Tidy First 적용 (structural 먼저)

---

## 변경 이력

| 버전 | 날짜 | 변경 내용 |
|------|------|-----------|
| 3.0 | 2026-02-23 | 모든 문서 병합, 단일 README로 통합 |
| 3.0 | 2026-02-23 | 범용 코드 생성 에이전트로 재설계 (Java/Spring, MSA 지원) |
| 2.0 | 2026-02-23 | 코드 생성 규칙 및 프로세스 룰 정립 |
| 1.0 | 2026-02-23 | 초안 작성 |

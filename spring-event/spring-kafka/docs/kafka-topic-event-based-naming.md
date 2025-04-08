# Kafka Topic Naming Convention Guide

토픽 네이밍엔 표준은 없지만, 조직 내 일관성이 가장 중요하다.

## Tell, Don't Ask

이벤트 이름은 프로듀서의 시점에서, 발생한 사실만을 표현해야 한다.

컨슈머는 해당 이벤트를 해석해서 필요한 행동을 하면 되지, 이벤트 이름이 그 행동을 유도하거나 강제하면 안 된다.
의존성을 줄이기 위해서라도 이벤트는 **과거 시제(past tense)**로, 발생한 사실에 집중하는 게 좋다.

예: "UserCreated"는 좋은 이름 → 컨슈머는 이걸 보고 권한 설정, 로깅, 알림 전송 등 다양한 걸 자유롭게 할 수 있음.

## 이벤트 기반 토픽 명명에 대한 심층 분석

이벤트 명명은 프로듀서의 관점에서 순수한 "사실"을 기록해야 합니다.

### 🔥 핵심 원칙: "발생한 사실"에 기반한 명명

(Your perspective is 100% correct. 이벤트 명명은 프로듀서의 관점에서 순수한 "사실"을 기록해야 합니다)

#### 📌 올바른 사례 vs 문제적 사례

| 올바른 명명 (과거형 사실) | 문제적 명명 (의존성 내포)         |
|-----------------|-------------------------|
| `user.created`  | `user.need_validation`  |
| `order.placed`  | `order.wait_payment`    |
| `file.uploaded` | `file.ready_processing` |

### 1. 프로듀서-컨슈머 관점 분리 필요성

- **프로듀서의 책임**: "무엇이 발생했는가"에 대한 사실만 기록
  ```java
  // Good: 순수 이벤트 기록
  producer.send("user.created", new UserCreatedEvent(userId));
  
  // Bad: 컨슈머 의도 내포
  producer.send("user.requires_activation", ...);
  ```
- **컨슈머의 책임**: 수신한 이벤트를 기반으로 **자율적**으로 처리 로직 결정
  ```mermaid
  graph LR
    A[user.created] --> B[로그 저장 컨슈머]
    A --> C[권한 생성 컨슈머]
    A --> D[웰컴 이메일 전송]
  ```

### 2. 시간적 일관성 보장

- **과거형 사용의 중요성**:
    - `created`/`deleted`/`updated` 등 변경 완료 상태를 명시
    - "하는 중"이 아닌 "했다"는 사실 강조
  ```python
  # 시간 흐름에 따른 자연스러운 시퀀스
  user.created → user.password_changed → user.deactivated
  ```

### 3. 확장성 보장 패턴

#### (1) 도메인 이벤트 스타일

```
..
예: account.created.v1
```

#### (2) CQRS 최적화형

```
..
예: billing.invoice.paid
```

### 4. 실제 운영 시 주의점

- **트랜잭션 경계**:
  ```java
  // 이벤트 발행 전 DB 커밋 완료 필수
  transactionManager.commit(); 
  producer.send("order.completed", event);
  ```
- **버저닝 전략**:
    - `order.created.v1` → 스키마 변경 시 `v2` 추가
    - 기존 컨슈머는 v1을 계속 소비 가능

### 🚨 반드시 피해야 할 안티패턴

```bash
# 시스템 상태를 가정한 이름 (X)
payment.awaiting_confirmation

# 처리 요청형 이름 (X)
data.request_cleansing

# 특정 컨슈머를 위한 이름 (X)
report.generate_monthly
```

### 정리

이벤트 이름은 **상태 변화 선언**에 집중해야 하며, 어떤 처리가 필요하다는 암시를 포함해선 안 됩니다. 토픽 이름이 `user.created`라면, 이는 "사용자가 생성되었다"는 사실만을 알리며, "어떤 후속 처리"가 필요한지는 모든 컨슈머가 자율적으로 결정해야 합니다. 이 방식이 마이크로서비스 환경에서 진정한 loose coupling을 달성하는 유일한 방법입니다.

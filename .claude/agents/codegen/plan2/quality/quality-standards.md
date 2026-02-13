# 코드 품질 표준 (Quality Standards)

## 개요

본 문서는 Rust 코드베이스의 품질을 보장하기 위한 표준을 정의합니다. 모든 코드는 이러한 표준을 준수해야 하며, 자동화된 검사와 수동 리뷰를 통해 검증됩니다.

---

## 1. 금지 패턴 (Prohibited Patterns)

다음 패턴들은 코드베이스에서 엄격히 금지됩니다.

### 1.1 panic! 매크로

**금지 이유:** 예측 불가능한 프로그램 종료, 복구 불가능한 에러

```rust
// ❌ 금지
panic!("something went wrong");

// ✅ 허용 - 명시적 에러 처리
return Err(Error::UnexpectedState {
    context: "module_name".to_string(),
    reason: "something went wrong".to_string(),
});
```

**예외 상황:**
- 테스트 코드 (`#[cfg(test)]` 블록 내)
- 빌드 스크립트 (build.rs)
- 프로토타입/스파이크 코드 (명시적으로 표시된 경우)

### 1.2 as 키워드 캐스팅

**금지 이유:** 런타임 오버플로우/언더플로우 위험, 데이터 손실 가능성

```rust
// ❌ 금지
let x: i32 = large_value as i32;

// ✅ 허용 - try_from 사용
let x: i32 = i32::try_from(large_value)
    .map_err(|e| Error::ConversionFailed {
        from: "i64",
        to: "i32",
        reason: e.to_string(),
    })?;

// ✅ 허용 - 명시적 경계 검사
let x: i32 = if large_value <= i32::MAX as i64 {
    large_value as i32
} else {
    return Err(Error::ValueOutOfRange {
        field: "large_value",
        max: i32::MAX as i64,
        actual: large_value,
    });
};
```

**예외 상황:**
- `u8`에서 `u32`로의 확장 (widening) - 런타임 오버플로우 불가능
- `&str`에서 `&[u8]`로의 변환
- 테스트 코드

### 1.3 unwrap() 메서드

**금지 이유:** 패닉 발생 가능성, 에러 처리 무시

```rust
// ❌ 금지
let value = result.unwrap();

// ✅ 허용 - 명시적 에러 전파
let value = result?;

// ✅ 허용 - 기본값 제공
let value = result.unwrap_or(default_value);

// ✅ 허용 - 커스텀 에러 매핑
let value = result.ok_or_else(|| Error::MissingValue {
    field: "config_key".to_string(),
})?;
```

**예외 상황:**
- 테스트 코드 (단, `expect()` 선호)
- 정적으로 보증된 안전한 경우 (문서화 필요)
- 프로토타입 코드 (TODO 주석 필수)

### 1.4 expect() 메서드

**금지 이유:** 패닉 메시지로만 디버깅, 복구 메커니즘 없음

```rust
// ❌ 금지
let value = result.expect("should never fail");

// ✅ 허용 - 구체적 에러 처리
let value = result.map_err(|e| Error::InitializationFailed {
    component: "database",
    source: e,
})?;
```

**예외 상황:**
- 테스트 코드
- 초기화 시 정적으로 보증된 값 (예: 상수 파싱)

### 1.5 unsafe 블록

**금지 이유:** 메모리 안전성 보장 불가, UB(undefined behavior) 위험

```rust
// ❌ 금지
unsafe { *raw_ptr }

// ✅ 허용 - 안전한 추상화로 감싸기
// unsafe 코드는 별도 모듈에 격리하고 안전한 API 제공
pub fn safe_operation() -> Result<T, Error> {
    // SAFETY: invariant 설명
    unsafe { internal_unsafe_op() }
}
```

**예외 상황:**
- FFI 바인딩
- 성능 최적화가 필요한 핫패스 (문서화 및 리뷰 필수)
- 표준 라이브러리/크레이트에서 제공하는 안전한 추상화

---

## 2. 필수 패턴 (Required Patterns)

### 2.1 명시적 에러 처리

모든 에러는 명시적으로 처리되어야 합니다.

```rust
// ✅ Result 타입 사용
pub fn process_data(input: &str) -> Result<Output, Error> {
    let parsed = parse_input(input)
        .map_err(|e| Error::ParseFailed {
            input: input.to_string(),
            source: e,
        })?;

    validate(&parsed)?;

    transform(parsed)
}

// ✅ thiserror를 사용한 에러 정의
#[derive(Debug, Error)]
pub enum Error {
    #[error("failed to parse input: {input}")]
    ParseFailed {
        input: String,
        #[source]
        source: ParseError,
    },

    #[error("validation failed: {reason}")]
    ValidationFailed { reason: String },
}
```

**요구사항:**
- `Result<T, E>` 반환 타입 사용
- 에러 타입은 `std::error::Error` 구현
- `thiserror` 또는 `snafu` 사용 권장
- 에러 컨텍스트 포함 (어디서, 왜 실패했는지)

### 2.2 Unsafe 사용 원칙

unsafe 코드는 다음 원칙을 따라야 합니다.

```rust
/// # Safety
/// - ptr은 유효한 메모리를 가리켜야 함
/// - ptr은 nullptr이 아니어야 함
/// - ptr이 가리키는 값은 초기화되어 있어야 함
pub unsafe fn dangerous_operation(ptr: *const T) -> T {
    // SAFETY: caller가 invariant를 보장함
    *ptr
}

// 안전한 래퍼 제공
pub fn safe_wrapper(value: &T) -> T {
    // SAFETY: 참조는 유효함이 보장됨
    unsafe { dangerous_operation(value) }
}
```

**요구사항:**
- `# Safety` 문서화 주석 필수
- `SAFETY:` 인라인 주석으로 invariant 설명
- unsafe 코드는 별도 모듈로 격리
- 코드 리뷰 시 추가 검증 필요
- `unsafe_op_in_unsafe_fn` lint 활성화

### 2.3 불변성 검증

데이터 불변성은 컴파일 타임에 보장되어야 합니다.

```rust
// ✅ 불변성을 타입 시스템으로 표현
pub struct ValidatedEmail(String);

impl ValidatedEmail {
    pub fn new(email: &str) -> Result<Self, Error> {
        if is_valid_email(email) {
            Ok(Self(email.to_string()))
        } else {
            Err(Error::InvalidEmail)
        }
    }

    pub fn as_str(&self) -> &str {
        &self.0
    }
}

// ✅ NonZeroU32 등 표준 타입 활용
use std::num::NonZeroU32;

pub fn divide(a: u32, b: NonZeroU32) -> u32 {
    a / b.get()  // 절대 패닉 없음
}
```

**요구사항:**
- 런타임 검증은 생성자에서 한 번만 수행
- 검증된 상태는 타입으로 표현
- getter만 제공하여 낮은 수준의 변경 방지

---

## 3. 코드 스타일

### 3.1 포매팅

- `rustfmt` 사용 (기본 설정)
- 최대 줄 길이: 100자
- 들여쓰기: 4 spaces

```toml
# rustfmt.toml
max_width = 100
tab_spaces = 4
edition = "2021"
```

### 3.2 네이밍 컨벤션

| 항목 | 규칙 | 예시 |
|------|------|------|
| 타입/트레이트 | PascalCase | `HttpRequest`, `AsyncTrait` |
| 함수/메서드 | snake_case | `send_request`, `process_data` |
| 변수 | snake_case | `response_body`, `item_count` |
| 상수 | SCREAMING_SNAKE_CASE | `MAX_RETRIES`, `DEFAULT_TIMEOUT` |
| 모듈 | snake_case | `http_client`, `error_handling` |
| 매크로 | snake_case! | `log_error!`, `measure_time!` |

### 3.3 문서화

모든 public API는 문서화되어야 합니다.

```rust
/// HTTP 요청을 비동기적으로 처리합니다.
///
/// # Arguments
///
/// * `request` - 처리할 HTTP 요청
/// * `timeout` - 최대 대기 시간
///
/// # Returns
///
/// 성공 시 `Ok(Response)`, 실패 시 `Err(Error)`를 반환합니다.
///
/// # Errors
///
/// 다음 상황에서 에러를 반환합니다:
/// - 네트워크 연결 실패
/// - 타임아웃 초과
/// - 유효하지 않은 응답
///
/// # Examples
///
/// ```
/// let request = HttpRequest::new("https://api.example.com");
/// let response = process_request(request, Duration::from_secs(30)).await?;
/// ```
pub async fn process_request(
    request: HttpRequest,
    timeout: Duration,
) -> Result<Response, Error> {
    // ...
}
```

---

## 4. 자동화된 검사

### 4.1 Clippy 설정

```toml
# .clippy.toml
avoid-breaking-exported-api = false
doc-valid-idents = ["OAuth", "GraphQL", "WebSocket"]
```

```rust
// lib.rs 또는 main.rs 상단
#![warn(clippy::all)]
#![warn(clippy::pedantic)]
#![warn(clippy::nursery)]
#![warn(clippy::unwrap_used)]
#![warn(clippy::expect_used)]
#![warn(clippy::panic)]
#![warn(clippy::as_conversions)]
#![warn(clippy::missing_docs_in_private_items)]
#![allow(clippy::module_name_repetitions)]
```

### 4.2 CI 검사

```yaml
# .github/workflows/quality.yml
name: Quality Checks

on: [push, pull_request]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Check formatting
        run: cargo fmt -- --check

      - name: Run clippy
        run: cargo clippy --all-targets --all-features -- -D warnings

      - name: Check for forbidden patterns
        run: |
          # panic!, unwrap, expect 검색
          ! grep -r "panic!" --include="*.rs" src/ || true
          ! grep -r "\.unwrap()" --include="*.rs" src/ || true
          ! grep -r "\.expect(" --include="*.rs" src/ || true

      - name: Run tests
        run: cargo test --all-features

      - name: Generate coverage
        run: cargo tarpaulin --out Xml

      - name: Check coverage threshold
        run: |
          # 커버리지 87% 이상 확인
          python scripts/check_coverage.py --min-lines 87 --min-functions 88.7
```

---

## 5. 위반 처리

| 위반 수준 | 처리 방법 |
|-----------|-----------|
| Critical (panic!, unsafe 남용) | CI 실패, PR 병합 불가 |
| Major (unwrap, expect) | CI 경고, 수동 리뷰 필수 |
| Minor (스타일 위반) | 자동 포매팅, clippy --fix |

---

## 6. 관련 문서

- [커밋 컨벤션](./commit-convention.md)
- [커밋 전 체크리스트](./pre-commit-checklist.md)
- [품질 메트릭](./quality-metrics.md)

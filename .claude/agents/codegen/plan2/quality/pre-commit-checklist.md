# 커밋 전 체크리스트 (Pre-Commit Checklist)

## 개요

본 체크리스트는 모든 커밋 전에 반드시 확인해야 할 항목들을 정의합니다. 이 체크리스트를 준수하는 것은 코드 품질 보장과 리뷰 프로세스 효율화에 필수적입니다.

---

## 1. 자동화된 검사 (Automated Checks)

### 1.1 코드 포맷팅

```bash
# 실행 명령
cargo fmt

# 검사 명령
cargo fmt -- --check
```

- [ ] `cargo fmt`가 변경사항을 만들지 않는가?
- [ ] 들여쓰기가 4 spaces로 통일되어 있는가?
- [ ] 줄 길이가 100자를 초과하지 않는가?

### 1.2 Clippy 검사

```bash
# 실행 명령
cargo clippy --all-targets --all-features -- -D warnings
```

- [ ] 모든 clippy warning이 해결되었는가?
- [ ] `unwrap_used` lint 위반이 없는가?
- [ ] `expect_used` lint 위반이 없는가?
- [ ] `panic` lint 위반이 없는가?
- [ ] `as_conversions` lint 위반이 없는가?

### 1.3 컴파일 검사

```bash
# 실행 명령
cargo build --all-targets --all-features
cargo check --all-targets --all-features
```

- [ ] 코드가 컴파일되는가?
- [ ] 모든 warning이 해결되었는가?
- [ ] `--all-features` 플래그로도 컴파일되는가?

### 1.4 테스트 실행

```bash
# 실행 명령
cargo test --all-features
```

- [ ] 모든 단위 테스트가 통과하는가?
- [ ] 모든 통합 테스트가 통과하는가?
- [ ] 새로운 기능에 대한 테스트가 추가되었는가?

### 1.5 금지 패턴 검사

```bash
# 검사 스크립트
./scripts/check-forbidden-patterns.sh
```

- [ ] `panic!` 매크로가 production 코드에 없는가?
- [ ] `.unwrap()` 호출이 없는가?
- [ ] `.expect()` 호출이 없는가?
- [ ] 불필요한 `as` 캐스팅이 없는가?
- [ ] `unsafe` 블록이 적절히 문서화되었는가?

---

## 2. 코드 품질 검사 (Code Quality Checks)

### 2.1 에러 처리

- [ ] 모든 함수가 적절한 `Result` 타입을 반환하는가?
- [ ] 에러 타입이 `thiserror` 또는 `snafu`로 정의되었는가?
- [ ] 에러 메시지가 충분한 컨텍스트를 제공하는가?
- [ ] `?` 연산자를 적절히 사용하는가?

### 2.2 문서화

- [ ] 모든 public API에 doc comment가 있는가?
- [ ] 복잡한 로직에 inline comment가 있는가?
- [ ] `unsafe` 코드에 `# Safety` 주석이 있는가?
- [ ] 예시 코드가 컴파일되는가 (`cargo test --doc`)?

### 2.3 테스트 커버리지

```bash
# 실행 명령
cargo tarpaulin --out Xml
```

- [ ] 라인 커버리지 >= 87%인가?
- [ ] 함수 커버리지 >= 88.7%인가?
- [ ] 새로운 코드가 테스트로 커버되었는가?
- [ ] edge case가 테스트되었는가?

### 2.4 성능 고려사항

- [ ] 불필요한 할당이 없는가?
- [ ] 큰 데이터 구조에 `&str` 대신 `String`을 사용하지 않는가?
- [ ] Iterator 체이닝이 효율적인가?

---

## 3. 커밋 메시지 검사 (Commit Message Checks)

### 3.1 타입 검사

- [ ] 커밋 타입이 유효한가? (`structural`, `behavioral`, `docs`, `test`, `perf`)
- [ ] 타입이 변경 내용을 정확히 반영하는가?

### 3.2 서브젝트 검사

- [ ] 50자 이내인가?
- [ ] 명령문으로 작성되었는가?
- [ ] 첫 글자가 소문자인가?
- [ ] 마침표가 없는가?

### 3.3 바디 검사

- [ ] 변경 이유(why)가 설명되었는가?
- [ ] 72자 이내로 줄바꿈되었는가?
- [ ] Breaking changes가 문서화되었는가?

### 3.4 크기 검사

- [ ] 변경 라인 수가 200줄 이하인가?
- [ ] 하나의 논리적 단위로 구성되었는가?

---

## 4. 보안 검사 (Security Checks)

### 4.1 의존성 검사

```bash
# 실행 명령
cargo audit
```

- [ ] 알려진 취약점이 없는가?
- [ ] 불필요한 의존성이 추가되지 않았는가?

### 4.2 비밀 정보 검사

```bash
# 검사 스크립트
./scripts/check-secrets.sh
```

- [ ] API 키, 비밀번호가 코드에 없는가?
- [ ] `.env` 파일이 staging되지 않았는가?
- [ ] 주석에 민감한 정보가 없는가?

### 4.3 unsafe 코드 검사

- [ ] `unsafe` 블록이 최소화되었는가?
- [ ] `SAFETY:` 주석이 포함되었는가?
- [ ] `# Safety` 문서가 작성되었는가?

---

## 5. 통합 검사 (Integration Checks)

### 5.1 API 호환성

- [ ] public API 변경이 문서화되었는가?
- [ ] Breaking change가 `BREAKING CHANGE:`로 표시되었는가?
- [ ] Deprecation이 적절히 표시되었는가?

### 5.2 설정 파일

- [ ] `Cargo.toml` 변경이 유효한가?
- [ ] `Cargo.lock`이 업데이트되었는가 (바이너리의 경우)?
- [ ] 새로운 feature flag가 문서화되었는가?

### 5.3 CI/CD

- [ ] CI 설정 파일이 유효한가?
- [ ] 새로운 job이 추가되었다면 테스트되었는가?

---

## 6. 최종 확인 (Final Verification)

### 6.1 변경 내용 검토

```bash
# 실행 명령
git diff --cached
```

- [ ] staging된 변경사항이意図한대로인가?
- [ ] 디버그 코드(`println!`, `dbg!`)가 제거되었는가?
- [ ] TODO/FIXME 주석이 티켓 번호와 함께 작성되었는가?

### 6.2 커밋 준비

- [ ] `git status`로 확인했을 때 예상대로인가?
- [ ] 커밋 메시지가 준비되었는가?

---

## 7. 체크리스트 실행 스크립트

```bash
#!/bin/bash
# scripts/pre-commit-check.sh

set -e

echo "=== Pre-Commit Checklist ==="
echo ""

# 1. 포맷팅
echo "1. Checking formatting..."
cargo fmt -- --check
echo "   ✓ Formatting OK"
echo ""

# 2. Clippy
echo "2. Running clippy..."
cargo clippy --all-targets --all-features -- -D warnings
echo "   ✓ Clippy OK"
echo ""

# 3. 테스트
echo "3. Running tests..."
cargo test --all-features
echo "   ✓ Tests OK"
echo ""

# 4. 금지 패턴
echo "4. Checking forbidden patterns..."
if grep -r "panic!" --include="*.rs" src/ 2>/dev/null; then
    echo "   ✗ Found panic! in production code"
    exit 1
fi
if grep -r "\.unwrap()" --include="*.rs" src/ 2>/dev/null; then
    echo "   ✗ Found unwrap() in production code"
    exit 1
fi
if grep -r "\.expect(" --include="*.rs" src/ 2>/dev/null; then
    echo "   ✗ Found expect() in production code"
    exit 1
fi
echo "   ✓ No forbidden patterns found"
echo ""

# 5. 커버리지 (선택)
echo "5. Checking coverage..."
if command -v cargo-tarpaulin &> /dev/null; then
    cargo tarpaulin --fail-under 87 --out Stdout
echo "   ✓ Coverage OK"
else
    echo "   ⚠ cargo-tarpaulin not installed, skipping coverage check"
fi
echo ""

echo "=== All checks passed! ==="
```

---

## 8. Git Hook 설정

### 8.1 pre-commit hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 체크리스트 실행
echo "Running pre-commit checks..."

# 포맷팅 검사
if ! cargo fmt -- --check 2>/dev/null; then
    echo -e "${RED}✗ Formatting check failed${NC}"
    echo "Run: cargo fmt"
    exit 1
fi

# Clippy 검사
if ! cargo clippy --all-targets --all-features -- -D warnings 2>/dev/null; then
    echo -e "${RED}✗ Clippy check failed${NC}"
    exit 1
fi

# 테스트 실행
if ! cargo test --all-features 2>/dev/null; then
    echo -e "${RED}✗ Tests failed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All pre-commit checks passed${NC}"
exit 0
```

### 8.2 commit-msg hook

```bash
#!/bin/bash
# .git/hooks/commit-msg

COMMIT_MSG_FILE=$1
COMMIT_MSG=$(head -n1 "$COMMIT_MSG_FILE")

# 타입 검사
VALID_TYPES="structural behavioral docs test perf"
TYPE=$(echo "$COMMIT_MSG" | cut -d':' -f1)

if ! echo "$VALID_TYPES" | grep -qw "$TYPE"; then
    echo "Error: Invalid commit type '$TYPE'"
    echo "Valid types: $VALID_TYPES"
    exit 1
fi

# Subject 길이 검사
SUBJECT=$(echo "$COMMIT_MSG" | cut -d':' -f2- | sed 's/^ *//')
if [ ${#SUBJECT} -gt 50 ]; then
    echo "Error: Subject too long (${#SUBJECT} chars, max 50)"
    exit 1
fi

echo "✓ Commit message format OK"
exit 0
```

---

## 9. 관련 문서

- [코드 품질 표준](./quality-standards.md)
- [커밋 컨벤션](./commit-convention.md)
- [품질 메트릭](./quality-metrics.md)

# 품질 메트릭 (Quality Metrics)

## 개요

본 문서는 코드 자동화 에이전트 팀의 품질 목표와 측정 방법을 정의합니다. 이러한 메트릭은 코드 품질을 객관적으로 평가하고 지속적인 개선을 위한 기준을 제공합니다.

---

## 1. 커버리지 목표 (Coverage Goals)

### 1.1 라인 커버리지 (Line Coverage)

| 목표 | 최소값 | 목표값 | 이상값 |
|------|--------|--------|--------|
| **라인 커버리지** | 87% | 90% | 95%+ |

**정의:** 실행된 코드 라인 수 / 전체 코드 라인 수

**측정 도구:**
- `cargo-tarpaulin`
- `cargo-llvm-cov`
- Codecov / Coveralls

### 1.2 함수 커버리지 (Function Coverage)

| 목표 | 최소값 | 목표값 | 이상값 |
|------|--------|--------|--------|
| **함수 커버리지** | 88.7% | 92% | 95%+ |

**정의:** 실행된 함수 수 / 전체 함수 수

**측정 도구:**
- `cargo-tarpaulin --out Xml`
- `grcov`

### 1.3 브랜치 커버리지 (Branch Coverage)

| 목표 | 최소값 | 목표값 | 이상값 |
|------|--------|--------|--------|
| **브랜치 커버리지** | 80% | 85% | 90%+ |

**정의:** 실행된 분기(if/else, match arms 등) / 전체 분기

---

## 2. 메트릭 측정 방법

### 2.1 cargo-tarpaulin 사용

```bash
# 설치
cargo install cargo-tarpaulin

# 기본 실행
cargo tarpaulin

# 상세 출력
cargo tarpaulin --out Stdout

# XML 리포트 생성 (CI용)
cargo tarpaulin --out Xml

# HTML 리포트 생성
cargo tarpaulin --out Html

# 특정 커버리지 미만에서 실패
cargo tarpaulin --fail-under 87

# 함수 커버리지 포함
cargo tarpaulin --out Xml --output-dir coverage/
```

### 2.2 cargo-llvm-cov 사용

```bash
# 설치
cargo install cargo-llvm-cov

# 실행
cargo llvm-cov

# 상세 리포트
cargo llvm-cov --html

# 커버리지 미만에서 실패
cargo llvm-cov --fail-under-lines 87
```

### 2.3 CI 통합

```yaml
# .github/workflows/coverage.yml
name: Coverage

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Install Rust
        uses: dtolnay/rust-action@stable

      - name: Install tarpaulin
        run: cargo install cargo-tarpaulin

      - name: Generate coverage
        run: cargo tarpaulin --out Xml --all-features

      - name: Check line coverage
        run: |
          COVERAGE=$(cargo tarpaulin --out Stdout 2>&1 | grep "Coverage:" | awk '{print $2}' | tr -d '%')
          if (( $(echo "$COVERAGE < 87" | bc -l) )); then
            echo "Line coverage $COVERAGE% is below minimum 87%"
            exit 1
          fi
          echo "Line coverage: $COVERAGE% ✓"

      - name: Upload to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./cobertura.xml
          fail_ci_if_error: true
```

---

## 3. 메트릭 해석

### 3.1 커버리지 보고서 읽기

```
|| Tested/Total Lines:
|| src/lib.rs: 45/50
|| src/parser.rs: 120/130
|| src/validator.rs: 80/80
||
|| 87.50% coverage, 245/280 lines covered
```

**분석:**
- `src/validator.rs`: 100% 커버리지 - 모범 사례
- `src/parser.rs`: 92.3% 커버리지 - 양호
- `src/lib.rs`: 90% 커버리지 - 개선 필요
- **전체**: 87.5% - 최소 기준 충족

### 3.2 커버리지가 낮은 파일 식별

```bash
# 커버리지가 낮은 파일 찾기
cargo tarpaulin --out Stdout 2>&1 | grep -E "^\|\| src.*: [0-9]+/[0-9]+" | \
  awk -F': ' '{file=$1; cov=$2; split(cov, arr, "/"); pct=arr[1]/arr[2]*100; print pct, file}' | \
  sort -n | head -10
```

### 3.3 커버리지 개선 우선순위

| 우선순위 | 기준 | 조치 |
|----------|------|------|
| **P0 (Critical)** | < 50% | 즉시 테스트 추가 |
| **P1 (High)** | 50-70% | 다음 스프린트에 테스트 추가 |
| **P2 (Medium)** | 70-87% | 기술 부채로 관리 |
| **P3 (Low)** | 87-95% | 지속적 개선 |

---

## 4. 테스트 품질 메트릭

### 4.1 테스트 수

| 메트릭 | 목표 | 설명 |
|--------|------|------|
| 단위 테스트 수 | 1+ per public fn | 핵심 로직 커버리지 |
| 통합 테스트 수 | 1+ per module | 모듈 간 상호작용 |
| 문서 테스트 수 | 1+ per public API | 예시 코드 검증 |

### 4.2 테스트 실행 시간

| 메트릭 | 목표 | 최대 |
|--------|------|------|
| 전체 테스트 | < 30초 | 60초 |
| 단위 테스트 | < 10초 | 20초 |
| 통합 테스트 | < 60초 | 120초 |

### 4.3 테스트 신뢰성

| 메트릭 | 목표 | 설명 |
|--------|------|------|
| 플레이크 비율 | < 1% | 비결정적 테스트 비율 |
| 실패 재현율 | 100% | 동일 조건에서 동일 결과 |

---

## 5. 코드 품질 메트릭

### 5.1 정적 분석

| 메트릭 | 도구 | 목표 |
|--------|------|------|
| Clippy warnings | cargo clippy | 0 |
| Compiler warnings | cargo build | 0 |
| Unsafe blocks | grep | 최소화 |
| TODO/FIXME 수 | grep | 문서화된 것만 허용 |

### 5.2 코드 복잡도

| 메트릭 | 도구 | 목표 | 최대 |
|--------|------|------|------|
| Cyclomatic complexity | cargo clippy | < 10 | 15 |
| 함수 길이 | manual | < 50 lines | 100 |
| 모듈 크기 | manual | < 500 lines | 1000 |

### 5.3 문서화

| 메트릭 | 목표 | 측정 방법 |
|--------|------|-----------|
| Public API 문서화 | 100% | `cargo doc` + grep |
| Module 문서화 | 100% | `#!` doc comments |
| Example 코드 | 1+ per API | doc tests |

---

## 6. 프로세스 메트릭

### 6.1 코드 리뷰

| 메트릭 | 목표 | 설명 |
|--------|------|------|
| 리뷰 완료 시간 | < 24시간 | PR 생성 후 첫 리뷰 |
| 리뷰 라운드 | < 3 | 승인까지 평균 |
| 리뷰 댓글 수 | < 10 | PR당 평균 |

### 6.2 커밋 품질

| 메트릭 | 목표 | 설명 |
|--------|------|------|
| 커밋 크기 | < 200 lines | 변경 라인 수 |
| 커밋 메시지 품질 | 100% | 컨벤션 준수 |
| 커밋 빈도 | 1+ per day | 활성 개발자당 |

---

## 7. 메트릭 대시보드

### 7.1 README 배지

```markdown
[![Coverage](https://img.shields.io/badge/coverage-87%25-brightgreen)](./coverage/)
[![Tests](https://img.shields.io/badge/tests-passing-brightgreen)]()
[![Clippy](https://img.shields.io/badge/clippy-clean-brightgreen)]()
```

### 7.2 주간 리포트 템플릿

```markdown
# 주간 품질 리포트 (YYYY-MM-DD)

## 커버리지
- 라인 커버리지: 87.5% (목표: 87% ✓)
- 함수 커버리지: 89.2% (목표: 88.7% ✓)
- 브랜치 커버리지: 82.1% (목표: 80% ✓)

## 테스트
- 총 테스트 수: 156
- 통과: 156 (100%)
- 실패: 0
- 스킵: 0

## 정적 분석
- Clippy warnings: 0
- Compiler warnings: 0
- TODO/FIXME: 12 (모두 문서화됨)

## 개선 영역
1. src/parser.rs 커버리지 85% → 87% 목표
2. 통합 테스트 실행 시간 45초 → 30초 목표
```

---

## 8. 메트릭 달성 전략

### 8.1 커버리지 향상 전략

1. **TDD 적용**: 기능 구현 전 테스트 작성
2. **Mutation testing**: `cargo-mutants`로 테스트 품질 검증
3. **커버리지 게이트**: CI에서 87% 미만 PR 차단
4. **페어 프로그래밍**: 복잡한 로직 함께 테스트 작성

### 8.2 지속적 모니터링

```bash
# 일일 커버리지 체크
#!/bin/bash
# scripts/daily-coverage.sh

echo "=== Daily Coverage Check ==="

# 커버리지 실행
cargo tarpaulin --out Json --output-dir /tmp/

# 결과 파싱
COVERAGE=$(cat /tmp/cobertura.json | jq '.coverage')

# 목표와 비교
if (( $(echo "$COVERAGE < 87" | bc -l) )); then
    echo "⚠️ Coverage dropped to ${COVERAGE}%"
    echo "Required: 87%"
    exit 1
fi

echo "✅ Coverage: ${COVERAGE}%"
```

---

## 9. 관련 문서

- [코드 품질 표준](./quality-standards.md)
- [커밋 컨벤션](./commit-convention.md)
- [커밋 전 체크리스트](./pre-commit-checklist.md)

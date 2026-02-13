# 통합 설계도

**code-automation-agent-team 통합 산출물**
**작성일:** 2026-02-13

---

## 개요

상호작용 토론 방식의 코드 자동화 에이전트 팀 설계의 통합 결과물입니다.
architecture-analyst, code-designer, testing-strategist, quality-manager의
분석 결과를 취합하여 작성되었습니다.

---

## 1. 에이전트 팀 구성

| 역할 | 책임 | 주요 산출물 | 상태 |
|------|------|-------------|------|
| **architecture-analyst** | 아키텍처 원칙 설계 | 5가지 핵심 원칙, ADR 문서 | ✅ 완료 |
| **code-designer** | 코드 구조 설계 | 디렉토리 구조, 파일별 책임 | ✅ 완료 |
| **testing-strategist** | 테스트 체계 설계 | TDD 워크플로우, 테스트 구조 | ✅ 완료 |
| **quality-manager** | 품질 관리 설계 | 품질 표준, 커밋 컨벤션 | ✅ 완료 |
| **team-lead** | 통합 및 조율 | 통합 설계도, 실행 계획 | ✅ 완료 |

---

## 2. 핵심 아키텍처 원칙 (5개)

### 원칙 1: Arena-Based Resource Management
- **설명**: 메모리, 파일 핸들, 네트워크 연결 등 모든 리소스를 Arena 패턴으로 관리
- **근거**: BPlusTree3의 `CompactArena<T>`가 캐시 지역성, 할당자 부하 감소, 예측 가능한 성능 제공
- **적용**: `src/core/arena.rs` - 템플릿 캐싱, 파일 핸들 관리
- **ADR**: [ADR-001-arena-management.md](docs/adr/ADR-001-arena-management.md)

### 원칙 2: Linked Processing Pipeline
- **설명**: 처리 단계를 연결 리스트로 구성하여 순차적 처리 최적화
- **근거**: BPlusTree3의 `next: NodeId` 필드가 범위 쿼리를 O(log n + k)로 최적화
- **적용**: `src/core/pipeline.rs` - 코드 생성 파이프라인의 단계별 연결
- **ADR**: [ADR-002-linked-pipeline.md](docs/adr/ADR-002-linked-pipeline.md)

### 원칙 3: Module Organization by Change Frequency
- **설명**: 기능별이 아닌 "변경 빈도" 기준으로 모듈 분리
- **근거**: BPlusTree3는 3,138줄의 `lib.rs`를 변경 빈도 기준으로 13개 모듈로 분리
- **적용**: types.rs(안정적) → generator/*.rs(자주 변경) 계층화
- **ADR**: [ADR-003-module-organization.md](docs/adr/ADR-003-module-organization.md)

### 원칙 4: Dual-Language Implementation Strategy
- **설명**: 핵심 엔진은 Rust로 구현하고, 스크립팅/확장은 Python/JavaScript로 제공
- **근거**: BPlusTree3는 Rust(성능)와 Python(생태계 통합) 모두를 지원
- **적용**: `src/bindings/` - PyO3 바인딩, WASM 지원 (향후 구현)

### 원칙 5: Documentation-Driven Development (DDD)
- **설명**: 코드 변경 전 문서를 작성하고, 설계를 검토한 후 구현
- **근거**: BPlusTree3는 PLAN.md, ADR.md, PERFORMANCE_HISTORY.md를 체계적으로 관리
- **적용**: `docs/adr/`, `docs/plans/` 디렉토리 구조

---

## 3. 통합 디렉토리 구조

```
code-automation/
├── src/
│   ├── lib.rs                    # 공개 API 및 모듈 선언 (~200줄)
│   ├── types.rs                  # 핵심 타입 정의 (안정적, ~100줄)
│   ├── error.rs                  # 에러 타입 및 Result 별칭 (~200줄)
│   │
│   ├── core/                     # 핵심 엔진 (변경 빈도: 중간)
│   │   ├── mod.rs
│   │   ├── arena.rs              # Arena-based resource management
│   │   ├── engine.rs             # 코드 생성 엔진
│   │   ├── pipeline.rs           # Linked processing pipeline
│   │   └── context.rs            # 생성 컨텍스트
│   │
│   ├── parser/                   # 입력 파싱 (변경 빈도: 중간)
│   │   ├── mod.rs
│   │   ├── input_parser.rs       # 입력 데이터 파싱
│   │   ├── schema.rs             # 스키마 정의
│   │   └── validator.rs          # 입력 검증
│   │
│   ├── generator/                # 코드 생성 (변경 빈도: 높음)
│   │   ├── mod.rs
│   │   ├── code_generator.rs     # 메인 생성기
│   │   ├── language/             # 언어별 생성기
│   │   │   ├── mod.rs
│   │   │   ├── rust.rs
│   │   │   ├── python.rs
│   │   │   └── java.rs
│   │   └── formatter.rs          # 코드 포맷팅
│   │
│   ├── transformer/              # 코드 변환 (변경 빈도: 중간)
│   │   ├── mod.rs
│   │   ├── ast_transformer.rs    # AST 기반 변환
│   │   ├── refactoring.rs        # 리팩토링 규칙
│   │   └── modifier.rs           # 코드 수정
│   │
│   ├── output/                   # 출력 관리 (변경 빈도: 낮음)
│   │   ├── mod.rs
│   │   ├── writer.rs             # 파일 쓰기
│   │   ├── diff.rs               # diff 생성
│   │   └── backup.rs             # 백업 관리
│   │
│   ├── utils/                    # 유틸리티 (변경 빈도: 낮음)
│   │   ├── mod.rs
│   │   ├── string_utils.rs
│   │   ├── path_utils.rs
│   │   └── logger.rs
│   │
│   └── validation/               # 검증 (변경 빈도: 중간)
│       ├── mod.rs
│       ├── syntax_validator.rs   # 구문 검증
│       ├── semantic_validator.rs # 의미 검증
│       └── invariant_checker.rs  # 불변성 검사
│
├── tests/                        # 테스트
│   ├── unit/                     # 단위 테스트 (60%)
│   ├── integration/              # 통합 테스트 (30%)
│   ├── adversarial/              # 적대적 테스트 (10%)
│   └── fixtures/                 # 테스트 픽스처
│
├── benches/                      # 벤치마크
│   ├── criterion/                # Criterion 벤치마크
│   └── iai/                      # IAI 정적 벤치마크
│
├── docs/                         # 문서
│   ├── adr/                      # 아키텍처 결정 기록
│   │   ├── ADR-001-arena-management.md
│   │   ├── ADR-002-linked-pipeline.md
│   │   ├── ADR-003-module-organization.md
│   │   └── ...
│   ├── plans/                    # 구현 계획
│   ├── analysis/                 # 분석 결과
│   └── guides/                   # 개발 가이드
│
├── scripts/                      # 개발 스크립트
│   ├── build.sh
│   ├── test.sh
│   ├── coverage.sh
│   └── pre-commit-check.sh
│
└── examples/                     # 사용 예시
```

---

## 4. 의존성 그래프

```
                    lib.rs (Public API)
                         │
    ┌────────────────────┼────────────────────┐
    │                    │                    │
    ▼                    ▼                    ▼
 types.rs            error.rs             config.rs
 (no deps)           (no deps)            (no deps)
    │                    │                    │
    └────────────────────┼────────────────────┘
                         │
                         ▼
                   core/arena.rs
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ▼               ▼               ▼
    parser/          core/pipeline    transformer/
    (types,          (types,          (types,
     error)           arena)           parser)
         │               │               │
         └───────────────┼───────────────┘
                         │
                         ▼
                   generator/
                   (types,
                    transformer)
                         │
                         ▼
                   validation/
                   (all above)
```

**의존성 규칙:**
1. 하위 레벨 모듈(types, error, config)은 다른 모듈에 의존하지 않음
2. 중간 레벨 모듈(arena, parser)은 하위 레벨에만 의존
3. 상위 레벨 모듈(generator, validation)은 모든 하위 모듈에 의존 가능
4. 순환 의존성 금지

---

## 5. TDD (Test-Driven Development) 사이클

```
┌────────────────────────────────────────────────────────────────────┐
│                         TDD 사이클                                 │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│   ┌─────────┐     ┌─────────┐     ┌─────────┐     ┌─────────┐    │
│   │  WRITE  │     │   RUN   │     │  WRITE  │     │  REFAC- │    │
│   │  TEST   │────→│  TEST   │────→│  CODE   │────→│   TOR   │    │
│   │ (RED)   │     │ (FAIL)  │     │ (GREEN) │     │         │    │
│   └─────────┘     └─────────┘     └─────────┘     └────┬────┘    │
│        ↑                                               │         │
│        └───────────────────────────────────────────────┘         │
│                                                                    │
│   시간 단위: 5-15분 per cycle                                      │
│   커밋 타이밍: Green phase 완료 후                                 │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

**가이드라인:**
| 단계 | 활동 | 시간 제한 |
|------|------|-----------|
| RED | 실패하는 테스트 작성 | 2-5분 |
| GREEN | 최소한의 구현 | 3-8분 |
| REFACTOR | 코드 개선 | 2-5분 |

---

## 6. Tidy First 워크플로우

```
┌─────────────────────────────────────────────────────────────────────┐
│                     Tidy First Workflow                             │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│   ┌──────────────────────────────────────────────────────────┐     │
│   │                  BEFORE ANY FEATURE                       │     │
│   │  1. Identify structural issues (naming, organization)    │     │
│   │  2. Make structural changes                              │     │
│   │  3. Verify all tests pass                                │     │
│   │  4. Commit: "structural: description"                    │     │
│   └────────────────────────────────┬───────────────────────────────┘     │
│                               ↓                                    │
│   ┌──────────────────────────────────────────────────────────┐     │
│   │                  IMPLEMENT FEATURE                        │     │
│   │  1. Write failing tests                                  │     │
│   │  2. Implement feature (behavioral change)                │     │
│   │  3. Verify tests pass                                    │     │
│   │  4. Commit: "behavioral: description"                    │     │
│   └──────────────────────────────────────────────────────────┘     │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 7. 코드 품질 표준

### 금지 패턴
| 패턴 | 금지 사유 | 대안 |
|------|----------|------|
| `panic!("...")` | 프로덕션 크래시 | `Result<T, E>` |
| `value as u32` | 오버플로우 위험 | `try_into()` |
| `.unwrap()` | 에러 무시 | `match` 또는 `if let` |
| `.expect()` in library | 라이브러리 패닉 | `Result` 기반 처리 |

### 필수 패턴
```rust
// 명시적 에러 처리
fn operation() -> Result<T, MyError> {
    match risky_operation() {
        Ok(value) => Ok(process(value)),
        Err(e) => Err(MyError::from(e)),
    }
}

// Unsafe 코드 사용 원칙
/// # Safety
/// Caller must ensure `index < self.keys_len()`
#[inline]
pub unsafe fn get_unchecked(&self, index: usize) -> &T {
    self.items.get_unchecked(index)
}

// 불변성 검증
pub fn try_operation(&mut self) -> Result<T, Error> {
    // Validate BEFORE
    self.check_invariants()?;

    let result = self.operation();

    // Validate AFTER
    self.check_invariants()?;
    Ok(result)
}
```

---

## 8. 커밋 컨벤션

| 타입 | 접두사 | 설명 | 예시 |
|------|--------|------|------|
| 구조적 변경 | `structural:` | 코드 재조직, 리팩토링 | `structural: extract error module` |
| 행위적 변경 | `behavioral:` | 기능 추가, 버그 수정 | `behavioral: add batch insert` |
| 문서화 | `docs:` | 문서 추가/수정 | `docs: update API reference` |
| 테스트 | `test:` | 테스트 추가/수정 | `test: add adversarial tests` |
| 성능 | `perf:` | 성능 최적화 | `perf: cache leaf references` |

**커밋 크기 가이드라인:**
- 목표: 50-100줄 변경
- 최대: 200줄
- 구조적/행위적 변경 혼재 시 반드시 분리

---

## 9. 품질 메트릭 목표

| 메트릭 | 목표값 | 측정 도구 |
|--------|--------|----------|
| 테스트 커버리지 | >= 87% | cargo-tarpaulin |
| 함수 커버리지 | >= 88.7% | cargo-tarpaulin |
| 문서화 비율 | 100% public API | cargo doc |
| Clippy 경고 | 0 | cargo clippy |
| 포맷팅 | 100% | cargo fmt --check |

---

## 10. 테스트 전략

### 테스트 구조
```
tests/
├── unit/                     # 단위 테스트 (60%)
│   ├── types_tests.rs
│   ├── arena_tests.rs
│   └── ...
├── integration/              # 통합 테스트 (30%)
│   ├── basic_operations.rs
│   ├── edge_cases.rs
│   └── performance.rs
├── adversarial/              # 적대적 테스트 (10%)
│   ├── structural_attacks.rs
│   ├── memory_stress.rs
│   └── invariant_violations.rs
└── fixtures/                 # 테스트 픽스처
    ├── sample_inputs/
    └── expected_outputs/
```

### 적대적 테스트 시나리오 예시
```rust
// tests/adversarial/structural_attacks.rs
#[test]
fn test_aggressive_left_bias_operations() {
    let mut engine = CodeEngine::new(Config::default());

    // 치우친 입력으로 스트레스 유도
    for i in 0..1000 {
        engine.process(Input::new(i, i));
    }

    // 역순 처리로 재조정 반복
    for i in (0..1000).rev() {
        engine.rollback(i);
        engine.check_invariants().unwrap();
    }
}
```

---

## 11. 실행 계획

### 11.1 구현 우선순위

| 단계 | 작업 | 의존성 | 예상 소요 |
|------|------|--------|----------|
| 1 | types.rs, error.rs 구현 | 없음 | 1-2일 |
| 2 | core/arena.rs 구현 | types.rs | 1-2일 |
| 3 | parser/ 모듈 구현 | types.rs, error.rs | 2-3일 |
| 4 | core/engine.rs 구현 | arena.rs, parser/ | 3-5일 |
| 5 | generator/ 모듈 구현 | engine.rs | 3-5일 |
| 6 | transformer/ 모듈 구현 | generator/ | 2-3일 |
| 7 | validation/ 모듈 구현 | 모든 모듈 | 2-3일 |
| 8 | 테스트 스위트 구축 | 모든 모듈 | 2-3일 |

### 11.2 첫 번째 마일스톤 (MVP)

**목표**: 기본 코드 생성 기능 구현

**범위:**
- [ ] types.rs - 핵심 타입 정의
- [ ] error.rs - 기본 에러 타입
- [ ] core/arena.rs - Arena 메모리 관리
- [ ] parser/input_parser.rs - 기본 입력 파싱
- [ ] generator/code_generator.rs - 단순 코드 생성
- [ ] tests/unit/ - 기본 단위 테스트

**성공 기준:**
- 모든 테스트 통과
- 라인 커버리지 >= 70%
- 기본 예시 작동

### 11.3 Critical Files

| 파일 | 중요도 | 설명 |
|------|--------|------|
| `/src/types.rs` | 높음 | 핵심 타입 정의로 모든 모듈의 기반 |
| `/src/error.rs` | 높음 | 일관된 에러 처리 및 Result 타입 정의 |
| `/src/core/arena.rs` | 높음 | Arena-based resource management 패턴 |
| `/src/core/engine.rs` | 높음 | 코드 생성 엔진의 핵심 로직 |
| `/src/lib.rs` | 중간 | 공개 API 및 모듈 선언의 진입점 |
| `/docs/adr/` | 중간 | 아키텍처 결정 기록 및 문서화 전략 |

---

## 12. 생성된 산출물 목록

### 아키텍처 (architecture-analyst)
- [architecture-principles.md](architecture-principles.md) - 5가지 핵심 원칙
- [docs/adr/ADR-001-arena-management.md](docs/adr/ADR-001-arena-management.md)
- [docs/adr/ADR-002-linked-pipeline.md](docs/adr/ADR-002-linked-pipeline.md)
- [docs/adr/ADR-003-module-organization.md](docs/adr/ADR-003-module-organization.md)

### 코드 구조 (code-designer)
- [code-structure-design.md](code-structure-design.md) - 디렉토리 구조, 파일별 책임

### 테스트 (testing-strategist)
- [testing-strategy-design.md](testing-strategy-design.md) - TDD, 테스트 구조, 벤치마크

### 품질 관리 (quality-manager)
- [quality/quality-standards.md](quality/quality-standards.md) - 코드 품질 표준
- [quality/commit-convention.md](quality/commit-convention.md) - 커밋 컨벤션
- [quality/pre-commit-checklist.md](quality/pre-commit-checklist.md) - 커밋 전 체크리스트
- [quality/quality-metrics.md](quality/quality-metrics.md) - 품질 메트릭 목표
- [templates/PLAN.md](templates/PLAN.md) - 개발 계획 템플릿
- [templates/ADR.md](templates/ADR.md) - ADR 템플릿

### 통합 (team-lead)
- [INTEGRATED_DESIGN.md](INTEGRATED_DESIGN.md) - 본 문서

---

## 13. 적용 가능한 원칙 요약

BPlusTree3 프로젝트에서 도출된 핵심 원칙:

1. **TDD**: 테스트를 먼저 작성 (RED → GREEN → REFACTOR)
2. **Tidy First**: 구조적 변경과 행위적 변경 분리
3. **Measure First**: 측정 없는 최적화 금지
4. **Small Commits**: 자주, 작게 커밋 (50-100줄 목표)
5. **Document Decisions**: 왜(why)를 기록 (ADR)
6. **Adversarial Testing**: 의도적으로 깨뜨려보기
7. **Arena Pattern**: 메모리 집약적 애플리케이션에 적용
8. **PhantomData**: 타입 안전성 확보
9. **Module by Change**: 변경 빈도 기준 모듈화
10. **Quality Gates**: 테스트와 리뷰의 다중 검증

---

## 참고 문서

- `/Users/moongyeom/IdeaProjects/private/toy/.claude/agents/codegen/plan1/bplustree3_vibe_coding_analysis.md`
- `/Users/moongyeom/IdeaProjects/private/toy/.claude/agents/codegen/plan2/README.md`
- `/Users/moongyeom/IdeaProjects/private/toy/.claude/agents/codegen/plan2/architecture-decisions.md`
- `/Users/moongyeom/IdeaProjects/private/toy/.claude/agents/codegen/plan2/code-structure-analysis.md`
- `/Users/moongyeom/IdeaProjects/private/toy/.claude/agents/codegen/plan2/development-feedback-loops.md`
- `/Users/moongyeom/IdeaProjects/private/toy/.claude/agents/codegen/plan2/lessons-learned.md`

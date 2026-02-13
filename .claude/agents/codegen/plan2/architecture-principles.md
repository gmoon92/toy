# 핵심 아키텍처 원칙 설계 문서

**architecture-analyst 에이전트 산출물**
**팀:** codegen-agent-team
**작성일:** 2026-02-13

---

## 개요

BPlusTree3 프로젝트의 성공적인 설계 패턴을 분석하여 코드 자동화 에이전트 팀의 아키텍처 원칙을 정립합니다.

---

## 1. Arena-Based Resource Management

### 설명
메모리, 파일 핸들, 네트워크 연결 등 모든 리소스를 Arena 패턴으로 관리합니다.

### 근거
BPlusTree3의 `CompactArena<T>`가 다음 이점을 제공합니다:
- **캐시 지역성**: 연속된 메모리 블록 사용으로 CPU 캐시 효율 향상
- **할당자 부하 감소**: 시스템 할당자 호출 최소화
- **예측 가능한 성능**: O(1) 할당/해제

### 적용
```rust
// 템플릿 캐싱에 Arena 패턴 적용
pub struct TemplateArena<T> {
    items: Vec<T>,
    free_list: Vec<usize>,
}

impl<T> TemplateArena<T> {
    pub fn allocate(&mut self, item: T) -> ArenaId {
        if let Some(index) = self.free_list.pop() {
            self.items[index] = item;
            ArenaId(index)
        } else {
            let index = self.items.len();
            self.items.push(item);
            ArenaId(index)
        }
    }
}
```

### 파일 적용 대상
- `src/core/arena.rs` - Arena 기반 리소스 관리
- `src/generator/code_generator.rs` - 템플릿 캐싱
- `src/output/writer.rs` - 파일 핸들 관리

---

## 2. Linked Processing Pipeline

### 설명
처리 단계를 연결 리스트로 구성하여 순차적 처리를 최적화합니다.

### 근거
BPlusTree3의 `next: NodeId` 필드가 범위 쿼리를 O(log n + k)로 최적화합니다:
- **순차 접근 최적화**: leaf 노드 간 링크로 인접 데이터 빠른 접근
- **캐시 효율성**: 메모리 순차 접근 패턴 활용
- **락 프리 순회**: 읽기 작업 시 동기화 오버헤드 감소

### 적용
```rust
// 코드 생성 파이프라인의 단계별 연결
pub struct PipelineStage {
    pub processor: Box<dyn Processor>,
    pub next: Option<StageId>,
}

pub struct ProcessingPipeline {
    stages: Arena<PipelineStage>,
    head: StageId,
}

impl ProcessingPipeline {
    pub fn process(&self, input: Input) -> Result<Output> {
        let mut current = self.head;
        let mut data = input;

        while let Some(stage) = self.stages.get(current) {
            data = stage.processor.process(data)?;
            current = stage.next.ok_or_else(|| Error::PipelineEnd)?;
        }

        Ok(data.into_output())
    }
}
```

### 파일 적용 대상
- `src/core/pipeline.rs` - 링크드 처리 파이프라인
- `src/generator/` - 코드 생성 단계 연결
- `src/transformer/` - 변환 단계 연결

---

## 3. Module Organization by Change Frequency

### 설명
기능별이 아닌 "변경 빈도" 기준으로 모듈을 분리합니다.

### 근거
BPlusTree3는 3,138줄의 `lib.rs`를 변경 빈도 기준으로 13개 모듈로 분리했습니다:
- **안정적 모듈**: types, error - 거의 변경되지 않음
- **진화적 모듈**: core, generator - 기능 추가 시 변경
- **실험적 모듈**: transformer - 빈번한 리팩토링

### 적용
```
src/
├── types.rs          # 안정적 - 핵심 타입 (변경 빈도: 낮음)
├── error.rs          # 안정적 - 에러 타입 (변경 빈도: 낮음)
├── core/
│   ├── arena.rs      # 중간 - 메모리 관리 (변경 빈도: 중간)
│   └── engine.rs     # 중간 - 엔진 로직 (변경 빈도: 중간)
├── parser/           # 중간 - 파싱 (변경 빈도: 중간)
├── generator/        # 진화적 - 코드 생성 (변경 빈도: 높음)
├── transformer/      # 실험적 - 변환 로직 (변경 빈도: 높음)
└── output/           # 안정적 - 출력 (변경 빈도: 낮음)
```

### 파일 적용 대상
- 전체 디렉토리 구조
- 각 모듈의 `mod.rs` - 명시적인 변경 빈도 주석

---

## 4. Dual-Language Implementation Strategy

### 설명
핵심 엔진은 Rust로 구현하고, 스크립팅/확장은 Python/JavaScript로 제공합니다.

### 근거
BPlusTree3는 Rust(성능)와 Python(생태계 통합) 모두를 지원합니다:
- **성능 크리티컬 경로**: Rust로 구현하여 메모리 안전성과 성능 확보
- **확장성**: Python/JavaScript 바인딩으로 생태계 통합
- **점진적 마이그레이션**: 기존 Python 프로젝트에 Rust 모듈 통합 가능

### 적용
```rust
// PyO3 바인딩 예시
use pyo3::prelude::*;

#[pyclass]
struct CodeEngine {
    inner: RustCodeEngine,
}

#[pymethods]
impl CodeEngine {
    #[new]
    fn new(config: &str) -> PyResult<Self> {
        let config = Config::from_json(config)?;
        Ok(Self {
            inner: RustCodeEngine::new(config),
        })
    }

    fn generate(&mut self, input: &str) -> PyResult<String> {
        self.inner.generate(input)
            .map_err(|e| PyRuntimeError::new_err(e.to_string()))
    }
}

#[pymodule]
fn code_automation(m: &Bound<'_, PyModule>) -> PyResult<()> {
    m.add_class::<CodeEngine>()?;
    Ok(())
}
```

### 파일 적용 대상
- `src/bindings/python.rs` - PyO3 바인딩
- `src/bindings/wasm.rs` - WASM 지원

---

## 5. Documentation-Driven Development (DDD)

### 설명
코드 변경 전 문서를 작성하고, 설계를 검토한 후 구현합니다.

### 근거
BPlusTree3는 다음 문서들을 체계적으로 관리합니다:
- `PLAN.md` - 구현 계획
- `ADR.md` - 아키텍처 결정 기록
- `PERFORMANCE_HISTORY.md` - 성능 변화 추적

### 적용
```markdown
<!-- PLAN.md 템플릿 -->
# Plan: [기능명]

## 개요
- 목적: 무엇을 해결하는가?
- 범위: 무엇을 포함/제외하는가?
- 예상 소요: 얼마나 걸리는가?

## 변경 분류
- [ ] 구조적 변경 (structural)
- [ ] 행위적 변경 (behavioral)

## 상세 설계
1. 구현 단계
2. 테스트 전략
3. 리스크 평가

## 검토 체크리스트
- [ ] 아키텍처 원칙 준수
- [ ] 의존성 규칙 준수
- [ ] 테스트 계획 수립
```

### 파일 적용 대상
- `docs/adr/` - 아키텍처 결정 기록
- `docs/plans/` - 구현 계획
- 각 기능별 `PLAN.md`

---

## 의존성 그래프

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

## 실행 계획

| 순서 | 원칙 | 적용 모듈 | 예상 소요 |
|------|------|-----------|----------|
| 1 | Arena-Based | core/arena.rs | 1-2일 |
| 2 | Linked Pipeline | core/pipeline.rs | 1일 |
| 3 | Change Frequency | 전체 디렉토리 구조 | 1일 |
| 4 | Dual-Language | bindings/ (추후) | 3-5일 |
| 5 | DDD | docs/ | 지속적 |

---

## 참고 문서

- `/Users/moongyeom/IdeaProjects/private/toy/.claude/agents/codegen/plan1/bplustree3_vibe_coding_analysis.md`
- `/Users/moongyeom/IdeaProjects/private/toy/.claude/agents/codegen/plan2/architecture-decisions.md`

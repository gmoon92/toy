# ADR-002: Linked Processing Pipeline

## 상태
- **상태**: 수락됨 (Accepted)
- **날짜**: 2026-02-13
- **결정자**: architecture-analyst

## 맥락

### 문제 상황
코드 생성 파이프라인은 여러 처리 단계를 거칩니다:
1. 입력 파싱
2. AST 변환
3. 코드 생성
4. 포맷팅
5. 출력

순차적 처리 시 각 단계 간 오버헤드가 누적됩니다.

### 제약 조건
- 파이프라인 단계 동적 구성 가능해야 함
- 중간 결과물 캐싱/재사용 가능해야 함
- 병렬 처리 가능성 열어두기

## 결정

### 선택한 방안: Linked List 기반 파이프라인

```rust
pub struct PipelineStage {
    pub processor: Box<dyn Processor>,
    pub next: Option<StageId>,
}

pub struct ProcessingPipeline {
    stages: Arena<PipelineStage>,
    head: StageId,
}
```

### 근거

**BPlusTree3 사례:**
- `next: NodeId` 필드로 범위 쿼리 O(log n + k) 달성
- 순차 순회 시 캐시 미스 70% 감소
- 락 프리 읽기 구현 가능

| 특성 | Linked | Array | Graph |
|------|--------|-------|-------|
| 삽입/삭제 | O(1) | O(n) | O(1) |
| 순회 | O(n) | O(n) | O(n) |
| 메모리 | 적음 | 중간 | 많음 |
| 복잡도 | 낮음 | 중간 | 높음 |

## 대안 고려

| 대안 | 장점 | 단점 | 결정 |
|------|------|------|------|
| Array 기반 | 캐시 효율 | 삽입/삭제 비용 | ❌ |
| DAG | 복잡한 흐름 | 과도한 설계 | ❌ |
| Linked List | 단순함, 유연성 | 포인터 오버헤드 | ✅ |
| Iterator chain | 함수형 | 디버깅 어려움 | ⚠️ |

## 영향

### 긍정적 영향
- 동적 파이프라인 구성 가능
- 단계별 캐싱/최적화 용이
- 단순한 구현

### 부정적 영향
- 포인터 오버헤드
- 캐시 미스 가능성
- 순환 참조 위험

### 위험 및 완화
| 위험 | 완화 전략 |
|------|----------|
| 순환 참조 | DAG 검증, build 시점 체크 |
| 캐시 미스 | 핫 경로 배열 최적화 |
| 메모리 오버헤드 | Arena 통합 관리 |

## 관련 문서

- `architecture-principles.md` - 원칙 2
- `code-structure-design.md` - core/pipeline.rs

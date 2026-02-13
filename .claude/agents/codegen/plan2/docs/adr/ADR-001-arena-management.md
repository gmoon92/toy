# ADR-001: Arena-Based Resource Management

## 상태
- **상태**: 수락됨 (Accepted)
- **날짜**: 2026-02-13
- **결정자**: architecture-analyst

## 맥락

### 문제 상황
코드 자동화 도구는 다음 리소스를 효율적으로 관리해야 합니다:
- 템플릿 캐싱 (빈번한 할당/해제)
- 파일 핸들 관리
- AST 노드 저장
- 중간 결과물 버퍼링

기본 할당자를 사용하면:
- 단편화로 인한 성능 저하
- 예측 불가능한 할당 시간
- 캐시 비효율성

### 제약 조건
- 메모리 안전성 유지 (Rust의 ownership 시스템)
- 예측 가능한 성능 (real-time 요구사항)
- 제로-카피 최적화 가능성

## 결정

### 선택한 방안: Arena 패턴 적용

모든 리소스를 Arena 기반으로 관리합니다:

```rust
pub struct CompactArena<T, const N: usize> {
    items: Vec<T>,
    free_list: Vec<usize>,
    _phantom: PhantomData<[T; N]>,
}
```

### 근거

| 요소 | Arena 방식 | 기본 할당자 |
|------|------------|-------------|
| 할당 시간 | O(1) | O(1) ~ O(log n) |
| 캐시 지역성 | 우수 | 불확실 |
| 단편화 | 없음 | 발생 가능 |
| 메모리 오버헤드 | 낮음 | 할당자 의존 |
| 예측 가능성 | 높음 | 낮음 |

**BPlusTree3 사례:**
- `CompactArena<T>` 사용으로 40% 성능 향상
- 벤치마크에서 할당 오버헤드 90% 감소

## 대안 고려

| 대안 | 장점 | 단점 | 결정 |
|------|------|------|------|
| 기본 할당자 | 간단함 | 예측 불가 | ❌ |
| 풀 할당자 | 재사용 가능 | 복잡함 | ⚠️ |
| Arena | 성능/단순함 | 수명 관리 필요 | ✅ |
| 커스텀 GC | 자동화 | 복잡성, 오버헤드 | ❌ |

## 영향

### 긍정적 영향
- 예측 가능한 성능
- 캐시 친화적 메모리 레이아웃
- 할당자 부하 감소

### 부정적 영향
- Arena 수명 관리 필요
- 복잡한 의존성 그래프
- 디버깅 난이도 증가

### 위험 및 완화
| 위험 | 완화 전략 |
|------|----------|
| 메모리 누수 | drop 구현, RAII 패턴 |
| use-after-free | borrow checker, PhantomData |
| Arena bloat | 주기적 compaction |

## 관련 문서

- `architecture-principles.md` - 원칙 1
- `code-structure-design.md` - core/arena.rs
- BPlusTree3: `src/core/arena.rs`

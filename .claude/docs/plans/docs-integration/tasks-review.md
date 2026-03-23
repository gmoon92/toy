# Task 문서 재검토 결과

## 검토 개요

- **기준 문서**: docs-integration-plan.md
- **대상 문서**: docs-integration-tasks.md
- **검토 목적**: 누락 작업 확인, 세분화 가능성, 모호한 문장 개선
- **검토일**: 2026-03-23

---

## 1. 누락된 작업 (Missing Tasks)

### 1.1 계획서에는 있으나 Task에 없는 작업

| # | 누락 항목 | 계획서 위치 | Task에 추가 필요 | 우선순위 |
|---|----------|------------|----------------|---------|
| 1 | **Hooks 문서 처리** | plan.md 1.1 주제별 분류 - Hooks (4개 중복) | ⚠️ 누락 | 높음 |
| 2 | **중복 섹션 매핑** | plan.md 6. 체크리스트 2번 | ⚠️ 일부 누락 | 중간 |
| 3 | **문서 타입별 비교** | plan.md 2.3 통합 방향에 포함 | ⚠️ 명시적 작업 없음 | 중간 |

### 1.2 세부 분석

#### 누락 1: Hooks 문서 통합

**현황:**
- plan.md에는 Hooks 문서가 4개로 분류됨 (hooks 상세 + essentials/standard-guid/onbording 요약)
- tasks.md에는 Hooks가 Phase 2 (Core Concepts)에서 간접적으로만 다룸
- **별도의 hooks 통합 작업이 없음**

**제안:**
```
[Task 추가 권장]
Task 2.1a: Hooks 콘텐츠 분석
- hooks.md의 상세 내용과 essentials/standard-guid/onbording의 요약 비교
- 어떤 내용이 통합되어야 하는지 명시
```

#### 누락 2: 중복 섹션 매핑

**현황:**
- plan.md 체크리스트: "중복 섹션 매핑" 항목 존재
- tasks.md: Task 2.1에서 "콘텐츠 매트릭스"로 대체하지만, **명확한 중복 매핑 작업 없음**

**제안:**
- Task 0.2 (고유 인사이트 추출)에 중복 섹션 매핑 체크리스트 추가

#### 누락 3: 문서 타입별 비교

**현황:**
- plan.md 그룹 C 통합 방향에 "문서 타입별 비교" 섹션 포함
- tasks.md: 이 섹션 작성을 위한 별도 작업 없음

**제안:**
```
Task 2.2a: 문서 타입 비교표 작성
- CLAUDE.md vs Rules vs Skills vs Agent 비교
- 메모리 관점, 로드 시점, 적용 범위 등 비교
```

---

## 2. 세분화 필요 작업 (Tasks Needing Granularity)

### 2.1 과도하게 큰 작업

| 작업 ID | 작업명 | 현재 예상 시간 | 문제점 | 세분화 제안 |
|---------|-------|--------------|-------|------------|
| 2.3 | 콘텐츠 통합 작성 (Core Concepts) | 45분 | 3개 문서 통합이 한 작업에 포함 | 3개로 분리 권장 |
| 3.2 | agent-fundamentals.md 작성 | 40분 | 300줄 작성이 한 번에 | 2개로 분리 권장 |
| 3.3 | agent-team-practice.md 작성 | 40분 | 300줄 작성이 한 번에 | 2개로 분리 권장 |

### 2.2 세분화 제안

#### Task 2.3 세분화

**현재:**
```
Task 2.3: 콘텐츠 통합 작성 (45분)
- 3개 문서 통합
```

**제안:**
```
Task 2.3a: 핵심 개념 섹션 작성 (15분)
- Context View, Skills, Hooks 통합

Task 2.3b: Agent 시스템 섹션 작성 (15분)
- 단일 Agent vs Agent Team, Feedback Loop

Task 2.3c: 실전 활용 및 오해와 진실 섹션 작성 (15분)
- 오케스트레이션, Git Worktree, 문서 관리
- 오해와 진실 핵심 인사이트
```

#### Task 3.2, 3.3 세분화

**현재:**
```
Task 3.2: agent-fundamentals.md 작성 (40분)
Task 3.3: agent-team-practice.md 작성 (40분)
```

**제안:**
```
Task 3.2a: Agent 개요 및 설계 원칙 작성 (20분)
Task 3.2b: 문서 구조와 템플릿 작성 (20분)

Task 3.3a: Blue/Red Team 및 실행 프로세스 작성 (20분)
Task 3.3b: Span Mode 및 고급 활용 작성 (20분)
```

---

## 3. 모호한 문장/정보 (Ambiguous Items)

### 3.1 모호한 부분 목록

| 위치 | 모호한 내용 | 문제 | 개선 제안 |
|------|------------|------|----------|
| Task 0.2 | "각 문서의 핵심 인사이트 3-5개 추출" | 기준이 모호 | 구체적 기준 제시 |
| Task 1.2 | "중복 내용 제거 (짧은 버전 우선)" | 우선순위가 상황에 따라 달라질 수 있음 | 결정 트리 제시 |
| Task 2.3 | "essentials 기반 + onbording의 description 전략" | 구체적인 통합 방법이 불명확 | 예시 포함 |
| Task 3.1 | "Task 2.4 권장" | 필수가 아닌 권장사항으로 표현되어 우선순위 모호 | 명확한 조건 제시 |
| Task 4.2 | "통합 완료된 9개 원본 문서" | 정확한 문서 목록이 리스트와 일치하는지 확인 필요 | 명확한 카운트 |

### 3.2 개선 제안 상세

#### 모호 1: 핵심 인사이트 추출 기준

**개선안:**
```markdown
**체크리스트 (개선):**
- [ ] 각 문서의 핵심 인사이트 3-5개 추출
  - 기준: 다른 문서에 없는 고유한 정보
  - 기준: 실무에서 바로 적용 가능한 구체적 지침
  - 기준: 독자가 오해하기 쉬운 부분에 대한 경고/팁
- [ ] 중복 가능성이 높은 섹션 표시
  - 동일 주제를 다루는 다른 문서와의 중복율 예상 (%)
```

#### 모호 2: 중복 내용 처리 우선순위

**개선안:**
```markdown
**작성 규칙 (개선):**
- 중복 내용 처리 결정 트리:
  1. 정확성: 더 최신/정확한 내용 선택
  2. 완성도: 더 상세하고 완성된 내용 선택
  3. 간결성: 동등한 경우 더 짧은 버전 선택
  4. 출처: 공식 문서 스타일을 따르는 버전 선택
```

#### 모호 3: 통합 방법 구체화

**개선안:**
```markdown
**작성 규칙 (구체화):**
- Skills 섹션:
  - 기본 구조와 개념: essentials.md 기반
  - description 작성 전략: onbording.md의 "description 작성 전략" 섹션 인용
  - 스킬 충돌 관리: onbording.md의 "스킬 충돌 관리" 표 포함
```

#### 모호 4: 의존성 조건 명확화

**개선안:**
```markdown
**의존성 (개선):**
- 필수: Task 0.2 (인사이트 추출)
- 선택: Task 2.4 (Core Concepts 완료) - 다음 경우 필수:
  - agent-fundamentals.md에 Agent 개요 포함 시
  - 문서 간 일관성이 중요한 경우
```

#### 모호 5: 문서 카운트 정확화

**현재:**
- Task 4.2: "통합 완료된 9개 원본 문서"
- 리스트: 9개 나열

**검증:**
```
그룹 A: 2개 (loading-strategy, claude-files-guid)
그룹 C: 3개 (essentials, onbording, standard-guid)
그룹 B: 4개 (agent-team, agent-design-guide, debate-pattern, advanced)
총: 9개 ✓
```

**개선안:**
```markdown
**이동 대상 (9개):**
그룹 A (2개):
- claude-code-loading-strategy.md
- claude-files-guid.md

그룹 C (3개):
- claude-code-essentials.md
- onbording.md
- standard-guid.md

그룹 B (4개):
- claude-code-agent-team.md
- claude-code-agent-design-guide.md
- agent-debate-pattern.md
- claude-code-advanced.md
```

---

## 4. 일관성 문제 (Consistency Issues)

### 4.1 문서명 불일치

| 위치 | 사용된 이름 | 일관성 문제 | 표준 이름 |
|------|----------|------------|----------|
| plan.md 2.1 | claude-code-memory-and-loading.md | tasks.md와 다름 | claude-code-memory-loading.md |
| plan.md 2.2 | claude-code-agent-comprehensive.md | 실제로는 2개로 분리 | agent-fundamentals.md + agent-team-practice.md |
| tasks.md 3.1 | agent-outline.md | 출력물이 2개 문서 구조 | agent-outline.md (OK) |

**권장:** plan.md의 문서명을 tasks.md와 일치시키거나, 참고 문서 표기 추가

### 4.2 라인 수 추정 불일치

| 문서 | plan.md 추정 | tasks.md 추정 | 차이 |
|------|-------------|--------------|------|
| memory-loading | 200줄 | 200줄 | ✅ 일치 |
| core-concepts | 400줄 | 400줄 | ✅ 일치 |
| agent-fundamentals | - | 300줄 | plan.md에는 600줄 통합으로 표기 |
| agent-team-practice | - | 300줄 | plan.md에는 600줄 통합으로 표기 |

**설명:** plan.md에는 통합 기준, tasks.md에는 분할 후 기준으로 표기되어 있어 혼란 가능

**권장:** 양쪽 문서에 분할 전/후 라인 수 모두 표기

---

## 5. 추가 고려사항 (Additional Considerations)

### 5.1 Risk Mitigation (리스크 완화)

plan.md에 언급된 "Lost in the Middle 현상"에 대한 대응이 tasks.md에 구체화되어 있지 않음.

**제안 작업 추가:**
```markdown
Task X.X: Lost in the Middle 대응 검토
- 각 통합 문서의 섹션별 토큰 예상량 계산
- 800줄 이상 시 분리 필요 여부 확인
- 핵심 정보가 문서 상/하단에 배치되었는지 확인
```

### 5.2 문서 품질 기준

plan.md에 "문서 크기 제약"이 명시되어 있지만, tasks.md의 품질 검증 항목에는 구체적인 수치 기준이 없음.

**제안:**
```markdown
품질 검증 항목 (구체화):
- [ ] 라인 수 기준: 800줄 초과 여부 확인
- [ ] 토큰 예상량: 200K 토큰의 10% 이하로 유지 권장
- [ ] TOC 포함: 2단계 이상 헤더 구조
- [ ] 핵심 정보 위치: 문서 상단 20% 내 위치
```

---

## 6. 검토 요약 및 권장사항

### 6.1 주요 발견사항

| 분류 | 개수 | 심각도 |
|------|------|-------|
| 누락 작업 | 3개 | 중간-높음 |
| 세분화 필요 | 3개 작업 | 중간 |
| 모호한 문장 | 5개 | 중간 |
| 일관성 문제 | 2개 | 낮음-중간 |

### 6.2 권장 조치

**즉시 수정 필요:**
1. ✅ Hooks 문서 처리 명시 (Task 2.1a 추가)
2. ✅ 중복 섹션 매핑 작업 구체화 (Task 0.2 보강)
3. ✅ 문서 타입 비교 작업 추가 (Task 2.2a 추가)

**개선 권장:**
4. ⚠️ Task 2.3, 3.2, 3.3 세분화 검토
5. ⚠️ 모호한 문장 구체화
6. ⚠️ plan.md와 tasks.md 문서명 일치화

**선택적 개선:**
7. 💡 Lost in the Middle 대응 작업 추가
8. 💡 품질 기준 수치화

---

## 7. 개선된 작업 목록 (개요)

**변경 전:** 18개 작업
**변경 후:** 24개 작업 (+6개)

**추가된 작업:**
- Task 2.1a: Hooks 콘텐츠 분석
- Task 2.2a: 문서 타입 비교표 작성
- Task 2.3a/b/c: 핵심 개념 섹션별 작성
- Task 3.2a/b: Agent 문서 섹션별 작성
- Task 3.3a/b: Agent Team 문서 섹션별 작성

**병합/조정된 작업:** 없음

---

*검토 완료: 2026-03-23*
*검토자: Claude Code*

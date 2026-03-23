# Claude Code 문서 통합 작업 목록 (Tasks)

## 개요

- **프로젝트**: ai/docs/tmp/ 문서 통합
- **목표**: 13개 문서 → 8개 문서로 통합 (38% 감소)
- **총 예상 소요**: 4-6시간
- **생성일**: 2026-03-23

---

## Phase 0: 준비 (Preparation)

### Task 0.1: 원본 문서 백업

**설명**: 모든 원본 문서를 backup/ 디렉토리에 복사
**입력**: ai/docs/tmp/*.md (13개)
**출력**: ai/docs/tmp/backup/*.md
**예상 시간**: 10분
**수행 조건**: 즉시

```bash
mkdir -p ai/docs/tmp/backup
cp ai/docs/tmp/*.md ai/docs/tmp/backup/
```

---

### Task 0.2: 고유 인사이트 추출

**설명**: 각 문서별로 다른 문서에 없는 고유 내용 목록화
**입력**: ai/docs/tmp/ 13개 문서
**출력**: ai/docs/tmp/analysis/unique-insights.md
**예상 시간**: 30분
**수행 조건**: Task 0.1 완료 후

**체크리스트**:
- [ ] 각 문서의 핵심 인사이트 3-5개 추출
  - 기준: 다른 문서에 없는 고유한 정보
  - 기준: 실무에서 바로 적용 가능한 구체적 지침
  - 기준: 독자가 오해하기 쉬운 부분에 대한 경고/팁
- [ ] 중복 가능성이 높은 섹션 표시
  - 동일 주제를 다루는 다른 문서와의 중복율 예상 (%)
  - 중복 섹션 매핑: 어떤 문서들이 같은 내용을 다루는지 목록화
- [ ] 인용/참조가 필요한 부분 기록

---

## Phase 1: 그룹 A - CLAUDE.md 메모리/로딩 통합

### Task 1.1: 통합 문서 구조 설계

**설명**: claude-code-memory-loading.md 목차 및 섹션 구조 설계
**입력**:
- claude-code-loading-strategy.md (100줄)
- claude-files-guid.md (270줄)
**출력**: ai/docs/tmp/drafts/memory-loading-outline.md
**예상 시간**: 15분
**수행 조건**: Phase 0 완료 후
**의존성**: Task 0.2

**설계 내용**:
```
claude-code-memory-loading.md (목표: 200줄)
├── 1. 핵심 원칙 (한 줄 요약 중심)
├── 2. 로딩 메커니즘
│   ├── 위쪽(부모) - Eager Loading
│   └── 아래쪽(자식) - Lazy Loading
├── 3. 모노레포 케이스 분석
│   ├── 케이스 1: 루트에서 실행
│   └── 케이스 2: 특정 모듈에서 실행
├── 4. 우선순위와 충돌
├── 5. 실무 설계 패턴
└── 6. 오해하기 쉬운 포인트
```

---

### Task 1.2: 콘텐츠 통합 작성

**설명**: 두 문서의 내용을 통합하여 새 문서 작성
**입력**: Task 1.1 구조 + 원본 2개 문서
**출력**: ai/docs/tmp/claude-code-memory-loading.md
**예상 시간**: 30분
**수행 조건**: Task 1.1 완료 후
**의존성**: Task 1.1

**작성 규칙**:
- 중복 내용 처리 결정 트리:
  1. 정확성: 더 최신/정확한 내용 선택
  2. 완성도: 더 상세하고 완성된 내용 선택
  3. 간결성: 동등한 경우 더 짧은 버전 선택
  4. 출처: 공식 문서 스타일을 따르는 버전 선택
- 상세 설명은 claude-files-guid 기준
- 실용적 예시 유지

---

### Task 1.3: 품질 검증

**설명**: 통합 문서 검토 및 교정
**입력**: claude-code-memory-loading.md
**출력**: 검토 완료 표시 + 수정 사항
**예상 시간**: 15분
**수행 조건**: Task 1.2 완료 후
**의존성**: Task 1.2

**검증 항목**:
- [ ] 200줄 내외 유지
- [ ] 모든 핵심 정보 포함
- [ ] TOC 포함
- [ ] 참조 링크 정확성

---

## Phase 2: 그룹 C - 핵심 개념 통합

### Task 2.1: 콘텐츠 매트릭스 작성

**설명**: 3개 문서의 섹션별 중복 분석 (Hooks 문서 포함)
**입력**:
- claude-code-essentials.md (406줄)
- onbording.md (527줄)
- standard-guid.md (518줄)
- claude-code-hooks.md (상세 내용)
**출력**: ai/docs/tmp/analysis/core-concepts-matrix.md
**예상 시간**: 30분
**수행 조건**: Phase 0 완료 후 (Task 1.x와 병렬 가능)
**의존성**: Task 0.2

**분석 대상**:
- Skills: 3개 문서의 Skills 관련 내용 비교
- Hooks: hooks.md 상세 내용 vs 3개 문서의 Hooks 요약 비교
- Context View: 모든 문서의 Context View 설명 비교
- 오해와 진실: onbording.md 고유 인사이트 중심

**매트릭스 예시**:
```
| 주제 | essentials | onbording | standard-guid | 통합 위치 |
|------|------------|-----------|---------------|----------|
| Skills 기본 | O | O | O | 섹션 1.2 |
| Hooks 기본 | O | O | O | 섹션 1.3 |
| Context View | O | O | O | 섹션 1.1 |
| 오해와 진실 | X | O | X | 섹션 4 |
```

---

### Task 2.2: 통합 문서 구조 설계

**설명**: claude-code-core-concepts.md 목차 설계
**입력**: Task 2.1 매트릭스
**출력**: ai/docs/tmp/drafts/core-concepts-outline.md
**예상 시간**: 20분
**수행 조건**: Task 2.1 완료 후
**의존성**: Task 2.1

---

### Task 2.2a: 문서 타입 비교표 작성

**설명**: CLAUDE.md vs Rules vs Skills vs Agent 비교표 작성
**입력**: 원본 문서들의 문서 타입 설명
**출력**: ai/docs/tmp/analysis/doc-type-comparison.md
**예상 시간**: 15분
**수행 조건**: Task 2.2와 병렬 또는 직후
**의존성**: Task 2.2

**비교 항목**:
- 메모리 관점 (로드 시점, 적용 범위)
- 작성 규약 (프론트매터, 구조)
- 사용 목적 (언제 어떤 문서를 작성하는가)
- 우선순위 (충돌 시 적용 규칙)

**설계 내용**:
```
claude-code-core-concepts.md (목표: 400줄)
├── 1. Claude Code 핵심 개념
│   ├── 1.1 Context View와 토큰 누적
│   ├── 1.2 Skills (정의, 작성 규약, description 매칭)
│   ├── 1.3 Hooks (이벤트, 설정, Exit Code)
│   └── 1.4 문서 타입별 비교
├── 2. Agent 시스템 개요
│   ├── 2.1 단일 Agent vs Agent Team
│   └── 2.2 Feedback Loop
├── 3. 실전 활용 가이드
│   ├── 3.1 오케스트레이션
│   ├── 3.2 Git Worktree
│   └── 3.3 문서 관리 방법
└── 4. 오해와 진실
    ├── 4.1 "지침에 적으면 무조건 실행?" → X
    ├── 4.2 "프롬프트만 잘 작성하면?" → X
    └── 4.3 문서 길이 권장사항
```

---

### Task 2.3a: 핵심 개념 섹션 작성

**설명**: Context View, Skills, Hooks, 문서 타입별 비교 섹션 작성
**입력**: Task 2.2 구조 + 원본 문서 (essentials, onbording, standard-guid, hooks)
**출력**: claude-code-core-concepts.md 섹션 1.1-1.4
**예상 시간**: 20분
**수행 조건**: Task 2.2 완료 후
**의존성**: Task 2.2

**작성 규칙**:
- Skills:
  - 기본 구조와 개념: essentials.md 기반
  - description 작성 전략: onbording.md의 "description 작성 전략" 섹션 인용
  - 스킬 충돌 관리: onbording.md의 "스킬 충돌 관리" 표 포함
- Hooks:
  - 기본 개념: essentials.md 기반
  - 상세 내용: hooks.md 인용
  - 실행 가능한 예시 포함
- 문서 타입별 비교: Task 2.2a 비교표 결과 포함

---

### Task 2.3b: Agent 시스템 섹션 작성

**설명**: 단일 Agent vs Agent Team, Feedback Loop 섹션 작성
**입력**: Task 2.2 구조 + 원본 문서
**출력**: claude-code-core-concepts.md 섹션 2.1-2.2
**예상 시간**: 15분
**수행 조건**: Task 2.3a 완료 후
**의존성**: Task 2.3a

**작성 규칙**:
- Agent 시스템 개요: essentials.md 기반
- Agent Team 간략 소개 (상세 내용은 agent-team-practice.md에서 다룸)

---

### Task 2.3c: 실전 활용 및 오해와 진실 섹션 작성

**설명**: 실전 활용 가이드 + 오해와 진실 섹션 작성
**입력**: Task 2.2 구조 + 원본 문서 (특히 onbording.md)
**출력**: claude-code-core-concepts.md 섹션 3-4
**예상 시간**: 15분
**수행 조건**: Task 2.3b 완료 후
**의존성**: Task 2.3b

**작성 규칙**:
- 실전 활용 가이드: essentials.md + standard-guid.md 내용 통합
- 오해와 진실: onbording.md 기반 (핵심 인사이트 중심)
  - "지침에 적으면 무조건 실행?" → X
  - "프롬프트만 잘 작성하면?" → X
  - 문서 길이 권장사항

---

### Task 2.4: 품질 검증

**설명**: 통합 문서 검토 및 교정
**입력**: claude-code-core-concepts.md
**출력**: 검토 완료 표시 + 수정 사항
**예상 시간**: 20분
**수행 조건**: Task 2.3 완료 후
**의존성**: Task 2.3

**검증 항목**:
- [ ] 400줄 내외 유지
- [ ] 4개 섹션 모두 균형 있게 구성
- [ ] onbording의 핵심 인사이트 모두 포함
- [ ] TOC 포함

---

## Phase 3: 그룹 B - Agent 문서 통합

### Task 3.1: 분할 결정 및 구조 설계

**설명**: 600줄 한계로 인한 2개 문서 분할 구조 설계
**입력**:
- claude-code-agent-team.md (103줄)
- claude-code-agent-design-guide.md (318줄)
- agent-debate-pattern.md (535줄)
- claude-code-advanced.md (494줄)
**출력**: ai/docs/tmp/drafts/agent-outline.md (2개 문서 구조)
**예상 시간**: 30분
**수행 조건**: Phase 0 완료 후
**의존성**:
- 필수: Task 0.2 (인사이트 추출)
- 선택: Task 2.4 (Core Concepts 완료) - 다음 경우 필수:
  - agent-fundamentals.md에 Agent 개요 포함 시
  - 문서 간 일관성이 중요한 경우

**설계 내용**:
```
agent-fundamentals.md (목표: 300줄)
├── 1. Agent 개요
├── 2. 행위 계약(Behavior Contract) 설계
│   ├── 2.1 역할-책임-입출력 정의
│   └── 2.2 제약 조건 설정
├── 3. 문서 구조와 작성 규약
│   ├── 3.1 프론트매터
│   └── 3.2 System Prompt 구조
├── 4. 기본 역할 템플릿
│   ├── 4.1 Architect
│   ├── 4.2 Developer
│   └── 4.3 Reviewer
└── 5. 자주 하는 실수

agent-team-practice.md (목표: 300줄)
├── 1. Agent Team 개요
├── 2. Blue Team vs Red Team
│   ├── 2.1 Blue Team 역할 (설계 및 구현)
│   └── 2.2 Red Team 역할 (검증 및 공격)
├── 3. 실행 프로세스
│   ├── 3.1 실행 흐름
│   └── 3.2 시간 관리
├── 4. Span Mode 설정
│   ├── 4.1 it2 설치 및 설정
│   └── 4.2 CLI 옵션
├── 5. 고급 활용
│   ├── 5.1 Git Worktree 연계
│   └── 5.2 오케스트레이션
└── 6. 비용/한계/주의사항
    ├── 6.1 Context Explosion
    ├── 6.2 비용 분석
    └── 6.3 적용 체크리스트
```

---

### Task 3.2a: Agent 개요 및 설계 원칙 작성

**설명**: Agent 개요, 행위 계약(Behavior Contract), 역할-책임-입출력 정의
**입력**: Task 3.1 구조 + 원본 (design-guide, advanced 일부)
**출력**: agent-fundamentals.md 섹션 1-2
**예상 시간**: 20분
**수행 조건**: Task 3.1 완료 후
**의존성**: Task 3.1

**작성 규칙**:
- design-guide의 이론적 내용 중심
- Agent 개요는 간략히 (Core Concepts와 중복 방지)
- 행위 계약 설계 방법론 상세히

---

### Task 3.2b: 문서 구조와 템플릿 작성

**설명**: 문서 구조/작성 규약, 역할 템플릿, 자주 하는 실수
**입력**: Task 3.1 구조 + 원본 (design-guide, agent-skills 참고)
**출력**: agent-fundamentals.md 섹션 3-5
**예상 시간**: 20분
**수행 조건**: Task 3.2a 완료 후
**의존성**: Task 3.2a

**작성 규칙**:
- 프론트매터, System Prompt 구조
- 기본 역할 템플릿: Architect, Developer, Reviewer
- agent-skills.md와 연계되는 내용은 간략히 + 링크 제공
- 실제 템플릿 예시 포함

---

### Task 3.3a: Blue/Red Team 및 실행 프로세스 작성

**설명**: Agent Team 개요, Blue/Red Team 역할, 실행 프로세스
**입력**: Task 3.1 구조 + 원본 (agent-team, debate-pattern)
**출력**: agent-team-practice.md 섹션 1-3
**예상 시간**: 20분
**수행 조건**: Task 3.2b 완료 후
**의존성**: Task 3.2b

**작성 규칙**:
- debate-pattern의 Blue/Red Team 구조 중심
- Blue Team: 설계 및 구현 역할
- Red Team: 검증 및 공격 역할
- 실행 흐름 및 시간 관리

---

### Task 3.3b: Span Mode 및 고급 활용 작성

**설명**: Span Mode 설정, 고급 활용, 비용/한계/주의사항
**입력**: Task 3.1 구조 + 원본 (agent-team, advanced)
**출력**: agent-team-practice.md 섹션 4-6
**예상 시간**: 20분
**수행 조건**: Task 3.3a 완료 후
**의존성**: Task 3.3a

**작성 규칙**:
- Span Mode 설정 (it2 설치, CLI 옵션)
- 고급 활용: Git Worktree 연계, 오케스트레이션
- 비용/한계/주의사항:
  - 문서 상단에 경고 배치
  - Context Explosion 설명
  - 비용 분석 및 적용 체크리스트

---

### Task 3.4: 품질 검증

**설명**: 2개 Agent 문서 검토
**입력**: agent-fundamentals.md, agent-team-practice.md
**출력**: 검토 완료 표시 + 수정 사항
**예상 시간**: 20분
**수행 조건**: Task 3.3b 완료 후
**의존성**: Task 3.3b

**검증 항목**:
- [ ] 각각 300줄 내외 유지 (agent-fundamentals.md: 섹션 1-5, agent-team-practice.md: 섹션 1-6)
- [ ] 중복 내용 없음 (또는 상호 참조)
- [ ] Blue/Red Team 구조 명확
- [ ] TOC 포함

---

## Phase 4: 정리 및 마무리

### Task 4.1: 독립 문서 연결 검토

**설명**: 통합되지 않는 4개 문서의 참조 업데이트
**입력**:
- claude-code-agent-skills.md (946줄)
- claude-code-lsp.md (316줄)
- claude-code-mcp.md (295줄)
- claude-code-context-windows.md (354줄)
**출력**: 수정 사항 목록 (필요시)
**예상 시간**: 15분
**수행 조건**: Phase 1-3 완료 후
**의존성**: Task 1.3, Task 2.4, Task 3.4

**작업 내용**:
- [ ] agent-skills.md에 agent-fundamentals.md 링크 추가
- [ ] 필요시 "관련 문서" 섹션 추가
- [ ] broken link 확인

---

### Task 4.2: 원본 문서 아카이브

**설명**: 통합 완료된 원본 문서를 archive/로 이동
**입력**: 통합 완료된 9개 원본 문서
**출력**: ai/docs/tmp/archive/ 하위로 이동
**예상 시간**: 10분
**수행 조건**: Task 4.1 완료 후
**의존성**: Task 4.1

**이동 대상 (9개)**:

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
archive/
├── claude-code-loading-strategy.md
├── claude-files-guid.md
├── claude-code-essentials.md
├── onbording.md
├── standard-guid.md
├── claude-code-agent-team.md
├── claude-code-agent-design-guide.md
├── agent-debate-pattern.md
└── claude-code-advanced.md
```

---

### Task 4.3: 최종 문서 구조 확인

**설명**: 통합 후 최종 문서 목록 및 크기 확인
**입력**: ai/docs/tmp/*.md
**출력**: ai/docs/tmp/README.md (통합 후 문서 목록)
**예상 시간**: 15분
**수행 조건**: Task 4.2 완료 후
**의존성**: Task 4.2

**예상 최종 구조**:
```
ai/docs/tmp/
├── README.md                          # 통합 후 문서 목록
├── docs-integration-plan.md           # 본 계획서
├── docs-integration-tasks.md          # 본 작업 목록
├── claude-code-core-concepts.md       # 통합 (400줄)
├── claude-code-memory-loading.md      # 통합 (200줄)
├── agent-fundamentals.md              # 통합 (300줄)
├── agent-team-practice.md             # 통합 (300줄)
├── claude-code-agent-skills.md        # 유지 (946줄)
├── claude-code-lsp.md                 # 유지 (316줄)
├── claude-code-mcp.md                 # 유지 (295줄)
├── claude-code-context-windows.md     # 유지 (354줄)
├── backup/                            # 원본 백업
└── archive/                           # 통합 후 이동된 원본
```

---

## Phase 5: 품질 보증 (QA)

### Task 5.1: 통합 문서 전체 검토

**설명**: 모든 통합 문서 최종 검토
**입력**: 4개 통합 문서
**출력**: QA 리포트
**예상 시간**: 30분
**수행 조건**: Task 4.3 완료 후
**의존성**: Task 4.3

**검토 항목**:
- [ ] 총 라인 수 확인 (목표: 1,200줄 → 1,200줄)
- [ ] 문서별 라인 수 확인:
  - [ ] claude-code-memory-loading.md: 200줄 내외
  - [ ] claude-code-core-concepts.md: 400줄 내외
  - [ ] agent-fundamentals.md: 300줄 내외
  - [ ] agent-team-practice.md: 300줄 내외
- [ ] Lost in the Middle 대응 확인:
  - [ ] 각 문서 800줄 미만
  - [ ] 핵심 정보가 문서 상단 20% 내 위치
  - [ ] 섹션별 토큰 예상량 적정 (200K 토큰의 10% 이하 권장)
- [ ] 모든 고유 인사이트 보존 확인
- [ ] 문서 간 일관성 확인
- [ ] 링크/참조 정확성
- [ ] TOC 포함 확인 (2단계 이상 헤더 구조)

---

### Task 5.2: 목표 달성 확인

**설명**: 통합 목표 달성 여부 확인
**입력**: QA 리포트 + 원본/통합 비교
**출력**: 달성 리포트
**예상 시간**: 10분
**수행 조건**: Task 5.1 완료 후
**의존성**: Task 5.1

**확인 항목**:
```
목표: 13개 문서 (5,459줄) → 8개 문서 (3,111줄)
      38% 문서 수 감소, 43% 라인 수 감소

체크:
- [ ] 문서 수: 13 → 8 (감소율 38%)
- [ ] 라인 수: 5,459 → ~3,100 (감소율 43%)
- [ ] 통합 그룹 A 완료
- [ ] 통합 그룹 B 완료
- [ ] 통합 그룹 C 완료
```

---

## 작업 의존성 다이어그램

```
Phase 0: 준비
├── Task 0.1 (백업)
└── Task 0.2 (인사이트 추출)
    ├──→ Phase 1: 그룹 A
    │   ├── Task 1.1 (구조 설계)
    │   ├── Task 1.2 (콘텐츠 작성)
    │   └── Task 1.3 (품질 검증)
    │
    ├──→ Phase 2: 그룹 C
    │   ├── Task 2.1 (매트릭스)
    │   ├── Task 2.2 (구조 설계)
    │   ├── Task 2.2a (문서 타입 비교)
    │   ├── Task 2.3a (핵심 개념 섹션)
    │   ├── Task 2.3b (Agent 시스템 섹션)
    │   ├── Task 2.3c (실전 활용 섹션)
    │   └── Task 2.4 (품질 검증)
    │
    └──→ Phase 3: 그룹 B
        ├── Task 3.1 (분할 구조)
        ├── Task 3.2a (Agent 개요/설계)
        ├── Task 3.2b (문서 구조/템플릿)
        ├── Task 3.3a (Blue/Red Team)
        ├── Task 3.3b (Span Mode/고급)
        └── Task 3.4 (품질 검증)

Phase 4: 정리
├── Task 4.1 (독립 문서 연결)
├── Task 4.2 (원본 아카이브)
└── Task 4.3 (최종 구조)

Phase 5: QA
├── Task 5.1 (전체 검토)
└── Task 5.2 (목표 확인)
```

---

## 실행 우선순위 시나리오

### 시나리오 1: 중간 통합 (권장)

**범위**: 그룹 A + 그룹 C만 통합, 그룹 B(Agent)는 유지
**작업**: Task 0.x → Task 1.x → Task 2.x → Task 4.x (Task 3.x 제외)
**예상 시간**: 2-3시간
**결과**: 11개 문서 유지 (13 → 11)

### 시나리오 2: 전면 통합

**범위**: 모든 그룹 통합
**작업**: 모든 Task 수행
**예상 시간**: 4-6시간
**결과**: 8개 문서 (13 → 8)

---

## 참고 자료

- docs-integration-plan.md: 통합 계획 상세
- backup/: 원본 문서 백업
- drafts/: 작성 중인 구조/아웃라인

---

*생성일: 2026-03-23*
*총 작업 수: 24개 (Task 2.2a, 2.3a/b/c, 3.2a/b, 3.3a/b 추가)*
*총 예상 소요: 5-7시간 (전면 통합 시)*

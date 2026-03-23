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
- [ ] 중복 가능성이 높은 섹션 표시
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
- 중복 내용 제거 (짧은 버전 우선)
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

**설명**: 3개 문서의 섹션별 중복 분석
**입력**:
- claude-code-essentials.md (406줄)
- onbording.md (527줄)
- standard-guid.md (518줄)
**출력**: ai/docs/tmp/analysis/core-concepts-matrix.md
**예상 시간**: 30분
**수행 조건**: Phase 0 완료 후 (Task 1.x와 병렬 가능)
**의존성**: Task 0.2

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

### Task 2.3: 콘텐츠 통합 작성

**설명**: 3개 문서 통합하여 새 문서 작성
**입력**: Task 2.2 구조 + 원본 3개 문서
**출력**: ai/docs/tmp/claude-code-core-concepts.md
**예상 시간**: 45분
**수행 조건**: Task 2.2 완료 후
**의존성**: Task 2.2

**작성 규칙**:
- Skills: essentials 기반 + onbording의 description 전략
- Hooks: essentials 기반 + 실행 가능한 예시
- 오해와 진실: onbording 기반 (핵심 인사이트)

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
**의존성**: Task 0.2, Task 2.4 (Core Concepts 완료 후 권장)

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

### Task 3.2: agent-fundamentals.md 작성

**설명**: Agent 기초 및 설계 문서 작성
**입력**: Task 3.1 구조 + 원본 (design-guide, advanced 일부)
**출력**: ai/docs/tmp/agent-fundamentals.md
**예상 시간**: 40분
**수행 조건**: Task 3.1 완료 후
**의존성**: Task 3.1

**작성 규칙**:
- design-guide의 이론적 내용 중심
- agent-skills.md와 연계되는 내용은 간략히
- 실제 템플릿 예시 포함

---

### Task 3.3: agent-team-practice.md 작성

**설명**: Agent Team 실전 활용 문서 작성
**입력**: Task 3.1 구조 + 원본 (agent-team, debate-pattern, advanced)
**출력**: ai/docs/tmp/agent-team-practice.md
**예상 시간**: 40분
**수행 조건**: Task 3.2 완료 후
**의존성**: Task 3.2

**작성 규칙**:
- debate-pattern의 Blue/Red Team 구조 중심
- agent-team의 실행 방법 포함
- 비용/한계는 상단에 경고로 배치

---

### Task 3.4: 품질 검증

**설명**: 2개 Agent 문서 검토
**입력**: agent-fundamentals.md, agent-team-practice.md
**출력**: 검토 완료 표시 + 수정 사항
**예상 시간**: 20분
**수행 조건**: Task 3.3 완료 후
**의존성**: Task 3.3

**검증 항목**:
- [ ] 각각 300줄 내외 유지
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

**이동 대상**:
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
- [ ] 모든 고유 인사이트 보존 확인
- [ ] 문서 간 일관성 확인
- [ ] 링크/참조 정확성

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
    │   ├── Task 2.3 (콘텐츠 작성)
    │   └── Task 2.4 (품질 검증)
    │
    └──→ Phase 3: 그룹 B (Task 2.4 권장)
        ├── Task 3.1 (분할 구조)
        ├── Task 3.2 (fundamentals 작성)
        ├── Task 3.3 (team-practice 작성)
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
*총 작업 수: 18개*
*총 예상 소요: 4-6시간 (전면 통합 시)*

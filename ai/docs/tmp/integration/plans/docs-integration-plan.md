# Claude Code 문서 통합 계획

## 분석 개요

ai/docs/tmp/ 디렉토리의 13개 문서를 분석하여 연관 내용과 통합 가능성을 파악했습니다.

---

## 1. 문서 분류 및 중복 분석

### 1.1 주제별 분류

| 카테고리 | 문서 | 페이지 수 | 특징 |
|---------|------|----------|------|
| **Agent Team** | agent-team, agent-design-guide, agent-skills, agent-debate-pattern, advanced | 5개 | Agent 관련 가장 많은 중복 |
| **핵심 개념** | essentials, standard-guid, onbording | 3개 | Skills, Hooks 등 기초 개념 중복 |
| **CLAUDE.md 로딩** | loading-strategy, claude-files-guid | 2개 | 완전히 동일한 주제 |
| **컨텍스트** | context-windows | 1개 | 독립적 내용 |
| **LSP** | claude-code-lsp | 1개 | 독립적 내용 |
| **MCP** | claude-code-mcp | 1개 | 독립적 내용 |
| **Hooks** | hooks (상세), essentials/standard-guid/onbording (요약) | 4개 | 중복 있음 |

### 1.2 중복 수준 분석

```
[높은 중복] ────────────────────────────────────────────
│
│ 1. CLAUDE.md 로딩 전략
│    - loading-strategy.md (100줄)
│    - claude-files-guid.md (270줄)
│    → 동일한 내용, 짧은 버전 vs 긴 버전
│
│ 2. Agent Team / Agent Debate Pattern
│    - agent-team.md: 실행 방법, CLI 옵션
│    - agent-debate-pattern.md: 패턴 설계, Blue/Red Team
│    - advanced.md: 고급 개념 포함 Agent Team
│    → 역할 정의, 실행 흐름 중복
│
│ 3. 핵심 개념 (Essentials vs Standard-guid vs Onbording)
│    - skills 설명 중복
│    - hooks 설명 중복
│    - context view 중복
│
[중간 중복] ────────────────────────────────────────────
│
│ 4. Agent 설계 가이드
│    - agent-design-guide.md: 이론적 설계
│    - agent-skills.md: 실전 작성 가이드
│    → 일부 원칙 중복
│
[낮은 중복/독립] ─────────────────────────────────────────
│
│ 5. LSP, MCP, Context Windows
│    - 각각 독립적인 주제
│    → 통합 불필요, 개별 유지 권장
```

---

## 2. 통합 가능 영역 상세

### 2.1 통합 권장 그룹 A: CLAUDE.md 로딩 전략

**대상 문서:**
- `claude-code-loading-strategy.md` (100줄)
- `claude-files-guid.md` (270줄)

**통합 방향:**
```
통합 문서: claude-code-memory-and-loading.md
├── 핵심 원칙 (loading-strategy 기반, 간결)
├── 상세 동작 설명 (claude-files-guid 기반)
├── 모노레포 케이스 분석
└── 실무 설계 패턴
```

**예상 효과:** 370줄 → 200줄 (중복 제거)

---

### 2.2 통합 권장 그룹 B: Agent 관련 문서

**대상 문서:**
- `claude-code-agent-team.md` (103줄) - 실행/설정
- `claude-code-agent-design-guide.md` (318줄) - 설계 원칙
- `agent-debate-pattern.md` (535줄) - 패턴/프로세스
- `claude-code-advanced.md` (494줄) - 고급 개념

**통합 방향:**
```
메인 문서: claude-code-agent-comprehensive.md
├── 1. Agent 개요와 설계 원칙 (design-guide 기반)
│   ├── 행위 계약(Behavior Contract) 개념
│   ├── 역할-책임-입출력 정의
│   └── 일반적인 설계 패턴
│
├── 2. Agent Team 구성과 실행 (agent-team + debate-pattern)
│   ├── Blue Team / Red Team 구조
│   ├── 역할별 정의 (Architect, Critic, etc.)
│   ├── 실행 흐름과 프로세스
│   └── Span Mode (멀티 Pane) 설정
│
├── 3. 고급 활용법 (advanced 기반)
│   ├── Git Worktree 연계
│   ├── Agent Native 워크플로우
│   └── 오케스트레이션
│
└── 4. 비용/한계/주의사항
    ├── Context Explosion
    ├── 비용 분석
    └── 적용 체크리스트
```

**예상 효과:** 1,450줄 → 600줄 (중복 제거, 체계적 재구성)

---

### 2.3 통합 권장 그룹 C: 핵심 개념 (Core Concepts)

**대상 문서:**
- `claude-code-essentials.md` (406줄)
- `onbording.md` (527줄)
- `standard-guid.md` (518줄)

**내용 분석:**
| 주제 | essentials | onbording | standard-guid |
|------|-----------|-----------|---------------|
| Skills | O | O | O |
| Hooks | O | O | O |
| Context View | O | O | O |
| Agent Team | O | O | O |
| Agent Native | X | O | O |
| 문서 관리 | X | O | O |

**통합 방향:**
```
통합 문서: claude-code-core-concepts.md
├── 1. Claude Code 핵심 개념
│   ├── Context View와 토큰 누적
│   ├── Skills (정의, 작성 규약, description 매칭)
│   ├── Hooks (이벤트, 설정, Exit Code)
│   └── 문서 타입별 비교
│
├── 2. Agent 시스템 개요
│   ├── 단일 Agent vs Agent Team
│   └── Feedback Loop
│
├── 3. 실전 활용 가이드
│   ├── 오케스트레이션
│   ├── Git Worktree
│   └── 문서 관리 방법
│
└── 4. 오해와 진실 (onbording의 핵심 인사이트)
    ├── "지침에 적으면 무조건 실행?" → X
    ├── "프롬프트만 잘 작성하면?" → X
    └── 문서 길이 권장사항
```

**예상 효과:** 1,451줄 → 400줄 (대폭적인 중복 제거)

---

### 2.4 통합 불필요 (독립 유지 권장)

| 문서 | 이유 | 크기 |
|------|------|------|
| `claude-code-lsp.md` | 독립적인 기술 주제 | 316줄 |
| `claude-code-mcp.md` | 독립적인 기술 주제 | 295줄 |
| `claude-code-context-windows.md` | 상세 기술 내용 | 354줄 |
| `claude-code-agent-skills.md` | 매우 상세한 작성 가이드 (946줄) | 946줄 |

---

## 3. 통합 시 고려사항

### 3.1 문서 크기 제약

**Claude Code 권장사항:**
- CLAUDE.md: ~60줄 (실무적), 공식 200줄
- Agent/Skill 문서: 100줄 권장, 500줄 이하 유지, 800줄 초과 시 분리 필수

**통합 문서별 예상 크기:**
| 통합 문서 | 예상 크기 | 권장 조치 |
|-----------|----------|----------|
| claude-code-memory-and-loading.md | ~200줄 | ✅ 적정 |
| claude-code-agent-comprehensive.md | ~600줄 | ⚠️ 2개로 분리 고려 |
| claude-code-core-concepts.md | ~400줄 | ✅ 적정 |

### 3.2 Lost in the Middle 현상 대응

**문제:** 문서가 길어지면 중간 내용이 요약됨

**해결책:**
1. **점진적 공개 (Progressive Disclosure)**
   - 핵심 내용은 본문에
   - 상세 내용은 별도 파일로 분리

2. **섹션 구조 강화**
   - 명확한 헤더 계층
   - TOC (Table of Contents) 포함

3. **분리 기준**
   - claude-code-agent-comprehensive.md → 2개로 분리:
     - agent-fundamentals.md (설계 원칙 + 기본)
     - agent-team-advanced.md (Team + 고급)

---

## 4. 최종 통합 제안

### 4.1 통합 그룹 구성

```
[그룹 A] 메모리/로딩 ──────────────────────────────
Before: 2개 문서 (370줄)
After:  1개 문서 (200줄)
- claude-code-memory-loading.md

[그룹 B] Agent 종합 ───────────────────────────────
Before: 4개 문서 (1,450줄)
After:  2개 문서 (600줄)
- agent-fundamentals.md (설계 + 기본)
- agent-team-practice.md (Team + 고급)

[그룹 C] 핵심 개념 ────────────────────────────────
Before: 3개 문서 (1,451줄)
After:  1개 문서 (400줄)
- claude-code-core-concepts.md

[독립 유지] ───────────────────────────────────────
- claude-code-lsp.md
- claude-code-mcp.md
- claude-code-context-windows.md
- claude-code-agent-skills.md (상세 작성 가이드)
```

### 4.2 통합 후 예상 문서 구조

```
ai/docs/tmp/ 통합 후:
├── claude-code-core-concepts.md (400줄) ← essentials + onbording + standard-guid
├── claude-code-memory-loading.md (200줄) ← loading-strategy + claude-files-guid
├── agent-fundamentals.md (300줄) ← design-guide + 일부 advanced
├── agent-team-practice.md (300줄) ← agent-team + debate-pattern + advanced
├── claude-code-agent-skills.md (946줄) ← 유지 (상세 가이드)
├── claude-code-lsp.md (316줄) ← 유지
├── claude-code-mcp.md (295줄) ← 유지
└── claude-code-context-windows.md (354줄) ← 유지

총: 8개 문서 (3,111줄)
기존: 13개 문서 (5,459줄)
감소율: 38% 문서 수, 43% 라인 수
```

---

## 5. 통합 시나리오별 권장사항

### 시나리오 1: 최소 통합 (보수적)

**통합 대상:**
- loading-strategy + claude-files-guid만 통합
- 나머지는 개별 유지

**이유:** 안전, 기존 참조 깨짐 최소화

### 시나리오 2: 중간 통합 (권장)

**통합 대상:**
- 그룹 A (메모리/로딩)
- 그룹 C (핵심 개념)
- Agent 문서는 개별 유지

**이유:** 핵심 중복 제거 + Agent 상세 내용 보존

### 시나리오 3: 전면 통합 (적극적)

**통합 대상:**
- 모든 그룹 통합
- Agent 2개 문서로 재구성

**이유:** 최종적으로 깔끔한 문서 구조

---

## 6. 통합 작업 시 체크리스트

- [ ] 각 문서의 고유한 인사이트 식별
- [ ] 중복 섹션 매핑
- [ ] 통합 문서 목차 설계
- [ ] 원본 문서 백업
- [ ] 통합 문서 작성
- [ ] 교차 참조 링크 업데이트
- [ ] 문서 품질 검증 (길이, 구조)

---

*생성일: 2026-03-23*
*분석 대상: ai/docs/tmp/ 하위 13개 문서*

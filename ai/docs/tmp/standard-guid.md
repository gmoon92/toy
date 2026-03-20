# Claude Code

클로드 코드의 전체적인 개념과 활용 방법을 설명합니다.

# TOC

- 동작 방식
    - Context View
        - 한정된 메모리
        - 턴 개념과 compact 현상
- Sub Agent, Agent Skill, Command
    - Claude Code CLI `md` 문서와 특징
        - rules → 모듈별 지침
        - agents → 행위 전문가
        - skills → 공통 레시피
        - commands → 명령어
        - CLAUDE.md → 프로젝트
    - 메모리, 특징, 차이점
- 문서는 어떤걸 작성하면 좋을지
    - AI 하니스(Harness)
        - AI Model 의 문제점
        - 추론 자유도에 대해 결정론적 사고와 비결정론적 사고
        - 앤트로픽 스킬 프로젝트 대폭 강화 업데이트
- Agent Team
    - Feedback Loop (피드백 루프)
    - 단일 에이전트의 문제점
    - Agent Debate Pattern
- Agent Native
    - 업무 자동화
    - 오케스트레이션

---

# 동작 방식

## Context View

Claude Code는 사용자와의 상호작용을 **Context View**라는 메모리 공간에서 관리합니다.

이 컨텍스트는 토큰 단위로 측정되며, 모든 입력과 출력이 누적되어 저장됩니다.

### 토큰 누적 메커니즘

```
[Turn 1] User Input (50 tokens) → AI Output (100 tokens) = 150 tokens
[Turn 2] Previous Context (150) + New Input (30) → Output (80) = 260 tokens
[Turn 3] Previous Context (260) + New Input (40) → Output (120) = 420 tokens
...
```

### 한정된 메모리와 Compact 현상

컨텍스트 뷰는 무한하지 않습니다. 일정 토큰 한도에 도달하면 **Compact** 현상이 발생합니다.

**Compact의 영향:**

- 오래된 대화 내용을 요약하여 공간 확보
- 요약 과정에서 세부 지침이나 중요 문맥이 사라질 수 있음
- 워크플로우나 작업 단위가 명시된 문서도 의도한 대로 동작하지 않을 수 있음

> **주의**: `.md` 파일에 작성된 워크플로우나 꼭 지켜야 할 지침들도 compact되면서 핵심 내용이 유실될 수 있습니다.

---

# Claude Code 확장 포인트

컨텍스트 한계를 극복하기 위해 Claude Code는 다양한 문서 기반 확장 기능을 제공합니다.

## 디렉토리 구조

```
project
└── .claude/
│   ├── agent-memory/        # 에이전트 메모리 파일
│   ├── rules/               # rules/ → 코드 작성 규칙 정의
│   ├── agents/              # agents/ → AI 에이전트 정의
│   ├── skills/              # skills/ → 재사용 가능한 작업 레시피
│   └── commands/            # commands/ → 커스텀 명령어
└── CLAUDE.md                # 프로젝트 전반 컨텍스트
```

## Sub Agent, Skill, Command 비교

| 구분          | Sub Agent                 | Skill                     | Command           |
|:------------|:--------------------------|:--------------------------|:------------------|
| **목적**      | 전문 작업 수행                  | 작업 방법 공유                  | 사용자 명령 실행         |
| **호출 방식**   | Agent tool로 코드에서 호출       | 에이전트 내에서 참조               | `/` 접두사로 CLI에서 실행 |
| **메모리 관점**  | 완전히 격리된 컨텍스트. 작업 과정은 블랙박스 | 문서가 프롬프트의 일부로 로드. 레시피북 역할 | 직접 메모리 없음. 일회성 실행 |
| **특징**      | 병렬 처리 가능, 독립 실행           | 재사용 가능한 레시피               | 즉시 실행, 결과 반환      |
| **프롬프트 유형** | system prompt             | instruction prompt        | action prompt     |

## 문서 타입별 역할

### rules/

코드 작성 규칙 및 지침을 정의합니다. 프로젝트 전체 또는 특정 파일 패턴에 적용됩니다.

```yaml
---
name: typescript-naming
description: TypeScript 네이밍 컨벤션 규칙
glob: "**/*.ts"
alwaysApply: false
---

## 클래스 명명 규칙
- PascalCase 사용
- 명사로 시작
...
```

**지원하는 프론트메터 항목:**

| 항목 | 필수 | 설명 | 예시 값 |
|:---|:---|:---|:---|
| `name` | 선택 | 규칙 식별자. 미지정 시 파일명 사용 | `typescript-naming` |
| `description` | **필수** | 규칙 설명 | `TypeScript 네이밍 컨벤션` |
| `glob` | 선택 | 규칙이 적용될 파일 패턴 | `**/*.ts` |
| `alwaysApply` | 선택 | 항상 적용 여부 | `true`, `false` |

### agents/

AI 에이전트를 정의합니다.

```yaml
---
name: backend-developer
description: 백엔드 아키텍처 설계 전문가
tools: [ Read, Write, Edit ]
model: inherit
color: yellow
memory: true
---

# 역할
당신은 백엔드 아키텍처 설계 전문가입니다...
```

**지원하는 프론트메터 항목:**

| 항목 | 필수 | 설명 | 예시 값 |
|:---|:---|:---|:---|
| `name` | 선택 | 에이전트 식별자. 미지정 시 파일명 사용 | `backend-developer` |
| `description` | **필수** | 에이전트 역할 설명 | `백엔드 아키텍처 설계 전문가` |
| `tools` | 선택 | 사용 가능한 도구 목록 | `[Read, Write, Edit, Bash]` |
| `allowed-tools` | 선택 | 허용된 도구 목록 (세밀한 제어) | `[Read, Glob, Grep]` |
| `model` | 선택 | 사용할 Claude 모델 | `inherit`, `claude-opus-4-6`, `claude-sonnet-4-6`, `claude-haiku-4-5` |
| `color` | 선택 | UI에서 표시할 색상 | `red`, `green`, `yellow`, `blue`, `purple`, `gray` |
| `memory` | 선택 | 메모리 사용 여부 | `true`, `false` |
| `mode` | 선택 | 권한 모드 | `default`, `plan`, `auto`, `acceptEdits`, `dontAsk`, `bypassPermissions` |

### skills/

재사용 가능한 작업 레시피를 정의합니다.

```yaml
---
name: git:commit
description: Git 커밋 메시지 작성을 요청할 때 반드시 사용하세요.
disable-model-invocation: true
user-invocable: true
allowed-tools: [Read, Bash, AskUserQuestion]
---

# Workflow
...
```

**지원하는 프론트메터 항목:**

| 항목 | 필수 | 설명 | 예시 값 |
|:---|:---|:---|:---|
| `name` | 선택 | 스킬 식별자. 미지정 시 파일명 사용 | `git:commit` |
| `description` | **필수** | 스킬 호출 조건 및 역할 설명 | `Git 변경사항 분석 시 사용` |
| `allowed-tools` | 선택 | 스킬 실행 시 사용 가능한 도구 | `[Read, Bash, Edit]` |
| `user-invocable` | 선택 | `/스킬명`으로 사용자가 직접 호출 가능 여부 | `true`, `false` |
| `disable-model-invocation` | 선택 | 모델 자동 호출 비활성화 여부 | `true`, `false` |

### commands/

커스텀 명령어를 정의합니다.

```yaml
---
name: docs:rewrite
description: 문서 품질을 다중 에이전트 협업으로 개선합니다.
user-invocable: true
---

## 사용법
/docs:rewrite <파일경로>
```

**지원하는 프론트메터 항목:**

| 항목 | 필수 | 설명 | 예시 값 |
|:---|:---|:---|:---|
| `name` | 선택 | 명령어 식별자. 미지정 시 `네임스페이스:명령어` 형식 사용 | `docs:rewrite` |
| `description` | **필수** | 명령어 설명 | `문서 품질을 개선합니다` |
| `user-invocable` | 선택 | 사용자가 `/명령어`로 호출 가능 여부 | `true` (기본값), `false` |
| `allowed-tools` | 선택 | 명령어 실행 시 사용 가능한 도구 | `[Read, Write, Agent]` |

### CLAUDE.md

프로젝트 전반의 컨텍스트를 제공합니다. 프론트메터 없이 순수 마크다운으로 작성합니다.

```markdown
# 프로젝트 가이드

## 기술 스택
- TypeScript, Node.js

## 아키텍처
- Layered Architecture 적용
...
```

---

# 문서는 어떤걸 작성하면 좋을지

## 1. AI Harness (하니스)

AI 모델의 비결정적 특성을 제어하고 안정적인 출력을 확보하기 위한 프레임워크입니다.

### AI Model의 문제점

- **Hallucination (환각)**: 학습 데이터에 없는 정보를 사실처럼 생성
- **Sycophancy (아첨)**: 사용자의 의견에 무조건 동조하여 잘못된 방향으로 진행

**방지 방법:**

- 허용된 라이브러리/패키지 명시
- "문제가 있다면 지적하라"는 지시 추가
- 검증 단계 추가

### 추론 자유도 결정

| 구분         | 결정론적 (Deterministic)  | 비결정론적 (Non-deterministic) |
|:-----------|:----------------------|:--------------------------|
| **적합한 작업** | 데이터 마이그레이션, 리팩토링, 배포  | 아키텍처 설계, UI/UX 개선, 문제 해결  |
| **문서 특징**  | 단계별 지시, 체크리스트, 검증 포인트 | 고려사항 나열, 다양한 안 제시, 판단 유도  |

### 앤트로픽 스킬 프로젝트

스킬 문서는 모델의 응답 텍스트를 기준으로 사용 여부가 결정됩니다.

**스킬 개발 원칙:**

- 재사용성: 여러 프로젝트/에이전트에서 활용
- 모듈성: 단일 책임 원칙 준수
- 검증 가능성: 성공/실패 판단 기준 명확화

---

# Agent Team

단일 에이전트의 한계를 극복하기 위해 여러 전문 에이전트가 협업하는 구조입니다.

## 단일 에이전트의 문제점

- **컨텍스트 한계**: 긴 대화 중 중요 정보가 compact되며 유실
- **검증 부재**: 메인 에이전트는 서브 에이전트의 작업 세부사항을 직접 확인 불가
- **메모리 관점의 한계**: 서브 에이전트는 독립된 메모리 공간 사용

## Agent Debate Pattern

여러 에이전트가 동일한 주제에 대해 토론하여 최적의 해결책을 도출합니다.

| 역할          | 책임             | 특성          |
|:------------|:---------------|:------------|
| Proposer    | 초안 생성, 아이디어 제시 | 창의적, 발산적 사고 |
| Critic      | 문제점 지적, 대안 제시  | 비판적, 분석적 사고 |
| Synthesizer | 의견 통합, 최종 결정   | 통합적, 판단력    |

## Feedback Loop (피드백 루프)

개발자 → 리뷰어 → 개발자 순환 구조를 통해 코드 품질을 향상시킵니다.

**장점:**

- 별도의 검증 단계 없이도 품질 향상
- 여러 라운드를 거치며 점진적 개선
- 리뷰 패턴이 문서화되어 재사용 가능

---

# Agent Native

AI 에이전트를 업무 프로세스의 핵심 실행 주체로 활용하는 패러다임입니다.

## 업무 자동화

| 단계      | 전통적 방식      | Agent Native           |
|:--------|:------------|:-----------------------|
| 요구사항 분석 | 사람이 문서 작성   | PRD Agent가 분석 및 문서화    |
| 설계      | 사람이 아키텍처 설계 | Architect Agent가 설계    |
| 개발      | 사람이 코드 작성   | Developer Agent가 코드 생성 |
| 리뷰      | 동료 개발자 검토   | Reviewer Agent가 자동 검증  |
| 테스트     | QA 팀 수동 테스트 | QA Agent가 자동 테스트       |
| 배포      | 수동 배포       | Deploy Agent가 자동 배포    |

### PoC 워크플로우

```
Redmine Issue → PRD Agent → Planner Agent → Developer Agent → Reviewer Agent → QA Agent → 완료
```

## 오케스트레이션

에이전트 실행 순서와 조건을 제어하는 메커니즘입니다.

- **직렬 실행 (Sequential)**: 각 단계의 출력이 다음 단계의 입력
- **병렬 실행 (Parallel)**: 독립적인 작업을 동시에 진행
- **조건 분기 (Conditional)**: 실행 결과에 따른 동적 경로 결정

---

# CLI 명령어 예시

## 에이전트 확인

```text
❯ /agents

────────────────────────────────────────────────────────────────────────────────
  backend-architect

  ❯ 1. View agent
    2. Edit agent
    3. Delete agent
    4. Back
```

- `/agents` 를 통해 현재 프로젝트에 등록된 모든 서브 에이전트 조회 가능
- `1. View agent` 선택 시 전체 문서가 메모리에 등록되어 있는 것 확인 가능
    - front matter + 본문
    - 프론트 메터 항목: `description`, `tools`, `model`, `memory`, `color`
    - 본문은 `system prompt` 로 등록

## 메모리 확인

```text
❯ /memory

────────────────────────────────────────────────────────────────────────────────
  Memory

    Auto-memory: on

  ❯ 1.  Project memory                       Checked in at ./CLAUDE.md
    2.  User memory                          Saved in ~/.claude/CLAUDE.md
    3.  Open auto-memory folder
    4.  Open backend-architect agent memory   project scope
    5.  Open backend-developer agent memory   project scope
```

- `Project memory` 선택 시 `CLAUDE.md` 파일 자체가 로드된 것 확인 가능

---

# 마무리

Claude Code는 단순한 코딩 도구가 아닌, AI Native 개발 환경의 핵심 인프라입니다.

## 핵심 원칙

1. **Harness 설계**: AI의 불확실성을 제어하기 위한 가이드라인과 제약
2. **Agent Team**: 복잡한 작업을 전문 에이전트로 분리하여 처리
3. **Orchestration**: 에이전트 간 협업과 실행 흐름 제어
4. **Continuous Improvement**: PoC를 통해 검증하고 표준화

## 향후 방향

PoC 단계에서 검증된 패턴들은 향후 표준 개발 프로세스로 정착될 것입니다.

지속적인 개선과 문서화를 통해 **재현 가능한 AI 기반 개발 환경**을 구축해야 합니다.

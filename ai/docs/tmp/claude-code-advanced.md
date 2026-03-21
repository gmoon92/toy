# Claude Code 고급 개념 가이드

Claude Code의 고급 활용법과 확장 포인트를 설명합니다.

---

## Claude Code 확장 포인트

컨텍스트 한계를 극복하기 위해 Claude Code는 다양한 문서 기반 확장 기능을 제공합니다.

### 디렉토리 구조

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

### Sub Agent, Skill, Command 비교

| 구분          | Sub Agent                 | Skill                     | Command           |
|:------------|:--------------------------|:--------------------------|:------------------|
| **목적**      | 전문 작업 수행                  | 작업 방법 공유                  | 사용자 명령 실행         |
| **호출 방식**   | Agent tool로 코드에서 호출       | 에이전트 내에서 참조               | `/` 접두사로 CLI에서 실행 |
| **메모리 관점**  | 완전히 격리된 컨텍스트. 작업 과정은 블랙박스 | 문서가 프롬프트의 일부로 로드. 레시피북 역할 | 직접 메모리 없음. 일회성 실행 |
| **특징**      | 병렬 처리 가능, 독립 실행           | 재사용 가능한 레시피               | 즉시 실행, 결과 반환      |
| **프롬프트 유형** | system prompt             | instruction prompt        | action prompt     |

### 문서 타입별 역할

#### rules/

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

| 항목            | 필수     | 설명                   | 예시 값                 |
|:--------------|:-------|:---------------------|:---------------------|
| `name`        | 선택     | 규칙 식별자. 미지정 시 파일명 사용 | `typescript-naming`  |
| `description` | **필수** | 규칙 설명                | `TypeScript 네이밍 컨벤션` |
| `glob`        | 선택     | 규칙이 적용될 파일 패턴        | `**/*.ts`            |
| `alwaysApply` | 선택     | 항상 적용 여부             | `true`, `false`      |

#### agents/

AI 에이전트를 정의합니다. 에이전트는 **행위 계약(Behavior Contract)**으로 설계하는 것이 가장 안정적입니다.

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
  소프트웨어 아키텍트로서 시스템 아키텍처를 설계합니다.

## 책임
- 시스템 아키텍처 설계
- 서비스 경계 정의
- 아키텍처 패턴 선택
- 확장성 및 안정성 고려사항 파악

## 프로세스
  1. 요구사항 분석
  2. 핵심 컴포넌트 식별
  3. 시스템 상호작용 정의
  4. 기술 스택 선택
  5. 설계 결정 문서화

## 출력 형식
아키텍처 문서 포함:
  - 시스템 개요
  - 컴포넌트 구조
  - 데이터 흐름
  - 기술 결정사항

  ## 제약
  - 구현 코드는 작성하지 않음
  - 아키텍처 수준의 결정에만 집중
```

**지원하는 프론트메터 항목:**

| 항목              | 필수     | 설명                     | 예시 값                                                                     |
|:----------------|:-------|:-----------------------|:-------------------------------------------------------------------------|
| `name`          | 선택     | 에이전트 식별자. 미지정 시 파일명 사용 | `backend-developer`                                                      |
| `description`   | **필수** | 에이전트 역할 설명             | `백엔드 아키텍처 설계 전문가`                                                        |
| `tools`         | 선택     | 사용 가능한 도구 목록           | `[Read, Write, Edit, Bash]`                                              |
| `allowed-tools` | 선택     | 허용된 도구 목록 (세밀한 제어)     | `[Read, Glob, Grep]`                                                     |
| `model`         | 선택     | 사용할 Claude 모델          | `inherit`, `claude-opus-4-6`, `claude-sonnet-4-6`, `claude-haiku-4-5`    |
| `color`         | 선택     | UI에서 표시할 색상            | `red`, `green`, `yellow`, `blue`, `purple`, `gray`                       |
| `memory`        | 선택     | 메모리 사용 여부              | `true`, `false`                                                          |
| `mode`          | 선택     | 권한 모드                  | `default`, `plan`, `auto`, `acceptEdits`, `dontAsk`, `bypassPermissions` |

#### skills/

재사용 가능한 작업 레시피를 정의합니다.

```yaml
---
name: git:commit
description: Git 커밋 메시지 작성을 요청할 때 반드시 사용하세요.
disable-model-invocation: true
user-invocable: true
allowed-tools: [ Read, Bash, AskUserQuestion ]
---

# Workflow
...
```

**지원하는 프론트메터 항목:**

| 항목                         | 필수     | 설명                        | 예시 값                 |
|:---------------------------|:-------|:--------------------------|:---------------------|
| `name`                     | 선택     | 스킬 식별자. 미지정 시 파일명 사용      | `git:commit`         |
| `description`              | **필수** | 스킬 호출 조건 및 역할 설명          | `Git 변경사항 분석 시 사용`   |
| `allowed-tools`            | 선택     | 스킬 실행 시 사용 가능한 도구         | `[Read, Bash, Edit]` |
| `user-invocable`           | 선택     | `/스킬명`으로 사용자가 직접 호출 가능 여부 | `true`, `false`      |
| `disable-model-invocation` | 선택     | 모델 자동 호출 비활성화 여부          | `true`, `false`      |

#### commands/

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

| 항목               | 필수     | 설명                                | 예시 값                   |
|:-----------------|:-------|:----------------------------------|:-----------------------|
| `name`           | 선택     | 명령어 식별자. 미지정 시 `네임스페이스:명령어` 형식 사용 | `docs:rewrite`         |
| `description`    | **필수** | 명령어 설명                            | `문서 품질을 개선합니다`         |
| `user-invocable` | 선택     | 사용자가 `/명령어`로 호출 가능 여부             | `true` (기본값), `false`  |
| `allowed-tools`  | 선택     | 명령어 실행 시 사용 가능한 도구                | `[Read, Write, Agent]` |

#### CLAUDE.md

프로젝트 전반의 컨텍스트를 제공합니다. 프론트매터 없이 순수 마크다운으로 작성합니다.

```markdown
# 프로젝트 가이드

## 기술 스택

- TypeScript, Node.js

## 아키텍처

- Layered Architecture 적용
  ...
```

---

## AI Harness (하니스)

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

### 스킬 개발 원칙

- 재사용성: 여러 프로젝트/에이전트에서 활용
- 모듈성: 단일 책임 원칙 준수
- 검증 가능성: 성공/실패 판단 기준 명확화

---

## Agent Team

단일 에이전트의 한계를 극복하기 위해 여러 전문 에이전트가 협업하는 구조입니다.

### 단일 에이전트의 문제점

- **컨텍스트 한계**: 긴 대화 중 중요 정보가 compact되며 유실
- **검증 부재**: 메인 에이전트는 서브 에이전트의 작업 세부사항을 직접 확인 불가
- **메모리 관점의 한계**: 서브 에이전트는 독립된 메모리 공간 사용

### Agent Debate Pattern

여러 에이전트가 동일한 주제에 대해 토론하여 최적의 해결책을 도출합니다. Claude Code Agent Teams를 활용한 고품질 설계 검토 방법론입니다.

#### Blue Team vs Red Team 구조

**Blue Team (설계 및 구현):**

| 역할              | 책임                 | 산출물           |
|:----------------|:-------------------|:--------------|
| **Architect**   | 시스템 구조, 확장성, 패턴 적용 | 설계 문서, 다이어그램  |
| **Backend**     | 구현 가능성, 운영 복잡도 검토  | 구현 가이드, 배포 전략 |
| **Performance** | 성능 병목 분석, 최적화 방안   | 성능 분석서, 벤치마크  |
| **Security**    | 보안 위험 분석, 취약점 검토   | 보안 검토서, 위험 목록 |

**Red Team (검증 및 공격):**

| 역할                   | 책임                | Focus                |
|:---------------------|:------------------|:---------------------|
| **Critic**           | 공격적 검증 및 문제 발굴    | 숨겨진 가정, 모순점, 실패 시나리오 |
| **Devil's Advocate** | 대안 관점 제시 및 철학적 비판 | 편향된 시각에 대한 도전        |

**Coordination (조율):**

| 역할              | 책임                  |
|:----------------|:--------------------|
| **Moderator**   | 토론 진행, 시간 관리, 합의 도출 |
| **Synthesizer** | 상충되는 의견 중재 및 통합     |
| **Editor**      | 최종 문서 작성 및 품질 보증    |

**Critic vs Devil's Advocate 차이점:**

- **Critic**: "이 설계가 왜 틀렸는지 찾아라" (공격적 검증)
- **Devil's Advocate**: "완전히 다른 접근법은 없는가?" (대안적 사고)

#### 실행 흐름

```
1. 문제 정의 → 2. Blue Team 제안 → 3. Red Team 검증 → 4. 합의 도출 → 5. 문서화
```

**주의사항:**

- 모든 역할은 **동일한 Shared Context**를 사용합니다
- **Context Explosion**에 주의 (최대 2~3라운드 권장)
- **Red Team (Critic)** 없이는 효과가 절반입니다
- 비용과 시간이 단일 AI 대비 3-10배 증가할 수 있습니다

#### 비용 분석

Multi-Agent는 단일 AI 대비 비용이 증가합니다:

| 방식                | 예상 비용    | 특징               |
|:------------------|:---------|:-----------------|
| Single AI         | ~$0.06   | 1회 호출, 빠른 결과     |
| Multi-Agent (8역할) | ~$0.60   | 8회 호출, 심층 검토     |
| **배율**            | **~10배** | 복잡도에 따라 3-10배 변동 |

**현실적인 추정:**

- 단순 설계: 30-60분, $0.30-0.60
- 복잡한 설계: 2-4시간, $2-5
- 대규모 시스템: 반나절 이상, $10+

#### 한계와 주의사항

**1. Context Explosion**
모든 역할이 동일한 Context Window를 공유하여 토큰이 급증합니다:

- 6개 역할 × 3라운드 × 1500토큰 = 27,000토큰
- 원본 문서/코드 추가 시 100K+ 토큰
- **해결 방안**: 주기적 요약, 최대 2-3라운드 제한

**2. 합의 불능 상황**
극단적으로 상반된 의견으로 인한 교착 상태 가능성 → Human-in-the-loop 필요

**3. 역할 경계 모호화 (Role Contamination)**
동일 Context 내에서 여러 역할을 수행하다 본면 역할 간 경계가 모호해집니다.

**4. Consensus Bias**
시간이 지남에 따라 Agent들이 서로 동의하는 경향이 생깁니다:

```
Round 1: good debate
Round 3: agents start agreeing (consensus bias)
```

#### 실전 적용 체크리스트

**적합한 경우:**

- 아키텍처 의사결정이 필요한 경우
- 다양한 이해관계자가 존재하는 복잡한 시스템
- 높은 신뢰성이 요구되는 미션 크리티컬한 설계

**부적합한 경우:**

- 간단한 기능 추가나 버그 수정
- 시간이 긴급한 핫픽스
- 이미 충분히 검증된 표준 패턴 적용

#### Span Mode (멀티 Pane) 실행

여러 전문 에이전트를 tmux 분할 창에서 동시에 실행할 수 있습니다.

```bash
# it2 설치 (iTerm2 Split Pane용)
uv tool install it2

# 환경변수 설정
export CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1

# Agent Team CLI 예시
claude --agent-id critic-1@team-name \
       --agent-name critic-1 \
       --team-name team-name \
       --agent-color pink \
       --agent-type backend-critic \
       --permission-mode acceptEdits \
       --model claude-opus-4-6
```

**주요 CLI 옵션:**

| 옵션                  | 설명                                 |
|---------------------|------------------------------------|
| `--agent-id`        | 고유 ID (이름@팀명)                      |
| `--agent-name`      | 에이전트 이름                            |
| `--team-name`       | 팀 이름                               |
| `--agent-color`     | 터미널 표시 색상                          |
| `--agent-type`      | 에이전트 유형                            |
| `--permission-mode` | 권한 모드 (acceptEdits, plan, default) |
| `--model`           | 사용 모델                              |

### Feedback Loop (피드백 루프)

개발자 → 리뷰어 → 개발자 순환 구조를 통해 코드 품질을 향상시킵니다.

**장점:**

- 별도의 검증 단계 없이도 품질 향상
- 여러 라운드를 거치며 점진적 개선
- 리뷰 패턴이 문서화되어 재사용 가능

---

## Git Worktree + Sub Agent

분리된 작업 공간에서 각 에이전트가 독립적으로 작업한 후 결과를 병합하는 방식입니다.

```text
claude --worktree feature-auth   # 터미널 1: 새 기능 개발
claude --worktree bugfix-123     # 터미널 2: 버그 수정
```

"워크트리에서 작업해줘" 또는 "새 worktree를 시작해줘"라고 요청하면, Claude가 자동으로 `.claude/worktree` 디렉토리를 생성하고 해당 위치에서 작업을 진행합니다.

```text
// 워크트리 목록 확인
git worktree list

// 특정 워크트리 수동 삭제
git worktree remove .claude/worktrees/feature-auth

// 이미 삭제된 디렉토리의 워크트리 레코드 정리
git worktree prune
```

서브 에이전트의 프론트매터에서도 워크트리 격리를 설정할 수 있습니다.

```yaml
---
isolation: worktree
---
```

---

## Agent Native

AI 에이전트를 업무 프로세스의 핵심 실행 주체로 활용하는 패러다임입니다.

### 업무 자동화

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

### 오케스트레이션

에이전트 실행 순서와 조건을 제어하는 메커니즘입니다.

- **직렬 실행 (Sequential)**: 각 단계의 출력이 다음 단계의 입력
- **병렬 실행 (Parallel)**: 독립적인 작업을 동시에 진행
- **조건 분기 (Conditional)**: 실행 결과에 따른 동적 경로 결정

---

## CLI 명령어 예시

### 에이전트 확인

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

### 메모리 확인

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

## 핵심 원칙

1. **Harness 설계**: AI의 불확실성을 제어하기 위한 가이드라인과 제약
2. **Agent Team**: 복잡한 작업을 전문 에이전트로 분리하여 처리
3. **Orchestration**: 에이전트 간 협업과 실행 흐름 제어
4. **Continuous Improvement**: PoC를 통해 검증하고 표준화

---

## 향후 방향

PoC 단계에서 검증된 패턴들은 향후 표준 개발 프로세스로 정착될 것입니다.

지속적인 개선과 문서화를 통해 **재현 가능한 AI 기반 개발 환경**을 구축해야 합니다.

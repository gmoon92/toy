# Claude Code 핵심 개념

Claude Code의 기본 개념과 효과적인 활용 방법을 설명합니다.

---

## 1. Claude Code 핵심 개념

### 1.1 Context View와 토큰 누적

Claude Code는 개발자와의 상호작용을 **Context View**라는 메모리 공간에서 관리합니다. 이 컨텍스트는 토큰 단위로 측정되며, 모든 입력과 출력이 누적되어 저장됩니다.

#### 토큰 누적 메커니즘

```
[Turn 1] User Input (50 tokens) → AI Output (100 tokens) = 150 tokens
[Turn 2] Previous Context (150) + New Input (30) → Output (80) = 260 tokens
[Turn 3] Previous Context (260) + New Input (40) → Output (120) = 420 tokens
...
```

#### 한정된 메모리와 Compact 현상

컨텍스트 뷰는 무한하지 않습니다. Claude Code는 **200K 토큰**(표준), 최대 **1M 토큰**(베타)을 지원합니다. 일정 토큰 한도에 도달하면 **Compact** 현상이 발생합니다.

**Compact의 영향:**

- 오래된 대화 내용을 요약하여 공간 확보
- 요약 과정에서 세부 지침이나 중요 문맥이 사라질 수 있음
- 워크플로우나 작업 단위가 명시된 문서도 의도한 대로 동작하지 않을 수 있음

> **주의**: `.md` 파일에 작성된 워크플로우나 꼭 지켜야 할 지침들도 compact되면서 핵심 내용이 유실될 수 있습니다.

#### 모델별 컨텍스트 윈도우 크기

| 모델 | 컨텍스트 윈도우 | 특징 |
|-----|---------------|------|
| Claude Opus 4.6 | 200K | 최고 성능, 컨텍스트 인식 |
| Claude Sonnet 4.6 | 200K / 1M (베타) | 컨텍스트 인식, 장문 컨텍스트 지원 |
| Claude Haiku 4.5 | 200K | 경량, 컨텍스트 인식 |

---

### 1.2 Skills

스킬(Skill)은 프론트매터의 메타데이터만 로드하고, 본문은 필요할 때만 로드하는 **재사용 가능한 작업 레시피**입니다.

#### 스킬의 등장 배경

스킬 문서는 기존 서브 에이전트와 커맨드 문서의 정적 로딩 문제를 해결하기 위해 도입되었습니다. 스킬은 프론트매터의 메타데이터만 로드하고, 본문은 필요할 때만 로드합니다. 이는 Claude Code가 컨텍스트 뷰 메모리 관리와 토큰 사용량 최적화에 중점을 두고 있음을 보여줍니다.

#### 스킬 vs 도구

| 구분 | 도구(Tool) | 스킬(Skill) |
|:---|:---|:---|
| **정의** | 특정 함수를 직접 실행하기 위한 인터페이스 | 파일과 지침을 포함한 모듈형 실행 계약 |
| **적합한 작업** | 단일 작업, API 호출 | 복잡한 워크플로우, 도메인 지식이 필요한 작업 |
| **예시** | 파일 읽기, 검색, API 호출 | Git 커밋 메시지 작성, 코드 리뷰, 문서 생성 |

#### 스킬의 장점과 단점

**장점:**
- 컨텍스트 윈도우의 auto compact 현상 완화
- 불필요한 토큰 사용 감소
- 재사용 가능한 레시피 제공

**단점:**
- 모델이 프론트매터의 `description`을 기준으로 스킬을 판별하여 호출하기 때문에, 동일한 프롬프트라도 호출 여부가 확률적으로 결정됩니다
- 문서를 위에서부터 순차적으로 읽으므로 섹션 위치가 중요합니다

#### SKILL.md 작성 규약

**프론트매터 (필수):**

```yaml
---
name: git-commit-message
description: git diff를 분석하여 컨벤션에 맞는 커밋 메시지를 생성합니다. 개발자가 커밋 메시지 작성 또는 변경사항 검토를 요청할 때 사용합니다.
---
```

**필수 포함 요소:**
1. **무엇을 수행하는지** - 기능 설명
2. **언제 사용하는지** - 사용 시점/조건

#### 자동 호출(Invocation) 원리

Claude는 description을 의미 기반 매칭의 주요 신호로 활용하여 개발자의 요청과 가장 관련성이 높은 스킬을 선택합니다. 이는 단순한 키워드 검색이 아닌 **의미 기반 매칭(semantic matching)**을 사용합니다.

| 문제 유형 | 원인 | 결과 |
|---------|------|------|
| **오탐** | description이 너무 모호함 | 관련 없는 스킬이 호출됨 |
| **미탐** | description이 너무 좁음 | 필요할 때 호출되지 않음 |
| **충돌** | 여러 스킬이 유사한 description | 예측 불가능한 선택 |

#### description 작성 전략

- **구체적이면서도 넓게**: 특정 기능은 명확히, 사용 시점은 다양한 표현을 고려
- **중복 최소화**: 조직 내 다른 스킬과 중복되지 않도록 유니크한 키워드 사용
- **키워드 최적화**: 개발자가 실제로 사용할 검색어 포함

#### 스킬 충돌 관리

| 전략 | 설명 |
|------|------|
| **네이밍 컨벤션** | 조직/도메인 접두사 사용 (finance-report, hr-onboarding) |
| **키워드 분리** | 유사한 스킬 간 description 키워드 중복 최소화 |
| **범위 명확화** | 각 스킬의 고유 사용 시점을 명확히 구분 |

#### 파일 크기 기준

| 기준 | 라인 수 | 조치 |
|-----|--------|------|
| 권장 | 500줄 이하 | 유지 |
| 경고 | 500~800줄 | 분리 검토 |
| 필수 | 800줄 초과 | 즉시 분리 (운영 상한) |

---

### 1.3 훅

훅(Hook)은 Claude Code 라이프사이클의 특정 시점에서 실행되는 **개발자가 정의한 셸 명령어**입니다. 코드 포맷팅, 알림 전송, 명령어 검증, 프로젝트 규칙 강제 등의 작업을 자동화할 수 있습니다.

> Hook은 셸 명령어를 직접 호출하여 결정론적인 응답을 산출하므로, LLM 추론 비용이 발생하지 않습니다.

#### 주요 Hook 이벤트

| 이벤트 | 설명 |
|--------|------|
| `SessionStart` | 세션 시작/재개 시 |
| `UserPromptSubmit` | 프롬프트 제출 전 |
| `PreToolUse` | 도구 호출 실행 전 (차단 가능) |
| `PostToolUse` | 도구 성공 후 |
| `PostToolUseFailure` | 도구 실패 후 |
| `Stop` | Claude 응답 완료 시 |
| `PreCompact` | 컨텍스트 압축 전 |

#### 설정 예시

`.claude/settings.json` 파일에 Hook을 설정합니다:

```json
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "./scripts/security-check.sh",
            "async": false
          }
        ]
      }
    ]
  }
}
```

#### Hook 타입

| 타입 | 설명 | 사용 예시 |
|------|------|----------|
| `command` | 셸 명령어 실행 | 코드 검증, 포맷팅 |
| `http` | HTTP POST 요청 | 외부 알림, 웹훅 |
| `prompt` | 단일 턴 프롬프트 | 간단한 검증 |
| `agent` | 다중 턴 에이전트 | 복잡한 검증 작업 |

#### Exit Code 동작

| Exit Code | 동작 |
|-----------|------|
| **0** | 작업 진행 (stdout은 Claude 컨텍스트에 추가) |
| **2** | 작업 차단 (stderr는 Claude에게 피드백) |
| **기타** | 작업 진행 (stderr는 로그에만 기록) |

차단 가능한 이벤트: `PreToolUse`, `PermissionRequest`, `UserPromptSubmit`, `Stop`

#### 설정 위치

| 위치 | 범위 | 공유 가능 |
|------|------|----------|
| `~/.claude/settings.json` | 모든 프로젝트 | 아니오 |
| `.claude/settings.json` | 단일 프로젝트 | 예 |

> 상세한 Hook 레퍼런스는 [claude-code-hooks.md](./claude-code-hooks.md)를 참조하세요.

---

### 1.4 문서 타입별 비교

지금까지 살펴 본 CLAUDE.md, 스킬, 훅을 포함한 주요 문서 타입의 차이점을 비교합니다.

| 타입 | 로딩 시점 | 적용 범위 | 메모리 로딩 | 주요 용도 |
|------|----------|----------|------------|----------|
| **CLAUDE.md** | 세션 시작 시 | 해당 디렉토리 및 하위 | Eager (전체 로드) | 프로젝트 전체 규칙 |
| **Rules** | 세션 시작 시 | 해당 디렉토리 및 하위 | Eager (전체 로드) | 모듈별 지침 |
| **스킬** | 호출 시 | 글로벌 (프로젝트 전체) | Lazy (프론트매터만) | 재사용 가능한 레시피 |
| **Agents** | 호출 시 | 태스크 단위 | Lazy (호출 시 전체) | 특정 작업 전문가 |

#### 우선순위 체인 (충돌 시)

```
Global Config (~/.claude/)
    ↓
Root CLAUDE.md
    ↓
Module CLAUDE.md / Rules
    ↓
Skills (호출 시)
    ↓
Agents (호출 시)
```

**규칙**: 더 구체적인 것이 우선 (Module > Root > Global)

#### 사용 시점 가이드

| 목적 | 추천 문서 |
|------|----------|
| 프로젝트 공통 규칙 | CLAUDE.md |
| 모듈별 특화 규칙 | Rules |
| 재사용 레시피 | 스킬 |
| 전문 작업 수행 | Agents |

**핵심 원칙**: Eager(CLAUDE.md/Rules)는 최소화, Lazy(Skills/Agents)는 적극 활용

> CLAUDE.md 로딩 전략에 대한 상세 내용은 [claude-code-memory-loading.md](./claude-code-memory-loading.md)를 참조하세요.

---

## 2. Agent 시스템 개요

### 2.1 단일 Agent vs Agent Team

#### 단일 Agent의 문제점

- **Context Window 제한**: 복잡한 작업 시 컨텍스트 초과
- **피드백 루프 부재**: 자기 검증 메커니즘 없음
- **검증 메커니즘 부족**: 외부 검토 없이 결과 산출

#### Agent Team 구조

Agent Team은 여러 전문 Agent가 협업하여 작업을 수행하는 구조입니다.

- **Blue Team**: 설계 및 구현 역할
  - Architect: 전체 설계 및 구조 결정
  - Developer: 코드 작성 및 구현

- **Red Team**: 검증 및 공격 역할
  - Reviewer: 코드 리뷰 및 품질 검증
  - Critic: 공격적 검증 및 가정 발굴

> Agent Team의 상세한 설계 및 실행 방법은 [agent-fundamentals.md](./agent-fundamentals.md)와 [agent-team-practice.md](./agent-team-practice.md)를 참조하세요.

---

### 2.2 Feedback Loop

#### Feedback Loop 개념

Feedback Loop는 지속적인 개선을 위한 순환 구조입니다:

1. **Blue Team**: 초안 작성 및 구현
2. **Red Team**: 검증 및 피드백 제공
3. **Blue Team**: 피드백 반영하여 개선
4. 반복

#### Agent Debate Pattern

Agent Debate Pattern은 여러 Agent가 토론을 통해 최적의 결과를 도출하는 패턴입니다.

```
설계(Architect) ↔ 구현(Developer) ↔ 검토(Reviewer)
         ↑___________________________↓
```

- 각 Agent는 고유한 관점에서 문제를 바라봅니다
- 토론을 통해 숨겨진 가정을 발굴하고 오류를 수정합니다
- 최종적으로 합의된 결과를 산출합니다

> Blue/Red Team 구조와 비용 분석은 [agent-team-practice.md](./agent-team-practice.md)를 참조하세요.

---

## 3. 실전 활용 가이드

이제 이러한 개념들을 실제로 어떻게 활용할 수 있는지 살펴 보겠습니다.

### 3.1 오케스트레이션

#### AI Harness

AI Harness는 AI Model의 특성을 고려한 제어 구조입니다.

**결정론적 영역**은 예측 가능하고 반복 가능한 작업을 다룹니다. 이러한 영역은 훅이나 셸 스크립트를 통해 제어하여 일관된 결과를 보장해야 합니다. 예를 들어 코드 포맷팅, 테스트 실행, 빌드 과정 등이 이에 해당합니다.

**비결정론적 영역**은 창의적이고 확률적인 작업을 다룹니다. 이러한 영역은 Agent나 LLM 추론을 활용하되, 반드시 검증 메커니즘을 함께 사용해야 합니다. 예를 들어 설계 결정, 코드 리뷰, 아키텍처 검토 등이 이에 해당합니다.

**원칙**:
- 결정론적 영역은 반드시 스크립트/훅으로 제어
- 비결정론적 영역은 Agent에 위임하되 검증 메커니즘 필수

#### 업무 자동화

Claude Code를 활용한 업무 자동화 패턴:

- **반복 작업 자동화**: Skills로 표준화
- **워크플로우 실행**: 문서화된 절차를 Agent가 수행
- **품질 검증**: Red Team 패턴으로 자동 검증

---

### 3.2 Git Worktree

오케스트레이션의 한 예시로, Git Worktree를 활용한 병렬 작업 방법을 살펴 보겠습니다.

#### 기본 개념

Git Worktree는 하나의 리포지토리에서 여러 브랜치를 동시에 체크아웃하는 기능입니다.

```bash
# 새 worktree 생성
git worktree add ../feature-branch feature-branch

# worktree 목록 확인
git worktree list
```

#### Claude Code 연계

Git Worktree를 Claude Code와 함께 사용하면:

- **멀티 태스킹**: 여러 작업을 동시에 진행
- **병렬 Agent 실행**: 각 worktree에서 독립적인 Agent 실행
- **격리된 환경**: 작업 간 간섭 방지

> Git Worktree와 Agent Team의 연계에 대한 상세 내용은 [agent-team-practice.md](./agent-team-practice.md)를 참조하세요.

---

### 3.3 문서 관리 방법

#### 파일 크기 기준

파일 크기에 따른 관리 기준은 [1.2절 Skills의 파일 크기 기준](#12-skills)을 참조하세요.

#### Lost in the Middle 현상

큰 컨텍스트에서 중간 부분의 정보가 압축되거나 무시되는 현상입니다.

**대응 전략**:
- **핵심 정보 상단 배치**: 문서 상단 20%에 핵심 내용 배치
- **800줄 분리 원칙**: 800줄 이상 시 반드시 문서 분리
- **TOC 활용**: 문서 구조를 명확히 표시하여 탐색 용이

#### 문서 분리 전략

```
큰 문서.md (1000줄)
    ↓ 분리
개요.md (200줄) - 핵심 개념만
상세-1.md (300줄) - 주제별 분리
상세-2.md (300줄) - 주제별 분리
참조.md (200줄) - 부록
```

---

## 4. 오해와 진실

### 4.1 "지침에 적으면 무조건 실행?" → X

Claude Code는 지침을 "참고"하지만 "무조건" 따르지는 않습니다.

**이유**:
- **프롬프트 우선**: 개발자의 실시간 프롬프트가 지침보다 우선
- **컨텍스트 한계**: 지침이 compact되면서 유실될 수 있음
- **의도 해석**: 지침의 의도를 상황에 따라 재해석

**해결책**:
- 중요한 규칙은 훅으로 강제 (결정론적 실행)
- 지침은 "가이드"로 활용, 강제가 아닌 권장

---

### 4.2 "프롬프트만 잘 작성하면?" → X

좋은 프롬프트만으로는 충분하지 않습니다.

**이유**:
- **반복적 작업**: 매번 프롬프트를 작성하는 것은 비효율적
- **일관성**: 사람이 프롬프트를 작성하면 일관성이 떨어짐
- **컨텍스트 한계**: 긴 프롬프트는 토큰을 많이 사용

**해결책**:
- **Skills 활용**: 반복 작업은 Skills로 표준화
- **Agent 활용**: 복잡한 작업은 Agent에 위임
- **문서화**: 지침을 문서로 관리하여 지속적 참조

---

### 4.3 문서 길이 권장사항

**짧을수록 좋습니다**.

| 권장사항 | 설명 |
|---------|------|
| **500줄 이하** | 이상적인 문서 크기 |
| **800줄 이상** | 반드시 분리 필요 |
| **핵심 정보 상단** | 문서 상단 20%에 핵심 배치 |
| **TOC 필수** | 2단계 이상 헤더 구조 |

**이유**:
- **컨텍스트 효율**: 짧은 문서는 로딩 및 처리가 빠름
- **Lost in the Middle 방지**: 중요 정보가 묻히지 않음
- **유지보수**: 짧은 문서는 수정 및 검토가 쉬움

---

## 참고 문서

- [claude-code-memory-loading.md](./claude-code-memory-loading.md) - CLAUDE.md 로딩 전략
- [claude-code-hooks.md](./claude-code-hooks.md) - 훅 상세 레퍼런스
- [agent-fundamentals.md](./agent-fundamentals.md) - Agent 설계 가이드
- [agent-team-practice.md](./agent-team-practice.md) - Agent Team 실전 활용
- [claude-code-agent-skills.md](./claude-code-agent-skills.md) - Skills 상세 작성법

---

*생성일: 2026-03-23*
*통합 문서: essentials.md + onbording.md + standard-guid.md*

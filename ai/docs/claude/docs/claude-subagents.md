# 사용자 정의 subagent 만들기

> Claude Code에서 작업별 워크플로우 및 향상된 컨텍스트 관리를 위한 특화된 AI subagent를 만들고 사용합니다.

Subagent는 특정 유형의 작업을 처리하는 특화된 AI 어시스턴트입니다. 각 subagent는 자체 컨텍스트 윈도우에서 실행되며 사용자 정의 시스템 프롬프트, 특정 도구 액세스 및 독립적인 권한을 가집니다. Claude가 subagent의 설명과 일치하는 작업을 만나면 해당 subagent에 위임하고, subagent는 독립적으로 작동하여 결과를 반환합니다.

> **참고:** 여러 에이전트가 병렬로 작동하고 서로 통신해야 하는 경우 [agent teams](/ko/agent-teams)를 참조하세요. Subagent는 단일 세션 내에서 작동하고, agent team은 별도 세션 간에 조정합니다.

Subagent는 다음을 도와줍니다:

* **컨텍스트 보존** - 탐색 및 구현을 주 대화에서 분리하여 유지
* **제약 조건 적용** - subagent가 사용할 수 있는 도구 제한
* **구성 재사용** - 사용자 수준 subagent를 통해 프로젝트 간 구성 재사용
* **동작 특화** - 특정 도메인을 위한 집중된 시스템 프롬프트
* **비용 제어** - Haiku와 같은 더 빠르고 저렴한 모델로 작업 라우팅

Claude는 각 subagent의 설명을 사용하여 작업을 위임할 시기를 결정합니다. Subagent를 만들 때 Claude가 언제 사용할지 알 수 있도록 명확한 설명을 작성하세요.

Claude Code에는 **Explore**, **Plan**, **general-purpose**와 같은 여러 내장 subagent가 포함되어 있습니다. 특정 작업을 처리하기 위해 사용자 정의 subagent를 만들 수도 있습니다. 이 페이지에서는 [내장 subagent](#내장-subagent), [자신의 subagent를 만드는 방법](#빠른-시작-첫-번째-subagent-만들기), [전체 구성 옵션](#subagent-구성), [subagent 작업 패턴](#subagent-작업), [예제 subagent](#예제-subagent)를 다룹니다.

## 내장 subagent

Claude Code에는 Claude가 적절할 때 자동으로 사용하는 내장 subagent가 포함되어 있습니다. 각각은 추가 도구 제한이 있는 부모 대화의 권한을 상속합니다.

### Explore

코드베이스 검색 및 분석에 최적화된 빠른 읽기 전용 에이전트입니다.

* **모델**: Haiku (빠름, 낮은 지연시간)
* **도구**: 읽기 전용 도구 (Write 및 Edit 도구에 대한 액세스 거부)
* **목적**: 파일 검색, 코드 검색, 코드베이스 탐색

Claude는 변경 없이 코드베이스를 검색하거나 이해해야 할 때 Explore에 위임합니다. 이렇게 하면 탐색 결과가 주 대화 컨텍스트에서 벗어납니다.

Explore를 호출할 때 Claude는 철저함 수준을 지정합니다: 대상 조회의 경우 **quick**, 균형 잡힌 탐색의 경우 **medium**, 포괄적인 분석의 경우 **very thorough**.

### Plan

[plan mode](/ko/common-workflows#use-plan-mode-for-safe-code-analysis) 중에 계획을 제시하기 전에 컨텍스트를 수집하는 데 사용되는 연구 에이전트입니다.

* **모델**: 주 대화에서 상속
* **도구**: 읽기 전용 도구 (Write 및 Edit 도구에 대한 액세스 거부)
* **목적**: 계획을 위한 코드베이스 연구

plan mode에 있고 Claude가 코드베이스를 이해해야 할 때 연구를 Plan subagent에 위임합니다. 이렇게 하면 무한 중첩을 방지하면서(subagent는 다른 subagent를 생성할 수 없음) 필요한 컨텍스트를 수집합니다.

### General-purpose

탐색과 작업 모두를 필요로 하는 복잡한 다단계 작업을 위한 유능한 에이전트입니다.

* **모델**: 주 대화에서 상속
* **도구**: 모든 도구
* **목적**: 복잡한 연구, 다단계 작업, 코드 수정

Claude는 작업이 탐색과 수정 모두를 필요로 하거나, 결과를 해석하기 위한 복잡한 추론이 필요하거나, 여러 종속 단계가 필요할 때 general-purpose에 위임합니다.

### Other

Claude Code에는 특정 작업을 위한 추가 도우미 에이전트가 포함되어 있습니다. 이들은 일반적으로 자동으로 호출되므로 직접 사용할 필요가 없습니다.

| 에이전트 | 모델 | Claude가 사용하는 경우 |
| :---------------- | :----- | :-------------------------------- |
| Bash | 상속 | 별도 컨텍스트에서 터미널 명령 실행 |
| statusline-setup | Sonnet | `/statusline`을 실행하여 상태 표시줄을 구성할 때 |
| Claude Code Guide | Haiku | Claude Code 기능에 대한 질문을 할 때 |

이러한 내장 subagent 외에도 사용자 정의 프롬프트, 도구 제한, 권한 모드, hooks 및 skills를 사용하여 자신의 subagent를 만들 수 있습니다.

## 빠른 시작: 첫 번째 subagent 만들기

Subagent는 YAML frontmatter가 있는 Markdown 파일로 정의됩니다. [수동으로 만들거나](#subagent-파일-작성) `/agents` 명령을 사용할 수 있습니다.

이 연습에서는 `/agents` 명령을 사용하여 사용자 수준 subagent를 만드는 과정을 안내합니다. Subagent는 코드를 검토하고 코드베이스에 대한 개선 사항을 제안합니다.

1. **subagent 인터페이스 열기**

   Claude Code에서 다음을 실행합니다:

   ```text
   /agents
   ```

2. **위치 선택**

   **Create new agent**를 선택한 다음 **Personal**을 선택합니다. 이렇게 하면 subagent가 `~/.claude/agents/`에 저장되어 모든 프로젝트에서 사용할 수 있습니다.

3. **Claude로 생성**

   **Generate with Claude**를 선택합니다. 메시지가 표시되면 subagent를 설명합니다:

   ```text
   A code improvement agent that scans files and suggests improvements
   for readability, performance, and best practices. It should explain
   each issue, show the current code, and provide an improved version.
   ```

   Claude가 식별자, 설명 및 시스템 프롬프트를 생성합니다.

4. **도구 선택**

   읽기 전용 검토자의 경우 **Read-only tools**를 제외한 모든 항목을 선택 해제합니다. 모든 도구를 선택한 상태로 유지하면 subagent는 주 대화에서 사용 가능한 모든 도구를 상속합니다.

5. **모델 선택**

   Subagent가 사용할 모델을 선택합니다. 이 예제 에이전트의 경우 코드 패턴 분석을 위해 기능과 속도의 균형을 맞추는 **Sonnet**을 선택합니다.

6. **색상 선택**

   Subagent의 배경색을 선택합니다. 이렇게 하면 UI에서 어느 subagent가 실행 중인지 식별하는 데 도움이 됩니다.

7. **메모리 구성**

   **User scope**를 선택하여 subagent에 `~/.claude/agent-memory/`에서 [지속적 메모리 디렉토리](#지속적-메모리-활성화)를 제공합니다. Subagent는 이를 사용하여 코드베이스 패턴 및 반복되는 문제와 같은 대화 간 통찰력을 축적합니다. Subagent가 학습을 유지하지 않으려면 **None**을 선택합니다.

8. **저장 및 시도**

   구성 요약을 검토합니다. `s` 또는 `Enter`를 눌러 저장하거나 `e`를 눌러 편집기에서 저장 및 편집합니다. Subagent는 즉시 사용 가능합니다. 시도해 봅니다:

   ```text
   Use the code-improver agent to suggest improvements in this project
   ```

   Claude가 새 subagent에 위임하고, subagent가 코드베이스를 스캔하여 개선 제안을 반환합니다.

이제 머신의 모든 프로젝트에서 코드베이스를 분석하고 개선 사항을 제안하는 데 사용할 수 있는 subagent가 있습니다.

Markdown 파일로 subagent를 수동으로 만들거나, CLI 플래그를 통해 정의하거나, 플러그인을 통해 배포할 수도 있습니다.

## Subagent 구성

### /agents 명령 사용

`/agents` 명령은 subagent를 관리하기 위한 대화형 인터페이스를 제공합니다. `/agents`를 실행하여:

* 사용 가능한 모든 subagent 보기 (내장, 사용자, 프로젝트, 플러그인)
* 안낸된 설정 또는 Claude 생성으로 새 subagent 만들기
* 기존 subagent 구성 및 도구 액세스 편집
* 사용자 정의 subagent 삭제
* 중복이 있을 때 활성 subagent 확인

이것이 subagent를 만들고 관리하는 권장 방법입니다. 수동 생성 또는 자동화의 경우 subagent 파일을 직접 추가할 수도 있습니다.

대화형 세션을 시작하지 않고 명령줄에서 구성된 모든 subagent를 나열하려면 `claude agents`를 실행합니다. 이렇게 하면 소스별로 그룹화된 에이전트가 표시되고 더 높은 우선순위 정의로 재정의되는 에이전트가 표시됩니다.

### Subagent 범위 선택

Subagent는 YAML frontmatter가 있는 Markdown 파일입니다. 범위에 따라 다른 위치에 저장합니다. 여러 subagent가 같은 이름을 공유할 때 더 높은 우선순위 위치가 우선합니다.

| 위치 | 범위 | 우선순위 | 만드는 방법 |
| :------------------- | :------------ | :----- | :------------------------- |
| `--agents` CLI 플래그 | 현재 세션 | 1 (최고) | Claude Code 시작 시 JSON 전달 |
| `.claude/agents/` | 현재 프로젝트 | 2 | 대화형 또는 수동 |
| `~/.claude/agents/` | 모든 프로젝트 | 3 | 대화형 또는 수동 |
| 플러그인의 `agents/` 디렉토리 | 플러그인이 활성화된 위치 | 4 (최저) | [플러그인](/ko/plugins)과 함께 설치 |

**프로젝트 subagent** (`.claude/agents/`)는 코드베이스에 특정한 subagent에 이상적입니다. 버전 제어에 체크인하여 팀이 협력하여 사용하고 개선할 수 있습니다.

**사용자 subagent** (`~/.claude/agents/`)는 모든 프로젝트에서 사용 가능한 개인 subagent입니다.

**CLI 정의 subagent**는 Claude Code를 시작할 때 JSON으로 전달됩니다. 해당 세션에만 존재하며 디스크에 저장되지 않으므로 빠른 테스트 또는 자동화 스크립트에 유용합니다. 단일 `--agents` 호출에서 여러 subagent를 정의할 수 있습니다:

```bash
claude --agents '{
  "code-reviewer": {
    "description": "Expert code reviewer. Use proactively after code changes.",
    "prompt": "You are a senior code reviewer. Focus on code quality, security, and best practices.",
    "tools": ["Read", "Grep", "Glob", "Bash"],
    "model": "sonnet"
  },
  "debugger": {
    "description": "Debugging specialist for errors and test failures.",
    "prompt": "You are an expert debugger. Analyze errors, identify root causes, and provide fixes."
  }
}'
```

`--agents` 플래그는 파일 기반 subagent와 동일한 [frontmatter](#지원되는-frontmatter-필드) 필드를 가진 JSON을 허용합니다: `description`, `prompt`, `tools`, `disallowedTools`, `model`, `permissionMode`, `mcpServers`, `hooks`, `maxTurns`, `skills`, `initialPrompt`, `memory`, `effort`, `background`, `isolation`. 시스템 프롬프트에는 `prompt`를 사용하며, 이는 파일 기반 subagent의 markdown 본문과 동등합니다.

**플러그인 subagent**는 설치한 [플러그인](/ko/plugins)에서 제공됩니다. `/agents`에서 사용자 정의 subagent와 함께 나타납니다.

> **참고:** 보안상의 이유로 플러그인 subagent는 `hooks`, `mcpServers`, `permissionMode` frontmatter 필드를 지원하지 않습니다. 이러한 필드는 플러그인에서 에이전트를 로드할 때 무시됩니다. 필요한 경우 에이전트 파일을 `.claude/agents/` 또는 `~/.claude/agents/`로 복사합니다. `settings.json` 또는 `settings.local.json`의 [`permissions.allow`](/ko/settings#permission-settings)에 규칙을 추가할 수도 있지만, 이러한 규칙은 전체 세션에 적용되며 플러그인 subagent에만 적용되지 않습니다.

### Subagent 파일 작성

Subagent 파일은 구성을 위한 YAML frontmatter를 사용하고 그 뒤에 Markdown의 시스템 프롬프트가 옵니다:

> **참고:** Subagent는 세션 시작 시 로드됩니다. 파일을 수동으로 추가하여 subagent를 만드는 경우 세션을 다시 시작하거나 `/agents`를 사용하여 즉시 로드합니다.

```markdown
---
name: code-reviewer
description: Reviews code for quality and best practices
tools: Read, Glob, Grep
model: sonnet
---

You are a code reviewer. When invoked, analyze the code and provide
specific, actionable feedback on quality, security, and best practices.
```

Frontmatter는 subagent의 메타데이터와 구성을 정의합니다. 본문은 subagent의 동작을 안내하는 시스템 프롬프트가 됩니다. Subagent는 이 시스템 프롬프트만 받습니다(작업 디렉토리와 같은 기본 환경 세부 정보 포함). 전체 Claude Code 시스템 프롬프트는 받지 않습니다.

#### 지원되는 frontmatter 필드

다음 필드를 YAML frontmatter에서 사용할 수 있습니다. `name`과 `description`만 필수입니다.

| 필드 | 필수 | 설명 |
| :---------------- | :-- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `name` | 예 | 소문자 및 하이픈을 사용한 고유 식별자 |
| `description` | 예 | Claude가 이 subagent에 위임해야 할 때 |
| `tools` | 아니오 | Subagent가 사용할 수 있는 [도구](#사용-가능한-도구). 생략하면 모든 도구 상속 |
| `disallowedTools` | 아니오 | 거부할 도구, 상속되거나 지정된 목록에서 제거됨 |
| `model` | 아니오 | 사용할 [모델](#모델-선택): `sonnet`, `opus`, `haiku`, 전체 모델 ID (예: `claude-opus-4-1-20250805`), 또는 `inherit`. 기본값: `inherit` |
| `permissionMode` | 아니오 | [권한 모드](#권한-모드): `default`, `acceptEdits`, `dontAsk`, `bypassPermissions`, 또는 `plan` |
| `maxTurns` | 아니오 | Subagent가 중지되기 전의 최대 에이전트 턴 수 |
| `skills` | 아니오 | 시작 시 subagent의 컨텍스트에 로드할 [skills](/ko/skills). 호출 가능하게 만들어지는 것이 아니라 전체 skill 콘텐츠가 주입됩니다. Subagent는 부모 대화에서 skills를 상속하지 않습니다 |
| `mcpServers` | 아니오 | 이 subagent에서 사용 가능한 [MCP servers](/ko/mcp). 각 항목은 이미 구성된 서버를 참조하는 서버 이름 (예: `"slack"`) 또는 서버 이름을 키로 하고 전체 [MCP server config](/ko/mcp#configure-mcp-servers)를 값으로 하는 인라인 정의입니다 |
| `hooks` | 아니오 | 이 subagent로 범위가 지정된 [라이프사이클 hooks](#subagent에-대한-hook-정의) |
| `memory` | 아니오 | [지속적 메모리 범위](#지속적-메모리-활성화): `user`, `project`, 또는 `local`. 교차 세션 학습 활성화 |
| `background` | 아니오 | 이 subagent를 항상 [background task](#subagent를-foreground-또는-background에서-실행)로 실행하려면 `true`로 설정합니다. 기본값: `false` |
| `effort` | 아니오 | 이 subagent가 활성화될 때의 노력 수준. 세션 노력 수준을 재정의합니다. 기본값: 세션에서 상속. 옵션: `low`, `medium`, `high`, `max` (Opus 4.6만 해당) |
| `isolation` | 아니오 | Subagent를 임시 [git worktree](/ko/common-workflows#run-parallel-claude-code-sessions-with-git-worktrees)에서 실행하려면 `worktree`로 설정하여 저장소의 격리된 복사본을 제공합니다. Subagent가 변경 사항을 만들지 않으면 worktree가 자동으로 정리됩니다 |
| `initialPrompt` | 아니오 | 이 에이전트가 주 세션 에이전트로 실행될 때 (`--agent` 또는 `agent` 설정을 통해) 첫 번째 사용자 턴으로 자동 제출됩니다. [Commands](/ko/commands) 및 [skills](/ko/skills)가 처리됩니다. 사용자 제공 프롬프트에 앞에 붙입니다 |

### 모델 선택

`model` 필드는 subagent가 사용하는 [AI 모델](/ko/model-config)을 제어합니다:

* **모델 별칭**: 사용 가능한 별칭 중 하나를 사용합니다: `sonnet`, `opus`, 또는 `haiku`
* **전체 모델 ID**: `claude-opus-4-1-20250805` 또는 `claude-sonnet-4-1-20250514`와 같은 전체 모델 ID를 사용합니다. `--model` 플래그와 동일한 값을 허용합니다
* **inherit**: 주 대화와 동일한 모델을 사용합니다
* **생략됨**: 지정하지 않으면 기본값은 `inherit`입니다 (주 대화와 동일한 모델 사용)

Claude가 subagent를 호출할 때 해당 특정 호출에 대해 `model` 매개변수를 전달할 수도 있습니다. Claude Code는 다음 순서로 subagent의 모델을 해결합니다:

1. [`CLAUDE_CODE_SUBAGENT_MODEL`](/ko/model-config#environment-variables) 환경 변수 (설정된 경우)
2. 호출별 `model` 매개변수
3. Subagent 정의의 `model` frontmatter
4. 주 대화의 모델

### Subagent 기능 제어

도구 액세스, 권한 모드 및 조걶부 규칙을 통해 subagent가 할 수 있는 작업을 제어할 수 있습니다.

#### 사용 가능한 도구

Subagent는 Claude Code의 [난부 도구](/ko/tools-reference) 중 하나를 사용할 수 있습니다. 기본적으로 subagent는 MCP 도구를 포함하여 주 대화의 모든 도구를 상속합니다.

도구를 제한하려면 `tools` 필드 (허용 목록) 또는 `disallowedTools` 필드 (거부 목록)를 사용합니다. 이 예제는 `tools`를 사용하여 Read, Grep, Glob, Bash만 허용합니다. Subagent는 파일을 편집하거나 쓸 수 없으며 MCP 도구를 사용할 수 없습니다:

```yaml
---
name: safe-researcher
description: Research agent with restricted capabilities
tools: Read, Grep, Glob, Bash
---
```

이 예제는 `disallowedTools`를 사용하여 주 대화에서 상속된 모든 도구를 상속하지만 Write 및 Edit은 제외합니다. Subagent는 Bash, MCP 도구 및 다른 모든 것을 유지합니다:

```yaml
---
name: no-writes
description: Inherits every tool except file writes
disallowedTools: Write, Edit
---
```

둘 다 설정되면 `disallowedTools`가 먼저 적용되고 `tools`가 남은 풀에 대해 해결됩니다. 둘 다에 나엶된 도구는 제거됩니다.

#### 생성할 수 있는 subagent 제한

에이전트가 `claude --agent`를 사용하여 주 스레드로 실행될 때 Agent 도구를 사용하여 subagent를 생성할 수 있습니다. 생성할 수 있는 subagent 유형을 제한하려면 `tools` 필드에서 `Agent(agent_type)` 구문을 사용합니다.

> 버전 2.1.63에서 Task 도구의 이름이 Agent로 변경되었습니다. 설정 및 에이전트 정의의 기존 `Task(...)` 참조는 여전히 별칭으로 작동합니다.

```yaml
---
name: coordinator
description: Coordinates work across specialized agents
tools: Agent(worker, researcher), Read, Bash
---
```

이것은 허용 목록입니다: `worker` 및 `researcher` subagent만 생성할 수 있습니다. 에이전트가 다른 유형을 생성하려고 하면 요청이 실패하고 에이전트는 프롬프트에서 허용된 유형만 봅니다. 다른 모든 에이전트를 허용하면서 특정 에이전트를 차단하려면 [`permissions.deny`](#특정-subagent-비활성화)를 대신 사용합니다.

제한 없이 모든 subagent를 생성할 수 있도록 허용하려면 괄호 없이 `Agent`를 사용합니다:

```yaml
tools: Agent, Read, Bash
```

`Agent`가 `tools` 목록에서 완전히 생략되면 에이전트는 subagent를 생성할 수 없습니다. 이 제한은 `claude --agent`를 사용하여 주 스레드로 실행되는 에이전트에만 적용됩니다. Subagent는 다른 subagent를 생성할 수 없으므로 `Agent(agent_type)`은 subagent 정의에서 효과가 없습니다.

#### Subagent에 MCP 서버 범위 지정

`mcpServers` 필드를 사용하여 주 대화에서 사용할 수 없는 [MCP](/ko/mcp) 서버에 subagent 액세스 권한을 부여합니다. 여기에 정의된 인라인 서버는 subagent가 시작될 때 연결되고 완료될 때 연결이 끊깁니다. 문자열 참조는 부모 세션의 연결을 공유합니다.

목록의 각 항목은 인라인 서버 정의 또는 세션에서 이미 구성된 MCP 서버를 참조하는 문자열입니다:

```yaml
---
name: browser-tester
description: Tests features in a real browser using Playwright
mcpServers:
  # Inline definition: scoped to this subagent only
  - playwright:
      type: stdio
      command: npx
      args: ["-y", "@playwright/mcp@latest"]
  # Reference by name: reuses an already-configured server
  - github
---

Use the Playwright tools to navigate, screenshot, and interact with pages.
```

인라인 정의는 `.mcp.json` 서버 항목 (`stdio`, `http`, `sse`, `ws`)과 동일한 스키마를 사용하며 서버 이름으로 키가 지정됩니다.

MCP 서버를 주 대화에서 완전히 분리하고 도구 설명이 컨텍스트를 소비하지 않도록 하려면 `.mcp.json`이 아닌 여기에 인라인으로 정의합니다. Subagent는 도구를 얻고 부모 대화는 그렇지 않습니다.

#### 권한 모드

`permissionMode` 필드는 subagent가 권한 프롬프트를 처리하는 방식을 제어합니다. Subagent는 주 대화의 권한 컨텍스트를 상속하고 모드를 재정의할 수 있습니다. 단, 아래에 설명된 대로 부모 모드가 우선하는 경우는 제외합니다.

| 모드 | 동작 |
| :------------------ | :----------------------------------- |
| `default` | 프롬프트를 사용한 표준 권한 확인 |
| `acceptEdits` | 파일 편집 자동 수락 |
| `dontAsk` | 권한 프롬프트 자동 거부 (명시적으로 허용된 도구는 여전히 작동) |
| `bypassPermissions` | 권한 프롬프트 걸뛰기 |
| `plan` | Plan mode (읽기 전용 탐색) |

> **경고:** `bypassPermissions`는 주의해서 사용하세요. 권한 프롬프트를 걸뛰어 subagent가 승인 없이 작업을 실행할 수 있습니다. `.git`, `.claude`, `.vscode`, `.idea` 디렉토리에 대한 쓰기는 여전히 확인을 요청합니다. `.claude/commands`, `.claude/agents`, `.claude/skills`는 제외합니다.

부모가 `bypassPermissions`를 사용하면 이것이 우선하며 재정의할 수 없습니다. 부모가 [auto mode](/ko/permission-modes#eliminate-prompts-with-auto-mode)를 사용하면 subagent는 auto mode를 상속하고 frontmatter의 모든 `permissionMode`는 무시됩니다: 분류기는 부모 세션과 동일한 차단 및 허용 규칙으로 subagent의 도구 호출을 평가합니다.

#### Subagent에 skills 미리 로드

`skills` 필드를 사용하여 시작 시 subagent의 컨텍스트에 skill 콘텐츠를 주입합니다. 이렇게 하면 실행 중에 skill을 검색하고 로드하도록 요구하지 않고 subagent에 도메인 지식을 제공합니다.

```yaml
---
name: api-developer
description: Implement API endpoints following team conventions
skills:
  - api-conventions
  - error-handling-patterns
---

Implement API endpoints. Follow the conventions and patterns from the preloaded skills.
```

각 skill의 전체 콘텐츠가 subagent의 컨텍스트에 주입되며, 호출 가능하게 만들어지는 것이 아닙니다. Subagent는 부모 대화에서 skills를 상속하지 않으므로 명시적으로 나열해야 합니다.

> 이것은 [subagent에서 skill 실행](/ko/skills#run-skills-in-a-subagent)의 역입니다. Subagent의 `skills`를 사용하면 subagent가 시스템 프롬프트를 제어하고 skill 콘텐츠를 로드합니다. Skill의 `context: fork`를 사용하면 skill 콘텐츠가 지정한 에이전트에 주입됩니다. 둘 다 동일한 기본 시스템을 사용합니다.

#### 지속적 메모리 활성화

`memory` 필드는 subagent에 대화 간에 유지되는 지속적 디렉토리를 제공합니다. Subagent는 이 디렉토리를 사용하여 코드베이스 패턴, 디버깅 통찰력, 아키텍처 결정과 같은 지식을 시간에 따라 구축합니다.

```yaml
---
name: code-reviewer
description: Reviews code for quality and best practices
memory: user
---

You are a code reviewer. As you review code, update your agent memory with
patterns, conventions, and recurring issues you discover.
```

메모리가 얼마나 광범위하게 적용되어야 하는지에 따라 범위를 선택합니다:

| 범위 | 위치 | 사용 시기 |
| :-------- | :-------------------------------------------- | :------------------------------------------ |
| `user` | `~/.claude/agent-memory/<name-of-agent>/` | Subagent가 모든 프로젝트 간 학습을 기억해야 할 때 |
| `project` | `.claude/agent-memory/<name-of-agent>/` | Subagent의 지식이 프로젝트별이고 버전 제어를 통해 공유 가능할 때 |
| `local` | `.claude/agent-memory-local/<name-of-agent>/` | Subagent의 지식이 프로젝트별이지만 버전 제어에 체크인되지 않아야 할 때 |

메모리가 활성화되면:

* Subagent의 시스템 프롬프트에는 메모리 디렉토리 읽기 및 쓰기 지침이 포함됩니다.
* Subagent의 시스템 프롬프트에는 메모리 디렉토리의 `MEMORY.md`의 처음 200줄이 포함되며, 200줄을 초과하면 `MEMORY.md`를 큐레이션하도록 지침이 포함됩니다.
* Read, Write, Edit 도구가 자동으로 활성화되어 subagent가 메모리 파일을 관리할 수 있습니다.

##### 지속적 메모리 팁

* `project`는 권장되는 기본 범위입니다. 메모리를 버전 제어를 통해 공유 가능하게 만듭니다. Subagent의 지식이 모든 프로젝트에 광범위하게 적용될 때 `user`를 사용하거나, 지식이 버전 제어에 체크인되지 않아야 할 때 `local`을 사용합니다.
* Subagent에 작업을 시작하기 전에 메모리를 확인하도록 요청합니다: "Review this PR, and check your memory for patterns you've seen before."
* Subagent에 작업을 완료한 후 메모리를 업데이트하도록 요청합니다: "Now that you're done, save what you learned to your memory." 시간이 지남에 따라 이렇게 하면 subagent를 더 효과적으로 만드는 지식 기반이 구축됩니다.
* Subagent가 자신의 지식 기반을 적극적으로 유지하도록 메모리 지침을 subagent의 markdown 파일에 직접 포함합니다:

  ```markdown
  Update your agent memory as you discover codepaths, patterns, library
  locations, and key architectural decisions. This builds up institutional
  knowledge across conversations. Write concise notes about what you found
  and where.
  ```

#### Hook을 사용한 조걶부 규칙

도구 사용을 더 동적으로 제어하려면 `PreToolUse` hook을 사용하여 실행 전에 작업을 검증합니다. 도구의 일부 작업은 허용하면서 다른 작업은 차단해야 할 때 유용합니다.

이 예제는 읽기 전용 데이터베이스 쿼리만 허용하는 subagent를 만듭니다. `PreToolUse` hook은 각 Bash 명령이 실행되기 전에 `command`에 지정된 스크립트를 실행합니다:

```yaml
---
name: db-reader
description: Execute read-only database queries
tools: Bash
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: "./scripts/validate-readonly-query.sh"
---
```

Claude Code는 [hook 입력을 JSON으로](/ko/hooks#pretooluse-input) stdin을 통해 hook 명령에 전달합니다. 검증 스크립트는 이 JSON을 읽고 Bash 명령을 추출하며 쓰기 작업을 차단하기 위해 [종료 코드 2](/ko/hooks#exit-code-2-behavior-per-event)로 종료합니다:

```bash
#!/bin/bash
# ./scripts/validate-readonly-query.sh

INPUT=$(cat)
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

# Block SQL write operations (case-insensitive)
if echo "$COMMAND" | grep -iE '\b(INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|TRUNCATE)\b' > /dev/null; then
  echo "Blocked: Only SELECT queries are allowed" >&2
  exit 2
fi

exit 0
```

전체 입력 스키마는 [Hook input](/ko/hooks#pretooluse-input)을 참조하고 종료 코드가 동작에 미치는 영향은 [exit codes](/ko/hooks#exit-code-output)를 참조하세요.

#### 특정 subagent 비활성화

[설정](/ko/settings#permission-settings)의 `deny` 배열에 추가하여 Claude가 특정 subagent를 사용하지 못하도록 할 수 있습니다. `Agent(subagent-name)` 형식을 사용합니다. 여기서 `subagent-name`은 subagent의 name 필드와 일치합니다.

```json
{
  "permissions": {
    "deny": ["Agent(Explore)", "Agent(my-custom-agent)"]
  }
}
```

이것은 내장 및 사용자 정의 subagent 모두에 작동합니다. `--disallowedTools` CLI 플래그를 사용할 수도 있습니다:

```bash
claude --disallowedTools "Agent(Explore)"
```

### Subagent에 대한 hook 정의

Subagent는 subagent의 라이프사이클 중에 실행되는 [hooks](/ko/hooks)를 정의할 수 있습니다. Hook을 구성하는 두 가지 방법이 있습니다:

1. **Subagent의 frontmatter에서**: 해당 subagent가 활성화된 동안만 실행되는 hook 정의
2. **`settings.json`에서**: Subagent가 시작되거나 중지될 때 주 세션에서 실행되는 hook 정의

#### Subagent frontmatter의 hook

Subagent의 markdown 파일에 직접 hook을 정의합니다. 이러한 hook은 해당 특정 subagent가 활성화된 동안만 실행되고 완료될 때 정리됩니다.

모든 [hook 이벤트](/ko/hooks#hook-events)가 지원됩니다. Subagent에 가장 일반적인 이벤트는:

| 이벤트 | Matcher 입력 | 실행 시기 |
| :------------ | :--------- | :------------------------------------------ |
| `PreToolUse` | 도구 이름 | Subagent가 도구를 사용하기 전 |
| `PostToolUse` | 도구 이름 | Subagent가 도구를 사용한 후 |
| `Stop` | (없음) | Subagent가 완료될 때 (런타임에 `SubagentStop`으로 변환됨) |

이 예제는 `PreToolUse` hook으로 Bash 명령을 검증하고 `PostToolUse`로 파일 편집 후 linter를 실행합니다:

```yaml
---
name: code-reviewer
description: Review code changes with automatic linting
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: "./scripts/validate-command.sh $TOOL_INPUT"
  PostToolUse:
    - matcher: "Edit|Write"
      hooks:
        - type: command
          command: "./scripts/run-linter.sh"
---
```

Frontmatter의 `Stop` hook은 자동으로 `SubagentStop` 이벤트로 변환됩니다.

#### Subagent 이벤트에 대한 프로젝트 수준 hook

주 세션에서 subagent 라이프사이클 이벤트에 응답하는 `settings.json`에서 hook을 구성합니다.

| 이벤트 | Matcher 입력 | 실행 시기 |
| :-------------- | :--------- | :------------------ |
| `SubagentStart` | 에이전트 유형 이름 | Subagent가 실행을 시작할 때 |
| `SubagentStop` | 에이전트 유형 이름 | Subagent가 완료될 때 |

두 이벤트 모두 이름별로 특정 에이전트 유형을 대상으로 하는 matcher를 지원합니다. 이 예제는 `db-agent` subagent가 시작될 때만 설정 스크립트를 실행하고 모든 subagent가 중지될 때 정리 스크립트를 실행합니다:

```json
{
  "hooks": {
    "SubagentStart": [
      {
        "matcher": "db-agent",
        "hooks": [
          { "type": "command", "command": "./scripts/setup-db-connection.sh" }
        ]
      }
    ],
    "SubagentStop": [
      {
        "hooks": [
          { "type": "command", "command": "./scripts/cleanup-db-connection.sh" }
        ]
      }
    ]
  }
}
```

## Subagent 작업

### 자동 위임 이해

Claude는 요청의 작업 설명, subagent 구성의 `description` 필드, 현재 컨텍스트를 기반으로 자동으로 작업을 위임합니다. 적극적인 위임을 장려하려면 subagent의 description 필드에 "use proactively"와 같은 구문을 포함합니다.

### Subagent를 명시적으로 호출

자동 위임이 충분하지 않을 때 subagent를 직접 요청할 수 있습니다. 일회성 제안에서 세션 전체 기본값으로 확대되는 세 가지 패턴이 있습니다:

* **자연어**: 프롬프트에서 subagent 이름을 지정합니다. Claude가 위임할지 결정합니다
* **@-mention**: 한 작업에 대해 subagent가 실행되도록 보장합니다
* **세션 전체**: 전체 세션이 `--agent` 플래그 또는 `agent` 설정을 통해 해당 subagent의 시스템 프롬프트, 도구 제한 및 모델을 사용합니다

자연어의 경우 특별한 구문이 없습니다. Subagent 이름을 지정하면 Claude는 일반적으로 위임합니다:

```text
Use the test-runner subagent to fix failing tests
Have the code-reviewer subagent look at my recent changes
```

**Subagent를 @-mention합니다.** `@`를 입력하고 파일을 @-mention하는 것과 동일한 방식으로 typeahead에서 subagent를 선택합니다. 이렇게 하면 Claude가 선택하도록 하는 대신 특정 subagent가 실행되도록 보장합니다:

```text
@"code-reviewer (agent)" look at the auth changes
```

전체 메시지는 여전히 Claude로 이동하며, Claude는 요청한 내용을 기반으로 subagent의 작업 프롬프트를 작성합니다. @-mention은 Claude가 호출하는 subagent를 제어하며, 받는 프롬프트는 제어하지 않습니다.

활성화된 [플러그인](/ko/plugins)에서 제공하는 Subagent는 typeahead에 `<plugin-name>:<agent-name>`으로 나타납니다. 선택기를 사용하지 않고 수동으로 mention을 입력할 수도 있습니다: 로컬 subagent의 경우 `@agent-<name>`, 플러그인 subagent의 경우 `@agent-<plugin-name>:<agent-name>`.

**전체 세션을 subagent로 실행합니다.** [`--agent <name>`](/ko/cli-reference)을 전달하여 주 스레드 자체가 해당 subagent의 시스템 프롬프트, 도구 제한 및 모델을 취하는 세션을 시작합니다:

```bash
claude --agent code-reviewer
```

Subagent의 시스템 프롬프트는 [`--system-prompt`](/ko/cli-reference)와 동일한 방식으로 기본 Claude Code 시스템 프롬프트를 완전히 대체합니다. `CLAUDE.md` 파일 및 프로젝트 메모리는 여전히 일반적인 메시지 흐름을 통해 로드됩니다. 에이전트 이름은 시작 헤더에 `@<name>`으로 나타나므로 활성화되었는지 확인할 수 있습니다.

이것은 내장 및 사용자 정의 subagent에서 작동하며, 세션을 재개할 때 선택이 유지됩니다.

플러그인 제공 subagent의 경우 범위가 지정된 이름을 전달합니다: `claude --agent <plugin-name>:<agent-name>`.

프로젝트의 모든 세션에 대한 기본값으로 만들려면 `.claude/settings.json`에서 `agent`를 설정합니다:

```json
{
  "agent": "code-reviewer"
}
```

CLI 플래그가 둘 다 있으면 설정을 재정의합니다.

### Subagent를 foreground 또는 background에서 실행

Subagent는 foreground (차단) 또는 background (동시)에서 실행할 수 있습니다:

* **Foreground subagent**는 완료될 때까지 주 대화를 차단합니다. 권한 프롬프트 및 명확한 질문 (예: [`AskUserQuestion`](/ko/tools-reference))이 사용자에게 전달됩니다.
* **Background subagent**는 계속 작업하는 동안 동시에 실행됩니다. 시작하기 전에 Claude Code는 subagent가 필요로 할 도구 권한을 요청하여 필요한 승인이 있는지 확인합니다. 실행 중이면 subagent는 이러한 권한을 상속하고 사전 승인되지 않은 모든 항목을 자동으로 거부합니다. Background subagent가 명확한 질문을 해야 하면 해당 도구 호출이 실패하지만 subagent는 계속됩니다.

Background subagent가 권한 부족으로 인해 실패하면 foreground에서 새 foreground subagent를 시작하여 대화형 프롬프트로 다시 시도할 수 있습니다.

Claude는 작업을 기반으로 subagent를 foreground 또는 background에서 실행할지 결정합니다. 다음을 수행할 수도 있습니다:

* Claude에 "run this in the background"를 요청
* **Ctrl+B**를 눌러 실행 중인 작업을 background로 이동

모든 background 작업 기능을 비활성화하려면 `CLAUDE_CODE_DISABLE_BACKGROUND_TASKS` 환경 변수를 `1`로 설정합니다.

### 일반적인 패턴

#### 대량 작업 격리

Subagent의 가장 효과적인 사용 중 하나는 많은 양의 출력을 생성하는 작업을 격리하는 것입니다. 테스트 실행, 문서 가져오기 또는 로그 파일 처리는 상당한 컨텍스트를 소비할 수 있습니다. 이를 subagent에 위임하면 자세한 출력이 subagent의 컨텍스트에 유지되고 관련 요약만 주 대화로 반환됩니다.

```text
Use a subagent to run the test suite and report only the failing tests with their error messages
```

#### 병렬 연구 실행

독립적인 조사의 경우 여러 subagent를 생성하여 동시에 작동하도록 합니다:

```text
Research the authentication, database, and API modules in parallel using separate subagents
```

각 subagent는 자신의 영역을 독립적으로 탐색한 다음 Claude가 결과를 종합합니다. 이것은 연구 경로가 서로 의존하지 않을 때 가장 잘 작동합니다.

> **경고:** Subagent가 완료되면 결과가 주 대화로 반환됩니다. 각각 자세한 결과를 반환하는 많은 subagent를 실행하면 상당한 컨텍스트를 소비할 수 있습니다.

지속적인 병렬성이 필요하거나 컨텍스트 윈도우를 초과하는 작업의 경우 [agent teams](/ko/agent-teams)는 각 워커에게 자신의 독립적인 컨텍스트를 제공합니다.

#### Subagent 체인

다단계 워크플로우의 경우 Claude에 subagent를 순차적으로 사용하도록 요청합니다. 각 subagent는 작업을 완료하고 결과를 Claude에 반환하고, Claude는 관련 컨텍스트를 다음 subagent에 전달합니다.

```text
Use the code-reviewer subagent to find performance issues, then use the optimizer subagent to fix them
```

### Subagent와 주 대화 중 선택

**주 대화**를 사용하는 경우:

* 작업이 빈번한 왕복 또는 반복적인 개선이 필요한 경우
* 여러 단계가 상당한 컨텍스트를 공유하는 경우 (계획 → 구현 → 테스트)
* 빠르고 대상이 지정된 변경을 수행하는 경우
* 지연시간이 중요한 경우. Subagent는 새로 시작하고 컨텍스트를 수집하는 데 시간이 걸릴 수 있습니다

**Subagent**를 사용하는 경우:

* 작업이 주 컨텍스트에서 필요하지 않은 자세한 출력을 생성하는 경우
* 특정 도구 제한 또는 권한을 적용하려는 경우
* 작업이 자체 포함되어 있고 요약을 반환할 수 있는 경우

격리된 subagent 컨텍스트가 아닌 주 대화 컨텍스트에서 실행되는 재사용 가능한 프롬프트 또는 워크플로우를 원할 때 [Skills](/ko/skills)를 대신 고려합니다.

대화에 이미 있는 항목에 대한 빠른 질문의 경우 subagent 대신 [`/btw`](/ko/interactive-mode#side-questions-with-btw)를 사용합니다. 전체 컨텍스트를 보지만 도구 액세스가 없으며 답변은 기록에 추가되지 않습니다.

> **참고:** Subagent는 다른 subagent를 생성할 수 없습니다. 워크플로우가 중첩된 위임이 필요한 경우 [Skills](/ko/skills) 또는 주 대화에서 [subagent 체인](#subagent-체인)을 사용합니다.

### Subagent 컨텍스트 관리

#### Subagent 재개

각 subagent 호출은 새로운 인스턴스를 만들고 새로운 컨텍스트를 생성합니다. 처음부터 시작하는 대신 기존 subagent의 작업을 계속하려면 Claude에 재개하도록 요청합니다.

재개된 subagent는 모든 이전 도구 호출, 결과 및 추론을 포함한 전체 대화 기록을 유지합니다. Subagent는 새로 시작하는 대신 정확히 중단한 위치에서 계속됩니다.

Subagent가 완료되면 Claude는 에이전트 ID를 받습니다. Claude는 `SendMessage` 도구를 에이전트의 ID를 `to` 필드로 사용하여 재개합니다. Subagent를 재개하려면 Claude에 이전 작업을 계속하도록 요청합니다:

```text
Use the code-reviewer subagent to review the authentication module
[Agent completes]

Continue that code review and now analyze the authorization logic
[Claude resumes the subagent with full context from previous conversation]
```

에이전트 ID를 명시적으로 참조하려면 Claude에 ID를 요청할 수도 있으며, `~/.claude/projects/{project}/{sessionId}/subagents/`의 트랜스크립트 파일에서 ID를 찾을 수 있습니다. 각 트랜스크립트는 `agent-{agentId}.jsonl`로 저장됩니다.

Subagent 트랜스크립트는 주 대화와 독립적으로 유지됩니다:

* **주 대화 압축**: 주 대화가 압축될 때 subagent 트랜스크립트는 영향을 받지 않습니다. 별도 파일에 저장됩니다.
* **세션 지속성**: Subagent 트랜스크립트는 세션 내에서 유지됩니다. 동일한 세션을 재개하여 Claude Code를 다시 시작한 후 [subagent를 재개](#subagent-재개)할 수 있습니다.
* **자동 정리**: 트랜스크립트는 `cleanupPeriodDays` 설정 (기본값: 30일)을 기반으로 정리됩니다.

#### 자동 압축

Subagent는 주 대화와 동일한 논리를 사용하여 자동 압축을 지원합니다. 기본적으로 자동 압축은 약 95% 용량에서 트리거됩니다. 압축을 더 일찍 트리거하려면 `CLAUDE_AUTOCOMPACT_PCT_OVERRIDE`를 더 낮은 백분율 (예: `50`)로 설정합니다.

압축 이벤트는 subagent 트랜스크립트 파일에 기록됩니다:

```json
{
  "type": "system",
  "subtype": "compact_boundary",
  "compactMetadata": {
    "trigger": "auto",
    "preTokens": 167189
  }
}
```

`preTokens` 값은 압축이 발생하기 전에 사용된 토큰 수를 보여줍니다.

## 예제 subagent

이러한 예제는 subagent를 구축하기 위한 효과적인 패턴을 보여줍니다. 시작점으로 사용하거나 Claude로 사용자 정의된 버전을 생성합니다.

> **모범 사례:**
>
> * **집중된 subagent 설계:** 각 subagent는 특정 작업에서 탁월해야 합니다
> * **자세한 설명 작성:** Claude는 설명을 사용하여 위임할 시기를 결정합니다
> * **도구 액세스 제한:** 보안 및 집중을 위해 필요한 권한만 부여합니다
> * **버전 제어에 체크인:** 프로젝트 subagent를 팀과 공유합니다

### 코드 검토자

수정하지 않고 코드를 검토하는 읽기 전용 subagent입니다. 이 예제는 제한된 도구 액세스 (Edit 또는 Write 없음)와 정확히 무엇을 찾을지 및 출력 형식을 지정하는 자세한 프롬프트를 사용하여 집중된 subagent를 설계하는 방법을 보여줍니다.

```markdown
---
name: code-reviewer
description: Expert code review specialist. Proactively reviews code for quality, security, and maintainability. Use immediately after writing or modifying code.
tools: Read, Grep, Glob, Bash
model: inherit
---

You are a senior code reviewer ensuring high standards of code quality and security.

When invoked:
1. Run git diff to see recent changes
2. Focus on modified files
3. Begin review immediately

Review checklist:
- Code is clear and readable
- Functions and variables are well-named
- No duplicated code
- Proper error handling
- No exposed secrets or API keys
- Input validation implemented
- Good test coverage
- Performance considerations addressed

Provide feedback organized by priority:
- Critical issues (must fix)
- Warnings (should fix)
- Suggestions (consider improving)

Include specific examples of how to fix issues.
```

### 디버거

문제를 분석하고 수정할 수 있는 subagent입니다. 코드 검토자와 달리 이 subagent는 버그 수정이 코드 수정을 필요로 하기 때문에 Edit을 포함합니다. 프롬프트는 진단에서 검증까지의 명확한 워크플로우를 제공합니다.

```markdown
---
name: debugger
description: Debugging specialist for errors, test failures, and unexpected behavior. Use proactively when encountering any issues.
tools: Read, Edit, Bash, Grep, Glob
---

You are an expert debugger specializing in root cause analysis.

When invoked:
1. Capture error message and stack trace
2. Identify reproduction steps
3. Isolate the failure location
4. Implement minimal fix
5. Verify solution works

Debugging process:
- Analyze error messages and logs
- Check recent code changes
- Form and test hypotheses
- Add strategic debug logging
- Inspect variable states

For each issue, provide:
- Root cause explanation
- Evidence supporting the diagnosis
- Specific code fix
- Testing approach
- Prevention recommendations

Focus on fixing the underlying issue, not the symptoms.
```

### 데이터 과학자

데이터 분석 작업을 위한 도메인별 subagent입니다. 이 예제는 일반적인 코딩 작업 외에 특화된 워크플로우를 위해 subagent를 만드는 방법을 보여줍니다. 명시적으로 `model: sonnet`을 설정하여 더 유능한 분석을 수행합니다.

```markdown
---
name: data-scientist
description: Data analysis expert for SQL queries, BigQuery operations, and data insights. Use proactively for data analysis tasks and queries.
tools: Bash, Read, Write
model: sonnet
---

You are a data scientist specializing in SQL and BigQuery analysis.

When invoked:
1. Understand the data analysis requirement
2. Write efficient SQL queries
3. Use BigQuery command line tools (bq) when appropriate
4. Analyze and summarize results
5. Present findings clearly

Key practices:
- Write optimized SQL queries with proper filters
- Use appropriate aggregations and joins
- Include comments explaining complex logic
- Format results for readability
- Provide data-driven recommendations

For each analysis:
- Explain the query approach
- Document any assumptions
- Highlight key findings
- Suggest next steps based on data

Always ensure queries are efficient and cost-effective.
```

### 데이터베이스 쿼리 검증자

Bash 액세스를 허용하지만 읽기 전용 SQL 쿼리만 허용하도록 명령을 검증하는 subagent입니다. 이 예제는 `tools` 필드보다 더 세밀한 제어가 필요할 때 `PreToolUse` hook을 사용하는 방법을 보여줍니다.

```markdown
---
name: db-reader
description: Execute read-only database queries. Use when analyzing data or generating reports.
tools: Bash
hooks:
  PreToolUse:
    - matcher: "Bash"
      hooks:
        - type: command
          command: "./scripts/validate-readonly-query.sh"
---

You are a database analyst with read-only access. Execute SELECT queries to answer questions about the data.

When asked to analyze data:
1. Identify which tables contain the relevant data
2. Write efficient SELECT queries with appropriate filters
3. Present results clearly with context

You cannot modify data. If asked to INSERT, UPDATE, DELETE, or modify schema, explain that you only have read access.
```

Claude Code는 [hook 입력을 JSON으로](/ko/hooks#pretooluse-input) stdin을 통해 hook 명령에 전달합니다. 검증 스크립트는 이 JSON을 읽고 실행 중인 명령을 추출하고 SQL 쓰기 작업 목록에 대해 확인합니다. 쓰기 작업이 감지되면 스크립트는 [종료 코드 2](/ko/hooks#exit-code-2-behavior-per-event)로 종료하여 실행을 차단하고 stderr를 통해 Claude에 오류 메시지를 반환합니다.

프로젝트의 어디든지 검증 스크립트를 만듭니다. 경로는 hook 구성의 `command` 필드와 일치해야 합니다:

```bash
#!/bin/bash
# Blocks SQL write operations, allows SELECT queries

# Read JSON input from stdin
INPUT=$(cat)

# Extract the command field from tool_input using jq
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

if [ -z "$COMMAND" ]; then
  exit 0
fi

# Block write operations (case-insensitive)
if echo "$COMMAND" | grep -iE '\b(INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|TRUNCATE|REPLACE|MERGE)\b' > /dev/null; then
  echo "Blocked: Write operations not allowed. Use SELECT queries only." >&2
  exit 2
fi

exit 0
```

스크립트를 실행 가능하게 만듭니다:

```bash
chmod +x ./scripts/validate-readonly-query.sh
```

Hook은 stdin을 통해 JSON을 받으며 Bash 명령은 `tool_input.command`에 있습니다. 종료 코드 2는 작업을 차단하고 오류 메시지를 Claude에 피드백합니다.

## 다음 단계

이제 subagent를 이해했으므로 다음 관련 기능을 탐색합니다:

* [플러그인으로 subagent 배포](/ko/plugins) - 팀 또는 프로젝트 간에 subagent 공유
* [Claude Code를 프로그래밍 방식으로 실행](/ko/headless) - CI/CD 및 자동화를 위한 Agent SDK
* [MCP 서버 사용](/ko/mcp) - Subagent에 외부 도구 및 데이터에 대한 액세스 제공

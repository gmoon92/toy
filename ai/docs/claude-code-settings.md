# Claude Code Settings

Claude Code 설정 파일(`settings.json`) 가이드 문서입니다.

자세한 내용은 [Claude Code 문서 - Settings](https://code.claude.com/docs/en/settings)를 참고해 주세요.

## 설정 파일 로드 순서 (Merge -> Override)

Claude Code의 설정은 `settings.json` 파일로 관리합니다.

설정 파일은 아래 **순서대로 병합하며**,  
동일한 키가 있을 경우 **마지막에 로드된 값이 우선 적용됩니다.**

| 순서 | 파일                              | 범위                | 설명                 |
|:--:|:--------------------------------|:------------------|:-------------------|
| 1  | `~/.claude/settings.json`       | User              | 전역 설정 (모든 프로젝트 공통) |
| 2  | `~/.claude/settings.local.json` | User (Private)    | 개인 전역 설정           |
| 3  | `.claude/settings.json`         | Project           | 프로젝트 설정            |
| 4  | `.claude/settings.local.json`   | Project (Private) | 프로젝트 개인 설정         |

- 동일한 키가 여러 파일에 있으면 **가장 마지막에 로드된 값**이 적용됩니다.
- 프로젝트 설정이 전역 설정보다 우선합니다.

> 예를 들어 아래와 같이 설정된 경우, 프로젝트에서 Claude를 실행하면 `model` 값은 `claude-sonnet-4-6`으로 설정됩니다.
>
> ```text
> # ~/.claude/settings.json
> { "model": "claude-opus-4-1" }
> 
> # .claude/settings.json
> { "model": "claude-sonnet-4-6" }
> ```

### `settings.json` 와 `settings.local.json` 차이

설정 파일은 크게 `settings.json`과 `settings.local.json` 두 종류로 나뉩니다.

| 파일                    | 용도        | 예시                         |
|:----------------------|:----------|:---------------------------|
| `settings.json`       | 팀 공유 설정   | MCP 서버 목록, 공통 권한 패턴        |
| `settings.local.json` | 개인 커스텀 설정 | 개별 API 키, 로컬 경로, 개인별 선호 모델 |

Claude Code에서 `.local`의 의미는 Git으로 공유하지 않는 개인 설정을 의미합니다.  
따라서 `settings.local.json` 파일은 Git에서 제외되도록 `.gitignore`에 추가하는 것을 권장합니다.

---

## `settings.json` 설정 예시

```json
{
  "model": "claude-sonnet-4-6",
  "language": "korean",
  "autoUpdatesChannel": "latest",
  "teammateMode": "in-process",
  "alwaysThinkingEnabled": true,
  "enableAllProjectMcpServers": true,
  "env": {
    "PROJECT_DIR": "/Users/gmoon/IdeaProjects/squadliterv",
    "PROJECT_DIR_SHRIMP_TASK_MANAGER": "/Users/gmoon/IdeaProjects/mcp-shrimp-task-manager",
    "ENABLE_TOOL_SEARCH": "1",
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1",
    "CLAUDE_CODE_MAX_OUTPUT_TOKENS": "64000",
    "CLAUDE_CODE_EFFORT_LEVEL": "high"
  },
  "attribution": {
    "commit": "",
    "pr": ""
  },
  "companyAnnouncements": [
    "   _____                       __   __    _ __          ____ _    __\n  / ___/____ ___  ______ _____/ /  / /   (_) /____     / __ \\ |  / /\n  \\__ \\/ __ `/ / / / __ `/ __  /  / /   / / __/ _ \\   / /_/ / | / / \n ___/ / /_/ / /_/ / /_/ / /_/ /  / /___/ / /_/  __/  / _, _/| |/ /  \n/____/\\__, /\\__,_/\\__,_/\\__,_/  /_____/_/\\__/\\___/  /_/ |_| |___/   \n        /_/                                                         "
  ],
  "hooks": {
    "Notification": [
      {
        "matcher": "*",
        "hooks": [
          {
            "type": "command",
            "command": "osascript -e 'display notification \"Claude Code needs your attention\" with title \"Claude Code\"'",
            "async": false
          }
        ]
      }
    ]
  },
  "permissions": {
    "allow": [
      "mcp__serena__*",
      "mcp__figma-desktop__*",
      "mcp__playwright__*",
      "mcp__chrome-devtools__*",
      "Bash(git status:*)",
      "Bash(git diff:*)",
      "Bash(git log:*)",
      "Bash(git add:*)",
      "Bash(git rebase:*)",
      "Bash(git commit:*)",
      "Bash(gradle:*)",
      "Bash(npm:*)"
    ]
  }
}
```

| 설정                           | 설명                                                       |
|:-----------------------------|:---------------------------------------------------------|
| `model`                      | 사용할 Claude 모델                                            |
| `language`                   | Claude CLI 인터페이스 기본 언어                                   |
| `autoUpdatesChannel`         | 자동 업데이트 채널 (`latest`, `stable`)                          |
| `teammateMode`               | 멀티 에이전트 팀 동기화 방식 (`in-process`, `tmux`)       |
| `alwaysThinkingEnabled`      | Claude의 심층 추론(`Extended Thinking`) 모드 기본 활성화             |
| `enableAllProjectMcpServers` | 프로젝트 MCP 서버 설정 파일(`.claude/mcp.json`)에 정의된 MCP 서버 자동 활성화 |
| `env`                        | Claude Code에서 사용할 환경 변수                                  |
| `attribution`                | 커밋/PR 작성자 정보                                             |
| `companyAnnouncements`       | CLI 시작 시 표시할 배너 정보                                       |
| `hooks`                      | 특정 이벤트 발생 시 실행할 동작                                       |
| `permissions`                | 자동 승인할 도구/명령어 패턴                                         |

> 더 자세한 설정 값은 [Claude Code 문서 - Available Settings](https://code.claude.com/docs/en/settings#available-settings)를 참고해 주세요.

### `env` - 환경 변수 설정

Claude Code 실행 시 프로세스 환경 변수로 주입됩니다.  
MCP 서버, Hooks, Bash 명령 등에서 사용할 수 있습니다.

```json
{
  "env": {
    "PROJECT_DIR": "/Users/gmoon/IdeaProjects/squadliterv",
    "PROJECT_DIR_SHRIMP_TASK_MANAGER": "/Users/gmoon/IdeaProjects/mcp-shrimp-task-manager",
    "ENABLE_TOOL_SEARCH": "1",
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1",
    "CLAUDE_CODE_MAX_OUTPUT_TOKENS": "64000",
    "CLAUDE_CODE_EFFORT_LEVEL": "high"
  }
}
```

- `ENABLE_TOOL_SEARCH`: MCP Tool Search 기능 활성화
- `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS`: 멀티 에이전트 실험 기능 활성화
- `CLAUDE_CODE_MAX_OUTPUT_TOKENS`: 최대 출력 토큰 수
- `CLAUDE_CODE_EFFORT_LEVEL`: 처리 수준

> 자세한 내용은 [Claude Code 문서 - Environment variables](https://code.claude.com/docs/en/env-vars) 환경 변수 섹션을 참고해 주세요.

---

### `hooks` - 이벤트 실행 설정

특정 이벤트 발생 시 자동으로 실행할 동작을 정의합니다.  
아래 예시는 Claude Code 작업 이후 `시스템 알림`을 표시하는 설정입니다.

```json
{
  "hooks": {
    "Notification": [
      {
        "matcher": "*",
        "hooks": [
          {
            "type": "command",
            "command": "osascript -e 'display notification \"Claude Code needs your attention\" with title \"Claude Code\"'",
            "async": false
          }
        ]
      }
    ]
  }
}
```

- `matcher`: 이벤트 필터 (`"*"` = 모든 이벤트)
- `type`: 후크 유형 (`"command"` = 명령어 실행)
- `command`: 실행할 셸 명령어
- `async`: 비동기 실행 여부

**OS별 알림 명령어 예시:**

| OS                   | 명령어                                                                                                                                                                                               |
|:---------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| macOS                | `osascript -e 'display notification "Claude Code needs your attention" with title "Claude Code"'`                                                                                                 |
| Linux                | `notify-send 'Claude Code' 'Claude Code needs your attention'`                                                                                                                                    |
| Windows (PowerShell) | `powershell.exe -Command "[System.Reflection.Assembly]::LoadWithPartialName('System.Windows.Forms'); [System.Windows.Forms.MessageBox]::Show('Claude Code needs your attention', 'Claude Code')"` |

---

### `attribution` - 커밋 작성자 정보

기본적으로 Claude Code는 커밋 메시지에 작성자 정보를 자동으로 추가합니다.

```
chore: remove .claude/settings.local.json from tracking

- Remove local settings file from git tracking
- File remains in .gitignore to prevent future tracking

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```

- `Co-authored-by: Claude <noreply@anthropic.com>`

이를 비우거나 다른 값으로 변경하려면 이 설정을 사용합니다.

```json
{
  "attribution": {
    "commit": "",
    "pr": ""
  }
}
```

- `commit`: Git 커밋 작성자 (`Co-authored-by`)
- `pr`: PR 작성자 정보

---

### `companyAnnouncements` - CLI 배너 설정

Claude Code CLI 시작 시 표시할 **조직 메시지(Announcement)를** 설정할 수 있습니다.

이 문서에서는 이를 활용하여 **프로젝트를 구분하기 위한 ASCII 아트 배너**를 표시하는 예시를 제공합니다.  
여러 Claude Code CLI 인스턴스를 동시에 실행하거나 여러 프로젝트를 병행할 때 구분에 유용합니다.

**적용 예시:**

```json 
{
  "companyAnnouncements": [
    "  ______                            \n /_  __/___  __  __                 \n  / / / __ \\/ / / /                 \n / / / /_/ / /_/ /                  \n/_/  \\____/\\__, /                   \n   ____ __/____/_  ____  ____  ____ \n  / __ `/ __ `__ \\/ __ \\/ __ \\/ __ \\\n / /_/ / / / / / / /_/ / /_/ / / / /\n \\__, /_/ /_/ /_/\\____/\\____/_/ /_/ \n/____/                              \n\n@github: https://github.com/gmoon92\nFont: Slant with https://www.patorjk.com/software/taag/#p=display&f=Slant"
  ]
}
```

```
➜  toy (master) claude
 ▐▛███▜▌   Claude Code v2.1.74
▝▜█████▛▘  claude-sonnet-4-6 · API Usage Billing
  ▘▘ ▝▝    ~/IdeaProjects/toy

  Message from gmoon’s Individual Org:
    ______
   /_  __/___  __  __
    / / / __ \/ / / /
   / / / /_/ / /_/ /
  /_/  \____/\__, /
     ____ __/____/_  ____  ____  ____
    / __ `/ __ `__ \/ __ \/ __ \/ __ \
   / /_/ / / / / / / /_/ / /_/ / / / /
   \__, /_/ /_/ /_/\____/\____/_/ /_/
  /____/

  @github: https://github.com/gmoon92
  Font: Slant with https://www.patorjk.com/software/taag/#p=display&f=Slant
```

- 폰트: Slant
- 사이트: https://www.patorjk.com/software/taag/#p=display&f=Slant

---

### `permissions` - 자동 권한 설정

사용자 승인 없이 자동으로 실행할 수 있는 도구 또는 명령어 패턴을 정의합니다.

**패턴 형식:** ToolName(command:argument-pattern)

```json
{
  "permissions": {
    "allow": [
      "mcp__serena__*",
      "mcp__figma-desktop__*",
      "mcp__playwright__*",
      "mcp__chrome-devtools__*",
      "Bash(git status:*)",
      "Bash(git diff:*)",
      "Bash(git log:*)",
      "Bash(git add:*)",
      "Bash(git rebase:*)",
      "Bash(git commit:*)",
      "Bash(gradle:*)",
      "Bash(npm:*)"
    ]
  }
}
```

- `mcp__서버명__*`: MCP 서버의 모든 도구
- `Bash(git status:*)`: git status 명령어
- `*`: 와일드카드로 모든 패턴 매칭

---

## Reference

- [Claude Code 문서 - Settings](https://code.claude.com/docs/en/settings)
- [Claude Code 문서 - Environment variables](https://code.claude.com/docs/en/env-vars)
- [Claude Code 문서 - Available Settings](https://code.claude.com/docs/en/settings#available-settings)

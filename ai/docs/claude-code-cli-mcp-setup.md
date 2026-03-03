# Claude Code CLI MCP 연동 가이드

이 문서는 Claude Code CLI에서 MCP 서버를 등록하고 운영하는 실무 절차를 간단히 안내합니다.

MCP의 개념과 배경은 [MCP 기본 개념 가이드](mcp-concepts.md)를 참고하세요.

---

## MCP 서버 추가하기

이 섹션은 MCP 서버를 Claude Code CLI에 등록하는 기본 명령과 연결 방식을 설명합니다.

```bash
claude mcp add [option] <name> -- <command> [args...]
```

`--` 앞은 Claude CLI 옵션, `--` 뒤는 CLI가 실행할 외부 프로그램입니다.

```text
claude mcp add \
  --scope project \
  sequential-thinking \
  -- \
  npx -y @modelcontextprotocol/server-sequential-thinking
```

### MCP 서버 연결 방식

Claude Code CLI는 MCP 서버 연결을 3가지 방식으로 지원합니다.

| 방식        | 설명                          | 예시                                                                                              |
|-----------|-----------------------------|-------------------------------------------------------------------------------------------------|
| `http`    | 원격 서버 URL에 연결               | `claude mcp add --transport http context7 https://mcp.context7.com/mcp`                         |
| `stdio`   | 로컬 프로세스 직접 실행               | `claude mcp add sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking` |
| ~~`sse`~~ | ~~SSE 전송 이벤트 (Deprecated)~~ | 사용 권장하지 않음                                                                                      |

> `sse` 방식은 레거시 방식이며 deprecated 상태입니다. 
> 자세한 내용은 [Claude Code 공식 문서 - Installing MCP servers](https://code.claude.com/docs/en/mcp#installing-mcp-servers)를 참고하세요.

### MCP 설치 범위

Claude Code CLI에서는 `--scope` 옵션을 통해 MCP 서버의 동작 범위를 지정할 수 있습니다.

```bash
# local (기본)
claude mcp add --transport http stripe https://mcp.stripe.com/mcp

# project
claude mcp add --scope project --transport http paypal https://mcp.paypal.com/mcp

# user
claude mcp add --scope user --transport http hubspot https://mcp.hubspot.com/anthropic
```

- `local`은 기본값이며 현재 작업 디렉토리의 `.claude.json`에 저장되고 현재 프로젝트에서만 사용됩니다.
- `project`는 프로젝트 루트의 `.mcp.json`에 저장되며 팀과 공유할 수 있습니다.
- `user`는 홈 디렉토리의 `~/.claude.json`에 저장되며 모든 프로젝트에서 사용됩니다.

> 현재 작업 디렉토리는 `claude` 명령을 실행한 폴더이며 `pwd`로 확인할 수 있습니다.

#### 설정 파일 로드 및 적용 순서

Claude Code는 기본적으로 다음 순서로 설정 파일을 로드합니다.

1. global (`user` 사용자)
2. project (`.mcp.json`)
3. local (`claude.json`)

이때 설정값은 역순으로 적용되며, 동일한 이름이 여러 범위에 있으면 `local > project > user` 순서로 우선 적용됩니다.

## 프로젝트 별 MCP 서버 설정 구성

프로젝트 범위(`--scope project`)로 등록된 MCP 서버는 `.mcp.json` 파일에 저장되어 팀과 공유됩니다. 
이 파일은 버전 관리에 체크인되므로, 외부 저장소에서 클론받았을 때 의도치 않은 MCP 서버가 자동으로 실행되는 것을 방지하기 위해 **최초 사용 전 보안 승인 절차**가 필요합니다.

### 보안 승인

프로젝트 범위 서버는 최초 사용 전에 보안 승인이 필요합니다.
기존 승인 내역을 초기화하여 재확인하거나, 프로젝트 설정 변경 시 전체를 재승인하려면 다음 명령을 사용하세요.

```bash
claude mcp reset-project-choices
```

**초기화가 필요한 경우:**

- 승인 내역 재확인 및 보안 재검토
- `.mcp.json` 변경 또는 새 서버 추가 시
- 정기적 보안 정책 준수
- 서버 연결 문제 해결

### 환경 변수 설정

프로젝트 설정 파일(`.mcp.json`)에서는 민감 정보를 하드코딩하지 않고 환경 변수 확장을 사용할 수 있습니다.

```json
{
  "mcpServers": {
    "api-server": {
      "type": "http",
      "url": "${API_BASE_URL:-https://api.example.com}/mcp",
      "headers": {
        "Authorization": "Bearer ${API_KEY}"
      }
    }
  }
}
```

`${VAR}`는 변수 값 치환, `${VAR:-default}`는 미설정 시 기본값 사용입니다.

---

## 프로젝트 `.mcp.json` 설정 가이드

예를 들어 `my-project`라는 프로젝트에서 `.mcp.json` 파일로 MCP 서버를 관리한다면 다음과 같은 구조를 가지고 있습니다.

```text
my-project/
├─ .claude/
├     └─ prd/
├     └─ promt/
├     └─ settings.local.json     <- 로컬 클로드 코드 CLI 설정 파일
├─ .mcp.json  <- 프로젝트 MCP 서버 설정 파일
├─ fe/
└─ be/
```

### 1. settings.local.json 환경변수 설정

프로젝트의 `.claude/settings.local.json` 파일에 아래 환경변수를 추가합니다.

```json
{
  "env": {
    "PROJECT_DIR": "/Users/gmoon/IdeaProjects/my-project",
    "PROJECT_DIR_SHRIMP_TASK_MANAGER": "/Users/gmoon/IdeaProjects/mcp-shrimp-task-manager"
  }
}
```

- `PROJECT_DIR`: 현재 프로젝트의 절대 경로
- `PROJECT_DIR_SHRIMP_TASK_MANAGER`: Shrimp Task Manager 프로젝트의 절대 경로
    - 자세한 프로젝트 설치 방법은 다음 [Shrimp Task Manager 섹션](#shrimp-task-manager)을 참고하세요.

두 값 모두 반드시 절대 경로로 지정하세요.

### 2. Claude Code CLI에서 MCP 서버 설정 확인

Claude Code CLI에서 `/mcp` 명령으로 서버 연결 상태를 확인할 수 있습니다.

```bash
➜  my-project (main) claude                                                                                                                                                                                                                                              ✭ ✱

╭─── Claude Code v2.1.62 ────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╮
│                                   │ Tips for getting started                                                                                                                                                                                                               │
│           Welcome back!           │ Run /init to create a CLAUDE.md file with instructions for Claude                                                                                                                                                                      │
│                                   │ ─────────────────────────────────────────────────────────────────                                                                                                                                                                      │
│                                   │ Recent activity                                                                                                                                                                                                                        │
│              ▐▛███▜▌              │ No recent activity                                                                                                                                                                                                                     │
│             ▝▜█████▛▘             │                                                                                                                                                                                                                                        │
│               ▘▘ ▝▝               │                                                                                                                                                                                                                                        │
│   kimi-k2.5 · API Usage Billing   │                                                                                                                                                                                                                                        │
│    ~/IdeaProjects/my-project     │                                                                                                                                                                                                                                        │
╰────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────╯

  /model to try Opus 4.6

❯ /mcp
──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 Manage MCP servers
 6 servers

   Project MCPs (/Users/gmoon/IdeaProjects/my-project/.mcp.json)
 ❯ chrome-devtools · ✔ connected
   context7 · ✔ connected
   playwright · ✔ connected
   sequential-thinking · ✔ connected
   serena · ✔ connected
   shrimp-task-manager · ✔ connected

 https://code.claude.com/docs/en/mcp for help

 ↑↓ to navigate · Enter to confirm · Esc to cancel
```

- Claude Code CLI에서 프로젝트 단위 MCP 서버 설정은 `.mcp.json`으로 관리됩니다.
- 아래 문구가 보이면 `.mcp.json`이 정상 로드된 상태입니다.
    - `Project MCPs (/Users/gmoon/IdeaProjects/my-project/.mcp.json)`

### 3. `.mcp.json` 설정이 아닌, 사용자 설정(글로벌)으로 로드된다면?

간혹 설정 충돌로 인해 프로젝트 설정(`.mcp.json`) 대신 사용자 설정(`~/.claude.json`)이 적용되는 경우가 있습니다. 이 경우 프로젝트 설정을 초기화하고 다시 로드해야 합니다.

#### 3.1. 사용자 클로드 설정에서 맵핑된 프로젝트 제거

사용자 설정 파일 `~/.claude.json`의 `projects` 항목에서 현재 프로젝트를 제거하세요.

```bash
vi ~/.claude.json
```

```json
{
  "projects": {
    "/Users/gmoon/IdeaProjects/my-project": {
      "allowedTools": [],
      "mcpContextUris": [],
      "mcpServers": {},
      "enabledMcpjsonServers": [],
      "disabledMcpjsonServers": [],
      "hasTrustDialogAccepted": true,
      "ignorePatterns": [],
      "projectOnboardingSeenCount": 4,
      "hasClaudeMdExternalIncludesApproved": false,
      "hasClaudeMdExternalIncludesWarningShown": false,
      "exampleFiles": [
        "SettingLicenseController.java",
        "PaymentManagerImpl.java",
        "LicenseManagerImpl.java",
        "PaymentController.java",
        "DateUtil.java"
      ],
      "lastTotalWebSearchRequests": 0,
      "exampleFilesGeneratedAt": 1771901333692,
      "reactVulnerabilityCache": {
        "detected": false,
        "package": null,
        "packageName": null,
        "version": null,
        "packageManager": null
      }
    }
  }
}
```

- `"/Users/gmoon/IdeaProjects/my-project"` 항목을 제거

### 3.2. Claude Code CLI 재시작

Claude Code CLI를 종료 후 다시 실행하면 변경된 프로젝트 설정이 적용됩니다.

```bash
my-project (main) claude                                                                                                     ✭ ✱

─────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
 Accessing workspace:

 /Users/gmoon/IdeaProjects/my-project

 Quick safety check: Is this a project you created or one you trust? (Like your own code, a well-known open source project, or work
 from your team). If not, take a moment to review what's in this folder first.

 Claude Code'll be able to read, edit, and execute files here.

 Security guide

 ❯ 1. Yes, I trust this folder
   2. No, exit

 Enter to confirm · Esc to cancel
```

- 프로젝트 신뢰성 컨펌 메시지가 나온다면 초기화가 정상적으로 됐다는 의미입니다.
- `1. Yes, I trust this folder` 선택하면 자동적으로 사용자 설정(글로벌)에 프로젝트가 재설정됩니다.

---

## MCP 서버 관리

이 섹션은 상태 점검, 상세 조회, 제거 등 운영 명령을 빠르게 확인하기 위한 내용입니다.

```bash
# 목록 조회
claude mcp list

# 상세 조회
claude mcp get <name>

# 제거
claude mcp remove <name>
```

CLI 내부에서는 `/mcp`로 연결 상태를 확인할 수 있습니다.

```bash
claude

# Claude Code CLI 진입 후
➜  ~  claude

▐▛███▜▌   Claude Code v2.1.56
▝▜█████▛▘  kimi-k2.5 · API Usage Billing
▘▘ ▝▝    /Users/gmoon

/model to try Opus 4.6

❯ /mcp
───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
Manage MCP servers
6 servers

User MCPs (/Users/gmoon/.claude.json)
❯ chrome-devtools · ✔ connected
playwright · ✔ connected
sequential-thinking · ✔ connected
serena · ✔ connected
shrimp-task-manager · ✔ connected

Built-in MCPs (always available)
plugin:context7:context7 · ✔ connected

https://code.claude.com/docs/en/mcp for help

↑↓ to navigate · Enter to confirm · Esc to cancel
```

---

## 추천 MCP 설치 가이드

- [Context7](#context7)
- [Sequential Thinking](#sequential-thinking)
- [Playwright](#playwright)
- [Chrome DevTools](#chrome-devtools)
- [Serena](#serena)
- [Shrimp Task Manager](#shrimp-task-manager)

더 많은 서버는 [Claude Code 공식 문서](https://code.claude.com/docs/en/mcp#popular-mcp-servers)를 참고하세요.

### Context7

최신 라이브러리 문서 검색/참조에 사용합니다.

```bash
# macOS/Linux
claude mcp add \
  --scope project \
  --transport http \
  context7 \
  https://mcp.context7.com/mcp

# Windows
claude mcp add --scope project --transport http context7 https://mcp.context7.com/mcp
```

> 참고:
> - https://github.com/upstash/context7
> - https://github.com/upstash/context7?tab=readme-ov-file#claude-code-local-server-connection

### Sequential Thinking

복잡한 작업을 단계적으로 분해해 추론할 때 사용합니다.

```bash
claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking
```

> 참고:
> - https://github.com/modelcontextprotocol/servers/tree/main/src/sequentialthinking

### Playwright

브라우저 자동화와 웹 테스트 실행에 사용합니다.

```bash
claude mcp add --scope project playwright -- npx @playwright/mcp@latest
```

> 참고:
> - https://github.com/microsoft/playwright-mcp

### Chrome DevTools

크롬 개발자 도구 기반 디버깅에 사용합니다.

```bash
claude mcp add chrome-devtools -- npx chrome-devtools-mcp@latest
```

> 참고:
> - https://github.com/ChromeDevTools/chrome-devtools-mcp
> - https://duck-blog.vercel.app/blog/web/chrome-devtools-mcp-game-changer

### Serena

코드베이스 심볼 검색과 구조 분석에 사용합니다.

```bash
# macOS
brew install uv

# Windows
python -m pip install uv

# add
claude mcp add --scope user serena -- uvx --from git+https://github.com/oraios/serena --context ide-assistant --enable-web-dashboard false serena-mcp-server start-mcp-server
```

> 참고:
> - https://oraios.github.io/serena
> - https://oraios.github.io/serena/01-about/000_intro.html

### Shrimp Task Manager

작업/계획 관리를 위한 서버로 빌드 후 직접 등록이 필요합니다.

```bash
git clone https://github.com/cjo4m06/mcp-shrimp-task-manager.git
cd mcp-shrimp-task-manager
npm install
npm run build
```

`.mcp.json` 예시:

```json
{
  "mcpServers": {
    "shrimp-task-manager": {
      "type": "stdio",
      "command": "node",
      "args": [
        "/path/to/mcp-shrimp-task-manager/dist/index.js"
      ],
      "env": {
        "DATA_DIR": "/path/to/your/shrimp_data",
        "TEMPLATES_USE": "en",
        "ENABLE_GUI": "false"
      }
    }
  }
}
```

`/path/to`를 직접 쓰지 않고, 프로젝트 로컬 `.claude/settings.local.json`의 `env`로 경로를 관리하는걸 권장합니다.

```json
{
  "mcpServers": {
    "shrimp-task-manager": {
      "type": "stdio",
      "command": "node",
      "args": [
        "${PROJECT_DIR_SHRIMP_TASK_MANAGER}/dist/index.js"
      ],
      "env": {
        "DATA_DIR": "${PROJECT_DIR}/.claude/shrimp_data",
        "TEMPLATES_USE": "en",
        "ENABLE_GUI": "false"
      }
    }
  }
}
```

프로젝트의 `.claude/settings.local.json` 파일에 환경변수를 추가합니다.

**Sample**

```json
{
  "env": {
    "PROJECT_DIR": "/Users/gmoon/IdeaProjects/my-project",
    "PROJECT_DIR_SHRIMP_TASK_MANAGER": "/Users/gmoon/IdeaProjects/mcp-shrimp-task-manager"
  }
}
```

> 참고: https://github.com/cjo4m06/mcp-shrimp-task-manager

---

## 보안 주의사항

MCP 운영 시 꼭 확인해야 할 보안 체크포인트입니다.

- MCP 서버는 외부 프로그램이므로 신뢰할 수 있는 출처만 사용하세요.
- 정기적으로 `claude mcp list`로 등록된 서버를 확인하고 불필요한 서버는 제거하세요.

## 참고 자료

- [Claude Code MCP 공식 문서](https://code.claude.com/docs/en/mcp)

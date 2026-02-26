# Claude Code CLI MCP 연동 가이드

이 문서는 Claude Code CLI에서 MCP 서버를 등록하고 운영하는 실무 절차를 간단히 안내합니다.

MCP의 개념과 배경은 [MCP 기본 개념 가이드](mcp-concepts.md)를 참고하세요.

MCP 개념 설명은 별도 문서를 참고하세요.

- [MCP 기본 개념](./mcp-concepts.md)
- [기존 LLM의 동작 방식과 한계](./mcp-concepts.md#1-기존-llm의-동작-방식과-한계)
- [실행 문제를 해결하기 위한 확장](./mcp-concepts.md#2-실행-문제를-해결하기-위한-확장)
- [MCP란?](./mcp-concepts.md#3-mcp란)
- [CLI 기반 MCP 동작 구조](./mcp-concepts.md#4-cli-기반-mcp-동작-구조)
- [MCP 연결 시 가능한 변화](./mcp-concepts.md#5-mcp-연결-시-가능한-변화)

---

## MCP 서버 추가하기

이 섹션은 MCP 서버를 Claude Code CLI에 등록하는 기본 명령과 연결 방식을 설명합니다.

기본 구문:

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

MCP 서버를 연동하는 방식은 크게 3가지입니다.

<details>
<summary>`http`: HTTP 원격 서버 연결</summary>

`http` transport 옵션은 원격에서 이미 실행 중인 서버 URL에 연결할 때 사용합니다.

```bash
# 예시 1: Context7
claude mcp add \
  --scope project \
  --transport http \
  context7 \
  https://mcp.context7.com/mcp

# 예시 2: Bearer 토큰
claude mcp add \
  --scope user \
  --transport http \
  secure-api \
  https://api.example.com/mcp \
  --header "Authorization: Bearer your-token"
```

```bash
claude mcp add --scope project --transport http context7 https://mcp.context7.com/mcp
```

</details>

<details>
<summary>`stdio`: 로컬 프로세스 실행</summary>

`stdio`는 로컬 컴퓨터에서 MCP 서버 프로세스를 직접 실행할 때 사용합니다.

```bash
claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking
```

</details>

<details>
<summary>~~`sse`: SSE 전송 이벤트 (Deprecated)~~</summary>

`sse`는 레거시 방식이며 deprecated 상태이므로 가능하면 `http`를 사용하세요.

```bash
claude mcp add --transport sse asana https://mcp.asana.com/sse
```

</details>

---

## MCP 설치 범위

이 섹션은 `--scope`에 따른 저장 위치와 적용 범위를 설명합니다.

- `local`은 기본값이며 현재 작업 디렉토리의 `.claude.json`에 저장되고 현재 프로젝트에서만 사용됩니다.
- `project`는 프로젝트 루트의 `.mcp.json`에 저장되며 팀과 공유할 수 있습니다.
- `user`는 홈 디렉토리의 `~/.claude.json`에 저장되며 모든 프로젝트에서 사용됩니다.

현재 작업 디렉토리는 `claude` 명령을 실행한 폴더이며 `pwd`로 확인할 수 있습니다.

```bash
# local (기본)
claude mcp add --transport http stripe https://mcp.stripe.com/mcp

# project
claude mcp add --scope project --transport http paypal https://mcp.paypal.com/mcp

# user
claude mcp add --scope user --transport http hubspot https://mcp.hubspot.com/anthropic
```

동일한 이름이 여러 범위에 있으면 `local > project > user` 순서로 우선 적용됩니다.

프로젝트 범위 서버는 최초 사용 전에 보안 승인 절차가 필요합니다.

```bash
claude mcp reset-project-choices
```

프로젝트 설정 파일에서는 환경 변수 확장을 사용할 수 있습니다.

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

## 설정 파일 예시

이 섹션은 `~/.claude.json` 또는 `.mcp.json`에서 사용하는 기본 구조 예시를 보여줍니다.

```json
{
  "mcpServers": {
    "sequential-thinking": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-sequential-thinking"
      ]
    },
    "context7": {
      "type": "http",
      "url": "https://mcp.context7.com/mcp"
    }
  }
}
```

직접 편집보다 `claude mcp add` 명령을 사용해 설정을 생성하는 방식을 권장합니다.

---

## 보안 주의사항

이 섹션은 MCP 운영 시 꼭 확인해야 할 보안 체크포인트를 요약합니다.

- MCP 서버는 외부 프로그램이므로 신뢰할 수 있는 출처만 사용하세요.
- `project` 범위 설정은 팀과 공유될 수 있으니 민감 정보를 직접 기록하지 마세요.
- API 키와 토큰은 환경 변수로 분리하고 설정 파일에서는 참조만 하세요.
- 프로젝트 범위 서버는 최초 승인 전에 동작 범위를 확인하세요.

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
▘▘ ▝▝    /Users/username

/model to try Opus 4.6

❯ /mcp
───────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
Manage MCP servers
6 servers

User MCPs (/Users/username/.claude.json)
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
    "PROJECT_DIR": "/Users/username/IdeaProjects/my-project",
    "PROJECT_DIR_SHRIMP_TASK_MANAGER": "/Users/username/IdeaProjects/mcp-shrimp-task-manager"
  }
}
```

> 참고:
> - https://github.com/cjo4m06/mcp-shrimp-task-manager

## 참고 자료

- [Claude Code MCP 공식 문서](https://code.claude.com/docs/en/mcp)

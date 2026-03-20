# Claude Code CLI MCP 가이드

Claude Code CLI 환경에서 MCP(Model Context Protocol) 서버를 연동하는 방법을 가이드합니다.

---

##  MCP 기본 개념

### 기존 LLM의 동작 방식과 한계

Claude, Kimi, GPT와 같은 LLM(대형 언어 모델)은 기본적으로 **텍스트 입력을 받아 텍스트를 생성하는 모델**입니다.

즉, 다음과 같이 단순히 텍스트를 입력받고 출력하는 작업은 가능합니다.

- 질문에 답변
- 코드 생성
- 문서 작성

하지만 다음 작업은 **LLM이 직접 수행할 수는 없습니다.**

- 파일 수정
- 브라우저 조작
- 데이터베이스 조회
- API 호출

LLM은 실행 주체가 아니라, 실행을 "요청하는 텍스트"를 생성하는 모델이기 때문입니다.

### 실행 문제를 해결하기 위한 확장

그렇다면 AI가 실제 작업을 수행하려면 어떻게 해야 할까요?

**텍스트 생성 능력과 외부 프로그램 실행 능력을 연결해주는 구조**가 필요합니다.

이 문제를 해결하기 위해 등장한 것이 **MCP(Model Context Protocol)**입니다.

### MCP란?

**MCP(Model Context Protocol)** 는 AI 모델이 외부 도구(Tool)와 통신할 수 있도록 정의된 표준 프로토콜입니다.

- **MCP**: 통신 규칙(Protocol)
- **MCP 서버**: 해당 규칙을 구현한 외부 실행 프로그램

MCP가 표준 프로토콜이기 때문에 다음과 같은 장점이 있습니다.

- 특정 모델에 종속되지 않음
- 모델 교체(Claude → Kimi → GPT 등)와 무관하게 동일한 구조 유지
- 쉽게 말해, **MCP 서버는 AI 모델이 호출하여 실제 작업을 수행할 수 있는 외부 도우미 프로그램**

---

## 2. Claude Code CLI 기반 MCP 동작 구조

실행 주체는 **CLI**이며, LLM은 **의사결정 및 명령 생성 역할**을 담당합니다.

```
사용자
   ↓ (명령 입력)
Claude Code CLI
   ↓ (프롬프트 전달)
LLM
   ↓ (Tool Call 생성)
Claude Code CLI
   ↓ (MCP 프로토콜)
MCP Server (외부 프로그램)
   ↓
파일 / 브라우저 / DB / API
```

**역할 분리:**

- **LLM**: 실행 주체가 아닌 의사결정 주체. "도구 호출 요청(Tool Call)" 생성
- **Claude Code CLI**: LLM의 요청을 해석하여 MCP 서버 실행
- **MCP 서버**: 실제 시스템 작업 수행

### MCP 연결 시 동작 확장 예시

예를 들어 사용자가 "이 코드의 문제점을 찾아서 수정해줘"라고 요청했다고 가정필보겠습니다.

| 구분 | MCP 미연결 | MCP 연결 |
|-----|-----------|---------|
| **작업** | 코드 분석 및 수정 방법 텍스트로 설명 | 코드 분석 → CLI가 MCP 호출 → 실제 파일 수정 및 저장 |
| **결과** | 설명과 제안까지만 가능 | 추론 → 실행 → 결과 알림까지 완료 |

MCP는 단순히 "더 똑똑한 답변"을 만드는 기능이 아니라, LLM의 추론 결과를 실제 시스템 작업으로 이어지게 만드는 연결 구조입니다. 그 결과 AI는 단순한 대화형 도구를 넘어, 실제 업무를 수행하는 실행형 AI로 확장됩니다.

---

## 3. MCP 서버 추가하기

기본 구문은 다음과 같습니다.

```bash
claude mcp add [option] <name> -- <command> [args...]
```

> ⚠️ `--` 이후의 명령어는 CLI가 실행하는 외부 프로세스입니다.
>
> `--` 전은 Claude CLI 옵션, `--` 후는 실행할 프로그램입니다.
>
> ```text
> claude mcp add \
>   --scope project \      # Claude CLI 옵션 (scope 지정)
>   sequential-thinking \  # MCP 서버 이름
>   -- \                   # 구분자 `--`
>   npx -y @modelco...     # 실행할 프로그램 및 인자
> ```

### MCP 서버 연결 방식

Claude Code CLI는 3가지 방식으로 MCP 서버를 연결할 수 있습니다.

| 방식        | 실행 위치 | 서버 실행 주체               | 사용 상황                |
|-----------|-------|------------------------|----------------------|
| **http**  | 외부 서버 | 원격에서 이미 실행 중           | 클라우드 기반 MCP 서버       |
| **stdio** | 내 컴퓨터 | Claude Code CLI가 직접 실행 | 로컬에서 직접 실행           |
| **sse**   | 외부 서버 | 원격 (Deprecated)        | 레거시 시스템 (deprecated) |

- **http** → 이미 실행 중인 서버에 연결
- **stdio** → Claude Code CLI가 직접 프로세스를 실행

### HTTP 원격 서버에 연결 (권장)

클로우드 기반 MCP 서버에 연결하기 위해 HTTP 옵션을 사용합니다.

HTTP 방식은 MCP 서버 프로그램을 로컬에서 실행하는 것이 아니라, URL로 접근 가능한 원격 서버에 네트워크로 연결하는 방식입니다.

```bash
# 예시1: Context7에 연결
claude mcp add \
  --scope project \
  --transport http \
  context7 \
  https://mcp.context7.com/mcp

# 예시2: Bearer 토큰을 사용한 예
claude mcp add \
  --scope user \
  --transport http \
  secure-api \
  https://api.example.com/mcp \
  --header "Authorization: Bearer your-token"
```

> ⚠️Windows 사용자 주의
>
> 이 문서의 명령어에서 줄 끝의 `\`는 **macOS/Linux**에서 여러 줄로 나누기 위한 문법입니다. <br/>
> **Windows PowerShell에서는 한 줄로 붙여서 실행하세요.**
>
> ```bash
> # 잘못된 예 (Windows에서 오류)
> claude mcp add \
>   --scope project \
>   sequential-thinking
>
> # 올바른 예 (Windows)
> claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking
> ```

### ~~SSE 서버 전송 이벤트 (Deprecated)~~

SSE (Server-Sent Events) 전송은 더 이상 사용되지 않습니다. 가능한 경우 HTTP 서버를 사용하세요.

```bash
# 실제 예: Asana에 연결
claude mcp add --transport sse asana https://mcp.asana.com/sse
```

### stdio 로컬 프로세스 실행

컴퓨터에서 직접 실행되는 MCP 서버를 추가할 때 사용합니다.

```bash
# 예시: Sequential Thinking 추가
claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking
```

---

## MCP 설치 범위

MCP 서버는 **설치 범위(Scope)에 따라 세 가지로 나뉩니다.**

`--scope` 옵션으로 지정하며, 지정하지 않으면 `local`이 기본값입니다.

| 범위       | 옵션 값         | 설정 파일 위치                   | 설명                              |
|----------|--------------|----------------------------|---------------------------------|
| **로컬**   | `local` (기본) | 현재 작업 디렉토리의 `.claude.json` | 현재 프로젝트에서만 사용, 다른 프로젝트와 공유되지 않음 |
| **프로젝트** | `project`    | 프로젝트 루트의 `.mcp.json`       | 프로젝트 루트에 저장되어 팀원과 공유, Git에 체크인됨 |
| **사용자**  | `user`       | 홈 디렉토리의 `~/.claude.json`   | 모든 프로젝트에서 사용 가능                 |

> 📁 **현재 작업 디렉토리란?** 현재 터미널에서 `claude` 명령을 실행한 폴더 기준입니다. `pwd` 명령으로 현재 위치를 확인할 수 있습니다.
>
> 이전 버전에서는 `project`가 기본값이었으나, 현재는 `local`이 기본값입니다. 또한 이전 `global`은 현재 `user`로 변경되었습니다.

```bash
### 예시
# 로컬 범위 (기본값) - 현재 프로젝트에서만 사용
claude mcp add --transport http stripe https://mcp.stripe.com/mcp

# 프로젝트 범위 - 팀과 공유
claude mcp add --scope project --transport http paypal https://mcp.paypal.com/mcp

# 사용자 범위 - 모든 프로젝트에서 사용
claude mcp add --scope user --transport http hubspot https://mcp.hubspot.com/anthropic
```

MCP 설정은 다음과 같은 표준화된 형식으로 관리됩니다.

```json
{
  "mcpServers": {
    "서버이름": {
      "type": "stdio",
      "command": "/path/to/server",
      "args": [],
      "env": {}
    }
  }
}
```

> 💡 **팁**: 가능하면 `claude mcp add` 명령을 사용하세요. 설정 파일을 직접 수정하는 것은 고급 사용자만 권장합니다.

### 범위 우선순위

동일한 이름의 서버가 여러 범위에 존재할 때 다음 우선순위로 적용됩니다:

**로컬 > 프로젝트 > 사용자**

예: `github`라는 이름의 서버가 로컬과 사용자 범위에 모두 있으면, 로컬 범위의 설정이 우선적으로 사용됩니다.

### 프로젝트 범위 보안 승인

보안상의 이유로 Claude Code CLI는 `.mcp.json` 파일의 프로젝트 범위 서버를 사용하기 전에 **사용자 승인**을 요청합니다.

```bash
# 프로젝트 범위 승인 선택 재설정
claude mcp reset-project-choices
```

### 환경 변수 확장 (프로젝트 범위)

`.mcp.json` 파일에서는 환경 변수를 사용할 수 있습니다:

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

- `${VAR}` - 환경 변수 `VAR`의 값으로 확장
- `${VAR:-default}` - `VAR`가 설정되면 해당 값을, 없으면 `default` 사용

---

## 5. 설정 파일 예시

아래는 `~/.claude.json` 또는 `.mcp.json`에 작성할 수 있는 전체 설정 예시입니다.

```json
{
  "mcpServers": {
    "serena": {
      "type": "stdio",
      "command": "uvx",
      "args": [
        "--from",
        "git+https://github.com/oraios/serena",
        "serena-mcp-server",
        "--enable-web-dashboard",
        "false",
        "start-mcp-server"
      ],
      "env": {}
    },
    "sequential-thinking": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-sequential-thinking"
      ],
      "env": {}
    },
    "shrimp-task-manager": {
      "type": "stdio",
      "command": "node",
      "args": [
        "/Users/사용자명/IdeaProjects/mcp-shrimp-task-manager/dist/index.js"
      ],
      "env": {
        "DATA_DIR": "/path/to/shrimp_data",
        "TEMPLATES_USE": "en",
        "ENABLE_GUI": "false"
      }
    },
    "playwright": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--isolated",
        "--storage-state=/path/to/storage.json"
      ]
    },
    "chrome-devtools": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "chrome-devtools-mcp@latest"
      ]
    },
    "context7": {
      "type": "http",
      "url": "https://mcp.context7.com/mcp"
    }
  }
}
```

> 💡 **팁**: 가능하면 `claude mcp add` 명령을 사용하세요. 설정 파일을 직접 수정하는 것은 고급 사용자만 권장합니다.

---

## 보안 주의사항

⚠️ **MCP 서버 사용 시 반드시 확인하세요:**

1. **외부 프로그램 위험성**
    - MCP 서버는 외부 프로그램입니다
    - 악성 코드를 실행할 수도 있으니 신뢰할 수 있는 출처만 사용하세요

2. **프로젝트 범위 공유**
    - 프로젝트 범위(`project`) MCP는 `.mcp.json` 파일로 팀과 공유됩니다
    - 민감한 정보(API 키 등)는 직접 `.mcp.json`에 작성하지 마세요

3. **환경 변수 사용**
    - API 키, 토큰 등은 환경 변수로 분리하여 관리하세요
    - `${VAR}` 문법을 활용해 설정 파일에서 참조하세요

4. **사용 전 승인**
    - 프로젝트 범위 MCP는 처음 사용할 때 사용자 승인이 필요합니다
    - 승인 전 서버의 동작을 반드시 확인하세요

---

## MCP 서버 관리

구성한 후에는 다음 명령으로 MCP 서버를 관리할 수 있습니다.

```bash
# 구성된 모든 서버 나열
claude mcp list

# 특정 서버의 세부 정보 가져오기
claude mcp get <name>

# 서버 제거
claude mcp remove <name>

# 예시
## Step1 MCP 서버 목록 조회
claude mcp list
Checking MCP server health...

plugin:context7:context7: npx -y @upstash/context7-mcp - ✓ Connected
serena: uvx --from git+https://github.com/oraios/serena serena-mcp-server --enable-web-dashboard false start-mcp-server - ✓ Connected
sequential-thinking: npx -y @modelcontextprotocol/server-sequential-thinking - ✓ Connected
shrimp-task-manager: node /Users/username/IdeaProjects/mcp-shrimp-task-manager/dist/index.js - ✓ Connected
playwright: npx @playwright/mcp@latest --isolated --storage-state=/Users/username/IdeaProjects/squadliterv/.claude/playwright/storage.json - ✓ Connected
chrome-devtools: npx -y chrome-devtools-mcp@latest - ✓ Connected

## Step2 MCP 서버 목록 조회
claude mcp get serena
serena:
  Scope: User config (available in all your projects)
  Status: ✓ Connected
  Type: stdio
  Command: uvx
  Args: --from git+https://github.com/oraios/serena serena-mcp-server --enable-web-dashboard false start-mcp-server
  Environment:

To remove this server, run: claude mcp remove "serena" -s user
```

### Claude Code CLI에서 확인

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

## 추천 MCP 서버

개발 생산성을 높여주는 추천 MCP 서버 목록입니다.

| 서버                      | 설명               | 설치 방법                         | 난이도        |
|-------------------------|------------------|-------------------------------|------------|
| **Context7**            | 최신 라이브러리 문서 조회   | [상세 보기](#context7)            | ⭐ 쉬움       |
| **Sequential Thinking** | 단계적 사고 및 문제 해결   | [상세 보기](#sequential-thinking) | ⭐ 쉬움       |
| **Playwright**          | 브라우저 자동화 및 테스트   | [상세 보기](#playwright)          | ⭐⭐ 보통      |
| **Chrome DevTools**     | 브라우저 디버깅 및 성능 분석 | [상세 보기](#chrome-devtools)     | ⭐⭐ 보통      |
| **Serena**              | 코드베이스 분석 및 심볼 검색 | [상세 보기](#serena)              | ⭐⭐⭐ 어려움    |
| **Shrimp Task Manager** | 작업 관리 및 계획 수립    | [상세 보기](#shrimp-task-manager) | ⭐⭐⭐ 개발자 전용 |

> 더 많은 MCP 서버는 [Claude Code 공식 문서](https://code.claude.com/docs/en/mcp#popular-mcp-servers)에서 확인할 수 있습니다.

---

## 상세 설치 가이드

### Context7 ⭐

최신 라이브러리 문서를 검색하고 코드 예시를 제공하는 MCP 서버입니다.

**필요 조건**

| 프로그램            | 왜 필요한가?                 | 설치 확인              |
|-----------------|-------------------------|--------------------|
| Claude Code CLI | MCP 서버를 추가하고 관리하기 위해 필요 | `claude --version` |

> 💡 **참고**: Context7은 원격 서버에 연결하는 방식이라 별도의 프로그램 설치가 필요 없습니다.

**macOS/Linux**:

```bash
claude mcp add \
  --scope project \
  --transport http \
  context7 \
  https://mcp.context7.com/mcp
```

**Windows**:

```bash
claude mcp add --scope project --transport http context7 https://mcp.context7.com/mcp
```

> 참고:
> - https://github.com/upstash/context7
> - https://github.com/upstash/context7?tab=readme-ov-file#claude-code-local-server-connection

---

### Sequential Thinking ⭐

복잡한 문제를 단계별로 분석하고 해결하는 데 도움을 주는 MCP 서버입니다.

**필요 조건**

| 프로그램            | 왜 필요한가?                                    | 설치 확인              |
|-----------------|--------------------------------------------|--------------------|
| Claude Code CLI | MCP 서버를 추가하고 관리하기 위해 필요                    | `claude --version` |
| Node.js 18+     | MCP 서버 프로그램을 다운로드하고 실행하는 `npx` 명령이 포함되어 있음 | `node -v`          |

```bash
claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking
```

---

### Playwright ⭐⭐

브라우저 자동화, 테스트, 스크린샷 캡처를 위한 MCP 서버입니다.

**필요 조건**

| 프로그램            | 왜 필요한가?                                    | 설치 확인              |
|-----------------|--------------------------------------------|--------------------|
| Claude Code CLI | MCP 서버를 추가하고 관리하기 위해 필요                    | `claude --version` |
| Node.js 18+     | MCP 서버 프로그램을 다운로드하고 실행하는 `npx` 명령이 포함되어 있음 | `node -v`          |

> ⚠️ **참고**: 첫 실행 시 브라우저 다운로드가 진행될 수 있습니다. 네트워크 연결 상태에서 기다려 주세요. 일부 MCP 서버는 메모리 사용량이 높을 수 있습니다.

```bash
claude mcp add --scope project playwright -- npx @playwright/mcp@latest
```

> 참고:
> - https://github.com/microsoft/playwright-mcp

---

### Chrome DevTools ⭐⭐

Chrome 개발자 도구 기능을 사용할 수 있는 MCP 서버입니다.

**필요 조건**

| 프로그램            | 왜 필요한가?                                    | 설치 확인              |
|-----------------|--------------------------------------------|--------------------|
| Claude Code CLI | MCP 서버를 추가하고 관리하기 위해 필요                    | `claude --version` |
| Node.js 18+     | MCP 서버 프로그램을 다운로드하고 실행하는 `npx` 명령이 포함되어 있음 | `node -v`          |

```bash
claude mcp add chrome-devtools -- npx chrome-devtools-mcp@latest
```

> 참고:
> - [github chrome-devtools-mcp](https://github.com/ChromeDevTools/chrome-devtools-mcp)
> - [Chrome DevTools MCP 상세 가이드](https://duck-blog.vercel.app/blog/web/chrome-devtools-mcp-game-changer)

---

### Serena ⭐⭐⭐

코드베이스의 클래스, 함수, 심볼을 검색하고 분석하는 MCP 서버입니다.

**필요 조건**

| 프로그램            | 왜 필요한가?                                          | 설치 확인               |
|-----------------|--------------------------------------------------|---------------------|
| Claude Code CLI | MCP 서버를 추가하고 관리하기 위해 필요                          | `claude --version`  |
| Python 3.10+    | Serena가 Python으로 개발되어 실행에 필요                     | `python3 --version` |
| uv              | Python 프로그램을 쉽게 설치하고 실행하는 도구 (별도 설정 없이 바로 실행 가능) | `uv --version`      |

1. uv 설치
    - Python이 설치되어 있어야 하며, `uv`는 Python 패키지를 별도 설정 없이 바로 실행할 수 있게 해주는 도구입니다.
    ```bash
    # macOS:
    brew install uv

    # Windows:
    python -m pip install uv
    ```
2. MCP 추가
    ```bash
    claude mcp add --scope user serena -- uvx --from git+https://github.com/oraios/serena --context ide-assistant --enable-web-dashboard false serena-mcp-server start-mcp-server
    ```

> 참고:
> - https://oraios.github.io/serena
> - https://oraios.github.io/serena/01-about/000_intro.html

---

### Shrimp Task Manager ⭐⭐⭐ (개발자 전용)

작업을 생성, 관리, 추적할 수 있는 프로젝트 관리 MCP 서버입니다.

**필요 조건**

| 프로그램            | 왜 필요한가?                                    | 설치 확인              |
|-----------------|--------------------------------------------|--------------------|
| Claude Code CLI | MCP 서버를 추가하고 관리하기 위해 필요                    | `claude --version` |
| Node.js 18+     | 소스 코드를 빌드하고 실행하는 `npm`, `node` 명령이 포함되어 있음 | `node -v`          |
| Git             | 소스 코드를 다운로드하기 위해 필요                        | `git --version`    |

> ⚠️ **주의**: 이 서버는 개발자 전용입니다. 소스 코드 빌드 과정이 필요하며, 초보자는 건너뛰세요.

**1. 설치**

```bash
git clone https://github.com/cjo4m06/mcp-shrimp-task-manager.git
cd mcp-shrimp-task-manager
npm install
npm run build
```

**2. 프로젝트 설정** (`.mcp.json`)

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

> 📁 **경로 설정**: 위 경로는 예시입니다. 본인의 OS에 맞게 수정하세요.
> - macOS/Linux: `/Users/사용자명/...` 또는 `/home/사용자명/...`
> - Windows: `C:\Users\사용자명\...`

> 참고:
> - https://github.com/cjo4m06/mcp-shrimp-task-manager

---

## 문제 해결 (Troubleshooting)

### "command not found: claude"

Claude Code CLI가 설치되어 있지 않거나 PATH에 없습니다.

```bash
# Claude Code 재설치
npm install -g @anthropic-ai/claude-code

# PATH 확인
echo $PATH
```

### "command not found: npx"

Node.js가 설치되어 있지 않습니다.

```bash
# 설치 확인
node -v
npm -v

# macOS
brew install node

# Windows: https://nodejs.org 에서 다운로드
```

### Windows에서 `\` 줄바꿈 오류

PowerShell에서는 `\` 줄바꿈이 작동하지 않습니다. 한 줄로 붙여서 실행하세요.

**잘못된 예**:

```bash
claude mcp add \
  --scope project \
  sequential-thinking
```

**올바른 예**:

```bash
claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking
```

### "uv: command not found" (Serena 설치 시)

uv가 설치되어 있지 않습니다.

```bash
# macOS
brew install uv

# Windows
python -m pip install uv
```

### MCP 서버 연결 실패

```bash
# MCP 서버 상태 확인
claude mcp list

# 특정 서버 상세 정보
claude mcp get <서버이름>

# 서버 재시작 (제거 후 재추가)
claude mcp remove <서버이름>
claude mcp add ...
```

---

## 참고 자료

- [Claude Code MCP 공식 문서](https://code.claude.com/docs/en/mcp)

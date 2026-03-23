# Claude Code MCP 가이드

Claude Code CLI 환경에서 MCP(Model Context Protocol) 서버를 연동하는 방법을 설명합니다.

---

## MCP 기본 개념

### MCP란?

**MCP(Model Context Protocol)**는 AI 모델이 외부 도구(Tool)와 통신할 수 있도록 정의된 표준 프로토콜입니다.

- **MCP**: 통신 규칙(Protocol)
- **MCP 서버**: 해당 규칙을 구현한 외부 실행 프로그램

**장점:**
- 특정 모델에 종속되지 않음
- 모델 교체(Claude → Kimi → GPT 등)와 무관하게 동일한 구조 유지
- **MCP 서버는 AI 모델이 호출하여 실제 작업을 수행할 수 있는 외부 도우미 프로그램**

### 동작 구조

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

---

## MCP 서버 추가하기

### 기본 구문

```bash
claude mcp add [option] <name> -- <command> [args...]
```

> ⚠️ `--` 이후의 명령어는 CLI가 실행하는 외부 프로세스입니다.
> - `--` 전: Claude CLI 옵션
> - `--` 후: 실행할 프로그램

### 연결 방식

| 방식 | 실행 위치 | 서버 실행 주체 | 사용 상황 |
|:---|:---|:---|:---|
| **http** | 외부 서버 | 원격에서 이미 실행 중 | 클라우드 기반 MCP 서버 |
| **stdio** | 내 컴퓨터 | Claude Code CLI가 직접 실행 | 로컬에서 직접 실행 |

### HTTP 원격 서버에 연결 (권장)

클로우드 기반 MCP 서버에 연결:

```bash
# 예시: Context7에 연결
claude mcp add \
  --scope project \
  --transport http \
  context7 \
  https://mcp.context7.com/mcp

# 예시: Bearer 토큰 사용
claude mcp add \
  --scope user \
  --transport http \
  secure-api \
  https://api.example.com/mcp \
  --header "Authorization: Bearer your-token"
```

> ⚠️ **Windows 사용자 주의**: 줄 끝의 `\`는 macOS/Linux용입니다. Windows는 한 줄로 실행하세요.

### stdio 로컬 프로세스 실행

컴퓨터에서 직접 실행되는 MCP 서버:

```bash
# 예시: Sequential Thinking 추가
claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking
```

---

## MCP 설치 범위

| 범위 | 옵션 값 | 설정 파일 위치 | 설명 |
|:---|:---|:---|:---|
| **로컬** | `local` (기본) | 현재 작업 디렉토리의 `.claude.json` | 현재 프로젝트에서만 사용 |
| **프로젝트** | `project` | 프로젝트 루트의 `.mcp.json` | 팀원과 공유, Git에 체크인됨 |
| **사용자** | `user` | 홈 디렉토리의 `~/.claude.json` | 모든 프로젝트에서 사용 가능 |

### 범위 우선순위

동일한 이름의 서버가 여러 범위에 존재할 때:

**로컬 > 프로젝트 > 사용자**

### 프로젝트 범위 보안 승인

`.mcp.json` 파일의 프로젝트 범위 서버는 처음 사용할 때 **사용자 승인**이 필요합니다.

```bash
# 프로젝트 범위 승인 선택 재설정
claude mcp reset-project-choices
```

### 환경 변수 확장

`.mcp.json` 파일에서 환경 변수 사용:

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

- `${VAR}`: 환경 변수 `VAR`의 값으로 확장
- `${VAR:-default}`: `VAR`가 설정되면 해당 값을, 없으면 `default` 사용

---

## 설정 파일 예시

```json
{
  "mcpServers": {
    "sequential-thinking": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-sequential-thinking"
      ],
      "env": {}
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

---

## MCP 서버 관리

```bash
# 구성된 모든 서버 나열
claude mcp list

# 특정 서버의 세부 정보 가져오기
claude mcp get <name>

# 서버 제거
claude mcp remove <name>
```

### Claude Code CLI에서 확인

```bash
claude

# 진입 후
❯ /mcp
─────────────────────────────────────────────────────────────────
Manage MCP servers
6 servers

User MCPs
❯ chrome-devtools · ✔ connected
  sequential-thinking · ✔ connected
  ...
```

---

## 추천 MCP 서버

| 서버 | 설명 | 설치 방법 | 난이도 |
|:---|:---|:---|:---|
| **Context7** | 최신 라이브러리 문서 조회 | `claude mcp add --scope project --transport http context7 https://mcp.context7.com/mcp` | ⭐ 쉬움 |
| **Sequential Thinking** | 단계적 사고 및 문제 해결 | `claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking` | ⭐ 쉬움 |
| **Playwright** | 브라우저 자동화 및 테스트 | `claude mcp add --scope project playwright -- npx @playwright/mcp@latest` | ⭐⭐ 보통 |
| **Chrome DevTools** | 브라우저 디버깅 및 성능 분석 | `claude mcp add chrome-devtools -- npx chrome-devtools-mcp@latest` | ⭐⭐ 보통 |
| **Serena** | 코드베이스 분석 및 심볼 검색 | `claude mcp add --scope user serena -- uvx --from git+https://github.com/oraios/serena serena-mcp-server start-mcp-server` | ⭐⭐⭐ 어려움 |

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

## 문제 해결

### "command not found: claude"

```bash
# Claude Code 재설치
npm install -g @anthropic-ai/claude-code
```

### "command not found: npx"

```bash
# macOS
brew install node

# Windows: https://nodejs.org 에서 다운로드
```

### Windows에서 `\` 줄바꿈 오류

PowerShell에서는 `\` 줄바꿈이 작동하지 않습니다. 한 줄로 붙여서 실행하세요.

**잘못된 예:**
```bash
claude mcp add \
  --scope project \
  sequential-thinking
```

**올바른 예:**
```bash
claude mcp add --scope project sequential-thinking -- npx -y @modelcontextprotocol/server-sequential-thinking
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
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)

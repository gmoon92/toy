# Claude Code Channel 완벽 가이드

외부 시스템에서 Claude Code 세션으로 이벤트를 실시간으로 전달하는 방법을 알아봅니다. CI/CD 알림, 모니터링 경고, 커스텀 웹훅까지 — HTTP POST만 별 수 있다면 무엇이든 Claude에게 직접
전달할 수 있습니다.

---

## 시작하기

### 준비물

| 항목      | 설명                                                                                         |
|:--------|:-------------------------------------------------------------------------------------------|
| MCP SDK | [`@modelcontextprotocol/sdk`](https://www.npmjs.com/package/@modelcontextprotocol/sdk) 패키지 |
| 런타임     | [Bun](https://bun.sh), [Node](https://nodejs.org), [Deno](https://deno.com) 중 선택           |

> 미리 빌드된 플러그인은 Bun을 사용하지만, 직접 만드는 채널은 어떤 런타임이든 가능합니다.

### 채널 서버의 3가지 역할

채널 서버는 다음 세 가지를 수행합니다:

1. **`claude/channel` 기능 선언** — Claude Code가 이벤트를 구독하도록 합니다
2. **이벤트 발행** — `notifications/claude/channel`로 이벤트를 전달합니다
3. **stdio 트랜스포트 연결** — Claude Code가 서버를 하위 프로세스로 실행합니다

> **참고**: 리서치 프리뷰 기간 동안 커스텀 채널은 허용 목록에 포함되어 있지 않습니다. 로컬 테스트 시 `--dangerously-load-development-channels` 플래그가 필요합니다.

---

## 실전 예제: 웹훅 채널 만들기

HTTP 요청을 받아 Claude Code 세션으로 전달하는 채널을 직접 구축합니다.

### 1단계: 프로젝트 생성

```bash
mkdir webhook-channel && cd webhook-channel
bun add @modelcontextprotocol/sdk
```

### 2단계: 채널 서버 작성

`webhook.ts` 파일을 생성합니다:

```ts
#!/usr/bin/env bun
import {Server} from '@modelcontextprotocol/sdk/server/index.js'
import {StdioServerTransport} from '@modelcontextprotocol/sdk/server/stdio.js'

// MCP 서버 생성 및 채널로 선언
const mcp = new Server(
    {name: 'webhook', version: '0.0.1'},
    {
        // 이 키가 채널로 만드는 핵심 — Claude Code가 리스너를 등록합니다
        capabilities: {experimental: {'claude/channel': {}}},
        // Claude의 시스템 프롬프트에 추가 — 이벤트 처리 방법 안내
        instructions: 'Events from the webhook channel arrive as <channel source="webhook" ...>. They are one-way: read them and act, no reply expected.',
    },
)

// stdio로 Claude Code에 연결 (Claude Code가 이 프로세스를 실행)
await mcp.connect(new StdioServerTransport())

// HTTP 서버 시작 — 모든 POST를 Claude에게 전달
Bun.serve({
    port: 8788,
    hostname: '127.0.0.1',  // localhost 전용: 외부 접근 차단
    async fetch(req) {
        const body = await req.text()
        await mcp.notification({
            method: 'notifications/claude/channel',
            params: {
                content: body,  // <channel> 태그의 본문이 됨
                // 각 키는 태그 속성이 됩니다, 예: <channel path="/" method="POST">
                meta: {path: new URL(req.url).pathname, method: req.method},
            },
        })
        return new Response('ok')
    },
})
```

**코드 설명:**

- **서버 설정**: `claude/channel`을 capabilities에 포함시켜 "이건 채널입니다"라고 알립니다. `instructions`는 Claude의 시스템 프롬프트에 삽입되어 이벤트를 어떻게
  처리할지 안내합니다.
- **Stdio 연결**: Claude Code가 서버를 하위 프로세스로 실행할 수 있도록 stdin/stdout으로 연결합니다.
- **HTTP 리스너**: 포트 8788에서 대기하며, POST 본문을 `mcp.notification()`을 통해 채널 이벤트로 전달합니다. `content`는 이벤트 본문이 되고, 각 `meta` 항목은
  `<channel>` 태그의 속성이 됩니다.

### 3단계: Claude Code에 서버 등록

`.mcp.json` 파일에 서버를 추가합니다. 프로젝트 수준 `.mcp.json`에는 상대 경로를, 사용자 수준 `~/.mcp.json`에는 전체 절대 경로를 사용하세요:

```json
{
  "mcpServers": {
    "webhook": {
      "command": "bun",
      "args": [
        "./webhook.ts"
      ]
    }
  }
}
```

### 4단계: 테스트

개발 플래그와 함께 Claude Code를 시작합니다:

```bash
claude --dangerously-load-development-channels server:webhook
```

Claude Code가 자동으로 서버를 실행하고 HTTP 리스너를 시작합니다. 별도로 실행할 필요가 없습니다.

별도 터미널에서 테스트:

```bash
curl -X POST localhost:8788 -d "build failed on main: https://ci.example.com/run/1234"
```

Claude Code 터미널에서 다음과 같이 표시됩니다:

```text
<channel source="webhook" path="/" method="POST">build failed on main: https://ci.example.com/run/1234</channel>
```

Claude가 메시지를 수신하고 파일 읽기, 명령 실행 등 적절히 응답합니다. 이는 단방향 채널이므로 Claude는 세션 내에서 동작하지만 웹훅으로 다시 응답하지는 않습니다.

---

## 리서치 프리뷰 중 테스트하기

리서치 프리뷰 기간 동안 모든 채널은 승인된 허용 목록에 있어야 합니다. 개발 플래그는 확인 프롬프트 후 허용 목록을 우회합니다.

```bash
# 개발 중인 플러그인 테스트
claude --dangerously-load-development-channels plugin:yourplugin@yourmarketplace

# 순수 .mcp.json 서버 테스트
claude --dangerously-load-development-channels server:webhook
```

**주의사항:**

- 우회는 항목별로 적용됩니다 (`--channels`와 함께 사용하면 해당 항목에는 적용되지 않습니다)
- 이 플래그는 허용 목록만 우회합니다. 조직의 `channelsEnabled` 정책은 여전히 적용됩니다
- "blocked by org policy"가 표시되면 Team/Enterprise 관리자가 먼저 채널을 활성화해야 합니다
- 신뢰할 수 없는 출처의 채널을 실행하는 데 사용하지 마세요

---

## 서버 옵션

채널은 `Server` 생성자에서 다음 옵션을 설정합니다:

| 필드                                            | 타입       | 설명                                                                                                                             |
|:----------------------------------------------|:---------|:-------------------------------------------------------------------------------------------------------------------------------|
| `capabilities.experimental['claude/channel']` | `object` | **필수**. 항상 `{}`. 존재하면 알림 리스너를 등록합니다.                                                                                           |
| `capabilities.tools`                          | `object` | **양방향 전용**. 항상 `{}`. 표준 MCP 도구 기능.                                                                                             |
| `instructions`                                | `string` | **권장**. Claude의 시스템 프롬프트에 추가됩니다. Claude에게 어떤 이벤트를 예상해야 하는지, `<channel>` 태그 속성이 무엇을 의미하는지, 답장해야 하는지, 그리고 어떤 도구를 사용해야 하는지 알려줍니다. |

단방향 채널을 생성하려면 `capabilities.tools`를 생략하세요. 양방향 설정 예시:

```ts
const mcp = new Server(
    {name: 'your-channel', version: '0.0.1'},
    {
        capabilities: {
            experimental: {'claude/channel': {}},  // 채널 리스너 등록
            tools: {},  // 단방향 채널의 경우 생략
        },
        // Claude의 시스템 프롬프트에 추가되어 이벤트를 처리하는 방법을 알 수 있게 합니다
        instructions: 'Messages arrive as <channel source="your-channel" ...>. Reply with the reply tool.',
    },
)
```

---

## 알림 형식

서버는 `notifications/claude/channel` 메서드로 이벤트를 전송합니다:

| 필드        | 타입                       | 설명                                                                                                                                         |
|:----------|:-------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------|
| `content` | `string`                 | 이벤트 본문. `<channel>` 태그의 본문으로 전달됩니다.                                                                                                        |
| `meta`    | `Record<string, string>` | 선택사항. 각 항목은 라우팅 컨텍스트(chat ID, 발신자 이름, 알림 심각도 등)를 위해 `<channel>` 태그의 속성이 됩니다. 키는 식별자여야 합니다(문자, 숫자, 밑줄만 허용). 하이픈이나 다른 문자를 포함하는 키는 조용히 삭제됩니다. |

예시 — CI 실패 알림:

```ts
await mcp.notification({
    method: 'notifications/claude/channel',
    params: {
        content: 'build failed on main: https://ci.example.com/run/1234',
        meta: {severity: 'high', run_id: '1234'},
    },
})
```

Claude의 컨텍스트에 도착하는 형태:

```text
<channel source="your-channel" severity="high" run_id="1234">
build failed on main: https://ci.example.com/run/1234
</channel>
```

---

## 참고 자료

- [Claude Code - Channels 공식 문서](https://code.claude.com/docs/en/channels-reference)
- [Google Chat Webhook 가이드](https://developers.google.com/chat/how-tos/webhooks)

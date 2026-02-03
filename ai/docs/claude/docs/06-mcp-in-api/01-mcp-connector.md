# [MCP 커넥터](https://platform.claude.com/docs/en/agents-and-tools/mcp-connector)

---

Claude의 Model Context Protocol(MCP) 커넥터 기능을 사용하면 별도의 MCP 클라이언트 없이 Messages API에서 직접 원격 MCP 서버에 연결할 수 있습니다.


> **현재 버전**: 이 기능을 사용하려면 베타 헤더가 필요합니다: `"anthropic-beta": "mcp-client-2025-11-20"`
>
> 이전 버전(`mcp-client-2025-04-04`)은 더 이상 지원되지 않습니다. 아래의 [더 이상 지원되지 않는 버전 문서](#deprecated-version-mcp-client-2025-04-04)를 참조하세요.


## 주요 기능

- **직접 API 통합**: MCP 클라이언트 구현 없이 MCP 서버에 연결
- **도구 호출 지원**: Messages API를 통해 MCP 도구에 액세스
- **유연한 도구 구성**: 모든 도구를 활성화하거나, 특정 도구를 허용 목록에 추가하거나, 원하지 않는 도구를 거부 목록에 추가
- **도구별 구성**: 개별 도구를 사용자 정의 설정으로 구성
- **OAuth 인증**: 인증된 서버를 위한 OAuth Bearer 토큰 지원
- **다중 서버**: 단일 요청에서 여러 MCP 서버에 연결

## 제한 사항

- [MCP 사양](https://modelcontextprotocol.io/introduction#explore-mcp)의 기능 중 현재는 [도구 호출](https://modelcontextprotocol.io/docs/concepts/tools)만 지원됩니다.
- 서버는 HTTP를 통해 공개적으로 노출되어야 합니다(Streamable HTTP 및 SSE 전송 모두 지원). 로컬 STDIO 서버는 직접 연결할 수 없습니다.
- MCP 커넥터는 현재 Amazon Bedrock 및 Google Vertex에서 지원되지 않습니다.

## Messages API에서 MCP 커넥터 사용하기

MCP 커넥터는 두 가지 구성 요소를 사용합니다:

1. **MCP 서버 정의** (`mcp_servers` 배열): 서버 연결 세부 정보(URL, 인증) 정의
2. **MCP 도구셋** (`tools` 배열): 활성화할 도구와 구성 방법 설정

### 기본 예제

이 예제는 기본 구성으로 MCP 서버의 모든 도구를 활성화합니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
  -H "Content-Type: application/json" \
  -H "X-API-Key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: mcp-client-2025-11-20" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1000,
    "messages": [{"role": "user", "content": "What tools do you have available?"}],
    "mcp_servers": [
      {
        "type": "url",
        "url": "https://example-server.modelcontextprotocol.io/sse",
        "name": "example-mcp",
        "authorization_token": "YOUR_TOKEN"
      }
    ],
    "tools": [
      {
        "type": "mcp_toolset",
        "mcp_server_name": "example-mcp"
      }
    ]
  }'
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1000,
  messages: [
    {
      role: "user",
      content: "What tools do you have available?",
    },
  ],
  mcp_servers: [
    {
      type: "url",
      url: "https://example-server.modelcontextprotocol.io/sse",
      name: "example-mcp",
      authorization_token: "YOUR_TOKEN",
    },
  ],
  tools: [
    {
      type: "mcp_toolset",
      mcp_server_name: "example-mcp",
    },
  ],
  betas: ["mcp-client-2025-11-20"],
});
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1000,
    messages=[{
        "role": "user",
        "content": "What tools do you have available?"
    }],
    mcp_servers=[{
        "type": "url",
        "url": "https://mcp.example.com/sse",
        "name": "example-mcp",
        "authorization_token": "YOUR_TOKEN"
    }],
    tools=[{
        "type": "mcp_toolset",
        "mcp_server_name": "example-mcp"
    }],
    betas=["mcp-client-2025-11-20"]
)
```
</CodeGroup>

## MCP 서버 구성

`mcp_servers` 배열의 각 MCP 서버는 연결 세부 정보를 정의합니다:

```json
{
  "type": "url",
  "url": "https://example-server.modelcontextprotocol.io/sse",
  "name": "example-mcp",
  "authorization_token": "YOUR_TOKEN"
}
```

### 필드 설명

| 속성 | 타입 | 필수 | 설명 |
|----------|------|----------|-------------|
| `type` | string | 예 | 현재는 "url"만 지원됩니다 |
| `url` | string | 예 | MCP 서버의 URL. https://로 시작해야 합니다 |
| `name` | string | 예 | 이 MCP 서버의 고유 식별자. `tools` 배열의 정확히 하나의 MCPToolset에서 참조되어야 합니다. |
| `authorization_token` | string | 아니오 | MCP 서버에서 요구하는 경우 OAuth 인증 토큰. [MCP 사양](https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization)을 참조하세요. |

## MCP 도구셋 구성

MCPToolset은 `tools` 배열 내에 위치하며 MCP 서버에서 어떤 도구를 활성화할지와 어떻게 구성할지를 설정합니다.

### 기본 구조

```json
{
  "type": "mcp_toolset",
  "mcp_server_name": "example-mcp",
  "default_config": {
    "enabled": true,
    "defer_loading": false
  },
  "configs": {
    "specific_tool_name": {
      "enabled": true,
      "defer_loading": true
    }
  }
}
```

### 필드 설명

| 속성 | 타입 | 필수 | 설명 |
|----------|------|----------|-------------|
| `type` | string | 예 | "mcp_toolset"이어야 합니다 |
| `mcp_server_name` | string | 예 | `mcp_servers` 배열에 정의된 서버 이름과 일치해야 합니다 |
| `default_config` | object | 아니오 | 이 세트의 모든 도구에 적용되는 기본 구성. `configs`의 개별 도구 구성이 이 기본값을 재정의합니다. |
| `configs` | object | 아니오 | 도구별 구성 재정의. 키는 도구 이름이고 값은 구성 객체입니다. |
| `cache_control` | object | 아니오 | 이 도구셋의 캐시 중단점 구성 |

### 도구 구성 옵션

각 도구(`default_config` 또는 `configs`에 구성됨)는 다음 필드를 지원합니다:

| 속성 | 타입 | 기본값 | 설명 |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | 이 도구의 활성화 여부 |
| `defer_loading` | boolean | `false` | true인 경우, 도구 설명이 처음에 모델에 전송되지 않습니다. [도구 검색 도구](../03-tools/12-tool-search-tool.md)와 함께 사용됩니다. |

### 구성 병합

구성 값은 다음 우선순위로 병합됩니다(높은 순서부터):

1. `configs`의 도구별 설정
2. 세트 수준의 `default_config`
3. 시스템 기본값

예제:

```json
{
  "type": "mcp_toolset",
  "mcp_server_name": "google-calendar-mcp",
  "default_config": {
    "defer_loading": true
  },
  "configs": {
    "search_events": {
      "enabled": false
    }
  }
}
```

결과:
- `search_events`: `enabled: false`(configs에서), `defer_loading: true`(default_config에서)
- 다른 모든 도구: `enabled: true`(시스템 기본값), `defer_loading: true`(default_config에서)

## 일반적인 구성 패턴

### 기본 구성으로 모든 도구 활성화

가장 간단한 패턴 - 서버의 모든 도구를 활성화합니다:

```json
{
  "type": "mcp_toolset",
  "mcp_server_name": "google-calendar-mcp",
}
```

### 허용 목록 - 특정 도구만 활성화

기본값으로 `enabled: false`를 설정한 다음 특정 도구를 명시적으로 활성화합니다:

```json
{
  "type": "mcp_toolset",
  "mcp_server_name": "google-calendar-mcp",
  "default_config": {
    "enabled": false
  },
  "configs": {
    "search_events": {
      "enabled": true
    },
    "create_event": {
      "enabled": true
    }
  }
}
```

### 거부 목록 - 특정 도구 비활성화

기본적으로 모든 도구를 활성화한 다음 원하지 않는 도구를 명시적으로 비활성화합니다:

```json
{
  "type": "mcp_toolset",
  "mcp_server_name": "google-calendar-mcp",
  "configs": {
    "delete_all_events": {
      "enabled": false
    },
    "share_calendar_publicly": {
      "enabled": false
    }
  }
}
```

### 혼합 - 도구별 구성이 포함된 허용 목록

허용 목록과 각 도구의 사용자 정의 구성을 결합합니다:

```json
{
  "type": "mcp_toolset",
  "mcp_server_name": "google-calendar-mcp",
  "default_config": {
    "enabled": false,
    "defer_loading": true
  },
  "configs": {
    "search_events": {
      "enabled": true,
      "defer_loading": false
    },
    "list_events": {
      "enabled": true
    }
  }
}
```

이 예제에서:
- `search_events`는 `defer_loading: false`로 활성화됩니다
- `list_events`는 `defer_loading: true`(default_config에서 상속)로 활성화됩니다
- 다른 모든 도구는 비활성화됩니다

## 검증 규칙

API는 다음 검증 규칙을 적용합니다:

- **서버가 존재해야 함**: MCPToolset의 `mcp_server_name`은 `mcp_servers` 배열에 정의된 서버와 일치해야 합니다
- **서버가 사용되어야 함**: `mcp_servers`에 정의된 모든 MCP 서버는 정확히 하나의 MCPToolset에서 참조되어야 합니다
- **서버당 고유한 도구셋**: 각 MCP 서버는 하나의 MCPToolset에서만 참조될 수 있습니다
- **알 수 없는 도구 이름**: `configs`의 도구 이름이 MCP 서버에 존재하지 않으면 백엔드 경고가 기록되지만 오류는 반환되지 않습니다(MCP 서버는 동적 도구 가용성을 가질 수 있음)

## 응답 콘텐츠 타입

Claude가 MCP 도구를 사용할 때 응답에는 두 가지 새로운 콘텐츠 블록 타입이 포함됩니다:

### MCP 도구 사용 블록

```json
{
  "type": "mcp_tool_use",
  "id": "mcptoolu_014Q35RayjACSWkSj4X2yov1",
  "name": "echo",
  "server_name": "example-mcp",
  "input": { "param1": "value1", "param2": "value2" }
}
```

### MCP 도구 결과 블록

```json
{
  "type": "mcp_tool_result",
  "tool_use_id": "mcptoolu_014Q35RayjACSWkSj4X2yov1",
  "is_error": false,
  "content": [
    {
      "type": "text",
      "text": "Hello"
    }
  ]
}
```

## 다중 MCP 서버

`mcp_servers`에 여러 서버 정의를 포함하고 `tools` 배열에 각각에 대한 해당 MCPToolset을 포함하여 여러 MCP 서버에 연결할 수 있습니다:

```json
{
  "model": "claude-sonnet-4-5",
  "max_tokens": 1000,
  "messages": [
    {
      "role": "user",
      "content": "Use tools from both mcp-server-1 and mcp-server-2 to complete this task"
    }
  ],
  "mcp_servers": [
    {
      "type": "url",
      "url": "https://mcp.example1.com/sse",
      "name": "mcp-server-1",
      "authorization_token": "TOKEN1"
    },
    {
      "type": "url",
      "url": "https://mcp.example2.com/sse",
      "name": "mcp-server-2",
      "authorization_token": "TOKEN2"
    }
  ],
  "tools": [
    {
      "type": "mcp_toolset",
      "mcp_server_name": "mcp-server-1"
    },
    {
      "type": "mcp_toolset",
      "mcp_server_name": "mcp-server-2",
      "default_config": {
        "defer_loading": true
      }
    }
  ]
}
```

## 인증

OAuth 인증이 필요한 MCP 서버의 경우 액세스 토큰을 얻어야 합니다. MCP 커넥터 베타는 MCP 서버 정의에서 `authorization_token` 매개변수 전달을 지원합니다.
API 소비자는 API 호출을 하기 전에 OAuth 흐름을 처리하고 액세스 토큰을 얻어야 하며, 필요에 따라 토큰을 갱신해야 합니다.

### 테스트용 액세스 토큰 얻기

MCP 인스펙터는 테스트 목적으로 액세스 토큰을 얻는 과정을 안내합니다.

1. 다음 명령으로 인스펙터를 실행합니다. 머신에 Node.js가 설치되어 있어야 합니다.

   ```bash
   npx @modelcontextprotocol/inspector
   ```

2. 왼쪽 사이드바에서 "Transport type"으로 "SSE" 또는 "Streamable HTTP"를 선택합니다.
3. MCP 서버의 URL을 입력합니다.
4. 오른쪽 영역에서 "Need to configure authentication?" 뒤의 "Open Auth Settings" 버튼을 클릭합니다.
5. "Quick OAuth Flow"를 클릭하고 OAuth 화면에서 인증합니다.
6. 인스펙터의 "OAuth Flow Progress" 섹션의 단계를 따르고 "Authentication complete"에 도달할 때까지 "Continue"를 클릭합니다.
7. `access_token` 값을 복사합니다.
8. MCP 서버 구성의 `authorization_token` 필드에 붙여넣습니다.

### 액세스 토큰 사용

위의 OAuth 흐름 중 하나를 사용하여 액세스 토큰을 얻은 후에는 MCP 서버 구성에서 사용할 수 있습니다:

```json
{
  "mcp_servers": [
    {
      "type": "url",
      "url": "https://example-server.modelcontextprotocol.io/sse",
      "name": "authenticated-server",
      "authorization_token": "YOUR_ACCESS_TOKEN_HERE"
    }
  ]
}
```

OAuth 흐름에 대한 자세한 설명은 MCP 사양의 [인증 섹션](https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization)을 참조하세요.

## 마이그레이션 가이드

더 이상 지원되지 않는 `mcp-client-2025-04-04` 베타 헤더를 사용하고 있다면 이 가이드를 따라 새 버전으로 마이그레이션하세요.

### 주요 변경 사항

1. **새 베타 헤더**: `mcp-client-2025-04-04`에서 `mcp-client-2025-11-20`으로 변경
2. **도구 구성 이동**: 도구 구성이 이제 MCP 서버 정의가 아닌 `tools` 배열에 MCPToolset 객체로 위치합니다
3. **더 유연한 구성**: 새 패턴은 허용 목록, 거부 목록 및 도구별 구성을 지원합니다

### 마이그레이션 단계

**이전 (더 이상 지원되지 않음):**

```json
{
  "model": "claude-sonnet-4-5",
  "max_tokens": 1000,
  "messages": [...],
  "mcp_servers": [
    {
      "type": "url",
      "url": "https://mcp.example.com/sse",
      "name": "example-mcp",
      "authorization_token": "YOUR_TOKEN",
      "tool_configuration": {
        "enabled": true,
        "allowed_tools": ["tool1", "tool2"]
      }
    }
  ]
}
```

**이후 (현재):**

```json
{
  "model": "claude-sonnet-4-5",
  "max_tokens": 1000,
  "messages": [...],
  "mcp_servers": [
    {
      "type": "url",
      "url": "https://mcp.example.com/sse",
      "name": "example-mcp",
      "authorization_token": "YOUR_TOKEN"
    }
  ],
  "tools": [
    {
      "type": "mcp_toolset",
      "mcp_server_name": "example-mcp",
      "default_config": {
        "enabled": false
      },
      "configs": {
        "tool1": {
          "enabled": true
        },
        "tool2": {
          "enabled": true
        }
      }
    }
  ]
}
```

### 일반적인 마이그레이션 패턴

| 이전 패턴 | 새 패턴 |
|-------------|-------------|
| `tool_configuration` 없음 (모든 도구 활성화) | `default_config` 또는 `configs` 없는 MCPToolset |
| `tool_configuration.enabled: false` | `default_config.enabled: false`인 MCPToolset |
| `tool_configuration.allowed_tools: [...]` | `default_config.enabled: false`이고 `configs`에서 특정 도구가 활성화된 MCPToolset |

## 더 이상 지원되지 않는 버전: mcp-client-2025-04-04


> 이 버전은 더 이상 지원되지 않습니다. 위의 [마이그레이션 가이드](#migration-guide)를 사용하여 `mcp-client-2025-11-20`으로 마이그레이션하세요.


이전 버전의 MCP 커넥터는 도구 구성을 MCP 서버 정의에 직접 포함했습니다:

```json
{
  "mcp_servers": [
    {
      "type": "url",
      "url": "https://example-server.modelcontextprotocol.io/sse",
      "name": "example-mcp",
      "authorization_token": "YOUR_TOKEN",
      "tool_configuration": {
        "enabled": true,
        "allowed_tools": ["example_tool_1", "example_tool_2"]
      }
    }
  ]
}
```

### 더 이상 지원되지 않는 필드 설명

| 속성 | 타입 | 설명 |
|----------|------|-------------|
| `tool_configuration` | object | **더 이상 지원되지 않음**: 대신 `tools` 배열의 MCPToolset을 사용하세요 |
| `tool_configuration.enabled` | boolean | **더 이상 지원되지 않음**: MCPToolset의 `default_config.enabled`를 사용하세요 |
| `tool_configuration.allowed_tools` | array | **더 이상 지원되지 않음**: MCPToolset의 `configs`와 함께 허용 목록 패턴을 사용하세요 |

# [Tool search tool](https://platform.claude.com/docs/en/agents-and-tools/tool-use/tool-search-tool)

---

Tool search tool은 Claude가 수백 또는 수천 개의 도구를 동적으로 검색하고 필요에 따라 로드하여 작업할 수 있도록 합니다. 모든 도구 정의를 미리 컨텍스트 윈도우에 로드하는 대신, Claude는 도구 이름, 설명, 인수 이름, 인수 설명을 포함한 도구 카탈로그를 검색하고 필요한 도구만 로드합니다.

이 접근 방식은 도구 라이브러리가 확장될 때 발생하는 두 가지 중요한 과제를 해결합니다:

- **컨텍스트 효율성**: 도구 정의는 컨텍스트 윈도우의 상당 부분을 소비할 수 있으며(50개 도구 ≈ 10-20K 토큰), 실제 작업을 위한 공간이 줄어듭니다
- **도구 선택 정확도**: 기존 방식으로 사용 가능한 도구가 30-50개를 초과하면 Claude의 도구 선택 능력이 크게 저하됩니다

이것은 서버 측 도구로 제공되지만, 클라이언트 측 도구 검색 기능을 직접 구현할 수도 있습니다. 자세한 내용은 [커스텀 도구 검색 구현](#custom-tool-search-implementation)을 참조하세요.


> Tool search tool은 현재 공개 베타 버전입니다. 제공자에 맞는 적절한 [베타 헤더](/docs/en/api/beta-headers)를 포함하세요:
>
> | 제공자                 | 베타 헤더                    | 지원 모델                       |
> | ------------------------ | ------------------------------ | -------------------------------------- |
> | Claude API<br/>Microsoft Foundry  | `advanced-tool-use-2025-11-20` | Claude Opus 4.5<br />Claude Sonnet 4.5 |
> | Google Cloud's Vertex AI | `tool-search-tool-2025-10-19`  | Claude Opus 4.5<br />Claude Sonnet 4.5 |
> | Amazon Bedrock           | `tool-search-tool-2025-10-19`  | Claude Opus 4.5<br />Claude Sonnet 4.5 |
>
> 이 기능에 대한 피드백을 [피드백 양식](https://forms.gle/MhcGFFwLxuwnWTkYA)을 통해 공유해 주세요.



> Amazon Bedrock에서 서버 측 도구 검색은 converse API가 아닌 [invoke
> API](https://docs.aws.amazon.com/bedrock/latest/userguide/bedrock-runtime_example_bedrock-runtime_InvokeModel_AnthropicClaude_section.html)를 통해서만 사용할 수 있습니다.


자체 검색 구현에서 `tool_reference` 블록을 반환하여 [클라이언트 측 도구 검색](#custom-tool-search-implementation)을 구현할 수도 있습니다.

## Tool search의 작동 방식

두 가지 도구 검색 변형이 있습니다:

- **Regex** (`tool_search_tool_regex_20251119`): Claude가 정규식 패턴을 구성하여 도구를 검색합니다
- **BM25** (`tool_search_tool_bm25_20251119`): Claude가 자연어 쿼리를 사용하여 도구를 검색합니다

Tool search tool을 활성화하면:

1. 도구 목록에 tool search tool(예: `tool_search_tool_regex_20251119` 또는 `tool_search_tool_bm25_20251119`)을 포함합니다
2. 즉시 로드되지 않아야 하는 도구에 대해 `defer_loading: true`로 모든 도구 정의를 제공합니다
3. Claude는 초기에 tool search tool과 지연되지 않은 도구만 볼 수 있습니다
4. Claude가 추가 도구가 필요할 때 tool search tool을 사용하여 검색합니다
5. API는 가장 관련성 높은 3-5개의 `tool_reference` 블록을 반환합니다
6. 이러한 참조는 자동으로 전체 도구 정의로 확장됩니다
7. Claude는 검색된 도구 중에서 선택하고 호출합니다

이는 컨텍스트 윈도우를 효율적으로 유지하면서 높은 도구 선택 정확도를 유지합니다.

## 빠른 시작

다음은 지연된 도구를 사용하는 간단한 예제입니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: advanced-tool-use-2025-11-20" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5-20250929",
        "max_tokens": 2048,
        "messages": [
            {
                "role": "user",
                "content": "What is the weather in San Francisco?"
            }
        ],
        "tools": [
            {
                "type": "tool_search_tool_regex_20251119",
                "name": "tool_search_tool_regex"
            },
            {
                "name": "get_weather",
                "description": "Get the weather at a specific location",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "location": {"type": "string"},
                        "unit": {
                            "type": "string",
                            "enum": ["celsius", "fahrenheit"]
                        }
                    },
                    "required": ["location"]
                },
                "defer_loading": true
            },
            {
                "name": "search_files",
                "description": "Search through files in the workspace",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "query": {"type": "string"},
                        "file_types": {
                            "type": "array",
                            "items": {"type": "string"}
                        }
                    },
                    "required": ["query"]
                },
                "defer_loading": true
            }
        ]
    }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.beta.messages.create(
    model="claude-sonnet-4-5-20250929",
    betas=["advanced-tool-use-2025-11-20"],
    max_tokens=2048,
    messages=[
        {
            "role": "user",
            "content": "What is the weather in San Francisco?"
        }
    ],
    tools=[
        {
            "type": "tool_search_tool_regex_20251119",
            "name": "tool_search_tool_regex"
        },
        {
            "name": "get_weather",
            "description": "Get the weather at a specific location",
            "input_schema": {
                "type": "object",
                "properties": {
                    "location": {"type": "string"},
                    "unit": {
                        "type": "string",
                        "enum": ["celsius", "fahrenheit"]
                    }
                },
                "required": ["location"]
            },
            "defer_loading": True
        },
        {
            "name": "search_files",
            "description": "Search through files in the workspace",
            "input_schema": {
                "type": "object",
                "properties": {
                    "query": {"type": "string"},
                    "file_types": {
                        "type": "array",
                        "items": {"type": "string"}
                    }
                },
                "required": ["query"]
            },
            "defer_loading": True
        }
    ]
)

print(response)
```

```typescript TypeScript
import Anthropic from "@anthropic-ai/sdk";

const client = new Anthropic();

async function main() {
  const response = await client.beta.messages.create({
    model: "claude-sonnet-4-5-20250929",
    betas: ["advanced-tool-use-2025-11-20"],
    max_tokens: 2048,
    messages: [
      {
        role: "user",
        content: "What is the weather in San Francisco?",
      },
    ],
    tools: [
      {
        type: "tool_search_tool_regex_20251119",
        name: "tool_search_tool_regex",
      },
      {
        name: "get_weather",
        description: "Get the weather at a specific location",
        input_schema: {
          type: "object",
          properties: {
            location: { type: "string" },
            unit: {
              type: "string",
              enum: ["celsius", "fahrenheit"],
            },
          },
          required: ["location"],
        },
        defer_loading: true,
      },
      {
        name: "search_files",
        description: "Search through files in the workspace",
        input_schema: {
          type: "object",
          properties: {
            query: { type: "string" },
            file_types: {
              type: "array",
              items: { type: "string" },
            },
          },
          required: ["query"],
        },
        defer_loading: true,
      },
    ],
  });

  console.log(JSON.stringify(response, null, 2));
}

main();
```

</CodeGroup>

## 도구 정의

Tool search tool에는 두 가지 변형이 있습니다:

```json JSON
{
  "type": "tool_search_tool_regex_20251119",
  "name": "tool_search_tool_regex"
}
```

```json JSON
{
  "type": "tool_search_tool_bm25_20251119",
  "name": "tool_search_tool_bm25"
}
```


> **Regex 변형 쿼리 형식: Python 정규식, 자연어 아님**
>
> `tool_search_tool_regex_20251119`를 사용할 때 Claude는 자연어 쿼리가 아닌 Python의 `re.search()` 문법을 사용하여 정규식 패턴을 구성합니다. 일반적인 패턴:
>
> - `"weather"` - "weather"를 포함하는 도구 이름/설명과 일치
> - `"get_.*_data"` - `get_user_data`, `get_weather_data`와 같은 도구와 일치
> - `"database.*query|query.*database"` - 유연성을 위한 OR 패턴
> - `"(?i)slack"` - 대소문자를 구분하지 않는 검색
>
> 최대 쿼리 길이: 200자



> **BM25 변형 쿼리 형식: 자연어**
>
> `tool_search_tool_bm25_20251119`를 사용할 때 Claude는 자연어 쿼리를 사용하여 도구를 검색합니다.


### 지연된 도구 로딩

`defer_loading: true`를 추가하여 필요에 따라 로드할 도구를 표시합니다:

```json JSON
{
  "name": "get_weather",
  "description": "Get current weather for a location",
  "input_schema": {
    "type": "object",
    "properties": {
      "location": { "type": "string" },
      "unit": { "type": "string", "enum": ["celsius", "fahrenheit"] }
    },
    "required": ["location"]
  },
  "defer_loading": true
}
```

**주요 사항:**

- `defer_loading`이 없는 도구는 즉시 컨텍스트에 로드됩니다
- `defer_loading: true`가 있는 도구는 Claude가 검색을 통해 발견할 때만 로드됩니다
- Tool search tool 자체는 **절대** `defer_loading: true`를 가져서는 안 됩니다
- 최적의 성능을 위해 가장 자주 사용되는 3-5개의 도구는 지연되지 않은 상태로 유지하세요

두 도구 검색 변형(`regex`와 `bm25`)은 모두 도구 이름, 설명, 인수 이름 및 인수 설명을 검색합니다.

## 응답 형식

Claude가 tool search tool을 사용하면 응답에 새로운 블록 타입이 포함됩니다:

```json JSON
{
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "I'll search for tools to help with the weather information."
    },
    {
      "type": "server_tool_use",
      "id": "srvtoolu_01ABC123",
      "name": "tool_search_tool_regex",
      "input": {
        "query": "weather"
      }
    },
    {
      "type": "tool_search_tool_result",
      "tool_use_id": "srvtoolu_01ABC123",
      "content": {
        "type": "tool_search_tool_search_result",
        "tool_references": [{ "type": "tool_reference", "tool_name": "get_weather" }]
      }
    },
    {
      "type": "text",
      "text": "I found a weather tool. Let me get the weather for San Francisco."
    },
    {
      "type": "tool_use",
      "id": "toolu_01XYZ789",
      "name": "get_weather",
      "input": { "location": "San Francisco", "unit": "fahrenheit" }
    }
  ],
  "stop_reason": "tool_use"
}
```

### 응답 이해하기

- **`server_tool_use`**: Claude가 tool search tool을 호출하고 있음을 나타냅니다
- **`tool_search_tool_result`**: 중첩된 `tool_search_tool_search_result` 객체와 함께 검색 결과를 포함합니다
- **`tool_references`**: 발견된 도구를 가리키는 `tool_reference` 객체의 배열입니다
- **`tool_use`**: Claude가 발견된 도구를 호출합니다

`tool_reference` 블록은 Claude에게 표시되기 전에 자동으로 전체 도구 정의로 확장됩니다. 이 확장을 직접 처리할 필요가 없습니다. `tools` 매개변수에 일치하는 모든 도구 정의를 제공하는 한 API에서 자동으로 수행됩니다.

## MCP 통합

Tool search tool은 [MCP 서버](./06-mcp-in-api-01-mcp-connector.md)와 함께 작동합니다. API 요청에 `"mcp-client-2025-11-20"` [베타 헤더](/docs/en/api/beta-headers)를 추가한 다음, `default_config`와 함께 `mcp_toolset`을 사용하여 MCP 도구 로딩을 지연시킵니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
  --header "x-api-key: $ANTHROPIC_API_KEY" \
  --header "anthropic-version: 2023-06-01" \
  --header "anthropic-beta: advanced-tool-use-2025-11-20,mcp-client-2025-11-20" \
  --header "content-type: application/json" \
  --data '{
    "model": "claude-sonnet-4-5-20250929",
    "max_tokens": 2048,
    "mcp_servers": [
      {
        "type": "url",
        "name": "database-server",
        "url": "https://mcp-db.example.com"
      }
    ],
    "tools": [
      {
        "type": "tool_search_tool_regex_20251119",
        "name": "tool_search_tool_regex"
      },
      {
        "type": "mcp_toolset",
        "mcp_server_name": "database-server",
        "default_config": {
          "defer_loading": true
        },
        "configs": {
          "search_events": {
            "defer_loading": false
          }
        }
      }
    ],
    "messages": [
      {
        "role": "user",
        "content": "What events are in my database?"
      }
    ]
  }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.beta.messages.create(
    model="claude-sonnet-4-5-20250929",
    betas=["advanced-tool-use-2025-11-20", "mcp-client-2025-11-20"],
    max_tokens=2048,
    mcp_servers=[
        {
            "type": "url",
            "name": "database-server",
            "url": "https://mcp-db.example.com"
        }
    ],
    tools=[
        {
            "type": "tool_search_tool_regex_20251119",
            "name": "tool_search_tool_regex"
        },
        {
            "type": "mcp_toolset",
            "mcp_server_name": "database-server",
            "default_config": {
                "defer_loading": True
            },
            "configs": {
                "search_events": {
                    "defer_loading": False
                }
            }
        }
    ],
    messages=[
        {
            "role": "user",
            "content": "What events are in my database?"
        }
    ]
)

print(response)
```

```typescript TypeScript
import Anthropic from "@anthropic-ai/sdk";

const client = new Anthropic();

async function main() {
  const response = await client.beta.messages.create({
    model: "claude-sonnet-4-5-20250929",
    betas: ["advanced-tool-use-2025-11-20", "mcp-client-2025-11-20"],
    max_tokens: 2048,
    mcp_servers: [
      {
        type: "url",
        name: "database-server",
        url: "https://mcp-db.example.com",
      },
    ],
    tools: [
      {
        type: "tool_search_tool_regex_20251119",
        name: "tool_search_tool_regex",
      },
      {
        type: "mcp_toolset",
        mcp_server_name: "database-server",
        default_config: {
          defer_loading: true,
        },
        configs: {
          search_events: {
            defer_loading: false,
          },
        },
      },
    ],
    messages: [
      {
        role: "user",
        content: "What events are in my database?",
      },
    ],
  });

  console.log(JSON.stringify(response, null, 2));
}

main();
```

</CodeGroup>

**MCP 구성 옵션:**

- `default_config.defer_loading`: MCP 서버의 모든 도구에 대한 기본값 설정
- `configs`: 이름으로 특정 도구에 대한 기본값 재정의
- 대규모 도구 라이브러리를 위해 여러 MCP 서버를 tool search와 결합

## 커스텀 도구 검색 구현

커스텀 도구에서 `tool_reference` 블록을 반환하여 자체 도구 검색 로직(예: 임베딩 또는 의미론적 검색 사용)을 구현할 수 있습니다. Claude가 커스텀 검색 도구를 호출하면 content 배열에 `tool_reference` 블록이 있는 표준 `tool_result`를 반환합니다:

```json JSON
{
  "type": "tool_result",
  "tool_use_id": "toolu_your_tool_id",
  "content": [
    { "type": "tool_reference", "tool_name": "discovered_tool_name" }
  ]
}
```

참조된 모든 도구는 최상위 `tools` 매개변수에 `defer_loading: true`와 함께 해당하는 도구 정의가 있어야 합니다. 이 접근 방식을 사용하면 도구 검색 시스템과의 호환성을 유지하면서 더욱 정교한 검색 알고리즘을 사용할 수 있습니다.


> [응답 형식](#response-format) 섹션에 표시된 `tool_search_tool_result` 형식은 Anthropic의 내장 도구 검색에서 내부적으로 사용되는 서버 측 형식입니다. 커스텀 클라이언트 측 구현의 경우 항상 위에 표시된 것처럼 `tool_reference` 콘텐츠 블록이 있는 표준 `tool_result` 형식을 사용하세요.


임베딩을 사용하는 완전한 예제는 [임베딩을 사용한 도구 검색 쿡북](https://platform.claude.com/cookbooks)을 참조하세요.

## 오류 처리


> Tool search tool은 [도구 사용 예제](../03-tools/02-how-to-implement-tool-use.md)와 호환되지 않습니다.
> 도구 사용 예제를 제공해야 하는 경우 도구 검색 없이 표준 도구 호출을 사용하세요.


### HTTP 오류 (400 상태)

이러한 오류는 요청이 처리되지 않도록 합니다:

**모든 도구가 지연됨:**

```json
{
  "type": "error",
  "error": {
    "type": "invalid_request_error",
    "message": "All tools have defer_loading set. At least one tool must be non-deferred."
  }
}
```

**도구 정의 누락:**

```json
{
  "type": "error",
  "error": {
    "type": "invalid_request_error",
    "message": "Tool reference 'unknown_tool' has no corresponding tool definition"
  }
}
```

### 도구 결과 오류 (200 상태)

도구 실행 중 오류는 본문에 오류 정보가 포함된 200 응답을 반환합니다:

```json JSON
{
  "type": "tool_result",
  "tool_use_id": "srvtoolu_01ABC123",
  "content": {
    "type": "tool_search_tool_result_error",
    "error_code": "invalid_pattern"
  }
}
```

**오류 코드:**

- `too_many_requests`: 도구 검색 작업에 대한 속도 제한 초과
- `invalid_pattern`: 잘못된 형식의 정규식 패턴
- `pattern_too_long`: 패턴이 200자 제한을 초과함
- `unavailable`: 도구 검색 서비스를 일시적으로 사용할 수 없음

### 일반적인 실수

<details>
<summary>400 오류: 모든 도구가 지연됨</summary>

**원인**: 검색 도구를 포함한 모든 도구에 `defer_loading: true`를 설정했습니다

**해결책**: Tool search tool에서 `defer_loading`을 제거합니다:

```json
{
  "type": "tool_search_tool_regex_20251119", // 여기에 defer_loading 없음
  "name": "tool_search_tool_regex"
}
```
</details>

<details>
<summary>400 오류: 도구 정의 누락</summary>

**원인**: `tool_reference`가 `tools` 배열에 없는 도구를 가리킵니다

**해결책**: 발견될 수 있는 모든 도구에 완전한 정의가 있는지 확인합니다:

```json
{
  "name": "my_tool",
  "description": "Full description here",
  "input_schema": {
    /* complete schema */
  },
  "defer_loading": true
}
```
</details>

<details>
<summary>Claude가 예상 도구를 찾지 못함</summary>

**원인**: 도구 이름이나 설명이 정규식 패턴과 일치하지 않습니다

**디버깅 단계:**

1. 도구 이름과 설명을 확인하세요—Claude는 두 필드를 모두 검색합니다
2. 패턴을 테스트하세요: `import re; re.search(r"your_pattern", "tool_name")`
3. 검색은 기본적으로 대소문자를 구분합니다(대소문자를 구분하지 않으려면 `(?i)` 사용)
4. Claude는 정확히 일치하는 것이 아니라 `".*weather.*"`와 같은 넓은 패턴을 사용합니다

**팁**: 검색 가능성을 향상시키기 위해 도구 설명에 일반적인 키워드를 추가하세요
</details>

## 프롬프트 캐싱

Tool search는 [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md)과 함께 작동합니다. 여러 턴 대화를 최적화하기 위해 `cache_control` 중단점을 추가합니다:

<CodeGroup>
```python Python
import anthropic

client = anthropic.Anthropic()

# Tool search를 사용한 첫 번째 요청
messages = [
    {
        "role": "user",
        "content": "What's the weather in Seattle?"
    }
]

response1 = client.beta.messages.create(
    model="claude-sonnet-4-5-20250929",
    betas=["advanced-tool-use-2025-11-20"],
    max_tokens=2048,
    messages=messages,
    tools=[
        {
            "type": "tool_search_tool_regex_20251119",
            "name": "tool_search_tool_regex"
        },
        {
            "name": "get_weather",
            "description": "Get weather for a location",
            "input_schema": {
                "type": "object",
                "properties": {
                    "location": {"type": "string"}
                },
                "required": ["location"]
            },
            "defer_loading": True
        }
    ]
)

# 대화에 Claude의 응답 추가
messages.append({
    "role": "assistant",
    "content": response1.content
})

# 캐시 중단점을 사용한 두 번째 요청
messages.append({
    "role": "user",
    "content": "What about New York?",
    "cache_control": {"type": "ephemeral"}
})

response2 = client.beta.messages.create(
    model="claude-sonnet-4-5-20250929",
    betas=["advanced-tool-use-2025-11-20"],
    max_tokens=2048,
    messages=messages,
    tools=[
        {
            "type": "tool_search_tool_regex_20251119",
            "name": "tool_search_tool_regex"
        },
        {
            "name": "get_weather",
            "description": "Get weather for a location",
            "input_schema": {
                "type": "object",
                "properties": {
                    "location": {"type": "string"}
                },
                "required": ["location"]
            },
            "defer_loading": True
        }
    ]
)

print(f"Cache read tokens: {response2.usage.get('cache_read_input_tokens', 0)}")
```
</CodeGroup>

시스템은 전체 대화 기록에 걸쳐 tool_reference 블록을 자동으로 확장하므로 Claude는 다시 검색하지 않고도 이후 턴에서 발견된 도구를 재사용할 수 있습니다.

## 스트리밍

스트리밍이 활성화되면 스트림의 일부로 도구 검색 이벤트를 받게 됩니다:

```javascript
event: content_block_start
data: {"type": "content_block_start", "index": 1, "content_block": {"type": "server_tool_use", "id": "srvtoolu_xyz789", "name": "tool_search_tool_regex"}}

// 검색 쿼리 스트리밍
event: content_block_delta
data: {"type": "content_block_delta", "index": 1, "delta": {"type": "input_json_delta", "partial_json": "{\"query\":\"weather\"}"}}

// 검색이 실행되는 동안 일시 중지

// 검색 결과 스트리밍
event: content_block_start
data: {"type": "content_block_start", "index": 2, "content_block": {"type": "tool_search_tool_result", "tool_use_id": "srvtoolu_xyz789", "content": {"type": "tool_search_tool_search_result", "tool_references": [{"type": "tool_reference", "tool_name": "get_weather"}]}}}

// Claude가 발견된 도구로 계속 진행
```

## 배치 요청

[Messages Batches API](../02-capabilities/06-batch-processing.md)에 tool search tool을 포함할 수 있습니다. Messages Batches API를 통한 도구 검색 작업은 일반 Messages API 요청과 동일하게 가격이 책정됩니다.

## 제한 사항 및 모범 사례

### 제한 사항

- **최대 도구**: 카탈로그에 10,000개의 도구
- **검색 결과**: 검색당 가장 관련성 높은 3-5개 도구 반환
- **패턴 길이**: 정규식 패턴의 최대 200자
- **모델 지원**: Sonnet 4.0+, Opus 4.0+만 지원(Haiku 없음)

### Tool search를 사용해야 하는 경우

**적합한 사용 사례:**

- 시스템에서 10개 이상의 도구 사용 가능
- 도구 정의가 >10K 토큰 소비
- 대규모 도구 세트에서 도구 선택 정확도 문제 발생
- 여러 서버가 있는 MCP 기반 시스템 구축(200개 이상의 도구)
- 시간이 지남에 따라 도구 라이브러리가 증가함

**기존 도구 호출이 더 나을 수 있는 경우:**

- 총 10개 미만의 도구
- 모든 요청에서 모든 도구가 자주 사용됨
- 매우 작은 도구 정의(<100 토큰 총합)

### 최적화 팁

- 가장 자주 사용되는 3-5개의 도구는 지연되지 않은 상태로 유지
- 명확하고 설명적인 도구 이름과 설명 작성
- 사용자가 작업을 설명하는 방법과 일치하는 의미론적 키워드를 설명에 사용
- 사용 가능한 도구 범주를 설명하는 시스템 프롬프트 섹션 추가: "Slack, GitHub 및 Jira와 상호 작용할 도구를 검색할 수 있습니다"
- Claude가 발견하는 도구를 모니터링하여 설명 개선

## 사용량

Tool search tool 사용량은 응답 usage 객체에서 추적됩니다:

```json JSON
{
  "usage": {
    "input_tokens": 1024,
    "output_tokens": 256,
    "server_tool_use": {
      "tool_search_requests": 2
    }
  }
}
```
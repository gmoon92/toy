# [검색 결과](https://platform.claude.com/docs/en/build-with-claude/search-results)

소스 속성과 함께 검색 결과를 제공하여 RAG 애플리케이션에 자연스러운 인용을 활성화합니다

---

검색 결과 콘텐츠 블록은 적절한 출처 표시와 함께 자연스러운 인용을 가능하게 하여 웹 검색 품질의 인용을 사용자 정의 애플리케이션에 제공합니다. 이 기능은 Claude가 출처를 정확하게 인용해야 하는 RAG(Retrieval-Augmented Generation) 애플리케이션에 특히 강력합니다.

검색 결과 기능은 다음 모델에서 사용할 수 있습니다:

- Claude Opus 4.5 (`claude-opus-4-5-20251101`)
- Claude Opus 4.1 (`claude-opus-4-1-20250805`)
- Claude Opus 4 (`claude-opus-4-20250514`)
- Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`)
- Claude Sonnet 4 (`claude-sonnet-4-20250514`)
- Claude Sonnet 3.7 ([deprecated](https://platform.claude.com/docs/en/about-claude/model-deprecations)) (`claude-3-7-sonnet-20250219`)
- Claude Haiku 4.5 (`claude-haiku-4-5-20251001`)
- Claude Haiku 3.5 ([deprecated](https://platform.claude.com/docs/en/about-claude/model-deprecations)) (`claude-3-5-haiku-20241022`)

## 주요 이점

- **자연스러운 인용** - 모든 콘텐츠에 대해 웹 검색과 동일한 인용 품질 달성
- **유연한 통합** - 동적 RAG를 위한 도구 반환 또는 미리 가져온 데이터를 위한 최상위 콘텐츠로 사용
- **적절한 출처 표시** - 각 결과에는 명확한 속성을 위한 소스 및 제목 정보 포함
- **문서 기반 해결 방법 불필요** - 문서 기반 해결 방법의 필요성 제거
- **일관된 인용 형식** - Claude의 웹 검색 기능의 인용 품질 및 형식과 일치

## 작동 방식

검색 결과는 두 가지 방법으로 제공될 수 있습니다:

1. **도구 호출에서** - 사용자 정의 도구가 검색 결과를 반환하여 동적 RAG 애플리케이션을 가능하게 합니다
2. **최상위 콘텐츠로** - 미리 가져오거나 캐시된 콘텐츠를 위해 사용자 메시지에 직접 검색 결과를 제공합니다

두 경우 모두 Claude는 적절한 출처 표시와 함께 검색 결과의 정보를 자동으로 인용할 수 있습니다.

### 검색 결과 스키마

검색 결과는 다음 구조를 사용합니다:

```json
{
  "type": "search_result",
  "source": "https://example.com/article",  // 필수: 소스 URL 또는 식별자
  "title": "Article Title",                  // 필수: 결과의 제목
  "content": [                               // 필수: 텍스트 블록 배열
    {
      "type": "text",
      "text": "The actual content of the search result..."
    }
  ],
  "citations": {                             // 선택: 인용 구성
    "enabled": true                          // 이 결과에 대한 인용 활성화/비활성화
  }
}
```

### 필수 필드

| 필드 | 타입 | 설명 |
|-------|------|-------------|
| `type` | string | `"search_result"`이어야 합니다 |
| `source` | string | 콘텐츠의 소스 URL 또는 식별자 |
| `title` | string | 검색 결과의 설명적 제목 |
| `content` | array | 실제 콘텐츠를 포함하는 텍스트 블록 배열 |

### 선택 필드

| 필드 | 타입 | 설명 |
|-------|------|-------------|
| `citations` | object | `enabled` 불린 필드가 있는 인용 구성 |
| `cache_control` | object | 캐시 제어 설정 (예: `{"type": "ephemeral"}`) |

`content` 배열의 각 항목은 다음을 포함하는 텍스트 블록이어야 합니다:
- `type`: `"text"`이어야 합니다
- `text`: 실제 텍스트 콘텐츠 (비어있지 않은 문자열)

## 방법 1: 도구 호출에서 검색 결과

가장 강력한 사용 사례는 사용자 정의 도구에서 검색 결과를 반환하는 것입니다. 이를 통해 도구가 자동 인용과 함께 관련 콘텐츠를 가져와 반환하는 동적 RAG 애플리케이션이 가능합니다.

### 예제: 지식 베이스 도구

<CodeGroup>
```python Python
from anthropic import Anthropic
from anthropic.types import (
    MessageParam,
    TextBlockParam,
    SearchResultBlockParam,
    ToolResultBlockParam
)

client = Anthropic()

# 지식 베이스 검색 도구 정의
knowledge_base_tool = {
    "name": "search_knowledge_base",
    "description": "Search the company knowledge base for information",
    "input_schema": {
        "type": "object",
        "properties": {
            "query": {
                "type": "string",
                "description": "The search query"
            }
        },
        "required": ["query"]
    }
}

# 도구 호출을 처리하는 함수
def search_knowledge_base(query):
    # 여기에 검색 로직 작성
    # 올바른 형식으로 검색 결과 반환
    return [
        SearchResultBlockParam(
            type="search_result",
            source="https://docs.company.com/product-guide",
            title="Product Configuration Guide",
            content=[
                TextBlockParam(
                    type="text",
                    text="To configure the product, navigate to Settings > Configuration. The default timeout is 30 seconds, but can be adjusted between 10-120 seconds based on your needs."
                )
            ],
            citations={"enabled": True}
        ),
        SearchResultBlockParam(
            type="search_result",
            source="https://docs.company.com/troubleshooting",
            title="Troubleshooting Guide",
            content=[
                TextBlockParam(
                    type="text",
                    text="If you encounter timeout errors, first check the configuration settings. Common causes include network latency and incorrect timeout values."
                )
            ],
            citations={"enabled": True}
        )
    ]

# 도구를 사용하여 메시지 생성
response = client.messages.create(
    model="claude-sonnet-4-5",  # 지원되는 모든 모델에서 작동
    max_tokens=1024,
    tools=[knowledge_base_tool],
    messages=[
        MessageParam(
            role="user",
            content="How do I configure the timeout settings?"
        )
    ]
)

# Claude가 도구를 호출하면 검색 결과 제공
if response.content[0].type == "tool_use":
    tool_result = search_knowledge_base(response.content[0].input["query"])

    # 도구 결과를 다시 전송
    final_response = client.messages.create(
        model="claude-sonnet-4-5",  # 지원되는 모든 모델에서 작동
        max_tokens=1024,
        messages=[
            MessageParam(role="user", content="How do I configure the timeout settings?"),
            MessageParam(role="assistant", content=response.content),
            MessageParam(
                role="user",
                content=[
                    ToolResultBlockParam(
                        type="tool_result",
                        tool_use_id=response.content[0].id,
                        content=tool_result  # 검색 결과가 여기에 들어감
                    )
                ]
            )
        ]
    )
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

// 지식 베이스 검색 도구 정의
const knowledgeBaseTool = {
  name: "search_knowledge_base",
  description: "Search the company knowledge base for information",
  input_schema: {
    type: "object",
    properties: {
      query: {
        type: "string",
        description: "The search query"
      }
    },
    required: ["query"]
  }
};

// 도구 호출을 처리하는 함수
function searchKnowledgeBase(query: string) {
  // 여기에 검색 로직 작성
  // 올바른 형식으로 검색 결과 반환
  return [
    {
      type: "search_result" as const,
      source: "https://docs.company.com/product-guide",
      title: "Product Configuration Guide",
      content: [
        {
          type: "text" as const,
          text: "To configure the product, navigate to Settings > Configuration. The default timeout is 30 seconds, but can be adjusted between 10-120 seconds based on your needs."
        }
      ],
      citations: { enabled: true }
    },
    {
      type: "search_result" as const,
      source: "https://docs.company.com/troubleshooting",
      title: "Troubleshooting Guide",
      content: [
        {
          type: "text" as const,
          text: "If you encounter timeout errors, first check the configuration settings. Common causes include network latency and incorrect timeout values."
        }
      ],
      citations: { enabled: true }
    }
  ];
}

// 도구를 사용하여 메시지 생성
const response = await anthropic.messages.create({
  model: "claude-sonnet-4-5", // 지원되는 모든 모델에서 작동
  max_tokens: 1024,
  tools: [knowledgeBaseTool],
  messages: [
    {
      role: "user",
      content: "How do I configure the timeout settings?"
    }
  ]
});

// 도구 사용 처리 및 결과 제공
if (response.content[0].type === "tool_use") {
  const toolResult = searchKnowledgeBase(response.content[0].input.query);

  const finalResponse = await anthropic.messages.create({
    model: "claude-sonnet-4-5", // 지원되는 모든 모델에서 작동
    max_tokens: 1024,
      messages: [
      { role: "user", content: "How do I configure the timeout settings?" },
      { role: "assistant", content: response.content },
      {
        role: "user",
        content: [
          {
            type: "tool_result" as const,
            tool_use_id: response.content[0].id,
            content: toolResult  // 검색 결과가 여기에 들어감
          }
        ]
      }
    ]
  });
}
```
</CodeGroup>

## 방법 2: 최상위 콘텐츠로서의 검색 결과

사용자 메시지에 검색 결과를 직접 제공할 수도 있습니다. 이는 다음에 유용합니다:
- 검색 인프라에서 미리 가져온 콘텐츠
- 이전 쿼리의 캐시된 검색 결과
- 외부 검색 서비스의 콘텐츠
- 테스트 및 개발

### 예제: 직접 검색 결과

<CodeGroup>
```python Python
from anthropic import Anthropic
from anthropic.types import (
    MessageParam,
    TextBlockParam,
    SearchResultBlockParam
)

client = Anthropic()

# 사용자 메시지에 검색 결과를 직접 제공
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[
        MessageParam(
            role="user",
            content=[
                SearchResultBlockParam(
                    type="search_result",
                    source="https://docs.company.com/api-reference",
                    title="API Reference - Authentication",
                    content=[
                        TextBlockParam(
                            type="text",
                            text="All API requests must include an API key in the Authorization header. Keys can be generated from the dashboard. Rate limits: 1000 requests per hour for standard tier, 10000 for premium."
                        )
                    ],
                    citations={"enabled": True}
                ),
                SearchResultBlockParam(
                    type="search_result",
                    source="https://docs.company.com/quickstart",
                    title="Getting Started Guide",
                    content=[
                        TextBlockParam(
                            type="text",
                            text="To get started: 1) Sign up for an account, 2) Generate an API key from the dashboard, 3) Install our SDK using pip install company-sdk, 4) Initialize the client with your API key."
                        )
                    ],
                    citations={"enabled": True}
                ),
                TextBlockParam(
                    type="text",
                    text="Based on these search results, how do I authenticate API requests and what are the rate limits?"
                )
            ]
        )
    ]
)

print(response.model_dump_json(indent=2))
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

// 사용자 메시지에 검색 결과를 직접 제공
const response = await anthropic.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  messages: [
    {
      role: "user",
      content: [
        {
          type: "search_result" as const,
          source: "https://docs.company.com/api-reference",
          title: "API Reference - Authentication",
          content: [
            {
              type: "text" as const,
              text: "All API requests must include an API key in the Authorization header. Keys can be generated from the dashboard. Rate limits: 1000 requests per hour for standard tier, 10000 for premium."
            }
          ],
          citations: { enabled: true }
        },
        {
          type: "search_result" as const,
          source: "https://docs.company.com/quickstart",
          title: "Getting Started Guide",
          content: [
            {
              type: "text" as const,
              text: "To get started: 1) Sign up for an account, 2) Generate an API key from the dashboard, 3) Install our SDK using pip install company-sdk, 4) Initialize the client with your API key."
            }
          ],
          citations: { enabled: true }
        },
        {
          type: "text" as const,
          text: "Based on these search results, how do I authenticate API requests and what are the rate limits?"
        }
      ]
    }
  ]
});

console.log(response);
```

```bash Shell
#!/bin/sh
curl https://api.anthropic.com/v1/messages \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data \
'{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
        {
            "role": "user",
            "content": [
                {
                    "type": "search_result",
                    "source": "https://docs.company.com/api-reference",
                    "title": "API Reference - Authentication",
                    "content": [
                        {
                            "type": "text",
                            "text": "All API requests must include an API key in the Authorization header. Keys can be generated from the dashboard. Rate limits: 1000 requests per hour for standard tier, 10000 for premium."
                        }
                    ],
                    "citations": {
                        "enabled": true
                    }
                },
                {
                    "type": "search_result",
                    "source": "https://docs.company.com/quickstart",
                    "title": "Getting Started Guide",
                    "content": [
                        {
                            "type": "text",
                            "text": "To get started: 1) Sign up for an account, 2) Generate an API key from the dashboard, 3) Install our SDK using pip install company-sdk, 4) Initialize the client with your API key."
                        }
                    ],
                    "citations": {
                        "enabled": true
                    }
                },
                {
                    "type": "text",
                    "text": "Based on these search results, how do I authenticate API requests and what are the rate limits?"
                }
            ]
        }
    ]
}'
```
</CodeGroup>

## 인용이 포함된 Claude의 응답

검색 결과가 어떻게 제공되든 관계없이 Claude는 검색 결과의 정보를 사용할 때 자동으로 인용을 포함합니다:

```json
{
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "To authenticate API requests, you need to include an API key in the Authorization header",
      "citations": [
        {
          "type": "search_result_location",
          "source": "https://docs.company.com/api-reference",
          "title": "API Reference - Authentication",
          "cited_text": "All API requests must include an API key in the Authorization header",
          "search_result_index": 0,
          "start_block_index": 0,
          "end_block_index": 0
        }
      ]
    },
    {
      "type": "text",
      "text": ". You can generate API keys from your dashboard",
      "citations": [
        {
          "type": "search_result_location",
          "source": "https://docs.company.com/api-reference",
          "title": "API Reference - Authentication",
          "cited_text": "Keys can be generated from the dashboard",
          "search_result_index": 0,
          "start_block_index": 0,
          "end_block_index": 0
        }
      ]
    },
    {
      "type": "text",
      "text": ". The rate limits are 1,000 requests per hour for the standard tier and 10,000 requests per hour for the premium tier.",
      "citations": [
        {
          "type": "search_result_location",
          "source": "https://docs.company.com/api-reference",
          "title": "API Reference - Authentication",
          "cited_text": "Rate limits: 1000 requests per hour for standard tier, 10000 for premium",
          "search_result_index": 0,
          "start_block_index": 0,
          "end_block_index": 0
        }
      ]
    }
  ]
}
```

### 인용 필드

각 인용에는 다음이 포함됩니다:

| 필드 | 타입 | 설명 |
|-------|------|-------------|
| `type` | string | 검색 결과 인용의 경우 항상 `"search_result_location"` |
| `source` | string | 원본 검색 결과의 소스 |
| `title` | string 또는 null | 원본 검색 결과의 제목 |
| `cited_text` | string | 인용되는 정확한 텍스트 |
| `search_result_index` | integer | 검색 결과의 인덱스 (0부터 시작) |
| `start_block_index` | integer | 콘텐츠 배열의 시작 위치 |
| `end_block_index` | integer | 콘텐츠 배열의 종료 위치 |

참고: `search_result_index`는 검색 결과가 제공된 방식(도구 호출 또는 최상위 콘텐츠)에 관계없이 검색 결과 콘텐츠 블록의 인덱스(0부터 시작)를 나타냅니다.

## 여러 콘텐츠 블록

검색 결과는 `content` 배열에 여러 텍스트 블록을 포함할 수 있습니다:

```json
{
  "type": "search_result",
  "source": "https://docs.company.com/api-guide",
  "title": "API Documentation",
  "content": [
    {
      "type": "text",
      "text": "Authentication: All API requests require an API key."
    },
    {
      "type": "text",
      "text": "Rate Limits: The API allows 1000 requests per hour per key."
    },
    {
      "type": "text",
      "text": "Error Handling: The API returns standard HTTP status codes."
    }
  ]
}
```

Claude는 `start_block_index` 및 `end_block_index` 필드를 사용하여 특정 블록을 인용할 수 있습니다.

## 고급 사용법

### 두 방법 결합

동일한 대화에서 도구 기반 및 최상위 검색 결과를 모두 사용할 수 있습니다:

```python
# 최상위 검색 결과가 있는 첫 번째 메시지
messages = [
    MessageParam(
        role="user",
        content=[
            SearchResultBlockParam(
                type="search_result",
                source="https://docs.company.com/overview",
                title="Product Overview",
                content=[
                    TextBlockParam(type="text", text="Our product helps teams collaborate...")
                ],
                citations={"enabled": True}
            ),
            TextBlockParam(
                type="text",
                text="Tell me about this product and search for pricing information"
            )
        ]
    )
]

# Claude가 응답하고 가격 정보를 검색하기 위해 도구를 호출할 수 있습니다
# 그런 다음 더 많은 검색 결과와 함께 도구 결과를 제공합니다
```

### 다른 콘텐츠 유형과 결합

두 방법 모두 검색 결과를 다른 콘텐츠와 혼합하는 것을 지원합니다:

```python
# 도구 결과에서
tool_result = [
    SearchResultBlockParam(
        type="search_result",
        source="https://docs.company.com/guide",
        title="User Guide",
        content=[TextBlockParam(type="text", text="Configuration details...")],
        citations={"enabled": True}
    ),
    TextBlockParam(
        type="text",
        text="Additional context: This applies to version 2.0 and later."
    )
]

# 최상위 콘텐츠에서
user_content = [
    SearchResultBlockParam(
        type="search_result",
        source="https://research.com/paper",
        title="Research Paper",
        content=[TextBlockParam(type="text", text="Key findings...")],
        citations={"enabled": True}
    ),
    {
        "type": "image",
        "source": {"type": "url", "url": "https://example.com/chart.png"}
    },
    TextBlockParam(
        type="text",
        text="How does the chart relate to the research findings?"
    )
]
```

### 캐시 제어

더 나은 성능을 위해 캐시 제어를 추가합니다:

```json
{
  "type": "search_result",
  "source": "https://docs.company.com/guide",
  "title": "User Guide",
  "content": [{"type": "text", "text": "..."}],
  "cache_control": {
    "type": "ephemeral"
  }
}
```

### 인용 제어

기본적으로 검색 결과에 대한 인용은 비활성화되어 있습니다. `citations` 구성을 명시적으로 설정하여 인용을 활성화할 수 있습니다:

```json
{
  "type": "search_result",
  "source": "https://docs.company.com/guide",
  "title": "User Guide",
  "content": [{"type": "text", "text": "Important documentation..."}],
  "citations": {
    "enabled": true  // 이 결과에 대한 인용 활성화
  }
}
```

`citations.enabled`가 `true`로 설정되면 Claude는 검색 결과의 정보를 사용할 때 인용 참조를 포함합니다. 이를 통해:
- 사용자 정의 RAG 애플리케이션에 대한 자연스러운 인용
- 독점 지식 베이스와 인터페이스할 때 출처 표시
- 검색 결과를 반환하는 사용자 정의 도구에 대한 웹 검색 품질의 인용

`citations` 필드가 생략되면 기본적으로 인용이 비활성화됩니다.


> 인용은 전체 또는 전무입니다: 요청의 모든 검색 결과에 인용이 활성화되거나 모두 비활성화되어야 합니다. 다른 인용 설정을 가진 검색 결과를 혼합하면 오류가 발생합니다. 일부 소스에 대한 인용을 비활성화해야 하는 경우 해당 요청의 모든 검색 결과에 대해 비활성화해야 합니다.


## 모범 사례

### 도구 기반 검색 (방법 1)

- **동적 콘텐츠**: 실시간 검색 및 동적 RAG 애플리케이션에 사용
- **오류 처리**: 검색 실패 시 적절한 메시지 반환
- **결과 제한**: 컨텍스트 오버플로를 방지하기 위해 가장 관련성 높은 결과만 반환

### 최상위 검색 (방법 2)

- **미리 가져온 콘텐츠**: 이미 검색 결과가 있는 경우 사용
- **일괄 처리**: 한 번에 여러 검색 결과를 처리하는 데 이상적
- **테스트**: 알려진 콘텐츠로 인용 동작을 테스트하는 데 적합

### 일반 모범 사례

1. **결과를 효과적으로 구조화**
   - 명확하고 영구적인 소스 URL 사용
   - 설명적인 제목 제공
   - 긴 콘텐츠를 논리적인 텍스트 블록으로 나누기

2. **일관성 유지**
   - 애플리케이션 전체에서 일관된 소스 형식 사용
   - 제목이 콘텐츠를 정확하게 반영하도록 보장
   - 형식을 일관되게 유지

3. **오류를 우아하게 처리**
   ```python
   def search_with_fallback(query):
       try:
           results = perform_search(query)
           if not results:
               return {"type": "text", "text": "No results found."}
           return format_as_search_results(results)
       except Exception as e:
           return {"type": "text", "text": f"Search error: {str(e)}"}
   ```

## 제한 사항

- 검색 결과 콘텐츠 블록은 Claude API, Amazon Bedrock 및 Google Cloud의 Vertex AI에서 사용할 수 있습니다
- 검색 결과 내에서는 텍스트 콘텐츠만 지원됩니다 (이미지 또는 기타 미디어는 지원되지 않음)
- `content` 배열에는 최소한 하나의 텍스트 블록이 포함되어야 합니다

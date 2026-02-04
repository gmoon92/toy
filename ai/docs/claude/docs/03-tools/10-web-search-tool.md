# [웹 검색 도구](https://platform.claude.com/docs/en/agents-and-tools/tool-use/web-search-tool)

---

웹 검색 도구는 Claude에게 실시간 웹 콘텐츠에 대한 직접 액세스를 제공하여, 지식 기준일(knowledge cutoff) 이후의 최신 정보로 질문에 답변할 수 있게 합니다. Claude는 자동으로 검색 결과의 출처를 답변의 일부로 인용합니다.


> 웹 검색 도구에 대한 경험을 공유하려면 [피드백 양식](https://forms.gle/sWjBtsrNEY2oKGuE8)을 통해 문의해 주세요.


## 지원되는 모델

웹 검색은 다음 모델에서 사용 가능합니다:

- Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`)
- Claude Sonnet 4 (`claude-sonnet-4-20250514`)
- Claude Sonnet 3.7 ([지원 중단됨](https://platform.claude.com/docs/en/about-claude/model-deprecations)) (`claude-3-7-sonnet-20250219`)
- Claude Haiku 4.5 (`claude-haiku-4-5-20251001`)
- Claude Haiku 3.5 ([지원 중단됨](https://platform.claude.com/docs/en/about-claude/model-deprecations)) (`claude-3-5-haiku-latest`)
- Claude Opus 4.5 (`claude-opus-4-5-20251101`)
- Claude Opus 4.1 (`claude-opus-4-1-20250805`)
- Claude Opus 4 (`claude-opus-4-20250514`)

## 웹 검색 작동 방식

API 요청에 웹 검색 도구를 추가하면:

1. Claude가 프롬프트를 기반으로 언제 검색할지 결정합니다.
2. API가 검색을 실행하고 Claude에게 결과를 제공합니다. 이 과정은 단일 요청 중에 여러 번 반복될 수 있습니다.
3. 차례가 끝나면 Claude는 인용된 출처와 함께 최종 응답을 제공합니다.

## 웹 검색 사용 방법


> 조직의 관리자가 [콘솔](/settings/privacy)에서 웹 검색을 활성화해야 합니다.


API 요청에 웹 검색 도구를 제공합니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 1024,
        "messages": [
            {
                "role": "user",
                "content": "What is the weather in NYC?"
            }
        ],
        "tools": [{
            "type": "web_search_20250305",
            "name": "web_search",
            "max_uses": 5
        }]
    }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[
        {
            "role": "user",
            "content": "What's the weather in NYC?"
        }
    ],
    tools=[{
        "type": "web_search_20250305",
        "name": "web_search",
        "max_uses": 5
    }]
)
print(response)
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

async function main() {
  const response = await anthropic.messages.create({
    model: "claude-sonnet-4-5",
    max_tokens: 1024,
    messages: [
      {
        role: "user",
        content: "What's the weather in NYC?"
      }
    ],
    tools: [{
      type: "web_search_20250305",
      name: "web_search",
      max_uses: 5
    }]
  });

  console.log(response);
}

main().catch(console.error);
```
</CodeGroup>

### 도구 정의

웹 검색 도구는 다음 매개변수를 지원합니다:

```json JSON
{
  "type": "web_search_20250305",
  "name": "web_search",

  // 선택사항: 요청당 검색 횟수 제한
  "max_uses": 5,

  // 선택사항: 이러한 도메인의 결과만 포함
  "allowed_domains": ["example.com", "trusteddomain.org"],

  // 선택사항: 이러한 도메인의 결과는 절대 포함하지 않음
  "blocked_domains": ["untrustedsource.com"],

  // 선택사항: 검색 결과 지역화
  "user_location": {
    "type": "approximate",
    "city": "San Francisco",
    "region": "California",
    "country": "US",
    "timezone": "America/Los_Angeles"
  }
}
```

#### 최대 사용 횟수

`max_uses` 매개변수는 수행되는 검색 횟수를 제한합니다. Claude가 허용된 것보다 더 많은 검색을 시도하면, `web_search_tool_result`는 `max_uses_exceeded` 오류 코드와 함께 오류가 됩니다.

#### 도메인 필터링

도메인 필터 사용 시:

- 도메인은 HTTP/HTTPS 스킴을 포함하지 않아야 합니다 (`https://example.com` 대신 `example.com` 사용)
- 하위 도메인은 자동으로 포함됩니다 (`example.com`은 `docs.example.com`을 포함)
- 특정 하위 도메인은 해당 하위 도메인으로만 결과를 제한합니다 (`docs.example.com`은 `example.com`이나 `api.example.com`이 아닌 해당 하위 도메인의 결과만 반환)
- 하위 경로가 지원되며 경로 이후의 모든 것과 일치합니다 (`example.com/blog`는 `example.com/blog/post-1`과 일치)
- `allowed_domains` 또는 `blocked_domains` 중 하나만 사용할 수 있으며, 동일한 요청에서 둘 다 사용할 수 없습니다.

**와일드카드 지원:**

- 도메인 항목당 하나의 와일드카드(`*`)만 허용되며, 도메인 부분 이후(경로에서)에 나타나야 합니다
- 유효: `example.com/*`, `example.com/*/articles`
- 유효하지 않음: `*.example.com`, `ex*.com`, `example.com/*/news/*`

유효하지 않은 도메인 형식은 `invalid_tool_input` 도구 오류를 반환합니다.


> 요청 수준의 도메인 제한은 콘솔에서 구성된 조직 수준의 도메인 제한과 호환되어야 합니다. 요청 수준의 도메인은 도메인을 더 제한할 수만 있고, 조직 수준 목록을 재정의하거나 확장할 수 없습니다. 요청에 조직 설정과 충돌하는 도메인이 포함된 경우, API는 유효성 검사 오류를 반환합니다.


#### 지역화

`user_location` 매개변수를 사용하면 사용자의 위치에 따라 검색 결과를 지역화할 수 있습니다.

- `type`: 위치 유형 (`approximate`이어야 함)
- `city`: 도시 이름
- `region`: 지역 또는 주
- `country`: 국가
- `timezone`: [IANA 시간대 ID](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)

### 응답

다음은 응답 구조의 예입니다:

```json
{
  "role": "assistant",
  "content": [
    // 1. Claude의 검색 결정
    {
      "type": "text",
      "text": "I'll search for when Claude Shannon was born."
    },
    // 2. 사용된 검색 쿼리
    {
      "type": "server_tool_use",
      "id": "srvtoolu_01WYG3ziw53XMcoyKL4XcZmE",
      "name": "web_search",
      "input": {
        "query": "claude shannon birth date"
      }
    },
    // 3. 검색 결과
    {
      "type": "web_search_tool_result",
      "tool_use_id": "srvtoolu_01WYG3ziw53XMcoyKL4XcZmE",
      "content": [
        {
          "type": "web_search_result",
          "url": "https://en.wikipedia.org/wiki/Claude_Shannon",
          "title": "Claude Shannon - Wikipedia",
          "encrypted_content": "EqgfCioIARgBIiQ3YTAwMjY1Mi1mZjM5LTQ1NGUtODgxNC1kNjNjNTk1ZWI3Y...",
          "page_age": "April 30, 2025"
        }
      ]
    },
    {
      "text": "Based on the search results, ",
      "type": "text"
    },
    // 4. 인용과 함께하는 Claude의 응답
    {
      "text": "Claude Shannon was born on April 30, 1916, in Petoskey, Michigan",
      "type": "text",
      "citations": [
        {
          "type": "web_search_result_location",
          "url": "https://en.wikipedia.org/wiki/Claude_Shannon",
          "title": "Claude Shannon - Wikipedia",
          "encrypted_index": "Eo8BCioIAhgBIiQyYjQ0OWJmZi1lNm..",
          "cited_text": "Claude Elwood Shannon (April 30, 1916 – February 24, 2001) was an American mathematician, electrical engineer, computer scientist, cryptographer and i..."
        }
      ]
    }
  ],
  "id": "msg_a930390d3a",
  "usage": {
    "input_tokens": 6039,
    "output_tokens": 931,
    "server_tool_use": {
      "web_search_requests": 1
    }
  },
  "stop_reason": "end_turn"
}
```

#### 검색 결과

검색 결과에는 다음이 포함됩니다:

- `url`: 출처 페이지의 URL
- `title`: 출처 페이지의 제목
- `page_age`: 사이트가 마지막으로 업데이트된 시기
- `encrypted_content`: 인용을 위해 다중 턴 대화에서 다시 전달되어야 하는 암호화된 콘텐츠

#### 인용

인용은 웹 검색에 대해 항상 활성화되어 있으며, 각 `web_search_result_location`에는 다음이 포함됩니다:

- `url`: 인용된 출처의 URL
- `title`: 인용된 출처의 제목
- `encrypted_index`: 다중 턴 대화를 위해 다시 전달되어야 하는 참조
- `cited_text`: 인용된 콘텐츠의 최대 150자

웹 검색 인용 필드인 `cited_text`, `title`, `url`은 입력 또는 출력 토큰 사용량에 포함되지 않습니다.


> API 출력을 최종 사용자에게 직접 표시할 때는 원본 출처에 대한 인용이 포함되어야 합니다. 최종 사용자에게 표시하기 전에 재처리 및/또는 자체 자료와 결합하는 등 API 출력을 수정하는 경우, 법무팀과 상의하여 적절하게 인용을 표시하세요.


#### 오류

웹 검색 도구에서 오류(예: 속도 제한 도달)가 발생하면, Claude API는 여전히 200(성공) 응답을 반환합니다. 오류는 다음 구조를 사용하여 응답 본문 내에 표시됩니다:

```json
{
  "type": "web_search_tool_result",
  "tool_use_id": "servertoolu_a93jad",
  "content": {
    "type": "web_search_tool_result_error",
    "error_code": "max_uses_exceeded"
  }
}
```

가능한 오류 코드는 다음과 같습니다:

- `too_many_requests`: 속도 제한 초과
- `invalid_input`: 유효하지 않은 검색 쿼리 매개변수
- `max_uses_exceeded`: 최대 웹 검색 도구 사용 횟수 초과
- `query_too_long`: 쿼리가 최대 길이를 초과
- `unavailable`: 내부 오류 발생

#### `pause_turn` 중단 이유

응답에는 `pause_turn` 중단 이유가 포함될 수 있으며, 이는 API가 장시간 실행되는 턴을 일시 중지했음을 나타냅니다. 응답을 그대로 후속 요청에 제공하여 Claude가 턴을 계속하도록 하거나, 대화를 중단하려면 콘텐츠를 수정할 수 있습니다.

## 프롬프트 캐싱

웹 검색은 [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md)과 함께 작동합니다. 프롬프트 캐싱을 활성화하려면 요청에 최소 하나의 `cache_control` 중단점을 추가하세요. 시스템은 도구를 실행할 때 마지막 `web_search_tool_result` 블록까지 자동으로 캐시합니다.

다중 턴 대화의 경우, 마지막 `web_search_tool_result` 블록에 또는 그 이후에 `cache_control` 중단점을 설정하여 캐시된 콘텐츠를 재사용하세요.

예를 들어, 다중 턴 대화에 대해 웹 검색과 함께 프롬프트 캐싱을 사용하려면:

<CodeGroup>
```python
import anthropic

client = anthropic.Anthropic()

# 웹 검색 및 캐시 중단점이 있는 첫 번째 요청
messages = [
    {
        "role": "user",
        "content": "What's the current weather in San Francisco today?"
    }
]

response1 = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=messages,
    tools=[{
        "type": "web_search_20250305",
        "name": "web_search",
        "user_location": {
            "type": "approximate",
            "city": "San Francisco",
            "region": "California",
            "country": "US",
            "timezone": "America/Los_Angeles"
        }
    }]
)

# 대화에 Claude의 응답 추가
messages.append({
    "role": "assistant",
    "content": response1.content
})

# 검색 결과 이후에 캐시 중단점이 있는 두 번째 요청
messages.append({
    "role": "user",
    "content": "Should I expect rain later this week?",
    "cache_control": {"type": "ephemeral"}  # 이 지점까지 캐시
})

response2 = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=messages,
    tools=[{
        "type": "web_search_20250305",
        "name": "web_search",
        "user_location": {
            "type": "approximate",
            "city": "San Francisco",
            "region": "California",
            "country": "US",
            "timezone": "America/Los_Angeles"
        }
    }]
)
# 두 번째 응답은 캐시된 검색 결과의 이점을 누리면서
# 필요한 경우 여전히 새로운 검색을 수행할 수 있습니다
print(f"Cache read tokens: {response2.usage.get('cache_read_input_tokens', 0)}")
```

</CodeGroup>

## 스트리밍

스트리밍이 활성화되면 스트림의 일부로 검색 이벤트를 받게 됩니다. 검색이 실행되는 동안 일시 중지됩니다:

```javascript
event: message_start
data: {"type": "message_start", "message": {"id": "msg_abc123", "type": "message"}}

event: content_block_start
data: {"type": "content_block_start", "index": 0, "content_block": {"type": "text", "text": ""}}

// Claude의 검색 결정

event: content_block_start
data: {"type": "content_block_start", "index": 1, "content_block": {"type": "server_tool_use", "id": "srvtoolu_xyz789", "name": "web_search"}}

// 검색 쿼리 스트리밍
event: content_block_delta
data: {"type": "content_block_delta", "index": 1, "delta": {"type": "input_json_delta", "partial_json": "{\"query\":\"latest quantum computing breakthroughs 2025\"}"}}

// 검색 실행 중 일시 중지

// 검색 결과 스트리밍
event: content_block_start
data: {"type": "content_block_start", "index": 2, "content_block": {"type": "web_search_tool_result", "tool_use_id": "srvtoolu_xyz789", "content": [{"type": "web_search_result", "title": "Quantum Computing Breakthroughs in 2025", "url": "https://example.com"}]}}

// 인용과 함께하는 Claude의 응답 (이 예시에서 생략됨)
```

## 배치 요청

[메시지 배치 API](../02-capabilities/06-batch-processing.md)에 웹 검색 도구를 포함할 수 있습니다. 메시지 배치 API를 통한 웹 검색 도구 호출은 일반 메시지 API 요청과 동일하게 가격이 책정됩니다.

## 사용량 및 가격

웹 검색 사용량은 토큰 사용량에 추가로 청구됩니다:

```json
"usage": {
  "input_tokens": 105,
  "output_tokens": 6039,
  "cache_read_input_tokens": 7123,
  "cache_creation_input_tokens": 7345,
  "server_tool_use": {
    "web_search_requests": 1
  }
}
```

웹 검색은 Claude API에서 **1,000회 검색당 $10**, 그리고 검색으로 생성된 콘텐츠에 대한 표준 토큰 비용이 청구됩니다. 대화 전체에서 검색된 웹 검색 결과는 단일 턴에서 실행된 검색 반복과 후속 대화 턴에서 입력 토큰으로 계산됩니다.

각 웹 검색은 반환된 결과 수에 관계없이 한 번의 사용으로 계산됩니다. 웹 검색 중에 오류가 발생하면 웹 검색에 대한 요금이 청구되지 않습니다.

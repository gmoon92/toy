# [Streaming Messages](https://platform.claude.com/docs/en/build-with-claude/streaming)

---

Message를 생성할 때 `"stream": true`로 설정하여 [서버 전송 이벤트](https://developer.mozilla.org/en-US/Web/API/Server-sent%5Fevents/Using%5Fserver-sent%5Fevents) (SSE)를 사용하여 응답을 점진적으로 스트리밍할 수 있습니다.

## SDK를 사용한 스트리밍

[Python](https://github.com/anthropics/anthropic-sdk-python) 및 [TypeScript](https://github.com/anthropics/anthropic-sdk-typescript) SDK는 여러 가지 스트리밍 방법을 제공합니다. Python SDK는 동기 및 비동기 스트림을 모두 허용합니다. 자세한 내용은 각 SDK의 문서를 참조하세요.

<CodeGroup>
    ```python Python
    import anthropic

    client = anthropic.Anthropic()

    with client.messages.stream(
        max_tokens=1024,
        messages=[{"role": "user", "content": "Hello"}],
        model="claude-sonnet-4-5",
    ) as stream:
      for text in stream.text_stream:
          print(text, end="", flush=True)
    ```

    ```typescript TypeScript
    import Anthropic from '@anthropic-ai/sdk';

    const client = new Anthropic();

    await client.messages.stream({
        messages: [{role: 'user', content: "Hello"}],
        model: 'claude-sonnet-4-5',
        max_tokens: 1024,
    }).on('text', (text) => {
        console.log(text);
    });
    ```
</CodeGroup>

## 이벤트 유형

각 서버 전송 이벤트에는 이름이 지정된 이벤트 유형과 연결된 JSON 데이터가 포함됩니다. 각 이벤트는 SSE 이벤트 이름(예: `event: message_stop`)을 사용하고 데이터에 일치하는 이벤트 `type`을 포함합니다.

각 스트림은 다음 이벤트 흐름을 사용합니다:

1. `message_start`: 빈 `content`가 있는 `Message` 객체를 포함합니다.
2. 일련의 콘텐츠 블록. 각 블록에는 `content_block_start`, 하나 이상의 `content_block_delta` 이벤트 및 `content_block_stop` 이벤트가 있습니다. 각 콘텐츠 블록에는 최종 Message `content` 배열의 인덱스에 해당하는 `index`가 있습니다.
3. 하나 이상의 `message_delta` 이벤트, 최종 `Message` 객체에 대한 최상위 변경 사항을 나타냅니다.
4. 최종 `message_stop` 이벤트.

  
> `message_delta` 이벤트의 `usage` 필드에 표시된 토큰 수는 *누적*입니다.


### Ping 이벤트

이벤트 스트림에는 여러 `ping` 이벤트도 포함될 수 있습니다.

### 오류 이벤트

이벤트 스트림에서 때때로 [오류](https://platform.claude.com/docs/en/api/errors)를 보낼 수 있습니다. 예를 들어, 사용량이 많은 기간 동안 일반적으로 비스트리밍 컨텍스트에서 HTTP 529에 해당하는 `overloaded_error`를 받을 수 있습니다:

```json 오류 예제
event: error
data: {"type": "error", "error": {"type": "overloaded_error", "message": "Overloaded"}}
```

### 기타 이벤트

[버전 관리 정책](https://platform.claude.com/docs/en/api/versioning)에 따라 새 이벤트 유형을 추가할 수 있으며, 코드는 알 수 없는 이벤트 유형을 우아하게 처리해야 합니다.

## 콘텐츠 블록 델타 유형

각 `content_block_delta` 이벤트에는 주어진 `index`에서 `content` 블록을 업데이트하는 유형의 `delta`가 포함됩니다.

### 텍스트 델타

`text` 콘텐츠 블록 델타는 다음과 같습니다:
```json 텍스트 델타
event: content_block_delta
data: {"type": "content_block_delta","index": 0,"delta": {"type": "text_delta", "text": "ello frien"}}
```

### 입력 JSON 델타

`tool_use` 콘텐츠 블록에 대한 델타는 블록의 `input` 필드에 대한 업데이트에 해당합니다. 최대 세분성을 지원하기 위해 델타는 _부분 JSON 문자열_인 반면 최종 `tool_use.input`은 항상 _객체_입니다.

`content_block_stop` 이벤트를 받은 후 문자열 델타를 누적하고 JSON을 구문 분석하거나, [Pydantic](https://docs.pydantic.dev/latest/concepts/json/#partial-json-parsing)과 같은 라이브러리를 사용하여 부분 JSON 구문 분석을 수행하거나, 구문 분석된 증분 값에 액세스하는 헬퍼를 제공하는 [SDK](https://platform.claude.com/docs/en/api/client-sdks)를 사용할 수 있습니다.

`tool_use` 콘텐츠 블록 델타는 다음과 같습니다:
```json 입력 JSON 델타
event: content_block_delta
data: {"type": "content_block_delta","index": 1,"delta": {"type": "input_json_delta","partial_json": "{\"location\": \"San Fra"}}}
```
참고: 현재 모델은 한 번에 `input`에서 하나의 완전한 키와 값 속성만 내보내는 것을 지원합니다. 따라서 도구를 사용할 때 모델이 작업하는 동안 스트리밍 이벤트 사이에 지연이 있을 수 있습니다. `input` 키와 값이 누적되면 향후 모델에서 더 세밀한 세분성을 자동으로 지원할 수 있도록 청크된 부분 json과 함께 여러 `content_block_delta` 이벤트로 내보냅니다.

### Thinking 델타

스트리밍이 활성화된 상태에서 [extended thinking](../02-capabilities/03-extended-thinking.md)을 사용하면 `thinking_delta` 이벤트를 통해 thinking 콘텐츠를 받게 됩니다. 이러한 델타는 `thinking` 콘텐츠 블록의 `thinking` 필드에 해당합니다.

thinking 콘텐츠의 경우 `content_block_stop` 이벤트 직전에 특수 `signature_delta` 이벤트가 전송됩니다. 이 서명은 thinking 블록의 무결성을 확인하는 데 사용됩니다.

일반적인 thinking 델타는 다음과 같습니다:
```json Thinking 델타
event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "단계별로 풀어보겠습니다:\n\n1. 먼저 27 * 453을 분해합니다"}}
```

서명 델타는 다음과 같습니다:
```json 서명 델타
event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "signature_delta", "signature": "EqQBCgIYAhIM1gbcDa9GJwZA2b3hGgxBdjrkzLoky3dl1pkiMOYds..."}}
```

## 전체 HTTP 스트림 응답

스트리밍 모드를 사용할 때 [클라이언트 SDK](https://platform.claude.com/docs/en/api/client-sdks)를 사용하는 것을 강력히 권장합니다. 그러나 직접 API 통합을 구축하는 경우 이러한 이벤트를 직접 처리해야 합니다.

스트림 응답은 다음으로 구성됩니다:
1. `message_start` 이벤트
2. 잠재적으로 여러 콘텐츠 블록. 각 블록에는 다음이 포함됩니다:
    - `content_block_start` 이벤트
    - 잠재적으로 여러 `content_block_delta` 이벤트
    - `content_block_stop` 이벤트
3. `message_delta` 이벤트
4. `message_stop` 이벤트

응답 전체에 분산된 `ping` 이벤트도 있을 수 있습니다. 형식에 대한 자세한 내용은 [이벤트 유형](#event-types)을 참조하세요.

### 기본 스트리밍 요청

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --data \
'{
  "model": "claude-sonnet-4-5",
  "messages": [{"role": "user", "content": "Hello"}],
  "max_tokens": 256,
  "stream": true
}'
```

```python Python
import anthropic

client = anthropic.Anthropic()

with client.messages.stream(
    model="claude-sonnet-4-5",
    messages=[{"role": "user", "content": "Hello"}],
    max_tokens=256,
) as stream:
    for text in stream.text_stream:
        print(text, end="", flush=True)
```
</CodeGroup>

```json Response
event: message_start
data: {"type": "message_start", "message": {"id": "msg_1nZdL29xx5MUA1yADyHTEsnR8uuvGzszyY", "type": "message", "role": "assistant", "content": [], "model": "claude-sonnet-4-5-20250929", "stop_reason": null, "stop_sequence": null, "usage": {"input_tokens": 25, "output_tokens": 1}}}

event: content_block_start
data: {"type": "content_block_start", "index": 0, "content_block": {"type": "text", "text": ""}}

event: ping
data: {"type": "ping"}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "text_delta", "text": "Hello"}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "text_delta", "text": "!"}}

event: content_block_stop
data: {"type": "content_block_stop", "index": 0}

event: message_delta
data: {"type": "message_delta", "delta": {"stop_reason": "end_turn", "stop_sequence":null}, "usage": {"output_tokens": 15}}

event: message_stop
data: {"type": "message_stop"}

```

### 도구 사용이 있는 스트리밍 요청


> 도구 사용은 이제 베타 기능으로 매개변수 값에 대한 세밀한 스트리밍을 지원합니다. 자세한 내용은 [세밀한 도구 스트리밍](../03-tools/03-fine-grained-tool-streaming.md)을 참조하세요.


이 요청에서는 Claude에게 도구를 사용하여 날씨를 알려달라고 요청합니다.

<CodeGroup>
```bash Shell
  curl https://api.anthropic.com/v1/messages \
    -H "content-type: application/json" \
    -H "x-api-key: $ANTHROPIC_API_KEY" \
    -H "anthropic-version: 2023-06-01" \
    -d '{
      "model": "claude-sonnet-4-5",
      "max_tokens": 1024,
      "tools": [
        {
          "name": "get_weather",
          "description": "주어진 위치의 현재 날씨를 가져옵니다",
          "input_schema": {
            "type": "object",
            "properties": {
              "location": {
                "type": "string",
                "description": "도시와 주, 예: San Francisco, CA"
              }
            },
            "required": ["location"]
          }
        }
      ],
      "tool_choice": {"type": "any"},
      "messages": [
        {
          "role": "user",
          "content": "샌프란시스코의 날씨는 어떻습니까?"
        }
      ],
      "stream": true
    }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

tools = [
    {
        "name": "get_weather",
        "description": "주어진 위치의 현재 날씨를 가져옵니다",
        "input_schema": {
            "type": "object",
            "properties": {
                "location": {
                    "type": "string",
                    "description": "도시와 주, 예: San Francisco, CA"
                }
            },
            "required": ["location"]
        }
    }
]

with client.messages.stream(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=tools,
    tool_choice={"type": "any"},
    messages=[
        {
            "role": "user",
            "content": "샌프란시스코의 날씨는 어떻습니까?"
        }
    ],
) as stream:
    for text in stream.text_stream:
        print(text, end="", flush=True)
```
</CodeGroup>

```json Response
event: message_start
data: {"type":"message_start","message":{"id":"msg_014p7gG3wDgGV9EUtLvnow3U","type":"message","role":"assistant","model":"claude-sonnet-4-5-20250929","stop_sequence":null,"usage":{"input_tokens":472,"output_tokens":2},"content":[],"stop_reason":null}}

event: content_block_start
data: {"type":"content_block_start","index":0,"content_block":{"type":"text","text":""}}

event: ping
data: {"type": "ping"}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"Okay"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":","}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" let"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"'s"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" check"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" the"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" weather"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" for"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" San"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" Francisco"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":","}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" CA"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":":"}}

event: content_block_stop
data: {"type":"content_block_stop","index":0}

event: content_block_start
data: {"type":"content_block_start","index":1,"content_block":{"type":"tool_use","id":"toolu_01T1x1fJ34qAmk2tNTrN7Up6","name":"get_weather","input":{}}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":""}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":"{\"location\":"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":" \"San"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":" Francisc"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":"o,"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":" CA\""}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":", "}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":"\"unit\": \"fah"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":"renheit\"}"}}

event: content_block_stop
data: {"type":"content_block_stop","index":1}

event: message_delta
data: {"type":"message_delta","delta":{"stop_reason":"tool_use","stop_sequence":null},"usage":{"output_tokens":89}}

event: message_stop
data: {"type":"message_stop"}
```

### Extended thinking이 있는 스트리밍 요청

이 요청에서는 스트리밍과 함께 extended thinking을 활성화하여 Claude의 단계별 추론을 확인합니다.

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data \
'{
    "model": "claude-sonnet-4-5",
    "max_tokens": 20000,
    "stream": true,
    "thinking": {
        "type": "enabled",
        "budget_tokens": 16000
    },
    "messages": [
        {
            "role": "user",
            "content": "27 * 453은 무엇입니까?"
        }
    ]
}'
```

```python Python
import anthropic

client = anthropic.Anthropic()

with client.messages.stream(
    model="claude-sonnet-4-5",
    max_tokens=20000,
    thinking={
        "type": "enabled",
        "budget_tokens": 16000
    },
    messages=[
        {
            "role": "user",
            "content": "27 * 453은 무엇입니까?"
        }
    ],
) as stream:
    for event in stream:
        if event.type == "content_block_delta":
            if event.delta.type == "thinking_delta":
                print(event.delta.thinking, end="", flush=True)
            elif event.delta.type == "text_delta":
                print(event.delta.text, end="", flush=True)
```
</CodeGroup>

```json Response
event: message_start
data: {"type": "message_start", "message": {"id": "msg_01...", "type": "message", "role": "assistant", "content": [], "model": "claude-sonnet-4-5-20250929", "stop_reason": null, "stop_sequence": null}}

event: content_block_start
data: {"type": "content_block_start", "index": 0, "content_block": {"type": "thinking", "thinking": ""}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "단계별로 풀어보겠습니다:\n\n1. 먼저 27 * 453을 분해합니다"}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "\n2. 453 = 400 + 50 + 3"}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "\n3. 27 * 400 = 10,800"}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "\n4. 27 * 50 = 1,350"}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "\n5. 27 * 3 = 81"}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "\n6. 10,800 + 1,350 + 81 = 12,231"}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "signature_delta", "signature": "EqQBCgIYAhIM1gbcDa9GJwZA2b3hGgxBdjrkzLoky3dl1pkiMOYds..."}}

event: content_block_stop
data: {"type": "content_block_stop", "index": 0}

event: content_block_start
data: {"type": "content_block_start", "index": 1, "content_block": {"type": "text", "text": ""}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 1, "delta": {"type": "text_delta", "text": "27 * 453 = 12,231"}}

event: content_block_stop
data: {"type": "content_block_stop", "index": 1}

event: message_delta
data: {"type": "message_delta", "delta": {"stop_reason": "end_turn", "stop_sequence": null}}

event: message_stop
data: {"type": "message_stop"}
```

### 웹 검색 도구 사용이 있는 스트리밍 요청

이 요청에서는 Claude에게 현재 날씨 정보를 웹에서 검색하도록 요청합니다.

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data \
'{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "stream": true,
    "tools": [
        {
            "type": "web_search_20250305",
            "name": "web_search",
            "max_uses": 5
        }
    ],
    "messages": [
        {
            "role": "user",
            "content": "오늘 뉴욕시의 날씨는 어떻습니까?"
        }
    ]
}'
```

```python Python
import anthropic

client = anthropic.Anthropic()

with client.messages.stream(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[
        {
            "type": "web_search_20250305",
            "name": "web_search",
            "max_uses": 5
        }
    ],
    messages=[
        {
            "role": "user",
            "content": "오늘 뉴욕시의 날씨는 어떻습니까?"
        }
    ],
) as stream:
    for text in stream.text_stream:
        print(text, end="", flush=True)
```
</CodeGroup>

```json Response
event: message_start
data: {"type":"message_start","message":{"id":"msg_01G...","type":"message","role":"assistant","model":"claude-sonnet-4-5-20250929","content":[],"stop_reason":null,"stop_sequence":null,"usage":{"input_tokens":2679,"cache_creation_input_tokens":0,"cache_read_input_tokens":0,"output_tokens":3}}}

event: content_block_start
data: {"type":"content_block_start","index":0,"content_block":{"type":"text","text":""}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"뉴욕시의"}}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":" 현재 날씨를 확인하겠습니다"}}

event: ping
data: {"type": "ping"}

event: content_block_delta
data: {"type":"content_block_delta","index":0,"delta":{"type":"text_delta","text":"."}}

event: content_block_stop
data: {"type":"content_block_stop","index":0}

event: content_block_start
data: {"type":"content_block_start","index":1,"content_block":{"type":"server_tool_use","id":"srvtoolu_014hJH82Qum7Td6UV8gDXThB","name":"web_search","input":{}}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":""}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":"{\"query"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":"\":"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":" \"weather"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":" NY"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":"C to"}}

event: content_block_delta
data: {"type":"content_block_delta","index":1,"delta":{"type":"input_json_delta","partial_json":"day\"}"}}

event: content_block_stop
data: {"type":"content_block_stop","index":1 }

event: content_block_start
data: {"type":"content_block_start","index":2,"content_block":{"type":"web_search_tool_result","tool_use_id":"srvtoolu_014hJH82Qum7Td6UV8gDXThB","content":[{"type":"web_search_result","title":"Weather in New York City in May 2025 (New York) - detailed Weather Forecast for a month","url":"https://world-weather.info/forecast/usa/new_york/may-2025/","encrypted_content":"Ev0DCioIAxgCIiQ3NmU4ZmI4OC1k...","page_age":null},...]}}

event: content_block_stop
data: {"type":"content_block_stop","index":2}

event: content_block_start
data: {"type":"content_block_start","index":3,"content_block":{"type":"text","text":""}}

event: content_block_delta
data: {"type":"content_block_delta","index":3,"delta":{"type":"text_delta","text":"다음은 뉴욕시의 현재 날씨 정보입니다"}}

event: content_block_delta
data: {"type":"content_block_delta","index":3,"delta":{"type":"text_delta","text":":\n\n# 날씨"}}

event: content_block_delta
data: {"type":"content_block_delta","index":3,"delta":{"type":"text_delta","text":" in 뉴욕시"}}

event: content_block_delta
data: {"type":"content_block_delta","index":3,"delta":{"type":"text_delta","text":"\n\n"}}

...

event: content_block_stop
data: {"type":"content_block_stop","index":17}

event: message_delta
data: {"type":"message_delta","delta":{"stop_reason":"end_turn","stop_sequence":null},"usage":{"input_tokens":10682,"cache_creation_input_tokens":0,"cache_read_input_tokens":0,"output_tokens":510,"server_tool_use":{"web_search_requests":1}}}

event: message_stop
data: {"type":"message_stop"}
```

## 오류 복구

네트워크 문제, 타임아웃 또는 기타 오류로 인해 스트리밍 요청이 중단된 경우 스트림이 중단된 지점부터 재개하여 복구할 수 있습니다. 이 접근 방식을 사용하면 전체 응답을 다시 처리하지 않아도 됩니다.

기본 복구 전략은 다음과 같습니다:

1. **부분 응답 캡처**: 오류가 발생하기 전에 성공적으로 수신한 모든 콘텐츠를 저장합니다
2. **계속 요청 구성**: 부분 어시스턴트 응답을 새 어시스턴트 메시지의 시작으로 포함하는 새 API 요청을 생성합니다
3. **스트리밍 재개**: 중단된 지점부터 나머지 응답을 계속 수신합니다

### 오류 복구 모범 사례

1. **SDK 기능 사용**: SDK의 내장 메시지 누적 및 오류 처리 기능을 활용합니다
2. **콘텐츠 유형 처리**: 메시지에 여러 콘텐츠 블록(`text`, `tool_use`, `thinking`)이 포함될 수 있음을 인식합니다. 도구 사용 및 extended thinking 블록은 부분적으로 복구할 수 없습니다. 가장 최근의 텍스트 블록에서 스트리밍을 재개할 수 있습니다.

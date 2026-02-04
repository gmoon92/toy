# [Extended thinking으로 구축하기](https://platform.claude.com/docs/en/build-with-claude/extended-thinking)

---

Extended thinking은 Claude에게 복잡한 작업을 위한 향상된 추론 능력을 제공하며, 최종 답변을 제공하기 전에 단계별 사고 과정에 대한 다양한 수준의 투명성을 제공합니다.

## 지원되는 모델

Extended thinking은 다음 모델에서 지원됩니다:

- Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`)
- Claude Sonnet 4 (`claude-sonnet-4-20250514`)
- Claude Sonnet 3.7 (`claude-3-7-sonnet-20250219`) ([deprecated](https://platform.claude.com/docs/en/about-claude/model-deprecations))
- Claude Haiku 4.5 (`claude-haiku-4-5-20251001`)
- Claude Opus 4.5 (`claude-opus-4-5-20251101`)
- Claude Opus 4.1 (`claude-opus-4-1-20250805`)
- Claude Opus 4 (`claude-opus-4-20250514`)


> API 동작은 Claude Sonnet 3.7과 Claude 4 모델 간에 다르지만 API 형태는 정확히 동일하게 유지됩니다.
>
> 자세한 내용은 [모델 버전별 thinking 차이](#differences-in-thinking-across-model-versions)를 참조하세요.


## Extended thinking 작동 방식

Extended thinking이 켜져 있으면 Claude는 내부 추론을 출력하는 `thinking` 콘텐츠 블록을 생성합니다. Claude는 최종 응답을 작성하기 전에 이 추론에서 얻은 통찰력을 통합합니다.

API 응답에는 `thinking` 콘텐츠 블록이 포함되고 그 뒤에 `text` 콘텐츠 블록이 포함됩니다.

다음은 기본 응답 형식의 예입니다:

```json
{
  "content": [
    {
      "type": "thinking",
      "thinking": "단계별로 분석해 보겠습니다...",
      "signature": "WaUjzkypQ2mUEVM36O2TxuC06KN8xyfbJwyem2dw3URve/op91XWHOEBLLqIOMfFG/UvLEczmEsUjavL...."
    },
    {
      "type": "text",
      "text": "분석 결과를 바탕으로..."
    }
  ]
}
```

Extended thinking의 응답 형식에 대한 자세한 내용은 [Messages API Reference](https://platform.claude.com/docs/en/api/messages)를 참조하세요.

## Extended thinking 사용 방법

다음은 Messages API에서 extended thinking을 사용하는 예제입니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data \
'{
    "model": "claude-sonnet-4-5",
    "max_tokens": 16000,
    "thinking": {
        "type": "enabled",
        "budget_tokens": 10000
    },
    "messages": [
        {
            "role": "user",
            "content": "n mod 4 == 3인 무한한 수의 소수가 존재합니까?"
        }
    ]
}'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=16000,
    thinking={
        "type": "enabled",
        "budget_tokens": 10000
    },
    messages=[{
        "role": "user",
        "content": "n mod 4 == 3인 무한한 수의 소수가 존재합니까?"
    }]
)

# 응답에는 요약된 thinking 블록과 text 블록이 포함됩니다
for block in response.content:
    if block.type == "thinking":
        print(f"\nThinking 요약: {block.thinking}")
    elif block.type == "text":
        print(f"\n응답: {block.text}")
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const client = new Anthropic();

const response = await client.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 16000,
  thinking: {
    type: "enabled",
    budget_tokens: 10000
  },
  messages: [{
    role: "user",
    content: "n mod 4 == 3인 무한한 수의 소수가 존재합니까?"
  }]
});

// 응답에는 요약된 thinking 블록과 text 블록이 포함됩니다
for (const block of response.content) {
  if (block.type === "thinking") {
    console.log(`\nThinking 요약: ${block.thinking}`);
  } else if (block.type === "text") {
    console.log(`\n응답: ${block.text}`);
  }
}
```

```java Java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.beta.messages.*;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.messages.*;

public class SimpleThinkingExample {
    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        BetaMessage response = client.beta().messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_OPUS_4_0)
                        .maxTokens(16000)
                        .thinking(BetaThinkingConfigEnabled.builder().budgetTokens(10000).build())
                        .addUserMessage("n mod 4 == 3인 무한한 수의 소수가 존재합니까?")
                        .build()
        );

        System.out.println(response);
    }
}
```

</CodeGroup>

Extended thinking을 켜려면 `thinking` 객체를 추가하고, `type` 매개변수를 `enabled`로 설정하고, `budget_tokens`를 extended thinking에 대한 지정된 토큰 예산으로 설정하세요.

`budget_tokens` 매개변수는 Claude가 내부 추론 프로세스에 사용할 수 있는 최대 토큰 수를 결정합니다. Claude 4 모델에서 이 제한은 전체 thinking 토큰에 적용되며 [요약된 출력](#summarized-thinking)에는 적용되지 않습니다. 더 큰 예산은 복잡한 문제에 대해 보다 철저한 분석을 가능하게 하여 응답 품질을 향상시킬 수 있지만, Claude는 특히 32k 이상의 범위에서 할당된 전체 예산을 사용하지 않을 수 있습니다.

`budget_tokens`는 `max_tokens`보다 작은 값으로 설정해야 합니다. 그러나 [도구와 함께 인터리브 thinking](#interleaved-thinking)을 사용할 때는 토큰 제한이 전체 컨텍스트 윈도우(200k 토큰)가 되므로 이 제한을 초과할 수 있습니다.

### 요약된 thinking

Extended thinking이 활성화되면 Claude 4 모델용 Messages API는 Claude의 전체 thinking 프로세스 요약을 반환합니다. 요약된 thinking은 오용을 방지하면서 extended thinking의 전체 지능 이점을 제공합니다.

요약된 thinking에 대한 몇 가지 중요한 고려 사항은 다음과 같습니다:

- 원래 요청에서 생성된 전체 thinking 토큰에 대해 요금이 청구되며 요약 토큰에 대해서는 청구되지 않습니다.
- 청구된 출력 토큰 수는 응답에서 보는 토큰 수와 **일치하지 않습니다**.
- thinking 출력의 처음 몇 줄은 더 자세하며 프롬프트 엔지니어링 목적에 특히 유용한 상세한 추론을 제공합니다.
- Anthropic이 extended thinking 기능을 개선하려고 노력함에 따라 요약 동작은 변경될 수 있습니다.
- 요약은 최소한의 추가 지연 시간으로 Claude의 thinking 프로세스의 핵심 아이디어를 보존하여 스트리밍 가능한 사용자 경험과 Claude Sonnet 3.7에서 Claude 4 모델로의 쉬운 마이그레이션을 가능하게 합니다.
- 요약은 요청에서 대상으로 하는 모델과 다른 모델에 의해 처리됩니다. thinking 모델은 요약된 출력을 보지 않습니다.


> Claude Sonnet 3.7은 계속해서 전체 thinking 출력을 반환합니다.
>
> Claude 4 모델에 대한 전체 thinking 출력에 액세스해야 하는 드문 경우 [영업 팀에 문의](mailto:sales@anthropic.com)하세요.


### Thinking 스트리밍

[서버 전송 이벤트(SSE)](https://developer.mozilla.org/en-US/Web/API/Server-sent%5Fevents/Using%5Fserver-sent%5Fevents)를 사용하여 extended thinking 응답을 스트리밍할 수 있습니다.

Extended thinking에 대해 스트리밍이 활성화되면 `thinking_delta` 이벤트를 통해 thinking 콘텐츠를 받습니다.

Messages API를 통한 스트리밍에 대한 자세한 문서는 [Streaming Messages](../02-capabilities/05-streaming-messages.md)를 참조하세요.

다음은 thinking과 함께 스트리밍을 처리하는 방법입니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data \
'{
    "model": "claude-sonnet-4-5",
    "max_tokens": 16000,
    "stream": true,
    "thinking": {
        "type": "enabled",
        "budget_tokens": 10000
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
    max_tokens=16000,
    thinking={"type": "enabled", "budget_tokens": 10000},
    messages=[{"role": "user", "content": "27 * 453은 무엇입니까?"}],
) as stream:
    thinking_started = False
    response_started = False

    for event in stream:
        if event.type == "content_block_start":
            print(f"\n{event.content_block.type} 블록 시작...")
            # 각 새 블록에 대해 플래그 재설정
            thinking_started = False
            response_started = False
        elif event.type == "content_block_delta":
            if event.delta.type == "thinking_delta":
                if not thinking_started:
                    print("Thinking: ", end="", flush=True)
                    thinking_started = True
                print(event.delta.thinking, end="", flush=True)
            elif event.delta.type == "text_delta":
                if not response_started:
                    print("Response: ", end="", flush=True)
                    response_started = True
                print(event.delta.text, end="", flush=True)
        elif event.type == "content_block_stop":
            print("\n블록 완료.")
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const client = new Anthropic();

const stream = await client.messages.stream({
  model: "claude-sonnet-4-5",
  max_tokens: 16000,
  thinking: {
    type: "enabled",
    budget_tokens: 10000
  },
  messages: [{
    role: "user",
    content: "27 * 453은 무엇입니까?"
  }]
});

let thinkingStarted = false;
let responseStarted = false;

for await (const event of stream) {
  if (event.type === 'content_block_start') {
    console.log(`\n${event.content_block.type} 블록 시작...`);
    // 각 새 블록에 대해 플래그 재설정
    thinkingStarted = false;
    responseStarted = false;
  } else if (event.type === 'content_block_delta') {
    if (event.delta.type === 'thinking_delta') {
      if (!thinkingStarted) {
        process.stdout.write('Thinking: ');
        thinkingStarted = true;
      }
      process.stdout.write(event.delta.thinking);
    } else if (event.delta.type === 'text_delta') {
      if (!responseStarted) {
        process.stdout.write('Response: ');
        responseStarted = true;
      }
      process.stdout.write(event.delta.text);
    }
  } else if (event.type === 'content_block_stop') {
    console.log('\n블록 완료.');
  }
}
```

```java Java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.beta.messages.BetaRawMessageStreamEvent;
import com.anthropic.models.beta.messages.BetaThinkingConfigEnabled;
import com.anthropic.models.messages.Model;

public class SimpleThinkingStreamingExample {
    private static boolean thinkingStarted = false;
    private static boolean responseStarted = false;

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        MessageCreateParams createParams = MessageCreateParams.builder()
                .model(Model.CLAUDE_OPUS_4_0)
                .maxTokens(16000)
                .thinking(BetaThinkingConfigEnabled.builder().budgetTokens(10000).build())
                .addUserMessage("27 * 453은 무엇입니까?")
                .build();

        try (StreamResponse<BetaRawMessageStreamEvent> streamResponse =
                     client.beta().messages().createStreaming(createParams)) {
            streamResponse.stream()
                    .forEach(event -> {
                        if (event.isContentBlockStart()) {
                            System.out.printf("\n%s 블록 시작...%n",
                                    event.asContentBlockStart()._type());
                            // 각 새 블록에 대해 플래그 재설정
                            thinkingStarted = false;
                            responseStarted = false;
                        } else if (event.isContentBlockDelta()) {
                            var delta = event.asContentBlockDelta().delta();
                            if (delta.isBetaThinking()) {
                                if (!thinkingStarted) {
                                    System.out.print("Thinking: ");
                                    thinkingStarted = true;
                                }
                                System.out.print(delta.asBetaThinking().thinking());
                                System.out.flush();
                            } else if (delta.isBetaText()) {
                                if (!responseStarted) {
                                    System.out.print("Response: ");
                                    responseStarted = true;
                                }
                                System.out.print(delta.asBetaText().text());
                                System.out.flush();
                            }
                        } else if (event.isContentBlockStop()) {
                            System.out.println("\n블록 완료.");
                        }
                    });
        }
    }
}
```

</CodeGroup>

스트리밍 출력 예제:
```json
event: message_start
data: {"type": "message_start", "message": {"id": "msg_01...", "type": "message", "role": "assistant", "content": [], "model": "claude-sonnet-4-5", "stop_reason": null, "stop_sequence": null}}

event: content_block_start
data: {"type": "content_block_start", "index": 0, "content_block": {"type": "thinking", "thinking": ""}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "단계별로 풀어보겠습니다:\n\n1. 먼저 27 * 453을 분해합니다"}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "thinking_delta", "thinking": "\n2. 453 = 400 + 50 + 3"}}

// 추가 thinking 델타...

event: content_block_delta
data: {"type": "content_block_delta", "index": 0, "delta": {"type": "signature_delta", "signature": "EqQBCgIYAhIM1gbcDa9GJwZA2b3hGgxBdjrkzLoky3dl1pkiMOYds..."}}

event: content_block_stop
data: {"type": "content_block_stop", "index": 0}

event: content_block_start
data: {"type": "content_block_start", "index": 1, "content_block": {"type": "text", "text": ""}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 1, "delta": {"type": "text_delta", "text": "27 * 453 = 12,231"}}

// 추가 text 델타...

event: content_block_stop
data: {"type": "content_block_stop", "index": 1}

event: message_delta
data: {"type": "message_delta", "delta": {"stop_reason": "end_turn", "stop_sequence": null}}

event: message_stop
data: {"type": "message_stop"}
```


> thinking이 활성화된 스트리밍을 사용할 때 텍스트가 때때로 더 큰 청크로 도착하고 더 작은 토큰별 전달과 교대로 나타날 수 있습니다. 이것은 예상되는 동작이며 특히 thinking 콘텐츠에 해당합니다.
>
> 스트리밍 시스템은 최적의 성능을 위해 콘텐츠를 배치로 처리해야 하므로 스트리밍 이벤트 간에 가능한 지연과 함께 이 "청크형" 전달 패턴이 발생할 수 있습니다. 향후 업데이트가 thinking 콘텐츠를 더 부드럽게 스트리밍하는 데 중점을 두고 이 경험을 개선하기 위해 지속적으로 노력하고 있습니다.


## 도구 사용과 함께 Extended thinking

Extended thinking은 [도구 사용](../03-tools/01-overview.md)과 함께 사용할 수 있어 Claude가 도구 선택 및 결과 처리를 통해 추론할 수 있습니다.

도구 사용과 함께 extended thinking을 사용할 때 다음 제한 사항에 유의하세요:

1. **도구 선택 제한**: thinking과 함께 도구 사용은 `tool_choice: {"type": "auto"}`(기본값) 또는 `tool_choice: {"type": "none"}`만 지원합니다. `tool_choice: {"type": "any"}` 또는 `tool_choice: {"type": "tool", "name": "..."}`를 사용하면 오류가 발생합니다. 이러한 옵션은 extended thinking과 호환되지 않는 도구 사용을 강제하기 때문입니다.

2. **Thinking 블록 보존**: 도구 사용 중에 마지막 어시스턴트 메시지에 대해 `thinking` 블록을 API에 다시 전달해야 합니다. 추론 연속성을 유지하려면 완전히 수정되지 않은 블록을 API에 다시 포함하세요.

### 대화 중 thinking 모드 전환

도구 사용 루프를 포함하여 어시스턴트 턴 중간에 thinking을 전환할 수 없습니다. 전체 어시스턴트 턴은 단일 thinking 모드에서 작동해야 합니다:

- **thinking이 활성화된 경우**, 최종 어시스턴트 턴은 thinking 블록으로 시작해야 합니다.
- **thinking이 비활성화된 경우**, 최종 어시스턴트 턴에는 thinking 블록이 포함되어서는 안 됩니다.

모델의 관점에서 **도구 사용 루프는 어시스턴트 턴의 일부**입니다. 어시스턴트 턴은 Claude가 여러 도구 호출과 결과를 포함할 수 있는 전체 응답을 완료할 때까지 완료되지 않습니다.

예를 들어, 이 시퀀스는 모두 **단일 어시스턴트 턴**의 일부입니다:
```
사용자: "파리의 날씨는?"
어시스턴트: [thinking] + [tool_use: get_weather]
사용자: [tool_result: "20°C, 맑음"]
어시스턴트: [text: "파리의 날씨는 20°C이고 맑습니다"]
```

여러 API 메시지가 있더라도 도구 사용 루프는 개념적으로 하나의 연속적인 어시스턴트 응답의 일부입니다.

#### 우아한 thinking 저하

턴 중간 thinking 충돌이 발생하면(예: 도구 사용 루프 중에 thinking을 켜거나 끄는 경우) API는 해당 요청에 대해 자동으로 thinking을 비활성화합니다. 모델 품질을 보존하고 분포 내에 유지하기 위해 API는 다음을 수행할 수 있습니다:

- 잘못된 턴 구조를 생성하는 경우 대화에서 thinking 블록을 제거합니다
- 대화 기록이 thinking이 활성화되는 것과 호환되지 않을 때 현재 요청에 대해 thinking을 비활성화합니다

즉, 턴 중간에 thinking을 전환하려고 시도해도 오류가 발생하지 않지만 해당 요청에 대해 thinking이 자동으로 비활성화됩니다. thinking이 활성화되었는지 확인하려면 응답에 `thinking` 블록이 있는지 확인하세요.

#### 실용적 지침

**모범 사례**: 턴 중간에 전환하려고 하기보다는 각 턴이 시작될 때 thinking 전략을 계획하세요.

**예제: 턴 완료 후 thinking 전환**
```
사용자: "날씨는?"
어시스턴트: [tool_use] (thinking 비활성화)
사용자: [tool_result]
어시스턴트: [text: "맑습니다"]
사용자: "내일은 어떻습니까?"
어시스턴트: [thinking] + [text: "..."] (thinking 활성화 - 새 턴)
```

어시스턴트 턴을 완료한 후 thinking을 전환하면 새 요청에 대해 thinking이 실제로 활성화되도록 보장합니다.


> thinking 모드를 전환하면 메시지 기록에 대한 프롬프트 캐싱도 무효화됩니다. 자세한 내용은 [프롬프트 캐싱과 함께 Extended thinking](#extended-thinking-with-prompt-caching) 섹션을 참조하세요.


<details>
<summary>예제: 도구 결과와 함께 thinking 블록 전달</summary>

다음은 도구 결과를 제공할 때 thinking 블록을 보존하는 방법을 보여주는 실용적인 예제입니다:

<CodeGroup>
```python Python
weather_tool = {
    "name": "get_weather",
    "description": "위치에 대한 현재 날씨를 가져옵니다",
    "input_schema": {
        "type": "object",
        "properties": {
            "location": {"type": "string"}
        },
        "required": ["location"]
    }
}

# 첫 번째 요청 - Claude가 thinking 및 도구 요청으로 응답
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=16000,
    thinking={
        "type": "enabled",
        "budget_tokens": 10000
    },
    tools=[weather_tool],
    messages=[
        {"role": "user", "content": "파리의 날씨는?"}
    ]
)
```

```typescript TypeScript
const weatherTool = {
  name: "get_weather",
  description: "위치에 대한 현재 날씨를 가져옵니다",
  input_schema: {
    type: "object",
    properties: {
      location: { type: "string" }
    },
    required: ["location"]
  }
};

// 첫 번째 요청 - Claude가 thinking 및 도구 요청으로 응답
const response = await client.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 16000,
  thinking: {
    type: "enabled",
    budget_tokens: 10000
  },
  tools: [weatherTool],
  messages: [
    { role: "user", content: "파리의 날씨는?" }
  ]
});
```

```java Java
import java.util.List;
import java.util.Map;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.beta.messages.BetaMessage;
import com.anthropic.models.beta.messages.MessageCreateParams;
import com.anthropic.models.beta.messages.BetaThinkingConfigEnabled;
import com.anthropic.models.beta.messages.BetaTool;
import com.anthropic.models.beta.messages.BetaTool.InputSchema;
import com.anthropic.models.messages.Model;

public class ThinkingWithToolsExample {
    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        InputSchema schema = InputSchema.builder()
                .properties(JsonValue.from(Map.of(
                        "location", Map.of("type", "string")
                )))
                .putAdditionalProperty("required", JsonValue.from(List.of("location")))
                .build();

        BetaTool weatherTool = BetaTool.builder()
                .name("get_weather")
                .description("위치에 대한 현재 날씨를 가져옵니다")
                .inputSchema(schema)
                .build();

        BetaMessage response = client.beta().messages().create(
                MessageCreateParams.builder()
                        .model(Model.CLAUDE_OPUS_4_0)
                        .maxTokens(16000)
                        .thinking(BetaThinkingConfigEnabled.builder().budgetTokens(10000).build())
                        .addTool(weatherTool)
                        .addUserMessage("파리의 날씨는?")
                        .build()
        );

        System.out.println(response);
    }
}
```
</CodeGroup>

API 응답에는 thinking, text, tool_use 블록이 포함됩니다:

```json
{
    "content": [
        {
            "type": "thinking",
            "thinking": "사용자가 파리의 현재 날씨를 알고 싶어합니다. `get_weather` 함수에 액세스할 수 있습니다...",
            "signature": "BDaL4VrbR2Oj0hO4XpJxT28J5TILnCrrUXoKiiNBZW9P+nr8XSj1zuZzAl4egiCCpQNvfyUuFFJP5CncdYZEQPPmLxYsNrcs...."
        },
        {
            "type": "text",
            "text": "파리의 현재 날씨 정보를 가져오는 데 도움을 드릴 수 있습니다. 확인해 보겠습니다"
        },
        {
            "type": "tool_use",
            "id": "toolu_01CswdEQBMshySk6Y9DFKrfq",
            "name": "get_weather",
            "input": {
                "location": "Paris"
            }
        }
    ]
}
```

이제 대화를 계속하고 도구를 사용하겠습니다

<CodeGroup>
```python Python
# thinking 블록과 tool use 블록 추출
thinking_block = next((block for block in response.content
                      if block.type == 'thinking'), None)
tool_use_block = next((block for block in response.content
                      if block.type == 'tool_use'), None)

# 실제 날씨 API 호출, 실제 API 호출이 여기에 들어갑니다
# 이것이 반환된다고 가정합시다
weather_data = {"temperature": 88}

# 두 번째 요청 - thinking 블록과 도구 결과 포함
# 응답에 새 thinking 블록이 생성되지 않습니다
continuation = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=16000,
    thinking={
        "type": "enabled",
        "budget_tokens": 10000
    },
    tools=[weather_tool],
    messages=[
        {"role": "user", "content": "파리의 날씨는?"},
        # thinking_block이 tool_use_block과 함께 전달되는 것에 주목하세요
        # 이것이 전달되지 않으면 오류가 발생합니다
        {"role": "assistant", "content": [thinking_block, tool_use_block]},
        {"role": "user", "content": [{
            "type": "tool_result",
            "tool_use_id": tool_use_block.id,
            "content": f"현재 온도: {weather_data['temperature']}°F"
        }]}
    ]
)
```

```typescript TypeScript
// thinking 블록과 tool use 블록 추출
const thinkingBlock = response.content.find(block =>
  block.type === 'thinking');
const toolUseBlock = response.content.find(block =>
  block.type === 'tool_use');

// 실제 날씨 API 호출, 실제 API 호출이 여기에 들어갑니다
// 이것이 반환된다고 가정합시다
const weatherData = { temperature: 88 };

// 두 번째 요청 - thinking 블록과 도구 결과 포함
// 응답에 새 thinking 블록이 생성되지 않습니다
const continuation = await client.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 16000,
  thinking: {
    type: "enabled",
    budget_tokens: 10000
  },
  tools: [weatherTool],
  messages: [
    { role: "user", content: "파리의 날씨는?" },
    // thinkingBlock이 toolUseBlock과 함께 전달되는 것에 주목하세요
    // 이것이 전달되지 않으면 오류가 발생합니다
    { role: "assistant", content: [thinkingBlock, toolUseBlock] },
    { role: "user", content: [{
      type: "tool_result",
      tool_use_id: toolUseBlock.id,
      content: `현재 온도: ${weatherData.temperature}°F`
    }]}
  ]
});
```

```java Java
// thinking 블록과 tool use 블록 추출 코드는 위 Java 예제 참조

if (thinkingBlockOpt.isPresent() && toolUseBlockOpt.isPresent()) {
    BetaThinkingBlock thinkingBlock = thinkingBlockOpt.get();
    BetaToolUseBlock toolUseBlock = toolUseBlockOpt.get();

    // 실제 날씨 API 호출
    Map<String, Object> weatherData = Map.of("temperature", 88);

    // 두 번째 요청 - thinking 블록과 도구 결과 포함
    BetaMessage continuation = client.beta().messages().create(
            MessageCreateParams.builder()
                    .model(Model.CLAUDE_OPUS_4_0)
                    .maxTokens(16000)
                    .thinking(BetaThinkingConfigEnabled.builder().budgetTokens(10000).build())
                    .addTool(weatherTool)
                    .addUserMessage("파리의 날씨는?")
                    .addAssistantMessageOfBetaContentBlockParams(
                            // thinkingBlock이 toolUseBlock과 함께 전달되는 것에 주목하세요
                            List.of(
                                    BetaContentBlockParam.ofThinking(thinkingBlock.toParam()),
                                    BetaContentBlockParam.ofToolUse(toolUseBlock.toParam())
                            )
                    )
                    .addUserMessageOfBetaContentBlockParams(List.of(
                            BetaContentBlockParam.ofToolResult(
                                    BetaToolResultBlockParam.builder()
                                            .toolUseId(toolUseBlock.id())
                                            .content(String.format("현재 온도: %d°F", (Integer)weatherData.get("temperature")))
                                            .build()
                            )
                    ))
                    .build()
    );

    System.out.println(continuation);
}
```
</CodeGroup>

이제 API 응답에는 **text만** 포함됩니다

```json
{
    "content": [
        {
            "type": "text",
            "text": "현재 파리의 온도는 88°F(31°C)입니다"
        }
    ]
}
```
</details>

### Thinking 블록 보존

도구 사용 중에 `thinking` 블록을 API에 다시 전달해야 하며 완전히 수정되지 않은 블록을 API에 다시 포함해야 합니다. 이는 모델의 추론 흐름과 대화 무결성을 유지하는 데 중요합니다.


> 이전 `assistant` 역할 턴에서 `thinking` 블록을 생략할 수 있지만 다회전 대화의 경우 항상 모든 thinking 블록을 API에 다시 전달하는 것이 좋습니다. API는 다음을 수행합니다:
> - 제공된 thinking 블록을 자동으로 필터링합니다
> - 모델의 추론을 보존하는 데 필요한 관련 thinking 블록을 사용합니다
> - Claude에게 표시된 블록에 대한 입력 토큰에 대해서만 청구합니다



> 대화 중에 thinking 모드를 전환할 때 전체 어시스턴트 턴(도구 사용 루프 포함)이 단일 thinking 모드에서 작동해야 한다는 점을 기억하세요. 자세한 내용은 [대화 중 thinking 모드 전환](#toggling-thinking-modes-in-conversations)을 참조하세요.


Claude가 도구를 호출하면 외부 정보를 기다리기 위해 응답 구성을 일시 중지합니다. 도구 결과가 반환되면 Claude는 기존 응답 구축을 계속합니다. 이는 여러 가지 이유로 도구 사용 중에 thinking 블록을 보존해야 합니다:

1. **추론 연속성**: thinking 블록은 도구 요청으로 이어진 Claude의 단계별 추론을 캡처합니다. 도구 결과를 게시할 때 원래 thinking을 포함하면 Claude가 중단한 부분에서 추론을 계속할 수 있습니다.

2. **컨텍스트 유지**: 도구 결과가 API 구조에서 사용자 메시지로 나타나지만 연속적인 추론 흐름의 일부입니다. thinking 블록을 보존하면 여러 API 호출에 걸쳐 이 개념적 흐름이 유지됩니다. 컨텍스트 관리에 대한 자세한 내용은 [컨텍스트 윈도우 가이드](./01-build-with-claude-02-context-windows.md)를 참조하세요.

**중요**: `thinking` 블록을 제공할 때 연속적인 `thinking` 블록의 전체 시퀀스는 원래 요청 중에 모델이 생성한 출력과 일치해야 합니다. 이러한 블록의 시퀀스를 재배열하거나 수정할 수 없습니다.

### 인터리브 thinking

Claude 4 모델의 도구 사용과 함께 extended thinking은 인터리브 thinking을 지원하여 Claude가 도구 호출 사이에 생각하고 도구 결과를 받은 후 보다 정교한 추론을 할 수 있습니다.

인터리브 thinking을 사용하면 Claude는 다음을 수행할 수 있습니다:
- 다음 단계를 결정하기 전에 도구 호출 결과에 대해 추론합니다
- 중간에 추론 단계를 포함하여 여러 도구 호출을 연결합니다
- 중간 결과를 기반으로 보다 미묘한 결정을 내립니다

인터리브 thinking을 활성화하려면 API 요청에 [베타 헤더](https://platform.claude.com/docs/en/api/beta-headers) `interleaved-thinking-2025-05-14`를 추가하세요.

인터리브 thinking에 대한 몇 가지 중요한 고려 사항은 다음과 같습니다:
- 인터리브 thinking을 사용하면 `budget_tokens`가 하나의 어시스턴트 턴 내의 모든 thinking 블록에 걸쳐 총 예산을 나타내므로 `max_tokens` 매개변수를 초과할 수 있습니다.
- 인터리브 thinking은 [Messages API를 통해 사용되는 도구](../03-tools/01-overview.md)에 대해서만 지원됩니다.
- 인터리브 thinking은 베타 헤더 `interleaved-thinking-2025-05-14`와 함께 Claude 4 모델에 대해서만 지원됩니다.
- Claude API에 대한 직접 호출을 사용하면 효과 없이 모든 모델에 대한 요청에 `interleaved-thinking-2025-05-14`를 전달할 수 있습니다.
- 타사 플랫폼(예: [Amazon Bedrock](./07-claude-on-3rd-party-01-amazon-bedrock.md) 및 [Vertex AI](./07-claude-on-3rd-party-03-vertex-ai.md))에서 Claude Opus 4.5, Claude Opus 4.1, Opus 4 또는 Sonnet 4 이외의 모델에 `interleaved-thinking-2025-05-14`를 전달하면 요청이 실패합니다.

<details>
<summary>인터리브 thinking 없는 도구 사용</summary>

인터리브 thinking 없이 Claude는 어시스턴트 턴이 시작될 때 한 번 생각합니다. 도구 결과 이후의 후속 응답은 새 thinking 블록 없이 계속됩니다.

```
사용자: "단위당 $50에 150개 단위를 판매한 경우 총 수익은 얼마이며,
       이것이 평균 월 수익과 어떻게 비교됩니까?"

턴 1: [thinking] "150 * $50을 계산한 다음 데이터베이스를 확인해야 합니다..."
        [tool_use: calculator] { "expression": "150 * 50" }
  ↓ 도구 결과: "7500"

턴 2: [tool_use: database_query] { "query": "SELECT AVG(revenue)..." }
        ↑ thinking 블록 없음
  ↓ 도구 결과: "5200"

턴 3: [text] "총 수익은 $7,500이며, 이는 평균 월 수익인
        $5,200보다 44% 높습니다."
        ↑ thinking 블록 없음
```
</details>

<details>
<summary>인터리브 thinking과 함께 도구 사용</summary>

인터리브 thinking이 활성화되면 Claude는 각 도구 결과를 받은 후 생각할 수 있어 계속하기 전에 중간 결과에 대해 추론할 수 있습니다.

```
사용자: "단위당 $50에 150개 단위를 판매한 경우 총 수익은 얼마이며,
       이것이 평균 월 수익과 어떻게 비교됩니까?"

턴 1: [thinking] "먼저 150 * $50을 계산해야 합니다..."
        [tool_use: calculator] { "expression": "150 * 50" }
  ↓ 도구 결과: "7500"

턴 2: [thinking] "$7,500을 받았습니다. 이제 비교를 위해 데이터베이스를 쿼리해야 합니다..."
        [tool_use: database_query] { "query": "SELECT AVG(revenue)..." }
        ↑ calculator 결과를 받은 후 thinking
  ↓ 도구 결과: "5200"

턴 3: [thinking] "$7,500 대 $5,200 평균 - 44% 증가..."
        [text] "총 수익은 $7,500이며, 이는 평균 월 수익인
        $5,200보다 44% 높습니다."
        ↑ 최종 답변 전 thinking
```
</details>

## 프롬프트 캐싱과 함께 Extended thinking

thinking과 함께 [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md)에는 몇 가지 중요한 고려 사항이 있습니다:


> Extended thinking 작업은 종종 5분 이상 걸립니다. 더 긴 thinking 세션과 다단계 워크플로에서 캐시 히트를 유지하려면 [1시간 캐시 기간](../02-capabilities/01-prompt-caching.md) 사용을 고려하세요.


**Thinking 블록 컨텍스트 제거**
- 이전 턴의 thinking 블록이 컨텍스트에서 제거되어 캐시 중단점에 영향을 줄 수 있습니다
- 도구 사용과 함께 대화를 계속할 때 thinking 블록이 캐시되고 캐시에서 읽을 때 입력 토큰으로 계산됩니다
- 이는 트레이드오프를 생성합니다: thinking 블록이 시각적으로 컨텍스트 윈도우 공간을 소비하지 않지만 캐시될 때 여전히 입력 토큰 사용량에 계산됩니다
- thinking이 비활성화되고 현재 도구 사용 턴에서 thinking 콘텐츠를 전달하면 thinking 콘텐츠가 제거되고 해당 요청에 대해 thinking이 비활성화된 상태로 유지됩니다

**캐시 무효화 패턴**
- thinking 매개변수(활성화/비활성화 또는 예산 할당)에 대한 변경 사항은 메시지 캐시 중단점을 무효화합니다
- [인터리브 thinking](#interleaved-thinking)은 여러 [도구 호출](#extended-thinking-with-tool-use) 사이에 thinking 블록이 발생할 수 있으므로 캐시 무효화를 증폭시킵니다
- 시스템 프롬프트와 도구는 thinking 매개변수 변경이나 블록 제거에도 불구하고 캐시된 상태로 유지됩니다


> thinking 블록은 캐싱 및 컨텍스트 계산을 위해 제거되지만 특히 [인터리브 thinking](#interleaved-thinking)과 함께 [도구 사용](#extended-thinking-with-tool-use)으로 대화를 계속할 때 보존해야 합니다.


### Thinking 블록 캐싱 동작 이해

도구 사용과 함께 extended thinking을 사용할 때 thinking 블록은 토큰 계산에 영향을 주는 특정 캐싱 동작을 나타냅니다:

**작동 방식:**

1. 캐싱은 도구 결과를 포함하는 후속 요청을 할 때만 발생합니다
2. 후속 요청이 이루어지면 이전 대화 기록(thinking 블록 포함)이 캐시될 수 있습니다
3. 이러한 캐시된 thinking 블록은 캐시에서 읽을 때 사용량 메트릭에서 입력 토큰으로 계산됩니다
4. 도구 결과가 아닌 사용자 블록이 포함되면 이전의 모든 thinking 블록이 무시되고 컨텍스트에서 제거됩니다

**상세 예제 흐름:**

**요청 1:**
```
사용자: "파리의 날씨는?"
```
**응답 1:**
```
[thinking_block_1] + [tool_use block 1]
```

**요청 2:**
```
사용자: ["파리의 날씨는?"],
어시스턴트: [thinking_block_1] + [tool_use block 1],
사용자: [tool_result_1, cache=True]
```
**응답 2:**
```
[thinking_block_2] + [text block 2]
```
요청 2는 요청 콘텐츠의 캐시를 작성합니다(응답이 아님). 캐시에는 원래 사용자 메시지, 첫 번째 thinking 블록, tool use 블록 및 도구 결과가 포함됩니다.

**요청 3:**
```
사용자: ["파리의 날씨는?"],
어시스턴트: [thinking_block_1] + [tool_use block 1],
사용자: [tool_result_1, cache=True],
어시스턴트: [thinking_block_2] + [text block 2],
사용자: [Text response, cache=True]
```
Claude Opus 4.5 이상의 경우 이전의 모든 thinking 블록이 기본적으로 유지됩니다. 이전 모델의 경우 도구 결과가 아닌 사용자 블록이 포함되었기 때문에 이전의 모든 thinking 블록이 무시됩니다. 이 요청은 다음과 동일하게 처리됩니다:
```
사용자: ["파리의 날씨는?"],
어시스턴트: [tool_use block 1],
사용자: [tool_result_1, cache=True],
어시스턴트: [text block 2],
사용자: [Text response, cache=True]
```

**주요 사항:**
- 이 캐싱 동작은 명시적 `cache_control` 마커 없이도 자동으로 발생합니다
- 이 동작은 일반 thinking 또는 인터리브 thinking을 사용하든 일관됩니다

## Extended thinking과 함께 Max tokens 및 컨텍스트 윈도우 크기

이전 Claude 모델(Claude Sonnet 3.7 이전)에서는 프롬프트 토큰과 `max_tokens`의 합이 모델의 컨텍스트 윈도우를 초과하면 시스템이 컨텍스트 제한 내에 맞도록 `max_tokens`를 자동으로 조정했습니다. 즉, 큰 `max_tokens` 값을 설정할 수 있었고 시스템이 필요에 따라 자동으로 줄여주었습니다.

Claude 3.7 및 4 모델에서는 `max_tokens`(thinking이 활성화된 경우 thinking 예산 포함)가 엄격한 제한으로 적용됩니다. 이제 프롬프트 토큰 + `max_tokens`가 컨텍스트 윈도우 크기를 초과하면 시스템이 검증 오류를 반환합니다.


> 더 철저한 심층 분석을 위해 [컨텍스트 윈도우 가이드](./01-build-with-claude-02-context-windows.md)를 읽어보세요.


### Extended thinking과 함께 컨텍스트 윈도우

thinking이 활성화된 상태에서 컨텍스트 윈도우 사용량을 계산할 때 알아야 할 몇 가지 고려 사항이 있습니다:

- 이전 턴의 thinking 블록이 제거되고 컨텍스트 윈도우에 계산되지 않습니다
- 현재 턴 thinking은 해당 턴의 `max_tokens` 제한에 계산됩니다

아래 다이어그램은 extended thinking이 활성화될 때 특수화된 토큰 관리를 보여줍니다:

![Extended thinking이 있는 컨텍스트 윈도우 다이어그램](/docs/images/context-window-thinking.svg)

효과적인 컨텍스트 윈도우는 다음과 같이 계산됩니다:

```
context window =
  (현재 입력 토큰 - 이전 thinking 토큰) +
  (thinking 토큰 + 암호화된 thinking 토큰 + text 출력 토큰)
```

특히 thinking 블록을 포함하는 다회전 대화를 작업할 때 특정 사용 사례에 대한 정확한 토큰 수를 얻으려면 [토큰 계산 API](../02-capabilities/09-token-counting.md)를 사용하는 것이 좋습니다.

### 도구 사용과 함께 Extended thinking의 컨텍스트 윈도우

도구 사용과 함께 extended thinking을 사용할 때 thinking 블록은 명시적으로 보존되고 도구 결과와 함께 반환되어야 합니다.

도구 사용과 함께 extended thinking에 대한 효과적인 컨텍스트 윈도우 계산은 다음과 같습니다:

```
context window =
  (현재 입력 토큰 + 이전 thinking 토큰 + tool use 토큰) +
  (thinking 토큰 + 암호화된 thinking 토큰 + text 출력 토큰)
```

아래 다이어그램은 도구 사용과 함께 extended thinking에 대한 토큰 관리를 보여줍니다:

![도구 사용과 함께 extended thinking이 있는 컨텍스트 윈도우 다이어그램](/docs/images/context-window-thinking-tools.svg)

### Extended thinking과 함께 토큰 관리

extended thinking Claude 3.7 및 4 모델의 컨텍스트 윈도우 및 `max_tokens` 동작을 고려할 때 다음을 수행해야 할 수 있습니다:

- 토큰 사용량을 보다 적극적으로 모니터링하고 관리합니다
- 프롬프트 길이가 변경됨에 따라 `max_tokens` 값을 조정합니다
- [토큰 계산 엔드포인트](../02-capabilities/09-token-counting.md)를 더 자주 사용합니다
- 이전 thinking 블록이 컨텍스트 윈도우에 누적되지 않는다는 것을 인식합니다

이 변경 사항은 특히 최대 토큰 제한이 크게 증가함에 따라 보다 예측 가능하고 투명한 동작을 제공하기 위해 이루어졌습니다.

## Thinking 암호화

전체 thinking 콘텐츠가 암호화되어 `signature` 필드에 반환됩니다. 이 필드는 API에 다시 전달될 때 Claude가 thinking 블록을 생성했음을 확인하는 데 사용됩니다.


> [도구와 함께 extended thinking](#extended-thinking-with-tool-use)을 사용할 때만 thinking 블록을 다시 보내는 것이 엄격하게 필요합니다. 그렇지 않으면 이전 턴에서 thinking 블록을 생략하거나 다시 전달하면 API가 자동으로 제거하도록 할 수 있습니다.
>
> thinking 블록을 다시 보내는 경우 일관성을 위해 받은 모든 것을 다시 전달하고 잠재적인 문제를 피하는 것이 좋습니다.


thinking 암호화에 대한 몇 가지 중요한 고려 사항은 다음과 같습니다:
- [응답을 스트리밍](#streaming-thinking)할 때 서명은 `content_block_stop` 이벤트 직전에 `content_block_delta` 이벤트 내부의 `signature_delta`를 통해 추가됩니다.
- `signature` 값은 Claude 4 모델에서 이전 모델보다 훨씬 깁니다.
- `signature` 필드는 불투명 필드이며 해석하거나 구문 분석해서는 안 됩니다. 검증 목적으로만 존재합니다.
- `signature` 값은 플랫폼 간에 호환됩니다(Claude API, [Amazon Bedrock](./07-claude-on-3rd-party-01-amazon-bedrock.md) 및 [Vertex AI](./07-claude-on-3rd-party-03-vertex-ai.md)). 한 플랫폼에서 생성된 값은 다른 플랫폼과 호환됩니다.

### Thinking 리댁션

때때로 Claude의 내부 추론이 안전 시스템에 의해 플래그가 지정됩니다. 이런 일이 발생하면 `thinking` 블록의 일부 또는 전체를 암호화하여 `redacted_thinking` 블록으로 반환합니다. `redacted_thinking` 블록은 API에 다시 전달될 때 복호화되어 Claude가 컨텍스트를 잃지 않고 응답을 계속할 수 있습니다.

extended thinking을 사용하는 고객 대면 애플리케이션을 구축할 때:

- 리댁트된 thinking 블록에는 사람이 읽을 수 없는 암호화된 콘텐츠가 포함되어 있다는 점에 유의하세요
- "Claude의 일부 내부 추론이 안전상의 이유로 자동으로 암호화되었습니다. 이것이 응답의 품질에 영향을 주지 않습니다."와 같은 간단한 설명 제공을 고려하세요.
- thinking 블록을 사용자에게 표시하는 경우 일반 thinking 블록을 보존하면서 리댁트된 블록을 필터링할 수 있습니다
- extended thinking 기능을 사용하면 때때로 일부 추론이 암호화될 수 있음을 투명하게 알립니다
- UI를 깨뜨리지 않고 리댁트된 thinking을 우아하게 관리하기 위한 적절한 오류 처리를 구현합니다

다음은 일반 및 리댁트된 thinking 블록을 모두 보여주는 예제입니다:

```json
{
  "content": [
    {
      "type": "thinking",
      "thinking": "단계별로 분석해 보겠습니다...",
      "signature": "WaUjzkypQ2mUEVM36O2TxuC06KN8xyfbJwyem2dw3URve/op91XWHOEBLLqIOMfFG/UvLEczmEsUjavL...."
    },
    {
      "type": "redacted_thinking",
      "data": "EmwKAhgBEgy3va3pzix/LafPsn4aDFIT2Xlxh0L5L8rLVyIwxtE3rAFBa8cr3qpPkNRj2YfWXGmKDxH4mPnZ5sQ7vB9URj2pLmN3kF8/dW5hR7xJ0aP1oLs9yTcMnKVf2wRpEGjH9XZaBt4UvDcPrQ..."
    },
    {
      "type": "text",
      "text": "분석 결과를 바탕으로..."
    }
  ]
}
```


> 출력에서 리댁트된 thinking 블록을 보는 것은 예상되는 동작입니다. 모델은 안전 가드레일을 유지하면서 이 리댁트된 추론을 사용하여 응답을 알릴 수 있습니다.
>
> 애플리케이션에서 리댁트된 thinking 처리를 테스트해야 하는 경우 이 특수 테스트 문자열을 프롬프트로 사용할 수 있습니다: `ANTHROPIC_MAGIC_STRING_TRIGGER_REDACTED_THINKING_46C9A13E193C177646C7398A98432ECCCE4C1253D5E2D82641AC0E52CC2876CB`


다회전 대화에서 `thinking` 및 `redacted_thinking` 블록을 API에 다시 전달할 때 마지막 어시스턴트 턴에 대해 완전히 수정되지 않은 블록을 API에 다시 포함해야 합니다. 이는 모델의 추론 흐름을 유지하는 데 중요합니다. 항상 모든 thinking 블록을 API에 다시 전달하는 것이 좋습니다. 자세한 내용은 위의 [Thinking 블록 보존](#preserving-thinking-blocks) 섹션을 참조하세요.

## 모델 버전별 thinking 차이

Messages API는 Claude Sonnet 3.7과 Claude 4 모델에서 thinking을 다르게 처리하며, 주로 리댁션 및 요약 동작에서 차이가 있습니다.

압축된 비교는 아래 표를 참조하세요:

| 기능 | Claude Sonnet 3.7 | Claude 4 모델 (Opus 4.5 이전) | Claude Opus 4.5 이상 |
|---------|------------------|-------------------------------|--------------------------|
| **Thinking 출력** | 전체 thinking 출력 반환 | 요약된 thinking 반환 | 요약된 thinking 반환 |
| **인터리브 Thinking** | 지원되지 않음 | `interleaved-thinking-2025-05-14` 베타 헤더와 함께 지원됨 | `interleaved-thinking-2025-05-14` 베타 헤더와 함께 지원됨 |
| **Thinking 블록 보존** | 턴 간에 보존되지 않음 | 턴 간에 보존되지 않음 | **기본적으로 보존됨** (캐시 최적화, 토큰 절약 가능) |

### Claude Opus 4.5의 Thinking 블록 보존

Claude Opus 4.5는 새로운 기본 동작을 도입합니다: **이전 어시스턴트 턴의 thinking 블록이 기본적으로 모델 컨텍스트에 보존됩니다**. 이는 이전 턴의 thinking 블록을 제거하는 이전 모델과 다릅니다.

**Thinking 블록 보존의 이점:**

- **캐시 최적화**: 도구 사용을 사용할 때 보존된 thinking 블록은 도구 결과와 함께 다시 전달되고 어시스턴트 턴에 걸쳐 점진적으로 캐시되므로 캐시 히트를 활성화하여 다단계 워크플로에서 토큰 절약을 가능하게 합니다
- **지능 영향 없음**: thinking 블록을 보존해도 모델 성능에 부정적인 영향이 없습니다

**중요한 고려 사항:**

- **컨텍스트 사용량**: thinking 블록이 컨텍스트에 보존되므로 긴 대화는 더 많은 컨텍스트 공간을 소비합니다
- **자동 동작**: 이것은 Claude Opus 4.5의 기본 동작입니다. 코드 변경이나 베타 헤더가 필요하지 않습니다
- **하위 호환성**: 이 기능을 활용하려면 도구 사용을 위해 수행하는 것처럼 완전하고 수정되지 않은 thinking 블록을 API에 다시 계속 전달하세요


> 이전 모델(Claude Sonnet 4.5, Opus 4.1 등)의 경우 이전 턴의 thinking 블록은 계속 컨텍스트에서 제거됩니다. [프롬프트 캐싱과 함께 Extended thinking](#extended-thinking-with-prompt-caching) 섹션에 설명된 기존 동작이 해당 모델에 적용됩니다.


## 가격

기본 요금, 캐시 쓰기, 캐시 히트 및 출력 토큰을 포함한 전체 가격 정보는 [가격 페이지](https://platform.claude.com/docs/en/about-claude/pricing)를 참조하세요.

thinking 프로세스는 다음에 대한 요금이 부과됩니다:
- thinking 중에 사용된 토큰 (출력 토큰)
- 후속 요청에 포함된 마지막 어시스턴트 턴의 thinking 블록 (입력 토큰)
- 표준 텍스트 출력 토큰


> extended thinking이 활성화되면 이 기능을 지원하기 위해 특수화된 시스템 프롬프트가 자동으로 포함됩니다.


요약된 thinking을 사용할 때:
- **입력 토큰**: 원래 요청의 토큰 (이전 턴의 thinking 토큰 제외)
- **출력 토큰 (청구됨)**: Claude가 내부적으로 생성한 원래 thinking 토큰
- **출력 토큰 (표시됨)**: 응답에서 보는 요약된 thinking 토큰
- **요금 없음**: 요약을 생성하는 데 사용된 토큰


> 청구된 출력 토큰 수는 응답에서 보이는 토큰 수와 **일치하지 않습니다**. 보이는 요약이 아니라 전체 thinking 프로세스에 대해 청구됩니다.


## Extended thinking에 대한 모범 사례 및 고려 사항

### Thinking 예산 작업

- **예산 최적화:** 최소 예산은 1,024토큰입니다. 최소에서 시작하여 사용 사례에 대한 최적 범위를 찾기 위해 thinking 예산을 점진적으로 증가시키는 것이 좋습니다. 토큰 수가 높을수록 작업에 따라 수익 감소가 있지만 보다 포괄적인 추론이 가능합니다. 예산을 늘리면 지연 시간이 증가하는 대신 응답 품질이 향상될 수 있습니다. 중요한 작업의 경우 다양한 설정을 테스트하여 최적의 균형을 찾으세요. thinking 예산은 엄격한 제한이 아닌 목표이며 실제 토큰 사용량은 작업에 따라 다를 수 있습니다.
- **시작점:** 복잡한 작업의 경우 더 큰 thinking 예산(16k+ 토큰)으로 시작하고 필요에 따라 조정하세요.
- **큰 예산:** 32k 이상의 thinking 예산의 경우 네트워킹 문제를 피하기 위해 [배치 처리](../02-capabilities/06-batch-processing.md) 사용을 권장합니다. 모델이 32k 토큰 이상 생각하도록 푸시하는 요청은 시스템 타임아웃 및 열린 연결 제한에 부딪힐 수 있는 장기 실행 요청을 발생시킵니다.
- **토큰 사용량 추적:** 비용 및 성능을 최적화하기 위해 thinking 토큰 사용량을 모니터링하세요.

### 성능 고려 사항

- **응답 시간:** 추론 프로세스에 필요한 추가 처리로 인해 응답 시간이 더 길어질 수 있습니다. thinking 블록을 생성하면 전체 응답 시간이 증가할 수 있습니다.
- **스트리밍 요구 사항:** `max_tokens`가 21,333보다 큰 경우 스트리밍이 필요합니다. 스트리밍할 때 thinking 및 text 콘텐츠 블록이 도착할 때 처리할 준비를 하세요.

### 기능 호환성

- Thinking은 `temperature` 또는 `top_k` 수정 및 [강제 도구 사용](../03-tools/02-how-to-implement-tool-use.md)과 호환되지 않습니다.
- thinking이 활성화되면 `top_p`를 1과 0.95 사이의 값으로 설정할 수 있습니다.
- thinking이 활성화되면 응답을 미리 채울 수 없습니다.
- thinking 예산에 대한 변경 사항은 메시지를 포함하는 캐시된 프롬프트 접두사를 무효화합니다. 그러나 캐시된 시스템 프롬프트 및 도구 정의는 thinking 매개변수가 변경될 때 계속 작동합니다.

### 사용 지침

- **작업 선택:** 수학, 코딩 및 분석과 같은 단계별 추론의 이점을 얻는 특히 복잡한 작업에 extended thinking을 사용하세요.
- **컨텍스트 처리:** 이전 thinking 블록을 직접 제거할 필요가 없습니다. Claude API는 자동으로 이전 턴의 thinking 블록을 무시하며 컨텍스트 사용량을 계산할 때 포함되지 않습니다.
- **프롬프트 엔지니어링:** Claude의 thinking 능력을 최대화하려면 [extended thinking 프롬프팅 팁](../08-prompt-engineering/13-extended-thinking-tips.md)을 검토하세요.

## 다음 단계


  
> 쿡북에서 thinking의 실용적인 예제를 탐색하세요.

  
> extended thinking을 위한 프롬프트 엔지니어링 모범 사례를 배우세요.


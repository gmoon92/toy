# [Claude의 도구 사용](https://platform.claude.com/docs/en/agents-and-tools/tool-use/overview)

---

Claude는 도구와 함수와 상호작용할 수 있어, Claude의 기능을 확장하여 더 다양한 작업을 수행할 수 있습니다.


> 새로운 [코스](https://anthropic.skilljar.com/)의 일부로 Claude의 도구 사용을 마스터하는 데 필요한 모든 것을 배워보세요! 이 [양식](https://forms.gle/BFnYc6iCkWoRzFgk7)을 통해 계속해서 아이디어와 제안을 공유해 주세요.



> **엄격한 도구 사용으로 스키마 일치 보장**
>
> [구조화된 출력](../02-capabilities/15-structured-outputs.md)은 도구 입력에 대한 보장된 스키마 검증을 제공합니다. 도구 정의에 `strict: true`를 추가하면 Claude의 도구 호출이 항상 스키마와 정확히 일치하도록 보장합니다—더 이상 타입 불일치나 누락된 필드가 없습니다.
>
> 유효하지 않은 도구 매개변수가 실패를 일으킬 수 있는 프로덕션 에이전트에 완벽합니다. [엄격한 도구 사용을 언제 사용해야 하는지 알아보기 →](../02-capabilities/15-structured-outputs.md)


다음은 Messages API를 사용하여 Claude에게 도구를 제공하는 방법의 예시입니다:

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
        "description": "Get the current weather in a given location",
        "input_schema": {
          "type": "object",
          "properties": {
            "location": {
              "type": "string",
              "description": "The city and state, e.g. San Francisco, CA"
            }
          },
          "required": ["location"]
        }
      }
    ],
    "messages": [
      {
        "role": "user",
        "content": "What is the weather like in San Francisco?"
      }
    ]
  }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[
        {
            "name": "get_weather",
            "description": "Get the current weather in a given location",
            "input_schema": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "The city and state, e.g. San Francisco, CA",
                    }
                },
                "required": ["location"],
            },
        }
    ],
    messages=[{"role": "user", "content": "What's the weather like in San Francisco?"}],
)
print(response)
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY
});

async function main() {
  const response = await anthropic.messages.create({
    model: "claude-sonnet-4-5",
    max_tokens: 1024,
    tools: [{
      name: "get_weather",
      description: "Get the current weather in a given location",
      input_schema: {
        type: "object",
        properties: {
          location: {
            type: "string",
            description: "The city and state, e.g. San Francisco, CA"
          }
        },
        required: ["location"]
      }
    }],
    messages: [{
      role: "user",
      content: "Tell me the weather in San Francisco."
    }]
  });

  console.log(response);
}

main().catch(console.error);
```

```java Java
import java.util.List;
import java.util.Map;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.Tool.InputSchema;

public class GetWeatherExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        InputSchema schema = InputSchema.builder()
                .properties(JsonValue.from(Map.of(
                        "location",
                        Map.of(
                                "type", "string",
                                "description", "The city and state, e.g. San Francisco, CA"))))
                .putAdditionalProperty("required", JsonValue.from(List.of("location")))
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_OPUS_4_0)
                .maxTokens(1024)
                .addTool(Tool.builder()
                        .name("get_weather")
                        .description("Get the current weather in a given location")
                        .inputSchema(schema)
                        .build())
                .addUserMessage("What's the weather like in San Francisco?")
                .build();

        Message message = client.messages().create(params);
        System.out.println(message);
    }
}
```

</CodeGroup>

---

## 도구 사용 작동 방식

Claude는 두 가지 유형의 도구를 지원합니다:

1. **클라이언트 도구**: 시스템에서 실행되는 도구로, 다음을 포함합니다:
   - 사용자가 생성하고 구현하는 사용자 정의 커스텀 도구
   - 클라이언트 구현이 필요한 [컴퓨터 사용](../03-tools/07-computer-use-tool.md) 및 [텍스트 에디터](../03-tools/08-text-editor-tool.md)와 같은 Anthropic 정의 도구

2. **서버 도구**: [웹 검색](../03-tools/10-web-search-tool.md) 및 [웹 페치](../03-tools/09-web-fetch-tool.md) 도구와 같이 Anthropic의 서버에서 실행되는 도구입니다. 이러한 도구는 API 요청에 명시되어야 하지만 사용자 측에서 구현할 필요는 없습니다.


> Anthropic 정의 도구는 버전이 지정된 타입(예: `web_search_20250305`, `text_editor_20250124`)을 사용하여 모델 버전 간 호환성을 보장합니다.


### 클라이언트 도구
다음 단계를 통해 클라이언트 도구를 Claude와 통합할 수 있습니다:

<Steps>
  <Step title="Claude에게 도구와 사용자 프롬프트 제공">
    - API 요청에 이름, 설명 및 입력 스키마와 함께 클라이언트 도구를 정의합니다.
    - 이러한 도구가 필요할 수 있는 사용자 프롬프트를 포함합니다. 예: "샌프란시스코의 날씨는 어때?"
  </Step>
  <Step title="Claude가 도구 사용 결정">
    - Claude는 도구가 사용자의 쿼리에 도움이 될 수 있는지 평가합니다.
    - 도움이 된다면, Claude는 올바르게 포맷된 도구 사용 요청을 구성합니다.
    - 클라이언트 도구의 경우, API 응답의 `stop_reason`이 `tool_use`로 설정되어 Claude의 의도를 알립니다.
  </Step>
  <Step title="도구를 실행하고 결과 반환">
    - Claude의 요청에서 도구 이름과 입력을 추출합니다
    - 시스템에서 도구 코드를 실행합니다
    - `tool_result` 콘텐츠 블록을 포함하는 새 `user` 메시지로 결과를 반환합니다
  </Step>
  <Step title="Claude가 도구 결과를 사용하여 응답 작성">
    - Claude는 도구 결과를 분석하여 원래 사용자 프롬프트에 대한 최종 응답을 작성합니다.
  </Step>
</Steps>
참고: 3단계와 4단계는 선택 사항입니다. 일부 워크플로우의 경우 Claude의 도구 사용 요청(2단계)만으로 충분할 수 있으며, 결과를 Claude에게 다시 보낼 필요가 없습니다.

### 서버 도구

서버 도구는 다른 워크플로우를 따릅니다:

<Steps>
  <Step title="Claude에게 도구와 사용자 프롬프트 제공">
    - [웹 검색](../03-tools/10-web-search-tool.md) 및 [웹 페치](../03-tools/09-web-fetch-tool.md)와 같은 서버 도구는 자체 매개변수를 가집니다.
    - 이러한 도구가 필요할 수 있는 사용자 프롬프트를 포함합니다. 예: "AI에 대한 최신 뉴스 검색" 또는 "이 URL의 콘텐츠 분석."
  </Step>
  <Step title="Claude가 서버 도구 실행">
    - Claude는 서버 도구가 사용자의 쿼리에 도움이 될 수 있는지 평가합니다.
    - 도움이 된다면, Claude는 도구를 실행하고 결과가 자동으로 Claude의 응답에 통합됩니다.
  </Step>
  <Step title="Claude가 서버 도구 결과를 사용하여 응답 작성">
    - Claude는 서버 도구 결과를 분석하여 원래 사용자 프롬프트에 대한 최종 응답을 작성합니다.
    - 서버 도구 실행에는 추가 사용자 상호작용이 필요하지 않습니다.
  </Step>
</Steps>

---

## Claude와 MCP 도구 사용하기

[Model Context Protocol (MCP)](https://modelcontextprotocol.io)을 사용하는 애플리케이션을 구축하는 경우, MCP 서버의 도구를 Claude의 Messages API와 직접 사용할 수 있습니다. MCP 도구 정의는 Claude의 도구 형식과 유사한 스키마 형식을 사용합니다. `inputSchema`를 `input_schema`로 이름만 변경하면 됩니다.


> **자체 MCP 클라이언트를 구축하고 싶지 않으신가요?** [MCP 커넥터](./06-mcp-in-api-01-mcp-connector.md)를 사용하여 클라이언트를 구현하지 않고도 Messages API에서 원격 MCP 서버에 직접 연결할 수 있습니다.


### MCP 도구를 Claude 형식으로 변환

MCP 클라이언트를 구축하고 MCP 서버에서 `list_tools()`를 호출하면 `inputSchema` 필드가 있는 도구 정의를 받게 됩니다. 이러한 도구를 Claude와 함께 사용하려면 Claude의 형식으로 변환하세요:

<CodeGroup>
```python Python
from mcp import ClientSession

async def get_claude_tools(mcp_session: ClientSession):
    """Convert MCP tools to Claude's tool format."""
    mcp_tools = await mcp_session.list_tools()

    claude_tools = []
    for tool in mcp_tools.tools:
        claude_tools.append({
            "name": tool.name,
            "description": tool.description or "",
            "input_schema": tool.inputSchema  # Rename inputSchema to input_schema
        })

    return claude_tools
```

```typescript TypeScript
import { Client } from "@modelcontextprotocol/sdk/client/index.js";

async function getClaudeTools(mcpClient: Client) {
  // Convert MCP tools to Claude's tool format
  const mcpTools = await mcpClient.listTools();

  return mcpTools.tools.map((tool) => ({
    name: tool.name,
    description: tool.description ?? "",
    input_schema: tool.inputSchema, // Rename inputSchema to input_schema
  }));
}
```
</CodeGroup>

그런 다음 변환된 도구를 Claude에게 전달합니다:

<CodeGroup>
```python Python
import anthropic

client = anthropic.Anthropic()
claude_tools = await get_claude_tools(mcp_session)

response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=claude_tools,
    messages=[{"role": "user", "content": "What tools do you have available?"}]
)
```

```typescript TypeScript
import Anthropic from "@anthropic-ai/sdk";

const anthropic = new Anthropic();
const claudeTools = await getClaudeTools(mcpClient);

const response = await anthropic.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: claudeTools,
  messages: [{ role: "user", content: "What tools do you have available?" }],
});
```
</CodeGroup>

Claude가 `tool_use` 블록으로 응답하면, MCP 서버에서 `call_tool()`을 사용하여 도구를 실행하고 `tool_result` 블록으로 결과를 Claude에게 반환합니다.

MCP 클라이언트 구축에 대한 완전한 가이드는 [MCP 클라이언트 구축](https://modelcontextprotocol.io/docs/develop/build-client)을 참조하세요.

---

## 도구 사용 예제

다양한 도구 사용 패턴과 기술을 보여주는 몇 가지 코드 예제입니다. 간결성을 위해 도구는 간단한 도구이며, 도구 설명은 최상의 성능을 보장하기 위한 이상적인 길이보다 짧습니다.

<details>
<summary>단일 도구 예제</summary>

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
        "tools": [{
            "name": "get_weather",
            "description": "Get the current weather in a given location",
            "input_schema": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "The city and state, e.g. San Francisco, CA"
                    },
                    "unit": {
                        "type": "string",
                        "enum": ["celsius", "fahrenheit"],
                        "description": "The unit of temperature, either \"celsius\" or \"fahrenheit\""
                    }
                },
                "required": ["location"]
            }
        }],
        "messages": [{"role": "user", "content": "What is the weather like in San Francisco?"}]
    }'
    ```

    ```python Python
    import anthropic
    client = anthropic.Anthropic()

    response = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        tools=[
            {
                "name": "get_weather",
                "description": "Get the current weather in a given location",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "location": {
                            "type": "string",
                            "description": "The city and state, e.g. San Francisco, CA"
                        },
                        "unit": {
                            "type": "string",
                            "enum": ["celsius", "fahrenheit"],
                            "description": "The unit of temperature, either \"celsius\" or \"fahrenheit\""
                        }
                    },
                    "required": ["location"]
                }
            }
        ],
        messages=[{"role": "user", "content": "What is the weather like in San Francisco?"}]
    )

    print(response)
    ```

    ```java Java
    import java.util.List;
    import java.util.Map;

    import com.anthropic.client.AnthropicClient;
    import com.anthropic.client.okhttp.AnthropicOkHttpClient;
    import com.anthropic.core.JsonValue;
    import com.anthropic.models.messages.Message;
    import com.anthropic.models.messages.MessageCreateParams;
    import com.anthropic.models.messages.Model;
    import com.anthropic.models.messages.Tool;
    import com.anthropic.models.messages.Tool.InputSchema;

    public class WeatherToolExample {

        public static void main(String[] args) {
            AnthropicClient client = AnthropicOkHttpClient.fromEnv();

            InputSchema schema = InputSchema.builder()
                    .properties(JsonValue.from(Map.of(
                            "location", Map.of(
                                    "type", "string",
                                    "description", "The city and state, e.g. San Francisco, CA"
                            ),
                            "unit", Map.of(
                                    "type", "string",
                                    "enum", List.of("celsius", "fahrenheit"),
                                    "description", "The unit of temperature, either \"celsius\" or \"fahrenheit\""
                            )
                    )))
                    .putAdditionalProperty("required", JsonValue.from(List.of("location")))
                    .build();

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_OPUS_4_0)
                    .maxTokens(1024)
                    .addTool(Tool.builder()
                            .name("get_weather")
                            .description("Get the current weather in a given location")
                            .inputSchema(schema)
                            .build())
                    .addUserMessage("What is the weather like in San Francisco?")
                    .build();

            Message message = client.messages().create(params);
            System.out.println(message);
        }
    }
    ```

</CodeGroup>

Claude는 다음과 유사한 응답을 반환합니다:

```json JSON
{
  "id": "msg_01Aq9w938a90dw8q",
  "model": "claude-sonnet-4-5",
  "stop_reason": "tool_use",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "I'll check the current weather in San Francisco for you."
    },
    {
      "type": "tool_use",
      "id": "toolu_01A09q90qw90lq917835lq9",
      "name": "get_weather",
      "input": {"location": "San Francisco, CA", "unit": "celsius"}
    }
  ]
}
```

그런 다음 제공된 입력으로 `get_weather` 함수를 실행하고 새 `user` 메시지로 결과를 반환해야 합니다:

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
        "tools": [
            {
                "name": "get_weather",
                "description": "Get the current weather in a given location",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "location": {
                            "type": "string",
                            "description": "The city and state, e.g. San Francisco, CA"
                        },
                        "unit": {
                            "type": "string",
                            "enum": ["celsius", "fahrenheit"],
                            "description": "The unit of temperature, either \"celsius\" or \"fahrenheit\""
                        }
                    },
                    "required": ["location"]
                }
            }
        ],
        "messages": [
            {
                "role": "user",
                "content": "What is the weather like in San Francisco?"
            },
            {
                "role": "assistant",
                "content": [
                    {
                        "type": "text",
                        "text": "I'll check the current weather in San Francisco for you."
                    },
                    {
                        "type": "tool_use",
                        "id": "toolu_01A09q90qw90lq917835lq9",
                        "name": "get_weather",
                        "input": {
                            "location": "San Francisco, CA",
                            "unit": "celsius"
                        }
                    }
                ]
            },
            {
                "role": "user",
                "content": [
                    {
                        "type": "tool_result",
                        "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
                        "content": "15 degrees"
                    }
                ]
            }
        ]
    }'
    ```

    ```python Python
    response = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        tools=[
            {
                "name": "get_weather",
                "description": "Get the current weather in a given location",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "location": {
                            "type": "string",
                            "description": "The city and state, e.g. San Francisco, CA"
                        },
                        "unit": {
                            "type": "string",
                            "enum": ["celsius", "fahrenheit"],
                            "description": "The unit of temperature, either 'celsius' or 'fahrenheit'"
                        }
                    },
                    "required": ["location"]
                }
            }
        ],
        messages=[
            {
                "role": "user",
                "content": "What's the weather like in San Francisco?"
            },
            {
                "role": "assistant",
                "content": [
                    {
                        "type": "text",
                        "text": "I'll check the current weather in San Francisco for you."
                    },
                    {
                        "type": "tool_use",
                        "id": "toolu_01A09q90qw90lq917835lq9",
                        "name": "get_weather",
                        "input": {"location": "San Francisco, CA", "unit": "celsius"}
                    }
                ]
            },
            {
                "role": "user",
                "content": [
                    {
                        "type": "tool_result",
                        "tool_use_id": "toolu_01A09q90qw90lq917835lq9", # from the API response
                        "content": "65 degrees" # from running your tool
                    }
                ]
            }
        ]
    )

    print(response)
    ```

   ```java Java
    import java.util.List;
    import java.util.Map;

    import com.anthropic.client.AnthropicClient;
    import com.anthropic.client.okhttp.AnthropicOkHttpClient;
    import com.anthropic.core.JsonValue;
    import com.anthropic.models.messages.*;
    import com.anthropic.models.messages.Tool.InputSchema;

    public class ToolConversationExample {

        public static void main(String[] args) {
            AnthropicClient client = AnthropicOkHttpClient.fromEnv();

            InputSchema schema = InputSchema.builder()
                    .properties(JsonValue.from(Map.of(
                            "location", Map.of(
                                    "type", "string",
                                    "description", "The city and state, e.g. San Francisco, CA"
                            ),
                            "unit", Map.of(
                                    "type", "string",
                                    "enum", List.of("celsius", "fahrenheit"),
                                    "description", "The unit of temperature, either \"celsius\" or \"fahrenheit\""
                            )
                    )))
                    .putAdditionalProperty("required", JsonValue.from(List.of("location")))
                    .build();

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_OPUS_4_0)
                    .maxTokens(1024)
                    .addTool(Tool.builder()
                            .name("get_weather")
                            .description("Get the current weather in a given location")
                            .inputSchema(schema)
                            .build())
                    .addUserMessage("What is the weather like in San Francisco?")
                    .addAssistantMessageOfBlockParams(
                            List.of(
                                    ContentBlockParam.ofText(
                                            TextBlockParam.builder()
                                                    .text("I'll check the current weather in San Francisco for you.")
                                                    .build()
                                    ),
                                    ContentBlockParam.ofToolUse(
                                            ToolUseBlockParam.builder()
                                                    .id("toolu_01A09q90qw90lq917835lq9")
                                                    .name("get_weather")
                                                    .input(JsonValue.from(Map.of(
                                                            "location", "San Francisco, CA",
                                                            "unit", "celsius"
                                                    )))
                                                    .build()
                                    )
                            )
                    )
                    .addUserMessageOfBlockParams(List.of(
                            ContentBlockParam.ofToolResult(
                                    ToolResultBlockParam.builder()
                                            .toolUseId("toolu_01A09q90qw90lq917835lq9")
                                            .content("15 degrees")
                                            .build()
                            )
                    ))
                    .build();

            Message message = client.messages().create(params);
            System.out.println(message);
        }
    }
   ```

</CodeGroup>
이는 날씨 데이터를 통합한 Claude의 최종 응답을 출력합니다:

```json JSON
{
  "id": "msg_01Aq9w938a90dw8q",
  "model": "claude-sonnet-4-5",
  "stop_reason": "stop_sequence",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "The current weather in San Francisco is 15 degrees Celsius (59 degrees Fahrenheit). It's a cool day in the city by the bay!"
    }
  ]
}
```
</details>
<details>
<summary>병렬 도구 사용</summary>

Claude는 단일 응답 내에서 여러 도구를 병렬로 호출할 수 있으며, 이는 여러 독립적인 작업이 필요한 작업에 유용합니다. 병렬 도구를 사용할 때 모든 `tool_use` 블록은 단일 assistant 메시지에 포함되며, 모든 해당 `tool_result` 블록은 후속 user 메시지에 제공되어야 합니다.


> **중요**: 도구 결과는 API 오류를 방지하고 Claude가 병렬 도구를 계속 사용하도록 올바르게 포맷되어야 합니다. 자세한 포맷 요구 사항과 완전한 코드 예제는 [구현 가이드](../03-tools/02-how-to-implement-tool-use.md)를 참조하세요.


병렬 도구 호출 구현을 위한 포괄적인 예제, 테스트 스크립트 및 모범 사례는 구현 가이드의 [병렬 도구 사용 섹션](../03-tools/02-how-to-implement-tool-use.md)을 참조하세요.
</details>
<details>
<summary>다중 도구 예제</summary>

단일 요청에서 Claude에게 선택할 수 있는 여러 도구를 제공할 수 있습니다. 다음은 `get_weather` 및 `get_time` 도구를 모두 포함하고 두 가지를 모두 요청하는 사용자 쿼리의 예입니다.

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
        "tools": [{
            "name": "get_weather",
            "description": "Get the current weather in a given location",
            "input_schema": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "The city and state, e.g. San Francisco, CA"
                    },
                    "unit": {
                        "type": "string",
                        "enum": ["celsius", "fahrenheit"],
                        "description": "The unit of temperature, either 'celsius' or 'fahrenheit'"
                    }
                },
                "required": ["location"]
            }
        },
        {
            "name": "get_time",
            "description": "Get the current time in a given time zone",
            "input_schema": {
                "type": "object",
                "properties": {
                    "timezone": {
                        "type": "string",
                        "description": "The IANA time zone name, e.g. America/Los_Angeles"
                    }
                },
                "required": ["timezone"]
            }
        }],
        "messages": [{
            "role": "user",
            "content": "What is the weather like right now in New York? Also what time is it there?"
        }]
    }'
    ```

    ```python Python
    import anthropic
    client = anthropic.Anthropic()

    response = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        tools=[
            {
                "name": "get_weather",
                "description": "Get the current weather in a given location",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "location": {
                            "type": "string",
                            "description": "The city and state, e.g. San Francisco, CA"
                        },
                        "unit": {
                            "type": "string",
                            "enum": ["celsius", "fahrenheit"],
                            "description": "The unit of temperature, either 'celsius' or 'fahrenheit'"
                        }
                    },
                    "required": ["location"]
                }
            },
            {
                "name": "get_time",
                "description": "Get the current time in a given time zone",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "timezone": {
                            "type": "string",
                            "description": "The IANA time zone name, e.g. America/Los_Angeles"
                        }
                    },
                    "required": ["timezone"]
                }
            }
        ],
        messages=[
            {
                "role": "user",
                "content": "What is the weather like right now in New York? Also what time is it there?"
            }
        ]
    )
    print(response)
    ```

    ```java Java
    import java.util.List;
    import java.util.Map;

    import com.anthropic.client.AnthropicClient;
    import com.anthropic.client.okhttp.AnthropicOkHttpClient;
    import com.anthropic.core.JsonValue;
    import com.anthropic.models.messages.Message;
    import com.anthropic.models.messages.MessageCreateParams;
    import com.anthropic.models.messages.Model;
    import com.anthropic.models.messages.Tool;
    import com.anthropic.models.messages.Tool.InputSchema;

    public class MultipleToolsExample {

        public static void main(String[] args) {
            AnthropicClient client = AnthropicOkHttpClient.fromEnv();

            // Weather tool schema
            InputSchema weatherSchema = InputSchema.builder()
                    .properties(JsonValue.from(Map.of(
                            "location", Map.of(
                                    "type", "string",
                                    "description", "The city and state, e.g. San Francisco, CA"
                            ),
                            "unit", Map.of(
                                    "type", "string",
                                    "enum", List.of("celsius", "fahrenheit"),
                                    "description", "The unit of temperature, either \"celsius\" or \"fahrenheit\""
                            )
                    )))
                    .putAdditionalProperty("required", JsonValue.from(List.of("location")))
                    .build();

            // Time tool schema
            InputSchema timeSchema = InputSchema.builder()
                    .properties(JsonValue.from(Map.of(
                            "timezone", Map.of(
                                    "type", "string",
                                    "description", "The IANA time zone name, e.g. America/Los_Angeles"
                            )
                    )))
                    .putAdditionalProperty("required", JsonValue.from(List.of("timezone")))
                    .build();

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_OPUS_4_0)
                    .maxTokens(1024)
                    .addTool(Tool.builder()
                            .name("get_weather")
                            .description("Get the current weather in a given location")
                            .inputSchema(weatherSchema)
                            .build())
                    .addTool(Tool.builder()
                            .name("get_time")
                            .description("Get the current time in a given time zone")
                            .inputSchema(timeSchema)
                            .build())
                    .addUserMessage("What is the weather like right now in New York? Also what time is it there?")
                    .build();

            Message message = client.messages().create(params);
            System.out.println(message);
        }
    }
    ```

</CodeGroup>

이 경우 Claude는 다음 중 하나를 수행할 수 있습니다:
- 순차적으로 도구 사용 (한 번에 하나씩) — 먼저 `get_weather`를 호출한 다음 날씨 결과를 받은 후 `get_time`을 호출
- 병렬 도구 호출 사용 — 작업이 독립적일 때 단일 응답에서 여러 `tool_use` 블록을 출력

Claude가 병렬 도구 호출을 하는 경우, 각 결과가 자체 `tool_result` 블록에 있는 단일 `user` 메시지로 모든 도구 결과를 반환해야 합니다.
</details>
<details>
<summary>누락된 정보</summary>

사용자의 프롬프트에 도구의 모든 필수 매개변수를 채우기에 충분한 정보가 포함되어 있지 않은 경우, Claude Opus는 매개변수가 누락되었음을 인식하고 요청할 가능성이 훨씬 높습니다. Claude Sonnet도 특히 도구 요청을 출력하기 전에 생각하도록 프롬프트될 때 요청할 수 있습니다. 그러나 합리적인 값을 추론하기 위해 최선을 다할 수도 있습니다.

예를 들어, 위의 `get_weather` 도구를 사용하여 위치를 지정하지 않고 Claude에게 "날씨가 어때?"라고 물으면 Claude, 특히 Claude Sonnet은 도구 입력을 추측할 수 있습니다:

```json JSON
{
  "type": "tool_use",
  "id": "toolu_01A09q90qw90lq917835lq9",
  "name": "get_weather",
  "input": {"location": "New York, NY", "unit": "fahrenheit"}
}
```

이러한 동작은 보장되지 않으며, 특히 더 모호한 프롬프트와 덜 지능적인 모델의 경우에는 더욱 그렇습니다. Claude Opus가 필수 매개변수를 채우기에 충분한 컨텍스트가 없는 경우, 도구 호출을 하는 대신 명확한 질문으로 응답할 가능성이 훨씬 높습니다.
</details>
<details>
<summary>순차 도구</summary>

일부 작업은 한 도구의 출력을 다른 도구의 입력으로 사용하여 여러 도구를 순차적으로 호출해야 할 수 있습니다. 이러한 경우 Claude는 한 번에 하나의 도구를 호출합니다. 도구를 한 번에 모두 호출하도록 프롬프트되면, Claude는 상위 도구의 도구 결과에 종속되는 경우 하위 도구의 매개변수를 추측할 가능성이 높습니다.

다음은 `get_location` 도구를 사용하여 사용자의 위치를 가져온 다음 해당 위치를 `get_weather` 도구에 전달하는 예입니다:

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
        "tools": [
            {
                "name": "get_location",
                "description": "Get the current user location based on their IP address. This tool has no parameters or arguments.",
                "input_schema": {
                    "type": "object",
                    "properties": {}
                }
            },
            {
                "name": "get_weather",
                "description": "Get the current weather in a given location",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "location": {
                            "type": "string",
                            "description": "The city and state, e.g. San Francisco, CA"
                        },
                        "unit": {
                            "type": "string",
                            "enum": ["celsius", "fahrenheit"],
                            "description": "The unit of temperature, either 'celsius' or 'fahrenheit'"
                        }
                    },
                    "required": ["location"]
                }
            }
        ],
        "messages": [{
            "role": "user",
            "content": "What is the weather like where I am?"
        }]
    }'
    ```

    ```python Python
    response = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        tools=[
            {
                "name": "get_location",
                "description": "Get the current user location based on their IP address. This tool has no parameters or arguments.",
                "input_schema": {
                    "type": "object",
                    "properties": {}
                }
            },
            {
                "name": "get_weather",
                "description": "Get the current weather in a given location",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "location": {
                            "type": "string",
                            "description": "The city and state, e.g. San Francisco, CA"
                        },
                        "unit": {
                            "type": "string",
                            "enum": ["celsius", "fahrenheit"],
                            "description": "The unit of temperature, either 'celsius' or 'fahrenheit'"
                        }
                    },
                    "required": ["location"]
                }
            }
        ],
        messages=[{
       		  "role": "user",
        	  "content": "What's the weather like where I am?"
        }]
    )
    ```

    ```java Java
    import java.util.List;
    import java.util.Map;

    import com.anthropic.client.AnthropicClient;
    import com.anthropic.client.okhttp.AnthropicOkHttpClient;
    import com.anthropic.core.JsonValue;
    import com.anthropic.models.messages.Message;
    import com.anthropic.models.messages.MessageCreateParams;
    import com.anthropic.models.messages.Model;
    import com.anthropic.models.messages.Tool;
    import com.anthropic.models.messages.Tool.InputSchema;

    public class EmptySchemaToolExample {

        public static void main(String[] args) {
            AnthropicClient client = AnthropicOkHttpClient.fromEnv();

            // Empty schema for location tool
            InputSchema locationSchema = InputSchema.builder()
                    .properties(JsonValue.from(Map.of()))
                    .build();

            // Weather tool schema
            InputSchema weatherSchema = InputSchema.builder()
                    .properties(JsonValue.from(Map.of(
                            "location", Map.of(
                                    "type", "string",
                                    "description", "The city and state, e.g. San Francisco, CA"
                            ),
                            "unit", Map.of(
                                    "type", "string",
                                    "enum", List.of("celsius", "fahrenheit"),
                                    "description", "The unit of temperature, either \"celsius\" or \"fahrenheit\""
                            )
                    )))
                    .putAdditionalProperty("required", JsonValue.from(List.of("location")))
                    .build();

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_OPUS_4_0)
                    .maxTokens(1024)
                    .addTool(Tool.builder()
                            .name("get_location")
                            .description("Get the current user location based on their IP address. This tool has no parameters or arguments.")
                            .inputSchema(locationSchema)
                            .build())
                    .addTool(Tool.builder()
                            .name("get_weather")
                            .description("Get the current weather in a given location")
                            .inputSchema(weatherSchema)
                            .build())
                    .addUserMessage("What is the weather like where I am?")
                    .build();

            Message message = client.messages().create(params);
            System.out.println(message);
        }
    }
    ```

</CodeGroup>

이 경우 Claude는 먼저 `get_location` 도구를 호출하여 사용자의 위치를 가져옵니다. `tool_result`로 위치를 반환한 후, Claude는 해당 위치로 `get_weather`를 호출하여 최종 답변을 얻습니다.

전체 대화는 다음과 같이 보일 수 있습니다:

| 역할      | 내용                                                                                                                                                                                                                                 |
| --------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| User      | 나는 어디에 있고 날씨는 어때?                                                                                                                                                                                                                     |
| Assistant | 먼저 현재 위치를 찾은 다음 그곳의 날씨를 확인하겠습니다. \[get_location에 대한 도구 사용\] |
| User      | \[일치하는 id와 San Francisco, CA 결과가 있는 get_location에 대한 도구 결과\]                                                                                                                                                       |
| Assistant | \[다음 입력으로 get_weather에 대한 도구 사용\]\{ "location": "San Francisco, CA", "unit": "fahrenheit" }                                                                                                                         |
| User      | \[일치하는 id와 "59°F (15°C), mostly cloudy" 결과가 있는 get_weather에 대한 도구 결과\]                                                                                                                                             |
| Assistant | 샌프란시스코, CA의 현재 위치를 기반으로 현재 날씨는 59°F (15°C)이고 대부분 흐립니다. 도시에서 꽤 시원하고 흐린 날입니다. 밖에 나가실 예정이라면 가벼운 재킷을 가져가시는 것이 좋습니다.           |

이 예제는 Claude가 여러 소스에서 데이터를 수집해야 하는 질문에 답하기 위해 여러 도구 호출을 연결하는 방법을 보여줍니다. 주요 단계는 다음과 같습니다:

1. Claude는 먼저 날씨 질문에 답하기 위해 사용자의 위치가 필요하다는 것을 인식하므로 `get_location` 도구를 호출합니다.
2. 사용자(즉, 클라이언트 코드)는 실제 `get_location` 함수를 실행하고 `tool_result` 블록에 "San Francisco, CA" 결과를 반환합니다.
3. 이제 위치를 알았으므로 Claude는 "San Francisco, CA"를 `location` 매개변수로 전달하여 `get_weather` 도구를 호출합니다(`unit`은 필수 매개변수가 아니므로 추측된 `unit` 매개변수도 함께).
4. 사용자는 제공된 인수로 실제 `get_weather` 함수를 다시 실행하고 다른 `tool_result` 블록으로 날씨 데이터를 반환합니다.
5. 마지막으로 Claude는 날씨 데이터를 원래 질문에 대한 자연어 응답에 통합합니다.
</details>
<details>
<summary>사고 연쇄 도구 사용</summary>

기본적으로 Claude Opus는 도구 사용 쿼리에 답하기 전에 생각하도록 프롬프트되어 도구가 필요한지, 어떤 도구를 사용할지, 적절한 매개변수가 무엇인지 가장 잘 결정합니다. Claude Sonnet과 Claude Haiku는 가능한 한 많이 도구를 사용하려고 시도하도록 프롬프트되며 불필요한 도구를 호출하거나 누락된 매개변수를 추론할 가능성이 높습니다. Sonnet 또는 Haiku가 도구 호출을 하기 전에 사용자 쿼리를 더 잘 평가하도록 하려면 다음 프롬프트를 사용할 수 있습니다:

사고 연쇄 프롬프트

`Answer the user's request using relevant tools (if they are available). Before calling a tool, do some analysis. First, think about which of the provided tools is the relevant tool to answer the user's request. Second, go through each of the required parameters of the relevant tool and determine if the user has directly provided or given enough information to infer a value. When deciding if the parameter can be inferred, carefully consider all the context to see if it supports a specific value. If all of the required parameters are present or can be reasonably inferred, proceed with the tool call. BUT, if one of the values for a required parameter is missing, DO NOT invoke the function (not even with fillers for the missing params) and instead, ask the user to provide the missing parameters. DO NOT ask for more information on optional parameters if it is not provided.
`
</details>

---

## 가격

도구 사용 요청은 다음을 기준으로 가격이 책정됩니다:
1. 모델에 전송된 총 입력 토큰 수(`tools` 매개변수 포함)
2. 생성된 출력 토큰 수
3. 서버 측 도구의 경우 추가 사용량 기반 가격(예: 웹 검색은 수행된 검색당 요금 부과)

클라이언트 측 도구는 다른 Claude API 요청과 동일하게 가격이 책정되며, 서버 측 도구는 특정 사용량에 따라 추가 요금이 부과될 수 있습니다.

도구 사용의 추가 토큰은 다음에서 발생합니다:

- API 요청의 `tools` 매개변수(도구 이름, 설명 및 스키마)
- API 요청 및 응답의 `tool_use` 콘텐츠 블록
- API 요청의 `tool_result` 콘텐츠 블록

`tools`를 사용하면 도구 사용을 가능하게 하는 모델을 위한 특수 시스템 프롬프트도 자동으로 포함됩니다. 각 모델에 필요한 도구 사용 토큰 수는 아래에 나열되어 있습니다(위에 나열된 추가 토큰 제외). 테이블은 적어도 1개의 도구가 제공된다고 가정합니다. `tools`가 제공되지 않으면 `none`의 도구 선택은 0개의 추가 시스템 프롬프트 토큰을 사용합니다.

| 모델                    | 도구 선택                                          | 도구 사용 시스템 프롬프트 토큰 수          |
|--------------------------|------------------------------------------------------|---------------------------------------------|
| Claude Opus 4.5            | `auto`, `none`<hr />`any`, `tool`   | 346 토큰<hr />313 토큰 |
| Claude Opus 4.1            | `auto`, `none`<hr />`any`, `tool`   | 346 토큰<hr />313 토큰 |
| Claude Opus 4            | `auto`, `none`<hr />`any`, `tool`   | 346 토큰<hr />313 토큰 |
| Claude Sonnet 4.5          | `auto`, `none`<hr />`any`, `tool`   | 346 토큰<hr />313 토큰 |
| Claude Sonnet 4          | `auto`, `none`<hr />`any`, `tool`   | 346 토큰<hr />313 토큰 |
| Claude Sonnet 3.7 ([더 이상 사용되지 않음](/docs/en/about-claude/model-deprecations))        | `auto`, `none`<hr />`any`, `tool`   | 346 토큰<hr />313 토큰 |
| Claude Haiku 4.5         | `auto`, `none`<hr />`any`, `tool`   | 346 토큰<hr />313 토큰 |
| Claude Haiku 3.5         | `auto`, `none`<hr />`any`, `tool`   | 264 토큰<hr />340 토큰 |
| Claude Opus 3 ([더 이상 사용되지 않음](/docs/en/about-claude/model-deprecations))            | `auto`, `none`<hr />`any`, `tool`   | 530 토큰<hr />281 토큰 |
| Claude Sonnet 3          | `auto`, `none`<hr />`any`, `tool`   | 159 토큰<hr />235 토큰 |
| Claude Haiku 3           | `auto`, `none`<hr />`any`, `tool`   | 264 토큰<hr />340 토큰 |

이러한 토큰 수는 요청의 총 비용을 계산하기 위해 일반 입력 및 출력 토큰에 추가됩니다.

현재 모델별 가격은 [모델 개요 테이블](/docs/en/about-claude/models/overview#latest-models-comparison)을 참조하세요.

도구 사용 프롬프트를 보낼 때 다른 API 요청과 마찬가지로 응답은 보고된 `usage` 메트릭의 일부로 입력 및 출력 토큰 수를 모두 출력합니다.

---

## 다음 단계

쿡북에서 바로 구현할 수 있는 도구 사용 코드 예제 저장소를 살펴보세요:


  
> 정확한 수치 계산을 위해 간단한 계산기 도구를 Claude와 통합하는 방법을 알아보세요.


{" "}

> 클라이언트 도구를 활용하여 지원을 향상시키는 반응형 고객 서비스 봇을 구축하세요.


  
> Claude와 도구 사용이 구조화되지 않은 텍스트에서 구조화된 데이터를 추출하는 방법을 확인하세요.



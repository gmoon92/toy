# [도구 사용 구현하기](https://platform.claude.com/docs/en/agents-and-tools/tool-use/implement-tool-use)

---

## 모델 선택하기

복잡한 도구와 모호한 쿼리에는 최신 Claude Sonnet(4.5) 또는 Claude Opus(4.5) 모델을 권장합니다. 
이 모델들은 여러 도구를 더 잘 처리하고 필요할 때 명확한 설명을 요청합니다.

간단한 도구에는 Claude Haiku 모델을 사용할 수 있지만, 누락된 매개변수를 추론할 수 있다는 점에 유의하세요.

> 확장 사고(extended thinking)와 함께 도구 사용을 활용하는 경우, 자세한 내용은 [여기](../02-capabilities/03-extended-thinking.md)의 가이드를 참조하세요.

## 클라이언트 도구 지정하기

클라이언트 도구(Anthropic 정의 및 사용자 정의 모두)는 API 요청의 최상위 `tools` 매개변수에 지정됩니다. 각 도구 정의에는 다음이 포함됩니다:

| 매개변수             | 설명                                                                                                               |
|:-----------------|:-----------------------------------------------------------------------------------------------------------------|
| `name`           | 도구의 이름입니다. 정규식 `^[a-zA-Z0-9_-]{1,64}$`와 일치해야 합니다.                                                                |
| `description`    | 도구가 수행하는 작업, 사용 시기, 동작 방식에 대한 상세한 일반 텍스트 설명입니다.                                                                  |
| `input_schema`   | 도구에 예상되는 매개변수를 정의하는 [JSON Schema](https://json-schema.org/) 객체입니다.                                               |
| `input_examples` | (선택사항, 베타) Claude가 도구 사용 방법을 이해하는 데 도움이 되는 예제 입력 객체 배열입니다. [도구 사용 예제 제공하기](#providing-tool-use-examples)를 참조하세요. |

<details>
<summary>간단한 도구 정의 예제</summary>

```json JSON
{
  "name": "get_weather",
  "description": "지정된 위치의 현재 날씨를 가져옵니다",
  "input_schema": {
    "type": "object",
    "properties": {
      "location": {
        "type": "string",
        "description": "도시와 주, 예: San Francisco, CA"
      },
      "unit": {
        "type": "string",
        "enum": ["celsius", "fahrenheit"],
        "description": "온도 단위, 'celsius' 또는 'fahrenheit'"
      }
    },
    "required": ["location"]
  }
}
```

이 도구는 `get_weather`라는 이름으로, 필수 `location` 문자열과 "celsius" 또는 "fahrenheit" 중 하나여야 하는 선택적 `unit` 문자열을 포함하는 입력 객체를 예상합니다.
</details>

### 도구 사용 시스템 프롬프트

`tools` 매개변수를 사용하여 Claude API를 호출하면, 도구 정의, 도구 구성 및 사용자 지정 시스템 프롬프트로부터 특별한 시스템 프롬프트를 구성합니다. 
구성된 프롬프트는 모델이 지정된 도구를 사용하고 도구가 올바르게 작동하는 데 필요한 컨텍스트를 제공하도록 설계되었습니다:

```
In this environment you have access to a set of tools you can use to answer the user's question.
{{ FORMATTING INSTRUCTIONS }}
String and scalar parameters should be specified as is, while lists and objects should use JSON format. Note that spaces for string values are not stripped. The output is not expected to be valid XML and is parsed with regular expressions.
Here are the functions available in JSONSchema format:
{{ TOOL DEFINITIONS IN JSON SCHEMA }}
{{ USER SYSTEM PROMPT }}
{{ TOOL CONFIGURATION }}
```

### 도구 정의 모범 사례

도구를 사용할 때 Claude에서 최상의 성능을 얻으려면 다음 지침을 따르세요:

- **매우 상세한 설명을 제공하세요.** 
  - 이것은 도구 성능에서 가장 중요한 요소입니다. 설명에는 다음을 포함한 도구에 대한 모든 세부 정보가 포함되어야 합니다:
      - 도구가 수행하는 작업
      - 사용해야 하는 시기(그리고 사용하지 말아야 하는 시기)
      - 각 매개변수의 의미와 도구 동작에 미치는 영향
      - 도구 이름이 불명확한 경우 도구가 반환하지 않는 정보와 같은 중요한 주의사항이나 제한사항. 
      - 도구에 대해 Claude에게 더 많은 컨텍스트를 제공할수록 도구를 언제 어떻게 사용할지 더 잘 결정할 수 있습니다. 
      - 도구 설명당 최소 3-4문장을 목표로 하고, 도구가 복잡한 경우 더 많이 작성하세요.
- **설명을 우선시하되, 복잡한 도구에는 `input_examples` 사용을 고려하세요.** 명확한 설명이 가장 중요하지만, 복잡한 입력, 중첩된 객체 또는 형식에 민감한 매개변수가 있는 도구의 경우
  `input_examples` 필드(베타)를 사용하여 스키마 검증된 예제를 제공할 수 있습니다. 자세한 내용은 [도구 사용 예제 제공하기](#providing-tool-use-examples)를 참조하세요.

<details>
<summary>좋은 도구 설명의 예</summary>

```json JSON
{
  "name": "get_stock_price",
  "description": "주어진 티커 심볼의 현재 주가를 검색합니다. 티커 심볼은 NYSE 또는 NASDAQ과 같은 미국 주요 증권 거래소에서 공개적으로 거래되는 회사의 유효한 심볼이어야 합니다. 이 도구는 최신 거래 가격을 USD로 반환합니다. 사용자가 특정 주식의 현재 또는 최근 가격에 대해 문의할 때 사용해야 합니다. 주식이나 회사에 대한 다른 정보는 제공하지 않습니다.",
  "input_schema": {
    "type": "object",
    "properties": {
      "ticker": {
        "type": "string",
        "description": "주식 티커 심볼, 예: Apple Inc.의 경우 AAPL"
      }
    },
    "required": ["ticker"]
  }
}
```

</details>

<details>
<summary>좋지 않은 도구 설명의 예</summary>

```json JSON
{
  "name": "get_stock_price",
  "description": "티커의 주가를 가져옵니다.",
  "input_schema": {
    "type": "object",
    "properties": {
      "ticker": {
        "type": "string"
      }
    },
    "required": ["ticker"]
  }
}
```

</details>

좋은 설명은 도구가 수행하는 작업, 사용 시기, 반환하는 데이터, `ticker` 매개변수의 의미를 명확하게 설명합니다. 좋지 않은 설명은 너무 간략하여 Claude가 도구의 동작과 사용법에 대해 많은 미해결 질문을
남깁니다.

## 도구 사용 예제 제공하기

복잡한 도구를 더 효과적으로 사용하는 방법을 Claude가 이해하도록 도와주는 유효한 도구 입력의 구체적인 예제를 제공할 수 있습니다. 이는 중첩된 객체, 선택적 매개변수 또는 형식에 민감한 입력이 있는 복잡한
도구에 특히 유용합니다.


> 도구 사용 예제는 베타 기능입니다. 제공업체에 적합한 [베타 헤더](https://platform.claude.com/docs/en/api/beta-headers)를 포함하세요:
>
> | 제공업체 | 베타 헤더 | 지원 모델 |
> |----------|-------------|------------------|
> | Claude API,<br/>Microsoft Foundry | `advanced-tool-use-2025-11-20` | 모든 모델 |
> | Vertex AI,<br/>Amazon Bedrock | `tool-examples-2025-10-29` | Claude Opus 4.5만 해당 |

### 기본 사용법

도구 정의에 선택적 `input_examples` 필드를 추가하고 예제 입력 객체 배열을 제공하세요. 각 예제는 도구의 `input_schema`에 따라 유효해야 합니다:

<details>
<summary>Python 예시</summary>

```python
import anthropic

client = anthropic.Anthropic()

response = client.messages.create(
    model="claude-sonnet-4-5-20250929",
    max_tokens=1024,
    betas=["advanced-tool-use-2025-11-20"],
    tools=[
        {
            "name": "get_weather",
            "description": "지정된 위치의 현재 날씨를 가져옵니다",
            "input_schema": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "도시와 주, 예: San Francisco, CA"
                    },
                    "unit": {
                        "type": "string",
                        "enum": ["celsius", "fahrenheit"],
                        "description": "온도 단위"
                    }
                },
                "required": ["location"]
            },
            "input_examples": [
                {
                    "location": "San Francisco, CA",
                    "unit": "fahrenheit"
                },
                {
                    "location": "Tokyo, Japan",
                    "unit": "celsius"
                },
                {
                    "location": "New York, NY"  # 'unit'은 선택사항
                }
            ]
        }
    ],
    messages=[
        {"role": "user", "content": "샌프란시스코의 날씨는 어떤가요?"}
    ]
)
```

</details>

예제는 도구 스키마와 함께 프롬프트에 포함되어 Claude에게 올바른 형식의 도구 호출에 대한 구체적인 패턴을 보여줍니다. 이는 Claude가 선택적 매개변수를 포함할 시기, 사용할 형식, 복잡한 입력을 구조화하는
방법을 이해하는 데 도움이 됩니다.

### 요구사항 및 제한사항

- **스키마 검증** - 각 예제는 도구의 `input_schema`에 따라 유효해야 합니다. 유효하지 않은 예제는 400 오류를 반환합니다
- **서버 측 도구에는 지원되지 않음** - 사용자 정의 도구만 입력 예제를 가질 수 있습니다
- **토큰 비용** - 예제는 프롬프트 토큰을 추가합니다: 간단한 예제의 경우 약 20-50 토큰, 복잡한 중첩 객체의 경우 약 100-200 토큰

## 도구 러너 (베타)

도구 러너는 Claude로 도구를 실행하기 위한 즉시 사용 가능한 솔루션을 제공합니다. 도구 호출, 도구 결과, 대화 관리를 수동으로 처리하는 대신, 도구 러너가 자동으로:

- Claude가 도구를 호출할 때 도구를 실행합니다
- 요청/응답 주기를 처리합니다
- 대화 상태를 관리합니다
- 타입 안전성 및 검증을 제공합니다

대부분의 도구 사용 구현에 도구 러너를 사용하는 것을 권장합니다.


> 도구 러너는 현재 베타
> 버전이며 [Python](https://github.com/anthropics/anthropic-sdk-python/blob/main/tools.md), [TypeScript](https://github.com/anthropics/anthropic-sdk-typescript/blob/main/helpers.md#tool-helpers), [Ruby](https://github.com/anthropics/anthropic-sdk-ruby/blob/main/helpers.md#3-auto-looping-tool-runner-beta)
> SDK에서 사용할 수 있습니다.



> **압축을 통한 자동 컨텍스트 관리**
>
> 도구 러너는 자동 [압축](../02-capabilities/02-context-editing.md)을 지원하며, 토큰 사용량이 임계값을 초과하면 요약을 생성합니다. 이를 통해 장기 실행 에이전트 작업이 컨텍스트
> 윈도우 제한을 넘어 계속될 수 있습니다.

### 기본 사용법

SDK 헬퍼를 사용하여 도구를 정의한 다음 도구 러너를 사용하여 실행하세요.

<details>
<summary>Python</summary>

타입 힌트와 독스트링으로 도구를 정의하려면 `@beta_tool` 데코레이터를 사용하세요.


> 비동기 클라이언트를 사용하는 경우, `@beta_tool`을 `@beta_async_tool`로 바꾸고 `async def`로 함수를 정의하세요.

```python
import anthropic
import json
from anthropic import beta_tool

# 클라이언트 초기화
client = anthropic.Anthropic()

# 데코레이터를 사용하여 도구 정의
@beta_tool
def get_weather(location: str, unit: str = "fahrenheit") -> str:
    """지정된 위치의 현재 날씨를 가져옵니다.

    Args:
        location: 도시와 주, 예: San Francisco, CA
        unit: 온도 단위, 'celsius' 또는 'fahrenheit'
    """
    # 실제 구현에서는 여기서 날씨 API를 호출합니다
    return json.dumps({"temperature": "20°C", "condition": "Sunny"})

@beta_tool
def calculate_sum(a: int, b: int) -> str:
    """두 숫자를 더합니다.

    Args:
        a: 첫 번째 숫자
        b: 두 번째 숫자
    """
    return str(a + b)

# 도구 러너 사용
runner = client.beta.messages.tool_runner(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[get_weather, calculate_sum],
    messages=[
        {"role": "user", "content": "파리의 날씨는 어떤가요? 그리고 15 + 27은 얼마인가요?"}
    ]
)
for message in runner:
    print(message.content[0].text)
```

`@beta_tool` 데코레이터는 함수 인자와 독스트링을 검사하여 JSON 스키마 표현을 추출합니다. 예를 들어, `calculate_sum`은 다음과 같이 변환됩니다:

```json
{
  "name": "calculate_sum",
  "description": "두 정수를 더합니다.",
  "input_schema": {
    "additionalProperties": false,
    "properties": {
      "left": {
        "description": "더할 첫 번째 정수입니다.",
        "title": "Left",
        "type": "integer"
      },
      "right": {
        "description": "더할 두 번째 정수입니다.",
        "title": "Right",
        "type": "integer"
      }
    },
    "required": ["left", "right"],
    "type": "object"
  }
}
```

</details>

<details>
<summary>TypeScript</summary>

Zod 검증을 사용한 타입 안전 도구 정의에는 `betaZodTool()`을 사용하거나, JSON Schema 기반 정의에는 `betaTool()`을 사용하세요.

TypeScript는 도구 정의를 위한 두 가지 접근 방식을 제공합니다:

**Zod 사용 (권장)** - Zod 검증을 사용한 타입 안전 도구 정의에는 `betaZodTool()`을 사용하세요 (Zod 3.25.0 이상 필요):

```typescript
import { Anthropic } from '@anthropic-ai/sdk';
import { betaZodTool } from '@anthropic-ai/sdk/helpers/beta/zod';
import { z } from 'zod';

const anthropic = new Anthropic();

const getWeatherTool = betaZodTool({
  name: 'get_weather',
  description: '지정된 위치의 현재 날씨를 가져옵니다',
  inputSchema: z.object({
    location: z.string().describe('도시와 주, 예: San Francisco, CA'),
    unit: z.enum(['celsius', 'fahrenheit']).default('fahrenheit')
      .describe('온도 단위')
  }),
  run: async (input) => {
    // 실제 구현에서는 여기서 날씨 API를 호출합니다
    return JSON.stringify({temperature: '20°C', condition: 'Sunny'});
  }
});

const runner = anthropic.beta.messages.toolRunner({
  model: 'claude-sonnet-4-5',
  max_tokens: 1024,
  tools: [getWeatherTool],
  messages: [{ role: 'user', content: "파리의 날씨는 어떤가요?" }]
});

for await (const message of runner) {
  console.log(message.content[0].text);
}
```

**JSON Schema 사용** - Zod 없이 타입 안전 도구 정의를 위해 `betaTool()`을 사용하세요:


> Claude가 생성한 입력은 런타임에 검증되지 않습니다. 필요한 경우 `run` 함수 내에서 검증을 수행하세요.

```typescript
import { Anthropic } from '@anthropic-ai/sdk';
import { betaTool } from '@anthropic-ai/sdk/helpers/beta/json-schema';

const anthropic = new Anthropic();

const calculateSumTool = betaTool({
  name: 'calculate_sum',
  description: '두 숫자를 더합니다',
  inputSchema: {
    type: 'object',
    properties: {
      a: { type: 'number', description: '첫 번째 숫자' },
      b: { type: 'number', description: '두 번째 숫자' }
    },
    required: ['a', 'b']
  },
  run: async (input) => {
    return String(input.a + input.b);
  }
});

const runner = anthropic.beta.messages.toolRunner({
  model: 'claude-sonnet-4-5',
  max_tokens: 1024,
  tools: [calculateSumTool],
  messages: [{ role: 'user', content: "15 + 27은 얼마인가요?" }]
});

for await (const message of runner) {
  console.log(message.content[0].text);
}
```

</details>

<details>
<summary>Ruby</summary>

타입이 지정된 입력 스키마로 도구를 정의하려면 `Anthropic::BaseTool` 클래스를 사용하세요.

```ruby
require "anthropic"

# 클라이언트 초기화
client = Anthropic::Client.new

# 입력 스키마 정의
class GetWeatherInput < Anthropic::BaseModel
  required :location, String, doc: "도시와 주, 예: San Francisco, CA"
  optional :unit, Anthropic::InputSchema::EnumOf["celsius", "fahrenheit"],
           doc: "온도 단위"
end

# 도구 정의
class GetWeather < Anthropic::BaseTool
  doc "지정된 위치의 현재 날씨를 가져옵니다"
  input_schema GetWeatherInput

  def call(input)
    # 실제 구현에서는 여기서 날씨 API를 호출합니다
    JSON.generate({temperature: "20°C", condition: "Sunny"})
  end
end

class CalculateSumInput < Anthropic::BaseModel
  required :a, Integer, doc: "첫 번째 숫자"
  required :b, Integer, doc: "두 번째 숫자"
end

class CalculateSum < Anthropic::BaseTool
  doc "두 숫자를 더합니다"
  input_schema CalculateSumInput

  def call(input)
    (input.a + input.b).to_s
  end
end

# 도구 러너 사용
runner = client.beta.messages.tool_runner(
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [GetWeather.new, CalculateSum.new],
  messages: [
    {role: "user", content: "파리의 날씨는 어떤가요? 그리고 15 + 27은 얼마인가요?"}
  ]
)

runner.each_message do |message|
  message.content.each do |block|
    puts block.text if block.respond_to?(:text)
  end
end
```

`Anthropic::BaseTool` 클래스는 도구 설명에 `doc` 메서드를 사용하고 예상 매개변수를 정의하는 데 `input_schema`를 사용합니다. SDK는 이것을 적절한 JSON 스키마 형식으로 자동
변환합니다.

</details>

도구 함수는 텍스트, 이미지 또는 문서 블록을 포함한 콘텐츠 블록 또는 콘텐츠 블록 배열을 반환해야 합니다. 이를 통해 도구가 풍부한 멀티모달 응답을 반환할 수 있습니다. 반환된 문자열은 텍스트 콘텐츠 블록으로
변환됩니다. 구조화된 JSON 객체를 Claude에게 반환하려면 반환하기 전에 JSON 문자열로 인코딩하세요. 숫자, 불리언 또는 기타 비문자열 원시 타입도 문자열로 변환해야 합니다.

### 도구 러너 반복하기

도구 러너는 Claude의 메시지를 생성하는 반복 가능한 객체입니다. 이를 흔히 "도구 호출 루프"라고 합니다. 각 반복에서 러너는 Claude가 도구 사용을 요청했는지 확인합니다. 그렇다면 도구를 호출하고 결과를
자동으로 Claude에게 다시 보낸 다음, Claude의 다음 메시지를 생성하여 루프를 계속합니다.

`break` 문으로 모든 반복에서 루프를 종료할 수 있습니다. 러너는 Claude가 도구 사용 없이 메시지를 반환할 때까지 루프합니다.

중간 메시지가 필요하지 않은 경우 최종 메시지를 직접 가져올 수 있습니다:

<details>
<summary>Python</summary>

최종 메시지를 얻으려면 `runner.until_done()`을 사용하세요.

```python
runner = client.beta.messages.tool_runner(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[get_weather, calculate_sum],
    messages=[
        {"role": "user", "content": "파리의 날씨는 어떤가요? 그리고 15 + 27은 얼마인가요?"}
    ]
)
final_message = runner.until_done()
print(final_message.content[0].text)
```

</details>

<details>
<summary>TypeScript</summary>

최종 메시지를 얻으려면 러너를 단순히 `await`하세요.

```typescript
const runner = anthropic.beta.messages.toolRunner({
  model: 'claude-sonnet-4-5',
  max_tokens: 1024,
  tools: [getWeatherTool],
  messages: [{ role: 'user', content: "파리의 날씨는 어떤가요?" }]
});

const finalMessage = await runner;
console.log(finalMessage.content[0].text);
```

</details>

<details>
<summary>Ruby</summary>

모든 메시지를 얻으려면 `runner.run_until_finished`를 사용하세요.

```ruby
runner = client.beta.messages.tool_runner(
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [GetWeather.new, CalculateSum.new],
  messages: [
    {role: "user", content: "파리의 날씨는 어떤가요? 그리고 15 + 27은 얼마인가요?"}
  ]
)

all_messages = runner.run_until_finished
all_messages.each { |msg| puts msg.content }
```

</details>

### 고급 사용법

루프 내에서 도구 러너의 다음 요청을 완전히 사용자 정의할 수 있습니다. 러너는 메시지 기록에 도구 결과를 자동으로 추가하므로 수동으로 관리할 필요가 없습니다. 선택적으로 로깅이나 디버깅을 위해 도구 결과를 검사하고
다음 API 호출 전에 요청 매개변수를 수정할 수 있습니다.

<details>
<summary>Python</summary>

도구 결과를 선택적으로 검사하려면 `generate_tool_call_response()`를 사용하세요(러너가 자동으로 추가합니다). 요청을 수정하려면 `set_messages_params()` 및
`append_messages()`를 사용하세요.

```python
runner = client.beta.messages.tool_runner(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[get_weather],
    messages=[{"role": "user", "content": "샌프란시스코의 날씨는 어떤가요?"}]
)
for message in runner:
    # 선택사항: 도구 응답 검사 (러너가 자동으로 추가함)
    tool_response = runner.generate_tool_call_response()
    if tool_response:
        print(f"도구 결과: {tool_response}")

    # 다음 요청 사용자 정의
    runner.set_messages_params(lambda params: {
        **params,
        "max_tokens": 2048  # 다음 요청에 대한 토큰 증가
    })

    # 또는 추가 메시지 추가
    runner.append_messages(
        {"role": "user", "content": "답변을 간결하게 해주세요."}
    )
```

</details>

<details>
<summary>TypeScript</summary>

도구 결과를 선택적으로 검사하려면 `generateToolResponse()`를 사용하세요(러너가 자동으로 추가합니다). 요청을 수정하려면 `setMessagesParams()` 및 `pushMessages()`를
사용하세요.

```typescript
const runner = anthropic.beta.messages.toolRunner({
  model: 'claude-sonnet-4-5',
  max_tokens: 1024,
  tools: [getWeatherTool],
  messages: [{ role: 'user', content: "샌프란시스코의 날씨는 어떤가요?" }]
});

for await (const message of runner) {
  // 선택사항: 도구 결과 메시지 검사 (러너가 자동으로 추가함)
  const toolResultMessage = await runner.generateToolResponse();
  if (toolResultMessage) {
    console.log('도구 결과:', toolResultMessage);
  }

  // 다음 요청 사용자 정의
  runner.setMessagesParams(params => ({
    ...params,
    max_tokens: 2048  // 다음 요청에 대한 토큰 증가
  }));

  // 또는 추가 메시지 추가
  runner.pushMessages(
    { role: 'user', content: '답변을 간결하게 해주세요.' }
  );
}
```

</details>

<details>
<summary>Ruby</summary>

단계별 제어를 위해 `next_message`를 사용하세요. 메시지를 삽입하려면 `feed_messages`를, 매개변수에 액세스하려면 `params`를 사용하세요.

```ruby
runner = client.beta.messages.tool_runner(
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [GetWeather.new],
  messages: [{role: "user", content: "샌프란시스코의 날씨는 어떤가요?"}]
)

# 수동 단계별 제어
message = runner.next_message
puts message.content

# 후속 메시지 삽입
runner.feed_messages([
  {role: "user", content: "보스턴도 확인해주세요"}
])

# 현재 매개변수에 액세스
puts runner.params
```

</details>

#### 도구 실행 디버깅

도구가 예외를 발생시키면 도구 러너가 이를 포착하고 `is_error: true`와 함께 도구 결과로 Claude에게 오류를 반환합니다. 기본적으로 예외 메시지만 포함되고 전체 스택 추적은 포함되지 않습니다.

전체 스택 추적 및 디버그 정보를 보려면 `ANTHROPIC_LOG` 환경 변수를 설정하세요:

```bash
# 도구 오류를 포함한 info 레벨 로그 보기
export ANTHROPIC_LOG=info

# 더 자세한 출력을 위한 debug 레벨 로그 보기
export ANTHROPIC_LOG=debug
```

활성화되면 SDK는 도구가 실패할 때 전체 스택 추적을 포함한 전체 예외 세부 정보를 로깅합니다(Python의 `logging` 모듈, TypeScript의 콘솔 또는 Ruby의 로거 사용).

#### 도구 오류 가로채기

기본적으로 도구 오류는 Claude에게 다시 전달되며, Claude는 적절하게 응답할 수 있습니다. 그러나 오류를 감지하고 다르게 처리하고 싶을 수 있습니다. 예를 들어 실행을 조기에 중지하거나 사용자 정의 오류
처리를 구현하는 경우입니다.

도구 응답 메서드를 사용하여 도구 결과를 가로채고 Claude에게 전송되기 전에 오류를 확인하세요:

<details>
<summary>Python</summary>

```python
import json

runner = client.beta.messages.tool_runner(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[my_tool],
    messages=[{"role": "user", "content": "도구 실행"}]
)

for message in runner:
    tool_response = runner.generate_tool_call_response()

    if tool_response:
        # 도구 결과에 오류가 있는지 확인
        for block in tool_response.content:
            if block.is_error:
                # 옵션 1: 루프를 중지하기 위해 예외 발생
                raise RuntimeError(f"도구 실패: {json.dumps(block.content)}")

                # 옵션 2: 로그 남기고 계속 (Claude가 처리하도록 함)
                # logger.error(f"도구 오류: {json.dumps(block.content)}")

    # 메시지를 정상적으로 처리
    print(message.content)
```

</details>

<details>
<summary>TypeScript</summary>

```typescript
const runner = anthropic.beta.messages.toolRunner({
  model: 'claude-sonnet-4-5',
  max_tokens: 1024,
  tools: [myTool],
  messages: [{ role: 'user', content: '도구 실행' }]
});

for await (const message of runner) {
  const toolResultMessage = await runner.generateToolResponse();

  if (toolResultMessage) {
    // 도구 결과에 오류가 있는지 확인
    for (const block of toolResultMessage.content) {
      if (block.type === 'tool_result' && block.is_error) {
        // 옵션 1: 루프를 중지하기 위해 예외 발생
        throw new Error(`도구 실패: ${JSON.stringify(block.content)}`);

        // 옵션 2: 로그 남기고 계속 (Claude가 처리하도록 함)
        // console.error(`도구 오류: ${JSON.stringify(block.content)}`);
      }
    }
  }

  // 메시지를 정상적으로 처리
  console.log(message.content);
}
```

</details>

<details>
<summary>Ruby</summary>

```ruby
runner = client.beta.messages.tool_runner(
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [MyTool.new],
  messages: [{role: "user", content: "도구 실행"}]
)

runner.each_message do |message|
  # 오류를 확인하기 위해 도구 응답 가져오기
  # 참고: 러너가 자동으로 도구 실행 및 결과 추가를 처리합니다
  # 이는 오류 확인/로깅 목적일 뿐입니다
  tool_results = runner.params[:messages].last

  if tool_results && tool_results[:role] == "user"
    tool_results[:content].each do |block|
      if block[:type] == "tool_result" && block[:is_error]
        # 옵션 1: 루프를 중지하기 위해 예외 발생
        raise "도구 실패: #{block[:content]}"

        # 옵션 2: 로그 남기고 계속 (Claude가 처리하도록 함)
        # logger.error("도구 오류: #{block[:content]}")
      end
    end
  end

  puts message.content
end
```

</details>

#### 도구 결과 수정

Claude에게 다시 보내기 전에 도구 결과를 수정할 수 있습니다. 이는 도구 결과에 [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md)을 활성화하기 위해
`cache_control`과 같은 메타데이터를 추가하거나 도구 출력을 변환하는 데 유용합니다.

도구 응답 메서드를 사용하여 도구 결과를 가져온 다음 수정하고 수정된 버전을 메시지에 추가하세요:

<details>
<summary>Python</summary>

```python
runner = client.beta.messages.tool_runner(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[search_documents],
    messages=[{"role": "user", "content": "샌프란시스코의 기후에 대한 정보를 검색하세요"}]
)

for message in runner:
    tool_response = runner.generate_tool_call_response()

    if tool_response:
        # 캐시 제어를 추가하기 위해 도구 결과 수정
        for block in tool_response.content:
            if block.type == "tool_result":
                # 이 도구 결과를 캐시하기 위해 cache_control 추가
                block.cache_control = {"type": "ephemeral"}

        # 수정된 응답 추가 (원본의 자동 추가 방지)
        runner.append_messages(message, tool_response)

    print(message.content)
```

</details>

<details>
<summary>TypeScript</summary>

```typescript
const runner = anthropic.beta.messages.toolRunner({
  model: 'claude-sonnet-4-5',
  max_tokens: 1024,
  tools: [searchDocuments],
  messages: [{ role: 'user', content: '샌프란시스코의 기후에 대한 정보를 검색하세요' }]
});

for await (const message of runner) {
  const toolResultMessage = await runner.generateToolResponse();

  if (toolResultMessage) {
    // 캐시 제어를 추가하기 위해 도구 결과 수정
    for (const block of toolResultMessage.content) {
      if (block.type === 'tool_result') {
        // 이 도구 결과를 캐시하기 위해 cache_control 추가
        block.cache_control = { type: 'ephemeral' };
      }
    }

    // 수정된 메시지 추가 (원본의 자동 추가 방지)
    runner.pushMessages(message, toolResultMessage);
  }

  console.log(message.content);
}
```

</details>

<details>
<summary>Ruby</summary>

```ruby
runner = client.beta.messages.tool_runner(
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [SearchDocuments.new],
  messages: [{role: "user", content: "샌프란시스코의 기후에 대한 정보를 검색하세요"}]
)

loop do
  message = runner.next_message
  break unless message

  # 메시지 배열에서 가장 최근 도구 결과에 액세스
  # 러너가 자동으로 도구 결과를 추가하지만 수정할 수 있습니다
  tool_results_message = runner.params[:messages].last

  if tool_results_message && tool_results_message[:role] == "user"
    tool_results_message[:content].each do |block|
      if block[:type] == "tool_result"
        # 캐시 제어를 추가하기 위해 도구 결과 수정
        block[:cache_control] = {type: "ephemeral"}
      end
    end
  end

  puts message.content
  break if message.stop_reason != "tool_use"
end
```

</details>


> 도구 결과에 `cache_control`을 추가하는 것은 도구가 후속 API 호출을 위해 캐시하려는 많은 양의 데이터(예: 문서 검색 결과)를 반환할 때 특히 유용합니다. 캐싱 전략에 대한 자세한
> 내용은 [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md)을 참조하세요.

### 스트리밍

스트리밍을 활성화하여 도착하는 대로 이벤트를 수신하세요. 각 반복은 이벤트를 반복할 수 있는 스트림 객체를 생성합니다.

<details>
<summary>Python</summary>

`stream=True`를 설정하고 누적된 메시지를 얻으려면 `get_final_message()`를 사용하세요.

```python
runner = client.beta.messages.tool_runner(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[calculate_sum],
    messages=[{"role": "user", "content": "15 + 27은 얼마인가요?"}],
    stream=True
)

# 스트리밍할 때 러너는 BetaMessageStream을 반환합니다
for message_stream in runner:
    for event in message_stream:
        print('이벤트:', event)
    print('메시지:', message_stream.get_final_message())

print(runner.until_done())
```

</details>

<details>
<summary>TypeScript</summary>

`stream: true`를 설정하고 누적된 메시지를 얻으려면 `finalMessage()`를 사용하세요.

```typescript
const runner = anthropic.beta.messages.toolRunner({
  model: 'claude-sonnet-4-5-20250929',
  max_tokens: 1000,
  messages: [{ role: 'user', content: '샌프란시스코의 날씨는 어떤가요?' }],
  tools: [getWeatherTool],
  stream: true,
});

// 스트리밍할 때 러너는 BetaMessageStream을 반환합니다
for await (const messageStream of runner) {
  for await (const event of messageStream) {
    console.log('이벤트:', event);
  }
  console.log('메시지:', await messageStream.finalMessage());
}

console.log(await runner);
```

</details>

<details>
<summary>Ruby</summary>

스트리밍 이벤트를 반복하려면 `each_streaming`을 사용하세요.

```ruby
runner = client.beta.messages.tool_runner(
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [CalculateSum.new],
  messages: [{role: "user", content: "15 + 27은 얼마인가요?"}]
)

runner.each_streaming do |event|
  case event
  when Anthropic::Streaming::TextEvent
    print event.text
  when Anthropic::Streaming::ToolUseEvent
    puts "\n도구 호출됨: #{event.tool_name}"
  end
end
```

</details>


> SDK 도구 러너는 베타 버전입니다. 이 문서의 나머지 부분에서는 수동 도구 구현을 다룹니다.

## Claude의 출력 제어하기

### 도구 사용 강제하기

일부 경우에는 Claude가 도구 없이 답변을 제공할 수 있다고 생각하더라도 사용자의 질문에 답변하기 위해 특정 도구를 사용하도록 하고 싶을 수 있습니다. 다음과 같이 `tool_choice` 필드에 도구를 지정하여
이를 수행할 수 있습니다:

```
tool_choice = {"type": "tool", "name": "get_weather"}
```

tool_choice 매개변수를 사용할 때 네 가지 가능한 옵션이 있습니다:

- `auto`는 Claude가 제공된 도구를 호출할지 여부를 결정하도록 허용합니다. 이것은 `tools`가 제공될 때의 기본값입니다.
- `any`는 Claude에게 제공된 도구 중 하나를 사용해야 한다고 알리지만 특정 도구를 강제하지는 않습니다.
- `tool`은 Claude가 항상 특정 도구를 사용하도록 강제할 수 있게 합니다.
- `none`은 Claude가 도구를 사용하는 것을 방지합니다. 이것은 `tools`가 제공되지 않을 때의 기본값입니다.

> [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md)을 사용할 때 `tool_choice` 매개변수의 변경은 캐시된 메시지 블록을 무효화합니다. 도구 정의와 시스템 프롬프트는
> 캐시된 상태로 유지되지만 메시지 콘텐츠는 재처리되어야 합니다.


이 다이어그램은 각 옵션이 어떻게 작동하는지 보여줍니다:

<Frame>
  ![Image](/docs/images/tool_choice.png)
</Frame>

`tool_choice`가 `any` 또는 `tool`일 때 도구를 사용하도록 강제하기 위해 어시스턴트 메시지를 미리 채운다는 점에 유의하세요. 이는 모델이 `tool_use` 콘텐츠 블록 전에 자연어 응답이나
설명을 명시적으로 요청받더라도 출력하지 않는다는 것을 의미합니다.


> [확장 사고](../02-capabilities/03-extended-thinking.md)를 도구 사용과 함께 사용할 때, `tool_choice: {"type": "any"}` 및
`tool_choice: {"type": "tool", "name": "..."}`는 지원되지 않으며 오류가 발생합니다. `tool_choice: {"type": "auto"}`(기본값) 및
`tool_choice: {"type": "none"}`만 확장 사고와 호환됩니다.


우리의 테스트에 따르면 이것이 성능을 감소시키지 않아야 합니다. 모델이 특정 도구를 사용하도록 요청하면서 자연어 컨텍스트나 설명을 제공하기를 원한다면, `tool_choice`에 `{"type": "auto"}`(
기본값)를 사용하고 `user` 메시지에 명시적 지시를 추가할 수 있습니다. 예를 들어: `런던의 날씨는 어떤가요? 응답에 get_weather 도구를 사용하세요.`


> **엄격한 도구를 사용한 보장된 도구 호출**
>
> `tool_choice: {"type": "any"}`를 [엄격한 도구 사용](../02-capabilities/15-structured-outputs.md)과 결합하여 도구 중 하나가 호출되고 도구 입력이
> 스키마를 엄격하게 따르는 것을 모두 보장하세요. 스키마 검증을 활성화하려면 도구 정의에 `strict: true`를 설정하세요.

### JSON 출력

도구가 반드시 클라이언트 함수일 필요는 없습니다. 모델이 제공된 스키마를 따르는 JSON 출력을 반환하기를 원할 때마다 도구를 사용할 수 있습니다. 예를 들어, 특정 스키마를 가진 `record_summary`
도구를 사용할 수 있습니다. 전체 작동 예제는 [Claude와 도구 사용](../03-tools/01-overview.md)을 참조하세요.

### 도구를 사용한 모델 응답

도구를 사용할 때 Claude는 도구를 호출하기 전에 자신이 하고 있는 일에 대해 언급하거나 사용자에게 자연스럽게 응답하는 경우가 많습니다.

예를 들어, "지금 샌프란시스코의 날씨는 어떤가요, 그리고 그곳은 몇 시인가요?"라는 프롬프트가 주어지면 Claude는 다음과 같이 응답할 수 있습니다:

```json JSON
{
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "샌프란시스코의 현재 날씨와 시간을 확인하는 데 도움을 드리겠습니다."
    },
    {
      "type": "tool_use",
      "id": "toolu_01A09q90qw90lq917835lq9",
      "name": "get_weather",
      "input": {"location": "San Francisco, CA"}
    }
  ]
}
```

이러한 자연스러운 응답 스타일은 사용자가 Claude가 무엇을 하고 있는지 이해하는 데 도움이 되며 더 대화적인 상호작용을 만듭니다. 시스템 프롬프트에 `<examples>`를 제공하여 이러한 응답의 스타일과 내용을
안내할 수 있습니다.

Claude가 자신의 행동을 설명할 때 다양한 표현과 접근 방식을 사용할 수 있다는 점에 유의하는 것이 중요합니다. 코드는 이러한 응답을 다른 어시스턴트 생성 텍스트처럼 처리해야 하며 특정 형식 규칙에 의존해서는 안
됩니다.

### 병렬 도구 사용

기본적으로 Claude는 사용자 쿼리에 답변하기 위해 여러 도구를 사용할 수 있습니다. 다음을 통해 이 동작을 비활성화할 수 있습니다:

- tool_choice 타입이 `auto`일 때 `disable_parallel_tool_use=true`를 설정하면 Claude가 **최대 하나의** 도구를 사용하도록 보장합니다
- tool_choice 타입이 `any` 또는 `tool`일 때 `disable_parallel_tool_use=true`를 설정하면 Claude가 **정확히 하나의** 도구를 사용하도록 보장합니다

<details>
<summary>완전한 병렬 도구 사용 예제</summary>

> **도구 러너로 더 간단하게**: 아래 예제는 수동 병렬 도구 처리를 보여줍니다. 대부분의 사용 사례에서 [도구 러너](#tool-runner-beta)가 훨씬 적은 코드로 병렬 도구 실행을 자동으로 처리합니다.


다음은 메시지 기록에서 병렬 도구 호출을 올바르게 형식화하는 방법을 보여주는 완전한 예제입니다:

<details>
<summary>Python 예시</summary>

```python
import anthropic

client = anthropic.Anthropic()

# 도구 정의
tools = [
    {
        "name": "get_weather",
        "description": "지정된 위치의 현재 날씨를 가져옵니다",
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
    },
    {
        "name": "get_time",
        "description": "지정된 시간대의 현재 시간을 가져옵니다",
        "input_schema": {
            "type": "object",
            "properties": {
                "timezone": {
                    "type": "string",
                    "description": "시간대, 예: America/New_York"
                }
            },
            "required": ["timezone"]
        }
    }
]

# 초기 요청
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=tools,
    messages=[
        {
            "role": "user",
            "content": "SF와 NYC의 날씨는 어떤가요, 그리고 그곳의 시간은 몇 시인가요?"
        }
    ]
)

# 병렬 도구 호출을 포함한 Claude의 응답
print("Claude가 도구를 사용하려고 합니다:", response.stop_reason == "tool_use")
print("도구 호출 수:", len([c for c in response.content if c.type == "tool_use"]))

# 도구 결과로 대화 구성
messages = [
    {
        "role": "user",
        "content": "SF와 NYC의 날씨는 어떤가요, 그리고 그곳의 시간은 몇 시인가요?"
    },
    {
        "role": "assistant",
        "content": response.content  # 여러 tool_use 블록 포함
    },
    {
        "role": "user",
        "content": [
            {
                "type": "tool_result",
                "tool_use_id": "toolu_01",  # tool_use의 ID와 일치해야 함
                "content": "샌프란시스코: 68°F, 부분적으로 흐림"
            },
            {
                "type": "tool_result",
                "tool_use_id": "toolu_02",
                "content": "뉴욕: 45°F, 맑음"
            },
            {
                "type": "tool_result",
                "tool_use_id": "toolu_03",
                "content": "샌프란시스코 시간: 오후 2:30 PST"
            },
            {
                "type": "tool_result",
                "tool_use_id": "toolu_04",
                "content": "뉴욕 시간: 오후 5:30 EST"
            }
        ]
    }
]

# 최종 응답 얻기
final_response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=tools,
    messages=messages
)

print(final_response.content[0].text)
```

</details>

병렬 도구 호출이 있는 어시스턴트 메시지는 다음과 같습니다:

```json
{
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "샌프란시스코와 뉴욕시의 날씨와 시간을 확인하겠습니다."
    },
    {
      "type": "tool_use",
      "id": "toolu_01",
      "name": "get_weather",
      "input": {"location": "San Francisco, CA"}
    },
    {
      "type": "tool_use",
      "id": "toolu_02",
      "name": "get_weather",
      "input": {"location": "New York, NY"}
    },
    {
      "type": "tool_use",
      "id": "toolu_03",
      "name": "get_time",
      "input": {"timezone": "America/Los_Angeles"}
    },
    {
      "type": "tool_use",
      "id": "toolu_04",
      "name": "get_time",
      "input": {"timezone": "America/New_York"}
    }
  ]
}
```

</details>
<details>
<summary>병렬 도구를 위한 완전한 테스트 스크립트</summary>

다음은 병렬 도구 호출이 올바르게 작동하는지 테스트하고 검증하기 위한 완전한 실행 가능한 스크립트입니다:

<details>
<summary>Python 예시</summary>

```python
#!/usr/bin/env python3
"""Claude API로 병렬 도구 호출을 검증하는 테스트 스크립트"""

import os
from anthropic import Anthropic

# 클라이언트 초기화
client = Anthropic(api_key=os.environ.get("ANTHROPIC_API_KEY"))

# 도구 정의
tools = [
    {
        "name": "get_weather",
        "description": "지정된 위치의 현재 날씨를 가져옵니다",
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
    },
    {
        "name": "get_time",
        "description": "지정된 시간대의 현재 시간을 가져옵니다",
        "input_schema": {
            "type": "object",
            "properties": {
                "timezone": {
                    "type": "string",
                    "description": "시간대, 예: America/New_York"
                }
            },
            "required": ["timezone"]
        }
    }
]

# 병렬 도구 호출이 있는 테스트 대화
messages = [
    {
        "role": "user",
        "content": "SF와 NYC의 날씨는 어떤가요, 그리고 그곳의 시간은 몇 시인가요?"
    }
]

# 초기 요청
print("병렬 도구 호출 요청 중...")
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=messages,
    tools=tools
)

# 병렬 도구 호출 확인
tool_uses = [block for block in response.content if block.type == "tool_use"]
print(f"\n✓ Claude가 {len(tool_uses)}개의 도구를 호출했습니다")

if len(tool_uses) > 1:
    print("✓ 병렬 도구 호출 감지!")
    for tool in tool_uses:
        print(f"  - {tool.name}: {tool.input}")
else:
    print("✗ 병렬 도구 호출이 감지되지 않았습니다")

# 도구 실행 시뮬레이션 및 결과 올바르게 형식화
tool_results = []
for tool_use in tool_uses:
    if tool_use.name == "get_weather":
        if "San Francisco" in str(tool_use.input):
            result = "샌프란시스코: 68°F, 부분적으로 흐림"
        else:
            result = "뉴욕: 45°F, 맑음"
    else:  # get_time
        if "Los_Angeles" in str(tool_use.input):
            result = "오후 2:30 PST"
        else:
            result = "오후 5:30 EST"

    tool_results.append({
        "type": "tool_result",
        "tool_use_id": tool_use.id,
        "content": result
    })

# 도구 결과로 대화 계속
messages.extend([
    {"role": "assistant", "content": response.content},
    {"role": "user", "content": tool_results}  # 모든 결과를 하나의 메시지에!
])

# 최종 응답 얻기
print("\n최종 응답을 가져오는 중...")
final_response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=messages,
    tools=tools
)

print(f"\nClaude의 응답:\n{final_response.content[0].text}")

# 형식 검증
print("\n--- 검증 ---")
print(f"✓ 도구 결과가 단일 사용자 메시지로 전송됨: {len(tool_results)}개 결과")
print("✓ 콘텐츠 배열에서 도구 결과 전에 텍스트 없음")
print("✓ 향후 병렬 도구 사용을 위해 대화가 올바르게 형식화됨")
```

</details>

이 스크립트는 다음을 보여줍니다:

- 병렬 도구 호출 및 결과를 올바르게 형식화하는 방법
- 병렬 호출이 이루어지고 있는지 검증하는 방법
- 향후 병렬 도구 사용을 권장하는 올바른 메시지 구조
- 피해야 할 일반적인 실수 (예: 도구 결과 전에 텍스트 넣기)

이 스크립트를 실행하여 구현을 테스트하고 Claude가 병렬 도구 호출을 효과적으로 수행하는지 확인하세요.
</details>

#### 병렬 도구 사용 극대화

Claude 4 모델은 기본적으로 우수한 병렬 도구 사용 기능을 갖추고 있지만, 타겟 프롬프팅을 통해 모든 모델에서 병렬 도구 실행의 가능성을 높일 수 있습니다:

<details>
<summary>병렬 도구 사용을 위한 시스템 프롬프트</summary>

Claude 4 모델(Opus 4, Sonnet 4)의 경우 시스템 프롬프트에 다음을 추가하세요:

```text
최대 효율을 위해 여러 독립적인 작업을 수행해야 할 때마다 순차적이 아닌 동시에 관련된 모든 도구를 호출하세요.
```

더 강력한 병렬 도구 사용을 위해 (기본값이 충분하지 않은 경우 권장):

```text
<use_parallel_tool_calls>
최대 효율을 위해 여러 독립적인 작업을 수행할 때마다 순차적이 아닌 동시에 관련된 모든 도구를 호출하세요. 가능한 한 병렬로 도구를 호출하는 것을 우선시하세요. 예를 들어, 3개의 파일을 읽을 때 3개의 도구 호출을 병렬로 실행하여 3개의 파일을 모두 동시에 컨텍스트로 읽어들이세요. `ls` 또는 `list_dir`와 같은 여러 읽기 전용 명령을 실행할 때 항상 모든 명령을 병렬로 실행하세요. 너무 많은 도구를 순차적으로 실행하는 것보다 병렬 도구 호출을 극대화하는 쪽으로 치우치세요.
</use_parallel_tool_calls>
```

</details>
<details>
<summary>사용자 메시지 프롬프팅</summary>

특정 사용자 메시지 내에서 병렬 도구 사용을 권장할 수도 있습니다:

```python
# 다음 대신:
"파리의 날씨는 어떤가요? 런던도 확인해주세요."

# 다음을 사용:
"파리와 런던의 날씨를 동시에 확인하세요."

# 또는 명시적으로:
"병렬 도구 호출을 사용하여 파리, 런던, 도쿄의 날씨를 동시에 가져오세요."
```

</details>


> **Claude Sonnet 3.7의 병렬 도구 사용**
>
> Claude Sonnet 3.7은 `disable_parallel_tool_use`를 설정하지 않은 경우에도 응답에서 병렬 도구 호출을 수행할 가능성이 낮을 수 있습니다. 내장 토큰 효율적 도구 사용 및 향상된
> 병렬 도구 호출이 있는 [Claude 4 모델로 업그레이드](https://platform.claude.com/docs/en/about-claude/models/migrating-to-claude-4)하는 것을
> 권장합니다.
>
> 여전히 Claude Sonnet 3.7을 사용하는 경우, Claude가 병렬 도구를 사용하도록 장려하는 데 도움이 되는
`token-efficient-tools-2025-02-19` [베타 헤더](https://platform.claude.com/docs/en/api/beta-headers)를 활성화할 수 있습니다. 여러 도구를
> 동시에 호출하는 메타 도구로 작동할 수 있는 "배치 도구"를 도입할 수도 있습니다.
>
> 이 해결 방법을 사용하는 방법은 쿡북의 [이 예제](https://platform.claude.com/cookbook/tool-use-parallel-tools)를 참조하세요.

## 도구 사용 및 도구 결과 콘텐츠 블록 처리

> **도구 러너로 더 간단하게**: 이 섹션에서 설명하는 수동 도구 처리는 [도구 러너](#tool-runner-beta)가 자동으로 관리합니다. 도구 실행에 대한 사용자 정의 제어가 필요할 때 이 섹션을
> 사용하세요.


Claude의 응답은 클라이언트 도구를 사용하는지 서버 도구를 사용하는지에 따라 다릅니다.

### 클라이언트 도구의 결과 처리

응답은 `stop_reason`이 `tool_use`이고 다음을 포함하는 하나 이상의 `tool_use` 콘텐츠 블록을 갖습니다:

- `id`: 이 특정 도구 사용 블록의 고유 식별자입니다. 나중에 도구 결과와 일치시키는 데 사용됩니다.
- `name`: 사용 중인 도구의 이름입니다.
- `input`: 도구의 `input_schema`를 준수하여 도구에 전달되는 입력을 포함하는 객체입니다.

<details>
<summary>`tool_use` 콘텐츠 블록이 있는 API 응답 예제</summary>

```json JSON
{
  "id": "msg_01Aq9w938a90dw8q",
  "model": "claude-sonnet-4-5",
  "stop_reason": "tool_use",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "샌프란시스코의 현재 날씨를 확인하겠습니다."
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

</details>

클라이언트 도구에 대한 도구 사용 응답을 받으면 다음을 수행해야 합니다:

1. `tool_use` 블록에서 `name`, `id`, `input`을 추출합니다.
2. 해당 도구 이름에 해당하는 코드베이스의 실제 도구를 실행하고 도구 `input`을 전달합니다.
3. `role`이 `user`이고 다음 정보를 포함하는 `tool_result` 타입의 `content` 블록이 있는 새 메시지를 보내 대화를 계속합니다:
    - `tool_use_id`: 이것이 결과인 도구 사용 요청의 `id`.
    - `content`: 도구의 결과, 문자열(예: `"content": "15 degrees"`), 중첩된 콘텐츠 블록 목록(예:
      `"content": [{"type": "text", "text": "15 degrees"}]`), 또는 문서 블록 목록(예:
      `"content": ["type": "document", "source": {"type": "text", "media_type": "text/plain", "data": "15 degrees"}]`)으로
      제공됩니다. 이러한 콘텐츠 블록은 `text`, `image` 또는 `document` 타입을 사용할 수 있습니다.
    - `is_error` (선택사항): 도구 실행이 오류를 발생시킨 경우 `true`로 설정합니다.

> **중요한 형식 요구사항**:
> - 도구 결과 블록은 메시지 기록에서 해당 도구 사용 블록 바로 뒤에 와야 합니다. 어시스턴트의 도구 사용 메시지와 사용자의 도구 결과 메시지 사이에 메시지를 포함할 수 없습니다.
> - 도구 결과를 포함하는 사용자 메시지에서 tool_result 블록은 콘텐츠 배열에서 먼저 와야 합니다. 텍스트는 모든 도구 결과 뒤에 와야 합니다.
>
> 예를 들어, 다음은 400 오류를 발생시킵니다:
> ```json
> {"role": "user", "content": [
> {"type": "text", "text": "결과는 다음과 같습니다:"},  // ❌ tool_result 전에 텍스트
> {"type": "tool_result", "tool_use_id": "toolu_01", ...}
> ]}
> ```
>
> 다음이 올바릅니다:
> ```json
> {"role": "user", "content": [
> {"type": "tool_result", "tool_use_id": "toolu_01", ...},
> {"type": "text", "text": "다음에 무엇을 해야 하나요?"}  // ✅ tool_result 뒤에 텍스트
> ]}
> ```
>
> "tool_use ids were found without tool_result blocks immediately after"와 같은 오류가 발생하면 도구 결과가 올바르게 형식화되었는지 확인하세요.


<details>
<summary>성공적인 도구 결과의 예</summary>

```json JSON
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
```

</details>

<details>
<summary>이미지가 있는 도구 결과의 예</summary>

```json JSON
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
      "content": [
        {"type": "text", "text": "15 degrees"},
        {
          "type": "image",
          "source": {
            "type": "base64",
            "media_type": "image/jpeg",
            "data": "/9j/4AAQSkZJRg...",
          }
        }
      ]
    }
  ]
}
```

</details>
<details>
<summary>빈 도구 결과의 예</summary>

```json JSON
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
    }
  ]
}
```

</details>

<details>
<summary>문서가 있는 도구 결과의 예</summary>

```json JSON
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
      "content": [
        {"type": "text", "text": "날씨는"},
        {
          "type": "document",
          "source": {
            "type": "text",
            "media_type": "text/plain",
            "data": "15 degrees"
          }
        }
      ]
    }
  ]
}
```

</details>

도구 결과를 받은 후 Claude는 해당 정보를 사용하여 원래 사용자 프롬프트에 대한 응답을 계속 생성합니다.

### 서버 도구의 결과 처리

Claude는 도구를 내부적으로 실행하고 추가적인 사용자 상호작용 없이 결과를 응답에 직접 통합합니다.


> **다른 API와의 차이점**
>
> 도구 사용을 분리하거나 `tool` 또는 `function`과 같은 특수 역할을 사용하는 API와 달리, Claude API는 도구를 `user` 및 `assistant` 메시지 구조에 직접 통합합니다.
>
> 메시지에는 `text`, `image`, `tool_use`, `tool_result` 블록의 배열이 포함됩니다. `user` 메시지에는 클라이언트 콘텐츠와 `tool_result`가 포함되고,
`assistant` 메시지에는 AI 생성 콘텐츠와 `tool_use`가 포함됩니다.

### `max_tokens` 중지 이유 처리

[
`max_tokens` 제한에 도달하여 Claude의 응답이 중단된](https://platform.claude.com/docs/en/build-with-claude/handling-stop-reasons#max-tokens)
경우 잘린 응답에 불완전한 도구 사용 블록이 포함되어 있으면 전체 도구 사용을 얻기 위해 더 높은 `max_tokens` 값으로 요청을 다시 시도해야 합니다.

<details>
<summary>Python 예시</summary>

```python
# 도구 사용 중 응답이 잘렸는지 확인
if response.stop_reason == "max_tokens":
    # 마지막 콘텐츠 블록이 불완전한 tool_use인지 확인
    last_block = response.content[-1]
    if last_block.type == "tool_use":
        # 더 높은 max_tokens로 요청 전송
        response = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=4096,  # 증가된 제한
            messages=messages,
            tools=tools
        )
```

</details>

#### `pause_turn` 중지 이유 처리

웹 검색과 같은 서버 도구를 사용할 때 API는 `pause_turn` 중지 이유를 반환할 수 있으며, 이는 API가 장기 실행 턴을 일시 중지했음을 나타냅니다.

다음은 `pause_turn` 중지 이유를 처리하는 방법입니다:

<details>
<summary>Python 예시</summary>

```python
import anthropic

client = anthropic.Anthropic()

# 웹 검색을 사용한 초기 요청
response = client.messages.create(
    model="claude-3-7-sonnet-latest",
    max_tokens=1024,
    messages=[
        {
            "role": "user",
            "content": "2025년 양자 컴퓨팅 돌파구에 대한 포괄적인 정보를 검색하세요"
        }
    ],
    tools=[{
        "type": "web_search_20250305",
        "name": "web_search",
        "max_uses": 10
    }]
)

# 응답에 pause_turn 중지 이유가 있는지 확인
if response.stop_reason == "pause_turn":
    # 일시 중지된 콘텐츠로 대화 계속
    messages = [
        {"role": "user", "content": "2025년 양자 컴퓨팅 돌파구에 대한 포괄적인 정보를 검색하세요"},
        {"role": "assistant", "content": response.content}
    ]

    # 연속 요청 전송
    continuation = client.messages.create(
        model="claude-3-7-sonnet-latest",
        max_tokens=1024,
        messages=messages,
        tools=[{
            "type": "web_search_20250305",
            "name": "web_search",
            "max_uses": 10
        }]
    )

    print(continuation)
else:
    print(response)
```

</details>

`pause_turn`을 처리할 때:

- **대화 계속**: 일시 중지된 응답을 후속 요청에서 그대로 전달하여 Claude가 턴을 계속하도록 합니다
- **필요시 수정**: 대화를 중단하거나 방향을 전환하려는 경우 계속하기 전에 선택적으로 콘텐츠를 수정할 수 있습니다
- **도구 상태 보존**: 기능을 유지하기 위해 연속 요청에 동일한 도구를 포함합니다

## 오류 문제 해결

> **내장 오류 처리**: [도구 러너](#tool-runner-beta)는 가장 일반적인 시나리오에 대한 자동 오류 처리를 제공합니다. 이 섹션은 고급 사용 사례를 위한 수동 오류 처리를 다룹니다.


Claude와 도구를 사용할 때 발생할 수 있는 몇 가지 유형의 오류가 있습니다:

<details>
<summary>도구 실행 오류</summary>

도구 자체가 실행 중에 오류를 발생시키는 경우(예: 날씨 데이터를 가져올 때 네트워크 오류), `"is_error": true`와 함께 오류 메시지를 `content`에 반환할 수 있습니다:

```json JSON
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
      "content": "ConnectionError: 날씨 서비스 API를 사용할 수 없습니다 (HTTP 500)",
      "is_error": true
    }
  ]
}
```

그러면 Claude는 이 오류를 사용자에게 전달하는 응답에 통합합니다. 예: "죄송합니다. 날씨 서비스 API를 사용할 수 없어 현재 날씨를 검색할 수 없습니다. 나중에 다시 시도해 주세요."
</details>
<details>
<summary>잘못된 도구 이름</summary>

Claude의 도구 사용 시도가 유효하지 않은 경우(예: 필수 매개변수 누락), 일반적으로 Claude가 도구를 올바르게 사용하기에 충분한 정보가 없었음을 의미합니다. 개발 중 최선의 방법은 도구 정의에서 더 상세한
`description` 값으로 요청을 다시 시도하는 것입니다.

그러나 오류를 나타내는 `tool_result`로 대화를 계속 진행할 수도 있으며, Claude는 누락된 정보를 채워 도구를 다시 사용하려고 시도합니다:

```json JSON
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
      "content": "Error: 필수 'location' 매개변수가 누락되었습니다",
      "is_error": true
    }
  ]
}
```

도구 요청이 유효하지 않거나 매개변수가 누락된 경우 Claude는 사용자에게 사과하기 전에 수정과 함께 2-3번 재시도합니다.


> 잘못된 도구 호출을 완전히 제거하려면 도구 정의에 `strict: true`를 설정하여 [엄격한 도구 사용](../02-capabilities/15-structured-outputs.md)을 사용하세요. 이렇게
> 하면 도구 입력이 항상 스키마와 정확히 일치하여 누락된 매개변수와 타입 불일치를 방지할 수 있습니다.
</details>
<details>
<summary><search_quality_reflection> 태그</summary>

Claude가 \<search_quality_reflection> 태그로 검색 품질을 반영하지 않도록 하려면 프롬프트에 "반환된 검색 결과의 품질을 응답에서 반영하지 마세요"를 추가하세요.
</details>
<details>
<summary>서버 도구 오류</summary>

서버 도구에 오류가 발생하면(예: 웹 검색의 네트워크 문제) Claude는 이러한 오류를 투명하게 처리하고 사용자에게 대안 응답이나 설명을 제공하려고 시도합니다. 클라이언트 도구와 달리 서버 도구에 대한
`is_error` 결과를 처리할 필요가 없습니다.

웹 검색의 경우 가능한 오류 코드는 다음과 같습니다:

- `too_many_requests`: 속도 제한 초과
- `invalid_input`: 잘못된 검색 쿼리 매개변수
- `max_uses_exceeded`: 최대 웹 검색 도구 사용 초과
- `query_too_long`: 쿼리가 최대 길이를 초과함
- `unavailable`: 내부 오류 발생

</details>
<details>
<summary>병렬 도구 호출이 작동하지 않음</summary>

예상대로 Claude가 병렬 도구 호출을 하지 않는 경우 다음과 같은 일반적인 문제를 확인하세요:

**1. 잘못된 도구 결과 형식**

가장 일반적인 문제는 대화 기록에서 도구 결과를 잘못 형식화하는 것입니다. 이는 Claude가 병렬 호출을 피하도록 "학습"시킵니다.

특히 병렬 도구 사용의 경우:

- ❌ **잘못됨**: 각 도구 결과에 대해 별도의 사용자 메시지 전송
- ✅ **올바름**: 모든 도구 결과는 단일 사용자 메시지에 있어야 함

```json
// ❌ 이것은 병렬 도구 사용을 줄입니다
[
  {"role": "assistant", "content": [tool_use_1, tool_use_2]},
  {"role": "user", "content": [tool_result_1]},
  {"role": "user", "content": [tool_result_2]}  // 별도의 메시지
]

// ✅ 이것은 병렬 도구 사용을 유지합니다
[
  {"role": "assistant", "content": [tool_use_1, tool_use_2]},
  {"role": "user", "content": [tool_result_1, tool_result_2]}  // 단일 메시지
]
```

기타 형식 규칙은 [위의 일반 형식 요구사항](#handling-tool-use-and-tool-result-content-blocks)을 참조하세요.

**2. 약한 프롬프팅**

기본 프롬프팅이 충분하지 않을 수 있습니다. 더 강한 표현을 사용하세요:

```text
<use_parallel_tool_calls>
최대 효율을 위해 여러 독립적인 작업을 수행할 때마다
순차적이 아닌 동시에 관련된 모든 도구를 호출하세요.
가능한 한 병렬로 도구를 호출하는 것을 우선시하세요.
</use_parallel_tool_calls>
```

**3. 병렬 도구 사용 측정**

병렬 도구 호출이 작동하는지 확인하려면:

```python
# 도구 호출 메시지당 평균 도구 계산
tool_call_messages = [msg for msg in messages if any(
    block.type == "tool_use" for block in msg.content
)]
total_tool_calls = sum(
    len([b for b in msg.content if b.type == "tool_use"])
    for msg in tool_call_messages
)
avg_tools_per_message = total_tool_calls / len(tool_call_messages)
print(f"메시지당 평균 도구: {avg_tools_per_message}")
# 병렬 호출이 작동하는 경우 > 1.0이어야 함
```

**4. 모델별 동작**

- Claude Opus 4.5, Opus 4.1, Sonnet 4: 최소한의 프롬프팅으로 병렬 도구 사용에 뛰어남
- Claude Sonnet 3.7: 더 강한 프롬프팅 또는
  `token-efficient-tools-2025-02-19` [베타 헤더](https://platform.claude.com/docs/en/api/beta-headers)가 필요할 수
  있습니다. [Claude 4로 업그레이드](https://platform.claude.com/docs/en/about-claude/models/migrating-to-claude-4)를 고려하세요.
- Claude Haiku: 명시적 프롬프팅 없이는 병렬 도구를 사용할 가능성이 낮음

</details>

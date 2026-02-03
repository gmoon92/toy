# [토큰 카운팅](https://platform.claude.com/docs/en/build-with-claude/token-counting)

---

토큰 카운팅을 사용하면 메시지를 Claude에게 전송하기 전에 해당 메시지의 토큰 수를 파악할 수 있어, 프롬프트와 사용량에 대한 정보에 기반한 의사결정을 할 수 있습니다. 토큰 카운팅을 통해 다음을 수행할 수 있습니다:
- 요청 제한 및 비용을 사전에 관리
- 스마트한 모델 라우팅 결정 수행
- 특정 길이에 맞게 프롬프트 최적화
---

## 메시지 토큰 카운트 방법

[토큰 카운팅](/docs/en/api/messages-count-tokens) 엔드포인트는 메시지 생성을 위한 구조화된 입력 목록과 동일한 형식을 허용하며, 시스템 프롬프트, [도구](../03-tools/01-overview.md), [이미지](../02-capabilities/11-vision.md), [PDF](../02-capabilities/12-pdf-support.md)를 지원합니다. 응답에는 총 입력 토큰 수가 포함됩니다.


> 토큰 카운트는 **추정치**로 간주되어야 합니다. 일부 경우에는 메시지 생성 시 사용되는 실제 입력 토큰 수가 소량 차이날 수 있습니다.
>
> 토큰 카운트에는 시스템 최적화를 위해 Anthropic에서 자동으로 추가한 토큰이 포함될 수 있습니다. **시스템에서 추가한 토큰에 대해서는 요금이 청구되지 않습니다**. 요금은 귀하의 콘텐츠에 대해서만 반영됩니다.


### 지원 모델
모든 [활성 모델](/docs/en/about-claude/models/overview)이 토큰 카운팅을 지원합니다.

### 기본 메시지의 토큰 카운트

<CodeGroup>

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.messages.count_tokens(
    model="claude-sonnet-4-5",
    system="You are a scientist",
    messages=[{
        "role": "user",
        "content": "Hello, Claude"
    }],
)

print(response.json())
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const client = new Anthropic();

const response = await client.messages.countTokens({
  model: 'claude-sonnet-4-5',
  system: 'You are a scientist',
  messages: [{
    role: 'user',
    content: 'Hello, Claude'
  }]
});

console.log(response);
```

```bash Shell
curl https://api.anthropic.com/v1/messages/count_tokens \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "content-type: application/json" \
    --header "anthropic-version: 2023-06-01" \
    --data '{
      "model": "claude-sonnet-4-5",
      "system": "You are a scientist",
      "messages": [{
        "role": "user",
        "content": "Hello, Claude"
      }]
    }'
```

```java Java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCountTokensParams;
import com.anthropic.models.messages.MessageTokensCount;
import com.anthropic.models.messages.Model;

public class CountTokensExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        MessageCountTokensParams params = MessageCountTokensParams.builder()
                .model(Model.CLAUDE_SONNET_4_20250514)
                .system("You are a scientist")
                .addUserMessage("Hello, Claude")
                .build();

        MessageTokensCount count = client.messages().countTokens(params);
        System.out.println(count);
    }
}
```
</CodeGroup>

```json JSON
{ "input_tokens": 14 }
```

### 도구를 사용하는 메시지의 토큰 카운트


> [서버 도구](../03-tools/01-overview.md) 토큰 카운트는 첫 번째 샘플링 호출에만 적용됩니다.


<CodeGroup>

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.messages.count_tokens(
    model="claude-sonnet-4-5",
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
    messages=[{"role": "user", "content": "What's the weather like in San Francisco?"}]
)

print(response.json())
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const client = new Anthropic();

const response = await client.messages.countTokens({
  model: 'claude-sonnet-4-5',
  tools: [
    {
      name: "get_weather",
      description: "Get the current weather in a given location",
      input_schema: {
        type: "object",
        properties: {
          location: {
            type: "string",
            description: "The city and state, e.g. San Francisco, CA",
          }
        },
        required: ["location"],
      }
    }
  ],
  messages: [{ role: "user", content: "What's the weather like in San Francisco?" }]
});

console.log(response);
```

```bash Shell
curl https://api.anthropic.com/v1/messages/count_tokens \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "content-type: application/json" \
    --header "anthropic-version: 2023-06-01" \
    --data '{
      "model": "claude-sonnet-4-5",
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
          "content": "What'\''s the weather like in San Francisco?"
        }
      ]
    }'
```

```java Java
import java.util.List;
import java.util.Map;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.MessageCountTokensParams;
import com.anthropic.models.messages.MessageTokensCount;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.Tool.InputSchema;

public class CountTokensWithToolsExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        InputSchema schema = InputSchema.builder()
                .properties(JsonValue.from(Map.of(
                        "location", Map.of(
                                "type", "string",
                                "description", "The city and state, e.g. San Francisco, CA"
                        )
                )))
                .putAdditionalProperty("required", JsonValue.from(List.of("location")))
                .build();

        MessageCountTokensParams params = MessageCountTokensParams.builder()
                .model(Model.CLAUDE_SONNET_4_20250514)
                .addTool(Tool.builder()
                        .name("get_weather")
                        .description("Get the current weather in a given location")
                        .inputSchema(schema)
                        .build())
                .addUserMessage("What's the weather like in San Francisco?")
                .build();

        MessageTokensCount count = client.messages().countTokens(params);
        System.out.println(count);
    }
}
```
</CodeGroup>

```json JSON
{ "input_tokens": 403 }
```

### 이미지를 포함한 메시지의 토큰 카운트

<CodeGroup>
```bash Shell
#!/bin/sh

IMAGE_URL="https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg"
IMAGE_MEDIA_TYPE="image/jpeg"
IMAGE_BASE64=$(curl "$IMAGE_URL" | base64)

curl https://api.anthropic.com/v1/messages/count_tokens \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data \
'{
    "model": "claude-sonnet-4-5",
    "messages": [
        {"role": "user", "content": [
            {"type": "image", "source": {
                "type": "base64",
                "media_type": "'$IMAGE_MEDIA_TYPE'",
                "data": "'$IMAGE_BASE64'"
            }},
            {"type": "text", "text": "Describe this image"}
        ]}
    ]
}'
```

```python Python
import anthropic
import base64
import httpx

image_url = "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg"
image_media_type = "image/jpeg"
image_data = base64.standard_b64encode(httpx.get(image_url).content).decode("utf-8")

client = anthropic.Anthropic()

response = client.messages.count_tokens(
    model="claude-sonnet-4-5",
    messages=[
        {
            "role": "user",
            "content": [
                {
                    "type": "image",
                    "source": {
                        "type": "base64",
                        "media_type": image_media_type,
                        "data": image_data,
                    },
                },
                {
                    "type": "text",
                    "text": "Describe this image"
                }
            ],
        }
    ],
)
print(response.json())
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

const image_url = "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg"
const image_media_type = "image/jpeg"
const image_array_buffer = await ((await fetch(image_url)).arrayBuffer());
const image_data = Buffer.from(image_array_buffer).toString('base64');

const response = await anthropic.messages.countTokens({
  model: 'claude-sonnet-4-5',
  messages: [
    {
      "role": "user",
      "content": [
        {
          "type": "image",
          "source": {
            "type": "base64",
            "media_type": image_media_type,
            "data": image_data,
          },
        }
      ],
    },
    {
      "type": "text",
      "text": "Describe this image"
    }
  ]
});
console.log(response);
```

```java Java
import java.util.Base64;
import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.MessageCountTokensParams;
import com.anthropic.models.messages.MessageTokensCount;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlockParam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CountTokensImageExample {

    public static void main(String[] args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg";
        String imageMediaType = "image/jpeg";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .build();
        byte[] imageBytes = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
        String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);

        ContentBlockParam imageBlock = ContentBlockParam.ofImage(
                ImageBlockParam.builder()
                        .source(Base64ImageSource.builder()
                                .mediaType(Base64ImageSource.MediaType.IMAGE_JPEG)
                                .data(imageBase64)
                                .build())
                        .build());

        ContentBlockParam textBlock = ContentBlockParam.ofText(
                TextBlockParam.builder()
                        .text("Describe this image")
                        .build());

        MessageCountTokensParams params = MessageCountTokensParams.builder()
                .model(Model.CLAUDE_SONNET_4_20250514)
                .addUserMessageOfBlockParams(List.of(imageBlock, textBlock))
                .build();

        MessageTokensCount count = client.messages().countTokens(params);
        System.out.println(count);
    }
}
```
</CodeGroup>

```json JSON
{ "input_tokens": 1551 }
```

### 확장 사고(Extended Thinking)를 사용하는 메시지의 토큰 카운트


> 확장 사고를 사용할 때 컨텍스트 윈도우가 어떻게 계산되는지에 대한 자세한 내용은 [여기](../02-capabilities/03-extended-thinking.md)를 참조하세요.
> - **이전** 어시스턴트 턴의 사고 블록은 무시되며 입력 토큰에 포함되지 **않습니다**.
> - **현재** 어시스턴트 턴의 사고는 입력 토큰에 포함**됩니다**.


<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages/count_tokens \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "content-type: application/json" \
    --header "anthropic-version: 2023-06-01" \
    --data '{
      "model": "claude-sonnet-4-5",
      "thinking": {
        "type": "enabled",
        "budget_tokens": 16000
      },
      "messages": [
        {
          "role": "user",
          "content": "Are there an infinite number of prime numbers such that n mod 4 == 3?"
        },
        {
          "role": "assistant",
          "content": [
            {
              "type": "thinking",
              "thinking": "This is a nice number theory question. Lets think about it step by step...",
              "signature": "EuYBCkQYAiJAgCs1le6/Pol5Z4/JMomVOouGrWdhYNsH3ukzUECbB6iWrSQtsQuRHJID6lWV..."
            },
            {
              "type": "text",
              "text": "Yes, there are infinitely many prime numbers p such that p mod 4 = 3..."
            }
          ]
        },
        {
          "role": "user",
          "content": "Can you write a formal proof?"
        }
      ]
    }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.messages.count_tokens(
    model="claude-sonnet-4-5",
    thinking={
        "type": "enabled",
        "budget_tokens": 16000
    },
    messages=[
        {
            "role": "user",
            "content": "Are there an infinite number of prime numbers such that n mod 4 == 3?"
        },
        {
            "role": "assistant",
            "content": [
                {
                    "type": "thinking",
                    "thinking": "This is a nice number theory question. Let's think about it step by step...",
                    "signature": "EuYBCkQYAiJAgCs1le6/Pol5Z4/JMomVOouGrWdhYNsH3ukzUECbB6iWrSQtsQuRHJID6lWV..."
                },
                {
                  "type": "text",
                  "text": "Yes, there are infinitely many prime numbers p such that p mod 4 = 3..."
                }
            ]
        },
        {
            "role": "user",
            "content": "Can you write a formal proof?"
        }
    ]
)

print(response.json())
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const client = new Anthropic();

const response = await client.messages.countTokens({
  model: 'claude-sonnet-4-5',
  thinking: {
    'type': 'enabled',
    'budget_tokens': 16000
  },
  messages: [
    {
      'role': 'user',
      'content': 'Are there an infinite number of prime numbers such that n mod 4 == 3?'
    },
    {
      'role': 'assistant',
      'content': [
        {
          'type': 'thinking',
          'thinking': "This is a nice number theory question. Let's think about it step by step...",
          'signature': 'EuYBCkQYAiJAgCs1le6/Pol5Z4/JMomVOouGrWdhYNsH3ukzUECbB6iWrSQtsQuRHJID6lWV...'
        },
        {
          'type': 'text',
          'text': 'Yes, there are infinitely many prime numbers p such that p mod 4 = 3...',
        }
      ]
    },
    {
      'role': 'user',
      'content': 'Can you write a formal proof?'
    }
  ]
});

console.log(response);
```

```java Java
import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.MessageCountTokensParams;
import com.anthropic.models.messages.MessageTokensCount;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ThinkingBlockParam;

public class CountTokensThinkingExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        List<ContentBlockParam> assistantBlocks = List.of(
                ContentBlockParam.ofThinking(ThinkingBlockParam.builder()
                        .thinking("This is a nice number theory question. Let's think about it step by step...")
                        .signature("EuYBCkQYAiJAgCs1le6/Pol5Z4/JMomVOouGrWdhYNsH3ukzUECbB6iWrSQtsQuRHJID6lWV...")
                        .build()),
                ContentBlockParam.ofText(TextBlockParam.builder()
                        .text("Yes, there are infinitely many prime numbers p such that p mod 4 = 3...")
                        .build())
        );

        MessageCountTokensParams params = MessageCountTokensParams.builder()
                .model(Model.CLAUDE_SONNET_4_20250514)
                .enabledThinking(16000)
                .addUserMessage("Are there an infinite number of prime numbers such that n mod 4 == 3?")
                .addAssistantMessageOfBlockParams(assistantBlocks)
                .addUserMessage("Can you write a formal proof?")
                .build();

        MessageTokensCount count = client.messages().countTokens(params);
        System.out.println(count);
    }
}
```
</CodeGroup>

```json JSON
{ "input_tokens": 88 }
```

### PDF를 포함한 메시지의 토큰 카운트


> 토큰 카운팅은 Messages API와 동일한 [제한 사항](../02-capabilities/12-pdf-support.md)을 가진 PDF를 지원합니다.


<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages/count_tokens \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "content-type: application/json" \
    --header "anthropic-version: 2023-06-01" \
    --data '{
      "model": "claude-sonnet-4-5",
      "messages": [{
        "role": "user",
        "content": [
          {
            "type": "document",
            "source": {
              "type": "base64",
              "media_type": "application/pdf",
              "data": "'$(base64 -i document.pdf)'"
            }
          },
          {
            "type": "text",
            "text": "Please summarize this document."
          }
        ]
      }]
    }'
```

```python Python
import base64
import anthropic

client = anthropic.Anthropic()

with open("document.pdf", "rb") as pdf_file:
    pdf_base64 = base64.standard_b64encode(pdf_file.read()).decode("utf-8")

response = client.messages.count_tokens(
    model="claude-sonnet-4-5",
    messages=[{
        "role": "user",
        "content": [
            {
                "type": "document",
                "source": {
                    "type": "base64",
                    "media_type": "application/pdf",
                    "data": pdf_base64
                }
            },
            {
                "type": "text",
                "text": "Please summarize this document."
            }
        ]
    }]
)

print(response.json())
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';
import { readFileSync } from 'fs';

const client = new Anthropic();

const pdfBase64 = readFileSync('document.pdf', { encoding: 'base64' });

const response = await client.messages.countTokens({
  model: 'claude-sonnet-4-5',
  messages: [{
    role: 'user',
    content: [
      {
        type: 'document',
        source: {
          type: 'base64',
          media_type: 'application/pdf',
          data: pdfBase64
        }
      },
      {
        type: 'text',
        text: 'Please summarize this document.'
      }
    ]
  }]
});

console.log(response);
```

```java Java
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64PdfSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.MessageCountTokensParams;
import com.anthropic.models.messages.MessageTokensCount;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlockParam;

public class CountTokensPdfExample {

    public static void main(String[] args) throws Exception {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        byte[] fileBytes = Files.readAllBytes(Path.of("document.pdf"));
        String pdfBase64 = Base64.getEncoder().encodeToString(fileBytes);

        ContentBlockParam documentBlock = ContentBlockParam.ofDocument(
                DocumentBlockParam.builder()
                        .source(Base64PdfSource.builder()
                                .mediaType(Base64PdfSource.MediaType.APPLICATION_PDF)
                                .data(pdfBase64)
                                .build())
                        .build());

        ContentBlockParam textBlock = ContentBlockParam.ofText(
                TextBlockParam.builder()
                        .text("Please summarize this document.")
                        .build());

        MessageCountTokensParams params = MessageCountTokensParams.builder()
                .model(Model.CLAUDE_SONNET_4_20250514)
                .addUserMessageOfBlockParams(List.of(documentBlock, textBlock))
                .build();

        MessageTokensCount count = client.messages().countTokens(params);
        System.out.println(count);
    }
}
```
</CodeGroup>

```json JSON
{ "input_tokens": 2188 }
```

---

## 가격 및 요청 제한

토큰 카운팅은 **무료로 사용**할 수 있지만, [사용 등급](/docs/en/api/rate-limits#rate-limits)에 따라 분당 요청 수 제한이 적용됩니다. 더 높은 제한이 필요한 경우 [Claude Console](/settings/limits)을 통해 영업팀에 문의하세요.

| 사용 등급 | 분당 요청 수(RPM) |
|-----------|-------------------|
| 1         | 100               |
| 2         | 2,000             |
| 3         | 4,000             |
| 4         | 8,000             |


> 토큰 카운팅과 메시지 생성은 별도의 독립적인 요청 제한을 가지고 있습니다. 한쪽의 사용이 다른 쪽의 제한에 영향을 주지 않습니다.


---
## FAQ

  <details>
<summary>토큰 카운팅은 프롬프트 캐싱을 사용하나요?</summary>

아니요, 토큰 카운팅은 캐싱 로직을 사용하지 않고 추정치를 제공합니다. 토큰 카운팅 요청에 `cache_control` 블록을 제공할 수 있지만, 프롬프트 캐싱은 실제 메시지 생성 시에만 발생합니다.
</details>

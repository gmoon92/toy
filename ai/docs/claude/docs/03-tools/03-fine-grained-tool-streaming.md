# [세밀한 도구 스트리밍](https://platform.claude.com/docs/en/agents-and-tools/tool-use/fine-grained-tool-streaming)

---

도구 사용이 이제 매개변수 값에 대한 세밀한 [스트리밍](../02-capabilities/05-streaming-messages.md)을 지원합니다. 이를 통해 개발자는 버퍼링이나 JSON 검증 없이 도구 사용 매개변수를 스트리밍할 수 있어, 대용량 매개변수 수신을 시작하는 지연 시간이 단축됩니다.

세밀한 도구 스트리밍은 Claude API, AWS Bedrock, Google Cloud의 Vertex AI, 그리고 Microsoft Foundry를 통해 사용할 수 있습니다.


> 세밀한 도구 스트리밍은 베타 기능입니다. 프로덕션에서 사용하기 전에 응답을 평가해 주세요.
>
> 모델 응답의 품질, API 자체, 또는 문서의 품질에 대한 피드백을 제공하려면 [이 양식](https://forms.gle/D4Fjr7GvQRzfTZT96)을 사용해 주세요. 여러분의 의견을 기다리고 있습니다!



> 세밀한 도구 스트리밍을 사용할 때는 잠재적으로 유효하지 않거나 부분적인 JSON 입력을 받을 수 있습니다. 코드에서 이러한 엣지 케이스를 고려해 주세요.


## 세밀한 도구 스트리밍 사용 방법

이 베타 기능을 사용하려면 도구 사용 요청에 베타 헤더 `fine-grained-tool-streaming-2025-05-14`를 추가하고 스트리밍을 켜면 됩니다.

다음은 API에서 세밀한 도구 스트리밍을 사용하는 예시입니다:

<CodeGroup>

  ```bash Shell
  curl https://api.anthropic.com/v1/messages \
    -H "content-type: application/json" \
    -H "x-api-key: $ANTHROPIC_API_KEY" \
    -H "anthropic-version: 2023-06-01" \
    -H "anthropic-beta: fine-grained-tool-streaming-2025-05-14" \
    -d '{
      "model": "claude-sonnet-4-5",
      "max_tokens": 65536,
      "tools": [
        {
          "name": "make_file",
          "description": "Write text to a file",
          "input_schema": {
            "type": "object",
            "properties": {
              "filename": {
                "type": "string",
                "description": "The filename to write text to"
              },
              "lines_of_text": {
                "type": "array",
                "description": "An array of lines of text to write to the file"
              }
            },
            "required": ["filename", "lines_of_text"]
          }
        }
      ],
      "messages": [
        {
          "role": "user",
          "content": "Can you write a long poem and make a file called poem.txt?"
        }
      ],
      "stream": true
    }' | jq '.usage'
  ```

  ```python Python
  import anthropic

  client = anthropic.Anthropic()

  response = client.beta.messages.stream(
      max_tokens=65536,
      model="claude-sonnet-4-5",
      tools=[{
        "name": "make_file",
        "description": "Write text to a file",
        "input_schema": {
          "type": "object",
          "properties": {
            "filename": {
              "type": "string",
              "description": "The filename to write text to"
            },
            "lines_of_text": {
              "type": "array",
              "description": "An array of lines of text to write to the file"
            }
          },
          "required": ["filename", "lines_of_text"]
        }
      }],
      messages=[{
        "role": "user",
        "content": "Can you write a long poem and make a file called poem.txt?"
      }],
      betas=["fine-grained-tool-streaming-2025-05-14"]
  )

  print(response.usage)
  ```

  ```typescript TypeScript
  import Anthropic from '@anthropic-ai/sdk';

  const anthropic = new Anthropic();

  const message = await anthropic.beta.messages.stream({
    model: "claude-sonnet-4-5",
    max_tokens: 65536,
    tools: [{
      "name": "make_file",
      "description": "Write text to a file",
      "input_schema": {
        "type": "object",
        "properties": {
          "filename": {
            "type": "string",
            "description": "The filename to write text to"
          },
          "lines_of_text": {
            "type": "array",
            "description": "An array of lines of text to write to the file"
          }
        },
        "required": ["filename", "lines_of_text"]
      }
    }],
    messages: [{
      role: "user",
      content: "Can you write a long poem and make a file called poem.txt?"
    }],
    betas: ["fine-grained-tool-streaming-2025-05-14"]
  });

  console.log(message.usage);
  ```
</CodeGroup>

이 예시에서 세밀한 도구 스트리밍은 Claude가 `lines_of_text` 매개변수가 유효한 JSON인지 검증하기 위해 버퍼링하지 않고도 긴 시의 줄들을 `make_file` 도구 호출로 스트리밍할 수 있게 합니다. 이는 전체 매개변수가 버퍼링되고 검증될 때까지 기다릴 필요 없이, 매개변수가 도착하는 대로 스트림을 볼 수 있음을 의미합니다.


> 세밀한 도구 스트리밍을 사용하면 도구 사용 청크가 더 빠르게 스트리밍을 시작하며, 종종 더 길고 단어 중단이 적습니다. 이는 청킹 동작의 차이 때문입니다.
>
> 예시:
>
> 세밀한 스트리밍 없이 (15초 지연):
> ```
> Chunk 1: '{"'
> Chunk 2: 'query": "Ty'
> Chunk 3: 'peScri'
> Chunk 4: 'pt 5.0 5.1 '
> Chunk 5: '5.2 5'
> Chunk 6: '.3'
> Chunk 8: ' new f'
> Chunk 9: 'eatur'
> ...
> ```
>
> 세밀한 스트리밍 사용 시 (3초 지연):
> ```
> Chunk 1: '{"query": "TypeScript 5.0 5.1 5.2 5.3'
> Chunk 2: ' new features comparison'
> ```



> 세밀한 스트리밍은 버퍼링이나 JSON 검증 없이 매개변수를 전송하기 때문에, 결과 스트림이 유효한 JSON 문자열로 완성된다는 보장이 없습니다.
> 특히 [중지 이유](/docs/en/build-with-claude/handling-stop-reasons) `max_tokens`에 도달하면 스트림이 매개변수 중간에 끝나고 불완전할 수 있습니다. 일반적으로 `max_tokens`에 도달했을 때를 처리하기 위한 특정 지원을 작성해야 합니다.


## 도구 응답에서 유효하지 않은 JSON 처리

세밀한 도구 스트리밍을 사용할 때 모델로부터 유효하지 않거나 불완전한 JSON을 받을 수 있습니다. 이 유효하지 않은 JSON을 오류 응답 블록으로 모델에 다시 전달해야 하는 경우, 적절한 처리를 보장하기 위해 JSON 객체(합리적인 키와 함께)로 감쌀 수 있습니다. 예를 들어:

```json
{
  "INVALID_JSON": "<your invalid json string>"
}
```

이 접근 방식은 모델이 콘텐츠가 유효하지 않은 JSON임을 이해하도록 도우면서 디버깅 목적으로 원본 잘못된 데이터를 보존합니다.


> 유효하지 않은 JSON을 감쌀 때는 래퍼 객체의 유효한 JSON 구조를 유지하기 위해 유효하지 않은 JSON 문자열의 따옴표나 특수 문자를 적절히 이스케이프 처리해야 합니다.


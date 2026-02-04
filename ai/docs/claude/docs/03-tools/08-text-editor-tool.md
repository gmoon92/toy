# [텍스트 에디터 도구](https://platform.claude.com/docs/en/agents-and-tools/tool-use/text-editor-tool)

---

Claude는 Anthropic이 정의한 텍스트 에디터 도구를 사용하여 텍스트 파일을 보고 수정할 수 있으며, 코드나 다른 텍스트 문서를 디버그하고, 수정하고, 개선하는 데 도움을 줄 수 있습니다. 이를 통해 Claude가 파일과 직접 상호작용하여 단순히 변경사항을 제안하는 것이 아니라 실질적인 지원을 제공할 수 있습니다.

## 모델 호환성

| 모델 | 도구 버전 |
|-------|--------------|
| Claude 4.x 모델 | `text_editor_20250728` |
| Claude Sonnet 3.7 ([더 이상 사용되지 않음](https://platform.claude.com/docs/en/about-claude/model-deprecations)) | `text_editor_20250124` |


> Claude 4 모델용 `text_editor_20250728` 도구는 `undo_edit` 명령을 포함하지 않습니다. 이 기능이 필요한 경우 Claude Sonnet 3.7([더 이상 사용되지 않음](https://platform.claude.com/docs/en/about-claude/model-deprecations))을 사용해야 합니다.



> 이전 도구 버전은 새로운 모델과의 하위 호환성이 보장되지 않습니다. 항상 모델 버전에 해당하는 도구 버전을 사용하세요.


## 텍스트 에디터 도구를 사용하는 경우

텍스트 에디터 도구를 사용하는 몇 가지 예시는 다음과 같습니다.
- **코드 디버깅**: Claude가 구문 오류부터 로직 문제까지 코드의 버그를 식별하고 수정하도록 합니다.
- **코드 리팩토링**: Claude가 대상 편집을 통해 코드 구조, 가독성 및 성능을 개선하도록 합니다.
- **문서 생성**: Claude에게 코드베이스에 docstring, 주석 또는 README 파일을 추가하도록 요청합니다.
- **테스트 생성**: Claude가 구현에 대한 이해를 바탕으로 코드에 대한 단위 테스트를 생성하도록 합니다.

## 텍스트 에디터 도구 사용하기

<Tabs>
<Tab title="Claude 4">
Messages API를 사용하여 텍스트 에디터 도구(`str_replace_based_edit_tool`이라는 이름)를 Claude에 제공합니다.

대용량 파일을 볼 때 잘림을 제어하기 위해 선택적으로 `max_characters` 매개변수를 지정할 수 있습니다.


> `max_characters`는 `text_editor_20250728` 이상 버전의 텍스트 에디터 도구에서만 호환됩니다.


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
        "type": "text_editor_20250728",
        "name": "str_replace_based_edit_tool",
        "max_characters": 10000
      }
    ],
    "messages": [
      {
        "role": "user",
        "content": "There'\''s a syntax error in my primes.py file. Can you help me fix it?"
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
            "type": "text_editor_20250728",
            "name": "str_replace_based_edit_tool",
            "max_characters": 10000
        }
    ],
    messages=[
        {
            "role": "user",
            "content": "There's a syntax error in my primes.py file. Can you help me fix it?"
        }
    ]
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

const response = await anthropic.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [
    {
      type: "text_editor_20250728",
      name: "str_replace_based_edit_tool",
      max_characters: 10000
    }
  ],
  messages: [
    {
      role: "user",
      content: "There's a syntax error in my primes.py file. Can you help me fix it?"
    }
  ]
});
```

```java Java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ToolStrReplaceBasedEditTool20250728;

public class TextEditorToolExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        ToolStrReplaceBasedEditTool20250728 editorTool = ToolStrReplaceBasedEditTool20250728.builder()
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_0)
                .maxTokens(1024)
                .addTool(editorTool)
                .addUserMessage("There's a syntax error in my primes.py file. Can you help me fix it?")
                .build();

        Message message = client.messages().create(params);
    }
}
```
</CodeGroup>
</Tab>
<Tab title="Claude Sonnet 3.7">
Messages API를 사용하여 텍스트 에디터 도구(`str_replace_editor`라는 이름)를 Claude에 제공합니다.
<CodeGroup>

```bash Shell
curl https://api.anthropic.com/v1/messages \
  -H "content-type: application/json" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-3-7-sonnet-20250219",
    "max_tokens": 1024,
    "tools": [
      {
        "type": "text_editor_20250124",
        "name": "str_replace_editor"
      }
    ],
    "messages": [
      {
        "role": "user",
        "content": "There'\''s a syntax error in my primes.py file. Can you help me fix it?"
      }
    ]
  }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.messages.create(
    model="claude-3-7-sonnet-20250219",
    max_tokens=1024,
    tools=[
        {
            "type": "text_editor_20250124",
            "name": "str_replace_editor"
        }
    ],
    messages=[
        {
            "role": "user",
            "content": "There's a syntax error in my primes.py file. Can you help me fix it?"
        }
    ]
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

const response = await anthropic.messages.create({
  model: "claude-3-7-sonnet-20250219",
  max_tokens: 1024,
  tools: [
    {
      type: "text_editor_20250124",
      name: "str_replace_editor"
    }
  ],
  messages: [
    {
      role: "user",
      content: "There's a syntax error in my primes.py file. Can you help me fix it?"
    }
  ]
});
```

```java Java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ToolTextEditor20250124;

public class TextEditorToolExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        ToolTextEditor20250124 editorTool = ToolTextEditor20250124.builder()
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_3_7_SONNET_LATEST)
                .maxTokens(1024)
                .addTool(editorTool)
                .addUserMessage("There's a syntax error in my primes.py file. Can you help me fix it?")
                .build();

        Message message = client.messages().create(params);
    }
}
```
</CodeGroup>
</Tab>
</Tabs>

텍스트 에디터 도구는 다음과 같은 방식으로 사용할 수 있습니다.

<Steps>
  <Step title="Claude에 텍스트 에디터 도구와 사용자 프롬프트 제공">
    - API 요청에 텍스트 에디터 도구 포함
    - "내 코드의 구문 오류를 수정해 줄 수 있나요?"와 같이 파일을 검사하거나 수정해야 할 수 있는 사용자 프롬프트 제공
  </Step>
  <Step title="Claude가 도구를 사용하여 파일 또는 디렉토리 검사">
    - Claude가 확인이 필요한 내용을 평가하고 `view` 명령을 사용하여 파일 내용을 검사하거나 디렉토리 내용을 나열
    - API 응답에는 `view` 명령이 포함된 `tool_use` 콘텐츠 블록이 포함됨
  </Step>
  <Step title="view 명령 실행 및 결과 반환">
    - Claude의 도구 사용 요청에서 파일 또는 디렉토리 경로 추출
    - 파일 내용을 읽거나 디렉토리 내용을 나열
    - 도구 구성에 `max_characters` 매개변수가 지정된 경우 파일 내용을 해당 길이로 잘라냄
    - `tool_result` 콘텐츠 블록을 포함하는 새 `user` 메시지로 대화를 계속하여 결과를 Claude에 반환
  </Step>
  <Step title="Claude가 도구를 사용하여 파일 수정">
    - 파일 또는 디렉토리를 검사한 후 Claude는 `str_replace`와 같은 명령을 사용하여 변경하거나 `insert`를 사용하여 특정 줄 번호에 텍스트를 추가할 수 있음
    - Claude가 `str_replace` 명령을 사용하는 경우 이전 텍스트와 교체할 새 텍스트로 적절한 형식의 도구 사용 요청을 구성
  </Step>
  <Step title="편집 실행 및 결과 반환">
    - Claude의 도구 사용 요청에서 파일 경로, 이전 텍스트 및 새 텍스트 추출
    - 파일에서 텍스트 교체 수행
    - Claude에 결과 반환
  </Step>
  <Step title="Claude가 분석 및 설명 제공">
    - 파일을 검사하고 편집한 후 Claude는 발견한 내용과 수행한 변경사항에 대한 완전한 설명을 제공
  </Step>
</Steps>

### 텍스트 에디터 도구 명령

텍스트 에디터 도구는 파일을 보고 수정하기 위한 여러 명령을 지원합니다.

#### view

`view` 명령을 사용하면 Claude가 파일 내용을 검사하거나 디렉토리 내용을 나열할 수 있습니다. 전체 파일 또는 특정 줄 범위를 읽을 수 있습니다.

매개변수:
- `command`: "view"여야 함
- `path`: 볼 파일 또는 디렉토리의 경로
- `view_range` (선택 사항): 볼 시작 및 종료 줄 번호를 지정하는 두 개의 정수 배열. 줄 번호는 1부터 시작하며, 종료 줄에 대한 -1은 파일의 끝까지 읽음을 의미합니다. 이 매개변수는 파일을 볼 때만 적용되며 디렉토리에는 적용되지 않습니다.

<details>
<summary>view 명령 예시</summary>

```json
// 파일 보기 예시
{
  "type": "tool_use",
  "id": "toolu_01A09q90qw90lq917835lq9",
  "name": "str_replace_editor",
  "input": {
    "command": "view",
    "path": "primes.py"
  }
}

// 디렉토리 보기 예시
{
  "type": "tool_use",
  "id": "toolu_02B19r91rw91mr917835mr9",
  "name": "str_replace_editor",
  "input": {
    "command": "view",
    "path": "src/"
  }
}
```
</details>

#### str_replace

`str_replace` 명령을 사용하면 Claude가 파일의 특정 문자열을 새 문자열로 교체할 수 있습니다. 이는 정밀한 편집을 위해 사용됩니다.

매개변수:
- `command`: "str_replace"여야 함
- `path`: 수정할 파일의 경로
- `old_str`: 교체할 텍스트 (공백과 들여쓰기를 포함하여 정확히 일치해야 함)
- `new_str`: 이전 텍스트 대신 삽입할 새 텍스트

<details>
<summary>str_replace 명령 예시</summary>

```json
{
  "type": "tool_use",
  "id": "toolu_01A09q90qw90lq917835lq9",
  "name": "str_replace_editor",
  "input": {
    "command": "str_replace",
    "path": "primes.py",
    "old_str": "for num in range(2, limit + 1)",
    "new_str": "for num in range(2, limit + 1):"
  }
}
```
</details>

#### create

`create` 명령을 사용하면 Claude가 지정된 내용으로 새 파일을 생성할 수 있습니다.

매개변수:
- `command`: "create"여야 함
- `path`: 새 파일을 생성할 경로
- `file_text`: 새 파일에 쓸 내용

<details>
<summary>create 명령 예시</summary>

```json
{
  "type": "tool_use",
  "id": "toolu_01A09q90qw90lq917835lq9",
  "name": "str_replace_editor",
  "input": {
    "command": "create",
    "path": "test_primes.py",
    "file_text": "import unittest\nimport primes\n\nclass TestPrimes(unittest.TestCase):\n    def test_is_prime(self):\n        self.assertTrue(primes.is_prime(2))\n        self.assertTrue(primes.is_prime(3))\n        self.assertFalse(primes.is_prime(4))\n\nif __name__ == '__main__':\n    unittest.main()"
  }
}
```
</details>

#### insert

`insert` 명령을 사용하면 Claude가 파일의 특정 위치에 텍스트를 삽입할 수 있습니다.

매개변수:
- `command`: "insert"여야 함
- `path`: 수정할 파일의 경로
- `insert_line`: 텍스트를 삽입할 줄 번호 (파일 시작은 0)
- `insert_text`: 삽입할 텍스트

<details>
<summary>insert 명령 예시</summary>

```json
{
  "type": "tool_use",
  "id": "toolu_01A09q90qw90lq917835lq9",
  "name": "str_replace_editor",
  "input": {
    "command": "insert",
    "path": "primes.py",
    "insert_line": 0,
    "insert_text": "\"\"\"Module for working with prime numbers.\n\nThis module provides functions to check if a number is prime\nand to generate a list of prime numbers up to a given limit.\n\"\"\"\n"
  }
}
```
</details>

#### undo_edit

`undo_edit` 명령을 사용하면 Claude가 파일에 대한 마지막 편집을 되돌릴 수 있습니다.


> 이 명령은 Claude Sonnet 3.7([더 이상 사용되지 않음](https://platform.claude.com/docs/en/about-claude/model-deprecations))에서만 사용할 수 있습니다. `text_editor_20250728`을 사용하는 Claude 4 모델에서는 지원되지 않습니다.


매개변수:
- `command`: "undo_edit"여야 함
- `path`: 마지막 편집을 취소할 파일의 경로

<details>
<summary>undo_edit 명령 예시</summary>

```json
{
  "type": "tool_use",
  "id": "toolu_01A09q90qw90lq917835lq9",
  "name": "str_replace_editor",
  "input": {
    "command": "undo_edit",
    "path": "primes.py"
  }
}
```
</details>

### 예시: 텍스트 에디터 도구로 구문 오류 수정하기

<Tabs>
<Tab title="Claude 4">

이 예시는 Claude 4 모델이 텍스트 에디터 도구를 사용하여 Python 파일의 구문 오류를 수정하는 방법을 보여줍니다.

먼저, 애플리케이션이 텍스트 에디터 도구와 구문 오류를 수정하라는 프롬프트를 Claude에 제공합니다.

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
        "type": "text_editor_20250728",
        "name": "str_replace_based_edit_tool"
      }
    ],
    "messages": [
      {
        "role": "user",
        "content": "There'\''s a syntax error in my primes.py file. Can you help me fix it?"
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
            "type": "text_editor_20250728",
            "name": "str_replace_based_edit_tool"
        }
    ],
    messages=[
        {
            "role": "user",
            "content": "There's a syntax error in my primes.py file. Can you help me fix it?"
        }
    ]
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

const response = await anthropic.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [
    {
      type: "text_editor_20250728",
      name: "str_replace_based_edit_tool"
    }
  ],
  messages: [
    {
      role: "user",
      content: "There's a syntax error in my primes.py file. Can you help me fix it?"
    }
  ]
});
```

```java Java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ToolStrReplaceBasedEditTool20250728;

public class TextEditorToolExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        ToolStrReplaceBasedEditTool20250728 editorTool = ToolStrReplaceBasedEditTool20250728.builder()
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_0)
                .maxTokens(1024)
                .addTool(editorTool)
                .addUserMessage("There's a syntax error in my primes.py file. Can you help me fix it?")
                .build();

        Message message = client.messages().create(params);
    }
}
```
</CodeGroup>

Claude는 먼저 텍스트 에디터 도구를 사용하여 파일을 봅니다.

```json
{
  "id": "msg_01XAbCDeFgHiJkLmNoPQrStU",
  "model": "claude-sonnet-4-5",
  "stop_reason": "tool_use",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "I'll help you fix the syntax error in your primes.py file. First, let me take a look at the file to identify the issue."
    },
    {
      "type": "tool_use",
      "id": "toolu_01AbCdEfGhIjKlMnOpQrStU",
      "name": "str_replace_based_edit_tool",
      "input": {
        "command": "view",
        "path": "primes.py"
      }
    }
  ]
}
```

그런 다음 애플리케이션은 파일을 읽고 그 내용을 Claude에 반환해야 합니다.

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
        "type": "text_editor_20250728",
        "name": "str_replace_based_edit_tool"
      }
    ],
    "messages": [
      {
        "role": "user",
        "content": "There'\''s a syntax error in my primes.py file. Can you help me fix it?"
      },
      {
            "role": "assistant",
            "content": [
                {
                    "type": "text",
                    "text": "I'\''ll help you fix the syntax error in your primes.py file. First, let me take a look at the file to identify the issue."
                },
                {
                    "type": "tool_use",
                    "id": "toolu_01AbCdEfGhIjKlMnOpQrStU",
                    "name": "str_replace_based_edit_tool",
                    "input": {
                        "command": "view",
                        "path": "primes.py"
                    }
                }
            ]
        },
        {
            "role": "user",
            "content": [
                {
                    "type": "tool_result",
                    "tool_use_id": "toolu_01AbCdEfGhIjKlMnOpQrStU",
                    "content": "1: def is_prime(n):\n2:     \"\"\"Check if a number is prime.\"\"\"\n3:     if n <= 1:\n4:         return False\n5:     if n <= 3:\n6:         return True\n7:     if n % 2 == 0 or n % 3 == 0:\n8:         return False\n9:     i = 5\n10:     while i * i <= n:\n11:         if n % i == 0 or n % (i + 2) == 0:\n12:             return False\n13:         i += 6\n14:     return True\n15: \n16: def get_primes(limit):\n17:     \"\"\"Generate a list of prime numbers up to the given limit.\"\"\"\n18:     primes = []\n19:     for num in range(2, limit + 1)\n20:         if is_prime(num):\n21:             primes.append(num)\n22:     return primes\n23: \n24: def main():\n25:     \"\"\"Main function to demonstrate prime number generation.\"\"\"\n26:     limit = 100\n27:     prime_list = get_primes(limit)\n28:     print(f\"Prime numbers up to {limit}:\")\n29:     print(prime_list)\n30:     print(f\"Found {len(prime_list)} prime numbers.\")\n31: \n32: if __name__ == \"__main__\":\n33:     main()"
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
            "type": "text_editor_20250728",
            "name": "str_replace_based_edit_tool"
        }
    ],
    messages=[
        {
            "role": "user",
            "content": "There's a syntax error in my primes.py file. Can you help me fix it?"
        },
        {
            "role": "assistant",
            "content": [
                {
                    "type": "text",
                    "text": "I'll help you fix the syntax error in your primes.py file. First, let me take a look at the file to identify the issue."
                },
                {
                    "type": "tool_use",
                    "id": "toolu_01AbCdEfGhIjKlMnOpQrStU",
                    "name": "str_replace_based_edit_tool",
                    "input": {
                        "command": "view",
                        "path": "primes.py"
                    }
                }
            ]
        },
        {
            "role": "user",
            "content": [
                {
                    "type": "tool_result",
                    "tool_use_id": "toolu_01AbCdEfGhIjKlMnOpQrStU",
                    "content": "1: def is_prime(n):\n2:     \"\"\"Check if a number is prime.\"\"\"\n3:     if n <= 1:\n4:         return False\n5:     if n <= 3:\n6:         return True\n7:     if n % 2 == 0 or n % 3 == 0:\n8:         return False\n9:     i = 5\n10:     while i * i <= n:\n11:         if n % i == 0 or n % (i + 2) == 0:\n12:             return False\n13:         i += 6\n14:     return True\n15: \n16: def get_primes(limit):\n17:     \"\"\"Generate a list of prime numbers up to the given limit.\"\"\"\n18:     primes = []\n19:     for num in range(2, limit + 1)\n20:         if is_prime(num):\n21:             primes.append(num)\n22:     return primes\n23: \n24: def main():\n25:     \"\"\"Main function to demonstrate prime number generation.\"\"\"\n26:     limit = 100\n27:     prime_list = get_primes(limit)\n28:     print(f\"Prime numbers up to {limit}:\")\n29:     print(prime_list)\n30:     print(f\"Found {len(prime_list)} prime numbers.\")\n31: \n32: if __name__ == \"__main__\":\n33:     main()"
                }
            ]
        }
    ]
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

const response = await anthropic.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [
    {
      type: "text_editor_20250728",
      name: "str_replace_based_edit_tool"
    }
  ],
  messages: [
    {
      role: "user",
      content: "There's a syntax error in my primes.py file. Can you help me fix it?"
    },
    {
      role: "assistant",
      content: [
          {
              type: "text",
              text: "I'll help you fix the syntax error in your primes.py file. First, let me take a look at the file to identify the issue."
          },
          {
              type: "tool_use",
              id: "toolu_01AbCdEfGhIjKlMnOpQrStU",
              name: "str_replace_based_edit_tool",
              input: {
                  command: "view",
                  path: "primes.py"
              }
          }
      ]
    },
    {
      role: "user",
      content: [
          {
              type: "tool_result",
              tool_use_id: "toolu_01AbCdEfGhIjKlMnOpQrStU",
              content: "1: def is_prime(n):\n2:     \"\"\"Check if a number is prime.\"\"\"\n3:     if n <= 1:\n4:         return False\n5:     if n <= 3:\n6:         return True\n7:     if n % 2 == 0 or n % 3 == 0:\n8:         return False\n9:     i = 5\n10:     while i * i <= n:\n11:         if n % i == 0 or n % (i + 2) == 0:\n12:             return False\n13:         i += 6\n14:     return True\n15: \n16: def get_primes(limit):\n17:     \"\"\"Generate a list of prime numbers up to the given limit.\"\"\"\n18:     primes = []\n19:     for num in range(2, limit + 1)\n20:         if is_prime(num):\n21:             primes.append(num)\n22:     return primes\n23: \n24: def main():\n25:     \"\"\"Main function to demonstrate prime number generation.\"\"\"\n26:     limit = 100\n27:     prime_list = get_primes(limit)\n28:     print(f\"Prime numbers up to {limit}:\")\n29:     print(prime_list)\n30:     print(f\"Found {len(prime_list)} prime numbers.\")\n31: \n32: if __name__ == \"__main__\":\n33:     main()"
          }
      ]
    }
  ]
});
```

```java Java
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.ToolStrReplaceBasedEditTool20250728;

public class TextEditorToolExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        ToolStrReplaceBasedEditTool20250728 editorTool = ToolStrReplaceBasedEditTool20250728.builder()
            .build();

        MessageCreateParams params = MessageCreateParams.builder()
            .model(Model.CLAUDE_SONNET_4_0)
            .maxTokens(1024)
            .addTool(editorTool)
            .addUserMessage("There's a syntax error in my primes.py file. Can you help me fix it?")
            .build();

        Message message = client.messages().create(params);
        System.out.println(message);
    }
}
```
</CodeGroup>


> **줄 번호**
>
> 위의 예시에서 `view` 도구 결과에는 각 줄에 줄 번호가 앞에 붙은 파일 내용(예: "1: def is_prime(n):")이 포함됩니다. 줄 번호는 필수는 아니지만 `view_range` 매개변수를 사용하여 파일의 특정 섹션을 검사하고 `insert_line` 매개변수를 사용하여 정확한 위치에 내용을 추가하는 데 필수적입니다.


Claude는 구문 오류를 식별하고 `str_replace` 명령을 사용하여 수정합니다.

```json
{
  "id": "msg_01VwXyZAbCdEfGhIjKlMnO",
  "model": "claude-sonnet-4-5",
  "stop_reason": "tool_use",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "I found the syntax error in your primes.py file. In the `get_primes` function, there is a missing colon (:) at the end of the for loop line. Let me fix that for you."
    },
    {
      "type": "tool_use",
      "id": "toolu_01PqRsTuVwXyZAbCdEfGh",
      "name": "str_replace_based_edit_tool",
      "input": {
        "command": "str_replace",
        "path": "primes.py",
        "old_str": "    for num in range(2, limit + 1)",
        "new_str": "    for num in range(2, limit + 1):"
      }
    }
  ]
}
```

그런 다음 애플리케이션은 편집을 수행하고 결과를 반환해야 합니다.

<CodeGroup>
```python Python
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    tools=[
        {
            "type": "text_editor_20250728",
            "name": "str_replace_based_edit_tool"
        }
    ],
    messages=[
        # 이전 메시지들...
        {
            "role": "assistant",
            "content": [
                {
                    "type": "text",
                    "text": "I found the syntax error in your primes.py file. In the `get_primes` function, there is a missing colon (:) at the end of the for loop line. Let me fix that for you."
                },
                {
                    "type": "tool_use",
                    "id": "toolu_01PqRsTuVwXyZAbCdEfGh",
                    "name": "str_replace_based_edit_tool",
                    "input": {
                        "command": "str_replace",
                        "path": "primes.py",
                        "old_str": "    for num in range(2, limit + 1)",
                        "new_str": "    for num in range(2, limit + 1):"
                    }
                }
            ]
        },
        {
            "role": "user",
            "content": [
                {
                    "type": "tool_result",
                    "tool_use_id": "toolu_01PqRsTuVwXyZAbCdEfGh",
                    "content": "Successfully replaced text at exactly one location."
                }
            ]
        }
    ]
)
```

```typescript TypeScript
const response = await anthropic.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  tools: [
    {
      type: "text_editor_20250728",
      name: "str_replace_based_edit_tool"
    }
  ],
  messages: [
    // 이전 메시지들...
    {
      role: "assistant",
      content: [
        {
          type: "text",
          text: "I found the syntax error in your primes.py file. In the `get_primes` function, there is a missing colon (:) at the end of the for loop line. Let me fix that for you."
        },
        {
          type: "tool_use",
          id: "toolu_01PqRsTuVwXyZAbCdEfGh",
          name: "str_replace_based_edit_tool",
          input: {
            command: "str_replace",
            path: "primes.py",
            old_str: "    for num in range(2, limit + 1)",
            new_str: "    for num in range(2, limit + 1):"
          }
        }
      ]
    },
    {
      role: "user",
      content: [
        {
          type: "tool_result",
          tool_use_id: "toolu_01PqRsTuVwXyZAbCdEfGh",
          content: "Successfully replaced text at exactly one location."
        }
      ]
    }
  ]
});
```

```java Java
import java.util.List;
import java.util.Map;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.Model;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolStrReplaceBasedEditTool20250728;
import com.anthropic.models.messages.ToolUseBlockParam;

public class TextEditorConversationExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_0)
                .maxTokens(1024)
                .addTool(ToolStrReplaceBasedEditTool20250728.builder()
                        .build())
                // 이전 메시지들이 여기에 들어감
                .addAssistantMessageOfBlockParams(
                        List.of(
                                ContentBlockParam.ofText(TextBlockParam.builder()
                                        .text("I found the syntax error in your primes.py file. In the `get_primes` function, there is a missing colon (:) at the end of the for loop line. Let me fix that for you.")
                                        .build()),
                                ContentBlockParam.ofToolUse(ToolUseBlockParam.builder()
                                        .id("toolu_01PqRsTuVwXyZAbCdEfGh")
                                        .name("str_replace_based_edit_tool")
                                        .input(JsonValue.from(Map.of(
                                                "command", "str_replace",
                                                "path", "primes.py",
                                                "old_str", "    for num in range(2, limit + 1)",
                                                "new_str", "    for num in range(2, limit + 1):"
                                        )))
                                        .build()
                                )
                        )
                )
                .addUserMessageOfBlockParams(List.of(
                        ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                                .toolUseId("toolu_01PqRsTuVwXyZAbCdEfGh")
                                .content("Successfully replaced text at exactly one location.")
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

마지막으로 Claude는 수정사항에 대한 완전한 설명을 제공합니다.

```json
{
  "id": "msg_01IjKlMnOpQrStUvWxYzAb",
  "model": "claude-sonnet-4-5",
  "stop_reason": "end_turn",
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "I've fixed the syntax error in your primes.py file. The issue was in the `get_primes` function at line 19. There was a missing colon (:) at the end of the for loop line.\n\nHere's what I changed:\n\nFrom:\n```python\nfor num in range(2, limit + 1)\n```\n\nTo:\n```python\nfor num in range(2, limit + 1):\n```\n\nIn Python, a colon is required at the end of statements that start a new code block, such as for loops, if statements, function definitions, etc. Without the colon, Python raises a syntax error because it doesn't know where the code block begins.\n\nYour code should now run correctly. The function will properly generate a list of prime numbers up to the specified limit."
    }
  ]
}
```
</Tab>

<Tab title="Claude Sonnet 3.7">
이 예시는 Claude Sonnet 3.7이 텍스트 에디터 도구를 사용하여 Python 파일의 구문 오류를 수정하는 방법을 보여줍니다.

먼저, 애플리케이션이 텍스트 에디터 도구와 구문 오류를 수정하라는 프롬프트를 Claude에 제공합니다.

```python
import anthropic

client = anthropic.Anthropic()

response = client.messages.create(
    model="claude-3-7-sonnet-20250219",
    max_tokens=1024,
    tools=[
        {
            "type": "text_editor_20250124",
            "name": "str_replace_editor"
        }
    ],
    messages=[
        {
            "role": "user",
            "content": "There's a syntax error in my primes.py file. Can you help me fix it?"
        }
    ]
)

print(response)
```


> Claude Sonnet 3.7 예시는 위의 Claude 4 예시와 동일한 형식을 따르며, 동일한 도구 호출과 응답을 사용하지만 `text_editor_20250124` 도구 타입과 `str_replace_editor` 이름을 사용합니다.

</Tab>
</Tabs>

***

## 텍스트 에디터 도구 구현하기

텍스트 에디터 도구는 스키마 없는 도구로 구현됩니다. 이 도구를 사용할 때 다른 도구와 달리 입력 스키마를 제공할 필요가 없습니다. 스키마는 Claude의 모델에 내장되어 있으며 수정할 수 없습니다.

도구 타입은 모델 버전에 따라 다릅니다.
- **Claude 4**: `type: "text_editor_20250728"`
- **Claude Sonnet 3.7**: `type: "text_editor_20250124"`

<Steps>
  <Step title="에디터 구현 초기화">
    파일 읽기, 쓰기 및 수정과 같은 파일 작업을 처리하는 헬퍼 함수를 만듭니다. 실수에서 복구하기 위해 백업 기능 구현을 고려하세요.
  </Step>
  <Step title="에디터 도구 호출 처리">
    명령 타입에 따라 Claude의 도구 호출을 처리하는 함수를 만듭니다.
    ```python
    def handle_editor_tool(tool_call, model_version):
        input_params = tool_call.input
        command = input_params.get('command', '')
        file_path = input_params.get('path', '')

        if command == 'view':
            # 파일 내용 읽기 및 반환
            pass
        elif command == 'str_replace':
            # 파일에서 텍스트 교체
            pass
        elif command == 'create':
            # 새 파일 생성
            pass
        elif command == 'insert':
            # 위치에 텍스트 삽입
            pass
        elif command == 'undo_edit':
            # Claude 4 모델인지 확인
            if 'str_replace_based_edit_tool' in model_version:
                return {"error": "undo_edit command is not supported in Claude 4"}
            # Claude 3.7용 백업에서 복원
            pass
    ```
  </Step>
  <Step title="보안 조치 구현">
    유효성 검사 및 보안 검사 추가:
    - 디렉토리 순회를 방지하기 위해 파일 경로 검증
    - 변경하기 전에 백업 생성
    - 오류를 우아하게 처리
    - 권한 검사 구현
  </Step>
  <Step title="Claude의 응답 처리">
    Claude의 응답에서 도구 호출을 추출하고 처리합니다.
    ```python
    # Claude의 응답에서 도구 사용 처리
    for content in response.content:
        if content.type == "tool_use":
            # 명령에 따라 도구 실행
            result = handle_editor_tool(content)

            # Claude에 결과 반환
            tool_result = {
                "type": "tool_result",
                "tool_use_id": content.id,
                "content": result
            }
    ```
  </Step>
</Steps>


> 텍스트 에디터 도구를 구현할 때 다음 사항을 염두에 두세요.
>
> 1. **보안**: 도구가 로컬 파일시스템에 액세스할 수 있으므로 적절한 보안 조치를 구현하세요.
> 2. **백업**: 중요한 파일을 편집하기 전에 항상 백업을 생성하세요.
> 3. **검증**: 의도하지 않은 변경을 방지하기 위해 모든 입력을 검증하세요.
> 4. **고유 매칭**: 의도하지 않은 편집을 피하기 위해 교체가 정확히 한 위치와 일치하는지 확인하세요.


### 오류 처리

텍스트 에디터 도구를 사용할 때 다양한 오류가 발생할 수 있습니다. 이를 처리하는 방법에 대한 지침은 다음과 같습니다.

<details>
<summary>파일을 찾을 수 없음</summary>

Claude가 존재하지 않는 파일을 보거나 수정하려고 하면 `tool_result`에 적절한 오류 메시지를 반환합니다.

```json
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
      "content": "Error: File not found",
      "is_error": true
    }
  ]
}
```
</details>

<details>
<summary>교체를 위한 여러 일치</summary>

Claude의 `str_replace` 명령이 파일의 여러 위치와 일치하는 경우 적절한 오류 메시지를 반환합니다.

```json
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
      "content": "Error: Found 3 matches for replacement text. Please provide more context to make a unique match.",
      "is_error": true
    }
  ]
}
```
</details>

<details>
<summary>교체를 위한 일치 없음</summary>

Claude의 `str_replace` 명령이 파일의 어떤 텍스트와도 일치하지 않으면 적절한 오류 메시지를 반환합니다.

```json
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
      "content": "Error: No match found for replacement. Please check your text and try again.",
      "is_error": true
    }
  ]
}
```
</details>

<details>
<summary>권한 오류</summary>

파일 생성, 읽기 또는 수정에 권한 문제가 있는 경우 적절한 오류 메시지를 반환합니다.

```json
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_01A09q90qw90lq917835lq9",
      "content": "Error: Permission denied. Cannot write to file.",
      "is_error": true
    }
  ]
}
```
</details>

### 구현 모범 사례 따르기

<details>
<summary>명확한 컨텍스트 제공</summary>

Claude에게 코드를 수정하거나 변경하도록 요청할 때 검사해야 할 파일이나 해결해야 할 문제에 대해 구체적으로 설명하세요. 명확한 컨텍스트는 Claude가 올바른 파일을 식별하고 적절한 변경을 수행하는 데 도움이 됩니다.

**덜 유용한 프롬프트**: "내 코드를 수정해 줄 수 있나요?"

**더 나은 프롬프트**: "실행을 방해하는 primes.py 파일의 구문 오류가 있습니다. 수정해 줄 수 있나요?"
</details>

<details>
<summary>파일 경로를 명시적으로 지정</summary>

특히 여러 파일이나 다른 디렉토리의 파일을 작업하는 경우 필요할 때 파일 경로를 명확하게 지정하세요.

**덜 유용한 프롬프트**: "내 헬퍼 파일을 검토해 주세요"

**더 나은 프롬프트**: "utils/helpers.py 파일에서 성능 문제가 있는지 확인해 줄 수 있나요?"
</details>

<details>
<summary>편집하기 전에 백업 생성</summary>

특히 중요하거나 프로덕션 코드의 경우 Claude가 파일을 편집하기 전에 파일 복사본을 생성하는 백업 시스템을 애플리케이션에 구현하세요.

```python
def backup_file(file_path):
    """편집하기 전에 파일 백업 생성."""
    backup_path = f"{file_path}.backup"
    if os.path.exists(file_path):
        with open(file_path, 'r') as src, open(backup_path, 'w') as dst:
            dst.write(src.read())
```
</details>

<details>
<summary>고유한 텍스트 교체를 신중하게 처리</summary>

`str_replace` 명령은 교체할 텍스트의 정확한 일치를 요구합니다. 애플리케이션은 이전 텍스트에 대해 정확히 하나의 일치가 있는지 확인하거나 적절한 오류 메시지를 제공해야 합니다.
```python
def safe_replace(file_path, old_text, new_text):
    """정확히 하나의 일치가 있는 경우에만 텍스트 교체."""
    with open(file_path, 'r') as f:
        content = f.read()

    count = content.count(old_text)
    if count == 0:
        return "Error: No match found"
    elif count > 1:
        return f"Error: Found {count} matches"
    else:
        new_content = content.replace(old_text, new_text)
        with open(file_path, 'w') as f:
            f.write(new_content)
        return "Successfully replaced text"
```
</details>

<details>
<summary>변경사항 확인</summary>

Claude가 파일을 변경한 후 테스트를 실행하거나 코드가 여전히 예상대로 작동하는지 확인하여 변경사항을 검증하세요.
```python
def verify_changes(file_path):
    """변경 후 테스트 또는 검사 실행."""
    try:
        # Python 파일의 경우 구문 오류 확인
        if file_path.endswith('.py'):
            import ast
            with open(file_path, 'r') as f:
                ast.parse(f.read())
            return "Syntax check passed"
    except Exception as e:
        return f"Verification failed: {str(e)}"
```
</details>

---

## 가격 및 토큰 사용량

텍스트 에디터 도구는 Claude와 함께 사용되는 다른 도구와 동일한 가격 구조를 사용합니다. 사용 중인 Claude 모델에 따라 표준 입력 및 출력 토큰 가격을 따릅니다.

기본 토큰 외에도 텍스트 에디터 도구에는 다음과 같은 추가 입력 토큰이 필요합니다.

| 도구 | 추가 입력 토큰 |
| ----------------------------------------- | --------------------------------------- |
| `text_editor_20250429` (Claude 4.x) | 700 토큰 |
| `text_editor_20250124` (Claude Sonnet 3.7 ([더 이상 사용되지 않음](https://platform.claude.com/docs/en/about-claude/model-deprecations))) | 700 토큰 |

도구 가격에 대한 자세한 내용은 [도구 사용 가격](../03-tools/01-overview.md)을 참조하세요.

## 텍스트 에디터 도구를 다른 도구와 통합

텍스트 에디터 도구는 다른 Claude 도구와 함께 사용할 수 있습니다. 도구를 결합할 때 다음을 확인하세요.
- 사용 중인 모델과 도구 버전을 일치시킵니다.
- 요청에 포함된 모든 도구에 대한 추가 토큰 사용량을 고려합니다.

## 변경 로그

| 날짜 | 버전 | 변경사항 |
| ---- | ------- | ------- |
| 2025년 7월 28일 | `text_editor_20250728` | 일부 문제를 수정하고 선택적 `max_characters` 매개변수를 추가한 업데이트된 텍스트 에디터 도구 릴리스. 그 외에는 `text_editor_20250429`와 동일합니다. |
| 2025년 4월 29일 | `text_editor_20250429` | Claude 4용 텍스트 에디터 도구 릴리스. 이 버전은 `undo_edit` 명령을 제거하지만 다른 모든 기능은 유지합니다. 도구 이름은 str_replace 기반 아키텍처를 반영하도록 업데이트되었습니다. |
| 2025년 3월 13일 | `text_editor_20250124` | 독립형 텍스트 에디터 도구 문서 도입. 이 버전은 Claude Sonnet 3.7에 최적화되어 있지만 이전 버전과 동일한 기능을 가지고 있습니다. |
| 2024년 10월 22일 | `text_editor_20241022` | Claude Sonnet 3.5([사용 중단](https://platform.claude.com/docs/en/about-claude/model-deprecations))와 함께 텍스트 에디터 도구의 초기 릴리스. `view`, `create`, `str_replace`, `insert` 및 `undo_edit` 명령을 통해 파일을 보고, 생성하고, 편집하는 기능을 제공합니다. |

## 다음 단계

텍스트 에디터 도구를 보다 편리하고 강력하게 사용하는 방법에 대한 몇 가지 아이디어는 다음과 같습니다.

- **개발 워크플로우와 통합**: 텍스트 에디터 도구를 개발 도구 또는 IDE에 통합
- **코드 리뷰 시스템 생성**: Claude가 코드를 검토하고 개선하도록 설정
- **디버깅 어시스턴트 구축**: Claude가 코드의 문제를 진단하고 수정하는 데 도움을 줄 수 있는 시스템 생성
- **파일 형식 변환 구현**: Claude가 한 형식에서 다른 형식으로 파일을 변환하도록 지원
- **문서화 자동화**: Claude가 코드를 자동으로 문서화하는 워크플로우 설정

텍스트 에디터 도구를 사용하여 애플리케이션을 구축할 때 Claude의 기능을 활용하여 개발 워크플로우와 생산성을 향상시키는 방법을 기대합니다.


  
> Claude와 함께 사용할 도구 워크플로우를 구현하는 방법을 알아보세요.


  
> Claude로 셸 명령을 실행하세요.



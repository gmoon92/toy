# [Structured outputs](https://platform.claude.com/docs/en/build-with-claude/structured-outputs)

에이전트 워크플로에서 검증된 JSON 결과 얻기

---

Structured outputs는 Claude의 응답이 특정 스키마를 따르도록 제약하여, 다운스트림 처리를 위한 유효하고 파싱 가능한 출력을 보장합니다.

두 가지 보완적인 기능을 사용할 수 있습니다:
- **JSON outputs** (`output_config.format`): 특정 JSON 형식으로 Claude의 응답 받기
- **Strict tool use** (`strict: true`): 도구 이름과 입력에 대한 스키마 검증 보장

이러한 기능은 독립적으로 사용하거나 동일한 요청에서 함께 사용할 수 있습니다.


> Structured outputs는 Claude API의 Claude Sonnet 4.5, Claude Opus 4.5, Claude Haiku 4.5에서 일반적으로 사용 가능합니다.<br/> 
> Amazon Bedrock 및 Microsoft Foundry에서는 공개 베타 상태로 제공됩니다.<br/>
> **베타 버전에서 마이그레이션하시나요?** `output_format` 매개변수가 `output_config.format`으로 이동되었으며, 베타 헤더는 더 이상 필요하지 않습니다.<br/> 
> 기존 베타 헤더(`structured-outputs-2025-11-13`)와 `output_format` 매개변수는 전환 기간 동안 계속 작동합니다. 업데이트된 API 형식은 아래 코드 예제를 참조하세요.

## Structured outputs를 사용하는 이유

Structured outputs가 없으면 Claude가 애플리케이션을 중단시키는 잘못된 형식의 JSON 응답이나 유효하지 않은 도구 입력을 생성할 수 있습니다. 
신중한 프롬프트 작성에도 불구하고 다음과 같은 문제가 발생할 수 있습니다:

- 유효하지 않은 JSON 구문으로 인한 파싱 오류
- 필수 필드 누락
- 일관되지 않은 데이터 타입
- 오류 처리 및 재시도가 필요한 스키마 위반

Structured outputs는 제약된 디코딩을 통해 스키마를 준수하는 응답을 보장합니다:

- **항상 유효함**: 더 이상 `JSON.parse()` 오류 없음
- **타입 안전**: 필드 타입 및 필수 필드 보장
- **신뢰성**: 스키마 위반에 대한 재시도 불필요

## JSON outputs

JSON outputs는 Claude의 응답 형식을 제어하여, Claude가 스키마와 일치하는 유효한 JSON을 반환하도록 보장합니다. 다음과 같은 경우에 JSON outputs를 사용하세요:

- Claude의 응답 형식 제어
- 이미지나 텍스트에서 데이터 추출
- 구조화된 보고서 생성
- API 응답 형식 지정

### 빠른 시작

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages \
  -H "content-type: application/json" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
      {
        "role": "user",
        "content": "Extract the key information from this email: John Smith (john@example.com) is interested in our Enterprise plan and wants to schedule a demo for next Tuesday at 2pm."
      }
    ],
    "output_config": {
      "format": {
        "type": "json_schema",
        "schema": {
          "type": "object",
          "properties": {
            "name": {"type": "string"},
            "email": {"type": "string"},
            "plan_interest": {"type": "string"},
            "demo_requested": {"type": "boolean"}
          },
          "required": ["name", "email", "plan_interest", "demo_requested"],
          "additionalProperties": false
        }
      }
    }
  }'
```

</details>

**응답 형식:** `response.content[0].text`에서 스키마와 일치하는 유효한 JSON

```json
{
  "name": "John Smith",
  "email": "john@example.com",
  "plan_interest": "Enterprise",
  "demo_requested": true
}
```

### 작동 방식

- **JSON 스키마 정의**
  - Claude가 따라야 하는 구조를 설명하는 JSON 스키마를 생성합니다. 
  - 스키마는 일부 제한 사항이 있는 표준 JSON Schema 형식을 사용합니다([JSON Schema 제한사항](#json-schema-limitations) 참조).
- **output_config.format 매개변수 추가**
  - API 요청에 `type: "json_schema"`와 스키마 정의를 포함한 `output_config.format` 매개변수를 포함합니다.
- **응답 파싱**
  - Claude의 응답은 `response.content[0].text`에 반환되는 스키마와 일치하는 유효한 JSON입니다.

### SDK에서 JSON outputs 사용하기

Python 및 TypeScript SDK는 스키마 변환, 자동 검증, 인기 있는 스키마 라이브러리와의 통합을 포함하여 JSON outputs 작업을 더 쉽게 만드는 헬퍼를 제공합니다.

#### Pydantic 및 Zod 사용하기

Python 및 TypeScript 개발자의 경우, 원시 JSON 스키마를 작성하는 대신 Pydantic 및 Zod와 같은 친숙한 스키마 정의 도구를 사용할 수 있습니다.

<details>
<summary>Python 예시</summary>

```python
from pydantic import BaseModel
from anthropic import Anthropic, transform_schema

class ContactInfo(BaseModel):
    name: str
    email: str
    plan_interest: str
    demo_requested: bool

client = Anthropic()

# .create() 사용 - transform_schema() 필요
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[
        {
            "role": "user",
            "content": "Extract the key information from this email: John Smith (john@example.com) is interested in our Enterprise plan and wants to schedule a demo for next Tuesday at 2pm."
        }
    ],
    output_config={
        "format": {
            "type": "json_schema",
            "schema": transform_schema(ContactInfo),
        }
    }
)

print(response.content[0].text)

# .parse() 사용 - Pydantic 모델을 직접 전달 가능
response = client.messages.parse(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[
        {
            "role": "user",
            "content": "Extract the key information from this email: John Smith (john@example.com) is interested in our Enterprise plan and wants to schedule a demo for next Tuesday at 2pm."
        }
    ],
    output_format=ContactInfo,
)

print(response.parsed_output)
```

</details>

#### SDK 전용 메서드

**Python: `client.messages.parse()` (권장)**

`parse()` 메서드는 Pydantic 모델을 자동으로 변환하고, 응답을 검증하며, `parsed_output` 속성을 반환합니다.

<details>
<summary>사용 예시</summary>

```python
from pydantic import BaseModel
import anthropic

class ContactInfo(BaseModel):
    name: str
    email: str
    plan_interest: str

client = anthropic.Anthropic()

response = client.messages.parse(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[{"role": "user", "content": "..."}],
    output_format=ContactInfo,
)

# 파싱된 출력에 직접 접근
contact = response.parsed_output
print(contact.name, contact.email)
```

</details>

**Python: `transform_schema()` 헬퍼**

전송하기 전에 스키마를 수동으로 변환해야 하거나 Pydantic 생성 스키마를 수정하려는 경우에 사용합니다. 
제공된 스키마를 자동으로 변환하는 `client.messages.parse()`와 달리, 이 메서드는 변환된 스키마를 제공하여 추가로 사용자 지정할 수 있습니다.

<details>
<summary>사용 예시</summary>

```python
from anthropic import transform_schema
from pydantic import TypeAdapter

# 먼저 Pydantic 모델을 JSON 스키마로 변환한 다음 변환
schema = TypeAdapter(ContactInfo).json_schema()
schema = transform_schema(schema)
# 필요한 경우 스키마 수정
schema["properties"]["custom_field"] = {"type": "string"}

response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[{"role": "user", "content": "..."}],
    output_config={
        "format": {"type": "json_schema", "schema": schema},
    },
)
```

</details>

#### SDK 변환 작동 방식

Python 및 TypeScript SDK는 지원되지 않는 기능을 가진 스키마를 자동으로 변환합니다:

1. **지원되지 않는 제약 조건 제거** (예: `minimum`, `maximum`, `minLength`, `maxLength`)
2. **설명 업데이트** - 제약 조건이 structured outputs에서 직접 지원되지 않는 경우 제약 조건 정보를 포함 (예: "최소 100이어야 함")
3. **모든 객체에 `additionalProperties: false` 추가**
4. **지원되는 목록에 있는 문자열 형식만 필터링**
5. **원본 스키마에 대해 응답 검증** (모든 제약 조건 포함)

이는 Claude가 단순화된 스키마를 받지만 코드는 여전히 검증을 통해 모든 제약 조건을 적용함을 의미합니다.

**예시:** `minimum: 100`이 있는 Pydantic 필드는 전송된 스키마에서 일반 정수가 되지만, 설명이 "최소 100이어야 함"으로 업데이트되고 SDK는 원래 제약 조건에 대해 응답을 검증합니다.

### 일반적인 사용 사례

데이터 추출

<details>
<summary>비구조화된 텍스트에서 구조화된 데이터 추출:</summary>

```python
from pydantic import BaseModel
from typing import List

class Invoice(BaseModel):
    invoice_number: str
    date: str
    total_amount: float
    line_items: List[dict]
    customer_name: str

response = client.messages.parse(
    model="claude-sonnet-4-5",
    output_format=Invoice,
    messages=[{"role": "user", "content": f"Extract invoice data from: {invoice_text}"}]
)
```

</details>

<details>
<summary>구조화된 카테고리로 콘텐츠 분류:</summary>

```python
from pydantic import BaseModel
from typing import List

class Classification(BaseModel):
    category: str
    confidence: float
    tags: List[str]
    sentiment: str

response = client.messages.parse(
    model="claude-sonnet-4-5",
    output_format=Classification,
    messages=[{"role": "user", "content": f"Classify this feedback: {feedback_text}"}]
)
```

</details>

<details>
<summary>API 응답 형식 지정</summary>

API에 바로 사용 가능한 응답 생성:

```python
from pydantic import BaseModel
from typing import List, Optional

class APIResponse(BaseModel):
    status: str
    data: dict
    errors: Optional[List[dict]]
    metadata: dict

response = client.messages.parse(
    model="claude-sonnet-4-5",
    output_format=APIResponse,
    messages=[{"role": "user", "content": "Process this request: ..."}]
)
```

</details>

## Strict tool use

Strict tool use는 도구 매개변수를 검증하여, Claude가 올바른 타입의 인수로 함수를 호출하도록 보장합니다. 다음과 같은 경우에 strict tool use를 사용하세요:

- 도구 매개변수 검증
- 에이전트 워크플로 구축
- 타입 안전 함수 호출 보장
- 중첩된 속성이 있는 복잡한 도구 처리

### 에이전트에게 strict tool use가 중요한 이유

신뢰할 수 있는 에이전트 시스템을 구축하려면 스키마 준수가 보장되어야 합니다. strict 모드가 없으면 Claude가 호환되지 않는 타입(`2` 대신 `"2"`)이나 필수 필드 누락을 반환하여 함수를 중단시키고
런타임 오류를 발생시킬 수 있습니다.

Strict tool use는 타입 안전 매개변수를 보장합니다:

- 함수가 항상 올바른 타입의 인수를 받음
- 도구 호출 검증 및 재시도 불필요
- 대규모에서 일관되게 작동하는 프로덕션 준비 에이전트

예를 들어, 예약 시스템에 `passengers: int`가 필요하다고 가정해 봅시다. strict 모드가 없으면 Claude가 `passengers: "two"` 또는 `passengers: "2"`를 제공할 수
있습니다. `strict: true`를 사용하면 응답에 항상 `passengers: 2`가 포함됩니다.

### 빠른 시작

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages \
  -H "content-type: application/json" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
      {"role": "user", "content": "What is the weather in San Francisco?"}
    ],
    "tools": [{
      "name": "get_weather",
      "description": "Get the current weather in a given location",
      "strict": true,
      "input_schema": {
        "type": "object",
        "properties": {
          "location": {
            "type": "string",
            "description": "The city and state, e.g. San Francisco, CA"
          },
          "unit": {
            "type": "string",
            "enum": ["celsius", "fahrenheit"]
          }
        },
        "required": ["location"],
        "additionalProperties": false
      }
    }]
  }'
```

</details>

**응답 형식:** `response.content[x].input`에서 검증된 입력이 포함된 도구 사용 블록

```json
{
  "type": "tool_use",
  "name": "get_weather",
  "input": {
    "location": "San Francisco, CA"
  }
}
```

**보장 사항:**

- 도구 `input`은 `input_schema`를 엄격히 따름
- 도구 `name`은 항상 유효함 (제공된 도구 또는 서버 도구에서)

### 작동 방식

- **도구 스키마 정의**
  - 도구의 `input_schema`에 대한 JSON 스키마를 생성합니다. 
  - 스키마는 일부 제한 사항이 있는 표준 JSON Schema 형식을 사용합니다([JSON Schema 제한사항](#json-schema-limitations) 참조).
- **strict: true 추가**
  - 도구 정의에서 `name`, `description`, `input_schema`와 함께 최상위 속성으로 `"strict": true`를 설정합니다.
- **도구 호출 처리**
  - Claude가 도구를 사용할 때, tool_use 블록의 `input` 필드는 `input_schema`를 엄격히 따르며, `name`은 항상 유효합니다.

### 일반적인 사용 사례

<details>
<summary>검증된 도구 입력</summary>

도구 매개변수가 스키마와 정확히 일치하는지 확인:

```python
response = client.messages.create(
    model="claude-sonnet-4-5",
    messages=[{"role": "user", "content": "Search for flights to Tokyo"}],
    tools=[{
        "name": "search_flights",
        "strict": True,
        "input_schema": {
            "type": "object",
            "properties": {
                "destination": {"type": "string"},
                "departure_date": {"type": "string", "format": "date"},
                "passengers": {"type": "integer", "enum": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]}
            },
            "required": ["destination", "departure_date"],
            "additionalProperties": False
        }
    }]
)
```

</details>

<details>
<summary>여러 검증된 도구를 사용한 에이전트 워크플로</summary>

보장된 도구 매개변수로 신뢰할 수 있는 다단계 에이전트 구축:

```python
response = client.messages.create(
    model="claude-sonnet-4-5",
    messages=[{"role": "user", "content": "Help me plan a trip to Paris for 2 people"}],
    tools=[
        {
            "name": "search_flights",
            "strict": True,
            "input_schema": {
                "type": "object",
                "properties": {
                    "origin": {"type": "string"},
                    "destination": {"type": "string"},
                    "departure_date": {"type": "string", "format": "date"},
                    "travelers": {"type": "integer", "enum": [1, 2, 3, 4, 5, 6]}
                },
                "required": ["origin", "destination", "departure_date"],
                "additionalProperties": False
            }
        },
        {
            "name": "search_hotels",
            "strict": True,
            "input_schema": {
                "type": "object",
                "properties": {
                    "city": {"type": "string"},
                    "check_in": {"type": "string", "format": "date"},
                    "guests": {"type": "integer", "enum": [1, 2, 3, 4]}
                },
                "required": ["city", "check_in"],
                "additionalProperties": False
            }
        }
    ]
)
```

</details>

## 두 기능을 함께 사용하기

JSON outputs와 strict tool use는 서로 다른 문제를 해결하며 함께 사용할 수 있습니다:

- **JSON outputs**는 Claude의 응답 형식을 제어 (Claude가 말하는 내용)
- **Strict tool use**는 도구 매개변수를 검증 (Claude가 함수를 호출하는 방법)

결합하면 Claude가 보장된 유효 매개변수로 도구를 호출하고 구조화된 JSON 응답을 반환할 수 있습니다. 
이는 신뢰할 수 있는 도구 호출과 구조화된 최종 출력이 모두 필요한 에이전트 워크플로에 유용합니다.

<details>
<summary>Python 예시</summary>

```python
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[{"role": "user", "content": "Help me plan a trip to Paris for next month"}],
    # JSON outputs: 구조화된 응답 형식
    output_config={
        "format": {
            "type": "json_schema",
            "schema": {
                "type": "object",
                "properties": {
                    "summary": {"type": "string"},
                    "next_steps": {"type": "array", "items": {"type": "string"}}
                },
                "required": ["summary", "next_steps"],
                "additionalProperties": False
            }
        }
    },
    # Strict tool use: 보장된 도구 매개변수
    tools=[{
        "name": "search_flights",
        "strict": True,
        "input_schema": {
            "type": "object",
            "properties": {
                "destination": {"type": "string"},
                "date": {"type": "string", "format": "date"}
            },
            "required": ["destination", "date"],
            "additionalProperties": False
        }
    }]
)
```

</details>

## 중요한 고려 사항

### 문법 컴파일 및 캐싱

Structured outputs는 컴파일된 문법 아티팩트를 사용하는 제약된 샘플링을 사용합니다. 

이로 인해 알아야 할 몇 가지 성능 특성이 있습니다:
- **첫 번째 요청 지연 시간**: 특정 스키마를 처음 사용할 때 문법이 컴파일되는 동안 추가 지연 시간이 발생합니다
- **자동 캐싱**: 컴파일된 문법은 마지막 사용으로부터 24시간 동안 캐시되어 후속 요청이 훨씬 빨라집니다
- **캐시 무효화**: 다음을 변경하면 캐시가 무효화됩니다:
    - JSON 스키마 구조
    - 요청의 도구 세트 (structured outputs와 tool use를 함께 사용하는 경우)
    - `name` 또는 `description` 필드만 변경하면 캐시가 무효화되지 않습니다

### 프롬프트 수정 및 토큰 비용

Structured outputs를 사용할 때 Claude는 예상되는 출력 형식을 설명하는 추가 시스템 프롬프트를 자동으로 받습니다. 

이는 다음을 의미합니다:
- 입력 토큰 수가 약간 높아집니다
- 주입된 프롬프트는 다른 시스템 프롬프트와 마찬가지로 토큰 비용이 발생합니다
- `output_config.format` 매개변수를 변경하면 해당 대화 스레드에 대한 모든 [프롬프트 캐시](../02-capabilities/01-prompt-caching.md)가 무효화됩니다

### JSON Schema 제한사항

Structured outputs는 일부 제한 사항이 있는 표준 JSON Schema를 지원합니다. 
JSON outputs와 strict tool use 모두 이러한 제한 사항을 공유합니다.

<details>
<summary>지원되는 기능</summary>

- 모든 기본 타입: object, array, string, integer, number, boolean, null
- `enum` (문자열, 숫자, 불리언 또는 null만 - 복잡한 타입 불가)
- `const`
- `anyOf` 및 `allOf` (제한 사항 있음 - `$ref`가 있는 `allOf`는 지원되지 않음)
- `$ref`, `$def`, `definitions` (외부 `$ref`는 지원되지 않음)
- 지원되는 모든 타입에 대한 `default` 속성
- `required` 및 `additionalProperties` (객체의 경우 `false`로 설정해야 함)
- 문자열 형식: `date-time`, `time`, `date`, `duration`, `email`, `hostname`, `uri`, `ipv4`, `ipv6`, `uuid`
- 배열 `minItems` (0 및 1 값만 지원)

</details>

<details>
<summary>지원되지 않음</summary>

- 재귀 스키마
- enum 내의 복잡한 타입
- 외부 `$ref` (예: `'$ref': 'http://...'`)
- 숫자 제약 조건 (`minimum`, `maximum`, `multipleOf` 등)
- 문자열 제약 조건 (`minLength`, `maxLength`)
- 0 또는 1이 아닌 `minItems` 이외의 배열 제약 조건
- `false` 이외의 값으로 설정된 `additionalProperties`

지원되지 않는 기능을 사용하면 세부 정보가 포함된 400 오류가 발생합니다.
</details>

<details>
<summary>패턴 지원 (regex)</summary>

**지원되는 regex 기능:**

- 완전 일치 (`^...$`) 및 부분 일치
- 수량자: `*`, `+`, `?`, 단순한 `{n,m}` 케이스
- 문자 클래스: `[]`, `.`, `\d`, `\w`, `\s`
- 그룹: `(...)`

**지원되지 않음:**

- 그룹에 대한 역참조 (예: `\1`, `\2`)
- 전방 탐색/후방 탐색 어설션 (예: `(?=...)`, `(?!...)`)
- 단어 경계: `\b`, `\B`
- 큰 범위를 가진 복잡한 `{n,m}` 수량자

단순한 정규식 패턴은 잘 작동합니다. 복잡한 패턴은 400 오류를 발생시킬 수 있습니다.
</details>

> Python 및 TypeScript SDK는 지원되지 않는 기능을 제거하고 필드 설명에 제약 조건을 추가하여 지원되지 않는 기능이 있는 스키마를 자동으로 변환할 수 있습니다. 자세한
> 내용은 [SDK 전용 메서드](#sdk-specific-methods)를 참조하세요.

### 유효하지 않은 출력

Structured outputs가 대부분의 경우 스키마 준수를 보장하지만, 출력이 스키마와 일치하지 않을 수 있는 시나리오가 있습니다:

**거부** (`stop_reason: "refusal"`)

Claude는 structured outputs를 사용할 때도 안전성과 유용성 속성을 유지합니다. 

Claude가 안전상의 이유로 요청을 거부하는 경우:
- 응답에 `stop_reason: "refusal"`이 포함됩니다
- 200 상태 코드를 받습니다
- 생성된 토큰에 대해 비용이 청구됩니다
- 거부 메시지가 스키마 제약 조건보다 우선하기 때문에 출력이 스키마와 일치하지 않을 수 있습니다

**토큰 제한 도달** (`stop_reason: "max_tokens"`)

`max_tokens` 제한에 도달하여 응답이 잘린 경우:

- 응답에 `stop_reason: "max_tokens"`가 포함됩니다
- 출력이 불완전하고 스키마와 일치하지 않을 수 있습니다
- 완전한 구조화된 출력을 얻으려면 더 높은 `max_tokens` 값으로 재시도하세요

### 스키마 검증 오류

스키마가 지원되지 않는 기능을 사용하거나 너무 복잡한 경우 400 오류가 발생합니다:

**"Too many recursive definitions in schema"**

- 원인: 스키마에 과도하거나 순환적인 재귀 정의가 있음
- 해결 방법: 스키마 구조 단순화, 중첩 깊이 줄이기

**"Schema is too complex"**

- 원인: 스키마가 복잡도 제한을 초과함
- 해결 방법: 더 작은 스키마로 분할, 구조 단순화, 또는 `strict: true`로 표시된 도구 수 줄이기

유효한 스키마에 대한 지속적인 문제가 있는 경우, 스키마 정의와 함께 [지원팀에 문의](https://support.claude.com/en/articles/9015913-how-to-get-support)하세요.

## 기능 호환성

**다음과 함께 작동:**

- **[배치 처리](../02-capabilities/06-batch-processing.md)**: 50% 할인으로 대규모 structured outputs 처리
- **[토큰 계산](../02-capabilities/09-token-counting.md)**: 컴파일 없이 토큰 계산
- **[스트리밍](../02-capabilities/05-streaming-messages.md)**: 일반 응답처럼 structured outputs 스트리밍
- **결합 사용**: 동일한 요청에서 JSON outputs (`output_config.format`)와 strict tool use (`strict: true`)를 함께 사용

**호환되지 않음:**

- **[Citations](../02-capabilities/07-citations.md)**: Citations는 인용 블록을 텍스트와 인터리빙해야 하는데, 이는 엄격한 JSON 스키마 제약 조건과 충돌합니다.
  `output_config.format`과 함께 citations를 활성화하면 400 오류가 반환됩니다.
- **[Message Prefilling](../08-prompt-engineering/10-prefill-claudes-response.md)**: JSON outputs와 호환되지 않음

> **문법 범위**: 문법은 Claude의 직접 출력에만 적용되며, 도구 사용 호출, 도구 결과 또는 thinking 태그([Extended Thinking](../02-capabilities/03-extended-thinking.md) 사용 시)에는 적용되지 않습니다. 
> 문법 상태는 섹션 간에 재설정되어 Claude가 자유롭게 생각하면서도 최종 응답에서 구조화된 출력을 생성할 수 있습니다.

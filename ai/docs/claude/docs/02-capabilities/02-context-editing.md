# [Context editing](https://platform.claude.com/docs/en/build-with-claude/context-editing)

대화 컨텍스트가 증가함에 따라 컨텍스트 편집을 통해 자동으로 관리합니다.

---

## 개요

컨텍스트 편집을 사용하면 대화 컨텍스트가 증가함에 따라 자동으로 관리할 수 있어 비용을 최적화하고 컨텍스트 윈도우 제한 내에 머물 수 있습니다. 서버 측 API 전략, 클라이언트 측 SDK 기능 또는 둘 다 함께 사용할 수 있습니다.

| 접근 방식 | 실행 위치 | 전략 | 작동 방식 |
|----------|---------------|------------|--------------|
| **서버 측** | API | 도구 결과 지우기 (`clear_tool_uses_20250919`)<br/>Thinking 블록 지우기 (`clear_thinking_20251015`) | 프롬프트가 Claude에 도달하기 전에 적용됩니다. 대화 기록에서 특정 콘텐츠를 지웁니다. 각 전략은 독립적으로 구성할 수 있습니다. |
| **클라이언트 측** | SDK | 압축(Compaction) | [`tool_runner`](../03-tools/02-how-to-implement-tool-use.md)를 사용할 때 [Python 및 TypeScript SDK](https://platform.claude.com/docs/en/api/client-sdks)에서 사용 가능합니다. 요약을 생성하고 전체 대화 기록을 대체합니다. 아래 [압축](#client-side-compaction-sdk)을 참조하세요. |

## 서버 측 전략


> 컨텍스트 편집은 현재 베타 버전이며 도구 결과 지우기 및 thinking 블록 지우기를 지원합니다. 이를 활성화하려면 API 요청에 베타 헤더 `context-management-2025-06-27`을 사용하세요.
>
> 이 기능에 대한 피드백을 공유하려면 [피드백 양식](https://forms.gle/YXC2EKGMhjN1c4L88)을 통해 연락해 주세요.


### 도구 결과 지우기

`clear_tool_uses_20250919` 전략은 대화 컨텍스트가 구성된 임계값을 초과할 때 도구 결과를 지웁니다. 활성화되면 API는 시간순으로 가장 오래된 도구 결과를 자동으로 지우고, 도구 결과가 제거되었음을 Claude에게 알리는 자리 표시자 텍스트로 대체합니다. 기본적으로 도구 결과만 지워집니다. `clear_tool_inputs`를 true로 설정하여 선택적으로 도구 결과와 도구 호출(도구 사용 매개변수)을 모두 지울 수 있습니다.

### Thinking 블록 지우기

`clear_thinking_20251015` 전략은 extended thinking이 활성화된 경우 대화의 `thinking` 블록을 관리합니다. 이 전략은 이전 턴의 오래된 thinking 블록을 자동으로 지웁니다.


> **기본 동작**: `clear_thinking_20251015` 전략을 구성하지 않고 extended thinking을 활성화하면 API는 자동으로 마지막 어시스턴트 턴의 thinking 블록만 유지합니다(`keep: {type: "thinking_turns", value: 1}`과 동일).
>
> 캐시 히트를 최대화하려면 `keep: "all"`을 설정하여 모든 thinking 블록을 보존하세요.


어시스턴트 대화 턴에는 여러 콘텐츠 블록(예: 도구 사용 시) 및 여러 thinking 블록(예: [인터리브 thinking](../02-capabilities/03-extended-thinking.md))이 포함될 수 있습니다.

### 컨텍스트 편집은 서버 측에서 발생합니다

컨텍스트 편집은 프롬프트가 Claude에 도달하기 전에 서버 측에서 적용됩니다. 클라이언트 애플리케이션은 전체 수정되지 않은 대화 기록을 유지합니다. 클라이언트 상태를 편집된 버전과 동기화할 필요가 없습니다. 평소처럼 전체 대화 기록을 로컬에서 계속 관리하세요.

### 컨텍스트 편집과 프롬프트 캐싱

컨텍스트 편집과 [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md)의 상호 작용은 전략에 따라 다릅니다:

- **도구 결과 지우기**: 콘텐츠가 지워질 때 캐시된 프롬프트 접두사를 무효화합니다. 이를 고려하여 캐시 무효화를 가치 있게 만들기 위해 충분한 토큰을 지우는 것이 좋습니다. `clear_at_least` 매개변수를 사용하여 매번 지워지는 최소 토큰 수를 보장하세요. 콘텐츠가 지워질 때마다 캐시 쓰기 비용이 발생하지만 후속 요청은 새로 캐시된 접두사를 재사용할 수 있습니다.

- **Thinking 블록 지우기**: thinking 블록이 컨텍스트에 **유지**되면(지워지지 않음) 프롬프트 캐시가 보존되어 캐시 히트를 활성화하고 입력 토큰 비용을 줄입니다. thinking 블록이 **지워지면** 지우기가 발생하는 지점에서 캐시가 무효화됩니다. 캐시 성능을 우선시할지 컨텍스트 윈도우 가용성을 우선시할지에 따라 `keep` 매개변수를 구성하세요.

## 지원되는 모델

컨텍스트 편집은 다음 모델에서 사용 가능합니다:

- Claude Opus 4.5 (`claude-opus-4-5-20251101`)
- Claude Opus 4.1 (`claude-opus-4-1-20250805`)
- Claude Opus 4 (`claude-opus-4-20250514`)
- Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`)
- Claude Sonnet 4 (`claude-sonnet-4-20250514`)
- Claude Haiku 4.5 (`claude-haiku-4-5-20251001`)

## 도구 결과 지우기 사용법

도구 결과 지우기를 활성화하는 가장 간단한 방법은 전략 유형만 지정하는 것입니다. 다른 모든 [구성 옵션](#configuration-options-for-tool-result-clearing)은 기본값을 사용합니다:

<CodeGroup>

```bash cURL
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "content-type: application/json" \
    --header "anthropic-beta: context-management-2025-06-27" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [
            {
                "role": "user",
                "content": "Search for recent developments in AI"
            }
        ],
        "tools": [
            {
                "type": "web_search_20250305",
                "name": "web_search"
            }
        ],
        "context_management": {
            "edits": [
                {"type": "clear_tool_uses_20250919"}
            ]
        }
    }'
```

```python Python
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=4096,
    messages=[
        {
            "role": "user",
            "content": "Search for recent developments in AI"
        }
    ],
    tools=[
        {
            "type": "web_search_20250305",
            "name": "web_search"
        }
    ],
    betas=["context-management-2025-06-27"],
    context_management={
        "edits": [
            {"type": "clear_tool_uses_20250919"}
        ]
    }
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 4096,
  messages: [
    {
      role: "user",
      content: "Search for recent developments in AI"
    }
  ],
  tools: [
    {
      type: "web_search_20250305",
      name: "web_search"
    }
  ],
  context_management: {
    edits: [
      { type: "clear_tool_uses_20250919" }
    ]
  },
  betas: ["context-management-2025-06-27"]
});
```

</CodeGroup>

### 고급 구성

추가 매개변수로 도구 결과 지우기 동작을 사용자 정의할 수 있습니다:

<CodeGroup>

```bash cURL
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "content-type: application/json" \
    --header "anthropic-beta: context-management-2025-06-27" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [
            {
                "role": "user",
                "content": "Create a simple command line calculator app using Python"
            }
        ],
        "tools": [
            {
                "type": "text_editor_20250728",
                "name": "str_replace_based_edit_tool",
                "max_characters": 10000
            },
            {
                "type": "web_search_20250305",
                "name": "web_search",
                "max_uses": 3
            }
        ],
        "context_management": {
            "edits": [
                {
                    "type": "clear_tool_uses_20250919",
                    "trigger": {
                        "type": "input_tokens",
                        "value": 30000
                    },
                    "keep": {
                        "type": "tool_uses",
                        "value": 3
                    },
                    "clear_at_least": {
                        "type": "input_tokens",
                        "value": 5000
                    },
                    "exclude_tools": ["web_search"]
                }
            ]
        }
    }'
```

```python Python
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=4096,
    messages=[
        {
            "role": "user",
            "content": "Create a simple command line calculator app using Python"
        }
    ],
    tools=[
        {
            "type": "text_editor_20250728",
            "name": "str_replace_based_edit_tool",
            "max_characters": 10000
        },
        {
            "type": "web_search_20250305",
            "name": "web_search",
            "max_uses": 3
        }
    ],
    betas=["context-management-2025-06-27"],
    context_management={
        "edits": [
            {
                "type": "clear_tool_uses_20250919",
                # 임계값이 초과될 때 지우기 트리거
                "trigger": {
                    "type": "input_tokens",
                    "value": 30000
                },
                # 지운 후 유지할 도구 사용 수
                "keep": {
                    "type": "tool_uses",
                    "value": 3
                },
                # 선택 사항: 최소한 이 많은 토큰 지우기
                "clear_at_least": {
                    "type": "input_tokens",
                    "value": 5000
                },
                # 지우지 않을 도구 제외
                "exclude_tools": ["web_search"]
            }
        ]
    }
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 4096,
  messages: [
    {
      role: "user",
      content: "Create a simple command line calculator app using Python"
    }
  ],
  tools: [
    {
      type: "text_editor_20250728",
      name: "str_replace_based_edit_tool",
      max_characters: 10000
    },
    {
      type: "web_search_20250305",
      name: "web_search",
      max_uses: 3
    }
  ],
  betas: ["context-management-2025-06-27"],
  context_management: {
    edits: [
      {
        type: "clear_tool_uses_20250919",
        // 임계값이 초과될 때 지우기 트리거
        trigger: {
          type: "input_tokens",
          value: 30000
        },
        // 지운 후 유지할 도구 사용 수
        keep: {
          type: "tool_uses",
          value: 3
        },
        // 선택 사항: 최소한 이 많은 토큰 지우기
        clear_at_least: {
          type: "input_tokens",
          value: 5000
        },
        // 지우지 않을 도구 제외
        exclude_tools: ["web_search"]
      }
    ]
  }
});
```

</CodeGroup>

## Thinking 블록 지우기 사용법

extended thinking이 활성화된 경우 컨텍스트와 프롬프트 캐싱을 효과적으로 관리하려면 thinking 블록 지우기를 활성화하세요:

<CodeGroup>

```bash cURL
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "content-type: application/json" \
    --header "anthropic-beta: context-management-2025-06-27" \
    --data '{
        "model": "claude-sonnet-4-5-20250929",
        "max_tokens": 1024,
        "messages": [...],
        "thinking": {
            "type": "enabled",
            "budget_tokens": 10000
        },
        "context_management": {
            "edits": [
                {
                    "type": "clear_thinking_20251015",
                    "keep": {
                        "type": "thinking_turns",
                        "value": 2
                    }
                }
            ]
        }
    }'
```

```python Python
response = client.beta.messages.create(
    model="claude-sonnet-4-5-20250929",
    max_tokens=1024,
    messages=[...],
    thinking={
        "type": "enabled",
        "budget_tokens": 10000
    },
    betas=["context-management-2025-06-27"],
    context_management={
        "edits": [
            {
                "type": "clear_thinking_20251015",
                "keep": {
                    "type": "thinking_turns",
                    "value": 2
                }
            }
        ]
    }
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5-20250929",
  max_tokens: 1024,
  messages: [...],
  thinking: {
    type: "enabled",
    budget_tokens: 10000
  },
  betas: ["context-management-2025-06-27"],
  context_management: {
    edits: [
      {
        type: "clear_thinking_20251015",
        keep: {
          type: "thinking_turns",
          value: 2
        }
      }
    ]
  }
});
```

</CodeGroup>

### Thinking 블록 지우기 구성 옵션

`clear_thinking_20251015` 전략은 다음 구성을 지원합니다:

| 구성 옵션 | 기본값 | 설명 |
|---------------------|---------|-------------|
| `keep` | `{type: "thinking_turns", value: 1}` | thinking 블록이 있는 최근 어시스턴트 턴을 몇 개 보존할지 정의합니다. 마지막 N개 턴을 유지하려면 `{type: "thinking_turns", value: N}`을 사용하거나(N은 > 0이어야 함), 모든 thinking 블록을 유지하려면 `"all"`을 사용하세요. |

**구성 예제:**

```json
// 마지막 3개의 어시스턴트 턴에서 thinking 블록 유지
{
  "type": "clear_thinking_20251015",
  "keep": {
    "type": "thinking_turns",
    "value": 3
  }
}

// 모든 thinking 블록 유지 (캐시 히트 최대화)
{
  "type": "clear_thinking_20251015",
  "keep": "all"
}
```

### 전략 결합

thinking 블록 지우기와 도구 결과 지우기를 함께 사용할 수 있습니다:


> 여러 전략을 사용할 때 `clear_thinking_20251015` 전략은 `edits` 배열에서 먼저 나열되어야 합니다.


<CodeGroup>

```python Python
response = client.beta.messages.create(
    model="claude-sonnet-4-5-20250929",
    max_tokens=1024,
    messages=[...],
    thinking={
        "type": "enabled",
        "budget_tokens": 10000
    },
    tools=[...],
    betas=["context-management-2025-06-27"],
    context_management={
        "edits": [
            {
                "type": "clear_thinking_20251015",
                "keep": {
                    "type": "thinking_turns",
                    "value": 2
                }
            },
            {
                "type": "clear_tool_uses_20250919",
                "trigger": {
                    "type": "input_tokens",
                    "value": 50000
                },
                "keep": {
                    "type": "tool_uses",
                    "value": 5
                }
            }
        ]
    }
)
```

```typescript TypeScript
const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5-20250929",
  max_tokens: 1024,
  messages: [...],
  thinking: {
    type: "enabled",
    budget_tokens: 10000
  },
  tools: [...],
  betas: ["context-management-2025-06-27"],
  context_management: {
    edits: [
      {
        type: "clear_thinking_20251015",
        keep: {
          type: "thinking_turns",
          value: 2
        }
      },
      {
        type: "clear_tool_uses_20250919",
        trigger: {
          type: "input_tokens",
          value: 50000
        },
        keep: {
          type: "tool_uses",
          value: 5
        }
      }
    ]
  }
});
```

</CodeGroup>

## 도구 결과 지우기 구성 옵션

| 구성 옵션 | 기본값 | 설명 |
|---------------------|---------|-------------|
| `trigger` | 100,000 입력 토큰 | 컨텍스트 편집 전략이 활성화되는 시기를 정의합니다. 프롬프트가 이 임계값을 초과하면 지우기가 시작됩니다. 이 값을 `input_tokens` 또는 `tool_uses`로 지정할 수 있습니다. |
| `keep` | 3개의 도구 사용 | 지우기가 발생한 후 유지할 최근 도구 사용/결과 쌍의 수를 정의합니다. API는 가장 오래된 도구 상호 작용을 먼저 제거하고 가장 최근 것을 보존합니다. |
| `clear_at_least` | None | 전략이 활성화될 때마다 지워지는 최소 토큰 수를 보장합니다. API가 최소한 지정된 양을 지울 수 없으면 전략이 적용되지 않습니다. 이는 컨텍스트 지우기가 프롬프트 캐시를 깨뜨릴 가치가 있는지 결정하는 데 도움이 됩니다. |
| `exclude_tools` | None | 도구 사용 및 결과를 절대 지우지 않아야 하는 도구 이름 목록입니다. 중요한 컨텍스트를 보존하는 데 유용합니다. |
| `clear_tool_inputs` | `false` | 도구 결과와 함께 도구 호출 매개변수를 지울지 여부를 제어합니다. 기본적으로 Claude의 원래 도구 호출을 보이게 유지하면서 도구 결과만 지워집니다. |

## 컨텍스트 편집 응답

요청에 적용된 컨텍스트 편집과 지워진 콘텐츠 및 입력 토큰에 대한 유용한 통계를 `context_management` 응답 필드를 사용하여 볼 수 있습니다.

```json Response
{
    "id": "msg_013Zva2CMHLNnXjNJJKqJ2EF",
    "type": "message",
    "role": "assistant",
    "content": [...],
    "usage": {...},
    "context_management": {
        "applied_edits": [
            // `clear_thinking_20251015` 사용 시
            {
                "type": "clear_thinking_20251015",
                "cleared_thinking_turns": 3,
                "cleared_input_tokens": 15000
            },
            // `clear_tool_uses_20250919` 사용 시
            {
                "type": "clear_tool_uses_20250919",
                "cleared_tool_uses": 8,
                "cleared_input_tokens": 50000
            }
        ]
    }
}
```

스트리밍 응답의 경우 컨텍스트 편집은 최종 `message_delta` 이벤트에 포함됩니다:

```json Streaming Response
{
    "type": "message_delta",
    "delta": {
        "stop_reason": "end_turn",
        "stop_sequence": null
    },
    "usage": {
        "output_tokens": 1024
    },
    "context_management": {
        "applied_edits": [...]
    }
}
```

## 토큰 계산

[토큰 계산](../02-capabilities/09-token-counting.md) 엔드포인트는 컨텍스트 관리를 지원하여 컨텍스트 편집이 적용된 후 프롬프트가 사용할 토큰 수를 미리 볼 수 있습니다.

<CodeGroup>

```bash cURL
curl https://api.anthropic.com/v1/messages/count_tokens \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "content-type: application/json" \
    --header "anthropic-beta: context-management-2025-06-27" \
    --data '{
        "model": "claude-sonnet-4-5",
        "messages": [
            {
                "role": "user",
                "content": "Continue our conversation..."
            }
        ],
        "tools": [...],
        "context_management": {
            "edits": [
                {
                    "type": "clear_tool_uses_20250919",
                    "trigger": {
                        "type": "input_tokens",
                        "value": 30000
                    },
                    "keep": {
                        "type": "tool_uses",
                        "value": 5
                    }
                }
            ]
        }
    }'
```

```python Python
response = client.beta.messages.count_tokens(
    model="claude-sonnet-4-5",
    messages=[
        {
            "role": "user",
            "content": "Continue our conversation..."
        }
    ],
    tools=[...],  # 도구 정의
    betas=["context-management-2025-06-27"],
    context_management={
        "edits": [
            {
                "type": "clear_tool_uses_20250919",
                "trigger": {
                    "type": "input_tokens",
                    "value": 30000
                },
                "keep": {
                    "type": "tool_uses",
                    "value": 5
                }
            }
        ]
    }
)

print(f"원래 토큰: {response.context_management['original_input_tokens']}")
print(f"지운 후: {response.input_tokens}")
print(f"절약: {response.context_management['original_input_tokens'] - response.input_tokens} 토큰")
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

const response = await anthropic.beta.messages.countTokens({
  model: "claude-sonnet-4-5",
  messages: [
    {
      role: "user",
      content: "Continue our conversation..."
    }
  ],
  tools: [...],  // 도구 정의
  betas: ["context-management-2025-06-27"],
  context_management: {
    edits: [
      {
        type: "clear_tool_uses_20250919",
        trigger: {
          type: "input_tokens",
          value: 30000
        },
        keep: {
          type: "tool_uses",
          value: 5
        }
      }
    ]
  }
});

console.log(`원래 토큰: ${response.context_management?.original_input_tokens}`);
console.log(`지운 후: ${response.input_tokens}`);
console.log(`절약: ${(response.context_management?.original_input_tokens || 0) - response.input_tokens} 토큰`);
```
</CodeGroup>

```json Response
{
    "input_tokens": 25000,
    "context_management": {
        "original_input_tokens": 70000
    }
}
```

응답은 컨텍스트 관리가 적용된 후의 최종 토큰 수(`input_tokens`)와 지우기가 발생하기 전의 원래 토큰 수(`original_input_tokens`)를 모두 표시합니다.

## Memory Tool과 함께 사용

컨텍스트 편집은 [memory tool](../03-tools/11-memory-tool.md)과 결합할 수 있습니다. 대화 컨텍스트가 구성된 지우기 임계값에 가까워지면 Claude는 중요한 정보를 보존하라는 자동 경고를 받습니다. 이를 통해 Claude는 대화 기록에서 지워지기 전에 도구 결과나 컨텍스트를 메모리 파일에 저장할 수 있습니다.

이 조합을 통해 다음을 수행할 수 있습니다:

- **중요한 컨텍스트 보존**: Claude는 도구 결과가 지워지기 전에 해당 결과의 필수 정보를 메모리 파일에 작성할 수 있습니다
- **장기 실행 워크플로 유지**: 정보를 영구 저장소에 오프로드하여 그렇지 않으면 컨텍스트 제한을 초과할 에이전틱 워크플로를 활성화합니다
- **필요 시 정보 액세스**: Claude는 활성 컨텍스트 윈도우에 모든 것을 유지하는 대신 필요할 때 메모리 파일에서 이전에 지워진 정보를 조회할 수 있습니다

예를 들어, Claude가 많은 작업을 수행하는 파일 편집 워크플로에서 컨텍스트가 증가함에 따라 Claude는 완료된 변경 사항을 메모리 파일에 요약할 수 있습니다. 도구 결과가 지워지면 Claude는 메모리 시스템을 통해 해당 정보에 대한 액세스를 유지하고 효과적으로 계속 작업할 수 있습니다.

두 기능을 함께 사용하려면 API 요청에서 활성화하세요:

<CodeGroup>

```python Python
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=4096,
    messages=[...],
    tools=[
        {
            "type": "memory_20250818",
            "name": "memory"
        },
        # 다른 도구들
    ],
    betas=["context-management-2025-06-27"],
    context_management={
        "edits": [
            {"type": "clear_tool_uses_20250919"}
        ]
    }
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const anthropic = new Anthropic({
  apiKey: process.env.ANTHROPIC_API_KEY,
});

const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 4096,
  messages: [...],
  tools: [
    {
      type: "memory_20250818",
      name: "memory"
    },
    // 다른 도구들
  ],
  betas: ["context-management-2025-06-27"],
  context_management: {
    edits: [
      { type: "clear_tool_uses_20250919" }
    ]
  }
});
```

</CodeGroup>

## 클라이언트 측 압축 (SDK)


> 압축은 [`tool_runner` 메서드](../03-tools/02-how-to-implement-tool-use.md)를 사용할 때 [Python 및 TypeScript SDK](https://platform.claude.com/docs/en/api/client-sdks)에서 사용할 수 있습니다.


압축은 토큰 사용량이 너무 커질 때 요약을 생성하여 대화 컨텍스트를 자동으로 관리하는 SDK 기능입니다. 콘텐츠를 지우는 서버 측 컨텍스트 편집 전략과 달리 압축은 Claude에게 대화 기록을 요약하도록 지시한 다음 전체 기록을 해당 요약으로 대체합니다. 이를 통해 Claude는 그렇지 않으면 [컨텍스트 윈도우](./01-build-with-claude-02-context-windows.md)를 초과할 장기 실행 작업을 계속할 수 있습니다.

### 압축 작동 방식

압축이 활성화되면 SDK는 각 모델 응답 후 토큰 사용량을 모니터링합니다:

1. **임계값 확인**: SDK는 총 토큰을 `input_tokens + cache_creation_input_tokens + cache_read_input_tokens + output_tokens`로 계산합니다
2. **요약 생성**: 임계값이 초과되면 사용자 턴으로 요약 프롬프트가 주입되고 Claude는 `<summary></summary>` 태그로 래핑된 구조화된 요약을 생성합니다
3. **컨텍스트 대체**: SDK는 요약을 추출하고 전체 메시지 기록을 요약으로 대체합니다
4. **계속**: 대화는 요약에서 재개되며 Claude는 중단한 부분에서 계속합니다

### 압축 사용

`tool_runner` 호출에 `compaction_control`을 추가하세요:

<CodeGroup>

```python Python
import anthropic

client = anthropic.Anthropic()

runner = client.beta.messages.tool_runner(
    model="claude-sonnet-4-5",
    max_tokens=4096,
    tools=[...],
    messages=[
        {
            "role": "user",
            "content": "Analyze all the files in this directory and write a summary report."
        }
    ],
    compaction_control={
        "enabled": True,
        "context_token_threshold": 100000
    }
)

for message in runner:
    print(f"사용된 토큰: {message.usage.input_tokens}")

final = runner.until_done()
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const client = new Anthropic();

const runner = client.beta.messages.toolRunner({
    model: 'claude-sonnet-4-5',
    max_tokens: 4096,
    tools: [...],
    messages: [
        {
            role: 'user',
            content: 'Analyze all the files in this directory and write a summary report.'
        }
    ],
    compactionControl: {
        enabled: true,
        contextTokenThreshold: 100000
    }
});

for await (const message of runner) {
    console.log('사용된 토큰:', message.usage.input_tokens);
}

const finalMessage = await runner.runUntilDone();
```

</CodeGroup>

#### 압축 중 발생하는 일

대화가 증가함에 따라 메시지 기록이 누적됩니다:

**압축 전 (100k 토큰에 근접):**
```json
[
  { "role": "user", "content": "모든 파일을 분석하고 보고서를 작성..." },
  { "role": "assistant", "content": "도와드리겠습니다. 먼저 읽어보겠습니다..." },
  { "role": "user", "content": [{ "type": "tool_result", "tool_use_id": "...", "content": "..." }] },
  { "role": "assistant", "content": "file1.txt를 기반으로 볼 수 있습니다..." },
  { "role": "user", "content": [{ "type": "tool_result", "tool_use_id": "...", "content": "..." }] },
  { "role": "assistant", "content": "file2.txt를 분석한 후..." },
  // ... 이와 같은 50개 이상의 교환 ...
]
```

토큰이 임계값을 초과하면 SDK는 요약 요청을 주입하고 Claude는 요약을 생성합니다. 그런 다음 전체 기록이 대체됩니다:

**압축 후 (약 2-3k 토큰으로 돌아감):**
```json
[
  {
    "role": "assistant",
    "content": "# 작업 개요\n사용자가 요약 보고서를 생성하기 위해 디렉토리 파일 분석을 요청했습니다...\n\n# 현재 상태\n3개의 하위 디렉토리에서 52개 파일을 분석했습니다. 주요 발견 사항이 report.md에 문서화되었습니다...\n\n# 중요한 발견\n- 구성 파일이 YAML 형식을 사용합니다\n- 3개의 더 이상 사용되지 않는 종속성을 발견했습니다\n- 테스트 커버리지가 67%입니다\n\n# 다음 단계\n1. /src/legacy의 나머지 파일을 분석합니다\n2. 최종 보고서 섹션을 완성합니다...\n\n# 보존할 컨텍스트\n사용자는 먼저 요약 개요가 있는 마크다운 형식을 선호합니다..."
  }
]
```

Claude는 이 요약이 원래 대화 기록인 것처럼 계속 작업합니다.

### 구성 옵션

| 매개변수 | 타입 | 필수 | 기본값 | 설명 |
|-----------|------|----------|---------|-------------|
| `enabled` | boolean | 예 | - | 자동 압축 활성화 여부 |
| `context_token_threshold` | number | 아니요 | 100,000 | 압축이 트리거되는 토큰 수 |
| `model` | string | 아니요 | 메인 모델과 동일 | 요약 생성에 사용할 모델 |
| `summary_prompt` | string | 아니요 | 아래 참조 | 요약 생성을 위한 사용자 정의 프롬프트 |

#### 토큰 임계값 선택

임계값은 압축이 발생하는 시기를 결정합니다. 낮은 임계값은 작은 컨텍스트 윈도우로 더 빈번한 압축을 의미합니다. 높은 임계값은 더 많은 컨텍스트를 허용하지만 제한에 도달할 위험이 있습니다.

<CodeGroup>

```python Python
# 메모리 제약 시나리오를 위한 더 빈번한 압축
compaction_control={
    "enabled": True,
    "context_token_threshold": 50000
}

# 더 많은 컨텍스트가 필요할 때 덜 빈번한 압축
compaction_control={
    "enabled": True,
    "context_token_threshold": 150000
}
```

```typescript TypeScript
// 메모리 제약 시나리오를 위한 더 빈번한 압축
compactionControl: {
    enabled: true,
    contextTokenThreshold: 50000
}

// 더 많은 컨텍스트가 필요할 때 덜 빈번한 압축
compactionControl: {
    enabled: true,
    contextTokenThreshold: 150000
}
```

</CodeGroup>

#### 요약에 다른 모델 사용

요약 생성에 더 빠르거나 저렴한 모델을 사용할 수 있습니다:

<CodeGroup>

```python Python
compaction_control={
    "enabled": True,
    "context_token_threshold": 100000,
    "model": "claude-haiku-4-5"
}
```

```typescript TypeScript
compactionControl: {
    enabled: true,
    contextTokenThreshold: 100000,
    model: 'claude-haiku-4-5'
}
```

</CodeGroup>

#### 사용자 정의 요약 프롬프트

도메인별 요구 사항에 대해 사용자 정의 프롬프트를 제공할 수 있습니다. 프롬프트는 Claude에게 요약을 `<summary></summary>` 태그로 래핑하도록 지시해야 합니다.

<CodeGroup>

```python Python
compaction_control={
    "enabled": True,
    "context_token_threshold": 100000,
    "summary_prompt": """지금까지 수행된 연구를 다음을 포함하여 요약하세요:
- 참조한 출처 및 주요 발견 사항
- 답변한 질문 및 남은 미지의 것
- 권장되는 다음 단계

요약을 <summary></summary> 태그로 래핑하세요."""
}
```

```typescript TypeScript
compactionControl: {
    enabled: true,
    contextTokenThreshold: 100000,
    summaryPrompt: `지금까지 수행된 연구를 다음을 포함하여 요약하세요:
- 참조한 출처 및 주요 발견 사항
- 답변한 질문 및 남은 미지의 것
- 권장되는 다음 단계

요약을 <summary></summary> 태그로 래핑하세요.`
}
```

</CodeGroup>

### 기본 요약 프롬프트

내장 요약 프롬프트는 Claude에게 다음을 포함하는 구조화된 계속 요약을 만들도록 지시합니다:

1. **작업 개요**: 사용자의 핵심 요청, 성공 기준 및 제약 조건
2. **현재 상태**: 완료된 것, 수정된 파일 및 생성된 아티팩트
3. **중요한 발견**: 기술적 제약, 내린 결정, 해결된 오류 및 실패한 접근 방식
4. **다음 단계**: 필요한 특정 작업, 차단 요소 및 우선 순위 순서
5. **보존할 컨텍스트**: 사용자 선호 사항, 도메인별 세부 정보 및 한 약속

이 구조를 통해 Claude는 중요한 컨텍스트를 잃거나 실수를 반복하지 않고 효율적으로 작업을 재개할 수 있습니다.

<details>
<summary>전체 기본 프롬프트 보기</summary>

```
위에서 설명한 작업을 수행했지만 아직 완료하지 못했습니다. 대화 기록이 이 요약으로 대체될 미래의 컨텍스트 윈도우에서 귀하(또는 귀하의 다른 인스턴스)가 효율적으로 작업을 재개할 수 있도록 계속 요약을 작성하세요. 요약은 구조화되고 간결하며 실행 가능해야 합니다. 다음을 포함하세요:

1. 작업 개요
사용자의 핵심 요청 및 성공 기준
지정한 명확화 또는 제약 조건

2. 현재 상태
지금까지 완료된 것
생성, 수정 또는 분석된 파일(관련된 경우 경로 포함)
생성된 주요 출력 또는 아티팩트

3. 중요한 발견
발견된 기술적 제약 또는 요구 사항
내린 결정 및 그 근거
발생한 오류 및 해결 방법
작동하지 않은 접근 방식(및 이유)

4. 다음 단계
작업을 완료하는 데 필요한 특정 작업
해결해야 할 차단 요소 또는 미해결 질문
여러 단계가 남아 있는 경우 우선 순위 순서

5. 보존할 컨텍스트
사용자 선호 사항 또는 스타일 요구 사항
명확하지 않은 도메인별 세부 정보
사용자에게 한 약속

간결하지만 완전하게 작성하세요. 중복 작업이나 반복된 실수를 방지하는 정보를 포함하는 쪽으로 오류를 범하세요. 작업을 즉시 재개할 수 있는 방식으로 작성하세요.

요약을 <summary></summary> 태그로 래핑하세요.
```
</details>

### 제한 사항

#### 서버 측 도구


> 압축은 [웹 검색](../03-tools/10-web-search-tool.md) 또는 [웹 가져오기](../03-tools/09-web-fetch-tool.md)와 같은 서버 측 도구를 사용할 때 특별한 고려가 필요합니다.


서버 측 도구를 사용할 때 SDK는 토큰 사용량을 잘못 계산하여 압축이 잘못된 시간에 트리거될 수 있습니다.

예를 들어, 웹 검색 작업 후 API 응답은 다음과 같이 표시될 수 있습니다:

```json
{
  "usage": {
    "input_tokens": 63000,
    "cache_read_input_tokens": 270000,
    "output_tokens": 1400
  }
}
```

SDK는 총 사용량을 63,000 + 270,000 = 333,000 토큰으로 계산합니다. 그러나 `cache_read_input_tokens` 값에는 실제 대화 컨텍스트가 아니라 서버 측 도구가 수행한 여러 내부 API 호출에서 누적된 읽기가 포함됩니다. 실제 컨텍스트 길이는 63,000 `input_tokens`일 수 있지만 SDK는 333k를 보고 압축을 조기에 트리거합니다.

**해결 방법:**

- 정확한 컨텍스트 길이를 얻으려면 [토큰 계산](../02-capabilities/09-token-counting.md) 엔드포인트를 사용하세요
- 서버 측 도구를 광범위하게 사용할 때는 압축을 피하세요

#### 도구 사용 에지 케이스

도구 사용 응답이 대기 중일 때 압축이 트리거되면 SDK는 요약을 생성하기 전에 메시지 기록에서 도구 사용 블록을 제거합니다. Claude는 여전히 필요한 경우 요약에서 재개한 후 도구 호출을 다시 발행합니다.

### 압축 모니터링

로깅을 활성화하여 압축이 발생하는 시기를 추적하세요:

<CodeGroup>

```python Python
import logging

logging.basicConfig(level=logging.INFO)
logging.getLogger("anthropic.lib.tools").setLevel(logging.INFO)

# 로그는 다음을 표시합니다:
# INFO: 토큰 사용량 105000이 임계값 100000을 초과했습니다. 압축을 수행합니다.
# INFO: 압축이 완료되었습니다. 새 토큰 사용량: 2500
```

```typescript TypeScript
// SDK는 콘솔에 압축 이벤트를 로그합니다
// 다음과 같은 메시지를 볼 수 있습니다:
// 토큰 사용량 105000이 임계값 100000을 초과했습니다. 압축을 수행합니다.
// 압축이 완료되었습니다. 새 토큰 사용량: 2500
```

</CodeGroup>

### 압축을 사용해야 하는 경우

**좋은 사용 사례:**

- 많은 파일이나 데이터 소스를 처리하는 장기 실행 에이전트 작업
- 많은 양의 정보를 축적하는 연구 워크플로
- 명확하고 측정 가능한 진행 상황이 있는 다단계 작업
- 대화 밖에 지속되는 아티팩트(파일, 보고서)를 생성하는 작업

**덜 이상적인 사용 사례:**

- 초기 대화 세부 정보의 정확한 회상이 필요한 작업
- 서버 측 도구를 광범위하게 사용하는 워크플로
- 많은 변수에 걸쳐 정확한 상태를 유지해야 하는 작업

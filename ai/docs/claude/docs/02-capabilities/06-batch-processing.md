# [배치 처리](https://platform.claude.com/docs/en/build-with-claude/batch-processing)

---

배치 처리는 대량의 요청을 효율적으로 처리하기 위한 강력한 접근 방식입니다. <br/>
즉시 응답을 받으며 요청을 하나씩 처리하는 대신, 배치 처리를 사용하면 여러 요청을 함께 제출하여 비동기적으로 처리할 수 있습니다.

이 패턴은 다음과 같은 경우에 특히 유용합니다:

- 대량의 데이터를 처리해야 하는 경우
- 즉각적인 응답이 필요하지 않은 경우
- 비용 효율성을 최적화하고자 하는 경우
- 대규모 평가 또는 분석을 실행하는 경우

Message Batches API는 이 패턴의 첫 번째 구현입니다.

---

# Message Batches API

Message Batches API는 대량의 [Messages](https://platform.claude.com/docs/en/api/messages) 요청을 비동기적으로 처리할 수 있는 강력하고 비용 효율적인
방법입니다.<br/>
이 접근 방식은 즉각적인 응답이 필요하지 않은 작업에 적합하며, 대부분의 배치는 1시간 이내에 완료되면서 비용은 50% 절감하고 처리량은 증가시킵니다.

이 가이드 외에도 [API 레퍼런스를 직접 탐색](https://platform.claude.com/docs/en/api/creating-message-batches)할 수 있습니다.

## Message Batches API 작동 방식

Message Batches API에 요청을 보내면:

1. 시스템은 제공된 Messages 요청으로 새로운 Message Batch를 생성합니다.
2. 배치는 각 요청을 독립적으로 처리하면서 비동기적으로 처리됩니다.
3. 모든 요청의 처리가 완료되면 배치 상태를 폴링하고 결과를 검색할 수 있습니다.

이는 즉각적인 결과가 필요하지 않은 대량 작업에 특히 유용합니다:

- 대규모 평가: 수천 개의 테스트 케이스를 효율적으로 처리합니다.
- 콘텐츠 조절: 대량의 사용자 생성 콘텐츠를 비동기적으로 분석합니다.
- 데이터 분석: 대규모 데이터셋에 대한 인사이트나 요약을 생성합니다.
- 대량 콘텐츠 생성: 다양한 목적(예: 제품 설명, 기사 요약)을 위해 대량의 텍스트를 생성합니다.

### 배치 제한사항

- Message Batch는 100,000개의 Message 요청 또는 256MB 크기 중 먼저 도달하는 제한까지 허용됩니다.
- 각 배치는 가능한 한 빠르게 처리되며, 대부분의 배치는 1시간 이내에 완료됩니다.
    - 모든 메시지가 완료되거나 24시간이 지난 후 배치 결과에 액세스할 수 있습니다.
    - 처리가 24시간 이내에 완료되지 않으면 배치가 만료됩니다.
- 배치 결과는 생성 후 29일 동안 사용할 수 있습니다. 그 이후에도 배치를 볼 수 있지만 결과는 더 이상 다운로드할 수 없습니다.
- 배치는 [Workspace](/settings/workspaces)로 범위가 지정됩니다. API 키가 속한 Workspace 내에서 생성된 모든 배치와 그 결과를 볼 수 있습니다.
- Batches API HTTP 요청과 처리 대기 중인 배치 내 요청 수 모두에 속도 제한이
  적용됩니다. [Message Batches API 속도 제한](https://platform.claude.com/docs/en/api/rate-limits#message-batches-api)을 참조하세요.
    - 또한 현재 수요와 요청 볼륨에 따라 처리 속도가 느려질 수 있습니다.
    - 이 경우 24시간 후 만료되는 요청이 더 많아질 수 있습니다.
- 높은 처리량과 동시 처리로 인해 배치가 Workspace에 구성된 [지출 제한](/settings/limits)을 약간 초과할 수 있습니다.

### 지원 모델

모든 [활성 모델](https://platform.claude.com/docs/en/about-claude/models/overview)이 Message Batches API를 지원합니다.

### 배치 가능한 항목

Messages API에 만들 수 있는 모든 요청을 배치에 포함할 수 있습니다. 여기에는 다음이 포함됩니다:

- Vision
- Tool use
- System messages
- Multi-turn conversations
- 모든 베타 기능

배치 내 각 요청은 독립적으로 처리되므로 단일 배치 내에서 다양한 유형의 요청을 혼합할 수 있습니다.

> 배치 처리에는 5분 이상 걸릴 수 있으므로 공유 컨텍스트가 있는 배치를 처리할 때 더 나은 캐시 적중률을 위해 프롬프트 캐싱과
> 함께 [1시간 캐시 지속 시간](../02-capabilities/01-prompt-caching.md)을 사용하는 것을 고려하세요.

---

## 가격

Batches API는 상당한 비용 절감 효과를 제공합니다. 모든 사용량은 표준 API 가격의 50%로 청구됩니다.

| 모델                                                                                               | 배치 입력         | 배치 출력         |
|--------------------------------------------------------------------------------------------------|---------------|---------------|
| Claude Opus 4.5                                                                                  | $2.50 / MTok  | $12.50 / MTok |
| Claude Opus 4.1                                                                                  | $7.50 / MTok  | $37.50 / MTok |
| Claude Opus 4                                                                                    | $7.50 / MTok  | $37.50 / MTok |
| Claude Sonnet 4.5                                                                                | $1.50 / MTok  | $7.50 / MTok  |
| Claude Sonnet 4                                                                                  | $1.50 / MTok  | $7.50 / MTok  |
| Claude Sonnet 3.7 ([지원 중단](https://platform.claude.com/docs/en/about-claude/model-deprecations)) | $1.50 / MTok  | $7.50 / MTok  |
| Claude Haiku 4.5                                                                                 | $0.50 / MTok  | $2.50 / MTok  |
| Claude Haiku 3.5                                                                                 | $0.40 / MTok  | $2 / MTok     |
| Claude Opus 3 ([지원 중단](https://platform.claude.com/docs/en/about-claude/model-deprecations))     | $7.50 / MTok  | $37.50 / MTok |
| Claude Haiku 3                                                                                   | $0.125 / MTok | $0.625 / MTok |

---

## Message Batches API 사용 방법

### 배치 준비 및 생성

Message Batch는 Message를 생성하기 위한 요청 목록으로 구성됩니다. 개별 요청의 구조는 다음으로 구성됩니다:

- Messages 요청을 식별하기 위한 고유한 `custom_id`
- 표준 [Messages API](https://platform.claude.com/docs/en/api/messages) 매개변수를 포함하는 `params` 객체

이 목록을 `requests` 매개변수에 전달하여 [배치를 생성](https://platform.claude.com/docs/en/api/creating-message-batches)할 수 있습니다:

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages/batches \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data \
'{
    "requests": [
        {
            "custom_id": "my-first-request",
            "params": {
                "model": "claude-sonnet-4-5",
                "max_tokens": 1024,
                "messages": [
                    {"role": "user", "content": "Hello, world"}
                ]
            }
        },
        {
            "custom_id": "my-second-request",
            "params": {
                "model": "claude-sonnet-4-5",
                "max_tokens": 1024,
                "messages": [
                    {"role": "user", "content": "Hi again, friend"}
                ]
            }
        }
    ]
}'
```

</details>

이 예제에서는 두 개의 개별 요청이 비동기 처리를 위해 함께 배치됩니다. 각 요청에는 고유한 `custom_id`가 있으며 Messages API 호출에 사용할 표준 매개변수가 포함되어 있습니다.

> **Messages API로 배치 요청 테스트**<br/>
> 각 메시지 요청에 대한 `params` 객체의 유효성 검사는 비동기적으로 수행되며, 유효성 검사 오류는 전체 배치 처리가 완료되면 반환됩니다.<br/>
> 먼저 [Messages API](https://platform.claude.com/docs/en/api/messages)로 요청 형식을 확인하여 입력을 올바르게 구성하고 있는지 확인할 수 있습니다.


배치가 처음 생성되면 응답의 처리 상태는 `in_progress`가 됩니다.

```json JSON
{
  "id": "msgbatch_01HkcTjaV5uDC8jWR4ZsDV8d",
  "type": "message_batch",
  "processing_status": "in_progress",
  "request_counts": {
    "processing": 2,
    "succeeded": 0,
    "errored": 0,
    "canceled": 0,
    "expired": 0
  },
  "ended_at": null,
  "created_at": "2024-09-24T18:37:24.100435Z",
  "expires_at": "2024-09-25T18:37:24.100435Z",
  "cancel_initiated_at": null,
  "results_url": null
}
```

### 배치 추적

Message Batch의 `processing_status` 필드는 배치가 처리 중인 단계를 나타냅니다. 
`in_progress`로 시작하여 배치의 모든 요청이 처리를 완료하고 결과가 준비되면 `ended`로 업데이트됩니다. [Console](/settings/workspaces/default/batches)을
방문하거나 [검색 엔드포인트](https://platform.claude.com/docs/en/api/retrieving-message-batches)를 사용하여 배치 상태를 모니터링할 수 있습니다.

#### Message Batch 완료 폴링

Message Batch를 폴링하려면 배치를 생성할 때 또는 배치를 나열하여 제공되는 `id`가 필요합니다.

처리가 완료될 때까지 주기적으로 배치 상태를 확인하는 폴링 루프를 구현할 수 있습니다:
<details>
<summary>REST API 예시</summary>

```bash
#!/bin/sh

until [[ $(curl -s "https://api.anthropic.com/v1/messages/batches/$MESSAGE_BATCH_ID" \
          --header "x-api-key: $ANTHROPIC_API_KEY" \
          --header "anthropic-version: 2023-06-01" \
          | grep -o '"processing_status":[[:space:]]*"[^"]*"' \
          | cut -d'"' -f4) == "ended" ]]; do
    echo "Batch $MESSAGE_BATCH_ID is still processing..."
    sleep 60
done

echo "Batch $MESSAGE_BATCH_ID has finished processing"
```

</details>

### 모든 Message Batch 나열

[목록 엔드포인트](https://platform.claude.com/docs/en/api/listing-message-batches)를 사용하여 Workspace의 모든 Message Batch를 나열할 수
있습니다. API는 페이지네이션을 지원하며 필요에 따라 추가 페이지를 자동으로 가져옵니다:

<details>
<summary>REST API 예시</summary>

```bash
#!/bin/sh

if ! command -v jq &> /dev/null; then
    echo "Error: This script requires jq. Please install it first."
    exit 1
fi

BASE_URL="https://api.anthropic.com/v1/messages/batches"

has_more=true
after_id=""

while [ "$has_more" = true ]; do
    # after_id가 존재하면 URL 구성
    if [ -n "$after_id" ]; then
        url="${BASE_URL}?limit=20&after_id=${after_id}"
    else
        url="$BASE_URL?limit=20"
    fi

    response=$(curl -s "$url" \
              --header "x-api-key: $ANTHROPIC_API_KEY" \
              --header "anthropic-version: 2023-06-01")

    # jq를 사용하여 값 추출
    has_more=$(echo "$response" | jq -r '.has_more')
    after_id=$(echo "$response" | jq -r '.last_id')

    # data 배열의 각 항목 처리 및 출력
    echo "$response" | jq -c '.data[]' | while read -r entry; do
        echo "$entry" | jq '.'
    done
done
```

</details>

### 배치 결과 검색

배치 처리가 완료되면 배치의 각 Messages 요청에 결과가 있습니다. 결과 유형은 4가지입니다:

| 결과 유형       | 설명                                                                                       |
|-------------|------------------------------------------------------------------------------------------|
| `succeeded` | 요청이 성공했습니다. 메시지 결과가 포함됩니다.                                                               |
| `errored`   | 요청에서 오류가 발생하여 메시지가 생성되지 않았습니다. 가능한 오류에는 잘못된 요청과 내부 서버 오류가 포함됩니다. 이러한 요청에 대해서는 청구되지 않습니다. |
| `canceled`  | 사용자가 이 요청이 모델로 전송되기 전에 배치를 취소했습니다. 이러한 요청에 대해서는 청구되지 않습니다.                               |
| `expired`   | 이 요청이 모델로 전송되기 전에 배치가 24시간 만료 시간에 도달했습니다. 이러한 요청에 대해서는 청구되지 않습니다.                        |

배치의 `request_counts`를 통해 결과 개요를 확인할 수 있으며, 이는 각 네 가지 상태에 도달한 요청 수를 보여줍니다.

배치 결과는 Message Batch의 `results_url` 속성에서 다운로드할 수 있으며, 조직 권한이 허용하는 경우 Console에서도 다운로드할 수 있습니다. 
결과가 잠재적으로 클 수 있으므로 한 번에 모두 다운로드하는 대신 [결과를 스트리밍](https://platform.claude.com/docs/en/api/retrieving-message-batch-results)하는 것이 좋습니다.

<details>
<summary>REST API 예시</summary>

```bash
#!/bin/sh
curl "https://api.anthropic.com/v1/messages/batches/msgbatch_01HkcTjaV5uDC8jWR4ZsDV8d" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_API_KEY" \
  | grep -o '"results_url":[[:space:]]*"[^"]*"' \
  | cut -d'"' -f4 \
  | while read -r url; do
    curl -s "$url" \
      --header "anthropic-version: 2023-06-01" \
      --header "x-api-key: $ANTHROPIC_API_KEY" \
      | sed 's/}{/}\n{/g' \
      | while IFS= read -r line
    do
      result_type=$(echo "$line" | sed -n 's/.*"result":[[:space:]]*{[[:space:]]*"type":[[:space:]]*"\([^"]*\)".*/\1/p')
      custom_id=$(echo "$line" | sed -n 's/.*"custom_id":[[:space:]]*"\([^"]*\)".*/\1/p')
      error_type=$(echo "$line" | sed -n 's/.*"error":[[:space:]]*{[[:space:]]*"type":[[:space:]]*"\([^"]*\)".*/\1/p')

      case "$result_type" in
        "succeeded")
          echo "Success! $custom_id"
          ;;
        "errored")
          if [ "$error_type" = "invalid_request" ]; then
            # 요청을 다시 보내기 전에 요청 본문을 수정해야 합니다
            echo "Validation error: $custom_id"
          else
            # 요청을 직접 재시도할 수 있습니다
            echo "Server error: $custom_id"
          fi
          ;;
        "expired")
          echo "Expired: $line"
          ;;
      esac
    done
  done
```

</details>

결과는 `.jsonl` 형식이며, 각 줄은 Message Batch의 단일 요청 결과를 나타내는 유효한 JSON 객체입니다. 
각 스트리밍된 결과에 대해 `custom_id`와 결과 유형에 따라 다른 작업을 수행할 수 있습니다. 

다음은 결과 세트의 예입니다:
```json .jsonl file
{
  "custom_id": "my-second-request",
  "result": {
    "type": "succeeded",
    "message": {
      "id": "msg_014VwiXbi91y3JMjcpyGBHX5",
      "type": "message",
      "role": "assistant",
      "model": "claude-sonnet-4-5-20250929",
      "content": [
        {
          "type": "text",
          "text": "Hello again! It's nice to see you. How can I assist you today? Is there anything specific you'd like to chat about or any questions you have?"
        }
      ],
      "stop_reason": "end_turn",
      "stop_sequence": null,
      "usage": {
        "input_tokens": 11,
        "output_tokens": 36
      }
    }
  }
}
{
  "custom_id": "my-first-request",
  "result": {
    "type": "succeeded",
    "message": {
      "id": "msg_01FqfsLoHwgeFbguDgpz48m7",
      "type": "message",
      "role": "assistant",
      "model": "claude-sonnet-4-5-20250929",
      "content": [
        {
          "type": "text",
          "text": "Hello! How can I assist you today? Feel free to ask me any questions or let me know if there's anything you'd like to chat about."
        }
      ],
      "stop_reason": "end_turn",
      "stop_sequence": null,
      "usage": {
        "input_tokens": 10,
        "output_tokens": 34
      }
    }
  }
}
```

결과에 오류가 있는 경우 `result.error`가 표준 [오류 형식](https://platform.claude.com/docs/en/api/errors#error-shapes)으로 설정됩니다.

> **배치 결과는 입력 순서와 일치하지 않을 수 있습니다**<br/>
> 배치 결과는 임의의 순서로 반환될 수 있으며 배치를 생성할 때의 요청 순서와 일치하지 않을 수 있습니다. <br/>
> 위의 예에서 두 번째 배치 요청의 결과가 첫 번째 요청보다 먼저 반환됩니다. 결과를 해당 요청과 올바르게 일치시키려면 항상 `custom_id` 필드를 사용하세요.

### Message Batch 취소

[취소 엔드포인트](https://platform.claude.com/docs/en/api/canceling-message-batches)를 사용하여 현재 처리 중인 Message Batch를 취소할 수 있습니다.
취소 직후 배치의 `processing_status`는 `canceling`이 됩니다. 
위에서 설명한 것과 동일한 폴링 기술을 사용하여 취소가 완료될 때까지 기다릴 수 있습니다. 
취소된 배치는 `ended` 상태로 끝나며 취소 전에 처리된 요청에 대한 부분 결과를 포함할 수 있습니다.

<details>
<summary>REST API 예시</summary>

```bash
#!/bin/sh
curl --request POST https://api.anthropic.com/v1/messages/batches/$MESSAGE_BATCH_ID/cancel \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01"
```

</details>

응답은 `canceling` 상태의 배치를 보여줍니다:

```json JSON
{
  "id": "msgbatch_013Zva2CMHLNnXjNJJKqJ2EF",
  "type": "message_batch",
  "processing_status": "canceling",
  "request_counts": {
    "processing": 2,
    "succeeded": 0,
    "errored": 0,
    "canceled": 0,
    "expired": 0
  },
  "ended_at": null,
  "created_at": "2024-09-24T18:37:24.100435Z",
  "expires_at": "2024-09-25T18:37:24.100435Z",
  "cancel_initiated_at": "2024-09-24T18:39:03.114875Z",
  "results_url": null
}
```

### Message Batches와 프롬프트 캐싱 사용

Message Batches API는 프롬프트 캐싱을 지원하여 배치 요청의 비용과 처리 시간을 잠재적으로 줄일 수 있습니다. 
프롬프트 캐싱과 Message Batches의 가격 할인은 함께 사용할 때 더 큰 비용 절감 효과를 제공할 수 있습니다. 
그러나 배치 요청은 비동기적이고 동시에 처리되므로 캐시 적중은 최선의 노력을 기반으로 제공됩니다.
사용자는 일반적으로 트래픽 패턴에 따라 30%에서 98%의 캐시 적중률을 경험합니다.

배치 요청에서 캐시 적중 가능성을 최대화하려면:

1. 배치 내의 모든 Message 요청에 동일한 `cache_control` 블록을 포함합니다
2. 5분의 캐시 수명이 끝나기 전에 캐시 항목이 만료되지 않도록 안정적인 요청 스트림을 유지합니다
3. 가능한 한 많은 캐시된 콘텐츠를 공유하도록 요청을 구조화합니다

배치에서 프롬프트 캐싱을 구현하는 예:

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages/batches \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data \
'{
    "requests": [
        {
            "custom_id": "my-first-request",
            "params": {
                "model": "claude-sonnet-4-5",
                "max_tokens": 1024,
                "system": [
                    {
                        "type": "text",
                        "text": "You are an AI assistant tasked with analyzing literary works. Your goal is to provide insightful commentary on themes, characters, and writing style.\n"
                    },
                    {
                        "type": "text",
                        "text": "<the entire contents of Pride and Prejudice>",
                        "cache_control": {"type": "ephemeral"}
                    }
                ],
                "messages": [
                    {"role": "user", "content": "Analyze the major themes in Pride and Prejudice."}
                ]
            }
        },
        {
            "custom_id": "my-second-request",
            "params": {
                "model": "claude-sonnet-4-5",
                "max_tokens": 1024,
                "system": [
                    {
                        "type": "text",
                        "text": "You are an AI assistant tasked with analyzing literary works. Your goal is to provide insightful commentary on themes, characters, and writing style.\n"
                    },
                    {
                        "type": "text",
                        "text": "<the entire contents of Pride and Prejudice>",
                        "cache_control": {"type": "ephemeral"}
                    }
                ],
                "messages": [
                    {"role": "user", "content": "Write a summary of Pride and Prejudice."}
                ]
            }
        }
    ]
}'
```

</details>

이 예제에서 배치의 두 요청 모두 동일한 시스템 메시지와 `cache_control`로 표시된 Pride and Prejudice의 전체 텍스트를 포함하여 캐시 적중 가능성을 높입니다.

### 효과적인 배치 처리를 위한 모범 사례

Batches API를 최대한 활용하려면:

- 배치 처리 상태를 정기적으로 모니터링하고 실패한 요청에 대한 적절한 재시도 로직을 구현하세요.
- 순서가 보장되지 않으므로 결과를 요청과 쉽게 일치시킬 수 있도록 의미 있는 `custom_id` 값을 사용하세요.
- 더 나은 관리를 위해 매우 큰 데이터셋을 여러 배치로 나누는 것을 고려하세요.
- 유효성 검사 오류를 방지하기 위해 Messages API로 단일 요청 형식을 미리 테스트하세요.

### 일반적인 문제 해결

예상치 못한 동작이 발생하는 경우:

- 총 배치 요청 크기가 256MB를 초과하지 않는지 확인하세요. 요청 크기가 너무 크면 413 `request_too_large` 오류가 발생할 수 있습니다.
- 배치의 모든 요청에 [지원 모델](#supported-models)을 사용하고 있는지 확인하세요.
- 배치의 각 요청에 고유한 `custom_id`가 있는지 확인하세요.
- 배치 `created_at`(처리 `ended_at`이 아님) 시간 이후 29일이 지나지 않았는지 확인하세요. 29일이 넘으면 결과를 더 이상 볼 수 없습니다.
- 배치가 취소되지 않았는지 확인하세요.

배치의 한 요청이 실패해도 다른 요청의 처리에는 영향을 미치지 않습니다.

---

## 배치 저장 및 개인정보 보호

- **Workspace 격리**: 배치는 생성된 Workspace 내에서 격리됩니다. 
  - 해당 Workspace와 연결된 API 키 또는 Console에서 Workspace 배치를 볼 수 있는 권한이 있는 사용자만 액세스할 수 있습니다.
- **결과 가용성**: 배치 결과는 배치 생성 후 29일 동안 사용할 수 있어 검색 및 처리를 위한 충분한 시간을 제공합니다.

---

## FAQ

<details>
<summary>배치를 처리하는 데 얼마나 걸리나요?</summary>

배치는 처리에 최대 24시간이 걸릴 수 있지만 많은 배치가 더 빨리 완료됩니다. 실제 처리 시간은 배치 크기, 현재 수요 및 요청 볼륨에 따라 달라집니다. 배치가 만료되어 24시간 이내에 완료되지 않을 수 있습니다.
</details>

<details>
<summary>Batches API는 모든 모델에서 사용할 수 있나요?</summary>

지원되는 모델 목록은 [위](#supported-models)를 참조하세요.
</details>

<details>
<summary>Message Batches API를 다른 API 기능과 함께 사용할 수 있나요?</summary>

예, Message Batches API는 베타 기능을 포함하여 Messages API에서 사용할 수 있는 모든 기능을 지원합니다. 그러나 스트리밍은 배치 요청에 대해 지원되지 않습니다.
</details>

<details>
<summary>Message Batches API는 가격에 어떤 영향을 미치나요?</summary>

Message Batches API는 표준 API 가격에 비해 모든 사용량에 대해 50% 할인을 제공합니다. 이는 입력 토큰, 출력 토큰 및 특수 토큰에 적용됩니다. 가격에 대한 자세한
내용은 [가격 페이지](https://claude.com/pricing#anthropic-api)를 방문하세요.
</details>

<details>
<summary>제출한 후 배치를 업데이트할 수 있나요?</summary>

아니요, 배치가 제출된 후에는 수정할 수 없습니다. 변경이 필요한 경우 현재 배치를 취소하고 새 배치를 제출해야 합니다. 취소가 즉시 적용되지 않을 수 있습니다.
</details>

<details>
<summary>Message Batches API 속도 제한이 있으며 Messages API 속도 제한과 어떻게 상호 작용하나요?</summary>

Message Batches API에는 HTTP 요청 기반 속도 제한과 처리 대기 중인 요청 수 제한이
있습니다. [Message Batches API 속도 제한](https://platform.claude.com/docs/en/api/rate-limits#message-batches-api)을 참조하세요.
Batches API 사용은 Messages API의 속도 제한에 영향을 미치지 않습니다.
</details>

<details>
<summary>배치 요청의 오류를 어떻게 처리하나요?</summary>

결과를 검색하면 각 요청에 `succeeded`, `errored`, `canceled` 또는 `expired` 여부를 나타내는 `result` 필드가 있습니다. `errored` 결과의 경우 추가 오류 정보가
제공됩니다. [API 레퍼런스](https://platform.claude.com/docs/en/api/creating-message-batches)에서 오류 응답 객체를 확인하세요.
</details>

<details>
<summary>Message Batches API는 개인정보 보호 및 데이터 분리를 어떻게 처리하나요?</summary>

Message Batches API는 강력한 개인정보 보호 및 데이터 분리 조치로 설계되었습니다:

    1. 배치와 그 결과는 생성된 Workspace 내에서 격리됩니다. 즉, 동일한 Workspace의 API 키로만 액세스할 수 있습니다.
    2. 배치 내의 각 요청은 독립적으로 처리되며 요청 간 데이터 누출이 없습니다.
    3. 결과는 제한된 시간(29일) 동안만 사용할 수 있으며 [데이터 보관 정책](https://support.claude.com/en/articles/7996866-how-long-do-you-store-personal-data)을 따릅니다.
    4. Console에서 배치 결과 다운로드는 조직 수준 또는 Workspace별로 비활성화할 수 있습니다.

</details>

<details>
<summary>Message Batches API에서 프롬프트 캐싱을 사용할 수 있나요?</summary>

예, Message Batches API에서 프롬프트 캐싱을 사용할 수 있습니다. 그러나 비동기 배치 요청은 동시에 그리고 임의의 순서로 처리될 수 있으므로 캐시 적중은 최선의 노력을 기반으로 제공됩니다.
</details>

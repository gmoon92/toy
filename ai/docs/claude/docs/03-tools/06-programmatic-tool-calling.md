# [프로그래밍 방식의 도구 호출](https://platform.claude.com/docs/en/agents-and-tools/tool-use/programmatic-tool-calling)

---

프로그래밍 방식의 도구 호출을 사용하면 Claude가 각 도구 호출마다 모델을 거치는 대신, [코드 실행](../03-tools/05-code-execution-tool.md) 컨테이너 내에서 프로그래밍 방식으로 도구를 호출하는 코드를 작성할 수 있습니다. 이를 통해 다중 도구 워크플로의 지연 시간을 줄이고, Claude가 데이터를 모델의 컨텍스트 윈도우에 도달하기 전에 필터링하거나 처리할 수 있도록 하여 토큰 소비를 줄일 수 있습니다.


> 프로그래밍 방식의 도구 호출은 현재 공개 베타 단계입니다.
>
> 이 기능을 사용하려면 API 요청에 `"advanced-tool-use-2025-11-20"` [베타 헤더](https://platform.claude.com/docs/en/api/beta-headers)를 추가하세요.
>
> 이 기능을 사용하려면 코드 실행 도구가 활성화되어 있어야 합니다.
>
> 이 기능에 대한 피드백을 공유하려면 [피드백 양식](https://forms.gle/MVGTnrHe73HpMiho8)을 통해 연락해 주세요.


## 모델 호환성

프로그래밍 방식의 도구 호출은 다음 모델에서 사용할 수 있습니다:

| 모델 | 도구 버전 |
|-------|--------------|
| Claude Opus 4.5 (`claude-opus-4-5-20251101`) | `code_execution_20250825` |
| Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`) | `code_execution_20250825` |


> 프로그래밍 방식의 도구 호출은 Claude API 및 Microsoft Foundry를 통해 사용할 수 있습니다.


## 빠른 시작

다음은 Claude가 프로그래밍 방식으로 데이터베이스를 여러 번 쿼리하고 결과를 집계하는 간단한 예시입니다:

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: advanced-tool-use-2025-11-20" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [
            {
                "role": "user",
                "content": "서부, 동부, 중부 지역의 판매 데이터를 쿼리한 다음 어느 지역이 가장 높은 수익을 올렸는지 알려줘"
            }
        ],
        "tools": [
            {
                "type": "code_execution_20250825",
                "name": "code_execution"
            },
            {
                "name": "query_database",
                "description": "판매 데이터베이스에 대해 SQL 쿼리를 실행합니다. JSON 객체로 행 목록을 반환합니다.",
                "input_schema": {
                    "type": "object",
                    "properties": {
                        "sql": {
                            "type": "string",
                            "description": "실행할 SQL 쿼리"
                        }
                    },
                    "required": ["sql"]
                },
                "allowed_callers": ["code_execution_20250825"]
            }
        ]
    }'
```

</details>

## 프로그래밍 방식의 도구 호출 작동 원리

코드 실행에서 호출 가능하도록 도구를 설정하고 Claude가 해당 도구를 사용하기로 결정하면:

1. Claude는 도구를 함수로 호출하는 Python 코드를 작성합니다. 여기에는 여러 도구 호출과 사전/사후 처리 로직이 포함될 수 있습니다
2. Claude는 코드 실행을 통해 샌드박스 컨테이너에서 이 코드를 실행합니다
3. 도구 함수가 호출되면 코드 실행이 일시 중지되고 API가 `tool_use` 블록을 반환합니다
4. 도구 결과를 제공하면 코드 실행이 계속됩니다(중간 결과는 Claude의 컨텍스트 윈도우에 로드되지 않습니다)
5. 모든 코드 실행이 완료되면 Claude는 최종 출력을 받고 작업을 계속합니다

이 접근 방식은 다음과 같은 경우에 특히 유용합니다:
- **대용량 데이터 처리**: 도구 결과가 Claude의 컨텍스트에 도달하기 전에 필터링하거나 집계
- **다단계 워크플로**: 도구 호출 사이에 Claude를 샘플링하지 않고 도구를 순차적으로 또는 루프로 호출하여 토큰과 지연 시간 절약
- **조건부 로직**: 중간 도구 결과를 기반으로 의사 결정 수행


> 사용자 정의 도구는 병렬 도구 호출을 지원하기 위해 비동기 Python 함수로 변환됩니다. Claude가 도구를 호출하는 코드를 작성할 때 `await`를 사용하며(예: `result = await query_database("<sql>")`) 적절한 비동기 래퍼 함수를 자동으로 포함합니다.
>
> 명확성을 위해 이 문서의 코드 예제에서는 비동기 래퍼가 생략되었습니다.


## 핵심 개념

### `allowed_callers` 필드

`allowed_callers` 필드는 어떤 컨텍스트가 도구를 호출할 수 있는지 지정합니다:

```json
{
  "name": "query_database",
  "description": "데이터베이스에 대해 SQL 쿼리를 실행합니다",
  "input_schema": {...},
  "allowed_callers": ["code_execution_20250825"]
}
```

**가능한 값:**
- `["direct"]` - Claude만 이 도구를 직접 호출할 수 있습니다(생략 시 기본값)
- `["code_execution_20250825"]` - 코드 실행 내에서만 호출 가능
- `["direct", "code_execution_20250825"]` - 직접 호출과 코드 실행에서 모두 호출 가능


> 각 도구에 대해 둘 다 활성화하기보다는 `["direct"]` 또는 `["code_execution_20250825"]` 중 하나를 선택하는 것이 좋습니다. 이렇게 하면 Claude에게 도구를 최적으로 사용하는 방법에 대한 더 명확한 지침을 제공할 수 있습니다.


### 응답의 `caller` 필드

모든 도구 사용 블록에는 호출 방법을 나타내는 `caller` 필드가 포함됩니다:

**직접 호출(전통적인 도구 사용):**
```json
{
  "type": "tool_use",
  "id": "toolu_abc123",
  "name": "query_database",
  "input": {"sql": "<sql>"},
  "caller": {"type": "direct"}
}
```

**프로그래밍 방식 호출:**
```json
{
  "type": "tool_use",
  "id": "toolu_xyz789",
  "name": "query_database",
  "input": {"sql": "<sql>"},
  "caller": {
    "type": "code_execution_20250825",
    "tool_id": "srvtoolu_abc123"
  }
}
```

`tool_id`는 프로그래밍 방식 호출을 수행한 코드 실행 도구를 참조합니다.

### 컨테이너 생명주기

프로그래밍 방식의 도구 호출은 코드 실행과 동일한 컨테이너를 사용합니다:

- **컨테이너 생성**: 기존 컨테이너를 재사용하지 않는 한 각 세션마다 새 컨테이너가 생성됩니다
- **만료**: 컨테이너는 약 4.5분의 비활성 시간 후에 만료됩니다(변경될 수 있음)
- **컨테이너 ID**: 응답의 `container` 필드를 통해 반환됩니다
- **재사용**: 컨테이너 ID를 전달하여 요청 간에 상태를 유지합니다


> 도구가 프로그래밍 방식으로 호출되고 컨테이너가 도구 결과를 기다리는 경우, 컨테이너가 만료되기 전에 응답해야 합니다. `expires_at` 필드를 모니터링하세요. 컨테이너가 만료되면 Claude는 도구 호출이 시간 초과된 것으로 간주하고 재시도할 수 있습니다.


## 예제 워크플로

다음은 완전한 프로그래밍 방식의 도구 호출 흐름이 어떻게 작동하는지 보여줍니다:

### 1단계: 초기 요청

코드 실행과 프로그래밍 방식 호출을 허용하는 도구를 포함한 요청을 보냅니다. 프로그래밍 방식 호출을 활성화하려면 도구 정의에 `allowed_callers` 필드를 추가하세요.


> 도구 설명에 도구의 출력 형식에 대한 자세한 설명을 제공하세요. 도구가 JSON을 반환한다고 지정하면 Claude는 코드에서 결과를 역직렬화하고 처리하려고 시도합니다. 출력 스키마에 대해 더 자세히 제공할수록 Claude가 응답을 프로그래밍 방식으로 더 잘 처리할 수 있습니다.


<details>
<summary>Python 예시</summary>

```python
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["advanced-tool-use-2025-11-20"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "지난 분기의 고객 구매 이력을 쿼리하고 수익 기준으로 상위 5명의 고객을 식별해줘"
    }],
    tools=[
        {
            "type": "code_execution_20250825",
            "name": "code_execution"
        },
        {
            "name": "query_database",
            "description": "판매 데이터베이스에 대해 SQL 쿼리를 실행합니다. JSON 객체로 행 목록을 반환합니다.",
            "input_schema": {...},
            "allowed_callers": ["code_execution_20250825"]
        }
    ]
)
```

</details>

### 2단계: 도구 호출이 포함된 API 응답

Claude가 도구를 호출하는 코드를 작성합니다. API가 일시 중지되고 다음을 반환합니다:

```json
{
  "role": "assistant",
  "content": [
    {
      "type": "text",
      "text": "구매 이력을 쿼리하고 결과를 분석하겠습니다."
    },
    {
      "type": "server_tool_use",
      "id": "srvtoolu_abc123",
      "name": "code_execution",
      "input": {
        "code": "results = await query_database('<sql>')\ntop_customers = sorted(results, key=lambda x: x['revenue'], reverse=True)[:5]\nprint(f'상위 5명의 고객: {top_customers}')"
      }
    },
    {
      "type": "tool_use",
      "id": "toolu_def456",
      "name": "query_database",
      "input": {"sql": "<sql>"},
      "caller": {
        "type": "code_execution_20250825",
        "tool_id": "srvtoolu_abc123"
      }
    }
  ],
  "container": {
    "id": "container_xyz789",
    "expires_at": "2025-01-15T14:30:00Z"
  },
  "stop_reason": "tool_use"
}
```

### 3단계: 도구 결과 제공

전체 대화 기록과 도구 결과를 포함합니다:

<details>
<summary>Python 예시</summary>

```python
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["advanced-tool-use-2025-11-20"],
    max_tokens=4096,
    container="container_xyz789",  # 컨테이너 재사용
    messages=[
        {"role": "user", "content": "지난 분기의 고객 구매 이력을 쿼리하고 수익 기준으로 상위 5명의 고객을 식별해줘"},
        {
            "role": "assistant",
            "content": [
                {"type": "text", "text": "구매 이력을 쿼리하고 결과를 분석하겠습니다."},
                {
                    "type": "server_tool_use",
                    "id": "srvtoolu_abc123",
                    "name": "code_execution",
                    "input": {"code": "..."}
                },
                {
                    "type": "tool_use",
                    "id": "toolu_def456",
                    "name": "query_database",
                    "input": {"sql": "<sql>"},
                    "caller": {
                        "type": "code_execution_20250825",
                        "tool_id": "srvtoolu_abc123"
                    }
                }
            ]
        },
        {
            "role": "user",
            "content": [
                {
                    "type": "tool_result",
                    "tool_use_id": "toolu_def456",
                    "content": "[{\"customer_id\": \"C1\", \"revenue\": 45000}, {\"customer_id\": \"C2\", \"revenue\": 38000}, ...]"
                }
            ]
        }
    ],
    tools=[...]
)
```

</details>

### 4단계: 다음 도구 호출 또는 완료

코드 실행이 계속되고 결과를 처리합니다. 추가 도구 호출이 필요한 경우 모든 도구 호출이 만족될 때까지 3단계를 반복합니다.

### 5단계: 최종 응답

코드 실행이 완료되면 Claude는 최종 응답을 제공합니다:

```json
{
  "content": [
    {
      "type": "code_execution_tool_result",
      "tool_use_id": "srvtoolu_abc123",
      "content": {
        "type": "code_execution_result",
        "stdout": "수익 기준 상위 5명의 고객:\n1. 고객 C1: $45,000\n2. 고객 C2: $38,000\n3. 고객 C5: $32,000\n4. 고객 C8: $28,500\n5. 고객 C3: $24,000",
        "stderr": "",
        "return_code": 0,
        "content": []
      }
    },
    {
      "type": "text",
      "text": "지난 분기의 구매 이력을 분석했습니다. 상위 5명의 고객이 총 $167,500의 수익을 창출했으며, 고객 C1이 $45,000로 선두를 달리고 있습니다."
    }
  ],
  "stop_reason": "end_turn"
}
```

## 고급 패턴

### 루프를 사용한 일괄 처리

Claude는 여러 항목을 효율적으로 처리하는 코드를 작성할 수 있습니다:

```python
# 명확성을 위해 비동기 래퍼 생략
regions = ["서부", "동부", "중부", "북부", "남부"]
results = {}
for region in regions:
    data = await query_database(f"<{region}에 대한 sql>")
    results[region] = sum(row["revenue"] for row in data)

# 결과를 프로그래밍 방식으로 처리
top_region = max(results.items(), key=lambda x: x[1])
print(f"상위 지역: {top_region[0]}, 수익 ${top_region[1]:,}")
```

이 패턴은:
- 모델 왕복을 N회(지역당 1회)에서 1회로 줄입니다
- Claude로 반환하기 전에 프로그래밍 방식으로 대용량 결과 집합을 처리합니다
- 원시 데이터 대신 집계된 결론만 반환하여 토큰을 절약합니다

### 조기 종료

Claude는 성공 기준이 충족되는 즉시 처리를 중단할 수 있습니다:

```python
# 명확성을 위해 비동기 래퍼 생략
endpoints = ["us-east", "eu-west", "apac"]
for endpoint in endpoints:
    status = await check_health(endpoint)
    if status == "healthy":
        print(f"정상 엔드포인트 발견: {endpoint}")
        break  # 조기 중단, 나머지는 확인하지 않음
```

### 조건부 도구 선택

```python
# 명확성을 위해 비동기 래퍼 생략
file_info = await get_file_info(path)
if file_info["size"] < 10000:
    content = await read_full_file(path)
else:
    content = await read_file_summary(path)
print(content)
```

### 데이터 필터링

```python
# 명확성을 위해 비동기 래퍼 생략
logs = await fetch_logs(server_id)
errors = [log for log in logs if "ERROR" in log]
print(f"{len(errors)}개의 오류 발견")
for error in errors[-10:]:  # 마지막 10개 오류만 반환
    print(error)
```

## 응답 형식

### 프로그래밍 방식 도구 호출

코드 실행이 도구를 호출하는 경우:

```json
{
  "type": "tool_use",
  "id": "toolu_abc123",
  "name": "query_database",
  "input": {"sql": "<sql>"},
  "caller": {
    "type": "code_execution_20250825",
    "tool_id": "srvtoolu_xyz789"
  }
}
```

### 도구 결과 처리

도구 결과는 실행 중인 코드로 다시 전달됩니다:

```json
{
  "role": "user",
  "content": [
    {
      "type": "tool_result",
      "tool_use_id": "toolu_abc123",
      "content": "[{\"customer_id\": \"C1\", \"revenue\": 45000, \"orders\": 23}, {\"customer_id\": \"C2\", \"revenue\": 38000, \"orders\": 18}, ...]"
    }
  ]
}
```

### 코드 실행 완료

모든 도구 호출이 만족되고 코드가 완료되면:

```json
{
  "type": "code_execution_tool_result",
  "tool_use_id": "srvtoolu_xyz789",
  "content": {
    "type": "code_execution_result",
    "stdout": "분석 완료. 총 847개 레코드에서 상위 5명의 고객이 식별되었습니다.",
    "stderr": "",
    "return_code": 0,
    "content": []
  }
}
```

## 오류 처리

### 일반적인 오류

| 오류 | 설명 | 해결 방법 |
|-------|-------------|----------|
| `invalid_tool_input` | 도구 입력이 스키마와 일치하지 않음 | 도구의 input_schema를 확인하세요 |
| `tool_not_allowed` | 도구가 요청된 호출자 유형을 허용하지 않음 | `allowed_callers`에 올바른 컨텍스트가 포함되어 있는지 확인하세요 |
| `missing_beta_header` | PTC 베타 헤더가 제공되지 않음 | 요청에 베타 헤더를 추가하세요 |

### 도구 호출 중 컨테이너 만료

도구 응답이 너무 오래 걸리면 코드 실행에서 `TimeoutError`가 발생합니다. Claude는 stderr에서 이를 확인하고 일반적으로 재시도합니다:

```json
{
  "type": "code_execution_tool_result",
  "tool_use_id": "srvtoolu_abc123",
  "content": {
    "type": "code_execution_result",
    "stdout": "",
    "stderr": "TimeoutError: 도구 ['query_database'] 호출 시간 초과.",
    "return_code": 0,
    "content": []
  }
}
```

시간 초과를 방지하려면:
- 응답의 `expires_at` 필드를 모니터링하세요
- 도구 실행에 대한 시간 초과를 구현하세요
- 긴 작업을 더 작은 청크로 나누는 것을 고려하세요

### 도구 실행 오류

도구가 오류를 반환하는 경우:

```python
# 도구 결과에 오류 정보 제공
{
    "type": "tool_result",
    "tool_use_id": "toolu_abc123",
    "content": "오류: 쿼리 시간 초과 - 테이블 잠금이 30초를 초과했습니다"
}
```

Claude의 코드는 이 오류를 받고 적절하게 처리할 수 있습니다.

## 제약 사항 및 제한 사항

### 기능 비호환성

- **구조화된 출력**: `strict: true`가 있는 도구는 프로그래밍 방식 호출과 함께 사용할 수 없습니다
- **도구 선택**: `tool_choice`를 통해 특정 도구의 프로그래밍 방식 호출을 강제할 수 없습니다
- **병렬 도구 사용**: `disable_parallel_tool_use: true`는 프로그래밍 방식 호출과 함께 사용할 수 없습니다

### 도구 제한 사항

다음 도구는 현재 프로그래밍 방식으로 호출할 수 없지만 향후 릴리스에서 지원이 추가될 수 있습니다:

- Web search
- Web fetch
- [MCP 커넥터](./06-mcp-in-api-01-mcp-connector.md)에서 제공하는 도구

### 메시지 형식 제한 사항

프로그래밍 방식 도구 호출에 응답할 때 엄격한 형식 요구 사항이 있습니다:

**도구 결과 전용 응답**: 결과를 기다리는 보류 중인 프로그래밍 방식 도구 호출이 있는 경우, 응답 메시지에는 **오직** `tool_result` 블록만 포함되어야 합니다. 도구 결과 이후에도 텍스트 콘텐츠를 포함할 수 없습니다.

```json
// ❌ 잘못됨 - 프로그래밍 방식 도구 호출에 응답할 때 텍스트를 포함할 수 없음
{
  "role": "user",
  "content": [
    {"type": "tool_result", "tool_use_id": "toolu_01", "content": "[{\"customer_id\": \"C1\", \"revenue\": 45000}]"},
    {"type": "text", "text": "다음에 무엇을 해야 하나요?"}  // 오류 발생
  ]
}

// ✅ 올바름 - 프로그래밍 방식 도구 호출에 응답할 때 도구 결과만 포함
{
  "role": "user",
  "content": [
    {"type": "tool_result", "tool_use_id": "toolu_01", "content": "[{\"customer_id\": \"C1\", \"revenue\": 45000}]"}
  ]
}
```

이 제한은 프로그래밍 방식(코드 실행) 도구 호출에 응답할 때만 적용됩니다. 일반적인 클라이언트 측 도구 호출의 경우 도구 결과 이후에 텍스트 콘텐츠를 포함할 수 있습니다.

### 속도 제한

프로그래밍 방식 도구 호출은 일반 도구 호출과 동일한 속도 제한이 적용됩니다. 코드 실행의 각 도구 호출은 별도의 호출로 계산됩니다.

### 사용 전 도구 결과 검증

프로그래밍 방식으로 호출될 사용자 정의 도구를 구현할 때:

- **도구 결과는 문자열로 반환됩니다**: 실행 환경에서 처리될 수 있는 코드 스니펫이나 실행 가능한 명령을 포함한 모든 콘텐츠를 포함할 수 있습니다.
- **외부 도구 결과 검증**: 도구가 외부 소스의 데이터를 반환하거나 사용자 입력을 받는 경우, 출력이 코드로 해석되거나 실행될 경우 코드 인젝션 위험을 인지하세요.

## 토큰 효율성

프로그래밍 방식의 도구 호출은 토큰 소비를 크게 줄일 수 있습니다:

- **프로그래밍 방식 호출의 도구 결과는 Claude의 컨텍스트에 추가되지 않습니다** - 최종 코드 출력만 추가됩니다
- **중간 처리가 코드에서 발생합니다** - 필터링, 집계 등이 모델 토큰을 소비하지 않습니다
- **하나의 코드 실행에서 여러 도구 호출** - 별도의 모델 턴에 비해 오버헤드를 줄입니다

예를 들어, 10개의 도구를 직접 호출하면 프로그래밍 방식으로 호출하고 요약을 반환하는 것보다 약 10배의 토큰을 사용합니다.

## 사용량 및 가격

프로그래밍 방식의 도구 호출은 코드 실행과 동일한 가격을 사용합니다. 자세한 내용은 [코드 실행 가격](../03-tools/05-code-execution-tool.md)을 참조하세요.


> 프로그래밍 방식 도구 호출의 토큰 카운팅: 프로그래밍 방식 호출의 도구 결과는 입력/출력 토큰 사용량에 포함되지 않습니다. 최종 코드 실행 결과와 Claude의 응답만 계산됩니다.


## 모범 사례

### 도구 설계

- **자세한 출력 설명 제공**: Claude가 코드에서 도구 결과를 역직렬화하므로 형식(JSON 구조, 필드 유형 등)을 명확하게 문서화하세요
- **구조화된 데이터 반환**: JSON 또는 기타 쉽게 구문 분석할 수 있는 형식이 프로그래밍 방식 처리에 가장 적합합니다
- **응답을 간결하게 유지**: 처리 오버헤드를 최소화하기 위해 필요한 데이터만 반환하세요

### 프로그래밍 방식 호출을 사용해야 하는 경우

**좋은 사용 사례:**
- 집계나 요약만 필요한 대용량 데이터셋 처리
- 3개 이상의 종속 도구 호출이 있는 다단계 워크플로
- 도구 결과의 필터링, 정렬 또는 변환이 필요한 작업
- 중간 데이터가 Claude의 추론에 영향을 미치지 않아야 하는 작업
- 많은 항목에 대한 병렬 작업(예: 50개 엔드포인트 확인)

**덜 이상적인 사용 사례:**
- 간단한 응답이 있는 단일 도구 호출
- 즉각적인 사용자 피드백이 필요한 도구
- 코드 실행 오버헤드가 이점보다 클 정도로 매우 빠른 작업

### 성능 최적화

- 관련된 여러 요청을 수행할 때 **컨테이너를 재사용**하여 상태를 유지하세요
- 가능한 경우 단일 코드 실행에서 **유사한 작업을 일괄 처리**하세요

## 문제 해결

### 일반적인 문제

**"Tool not allowed" 오류**
- 도구 정의에 `"allowed_callers": ["code_execution_20250825"]`가 포함되어 있는지 확인하세요
- 올바른 베타 헤더를 사용하고 있는지 확인하세요

**컨테이너 만료**
- 컨테이너 수명(약 4.5분) 내에 도구 호출에 응답하는지 확인하세요
- 응답의 `expires_at` 필드를 모니터링하세요
- 더 빠른 도구 실행 구현을 고려하세요

**베타 헤더 문제**
- 헤더 필요: `"advanced-tool-use-2025-11-20"`

**도구 결과가 올바르게 구문 분석되지 않음**
- 도구가 Claude가 역직렬화할 수 있는 문자열 데이터를 반환하는지 확인하세요
- 도구 설명에 명확한 출력 형식 문서를 제공하세요

### 디버깅 팁

1. **모든 도구 호출 및 결과를 로깅**하여 흐름을 추적하세요
2. **`caller` 필드를 확인**하여 프로그래밍 방식 호출을 확인하세요
3. **컨테이너 ID를 모니터링**하여 적절한 재사용을 보장하세요
4. 프로그래밍 방식 호출을 활성화하기 전에 **도구를 독립적으로 테스트**하세요

## 프로그래밍 방식의 도구 호출이 작동하는 이유

Claude의 훈련에는 코드에 대한 광범위한 노출이 포함되어 있어 함수 호출을 추론하고 연결하는 데 효과적입니다. 도구가 코드 실행 환경 내에서 호출 가능한 함수로 제공되면 Claude는 이러한 강점을 활용하여 다음을 수행할 수 있습니다:

- **도구 구성에 대해 자연스럽게 추론**: Python 코드를 작성하는 것처럼 자연스럽게 작업을 연결하고 종속성을 처리합니다
- **대용량 결과를 효율적으로 처리**: 대용량 도구 출력을 필터링하거나, 관련 데이터만 추출하거나, 컨텍스트 윈도우로 요약을 반환하기 전에 중간 결과를 파일에 씁니다
- **지연 시간을 크게 줄입니다**: 다단계 워크플로에서 각 도구 호출 사이에 Claude를 재샘플링하는 오버헤드를 제거합니다

이 접근 방식을 사용하면 전통적인 도구 사용으로는 실행 불가능한 워크플로(예: 1M 토큰 이상의 파일 처리)를 가능하게 합니다. Claude가 모든 것을 대화 컨텍스트에 로드하는 대신 프로그래밍 방식으로 데이터를 작업할 수 있기 때문입니다.

## 대체 구현

프로그래밍 방식의 도구 호출은 Anthropic의 관리형 코드 실행 외부에서 구현할 수 있는 일반화 가능한 패턴입니다. 다음은 접근 방식의 개요입니다:

### 클라이언트 측 직접 실행

Claude에게 코드 실행 도구를 제공하고 해당 환경에서 사용 가능한 함수를 설명합니다. Claude가 코드로 도구를 호출하면 애플리케이션이 해당 함수가 정의된 로컬에서 실행합니다.

**장점:**
- 최소한의 재구성으로 간단하게 구현
- 환경 및 지침에 대한 완전한 제어

**단점:**
- 샌드박스 외부에서 신뢰할 수 없는 코드 실행
- 도구 호출이 코드 인젝션의 벡터가 될 수 있음

**사용 시기:** 애플리케이션이 임의의 코드를 안전하게 실행할 수 있고, 간단한 솔루션을 원하며, Anthropic의 관리형 제품이 요구 사항에 맞지 않을 때.

### 자체 관리형 샌드박스 실행

Claude의 관점에서는 동일한 접근 방식이지만 코드는 보안 제한(예: 네트워크 송신 없음)이 있는 샌드박스 컨테이너에서 실행됩니다. 도구에 외부 리소스가 필요한 경우 샌드박스 외부에서 도구 호출을 실행하기 위한 프로토콜이 필요합니다.

**장점:**
- 자체 인프라에서 안전한 프로그래밍 방식의 도구 호출
- 실행 환경에 대한 완전한 제어

**단점:**
- 구축 및 유지 관리가 복잡함
- 인프라와 프로세스 간 통신을 모두 관리해야 함

**사용 시기:** 보안이 중요하고 Anthropic의 관리형 솔루션이 요구 사항에 맞지 않을 때.

### Anthropic 관리형 실행

Anthropic의 프로그래밍 방식의 도구 호출은 Claude에 최적화된 독창적인 Python 환경을 갖춘 관리형 버전의 샌드박스 실행입니다. Anthropic은 컨테이너 관리, 코드 실행 및 안전한 도구 호출 통신을 처리합니다.

**장점:**
- 기본적으로 안전하고 보안적
- 최소한의 설정으로 쉽게 활성화
- Claude에 최적화된 환경 및 지침

Claude API를 사용하는 경우 Anthropic의 관리형 솔루션 사용을 권장합니다.

## 관련 기능


  
> 프로그래밍 방식의 도구 호출을 지원하는 기본 코드 실행 기능에 대해 알아보세요.

  
> Claude의 도구 사용 기본 사항을 이해하세요.

  
> 도구 구현을 위한 단계별 가이드입니다.



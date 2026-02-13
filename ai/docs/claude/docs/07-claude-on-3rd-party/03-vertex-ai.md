# [Vertex AI에서 Claude 사용하기](https://platform.claude.com/docs/en/build-with-claude/claude-on-vertex-ai)

Anthropic의 Claude 모델은 이제 [Vertex AI](https://cloud.google.com/vertex-ai)를 통해 일반적으로 사용할 수 있습니다.

---

Claude에 접근하기 위한 Vertex API는 [Messages API](https://platform.claude.com/docs/en/api/messages)와 거의 동일하며 동일한 옵션을 모두 지원하지만,
두 가지 주요 차이점이 있습니다:

* Vertex에서 `model`은 요청 본문에 전달되지 않습니다. 대신 Google Cloud 엔드포인트 URL에 지정됩니다.
* Vertex에서 `anthropic_version`은 (헤더가 아닌) 요청 본문에 전달되며, `vertex-2023-10-16` 값으로 설정해야 합니다.

Vertex는 Anthropic의 공식 [클라이언트 SDK](https://platform.claude.com/docs/en/api/client-sdks)에서도 지원됩니다. 이 가이드는 Python 또는
TypeScript에서 Vertex AI의 Claude에 요청하는 과정을 안내합니다.

이 가이드는 Vertex AI를 사용할 수 있는 GCP 프로젝트가 이미 있다고 가정합니다. 필요한 설정 및 전체 안내에 대한 자세한
내용은 [Anthropic의 Claude 3 모델 사용하기](https://cloud.google.com/vertex-ai/generative-ai/docs/partner-models/use-claude)를
참조하세요.

## Vertex AI 접근을 위한 SDK 설치

먼저, 선택한 언어에 맞는 Anthropic의 [클라이언트 SDK](https://platform.claude.com/docs/en/api/client-sdks)를 설치합니다.

<details>
<summary>Python 예시</summary>

```python
pip install -U google-cloud-aiplatform "anthropic[vertex]"
```

</details>

## Vertex AI 접근하기

### 모델 가용성

Anthropic 모델 가용성은 지역에 따라 다릅니다. 최신 정보는 [Vertex AI Model Garden](https://cloud.google.com/model-garden)에서 "Claude"를
검색하거나 [Claude 3 사용하기](https://cloud.google.com/vertex-ai/generative-ai/docs/partner-models/use-claude)를 참조하세요.

#### API 모델 ID

| 모델                                                                                       | Vertex AI API 모델 ID        |
|------------------------------------------------------------------------------------------|----------------------------|
| Claude Sonnet 4.5                                                                        | claude-sonnet-4-5@20250929 |
| Claude Sonnet 4                                                                          | claude-sonnet-4@20250514   |
| Claude Sonnet 3.7 <Tooltip tooltipContent="2025년 10월 28일부터 더 이상 사용되지 않습니다.">⚠️</Tooltip> | claude-3-7-sonnet@20250219 |
| Claude Opus 4.5                                                                          | claude-opus-4-5@20251101   |
| Claude Opus 4.1                                                                          | claude-opus-4-1@20250805   |
| Claude Opus 4                                                                            | claude-opus-4@20250514     |
| Claude Haiku 4.5                                                                         | claude-haiku-4-5@20251001  |
| Claude Haiku 3.5 <Tooltip tooltipContent="2025년 12월 19일부터 더 이상 사용되지 않습니다.">⚠️</Tooltip>  | claude-3-5-haiku@20241022  |
| Claude Haiku 3                                                                           | claude-3-haiku@20240307    |

### 요청 만들기

요청을 실행하기 전에 GCP로 인증하려면 `gcloud auth application-default login`을 실행해야 할 수 있습니다.

다음 예제는 Vertex AI에서 Claude로부터 텍스트를 생성하는 방법을 보여줍니다:
<details>
<summary>REST API 예시</summary>

```bash
MODEL_ID=claude-sonnet-4-5@20250929
  LOCATION=global
  PROJECT_ID=MY_PROJECT_ID

  curl \
  -X POST \
  -H "Authorization: Bearer $(gcloud auth print-access-token)" \
  -H "Content-Type: application/json" \
  https://$LOCATION-aiplatform.googleapis.com/v1/projects/${PROJECT_ID}/locations/${LOCATION}/publishers/anthropic/models/${MODEL_ID}:streamRawPredict -d \
  '{
    "anthropic_version": "vertex-2023-10-16",
    "messages": [{
      "role": "user",
      "content": "Hey Claude!"
    }],
    "max_tokens": 100,
  }'
```

</details>

자세한 내용은 [클라이언트 SDK](https://platform.claude.com/docs/en/api/client-sdks) 및
공식 [Vertex AI 문서](https://cloud.google.com/vertex-ai/docs)를 참조하세요.

## 활동 로깅

Vertex는 사용과 관련된 프롬프트 및 완성을 로깅할 수
있는 [요청-응답 로깅 서비스](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/request-response-logging)를 제공합니다.

Anthropic은 활동을 이해하고 잠재적인 오용을 조사하기 위해 최소 30일 롤링 기준으로 활동을 로깅할 것을 권장합니다.


> 이 서비스를 켜는 것이 Google이나 Anthropic에게 귀하의 콘텐츠에 대한 액세스 권한을 부여하지 않습니다.

## 기능 지원

Vertex에서 현재 지원되는 모든 기능은 [여기](https://platform.claude.com/docs/en/api/overview)에서 찾을 수 있습니다.

## 글로벌 vs 지역 엔드포인트

**Claude Sonnet 4.5 및 향후 모든 모델**부터 Google Vertex AI는 두 가지 엔드포인트 유형을 제공합니다:

- **글로벌 엔드포인트**: 최대 가용성을 위한 동적 라우팅
- **지역 엔드포인트**: 특정 지리적 지역을 통한 보장된 데이터 라우팅

지역 엔드포인트는 글로벌 엔드포인트 대비 10% 가격 프리미엄이 포함됩니다.


> 이는 Claude Sonnet 4.5 및 향후 모델에만 적용됩니다. 이전 모델(Claude Sonnet 4, Opus 4 및 이전 버전)은 기존 가격 구조를 유지합니다.

### 각 옵션을 사용해야 하는 경우

**글로벌 엔드포인트(권장):**

- 최대 가용성 및 가동 시간 제공
- 사용 가능한 용량이 있는 지역으로 요청을 동적으로 라우팅
- 가격 프리미엄 없음
- 데이터 거주지가 유연한 애플리케이션에 가장 적합
- 사용량 기반 요금제 트래픽만 지원(프로비저닝된 처리량은 지역 엔드포인트 필요)

**지역 엔드포인트:**

- 특정 지리적 지역을 통해 트래픽 라우팅
- 데이터 거주지 및 규정 준수 요구 사항에 필요
- 사용량 기반 요금제 및 프로비저닝된 처리량 모두 지원
- 10% 가격 프리미엄은 전용 지역 용량을 위한 인프라 비용을 반영

### 구현

**글로벌 엔드포인트 사용(권장):**

클라이언트를 초기화할 때 `region` 매개변수를 `"global"`로 설정합니다:

<details>
<summary>Python 예시</summary>

```python
from anthropic import AnthropicVertex

project_id = "MY_PROJECT_ID"
region = "global"

client = AnthropicVertex(project_id=project_id, region=region)

message = client.messages.create(
    model="claude-sonnet-4-5@20250929",
    max_tokens=100,
    messages=[
        {
            "role": "user",
            "content": "Hey Claude!",
        }
    ],
)
print(message)
```

</details>

**지역 엔드포인트 사용:**

`"us-east1"` 또는 `"europe-west1"`과 같은 특정 지역을 지정합니다:

<details>
<summary>Python 예시</summary>

```python
from anthropic import AnthropicVertex

project_id = "MY_PROJECT_ID"
region = "us-east1"  # 특정 지역 지정

client = AnthropicVertex(project_id=project_id, region=region)

message = client.messages.create(
    model="claude-sonnet-4-5@20250929",
    max_tokens=100,
    messages=[
        {
            "role": "user",
            "content": "Hey Claude!",
        }
    ],
)
print(message)
```

</details>

### 추가 리소스

- **Google Vertex AI 가격:
  ** [cloud.google.com/vertex-ai/generative-ai/pricing](https://cloud.google.com/vertex-ai/generative-ai/pricing)
- **Claude 모델 문서:** [Vertex AI의 Claude](https://cloud.google.com/vertex-ai/generative-ai/docs/partner-models/claude)
- **Google 블로그 게시물:
  ** [Claude 모델을 위한 글로벌 엔드포인트](https://cloud.google.com/blog/products/ai-machine-learning/global-endpoint-for-claude-models-generally-available-on-vertex-ai)
- **Anthropic 가격 세부정보:** [가격 문서](https://platform.claude.com/docs/en/about-claude/pricing#third-party-platform-pricing)

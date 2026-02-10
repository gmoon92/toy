# [Amazon Bedrock에서 Claude 사용하기](https://platform.claude.com/docs/en/build-with-claude/claude-on-amazon-bedrock)

Anthropic의 Claude 모델은 이제 Amazon Bedrock을 통해 정식으로 제공됩니다.

---

Bedrock을 통해 Claude를 호출하는 방식은 Anthropic의 클라이언트 SDK를 사용할 때와 약간 다릅니다. 이 가이드는 Python 또는 TypeScript를 사용하여 Bedrock에서 Claude API 호출을 완료하는 과정을 안내합니다.

이 가이드는 이미 [AWS 계정](https://portal.aws.amazon.com/billing/signup)에 가입하고 프로그래밍 방식 액세스를 구성했다고 가정합니다.

## AWS CLI 설치 및 구성

1. 버전 `2.13.23` 이상의 [AWS CLI를 설치](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-welcome.html)합니다.
2. AWS configure 명령을 사용하여 AWS 자격 증명을 구성하거나([AWS CLI 구성](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html) 참조), AWS 대시보드 내에서 "Command line or programmatic access"로 이동하여 팝업 모달의 지침에 따라 자격 증명을 찾습니다.
3. 자격 증명이 올바르게 작동하는지 확인합니다.

```bash Shell
aws sts get-caller-identity
```

## Bedrock 액세스를 위한 SDK 설치

Anthropic의 [클라이언트 SDK](https://platform.claude.com/docs/en/api/client-sdks)는 Bedrock을 지원합니다. 또한 `boto3`와 같은 AWS SDK를 직접 사용할 수도 있습니다.

<details>
<summary>Python 예시</summary>

```python
pip install -U "anthropic[bedrock]"
```

</details>

## Bedrock 액세스

### Anthropic 모델 구독

[AWS Console > Bedrock > Model Access](https://console.aws.amazon.com/bedrock/home?region=us-west-2#/modelaccess)로 이동하여 Anthropic 모델에 대한 액세스를 요청합니다. Anthropic 모델의 가용성은 리전마다 다릅니다. 최신 정보는 [AWS 문서](https://docs.aws.amazon.com/bedrock/latest/userguide/models-regions.html)를 참조하세요.

#### API 모델 ID

| 모델 | 기본 Bedrock 모델 ID | `global` | `us` | `eu` | `jp` | `apac` |
| :---- | :---- | :---- | :---- | :---- | :---- | :---- |
| Claude Sonnet 4.5 | anthropic.claude-sonnet-4-5-20250929-v1:0 | 예 | 예 | 예 | 예 | 아니오 |
| Claude Sonnet 4 | anthropic.claude-sonnet-4-20250514-v1:0 | 예 | 예 | 예 | 아니오 | 예 |
| Claude Sonnet 3.7 <Tooltip tooltipContent="2025년 10월 28일부터 지원 종료.">⚠️</Tooltip> | anthropic.claude-3-7-sonnet-20250219-v1:0 | 아니오 | 예 | 예 | 아니오 | 예 |
| Claude Opus 4.5 | anthropic.claude-opus-4-5-20251101-v1:0 | 예 | 예 | 예 | 아니오 | 아니오 |
| Claude Opus 4.1 | anthropic.claude-opus-4-1-20250805-v1:0 | 아니오 | 예 | 아니오 | 아니오 | 아니오 |
| Claude Opus 4 | anthropic.claude-opus-4-20250514-v1:0 | 아니오 | 예 | 아니오 | 아니오 | 아니오 |
| Claude Haiku 4.5 | anthropic.claude-haiku-4-5-20251001-v1:0 | 예 | 예 | 예 | 아니오 | 아니오 |
| Claude Haiku 3.5 <Tooltip tooltipContent="2025년 12월 19일부터 지원 종료.">⚠️</Tooltip> | anthropic.claude-3-5-haiku-20241022-v1:0 | 아니오 | 예 | 아니오 | 아니오 | 아니오 |
| Claude Haiku 3 | anthropic.claude-3-haiku-20240307-v1:0 | 아니오 | 예 | 예 | 아니오 | 예 |

리전별 모델 ID와 글로벌 모델 ID에 대한 자세한 내용은 아래의 [글로벌 vs 리전별 엔드포인트](#global-vs-regional-endpoints) 섹션을 참조하세요.

### 사용 가능한 모델 목록 조회

다음 예제는 Bedrock을 통해 사용 가능한 모든 Claude 모델 목록을 출력하는 방법을 보여줍니다.

<details>
<summary>REST API 예시</summary>

```bash
CLI
  aws bedrock list-foundation-models --region=us-west-2 --by-provider anthropic --query "modelSummaries[*].modelId"
```

</details>

### 요청 보내기

다음 예제는 Bedrock에서 Claude를 사용하여 텍스트를 생성하는 방법을 보여줍니다.

<details>
<summary>Python 예시</summary>

```python
from anthropic import AnthropicBedrock

  client = AnthropicBedrock(
      # 아래 키를 제공하여 인증하거나 ~/.aws/credentials 사용 또는
      # "AWS_SECRET_ACCESS_KEY" 및 "AWS_ACCESS_KEY_ID" 환경 변수와 같은
      # 기본 AWS 자격 증명 공급자를 사용합니다.
      aws_access_key="<access key>",
      aws_secret_key="<secret key>",
      # aws_session_token을 사용하여 임시 자격 증명을 사용할 수 있습니다.
      # 자세한 내용은 https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_temp.html을 참조하세요.
      aws_session_token="<session_token>",
      # aws_region은 요청이 전송되는 AWS 리전을 변경합니다. 기본적으로 AWS_REGION을 읽으며,
      # 없는 경우 us-east-1을 기본값으로 사용합니다. ~/.aws/config에서 리전을 읽지 않습니다.
      aws_region="us-west-2",
  )

  message = client.messages.create(
      model="global.anthropic.claude-sonnet-4-5-20250929-v1:0",
      max_tokens=256,
      messages=[{"role": "user", "content": "Hello, world"}]
  )
  print(message.content)
```

</details>

자세한 내용은 [클라이언트 SDK](https://platform.claude.com/docs/en/api/client-sdks)를 참조하고, 공식 Bedrock 문서는 [여기](https://docs.aws.amazon.com/bedrock/)에서 확인할 수 있습니다.

## 활동 로깅

Bedrock은 사용량과 관련된 프롬프트 및 완성을 로깅할 수 있는 [호출 로깅 서비스](https://docs.aws.amazon.com/bedrock/latest/userguide/model-invocation-logging.html)를 제공합니다.

Anthropic은 활동을 이해하고 잠재적인 오용을 조사하기 위해 최소 30일 단위로 활동을 로깅할 것을 권장합니다.


> 이 서비스를 켜도 AWS나 Anthropic이 귀하의 콘텐츠에 액세스할 수 없습니다.


## 기능 지원
Bedrock에서 현재 지원되는 모든 기능은 [여기](https://platform.claude.com/docs/en/api/overview)에서 확인할 수 있습니다.

### Bedrock의 PDF 지원

PDF 지원은 Converse API와 InvokeModel API를 모두 통해 Amazon Bedrock에서 사용할 수 있습니다. PDF 처리 기능 및 제한 사항에 대한 자세한 내용은 [PDF 지원 문서](../02-capabilities/12-pdf-support.md)를 참조하세요.

**Converse API 사용자를 위한 중요 고려 사항:**
- 시각적 PDF 분석(차트, 이미지, 레이아웃)을 사용하려면 인용(citations)을 활성화해야 합니다.
- 인용 없이는 기본적인 텍스트 추출만 가능합니다.
- 강제 인용 없이 완전한 제어가 필요한 경우 InvokeModel API를 사용하세요.

두 가지 문서 처리 모드와 제한 사항에 대한 자세한 내용은 [PDF 지원 가이드](../02-capabilities/12-pdf-support.md)를 참조하세요.

### 1M 토큰 컨텍스트 윈도우

Claude Sonnet 4 및 4.5는 Amazon Bedrock에서 [1M 토큰 컨텍스트 윈도우](../01-build-with-claude/02-context-windows.md)를 지원합니다.


> 1M 토큰 컨텍스트 윈도우는 현재 베타 버전입니다. 확장된 컨텍스트 윈도우를 사용하려면 [Bedrock API 요청](https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-anthropic-claude-messages-request-response.html)에 `context-1m-2025-08-07` 베타 헤더를 포함해야 합니다.


## 글로벌 vs 리전별 엔드포인트

**Claude Sonnet 4.5 및 향후 모든 모델**부터 Amazon Bedrock은 두 가지 엔드포인트 유형을 제공합니다.

- **글로벌 엔드포인트**: 최대 가용성을 위한 동적 라우팅
- **리전별 엔드포인트**: 특정 지리적 리전을 통한 데이터 라우팅 보장

리전별 엔드포인트는 글로벌 엔드포인트 대비 10%의 가격 프리미엄이 적용됩니다.


> 이는 Claude Sonnet 4.5 및 향후 모델에만 적용됩니다. 이전 모델(Claude Sonnet 4, Opus 4 및 그 이전 버전)은 기존 가격 구조를 유지합니다.


### 각 옵션을 사용해야 하는 경우

**글로벌 엔드포인트(권장):**
- 최대 가용성과 가동 시간 제공
- 사용 가능한 용량이 있는 리전으로 요청을 동적으로 라우팅
- 가격 프리미엄 없음
- 데이터 거주 요구사항이 유연한 애플리케이션에 최적

**리전별 엔드포인트(CRIS):**
- 특정 지리적 리전을 통해 트래픽 라우팅
- 데이터 거주 및 규정 준수 요구사항에 필수
- 미국, EU, 일본, 호주에서 사용 가능
- 10% 가격 프리미엄은 전용 리전별 용량에 대한 인프라 비용을 반영

### 구현

**글로벌 엔드포인트 사용(Sonnet 4.5 및 4의 기본값):**

Claude Sonnet 4.5 및 4의 모델 ID는 이미 `global.` 접두사를 포함합니다.

<details>
<summary>Python 예시</summary>

```python
from anthropic import AnthropicBedrock

client = AnthropicBedrock(aws_region="us-west-2")

message = client.messages.create(
    model="global.anthropic.claude-sonnet-4-5-20250929-v1:0",
    max_tokens=256,
    messages=[{"role": "user", "content": "Hello, world"}]
)
```

</details>

**리전별 엔드포인트 사용(CRIS):**

리전별 엔드포인트를 사용하려면 모델 ID에서 `global.` 접두사를 제거합니다.

<details>
<summary>Python 예시</summary>

```python
from anthropic import AnthropicBedrock

client = AnthropicBedrock(aws_region="us-west-2")

# US 리전별 엔드포인트(CRIS) 사용
message = client.messages.create(
    model="anthropic.claude-sonnet-4-5-20250929-v1:0",  # global. 접두사 없음
    max_tokens=256,
    messages=[{"role": "user", "content": "Hello, world"}]
)
```

</details>

### 추가 리소스

- **AWS Bedrock 가격:** [aws.amazon.com/bedrock/pricing](https://aws.amazon.com/bedrock/pricing/)
- **AWS 가격 문서:** [Bedrock 가격 가이드](https://docs.aws.amazon.com/bedrock/latest/userguide/bedrock-pricing.html)
- **AWS 블로그 포스트:** [Amazon Bedrock에서 Claude Sonnet 4.5 소개](https://aws.amazon.com/blogs/aws/introducing-claude-sonnet-4-5-in-amazon-bedrock-anthropics-most-intelligent-model-best-for-coding-and-complex-agents/)
- **Anthropic 가격 세부 정보:** [가격 문서](https://platform.claude.com/docs/en/about-claude/pricing#third-party-platform-pricing)

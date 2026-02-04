# [Microsoft Foundry에서 Claude 사용하기](https://platform.claude.com/docs/en/build-with-claude/claude-in-microsoft-foundry)

Azure 네이티브 엔드포인트와 인증을 통해 Microsoft Foundry에서 Claude 모델에 액세스하세요.

---

이 가이드는 Python, TypeScript 또는 직접 HTTP 요청을 사용하여 Foundry에서 Claude로 API 호출을 설정하고 실행하는 과정을 안내합니다. Foundry에서 Claude에 액세스하면 Azure 구독으로 Microsoft Marketplace에서 Claude 사용에 대한 비용이 청구되며, Azure 구독을 통해 비용을 관리하면서 Claude의 최신 기능에 액세스할 수 있습니다.

지역 가용성: 출시 시점에 Claude는 Foundry 리소스에서 Global Standard 배포 유형으로 사용할 수 있습니다(US DataZone은 곧 출시 예정). Microsoft Marketplace의 Claude 가격은 Anthropic의 표준 API 가격을 사용합니다. 자세한 내용은 [가격 페이지](https://claude.com/pricing#api)를 참조하세요.

## 프리뷰

이 프리뷰 플랫폼 통합에서 Claude 모델은 Anthropic의 인프라에서 실행됩니다. 이는 Azure를 통한 청구 및 액세스를 위한 상용 통합입니다. Microsoft의 독립 프로세서로서, Microsoft Foundry를 통해 Claude를 사용하는 고객은 Anthropic의 데이터 사용 약관을 따릅니다. Anthropic은 제로 데이터 보유 가용성을 포함하여 업계 최고 수준의 안전 및 데이터 약속을 계속 제공합니다.

## 전제 조건

시작하기 전에 다음이 필요합니다:

- 활성 Azure 구독
- [Foundry](https://ai.azure.com/)에 대한 액세스 권한
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) 설치 (선택 사항, 리소스 관리용)

## SDK 설치

Anthropic의 [클라이언트 SDK](https://platform.claude.com/docs/en/api/client-sdks)는 플랫폼별 패키지를 통해 Foundry를 지원합니다.

<Tabs>
<Tab title="Python">
```bash
pip install -U "anthropic"
```
</Tab>

<Tab title="TypeScript">
```bash
npm install @anthropic-ai/foundry-sdk
```
</Tab>

<Tab title="C#">
```bash
dotnet add package Anthropic.Foundry
```
</Tab>
</Tabs>

## 프로비저닝

Foundry는 2단계 계층 구조를 사용합니다: **리소스**에는 보안 및 청구 구성이 포함되고, **배포**는 API를 통해 호출하는 모델 인스턴스입니다. 먼저 Foundry 리소스를 생성한 다음 그 안에 하나 이상의 Claude 배포를 생성합니다.

### Foundry 리소스 프로비저닝

Azure에서 서비스를 사용하고 관리하는 데 필요한 Foundry 리소스를 생성합니다. 다음 지침에 따라 [Foundry 리소스](https://learn.microsoft.com/en-us/azure/ai-services/multi-service-resource?pivots=azportal#create-a-new-azure-ai-foundry-resource)를 생성할 수 있습니다. 또는 [Foundry 프로젝트](https://learn.microsoft.com/en-us/azure/ai-foundry/how-to/create-projects?tabs=ai-foundry)를 생성하여 시작할 수 있으며, 이는 Foundry 리소스 생성을 포함합니다.

리소스를 프로비저닝하려면:

1. [Foundry 포털](https://ai.azure.com/)로 이동합니다
2. 새 Foundry 리소스를 생성하거나 기존 리소스를 선택합니다
3. Azure에서 발급한 API 키 또는 역할 기반 액세스 제어를 위한 Entra ID를 사용하여 액세스 관리를 구성합니다
4. 보안 강화를 위해 선택적으로 리소스를 프라이빗 네트워크(Azure Virtual Network)의 일부로 구성합니다
5. 리소스 이름을 기록합니다. API 엔드포인트에서 `{resource}`로 사용합니다(예: `https://{resource}.services.ai.azure.com/anthropic/v1/*`)

### Foundry 배포 생성

리소스를 생성한 후 Claude 모델을 배포하여 API 호출에 사용할 수 있도록 합니다:

1. Foundry 포털에서 리소스로 이동합니다
2. **Models + endpoints**로 이동하고 **+ Deploy model** > **Deploy base model**을 선택합니다
3. Claude 모델을 검색하고 선택합니다(예: `claude-sonnet-4-5`)
4. 배포 설정을 구성합니다:
   - **Deployment name**: 기본값은 모델 ID이지만 사용자 정의할 수 있습니다(예: `my-claude-deployment`). 배포 이름은 생성 후 변경할 수 없습니다.
   - **Deployment type**: Global Standard 선택(Claude에 권장)
5. **Deploy**를 선택하고 프로비저닝이 완료될 때까지 기다립니다
6. 배포가 완료되면 **Keys and Endpoint**에서 엔드포인트 URL과 키를 찾을 수 있습니다


> 선택한 배포 이름은 API 요청의 `model` 매개변수에 전달하는 값이 됩니다. 동일한 모델에 대해 다른 이름으로 여러 배포를 생성하여 별도의 구성 또는 속도 제한을 관리할 수 있습니다.


## 인증

Foundry의 Claude는 API 키와 Entra ID 토큰의 두 가지 인증 방법을 지원합니다. 두 방법 모두 `https://{resource}.services.ai.azure.com/anthropic/v1/*` 형식의 Azure 호스팅 엔드포인트를 사용합니다.

### API 키 인증

Foundry Claude 리소스를 프로비저닝한 후 Foundry 포털에서 API 키를 얻을 수 있습니다:

1. Foundry 포털에서 리소스로 이동합니다
2. **Keys and Endpoint** 섹션으로 이동합니다
3. 제공된 API 키 중 하나를 복사합니다
4. 요청에서 `api-key` 또는 `x-api-key` 헤더를 사용하거나 SDK에 제공합니다

Python 및 TypeScript SDK는 API 키와 리소스 이름 또는 base URL이 필요합니다. 다음 환경 변수가 정의되어 있으면 SDK가 자동으로 읽습니다:

- `ANTHROPIC_FOUNDRY_API_KEY` - API 키
- `ANTHROPIC_FOUNDRY_RESOURCE` - 리소스 이름(예: `example-resource`)
- `ANTHROPIC_FOUNDRY_BASE_URL` - 리소스 이름의 대안; 전체 base URL(예: `https://example-resource.services.ai.azure.com/anthropic/`)


> `resource`와 `base_url` 매개변수는 상호 배타적입니다. 리소스 이름(SDK가 `https://{resource}.services.ai.azure.com/anthropic/`로 URL을 구성하는 데 사용) 또는 전체 base URL을 직접 제공하세요.


**API 키 사용 예시:**

<CodeGroup>
```python Python
import os
from anthropic import AnthropicFoundry

client = AnthropicFoundry(
    api_key=os.environ.get("ANTHROPIC_FOUNDRY_API_KEY"),
    resource='example-resource', # your resource name
)

message = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[{"role": "user", "content": "Hello!"}]
)
print(message.content)
```

```typescript TypeScript
import AnthropicFoundry from "@anthropic-ai/foundry-sdk";

const client = new AnthropicFoundry({
  apiKey: process.env.ANTHROPIC_FOUNDRY_API_KEY,
  resource: 'example-resource', // your resource name
});

const message = await client.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  messages: [{ role: "user", content: "Hello!" }],
});
console.log(message.content);
```

```bash Shell
curl https://{resource}.services.ai.azure.com/anthropic/v1/messages \
  -H "content-type: application/json" \
  -H "api-key: YOUR_AZURE_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
      {"role": "user", "content": "Hello!"}
    ]
  }'
```
</CodeGroup>


> API 키를 안전하게 보관하세요. 버전 관리 시스템에 커밋하거나 공개적으로 공유하지 마세요. API 키에 액세스할 수 있는 사람은 누구나 Foundry 리소스를 통해 Claude에 요청할 수 있습니다.


## Microsoft Entra 인증

보안 강화 및 중앙 집중식 액세스 관리를 위해 Entra ID(이전의 Azure Active Directory) 토큰을 사용할 수 있습니다:

1. Foundry 리소스에 대해 Entra 인증을 활성화합니다
2. Entra ID에서 액세스 토큰을 얻습니다
3. `Authorization: Bearer {TOKEN}` 헤더에서 토큰을 사용합니다

**Entra ID 사용 예시:**

<CodeGroup>
```python Python
import os
from anthropic import AnthropicFoundry
from azure.identity import DefaultAzureCredential, get_bearer_token_provider

# Get Azure Entra ID token using token provider pattern
token_provider = get_bearer_token_provider(
    DefaultAzureCredential(),
    "https://cognitiveservices.azure.com/.default"
)

# Create client with Entra ID authentication
client = AnthropicFoundry(
    resource='example-resource', # your resource name
    azure_ad_token_provider=token_provider  # Use token provider for Entra ID auth
)

# Make request
message = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[{"role": "user", "content": "Hello!"}]
)
print(message.content)
```

```typescript TypeScript
import AnthropicFoundry from "@anthropic-ai/foundry-sdk";
import {
  DefaultAzureCredential,
  getBearerTokenProvider,
} from "@azure/identity";

// Get Entra ID token using token provider pattern
const credential = new DefaultAzureCredential();
const tokenProvider = getBearerTokenProvider(
  credential,
  "https://cognitiveservices.azure.com/.default"
);

// Create client with Entra ID authentication
const client = new AnthropicFoundry({
  resource: 'example-resource', // your resource name
  azureADTokenProvider: tokenProvider, // Use token provider for Entra ID auth
});

// Make request
const message = await client.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  messages: [{ role: "user", content: "Hello!" }],
});
console.log(message.content);
```

```bash Shell
# Get Azure Entra ID token
ACCESS_TOKEN=$(az account get-access-token --resource https://cognitiveservices.azure.com --query accessToken -o tsv)

# Make request with token. Replace {resource} with your resource name
curl https://{resource}.services.ai.azure.com/anthropic/v1/messages \
  -H "content-type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
      {"role": "user", "content": "Hello!"}
    ]
  }'
```
</CodeGroup>


> Azure Entra ID 인증을 사용하면 Azure RBAC를 사용하여 액세스를 관리하고, 조직의 ID 관리와 통합하며, API 키를 수동으로 관리하는 것을 피할 수 있습니다.


## 상관 관계 요청 ID

Foundry는 디버깅 및 추적을 위해 HTTP 응답 헤더에 요청 식별자를 포함합니다. 지원팀에 문의할 때 `request-id`와 `apim-request-id` 값을 모두 제공하면 팀이 Anthropic 및 Azure 시스템 모두에서 요청을 신속하게 찾아 조사하는 데 도움이 됩니다.

## 지원되는 기능

Foundry의 Claude는 Claude의 강력한 기능 대부분을 지원합니다. 현재 지원되는 모든 기능은 [개요 문서](./01-build-with-claude-01-features-overview.md)에서 확인할 수 있습니다.

### 지원되지 않는 기능

- Admin API (`/v1/organizations/*` 엔드포인트)
- Models API (`/v1/models`)
- Message Batch API (`/v1/messages/batches`)

## API 응답

Foundry의 Claude API 응답은 표준 [Claude API 응답 형식](https://platform.claude.com/docs/en/api/messages)을 따릅니다. 여기에는 응답 본문의 `usage` 객체가 포함되며, 요청에 대한 자세한 토큰 소비 정보를 제공합니다. `usage` 객체는 모든 플랫폼(자체 API, Foundry, Amazon Bedrock, Google Vertex AI)에서 일관됩니다.

Foundry에만 해당하는 응답 헤더에 대한 자세한 내용은 [상관 관계 요청 ID 섹션](#correlation-request-ids)을 참조하세요.

## API 모델 ID 및 배포

다음 Claude 모델은 Foundry를 통해 사용할 수 있습니다. 최신 세대 모델(Sonnet 4.5, Opus 4.1, Haiku 4.5)은 가장 고급 기능을 제공합니다:

| 모델              | 기본 배포 이름              |
| :---------------- | :-------------------------- |
| Claude Opus 4.5   | `claude-opus-4-5`           |
| Claude Sonnet 4.5 | `claude-sonnet-4-5`         |
| Claude Opus 4.1   | `claude-opus-4-1`           |
| Claude Haiku 4.5  | `claude-haiku-4-5`          |

기본적으로 배포 이름은 위에 표시된 모델 ID와 일치합니다. 그러나 Foundry 포털에서 다른 구성, 버전 또는 속도 제한을 관리하기 위해 다른 이름으로 사용자 정의 배포를 생성할 수 있습니다. API 요청에서 배포 이름(반드시 모델 ID가 아님)을 사용하세요.

## 모니터링 및 로깅

Azure는 표준 Azure 패턴을 통해 Claude 사용에 대한 포괄적인 모니터링 및 로깅 기능을 제공합니다:

- **Azure Monitor**: API 사용, 지연 시간 및 오류율 추적
- **Azure Log Analytics**: 요청/응답 로그 쿼리 및 분석
- **Cost Management**: Claude 사용과 관련된 비용 모니터링 및 예측

Anthropic은 사용 패턴을 이해하고 잠재적인 문제를 조사하기 위해 최소 30일의 롤링 기간으로 활동을 로깅할 것을 권장합니다.


> Azure의 로깅 서비스는 Azure 구독 내에서 구성됩니다. 로깅을 활성화해도 청구 및 서비스 운영에 필요한 것 이상으로 Microsoft 또는 Anthropic에 콘텐츠에 대한 액세스 권한이 제공되지 않습니다.


## 문제 해결

### 인증 오류

**오류**: `401 Unauthorized` 또는 `Invalid API key`

- **해결책**: API 키가 올바른지 확인하세요. Claude 리소스의 **Keys and Endpoint**에서 Azure 포털에서 새 API 키를 얻을 수 있습니다.
- **해결책**: Azure Entra ID를 사용하는 경우 액세스 토큰이 유효하고 만료되지 않았는지 확인하세요. 토큰은 일반적으로 1시간 후에 만료됩니다.

**오류**: `403 Forbidden`

- **해결책**: Azure 계정에 필요한 권한이 없을 수 있습니다. 적절한 Azure RBAC 역할이 할당되었는지 확인하세요(예: "Cognitive Services OpenAI User").

### 속도 제한

**오류**: `429 Too Many Requests`

- **해결책**: 속도 제한을 초과했습니다. 애플리케이션에서 지수 백오프 및 재시도 로직을 구현하세요.
- **해결책**: Azure 포털 또는 Azure 지원을 통해 속도 제한 증가를 요청하는 것을 고려하세요.

#### 속도 제한 헤더

Foundry는 응답에 Anthropic의 표준 속도 제한 헤더(`anthropic-ratelimit-tokens-limit`, `anthropic-ratelimit-tokens-remaining`, `anthropic-ratelimit-tokens-reset`, `anthropic-ratelimit-input-tokens-limit`, `anthropic-ratelimit-input-tokens-remaining`, `anthropic-ratelimit-input-tokens-reset`, `anthropic-ratelimit-output-tokens-limit`, `anthropic-ratelimit-output-tokens-remaining`, `anthropic-ratelimit-output-tokens-reset`)를 포함하지 않습니다. 대신 Azure의 모니터링 도구를 통해 속도 제한을 관리하세요.

### 모델 및 배포 오류

**오류**: `Model not found` 또는 `Deployment not found`

- **해결책**: 올바른 배포 이름을 사용하고 있는지 확인하세요. 사용자 정의 배포를 생성하지 않은 경우 기본 모델 ID를 사용하세요(예: `claude-sonnet-4-5`).
- **해결책**: Azure 리전에서 모델/배포를 사용할 수 있는지 확인하세요.

**오류**: `Invalid model parameter`

- **해결책**: model 매개변수에는 Foundry 포털에서 사용자 정의할 수 있는 배포 이름이 포함되어야 합니다. 배포가 존재하고 올바르게 구성되어 있는지 확인하세요.

## 추가 리소스

- **Foundry 문서**: [ai.azure.com/catalog](https://ai.azure.com/catalog/publishers/anthropic)
- **Azure 가격**: [azure.microsoft.com/en-us/pricing](https://azure.microsoft.com/en-us/pricing/)
- **Anthropic 가격 세부정보**: [가격 문서](https://platform.claude.com/docs/en/about-claude/pricing#third-party-platform-pricing)
- **인증 가이드**: 위의 [인증 섹션](#authentication) 참조
- **Azure 포털**: [portal.azure.com](https://portal.azure.com/)

# [Claude Code Analytics API](https://platform.claude.com/docs/en/build-with-claude/claude-code-analytics-api)

Claude Code Analytics Admin API를 통해 조직의 Claude Code 사용 분석 및 생산성 지표에 프로그래밍 방식으로 접근하세요.

---


> **Admin API는 개인 계정에서는 사용할 수 없습니다.** 팀원과 협업하고 멤버를 추가하려면 **Console → Settings → Organization**에서 조직을 설정하세요.


Claude Code Analytics Admin API는 Claude Code 사용자의 일일 집계 사용 지표에 프로그래밍 방식으로 접근할 수 있게 해주며, 조직이 개발자 생산성을 분석하고 맞춤 대시보드를 구축할 수
있도록 합니다. 이 API는 기본 [Analytics 대시보드](/claude-code)와 복잡한 OpenTelemetry 통합 사이의 격차를 메워줍니다.

이 API를 통해 Claude Code 도입을 더 효과적으로 모니터링하고, 분석하고, 최적화할 수 있습니다:

* **개발자 생산성 분석:** Claude Code를 사용하여 생성된 세션, 추가/제거된 코드 라인 수, 커밋, 풀 리퀘스트를 추적합니다
* **도구 사용 지표:** 다양한 Claude Code 도구(Edit, Write, NotebookEdit)에 대한 승인 및 거부 비율을 모니터링합니다
* **비용 분석:** Claude 모델별로 세분화된 예상 비용과 토큰 사용량을 확인합니다
* **맞춤 리포팅:** 경영진 대시보드와 관리 팀용 리포트를 구축하기 위해 데이터를 내보냅니다
* **사용 근거 제시:** Claude Code 도입을 내부적으로 정당화하고 확장하기 위한 지표를 제공합니다

<Check>
  **Admin API 키 필요**

이 API는 [Admin API](../11-administration-monitoring/01-admin-api-overview.md)의 일부입니다. 이 엔드포인트는 표준 API 키와 다른 Admin API 키(
`sk-ant-admin...`으로 시작)가 필요합니다. 관리자 역할을 가진 조직 멤버만 [Claude Console](/settings/admin-keys)을 통해 Admin API 키를 발급할 수 있습니다.
</Check>

## 빠른 시작

특정 날짜의 조직 Claude Code 분석 데이터를 가져옵니다:

```bash
curl "https://api.anthropic.com/v1/organizations/usage_report/claude_code?\
starting_at=2025-09-08&\
limit=20" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

> **통합을 위해 User-Agent 헤더 설정**
>
> 통합을 구축하는 경우, 사용 패턴을 이해하는 데 도움이 되도록 User-Agent 헤더를 설정하세요:
> ```
> User-Agent: YourApp/1.0.0 (https://yourapp.com)
> ```

## Claude Code Analytics API

`/v1/organizations/usage_report/claude_code` 엔드포인트를 통해 조직 전체의 Claude Code 사용량, 생산성 지표 및 개발자 활동을 추적합니다.

### 주요 개념

- **일일 집계**: `starting_at` 매개변수로 지정된 하루의 지표를 반환합니다
- **사용자 수준 데이터**: 각 레코드는 지정된 날짜의 한 사용자의 활동을 나타냅니다
- **생산성 지표**: 세션, 코드 라인 수, 커밋, 풀 리퀘스트 및 도구 사용량을 추적합니다
- **토큰 및 비용 데이터**: Claude 모델별로 세분화된 사용량과 예상 비용을 모니터링합니다
- **커서 기반 페이지네이션**: 불투명한 커서를 사용하여 대용량 데이터셋을 안정적으로 페이지네이션합니다
- **데이터 신선도**: 일관성을 위해 최대 1시간 지연으로 지표를 사용할 수 있습니다

전체 매개변수 세부 정보 및 응답
스키마는 [Claude Code Analytics API 참조](https://platform.claude.com/docs/en/api/admin-api/claude-code/get-claude-code-usage-report)
를 참조하세요.

### 기본 예제

#### 특정 날짜의 분석 데이터 가져오기

```bash
curl "https://api.anthropic.com/v1/organizations/usage_report/claude_code?\
starting_at=2025-09-08" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

#### 페이지네이션을 사용한 분석 데이터 가져오기

```bash
# 첫 번째 요청
curl "https://api.anthropic.com/v1/organizations/usage_report/claude_code?\
starting_at=2025-09-08&\
limit=20" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"

# 응답의 커서를 사용한 후속 요청
curl "https://api.anthropic.com/v1/organizations/usage_report/claude_code?\
starting_at=2025-09-08&\
page=page_MjAyNS0wNS0xNFQwMDowMDowMFo=" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

### 요청 매개변수

| 매개변수          | 타입      | 필수  | 설명                                      |
|---------------|---------|-----|-----------------------------------------|
| `starting_at` | string  | Yes | YYYY-MM-DD 형식의 UTC 날짜. 해당 하루의 지표만 반환합니다 |
| `limit`       | integer | No  | 페이지당 레코드 수(기본값: 20, 최대: 1000)           |
| `page`        | string  | No  | 이전 응답의 `next_page` 필드에서 받은 불투명한 커서 토큰   |

### 사용 가능한 지표

각 응답 레코드는 하루 동안 한 사용자에 대한 다음 지표를 포함합니다:

#### 차원(Dimensions)

- **date**: RFC 3339 형식의 날짜(UTC 타임스탬프)
- **actor**: Claude Code 작업을 수행한 사용자 또는 API 키(`email_address`가 있는 `user_actor` 또는 `api_key_name`이 있는 `api_actor`)
- **organization_id**: 조직 UUID
- **customer_type**: 고객 계정 유형(API 고객의 경우 `api`, Pro/Team 고객의 경우 `subscription`)
- **terminal_type**: Claude Code가 사용된 터미널 또는 환경의 유형(예: `vscode`, `iTerm.app`, `tmux`)

#### 핵심 지표

- **num_sessions**: 이 액터가 시작한 개별 Claude Code 세션 수
- **lines_of_code.added**: Claude Code에 의해 모든 파일에 추가된 총 코드 라인 수
- **lines_of_code.removed**: Claude Code에 의해 모든 파일에서 제거된 총 코드 라인 수
- **commits_by_claude_code**: Claude Code의 커밋 기능을 통해 생성된 git 커밋 수
- **pull_requests_by_claude_code**: Claude Code의 PR 기능을 통해 생성된 풀 리퀘스트 수

#### 도구 작업 지표

도구 유형별 도구 작업 승인 및 거부 비율 세분화:

- **edit_tool.accepted/rejected**: 사용자가 승인/거부한 Edit 도구 제안 수
- **write_tool.accepted/rejected**: 사용자가 승인/거부한 Write 도구 제안 수
- **notebook_edit_tool.accepted/rejected**: 사용자가 승인/거부한 NotebookEdit 도구 제안 수

#### 모델 세분화

사용된 각 Claude 모델에 대해:

- **model**: Claude 모델 식별자(예: `claude-sonnet-4-5-20250929`)
- **tokens.input/output**: 이 모델의 입력 및 출력 토큰 수
- **tokens.cache_read/cache_creation**: 이 모델의 캐시 관련 토큰 사용량
- **estimated_cost.amount**: 이 모델에 대한 센트 단위 USD 예상 비용
- **estimated_cost.currency**: 비용 금액의 통화 코드(현재 항상 `USD`)

### 응답 구조

API는 다음 형식으로 데이터를 반환합니다:

```json
{
  "data": [
    {
      "date": "2025-09-01T00:00:00Z",
      "actor": {
        "type": "user_actor",
        "email_address": "developer@company.com"
      },
      "organization_id": "dc9f6c26-b22c-4831-8d01-0446bada88f1",
      "customer_type": "api",
      "terminal_type": "vscode",
      "core_metrics": {
        "num_sessions": 5,
        "lines_of_code": {
          "added": 1543,
          "removed": 892
        },
        "commits_by_claude_code": 12,
        "pull_requests_by_claude_code": 2
      },
      "tool_actions": {
        "edit_tool": {
          "accepted": 45,
          "rejected": 5
        },
        "multi_edit_tool": {
          "accepted": 12,
          "rejected": 2
        },
        "write_tool": {
          "accepted": 8,
          "rejected": 1
        },
        "notebook_edit_tool": {
          "accepted": 3,
          "rejected": 0
        }
      },
      "model_breakdown": [
        {
          "model": "claude-sonnet-4-5-20250929",
          "tokens": {
            "input": 100000,
            "output": 35000,
            "cache_read": 10000,
            "cache_creation": 5000
          },
          "estimated_cost": {
            "currency": "USD",
            "amount": 1025
          }
        }
      ]
    }
  ],
  "has_more": false,
  "next_page": null
}
```

## 페이지네이션

API는 많은 수의 사용자를 보유한 조직을 위한 커서 기반 페이지네이션을 지원합니다:

1. 선택적 `limit` 매개변수로 초기 요청을 수행합니다
2. 응답에서 `has_more`가 `true`인 경우, 다음 요청에서 `next_page` 값을 사용합니다
3. `has_more`가 `false`가 될 때까지 계속합니다

커서는 마지막 레코드의 위치를 인코딩하며 새 데이터가 도착하더라도 안정적인 페이지네이션을 보장합니다. 각 페이지네이션 세션은 레코드 누락이나 중복이 발생하지 않도록 일관된 데이터 경계를 유지합니다.

## 일반적인 사용 사례

- **경영진 대시보드**: 개발 속도에 대한 Claude Code의 영향을 보여주는 고수준 리포트 생성
- **AI 도구 비교**: Claude Code를 Copilot 및 Cursor와 같은 다른 AI 코딩 도구와 비교하기 위한 지표 내보내기
- **개발자 생산성 분석**: 시간에 따른 개인 및 팀 생산성 지표 추적
- **비용 추적 및 할당**: 지출 패턴 모니터링 및 팀 또는 프로젝트별 비용 할당
- **도입 모니터링**: 어떤 팀과 사용자가 Claude Code에서 가장 많은 가치를 얻고 있는지 식별
- **ROI 정당화**: Claude Code 도입을 내부적으로 정당화하고 확장하기 위한 구체적인 지표 제공

## 자주 묻는 질문

### 분석 데이터는 얼마나 최신입니까?

Claude Code 분석 데이터는 일반적으로 사용자 활동 완료 후 1시간 이내에 나타납니다. 일관된 페이지네이션 결과를 보장하기 위해 1시간이 지난 데이터만 응답에 포함됩니다.

### 실시간 지표를 얻을 수 있습니까?

아니요, 이 API는 일일 집계 지표만 제공합니다. 실시간 모니터링을 위해서는 [OpenTelemetry 통합](https://code.claude.com/docs/en/monitoring-usage) 사용을
고려하세요.

### 데이터에서 사용자는 어떻게 식별됩니까?

사용자는 `actor` 필드를 통해 두 가지 방식으로 식별됩니다:

- **`user_actor`**: OAuth를 통해 인증하는 사용자의 `email_address`를 포함합니다(가장 일반적)
- **`api_actor`**: API 키를 통해 인증하는 사용자의 `api_key_name`을 포함합니다

`customer_type` 필드는 사용이 `api` 고객(API PAYG)인지 `subscription` 고객(Pro/Team 플랜)인지를 나타냅니다.

### 데이터 보관 기간은 얼마입니까?

과거 Claude Code 분석 데이터는 보관되며 API를 통해 접근할 수 있습니다. 이 데이터에 대해 지정된 삭제 기간은 없습니다.

### 어떤 Claude Code 배포가 지원됩니까?

이 API는 Claude API(1st party)의 Claude Code 사용량만 추적합니다. Amazon Bedrock, Google Vertex AI 또는 기타 타사 플랫폼의 사용량은 포함되지 않습니다.

### 이 API를 사용하는 데 비용이 얼마입니까?

Claude Code Analytics API는 Admin API에 접근할 수 있는 모든 조직에 대해 무료로 사용할 수 있습니다.

### 도구 승인률은 어떻게 계산합니까?

도구 승인률 = 각 도구 유형에 대해 `accepted / (accepted + rejected)`입니다. 예를 들어, edit 도구가 45개 승인 및 5개 거부를 보여주는 경우, 승인률은 90%입니다.

### date 매개변수에 사용되는 시간대는 무엇입니까?

모든 날짜는 UTC입니다. `starting_at` 매개변수는 YYYY-MM-DD 형식이어야 하며 해당 날짜의 UTC 자정을 나타냅니다.

## 참고 항목

Claude Code Analytics API는 팀의 개발 워크플로를 이해하고 최적화하는 데 도움이 됩니다. 관련 기능에 대해 자세히 알아보세요:

- [Admin API 개요](../11-administration-monitoring/01-admin-api-overview.md)
- [Admin API 참조](https://platform.claude.com/docs/en/api/admin)
- [Claude Code Analytics 대시보드](/claude-code)
- [Usage and Cost API](../11-administration-monitoring/03-usage-cost-api.md) - 모든 Anthropic 서비스에서 API 사용량 추적
- [Identity and access management](https://code.claude.com/docs/en/iam)
- 맞춤 지표 및 알림을 위한 [OpenTelemetry로 사용량 모니터링](https://code.claude.com/docs/en/monitoring-usage)

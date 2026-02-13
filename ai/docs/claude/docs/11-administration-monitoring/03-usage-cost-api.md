# [사용량 및 비용 API](https://platform.claude.com/docs/en/build-with-claude/usage-cost-api)

사용량 및 비용 관리자 API를 통해 조직의 API 사용량과 비용 데이터에 프로그래밍 방식으로 접근할 수 있습니다.

---


> **관리자 API는 개인 계정에서는 사용할 수 없습니다.** 팀원과 협업하고 멤버를 추가하려면 **Console → Settings → Organization**에서 조직을 설정하세요.


사용량 및 비용 관리자 API는 조직의 과거 API 사용량과 비용 데이터에 대한 프로그래밍 방식의 세밀한 접근을 제공합니다. 이 데이터는 Claude Console의 [Usage](/usage)
및 [Cost](/cost) 페이지에서 제공되는 정보와 유사합니다.

이 API를 통해 Claude 구현을 더 효과적으로 모니터링하고, 분석하고, 최적화할 수 있습니다:

* **정확한 사용량 추적:** 응답 토큰 카운팅에만 의존하는 대신 정확한 토큰 수와 사용 패턴을 얻을 수 있습니다
* **비용 조정:** 재무 및 회계 팀을 위해 내부 기록과 Anthropic 청구서를 일치시킬 수 있습니다
* **제품 성능 및 개선:** 시스템 변경이 성능을 개선했는지 측정하면서 제품 성능을 모니터링하거나 알림을 설정할 수 있습니다
* **[속도 제한](https://platform.claude.com/docs/en/api/rate-limits)
  및 [우선순위 티어](https://platform.claude.com/docs/en/api/service-tiers#get-started-with-priority-tier) 최적화:
  ** [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md) 또는 특정 프롬프트와 같은 기능을 최적화하여 할당된 용량을 최대한 활용하거나 전용 용량을 구매할 수 있습니다
* **고급 분석:** Console에서 제공되는 것보다 더 깊은 데이터 분석을 수행할 수 있습니다

<Check>
  **관리자 API 키 필요**

이 API는 [관리자 API](../11-administration-monitoring/01-admin-api-overview.md)의 일부입니다. 이러한 엔드포인트는 표준 API 키와 다른 관리자 API 키(
`sk-ant-admin...`로 시작)가 필요합니다. 관리자 역할을 가진 조직 멤버만 [Claude Console](/settings/admin-keys)을 통해 관리자 API 키를 프로비저닝할 수 있습니다.
</Check>

## 파트너 솔루션

주요 관측성 플랫폼은 커스텀 코드 작성 없이 Claude API 사용량과 비용을 모니터링할 수 있는 바로 사용 가능한 통합을 제공합니다. 이러한 통합은 API 사용량을 효과적으로 관리하는 데 도움이 되는 대시보드,
알림 및 분석을 제공합니다.



> 비용 추적 및 예측을 위한 클라우드 인텔리전스 플랫폼


> 자동 추적 및 모니터링을 통한 LLM 관측성


> 즉시 사용 가능한 대시보드 및 알림을 제공하는 에이전트리스 통합으로 간편한 LLM 관측성 구현


> OpenTelemetry를 통한 고급 쿼리 및 시각화


> LLM 비용 및 사용량 관측성을 위한 FinOps 플랫폼

## 빠른 시작

지난 7일간의 조직 일일 사용량을 가져오기:

```bash
curl "https://api.anthropic.com/v1/organizations/usage_report/messages?\
starting_at=2025-01-08T00:00:00Z&\
ending_at=2025-01-15T00:00:00Z&\
bucket_width=1d" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

> **통합을 위한 User-Agent 헤더 설정**
>
> 통합을 구축하는 경우 사용 패턴을 이해하는 데 도움이 되도록 User-Agent 헤더를 설정하세요:
> ```
> User-Agent: YourApp/1.0.0 (https://yourapp.com)
> ```

## 사용량 API

`/v1/organizations/usage_report/messages` 엔드포인트를 통해 모델, 워크스페이스 및 서비스 티어별 세부 분석으로 조직 전체의 토큰 소비를 추적할 수 있습니다.

### 주요 개념

- **시간 버킷**: 고정된 간격(`1m`, `1h` 또는 `1d`)으로 사용량 데이터를 집계합니다
- **토큰 추적**: 캐시되지 않은 입력, 캐시된 입력, 캐시 생성 및 출력 토큰을 측정합니다
- **필터링 및 그룹화**: API 키, 워크스페이스, 모델, 서비스 티어 또는 컨텍스트 윈도우별로 필터링하고 이러한 차원으로 결과를 그룹화합니다
- **서버 도구 사용량**: 웹 검색과 같은 서버측 도구의 사용량을 추적합니다

전체 매개변수 세부 정보 및 응답
스키마는 [사용량 API 참조](https://platform.claude.com/docs/en/api/admin-api/usage-cost/get-messages-usage-report)를 참조하세요.

### 기본 예제

#### 모델별 일일 사용량

```bash
curl "https://api.anthropic.com/v1/organizations/usage_report/messages?\
starting_at=2025-01-01T00:00:00Z&\
ending_at=2025-01-08T00:00:00Z&\
group_by[]=model&\
bucket_width=1d" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

#### 필터링을 통한 시간별 사용량

```bash
curl "https://api.anthropic.com/v1/organizations/usage_report/messages?\
starting_at=2025-01-15T00:00:00Z&\
ending_at=2025-01-15T23:59:59Z&\
models[]=claude-sonnet-4-5-20250929&\
service_tiers[]=batch&\
context_window[]=0-200k&\
bucket_width=1h" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

#### API 키 및 워크스페이스별 사용량 필터링

```bash
curl "https://api.anthropic.com/v1/organizations/usage_report/messages?\
starting_at=2025-01-01T00:00:00Z&\
ending_at=2025-01-08T00:00:00Z&\
api_key_ids[]=apikey_01Rj2N8SVvo6BePZj99NhmiT&\
api_key_ids[]=apikey_01ABC123DEF456GHI789JKL&\
workspace_ids[]=wrkspc_01JwQvzr7rXLA5AGx3HKfFUJ&\
workspace_ids[]=wrkspc_01XYZ789ABC123DEF456MNO&\
bucket_width=1d" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

> 조직의 API 키 ID를 검색하려면 [List API Keys](https://platform.claude.com/docs/en/api/admin-api/apikeys/list-api-keys) 엔드포인트를
> 사용하세요.
>
> 조직의 워크스페이스 ID를 검색하려면 [List Workspaces](https://platform.claude.com/docs/en/api/admin-api/workspaces/list-workspaces)
> 엔드포인트를 사용하거나 Anthropic Console에서 조직의 워크스페이스 ID를 찾으세요.

### 시간 세분성 제한

| 세분성  | 기본 제한 | 최대 제한   | 사용 사례     |
|------|-------|---------|-----------|
| `1m` | 60 버킷 | 1440 버킷 | 실시간 모니터링  |
| `1h` | 24 버킷 | 168 버킷  | 일일 패턴     |
| `1d` | 7 버킷  | 31 버킷   | 주간/월간 보고서 |

## 비용 API

`/v1/organizations/cost_report` 엔드포인트를 통해 USD 단위의 서비스 수준 비용 분석을 검색할 수 있습니다.

### 주요 개념

- **통화**: 모든 비용은 USD로 표시되며, 최소 단위(센트)의 십진수 문자열로 보고됩니다
- **비용 유형**: 토큰 사용량, 웹 검색 및 코드 실행 비용을 추적합니다
- **그룹화**: 세부적인 분석을 위해 워크스페이스 또는 설명별로 비용을 그룹화합니다
- **시간 버킷**: 일일 세분성만 가능(`1d`)

전체 매개변수 세부 정보 및 응답 스키마는 [비용 API 참조](https://platform.claude.com/docs/en/api/admin-api/usage-cost/get-cost-report)를
참조하세요.


> 우선순위 티어 비용은 다른 청구 모델을 사용하며 비용 엔드포인트에 포함되지 않습니다. 대신 사용량 엔드포인트를 통해 우선순위 티어 사용량을 추적하세요.

### 기본 예제

```bash
curl "https://api.anthropic.com/v1/organizations/cost_report?\
starting_at=2025-01-01T00:00:00Z&\
ending_at=2025-01-31T00:00:00Z&\
group_by[]=workspace_id&\
group_by[]=description" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

## 페이지네이션

두 엔드포인트 모두 대용량 데이터셋에 대한 페이지네이션을 지원합니다:

1. 초기 요청을 수행합니다
2. `has_more`가 `true`인 경우 다음 요청에서 `next_page` 값을 사용합니다
3. `has_more`가 `false`가 될 때까지 계속합니다

```bash
# 첫 번째 요청
curl "https://api.anthropic.com/v1/organizations/usage_report/messages?\
starting_at=2025-01-01T00:00:00Z&\
ending_at=2025-01-31T00:00:00Z&\
limit=7" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"

# 응답에 포함됨: "has_more": true, "next_page": "page_xyz..."

# 페이지네이션을 사용한 다음 요청
curl "https://api.anthropic.com/v1/organizations/usage_report/messages?\
starting_at=2025-01-01T00:00:00Z&\
ending_at=2025-01-31T00:00:00Z&\
limit=7&\
page=page_xyz..." \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

## 일반적인 사용 사례

[Claude Cookbook](https://platform.claude.com/cookbooks)에서 자세한 구현을 살펴보세요:

- **일일 사용량 보고서**: 토큰 소비 추세 추적
- **비용 귀속**: 차지백을 위해 워크스페이스별로 비용 할당
- **캐시 효율성**: 프롬프트 캐싱 측정 및 최적화
- **예산 모니터링**: 지출 임계값에 대한 알림 설정
- **CSV 내보내기**: 재무 팀을 위한 보고서 생성

## 자주 묻는 질문

### 데이터는 얼마나 최신인가요?

사용량 및 비용 데이터는 일반적으로 API 요청 완료 후 5분 이내에 나타나지만, 때때로 지연이 더 길어질 수 있습니다.

### 권장 폴링 빈도는 얼마인가요?

API는 지속적인 사용을 위해 분당 1회 폴링을 지원합니다. 짧은 버스트(예: 페이지네이션된 데이터 다운로드)의 경우 더 빈번한 폴링이 허용됩니다. 자주 업데이트가 필요한 대시보드의 경우 결과를 캐시하세요.

### 코드 실행 사용량은 어떻게 추적하나요?

코드 실행 비용은 비용 엔드포인트에서 설명 필드의 `Code Execution Usage` 아래에 그룹화되어 나타납니다. 코드 실행은 사용량 엔드포인트에 포함되지 않습니다.

### 우선순위 티어 사용량은 어떻게 추적하나요?

사용량 엔드포인트에서 `service_tier`로 필터링하거나 그룹화하고 `priority` 값을 찾으세요. 우선순위 티어 비용은 비용 엔드포인트에서 사용할 수 없습니다.

### Workbench 사용량은 어떻게 처리되나요?

Workbench의 API 사용량은 API 키와 연결되지 않으므로 해당 차원으로 그룹화하더라도 `api_key_id`가 `null`이 됩니다.

### 기본 워크스페이스는 어떻게 표시되나요?

기본 워크스페이스에 귀속된 사용량 및 비용은 `workspace_id`에 대해 `null` 값을 가집니다.

### Claude Code의 사용자별 비용 분석은 어떻게 얻나요?

많은 API 키로 비용을 분석할 때의 성능 제한 없이 사용자별 추정 비용 및 생산성 지표를
제공하는 [Claude Code Analytics API](../11-administration-monitoring/04-claude-code-analytics-api.md)를 사용하세요. 많은 키를 사용하는 일반
API 사용의 경우 [사용량 API](#usage-api)를 사용하여 토큰 소비를 비용 프록시로 추적하세요.

## 참고 자료

사용량 및 비용 API를 사용하여 사용자에게 더 나은 경험을 제공하고, 비용을 관리하고, 속도 제한을 유지할 수 있습니다. 다음과 같은 다른 기능에 대해 자세히 알아보세요:

- [관리자 API 개요](../11-administration-monitoring/01-admin-api-overview.md)
- [관리자 API 참조](https://platform.claude.com/docs/en/api/admin)
- [가격 정책](https://platform.claude.com/docs/en/about-claude/pricing)
- [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md) - 캐싱을 통한 비용 최적화
- [배치 처리](../02-capabilities/06-batch-processing.md) - 배치 요청에 대한 50% 할인
- [속도 제한](https://platform.claude.com/docs/en/api/rate-limits) - 사용량 티어 이해하기

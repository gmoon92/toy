# [Admin API 개요](https://platform.claude.com/docs/en/build-with-claude/administration-api)

---


> **Admin API는 개인 계정에서는 사용할 수 없습니다.** 팀원과 협업하고 멤버를 추가하려면 **Console → Settings → Organization**에서 조직을 설정하세요.


[Admin API](https://platform.claude.com/docs/en/api/admin)를 사용하면 조직 멤버, 워크스페이스, API 키를 포함한 조직의 리소스를 프로그래밍 방식으로 관리할 수
있습니다. 이를 통해 [Claude Console](/)에서 수동으로 구성해야 하는 관리 작업을 프로그래밍 방식으로 제어할 수 있습니다.

<Check>
  **Admin API는 특별한 액세스 권한이 필요합니다**

Admin API를 사용하려면 표준 API 키와 다른 특별한 Admin API 키(`sk-ant-admin...`으로 시작)가 필요합니다. admin 역할을 가진 조직 멤버만 Claude Console을 통해
Admin API 키를 프로비저닝할 수 있습니다.
</Check>

## Admin API 작동 방식

Admin API를 사용하는 방법:

1. `x-api-key` 헤더에 Admin API 키를 사용하여 요청을 보냅니다
2. API를 통해 다음을 관리할 수 있습니다:
    - 조직 멤버 및 역할
    - 조직 멤버 초대
    - 워크스페이스 및 멤버
    - API 키

다음과 같은 경우에 유용합니다:

- 사용자 온보딩/오프보딩 자동화
- 워크스페이스 액세스를 프로그래밍 방식으로 관리
- API 키 사용량 모니터링 및 관리

## 조직 역할 및 권한

조직 수준의 역할은 다섯 가지가 있습니다. 자세한 내용은 [여기](https://support.claude.com/en/articles/10186004-api-console-roles-and-permissions)를
참조하세요.

| 역할               | 권한                                                                        |
|------------------|---------------------------------------------------------------------------|
| user             | Workbench 사용 가능                                                           |
| claude_code_user | Workbench 및 [Claude Code](https://code.claude.com/docs/en/overview) 사용 가능 |
| developer        | Workbench 사용 및 API 키 관리 가능                                                |
| billing          | Workbench 사용 및 결제 세부정보 관리 가능                                              |
| admin            | 위의 모든 작업 및 사용자 관리 가능                                                      |

## 주요 개념

### 조직 멤버

[조직 멤버](https://platform.claude.com/docs/en/api/admin-api/users/get-user)를 조회하고, 멤버 역할을 업데이트하고, 멤버를 제거할 수 있습니다.

<details>
<summary>REST API 예시</summary>

```bash
# 조직 멤버 조회
curl "https://api.anthropic.com/v1/organizations/users?limit=10" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"

# 멤버 역할 업데이트
curl "https://api.anthropic.com/v1/organizations/users/{user_id}" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{"role": "developer"}'

# 멤버 제거
curl --request DELETE "https://api.anthropic.com/v1/organizations/users/{user_id}" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"
```

</details>

### 조직 초대

조직에 사용자를 초대하고 해당 [초대](https://platform.claude.com/docs/en/api/admin-api/invites/get-invite)를 관리할 수 있습니다.

<details>
<summary>REST API 예시</summary>

```bash
# 초대 생성
curl --request POST "https://api.anthropic.com/v1/organizations/invites" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{
    "email": "newuser@domain.com",
    "role": "developer"
  }'

# 초대 목록 조회
curl "https://api.anthropic.com/v1/organizations/invites?limit=10" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"

# 초대 삭제
curl --request DELETE "https://api.anthropic.com/v1/organizations/invites/{invite_id}" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"
```

</details>

### 워크스페이스

워크스페이스에 대한 종합적인 가이드는 [Workspaces](../11-administration-monitoring/02-workspaces.md)를 참조하세요.

리소스를 구성하기
위해 [워크스페이스](https://platform.claude.com/docs/en/api/admin-api/workspaces/get-workspace)([콘솔](/settings/workspaces))를
생성하고 관리합니다:

<details>
<summary>REST API 예시</summary>

```bash
# 워크스페이스 생성
curl --request POST "https://api.anthropic.com/v1/organizations/workspaces" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{"name": "Production"}'

# 워크스페이스 목록 조회
curl "https://api.anthropic.com/v1/organizations/workspaces?limit=10&include_archived=false" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"

# 워크스페이스 아카이브
curl --request POST "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/archive" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"
```

</details>

### 워크스페이스 멤버

[특정 워크스페이스에 대한 사용자 액세스](https://platform.claude.com/docs/en/api/admin-api/workspace_members/get-workspace-member)를
관리합니다:

<details>
<summary>REST API 예시</summary>

```bash
# 워크스페이스에 멤버 추가
curl --request POST "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/members" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{
    "user_id": "user_xxx",
    "workspace_role": "workspace_developer"
  }'

# 워크스페이스 멤버 목록 조회
curl "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/members?limit=10" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"

# 멤버 역할 업데이트
curl --request POST "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/members/{user_id}" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{
    "workspace_role": "workspace_admin"
  }'

# 워크스페이스에서 멤버 제거
curl --request DELETE "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/members/{user_id}" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"
```

</details>

### API 키

[API 키](https://platform.claude.com/docs/en/api/admin-api/apikeys/get-api-key)를 모니터링하고 관리합니다:

<details>
<summary>REST API 예시</summary>

```bash
# API 키 목록 조회
curl "https://api.anthropic.com/v1/organizations/api_keys?limit=10&status=active&workspace_id=wrkspc_xxx" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"

# API 키 업데이트
curl --request POST "https://api.anthropic.com/v1/organizations/api_keys/{api_key_id}" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{
    "status": "inactive",
    "name": "New Key Name"
  }'
```

</details>

## 조직 정보 액세스

`/v1/organizations/me` 엔드포인트를 사용하여 프로그래밍 방식으로 조직 정보를 가져옵니다.

예시:

```bash
curl "https://api.anthropic.com/v1/organizations/me" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

```json
{
  "id": "12345678-1234-5678-1234-567812345678",
  "type": "organization",
  "name": "Organization Name"
}
```

이 엔드포인트는 Admin API 키가 속한 조직을 프로그래밍 방식으로 확인할 때 유용합니다.

전체 매개변수 세부정보 및 응답 스키마는 [Organization Info API 참조](https://platform.claude.com/docs/en/api/admin-api/organization/get-me)
를 확인하세요.

## 사용량 및 비용 보고서 액세스

조직의 사용량 및 비용 보고서에 액세스하려면 Usage and Cost API 엔드포인트를 사용하세요:

- [**Usage 엔드포인트**](../11-administration-monitoring/03-usage-cost-api.md) (`/v1/organizations/usage_report/messages`)는
  워크스페이스, 사용자, 모델과 같은 다양한 차원으로 그룹화된 토큰 수 및 요청 메트릭을 포함한 세부 사용량 데이터를 제공합니다.
- [**Cost 엔드포인트**](../11-administration-monitoring/03-usage-cost-api.md) (`/v1/organizations/cost_report`)는 조직의 사용량과 관련된
  비용 데이터를 제공하여 비용을 추적하고 워크스페이스 또는 설명별로 비용을 할당할 수 있습니다.

이러한 엔드포인트는 조직의 사용량 및 관련 비용에 대한 자세한 인사이트를 제공합니다.

## Claude Code 분석 액세스

Claude Code를 사용하는 조직의 경우, [**Claude Code Analytics API
**](../11-administration-monitoring/04-claude-code-analytics-api.md)가 세부적인 생산성 메트릭과 사용량 인사이트를 제공합니다:

- [**Claude Code Analytics 엔드포인트**](../11-administration-monitoring/04-claude-code-analytics-api.md) (
  `/v1/organizations/usage_report/claude_code`)는 세션, 코드 라인 수, 커밋, 풀 리퀘스트, 도구 사용 통계, 사용자 및 모델별로 분류된 비용 데이터를 포함한 Claude
  Code 사용량의 일일 집계 메트릭을 제공합니다.

이 API를 통해 개발자 생산성을 추적하고, Claude Code 도입을 분석하며, 조직을 위한 맞춤형 대시보드를 구축할 수 있습니다.

## 모범 사례

Admin API를 효과적으로 사용하려면:

- 워크스페이스와 API 키에 의미 있는 이름과 설명을 사용하세요
- 실패한 작업에 대한 적절한 오류 처리를 구현하세요
- 멤버 역할과 권한을 정기적으로 감사하세요
- 사용하지 않는 워크스페이스와 만료된 초대를 정리하세요
- API 키 사용량을 모니터링하고 주기적으로 키를 교체하세요

## FAQ

<details>
<summary>Admin API를 사용하는 데 필요한 권한은 무엇인가요?</summary>

admin 역할을 가진 조직 멤버만 Admin API를 사용할 수 있습니다. 또한 특별한 Admin API 키(`sk-ant-admin`으로 시작)가 있어야 합니다.
</details>

<details>
<summary>Admin API를 통해 새 API 키를 생성할 수 있나요?</summary>

아니요, 보안상의 이유로 새 API 키는 Claude Console을 통해서만 생성할 수 있습니다. Admin API는 기존 API 키만 관리할 수 있습니다.
</details>

<details>
<summary>사용자를 제거하면 API 키는 어떻게 되나요?</summary>

API 키는 개별 사용자가 아닌 조직에 범위가 지정되어 있으므로 현재 상태를 유지합니다.
</details>

<details>
<summary>조직 관리자를 API를 통해 제거할 수 있나요?</summary>

아니요, 보안상의 이유로 admin 역할을 가진 조직 멤버는 API를 통해 제거할 수 없습니다.
</details>

<details>
<summary>조직 초대는 얼마나 오래 지속되나요?</summary>

조직 초대는 21일 후에 만료됩니다. 현재 이 만료 기간을 수정할 방법은 없습니다.
</details>

<details>
<summary>워크스페이스에 제한이 있나요?</summary>

예, 조직당 최대 100개의 워크스페이스를 가질 수 있습니다. 아카이브된 워크스페이스는 이 제한에 포함되지 않습니다.
</details>

<details>
<summary>Default Workspace란 무엇인가요?</summary>

모든 조직에는 편집하거나 제거할 수 없고 ID가 없는 "Default Workspace"가 있습니다. 이 워크스페이스는 워크스페이스 목록 엔드포인트에 표시되지 않습니다.
</details>

<details>
<summary>조직 역할이 워크스페이스 액세스에 어떤 영향을 미치나요?</summary>

조직 관리자는 자동으로 모든 워크스페이스에 대해 `workspace_admin` 역할을 받습니다. 조직 결제 멤버는 자동으로 `workspace_billing` 역할을 받습니다. 조직 사용자와 개발자는 각
워크스페이스에 수동으로 추가되어야 합니다.
</details>

<details>
<summary>워크스페이스에서 어떤 역할을 할당할 수 있나요?</summary>

조직 사용자와 개발자는 `workspace_admin`, `workspace_developer` 또는 `workspace_user` 역할을 할당받을 수 있습니다. `workspace_billing` 역할은 수동으로
할당할 수 없으며, 조직의 `billing` 역할을 가진 경우 상속됩니다.
</details>

<details>
<summary>조직 관리자 또는 결제 멤버의 워크스페이스 역할을 변경할 수 있나요?</summary>

조직 결제 멤버만 워크스페이스 역할을 관리자 역할로 업그레이드할 수 있습니다. 그 외에는 조직 관리자와 결제 멤버의 워크스페이스 역할을 변경하거나 해당 조직 역할을 보유하는 동안 워크스페이스에서 제거할 수 없습니다.
워크스페이스 액세스를 수정하려면 먼저 조직 역할을 변경해야 합니다.
</details>

<details>
<summary>조직 역할이 변경되면 워크스페이스 액세스는 어떻게 되나요?</summary>

조직 관리자 또는 결제 멤버가 사용자 또는 개발자로 강등되면 수동으로 역할이 할당된 워크스페이스를 제외한 모든 워크스페이스에 대한 액세스 권한을 잃습니다. 사용자가 관리자 또는 결제 역할로 승격되면 모든
워크스페이스에 대한 자동 액세스 권한을 얻습니다.
</details>

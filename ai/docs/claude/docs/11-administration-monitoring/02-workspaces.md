# [워크스페이스](https://platform.claude.com/docs/en/build-with-claude/workspaces)

워크스페이스를 사용하여 API 키를 정리하고, 팀 액세스를 관리하며, 비용을 제어하세요.

---

워크스페이스는 조직 내에서 API 사용을 정리할 수 있는 방법을 제공합니다. 워크스페이스를 사용하여 중앙 집중식 청구 및 관리를 유지하면서 서로 다른 프로젝트, 환경 또는 팀을 분리할 수 있습니다.

## 워크스페이스 작동 방식

모든 조직에는 이름을 변경하거나 보관하거나 삭제할 수 없는 **기본 워크스페이스(Default Workspace)**가 있습니다. 추가 워크스페이스를 생성하면 각 워크스페이스에 API 키, 멤버 및 리소스 제한을 할당할 수 있습니다.

주요 특성:
- **워크스페이스 식별자**는 `wrkspc_` 접두사를 사용합니다 (예: `wrkspc_01JwQvzr7rXLA5AGx3HKfFUJ`)
- **조직당 최대 100개의 워크스페이스** (보관된 워크스페이스는 포함되지 않음)
- **기본 워크스페이스**는 ID가 없으며 목록 엔드포인트에 표시되지 않습니다
- **API 키**는 단일 워크스페이스로 범위가 지정되며 해당 워크스페이스 내의 리소스에만 액세스할 수 있습니다

## 워크스페이스 역할 및 권한

멤버는 각 워크스페이스에서 다른 역할을 가질 수 있어 세밀한 액세스 제어가 가능합니다.

| 역할 | 권한 |
|------|------|
| Workspace User | Workbench만 사용 |
| Workspace Developer | API 키 생성 및 관리, API 사용 |
| Workspace Admin | 워크스페이스 설정 및 멤버에 대한 완전한 제어 |
| Workspace Billing | 워크스페이스 청구 정보 조회 (조직 청구 역할에서 상속됨) |

### 역할 상속

- **조직 관리자(Organization admins)**는 모든 워크스페이스에 대한 Workspace Admin 액세스 권한을 자동으로 받습니다
- **조직 청구 멤버(Organization billing members)**는 모든 워크스페이스에 대한 Workspace Billing 액세스 권한을 자동으로 받습니다
- **조직 사용자 및 개발자(Organization users and developers)**는 각 워크스페이스에 명시적으로 추가되어야 합니다


> Workspace Billing 역할은 수동으로 할당할 수 없습니다. 조직 청구 역할을 보유하고 있을 때 상속됩니다.


## 워크스페이스 관리


> 조직 관리자만 워크스페이스를 생성할 수 있습니다. 조직 사용자 및 개발자는 관리자가 워크스페이스에 추가해야 합니다.


### 콘솔을 통해

[Claude Console](/settings/workspaces)에서 워크스페이스를 생성하고 관리합니다.

#### 워크스페이스 생성

<Steps>
  <Step title="워크스페이스 설정 열기">
    Claude Console에서 **Settings > Workspaces**로 이동합니다.
  </Step>
  <Step title="새 워크스페이스 추가">
    **Add Workspace**를 클릭합니다.
  </Step>
  <Step title="워크스페이스 구성">
    워크스페이스 이름을 입력하고 시각적 식별을 위한 색상을 선택합니다.
  </Step>
  <Step title="워크스페이스 생성">
    **Create**를 클릭하여 완료합니다.
  </Step>
</Steps>


> 콘솔에서 워크스페이스 간에 전환하려면 왼쪽 상단의 **Workspaces** 선택기를 사용하세요.


#### 워크스페이스 세부 정보 편집

워크스페이스의 이름이나 색상을 수정하려면:

1. 목록에서 워크스페이스를 선택합니다
2. 줄임표 메뉴(**...**)를 클릭하고 **Edit details**를 선택합니다
3. 이름이나 색상을 업데이트하고 변경 사항을 저장합니다


> 기본 워크스페이스는 이름을 변경하거나 삭제할 수 없습니다.


#### 워크스페이스에 멤버 추가

1. 워크스페이스의 **Members** 탭으로 이동합니다
2. **Add to Workspace**를 클릭합니다
3. 조직 멤버를 선택하고 [워크스페이스 역할](#workspace-roles-and-permissions)을 할당합니다
4. 추가를 확인합니다

멤버를 제거하려면 해당 멤버 이름 옆의 휴지통 아이콘을 클릭합니다.


> 조직 관리자 및 청구 멤버는 해당 조직 역할을 보유하는 동안 워크스페이스에서 제거할 수 없습니다.


#### 워크스페이스 제한 설정

**Limits** 탭에서 다음을 구성할 수 있습니다:

- **Rate limits**: 분당 요청, 입력 토큰 또는 출력 토큰에 대한 모델 계층별 제한 설정
- **Spend notifications**: 지출이 특정 임계값에 도달할 때 알림 구성

#### 워크스페이스 보관

워크스페이스를 보관하려면 줄임표 메뉴(**...**)를 클릭하고 **Archive**를 선택합니다. 보관 시:

- 보고용 이력 데이터를 보존합니다
- 워크스페이스 및 관련된 모든 API 키를 비활성화합니다
- 실행 취소할 수 없습니다


> 워크스페이스를 보관하면 해당 워크스페이스의 모든 API 키가 즉시 취소됩니다. 이 작업은 실행 취소할 수 없습니다.


### Admin API를 통해

[Admin API](../11-administration-monitoring/01-admin-api-overview.md)를 사용하여 프로그래밍 방식으로 워크스페이스를 관리합니다.


> Admin API 엔드포인트는 표준 API 키와 다른 Admin API 키(`sk-ant-admin...`로 시작)가 필요합니다. 관리자 역할을 가진 조직 멤버만 [Claude Console](/settings/admin-keys)을 통해 Admin API 키를 프로비저닝할 수 있습니다.


```bash
# 워크스페이스 생성
curl --request POST "https://api.anthropic.com/v1/organizations/workspaces" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{"name": "Production"}'

# 워크스페이스 목록
curl "https://api.anthropic.com/v1/organizations/workspaces?limit=10&include_archived=false" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"

# 워크스페이스 보관
curl --request POST "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/archive" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"
```

전체 매개변수 세부 정보 및 응답 스키마는 [Workspaces API reference](/docs/en/api/admin-api/workspaces/get-workspace)를 참조하세요.

### 워크스페이스 멤버 관리

워크스페이스에서 멤버를 추가, 업데이트 또는 제거합니다:

```bash
# 워크스페이스에 멤버 추가
curl --request POST "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/members" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{
    "user_id": "user_xxx",
    "workspace_role": "workspace_developer"
  }'

# 멤버의 역할 업데이트
curl --request POST "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/members/{user_id}" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY" \
  --data '{"workspace_role": "workspace_admin"}'

# 워크스페이스에서 멤버 제거
curl --request DELETE "https://api.anthropic.com/v1/organizations/workspaces/{workspace_id}/members/{user_id}" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ANTHROPIC_ADMIN_KEY"
```

전체 매개변수 세부 정보는 [Workspace Members API reference](/docs/en/api/admin-api/workspace_members/get-workspace-member)를 참조하세요.

## API 키 및 리소스 범위 지정

API 키는 특정 워크스페이스로 범위가 지정됩니다. 워크스페이스에서 API 키를 생성하면 해당 워크스페이스 내의 리소스에만 액세스할 수 있습니다.

워크스페이스로 범위가 지정된 리소스는 다음과 같습니다:
- [Files API](../02-capabilities/13-files-api.md)를 통해 생성된 **파일(Files)**
- [Batch API](../02-capabilities/06-batch-processing.md)를 통해 생성된 **메시지 배치(Message Batches)**
- [Skills API](../04-agent-skills/05-using-skills-with-api.md)를 통해 생성된 **스킬(Skills)**


> 2026년 2월 5일부터 [프롬프트 캐시(prompt caches)](../02-capabilities/01-prompt-caching.md)도 워크스페이스별로 격리됩니다 (Claude API 및 Azure에만 적용됨).



> 조직의 워크스페이스 ID를 검색하려면 [List Workspaces](/docs/en/api/admin-api/workspaces/list-workspaces) 엔드포인트를 사용하거나 [Claude Console](/settings/workspaces)에서 찾을 수 있습니다.


## 워크스페이스 제한

각 워크스페이스에 대한 사용자 지정 지출 및 속도 제한을 설정하여 과도한 사용을 방지하고 공정한 리소스 분배를 보장할 수 있습니다.

### 워크스페이스 제한 설정

워크스페이스 제한은 조직의 제한보다 낮게 설정할 수 있지만 높게 설정할 수는 없습니다:

- **Spend limits**: 워크스페이스의 월별 지출 상한 설정
- **Rate limits**: 분당 요청, 분당 입력 토큰 또는 분당 출력 토큰 제한


> - 기본 워크스페이스에는 제한을 설정할 수 없습니다
> - 설정하지 않으면 워크스페이스 제한이 조직의 제한과 일치합니다
> - 워크스페이스 제한의 합계가 더 많더라도 조직 전체 제한이 항상 적용됩니다


속도 제한 및 작동 방식에 대한 자세한 내용은 [Rate limits](/docs/en/api/rate-limits)를 참조하세요.

## 사용량 및 비용 추적

[Usage and Cost API](../11-administration-monitoring/03-usage-cost-api.md)를 사용하여 워크스페이스별로 사용량과 비용을 추적합니다:

```bash
curl "https://api.anthropic.com/v1/organizations/usage_report/messages?\
starting_at=2025-01-01T00:00:00Z&\
ending_at=2025-01-08T00:00:00Z&\
workspace_ids[]=wrkspc_01JwQvzr7rXLA5AGx3HKfFUJ&\
group_by[]=workspace_id&\
bucket_width=1d" \
  --header "anthropic-version: 2023-06-01" \
  --header "x-api-key: $ADMIN_API_KEY"
```

기본 워크스페이스에 귀속된 사용량 및 비용은 `workspace_id`에 대해 `null` 값을 갖습니다.

## 일반적인 사용 사례

### 환경 분리

개발, 스테이징 및 프로덕션을 위한 별도의 워크스페이스를 생성합니다:

| 워크스페이스 | 용도 |
|-------------|------|
| Development | 낮은 속도 제한으로 테스트 및 실험 |
| Staging | 프로덕션과 유사한 제한으로 사전 프로덕션 테스트 |
| Production | 전체 속도 제한 및 모니터링으로 실제 트래픽 처리 |

### 팀 또는 부서 격리

비용 할당 및 액세스 제어를 위해 다른 팀에 워크스페이스를 할당합니다:

- 개발자 액세스 권한이 있는 **엔지니어링 팀**
- 자체 API 키가 있는 **데이터 과학 팀**
- 고객 도구에 대한 제한된 액세스 권한이 있는 **지원 팀**

### 프로젝트 기반 조직

특정 프로젝트 또는 제품에 대한 워크스페이스를 생성하여 사용량과 비용을 개별적으로 추적합니다.

## 모범 사례

<Steps>
  <Step title="워크스페이스 구조 계획">
    워크스페이스를 생성하기 전에 어떻게 구성할지 고려하세요. 청구, 액세스 제어 및 사용량 추적 요구 사항을 생각하세요.
  </Step>
  <Step title="의미 있는 이름 사용">
    워크스페이스의 용도를 나타내기 위해 명확하게 이름을 지정하세요 (예: "Production - Customer Chatbot", "Dev - Internal Tools").
  </Step>
  <Step title="적절한 제한 설정">
    예상치 못한 비용을 방지하고 공정한 리소스 분배를 보장하기 위해 지출 및 속도 제한을 구성하세요.
  </Step>
  <Step title="정기적으로 액세스 감사">
    적절한 사용자만 액세스 권한을 갖도록 주기적으로 워크스페이스 멤버십을 검토하세요.
  </Step>
  <Step title="사용량 모니터링">
    [Usage and Cost API](../11-administration-monitoring/03-usage-cost-api.md)를 사용하여 워크스페이스 수준의 소비를 추적하세요.
  </Step>
</Steps>

## FAQ

<details>
<summary>기본 워크스페이스란 무엇인가요?</summary>

모든 조직에는 편집, 이름 변경 또는 제거할 수 없는 "기본 워크스페이스(Default Workspace)"가 있습니다. 이 워크스페이스에는 ID가 없으며 워크스페이스 목록 엔드포인트에 표시되지 않습니다. 기본 워크스페이스에 귀속된 사용량은 API 응답에서 `workspace_id`에 대해 `null` 값을 표시합니다.
</details>

<details>
<summary>워크스페이스에 제한이 있나요?</summary>

예, 조직당 최대 100개의 워크스페이스를 가질 수 있습니다. 보관된 워크스페이스는 이 제한에 포함되지 않습니다.
</details>

<details>
<summary>조직 역할이 워크스페이스 액세스에 어떤 영향을 미치나요?</summary>

조직 관리자는 모든 워크스페이스에서 자동으로 Workspace Admin 역할을 받습니다. 조직 청구 멤버는 자동으로 Workspace Billing 역할을 받습니다. 조직 사용자 및 개발자는 각 워크스페이스에 수동으로 추가되어야 합니다.
</details>

<details>
<summary>워크스페이스에서 어떤 역할을 할당할 수 있나요?</summary>

조직 사용자 및 개발자는 Workspace Admin, Workspace Developer 또는 Workspace User 역할을 할당받을 수 있습니다. Workspace Billing 역할은 수동으로 할당할 수 없으며 조직 `billing` 역할을 보유하고 있을 때 상속됩니다.
</details>

<details>
<summary>조직 관리자 또는 청구 멤버의 워크스페이스 역할을 변경할 수 있나요?</summary>

조직 청구 멤버만 워크스페이스 역할을 관리자 역할로 업그레이드할 수 있습니다. 그 외에는 조직 관리자 및 청구 멤버가 해당 조직 역할을 보유하는 동안 워크스페이스 역할을 변경하거나 워크스페이스에서 제거할 수 없습니다. 워크스페이스 액세스는 먼저 조직 역할을 변경하여 수정해야 합니다.
</details>

<details>
<summary>조직 역할이 변경되면 워크스페이스 액세스는 어떻게 되나요?</summary>

조직 관리자 또는 청구 멤버가 사용자 또는 개발자로 강등되면 수동으로 역할이 할당된 워크스페이스를 제외한 모든 워크스페이스에 대한 액세스 권한을 잃습니다. 사용자가 관리자 또는 청구 역할로 승격되면 모든 워크스페이스에 대한 자동 액세스 권한을 얻습니다.
</details>

<details>
<summary>사용자가 워크스페이스에서 제거되면 API 키는 어떻게 되나요?</summary>

API 키는 개별 사용자가 아닌 조직 및 워크스페이스로 범위가 지정되므로 현재 상태를 유지합니다.
</details>

## 참고 자료

- [Admin API overview](../11-administration-monitoring/01-admin-api-overview.md)
- [Admin API reference](/docs/en/api/admin)
- [Rate limits](/docs/en/api/rate-limits)
- [Usage and Cost API](../11-administration-monitoring/03-usage-cost-api.md)

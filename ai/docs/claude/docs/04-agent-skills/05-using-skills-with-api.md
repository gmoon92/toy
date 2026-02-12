# [API를 통한 에이전트 스킬 사용하기](https://platform.claude.com/docs/en/build-with-claude/skills-guide)

API를 통해 에이전트 스킬을 사용하여 Claude의 기능을 확장하는 방법을 알아봅니다.

---

에이전트 스킬(Agent Skills)은 명령, 스크립트, 리소스로 구성된 폴더를 통해 Claude의 기능을 확장합니다. 
이 가이드에서는 Claude API를 사용하여 사전 구축된 스킬과 사용자 정의 스킬을 모두 사용하는 방법을 보여줍니다.

> 요청/응답 스키마 및 모든 매개변수를 포함한 전체 API 참조는 다음을 참조하세요:
> - [스킬 관리 API 참조](https://platform.claude.com/docs/en/api/skills/list-skills) - 스킬의 CRUD 작업
> - [스킬 버전 API 참조](https://platform.claude.com/docs/en/api/skills/list-skill-versions) - 버전 관리

## 개요

> 에이전트 스킬의 아키텍처와 실제 적용 사례에 대한 심층 분석은 엔지니어링 블로그를 읽어보세요 
> [Equipping agents for the real world with Agent Skills](https://www.anthropic.com/engineering/equipping-agents-for-the-real-world-with-agent-skills).

스킬은 코드 실행 도구를 통해 Messages API와 통합됩니다. 
Anthropic에서 관리하는 사전 구축된 스킬을 사용하든 업로드한 사용자 정의 스킬을 사용하든 통합 형태는 동일합니다. 
둘 다 코드 실행이 필요하며 동일한 `container` 구조를 사용합니다.

### 스킬 사용하기

스킬은 출처에 관계없이 Messages API에서 동일하게 통합됩니다. 
`container` 매개변수에 `skill_id`, `type`, 선택적으로 `version`을 지정하면 코드 실행 환경에서 실행됩니다.

**두 가지 출처에서 스킬을 사용할 수 있습니다:**

| 구분         | Anthropic 스킬                         | 사용자 정의 스킬                                                                              |
|------------|--------------------------------------|----------------------------------------------------------------------------------------|
| **Type 값** | `anthropic`                          | `custom`                                                                               |
| **스킬 ID**  | 짧은 이름: `pptx`, `xlsx`, `docx`, `pdf` | 생성된 ID: `skill_01AbCdEfGhIjKlMnOpQrStUv`                                               |
| **버전 형식**  | 날짜 기반: `20251013` 또는 `latest`        | Epoch 타임스탬프: `1759178010641129` 또는 `latest`                                            |
| **관리**     | Anthropic에서 사전 구축 및 유지 관리            | [Skills API](https://platform.claude.com/docs/en/api/skills/create-skill)를 통해 업로드 및 관리 |
| **가용성**    | 모든 사용자에게 제공                          | 워크스페이스 내 비공개                                                                           |

두 스킬 출처 모두 [List Skills 엔드포인트](https://platform.claude.com/docs/en/api/skills/list-skills)에서 반환됩니다(`source` 매개변수를 사용하여 필터링). 
통합 형태와 실행 환경은 동일하며, 유일한 차이점은 스킬의 출처와 관리 방법입니다.

### 사전 요구 사항

스킬을 사용하려면 다음이 필요합니다:

1. **Anthropic API 키** - [콘솔](/settings/keys)에서 발급
2. **베타 헤더**:
    - `code-execution-2025-08-25` - 코드 실행 활성화 (스킬에 필수)
    - `skills-2025-10-02` - Skills API 활성화
    - `files-api-2025-04-14` - 컨테이너로/에서 파일 업로드/다운로드용
3. **코드 실행 도구** - 요청에서 활성화

---

## 메시지에서 스킬 사용하기

### Container 매개변수

스킬은 Messages API의 `container` 매개변수를 사용하여 지정합니다. 
요청당 최대 8개의 스킬을 포함할 수 있습니다.

구조는 Anthropic 스킬과 사용자 정의 스킬 모두 동일합니다. 
필수 `type`과 `skill_id`를 지정하고, 선택적으로 `version`을 포함하여 특정 버전으로 고정할 수 있습니다:

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5-20250929",
    "max_tokens": 4096,
    "container": {
      "skills": [
        {
          "type": "anthropic",
          "skill_id": "pptx",
          "version": "latest"
        }
      ]
    },
    "messages": [{
      "role": "user",
      "content": "Create a presentation about renewable energy"
    }],
    "tools": [{
      "type": "code_execution_20250825",
      "name": "code_execution"
    }]
  }'
```

</details>

### 생성된 파일 다운로드하기

스킬이 문서(Excel, PowerPoint, PDF, Word)를 생성할 때, 응답에 `file_id` 속성을 반환합니다. 
이러한 파일을 다운로드하려면 Files API를 사용해야 합니다.

**작동 방식:**

1. 스킬이 코드 실행 중에 파일을 생성합니다
2. 응답에 생성된 각 파일의 `file_id`가 포함됩니다
3. Files API를 사용하여 실제 파일 콘텐츠를 다운로드합니다
4. 로컬에 저장하거나 필요에 따라 처리합니다

**예제: Excel 파일 생성 및 다운로드**

<details>
<summary>REST API 예시</summary>

```bash
# 1단계: 스킬을 사용하여 파일 생성
RESPONSE=$(curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5-20250929",
    "max_tokens": 4096,
    "container": {
      "skills": [
        {"type": "anthropic", "skill_id": "xlsx", "version": "latest"}
      ]
    },
    "messages": [{
      "role": "user",
      "content": "Create an Excel file with a simple budget spreadsheet"
    }],
    "tools": [{
      "type": "code_execution_20250825",
      "name": "code_execution"
    }]
  }')

# 2단계: 응답에서 file_id 추출 (jq 사용)
FILE_ID=$(echo "$RESPONSE" | jq -r '.content[] | select(.type=="bash_code_execution_tool_result") | .content | select(.type=="bash_code_execution_result") | .content[] | select(.file_id) | .file_id')

# 3단계: 메타데이터에서 파일명 가져오기
FILENAME=$(curl "https://api.anthropic.com/v1/files/$FILE_ID" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: files-api-2025-04-14" | jq -r '.filename')

# 4단계: Files API를 사용하여 파일 다운로드
curl "https://api.anthropic.com/v1/files/$FILE_ID/content" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: files-api-2025-04-14" \
  --output "$FILENAME"

echo "Downloaded: $FILENAME"
```

</details>

**추가 Files API 작업:**

<details>
<summary>REST API 예시</summary>

```bash
# 파일 메타데이터 가져오기
curl "https://api.anthropic.com/v1/files/$FILE_ID" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: files-api-2025-04-14"

# 모든 파일 목록 조회
curl "https://api.anthropic.com/v1/files" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: files-api-2025-04-14"

# 파일 삭제
curl -X DELETE "https://api.anthropic.com/v1/files/$FILE_ID" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: files-api-2025-04-14"
```

</details>


> Files API에 대한 자세한 내용은 [Files API 문서](https://platform.claude.com/docs/en/api/files-content)를 참조하세요.

### 다중 턴 대화

컨테이너 ID를 지정하여 여러 메시지에서 동일한 컨테이너를 재사용합니다:

<details>
<summary>Python 예시</summary>

```python
# 첫 번째 요청이 컨테이너를 생성
response1 = client.beta.messages.create(
    model="claude-sonnet-4-5-20250929",
    max_tokens=4096,
    betas=["code-execution-2025-08-25", "skills-2025-10-02"],
    container={
        "skills": [
            {"type": "anthropic", "skill_id": "xlsx", "version": "latest"}
        ]
    },
    messages=[{"role": "user", "content": "Analyze this sales data"}],
    tools=[{"type": "code_execution_20250825", "name": "code_execution"}]
)

# 동일한 컨테이너로 대화 계속
messages = [
    {"role": "user", "content": "Analyze this sales data"},
    {"role": "assistant", "content": response1.content},
    {"role": "user", "content": "What was the total revenue?"}
]

response2 = client.beta.messages.create(
    model="claude-sonnet-4-5-20250929",
    max_tokens=4096,
    betas=["code-execution-2025-08-25", "skills-2025-10-02"],
    container={
        "id": response1.container.id,  # 컨테이너 재사용
        "skills": [
            {"type": "anthropic", "skill_id": "xlsx", "version": "latest"}
        ]
    },
    messages=messages,
    tools=[{"type": "code_execution_20250825", "name": "code_execution"}]
)
```

</details>

### 장기 실행 작업

스킬이 여러 턴이 필요한 작업을 수행할 수 있습니다. `pause_turn` 중지 이유를 처리합니다:

<details>
<summary>REST API 예시</summary>

```bash
# 초기 요청
RESPONSE=$(curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5-20250929",
    "max_tokens": 4096,
    "container": {
      "skills": [
        {
          "type": "custom",
          "skill_id": "skill_01AbCdEfGhIjKlMnOpQrStUv",
          "version": "latest"
        }
      ]
    },
    "messages": [{
      "role": "user",
      "content": "Process this large dataset"
    }],
    "tools": [{
      "type": "code_execution_20250825",
      "name": "code_execution"
    }]
  }')

# stop_reason 확인 및 루프에서 pause_turn 처리
STOP_REASON=$(echo "$RESPONSE" | jq -r '.stop_reason')
CONTAINER_ID=$(echo "$RESPONSE" | jq -r '.container.id')

while [ "$STOP_REASON" = "pause_turn" ]; do
  # 동일한 컨테이너로 계속
  RESPONSE=$(curl https://api.anthropic.com/v1/messages \
    -H "x-api-key: $ANTHROPIC_API_KEY" \
    -H "anthropic-version: 2023-06-01" \
    -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02" \
    -H "content-type: application/json" \
    -d "{
      \"model\": \"claude-sonnet-4-5-20250929\",
      \"max_tokens\": 4096,
      \"container\": {
        \"id\": \"$CONTAINER_ID\",
        \"skills\": [{
          \"type\": \"custom\",
          \"skill_id\": \"skill_01AbCdEfGhIjKlMnOpQrStUv\",
          \"version\": \"latest\"
        }]
      },
      \"messages\": [/* 대화 히스토리 포함 */],
      \"tools\": [{
        \"type\": \"code_execution_20250825\",
        \"name\": \"code_execution\"
      }]
    }")

  STOP_REASON=$(echo "$RESPONSE" | jq -r '.stop_reason')
done
```

</details>

> 응답에 `pause_turn` 중지 이유가 포함될 수 있으며, 이는 API가 장기 실행 스킬 작업을 일시 중지했음을 나타냅니다. Claude가 턴을 계속하도록 응답을 그대로 후속 요청에 제공하거나, 대화를
> 중단하고 추가 안내를 제공하려는 경우 콘텐츠를 수정할 수 있습니다.

### 여러 스킬 사용하기

복잡한 워크플로를 처리하기 위해 단일 요청에서 여러 스킬을 결합합니다:

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5-20250929",
    "max_tokens": 4096,
    "container": {
      "skills": [
        {
          "type": "anthropic",
          "skill_id": "xlsx",
          "version": "latest"
        },
        {
          "type": "anthropic",
          "skill_id": "pptx",
          "version": "latest"
        },
        {
          "type": "custom",
          "skill_id": "skill_01AbCdEfGhIjKlMnOpQrStUv",
          "version": "latest"
        }
      ]
    },
    "messages": [{
      "role": "user",
      "content": "Analyze sales data and create a presentation"
    }],
    "tools": [{
      "type": "code_execution_20250825",
      "name": "code_execution"
    }]
  }'
```

</details>

---

## 사용자 정의 스킬 관리하기

### 스킬 생성하기

워크스페이스에서 사용할 수 있도록 사용자 정의 스킬을 업로드합니다. 디렉토리 경로 또는 개별 파일 객체를 사용하여 업로드할 수 있습니다.

<details>
<summary>REST API 예시</summary>

```bash
curl -X POST "https://api.anthropic.com/v1/skills" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02" \
  -F "display_title=Financial Analysis" \
  -F "files[]=@financial_skill/SKILL.md;filename=financial_skill/SKILL.md" \
  -F "files[]=@financial_skill/analyze.py;filename=financial_skill/analyze.py"
```

</details>

**요구 사항:**

- 최상위 수준에 SKILL.md 파일이 포함되어야 합니다
- 모든 파일은 경로에 공통 루트 디렉토리를 지정해야 합니다
- 총 업로드 크기는 8MB 미만이어야 합니다
- YAML frontmatter 요구 사항:
    - `name`: 최대 64자, 소문자/숫자/하이픈만 사용, XML 태그 없음, 예약어("anthropic", "claude") 사용 불가
    - `description`: 최대 1024자, 비어있지 않음, XML 태그 없음

전체 요청/응답 스키마는 [Create Skill API 참조](https://platform.claude.com/docs/en/api/skills/create-skill)를 참조하세요.

### 스킬 목록 조회하기

Anthropic 사전 구축 스킬과 사용자 정의 스킬을 포함하여 워크스페이스에서 사용 가능한 모든 스킬을 검색합니다. `source` 매개변수를 사용하여 스킬 유형별로 필터링합니다:

<details>
<summary>REST API 예시</summary>

```bash
# 모든 스킬 목록 조회
curl "https://api.anthropic.com/v1/skills" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02"

# 사용자 정의 스킬만 목록 조회
curl "https://api.anthropic.com/v1/skills?source=custom" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02"
```

</details>

페이지네이션 및 필터링 옵션은 [List Skills API 참조](https://platform.claude.com/docs/en/api/skills/list-skills)를 참조하세요.

### 스킬 상세 조회하기

특정 스킬에 대한 세부 정보를 가져옵니다:

<details>
<summary>REST API 예시</summary>

```bash
curl "https://api.anthropic.com/v1/skills/skill_01AbCdEfGhIjKlMnOpQrStUv" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02"
```

</details>

### 스킬 삭제하기

스킬을 삭제하려면 먼저 모든 버전을 삭제해야 합니다:

<details>
<summary>REST API 예시</summary>

```bash
# 먼저 모든 버전을 삭제한 다음 스킬을 삭제
curl -X DELETE "https://api.anthropic.com/v1/skills/skill_01AbCdEfGhIjKlMnOpQrStUv" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02"
```

</details>

기존 버전이 있는 스킬을 삭제하려고 시도하면 400 오류가 반환됩니다.

### 버전 관리

스킬은 안전하게 업데이트를 관리하기 위한 버전 관리를 지원합니다:

**Anthropic 관리 스킬**:

- 날짜 형식의 버전 사용: `20251013`
- 업데이트가 이루어질 때마다 새 버전이 릴리스됨
- 안정성을 위해 정확한 버전 지정

**사용자 정의 스킬**:

- 자동 생성된 epoch 타임스탬프: `1759178010641129`
- 항상 최신 버전을 가져오려면 `"latest"` 사용
- 스킬 파일을 업데이트할 때 새 버전 생성

<details>
<summary>REST API 예시</summary>

```bash
# 새 버전 생성
NEW_VERSION=$(curl -X POST "https://api.anthropic.com/v1/skills/skill_01AbCdEfGhIjKlMnOpQrStUv/versions" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02" \
  -F "files[]=@updated_skill/SKILL.md;filename=updated_skill/SKILL.md")

VERSION_NUMBER=$(echo "$NEW_VERSION" | jq -r '.version')

# 특정 버전 사용
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02" \
  -H "content-type: application/json" \
  -d "{
    \"model\": \"claude-sonnet-4-5-20250929\",
    \"max_tokens\": 4096,
    \"container\": {
      \"skills\": [{
        \"type\": \"custom\",
        \"skill_id\": \"skill_01AbCdEfGhIjKlMnOpQrStUv\",
        \"version\": \"$VERSION_NUMBER\"
      }]
    },
    \"messages\": [{\"role\": \"user\", \"content\": \"Use updated Skill\"}],
    \"tools\": [{\"type\": \"code_execution_20250825\", \"name\": \"code_execution\"}]
  }"

# 최신 버전 사용
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5-20250929",
    "max_tokens": 4096,
    "container": {
      "skills": [{
        "type": "custom",
        "skill_id": "skill_01AbCdEfGhIjKlMnOpQrStUv",
        "version": "latest"
      }]
    },
    "messages": [{"role": "user", "content": "Use latest Skill version"}],
    "tools": [{"type": "code_execution_20250825", "name": "code_execution"}]
  }'
```

</details>

자세한 내용은 [Create Skill Version API 참조](https://platform.claude.com/docs/en/api/skills/create-skill-version)를 참조하세요.

---

## 스킬이 로드되는 방식

컨테이너에 스킬을 지정하면:

1. **메타데이터 발견**: Claude는 시스템 프롬프트에서 각 스킬의 메타데이터(이름, 설명)를 확인합니다
2. **파일 로딩**: 스킬 파일이 `/skills/{directory}/` 경로로 컨테이너에 복사됩니다
3. **자동 사용**: Claude는 요청과 관련이 있을 때 자동으로 스킬을 로드하고 사용합니다
4. **구성**: 복잡한 워크플로를 위해 여러 스킬이 함께 구성됩니다

점진적 공개 아키텍처는 효율적인 컨텍스트 사용을 보장합니다. Claude는 필요할 때만 전체 스킬 지침을 로드합니다.

---

## 사용 사례

### 조직용 스킬

**브랜드 및 커뮤니케이션**
- 회사 전용 포맷팅(색상, 글꼴, 레이아웃)을 문서에 적용
- 조직 템플릿에 따라 커뮤니케이션 생성
- 모든 결과물에 일관된 브랜드 가이드라인 보장

**프로젝트 관리**
- 회사 전용 형식(OKR, 의사결정 로그)으로 노트 구조화
- 팀 규약에 따라 작업 생성
- 표준화된 회의 요약 및 상태 업데이트 생성

**비즈니스 운영**
- 회사 표준 보고서, 제안서, 분석 생성
- 회사 전용 분석 절차 실행
- 조직 템플릿에 따라 재무 모델 생성

### 개인용 스킬

**콘텐츠 생성**
- 사용자 정의 문서 템플릿
- 전문화된 포맷팅 및 스타일링
- 도메인 특화 콘텐츠 생성

**데이터 분석**
- 사용자 정의 데이터 처리 파이프라인
- 전문화된 시각화 템플릿
- 산업 특화 분석 방법

**개발 및 자동화**
- 코드 생성 템플릿
- 테스트 프레임워크
- 배포 워크플로

### 예제: 재무 모델링

Excel과 사용자 정의 DCF 분석 스킬 결합:

<details>
<summary>REST API 예시</summary>

```bash
# 사용자 정의 DCF 분석 스킬 생성
DCF_SKILL=$(curl -X POST "https://api.anthropic.com/v1/skills" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02" \
  -F "display_title=DCF Analysis" \
  -F "files[]=@dcf_skill/SKILL.md;filename=dcf_skill/SKILL.md")

DCF_SKILL_ID=$(echo "$DCF_SKILL" | jq -r '.id')

# Excel과 함께 사용하여 재무 모델 생성
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02" \
  -H "content-type: application/json" \
  -d "{
    \"model\": \"claude-sonnet-4-5-20250929\",
    \"max_tokens\": 4096,
    \"container\": {
      \"skills\": [
        {
          \"type\": \"anthropic\",
          \"skill_id\": \"xlsx\",
          \"version\": \"latest\"
        },
        {
          \"type\": \"custom\",
          \"skill_id\": \"$DCF_SKILL_ID\",
          \"version\": \"latest\"
        }
      ]
    },
    \"messages\": [{
      \"role\": \"user\",
      \"content\": \"Build a DCF valuation model for a SaaS company with the attached financials\"
    }],
    \"tools\": [{
      \"type\": \"code_execution_20250825\",
      \"name\": \"code_execution\"
    }]
  }"
```

</details>

---

## 제한 사항 및 제약 조건

### 요청 제한

- **요청당 최대 스킬 수**: 8개
- **최대 스킬 업로드 크기**: 8MB (모든 파일 합계)
- **YAML frontmatter 요구 사항**:
    - `name`: 최대 64자, 소문자/숫자/하이픈만 사용, XML 태그 없음, 예약어 사용 불가
    - `description`: 최대 1024자, 비어있지 않음, XML 태그 없음

### 환경 제약 사항

스킬은 다음과 같은 제한 사항이 있는 코드 실행 컨테이너에서 실행됩니다:

- **네트워크 액세스 없음** - 외부 API 호출 불가
- **런타임 패키지 설치 없음** - 사전 설치된 패키지만 사용 가능
- **격리된 환경** - 각 요청마다 새로운 컨테이너 제공

사용 가능한 패키지는 [코드 실행 도구 문서](../03-tools/05-code-execution-tool.md)를 참조하세요.

---

## 모범 사례

### 여러 스킬을 사용해야 하는 경우

여러 문서 유형이나 도메인이 포함된 작업의 경우 스킬을 결합합니다:

**좋은 사용 사례:**
- 데이터 분석(Excel) + 프레젠테이션 생성(PowerPoint)
- 보고서 생성(Word) + PDF로 내보내기
- 사용자 정의 도메인 로직 + 문서 생성

**피해야 할 사항:**
- 사용하지 않는 스킬 포함(성능에 영향)

### 버전 관리 전략

**프로덕션 환경:**

```python
# 안정성을 위해 특정 버전으로 고정
container={
    "skills": [{
        "type": "custom",
        "skill_id": "skill_01AbCdEfGhIjKlMnOpQrStUv",
        "version": "1759178010641129"  # 특정 버전
    }]
}
```

**개발 환경:**

```python
# 활성 개발을 위해 latest 사용
container={
    "skills": [{
        "type": "custom",
        "skill_id": "skill_01AbCdEfGhIjKlMnOpQrStUv",
        "version": "latest"  # 항상 최신 버전 사용
    }]
}
```

### 프롬프트 캐싱 고려 사항

프롬프트 캐싱을 사용할 때 컨테이너의 스킬 목록을 변경하면 캐시가 무효화됩니다:

<details>
<summary>REST API 예시</summary>

```bash
# 첫 번째 요청이 캐시 생성
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02,prompt-caching-2024-07-31" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5-20250929",
    "max_tokens": 4096,
    "container": {
      "skills": [
        {"type": "anthropic", "skill_id": "xlsx", "version": "latest"}
      ]
    },
    "messages": [{"role": "user", "content": "Analyze sales data"}],
    "tools": [{"type": "code_execution_20250825", "name": "code_execution"}]
  }'

# 스킬 추가/제거로 캐시 무효화
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: code-execution-2025-08-25,skills-2025-10-02,prompt-caching-2024-07-31" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5-20250929",
    "max_tokens": 4096,
    "container": {
      "skills": [
        {"type": "anthropic", "skill_id": "xlsx", "version": "latest"},
        {"type": "anthropic", "skill_id": "pptx", "version": "latest"}
      ]
    },
    "messages": [{"role": "user", "content": "Create a presentation"}],
    "tools": [{"type": "code_execution_20250825", "name": "code_execution"}]
  }'
```

</details>

최상의 캐싱 성능을 위해 요청 간에 스킬 목록을 일관되게 유지하세요.

### 오류 처리

스킬 관련 오류를 우아하게 처리합니다:

<details>
<summary>Python 예시</summary>

```python
try:
    response = client.beta.messages.create(
        model="claude-sonnet-4-5-20250929",
        max_tokens=4096,
        betas=["code-execution-2025-08-25", "skills-2025-10-02"],
        container={
            "skills": [
                {"type": "custom", "skill_id": "skill_01AbCdEfGhIjKlMnOpQrStUv", "version": "latest"}
            ]
        },
        messages=[{"role": "user", "content": "Process data"}],
        tools=[{"type": "code_execution_20250825", "name": "code_execution"}]
    )
except anthropic.BadRequestError as e:
    if "skill" in str(e):
        print(f"Skill error: {e}")
        # 스킬 관련 오류 처리
    else:
        raise
```

</details>

---

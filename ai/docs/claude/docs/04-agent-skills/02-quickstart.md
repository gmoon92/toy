# [API에서 Agent Skills 시작하기](https://platform.claude.com/docs/en/agents-and-tools/agent-skills/quickstart)

10분 이내에 Claude API를 사용하여 Agent Skills로 문서를 생성하는 방법을 배워보세요.

---

이 튜토리얼은 Agent Skills를 사용하여 PowerPoint 프레젠테이션을 만드는 방법을 보여줍니다. Skills를 활성화하고, 간단한 요청을 수행하며, 생성된 파일에 액세스하는 방법을 배우게 됩니다.

## 사전 요구사항

- [Anthropic API 키](/settings/keys)
- Python 3.7+ 또는 curl 설치
- API 요청 수행에 대한 기본 지식

## Agent Skills란 무엇인가요?

사전 구축된 Agent Skills는 문서 생성, 데이터 분석, 파일 처리와 같은 작업에 대한 전문적인 전문 지식으로 Claude의 기능을 확장합니다. Anthropic은 API에서 다음과 같은 사전 구축된 Agent Skills를 제공합니다:

- **PowerPoint (pptx)**: 프레젠테이션 생성 및 편집
- **Excel (xlsx)**: 스프레드시트 생성 및 분석
- **Word (docx)**: 문서 생성 및 편집
- **PDF (pdf)**: PDF 문서 생성


> **커스텀 Skills를 만들고 싶으신가요?** 도메인별 전문 지식을 갖춘 자신만의 Skills를 구축하는 예제는 [Agent Skills Cookbook](https://platform.claude.com/cookbook/skills-notebooks-01-skills-introduction)을 참조하세요.


## 1단계: 사용 가능한 Skills 목록 보기

먼저, 어떤 Skills를 사용할 수 있는지 확인해 보겠습니다. Skills API를 사용하여 Anthropic이 관리하는 모든 Skills 목록을 가져옵니다:

<details>
<summary>REST API 예시</summary>

```bash
curl "https://api.anthropic.com/v1/skills?source=anthropic" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: skills-2025-10-02"
```

</details>

다음과 같은 Skills를 확인할 수 있습니다: `pptx`, `xlsx`, `docx`, `pdf`.

이 API는 각 Skill의 메타데이터(이름과 설명)를 반환합니다. Claude는 시작 시 이 메타데이터를 로드하여 어떤 Skills를 사용할 수 있는지 파악합니다. 이것이 **점진적 공개**의 첫 번째 단계로, Claude가 전체 지침을 아직 로드하지 않고도 Skills를 발견할 수 있습니다.

## 2단계: 프레젠테이션 만들기

이제 PowerPoint Skill을 사용하여 재생 에너지에 관한 프레젠테이션을 만들어 보겠습니다. Messages API의 `container` 매개변수를 사용하여 Skills를 지정합니다:

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
      "content": "Create a presentation about renewable energy with 5 slides"
    }],
    "tools": [{
      "type": "code_execution_20250825",
      "name": "code_execution"
    }]
  }'
```

</details>

각 부분이 무엇을 하는지 살펴보겠습니다:

- **`container.skills`**: Claude가 사용할 수 있는 Skills 지정
- **`type: "anthropic"`**: Anthropic 관리 Skill임을 나타냄
- **`skill_id: "pptx"`**: PowerPoint Skill 식별자
- **`version: "latest"`**: 가장 최근에 게시된 Skill 버전으로 설정
- **`tools`**: 코드 실행 활성화 (Skills에 필수)
- **Beta 헤더**: `code-execution-2025-08-25` 및 `skills-2025-10-02`

이 요청을 수행하면 Claude는 자동으로 작업을 관련 Skill과 매칭합니다. 프레젠테이션을 요청했으므로 Claude는 PowerPoint Skill이 관련이 있다고 판단하고 전체 지침을 로드합니다. 이것이 점진적 공개의 두 번째 단계입니다. 그런 다음 Claude는 Skill의 코드를 실행하여 프레젠테이션을 생성합니다.

## 3단계: 생성된 파일 다운로드

프레젠테이션은 코드 실행 컨테이너에 생성되어 파일로 저장되었습니다. 응답에는 파일 ID가 포함된 파일 참조가 포함됩니다. 파일 ID를 추출하고 Files API를 사용하여 다운로드합니다:

<details>
<summary>REST API 예시</summary>

```bash
# 응답에서 file_id 추출 (jq 사용)
FILE_ID=$(echo "$RESPONSE" | jq -r '.content[] | select(.type=="tool_use" and .name=="code_execution") | .content[] | select(.file_id) | .file_id')

# 파일 다운로드
curl "https://api.anthropic.com/v1/files/$FILE_ID/content" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: files-api-2025-04-14" \
  --output renewable_energy.pptx

echo "Presentation saved to renewable_energy.pptx"
```

</details>


> 생성된 파일 작업에 대한 자세한 내용은 [코드 실행 도구 문서](../03-tools/05-code-execution-tool.md)를 참조하세요.


## 더 많은 예제 시도하기

Skills를 사용하여 첫 문서를 만들었으니, 이제 다음 변형을 시도해 보세요:

### 스프레드시트 만들기

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
        }
      ]
    },
    "messages": [{
      "role": "user",
      "content": "Create a quarterly sales tracking spreadsheet with sample data"
    }],
    "tools": [{
      "type": "code_execution_20250825",
      "name": "code_execution"
    }]
  }'
```

</details>

### Word 문서 만들기

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
          "skill_id": "docx",
          "version": "latest"
        }
      ]
    },
    "messages": [{
      "role": "user",
      "content": "Write a 2-page report on the benefits of renewable energy"
    }],
    "tools": [{
      "type": "code_execution_20250825",
      "name": "code_execution"
    }]
  }'
```

</details>

### PDF 생성하기

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
          "skill_id": "pdf",
          "version": "latest"
        }
      ]
    },
    "messages": [{
      "role": "user",
      "content": "Generate a PDF invoice template"
    }],
    "tools": [{
      "type": "code_execution_20250825",
      "name": "code_execution"
    }]
  }'
```

</details>

## 다음 단계

사전 구축된 Agent Skills를 사용해 보았으니 이제 다음을 수행할 수 있습니다:


  
> Claude API와 함께 Skills 사용하기

  
> 특수 작업을 위한 자체 Skills 업로드

  
> 효과적인 Skills 작성을 위한 모범 사례 배우기

  
> Claude Code의 Skills에 대해 배우기

  
> TypeScript와 Python에서 프로그래밍 방식으로 Skills 사용

  
> Skills 예제 및 구현 패턴 탐색



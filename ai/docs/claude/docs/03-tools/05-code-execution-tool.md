# [코드 실행 도구](https://platform.claude.com/docs/en/agents-and-tools/tool-use/code-execution-tool)

---

Claude는 API 대화 내에서 직접 데이터를 분석하고, 시각화를 생성하며, 복잡한 계산을 수행하고, 시스템 명령을 실행하며, 파일을 생성 및 편집하고, 업로드된 파일을 처리할 수 있습니다.

코드 실행 도구를 사용하면 Claude가 안전한 샌드박스 환경에서 Bash 명령을 실행하고 코드 작성을 포함한 파일 조작을 할 수 있습니다.


> 코드 실행 도구는 현재 공개 베타 상태입니다.
>
> 이 기능을 사용하려면 API 요청에 `"code-execution-2025-08-25"` [베타 헤더](https://platform.claude.com/docs/en/api/beta-headers)를 추가해야 합니다.
>
> 이 기능에 대한 피드백은 [피드백 양식](https://forms.gle/LTAU6Xn2puCJMi1n6)을 통해 공유해 주세요.


## 모델 호환성

코드 실행 도구는 다음 모델에서 사용할 수 있습니다:

| 모델 | 도구 버전 |
|-------|--------------|
| Claude Opus 4.5 (`claude-opus-4-5-20251101`) | `code_execution_20250825` |
| Claude Opus 4.1 (`claude-opus-4-1-20250805`) | `code_execution_20250825` |
| Claude Opus 4 (`claude-opus-4-20250514`) | `code_execution_20250825` |
| Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`) | `code_execution_20250825` |
| Claude Sonnet 4 (`claude-sonnet-4-20250514`) | `code_execution_20250825` |
| Claude Sonnet 3.7 (`claude-3-7-sonnet-20250219`) ([지원 종료됨](https://platform.claude.com/docs/en/about-claude/model-deprecations)) | `code_execution_20250825` |
| Claude Haiku 4.5 (`claude-haiku-4-5-20251001`) | `code_execution_20250825` |
| Claude Haiku 3.5 (`claude-3-5-haiku-latest`) ([지원 종료됨](https://platform.claude.com/docs/en/about-claude/model-deprecations)) | `code_execution_20250825` |


> 현재 버전인 `code_execution_20250825`는 Bash 명령과 파일 작업을 지원합니다. 레거시 버전인 `code_execution_20250522`(Python만 지원)도 여전히 사용할 수 있습니다. 마이그레이션 세부 사항은 [최신 도구 버전으로 업그레이드](#upgrade-to-latest-tool-version)를 참조하세요.



> 이전 도구 버전은 최신 모델과의 하위 호환성이 보장되지 않습니다. 항상 모델 버전에 해당하는 도구 버전을 사용하세요.


## 빠른 시작

다음은 Claude에게 계산을 수행하도록 요청하는 간단한 예제입니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: code-execution-2025-08-25" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [
            {
                "role": "user",
                "content": "Calculate the mean and standard deviation of [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"
            }
        ],
        "tools": [{
            "type": "code_execution_20250825",
            "name": "code_execution"
        }]
    }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["code-execution-2025-08-25"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "Calculate the mean and standard deviation of [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"
    }],
    tools=[{
        "type": "code_execution_20250825",
        "name": "code_execution"
    }]
)

print(response)
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

async function main() {
  const response = await anthropic.beta.messages.create({
    model: "claude-sonnet-4-5",
    betas: ["code-execution-2025-08-25"],
    max_tokens: 4096,
    messages: [
      {
        role: "user",
        content: "Calculate the mean and standard deviation of [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]"
      }
    ],
    tools: [{
      type: "code_execution_20250825",
      name: "code_execution"
    }]
  });

  console.log(response);
}

main().catch(console.error);
```
</CodeGroup>

## 코드 실행 작동 방식

API 요청에 코드 실행 도구를 추가하면:

1. Claude가 코드 실행이 질문에 답하는 데 도움이 될지 평가합니다
2. 도구는 자동으로 Claude에게 다음 기능을 제공합니다:
   - **Bash 명령**: 시스템 작업 및 패키지 관리를 위한 셸 명령 실행
   - **파일 작업**: 코드 작성을 포함하여 파일을 직접 생성, 보기 및 편집
3. Claude는 단일 요청에서 이러한 기능의 모든 조합을 사용할 수 있습니다
4. 모든 작업은 안전한 샌드박스 환경에서 실행됩니다
5. Claude는 생성된 차트, 계산 또는 분석과 함께 결과를 제공합니다

## 도구 사용 방법

### Bash 명령 실행

Claude에게 시스템 정보를 확인하고 패키지를 설치하도록 요청합니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: code-execution-2025-08-25" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [{
            "role": "user",
            "content": "Check the Python version and list installed packages"
        }],
        "tools": [{
            "type": "code_execution_20250825",
            "name": "code_execution"
        }]
    }'
```

```python Python
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["code-execution-2025-08-25"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "Check the Python version and list installed packages"
    }],
    tools=[{
        "type": "code_execution_20250825",
        "name": "code_execution"
    }]
)
```

```typescript TypeScript
const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5",
  betas: ["code-execution-2025-08-25"],
  max_tokens: 4096,
  messages: [{
    role: "user",
    content: "Check the Python version and list installed packages"
  }],
  tools: [{
    type: "code_execution_20250825",
    name: "code_execution"
  }]
});
```
</CodeGroup>

### 파일 직접 생성 및 편집

Claude는 파일 조작 기능을 사용하여 샌드박스에서 직접 파일을 생성, 보기 및 편집할 수 있습니다:

<CodeGroup>
```bash Shell
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: code-execution-2025-08-25" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [{
            "role": "user",
            "content": "Create a config.yaml file with database settings, then update the port from 5432 to 3306"
        }],
        "tools": [{
            "type": "code_execution_20250825",
            "name": "code_execution"
        }]
    }'
```

```python Python
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["code-execution-2025-08-25"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "Create a config.yaml file with database settings, then update the port from 5432 to 3306"
    }],
    tools=[{
        "type": "code_execution_20250825",
        "name": "code_execution"
    }]
)
```

```typescript TypeScript
const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5",
  betas: ["code-execution-2025-08-25"],
  max_tokens: 4096,
  messages: [{
    role: "user",
    content: "Create a config.yaml file with database settings, then update the port from 5432 to 3306"
  }],
  tools: [{
    type: "code_execution_20250825",
    name: "code_execution"
  }]
});
```
</CodeGroup>

### 자체 파일 업로드 및 분석

자체 데이터 파일(CSV, Excel, 이미지 등)을 분석하려면 Files API를 통해 업로드하고 요청에서 참조하세요:


> 코드 실행과 함께 Files API를 사용하려면 두 개의 베타 헤더가 필요합니다: `"anthropic-beta": "code-execution-2025-08-25,files-api-2025-04-14"`


Python 환경은 Files API를 통해 업로드된 다양한 파일 형식을 처리할 수 있습니다:

- CSV
- Excel (.xlsx, .xls)
- JSON
- XML
- 이미지 (JPEG, PNG, GIF, WebP)
- 텍스트 파일 (.txt, .md, .py 등)

#### 파일 업로드 및 분석

1. [Files API](https://platform.claude.com/docs/en/build-with-claude/files)를 사용하여 **파일을 업로드**합니다
2. `container_upload` 콘텐츠 블록을 사용하여 메시지에서 **파일을 참조**합니다
3. API 요청에 **코드 실행 도구를 포함**합니다

<CodeGroup>
```bash Shell
# 먼저 파일을 업로드합니다
curl https://api.anthropic.com/v1/files \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: files-api-2025-04-14" \
    --form 'file=@"data.csv"' \

# 그런 다음 코드 실행과 함께 file_id를 사용합니다
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: code-execution-2025-08-25,files-api-2025-04-14" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [{
            "role": "user",
            "content": [
                {"type": "text", "text": "Analyze this CSV data"},
                {"type": "container_upload", "file_id": "file_abc123"}
            ]
        }],
        "tools": [{
            "type": "code_execution_20250825",
            "name": "code_execution"
        }]
    }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

# 파일 업로드
file_object = client.beta.files.upload(
    file=open("data.csv", "rb"),
)

# 코드 실행과 함께 file_id 사용
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["code-execution-2025-08-25", "files-api-2025-04-14"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": [
            {"type": "text", "text": "Analyze this CSV data"},
            {"type": "container_upload", "file_id": file_object.id}
        ]
    }],
    tools=[{
        "type": "code_execution_20250825",
        "name": "code_execution"
    }]
)
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';
import { createReadStream } from 'fs';

const anthropic = new Anthropic();

async function main() {
  // 파일 업로드
  const fileObject = await anthropic.beta.files.create({
    file: createReadStream("data.csv"),
  });

  // 코드 실행과 함께 file_id 사용
  const response = await anthropic.beta.messages.create({
    model: "claude-sonnet-4-5",
    betas: ["code-execution-2025-08-25", "files-api-2025-04-14"],
    max_tokens: 4096,
    messages: [{
      role: "user",
      content: [
        { type: "text", text: "Analyze this CSV data" },
        { type: "container_upload", file_id: fileObject.id }
      ]
    }],
    tools: [{
      type: "code_execution_20250825",
      name: "code_execution"
    }]
  });

  console.log(response);
}

main().catch(console.error);
```
</CodeGroup>

#### 생성된 파일 검색

Claude가 코드 실행 중에 파일을 생성하면 Files API를 사용하여 이러한 파일을 검색할 수 있습니다:

<CodeGroup>
```python Python
from anthropic import Anthropic

# 클라이언트 초기화
client = Anthropic()

# 파일을 생성하는 코드 실행 요청
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["code-execution-2025-08-25", "files-api-2025-04-14"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "Create a matplotlib visualization and save it as output.png"
    }],
    tools=[{
        "type": "code_execution_20250825",
        "name": "code_execution"
    }]
)

# 응답에서 파일 ID 추출
def extract_file_ids(response):
    file_ids = []
    for item in response.content:
        if item.type == 'bash_code_execution_tool_result':
            content_item = item.content
            if content_item.type == 'bash_code_execution_result':
                for file in content_item.content:
                    if hasattr(file, 'file_id'):
                        file_ids.append(file.file_id)
    return file_ids

# 생성된 파일 다운로드
for file_id in extract_file_ids(response):
    file_metadata = client.beta.files.retrieve_metadata(file_id)
    file_content = client.beta.files.download(file_id)
    file_content.write_to_file(file_metadata.filename)
    print(f"Downloaded: {file_metadata.filename}")
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';
import { writeFileSync } from 'fs';

// 클라이언트 초기화
const anthropic = new Anthropic();

async function main() {
  // 파일을 생성하는 코드 실행 요청
  const response = await anthropic.beta.messages.create({
    model: "claude-sonnet-4-5",
    betas: ["code-execution-2025-08-25", "files-api-2025-04-14"],
    max_tokens: 4096,
    messages: [{
      role: "user",
      content: "Create a matplotlib visualization and save it as output.png"
    }],
    tools: [{
      type: "code_execution_20250825",
      name: "code_execution"
    }]
  });

  // 응답에서 파일 ID 추출
  function extractFileIds(response: any): string[] {
    const fileIds: string[] = [];
    for (const item of response.content) {
      if (item.type === 'bash_code_execution_tool_result') {
        const contentItem = item.content;
        if (contentItem.type === 'bash_code_execution_result' && contentItem.content) {
          for (const file of contentItem.content) {
            fileIds.push(file.file_id);
          }
        }
      }
    }
    return fileIds;
  }

  // 생성된 파일 다운로드
  const fileIds = extractFileIds(response);
  for (const fileId of fileIds) {
    const fileMetadata = await anthropic.beta.files.retrieveMetadata(fileId);
    const fileContent = await anthropic.beta.files.download(fileId);

    // ReadableStream을 Buffer로 변환하고 저장
    const chunks: Uint8Array[] = [];
    for await (const chunk of fileContent) {
      chunks.push(chunk);
    }
    const buffer = Buffer.concat(chunks);
    writeFileSync(fileMetadata.filename, buffer);
    console.log(`Downloaded: ${fileMetadata.filename}`);
  }
}

main().catch(console.error);
```
</CodeGroup>

### 작업 결합

모든 기능을 사용하는 복잡한 워크플로우:

<CodeGroup>
```bash Shell
# 먼저 파일을 업로드합니다
curl https://api.anthropic.com/v1/files \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: files-api-2025-04-14" \
    --form 'file=@"data.csv"' \
    > file_response.json

# file_id 추출 (jq 사용)
FILE_ID=$(jq -r '.id' file_response.json)

# 그런 다음 코드 실행과 함께 사용합니다
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: code-execution-2025-08-25,files-api-2025-04-14" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [{
            "role": "user",
            "content": [
                {
                    "type": "text",
                    "text": "Analyze this CSV data: create a summary report, save visualizations, and create a README with the findings"
                },
                {
                    "type": "container_upload",
                    "file_id": "'$FILE_ID'"
                }
            ]
        }],
        "tools": [{
            "type": "code_execution_20250825",
            "name": "code_execution"
        }]
    }'
```

```python Python
# 파일 업로드
file_object = client.beta.files.upload(
    file=open("data.csv", "rb"),
)

# 코드 실행과 함께 사용
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["code-execution-2025-08-25", "files-api-2025-04-14"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": [
            {"type": "text", "text": "Analyze this CSV data: create a summary report, save visualizations, and create a README with the findings"},
            {"type": "container_upload", "file_id": file_object.id}
        ]
    }],
    tools=[{
        "type": "code_execution_20250825",
        "name": "code_execution"
    }]
)

# Claude는 다음을 수행할 수 있습니다:
# 1. bash를 사용하여 파일 크기 확인 및 데이터 미리보기
# 2. text_editor를 사용하여 CSV를 분석하고 시각화를 생성하는 Python 코드 작성
# 3. bash를 사용하여 Python 코드 실행
# 4. text_editor를 사용하여 결과가 포함된 README.md 생성
# 5. bash를 사용하여 파일을 보고서 디렉터리로 구성
```

```typescript TypeScript
// 파일 업로드
const fileObject = await anthropic.beta.files.create({
  file: createReadStream("data.csv"),
});

// 코드 실행과 함께 사용
const response = await anthropic.beta.messages.create({
  model: "claude-sonnet-4-5",
  betas: ["code-execution-2025-08-25", "files-api-2025-04-14"],
  max_tokens: 4096,
  messages: [{
    role: "user",
    content: [
      {type: "text", text: "Analyze this CSV data: create a summary report, save visualizations, and create a README with the findings"},
      {type: "container_upload", file_id: fileObject.id}
    ]
  }],
  tools: [{
    type: "code_execution_20250825",
    name: "code_execution"
  }]
});

// Claude는 다음을 수행할 수 있습니다:
// 1. bash를 사용하여 파일 크기 확인 및 데이터 미리보기
// 2. text_editor를 사용하여 CSV를 분석하고 시각화를 생성하는 Python 코드 작성
// 3. bash를 사용하여 Python 코드 실행
// 4. text_editor를 사용하여 결과가 포함된 README.md 생성
// 5. bash를 사용하여 파일을 보고서 디렉터리로 구성
```
</CodeGroup>

## 도구 정의

코드 실행 도구는 추가 매개변수가 필요하지 않습니다:

```json JSON
{
  "type": "code_execution_20250825",
  "name": "code_execution"
}
```

이 도구가 제공되면 Claude는 자동으로 두 개의 하위 도구에 접근할 수 있습니다:
- `bash_code_execution`: 셸 명령 실행
- `text_editor_code_execution`: 코드 작성을 포함한 파일 보기, 생성 및 편집

## 응답 형식

코드 실행 도구는 작업에 따라 두 가지 유형의 결과를 반환할 수 있습니다:

### Bash 명령 응답

```json
{
  "type": "server_tool_use",
  "id": "srvtoolu_01B3C4D5E6F7G8H9I0J1K2L3",
  "name": "bash_code_execution",
  "input": {
    "command": "ls -la | head -5"
  }
},
{
  "type": "bash_code_execution_tool_result",
  "tool_use_id": "srvtoolu_01B3C4D5E6F7G8H9I0J1K2L3",
  "content": {
    "type": "bash_code_execution_result",
    "stdout": "total 24\ndrwxr-xr-x 2 user user 4096 Jan 1 12:00 .\ndrwxr-xr-x 3 user user 4096 Jan 1 11:00 ..\n-rw-r--r-- 1 user user  220 Jan 1 12:00 data.csv\n-rw-r--r-- 1 user user  180 Jan 1 12:00 config.json",
    "stderr": "",
    "return_code": 0
  }
}
```

### 파일 작업 응답

**파일 보기:**
```json
{
  "type": "server_tool_use",
  "id": "srvtoolu_01C4D5E6F7G8H9I0J1K2L3M4",
  "name": "text_editor_code_execution",
  "input": {
    "command": "view",
    "path": "config.json"
  }
},
{
  "type": "text_editor_code_execution_tool_result",
  "tool_use_id": "srvtoolu_01C4D5E6F7G8H9I0J1K2L3M4",
  "content": {
    "type": "text_editor_code_execution_result",
    "file_type": "text",
    "content": "{\n  \"setting\": \"value\",\n  \"debug\": true\n}",
    "numLines": 4,
    "startLine": 1,
    "totalLines": 4
  }
}
```

**파일 생성:**
```json
{
  "type": "server_tool_use",
  "id": "srvtoolu_01D5E6F7G8H9I0J1K2L3M4N5",
  "name": "text_editor_code_execution",
  "input": {
    "command": "create",
    "path": "new_file.txt",
    "file_text": "Hello, World!"
  }
},
{
  "type": "text_editor_code_execution_tool_result",
  "tool_use_id": "srvtoolu_01D5E6F7G8H9I0J1K2L3M4N5",
  "content": {
    "type": "text_editor_code_execution_result",
    "is_file_update": false
  }
}
```

**파일 편집 (str_replace):**
```json
{
  "type": "server_tool_use",
  "id": "srvtoolu_01E6F7G8H9I0J1K2L3M4N5O6",
  "name": "text_editor_code_execution",
  "input": {
    "command": "str_replace",
    "path": "config.json",
    "old_str": "\"debug\": true",
    "new_str": "\"debug\": false"
  }
},
{
  "type": "text_editor_code_execution_tool_result",
  "tool_use_id": "srvtoolu_01E6F7G8H9I0J1K2L3M4N5O6",
  "content": {
    "type": "text_editor_code_execution_result",
    "oldStart": 3,
    "oldLines": 1,
    "newStart": 3,
    "newLines": 1,
    "lines": ["-  \"debug\": true", "+  \"debug\": false"]
  }
}
```

### 결과

모든 실행 결과에는 다음이 포함됩니다:
- `stdout`: 성공적인 실행의 출력
- `stderr`: 실행 실패 시 오류 메시지
- `return_code`: 성공 시 0, 실패 시 0이 아닌 값

파일 작업을 위한 추가 필드:
- **보기**: `file_type`, `content`, `numLines`, `startLine`, `totalLines`
- **생성**: `is_file_update` (파일이 이미 존재했는지 여부)
- **편집**: `oldStart`, `oldLines`, `newStart`, `newLines`, `lines` (차이 형식)

### 오류

각 도구 유형은 특정 오류를 반환할 수 있습니다:

**공통 오류 (모든 도구):**
```json
{
  "type": "bash_code_execution_tool_result",
  "tool_use_id": "srvtoolu_01VfmxgZ46TiHbmXgy928hQR",
  "content": {
    "type": "bash_code_execution_tool_result_error",
    "error_code": "unavailable"
  }
}
```

**도구 유형별 오류 코드:**

| 도구 | 오류 코드 | 설명 |
|------|-----------|-------------|
| 모든 도구 | `unavailable` | 도구를 일시적으로 사용할 수 없음 |
| 모든 도구 | `execution_time_exceeded` | 실행이 최대 시간 제한을 초과함 |
| 모든 도구 | `container_expired` | 컨테이너가 만료되어 더 이상 사용할 수 없음 |
| 모든 도구 | `invalid_tool_input` | 도구에 잘못된 매개변수가 제공됨 |
| 모든 도구 | `too_many_requests` | 도구 사용에 대한 속도 제한 초과 |
| text_editor | `file_not_found` | 파일이 존재하지 않음 (보기/편집 작업의 경우) |
| text_editor | `string_not_found` | 파일에서 `old_str`을 찾을 수 없음 (str_replace의 경우) |

#### `pause_turn` 중지 이유

응답에는 `pause_turn` 중지 이유가 포함될 수 있으며, 이는 API가 오래 실행되는 턴을 일시 중지했음을 나타냅니다. Claude가 턴을 계속하도록 하려면 응답을 그대로 후속 요청에 제공하거나, 대화를 중단하고 싶다면 콘텐츠를 수정할 수 있습니다.

## 컨테이너

코드 실행 도구는 특히 Python에 중점을 둔 코드 실행을 위해 특별히 설계된 안전한 컨테이너화된 환경에서 실행됩니다.

### 런타임 환경
- **Python 버전**: 3.11.12
- **운영 체제**: Linux 기반 컨테이너
- **아키텍처**: x86_64 (AMD64)

### 리소스 제한
- **메모리**: 5GiB RAM
- **디스크 공간**: 5GiB 작업 공간 스토리지
- **CPU**: 1 CPU

### 네트워킹 및 보안
- **인터넷 액세스**: 보안을 위해 완전히 비활성화됨
- **외부 연결**: 아웃바운드 네트워크 요청이 허용되지 않음
- **샌드박스 격리**: 호스트 시스템 및 다른 컨테이너와 완전히 격리됨
- **파일 액세스**: 작업 공간 디렉터리로만 제한됨
- **작업 공간 범위**: [Files](https://platform.claude.com/docs/en/build-with-claude/files)와 마찬가지로 컨테이너는 API 키의 작업 공간으로 범위가 지정됨
- **만료**: 컨테이너는 생성 후 30일 후에 만료됨

### 사전 설치된 라이브러리
샌드박스 Python 환경에는 다음과 같이 일반적으로 사용되는 라이브러리가 포함되어 있습니다:
- **데이터 과학**: pandas, numpy, scipy, scikit-learn, statsmodels
- **시각화**: matplotlib, seaborn
- **파일 처리**: pyarrow, openpyxl, xlsxwriter, xlrd, pillow, python-pptx, python-docx, pypdf, pdfplumber, pypdfium2, pdf2image, pdfkit, tabula-py, reportlab[pycairo], Img2pdf
- **수학 및 컴퓨팅**: sympy, mpmath
- **유틸리티**: tqdm, python-dateutil, pytz, joblib, unzip, unrar, 7zip, bc, rg (ripgrep), fd, sqlite

## 컨테이너 재사용

이전 응답의 컨테이너 ID를 제공하여 여러 API 요청에서 기존 컨테이너를 재사용할 수 있습니다.
이를 통해 요청 간에 생성된 파일을 유지할 수 있습니다.

### 예제

<CodeGroup>
```python Python
import os
from anthropic import Anthropic

# 클라이언트 초기화
client = Anthropic(
    api_key=os.getenv("ANTHROPIC_API_KEY")
)

# 첫 번째 요청: 무작위 숫자로 파일 생성
response1 = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["code-execution-2025-08-25"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "Write a file with a random number and save it to '/tmp/number.txt'"
    }],
    tools=[{
        "type": "code_execution_20250825",
        "name": "code_execution"
    }]
)

# 첫 번째 응답에서 컨테이너 ID 추출
container_id = response1.container.id

# 두 번째 요청: 컨테이너를 재사용하여 파일 읽기
response2 = client.beta.messages.create(
    container=container_id,  # 동일한 컨테이너 재사용
    model="claude-sonnet-4-5",
    betas=["code-execution-2025-08-25"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "Read the number from '/tmp/number.txt' and calculate its square"
    }],
    tools=[{
        "type": "code_execution_20250825",
        "name": "code_execution"
    }]
)
```

```typescript TypeScript
import { Anthropic } from '@anthropic-ai/sdk';

const anthropic = new Anthropic();

async function main() {
  // 첫 번째 요청: 무작위 숫자로 파일 생성
  const response1 = await anthropic.beta.messages.create({
    model: "claude-sonnet-4-5",
    betas: ["code-execution-2025-08-25"],
    max_tokens: 4096,
    messages: [{
      role: "user",
      content: "Write a file with a random number and save it to '/tmp/number.txt'"
    }],
    tools: [{
      type: "code_execution_20250825",
      name: "code_execution"
    }]
  });

  // 첫 번째 응답에서 컨테이너 ID 추출
  const containerId = response1.container.id;

  // 두 번째 요청: 컨테이너를 재사용하여 파일 읽기
  const response2 = await anthropic.beta.messages.create({
    container: containerId,  // 동일한 컨테이너 재사용
    model: "claude-sonnet-4-5",
    betas: ["code-execution-2025-08-25"],
    max_tokens: 4096,
    messages: [{
      role: "user",
      content: "Read the number from '/tmp/number.txt' and calculate its square"
    }],
    tools: [{
      type: "code_execution_20250825",
      name: "code_execution"
    }]
  });

  console.log(response2.content);
}

main().catch(console.error);
```

```bash Shell
# 첫 번째 요청: 무작위 숫자로 파일 생성
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: code-execution-2025-08-25" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [{
            "role": "user",
            "content": "Write a file with a random number and save it to \"/tmp/number.txt\""
        }],
        "tools": [{
            "type": "code_execution_20250825",
            "name": "code_execution"
        }]
    }' > response1.json

# 응답에서 컨테이너 ID 추출 (jq 사용)
CONTAINER_ID=$(jq -r '.container.id' response1.json)

# 두 번째 요청: 컨테이너를 재사용하여 파일 읽기
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: code-execution-2025-08-25" \
    --header "content-type: application/json" \
    --data '{
        "container": "'$CONTAINER_ID'",
        "model": "claude-sonnet-4-5",
        "max_tokens": 4096,
        "messages": [{
            "role": "user",
            "content": "Read the number from \"/tmp/number.txt\" and calculate its square"
        }],
        "tools": [{
            "type": "code_execution_20250825",
            "name": "code_execution"
        }]
    }'
```
</CodeGroup>

## 스트리밍

스트리밍이 활성화되면 코드 실행 이벤트가 발생하는 대로 수신됩니다:

```javascript
event: content_block_start
data: {"type": "content_block_start", "index": 1, "content_block": {"type": "server_tool_use", "id": "srvtoolu_xyz789", "name": "code_execution"}}

// 코드 실행이 스트리밍됨
event: content_block_delta
data: {"type": "content_block_delta", "index": 1, "delta": {"type": "input_json_delta", "partial_json": "{\"code\":\"import pandas as pd\\ndf = pd.read_csv('data.csv')\\nprint(df.head())\"}"}}

// 코드 실행 중 일시 중지

// 실행 결과가 스트리밍됨
event: content_block_start
data: {"type": "content_block_start", "index": 2, "content_block": {"type": "code_execution_tool_result", "tool_use_id": "srvtoolu_xyz789", "content": {"stdout": "   A  B  C\n0  1  2  3\n1  4  5  6", "stderr": ""}}}
```

## 배치 요청

[Messages Batches API](https://platform.claude.com/docs/en/build-with-claude/batch-processing)에 코드 실행 도구를 포함할 수 있습니다. Messages Batches API를 통한 코드 실행 도구 호출은 일반 Messages API 요청과 동일한 가격으로 청구됩니다.

## 사용량 및 가격

코드 실행 도구 사용량은 토큰 사용량과 별도로 추적됩니다. 실행 시간은 최소 5분입니다.
요청에 파일이 포함된 경우, 파일이 컨테이너에 사전 로드되므로 도구를 사용하지 않더라도 실행 시간이 청구됩니다.

각 조직은 월별로 코드 실행 도구를 사용하여 1,550시간의 무료 사용량을 받습니다. 처음 1,550시간을 초과하는 추가 사용량은 컨테이너당 시간당 $0.05로 청구됩니다.

## 최신 도구 버전으로 업그레이드

`code-execution-2025-08-25`로 업그레이드하면 파일 조작 및 Bash 기능(여러 언어의 코드 포함)에 액세스할 수 있습니다. 가격 차이는 없습니다.

### 변경 사항

| 구성 요소 | 레거시 | 현재 |
|-----------|------------------|----------------------------|
| 베타 헤더 | `code-execution-2025-05-22` | `code-execution-2025-08-25` |
| 도구 유형 | `code_execution_20250522` | `code_execution_20250825` |
| 기능 | Python만 | Bash 명령, 파일 작업 |
| 응답 유형 | `code_execution_result` | `bash_code_execution_result`, `text_editor_code_execution_result` |

### 하위 호환성

- 모든 기존 Python 코드 실행은 이전과 정확히 동일하게 계속 작동합니다
- 기존 Python 전용 워크플로우에는 변경이 필요하지 않습니다

### 업그레이드 단계

업그레이드하려면 API 요청에서 다음 변경 사항을 수행해야 합니다:

1. **베타 헤더 업데이트**:
   ```diff
   - "anthropic-beta": "code-execution-2025-05-22"
   + "anthropic-beta": "code-execution-2025-08-25"
   ```

2. **도구 유형 업데이트**:
   ```diff
   - "type": "code_execution_20250522"
   + "type": "code_execution_20250825"
   ```

3. **응답 처리 검토** (프로그래밍 방식으로 응답을 구문 분석하는 경우):
   - Python 실행 응답에 대한 이전 블록은 더 이상 전송되지 않습니다
   - 대신 Bash 및 파일 작업에 대한 새로운 응답 유형이 전송됩니다(응답 형식 섹션 참조)

## 프로그래밍 방식 도구 호출

코드 실행 도구는 [프로그래밍 방식 도구 호출](https://platform.claude.com/docs/en/agents-and-tools/tool-use/programmatic-tool-calling)을 지원하며, 이를 통해 Claude가 실행 컨테이너 내에서 프로그래밍 방식으로 사용자 정의 도구를 호출하는 코드를 작성할 수 있습니다. 이를 통해 효율적인 다중 도구 워크플로우, Claude의 컨텍스트에 도달하기 전에 데이터 필터링 및 복잡한 조건부 로직이 가능합니다.

<CodeGroup>
```python Python
# 도구에 대해 프로그래밍 방식 호출 활성화
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    betas=["advanced-tool-use-2025-11-20"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "Get weather for 5 cities and find the warmest"
    }],
    tools=[
        {
            "type": "code_execution_20250825",
            "name": "code_execution"
        },
        {
            "name": "get_weather",
            "description": "Get weather for a city",
            "input_schema": {...},
            "allowed_callers": ["code_execution_20250825"]  # 프로그래밍 방식 호출 활성화
        }
    ]
)
```
</CodeGroup>

자세한 내용은 [프로그래밍 방식 도구 호출 문서](https://platform.claude.com/docs/en/agents-and-tools/tool-use/programmatic-tool-calling)를 참조하세요.

## Agent Skills와 함께 코드 실행 사용

코드 실행 도구를 사용하면 Claude가 [Agent Skills](https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview)를 사용할 수 있습니다. Skills는 Claude의 기능을 확장하는 지침, 스크립트 및 리소스로 구성된 모듈식 기능입니다.

자세한 내용은 [Agent Skills 문서](https://platform.claude.com/docs/en/agents-and-tools/agent-skills/overview) 및 [Agent Skills API 가이드](https://platform.claude.com/docs/en/build-with-claude/skills-guide)를 참조하세요.

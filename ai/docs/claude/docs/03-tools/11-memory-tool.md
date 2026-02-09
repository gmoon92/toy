# [Memory 도구](https://platform.claude.com/docs/en/agents-and-tools/tool-use/memory-tool)

---

Memory 도구는 Claude가 메모리 파일 디렉토리를 통해 대화 간에 정보를 저장하고 검색할 수 있도록 합니다. Claude는 세션 간에 지속되는 파일을 생성, 읽기, 업데이트 및 삭제할 수 있으므로, 모든 것을 컨텍스트 윈도우에 유지하지 않고도 시간이 지남에 따라 지식을 구축할 수 있습니다.

Memory 도구는 클라이언트 측에서 작동합니다. 즉, 사용자가 자체 인프라를 통해 데이터가 저장되는 위치와 방법을 제어합니다.


> Memory 도구는 현재 베타 버전입니다. 이를 활성화하려면 API 요청에 베타 헤더 `context-management-2025-06-27`을 사용하세요.
>
> 이 기능에 대한 피드백을 [피드백 양식](https://forms.gle/YXC2EKGMhjN1c4L88)을 통해 공유해 주세요.


## 사용 사례

- 여러 에이전트 실행에 걸쳐 프로젝트 컨텍스트 유지
- 과거 상호작용, 결정 및 피드백으로부터 학습
- 시간이 지남에 따라 지식 베이스 구축
- Claude가 반복적인 워크플로우에서 향상되는 대화 간 학습 활성화

## 작동 방식

활성화되면 Claude는 작업을 시작하기 전에 자동으로 메모리 디렉토리를 확인합니다. Claude는 작업하는 동안 학습한 내용을 저장하기 위해 `/memories` 디렉토리에서 파일을 생성, 읽기, 업데이트 및 삭제할 수 있으며, 나중에 해당 메모리를 참조하여 유사한 작업을 더 효과적으로 처리하거나 중단된 지점부터 재개할 수 있습니다.

이것은 클라이언트 측 도구이므로 Claude는 메모리 작업을 수행하기 위해 도구 호출을 하고, 애플리케이션은 해당 작업을 로컬에서 실행합니다. 이를 통해 메모리가 저장되는 위치와 방법을 완전히 제어할 수 있습니다. 보안을 위해 모든 메모리 작업을 `/memories` 디렉토리로 제한해야 합니다.

### 예제: Memory 도구 호출 작동 방식

Claude에게 작업 도움을 요청하면 Claude는 자동으로 먼저 메모리 디렉토리를 확인합니다. 일반적인 상호작용은 다음과 같습니다:

**1. 사용자 요청:**
```
"이 고객 서비스 티켓에 응답하도록 도와주세요."
```

**2. Claude가 메모리 디렉토리를 확인:**
```
"고객 서비스 티켓에 응답하도록 도와드리겠습니다. 이전 컨텍스트가 있는지 메모리를 확인하겠습니다."
```

Claude가 memory 도구를 호출합니다:
```json
{
  "type": "tool_use",
  "id": "toolu_01C4D5E6F7G8H9I0J1K2L3M4",
  "name": "memory",
  "input": {
    "command": "view",
    "path": "/memories"
  }
}
```

**3. 애플리케이션이 디렉토리 내용을 반환:**
```json
{
  "type": "tool_result",
  "tool_use_id": "toolu_01C4D5E6F7G8H9I0J1K2L3M4",
  "content": "Here're the files and directories up to 2 levels deep in /memories, excluding hidden items and node_modules:\n4.0K\t/memories\n1.5K\t/memories/customer_service_guidelines.xml\n2.0K\t/memories/refund_policies.xml"
}
```

**4. Claude가 관련 파일을 읽음:**
```json
{
  "type": "tool_use",
  "id": "toolu_01D5E6F7G8H9I0J1K2L3M4N5",
  "name": "memory",
  "input": {
    "command": "view",
    "path": "/memories/customer_service_guidelines.xml"
  }
}
```

**5. 애플리케이션이 파일 내용을 반환:**
```json
{
  "type": "tool_result",
  "tool_use_id": "toolu_01D5E6F7G8H9I0J1K2L3M4N5",
  "content": "Here's the content of /memories/customer_service_guidelines.xml with line numbers:\n     1\t<guidelines>\n     2\t<addressing_customers>\n     3\t- Always address customers by their first name\n     4\t- Use empathetic language\n..."
}
```

**6. Claude가 메모리를 사용하여 도움을 제공:**
```
"귀하의 고객 서비스 가이드라인을 기반으로 응답을 작성하도록 도와드릴 수 있습니다. 티켓 세부 정보를 공유해 주세요..."
```

## 지원되는 모델

Memory 도구는 다음 모델에서 사용할 수 있습니다:

- Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`)
- Claude Sonnet 4 (`claude-sonnet-4-20250514`)
- Claude Haiku 4.5 (`claude-haiku-4-5-20251001`)
- Claude Opus 4.5 (`claude-opus-4-5-20251101`)
- Claude Opus 4.1 (`claude-opus-4-1-20250805`)
- Claude Opus 4 (`claude-opus-4-20250514`)

## 시작하기

Memory 도구를 사용하려면:

1. API 요청에 베타 헤더 `context-management-2025-06-27`을 포함
2. 요청에 memory 도구를 추가
3. 메모리 작업을 위한 클라이언트 측 핸들러 구현


> 애플리케이션에서 memory 도구 작업을 처리하려면 각 memory 명령에 대한 핸들러를 구현해야 합니다. 우리의 SDK는 도구 인터페이스를 처리하는 memory 도구 헬퍼를 제공합니다. `BetaAbstractMemoryTool`(Python)을 서브클래스화하거나 `betaMemoryTool`(TypeScript)을 사용하여 자체 메모리 백엔드(파일 기반, 데이터베이스, 클라우드 스토리지, 암호화된 파일 등)를 구현할 수 있습니다.
>
> 작동하는 예제는 다음을 참조하세요:
> - Python: [examples/memory/basic.py](https://github.com/anthropics/anthropic-sdk-python/blob/main/examples/memory/basic.py)
> - TypeScript: [examples/tools-helpers-memory.ts](https://github.com/anthropics/anthropic-sdk-typescript/blob/main/examples/tools-helpers-memory.ts)


## 기본 사용법

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "content-type: application/json" \
    --header "anthropic-beta: context-management-2025-06-27" \
    --data '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 2048,
        "messages": [
            {
                "role": "user",
                "content": "I'\''m working on a Python web scraper that keeps crashing with a timeout error. Here'\''s the problematic function:\n\n
```

</details>

## 도구 명령

클라이언트 측 구현은 이러한 memory 도구 명령을 처리해야 합니다. 이러한 사양은 Claude가 가장 익숙한 권장 동작을 설명하지만, 사용 사례에 필요한 대로 구현을 수정하고 문자열을 반환할 수 있습니다.

### view
선택적 라인 범위로 디렉토리 내용 또는 파일 내용을 표시합니다:

```json
{
  "command": "view",
  "path": "/memories",
  "view_range": [1, 10]  // 선택사항: 특정 라인 보기
}
```

#### 반환 값

**디렉토리의 경우:** 파일과 디렉토리를 크기와 함께 표시하는 목록을 반환합니다:
```
Here're the files and directories up to 2 levels deep in {path}, excluding hidden items and node_modules:
{size}    {path}
{size}    {path}/{filename1}
{size}    {path}/{filename2}
```

- 최대 2단계 깊이까지 파일 나열
- 사람이 읽을 수 있는 크기 표시(예: `5.5K`, `1.2M`)
- 숨김 항목(`.`으로 시작하는 파일) 및 `node_modules` 제외
- 크기와 경로 사이에 탭 문자 사용

**파일의 경우:** 헤더와 라인 번호가 있는 파일 내용을 반환합니다:
```
Here's the content of {path} with line numbers:
{line_numbers}{tab}{content}
```

라인 번호 형식:
- **너비**: 6자, 공백으로 오른쪽 정렬
- **구분자**: 라인 번호와 내용 사이에 탭 문자
- **인덱싱**: 1부터 시작(첫 번째 라인은 라인 1)
- **라인 제한**: 999,999라인을 초과하는 파일은 오류를 반환해야 함: `"File {path} exceeds maximum line limit of 999,999 lines."`

**출력 예제:**
```
Here's the content of /memories/notes.txt with line numbers:
     1	Hello World
     2	This is line two
    10	Line ten
   100	Line one hundred
```

#### 오류 처리

- **파일/디렉토리가 존재하지 않음**: `"The path {path} does not exist. Please provide a valid path."`

### create
새 파일을 생성합니다:

```json
{
  "command": "create",
  "path": "/memories/notes.txt",
  "file_text": "Meeting notes:\n- Discussed project timeline\n- Next steps defined\n"
}
```

#### 반환 값

- **성공**: `"File created successfully at: {path}"`

#### 오류 처리

- **파일이 이미 존재함**: `"Error: File {path} already exists"`

### str_replace
파일의 텍스트를 교체합니다:

```json
{
  "command": "str_replace",
  "path": "/memories/preferences.txt",
  "old_str": "Favorite color: blue",
  "new_str": "Favorite color: green"
}
```

#### 반환 값

- **성공**: `"The memory file has been edited."`와 라인 번호가 있는 편집된 파일의 일부

#### 오류 처리

- **파일이 존재하지 않음**: `"Error: The path {path} does not exist. Please provide a valid path."`
- **텍스트를 찾을 수 없음**: ``"No replacement was performed, old_str `{old_str}` did not appear verbatim in {path}."``
- **중복 텍스트**: `old_str`이 여러 번 나타나는 경우: ``"No replacement was performed. Multiple occurrences of old_str `{old_str}` in lines: {line_numbers}. Please ensure it is unique"``

#### 디렉토리 처리

경로가 디렉토리인 경우 "파일이 존재하지 않음" 오류를 반환합니다.

### insert
특정 라인에 텍스트를 삽입합니다:

```json
{
  "command": "insert",
  "path": "/memories/todo.txt",
  "insert_line": 2,
  "insert_text": "- Review memory tool documentation\n"
}
```

#### 반환 값

- **성공**: `"The file {path} has been edited."`

#### 오류 처리

- **파일이 존재하지 않음**: `"Error: The path {path} does not exist"`
- **잘못된 라인 번호**: ``"Error: Invalid `insert_line` parameter: {insert_line}. It should be within the range of lines of the file: [0, {n_lines}]"``

#### 디렉토리 처리

경로가 디렉토리인 경우 "파일이 존재하지 않음" 오류를 반환합니다.

### delete
파일 또는 디렉토리를 삭제합니다:

```json
{
  "command": "delete",
  "path": "/memories/old_file.txt"
}
```

#### 반환 값

- **성공**: `"Successfully deleted {path}"`

#### 오류 처리

- **파일/디렉토리가 존재하지 않음**: `"Error: The path {path} does not exist"`

#### 디렉토리 처리

디렉토리와 모든 내용을 재귀적으로 삭제합니다.

### rename
파일/디렉토리의 이름을 변경하거나 이동합니다:

```json
{
  "command": "rename",
  "old_path": "/memories/draft.txt",
  "new_path": "/memories/final.txt"
}
```

#### 반환 값

- **성공**: `"Successfully renamed {old_path} to {new_path}"`

#### 오류 처리

- **원본이 존재하지 않음**: `"Error: The path {old_path} does not exist"`
- **대상이 이미 존재함**: 오류를 반환(덮어쓰지 않음): `"Error: The destination {new_path} already exists"`

#### 디렉토리 처리

디렉토리의 이름을 변경합니다.

## 프롬프트 가이드

memory 도구가 포함되면 시스템 프롬프트에 자동으로 다음 지시사항을 포함합니다:

```
IMPORTANT: ALWAYS VIEW YOUR MEMORY DIRECTORY BEFORE DOING ANYTHING ELSE.
MEMORY PROTOCOL:
1. Use the `view` command of your `memory` tool to check for earlier progress.
2. ... (work on the task) ...
     - As you make progress, record status / progress / thoughts etc in your memory.
ASSUME INTERRUPTION: Your context window might be reset at any moment, so you risk losing any progress that is not recorded in your memory directory.
```

Claude가 어수선한 메모리 파일을 생성하는 것을 관찰하면 다음 지시사항을 포함할 수 있습니다:

> 참고: 메모리 폴더를 편집할 때는 항상 내용을 최신 상태로 유지하고 일관성 있고 체계적으로 관리하세요. 더 이상 관련이 없는 파일은 이름을 변경하거나 삭제할 수 있습니다. 필요하지 않으면 새 파일을 생성하지 마세요.

또한 Claude가 메모리에 작성하는 내용을 안내할 수 있습니다. 예: "메모리 시스템에는 \<topic\>과 관련된 정보만 기록하세요."

## 보안 고려사항

메모리 저장소를 구현할 때 중요한 보안 문제는 다음과 같습니다:

### 민감한 정보
Claude는 일반적으로 메모리 파일에 민감한 정보를 기록하는 것을 거부합니다. 그러나 잠재적으로 민감한 정보를 제거하는 더 엄격한 검증을 구현하는 것을 고려할 수 있습니다.

### 파일 저장소 크기
메모리 파일 크기를 추적하고 파일이 너무 커지는 것을 방지하는 것을 고려하세요. memory read 명령이 반환할 수 있는 최대 문자 수를 추가하고 Claude가 내용을 페이지별로 읽을 수 있도록 하는 것을 고려하세요.

### 메모리 만료
장시간 액세스하지 않은 메모리 파일을 정기적으로 정리하는 것을 고려하세요.

### 경로 탐색 보호


> 악의적인 경로 입력은 `/memories` 디렉토리 외부의 파일에 액세스하려고 시도할 수 있습니다. 구현에서 디렉토리 탐색 공격을 방지하기 위해 모든 경로를 **반드시** 검증해야 합니다.


다음 안전 조치를 고려하세요:

- 모든 경로가 `/memories`로 시작하는지 확인
- 경로를 정규 형식으로 해석하고 메모리 디렉토리 내에 남아 있는지 확인
- `../`, `..\\` 또는 기타 탐색 패턴을 포함하는 경로 거부
- URL로 인코딩된 탐색 시퀀스 주의(`%2e%2e%2f`)
- 언어의 기본 제공 경로 보안 유틸리티 사용(예: Python의 `pathlib.Path.resolve()` 및 `relative_to()`)

## 오류 처리

Memory 도구는 [text editor 도구](../03-tools/08-text-editor-tool.md)와 유사한 오류 처리 패턴을 사용합니다. 자세한 오류 메시지 및 동작은 위의 개별 도구 명령 섹션을 참조하세요. 일반적인 오류로는 파일을 찾을 수 없음, 권한 오류, 잘못된 경로 및 중복 텍스트 일치가 있습니다.

## Context Editing과 함께 사용

Memory 도구는 [context editing](../02-capabilities/02-context-editing.md)과 결합할 수 있습니다. context editing은 대화 컨텍스트가 구성된 임계값을 초과하면 오래된 도구 결과를 자동으로 지웁니다. 이 조합은 그렇지 않으면 컨텍스트 제한을 초과할 장기 실행 에이전트 워크플로우를 가능하게 합니다.

### 함께 작동하는 방식

Context editing이 활성화되고 대화가 지우기 임계값에 접근하면 Claude는 자동으로 경고 알림을 받습니다. 이것은 Claude가 도구 결과가 컨텍스트 윈도우에서 지워지기 전에 도구 결과의 중요한 정보를 메모리 파일에 보존하도록 유도합니다.

도구 결과가 지워진 후 Claude는 필요할 때마다 메모리 파일에서 저장된 정보를 검색할 수 있으며, 메모리를 작업 컨텍스트의 확장으로 효과적으로 처리합니다. 이를 통해 Claude는 다음을 수행할 수 있습니다:

- 중요한 정보를 잃지 않고 복잡한 다단계 워크플로우 계속
- 도구 결과가 제거된 후에도 과거 작업 및 결정 참조
- 일반적인 컨텍스트 제한을 초과하는 대화에서 일관된 컨텍스트 유지
- 활성 컨텍스트 윈도우를 관리 가능하게 유지하면서 시간이 지남에 따라 지식 베이스 구축

### 예제 워크플로우

많은 파일 작업이 있는 코드 리팩토링 프로젝트를 고려해 보세요:

1. Claude가 파일에 대한 수많은 편집을 수행하여 많은 도구 결과를 생성
2. 컨텍스트가 증가하고 임계값에 접근하면 Claude는 경고를 받음
3. Claude가 지금까지 수행한 변경 사항을 메모리 파일에 요약(예: `/memories/refactoring_progress.xml`)
4. Context editing이 이전 도구 결과를 자동으로 지움
5. Claude가 작업을 계속하고, 이미 완료된 변경 사항을 기억해야 할 때 메모리 파일을 참조
6. 워크플로우가 무한정 계속될 수 있으며, Claude는 활성 컨텍스트와 영구 메모리를 모두 관리

### 구성

두 기능을 함께 사용하려면:

<details>
<summary>Python 예시</summary>

```python
response = client.beta.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=4096,
    messages=[...],
    tools=[
        {
            "type": "memory_20250818",
            "name": "memory"
        },
        # Your other tools
    ],
    betas=["context-management-2025-06-27"],
    context_management={
        "edits": [
            {
                "type": "clear_tool_uses_20250919",
                "trigger": {
                    "type": "input_tokens",
                    "value": 100000
                },
                "keep": {
                    "type": "tool_uses",
                    "value": 3
                }
            }
        ]
    }
)
```

</details>

또한 Claude가 항상 최근 메모리 작업에 액세스할 수 있도록 memory 도구 호출이 지워지지 않도록 제외할 수 있습니다:

<details>
<summary>Python 예시</summary>

```python
context_management={
    "edits": [
        {
            "type": "clear_tool_uses_20250919",
            "exclude_tools": ["memory"]
        }
    ]
}
```

</details>

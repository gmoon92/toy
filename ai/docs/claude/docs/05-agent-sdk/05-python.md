# [Agent SDK 레퍼런스 - Python](https://platform.claude.com/docs/en/agent-sdk/python)

Python Agent SDK의 완전한 API 레퍼런스입니다. 모든 함수, 타입, 클래스를 포함합니다.

---

## 설치

```bash
pip install claude-agent-sdk
```

## `query()`와 `ClaudeSDKClient` 중 선택하기

Python SDK는 Claude Code와 상호작용하는 두 가지 방법을 제공합니다:

### 빠른 비교

| 기능          | `query()`    | `ClaudeSDKClient`  |
|:------------|:-------------|:-------------------|
| **세션**      | 매번 새로운 세션 생성 | 동일한 세션 재사용         |
| **대화**      | 단일 교환        | 동일한 컨텍스트에서 여러 번 교환 |
| **연결**      | 자동으로 관리됨     | 수동 제어              |
| **스트리밍 입력** | ✅ 지원         | ✅ 지원               |
| **중단**      | ❌ 지원하지 않음    | ✅ 지원               |
| **훅**       | ❌ 지원하지 않음    | ✅ 지원               |
| **커스텀 도구**  | ❌ 지원하지 않음    | ✅ 지원               |
| **대화 계속하기** | ❌ 매번 새로운 세션  | ✅ 대화 유지            |
| **사용 사례**   | 일회성 작업       | 지속적인 대화            |

### `query()` 사용 시기 (매번 새로운 세션)

**다음의 경우에 적합합니다:**

- 대화 기록이 필요하지 않은 일회성 질문
- 이전 교환의 컨텍스트가 필요하지 않은 독립적인 작업
- 간단한 자동화 스크립트
- 매번 새로 시작하고 싶은 경우

### `ClaudeSDKClient` 사용 시기 (지속적인 대화)

**다음의 경우에 적합합니다:**

- **대화 계속하기** - Claude가 컨텍스트를 기억해야 하는 경우
- **후속 질문** - 이전 응답을 기반으로 이어나가는 경우
- **상호작용 애플리케이션** - 채팅 인터페이스, REPL
- **응답 기반 로직** - 다음 작업이 Claude의 응답에 의존하는 경우
- **세션 제어** - 대화 생명주기를 명시적으로 관리하는 경우

## 함수

### `query()`

Claude Code와의 각 상호작용마다 새로운 세션을 생성합니다. 메시지가 도착하는 대로 생성하는 비동기 이터레이터를 반환합니다. `query()`를 호출할 때마다 이전 상호작용에 대한 기억 없이 새로 시작합니다.

```python
async def query(
    *,
    prompt: str | AsyncIterable[dict[str, Any]],
    options: ClaudeAgentOptions | None = None
) -> AsyncIterator[Message]
```

#### 매개변수

| 매개변수      | 타입                           | 설명                                                 |
|:----------|:-----------------------------|:---------------------------------------------------|
| `prompt`  | `str \| AsyncIterable[dict]` | 문자열 또는 스트리밍 모드를 위한 비동기 iterable로 된 입력 프롬프트         |
| `options` | `ClaudeAgentOptions \| None` | 선택적 설정 객체 (None인 경우 `ClaudeAgentOptions()`로 기본 설정) |

#### 반환값

대화의 메시지를 생성하는 `AsyncIterator[Message]`를 반환합니다.

#### 예제 - 옵션 사용

```python

import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions

async def main():
    options = ClaudeAgentOptions(
        system_prompt="You are an expert Python developer",
        permission_mode='acceptEdits',
        cwd="/home/user/project"
    )

    async for message in query(
        prompt="Create a Python web server",
        options=options
    ):
        print(message)


asyncio.run(main())
```

### `tool()`

타입 안전성을 갖춘 MCP 도구를 정의하기 위한 데코레이터입니다.

```python
def tool(
    name: str,
    description: str,
    input_schema: type | dict[str, Any]
) -> Callable[[Callable[[Any], Awaitable[dict[str, Any]]]], SdkMcpTool[Any]]
```

#### 매개변수

| 매개변수           | 타입                       | 설명                            |
|:---------------|:-------------------------|:------------------------------|
| `name`         | `str`                    | 도구의 고유 식별자                    |
| `description`  | `str`                    | 도구가 하는 일에 대한 사람이 읽을 수 있는 설명   |
| `input_schema` | `type \| dict[str, Any]` | 도구의 입력 매개변수를 정의하는 스키마 (아래 참조) |

#### 입력 스키마 옵션

1. **간단한 타입 매핑** (권장):

   ```python
   {"text": str, "count": int, "enabled": bool}
   ```

2. **JSON 스키마 형식** (복잡한 유효성 검사를 위해):
   ```python
   {
       "type": "object",
       "properties": {
           "text": {"type": "string"},
           "count": {"type": "integer", "minimum": 0}
       },
       "required": ["text"]
   }
   ```

#### 반환값

도구 구현을 감싸고 `SdkMcpTool` 인스턴스를 반환하는 데코레이터 함수입니다.

#### 예제

```python
from claude_agent_sdk import tool
from typing import Any

@tool("greet", "Greet a user", {"name": str})
async def greet(args: dict[str, Any]) -> dict[str, Any]:
    return {
        "content": [{
            "type": "text",
            "text": f"Hello, {args['name']}!"
        }]
    }
```

### `create_sdk_mcp_server()`

Python 애플리케이션 내에서 실행되는 인프로세스 MCP 서버를 생성합니다.

```python
def create_sdk_mcp_server(
    name: str,
    version: str = "1.0.0",
    tools: list[SdkMcpTool[Any]] | None = None
) -> McpSdkServerConfig
```

#### 매개변수

| 매개변수      | 타입                              | 기본값       | 설명                          |
|:----------|:--------------------------------|:----------|:----------------------------|
| `name`    | `str`                           | -         | 서버의 고유 식별자                  |
| `version` | `str`                           | `"1.0.0"` | 서버 버전 문자열                   |
| `tools`   | `list[SdkMcpTool[Any]] \| None` | `None`    | `@tool` 데코레이터로 생성된 도구 함수 목록 |

#### 반환값

`ClaudeAgentOptions.mcp_servers`에 전달할 수 있는 `McpSdkServerConfig` 객체를 반환합니다.

#### 예제

```python
from claude_agent_sdk import tool, create_sdk_mcp_server

@tool("add", "Add two numbers", {"a": float, "b": float})
async def add(args):
    return {
        "content": [{
            "type": "text",
            "text": f"Sum: {args['a'] + args['b']}"
        }]
    }

@tool("multiply", "Multiply two numbers", {"a": float, "b": float})
async def multiply(args):
    return {
        "content": [{
            "type": "text",
            "text": f"Product: {args['a'] * args['b']}"
        }]
    }

calculator = create_sdk_mcp_server(
    name="calculator",
    version="2.0.0",
    tools=[add, multiply]  # 데코레이팅된 함수 전달
)

# Claude와 함께 사용
options = ClaudeAgentOptions(
    mcp_servers={"calc": calculator},
    allowed_tools=["mcp__calc__add", "mcp__calc__multiply"]
)
```

## 클래스

### `ClaudeSDKClient`

**여러 교환에 걸쳐 대화 세션을 유지합니다.** 이것은 TypeScript SDK의 `query()` 함수가 내부적으로 작동하는 방식과 동일한 Python 버전입니다 - 대화를 계속할 수 있는 클라이언트 객체를
생성합니다.

#### 주요 기능

- **세션 연속성**: 여러 `query()` 호출에 걸쳐 대화 컨텍스트 유지
- **동일한 대화**: Claude가 세션의 이전 메시지를 기억함
- **중단 지원**: Claude 실행 도중 중지 가능
- **명시적 생명주기**: 세션의 시작과 종료를 제어
- **응답 기반 흐름**: 응답에 반응하여 후속 조치 전송 가능
- **커스텀 도구 및 훅**: 커스텀 도구(`@tool` 데코레이터로 생성) 및 훅 지원

```python
class ClaudeSDKClient:
    def __init__(self, options: ClaudeAgentOptions | None = None)
    async def connect(self, prompt: str | AsyncIterable[dict] | None = None) -> None
    async def query(self, prompt: str | AsyncIterable[dict], session_id: str = "default") -> None
    async def receive_messages(self) -> AsyncIterator[Message]
    async def receive_response(self) -> AsyncIterator[Message]
    async def interrupt(self) -> None
    async def rewind_files(self, user_message_uuid: str) -> None
    async def disconnect(self) -> None
```

#### 메서드

| 메서드                               | 설명                                                                                                                                           |
|:----------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------|
| `__init__(options)`               | 선택적 설정으로 클라이언트 초기화                                                                                                                           |
| `connect(prompt)`                 | 선택적 초기 프롬프트 또는 메시지 스트림으로 Claude에 연결                                                                                                          |
| `query(prompt, session_id)`       | 스트리밍 모드로 새 요청 전송                                                                                                                             |
| `receive_messages()`              | Claude의 모든 메시지를 비동기 이터레이터로 수신                                                                                                                |
| `receive_response()`              | ResultMessage까지 포함하여 메시지 수신                                                                                                                  |
| `interrupt()`                     | 중단 신호 전송 (스트리밍 모드에서만 작동)                                                                                                                     |
| `rewind_files(user_message_uuid)` | 지정된 사용자 메시지의 상태로 파일 복원. `enable_file_checkpointing=True` 필요. [파일 체크포인팅](https://platform.claude.com/docs/en/agent-sdk/file-checkpointing) 참조 |
| `disconnect()`                    | Claude 연결 해제                                                                                                                                 |

#### 컨텍스트 매니저 지원

클라이언트는 자동 연결 관리를 위한 비동기 컨텍스트 매니저로 사용할 수 있습니다:

```python
async with ClaudeSDKClient() as client:
    await client.query("Hello Claude")
    async for message in client.receive_response():
        print(message)
```

> **중요:** 메시지를 반복할 때 `break`를 사용하여 조기에 종료하지 마세요. asyncio 정리 문제가 발생할 수 있습니다. 대신 반복이 자연스럽게 완료되도록 하거나 플래그를 사용하여 필요한 것을 찾았을
> 때를 추적하세요.

#### 예제 - 대화 계속하기

```python
import asyncio
from claude_agent_sdk import ClaudeSDKClient, AssistantMessage, TextBlock, ResultMessage

async def main():
    async with ClaudeSDKClient() as client:
        # 첫 번째 질문
        await client.query("What's the capital of France?")

        # 응답 처리
        async for message in client.receive_response():
            if isinstance(message, AssistantMessage):
                for block in message.content:
                    if isinstance(block, TextBlock):
                        print(f"Claude: {block.text}")

        # 후속 질문 - Claude가 이전 컨텍스트를 기억함
        await client.query("What's the population of that city?")

        async for message in client.receive_response():
            if isinstance(message, AssistantMessage):
                for block in message.content:
                    if isinstance(block, TextBlock):
                        print(f"Claude: {block.text}")

        # 또 다른 후속 질문 - 여전히 동일한 대화
        await client.query("What are some famous landmarks there?")

        async for message in client.receive_response():
            if isinstance(message, AssistantMessage):
                for block in message.content:
                    if isinstance(block, TextBlock):
                        print(f"Claude: {block.text}")

asyncio.run(main())
```

#### 예제 - ClaudeSDKClient로 입력 스트리밍

```python
import asyncio
from claude_agent_sdk import ClaudeSDKClient

async def message_stream():
    """동적으로 메시지 생성."""
    yield {"type": "text", "text": "Analyze the following data:"}
    await asyncio.sleep(0.5)
    yield {"type": "text", "text": "Temperature: 25°C"}
    await asyncio.sleep(0.5)
    yield {"type": "text", "text": "Humidity: 60%"}
    await asyncio.sleep(0.5)
    yield {"type": "text", "text": "What patterns do you see?"}

async def main():
    async with ClaudeSDKClient() as client:
        # Claude로 입력 스트리밍
        await client.query(message_stream())

        # 응답 처리
        async for message in client.receive_response():
            print(message)

        # 동일한 세션에서 후속 질문
        await client.query("Should we be concerned about these readings?")

        async for message in client.receive_response():
            print(message)

asyncio.run(main())
```

#### 예제 - 중단 사용

```python
import asyncio
from claude_agent_sdk import ClaudeSDKClient, ClaudeAgentOptions

async def interruptible_task():
    options = ClaudeAgentOptions(
        allowed_tools=["Bash"],
        permission_mode="acceptEdits"
    )

    async with ClaudeSDKClient(options=options) as client:
        # 장기 실행 작업 시작
        await client.query("Count from 1 to 100 slowly")

        # 잠시 실행하도록 허용
        await asyncio.sleep(2)

        # 작업 중단
        await client.interrupt()
        print("Task interrupted!")

        # 새로운 명령 전송
        await client.query("Just say hello instead")

        async for message in client.receive_response():
            # 새로운 응답 처리
            pass

asyncio.run(interruptible_task())
```

#### 예제 - 고급 권한 제어

```python
from claude_agent_sdk import (
    ClaudeSDKClient,
    ClaudeAgentOptions
)
from claude_agent_sdk.types import PermissionResultAllow, PermissionResultDeny

async def custom_permission_handler(
    tool_name: str,
    input_data: dict,
    context: dict
) -> PermissionResultAllow | PermissionResultDeny:
    """도구 권한에 대한 커스텀 로직."""

    # 시스템 디렉토리 쓰기 차단
    if tool_name == "Write" and input_data.get("file_path", "").startswith("/system/"):
        return PermissionResultDeny(
            message="System directory write not allowed",
            interrupt=True
        )

    # 민감한 파일 작업 리디렉션
    if tool_name in ["Write", "Edit"] and "config" in input_data.get("file_path", ""):
        safe_path = f"./sandbox/{input_data['file_path']}"
        return PermissionResultAllow(
            updated_input={**input_data, "file_path": safe_path}
        )

    # 나머지는 모두 허용
    return PermissionResultAllow(updated_input=input_data)

async def main():
    options = ClaudeAgentOptions(
        can_use_tool=custom_permission_handler,
        allowed_tools=["Read", "Write", "Edit"]
    )

    async with ClaudeSDKClient(options=options) as client:
        await client.query("Update the system config file")

        async for message in client.receive_response():
            # 대신 샌드박스 경로를 사용함
            print(message)

asyncio.run(main())
```

## 타입

### `SdkMcpTool`

`@tool` 데코레이터로 생성된 SDK MCP 도구의 정의입니다.

```python
@dataclass
class SdkMcpTool(Generic[T]):
    name: str
    description: str
    input_schema: type[T] | dict[str, Any]
    handler: Callable[[T], Awaitable[dict[str, Any]]]
```

| 속성             | 타입                                         | 설명                 |
|:---------------|:-------------------------------------------|:-------------------|
| `name`         | `str`                                      | 도구의 고유 식별자         |
| `description`  | `str`                                      | 사람이 읽을 수 있는 설명     |
| `input_schema` | `type[T] \| dict[str, Any]`                | 입력 유효성 검사를 위한 스키마  |
| `handler`      | `Callable[[T], Awaitable[dict[str, Any]]]` | 도구 실행을 처리하는 비동기 함수 |

### `ClaudeAgentOptions`

Claude Code 쿼리를 위한 설정 데이터클래스입니다.

```python
@dataclass
class ClaudeAgentOptions:
    tools: list[str] | ToolsPreset | None = None
    allowed_tools: list[str] = field(default_factory=list)
    system_prompt: str | SystemPromptPreset | None = None
    mcp_servers: dict[str, McpServerConfig] | str | Path = field(default_factory=dict)
    permission_mode: PermissionMode | None = None
    continue_conversation: bool = False
    resume: str | None = None
    max_turns: int | None = None
    max_budget_usd: float | None = None
    disallowed_tools: list[str] = field(default_factory=list)
    model: str | None = None
    fallback_model: str | None = None
    betas: list[SdkBeta] = field(default_factory=list)
    output_format: OutputFormat | None = None
    permission_prompt_tool_name: str | None = None
    cwd: str | Path | None = None
    cli_path: str | Path | None = None
    settings: str | None = None
    add_dirs: list[str | Path] = field(default_factory=list)
    env: dict[str, str] = field(default_factory=dict)
    extra_args: dict[str, str | None] = field(default_factory=dict)
    max_buffer_size: int | None = None
    debug_stderr: Any = sys.stderr  # Deprecated
    stderr: Callable[[str], None] | None = None
    can_use_tool: CanUseTool | None = None
    hooks: dict[HookEvent, list[HookMatcher]] | None = None
    user: str | None = None
    include_partial_messages: bool = False
    fork_session: bool = False
    agents: dict[str, AgentDefinition] | None = None
    setting_sources: list[SettingSource] | None = None
    max_thinking_tokens: int | None = None
```

| 속성                            | 타입                                               | 기본값            | 설명                                                                                                                                            |
|:------------------------------|:-------------------------------------------------|:---------------|:----------------------------------------------------------------------------------------------------------------------------------------------|
| `tools`                       | `list[str] \| ToolsPreset \| None`               | `None`         | 도구 설정. Claude Code의 기본 도구를 사용하려면 `{"type": "preset", "preset": "claude_code"}` 사용                                                             |
| `allowed_tools`               | `list[str]`                                      | `[]`           | 허용된 도구 이름 목록                                                                                                                                  |
| `system_prompt`               | `str \| SystemPromptPreset \| None`              | `None`         | 시스템 프롬프트 설정. 커스텀 프롬프트의 경우 문자열을 전달하거나, Claude Code의 시스템 프롬프트를 사용하려면 `{"type": "preset", "preset": "claude_code"}` 사용. 프리셋을 확장하려면 `"append"` 추가 |
| `mcp_servers`                 | `dict[str, McpServerConfig] \| str \| Path`      | `{}`           | MCP 서버 설정 또는 설정 파일 경로                                                                                                                         |
| `permission_mode`             | `PermissionMode \| None`                         | `None`         | 도구 사용에 대한 권한 모드                                                                                                                               |
| `continue_conversation`       | `bool`                                           | `False`        | 가장 최근 대화 계속하기                                                                                                                                 |
| `resume`                      | `str \| None`                                    | `None`         | 재개할 세션 ID                                                                                                                                     |
| `max_turns`                   | `int \| None`                                    | `None`         | 최대 대화 턴                                                                                                                                       |
| `max_budget_usd`              | `float \| None`                                  | `None`         | 세션의 최대 예산(USD)                                                                                                                                |
| `disallowed_tools`            | `list[str]`                                      | `[]`           | 허용되지 않는 도구 이름 목록                                                                                                                              |
| `enable_file_checkpointing`   | `bool`                                           | `False`        | 되감기를 위한 파일 변경 추적 활성화. [파일 체크포인팅](https://platform.claude.com/docs/en/agent-sdk/file-checkpointing) 참조                                         |
| `model`                       | `str \| None`                                    | `None`         | 사용할 Claude 모델                                                                                                                                 |
| `fallback_model`              | `str \| None`                                    | `None`         | 기본 모델이 실패할 경우 사용할 대체 모델                                                                                                                       |
| `betas`                       | `list[SdkBeta]`                                  | `[]`           | 활성화할 베타 기능. 사용 가능한 옵션은 [`SdkBeta`](#sdkbeta) 참조                                                                                               |
| `output_format`               | [`OutputFormat`](#outputformat) ` \| None`       | `None`         | 에이전트 결과의 출력 형식 정의. 자세한 내용은 [구조화된 출력](https://platform.claude.com/docs/en/agent-sdk/structured-outputs) 참조                                     |
| `permission_prompt_tool_name` | `str \| None`                                    | `None`         | 권한 프롬프트를 위한 MCP 도구 이름                                                                                                                         |
| `cwd`                         | `str \| Path \| None`                            | `None`         | 현재 작업 디렉토리                                                                                                                                    |
| `cli_path`                    | `str \| Path \| None`                            | `None`         | Claude Code CLI 실행 파일의 커스텀 경로                                                                                                                 |
| `settings`                    | `str \| None`                                    | `None`         | 설정 파일 경로                                                                                                                                      |
| `add_dirs`                    | `list[str \| Path]`                              | `[]`           | Claude가 액세스할 수 있는 추가 디렉토리                                                                                                                     |
| `env`                         | `dict[str, str]`                                 | `{}`           | 환경 변수                                                                                                                                         |
| `extra_args`                  | `dict[str, str \| None]`                         | `{}`           | CLI에 직접 전달할 추가 CLI 인수                                                                                                                         |
| `max_buffer_size`             | `int \| None`                                    | `None`         | CLI stdout를 버퍼링할 때의 최대 바이트                                                                                                                    |
| `debug_stderr`                | `Any`                                            | `sys.stderr`   | _사용 중단_ - 디버그 출력을 위한 파일형 객체. 대신 `stderr` 콜백 사용                                                                                                |
| `stderr`                      | `Callable[[str], None] \| None`                  | `None`         | CLI의 stderr 출력을 위한 콜백 함수                                                                                                                      |
| `can_use_tool`                | [`CanUseTool`](#canusertool) ` \| None`          | `None`         | 도구 권한 콜백 함수. 자세한 내용은 [권한 타입](#canusertool) 참조                                                                                                 |
| `hooks`                       | `dict[HookEvent, list[HookMatcher]] \| None`     | `None`         | 이벤트 가로채기를 위한 훅 설정                                                                                                                             |
| `user`                        | `str \| None`                                    | `None`         | 사용자 식별자                                                                                                                                       |
| `include_partial_messages`    | `bool`                                           | `False`        | 부분 메시지 스트리밍 이벤트 포함. 활성화되면 [`StreamEvent`](#streamevent) 메시지가 생성됨                                                                              |
| `fork_session`                | `bool`                                           | `False`        | `resume`으로 재개할 때, 원래 세션을 계속하는 대신 새 세션 ID로 분기                                                                                                  |
| `agents`                      | `dict[str, AgentDefinition] \| None`             | `None`         | 프로그래밍 방식으로 정의된 서브에이전트                                                                                                                         |
| `plugins`                     | `list[SdkPluginConfig]`                          | `[]`           | 로컬 경로에서 커스텀 플러그인 로드. 자세한 내용은 [플러그인](https://platform.claude.com/docs/en/agent-sdk/plugins) 참조                                                 |
| `sandbox`                     | [`SandboxSettings`](#sandboxsettings) ` \| None` | `None`         | 프로그래밍 방식으로 샌드박스 동작 구성. 자세한 내용은 [샌드박스 설정](#sandboxsettings) 참조                                                                                 |
| `setting_sources`             | `list[SettingSource] \| None`                    | `None` (설정 없음) | 로드할 파일 시스템 설정 제어. 생략하면 설정이 로드되지 않음. **참고:** CLAUDE.md 파일을 로드하려면 `"project"` 포함 필요                                                             |
| `max_thinking_tokens`         | `int \| None`                                    | `None`         | 사고 블록의 최대 토큰                                                                                                                                  |

### `OutputFormat`

구조화된 출력 유효성 검사를 위한 설정입니다.

```python
class OutputFormat(TypedDict):
    type: Literal["json_schema"]
    schema: dict[str, Any]
```

| 필드       | 필수 여부 | 설명                                  |
|:---------|:------|:------------------------------------|
| `type`   | 예     | JSON 스키마 유효성 검사를 위해 `"json_schema"` |
| `schema` | 예     | 출력 유효성 검사를 위한 JSON 스키마 정의           |

### `SystemPromptPreset`

선택적 추가 사항과 함께 Claude Code의 프리셋 시스템 프롬프트를 사용하기 위한 설정입니다.

```python
class SystemPromptPreset(TypedDict):
    type: Literal["preset"]
    preset: Literal["claude_code"]
    append: NotRequired[str]
```

| 필드       | 필수 여부 | 설명                                           |
|:---------|:------|:---------------------------------------------|
| `type`   | 예     | 프리셋 시스템 프롬프트를 사용하려면 `"preset"`               |
| `preset` | 예     | Claude Code의 시스템 프롬프트를 사용하려면 `"claude_code"` |
| `append` | 아니오   | 프리셋 시스템 프롬프트에 추가할 추가 지침                      |

### `SettingSource`

SDK가 설정을 로드하는 파일 시스템 기반 설정 소스를 제어합니다.

```python
SettingSource = Literal["user", "project", "local"]
```

| 값           | 설명                     | 위치                            |
|:------------|:-----------------------|:------------------------------|
| `"user"`    | 전역 사용자 설정              | `~/.claude/settings.json`     |
| `"project"` | 공유 프로젝트 설정 (버전 제어됨)    | `.claude/settings.json`       |
| `"local"`   | 로컬 프로젝트 설정 (git에서 무시됨) | `.claude/settings.local.json` |

#### 기본 동작

`setting_sources`가 **생략**되거나 **`None`**인 경우, SDK는 파일 시스템 설정을 로드하지 **않습니다**. 이는 SDK 애플리케이션에 격리를 제공합니다.

#### setting_sources를 사용하는 이유는?

**모든 파일 시스템 설정 로드 (레거시 동작):**

```python
# SDK v0.0.x처럼 모든 설정 로드
from claude_agent_sdk import query, ClaudeAgentOptions

async for message in query(
    prompt="Analyze this code",
    options=ClaudeAgentOptions(
        setting_sources=["user", "project", "local"]  # 모든 설정 로드
    )
):
    print(message)
```

**특정 설정 소스만 로드:**

```python
# 프로젝트 설정만 로드, 사용자 및 로컬 무시
async for message in query(
    prompt="Run CI checks",
    options=ClaudeAgentOptions(
        setting_sources=["project"]  # .claude/settings.json만
    )
):
    print(message)
```

**테스트 및 CI 환경:**

```python
# 로컬 설정을 제외하여 CI에서 일관된 동작 보장
async for message in query(
    prompt="Run tests",
    options=ClaudeAgentOptions(
        setting_sources=["project"],  # 팀 공유 설정만
        permission_mode="bypassPermissions"
    )
):
    print(message)
```

**SDK 전용 애플리케이션:**

```python
# 모든 것을 프로그래밍 방식으로 정의 (기본 동작)
# 파일 시스템 의존성 없음 - setting_sources는 기본적으로 None
async for message in query(
    prompt="Review this PR",
    options=ClaudeAgentOptions(
        # setting_sources=None이 기본값이므로 지정할 필요 없음
        agents={ /* ... */ },
        mcp_servers={ /* ... */ },
        allowed_tools=["Read", "Grep", "Glob"]
    )
):
    print(message)
```

**CLAUDE.md 프로젝트 지침 로드:**

```python
# CLAUDE.md 파일을 포함하려면 프로젝트 설정 로드
async for message in query(
    prompt="Add a new feature following project conventions",
    options=ClaudeAgentOptions(
        system_prompt={
            "type": "preset",
            "preset": "claude_code"  # Claude Code의 시스템 프롬프트 사용
        },
        setting_sources=["project"],  # 프로젝트의 CLAUDE.md를 로드하는 데 필요
        allowed_tools=["Read", "Write", "Edit"]
    )
):
    print(message)
```

#### 설정 우선순위

여러 소스가 로드될 때, 설정은 다음 우선순위로 병합됩니다 (높은 것부터 낮은 것까지):

1. 로컬 설정 (`.claude/settings.local.json`)
2. 프로젝트 설정 (`.claude/settings.json`)
3. 사용자 설정 (`~/.claude/settings.json`)

프로그래밍 방식 옵션(`agents`, `allowed_tools` 등)은 항상 파일 시스템 설정을 재정의합니다.

### `AgentDefinition`

프로그래밍 방식으로 정의된 서브에이전트의 설정입니다.

```python
@dataclass
class AgentDefinition:
    description: str
    prompt: str
    tools: list[str] | None = None
    model: Literal["sonnet", "opus", "haiku", "inherit"] | None = None
```

| 필드            | 필수 여부 | 설명                            |
|:--------------|:------|:------------------------------|
| `description` | 예     | 이 에이전트를 사용할 시기에 대한 자연어 설명     |
| `tools`       | 아니오   | 허용된 도구 이름 배열. 생략하면 모든 도구를 상속  |
| `prompt`      | 예     | 에이전트의 시스템 프롬프트                |
| `model`       | 아니오   | 이 에이전트의 모델 재정의. 생략하면 메인 모델 사용 |

### `PermissionMode`

도구 실행을 제어하기 위한 권한 모드입니다.

```python
PermissionMode = Literal[
    "default",           # 표준 권한 동작
    "acceptEdits",       # 파일 편집 자동 수락
    "plan",              # 계획 모드 - 실행 없음
    "bypassPermissions"  # 모든 권한 검사 우회 (주의하여 사용)
]
```

### `CanUseTool`

도구 권한 콜백 함수의 타입 별칭입니다.

```python
CanUseTool = Callable[
    [str, dict[str, Any], ToolPermissionContext],
    Awaitable[PermissionResult]
]
```

콜백은 다음을 받습니다:

- `tool_name`: 호출되는 도구의 이름
- `input_data`: 도구의 입력 매개변수
- `context`: 추가 정보가 있는 `ToolPermissionContext`

`PermissionResult`(`PermissionResultAllow` 또는 `PermissionResultDeny`)를 반환합니다.

### `ToolPermissionContext`

도구 권한 콜백에 전달되는 컨텍스트 정보입니다.

```python
@dataclass
class ToolPermissionContext:
    signal: Any | None = None  # 향후: 중단 신호 지원
    suggestions: list[PermissionUpdate] = field(default_factory=list)
```

| 필드            | 타입                       | 설명                  |
|:--------------|:-------------------------|:--------------------|
| `signal`      | `Any \| None`            | 향후 중단 신호 지원을 위해 예약됨 |
| `suggestions` | `list[PermissionUpdate]` | CLI의 권한 업데이트 제안     |

### `PermissionResult`

권한 콜백 결과의 유니온 타입입니다.

```python
PermissionResult = PermissionResultAllow | PermissionResultDeny
```

### `PermissionResultAllow`

도구 호출을 허용해야 함을 나타내는 결과입니다.

```python
@dataclass
class PermissionResultAllow:
    behavior: Literal["allow"] = "allow"
    updated_input: dict[str, Any] | None = None
    updated_permissions: list[PermissionUpdate] | None = None
```

| 필드                    | 타입                               | 기본값       | 설명               |
|:----------------------|:---------------------------------|:----------|:-----------------|
| `behavior`            | `Literal["allow"]`               | `"allow"` | "allow"여야 함      |
| `updated_input`       | `dict[str, Any] \| None`         | `None`    | 원본 대신 사용할 수정된 입력 |
| `updated_permissions` | `list[PermissionUpdate] \| None` | `None`    | 적용할 권한 업데이트      |

### `PermissionResultDeny`

도구 호출을 거부해야 함을 나타내는 결과입니다.

```python
@dataclass
class PermissionResultDeny:
    behavior: Literal["deny"] = "deny"
    message: str = ""
    interrupt: bool = False
```

| 필드          | 타입                | 기본값      | 설명                   |
|:------------|:------------------|:---------|:---------------------|
| `behavior`  | `Literal["deny"]` | `"deny"` | "deny"여야 함           |
| `message`   | `str`             | `""`     | 도구가 거부된 이유를 설명하는 메시지 |
| `interrupt` | `bool`            | `False`  | 현재 실행을 중단할지 여부       |

### `PermissionUpdate`

프로그래밍 방식으로 권한을 업데이트하기 위한 설정입니다.

```python
@dataclass
class PermissionUpdate:
    type: Literal[
        "addRules",
        "replaceRules",
        "removeRules",
        "setMode",
        "addDirectories",
        "removeDirectories",
    ]
    rules: list[PermissionRuleValue] | None = None
    behavior: Literal["allow", "deny", "ask"] | None = None
    mode: PermissionMode | None = None
    directories: list[str] | None = None
    destination: Literal["userSettings", "projectSettings", "localSettings", "session"] | None = None
```

| 필드            | 타입                                        | 설명                           |
|:--------------|:------------------------------------------|:-----------------------------|
| `type`        | `Literal[...]`                            | 권한 업데이트 작업의 타입               |
| `rules`       | `list[PermissionRuleValue] \| None`       | add/replace/remove 작업을 위한 규칙 |
| `behavior`    | `Literal["allow", "deny", "ask"] \| None` | 규칙 기반 작업을 위한 동작              |
| `mode`        | `PermissionMode \| None`                  | setMode 작업을 위한 모드            |
| `directories` | `list[str] \| None`                       | add/remove 디렉토리 작업을 위한 디렉토리  |
| `destination` | `Literal[...] \| None`                    | 권한 업데이트를 적용할 위치              |

### `SdkBeta`

SDK 베타 기능을 위한 리터럴 타입입니다.

```python
SdkBeta = Literal["context-1m-2025-08-07"]
```

베타 기능을 활성화하려면 `ClaudeAgentOptions`의 `betas` 필드와 함께 사용합니다.

### `McpSdkServerConfig`

`create_sdk_mcp_server()`로 생성된 SDK MCP 서버의 설정입니다.

```python
class McpSdkServerConfig(TypedDict):
    type: Literal["sdk"]
    name: str
    instance: Any  # MCP 서버 인스턴스
```

### `McpServerConfig`

MCP 서버 설정의 유니온 타입입니다.

```python
McpServerConfig = McpStdioServerConfig | McpSSEServerConfig | McpHttpServerConfig | McpSdkServerConfig
```

#### `McpStdioServerConfig`

```python
class McpStdioServerConfig(TypedDict):
    type: NotRequired[Literal["stdio"]]  # 하위 호환성을 위해 선택 사항
    command: str
    args: NotRequired[list[str]]
    env: NotRequired[dict[str, str]]
```

#### `McpSSEServerConfig`

```python
class McpSSEServerConfig(TypedDict):
    type: Literal["sse"]
    url: str
    headers: NotRequired[dict[str, str]]
```

#### `McpHttpServerConfig`

```python
class McpHttpServerConfig(TypedDict):
    type: Literal["http"]
    url: str
    headers: NotRequired[dict[str, str]]
```

### `SdkPluginConfig`

SDK에서 플러그인을 로드하기 위한 설정입니다.

```python
class SdkPluginConfig(TypedDict):
    type: Literal["local"]
    path: str
```

| 필드     | 타입                 | 설명                               |
|:-------|:-------------------|:---------------------------------|
| `type` | `Literal["local"]` | `"local"`이어야 함 (현재 로컬 플러그인만 지원됨) |
| `path` | `str`              | 플러그인 디렉토리의 절대 또는 상대 경로           |

**예제:**

```python
plugins=[
    {"type": "local", "path": "./my-plugin"},
    {"type": "local", "path": "/absolute/path/to/plugin"}
]
```

플러그인 생성 및 사용에 대한 전체 정보는 [플러그인](https://platform.claude.com/docs/en/agent-sdk/plugins)을 참조하세요.

## 메시지 타입

### `Message`

모든 가능한 메시지의 유니온 타입입니다.

```python
Message = UserMessage | AssistantMessage | SystemMessage | ResultMessage | StreamEvent
```

### `UserMessage`

사용자 입력 메시지입니다.

```python
@dataclass
class UserMessage:
    content: str | list[ContentBlock]
```

### `AssistantMessage`

콘텐츠 블록이 있는 어시스턴트 응답 메시지입니다.

```python
@dataclass
class AssistantMessage:
    content: list[ContentBlock]
    model: str
```

### `SystemMessage`

메타데이터가 있는 시스템 메시지입니다.

```python
@dataclass
class SystemMessage:
    subtype: str
    data: dict[str, Any]
```

### `ResultMessage`

비용 및 사용량 정보가 있는 최종 결과 메시지입니다.

```python
@dataclass
class ResultMessage:
    subtype: str
    duration_ms: int
    duration_api_ms: int
    is_error: bool
    num_turns: int
    session_id: str
    total_cost_usd: float | None = None
    usage: dict[str, Any] | None = None
    result: str | None = None
    structured_output: Any = None
```

### `StreamEvent`

스트리밍 중 부분 메시지 업데이트를 위한 스트림 이벤트입니다. `ClaudeAgentOptions`에서 `include_partial_messages=True`인 경우에만 수신됩니다.

```python
@dataclass
class StreamEvent:
    uuid: str
    session_id: str
    event: dict[str, Any]  # 원시 Anthropic API 스트림 이벤트
    parent_tool_use_id: str | None = None
```

| 필드                   | 타입               | 설명                               |
|:---------------------|:-----------------|:---------------------------------|
| `uuid`               | `str`            | 이 이벤트의 고유 식별자                    |
| `session_id`         | `str`            | 세션 식별자                           |
| `event`              | `dict[str, Any]` | 원시 Anthropic API 스트림 이벤트 데이터     |
| `parent_tool_use_id` | `str \| None`    | 이 이벤트가 서브에이전트에서 온 경우 부모 도구 사용 ID |

## 콘텐츠 블록 타입

### `ContentBlock`

모든 콘텐츠 블록의 유니온 타입입니다.

```python
ContentBlock = TextBlock | ThinkingBlock | ToolUseBlock | ToolResultBlock
```

### `TextBlock`

텍스트 콘텐츠 블록입니다.

```python
@dataclass
class TextBlock:
    text: str
```

### `ThinkingBlock`

사고 콘텐츠 블록입니다 (사고 기능이 있는 모델용).

```python
@dataclass
class ThinkingBlock:
    thinking: str
    signature: str
```

### `ToolUseBlock`

도구 사용 요청 블록입니다.

```python
@dataclass
class ToolUseBlock:
    id: str
    name: str
    input: dict[str, Any]
```

### `ToolResultBlock`

도구 실행 결과 블록입니다.

```python
@dataclass
class ToolResultBlock:
    tool_use_id: str
    content: str | list[dict[str, Any]] | None = None
    is_error: bool | None = None
```

## 오류 타입

### `ClaudeSDKError`

모든 SDK 오류의 기본 예외 클래스입니다.

```python
class ClaudeSDKError(Exception):
    """Claude SDK의 기본 오류."""
```

### `CLINotFoundError`

Claude Code CLI가 설치되지 않았거나 찾을 수 없을 때 발생합니다.

```python
class CLINotFoundError(CLIConnectionError):
    def __init__(self, message: str = "Claude Code not found", cli_path: str | None = None):
        """
        Args:
            message: 오류 메시지 (기본값: "Claude Code not found")
            cli_path: 찾을 수 없었던 CLI의 선택적 경로
        """
```

### `CLIConnectionError`

Claude Code 연결이 실패할 때 발생합니다.

```python
class CLIConnectionError(ClaudeSDKError):
    """Claude Code 연결 실패."""
```

### `ProcessError`

Claude Code 프로세스가 실패할 때 발생합니다.

```python
class ProcessError(ClaudeSDKError):
    def __init__(self, message: str, exit_code: int | None = None, stderr: str | None = None):
        self.exit_code = exit_code
        self.stderr = stderr
```

### `CLIJSONDecodeError`

JSON 파싱이 실패할 때 발생합니다.

```python
class CLIJSONDecodeError(ClaudeSDKError):
    def __init__(self, line: str, original_error: Exception):
        """
        Args:
            line: 파싱에 실패한 줄
            original_error: 원래 JSON 디코드 예외
        """
        self.line = line
        self.original_error = original_error
```

## 훅 타입

예제와 일반적인 패턴이 포함된 훅 사용에 대한 포괄적인 가이드는 [훅 가이드](https://platform.claude.com/docs/en/agent-sdk/hooks)를 참조하세요.

### `HookEvent`

지원되는 훅 이벤트 타입입니다. 설정 제한으로 인해 Python SDK는 SessionStart, SessionEnd, Notification 훅을 지원하지 않습니다.

```python
HookEvent = Literal[
    "PreToolUse",      # 도구 실행 전 호출
    "PostToolUse",     # 도구 실행 후 호출
    "UserPromptSubmit",# 사용자가 프롬프트를 제출할 때 호출
    "Stop",            # 실행을 중지할 때 호출
    "SubagentStop",    # 서브에이전트가 중지할 때 호출
    "PreCompact"       # 메시지 압축 전 호출
]
```

### `HookCallback`

훅 콜백 함수의 타입 정의입니다.

```python
HookCallback = Callable[
    [dict[str, Any], str | None, HookContext],
    Awaitable[dict[str, Any]]
]
```

매개변수:

- `input_data`: 훅 특정 입력 데이터 ([훅 가이드](https://platform.claude.com/docs/en/agent-sdk/hooks#input-data) 참조)
- `tool_use_id`: 선택적 도구 사용 식별자 (도구 관련 훅용)
- `context`: 추가 정보가 있는 훅 컨텍스트

다음을 포함할 수 있는 딕셔너리를 반환합니다:

- `decision`: 작업을 차단하려면 `"block"`
- `systemMessage`: 사용자에게 추가할 시스템 메시지
- `hookSpecificOutput`: 훅 특정 출력 데이터

### `HookContext`

훅 콜백에 전달되는 컨텍스트 정보입니다.

```python
@dataclass
class HookContext:
    signal: Any | None = None  # 향후: 중단 신호 지원
```

### `HookMatcher`

특정 이벤트 또는 도구에 훅을 매칭하기 위한 설정입니다.

```python
@dataclass
class HookMatcher:
    matcher: str | None = None        # 매칭할 도구 이름 또는 패턴 (예: "Bash", "Write|Edit")
    hooks: list[HookCallback] = field(default_factory=list)  # 실행할 콜백 목록
    timeout: float | None = None        # 이 매처의 모든 훅에 대한 타임아웃(초) (기본값: 60)
```

### `HookInput`

모든 훅 입력 타입의 유니온 타입입니다. 실제 타입은 `hook_event_name` 필드에 따라 다릅니다.

```python
HookInput = (
    PreToolUseHookInput
    | PostToolUseHookInput
    | UserPromptSubmitHookInput
    | StopHookInput
    | SubagentStopHookInput
    | PreCompactHookInput
)
```

### `BaseHookInput`

모든 훅 입력 타입에 존재하는 기본 필드입니다.

```python
class BaseHookInput(TypedDict):
    session_id: str
    transcript_path: str
    cwd: str
    permission_mode: NotRequired[str]
```

| 필드                | 타입            | 설명              |
|:------------------|:--------------|:----------------|
| `session_id`      | `str`         | 현재 세션 식별자       |
| `transcript_path` | `str`         | 세션 트랜스크립트 파일 경로 |
| `cwd`             | `str`         | 현재 작업 디렉토리      |
| `permission_mode` | `str` (선택 사항) | 현재 권한 모드        |

### `PreToolUseHookInput`

`PreToolUse` 훅 이벤트의 입력 데이터입니다.

```python
class PreToolUseHookInput(BaseHookInput):
    hook_event_name: Literal["PreToolUse"]
    tool_name: str
    tool_input: dict[str, Any]
```

| 필드                | 타입                      | 설명              |
|:------------------|:------------------------|:----------------|
| `hook_event_name` | `Literal["PreToolUse"]` | 항상 "PreToolUse" |
| `tool_name`       | `str`                   | 실행될 도구의 이름      |
| `tool_input`      | `dict[str, Any]`        | 도구의 입력 매개변수     |

### `PostToolUseHookInput`

`PostToolUse` 훅 이벤트의 입력 데이터입니다.

```python
class PostToolUseHookInput(BaseHookInput):
    hook_event_name: Literal["PostToolUse"]
    tool_name: str
    tool_input: dict[str, Any]
    tool_response: Any
```

| 필드                | 타입                       | 설명               |
|:------------------|:-------------------------|:-----------------|
| `hook_event_name` | `Literal["PostToolUse"]` | 항상 "PostToolUse" |
| `tool_name`       | `str`                    | 실행된 도구의 이름       |
| `tool_input`      | `dict[str, Any]`         | 사용된 입력 매개변수      |
| `tool_response`   | `Any`                    | 도구 실행의 응답        |

### `UserPromptSubmitHookInput`

`UserPromptSubmit` 훅 이벤트의 입력 데이터입니다.

```python
class UserPromptSubmitHookInput(BaseHookInput):
    hook_event_name: Literal["UserPromptSubmit"]
    prompt: str
```

| 필드                | 타입                            | 설명                    |
|:------------------|:------------------------------|:----------------------|
| `hook_event_name` | `Literal["UserPromptSubmit"]` | 항상 "UserPromptSubmit" |
| `prompt`          | `str`                         | 사용자가 제출한 프롬프트         |

### `StopHookInput`

`Stop` 훅 이벤트의 입력 데이터입니다.

```python
class StopHookInput(BaseHookInput):
    hook_event_name: Literal["Stop"]
    stop_hook_active: bool
```

| 필드                 | 타입                | 설명            |
|:-------------------|:------------------|:--------------|
| `hook_event_name`  | `Literal["Stop"]` | 항상 "Stop"     |
| `stop_hook_active` | `bool`            | 중지 훅이 활성 상태인지 |

### `SubagentStopHookInput`

`SubagentStop` 훅 이벤트의 입력 데이터입니다.

```python
class SubagentStopHookInput(BaseHookInput):
    hook_event_name: Literal["SubagentStop"]
    stop_hook_active: bool
```

| 필드                 | 타입                        | 설명                |
|:-------------------|:--------------------------|:------------------|
| `hook_event_name`  | `Literal["SubagentStop"]` | 항상 "SubagentStop" |
| `stop_hook_active` | `bool`                    | 중지 훅이 활성 상태인지     |

### `PreCompactHookInput`

`PreCompact` 훅 이벤트의 입력 데이터입니다.

```python
class PreCompactHookInput(BaseHookInput):
    hook_event_name: Literal["PreCompact"]
    trigger: Literal["manual", "auto"]
    custom_instructions: str | None
```

| 필드                    | 타입                          | 설명              |
|:----------------------|:----------------------------|:----------------|
| `hook_event_name`     | `Literal["PreCompact"]`     | 항상 "PreCompact" |
| `trigger`             | `Literal["manual", "auto"]` | 압축을 트리거한 것      |
| `custom_instructions` | `str \| None`               | 압축을 위한 커스텀 지침   |

### `HookJSONOutput`

훅 콜백 반환 값의 유니온 타입입니다.

```python
HookJSONOutput = AsyncHookJSONOutput | SyncHookJSONOutput
```

#### `SyncHookJSONOutput`

제어 및 결정 필드가 있는 동기 훅 출력입니다.

```python
class SyncHookJSONOutput(TypedDict):
    # 제어 필드
    continue_: NotRequired[bool]      # 계속할지 여부 (기본값: True)
    suppressOutput: NotRequired[bool] # 트랜스크립트에서 stdout 숨김
    stopReason: NotRequired[str]      # continue가 False일 때의 메시지

    # 결정 필드
    decision: NotRequired[Literal["block"]]
    systemMessage: NotRequired[str]   # 사용자를 위한 경고 메시지
    reason: NotRequired[str]          # Claude를 위한 피드백

    # 훅 특정 출력
    hookSpecificOutput: NotRequired[dict[str, Any]]
```

> Python 코드에서 `continue_`(언더스코어 포함)를 사용하세요. CLI에 전송될 때 자동으로 `continue`로 변환됩니다.

#### `AsyncHookJSONOutput`

훅 실행을 연기하는 비동기 훅 출력입니다.

```python
class AsyncHookJSONOutput(TypedDict):
    async_: Literal[True]             # 실행을 연기하려면 True로 설정
    asyncTimeout: NotRequired[int]    # 밀리초 단위 타임아웃
```

> Python 코드에서 `async_`(언더스코어 포함)를 사용하세요. CLI에 전송될 때 자동으로 `async`로 변환됩니다.

### 훅 사용 예제

이 예제는 두 개의 훅을 등록합니다: `rm -rf /`와 같은 위험한 bash 명령을 차단하는 훅과 감사를 위해 모든 도구 사용을 로깅하는 훅입니다. 보안 훅은 Bash 명령에서만 실행되며(`matcher`를
통해), 로깅 훅은 모든 도구에서 실행됩니다.

```python
from claude_agent_sdk import query, ClaudeAgentOptions, HookMatcher, HookContext
from typing import Any

async def validate_bash_command(
    input_data: dict[str, Any],
    tool_use_id: str | None,
    context: HookContext
) -> dict[str, Any]:
    """위험한 bash 명령을 유효성 검사하고 잠재적으로 차단."""
    if input_data['tool_name'] == 'Bash':
        command = input_data['tool_input'].get('command', '')
        if 'rm -rf /' in command:
            return {
                'hookSpecificOutput': {
                    'hookEventName': 'PreToolUse',
                    'permissionDecision': 'deny',
                    'permissionDecisionReason': 'Dangerous command blocked'
                }
            }
    return {}

async def log_tool_use(
    input_data: dict[str, Any],
    tool_use_id: str | None,
    context: HookContext
) -> dict[str, Any]:
    """감사를 위해 모든 도구 사용 로깅."""
    print(f"Tool used: {input_data.get('tool_name')}")
    return {}

options = ClaudeAgentOptions(
    hooks={
        'PreToolUse': [
            HookMatcher(matcher='Bash', hooks=[validate_bash_command], timeout=120),  # 유효성 검사에 2분
            HookMatcher(hooks=[log_tool_use])  # 모든 도구에 적용 (기본 60초 타임아웃)
        ],
        'PostToolUse': [
            HookMatcher(hooks=[log_tool_use])
        ]
    }
)

async for message in query(
    prompt="Analyze this codebase",
    options=options
):
    print(message)
```

## 도구 입력/출력 타입

모든 내장 Claude Code 도구의 입력/출력 스키마 문서입니다. Python SDK는 이를 타입으로 내보내지 않지만, 메시지의 도구 입력 및 출력 구조를 나타냅니다.

### Task

**도구 이름:** `Task`

**입력:**

```python
{
    "description": str,      # 작업에 대한 짧은 (3-5단어) 설명
    "prompt": str,           # 에이전트가 수행할 작업
    "subagent_type": str     # 사용할 특수 에이전트의 타입
}
```

**출력:**

```python
{
    "result": str,                    # 서브에이전트의 최종 결과
    "usage": dict | None,             # 토큰 사용량 통계
    "total_cost_usd": float | None,  # 총 비용(USD)
    "duration_ms": int | None         # 실행 시간(밀리초)
}
```

### AskUserQuestion

**도구 이름:** `AskUserQuestion`

실행 중 사용자에게 명확화 질문을 합니다. 사용법 세부
정보는 [승인 및 사용자 입력 처리](https://platform.claude.com/docs/en/agent-sdk/user-input#handle-clarifying-questions)를 참조하세요.

**입력:**

```python
{
    "questions": [                    # 사용자에게 질문할 질문 (1-4개 질문)
        {
            "question": str,          # 사용자에게 할 완전한 질문
            "header": str,            # 칩/태그로 표시되는 매우 짧은 레이블 (최대 12자)
            "options": [              # 사용 가능한 선택지 (2-4개 옵션)
                {
                    "label": str,         # 이 옵션의 표시 텍스트 (1-5단어)
                    "description": str    # 이 옵션이 무엇을 의미하는지 설명
                }
            ],
            "multiSelect": bool       # 여러 선택을 허용하려면 true로 설정
        }
    ],
    "answers": dict | None            # 권한 시스템에 의해 채워진 사용자 답변
}
```

**출력:**

```python
{
    "questions": [                    # 질문된 질문들
        {
            "question": str,
            "header": str,
            "options": [{"label": str, "description": str}],
            "multiSelect": bool
        }
    ],
    "answers": dict[str, str]         # 질문 텍스트를 답변 문자열에 매핑
                                      # 다중 선택 답변은 쉼표로 구분됨
}
```

### Bash

**도구 이름:** `Bash`

**입력:**

```python
{
    "command": str,                  # 실행할 명령
    "timeout": int | None,           # 선택적 타임아웃(밀리초, 최대 600000)
    "description": str | None,       # 명확하고 간결한 설명 (5-10단어)
    "run_in_background": bool | None # 백그라운드에서 실행하려면 true로 설정
}
```

**출력:**

```python
{
    "output": str,              # stdout과 stderr의 결합된 출력
    "exitCode": int,            # 명령의 종료 코드
    "killed": bool | None,      # 타임아웃으로 인해 명령이 종료되었는지 여부
    "shellId": str | None       # 백그라운드 프로세스의 셸 ID
}
```

### Edit

**도구 이름:** `Edit`

**입력:**

```python
{
    "file_path": str,           # 수정할 파일의 절대 경로
    "old_string": str,          # 교체할 텍스트
    "new_string": str,          # 교체될 텍스트
    "replace_all": bool | None  # 모든 발생 교체 (기본값 False)
}
```

**출력:**

```python
{
    "message": str,      # 확인 메시지
    "replacements": int, # 수행된 교체 횟수
    "file_path": str     # 편집된 파일 경로
}
```

### Read

**도구 이름:** `Read`

**입력:**

```python
{
    "file_path": str,       # 읽을 파일의 절대 경로
    "offset": int | None,   # 읽기를 시작할 줄 번호
    "limit": int | None     # 읽을 줄 수
}
```

**출력 (텍스트 파일):**

```python
{
    "content": str,         # 줄 번호가 있는 파일 내용
    "total_lines": int,     # 파일의 총 줄 수
    "lines_returned": int   # 실제로 반환된 줄 수
}
```

**출력 (이미지):**

```python
{
    "image": str,       # Base64 인코딩된 이미지 데이터
    "mime_type": str,   # 이미지 MIME 타입
    "file_size": int    # 파일 크기(바이트)
}
```

### Write

**도구 이름:** `Write`

**입력:**

```python
{
    "file_path": str,  # 쓸 파일의 절대 경로
    "content": str     # 파일에 쓸 내용
}
```

**출력:**

```python
{
    "message": str,        # 성공 메시지
    "bytes_written": int,  # 쓴 바이트 수
    "file_path": str       # 쓴 파일 경로
}
```

### Glob

**도구 이름:** `Glob`

**입력:**

```python
{
    "pattern": str,       # 파일을 매칭할 glob 패턴
    "path": str | None    # 검색할 디렉토리 (기본값은 cwd)
}
```

**출력:**

```python
{
    "matches": list[str],  # 매칭되는 파일 경로 배열
    "count": int,          # 발견된 매치 수
    "search_path": str     # 사용된 검색 디렉토리
}
```

### Grep

**도구 이름:** `Grep`

**입력:**

```python
{
    "pattern": str,                    # 정규 표현식 패턴
    "path": str | None,                # 검색할 파일 또는 디렉토리
    "glob": str | None,                # 파일을 필터링할 glob 패턴
    "type": str | None,                # 검색할 파일 타입
    "output_mode": str | None,         # "content", "files_with_matches", 또는 "count"
    "-i": bool | None,                 # 대소문자 구분 없는 검색
    "-n": bool | None,                 # 줄 번호 표시
    "-B": int | None,                  # 각 매치 전에 표시할 줄
    "-A": int | None,                  # 각 매치 후에 표시할 줄
    "-C": int | None,                  # 전후에 표시할 줄
    "head_limit": int | None,          # 출력을 처음 N개 줄/항목으로 제한
    "multiline": bool | None           # 다중 줄 모드 활성화
}
```

**출력 (content 모드):**

```python
{
    "matches": [
        {
            "file": str,
            "line_number": int | None,
            "line": str,
            "before_context": list[str] | None,
            "after_context": list[str] | None
        }
    ],
    "total_matches": int
}
```

**출력 (files_with_matches 모드):**

```python
{
    "files": list[str],  # 매치를 포함하는 파일
    "count": int         # 매치가 있는 파일 수
}
```

### NotebookEdit

**도구 이름:** `NotebookEdit`

**입력:**

```python
{
    "notebook_path": str,                     # Jupyter 노트북의 절대 경로
    "cell_id": str | None,                    # 편집할 셀의 ID
    "new_source": str,                        # 셀의 새 소스
    "cell_type": "code" | "markdown" | None,  # 셀의 타입
    "edit_mode": "replace" | "insert" | "delete" | None  # 편집 작업 타입
}
```

**출력:**

```python
{
    "message": str,                              # 성공 메시지
    "edit_type": "replaced" | "inserted" | "deleted",  # 수행된 편집 타입
    "cell_id": str | None,                       # 영향을 받은 셀 ID
    "total_cells": int                           # 편집 후 노트북의 총 셀 수
}
```

### WebFetch

**도구 이름:** `WebFetch`

**입력:**

```python
{
    "url": str,     # 콘텐츠를 가져올 URL
    "prompt": str   # 가져온 콘텐츠에서 실행할 프롬프트
}
```

**출력:**

```python
{
    "response": str,           # 프롬프트에 대한 AI 모델의 응답
    "url": str,                # 가져온 URL
    "final_url": str | None,   # 리디렉션 후 최종 URL
    "status_code": int | None  # HTTP 상태 코드
}
```

### WebSearch

**도구 이름:** `WebSearch`

**입력:**

```python
{
    "query": str,                        # 사용할 검색 쿼리
    "allowed_domains": list[str] | None, # 이 도메인의 결과만 포함
    "blocked_domains": list[str] | None  # 이 도메인의 결과를 절대 포함하지 않음
}
```

**출력:**

```python
{
    "results": [
        {
            "title": str,
            "url": str,
            "snippet": str,
            "metadata": dict | None
        }
    ],
    "total_results": int,
    "query": str
}
```

### TodoWrite

**도구 이름:** `TodoWrite`

**입력:**

```python
{
    "todos": [
        {
            "content": str,                              # 작업 설명
            "status": "pending" | "in_progress" | "completed",  # 작업 상태
            "activeForm": str                            # 설명의 활성 형식
        }
    ]
}
```

**출력:**

```python
{
    "message": str,  # 성공 메시지
    "stats": {
        "total": int,
        "pending": int,
        "in_progress": int,
        "completed": int
    }
}
```

### BashOutput

**도구 이름:** `BashOutput`

**입력:**

```python
{
    "bash_id": str,       # 백그라운드 셸의 ID
    "filter": str | None  # 출력 줄을 필터링할 선택적 정규식
}
```

**출력:**

```python
{
    "output": str,                                      # 마지막 확인 이후 새 출력
    "status": "running" | "completed" | "failed",       # 현재 셸 상태
    "exitCode": int | None                              # 완료 시 종료 코드
}
```

### KillBash

**도구 이름:** `KillBash`

**입력:**

```python
{
    "shell_id": str  # 종료할 백그라운드 셸의 ID
}
```

**출력:**

```python
{
    "message": str,  # 성공 메시지
    "shell_id": str  # 종료된 셸의 ID
}
```

### ExitPlanMode

**도구 이름:** `ExitPlanMode`

**입력:**

```python
{
    "plan": str  # 사용자 승인을 위해 실행할 계획
}
```

**출력:**

```python
{
    "message": str,          # 확인 메시지
    "approved": bool | None  # 사용자가 계획을 승인했는지 여부
}
```

### ListMcpResources

**도구 이름:** `ListMcpResources`

**입력:**

```python
{
    "server": str | None  # 리소스를 필터링할 선택적 서버 이름
}
```

**출력:**

```python
{
    "resources": [
        {
            "uri": str,
            "name": str,
            "description": str | None,
            "mimeType": str | None,
            "server": str
        }
    ],
    "total": int
}
```

### ReadMcpResource

**도구 이름:** `ReadMcpResource`

**입력:**

```python
{
    "server": str,  # MCP 서버 이름
    "uri": str      # 읽을 리소스 URI
}
```

**출력:**

```python
{
    "contents": [
        {
            "uri": str,
            "mimeType": str | None,
            "text": str | None,
            "blob": str | None
        }
    ],
    "server": str
}
```

## ClaudeSDKClient의 고급 기능

### 지속적인 대화 인터페이스 구축

```python
from claude_agent_sdk import ClaudeSDKClient, ClaudeAgentOptions, AssistantMessage, TextBlock
import asyncio

class ConversationSession:
    """Claude와의 단일 대화 세션을 유지합니다."""

    def __init__(self, options: ClaudeAgentOptions = None):
        self.client = ClaudeSDKClient(options)
        self.turn_count = 0

    async def start(self):
        await self.client.connect()
        print("Starting conversation session. Claude will remember context.")
        print("Commands: 'exit' to quit, 'interrupt' to stop current task, 'new' for new session")

        while True:
            user_input = input(f"\n[Turn {self.turn_count + 1}] You: ")

            if user_input.lower() == 'exit':
                break
            elif user_input.lower() == 'interrupt':
                await self.client.interrupt()
                print("Task interrupted!")
                continue
            elif user_input.lower() == 'new':
                # 새로운 세션을 위해 연결 해제 후 재연결
                await self.client.disconnect()
                await self.client.connect()
                self.turn_count = 0
                print("Started new conversation session (previous context cleared)")
                continue

            # 메시지 전송 - Claude는 이 세션의 모든 이전 메시지를 기억함
            await self.client.query(user_input)
            self.turn_count += 1

            # 응답 처리
            print(f"[Turn {self.turn_count}] Claude: ", end="")
            async for message in self.client.receive_response():
                if isinstance(message, AssistantMessage):
                    for block in message.content:
                        if isinstance(block, TextBlock):
                            print(block.text, end="")
            print()  # 응답 후 줄바꿈

        await self.client.disconnect()
        print(f"Conversation ended after {self.turn_count} turns.")

async def main():
    options = ClaudeAgentOptions(
        allowed_tools=["Read", "Write", "Bash"],
        permission_mode="acceptEdits"
    )
    session = ConversationSession(options)
    await session.start()

# 예제 대화:
# Turn 1 - You: "Create a file called hello.py"
# Turn 1 - Claude: "I'll create a hello.py file for you..."
# Turn 2 - You: "What's in that file?"
# Turn 2 - Claude: "The hello.py file I just created contains..." (기억함!)
# Turn 3 - You: "Add a main function to it"
# Turn 3 - Claude: "I'll add a main function to hello.py..." (어떤 파일인지 알고 있음!)

asyncio.run(main())
```

### 동작 수정을 위한 훅 사용

```python
from claude_agent_sdk import (
    ClaudeSDKClient,
    ClaudeAgentOptions,
    HookMatcher,
    HookContext
)
import asyncio
from typing import Any

async def pre_tool_logger(
    input_data: dict[str, Any],
    tool_use_id: str | None,
    context: HookContext
) -> dict[str, Any]:
    """실행 전 모든 도구 사용 로깅."""
    tool_name = input_data.get('tool_name', 'unknown')
    print(f"[PRE-TOOL] About to use: {tool_name}")

    # 여기서 도구 실행을 수정하거나 차단할 수 있음
    if tool_name == "Bash" and "rm -rf" in str(input_data.get('tool_input', {})):
        return {
            'hookSpecificOutput': {
                'hookEventName': 'PreToolUse',
                'permissionDecision': 'deny',
                'permissionDecisionReason': 'Dangerous command blocked'
            }
        }
    return {}

async def post_tool_logger(
    input_data: dict[str, Any],
    tool_use_id: str | None,
    context: HookContext
) -> dict[str, Any]:
    """도구 실행 후 결과 로깅."""
    tool_name = input_data.get('tool_name', 'unknown')
    print(f"[POST-TOOL] Completed: {tool_name}")
    return {}

async def user_prompt_modifier(
    input_data: dict[str, Any],
    tool_use_id: str | None,
    context: HookContext
) -> dict[str, Any]:
    """사용자 프롬프트에 컨텍스트 추가."""
    original_prompt = input_data.get('prompt', '')

    # 모든 프롬프트에 타임스탬프 추가
    from datetime import datetime
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    return {
        'hookSpecificOutput': {
            'hookEventName': 'UserPromptSubmit',
            'updatedPrompt': f"[{timestamp}] {original_prompt}"
        }
    }

async def main():
    options = ClaudeAgentOptions(
        hooks={
            'PreToolUse': [
                HookMatcher(hooks=[pre_tool_logger]),
                HookMatcher(matcher='Bash', hooks=[pre_tool_logger])
            ],
            'PostToolUse': [
                HookMatcher(hooks=[post_tool_logger])
            ],
            'UserPromptSubmit': [
                HookMatcher(hooks=[user_prompt_modifier])
            ]
        },
        allowed_tools=["Read", "Write", "Bash"]
    )

    async with ClaudeSDKClient(options=options) as client:
        await client.query("List files in current directory")

        async for message in client.receive_response():
            # 훅이 자동으로 도구 사용을 로깅함
            pass

asyncio.run(main())
```

### 실시간 진행 상황 모니터링

```python
from claude_agent_sdk import (
    ClaudeSDKClient,
    ClaudeAgentOptions,
    AssistantMessage,
    ToolUseBlock,
    ToolResultBlock,
    TextBlock
)
import asyncio

async def monitor_progress():
    options = ClaudeAgentOptions(
        allowed_tools=["Write", "Bash"],
        permission_mode="acceptEdits"
    )

    async with ClaudeSDKClient(options=options) as client:
        await client.query(
            "Create 5 Python files with different sorting algorithms"
        )

        # 실시간으로 진행 상황 모니터링
        files_created = []
        async for message in client.receive_messages():
            if isinstance(message, AssistantMessage):
                for block in message.content:
                    if isinstance(block, ToolUseBlock):
                        if block.name == "Write":
                            file_path = block.input.get("file_path", "")
                            print(f"🔨 Creating: {file_path}")
                    elif isinstance(block, ToolResultBlock):
                        print(f"✅ Completed tool execution")
                    elif isinstance(block, TextBlock):
                        print(f"💭 Claude says: {block.text[:100]}...")

            # 최종 결과를 받았는지 확인
            if hasattr(message, 'subtype') and message.subtype in ['success', 'error']:
                print(f"\n🎯 Task completed!")
                break

asyncio.run(monitor_progress())
```

## 예제 사용법

### 기본 파일 작업 (query 사용)

```python
from claude_agent_sdk import query, ClaudeAgentOptions, AssistantMessage, ToolUseBlock
import asyncio

async def create_project():
    options = ClaudeAgentOptions(
        allowed_tools=["Read", "Write", "Bash"],
        permission_mode='acceptEdits',
        cwd="/home/user/project"
    )

    async for message in query(
        prompt="Create a Python project structure with setup.py",
        options=options
    ):
        if isinstance(message, AssistantMessage):
            for block in message.content:
                if isinstance(block, ToolUseBlock):
                    print(f"Using tool: {block.name}")

asyncio.run(create_project())
```

### 오류 처리

```python
from claude_agent_sdk import (
    query,
    CLINotFoundError,
    ProcessError,
    CLIJSONDecodeError
)

try:
    async for message in query(prompt="Hello"):
        print(message)
except CLINotFoundError:
    print("Please install Claude Code: npm install -g @anthropic-ai/claude-code")
except ProcessError as e:
    print(f"Process failed with exit code: {e.exit_code}")
except CLIJSONDecodeError as e:
    print(f"Failed to parse response: {e}")
```

### 클라이언트로 스트리밍 모드

```python
from claude_agent_sdk import ClaudeSDKClient
import asyncio

async def interactive_session():
    async with ClaudeSDKClient() as client:
        # 초기 메시지 전송
        await client.query("What's the weather like?")

        # 응답 처리
        async for msg in client.receive_response():
            print(msg)

        # 후속 질문 전송
        await client.query("Tell me more about that")

        # 후속 응답 처리
        async for msg in client.receive_response():
            print(msg)

asyncio.run(interactive_session())
```

### ClaudeSDKClient로 커스텀 도구 사용

```python
from claude_agent_sdk import (
    ClaudeSDKClient,
    ClaudeAgentOptions,
    tool,
    create_sdk_mcp_server,
    AssistantMessage,
    TextBlock
)
import asyncio
from typing import Any

# @tool 데코레이터로 커스텀 도구 정의
@tool("calculate", "Perform mathematical calculations", {"expression": str})
async def calculate(args: dict[str, Any]) -> dict[str, Any]:
    try:
        result = eval(args["expression"], {"__builtins__": {}})
        return {
            "content": [{
                "type": "text",
                "text": f"Result: {result}"
            }]
        }
    except Exception as e:
        return {
            "content": [{
                "type": "text",
                "text": f"Error: {str(e)}"
            }],
            "is_error": True
        }

@tool("get_time", "Get current time", {})
async def get_time(args: dict[str, Any]) -> dict[str, Any]:
    from datetime import datetime
    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    return {
        "content": [{
            "type": "text",
            "text": f"Current time: {current_time}"
        }]
    }

async def main():
    # 커스텀 도구로 SDK MCP 서버 생성
    my_server = create_sdk_mcp_server(
        name="utilities",
        version="1.0.0",
        tools=[calculate, get_time]
    )

    # 서버로 옵션 설정
    options = ClaudeAgentOptions(
        mcp_servers={"utils": my_server},
        allowed_tools=[
            "mcp__utils__calculate",
            "mcp__utils__get_time"
        ]
    )

    # 상호작용 도구 사용을 위해 ClaudeSDKClient 사용
    async with ClaudeSDKClient(options=options) as client:
        await client.query("What's 123 * 456?")

        # 계산 응답 처리
        async for message in client.receive_response():
            if isinstance(message, AssistantMessage):
                for block in message.content:
                    if isinstance(block, TextBlock):
                        print(f"Calculation: {block.text}")

        # 시간 쿼리로 후속 질문
        await client.query("What time is it now?")

        async for message in client.receive_response():
            if isinstance(message, AssistantMessage):
                for block in message.content:
                    if isinstance(block, TextBlock):
                        print(f"Time: {block.text}")

asyncio.run(main())
```

## 샌드박스 설정

### `SandboxSettings`

샌드박스 동작에 대한 설정입니다. 명령 샌드박싱을 활성화하고 프로그래밍 방식으로 네트워크 제한을 구성하는 데 사용합니다.

```python
class SandboxSettings(TypedDict, total=False):
    enabled: bool
    autoAllowBashIfSandboxed: bool
    excludedCommands: list[str]
    allowUnsandboxedCommands: bool
    network: SandboxNetworkConfig
    ignoreViolations: SandboxIgnoreViolations
    enableWeakerNestedSandbox: bool
```

| 속성                          | 타입                                                    | 기본값     | 설명                                                                                                                                                                      |
|:----------------------------|:------------------------------------------------------|:--------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`                   | `bool`                                                | `False` | 명령 실행을 위한 샌드박스 모드 활성화                                                                                                                                                   |
| `autoAllowBashIfSandboxed`  | `bool`                                                | `False` | 샌드박스가 활성화된 경우 bash 명령 자동 승인                                                                                                                                             |
| `excludedCommands`          | `list[str]`                                           | `[]`    | 항상 샌드박스 제한을 우회하는 명령 (예: `["docker"]`). 이들은 모델 개입 없이 자동으로 샌드박스 없이 실행됨                                                                                                    |
| `allowUnsandboxedCommands`  | `bool`                                                | `False` | 모델이 샌드박스 외부에서 명령을 실행하도록 요청할 수 있도록 허용. `True`인 경우, 모델은 도구 입력에서 `dangerouslyDisableSandbox`를 설정할 수 있으며, 이는 [권한 시스템](#permissions-fallback-for-unsandboxed-commands)으로 폴백됨 |
| `network`                   | [`SandboxNetworkConfig`](#sandboxnetworkconfig)       | `None`  | 네트워크 특정 샌드박스 설정                                                                                                                                                         |
| `ignoreViolations`          | [`SandboxIgnoreViolations`](#sandboxignoreviolations) | `None`  | 무시할 샌드박스 위반 설정                                                                                                                                                          |
| `enableWeakerNestedSandbox` | `bool`                                                | `False` | 호환성을 위한 약한 중첩 샌드박스 활성화                                                                                                                                                  |

> **파일 시스템 및 네트워크 액세스 제한**은 샌드박스 설정을 통해 설정되지 **않습니다**.
> 대신 [권한 규칙](https://code.claude.com/docs/en/settings#permission-settings)에서 파생됩니다:
>
> - **파일 시스템 읽기 제한**: Read 거부 규칙
> - **파일 시스템 쓰기 제한**: Edit 허용/거부 규칙
> - **네트워크 제한**: WebFetch 허용/거부 규칙
>
> 명령 실행 샌드박싱에는 샌드박스 설정을 사용하고, 파일 시스템 및 네트워크 액세스 제어에는 권한 규칙을 사용하세요.

#### 예제 사용법

```python
from claude_agent_sdk import query, ClaudeAgentOptions, SandboxSettings

sandbox_settings: SandboxSettings = {
    "enabled": True,
    "autoAllowBashIfSandboxed": True,
    "network": {
        "allowLocalBinding": True
    }
}

async for message in query(
    prompt="Build and test my project",
    options=ClaudeAgentOptions(sandbox=sandbox_settings)
):
    print(message)
```

> **유닉스 소켓 보안**: `allowUnixSockets` 옵션은 강력한 시스템 서비스에 대한 액세스를 부여할 수 있습니다. 예를 들어, `/var/run/docker.sock`을 허용하면 Docker API를
> 통해 호스트 시스템에 대한 전체 액세스를 효과적으로 부여하여 샌드박스 격리를 우회합니다. 반드시 필요한 유닉스 소켓만 허용하고 각각의 보안 영향을 이해하세요.

### `SandboxNetworkConfig`

샌드박스 모드를 위한 네트워크 특정 설정입니다.

```python
class SandboxNetworkConfig(TypedDict, total=False):
    allowLocalBinding: bool
    allowUnixSockets: list[str]
    allowAllUnixSockets: bool
    httpProxyPort: int
    socksProxyPort: int
```

| 속성                    | 타입          | 기본값     | 설명                                       |
|:----------------------|:------------|:--------|:-----------------------------------------|
| `allowLocalBinding`   | `bool`      | `False` | 프로세스가 로컬 포트에 바인딩하도록 허용 (예: 개발 서버용)       |
| `allowUnixSockets`    | `list[str]` | `[]`    | 프로세스가 액세스할 수 있는 유닉스 소켓 경로 (예: Docker 소켓) |
| `allowAllUnixSockets` | `bool`      | `False` | 모든 유닉스 소켓에 대한 액세스 허용                     |
| `httpProxyPort`       | `int`       | `None`  | 네트워크 요청을 위한 HTTP 프록시 포트                  |
| `socksProxyPort`      | `int`       | `None`  | 네트워크 요청을 위한 SOCKS 프록시 포트                 |

### `SandboxIgnoreViolations`

특정 샌드박스 위반을 무시하기 위한 설정입니다.

```python
class SandboxIgnoreViolations(TypedDict, total=False):
    file: list[str]
    network: list[str]
```

| 속성        | 타입          | 기본값  | 설명               |
|:----------|:------------|:-----|:-----------------|
| `file`    | `list[str]` | `[]` | 위반을 무시할 파일 경로 패턴 |
| `network` | `list[str]` | `[]` | 위반을 무시할 네트워크 패턴  |

### 샌드박스 없는 명령을 위한 권한 폴백

`allowUnsandboxedCommands`가 활성화되면, 모델은 도구 입력에서 `dangerouslyDisableSandbox: True`를 설정하여 샌드박스 외부에서 명령을 실행하도록 요청할 수 있습니다.
이러한 요청은 기존 권한 시스템으로 폴백되며, 이는 `can_use_tool` 핸들러가 호출되어 커스텀 권한 로직을 구현할 수 있음을 의미합니다.


> **`excludedCommands` vs `allowUnsandboxedCommands`:**
> - `excludedCommands`: 항상 자동으로 샌드박스를 우회하는 명령의 정적 목록 (예: `["docker"]`). 모델은 이를 제어할 수 없습니다.
> - `allowUnsandboxedCommands`: 모델이 도구 입력에서 `dangerouslyDisableSandbox: True`를 설정하여 런타임에 샌드박스 없는 실행을 요청할지 여부를 결정할 수 있게
    합니다.

```python
from claude_agent_sdk import query, ClaudeAgentOptions

async def can_use_tool(tool: str, input: dict) -> bool:
    # 모델이 샌드박스를 우회하도록 요청하는지 확인
    if tool == "Bash" and input.get("dangerouslyDisableSandbox"):
        # 모델이 이 명령을 샌드박스 외부에서 실행하기를 원함
        print(f"Unsandboxed command requested: {input.get('command')}")

        # 허용하려면 True, 거부하려면 False 반환
        return is_command_authorized(input.get("command"))
    return True

async def main():
    async for message in query(
        prompt="Deploy my application",
        options=ClaudeAgentOptions(
            sandbox={
                "enabled": True,
                "allowUnsandboxedCommands": True  # 모델이 샌드박스 없는 실행 요청 가능
            },
            permission_mode="default",
            can_use_tool=can_use_tool
        )
    ):
        print(message)
```

이 패턴을 통해 다음을 수행할 수 있습니다:

- **모델 요청 감사**: 모델이 샌드박스 없는 실행을 요청할 때 로깅
- **허용 목록 구현**: 특정 명령만 샌드박스 없이 실행되도록 허용
- **승인 워크플로 추가**: 특권 작업에 대한 명시적 권한 필요

> `dangerouslyDisableSandbox: True`로 실행되는 명령은 전체 시스템 액세스 권한을 가집니다. `can_use_tool` 핸들러가 이러한 요청을 신중하게 유효성 검사하도록 하세요.
>
> `permission_mode`가 `bypassPermissions`로 설정되고 `allow_unsandboxed_commands`가 활성화된 경우, 모델은 승인 프롬프트 없이 샌드박스 외부에서 명령을 자율적으로
> 실행할 수 있습니다. 이 조합은 효과적으로 모델이 조용히 샌드박스 격리를 벗어날 수 있게 합니다.

## 참고

- [Python SDK 가이드](../05-agent-sdk/05-python.md) - 튜토리얼 및 예제
- [SDK 개요](../05-agent-sdk/01-overview.md) - 일반 SDK 개념
- [TypeScript SDK 레퍼런스](../05-agent-sdk/03-typescript.md) - TypeScript SDK 문서
- [CLI 레퍼런스](https://code.claude.com/docs/en/cli-reference) - 커맨드 라인 인터페이스
- [일반 워크플로](https://code.claude.com/docs/en/common-workflows) - 단계별 가이드

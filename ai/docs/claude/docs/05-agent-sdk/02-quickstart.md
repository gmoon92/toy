# [빠른 시작](https://platform.claude.com/docs/en/agent-sdk/quickstart)

Python 또는 TypeScript Agent SDK를 사용하여 자율적으로 작동하는 AI 에이전트를 구축해보세요.

---

Agent SDK를 사용하여 코드를 읽고, 버그를 찾고, 수동 개입 없이 모두 자동으로 수정하는 AI 에이전트를 구축합니다.

**수행할 작업:**
1. Agent SDK로 프로젝트 설정
2. 버그가 있는 코드 파일 생성
3. 버그를 자동으로 찾아 수정하는 에이전트 실행

## 사전 요구사항

- **Node.js 18+** 또는 **Python 3.10+**
- **Anthropic 계정** ([여기서 가입](https://platform.claude.com/))

## 설정

<Steps>
  <Step title="Claude Code 설치">
    Agent SDK는 Claude Code를 런타임으로 사용합니다. 플랫폼에 맞게 설치하세요:

    <Tabs>
      <Tab title="macOS/Linux/WSL">
        ```bash
        curl -fsSL https://claude.ai/install.sh | bash
        ```
      </Tab>
      <Tab title="Homebrew">
        ```bash
        brew install --cask claude-code
        ```
      </Tab>
      <Tab title="WinGet">
        ```powershell
        winget install Anthropic.ClaudeCode
        ```
      </Tab>
    </Tabs>

    컴퓨터에 Claude Code를 설치한 후, 터미널에서 `claude`를 실행하고 인증 안내에 따라 진행하세요. SDK는 이 인증을 자동으로 사용합니다.

    
> Claude Code 설치에 대한 자세한 정보는 [Claude Code 설정](https://code.claude.com/docs/en/setup)을 참조하세요.

  </Step>

  <Step title="프로젝트 폴더 생성">
    이 빠른 시작을 위한 새 디렉토리를 생성합니다:

    ```bash
    mkdir my-agent && cd my-agent
    ```

    자체 프로젝트의 경우 어떤 폴더에서든 SDK를 실행할 수 있으며, 기본적으로 해당 디렉토리와 하위 디렉토리의 파일에 접근할 수 있습니다.
  </Step>

  <Step title="SDK 설치">
    사용하는 언어에 맞는 Agent SDK 패키지를 설치합니다:

    <Tabs>
      <Tab title="TypeScript">
        ```bash
        npm install @anthropic-ai/claude-agent-sdk
        ```
      </Tab>
      <Tab title="Python (uv)">
        [uv Python 패키지 매니저](https://docs.astral.sh/uv/)는 가상 환경을 자동으로 처리하는 빠른 Python 패키지 매니저입니다:
        ```bash
        uv init && uv add claude-agent-sdk
        ```
      </Tab>
      <Tab title="Python (pip)">
        먼저 가상 환경을 생성한 후 설치합니다:
        ```bash
        python3 -m venv .venv && source .venv/bin/activate
        pip3 install claude-agent-sdk
        ```
      </Tab>
    </Tabs>
  </Step>

  <Step title="API 키 설정">
    이미 Claude Code를 인증했다면(터미널에서 `claude`를 실행하여), SDK는 해당 인증을 자동으로 사용합니다.

    그렇지 않은 경우 [Claude Console](https://platform.claude.com/)에서 API 키가 필요합니다.

    프로젝트 디렉토리에 `.env` 파일을 생성하고 API 키를 저장합니다:

    ```bash
    ANTHROPIC_API_KEY=your-api-key
    ```

    
> **Amazon Bedrock, Google Vertex AI 또는 Microsoft Azure를 사용하시나요?** [Bedrock](https://code.claude.com/docs/en/amazon-bedrock), [Vertex AI](https://code.claude.com/docs/en/google-vertex-ai) 또는 [Azure AI Foundry](https://code.claude.com/docs/en/azure-ai-foundry) 설정 가이드를 참조하세요.
>
> 사전 승인이 없는 한, Anthropic은 타사 개발자가 Agent SDK 기반 에이전트를 포함하여 자신의 제품에 claude.ai 로그인 또는 사용률 제한을 제공하는 것을 허용하지 않습니다. 대신 이 문서에 설명된 API 키 인증 방법을 사용하세요.

  </Step>
</Steps>

## 버그가 있는 파일 생성

이 빠른 시작은 코드에서 버그를 찾아 수정할 수 있는 에이전트를 구축하는 과정을 안내합니다. 먼저, 에이전트가 수정할 의도적인 버그가 있는 파일이 필요합니다. `my-agent` 디렉토리에 `utils.py`를 생성하고 다음 코드를 붙여넣으세요:

```python
def calculate_average(numbers):
    total = 0
    for num in numbers:
        total += num
    return total / len(numbers)

def get_user_name(user):
    return user["name"].upper()
```

이 코드에는 두 가지 버그가 있습니다:
1. `calculate_average([])`는 0으로 나누기로 인해 크래시됩니다
2. `get_user_name(None)`은 TypeError로 크래시됩니다

## 버그를 찾아 수정하는 에이전트 구축

Python SDK를 사용하는 경우 `agent.py`를, TypeScript를 사용하는 경우 `agent.ts`를 생성합니다:

<details>
<summary>Python 예시</summary>

```python
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, AssistantMessage, ResultMessage

async def main():
    # 에이전트 루프: Claude가 작업하는 동안 메시지를 스트리밍합니다
    async for message in query(
        prompt="utils.py에서 크래시를 일으킬 수 있는 버그를 검토하세요. 발견한 문제를 수정하세요.",
        options=ClaudeAgentOptions(
            allowed_tools=["Read", "Edit", "Glob"],  # Claude가 사용할 수 있는 도구
            permission_mode="acceptEdits"            # 파일 편집 자동 승인
        )
    ):
        # 사람이 읽을 수 있는 출력 출력
        if isinstance(message, AssistantMessage):
            for block in message.content:
                if hasattr(block, "text"):
                    print(block.text)              # Claude의 추론
                elif hasattr(block, "name"):
                    print(f"Tool: {block.name}")   # 호출되는 도구
        elif isinstance(message, ResultMessage):
            print(f"Done: {message.subtype}")      # 최종 결과

asyncio.run(main())
```

</details>

이 코드는 세 가지 주요 부분으로 구성됩니다:

1. **`query`**: 에이전트 루프를 생성하는 주요 진입점입니다. 비동기 반복자를 반환하므로 Claude가 작업하는 동안 메시지를 스트리밍하기 위해 `async for`를 사용합니다. [Python](../05-agent-sdk/05-python.md) 또는 [TypeScript](../05-agent-sdk/03-typescript.md) SDK 참조에서 전체 API를 확인하세요.

2. **`prompt`**: Claude가 수행할 작업입니다. Claude는 작업에 따라 사용할 도구를 결정합니다.

3. **`options`**: 에이전트 구성입니다. 이 예제는 `allowedTools`를 사용하여 Claude를 `Read`, `Edit`, `Glob`로 제한하고, `permissionMode: "acceptEdits"`를 사용하여 파일 변경을 자동 승인합니다. 다른 옵션으로는 `systemPrompt`, `mcpServers` 등이 있습니다. [Python](../05-agent-sdk/05-python.md) 또는 [TypeScript](../05-agent-sdk/03-typescript.md)의 모든 옵션을 확인하세요.

`async for` 루프는 Claude가 생각하고, 도구를 호출하고, 결과를 관찰하고, 다음에 할 일을 결정하는 동안 계속 실행됩니다. 각 반복은 메시지를 생성합니다: Claude의 추론, 도구 호출, 도구 결과 또는 최종 결과. SDK는 오케스트레이션(도구 실행, 컨텍스트 관리, 재시도)을 처리하므로 스트림을 소비하기만 하면 됩니다. 루프는 Claude가 작업을 완료하거나 오류가 발생하면 종료됩니다.

루프 내의 메시지 처리는 사람이 읽을 수 있는 출력을 필터링합니다. 필터링 없이는 시스템 초기화 및 내부 상태를 포함한 원시 메시지 객체를 보게 되며, 이는 디버깅에는 유용하지만 그렇지 않으면 노이즈가 많습니다.


> 이 예제는 실시간으로 진행 상황을 표시하기 위해 스트리밍을 사용합니다. 라이브 출력이 필요하지 않은 경우(예: 백그라운드 작업 또는 CI 파이프라인), 모든 메시지를 한 번에 수집할 수 있습니다. 자세한 내용은 [스트리밍 vs. 단일 턴 모드](https://platform.claude.com/docs/en/agent-sdk/streaming-vs-single-mode)를 참조하세요.


### 에이전트 실행

에이전트가 준비되었습니다. 다음 명령으로 실행하세요:

<Tabs>
  <Tab title="Python">
    ```bash
    python3 agent.py
    ```
  </Tab>
  <Tab title="TypeScript">
    ```bash
    npx tsx agent.ts
    ```
  </Tab>
</Tabs>

실행 후 `utils.py`를 확인하세요. 빈 리스트와 null 사용자를 처리하는 방어적 코드가 표시됩니다. 에이전트가 자율적으로:

1. `utils.py`를 **읽어** 코드를 이해했습니다
2. 로직을 **분석**하고 크래시를 일으킬 수 있는 엣지 케이스를 식별했습니다
3. 파일을 **편집**하여 적절한 오류 처리를 추가했습니다

이것이 Agent SDK가 다른 점입니다: Claude는 도구를 구현하도록 요청하는 대신 직접 실행합니다.


> "Claude Code not found"가 표시되면 [Claude Code를 설치](#install-claude-code)하고 터미널을 다시 시작하세요. "API key not found"의 경우 [API 키를 설정](#set-your-api-key)하세요. 자세한 내용은 [전체 문제 해결 가이드](https://code.claude.com/docs/en/troubleshooting)를 참조하세요.


### 다른 프롬프트 시도

이제 에이전트가 설정되었으므로 다른 프롬프트를 시도해보세요:

- `"utils.py의 모든 함수에 docstring을 추가하세요"`
- `"utils.py의 모든 함수에 타입 힌트를 추가하세요"`
- `"utils.py의 함수를 문서화하는 README.md를 생성하세요"`

### 에이전트 커스터마이징

옵션을 변경하여 에이전트의 동작을 수정할 수 있습니다. 몇 가지 예는 다음과 같습니다:

**웹 검색 기능 추가:**

<details>
<summary>Python 예시</summary>

```python
options=ClaudeAgentOptions(
    allowed_tools=["Read", "Edit", "Glob", "WebSearch"],
    permission_mode="acceptEdits"
)
```

</details>

**Claude에게 커스텀 시스템 프롬프트 제공:**

<details>
<summary>Python 예시</summary>

```python
options=ClaudeAgentOptions(
    allowed_tools=["Read", "Edit", "Glob"],
    permission_mode="acceptEdits",
    system_prompt="당신은 시니어 Python 개발자입니다. 항상 PEP 8 스타일 가이드라인을 따르세요."
)
```

</details>

**터미널에서 명령 실행:**

<details>
<summary>Python 예시</summary>

```python
options=ClaudeAgentOptions(
    allowed_tools=["Read", "Edit", "Glob", "Bash"],
    permission_mode="acceptEdits"
)
```

</details>

`Bash`가 활성화된 상태에서 시도해보세요: `"utils.py에 대한 단위 테스트를 작성하고 실행한 후 모든 실패를 수정하세요"`

## 핵심 개념

**도구**는 에이전트가 수행할 수 있는 작업을 제어합니다:

| 도구 | 에이전트가 할 수 있는 작업 |
|-------|----------------------|
| `Read`, `Glob`, `Grep` | 읽기 전용 분석 |
| `Read`, `Edit`, `Glob` | 코드 분석 및 수정 |
| `Read`, `Edit`, `Bash`, `Glob`, `Grep` | 전체 자동화 |

**권한 모드**는 사람의 감독이 얼마나 필요한지 제어합니다:

| 모드 | 동작 | 사용 사례 |
|------|----------|----------|
| `acceptEdits` | 파일 편집을 자동 승인하고 다른 작업은 확인 | 신뢰할 수 있는 개발 워크플로 |
| `bypassPermissions` | 프롬프트 없이 실행 | CI/CD 파이프라인, 자동화 |
| `default` | 승인을 처리하기 위한 `canUseTool` 콜백 필요 | 커스텀 승인 플로우 |

위의 예제는 `acceptEdits` 모드를 사용하며, 에이전트가 대화형 프롬프트 없이 실행될 수 있도록 파일 작업을 자동 승인합니다. 사용자에게 승인을 요청하려면 `default` 모드를 사용하고 사용자 입력을 수집하는 [`canUseTool` 콜백](https://platform.claude.com/docs/en/agent-sdk/user-input)을 제공하세요. 더 많은 제어를 원하면 [권한](https://platform.claude.com/docs/en/agent-sdk/permissions)을 참조하세요.

## 다음 단계

첫 번째 에이전트를 만들었으니 이제 기능을 확장하고 사용 사례에 맞게 조정하는 방법을 배우세요:

- **[권한](https://platform.claude.com/docs/en/agent-sdk/permissions)**: 에이전트가 할 수 있는 작업과 승인이 필요한 시기를 제어
- **[훅](https://platform.claude.com/docs/en/agent-sdk/hooks)**: 도구 호출 전후에 커스텀 코드 실행
- **[세션](https://platform.claude.com/docs/en/agent-sdk/sessions)**: 컨텍스트를 유지하는 멀티턴 에이전트 구축
- **[MCP 서버](https://platform.claude.com/docs/en/agent-sdk/mcp)**: 데이터베이스, 브라우저, API 및 기타 외부 시스템에 연결
- **[호스팅](https://platform.claude.com/docs/en/agent-sdk/hosting)**: Docker, 클라우드 및 CI/CD에 에이전트 배포
- **[예제 에이전트](https://github.com/anthropics/claude-agent-sdk-demos)**: 이메일 어시스턴트, 리서치 에이전트 등 완전한 예제 확인

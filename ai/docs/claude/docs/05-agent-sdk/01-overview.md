# [Agent SDK 개요](https://platform.claude.com/docs/en/agent-sdk/overview)

Claude Code를 라이브러리로 사용하여 프로덕션 AI 에이전트 구축하기

---


> Claude Code SDK가 Claude Agent SDK로 이름이 변경되었습니다. 이전 SDK에서 마이그레이션하는 경우 [마이그레이션 가이드](../05-agent-sdk/06-migration-guide.md)를 참조하세요.


파일 읽기, 명령 실행, 웹 검색, 코드 편집 등을 자율적으로 수행하는 AI 에이전트를 구축하세요. Agent SDK는 Claude Code를 구동하는 동일한 도구, 에이전트 루프, 컨텍스트 관리 기능을 Python과 TypeScript로 프로그래밍 가능하게 제공합니다.

<CodeGroup>
```python Python
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions

async def main():
    async for message in query(
        prompt="auth.py에서 버그를 찾아서 수정해줘",
        options=ClaudeAgentOptions(allowed_tools=["Read", "Edit", "Bash"])
    ):
        print(message)  # Claude가 파일을 읽고, 버그를 찾고, 편집합니다

asyncio.run(main())
```

```typescript TypeScript
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "auth.py에서 버그를 찾아서 수정해줘",
  options: { allowedTools: ["Read", "Edit", "Bash"] }
})) {
  console.log(message);  // Claude가 파일을 읽고, 버그를 찾고, 편집합니다
}
```
</CodeGroup>

Agent SDK는 파일 읽기, 명령 실행, 코드 편집을 위한 내장 도구를 포함하고 있어, 도구 실행을 직접 구현하지 않아도 에이전트가 즉시 작업을 시작할 수 있습니다. 퀵스타트를 시작하거나 SDK로 구축된 실제 에이전트를 살펴보세요:


  
> 몇 분 안에 버그 수정 에이전트 구축하기

  
> 이메일 어시스턴트, 리서치 에이전트 등



## 기능

Claude Code를 강력하게 만드는 모든 기능이 SDK에서 사용 가능합니다:

<Tabs>
  <Tab title="내장 도구">
    에이전트는 즉시 파일을 읽고, 명령을 실행하고, 코드베이스를 검색할 수 있습니다. 주요 도구는 다음과 같습니다:

    | 도구 | 기능 |
    |------|------|
    | **Read** | 작업 디렉터리의 모든 파일 읽기 |
    | **Write** | 새 파일 생성 |
    | **Edit** | 기존 파일에 정확한 편집 수행 |
    | **Bash** | 터미널 명령, 스크립트, git 작업 실행 |
    | **Glob** | 패턴으로 파일 찾기 (`**/*.ts`, `src/**/*.py`) |
    | **Grep** | 정규식으로 파일 내용 검색 |
    | **WebSearch** | 최신 정보를 위한 웹 검색 |
    | **WebFetch** | 웹 페이지 콘텐츠 가져오기 및 파싱 |
    | **[AskUserQuestion](/docs/en/agent-sdk/user-input#handle-clarifying-questions)** | 사용자에게 다중 선택 옵션으로 명확화 질문하기 |

    다음 예제는 코드베이스에서 TODO 주석을 검색하는 에이전트를 생성합니다:

    <CodeGroup>
    ```python Python
    import asyncio
    from claude_agent_sdk import query, ClaudeAgentOptions

    async def main():
        async for message in query(
            prompt="모든 TODO 주석을 찾아서 요약본을 만들어줘",
            options=ClaudeAgentOptions(allowed_tools=["Read", "Glob", "Grep"])
        ):
            if hasattr(message, "result"):
                print(message.result)

    asyncio.run(main())
    ```

    ```typescript TypeScript
    import { query } from "@anthropic-ai/claude-agent-sdk";

    for await (const message of query({
      prompt: "모든 TODO 주석을 찾아서 요약본을 만들어줘",
      options: { allowedTools: ["Read", "Glob", "Grep"] }
    })) {
      if ("result" in message) console.log(message.result);
    }
    ```
    </CodeGroup>

  </Tab>
  <Tab title="훅">
    에이전트 생명주기의 주요 시점에 커스텀 코드를 실행합니다. SDK 훅은 콜백 함수를 사용하여 에이전트 동작을 검증, 로깅, 차단 또는 변환할 수 있습니다.

    **사용 가능한 훅:** `PreToolUse`, `PostToolUse`, `Stop`, `SessionStart`, `SessionEnd`, `UserPromptSubmit` 등.

    다음 예제는 모든 파일 변경 사항을 감사 파일에 로깅합니다:

    <CodeGroup>
    ```python Python
    import asyncio
    from datetime import datetime
    from claude_agent_sdk import query, ClaudeAgentOptions, HookMatcher

    async def log_file_change(input_data, tool_use_id, context):
        file_path = input_data.get('tool_input', {}).get('file_path', 'unknown')
        with open('./audit.log', 'a') as f:
            f.write(f"{datetime.now()}: modified {file_path}\n")
        return {}

    async def main():
        async for message in query(
            prompt="utils.py를 리팩터링해서 가독성을 향상시켜줘",
            options=ClaudeAgentOptions(
                permission_mode="acceptEdits",
                hooks={
                    "PostToolUse": [HookMatcher(matcher="Edit|Write", hooks=[log_file_change])]
                }
            )
        ):
            if hasattr(message, "result"):
                print(message.result)

    asyncio.run(main())
    ```

    ```typescript TypeScript
    import { query, HookCallback } from "@anthropic-ai/claude-agent-sdk";
    import { appendFileSync } from "fs";

    const logFileChange: HookCallback = async (input) => {
      const filePath = (input as any).tool_input?.file_path ?? "unknown";
      appendFileSync("./audit.log", `${new Date().toISOString()}: modified ${filePath}\n`);
      return {};
    };

    for await (const message of query({
      prompt: "utils.py를 리팩터링해서 가독성을 향상시켜줘",
      options: {
        permissionMode: "acceptEdits",
        hooks: {
          PostToolUse: [{ matcher: "Edit|Write", hooks: [logFileChange] }]
        }
      }
    })) {
      if ("result" in message) console.log(message.result);
    }
    ```
    </CodeGroup>

    [훅에 대해 자세히 알아보기 →](/docs/en/agent-sdk/hooks)
  </Tab>
  <Tab title="서브에이전트">
    특정 하위 작업을 처리할 전문화된 에이전트를 생성합니다. 메인 에이전트가 작업을 위임하고, 서브에이전트가 결과를 보고합니다.

    전문화된 지시사항으로 커스텀 에이전트를 정의하세요. 서브에이전트는 Task 도구를 통해 호출되므로 `allowedTools`에 `Task`를 포함해야 합니다:

    <CodeGroup>
    ```python Python
    import asyncio
    from claude_agent_sdk import query, ClaudeAgentOptions, AgentDefinition

    async def main():
        async for message in query(
            prompt="code-reviewer 에이전트를 사용해서 이 코드베이스를 검토해줘",
            options=ClaudeAgentOptions(
                allowed_tools=["Read", "Glob", "Grep", "Task"],
                agents={
                    "code-reviewer": AgentDefinition(
                        description="품질 및 보안 검토를 위한 전문 코드 리뷰어.",
                        prompt="코드 품질을 분석하고 개선 사항을 제안해줘.",
                        tools=["Read", "Glob", "Grep"]
                    )
                }
            )
        ):
            if hasattr(message, "result"):
                print(message.result)

    asyncio.run(main())
    ```

    ```typescript TypeScript
    import { query } from "@anthropic-ai/claude-agent-sdk";

    for await (const message of query({
      prompt: "code-reviewer 에이전트를 사용해서 이 코드베이스를 검토해줘",
      options: {
        allowedTools: ["Read", "Glob", "Grep", "Task"],
        agents: {
          "code-reviewer": {
            description: "품질 및 보안 검토를 위한 전문 코드 리뷰어.",
            prompt: "코드 품질을 분석하고 개선 사항을 제안해줘.",
            tools: ["Read", "Glob", "Grep"]
          }
        }
      }
    })) {
      if ("result" in message) console.log(message.result);
    }
    ```
    </CodeGroup>

    서브에이전트의 컨텍스트 내에서 발생한 메시지에는 `parent_tool_use_id` 필드가 포함되어 있어, 어떤 메시지가 어떤 서브에이전트 실행에 속하는지 추적할 수 있습니다.

    [서브에이전트에 대해 자세히 알아보기 →](/docs/en/agent-sdk/subagents)
  </Tab>
  <Tab title="MCP">
    Model Context Protocol을 통해 외부 시스템에 연결하세요: 데이터베이스, 브라우저, API 및 [수백 가지 이상](https://github.com/modelcontextprotocol/servers)의 서비스.

    다음 예제는 [Playwright MCP 서버](https://github.com/microsoft/playwright-mcp)를 연결하여 에이전트에 브라우저 자동화 기능을 제공합니다:

    <CodeGroup>
    ```python Python
    import asyncio
    from claude_agent_sdk import query, ClaudeAgentOptions

    async def main():
        async for message in query(
            prompt="example.com을 열고 무엇이 보이는지 설명해줘",
            options=ClaudeAgentOptions(
                mcp_servers={
                    "playwright": {"command": "npx", "args": ["@playwright/mcp@latest"]}
                }
            )
        ):
            if hasattr(message, "result"):
                print(message.result)

    asyncio.run(main())
    ```

    ```typescript TypeScript
    import { query } from "@anthropic-ai/claude-agent-sdk";

    for await (const message of query({
      prompt: "example.com을 열고 무엇이 보이는지 설명해줘",
      options: {
        mcpServers: {
          playwright: { command: "npx", args: ["@playwright/mcp@latest"] }
        }
      }
    })) {
      if ("result" in message) console.log(message.result);
    }
    ```
    </CodeGroup>

    [MCP에 대해 자세히 알아보기 →](/docs/en/agent-sdk/mcp)
  </Tab>
  <Tab title="권한">
    에이전트가 사용할 수 있는 도구를 정확하게 제어합니다. 안전한 작업은 허용하고, 위험한 작업은 차단하거나, 민감한 작업에 대해 승인을 요구할 수 있습니다.

    
> 대화형 승인 프롬프트 및 `AskUserQuestion` 도구에 대해서는 [승인 및 사용자 입력 처리](/docs/en/agent-sdk/user-input)를 참조하세요.


    다음 예제는 코드를 분석할 수 있지만 수정할 수 없는 읽기 전용 에이전트를 생성합니다:

    <CodeGroup>
    ```python Python
    import asyncio
    from claude_agent_sdk import query, ClaudeAgentOptions

    async def main():
        async for message in query(
            prompt="이 코드를 베스트 프랙티스 관점에서 검토해줘",
            options=ClaudeAgentOptions(
                allowed_tools=["Read", "Glob", "Grep"],
                permission_mode="bypassPermissions"
            )
        ):
            if hasattr(message, "result"):
                print(message.result)

    asyncio.run(main())
    ```

    ```typescript TypeScript
    import { query } from "@anthropic-ai/claude-agent-sdk";

    for await (const message of query({
      prompt: "이 코드를 베스트 프랙티스 관점에서 검토해줘",
      options: {
        allowedTools: ["Read", "Glob", "Grep"],
        permissionMode: "bypassPermissions"
      }
    })) {
      if ("result" in message) console.log(message.result);
    }
    ```
    </CodeGroup>

    [권한에 대해 자세히 알아보기 →](/docs/en/agent-sdk/permissions)
  </Tab>
  <Tab title="세션">
    여러 교환에 걸쳐 컨텍스트를 유지합니다. Claude는 읽은 파일, 수행한 분석, 대화 기록을 기억합니다. 나중에 세션을 재개하거나, 포크하여 다른 접근 방식을 탐색할 수 있습니다.

    다음 예제는 첫 번째 쿼리에서 세션 ID를 캡처한 다음, 전체 컨텍스트와 함께 재개합니다:

    <CodeGroup>
    ```python Python
    import asyncio
    from claude_agent_sdk import query, ClaudeAgentOptions

    async def main():
        session_id = None

        # 첫 번째 쿼리: 세션 ID 캡처
        async for message in query(
            prompt="인증 모듈을 읽어줘",
            options=ClaudeAgentOptions(allowed_tools=["Read", "Glob"])
        ):
            if hasattr(message, 'subtype') and message.subtype == 'init':
                session_id = message.session_id

        # 첫 번째 쿼리의 전체 컨텍스트와 함께 재개
        async for message in query(
            prompt="이제 이걸 호출하는 모든 위치를 찾아줘",  # "이걸" = 인증 모듈
            options=ClaudeAgentOptions(resume=session_id)
        ):
            if hasattr(message, "result"):
                print(message.result)

    asyncio.run(main())
    ```

    ```typescript TypeScript
    import { query } from "@anthropic-ai/claude-agent-sdk";

    let sessionId: string | undefined;

    // 첫 번째 쿼리: 세션 ID 캡처
    for await (const message of query({
      prompt: "인증 모듈을 읽어줘",
      options: { allowedTools: ["Read", "Glob"] }
    })) {
      if (message.type === "system" && message.subtype === "init") {
        sessionId = message.session_id;
      }
    }

    // 첫 번째 쿼리의 전체 컨텍스트와 함께 재개
    for await (const message of query({
      prompt: "이제 이걸 호출하는 모든 위치를 찾아줘",  // "이걸" = 인증 모듈
      options: { resume: sessionId }
    })) {
      if ("result" in message) console.log(message.result);
    }
    ```
    </CodeGroup>

    [세션에 대해 자세히 알아보기 →](/docs/en/agent-sdk/sessions)
  </Tab>
</Tabs>

### Claude Code 기능

SDK는 Claude Code의 파일 시스템 기반 구성도 지원합니다. 이러한 기능을 사용하려면 옵션에서 `setting_sources=["project"]` (Python) 또는 `settingSources: ['project']` (TypeScript)를 설정하세요.

| 기능 | 설명 | 위치 |
|------|------|------|
| [스킬](/docs/en/agent-sdk/skills) | 마크다운으로 정의된 전문화된 기능 | `.claude/skills/SKILL.md` |
| [슬래시 커맨드](/docs/en/agent-sdk/slash-commands) | 일반적인 작업을 위한 커스텀 명령 | `.claude/commands/*.md` |
| [메모리](/docs/en/agent-sdk/modifying-system-prompts) | 프로젝트 컨텍스트 및 지시사항 | `CLAUDE.md` 또는 `.claude/CLAUDE.md` |
| [플러그인](/docs/en/agent-sdk/plugins) | 커스텀 명령, 에이전트, MCP 서버로 확장 | `plugins` 옵션을 통한 프로그래밍 방식 |

## 시작하기

<Steps>
  <Step title="Claude Code 설치">
    SDK는 Claude Code를 런타임으로 사용합니다:

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

    Windows 및 기타 옵션은 [Claude Code 설정](https://code.claude.com/docs/en/setup)을 참조하세요.
  </Step>
  <Step title="SDK 설치">
    <Tabs>
      <Tab title="TypeScript">
        ```bash
        npm install @anthropic-ai/claude-agent-sdk
        ```
      </Tab>
      <Tab title="Python">
        ```bash
        pip install claude-agent-sdk
        ```
      </Tab>
    </Tabs>
  </Step>
  <Step title="API 키 설정">
    ```bash
    export ANTHROPIC_API_KEY=your-api-key
    ```
    [콘솔](https://platform.claude.com/)에서 키를 받으세요.

    SDK는 서드파티 API 제공업체를 통한 인증도 지원합니다:

    - **Amazon Bedrock**: `CLAUDE_CODE_USE_BEDROCK=1` 환경 변수를 설정하고 AWS 자격 증명 구성
    - **Google Vertex AI**: `CLAUDE_CODE_USE_VERTEX=1` 환경 변수를 설정하고 Google Cloud 자격 증명 구성
    - **Microsoft Foundry**: `CLAUDE_CODE_USE_FOUNDRY=1` 환경 변수를 설정하고 Azure 자격 증명 구성

    
> 사전 승인이 없는 경우, 서드파티 개발자가 Claude Agent SDK로 구축된 에이전트를 포함하여 자신의 제품에 Claude.ai 로그인 또는 속도 제한을 제공하는 것을 허용하지 않습니다. 대신 이 문서에 설명된 API 키 인증 방법을 사용하세요.

  </Step>
  <Step title="첫 번째 에이전트 실행">
    다음 예제는 내장 도구를 사용하여 현재 디렉터리의 파일 목록을 나열하는 에이전트를 생성합니다.

    <CodeGroup>
    ```python Python
    import asyncio
    from claude_agent_sdk import query, ClaudeAgentOptions

    async def main():
        async for message in query(
            prompt="이 디렉터리에 어떤 파일들이 있어?",
            options=ClaudeAgentOptions(allowed_tools=["Bash", "Glob"])
        ):
            if hasattr(message, "result"):
                print(message.result)

    asyncio.run(main())
    ```

    ```typescript TypeScript
    import { query } from "@anthropic-ai/claude-agent-sdk";

    for await (const message of query({
      prompt: "이 디렉터리에 어떤 파일들이 있어?",
      options: { allowedTools: ["Bash", "Glob"] },
    })) {
      if ("result" in message) console.log(message.result);
    }
    ```
    </CodeGroup>
  </Step>
</Steps>

**빌드 준비 완료?** [퀵스타트](../05-agent-sdk/02-quickstart.md)를 따라 몇 분 안에 버그를 찾아 수정하는 에이전트를 만들어보세요.

## Agent SDK와 다른 Claude 도구 비교

Claude 플랫폼은 Claude로 빌드하는 여러 방법을 제공합니다. Agent SDK가 어떻게 적용되는지 살펴보세요:

<Tabs>
  <Tab title="Agent SDK vs Client SDK">
    [Anthropic Client SDK](/docs/en/api/client-sdks)는 직접 API 액세스를 제공합니다: 프롬프트를 보내고 도구 실행을 직접 구현합니다. **Agent SDK**는 내장 도구 실행이 포함된 Claude를 제공합니다.

    Client SDK로는 도구 루프를 구현해야 합니다. Agent SDK로는 Claude가 처리합니다:

    <CodeGroup>
    ```python Python
    # Client SDK: 도구 루프를 직접 구현
    response = client.messages.create(...)
    while response.stop_reason == "tool_use":
        result = your_tool_executor(response.tool_use)
        response = client.messages.create(tool_result=result, ...)

    # Agent SDK: Claude가 자율적으로 도구를 처리
    async for message in query(prompt="auth.py의 버그를 수정해줘"):
        print(message)
    ```

    ```typescript TypeScript
    // Client SDK: 도구 루프를 직접 구현
    let response = await client.messages.create({...});
    while (response.stop_reason === "tool_use") {
      const result = yourToolExecutor(response.tool_use);
      response = await client.messages.create({ tool_result: result, ... });
    }

    // Agent SDK: Claude가 자율적으로 도구를 처리
    for await (const message of query({ prompt: "auth.py의 버그를 수정해줘" })) {
      console.log(message);
    }
    ```
    </CodeGroup>
  </Tab>
  <Tab title="Agent SDK vs Claude Code CLI">
    동일한 기능, 다른 인터페이스:

    | 사용 사례 | 최선의 선택 |
    |-----------|-------------|
    | 대화형 개발 | CLI |
    | CI/CD 파이프라인 | SDK |
    | 커스텀 애플리케이션 | SDK |
    | 일회성 작업 | CLI |
    | 프로덕션 자동화 | SDK |

    많은 팀이 두 가지를 모두 사용합니다: 일상적인 개발에는 CLI, 프로덕션에는 SDK. 워크플로우는 두 가지 간에 직접 전환됩니다.
  </Tab>
</Tabs>

## 변경 로그

SDK 업데이트, 버그 수정, 새로운 기능에 대한 전체 변경 로그를 확인하세요:

- **TypeScript SDK**: [CHANGELOG.md 보기](https://github.com/anthropics/claude-agent-sdk-typescript/blob/main/CHANGELOG.md)
- **Python SDK**: [CHANGELOG.md 보기](https://github.com/anthropics/claude-agent-sdk-python/blob/main/CHANGELOG.md)

## 버그 보고

Agent SDK에서 버그나 문제가 발생한 경우:

- **TypeScript SDK**: [GitHub에서 이슈 보고](https://github.com/anthropics/claude-agent-sdk-typescript/issues)
- **Python SDK**: [GitHub에서 이슈 보고](https://github.com/anthropics/claude-agent-sdk-python/issues)

## 브랜딩 가이드라인

Claude Agent SDK를 통합하는 파트너의 경우, Claude 브랜딩 사용은 선택 사항입니다. 제품에서 Claude를 참조할 때:

**허용:**
- "Claude Agent" (드롭다운 메뉴에 권장)
- "Claude" (이미 "Agents"로 레이블이 지정된 메뉴 내에서)
- "{YourAgentName} Powered by Claude" (기존 에이전트 이름이 있는 경우)

**허용되지 않음:**
- "Claude Code" 또는 "Claude Code Agent"
- Claude Code를 모방한 Claude Code 브랜드 ASCII 아트 또는 시각적 요소

귀하의 제품은 자체 브랜딩을 유지해야 하며 Claude Code나 Anthropic 제품처럼 보여서는 안 됩니다. 브랜딩 규정 준수에 대한 질문은 [영업팀](https://www.anthropic.com/contact-sales)에 문의하세요.

## 라이선스 및 이용 약관

Claude Agent SDK의 사용은 [Anthropic의 상업 이용 약관](https://www.anthropic.com/legal/commercial-terms)의 적용을 받으며, 이는 귀하가 자체 고객과 최종 사용자에게 제공하는 제품 및 서비스를 구동하는 데 사용하는 경우를 포함합니다. 단, 특정 구성 요소 또는 종속성이 해당 구성 요소의 LICENSE 파일에 명시된 대로 다른 라이선스의 적용을 받는 경우는 제외됩니다.

## 다음 단계


  
> 몇 분 안에 버그를 찾아 수정하는 에이전트 구축하기

  
> 이메일 어시스턴트, 리서치 에이전트 등

  
> 전체 TypeScript API 레퍼런스 및 예제

  
> 전체 Python API 레퍼런스 및 예제



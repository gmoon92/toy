# [Claude Agent SDK로 마이그레이션](https://platform.claude.com/docs/en/agent-sdk/migration-guide)

Claude Code TypeScript 및 Python SDK를 Claude Agent SDK로 마이그레이션하기 위한 가이드

---

## 개요

Claude Code SDK가 **Claude Agent SDK**로 이름이 변경되었으며 문서가 재구성되었습니다. 
이 변경은 단순한 코딩 작업을 넘어 AI 에이전트를 구축하기 위한 SDK의 광범위한 기능을 반영합니다.

## 변경 사항

| 항목                 | 이전                          | 변경 후                             |
|:-------------------|:----------------------------|:---------------------------------|
| **패키지 이름 (TS/JS)** | `@anthropic-ai/claude-code` | `@anthropic-ai/claude-agent-sdk` |
| **Python 패키지**     | `claude-code-sdk`           | `claude-agent-sdk`               |
| **문서 위치**          | Claude Code 문서              | API 가이드 → Agent SDK 섹션           |

> **문서 변경 사항:** Agent SDK 문서가 Claude Code 문서에서 API 가이드의 전용 [Agent SDK](../05-agent-sdk/01-overview.md) 섹션으로 이동되었습니다.
> Claude Code 문서는 이제 CLI 도구 및 자동화 기능에 중점을 둡니다.

## 마이그레이션 단계

### TypeScript/JavaScript 프로젝트의 경우

**1. 기존 패키지를 제거합니다:**

```bash
npm uninstall @anthropic-ai/claude-code
```

**2. 새 패키지를 설치합니다:**

```bash
npm install @anthropic-ai/claude-agent-sdk
```

**3. import 문을 업데이트합니다:**

모든 import를 `@anthropic-ai/claude-code`에서 `@anthropic-ai/claude-agent-sdk`로 변경합니다:

```typescript
// 이전
import { query, tool, createSdkMcpServer } from "@anthropic-ai/claude-code";

// 변경 후
import {
  query,
  tool,
  createSdkMcpServer,
} from "@anthropic-ai/claude-agent-sdk";
```

**4. package.json 의존성을 업데이트합니다:**

`package.json`에 패키지가 나열되어 있는 경우 업데이트합니다:

```json
// 이전
{
  "dependencies": {
    "@anthropic-ai/claude-code": "^1.0.0"
  }
}

// 변경 후
{
  "dependencies": {
    "@anthropic-ai/claude-agent-sdk": "^0.1.0"
  }
}
```

이것으로 끝입니다! 다른 코드 변경은 필요하지 않습니다.

### Python 프로젝트의 경우

**1. 기존 패키지를 제거합니다:**

```bash
pip uninstall claude-code-sdk
```

**2. 새 패키지를 설치합니다:**

```bash
pip install claude-agent-sdk
```

**3. import 문을 업데이트합니다:**

모든 import를 `claude_code_sdk`에서 `claude_agent_sdk`로 변경합니다:

```python
# 이전
from claude_code_sdk import query, ClaudeCodeOptions

# 변경 후
from claude_agent_sdk import query, ClaudeAgentOptions
```

**4. 타입 이름을 업데이트합니다:**

`ClaudeCodeOptions`를 `ClaudeAgentOptions`로 변경합니다:

```python
# 이전
from claude_agent_sdk import query, ClaudeCodeOptions

options = ClaudeCodeOptions(
    model="claude-sonnet-4-5"
)

# 변경 후
from claude_agent_sdk import query, ClaudeAgentOptions

options = ClaudeAgentOptions(
    model="claude-sonnet-4-5"
)
```

**5. [주요 변경 사항](#주요-변경-사항)을 검토합니다**

마이그레이션을 완료하는 데 필요한 코드 변경 사항을 적용합니다.

## 주요 변경 사항

> 격리 및 명시적 구성을 개선하기 위해 Claude Agent SDK v0.1.0은 Claude Code SDK에서 마이그레이션하는 사용자에게 주요 변경 사항을 도입합니다. 마이그레이션하기 전에 이 섹션을 주의
> 깊게 검토하세요.

### Python: ClaudeCodeOptions가 ClaudeAgentOptions로 이름 변경

**변경 내용:** Python SDK 타입 `ClaudeCodeOptions`가 `ClaudeAgentOptions`로 이름이 변경되었습니다.

**마이그레이션:**

```python
# 이전 (v0.0.x)
from claude_agent_sdk import query, ClaudeCodeOptions

options = ClaudeCodeOptions(
    model="claude-sonnet-4-5",
    permission_mode="acceptEdits"
)

# 변경 후 (v0.1.0)
from claude_agent_sdk import query, ClaudeAgentOptions

options = ClaudeAgentOptions(
    model="claude-sonnet-4-5",
    permission_mode="acceptEdits"
)
```

**변경 이유:** 타입 이름이 이제 "Claude Agent SDK" 브랜딩과 일치하며 SDK의 명명 규칙 전반에 걸쳐 일관성을 제공합니다.

### 시스템 프롬프트가 더 이상 기본값이 아님

**변경 내용:** SDK는 더 이상 기본적으로 Claude Code의 시스템 프롬프트를 사용하지 않습니다.

**마이그레이션:**

<details>
<summary>Python 예시</summary>

```python
# 이전 (v0.0.x) - 기본적으로 Claude Code의 시스템 프롬프트 사용
async for message in query(prompt="Hello"):
    print(message)

# 변경 후 (v0.1.0) - 기본적으로 최소한의 시스템 프롬프트 사용
# 이전 동작을 원하면 명시적으로 Claude Code의 프리셋을 요청:
from claude_agent_sdk import query, ClaudeAgentOptions

async for message in query(
    prompt="Hello",
    options=ClaudeAgentOptions(
        system_prompt={"type": "preset", "preset": "claude_code"}  # 프리셋 사용
    )
):
    print(message)

# 또는 커스텀 시스템 프롬프트 사용:
async for message in query(
    prompt="Hello",
    options=ClaudeAgentOptions(
        system_prompt="You are a helpful coding assistant"
    )
):
    print(message)
```

</details>

**변경 이유:** SDK 애플리케이션에 대한 더 나은 제어 및 격리를 제공합니다. 이제 Claude Code의 CLI 중심 지시사항을 상속하지 않고 커스텀 동작으로 에이전트를 구축할 수 있습니다.

### 설정 소스가 더 이상 기본적으로 로드되지 않음

**변경 내용:** SDK는 더 이상 기본적으로 파일시스템 설정(CLAUDE.md, settings.json, 슬래시 명령 등)을 읽지 않습니다.

**마이그레이션:**

<details>
<summary>Python 예시</summary>

```python
# 이전 (v0.0.x) - 모든 설정을 자동으로 로드
async for message in query(prompt="Hello"):
    print(message)
# 다음에서 읽음:
# - ~/.claude/settings.json (user)
# - .claude/settings.json (project)
# - .claude/settings.local.json (local)
# - CLAUDE.md 파일
# - 커스텀 슬래시 명령

# 변경 후 (v0.1.0) - 기본적으로 설정이 로드되지 않음
# 이전 동작을 원하면:
from claude_agent_sdk import query, ClaudeAgentOptions

async for message in query(
    prompt="Hello",
    options=ClaudeAgentOptions(
        setting_sources=["user", "project", "local"]
    )
):
    print(message)

# 또는 특정 소스만 로드:
async for message in query(
    prompt="Hello",
    options=ClaudeAgentOptions(
        setting_sources=["project"]  # 프로젝트 설정만
    )
):
    print(message)
```

</details>

**변경 이유:** SDK 애플리케이션이 로컬 파일시스템 구성과 독립적으로 예측 가능한 동작을 보장합니다. 이는 특히 다음과 같은 경우에 중요합니다:

- **CI/CD 환경** - 로컬 커스터마이징 없이 일관된 동작
- **배포된 애플리케이션** - 파일시스템 설정에 대한 의존성 없음
- **테스트** - 격리된 테스트 환경
- **멀티 테넌트 시스템** - 사용자 간 설정 누출 방지

> **하위 호환성:** 애플리케이션이 파일시스템 설정(커스텀 슬래시 명령, CLAUDE.md 지시사항 등)에 의존했다면 옵션에 `settingSources: ['user', 'project', 'local']`을
> 추가하세요.

## 이름 변경 이유

Claude Code SDK는 원래 코딩 작업을 위해 설계되었지만, 모든 유형의 AI 에이전트를 구축하기 위한 강력한 프레임워크로 발전했습니다. 
새로운 이름 "Claude Agent SDK"는 다음과 같은 기능을 더 잘 반영합니다:

- 비즈니스 에이전트 구축(법률 보조, 재무 고문, 고객 지원)
- 전문 코딩 에이전트 생성(SRE 봇, 보안 검토자, 코드 리뷰 에이전트)
- 도구 사용, MCP 통합 등을 통한 모든 도메인의 커스텀 에이전트 개발

## 도움 받기

마이그레이션 중 문제가 발생하는 경우:

**TypeScript/JavaScript의 경우:**

1. 모든 import가 `@anthropic-ai/claude-agent-sdk`를 사용하도록 업데이트되었는지 확인
2. package.json에 새 패키지 이름이 있는지 확인
3. `npm install`을 실행하여 의존성이 업데이트되었는지 확인

**Python의 경우:**

1. 모든 import가 `claude_agent_sdk`를 사용하도록 업데이트되었는지 확인
2. requirements.txt 또는 pyproject.toml에 새 패키지 이름이 있는지 확인
3. `pip install claude-agent-sdk`를 실행하여 패키지가 설치되었는지 확인

## 다음 단계

- [Agent SDK 개요](../05-agent-sdk/01-overview.md)에서 사용 가능한 기능 살펴보기
- [TypeScript SDK 레퍼런스](../05-agent-sdk/03-typescript.md)에서 상세한 API 문서 확인
- [Python SDK 레퍼런스](../05-agent-sdk/05-python.md)에서 Python 관련 문서 확인
- [커스텀 도구](https://platform.claude.com/docs/en/agent-sdk/custom-tools)
  및 [MCP 통합](https://platform.claude.com/docs/en/agent-sdk/mcp)에 대해 학습

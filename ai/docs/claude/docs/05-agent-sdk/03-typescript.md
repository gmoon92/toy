# [Agent SDK 레퍼런스 - TypeScript](https://platform.claude.com/docs/en/agent-sdk/typescript)

TypeScript Agent SDK의 완전한 API 레퍼런스로, 모든 함수, 타입, 인터페이스를 포함합니다.

---


> **새로운 V2 인터페이스 체험하기 (프리뷰):** 이제 `send()`와 `receive()` 패턴을 사용하는 간소화된 인터페이스를 사용할 수 있으며, 이를 통해 다중 턴 대화가 더 쉬워졌습니다. [TypeScript V2 프리뷰에 대해 자세히 알아보기](../05-agent-sdk/04-typescript-v2-preview.md)


## 설치

```bash
npm install @anthropic-ai/claude-agent-sdk
```

## 함수

### `query()`

Claude Code와 상호작용하기 위한 주요 함수입니다. 메시지가 도착하면 스트리밍하는 async generator를 생성합니다.

```typescript
function query({
  prompt,
  options
}: {
  prompt: string | AsyncIterable<SDKUserMessage>;
  options?: Options;
}): Query
```

#### 파라미터

| 파라미터 | 타입 | 설명 |
| :-------- | :--- | :---------- |
| `prompt` | `string \| AsyncIterable<`[`SDKUserMessage`](#sdkusermessage)`>` | 입력 프롬프트(문자열) 또는 스트리밍 모드를 위한 async iterable |
| `options` | [`Options`](#options) | 선택적 설정 객체(아래 Options 타입 참조) |

#### 반환값

[`Query`](#query-1) 객체를 반환하며, 이는 `AsyncGenerator<`[`SDKMessage`](#sdkmessage)`, void>`를 확장하여 추가 메서드를 제공합니다.

### `tool()`

SDK MCP 서버와 함께 사용할 타입 안전한 MCP 도구 정의를 생성합니다.

```typescript
function tool<Schema extends ZodRawShape>(
  name: string,
  description: string,
  inputSchema: Schema,
  handler: (args: z.infer<ZodObject<Schema>>, extra: unknown) => Promise<CallToolResult>
): SdkMcpToolDefinition<Schema>
```

#### 파라미터

| 파라미터 | 타입 | 설명 |
| :-------- | :--- | :---------- |
| `name` | `string` | 도구의 이름 |
| `description` | `string` | 도구의 기능 설명 |
| `inputSchema` | `Schema extends ZodRawShape` | 도구의 입력 파라미터를 정의하는 Zod 스키마 |
| `handler` | `(args, extra) => Promise<`[`CallToolResult`](#calltoolresult)`>` | 도구 로직을 실행하는 비동기 함수 |

### `createSdkMcpServer()`

애플리케이션과 동일한 프로세스에서 실행되는 MCP 서버 인스턴스를 생성합니다.

```typescript
function createSdkMcpServer(options: {
  name: string;
  version?: string;
  tools?: Array<SdkMcpToolDefinition<any>>;
}): McpSdkServerConfigWithInstance
```

#### 파라미터

| 파라미터 | 타입 | 설명 |
| :-------- | :--- | :---------- |
| `options.name` | `string` | MCP 서버의 이름 |
| `options.version` | `string` | 선택적 버전 문자열 |
| `options.tools` | `Array<SdkMcpToolDefinition>` | [`tool()`](#tool)로 생성된 도구 정의 배열 |

## 타입

### `Options`

`query()` 함수에 대한 설정 객체입니다.

| 속성 | 타입 | 기본값 | 설명 |
| :------- | :--- | :------ | :---------- |
| `abortController` | `AbortController` | `new AbortController()` | 작업 취소를 위한 컨트롤러 |
| `additionalDirectories` | `string[]` | `[]` | Claude가 접근할 수 있는 추가 디렉터리 |
| `agents` | `Record<string, [`AgentDefinition`](#agentdefinition)>` | `undefined` | 프로그래밍 방식으로 서브에이전트 정의 |
| `allowDangerouslySkipPermissions` | `boolean` | `false` | 권한 우회 활성화. `permissionMode: 'bypassPermissions'` 사용 시 필요 |
| `allowedTools` | `string[]` | 모든 도구 | 허용된 도구 이름 목록 |
| `betas` | [`SdkBeta`](#sdkbeta)`[]` | `[]` | 베타 기능 활성화(예: `['context-1m-2025-08-07']`) |
| `canUseTool` | [`CanUseTool`](#canusetool) | `undefined` | 도구 사용을 위한 사용자 정의 권한 함수 |
| `continue` | `boolean` | `false` | 가장 최근 대화 계속하기 |
| `cwd` | `string` | `process.cwd()` | 현재 작업 디렉터리 |
| `disallowedTools` | `string[]` | `[]` | 허용되지 않는 도구 이름 목록 |
| `enableFileCheckpointing` | `boolean` | `false` | 되감기를 위한 파일 변경 추적 활성화. [파일 체크포인팅](https://platform.claude.com/docs/en/agent-sdk/file-checkpointing) 참조 |
| `env` | `Dict<string>` | `process.env` | 환경 변수 |
| `executable` | `'bun' \| 'deno' \| 'node'` | 자동 감지 | 사용할 JavaScript 런타임 |
| `executableArgs` | `string[]` | `[]` | 실행 파일에 전달할 인수 |
| `extraArgs` | `Record<string, string \| null>` | `{}` | 추가 인수 |
| `fallbackModel` | `string` | `undefined` | 주 모델이 실패할 경우 사용할 모델 |
| `forkSession` | `boolean` | `false` | `resume`으로 재개할 때, 원래 세션을 계속하는 대신 새 세션 ID로 분기 |
| `hooks` | `Partial<Record<`[`HookEvent`](#hookevent)`, `[`HookCallbackMatcher`](#hookcallbackmatcher)`[]>>` | `{}` | 이벤트에 대한 훅 콜백 |
| `includePartialMessages` | `boolean` | `false` | 부분 메시지 이벤트 포함 |
| `maxBudgetUsd` | `number` | `undefined` | 쿼리의 최대 예산(USD) |
| `maxThinkingTokens` | `number` | `undefined` | 사고 프로세스의 최대 토큰 수 |
| `maxTurns` | `number` | `undefined` | 최대 대화 턴 수 |
| `mcpServers` | `Record<string, [`McpServerConfig`](#mcpserverconfig)>` | `{}` | MCP 서버 설정 |
| `model` | `string` | CLI 기본값 | 사용할 Claude 모델 |
| `outputFormat` | `{ type: 'json_schema', schema: JSONSchema }` | `undefined` | 에이전트 결과의 출력 형식 정의. 자세한 내용은 [구조화된 출력](https://platform.claude.com/docs/en/agent-sdk/structured-outputs) 참조 |
| `pathToClaudeCodeExecutable` | `string` | 내장 실행 파일 사용 | Claude Code 실행 파일 경로 |
| `permissionMode` | [`PermissionMode`](#permissionmode) | `'default'` | 세션의 권한 모드 |
| `permissionPromptToolName` | `string` | `undefined` | 권한 프롬프트를 위한 MCP 도구 이름 |
| `plugins` | [`SdkPluginConfig`](#sdkpluginconfig)`[]` | `[]` | 로컬 경로에서 사용자 정의 플러그인 로드. 자세한 내용은 [플러그인](https://platform.claude.com/docs/en/agent-sdk/plugins) 참조 |
| `resume` | `string` | `undefined` | 재개할 세션 ID |
| `resumeSessionAt` | `string` | `undefined` | 특정 메시지 UUID에서 세션 재개 |
| `sandbox` | [`SandboxSettings`](#sandboxsettings) | `undefined` | 프로그래밍 방식으로 샌드박스 동작 구성. 자세한 내용은 [샌드박스 설정](#sandboxsettings) 참조 |
| `settingSources` | [`SettingSource`](#settingsource)`[]` | `[]` (설정 없음) | 로드할 파일 시스템 설정 제어. 생략하면 설정이 로드되지 않음. **참고:** CLAUDE.md 파일을 로드하려면 `'project'`를 포함해야 함 |
| `stderr` | `(data: string) => void` | `undefined` | stderr 출력 콜백 |
| `strictMcpConfig` | `boolean` | `false` | 엄격한 MCP 검증 시행 |
| `systemPrompt` | `string \| { type: 'preset'; preset: 'claude_code'; append?: string }` | `undefined` (최소 프롬프트) | 시스템 프롬프트 설정. 사용자 정의 프롬프트는 문자열로 전달하거나, Claude Code의 시스템 프롬프트를 사용하려면 `{ type: 'preset', preset: 'claude_code' }`를 사용. 프리셋 객체 형식 사용 시, 추가 지침으로 시스템 프롬프트를 확장하려면 `append` 추가 |
| `tools` | `string[] \| { type: 'preset'; preset: 'claude_code' }` | `undefined` | 도구 설정. 도구 이름 배열을 전달하거나 프리셋을 사용하여 Claude Code의 기본 도구 가져오기 |

### `Query`

`query()` 함수가 반환하는 인터페이스입니다.

```typescript
interface Query extends AsyncGenerator<SDKMessage, void> {
  interrupt(): Promise<void>;
  rewindFiles(userMessageUuid: string): Promise<void>;
  setPermissionMode(mode: PermissionMode): Promise<void>;
  setModel(model?: string): Promise<void>;
  setMaxThinkingTokens(maxThinkingTokens: number | null): Promise<void>;
  supportedCommands(): Promise<SlashCommand[]>;
  supportedModels(): Promise<ModelInfo[]>;
  mcpServerStatus(): Promise<McpServerStatus[]>;
  accountInfo(): Promise<AccountInfo>;
}
```

#### 메서드

| 메서드 | 설명 |
| :----- | :---------- |
| `interrupt()` | 쿼리 중단(스트리밍 입력 모드에서만 사용 가능) |
| `rewindFiles(userMessageUuid)` | 지정된 사용자 메시지의 상태로 파일 복원. `enableFileCheckpointing: true` 필요. [파일 체크포인팅](https://platform.claude.com/docs/en/agent-sdk/file-checkpointing) 참조 |
| `setPermissionMode()` | 권한 모드 변경(스트리밍 입력 모드에서만 사용 가능) |
| `setModel()` | 모델 변경(스트리밍 입력 모드에서만 사용 가능) |
| `setMaxThinkingTokens()` | 최대 사고 토큰 수 변경(스트리밍 입력 모드에서만 사용 가능) |
| `supportedCommands()` | 사용 가능한 슬래시 명령 반환 |
| `supportedModels()` | 표시 정보가 포함된 사용 가능한 모델 반환 |
| `mcpServerStatus()` | 연결된 MCP 서버의 상태 반환 |
| `accountInfo()` | 계정 정보 반환 |

### `AgentDefinition`

프로그래밍 방식으로 정의된 서브에이전트에 대한 설정입니다.

```typescript
type AgentDefinition = {
  description: string;
  tools?: string[];
  prompt: string;
  model?: 'sonnet' | 'opus' | 'haiku' | 'inherit';
}
```

| 필드 | 필수 여부 | 설명 |
|:------|:---------|:------------|
| `description` | 예 | 이 에이전트를 사용할 시점에 대한 자연어 설명 |
| `tools` | 아니오 | 허용된 도구 이름 배열. 생략하면 모든 도구 상속 |
| `prompt` | 예 | 에이전트의 시스템 프롬프트 |
| `model` | 아니오 | 이 에이전트에 대한 모델 재정의. 생략하면 주 모델 사용 |

### `SettingSource`

SDK가 설정을 로드하는 파일 시스템 기반 설정 소스를 제어합니다.

```typescript
type SettingSource = 'user' | 'project' | 'local';
```

| 값 | 설명 | 위치 |
|:------|:------------|:---------|
| `'user'` | 전역 사용자 설정 | `~/.claude/settings.json` |
| `'project'` | 공유 프로젝트 설정(버전 제어됨) | `.claude/settings.json` |
| `'local'` | 로컬 프로젝트 설정(gitignored) | `.claude/settings.local.json` |

#### 기본 동작

`settingSources`가 **생략**되거나 **undefined**인 경우, SDK는 파일 시스템 설정을 로드하지 **않습니다**. 이는 SDK 애플리케이션에 격리를 제공합니다.

#### settingSources를 사용하는 이유는?

**모든 파일 시스템 설정 로드(레거시 동작):**
```typescript
// SDK v0.0.x처럼 모든 설정 로드
const result = query({
  prompt: "Analyze this code",
  options: {
    settingSources: ['user', 'project', 'local']  // 모든 설정 로드
  }
});
```

**특정 설정 소스만 로드:**
```typescript
// 프로젝트 설정만 로드, 사용자 및 로컬 설정 무시
const result = query({
  prompt: "Run CI checks",
  options: {
    settingSources: ['project']  // .claude/settings.json만
  }
});
```

**테스트 및 CI 환경:**
```typescript
// 로컬 설정을 제외하여 CI에서 일관된 동작 보장
const result = query({
  prompt: "Run tests",
  options: {
    settingSources: ['project'],  // 팀 공유 설정만
    permissionMode: 'bypassPermissions'
  }
});
```

**SDK 전용 애플리케이션:**
```typescript
// 모든 것을 프로그래밍 방식으로 정의(기본 동작)
// 파일 시스템 종속성 없음 - settingSources 기본값은 []
const result = query({
  prompt: "Review this PR",
  options: {
    // settingSources: []가 기본값, 지정할 필요 없음
    agents: { /* ... */ },
    mcpServers: { /* ... */ },
    allowedTools: ['Read', 'Grep', 'Glob']
  }
});
```

**CLAUDE.md 프로젝트 지침 로드:**
```typescript
// CLAUDE.md 파일을 포함하려면 프로젝트 설정 로드
const result = query({
  prompt: "Add a new feature following project conventions",
  options: {
    systemPrompt: {
      type: 'preset',
      preset: 'claude_code'  // CLAUDE.md 사용에 필요
    },
    settingSources: ['project'],  // 프로젝트 디렉터리에서 CLAUDE.md 로드
    allowedTools: ['Read', 'Write', 'Edit']
  }
});
```

#### 설정 우선순위

여러 소스가 로드될 때, 설정은 다음 우선순위로 병합됩니다(높음에서 낮음 순):
1. 로컬 설정(`.claude/settings.local.json`)
2. 프로젝트 설정(`.claude/settings.json`)
3. 사용자 설정(`~/.claude/settings.json`)

프로그래밍 방식의 옵션(`agents`, `allowedTools` 등)은 항상 파일 시스템 설정을 재정의합니다.

### `PermissionMode`

```typescript
type PermissionMode =
  | 'default'           // 표준 권한 동작
  | 'acceptEdits'       // 파일 편집 자동 승인
  | 'bypassPermissions' // 모든 권한 검사 우회
  | 'plan'              // 계획 모드 - 실행 없음
```

### `CanUseTool`

도구 사용을 제어하기 위한 사용자 정의 권한 함수 타입입니다.

```typescript
type CanUseTool = (
  toolName: string,
  input: ToolInput,
  options: {
    signal: AbortSignal;
    suggestions?: PermissionUpdate[];
  }
) => Promise<PermissionResult>;
```

### `PermissionResult`

권한 검사의 결과입니다.

```typescript
type PermissionResult =
  | {
      behavior: 'allow';
      updatedInput: ToolInput;
      updatedPermissions?: PermissionUpdate[];
    }
  | {
      behavior: 'deny';
      message: string;
      interrupt?: boolean;
    }
```

### `McpServerConfig`

MCP 서버에 대한 설정입니다.

```typescript
type McpServerConfig =
  | McpStdioServerConfig
  | McpSSEServerConfig
  | McpHttpServerConfig
  | McpSdkServerConfigWithInstance;
```

#### `McpStdioServerConfig`

```typescript
type McpStdioServerConfig = {
  type?: 'stdio';
  command: string;
  args?: string[];
  env?: Record<string, string>;
}
```

#### `McpSSEServerConfig`

```typescript
type McpSSEServerConfig = {
  type: 'sse';
  url: string;
  headers?: Record<string, string>;
}
```

#### `McpHttpServerConfig`

```typescript
type McpHttpServerConfig = {
  type: 'http';
  url: string;
  headers?: Record<string, string>;
}
```

#### `McpSdkServerConfigWithInstance`

```typescript
type McpSdkServerConfigWithInstance = {
  type: 'sdk';
  name: string;
  instance: McpServer;
}
```

### `SdkPluginConfig`

SDK에서 플러그인을 로드하기 위한 설정입니다.

```typescript
type SdkPluginConfig = {
  type: 'local';
  path: string;
}
```

| 필드 | 타입 | 설명 |
|:------|:-----|:------------|
| `type` | `'local'` | `'local'`이어야 함(현재 로컬 플러그인만 지원) |
| `path` | `string` | 플러그인 디렉터리의 절대 또는 상대 경로 |

**예제:**
```typescript
plugins: [
  { type: 'local', path: './my-plugin' },
  { type: 'local', path: '/absolute/path/to/plugin' }
]
```

플러그인 생성 및 사용에 대한 완전한 정보는 [플러그인](https://platform.claude.com/docs/en/agent-sdk/plugins)을 참조하세요.

## 메시지 타입

### `SDKMessage`

쿼리에서 반환될 수 있는 모든 메시지의 유니온 타입입니다.

```typescript
type SDKMessage =
  | SDKAssistantMessage
  | SDKUserMessage
  | SDKUserMessageReplay
  | SDKResultMessage
  | SDKSystemMessage
  | SDKPartialAssistantMessage
  | SDKCompactBoundaryMessage;
```

### `SDKAssistantMessage`

어시스턴트 응답 메시지입니다.

```typescript
type SDKAssistantMessage = {
  type: 'assistant';
  uuid: UUID;
  session_id: string;
  message: APIAssistantMessage; // Anthropic SDK에서
  parent_tool_use_id: string | null;
}
```

### `SDKUserMessage`

사용자 입력 메시지입니다.

```typescript
type SDKUserMessage = {
  type: 'user';
  uuid?: UUID;
  session_id: string;
  message: APIUserMessage; // Anthropic SDK에서
  parent_tool_use_id: string | null;
}
```

### `SDKUserMessageReplay`

필수 UUID가 있는 재생된 사용자 메시지입니다.

```typescript
type SDKUserMessageReplay = {
  type: 'user';
  uuid: UUID;
  session_id: string;
  message: APIUserMessage;
  parent_tool_use_id: string | null;
}
```

### `SDKResultMessage`

최종 결과 메시지입니다.

```typescript
type SDKResultMessage =
  | {
      type: 'result';
      subtype: 'success';
      uuid: UUID;
      session_id: string;
      duration_ms: number;
      duration_api_ms: number;
      is_error: boolean;
      num_turns: number;
      result: string;
      total_cost_usd: number;
      usage: NonNullableUsage;
      modelUsage: { [modelName: string]: ModelUsage };
      permission_denials: SDKPermissionDenial[];
      structured_output?: unknown;
    }
  | {
      type: 'result';
      subtype:
        | 'error_max_turns'
        | 'error_during_execution'
        | 'error_max_budget_usd'
        | 'error_max_structured_output_retries';
      uuid: UUID;
      session_id: string;
      duration_ms: number;
      duration_api_ms: number;
      is_error: boolean;
      num_turns: number;
      total_cost_usd: number;
      usage: NonNullableUsage;
      modelUsage: { [modelName: string]: ModelUsage };
      permission_denials: SDKPermissionDenial[];
      errors: string[];
    }
```

### `SDKSystemMessage`

시스템 초기화 메시지입니다.

```typescript
type SDKSystemMessage = {
  type: 'system';
  subtype: 'init';
  uuid: UUID;
  session_id: string;
  apiKeySource: ApiKeySource;
  cwd: string;
  tools: string[];
  mcp_servers: {
    name: string;
    status: string;
  }[];
  model: string;
  permissionMode: PermissionMode;
  slash_commands: string[];
  output_style: string;
}
```

### `SDKPartialAssistantMessage`

스트리밍 부분 메시지(`includePartialMessages`가 true일 때만).

```typescript
type SDKPartialAssistantMessage = {
  type: 'stream_event';
  event: RawMessageStreamEvent; // Anthropic SDK에서
  parent_tool_use_id: string | null;
  uuid: UUID;
  session_id: string;
}
```

### `SDKCompactBoundaryMessage`

대화 압축 경계를 나타내는 메시지입니다.

```typescript
type SDKCompactBoundaryMessage = {
  type: 'system';
  subtype: 'compact_boundary';
  uuid: UUID;
  session_id: string;
  compact_metadata: {
    trigger: 'manual' | 'auto';
    pre_tokens: number;
  };
}
```

### `SDKPermissionDenial`

거부된 도구 사용에 대한 정보입니다.

```typescript
type SDKPermissionDenial = {
  tool_name: string;
  tool_use_id: string;
  tool_input: ToolInput;
}
```

## 훅 타입

예제와 일반적인 패턴이 포함된 훅 사용에 대한 포괄적인 가이드는 [훅 가이드](https://platform.claude.com/docs/en/agent-sdk/hooks)를 참조하세요.

### `HookEvent`

사용 가능한 훅 이벤트입니다.

```typescript
type HookEvent =
  | 'PreToolUse'
  | 'PostToolUse'
  | 'PostToolUseFailure'
  | 'Notification'
  | 'UserPromptSubmit'
  | 'SessionStart'
  | 'SessionEnd'
  | 'Stop'
  | 'SubagentStart'
  | 'SubagentStop'
  | 'PreCompact'
  | 'PermissionRequest';
```

### `HookCallback`

훅 콜백 함수 타입입니다.

```typescript
type HookCallback = (
  input: HookInput, // 모든 훅 입력 타입의 유니온
  toolUseID: string | undefined,
  options: { signal: AbortSignal }
) => Promise<HookJSONOutput>;
```

### `HookCallbackMatcher`

선택적 매처가 있는 훅 설정입니다.

```typescript
interface HookCallbackMatcher {
  matcher?: string;
  hooks: HookCallback[];
}
```

### `HookInput`

모든 훅 입력 타입의 유니온 타입입니다.

```typescript
type HookInput =
  | PreToolUseHookInput
  | PostToolUseHookInput
  | PostToolUseFailureHookInput
  | NotificationHookInput
  | UserPromptSubmitHookInput
  | SessionStartHookInput
  | SessionEndHookInput
  | StopHookInput
  | SubagentStartHookInput
  | SubagentStopHookInput
  | PreCompactHookInput
  | PermissionRequestHookInput;
```

### `BaseHookInput`

모든 훅 입력 타입이 확장하는 기본 인터페이스입니다.

```typescript
type BaseHookInput = {
  session_id: string;
  transcript_path: string;
  cwd: string;
  permission_mode?: string;
}
```

#### `PreToolUseHookInput`

```typescript
type PreToolUseHookInput = BaseHookInput & {
  hook_event_name: 'PreToolUse';
  tool_name: string;
  tool_input: unknown;
}
```

#### `PostToolUseHookInput`

```typescript
type PostToolUseHookInput = BaseHookInput & {
  hook_event_name: 'PostToolUse';
  tool_name: string;
  tool_input: unknown;
  tool_response: unknown;
}
```

#### `PostToolUseFailureHookInput`

```typescript
type PostToolUseFailureHookInput = BaseHookInput & {
  hook_event_name: 'PostToolUseFailure';
  tool_name: string;
  tool_input: unknown;
  error: string;
  is_interrupt?: boolean;
}
```

#### `NotificationHookInput`

```typescript
type NotificationHookInput = BaseHookInput & {
  hook_event_name: 'Notification';
  message: string;
  title?: string;
}
```

#### `UserPromptSubmitHookInput`

```typescript
type UserPromptSubmitHookInput = BaseHookInput & {
  hook_event_name: 'UserPromptSubmit';
  prompt: string;
}
```

#### `SessionStartHookInput`

```typescript
type SessionStartHookInput = BaseHookInput & {
  hook_event_name: 'SessionStart';
  source: 'startup' | 'resume' | 'clear' | 'compact';
}
```

#### `SessionEndHookInput`

```typescript
type SessionEndHookInput = BaseHookInput & {
  hook_event_name: 'SessionEnd';
  reason: ExitReason;  // EXIT_REASONS 배열의 문자열
}
```

#### `StopHookInput`

```typescript
type StopHookInput = BaseHookInput & {
  hook_event_name: 'Stop';
  stop_hook_active: boolean;
}
```

#### `SubagentStartHookInput`

```typescript
type SubagentStartHookInput = BaseHookInput & {
  hook_event_name: 'SubagentStart';
  agent_id: string;
  agent_type: string;
}
```

#### `SubagentStopHookInput`

```typescript
type SubagentStopHookInput = BaseHookInput & {
  hook_event_name: 'SubagentStop';
  stop_hook_active: boolean;
}
```

#### `PreCompactHookInput`

```typescript
type PreCompactHookInput = BaseHookInput & {
  hook_event_name: 'PreCompact';
  trigger: 'manual' | 'auto';
  custom_instructions: string | null;
}
```

#### `PermissionRequestHookInput`

```typescript
type PermissionRequestHookInput = BaseHookInput & {
  hook_event_name: 'PermissionRequest';
  tool_name: string;
  tool_input: unknown;
  permission_suggestions?: PermissionUpdate[];
}
```

### `HookJSONOutput`

훅 반환값입니다.

```typescript
type HookJSONOutput = AsyncHookJSONOutput | SyncHookJSONOutput;
```

#### `AsyncHookJSONOutput`

```typescript
type AsyncHookJSONOutput = {
  async: true;
  asyncTimeout?: number;
}
```

#### `SyncHookJSONOutput`

```typescript
type SyncHookJSONOutput = {
  continue?: boolean;
  suppressOutput?: boolean;
  stopReason?: string;
  decision?: 'approve' | 'block';
  systemMessage?: string;
  reason?: string;
  hookSpecificOutput?:
    | {
        hookEventName: 'PreToolUse';
        permissionDecision?: 'allow' | 'deny' | 'ask';
        permissionDecisionReason?: string;
        updatedInput?: Record<string, unknown>;
      }
    | {
        hookEventName: 'UserPromptSubmit';
        additionalContext?: string;
      }
    | {
        hookEventName: 'SessionStart';
        additionalContext?: string;
      }
    | {
        hookEventName: 'PostToolUse';
        additionalContext?: string;
      };
}
```

## 도구 입력 타입

모든 내장 Claude Code 도구의 입력 스키마 문서입니다. 이러한 타입은 `@anthropic-ai/claude-agent-sdk`에서 내보내지며 타입 안전한 도구 상호작용에 사용할 수 있습니다.

### `ToolInput`

**참고:** 이것은 명확성을 위한 문서 전용 타입입니다. 모든 도구 입력 타입의 유니온을 나타냅니다.

```typescript
type ToolInput =
  | AgentInput
  | AskUserQuestionInput
  | BashInput
  | BashOutputInput
  | FileEditInput
  | FileReadInput
  | FileWriteInput
  | GlobInput
  | GrepInput
  | KillShellInput
  | NotebookEditInput
  | WebFetchInput
  | WebSearchInput
  | TodoWriteInput
  | ExitPlanModeInput
  | ListMcpResourcesInput
  | ReadMcpResourceInput;
```

### Task

**도구 이름:** `Task`

```typescript
interface AgentInput {
  /**
   * 작업에 대한 짧은(3-5단어) 설명
   */
  description: string;
  /**
   * 에이전트가 수행할 작업
   */
  prompt: string;
  /**
   * 이 작업에 사용할 특수 에이전트 타입
   */
  subagent_type: string;
}
```

복잡한 다단계 작업을 자율적으로 처리하기 위해 새 에이전트를 시작합니다.

### AskUserQuestion

**도구 이름:** `AskUserQuestion`

```typescript
interface AskUserQuestionInput {
  /**
   * 사용자에게 물을 질문들(1-4개 질문)
   */
  questions: Array<{
    /**
     * 사용자에게 물을 완전한 질문. 명확하고 구체적이어야 하며,
     * 물음표로 끝나야 함.
     */
    question: string;
    /**
     * 칩/태그로 표시되는 매우 짧은 레이블(최대 12자).
     * 예: "Auth method", "Library", "Approach"
     */
    header: string;
    /**
     * 사용 가능한 선택지(2-4개 옵션). "Other" 옵션은
     * 자동으로 제공됨.
     */
    options: Array<{
      /**
       * 이 옵션의 표시 텍스트(1-5단어)
       */
      label: string;
      /**
       * 이 옵션의 의미 설명
       */
      description: string;
    }>;
    /**
     * 다중 선택을 허용하려면 true로 설정
     */
    multiSelect: boolean;
  }>;
  /**
   * 권한 시스템이 채우는 사용자 답변.
   * 질문 텍스트를 선택된 옵션 레이블로 매핑.
   * 다중 선택 답변은 쉼표로 구분됨.
   */
  answers?: Record<string, string>;
}
```

실행 중 사용자에게 명확한 질문을 합니다. 사용 세부 정보는 [승인 및 사용자 입력 처리](https://platform.claude.com/docs/en/agent-sdk/user-input#handle-clarifying-questions)를 참조하세요.

### Bash

**도구 이름:** `Bash`

```typescript
interface BashInput {
  /**
   * 실행할 명령
   */
  command: string;
  /**
   * 선택적 타임아웃(밀리초 단위, 최대 600000)
   */
  timeout?: number;
  /**
   * 5-10단어로 이 명령이 수행하는 작업에 대한 명확하고 간결한 설명
   */
  description?: string;
  /**
   * 백그라운드에서 이 명령을 실행하려면 true로 설정
   */
  run_in_background?: boolean;
}
```

선택적 타임아웃 및 백그라운드 실행과 함께 지속적인 셸 세션에서 bash 명령을 실행합니다.

### BashOutput

**도구 이름:** `BashOutput`

```typescript
interface BashOutputInput {
  /**
   * 출력을 검색할 백그라운드 셸의 ID
   */
  bash_id: string;
  /**
   * 출력 라인을 필터링할 선택적 정규식
   */
  filter?: string;
}
```

실행 중이거나 완료된 백그라운드 bash 셸에서 출력을 검색합니다.

### Edit

**도구 이름:** `Edit`

```typescript
interface FileEditInput {
  /**
   * 수정할 파일의 절대 경로
   */
  file_path: string;
  /**
   * 교체할 텍스트
   */
  old_string: string;
  /**
   * 교체할 새 텍스트(old_string과 달라야 함)
   */
  new_string: string;
  /**
   * old_string의 모든 발생을 교체(기본값 false)
   */
  replace_all?: boolean;
}
```

파일에서 정확한 문자열 교체를 수행합니다.

### Read

**도구 이름:** `Read`

```typescript
interface FileReadInput {
  /**
   * 읽을 파일의 절대 경로
   */
  file_path: string;
  /**
   * 읽기를 시작할 줄 번호
   */
  offset?: number;
  /**
   * 읽을 줄 수
   */
  limit?: number;
}
```

텍스트, 이미지, PDF, Jupyter 노트북을 포함하여 로컬 파일 시스템에서 파일을 읽습니다.

### Write

**도구 이름:** `Write`

```typescript
interface FileWriteInput {
  /**
   * 쓸 파일의 절대 경로
   */
  file_path: string;
  /**
   * 파일에 쓸 내용
   */
  content: string;
}
```

로컬 파일 시스템에 파일을 쓰며, 존재하는 경우 덮어씁니다.

### Glob

**도구 이름:** `Glob`

```typescript
interface GlobInput {
  /**
   * 파일을 매칭할 glob 패턴
   */
  pattern: string;
  /**
   * 검색할 디렉터리(기본값은 cwd)
   */
  path?: string;
}
```

모든 코드베이스 크기에서 작동하는 빠른 파일 패턴 매칭입니다.

### Grep

**도구 이름:** `Grep`

```typescript
interface GrepInput {
  /**
   * 검색할 정규식 패턴
   */
  pattern: string;
  /**
   * 검색할 파일 또는 디렉터리(기본값은 cwd)
   */
  path?: string;
  /**
   * 파일을 필터링할 glob 패턴(예: "*.js")
   */
  glob?: string;
  /**
   * 검색할 파일 타입(예: "js", "py", "rust")
   */
  type?: string;
  /**
   * 출력 모드: "content", "files_with_matches", 또는 "count"
   */
  output_mode?: 'content' | 'files_with_matches' | 'count';
  /**
   * 대소문자 구분 없이 검색
   */
  '-i'?: boolean;
  /**
   * 줄 번호 표시(content 모드용)
   */
  '-n'?: boolean;
  /**
   * 각 매치 전에 표시할 줄 수
   */
  '-B'?: number;
  /**
   * 각 매치 후에 표시할 줄 수
   */
  '-A'?: number;
  /**
   * 각 매치 전후에 표시할 줄 수
   */
  '-C'?: number;
  /**
   * 첫 N개 줄/항목으로 출력 제한
   */
  head_limit?: number;
  /**
   * 멀티라인 모드 활성화
   */
  multiline?: boolean;
}
```

정규식 지원이 포함된 ripgrep 기반의 강력한 검색 도구입니다.

### KillBash

**도구 이름:** `KillBash`

```typescript
interface KillShellInput {
  /**
   * 종료할 백그라운드 셸의 ID
   */
  shell_id: string;
}
```

ID로 실행 중인 백그라운드 bash 셸을 종료합니다.

### NotebookEdit

**도구 이름:** `NotebookEdit`

```typescript
interface NotebookEditInput {
  /**
   * Jupyter 노트북 파일의 절대 경로
   */
  notebook_path: string;
  /**
   * 편집할 셀의 ID
   */
  cell_id?: string;
  /**
   * 셀의 새 소스
   */
  new_source: string;
  /**
   * 셀의 타입(code 또는 markdown)
   */
  cell_type?: 'code' | 'markdown';
  /**
   * 편집 타입(replace, insert, delete)
   */
  edit_mode?: 'replace' | 'insert' | 'delete';
}
```

Jupyter 노트북 파일의 셀을 편집합니다.

### WebFetch

**도구 이름:** `WebFetch`

```typescript
interface WebFetchInput {
  /**
   * 콘텐츠를 가져올 URL
   */
  url: string;
  /**
   * 가져온 콘텐츠에 대해 실행할 프롬프트
   */
  prompt: string;
}
```

URL에서 콘텐츠를 가져와 AI 모델로 처리합니다.

### WebSearch

**도구 이름:** `WebSearch`

```typescript
interface WebSearchInput {
  /**
   * 사용할 검색 쿼리
   */
  query: string;
  /**
   * 이 도메인의 결과만 포함
   */
  allowed_domains?: string[];
  /**
   * 이 도메인의 결과는 절대 포함하지 않음
   */
  blocked_domains?: string[];
}
```

웹을 검색하고 형식화된 결과를 반환합니다.

### TodoWrite

**도구 이름:** `TodoWrite`

```typescript
interface TodoWriteInput {
  /**
   * 업데이트된 할 일 목록
   */
  todos: Array<{
    /**
     * 작업 설명
     */
    content: string;
    /**
     * 작업 상태
     */
    status: 'pending' | 'in_progress' | 'completed';
    /**
     * 작업 설명의 능동형
     */
    activeForm: string;
  }>;
}
```

진행 상황 추적을 위한 구조화된 작업 목록을 생성하고 관리합니다.

### ExitPlanMode

**도구 이름:** `ExitPlanMode`

```typescript
interface ExitPlanModeInput {
  /**
   * 사용자 승인을 위해 실행할 계획
   */
  plan: string;
}
```

계획 모드를 종료하고 사용자에게 계획 승인을 요청합니다.

### ListMcpResources

**도구 이름:** `ListMcpResources`

```typescript
interface ListMcpResourcesInput {
  /**
   * 리소스를 필터링할 선택적 서버 이름
   */
  server?: string;
}
```

연결된 서버에서 사용 가능한 MCP 리소스를 나열합니다.

### ReadMcpResource

**도구 이름:** `ReadMcpResource`

```typescript
interface ReadMcpResourceInput {
  /**
   * MCP 서버 이름
   */
  server: string;
  /**
   * 읽을 리소스 URI
   */
  uri: string;
}
```

서버에서 특정 MCP 리소스를 읽습니다.

## 도구 출력 타입

모든 내장 Claude Code 도구의 출력 스키마 문서입니다. 이러한 타입은 각 도구가 반환하는 실제 응답 데이터를 나타냅니다.

### `ToolOutput`

**참고:** 이것은 명확성을 위한 문서 전용 타입입니다. 모든 도구 출력 타입의 유니온을 나타냅니다.

```typescript
type ToolOutput =
  | TaskOutput
  | AskUserQuestionOutput
  | BashOutput
  | BashOutputToolOutput
  | EditOutput
  | ReadOutput
  | WriteOutput
  | GlobOutput
  | GrepOutput
  | KillBashOutput
  | NotebookEditOutput
  | WebFetchOutput
  | WebSearchOutput
  | TodoWriteOutput
  | ExitPlanModeOutput
  | ListMcpResourcesOutput
  | ReadMcpResourceOutput;
```

### Task

**도구 이름:** `Task`

```typescript
interface TaskOutput {
  /**
   * 서브에이전트의 최종 결과 메시지
   */
  result: string;
  /**
   * 토큰 사용 통계
   */
  usage?: {
    input_tokens: number;
    output_tokens: number;
    cache_creation_input_tokens?: number;
    cache_read_input_tokens?: number;
  };
  /**
   * 총 비용(USD)
   */
  total_cost_usd?: number;
  /**
   * 실행 시간(밀리초)
   */
  duration_ms?: number;
}
```

위임된 작업을 완료한 후 서브에이전트의 최종 결과를 반환합니다.

### AskUserQuestion

**도구 이름:** `AskUserQuestion`

```typescript
interface AskUserQuestionOutput {
  /**
   * 물어본 질문들
   */
  questions: Array<{
    question: string;
    header: string;
    options: Array<{
      label: string;
      description: string;
    }>;
    multiSelect: boolean;
  }>;
  /**
   * 사용자가 제공한 답변.
   * 질문 텍스트를 답변 문자열로 매핑.
   * 다중 선택 답변은 쉼표로 구분됨.
   */
  answers: Record<string, string>;
}
```

물어본 질문과 사용자의 답변을 반환합니다.

### Bash

**도구 이름:** `Bash`

```typescript
interface BashOutput {
  /**
   * stdout과 stderr의 결합 출력
   */
  output: string;
  /**
   * 명령의 종료 코드
   */
  exitCode: number;
  /**
   * 타임아웃으로 인해 명령이 종료되었는지 여부
   */
  killed?: boolean;
  /**
   * 백그라운드 프로세스의 셸 ID
   */
  shellId?: string;
}
```

종료 상태와 함께 명령 출력을 반환합니다. 백그라운드 명령은 shellId와 함께 즉시 반환됩니다.

### BashOutput

**도구 이름:** `BashOutput`

```typescript
interface BashOutputToolOutput {
  /**
   * 마지막 확인 이후의 새 출력
   */
  output: string;
  /**
   * 현재 셸 상태
   */
  status: 'running' | 'completed' | 'failed';
  /**
   * 종료 코드(완료 시)
   */
  exitCode?: number;
}
```

백그라운드 셸의 증분 출력을 반환합니다.

### Edit

**도구 이름:** `Edit`

```typescript
interface EditOutput {
  /**
   * 확인 메시지
   */
  message: string;
  /**
   * 수행된 교체 횟수
   */
  replacements: number;
  /**
   * 편집된 파일 경로
   */
  file_path: string;
}
```

교체 횟수와 함께 성공적인 편집 확인을 반환합니다.

### Read

**도구 이름:** `Read`

```typescript
type ReadOutput =
  | TextFileOutput
  | ImageFileOutput
  | PDFFileOutput
  | NotebookFileOutput;

interface TextFileOutput {
  /**
   * 줄 번호가 포함된 파일 내용
   */
  content: string;
  /**
   * 파일의 총 줄 수
   */
  total_lines: number;
  /**
   * 실제로 반환된 줄 수
   */
  lines_returned: number;
}

interface ImageFileOutput {
  /**
   * Base64로 인코딩된 이미지 데이터
   */
  image: string;
  /**
   * 이미지 MIME 타입
   */
  mime_type: string;
  /**
   * 파일 크기(바이트)
   */
  file_size: number;
}

interface PDFFileOutput {
  /**
   * 페이지 내용 배열
   */
  pages: Array<{
    page_number: number;
    text?: string;
    images?: Array<{
      image: string;
      mime_type: string;
    }>;
  }>;
  /**
   * 총 페이지 수
   */
  total_pages: number;
}

interface NotebookFileOutput {
  /**
   * Jupyter 노트북 셀
   */
  cells: Array<{
    cell_type: 'code' | 'markdown';
    source: string;
    outputs?: any[];
    execution_count?: number;
  }>;
  /**
   * 노트북 메타데이터
   */
  metadata?: Record<string, any>;
}
```

파일 타입에 적합한 형식으로 파일 내용을 반환합니다.

### Write

**도구 이름:** `Write`

```typescript
interface WriteOutput {
  /**
   * 성공 메시지
   */
  message: string;
  /**
   * 쓴 바이트 수
   */
  bytes_written: number;
  /**
   * 쓴 파일 경로
   */
  file_path: string;
}
```

파일을 성공적으로 쓴 후 확인을 반환합니다.

### Glob

**도구 이름:** `Glob`

```typescript
interface GlobOutput {
  /**
   * 매칭된 파일 경로 배열
   */
  matches: string[];
  /**
   * 찾은 매치 수
   */
  count: number;
  /**
   * 사용된 검색 디렉터리
   */
  search_path: string;
}
```

수정 시간 순으로 정렬된 glob 패턴과 일치하는 파일 경로를 반환합니다.

### Grep

**도구 이름:** `Grep`

```typescript
type GrepOutput =
  | GrepContentOutput
  | GrepFilesOutput
  | GrepCountOutput;

interface GrepContentOutput {
  /**
   * 컨텍스트가 포함된 매칭 라인
   */
  matches: Array<{
    file: string;
    line_number?: number;
    line: string;
    before_context?: string[];
    after_context?: string[];
  }>;
  /**
   * 총 매치 수
   */
  total_matches: number;
}

interface GrepFilesOutput {
  /**
   * 매치를 포함하는 파일
   */
  files: string[];
  /**
   * 매치가 있는 파일 수
   */
  count: number;
}

interface GrepCountOutput {
  /**
   * 파일당 매치 횟수
   */
  counts: Array<{
    file: string;
    count: number;
  }>;
  /**
   * 모든 파일의 총 매치 수
   */
  total: number;
}
```

output_mode에 지정된 형식으로 검색 결과를 반환합니다.

### KillBash

**도구 이름:** `KillBash`

```typescript
interface KillBashOutput {
  /**
   * 성공 메시지
   */
  message: string;
  /**
   * 종료된 셸의 ID
   */
  shell_id: string;
}
```

백그라운드 셸을 종료한 후 확인을 반환합니다.

### NotebookEdit

**도구 이름:** `NotebookEdit`

```typescript
interface NotebookEditOutput {
  /**
   * 성공 메시지
   */
  message: string;
  /**
   * 수행된 편집 타입
   */
  edit_type: 'replaced' | 'inserted' | 'deleted';
  /**
   * 영향을 받은 셀 ID
   */
  cell_id?: string;
  /**
   * 편집 후 노트북의 총 셀 수
   */
  total_cells: number;
}
```

Jupyter 노트북을 수정한 후 확인을 반환합니다.

### WebFetch

**도구 이름:** `WebFetch`

```typescript
interface WebFetchOutput {
  /**
   * 프롬프트에 대한 AI 모델의 응답
   */
  response: string;
  /**
   * 가져온 URL
   */
  url: string;
  /**
   * 리다이렉트 후 최종 URL
   */
  final_url?: string;
  /**
   * HTTP 상태 코드
   */
  status_code?: number;
}
```

가져온 웹 콘텐츠에 대한 AI의 분석을 반환합니다.

### WebSearch

**도구 이름:** `WebSearch`

```typescript
interface WebSearchOutput {
  /**
   * 검색 결과
   */
  results: Array<{
    title: string;
    url: string;
    snippet: string;
    /**
     * 가능한 경우 추가 메타데이터
     */
    metadata?: Record<string, any>;
  }>;
  /**
   * 총 결과 수
   */
  total_results: number;
  /**
   * 검색된 쿼리
   */
  query: string;
}
```

웹에서 형식화된 검색 결과를 반환합니다.

### TodoWrite

**도구 이름:** `TodoWrite`

```typescript
interface TodoWriteOutput {
  /**
   * 성공 메시지
   */
  message: string;
  /**
   * 현재 할 일 통계
   */
  stats: {
    total: number;
    pending: number;
    in_progress: number;
    completed: number;
  };
}
```

현재 작업 통계와 함께 확인을 반환합니다.

### ExitPlanMode

**도구 이름:** `ExitPlanMode`

```typescript
interface ExitPlanModeOutput {
  /**
   * 확인 메시지
   */
  message: string;
  /**
   * 사용자가 계획을 승인했는지 여부
   */
  approved?: boolean;
}
```

계획 모드를 종료한 후 확인을 반환합니다.

### ListMcpResources

**도구 이름:** `ListMcpResources`

```typescript
interface ListMcpResourcesOutput {
  /**
   * 사용 가능한 리소스
   */
  resources: Array<{
    uri: string;
    name: string;
    description?: string;
    mimeType?: string;
    server: string;
  }>;
  /**
   * 총 리소스 수
   */
  total: number;
}
```

사용 가능한 MCP 리소스 목록을 반환합니다.

### ReadMcpResource

**도구 이름:** `ReadMcpResource`

```typescript
interface ReadMcpResourceOutput {
  /**
   * 리소스 내용
   */
  contents: Array<{
    uri: string;
    mimeType?: string;
    text?: string;
    blob?: string;
  }>;
  /**
   * 리소스를 제공한 서버
   */
  server: string;
}
```

요청된 MCP 리소스의 내용을 반환합니다.

## 권한 타입

### `PermissionUpdate`

권한 업데이트를 위한 작업입니다.

```typescript
type PermissionUpdate =
  | {
      type: 'addRules';
      rules: PermissionRuleValue[];
      behavior: PermissionBehavior;
      destination: PermissionUpdateDestination;
    }
  | {
      type: 'replaceRules';
      rules: PermissionRuleValue[];
      behavior: PermissionBehavior;
      destination: PermissionUpdateDestination;
    }
  | {
      type: 'removeRules';
      rules: PermissionRuleValue[];
      behavior: PermissionBehavior;
      destination: PermissionUpdateDestination;
    }
  | {
      type: 'setMode';
      mode: PermissionMode;
      destination: PermissionUpdateDestination;
    }
  | {
      type: 'addDirectories';
      directories: string[];
      destination: PermissionUpdateDestination;
    }
  | {
      type: 'removeDirectories';
      directories: string[];
      destination: PermissionUpdateDestination;
    }
```

### `PermissionBehavior`

```typescript
type PermissionBehavior = 'allow' | 'deny' | 'ask';
```

### `PermissionUpdateDestination`

```typescript
type PermissionUpdateDestination =
  | 'userSettings'     // 전역 사용자 설정
  | 'projectSettings'  // 디렉터리별 프로젝트 설정
  | 'localSettings'    // gitignored 로컬 설정
  | 'session'          // 현재 세션만
```

### `PermissionRuleValue`

```typescript
type PermissionRuleValue = {
  toolName: string;
  ruleContent?: string;
}
```

## 기타 타입

### `ApiKeySource`

```typescript
type ApiKeySource = 'user' | 'project' | 'org' | 'temporary';
```

### `SdkBeta`

`betas` 옵션을 통해 활성화할 수 있는 사용 가능한 베타 기능입니다. 자세한 내용은 [베타 헤더](https://platform.claude.com/docs/en/api/beta-headers)를 참조하세요.

```typescript
type SdkBeta = 'context-1m-2025-08-07';
```

| 값 | 설명 | 호환 모델 |
|:------|:------------|:------------------|
| `'context-1m-2025-08-07'` | 100만 토큰 [컨텍스트 윈도우](../01-build-with-claude/02-context-windows.md) 활성화 | Claude Sonnet 4, Claude Sonnet 4.5 |

### `SlashCommand`

사용 가능한 슬래시 명령에 대한 정보입니다.

```typescript
type SlashCommand = {
  name: string;
  description: string;
  argumentHint: string;
}
```

### `ModelInfo`

사용 가능한 모델에 대한 정보입니다.

```typescript
type ModelInfo = {
  value: string;
  displayName: string;
  description: string;
}
```

### `McpServerStatus`

연결된 MCP 서버의 상태입니다.

```typescript
type McpServerStatus = {
  name: string;
  status: 'connected' | 'failed' | 'needs-auth' | 'pending';
  serverInfo?: {
    name: string;
    version: string;
  };
}
```

### `AccountInfo`

인증된 사용자의 계정 정보입니다.

```typescript
type AccountInfo = {
  email?: string;
  organization?: string;
  subscriptionType?: string;
  tokenSource?: string;
  apiKeySource?: string;
}
```

### `ModelUsage`

결과 메시지에서 반환되는 모델별 사용 통계입니다.

```typescript
type ModelUsage = {
  inputTokens: number;
  outputTokens: number;
  cacheReadInputTokens: number;
  cacheCreationInputTokens: number;
  webSearchRequests: number;
  costUSD: number;
  contextWindow: number;
}
```

### `ConfigScope`

```typescript
type ConfigScope = 'local' | 'user' | 'project';
```

### `NonNullableUsage`

모든 nullable 필드가 non-nullable로 변경된 [`Usage`](#usage) 버전입니다.

```typescript
type NonNullableUsage = {
  [K in keyof Usage]: NonNullable<Usage[K]>;
}
```

### `Usage`

토큰 사용 통계(`@anthropic-ai/sdk`에서).

```typescript
type Usage = {
  input_tokens: number | null;
  output_tokens: number | null;
  cache_creation_input_tokens?: number | null;
  cache_read_input_tokens?: number | null;
}
```

### `CallToolResult`

MCP 도구 결과 타입(`@modelcontextprotocol/sdk/types.js`에서).

```typescript
type CallToolResult = {
  content: Array<{
    type: 'text' | 'image' | 'resource';
    // 추가 필드는 타입에 따라 다름
  }>;
  isError?: boolean;
}
```

### `AbortError`

중단 작업을 위한 사용자 정의 오류 클래스입니다.

```typescript
class AbortError extends Error {}
```

## 샌드박스 설정

### `SandboxSettings`

샌드박스 동작에 대한 설정입니다. 프로그래밍 방식으로 명령 샌드박싱을 활성화하고 네트워크 제한을 구성하는 데 사용합니다.

```typescript
type SandboxSettings = {
  enabled?: boolean;
  autoAllowBashIfSandboxed?: boolean;
  excludedCommands?: string[];
  allowUnsandboxedCommands?: boolean;
  network?: NetworkSandboxSettings;
  ignoreViolations?: SandboxIgnoreViolations;
  enableWeakerNestedSandbox?: boolean;
}
```

| 속성 | 타입 | 기본값 | 설명 |
| :------- | :--- | :------ | :---------- |
| `enabled` | `boolean` | `false` | 명령 실행을 위한 샌드박스 모드 활성화 |
| `autoAllowBashIfSandboxed` | `boolean` | `false` | 샌드박스가 활성화된 경우 bash 명령 자동 승인 |
| `excludedCommands` | `string[]` | `[]` | 항상 샌드박스 제한을 우회하는 명령(예: `['docker']`). 이들은 모델 개입 없이 자동으로 샌드박스되지 않은 상태로 실행됨 |
| `allowUnsandboxedCommands` | `boolean` | `false` | 모델이 샌드박스 외부에서 명령 실행을 요청하도록 허용. `true`일 때, 모델은 도구 입력에서 `dangerouslyDisableSandbox`를 설정할 수 있으며, 이는 [권한 시스템](#permissions-fallback-for-unsandboxed-commands)으로 폴백됨 |
| `network` | [`NetworkSandboxSettings`](#networksandboxsettings) | `undefined` | 네트워크 특정 샌드박스 설정 |
| `ignoreViolations` | [`SandboxIgnoreViolations`](#sandboxignoreviolations) | `undefined` | 무시할 샌드박스 위반 구성 |
| `enableWeakerNestedSandbox` | `boolean` | `false` | 호환성을 위한 약한 중첩 샌드박스 활성화 |


> **파일 시스템 및 네트워크 액세스 제한**은 샌드박스 설정을 통해 구성되지 않습니다. 대신 [권한 규칙](https://code.claude.com/docs/en/settings#permission-settings)에서 파생됩니다:
>
> - **파일 시스템 읽기 제한**: Read deny 규칙
> - **파일 시스템 쓰기 제한**: Edit allow/deny 규칙
> - **네트워크 제한**: WebFetch allow/deny 규칙
>
> 명령 실행 샌드박싱에는 샌드박스 설정을 사용하고, 파일 시스템 및 네트워크 액세스 제어에는 권한 규칙을 사용하세요.


#### 사용 예제

```typescript
import { query } from "@anthropic-ai/claude-agent-sdk";

const result = await query({
  prompt: "Build and test my project",
  options: {
    sandbox: {
      enabled: true,
      autoAllowBashIfSandboxed: true,
      network: {
        allowLocalBinding: true
      }
    }
  }
});
```


> **Unix 소켓 보안**: `allowUnixSockets` 옵션은 강력한 시스템 서비스에 대한 액세스를 부여할 수 있습니다. 예를 들어, `/var/run/docker.sock`을 허용하면 Docker API를 통해 호스트 시스템에 대한 전체 액세스를 효과적으로 부여하여 샌드박스 격리를 우회합니다. 엄격히 필요한 Unix 소켓만 허용하고 각 소켓의 보안 영향을 이해하세요.


### `NetworkSandboxSettings`

샌드박스 모드에 대한 네트워크 특정 설정입니다.

```typescript
type NetworkSandboxSettings = {
  allowLocalBinding?: boolean;
  allowUnixSockets?: string[];
  allowAllUnixSockets?: boolean;
  httpProxyPort?: number;
  socksProxyPort?: number;
}
```

| 속성 | 타입 | 기본값 | 설명 |
| :------- | :--- | :------ | :---------- |
| `allowLocalBinding` | `boolean` | `false` | 프로세스가 로컬 포트에 바인딩하도록 허용(예: 개발 서버용) |
| `allowUnixSockets` | `string[]` | `[]` | 프로세스가 액세스할 수 있는 Unix 소켓 경로(예: Docker 소켓) |
| `allowAllUnixSockets` | `boolean` | `false` | 모든 Unix 소켓에 대한 액세스 허용 |
| `httpProxyPort` | `number` | `undefined` | 네트워크 요청용 HTTP 프록시 포트 |
| `socksProxyPort` | `number` | `undefined` | 네트워크 요청용 SOCKS 프록시 포트 |

### `SandboxIgnoreViolations`

특정 샌드박스 위반을 무시하기 위한 설정입니다.

```typescript
type SandboxIgnoreViolations = {
  file?: string[];
  network?: string[];
}
```

| 속성 | 타입 | 기본값 | 설명 |
| :------- | :--- | :------ | :---------- |
| `file` | `string[]` | `[]` | 위반을 무시할 파일 경로 패턴 |
| `network` | `string[]` | `[]` | 위반을 무시할 네트워크 패턴 |

### 샌드박스되지 않은 명령에 대한 권한 폴백

`allowUnsandboxedCommands`가 활성화되면, 모델은 도구 입력에서 `dangerouslyDisableSandbox: true`를 설정하여 샌드박스 외부에서 명령을 실행하도록 요청할 수 있습니다. 이러한 요청은 기존 권한 시스템으로 폴백되므로 `canUseTool` 핸들러가 호출되어 사용자 정의 권한 부여 로직을 구현할 수 있습니다.


> **`excludedCommands` vs `allowUnsandboxedCommands`:**
> - `excludedCommands`: 항상 샌드박스를 자동으로 우회하는 명령의 정적 목록(예: `['docker']`). 모델은 이를 제어할 수 없습니다.
> - `allowUnsandboxedCommands`: 모델이 도구 입력에서 `dangerouslyDisableSandbox: true`를 설정하여 런타임에 샌드박스되지 않은 실행을 요청할지 결정하도록 허용합니다.


```typescript
import { query } from "@anthropic-ai/claude-agent-sdk";

const result = await query({
  prompt: "Deploy my application",
  options: {
    sandbox: {
      enabled: true,
      allowUnsandboxedCommands: true  // 모델이 샌드박스되지 않은 실행을 요청할 수 있음
    },
    permissionMode: "default",
    canUseTool: async (tool, input) => {
      // 모델이 샌드박스 우회를 요청하는지 확인
      if (tool === "Bash" && input.dangerouslyDisableSandbox) {
        // 모델이 샌드박스 외부에서 이 명령을 실행하려고 함
        console.log(`Unsandboxed command requested: ${input.command}`);

        // 허용하려면 true, 거부하려면 false 반환
        return isCommandAuthorized(input.command);
      }
      return true;
    }
  }
});
```

이 패턴을 통해 다음을 수행할 수 있습니다:

- **모델 요청 감사**: 모델이 샌드박스되지 않은 실행을 요청할 때 로그 기록
- **허용 목록 구현**: 특정 명령만 샌드박스되지 않은 상태로 실행 허용
- **승인 워크플로 추가**: 권한이 필요한 작업에 대해 명시적인 권한 부여 요구


> `dangerouslyDisableSandbox: true`로 실행되는 명령은 전체 시스템 액세스 권한을 가집니다. `canUseTool` 핸들러가 이러한 요청을 신중하게 검증하도록 하세요.
>
> `permissionMode`가 `bypassPermissions`로 설정되고 `allowUnsandboxedCommands`가 활성화되면, 모델은 승인 프롬프트 없이 샌드박스 외부에서 자율적으로 명령을 실행할 수 있습니다. 이 조합은 모델이 샌드박스 격리를 자동으로 벗어날 수 있도록 효과적으로 허용합니다.


## 참조

- [SDK 개요](../05-agent-sdk/01-overview.md) - 일반 SDK 개념
- [Python SDK 레퍼런스](../05-agent-sdk/05-python.md) - Python SDK 문서
- [CLI 레퍼런스](https://code.claude.com/docs/en/cli-reference) - 명령줄 인터페이스
- [일반 워크플로](https://code.claude.com/docs/en/common-workflows) - 단계별 가이드

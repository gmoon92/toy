# [TypeScript SDK V2 인터페이스 (프리뷰)](https://platform.claude.com/docs/en/agent-sdk/typescript-v2-preview)

다중 턴 대화를 위한 세션 기반 send/stream 패턴이 적용된 간소화된 V2 TypeScript Agent SDK 프리뷰입니다.

---


> V2 인터페이스는 **불안정한 프리뷰** 버전입니다. 안정화되기 전에 피드백에 따라 API가 변경될 수 있습니다. 세션 포킹과 같은 일부 기능은 [V1 SDK](../05-agent-sdk/03-typescript.md)에서만 사용할 수 있습니다.


V2 Claude Agent TypeScript SDK는 비동기 제너레이터와 yield 조정의 필요성을 제거했습니다. 이를 통해 다중 턴 대화가 더 간단해졌습니다. 턴 간에 제너레이터 상태를 관리하는 대신, 각 턴은 별도의 `send()`/`stream()` 사이클이 됩니다. API 표면은 세 가지 개념으로 축소됩니다:

- `createSession()` / `resumeSession()`: 대화 시작 또는 계속하기
- `session.send()`: 메시지 전송
- `session.stream()`: 응답 받기

## 설치

V2 인터페이스는 기존 SDK 패키지에 포함되어 있습니다:

```bash
npm install @anthropic-ai/claude-agent-sdk
```

## 빠른 시작

### 단발성 프롬프트

세션을 유지할 필요가 없는 간단한 단일 턴 쿼리의 경우 `unstable_v2_prompt()`를 사용하세요. 이 예제는 수학 질문을 보내고 답변을 로그에 출력합니다:

```typescript
import { unstable_v2_prompt } from '@anthropic-ai/claude-agent-sdk'

const result = await unstable_v2_prompt('What is 2 + 2?', {
  model: 'claude-sonnet-4-5-20250929'
})
console.log(result.result)
```

<details>
<summary>V1에서 동일한 작업 보기</summary>

```typescript
import { query } from '@anthropic-ai/claude-agent-sdk'

const q = query({
  prompt: 'What is 2 + 2?',
  options: { model: 'claude-sonnet-4-5-20250929' }
})

for await (const msg of q) {
  if (msg.type === 'result') {
    console.log(msg.result)
  }
}
```

</details>

### 기본 세션

단일 프롬프트를 넘어서는 상호작용의 경우 세션을 생성하세요. V2는 전송과 스트리밍을 별도의 단계로 분리합니다:
- `send()`는 메시지를 전달합니다
- `stream()`은 응답을 스트리밍합니다

이러한 명시적인 분리는 턴 사이에 로직을 추가하기 쉽게 만듭니다(예: 후속 메시지를 보내기 전에 응답을 처리하는 등).

아래 예제는 세션을 생성하고 Claude에게 "Hello!"를 보낸 후 텍스트 응답을 출력합니다. [`await using`](https://www.typescriptlang.org/docs/handbook/release-notes/typescript-5-2.html#using-declarations-and-explicit-resource-management) (TypeScript 5.2+)을 사용하여 블록이 종료될 때 자동으로 세션을 닫습니다. 수동으로 `session.close()`를 호출할 수도 있습니다.

```typescript
import { unstable_v2_createSession } from '@anthropic-ai/claude-agent-sdk'

await using session = unstable_v2_createSession({
  model: 'claude-sonnet-4-5-20250929'
})

await session.send('Hello!')
for await (const msg of session.stream()) {
  // 사람이 읽을 수 있는 출력을 얻기 위해 assistant 메시지 필터링
  if (msg.type === 'assistant') {
    const text = msg.message.content
      .filter(block => block.type === 'text')
      .map(block => block.text)
      .join('')
    console.log(text)
  }
}
```

<details>
<summary>V1에서 동일한 작업 보기</summary>

V1에서는 입력과 출력 모두 단일 비동기 제너레이터를 통해 흐릅니다. 기본 프롬프트의 경우 비슷해 보이지만, 다중 턴 로직을 추가하려면 입력 제너레이터를 사용하도록 재구성해야 합니다.

```typescript
import { query } from '@anthropic-ai/claude-agent-sdk'

const q = query({
  prompt: 'Hello!',
  options: { model: 'claude-sonnet-4-5-20250929' }
})

for await (const msg of q) {
  if (msg.type === 'assistant') {
    const text = msg.message.content
      .filter(block => block.type === 'text')
      .map(block => block.text)
      .join('')
    console.log(text)
  }
}
```

</details>

### 다중 턴 대화

세션은 여러 차례의 교환에 걸쳐 컨텍스트를 유지합니다. 대화를 계속하려면 동일한 세션에서 `send()`를 다시 호출하세요. Claude는 이전 턴을 기억합니다.

이 예제는 수학 질문을 한 다음 이전 답변을 참조하는 후속 질문을 합니다:

```typescript
import { unstable_v2_createSession } from '@anthropic-ai/claude-agent-sdk'

await using session = unstable_v2_createSession({
  model: 'claude-sonnet-4-5-20250929'
})

// 턴 1
await session.send('What is 5 + 3?')
for await (const msg of session.stream()) {
  // 사람이 읽을 수 있는 출력을 얻기 위해 assistant 메시지 필터링
  if (msg.type === 'assistant') {
    const text = msg.message.content
      .filter(block => block.type === 'text')
      .map(block => block.text)
      .join('')
    console.log(text)
  }
}

// 턴 2
await session.send('Multiply that by 2')
for await (const msg of session.stream()) {
  if (msg.type === 'assistant') {
    const text = msg.message.content
      .filter(block => block.type === 'text')
      .map(block => block.text)
      .join('')
    console.log(text)
  }
}
```

<details>
<summary>V1에서 동일한 작업 보기</summary>

```typescript
import { query } from '@anthropic-ai/claude-agent-sdk'

// 메시지를 공급하기 위해 비동기 이터러블을 생성해야 함
async function* createInputStream() {
  yield {
    type: 'user',
    session_id: '',
    message: { role: 'user', content: [{ type: 'text', text: 'What is 5 + 3?' }] },
    parent_tool_use_id: null
  }
  // 다음 메시지를 언제 yield할지 조정해야 함
  yield {
    type: 'user',
    session_id: '',
    message: { role: 'user', content: [{ type: 'text', text: 'Multiply by 2' }] },
    parent_tool_use_id: null
  }
}

const q = query({
  prompt: createInputStream(),
  options: { model: 'claude-sonnet-4-5-20250929' }
})

for await (const msg of q) {
  if (msg.type === 'assistant') {
    const text = msg.message.content
      .filter(block => block.type === 'text')
      .map(block => block.text)
      .join('')
    console.log(text)
  }
}
```

</details>

### 세션 재개

이전 상호작용에서 얻은 세션 ID가 있는 경우 나중에 재개할 수 있습니다. 이는 장기 실행 워크플로우나 애플리케이션 재시작 간에 대화를 지속해야 할 때 유용합니다.

이 예제는 세션을 생성하고 ID를 저장한 다음 세션을 닫고, 나중에 대화를 재개합니다:

```typescript
import {
  unstable_v2_createSession,
  unstable_v2_resumeSession,
  type SDKMessage
} from '@anthropic-ai/claude-agent-sdk'

// assistant 메시지에서 텍스트를 추출하는 헬퍼 함수
function getAssistantText(msg: SDKMessage): string | null {
  if (msg.type !== 'assistant') return null
  return msg.message.content
    .filter(block => block.type === 'text')
    .map(block => block.text)
    .join('')
}

// 초기 세션 생성 및 대화 시작
const session = unstable_v2_createSession({
  model: 'claude-sonnet-4-5-20250929'
})

await session.send('Remember this number: 42')

// 수신된 메시지에서 세션 ID 가져오기
let sessionId: string | undefined
for await (const msg of session.stream()) {
  sessionId = msg.session_id
  const text = getAssistantText(msg)
  if (text) console.log('Initial response:', text)
}

console.log('Session ID:', sessionId)
session.close()

// 나중에: 저장된 ID를 사용하여 세션 재개
await using resumedSession = unstable_v2_resumeSession(sessionId!, {
  model: 'claude-sonnet-4-5-20250929'
})

await resumedSession.send('What number did I ask you to remember?')
for await (const msg of resumedSession.stream()) {
  const text = getAssistantText(msg)
  if (text) console.log('Resumed response:', text)
}
```

<details>
<summary>V1에서 동일한 작업 보기</summary>

```typescript
import { query } from '@anthropic-ai/claude-agent-sdk'

// 초기 세션 생성
const initialQuery = query({
  prompt: 'Remember this number: 42',
  options: { model: 'claude-sonnet-4-5-20250929' }
})

// 메시지에서 세션 ID 가져오기
let sessionId: string | undefined
for await (const msg of initialQuery) {
  sessionId = msg.session_id
  if (msg.type === 'assistant') {
    const text = msg.message.content
      .filter(block => block.type === 'text')
      .map(block => block.text)
      .join('')
    console.log('Initial response:', text)
  }
}

console.log('Session ID:', sessionId)

// 나중에: 세션 재개
const resumedQuery = query({
  prompt: 'What number did I ask you to remember?',
  options: {
    model: 'claude-sonnet-4-5-20250929',
    resume: sessionId
  }
})

for await (const msg of resumedQuery) {
  if (msg.type === 'assistant') {
    const text = msg.message.content
      .filter(block => block.type === 'text')
      .map(block => block.text)
      .join('')
    console.log('Resumed response:', text)
  }
}
```

</details>

### 정리

세션은 수동으로 닫거나 자동 리소스 정리를 위한 TypeScript 5.2+ 기능인 [`await using`](https://www.typescriptlang.org/docs/handbook/release-notes/typescript-5-2.html#using-declarations-and-explicit-resource-management)을 사용하여 자동으로 닫을 수 있습니다. 이전 TypeScript 버전을 사용 중이거나 호환성 문제가 발생하면 대신 수동 정리를 사용하세요.

**자동 정리 (TypeScript 5.2+):**

```typescript
import { unstable_v2_createSession } from '@anthropic-ai/claude-agent-sdk'

await using session = unstable_v2_createSession({
  model: 'claude-sonnet-4-5-20250929'
})
// 블록이 종료되면 세션이 자동으로 닫힘
```

**수동 정리:**

```typescript
import { unstable_v2_createSession } from '@anthropic-ai/claude-agent-sdk'

const session = unstable_v2_createSession({
  model: 'claude-sonnet-4-5-20250929'
})
// ... 세션 사용 ...
session.close()
```

## API 레퍼런스

### `unstable_v2_createSession()`

다중 턴 대화를 위한 새 세션을 생성합니다.

```typescript
function unstable_v2_createSession(options: {
  model: string;
  // 추가 옵션 지원됨
}): Session
```

### `unstable_v2_resumeSession()`

ID로 기존 세션을 재개합니다.

```typescript
function unstable_v2_resumeSession(
  sessionId: string,
  options: {
    model: string;
    // 추가 옵션 지원됨
  }
): Session
```

### `unstable_v2_prompt()`

단일 턴 쿼리를 위한 단발성 편의 함수입니다.

```typescript
function unstable_v2_prompt(
  prompt: string,
  options: {
    model: string;
    // 추가 옵션 지원됨
  }
): Promise<Result>
```

### Session 인터페이스

```typescript
interface Session {
  send(message: string): Promise<void>;
  stream(): AsyncGenerator<SDKMessage>;
  close(): void;
}
```

## 기능 가용성

모든 V1 기능이 아직 V2에서 사용 가능한 것은 아닙니다. 다음 기능은 [V1 SDK](../05-agent-sdk/03-typescript.md) 사용이 필요합니다:

- 세션 포킹 (`forkSession` 옵션)
- 일부 고급 스트리밍 입력 패턴

## 피드백

V2 인터페이스가 안정화되기 전에 피드백을 공유해 주세요. [GitHub Issues](https://github.com/anthropics/claude-code/issues)를 통해 문제와 제안 사항을 보고하세요.

## 참고 자료

- [TypeScript SDK 레퍼런스 (V1)](../05-agent-sdk/03-typescript.md) - 전체 V1 SDK 문서
- [SDK 개요](../05-agent-sdk/01-overview.md) - 일반적인 SDK 개념
- [GitHub의 V2 예제](https://github.com/anthropics/claude-agent-sdk-demos/tree/main/hello-world-v2) - 작동하는 코드 예제

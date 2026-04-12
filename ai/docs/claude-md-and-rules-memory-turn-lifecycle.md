# CLAUDE.md & rules/*.md — 메모리 관점 및 턴 라이프사이클 분석

> 소스 기반 실증 분석 (2026-04-09)
> 분석 대상: `src/utils/claudemd.ts`, `src/utils/api.ts`, `src/context.ts`, `src/query.ts`, `src/utils/attachments.ts`,
`src/utils/frontmatterParser.ts`

---

## 1. 전체 구조 개요

Claude Code는 "메모리 파일"을 두 종류로 다룬다.

| 종류             | 파일                                                                        | 관리 주체      | 목적                    |
|----------------|---------------------------------------------------------------------------|------------|-----------------------|
| **사용자 지시 메모리** | `CLAUDE.md`, `.claude/CLAUDE.md`, `.claude/rules/*.md`, `CLAUDE.local.md` | 사람이 작성     | 프로젝트·팀·조직 지침을 모델에게 전달 |
| **자동 관리 메모리**  | `MEMORY.md` (AutoMem), 팀 메모리 (TeamMem), 세션 메모리                            | Claude가 작성 | 세션 간 기억 유지            |

이 문서는 **사용자 지시 메모리** — 특히 `CLAUDE.md`와 `.claude/rules/*.md` — 에 집중한다.

---

## 2. 메모리 타입 분류

`src/utils/claudemd.ts` 최상단 주석 및 `MemoryType` 타입에서 확인:

```
Managed   → /etc/claude-code/CLAUDE.md          (시스템 관리자용, 전체 사용자 적용)
User      → ~/.claude/CLAUDE.md                 (개인 전역 지침)
           → ~/.claude/rules/*.md
Project   → <project>/CLAUDE.md                 (프로젝트 지침, git 추적)
           → <project>/.claude/CLAUDE.md
           → <project>/.claude/rules/*.md
Local     → <project>/CLAUDE.local.md           (개인 프로젝트 지침, git 미추적)
AutoMem   → ~/.claude/projects/<id>/memory.md   (Claude 자동 메모리)
TeamMem   → (feature 'TEAMMEM' 활성 시)        (팀 공유 메모리)
```

**로딩 우선순위**: Managed → User → Project(루트→CWD 방향) → Local  
나중에 로딩된 파일이 더 높은 우선순위를 가진다(모델이 더 주목).

---

## 3. CLAUDE.md vs rules/*.md 의 차이

### 3-1. 공통점

- 둘 다 `MemoryFileInfo` 타입으로 처리됨
- 둘 다 frontmatter를 지원함 (`parseFrontmatterPaths` 경유)
- 둘 다 `@include` 지시자로 다른 파일을 포함할 수 있음
- 둘 다 HTML 주석(`<!-- -->`) 자동 제거 후 로딩됨

### 3-2. 핵심 차이: `paths` frontmatter 유무

```typescript
// src/utils/claudemd.ts:765-775
} else
if (isFile && entry.name.endsWith('.md')) {
    const files = await processMemoryFile(...)
    result.push(
        ...files.filter(f => (conditionalRule ? f.globs : !f.globs)),
    )
}
```

- **`CLAUDE.md`**: `paths` frontmatter 미지원. 항상 로딩됨.
- **`rules/*.md` (paths 없음)**: 무조건 로딩 (unconditional rule)
- **`rules/*.md` (paths 있음)**: 해당 경로 접근 시에만 로딩 (conditional rule)

이 분기가 `conditionalRule` 플래그로 제어된다.

---

## 4. rules/*.md 가 지원하는 frontmatter

`src/utils/frontmatterParser.ts`의 `FrontmatterData` 타입에서 확인:

```yaml
---
# rules/*.md에서 의미 있는 필드들
paths: "src/**/*.ts, tests/**"   # 글로브 패턴 (conditional rule 전환 핵심)

# 기타 지원 필드 (rules/*.md 맥락에서는 제한적으로 사용됨)
description: "설명 텍스트"
allowed-tools: "Bash, Read"
type: "user"                      # 메모리 타입 (AutoMem 파일용)
argument-hint: "힌트"
version: "1.0"
hide-from-slash-command-tool: "false"
model: "sonnet"
skills: "skill1, skill2"
user-invocable: "true"
hooks: { ... }
effort: "high"
context: "inline"                 # 'inline' | 'fork'
agent: "general-purpose"
shell: "bash"                     # 'bash' | 'powershell'
---
```

> **실질적으로 rules/*.md에서 사용되는 핵심 필드는 `paths`이다.**  
> 나머지 필드들은 skills/, commands/ 파일에서 주로 사용되며, rules/*.md 컨텍스트에서의 동작은 대부분 무시되거나 부수적이다.

### paths 필드 처리 방식

`splitPathInFrontmatter()` 함수가 처리:

- 쉼표 구분 문자열 또는 YAML 배열 허용
- `{ts,tsx}` 형태의 brace 확장 지원
- `/**` 접미사 자동 제거 (ignore 라이브러리 호환)
- `**` 단독 패턴은 "전체 매치"로 처리 → paths 없는 것과 동일

### paths 패턴 문법

**정규식은 지원하지 않는다.** `paths`는 **glob 패턴 (gitignore 스타일)** 만 지원한다.

매칭 엔진: `ignore` 라이브러리 (`src/utils/claudemd.ts:1395`)

```typescript
return ignore().add(file.globs).ignores(relativePath)
```

#### 지원하는 패턴

| 패턴 | 의미 |
|------|------|
| `src/**/*.ts` | src 하위 전체 .ts 파일 |
| `src/**/*.{ts,tsx}` | brace 확장 — `src/**/*.ts`, `src/**/*.tsx` 로 펼쳐짐 |
| `tests/` | tests 디렉토리 전체 |
| `*.md` | 현재 디렉토리의 모든 .md 파일 |
| `!node_modules` | 제외 패턴 (gitignore 문법) |
| `src/**` | src 하위 전체 (단, `/**` 는 로딩 시 자동 제거됨) |

#### 지원하지 않는 것

- 정규식 (`/^src\/.+\.ts$/` 형태)

#### 실제 사용 예시

```yaml
# src 디렉토리의 TypeScript 파일 접근 시에만 활성화
---
paths: "src/**/*.{ts,tsx}"
---
TypeScript 코드 작성 시 다음 규칙을 따르세요: ...
```

```yaml
# YAML 배열 형태도 허용
---
paths:
  - "src/**/*.ts"
  - "tests/**/*.spec.ts"
---
테스트 파일과 소스 파일 작성 시 규칙: ...
```

#### 매칭 기준 경로

글로브 패턴의 기준 디렉토리는 rules 파일의 위치에 따라 다르다 (`src/utils/claudemd.ts:1376-1380`):

```typescript
const baseDir =
  type === 'Project'
    ? dirname(dirname(rulesDir))  // .claude/rules/ → 프로젝트 루트
    : getOriginalCwd()            // Managed/User rules → 현재 CWD
```

즉, `<project>/.claude/rules/` 안의 rule이라면 `<project>/src/**/*.ts` 처럼 프로젝트 루트 기준으로 매칭된다.

---

## 5. 대화 턴 라이프사이클에서의 위치

### 5-1. CLAUDE.md / 무조건 rules (unconditional rules)

**세션 시작 시 1회 로딩 → 매 API 호출마다 메시지 맨 앞에 주입**

```
세션 시작
  └─ getMemoryFiles() [memoize]
       └─ processMemoryFile() + processMdRules()
            └─ MemoryFileInfo[] 반환 (캐시됨)

매 API 호출 (query.ts)
  └─ prependUserContext(messages, userContext)
       └─ userContext.claudeMd = getClaudeMds(filterInjectedMemoryFiles(memoryFiles))
            └─ 모든 메모리 파일 내용을 하나의 문자열로 합침
       └─ [UserMessage(<system-reminder>...claudeMd 내용...</system-reminder>), ...원본 메시지들]
```

**위치**: 메시지 배열의 **인덱스 0** (맨 앞의 UserMessage)

소스 (`src/utils/api.ts:449-474`):

```typescript
export function prependUserContext(
    messages: Message[],
    context: { [k: string]: string },
): Message[] {
    return [
        createUserMessage({
            content: `<system-reminder>\nAs you answer the user's questions, you can use the following context:\n${
                Object.entries(context)
                    .map(([key, value]) => `# ${key}\n${value}`)
                    .join('\n')
            }\n\nIMPORTANT: this context may or may not be relevant to your tasks...\n</system-reminder>\n`,
            isMeta: true,
        }),
        ...messages,
    ]
}
```

### 5-2. 조건부 rules (conditional rules, paths 있음)

**파일 접근 시점 → 다음 어시스턴트 응답 전 주입**

```
사용자 입력 처리
  └─ 파일 읽기/쓰기/편집 도구 호출
       └─ nestedMemoryAttachmentTriggers.add(filePath)

다음 API 호출 준비
  └─ getAttachments(toolUseContext)
       └─ getNestedMemoryAttachments(toolUseContext)
            └─ nestedMemoryAttachmentTriggers 순회
                 └─ getNestedMemoryAttachmentsForFile(filePath)
                      └─ getConditionalRulesForCwdLevelDirectory()
                      └─ getMemoryFilesForNestedDirectory() [중첩 디렉토리]
                      └─ getManagedAndUserConditionalRules()
                      └─ filePath 글로브 매칭 → 해당 rules/*.md 선택
                 └─ <system-reminder> 래핑된 UserMessage로 변환
       └─ nestedMemoryAttachmentTriggers.clear()
```

**위치**: 매 턴의 어시스턴트 응답 바로 전, **attachment** 형태로 주입  
(메시지 배열에서 tool_result 블록 다음, 또는 현재 user 메시지에 포함)

---

## 6. getMemoryFiles() 캐싱 메커니즘

```typescript
// src/utils/claudemd.ts:790
export const getMemoryFiles = memoize(
    async (forceIncludeExternal: boolean = false): Promise<MemoryFileInfo[]> => { ...
    }
)
```

**memoize**: 같은 인수로 호출 시 캐시된 결과 반환.  
캐시는 다음 상황에서 무효화됨:

| 함수                                    | 사용 시점                        | InstructionsLoaded 훅 실행 |
|---------------------------------------|------------------------------|-------------------------|
| `clearMemoryFileCaches()`             | worktree 전환, /memory 다이얼로그 등 | **아니오**                 |
| `resetGetMemoryFilesCache('compact')` | 컴팩션 후 재로딩                    | **예**                   |

---

## 7. 시스템 프롬프트 vs. 메시지 배열 — 어디에 위치하는가

Claude API는 `system` 파라미터와 `messages` 배열을 분리한다.

```
API 호출 구조:
  system: [
    "기본 Claude Code 시스템 프롬프트",        ← getSystemPrompt()
    "gitStatus: ...\ncurrentDate: ...",          ← appendSystemContext(systemContext)
  ]
  messages: [
    { role: "user", content: "<system-reminder>\n# claudeMd\n...\n# currentDate\n...\n</system-reminder>" },  ← prependUserContext
    { role: "user", content: "사용자 실제 입력" },
    { role: "assistant", content: "..." },
    ...
  ]
```

> **핵심**: `CLAUDE.md` 내용은 `system` 파라미터가 **아니라** `messages[0]`에 `<system-reminder>` 태그로 주입된다.  
> 이는 프롬프트 캐시(prompt caching)에서 시스템 프롬프트가 변경될 때 캐시가 깨지는 것을 방지하기 위한 설계이다.

---

## 8. @include 지시자

CLAUDE.md 내에서 다른 파일을 포함할 수 있다:

```markdown
@path/to/file.md → 상대 경로 (CLAUDE.md 위치 기준)
@./relative/path.md → 명시적 상대 경로
@~/home/path.md → 홈 디렉토리 기준
@/absolute/path.md → 절대 경로
```

**동작**:

- 포함된 파일이 포함하는 파일보다 **먼저** 배치됨 (includes first)
- 최대 깊이 5단계 (`MAX_INCLUDE_DEPTH = 5`)
- 순환 참조 방지 (processedPaths 추적)
- 코드 블록 내 `@path`는 무시됨 (Lexer 기반 파싱)
- 존재하지 않는 파일은 조용히 무시됨

---

## 9. 로딩 순서 전체 흐름

```
세션 시작
│
├─ [1] Managed CLAUDE.md         (/etc/claude-code/CLAUDE.md)
├─ [2] Managed rules/*.md        (/etc/claude-code/.claude/rules/)
├─ [3] User CLAUDE.md            (~/.claude/CLAUDE.md)
├─ [4] User rules/*.md           (~/.claude/rules/)
│
│  (루트 → CWD 방향으로 각 디렉토리마다)
├─ [5] Project CLAUDE.md         (<dir>/CLAUDE.md)
├─ [6] Project .claude/CLAUDE.md (<dir>/.claude/CLAUDE.md)
├─ [7] Project rules/*.md        (<dir>/.claude/rules/) [paths 없는 것만]
├─ [8] Local CLAUDE.local.md     (<dir>/CLAUDE.local.md)
│
├─ [9] AutoMem memory.md         (MEMORY.md, feature 활성 시)
└─ [10] TeamMem                  (feature 'TEAMMEM' 활성 시)

API 호출 시
└─ [5~8] 조건부 rules/*.md       (paths 있는 것, 파일 접근 시점에 추가)
```

---

## 10. 요약 비교표

| 항목                       | CLAUDE.md                       | rules/*.md (조건 없음)              | rules/*.md (paths 있음)               |
|--------------------------|---------------------------------|---------------------------------|-------------------------------------|
| **로딩 시점**                | 세션 시작                           | 세션 시작                           | 파일 접근 시                             |
| **주입 위치**                | messages[0] `<system-reminder>` | messages[0] `<system-reminder>` | 해당 턴 attachment `<system-reminder>` |
| **캐싱**                   | memoize (세션 유지)                 | memoize (세션 유지)                 | 매 파일 접근마다 평가                        |
| **paths frontmatter**    | 미지원                             | 없음 (paths 없는 rules)             | 있음 (paths 있는 rules)                 |
| **@include**             | 지원                              | 지원                              | 지원                                  |
| **우선순위**                 | 로딩 순서 기반                        | 로딩 순서 기반                        | 파일 접근 시점 기반                         |
| **InstructionsLoaded 훅** | 세션 시작 시 발동                      | 세션 시작 시 발동                      | 발동 안 됨 (attachment)                 |

---

## 11. 중요 구현 세부 사항

### 11-1. `isMeta: true` 플래그

`prependUserContext`로 주입되는 메시지는 `isMeta: true`로 마킹된다.  
이 플래그는 UI 렌더링, 토큰 집계, 대화 요약 등에서 "사용자 실제 입력이 아님"을 구분하는 데 사용된다.

### 11-2. 프롬프트 캐시 안정성

`getUserContext()`는 `memoize`로 감싸져 세션 동안 동일한 문자열을 반환한다.  
`prependUserContext`가 매 API 호출마다 동일한 내용을 첫 번째 메시지로 앞에 붙이므로, 캐시 키(prompt prefix)가 안정적으로 유지된다.

### 11-3. AutoMem/TeamMem은 InstructionsLoaded 훅 제외

`isInstructionsMemoryType()` 함수가 User/Project/Local/Managed 타입만 포함한다.  
AutoMem, TeamMem은 "지시 파일"이 아닌 "기억 파일"로 분류되어 훅이 발동되지 않는다.

### 11-4. claudeMdExcludes 설정

`settings.claudeMdExcludes` (glob 패턴 배열)에 등록된 경로의 CLAUDE.md 파일은 로딩 제외된다.  
Managed/AutoMem/TeamMem 타입에는 적용되지 않는다.

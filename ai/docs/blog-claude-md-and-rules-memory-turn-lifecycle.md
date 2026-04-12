# Claude Code가 CLAUDE.md를 읽는 방법: 메모리 타입과 턴 라이프사이클 완전 분석

Claude Code를 쓰다 보면 자연스럽게 궁금해진다.

CLAUDE.md에 적어둔 지침은 정확히 언제 읽히는 걸까? `rules/*.md`는 CLAUDE.md와 어떻게 다를까? 매 응답마다 다시 읽는 걸까, 아니면 캐싱되는 걸까?

---

## 메모리 파일의 두 가지 종류

Claude Code는 "메모리 파일"을 크게 두 종류로 구분한다.

| 종류             | 파일                                                                        | 관리 주체      | 목적                    |
|----------------|---------------------------------------------------------------------------|------------|-----------------------|
| **사용자 지시 메모리** | `CLAUDE.md`, `.claude/CLAUDE.md`, `.claude/rules/*.md`, `CLAUDE.local.md` | 사람이 작성     | 프로젝트·팀·조직 지침을 모델에게 전달 |
| **자동 관리 메모리**  | `MEMORY.md` (AutoMem), 팀 메모리 (TeamMem), 세션 메모리                            | Claude가 작성 | 세션 간 기억 유지            |

이 글은 **사용자 지시 메모리** — 특히 `CLAUDE.md`와 `.claude/rules/*.md` — 에 집중한다.

---

## 메모리 타입 분류: 어디에 두느냐가 의미를 결정한다

총 6가지 메모리 타입이 있으며, 위치에 따라 적용 범위가 달라진다.

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

**로딩 우선순위**: Managed → User → Project(루트 → CWD 방향) → Local

나중에 로딩된 파일일수록 모델이 더 주목한다. 즉, 프로젝트 지침이 전역 지침보다 우선하고, 로컬 지침이 프로젝트 지침보다 우선한다.

---

## CLAUDE.md vs rules/*.md: 같아 보이지만 다르다

두 파일 형식은 표면적으로 비슷하지만, 핵심적인 차이가 하나 있다.

### 공통점

- 둘 다 동일한 파일 정보 구조로 처리된다.
- 둘 다 frontmatter를 지원한다.
- 둘 다 `@include` 지시자로 다른 파일을 포함할 수 있다.
- 둘 다 HTML 주석(`<!-- -->`)은 자동으로 제거된 후 로딩된다.

### 핵심 차이: `paths` frontmatter

- **`CLAUDE.md`**: `paths` frontmatter 미지원. 항상 로딩된다.
- **`rules/*.md` (paths 없음)**: 항상 로딩된다. (unconditional rule)
- **`rules/*.md` (paths 있음)**: 해당 경로에 접근할 때만 로딩된다. (conditional rule)

`paths` 하나로 로딩 전략 전체가 바뀐다.

---

## rules/*.md가 지원하는 frontmatter

다음 필드들을 frontmatter에서 사용할 수 있다.

```yaml
---
# rules/*.md에서 의미 있는 필드들
paths: "src/**/*.ts, tests/**"   # 글로브 패턴 (conditional rule 전환 핵심)

# 기타 지원 필드
description: "설명 텍스트"
allowed-tools: "Bash, Read"
type: "user"
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

**실질적으로 rules/*.md에서 사용되는 핵심 필드는 `paths`다.** 나머지 필드들은 skills/, commands/ 파일에서 주로 쓰이며, rules/*.md 컨텍스트에서는 대부분 무시되거나 부수적인
역할에 그친다.

### paths 필드 처리 방식

- 쉼표 구분 문자열 또는 YAML 배열 모두 허용
- `{ts,tsx}` 형태의 brace 확장 지원
- `/**` 접미사 자동 제거 (ignore 라이브러리 호환)
- `**` 단독 패턴은 "전체 매치"로 처리 → paths 없는 것과 동일하게 동작

### paths 패턴 문법

**정규식은 지원하지 않는다.** `paths`는 **glob 패턴 (gitignore 스타일)** 만 지원한다.

#### 지원하는 패턴

| 패턴 | 의미 |
|---|---|
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

글로브 패턴의 기준 디렉토리는 rules 파일의 위치에 따라 다르다.

- **Project rules** (`<project>/.claude/rules/`): 프로젝트 루트 기준으로 매칭
- **Managed/User rules**: 현재 CWD 기준으로 매칭

즉, `<project>/.claude/rules/` 안의 rule이라면 `src/**/*.ts` 패턴은 프로젝트 루트 기준으로 해석된다.

---

## 턴 라이프사이클에서의 주입 시점

이 부분이 이 글에서 가장 핵심적인 내용이다.

### CLAUDE.md / unconditional rules: 세션 시작 시 1회, 매 API 호출마다 주입

```
세션 시작
  └─ 메모리 파일 목록 로딩 및 캐싱

매 API 호출
  └─ 모든 메모리 파일 내용을 하나의 문자열로 합침
  └─ [UserMessage(<system-reminder>...내용...</system-reminder>), ...원본 메시지들]
```

파일 자체는 세션 시작 시 1회만 읽히지만, 그 내용은 **매 API 호출마다** 메시지 배열의 첫 번째 위치에 주입된다.

**주입 위치**: 메시지 배열의 인덱스 0 (맨 앞의 UserMessage)

### conditional rules (paths 있음): 파일 접근 시점에 주입

```
사용자 입력 처리
  └─ 파일 읽기/쓰기/편집 도구 호출
       └─ 접근한 파일 경로를 트리거 목록에 추가

다음 API 호출 준비
  └─ 트리거 목록 순회
       └─ 각 파일 경로에 대해 글로브 패턴 매칭
            └─ 매칭된 rules/*.md 선택
            └─ <system-reminder> 형태로 래핑
  └─ 트리거 목록 초기화
```

**주입 위치**: 매 턴의 어시스턴트 응답 직전, attachment 형태로 주입 (tool_result 블록 다음, 또는 현재 user 메시지에 포함)

---

## 캐싱 메커니즘

메모리 파일 목록은 세션 시작 시 한 번만 로딩되어 캐시된다. 같은 조건으로 재요청하면 캐시된 결과를 그대로 반환한다.

캐시가 무효화되는 시점은 두 가지다.

| 시점        | 상황                           | InstructionsLoaded 훅 실행 |
|-----------|------------------------------|-------------------------|
| 수동 무효화    | worktree 전환, /memory 다이얼로그 등 | **아니오**                 |
| 컴팩션 후 재로딩 | 대화 압축(compact) 이후            | **예**                   |

---

## 시스템 프롬프트가 아니라 메시지 배열에 들어간다

Claude API는 `system` 파라미터와 `messages` 배열을 구분한다. CLAUDE.md 내용은 어디에 들어갈까?

```
API 호출 구조:
  system: [
    "기본 Claude Code 시스템 프롬프트",
    "gitStatus: ...\ncurrentDate: ...",
  ]
  messages: [
    { role: "user", content: "<system-reminder>\n# claudeMd\n...\n</system-reminder>" },
    { role: "user", content: "사용자 실제 입력" },
    { role: "assistant", content: "..." },
    ...
  ]
```

**핵심**: CLAUDE.md 내용은 `system` 파라미터가 아니라 `messages[0]`에 `<system-reminder>` 태그로 주입된다.

이는 의도적인 설계다. 시스템 프롬프트가 변경되면 프롬프트 캐시(prompt caching)가 깨지기 때문에, CLAUDE.md처럼 세션마다 달라질 수 있는 내용을 메시지 배열에 넣어 캐시 안정성을 유지한다. 메모리
파일 내용은 세션 동안 캐싱되어 동일한 문자열을 반환하므로, 매 API 호출마다 동일한 내용을 첫 번째 메시지로 붙여도 캐시 키(prompt prefix)는 안정적으로 유지된다.

---

## @include 지시자

CLAUDE.md 내에서 다른 파일을 포함할 수 있다.

```markdown
@path/to/file.md → 상대 경로 (CLAUDE.md 위치 기준)
@./relative/path.md → 명시적 상대 경로
@~/home/path.md → 홈 디렉토리 기준
@/absolute/path.md → 절대 경로
```

동작 규칙:

- 포함된 파일이 포함하는 파일보다 **먼저** 배치된다 (includes first)
- 최대 깊이 5단계
- 순환 참조 방지
- 코드 블록 내 `@path`는 무시된다
- 존재하지 않는 파일은 조용히 무시된다

---

## 로딩 순서 전체 흐름

세션 시작부터 API 호출까지 전체 흐름을 정리하면 다음과 같다.

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

## 요약 비교표

| 항목                       | CLAUDE.md                       | rules/*.md (paths 없음)           | rules/*.md (paths 있음)               |
|--------------------------|---------------------------------|---------------------------------|-------------------------------------|
| **로딩 시점**                | 세션 시작                           | 세션 시작                           | 파일 접근 시                             |
| **주입 위치**                | messages[0] `<system-reminder>` | messages[0] `<system-reminder>` | 해당 턴 attachment `<system-reminder>` |
| **캐싱**                   | 세션 유지                           | 세션 유지                           | 매 파일 접근마다 평가                        |
| **paths frontmatter**    | 미지원                             | 없음                              | 있음                                  |
| **@include**             | 지원                              | 지원                              | 지원                                  |
| **우선순위**                 | 로딩 순서 기반                        | 로딩 순서 기반                        | 파일 접근 시점 기반                         |
| **InstructionsLoaded 훅** | 세션 시작 시 발동                      | 세션 시작 시 발동                      | 발동 안 됨 (attachment)                 |

---

## 알아두면 좋은 세부 동작

### 메타 메시지 처리

CLAUDE.md 내용이 담긴 주입 메시지는 "사용자 실제 입력이 아님"으로 내부적으로 구분된다. 이 구분은 UI 렌더링, 토큰 집계, 대화 요약 등에 영향을 준다.

### 프롬프트 캐시 안정성

메모리 파일 내용은 세션 동안 캐싱되어 매번 동일한 문자열을 반환한다. 이 덕분에 매 API 호출마다 동일한 내용을 첫 번째 메시지로 붙여도 캐시 키가 안정적으로 유지된다.

### AutoMem/TeamMem은 InstructionsLoaded 훅 제외

AutoMem과 TeamMem은 "지시 파일"이 아닌 "기억 파일"로 분류된다. 따라서 이 파일들이 로딩될 때는 InstructionsLoaded 훅이 발동되지 않는다.

### claudeMdExcludes 설정

설정에서 특정 glob 패턴을 등록하면, 해당 경로의 CLAUDE.md 파일을 로딩에서 제외할 수 있다. 단, Managed/AutoMem/TeamMem 타입에는 적용되지 않는다.

---

## 마치며

CLAUDE.md와 rules/*.md는 같은 마크다운 파일처럼 보이지만, 로딩 시점, 주입 위치, 캐싱 전략이 모두 다르다. `paths` frontmatter 하나가 그 경계를 가른다.

지침 파일을 더 세밀하게 제어하고 싶다면:

- **항상 적용되어야 하는 지침** → `CLAUDE.md` 또는 paths 없는 `rules/*.md`
- **특정 파일 작업 시에만 적용되어야 하는 지침** → `paths`가 있는 `rules/*.md`

이 차이를 이해하면 Claude Code의 컨텍스트 주입 메커니즘을 훨씬 의도적으로 활용할 수 있다.

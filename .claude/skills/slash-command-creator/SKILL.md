---
name: slash-command-creator
description: |
  Claude Code 슬래시 커맨드를 생성하고 관리합니다.
  반복 사용하는 프롬프트를 /커맨드로 자동화할 때 반드시 사용하세요.

  다음 상황에서 적극적으로 사용합니다:
  - 새 슬래시 커맨드 생성 또는 기존 커맨드 수정/편집
  - "커맨드 만들어줘", "/xxx 만들어줘", "자동화하고 싶어" 같은 요청
  - 슬래시 커맨드 문법, frontmatter 옵션에 대해 질문할 때
  - $ARGUMENTS, $1/$2, bash 실행(!), 파일 참조(@) 활용법 질문
  - allowed-tools, argument-hint, model, disable-model-invocation 설정
  - 반복 작업, 워크플로우, CI/CD 작업을 슬래시 커맨드로 만들 때
  - 프로젝트(shared) vs 개인(personal) 커맨드 스코프 선택 질문
---

# 슬래시 커맨드 생성기

자주 사용하는 프롬프트를 슬래시 커맨드로 자동화합니다.

## 사용자 돕는 방법

사용자가 슬래시 커맨드를 요청하면, 의도가 불명확할 경우 다음 질문으로 안내합니다:

1. **무엇을 자동화하고 싶은가?** - 반복하는 작업이나 프롬프트를 파악
2. **인수가 필요한가?** - 고정 명령이면 인수 불필요, 가변 입력이면 `$ARGUMENTS` 또는 `$1/$2`
3. **bash 실행이 필요한가?** - git 상태, 파일 목록 등 컨텍스트를 동적으로 가져올 때
4. **프로젝트 공유 vs 개인 전용?** - 팀 전체가 쓸 것인지 본인만 쓸 것인지

정보가 충분하면 바로 `scripts/init_command.py`로 파일을 생성하고 사용자에게 보여준다.

## Quick Start

프로젝트 루트에서 실행:

```bash
python .claude/skills/slash-command-creator/scripts/init_command.py <command-name> \
  [--scope project|personal] [--namespace <ns>] \
  [--description "..."] [--body "..."] [--allowed-tools "..."] \
  [--argument-hint "..."] [--model <model-id>] [--disable-model-invocation]
```

## 커맨드 구조

슬래시 커맨드는 선택적 YAML frontmatter를 가진 Markdown 파일입니다:

```markdown
---
description: /help에 표시되는 짧은 설명
---

프롬프트 내용을 여기에 작성하세요.

$ARGUMENTS
```

### 파일 위치

| 스코프  | 경로                    | /help 표시  |
|------|-----------------------|-----------|
| 프로젝트 | `.claude/commands/`   | (project) |
| 개인   | `~/.claude/commands/` | (user)    |

### 네임스페이스

하위 디렉토리로 커맨드를 분류합니다:

- `.claude/commands/frontend/component.md` → `/component` — "(project:frontend)" 표시
- `~/.claude/commands/backend/api.md` → `/api` — "(user:backend)" 표시

## 기능

### 인수(Arguments)

**전체 인수** - `$ARGUMENTS`:

```markdown
코딩 컨벤션에 따라 이슈 #$ARGUMENTS를 수정하세요.

# /fix-issue 123 → "...이슈 #123을 수정하세요."
```

**위치 인수** - `$1`, `$2` 등:

```markdown
PR #$1을 $2 우선순위로 리뷰하세요.

# /review 456 high → "PR #456을 high 우선순위로..."
```

### Bash 실행

`` !`command` `` 형태로 셸 명령을 실행하고 그 출력을 프롬프트에 삽입합니다 (`allowed-tools` frontmatter 필요):

```markdown
---
allowed-tools: Bash(git status:*), Bash(git diff:*)
---

현재 상태: !`git status`
변경사항: !`git diff HEAD`
```

### 파일 참조

`@` 접두사로 파일 내용을 프롬프트에 포함합니다:

```markdown
@src/utils/helpers.js 파일의 문제를 검토하세요.
@$1과 @$2를 비교하세요.
```

## Frontmatter 옵션

| 필드                         | 용도                           | 필수 여부 |
|----------------------------|------------------------------|-------|
| `description`              | /help에 표시되는 짧은 설명            | 권장    |
| `allowed-tools`            | 커맨드가 사용할 수 있는 도구             | 선택    |
| `argument-hint`            | 자동완성에 표시할 인수 힌트              | 선택    |
| `model`                    | 사용할 모델 지정 (기본: `inherit`)    | 선택    |
| `effort`                   | 추론 노력 레벨 (기본: `inherit`)     | 선택    |
| `disable-model-invocation` | 프로그래매틱 호출 방지                  | 선택    |

상세 레퍼런스: [references/frontmatter.md](references/frontmatter.md)

## 예시

전체 예시는 [references/examples.md](references/examples.md)를 참조하세요:

- 단순 리뷰/설명 커맨드
- 위치 인수가 있는 커맨드
- Bash 실행을 활용한 git 워크플로우 커맨드
- 프론트엔드/백엔드 네임스페이스 커맨드

## 생성 워크플로우

1. **의도 파악**: 어떤 프롬프트를 반복하는가? 인수가 필요한가?
2. **스코프 결정**: 팀 공유(project) vs 개인 전용(personal)
3. **생성**: `python .claude/skills/slash-command-creator/scripts/init_command.py <name>` 실행
4. **파일 확인 및 편집**: 생성된 파일의 description과 body를 사용자와 함께 검토
5. **테스트**: Claude Code에서 `/command-name` 실행

## 기능 선택 가이드

| 상황                    | 사용할 기능                             |
|-----------------------|------------------------------------|
| 단순 텍스트 자동화            | 본문에 직접 작성                          |
| 가변 입력 하나              | `$ARGUMENTS`                       |
| 여러 위치 인수              | `$1`, `$2`, ... + `argument-hint`  |
| git 상태·diff 등 동적 컨텍스트 | `!` bash 실행 + `allowed-tools`      |
| 파일 내용 포함              | `@filepath` 또는 `@$1`               |
| 빠른 단순 작업              | `model: claude-haiku-4-5-20251001` |
| 외부 호출 방지              | `disable-model-invocation: true`   |

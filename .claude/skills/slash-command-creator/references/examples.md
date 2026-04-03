# 슬래시 커맨드 예시 모음

## 단순 커맨드

### 코드 리뷰 커맨드
```markdown
---
description: 버그 및 개선사항 코드 리뷰
---

다음 코드를 아래 항목으로 검토하세요:
- 보안 취약점
- 성능 문제
- 코드 스타일 위반
- 잠재적 버그

$ARGUMENTS
```

### 코드 설명 커맨드
```markdown
---
description: 코드를 쉬운 말로 설명
---

다음 코드를 누구나 이해할 수 있는 쉬운 말로 설명하세요:

$ARGUMENTS
```

## 인수가 있는 커맨드

### 단일 인수 (`$ARGUMENTS`)
```markdown
---
description: GitHub 이슈 수정
---

코딩 컨벤션에 따라 이슈 #$ARGUMENTS를 수정하세요.
```

### 위치 인수 (`$1`, `$2` 등)
```markdown
---
argument-hint: [pr-number] [priority] [assignee]
description: 풀 리퀘스트 리뷰
---

PR #$1을 $2 우선순위로 $3에게 할당하여 리뷰하세요.
보안, 성능, 코드 스타일에 집중하세요.
```

## Bash 실행 커맨드

`` !`command` `` 형태로 셸 명령을 실행하고 출력을 컨텍스트에 포함합니다.

### Git 커밋 커맨드
```markdown
---
allowed-tools: Bash(git add:*), Bash(git status:*), Bash(git commit:*)
description: git 커밋 생성
---

## 컨텍스트

- 현재 git 상태: !`git status`
- 현재 변경사항: !`git diff HEAD`
- 현재 브랜치: !`git branch --show-current`
- 최근 커밋: !`git log --oneline -10`

## 작업

위 변경사항을 바탕으로 커밋 하나를 생성하세요.
```

### 배포 커맨드
```markdown
---
allowed-tools: Bash(npm:*), Bash(docker:*)
argument-hint: [environment]
description: 지정한 환경에 배포
---

## 현재 상태

- 브랜치: !`git branch --show-current`
- 최근 커밋: !`git log -1 --oneline`

## $1 환경 배포

$1 환경에 대한 배포 프로세스를 실행하세요.
```

## 파일 참조 커맨드

`@filepath` 형태로 파일 내용을 프롬프트에 포함합니다. 경로는 프로젝트 루트 기준 상대경로를 사용합니다.

### 구현 리뷰 커맨드
```markdown
---
description: 스펙에 맞게 구현 리뷰
---

@src/utils/helpers.js 구현이 스펙에 부합하는지 검토하세요.
```

### 파일 비교 커맨드
```markdown
---
argument-hint: [old-file] [new-file]
description: 두 파일 비교
---

@$1과 @$2를 비교하고 차이점을 요약하세요.
```

## 네임스페이스 커맨드

하위 디렉토리에 배치한 커맨드는 `/help`에 네임스페이스와 함께 표시됩니다.

### 프론트엔드 컴포넌트 (`.claude/commands/frontend/component.md`)
```markdown
---
description: React 컴포넌트 생성
---

다음 요구사항으로 React 컴포넌트를 생성하세요:

$ARGUMENTS

프론트엔드 코딩 컨벤션을 따르고 TypeScript를 사용하세요.
```

### 백엔드 API (`.claude/commands/backend/api.md`)
```markdown
---
description: API 엔드포인트 생성
---

다음을 위한 REST API 엔드포인트를 생성하세요:

$ARGUMENTS

유효성 검사, 에러 처리, 문서화를 포함하세요.
```

## 심층 분석 커맨드

복잡한 문제를 단계별로 분석하는 커맨드 패턴. "think step by step" 같은 표현은 Claude가 더 신중하게 추론하도록 유도합니다. 이것은 별도 Claude Code 기능이 아니라 프롬프팅 기법입니다.

```markdown
---
description: 아키텍처 심층 분석
---

다음의 아키텍처 영향을 단계별로 생각하세요:

$ARGUMENTS

확장성, 유지보수성, 성능을 고려하세요.
```

---
description: 블로그 글 개요 작성
allowed-tools: Task, Read, Write, Glob
---

## 개요 작성

리서치 결과를 바탕으로 블로그 글 개요를 작성합니다.

### 선정된 인사이트

@content/selected/*.md

### 리서치 결과

@content/research/*.md

### 작업 지시

1. `content/selected/`에서 선정된 인사이트 목록을 확인합니다
2. **선정된 인사이트가 여러 개인 경우, `outline-writer` 서브에이전트를 병렬로 호출합니다**
   - 각 인사이트마다 별도의 Task 호출
   - 동시에 여러 개요 작성 (시간 절약)
3. 각 개요에 다음을 포함합니다:
   - 제목과 타겟 독자
   - 도입부 훅 아이디어
   - 각 섹션의 핵심 포인트
   - 코드 예시 계획
   - 결론 및 CTA
4. 개요는 `content/outlines/{date}-{topic-slug}-outline.md`에 저장됩니다
5. 모든 개요 작성 완료 후 워크플로우 상태를 업데이트합니다

### 병렬 처리 예시

선정된 인사이트가 3개인 경우:
```
Task 1: outline-writer → prompt-engineering-outline.md
Task 2: outline-writer → mcp-development-outline.md
Task 3: outline-writer → saas-launch-outline.md
(동시 실행)
```

### 추가 인수

$ARGUMENTS

빈 경우: 선정된 모든 인사이트에 대해 개요 작성
숫자 지정 시 (예: "1"): 해당 우선순위의 인사이트만 개요 작성

---
name: blog-rewriter
description: 블로그 문서를 검토하고 개선합니다. blog-reviewer 에이전트로 문서를 검증한 뒤 blog-writer 에이전트로 개선된 최종본을 생성합니다. 블로그 포스트 품질 향상이 필요할 때 사용하세요.
---

# Blog Rewriter

블로그 문서를 blog-reviewer → blog-writer 순서로 처리하여 품질을 개선합니다.

## 실행 흐름

```
입력 문서
   ↓
[blog-reviewer 에이전트] 5가지 관점 검증 + <review-*> 마킹 삽입
   ↓ ${CLAUDE_TMP_DIR}/{filename}.marked.md
[blog-writer 에이전트] 마킹 기반 개선 + 마킹 태그 제거
   ↓ ${CLAUDE_TMP_DIR}/draft.md
최종 출력
```

## 실행

**1단계: blog-reviewer 에이전트 실행**

```
EXECUTE Agent:
  subagent_type: "blog-reviewer"
  prompt: """
  다음 문서를 검증하고 마킹을 삽입하세요.
  문서 경로: {$ARGUMENTS}
  """
```

**2단계: blog-writer 에이전트 실행**

```
EXECUTE Agent:
  subagent_type: "blog-writer"
  prompt: """
  다음 마킹된 문서를 개선하세요.
  문서 경로: ${CLAUDE_TMP_DIR}/{filename}.marked.md
  """
```

**3단계: 결과 보고**

- 최종 파일 경로: `${CLAUDE_TMP_DIR}/draft.md`
- 주요 개선 항목 요약 (CRITICAL 위주, 최대 5개)

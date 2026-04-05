---
name: blog-rewriter
description: 블로그 문서를 검토하고 개선합니다. blog-reviewer 에이전트로 문서를 병렬 검증한 뒤 blog-writer 에이전트로 개선된 최종본을 생성합니다. 블로그 포스트 품질 향상이 필요할 때 사용하세요.
---

# Blog Rewriter

블로그 문서를 blog-reviewer → blog-writer 순서로 처리하여 품질을 개선합니다.

## 실행 흐름

```
입력 문서 ({doc_path})
   ↓
[blog-reviewer 에이전트] 5관점 병렬 검증
   ↓ ${CLAUDE_TMP_DIR}/{filename}/summary.review.md
[blog-writer 에이전트] summary 기반 개선
   ↓
최종 출력
```

---

## 실행

### Step 1: {filename} 결정

입력 경로 `{$ARGUMENTS}`에서 파일명(확장자 제외)을 추출하여 `{filename}`으로 사용한다.

예: `ai/docs/claude-code-lsp.md` → `{filename}` = `claude-code-lsp`

### Step 2: blog-reviewer 에이전트 실행

```
EXECUTE Agent:
  subagent_type: "blog-reviewer"
  prompt: """
  다음 문서를 검증하세요.
  doc_path: {$ARGUMENTS}
  filename: {filename}
  """
```

반환값에서 출력 디렉토리 경로 확인:
`${CLAUDE_TMP_DIR}/{filename}/`

### Step 3: blog-writer 에이전트 실행

```
EXECUTE Agent:
  subagent_type: "blog-writer"
  prompt: """
  다음 문서를 개선하세요.

  원본 문서: {$ARGUMENTS}
  검토 요약: ${CLAUDE_TMP_DIR}/{filename}/summary.review.md
  관점별 상세: ${CLAUDE_TMP_DIR}/{filename}/ 디렉토리

  [작업 순서]
  1. Read 도구로 summary.review.md를 읽는다
  2. CRITICAL 항목을 우선으로 원본 문서 개선
  3. WARNING 항목 반영
  4. 개선된 문서를 원본 경로에 Write한다
  """
```

### Step 4: 결과 보고

- 개선 완료 파일 경로
- 반영된 CRITICAL 항목 수
- 반영된 WARNING 항목 수
- 미반영 항목 (있는 경우)

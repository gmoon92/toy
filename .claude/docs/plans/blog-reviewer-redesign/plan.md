---
name: blog-reviewer-redesign
description: blog-reviewer 에이전트를 병렬 실행 + 관점별 분리 파일 방식으로 재설계
---

# blog-reviewer 재설계 계획

## 목표

기존 순차 실행 + 원본 마킹 방식 → 병렬 실행 + 관점별 분리 파일 방식으로 전환

## 변경 파일

1. `.claude/agents/blog/blog-reviewer.md` — 완전 재작성
2. `.claude/skills/blog-rewriter/SKILL.md` — 연동 흐름 업데이트

## 새 동작 흐름

```
blog-reviewer 에이전트 호출 (doc_path 입력)
  ↓
Step 1: {filename} 추출, ${CLAUDE_TMP_DIR}/{filename}/ 디렉토리 생성
  ↓
Step 2: 5개 Agent 병렬 실행
  ├── standard  → standard.review.md
  ├── technical → technical.review.md
  ├── critical  → critical.review.md
  ├── reader    → reader.review.md
  └── structure → structure.review.md
  ↓
Step 3: 5개 파일 읽어 summary.review.md 생성
  ↓
반환: ${CLAUDE_TMP_DIR}/{filename}/ 경로
```

## 리뷰 파일 포맷

```markdown
---
checklist: standard
doc: ai/docs/some-post.md
date: 2026-04-03
total: {critical: 2, warning: 3, info: 1}
---

## CRITICAL

### STD-1-001 | L12 | 목적 명시 누락
> `"Claude Code는 다양한 기능을..."` ← 해당 라인 앞 40자

목적이 도입부에 명시되지 않았습니다.
**제안**: "이 문서는 X를 위한 Y 가이드입니다."로 시작하세요.

---

## WARNING
...

## INFO
...
```

## Summary 포맷

```markdown
---
doc: ai/docs/some-post.md
date: 2026-04-03
total: {critical: 5, warning: 8, info: 4}
---

## CRITICAL (5)

| 관점 | ID | Line | 요약 |
|------|-----|------|------|
| standard | STD-1-001 | L12 | 목적 명시 누락 |
| technical | TEC-1-001 | L67 | 코드 문법 오류 |

## WARNING (8)
...

## INFO (4)
...
```

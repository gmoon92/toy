---
description: 최종 블로그 글 작성
allowed-tools: Task, Read, Write, Glob
---

## 최종 글 작성

승인된 개요를 바탕으로 최종 블로그 글을 작성합니다.

### 승인된 개요

@content/outlines/*.md

### 리서치 자료

@content/research/*.md

### 작업 지시

1. `blog-writer` 서브에이전트를 호출합니다 (opus 모델)
2. 개요를 따라 완전한 블로그 글을 작성합니다
3. 다음을 포함합니다:
   - 매력적인 도입부
   - 논리적인 본문 구성
   - 실행 가능한 코드 예시
   - 인용 및 데이터
   - 강력한 결론과 CTA
4. 글은 `content/posts/{date}-{topic}.md`에 저장됩니다
5. 워크플로우를 "completed"로 표시합니다

### 품질 기준

- 글쓰기 스타일 가이드 준수
- 코드 예시는 실행 가능하고 주석 포함
- 출처 명확히 표기
- SEO 메타 정보 포함

### 추가 인수

$ARGUMENTS

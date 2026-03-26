# Blog Workflow 문서 재검토 피드백

**재검토일**: 2026-03-26
**검토 대상**: `.claude/tmp/.claude/` 하위 블로그 워크플로우 프로젝트 (34개 파일)
**참조 문서**: `claude-docs-complete-review.md` (기존 리뷰)
**검토 방법**: sequential-thinking MCP 기반 체계적 분석

---

## 1. 기존 리뷰 문서 평가

### 강점
- 34개 파일 전체를 빠짐없이 목록화함
- 아키텍처 다이어그램과 데이터 흐름 시각화가 명확함
- 각 에이전트의 도구 목록, 모델, 출력 위치를 잘 정리함
- 확장 아이디어(소스 다양화, 자동 발행 등) 방향성 제시

### 핵심 약점
- **전 항목 ⭐⭐⭐⭐⭐ 평가**: 아키텍처, 유지보수, 효율성, 문서화, 실용성 모두 5점 만점 → 비판적 분석 부재
- **원본 복사 위주**: 독립적인 검토보다 문서 내용 재현에 가까움
- **핵심 결함 미발견**: 아래 나열된 HIGH/MEDIUM 등급 문제들이 모두 누락됨
- **오타 미수정**: 1.4 섹션 제목 "데이터 폰더 구조" → "폴더"가 올바른 표현
- **다이어그램 중복**: 섹션 1.3과 섹션 7에 동일한 워크플로우 다이어그램 반복

---

## 2. 발견된 문제점

### HIGH — 즉시 수정 필요

#### H1. `research-agent.md` 연도 하드코딩
```
검색 쿼리:
  "[topic] expert opinion 2024"
  "[topic] statistics research data"
  "[topic] case study example"
  "[topic] criticism problems"
  "[topic] trends 2024"
```
**문제**: 현재 날짜는 2026-03-26. 2024 데이터만 수집하면 2년 전 정보 기반 블로그 작성
**영향**: 블로그 콘텐츠의 시의성 직결 — Timeliness 평가 기준과 정면 충돌
**수정안**: `{current_year}` 또는 `{year}` 플레이스홀더로 대체하거나 현재 연도를 동적으로 주입

---

#### H2. `workflow-state.json` 스키마 미정의
5개 에이전트(youtube-collector, insight-extractor, research-agent, outline-writer, blog-writer)가 모두 이 파일을 읽고 업데이트하지만, **파일 형식이 어느 문서에도 정의되어 있지 않음**.

`/status` 명령어도 이 파일에 의존하지만 파싱 방법 불명확.

**영향**: 에이전트별로 다른 형식으로 쓸 경우 상태 추적 불가, 파이프라인 중단
**수정안**: `blog-workflow/SKILL.md` 또는 별도 `workflow-state-schema.md`에 명시적 스키마 정의 필요

예시 스키마 추가가 필요한 구조:
```json
{
  "sessions": [
    {
      "id": "2026-03-26",
      "step": "collected|insights|selected|researched|outlined|completed",
      "artifacts": {
        "raw": ["content/raw/2026-03-26-channel/video.md"],
        "insights": "content/insights/2026-03-26-insights.md",
        "selected": "content/selected/2026-03-26-topic.md",
        "research": "content/research/2026-03-26-topic-research.md",
        "outline": "content/outlines/2026-03-26-topic-outline.md",
        "post": "content/posts/2026-03-26-topic.md"
      }
    }
  ]
}
```

---

#### H3. `templates/post.md`와 `blog-writer.md` 출력 형식 불일치

**templates/post.md** frontmatter (5개 필드):
```yaml
title, date, author, tags, description
```

**blog-writer.md** SEO frontmatter (9개 필드):
```yaml
title, date, slug, description, keywords, tags, author, canonical, sources
```

**누락된 필드**: `slug`, `keywords`, `canonical`, `sources`

**영향**: 템플릿이 실제 에이전트 출력과 다름 → 템플릿으로서의 가이드 역할 상실
**수정안**: templates/post.md를 blog-writer의 SEO frontmatter 기준으로 업데이트

---

### MEDIUM — 단기 수정 권장

#### M1. `outline-writer.md` 영어 전용 — 한국어 시스템과 불일치

outline-writer의 시스템 프롬프트와 모든 출력 구조가 영어:
```
"You are a blog outline specialist..."
"Hook/Introduction", "Section 1", "Practical Takeaways", "Conclusion"
"Target Audience", "Goal", "Estimated Length", "Tone"
```

blog-writer는 한국어 글쓰기에 특화되어 있으나, 개요 단계가 영어로 작성되면:
1. blog-writer가 영어 개요를 한국어 글로 변환할 때 추가 인지 부담 발생
2. 개요-본문 간 언어 연속성 없음
3. 사용자가 `/feedback`에서 개요 피드백 시 영어 문서를 검토해야 함

**수정안**: outline-writer의 출력 구조를 한국어로 전환하거나, 최소한 섹션 제목을 한국어로 변경

---

#### M2. `brand-logo-finder.md` — 워크플로우 연결점 없음

brand-logo-finder는 Brandfetch를 통한 브랜드 로고 검색 전문 에이전트로, 블로그 워크플로우 7단계 어디에도 연결되지 않음.

- `/collect`, `/insights`, `/select`, `/research`, `/outline`, `/feedback`, `/write`, `/status` 중 이 에이전트를 호출하는 명령어 없음
- 기존 리뷰는 이를 블로그 시스템의 일부로 나열하면서 사용 시점 미설명

**의문점**: 블로그 썸네일 생성? 본문 내 브랜드 이미지 삽입? 어떤 단계에서 필요한가?
**수정안**: 사용 시나리오를 명시하거나, 독립 유틸리티로 분리(별도 폴더), 혹은 `/write` 단계와 연결하는 명령어 추가

---

#### M3. `youtube-collector.md` 메서드 순서 일관성 없음

**채널 수집 메서드 순서**: API v3 (1순위) → RSS (2순위) → yt-dlp (3순위)
**자막 수집 메서드 순서**: yt-dlp (1순위) → youtube-transcript-api (2순위) → 웹 스크래핑 (3순위)

두 섹션에서 yt-dlp의 우선순위가 정반대 — 사용자/에이전트 혼란 유발
**수정안**: 자막 수집 섹션에서도 API/yt-dlp 우선순위 기준을 일관되게 정의

---

#### M4. Wildcard 파일 참조 처리 불명확

`write.md`:
```
@content/outlines/*.md
@content/research/*.md
```

`feedback.md`:
```
@content/outlines/*.md
```

여러 주제를 동시 진행 중이면 여러 파일이 로드됨. 어떤 파일을 기준으로 작업할지 로직 없음.

**수정안**: `workflow-state.json`의 현재 세션 정보를 먼저 읽어 특정 파일을 지정하도록 변경, 또는 최신 파일 1개만 사용하도록 명시

---

#### M5. `blog-writer.md` Rank Math 체크리스트 중복

Rank Math SEO 체크리스트가 두 곳에 중복 존재:
1. `## Rank Math SEO 최적화` 섹션 내 "SEO 체크리스트 (Rank Math 100점 기준)"
2. `## Quality Checklist` 섹션 내 "Rank Math SEO 체크 (CRITICAL)"

**수정안**: 하나의 섹션으로 통합

---

### LOW — 개선 권장

#### L1. `insight-extractor.md` "current session" 정의 모호
```
"Read all files in content/raw/ from the current session"
```
"current session"이 오늘 날짜 기준인지, 마지막 수집 기준인지 불명확.
**수정안**: "최근 수집 폴더(`workflow-state.json`의 artifacts.raw 기준)"로 명확화

---

#### L2. `channels.json` 초기화 가이드 없음
새 프로젝트 시작 시 `content/channels.json`이 없으면 `/collect`가 실패.
`channels.json` 생성 방법, 기본 채널 추가 예시가 어느 문서에도 없음.
**수정안**: `blog-workflow/SKILL.md`에 "시작하기" 섹션 추가

---

#### L3. `blog-writer.md` 파일 길이 과도
390줄 — Claude Code 에이전트 지시문으로는 과도하게 길며, Progressive Disclosure 원칙 위배.

주요 내용:
- 한국어 글쓰기 원칙 (상세)
- Rank Math SEO 최적화 (상세)
- Writing Guidelines (영어)
- Quality Checklist (중복 포함)

**수정안**: SEO 최적화 내용을 `references/seo-guide.md`로 분리하고 본문에서 참조

---

#### L4. `blog-writer.md` 언어 혼재
에이전트 지시문 전체에서 한국어와 영어가 혼재:
- 한국어: "한국어 글쓰기 스타일 (CRITICAL)", "Rank Math SEO 최적화"
- 영어: "Writing Guidelines", "Opening (Hook)", "Body Sections", "Style Requirements", "Quality Checklist"

에이전트의 일관성 저하.

---

#### L5. `brand-logo-finder.md` description 언어 불일치
다른 에이전트의 description은 모두 한국어인데 brand-logo-finder만 영어:
```
"Finds brand logos using Brandfetch. Use when user asks for a brand's logo..."
```

---

## 3. 파일별 요약

| 파일 | 등급 | 주요 문제 |
|------|------|-----------|
| `research-agent.md` | 🔴 HIGH | 연도 하드코딩 (2024) |
| `workflow-state.json` (스키마) | 🔴 HIGH | 형식 미정의 |
| `templates/post.md` | 🔴 HIGH | blog-writer 출력과 불일치 |
| `outline-writer.md` | 🟡 MEDIUM | 영어 전용, 언어 불일치 |
| `brand-logo-finder.md` | 🟡 MEDIUM | 워크플로우 연결 없음 |
| `youtube-collector.md` | 🟡 MEDIUM | 메서드 순서 혼란 |
| `write.md`, `feedback.md` | 🟡 MEDIUM | wildcard 참조 처리 불명확 |
| `blog-writer.md` | 🟡 MEDIUM | 체크리스트 중복 |
| `insight-extractor.md` | 🟢 LOW | "current session" 모호 |
| `blog-workflow/SKILL.md` | 🟢 LOW | 초기화 가이드 없음 |

---

## 4. 기존 리뷰 vs 재검토 비교

| 항목 | 기존 리뷰 | 재검토 |
|------|----------|--------|
| 확장성 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ (workflow-state 스키마 미정의로 확장 시 위험) |
| 유지보수 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ (템플릿-에이전트 불일치, 중복 코드) |
| 효율성 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ (연도 하드코딩으로 리서치 품질 저하) |
| 문서화 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ (스키마 미정의, 초기화 가이드 없음) |
| 실용성 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ (brand-logo-finder 연결 없음, wildcard 처리 불명확) |

---

## 5. 우선 수정 순서

1. **[즉시]** `research-agent.md` 검색 쿼리의 "2024" → 현재 연도 동적 주입
2. **[즉시]** `workflow-state.json` 스키마를 `blog-workflow/SKILL.md`에 추가
3. **[즉시]** `templates/post.md` frontmatter를 blog-writer SEO 형식으로 업데이트
4. **[단기]** `outline-writer.md` 출력 구조 한국어화
5. **[단기]** `write.md`, `feedback.md` wildcard 참조를 특정 파일 참조로 변경
6. **[단기]** `brand-logo-finder.md` 사용 시나리오 명시 또는 워크플로우 연결
7. **[단기]** `blog-writer.md` Rank Math 체크리스트 중복 제거
8. **[중기]** `blog-writer.md` SEO 가이드를 references로 분리
9. **[중기]** `blog-workflow/SKILL.md`에 "시작하기" 섹션 추가

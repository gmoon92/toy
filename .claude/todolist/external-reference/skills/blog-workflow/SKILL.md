---
name: blog-workflow
description: 유튜브 채널 기반 기술 블로그 컨텐츠 생성 워크플로우. 채널에서 컨텐츠 수집, 인사이트 추출, 글 작성까지 전체 프로세스 지원. /collect, /insights, /select, /research, /outline, /feedback, /write, /status 명령어로 워크플로우 실행.
---

# Blog Workflow

유튜브 채널 기반 기술 블로그 컨텐츠 생성 워크플로우를 관리합니다.

## 워크플로우 단계

1. **수집** (`/collect`): 등록된 유튜브 채널에서 신규 컨텐츠 수집
2. **인사이트** (`/insights`): 수집된 컨텐츠에서 주요 인사이트 도출
3. **선정** (`/select`): 글로 쓸 인사이트 대화형 선택 (Human)
4. **리서치** (`/research`): 선정 주제에 대한 전문적 견해/트렌드 조사
5. **개요** (`/outline`): 블로그 글 개요 작성
6. **피드백** (`/feedback`): 개요에 대한 대화형 피드백 (Human)
7. **작성** (`/write`): 최종 블로그 글 작성

## 채널 관리

채널 목록: `content/channels.json`

```json
{
  "channels": [
    {
      "id": "UC...",
      "name": "채널명",
      "url": "https://youtube.com/@channel",
      "category": "tech",
      "lastChecked": "2024-01-01T00:00:00Z",
      "enabled": true
    }
  ]
}
```

채널 추가: `content/channels.json` 파일 직접 편집

## 상태 관리

워크플로우 상태: `content/workflow-state.json`

## 파일 구조

```
content/
├── channels.json          # 채널 목록
├── workflow-state.json    # 워크플로우 상태
├── raw/                   # 수집된 원본 (raw/{date}-{channel}/*.md)
├── insights/              # 인사이트 (insights/{date}-insights.md)
├── selected/              # 선정된 주제 (selected/{date}-{topic}.md)
├── research/              # 리서치 결과 (research/{date}-{topic}-research.md)
├── outlines/              # 개요 (outlines/{date}-{topic}-outline.md)
└── posts/                 # 최종 포스트 (posts/{date}-{topic}.md)
```

## 참조

- YouTube 데이터 수집: [youtube-api.md](references/youtube-api.md)
- 글쓰기 스타일: [writing-style.md](references/writing-style.md)

## 템플릿

- 인사이트: [assets/templates/insight.md](assets/templates/insight.md)
- 개요: [assets/templates/outline.md](assets/templates/outline.md)
- 포스트: [assets/templates/post.md](assets/templates/post.md)

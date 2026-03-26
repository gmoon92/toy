---
description: 수집된 컨텐츠에서 블로그 인사이트 추출
allowed-tools: Task, Read, Write, Glob, Grep
---

## 인사이트 추출

수집된 유튜브 컨텐츠에서 블로그 글감이 될 인사이트를 추출합니다.

### 현재 워크플로우 상태

@content/workflow-state.json

### 작업 지시

1. `insight-extractor` 서브에이전트를 호출합니다
2. `content/raw/` 폴더의 최신 수집 컨텐츠를 분석합니다
3. 추출된 인사이트는 `content/insights/{date}-insights.md`에 저장됩니다
4. 각 인사이트에 대해 점수와 추천 이유를 포함합니다
5. 워크플로우 상태를 업데이트합니다

### 추가 인수

$ARGUMENTS

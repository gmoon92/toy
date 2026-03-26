---
description: 선정된 주제에 대한 심층 리서치
allowed-tools: Task, Read, Write, WebSearch, WebFetch, Glob
---

## 고도화 리서치

선정된 인사이트에 대해 전문적인 리서치를 수행합니다.

### 선정된 인사이트

@content/selected/*.md

### 작업 지시

1. `research-agent` 서브에이전트를 호출합니다
2. 선정된 주제에 대해 다음을 조사합니다:
   - 전문가 의견 및 견해
   - 관련 데이터와 통계
   - 실제 사례 연구
   - 반대 의견 및 비판
   - 최신 트렌드
3. 리서치 결과는 `content/research/{date}-{topic}-research.md`에 저장됩니다
4. 워크플로우 상태를 업데이트합니다

### 추가 인수

$ARGUMENTS

특정 주제 지정 시: 해당 주제만 리서치
빈 경우: 가장 최근 선정된 주제 리서치

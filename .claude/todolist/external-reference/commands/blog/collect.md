---
description: 등록된 유튜브 채널에서 신규 컨텐츠 수집
allowed-tools: Task, Read, Write, WebFetch, WebSearch, Bash, Glob
---

## 블로그 컨텐츠 수집

유튜브 채널에서 신규 컨텐츠를 수집합니다.

### 현재 등록된 채널

@content/channels.json

### 작업 지시

1. `youtube-collector` 서브에이전트를 호출하여 등록된 채널에서 컨텐츠를 수집합니다
2. 수집된 컨텐츠는 `content/raw/{date}-{channel}/` 폴더에 저장됩니다
3. 수집 완료 후 결과를 보고합니다
4. `content/workflow-state.json`을 업데이트합니다

### 추가 인수

$ARGUMENTS

빈 경우: 모든 활성화된 채널에서 수집
채널명 지정 시: 해당 채널만 수집

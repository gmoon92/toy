---
description: 블로그 워크플로우 진행 상태 확인
allowed-tools: Read, Glob
---

## 워크플로우 상태 확인

현재 블로그 컨텐츠 생성 워크플로우의 진행 상태를 확인합니다.

### 상태 파일

@content/workflow-state.json

### 채널 목록

@content/channels.json

### 확인 및 보고 사항

1. **현재 진행 단계**
   - 어떤 단계까지 완료되었는지
   - 현재 진행 중인 작업

2. **각 단계별 산출물**
   - raw/: 수집된 컨텐츠 수
   - insights/: 추출된 인사이트 파일
   - selected/: 선정된 주제
   - research/: 리서치 결과
   - outlines/: 작성된 개요
   - posts/: 완성된 글

3. **다음 단계 안내**
   - 다음으로 실행할 명령어
   - 필요한 작업

### 사용 가능한 명령어

- `/collect` - 컨텐츠 수집
- `/insights` - 인사이트 추출
- `/select` - 인사이트 선정 (대화형)
- `/research` - 심층 리서치
- `/outline` - 개요 작성
- `/feedback` - 개요 피드백 (대화형)
- `/write` - 최종 글 작성

### 추가 인수

$ARGUMENTS

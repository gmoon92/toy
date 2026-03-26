# YouTube 데이터 수집 가이드

## 수집 방법

### 1. WebSearch를 통한 최신 영상 검색

```
검색 쿼리: "[채널명] youtube 최신 영상" 또는 "site:youtube.com @채널핸들"
```

### 2. 채널 페이지 직접 접근

```
URL 패턴: https://www.youtube.com/@{handle}/videos
```

### 3. 영상 정보 추출

각 영상에서 추출할 정보:
- **제목**: 영상 타이틀
- **URL**: 영상 링크
- **게시일**: 업로드 날짜
- **설명**: 영상 설명문
- **자막/스크립트**: 가능한 경우 추출

### 4. 자막 추출 (가능한 경우)

자동 생성 자막 또는 수동 자막이 있는 경우:
- 영상 페이지에서 자막 데이터 확인
- 자막이 없는 경우 설명문과 제목 기반으로 요약

## 수집 주기

- `lastChecked` 이후 게시된 영상만 수집
- 기본 수집 범위: 최근 7일

## 출력 형식

```markdown
---
title: 영상 제목
channel: 채널명
url: https://youtube.com/watch?v=...
publishedAt: 2024-01-02
collectedAt: 2024-01-02T10:00:00Z
---

## Summary
[간략한 요약]

## Key Points
- 포인트 1
- 포인트 2

## Transcript/Content
[자막 또는 상세 설명]
```

## 제한사항

- YouTube API 키 없이 웹 스크래핑 방식 사용
- 자막이 없는 영상은 제목/설명 기반 정보만 수집
- 비공개 또는 연령 제한 영상은 수집 불가

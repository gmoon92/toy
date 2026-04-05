# 서브 에이전트 도구 조합 가이드

서브 에이전트는 `tools` 필드로 권한을 제한합니다. 생략 시 메인 스레드의 모든 도구를 상속합니다.

## 자주 사용하는 도구 조합

### 읽기 전용 조사
```
tools: Read, Grep, Glob, Bash
```
용도: 코드 분석, 문서 검토, 코드베이스 탐색 (reviewer, auditor, analyst)

### 코드 수정
```
tools: Read, Write, Edit, Grep, Glob, Bash
```
용도: 기능 구현, 버그 수정, 리팩터링 (fixer, writer)

### 읽기 전용(Read-Only)
```
tools: Read, Grep, Glob
```
용도: 보안 감사, 보고 전용 검토 — Bash 없이 실행 차단 강화 (auditor 강화)

### 전체 접근
`tools` 필드를 생략하면 사용 가능한 모든 도구를 상속합니다.
용도: 실행 작업 전반 (executor) — 도구 범위를 사전에 한정하기 어려운 경우 사용

## IDE 도구 (사용 가능한 경우)

| 도구 | 설명 |
|------|------|
| `mcp__ide__getDiagnostics` | VS Code 언어 진단 정보 (타입 오류, 린트 등) |
| `mcp__ide__executeCode` | Jupyter 커널에서 코드 실행 (analyst 활용) |

## MCP 도구

MCP 도구는 `tools` 필드에 직접 명시해야 합니다. 형식: `mcp__<서버>__<도구>`

예시:
```yaml
tools: Read, Grep, mcp__ide__getDiagnostics, mcp__sequential-thinking__sequentialthinking
```

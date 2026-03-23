# Tools Reference

## Metadata

- claude-code-version: v2.1.80 (2026-03-19)
- last-updated: 2026-03-20

## Tools

| Tool                   | 설명                                                                       | Permission Required | deprecated | 비고/제약 |
|:-----------------------|:---------------------------------------------------------------------------|:-------------------:|:-----------|:----------|
| `Agent`                | 독립적인 컨텍스트 창을 가진 서브에이전트를 생성하여 작업 처리              | N                   |            |           |
| `AskUserQuestion`      | 요구사항 수집 또는 모호성 해소를 위한 객관식 질문 표시                     | N                   |            |           |
| `Bash`                 | 사용자 환경에서 셸 명령 실행                                               | Y                   |            |           |
| `CronCreate`           | 현재 세션 내에서 반복 또는 일회성 프롬프트 예약 (세션 종료 시 삭제)       | N                   |            |           |
| `CronDelete`           | ID로 예약된 작업 취소                                                      | N                   |            |           |
| `CronList`             | 세션의 모든 예약 작업 목록 조회                                            | N                   |            |           |
| `Edit`                 | 특정 파일에 대한 대상 편집 수행                                            | Y                   |            |           |
| `EnterPlanMode`        | 접근 방식 설계를 위해 플랜 모드로 전환                                     | N                   |            |           |
| `EnterWorktree`        | 격리된 Git worktree 생성 및 전환                                           | N                   |            |           |
| `ExitPlanMode`         | 플랜 승인 요청 및 플랜 모드 종료                                           | Y                   |            |           |
| `ExitWorktree`         | worktree 세션 종료 및 원래 디렉토리로 복귀                                 | N                   |            |           |
| `Glob`                 | 패턴 매칭을 기반으로 파일 검색                                             | N                   |            |           |
| `Grep`                 | 파일 내용에서 패턴 검색                                                    | N                   |            |           |
| `ListMcpResourcesTool` | 연결된 MCP 서버에서 제공하는 리소스 목록 조회                              | N                   |            |           |
| `LSP`                  | 언어 서버를 통한 코드 인텔리전스. 파일 편집 후 자동으로 타입 오류 및 경고 보고 | N                   |            |           |
| `NotebookEdit`         | Jupyter 노트북 셀 수정                                                     | Y                   |            |           |
| `Read`                 | 파일 내용 읽기                                                             | N                   |            |           |
| `ReadMcpResourceTool`  | URI로 특정 MCP 리소스 읽기                                                 | N                   |            |           |
| `Skill`                | 메인 대화 내에서 스킬 실행                                                 | Y                   |            |           |
| `TaskCreate`           | 작업 목록에 새 작업 생성                                                   | N                   |            |           |
| `TaskGet`              | 특정 작업의 전체 세부 정보 조회                                            | N                   |            |           |
| `TaskList`             | 모든 작업의 현재 상태 목록 조회                                            | N                   |            |           |
| `TaskOutput`           | 백그라운드 작업의 출력 조회                                                | N                   |            |           |
| `TaskStop`             | ID로 실행 중인 백그라운드 작업 종료                                        | N                   |            |           |
| `TaskUpdate`           | 작업 상태, 의존성, 세부 정보 업데이트 또는 작업 삭제                       | N                   |            |           |
| `TodoWrite`            | 세션 작업 체크리스트 관리. non-interactive mode 및 Agent SDK에서 사용      | N                   |            |           |
| `ToolSearch`           | 도구 검색이 활성화된 경우 지연 로드 도구 검색 및 로드                      | N                   |            |           |
| `WebFetch`             | 지정된 URL에서 콘텐츠 가져오기                                             | Y                   |            |           |
| `WebSearch`            | 웹 검색 수행                                                               | Y                   |            |           |
| `Write`                | 파일 생성 또는 덮어쓰기                                                    | Y                   |            |           |

## Note

- Claude Code의 도구는 `permissions` 설정, `subagent` 도구 목록, `hook matcher`에서 정확한 도구 이름 문자열을 사용합니다.
- `LSP` 도구는 code intelligence plugin과 language server binary가 필요합니다.
- `TodoWrite`는 non-interactive mode와 Agent SDK에서 사용됩니다. Interactive sessions에서는 `TaskCreate`, `TaskGet`, `TaskList`, `TaskUpdate`를 사용합니다.

## Reference

- [Claude Code - Tools reference](https://code.claude.com/docs/en/tools-reference)

# Agent Frontmatter

## Metadata

- claude-code-version: v2.1.80 (2026-03-19)
- last-updated: 2026-03-20

## Custom Extensions

에이전트 카테고리별 권장 색상:

| 카테고리                | 색상 | 예시 |
|---------------------|------|------|
| 기획 (Requirements)   | Orange | `#FFA500` |
| 설계 (Architecture)   | Purple | `#800080` |
| 백엔드 (Backend)       | Yellow | `#FFFF00` |
| 프론트엔드 (Frontend)    | Red | `#FF0000` |
| macOS               | Pink | `#FFC0CB` |
| Windows             | Green | `#00FF00` |
| Mobile (Flutter)    | Blue | `#0000FF` |
| 검증 (Validation)     | White | `#FFFFFF` |

## Fields

| 필드              |          필수           |       타입        |     기본값      | 허용값/범위                                                                                                                              | 설명                                                                    | version         | deprecated                      | 비고/제약 |
|:----------------|:---------------------:|:---------------:|:------------:|:--------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------|:----------------|:--------------------------------|:------|
| `name`          |       Required        |     string      |     none     | 소문자, 숫자, 하이픈만. 최대 64자                                                                                                              | 소문자와 하이픈을 사용하는 고유 식별자                                      |      |                                 |       |
| `description`   |       Required        |     string      |     none     | 비어있지 않음. 최대 1024자                                                                                                                  | Claude가 이 서브에이전트에 작업을 위임해야 하는 상황                         |      |                                 |       |
| `tools`         |       Optional        |  array<string>  |   inherit    | 도구 이름 목록 또는 `Agent(type1, type2)` 문법                                                                                              | 서브에이전트가 사용할 수 있는 도구                                         |      |                                 |       |
| `disallowedTools` |       Optional        |  array<string>  |     none     | 도구 이름 목록                                                                                                                          | 상속되거나 지정된 목록에서 제거할 금지 도구                                  |      |                                 |       |
| `model`         |       Optional        |     string      |   inherit    | `inherit`, `sonnet`, `opus`, `haiku`, 전체 모델 ID (예: `claude-opus-4-6`)                                                                    | 사용할 모델                                                              |      |                                 |       |
| `permissionMode` |       Optional        |     string      |   default    | `default`, `acceptEdits`, `dontAsk`, `bypassPermissions`, `plan`                                                                        | 서브에이전트의 권한 모드                                                  |      |                                 |       |
| `maxTurns`      |       Optional        |     number      |     none     | 양의 정수                                                                                                                             | 서브에이전트가 중지하기 전의 최대 에이전틱 턴 수                            |      |                                 |       |
| `skills`        |       Optional        |  array<string>  |     none     | 스킬 이름 목록                                                                                                                         | 서브에이전트의 컨텍스트 시작 시 로드할 스킬                                  |      |                                 |       |
| `mcpServers`    |       Optional        | array<string>\|object |     none     | 문자열(서버 이름) 또는 인라인 정의 객체                                                                                                          | 이 서브에이전트에서 사용 가능한 MCP 서버                                   |      |                                 |       |
| `hooks`         |       Optional        |     object      |     none     | PreToolUse, PostToolUse, Stop 이벤트                                                                                                  | 이 서브에이전트에 범위가 지정된 라이프사이클 훅                              |      |                                 |       |
| `memory`        |       Optional        |     string      |     none     | `user`, `project`, `local`                                                                                                            | 세션 간 학습을 위한 영구 메모리 범위                                        |      |                                 |       |
| `background`    |       Optional        |     boolean     |    false     | `true`, `false`                                                                                                                       | `true`로 설정하면 이 서브에이전트를 항상 백그라운드 작업으로 실행             |      |                                 |       |
| `isolation`     |       Optional        |     string      |     none     | `worktree`                                                                                                                            | `worktree`로 설정하면 임시 git worktree에서 실행                            |      |                                 |       |
| `color`         |       Optional        |     string      |     none     | CSS 색상 값 (예: `#FF5733`, `red`)                                                                                                     | 서브에이전트 UI 배경색을 지정하여 시각적으로 구분                              |      |                                 | Custom Extension |
| `effort`        |       Optional        |     string      |   inherit    | `low`, `medium`, `high`, `max` (Opus 4.6 only)                                                                                         | 이 서브에이전트가 활성화될 때의 Effort 레벨                                 | v2.1.80 (2026-03-19) |                                 |       |

## Note

- `tools` 필드에서 `Agent(agent_type)` 문법을 사용하여 특정 서브에이전트만 스폰할 수 있도록 제한할 수 있습니다.
- `mcpServers`는 문자열(이미 설정된 서버 이름 참조) 또는 인라인 정의 객체를 지원합니다.
- `hooks`는 `PreToolUse`, `PostToolUse`, `Stop` 이벤트를 지원하며, 각 이벤트는 matcher와 hooks 배열을 포함합니다.
- `memory`가 활성화되면 Read, Write, Edit 도구가 자동으로 활성화됩니다.
- Plugin에서 로드된 에이전트는 `hooks`, `mcpServers`, `permissionMode` 필드를 지원하지 않습니다.

## Reference

- [Claude Code - Sub Agent: Supported frontmatter fields](https://code.claude.com/docs/en/sub-agents)

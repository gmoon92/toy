# Skill Frontmatter

## Metadata

- claude-code-version: v2.1.80 (2026-03-19)
- last-updated: 2026-03-20

## Custom Extensions

## Fields

| 필드                       |          필수           |       타입        |     기본값      | 허용값/범위                                                      | 설명                                                                    | version         | deprecated                      | 비고/제약 |
|:-------------------------|:---------------------:|:---------------:|:------------:|:-----------------------------------------------------------|:---------------------------------------------------------------|:----------------|:--------------------------------|:------|
| `name`                   |       Optional        |     string      |   디렉토리명    | 소문자, 숫자, 하이픈만. 최대 64자                                 | 스킬의 표시 이름                                                         |      |                                 |       |
| `description`            |      Recommended      |     string      |  마크다운 첫 단락  | 최대 1024자                                                  | 스킬의 기능과 사용 시기                                                   |      |                                 |       |
| `argument-hint`          |       Optional        |     string      |     none     | 예: `[issue-number]`, `[filename] [format]`                   | 자동완성 시 표시되는 힌트                                                  |      |                                 |       |
| `disable-model-invocation` |       Optional        |     boolean     |    false     | `true`, `false`                                             | `true`로 설정하면 Claude가 이 스킬을 자동으로 로드하는 것을 방지             |      |                                 |       |
| `user-invocable`         |       Optional        |     boolean     |    true      | `true`, `false`                                             | `false`로 설정하면 `/` 메뉴에서 숨김                                      |      |                                 |       |
| `allowed-tools`          |       Optional        |  array<string>  |     none     | 도구 이름 목록                                                  | 이 스킬이 활성화될 때 Claude가 권한 없이 사용할 수 있는 도구                |      |                                 |       |
| `model`                  |       Optional        |     string      |     none     | 모델 이름 또는 ID                                              | 이 스킬이 활성화될 때 사용할 모델                                          |      |                                 |       |
| `context`                |       Optional        |     string      |     none     | `fork`                                                      | `fork`로 설정하면 포크된 서브에이전트 컨텍스트에서 실행                      |      |                                 |       |
| `agent`                  |       Optional        |     string      | general-purpose | `Explore`, `Plan`, `general-purpose` 또는 커스텀 에이전트 이름    | `context: fork`가 설정된 경우 사용할 서브에이전트 유형                      |      |                                 |       |
| `hooks`                  |       Optional        |     object      |     none     | Hook 설정 객체                                                | 이 스킬의 라이프사이클에 범위가 지정된 훅                                   |      |                                 |       |
| `effort`                 |       Optional        |     string      |   inherit    | `low`, `medium`, `high`, `max` (Opus 4.6 only)               | 이 스킬이 활성화될 때의 Effort 레벨                                       | v2.1.80 (2026-03-19) |                                 |       |

## Note

- `$ARGUMENTS`, `$ARGUMENTS[N]`, `$N` 플레이스홀더를 사용하여 인자에 접근할 수 있습니다.
- `${CLAUDE_SESSION_ID}`, `${CLAUDE_SKILL_DIR}` 변수를 사용할 수 있습니다.
- ``!`command` `` 문법을 사용하여 스킬 실행 전 셸 명령을 실행하고 출력을 삽입할 수 있습니다.
- `context: fork`가 설정된 경우, 스킬 내용이 서브에이전트의 프롬프트가 됩니다.
- `hooks`는 서브에이전트와 마찬가지로 스킬에서도 라이프사이클 이벤트를 처리할 수 있습니다.

## Reference

- [Claude Code - Agent Skills: Frontmatter reference](https://code.claude.com/docs/en/skills)

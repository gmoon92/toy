# Command Frontmatter

## Metadata

- claude-code-version: v2.1.80 (2026-03-19)
- last-updated: 2026-03-20

## Custom Extensions

## Fields

> 커맨드 프론트매터는 선택 사항입니다. 커맨드 명은 파일명으로 동작됩니다.

| 필드                       |          필수           |       타입        |     기본값      | 허용값/범위                                                      | 설명                                                                    | version         | deprecated                      | 비고/제약 |
|:-------------------------|:---------------------:|:---------------:|:------------:|:-----------------------------------------------------------|:---------------------------------------------------------------|:----------------|:--------------------------------|:------|
| `allowed-tools`          |       Optional        |  array<string>  |     none     | 도구 이름 목록                                                  | 사용 가능한 도구 제한                                                   |      |                                 |       |
| `argument-hint`          |       Optional        |     string      |     none     | 예: `[issue-number] [priority]`                               | 인자 힌트 (자동완성 표시)                                               |      |                                 |       |
| `description`            |       Optional        |     string      |     none     | -                                                          | 커맨드 설명                                                             |      |                                 |       |
| `model`                  |       Optional        |     string      |     `inherit`     | `inherit`, `sonnet`, `opus`, `haiku` 등                        | 사용할 모델 지정                                                        |      |                                 |       |
| `effort`                 |       Optional        |     string      |     `inherit`     | `low`, `medium`, `high`, `max` (Opus 4.6 only)               | Effort 레벨 지정                                                        | v2.1.80 (2026-03-19) |                                 |       |

## Note

커스텀 커맨드는 스킬(Skill)로 통합되었습니다.

`.claude/commands/<name>.md`와 `.claude/skills/<name>/SKILL.md` 모두 동일하게 `/name` 명령어를 생성합니다.
`.claude/commands/`는 레거시 형식이며, 권장 형식은 `.claude/skills/<name>/SKILL.md`입니다.
스킬 형식은 추가 파일 지원, 프론트매터를 통한 호출 제어, 자동 로딩 등의 기능을 제공합니다.

- `$1`, `$2`, ... 또는 `$ARGUMENTS` 플레이스홀더를 사용하여 인자에 접근할 수 있습니다.
- ``!`command` `` 문법을 사용하여 커맨드 실행 전 셸 명령을 실행하고 출력을 삽입할 수 있습니다.
- `@filepath` 문법을 사용하여 파일 내용을 삽입할 수 있습니다.

## Reference

- [Slash Commands in the SDK](https://platform.claude.com/docs/en/agent-sdk/slash-commands)
- [Creating Custom Slash Commands](https://platform.claude.com/docs/en/agent-sdk/slash-commands#creating-custom-slash-commands)
- [File Format](https://platform.claude.com/docs/en/agent-sdk/slash-commands#file-format)

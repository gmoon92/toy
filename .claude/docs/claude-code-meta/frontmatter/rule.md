# Rule Frontmatter

## Metadata

- claude-code-version: v2.1.80 (2026-03-19)
- last-updated: 2026-03-20

## Custom Extensions

## Fields

> Rule 프론트매터는 선택 사항입니다. `paths` 필드를 사용하여 특정 파일 패턴에만 규칙을 적용할 수 있습니다.

| 필드            |          필수           |       타입        |     기본값      | 허용값/범위                                                      | 설명                                                                    | version         | deprecated                      | 비고/제약 |
|:--------------|:---------------------:|:---------------:|:------------:|:-----------------------------------------------------------|:---------------------------------------------------------------|:----------------|:--------------------------------|:------|
| `paths`       |       Optional        |  array<string>  |     none     | Glob 패턴 (예: `**/*.ts`, `src/**/*.{tsx,jsx}`)              | 규칙을 적용할 파일 경로 패턴                                             |      |                                 |       |
| `description` |       Optional        |     string      |     none     | -                                                          | 규칙 설명                                                                |      |                                 |       |

## Note

- `paths` 필드가 없는 규칙은 모든 파일에 대해 무조건 로드됩니다.
- `paths` 필드가 있는 규칙은 해당 패턴과 일치하는 파일을 작업할 때만 로드됩니다.
- `.claude/rules/` 디렉토리는 재귀적으로 스캔되며, 서브디렉토리도 지원합니다.
- 심볼릭 링크를 지원하며, 순환 참조는 gracefully 처리됩니다.

## Reference

- [Claude Code - Organize rules with `.claude/rules/`](https://code.claude.com/docs/en/memory#organize-rules-with-clauderules)

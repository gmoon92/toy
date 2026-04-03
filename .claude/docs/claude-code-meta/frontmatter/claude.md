# CLAUDE.md Frontmatter

## Metadata

- claude-code-version: v2.1.90 (2026-04-01)
- last-updated: 2026-03-20

## Custom Extensions

## Fields

> CLAUDE.md 문서는 **프론트매터가 없는** 일반 마크다운 문서입니다.

## Note

- `@path/to/file` 문법을 사용하여 추가 파일을 임포트할 수 있습니다.
- 상대 경로와 절대 경로(`~` 포함)를 모두 지원합니다.
- 최대 임포트 깊이는 5단계입니다.
- `AGENTS.md` 파일을 사용하는 다른 코딩 에이전트와의 호환을 위해, `CLAUDE.md`에서 `@AGENTS.md`로 임포트하는 방식을 지원합니다.
- 블록 수준 HTML 주석(`<!-- ... -->`)은 컨텍스트에 주입 전 제거됩니다. 코드 블록 내 주석은 유지됩니다.
- `claudeMdExcludes` 설정으로 특정 CLAUDE.md 파일을 경로 또는 glob 패턴으로 제외할 수 있습니다.
- `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1` 환경변수를 설정하면 `--add-dir`로 추가한 디렉토리의 CLAUDE.md 파일도 로드됩니다.

## Reference

- [Claude Code - CLAUDE.md files](https://code.claude.com/docs/en/memory#claude-md-files)
- [Claude Code - How Claude remembers your project](https://code.claude.com/docs/en/memory#how-claudemd-files-load)
- [Claude Code - Organize rules with `.claude/rules/`](https://code.claude.com/docs/en/memory#organize-rules-with-clauderules)

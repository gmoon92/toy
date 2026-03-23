---
name: cc:fetch
description: Claude Code 공식 문서를 참고하여 메타 문서를 최신화 상태로 유지합니다.
---

# Document fetch

Claude Code 공식 문서를 기반으로 메타 문서를 최신 정보로 업데이트합니다.

## Workflow

1. `fetch_changelogs.py` 실행
   ```bash
   python ${CLAUDE_PROJECT_DIR}/.claude/skills/claudecode-fetch/scripts/fetch_changelogs.py \
     ${CLAUDE_PROJECT_DIR}/.claude/docs/claude-code-meta/changelogs
   ```
2. create subagent
    - create `frontmatter` subagent in [frontmatter](instructions/fetch-frontmatter.md)
    - create `tools` subagent in [tools reference](instructions/fetch-tools-reference.md)
3. 각 문서별 작업(`TaskCreate`) 생성
    - `frontmatter` subagent create task
    - `tools` subagent create task
4. 작업(`task`) 실행
    - `frontmatter` subagent task running
    - `tools` subagent task running
5. 완료 보고

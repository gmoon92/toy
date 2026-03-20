---
name: cc:fetch
description: Claude Code 공식 문서를 참고하여 메타 문서를 최신화 상태로 유지합니다.
---

# Document fetch

Claude Code 공식 문서를 기반으로 메타 문서를 최신 정보로 업데이트합니다.

1. [change logs](../claudecode-document-validator/references/prompt/fetch-claude-code-changelogs.md) 실행
2. create subagent
   - create `frontmatter` subagent in [frontmatter](../claudecode-document-validator/references/sync/docs-update-frontmatter.md)
   - create `tools` subagent in [tools reference][docs-update-tools-reference.md](../claudecode-document-validator/references/sync/docs-update-tools-reference.md) 
2. 각 문서 문서별 작업(`TaskCreate`) 생성 
   - `frontmatter` subagent create task
   - `tools` subagent create task
3. 작업(`task`) 실행
   - `frontmatter` subagent task running
   - `tools` subagent task running
4. 완료 보고

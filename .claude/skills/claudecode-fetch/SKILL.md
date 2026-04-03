---
name: cc:fetch
description: Claude Code 공식 문서를 참고하여 메타 문서를 최신화 상태로 유지합니다.
---

# Document fetch

Claude Code 공식 문서를 기반으로 메타 문서를 최신 정보로 업데이트합니다.

## Workflow

1. `fetch_changelogs.py --json-output` 실행
   ```bash
   python ${CLAUDE_PROJECT_DIR}/.claude/skills/claudecode-fetch/scripts/fetch_changelogs.py \
     ${CLAUDE_PROJECT_DIR}/.claude/docs/claude-code-meta/changelogs \
     --json-output
   ```
2. 결과 분석 및 분기
   - `success=true`, `total_saved > 0`: 새로운 릴리즈 발견 -> 3단계로 진행
   - `success=true`, `total_saved = 0`: 이미 최신 상태 -> 5단계(완료 보고)로 이동
   - `success=false`: 에러 보고 및 중단
3. 서브에이전트 생성
    - create `frontmatter` subagent in [frontmatter](instructions/fetch-frontmatter.md)
    - create `tools` subagent in [tools reference](instructions/fetch-tools-reference.md)
4. 작업 실행
    - `frontmatter` subagent: 각 문서별 작업(`TaskCreate`) 생성 및 실행
    - `tools` subagent: 각 문서별 작업(`TaskCreate`) 생성 및 실행
5. 완료 보고

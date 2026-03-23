# Claude Code 프로젝트 지침

- 모든 Git 커밋 요청은 `git:commit` 스킬을 사용하세요.
- 소스 코드 분석엔 **LSP 도구를 우선 사용하세요.**

## 파일명 규칙

- 모든 디렉토리명과 파일명은 **kebab-case**로 작성합니다.

## 작업 계획 관리

- Claude Code 작업 계획은 `.claude/docs/plans/` 디렉토리에 서브 디렉토리를 구성하여 문서로 관리합니다.
- 계획 문서 경로: `${CLAUDE_PROJECT_ROOT}/.claude/docs/plans/{계획명}/`

## 프롬프트 관리

- 프롬프트 파일은 `.claude/docs/prompts/` 디렉토리에서 관리합니다.
- 재사용 가능한 시스템 프롬프트, 작업 지시문, 컨텍스트 템플릿을 이곳에 저장합니다.
- 프롬프트 경로: `${CLAUDE_PROJECT_ROOT}/.claude/docs/prompts/`
- 작업별로 하위 디렉토리를 구성하여 문서를 관리합니다:
  - 예: `.claude/docs/prompts/tasks/{작업명}/task-prompt.md`
  - 예: `.claude/docs/prompts/agents/{에이전트명}/system-prompt.md`

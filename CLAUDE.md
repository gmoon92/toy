# Important

- 모든 Git 커밋 요청은 `git:commit` 스킬을 사용하세요.
- 소스 코드 분석엔 **LSP 도구를 우선 사용하세요.**
- 모든 디렉토리명과 파일명은 **kebab-case**로 작성하세요.
- 사용자가 **검토 요청**이나 **상세 분석**을 요청하면 반드시 **`sequential-thinking` MCP를 사용**하여 체계적으로 분석하세요.

## `.claude/` Claude Code 메타 문서 관리

### 경로 변수

- `PROJECT_ROOT`: ${CLAUDE_PROJECT_ROOT}, 현재 작업 중인 프로젝트의 루트 경로 (환경변수)
- `CLAUDE_DIR`: `<PROJECT_ROOT>/.claude/`
- `CLAUDE_DOCS_DIR`: `<CLAUDE_DIR>/docs/`

### Claude Code 메타 문서 작성 지침

**기능(feature) 단위 원칙:**
- 모든 메타 문서는 기능(feature) 단위로 구성하세요.
- feature명은 도메인/모듈/컴포넌트 단위로 지정하세요 (예: auth, payment, dashboard).
- 동일 기능의 계획과 프롬프트는 동일한 feature명을 사용하세요.

**플랜 문서는 다음 조건 시 반드시 작성:**
- 3단계 이상 복잡한 작업, 다중 파일 변경, 2개 이상 도구 호출이 필요한 경우
- 위치: `<CLAUDE_DOCS_DIR>/plans/<feature>/<계획명>-plan.md`
- 포함 항목: 작업 목표, 단계별 실행 계획, 예상 소요 시간

**프롬프트 문서는 재사용 시 반드시 작성:**
- 2회 이상 재사용되는 시스템 프롬프트, 작업 지시문, 컨텍스트 템플릿
- 위치: `<CLAUDE_DOCS_DIR>/prompts/<feature>/<유형>/<이름>.md`
- 유형: tasks/ (작업용), agents/ (에이전트용)

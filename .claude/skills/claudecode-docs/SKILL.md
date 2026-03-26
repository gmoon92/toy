---
name: cc:docs
description: |
  Claude Code 문서(Agent, Skill, Rule, Command, CLAUDE.md)의 구조와 형식을 검증합니다.
  프론트매터 유효성, 필수 섹션, 문서 구조, 참조 일관성 등을 검사하고 개선 제안을 제공합니다.

  다음 상황에서 사용합니다:
  - 새로 작성한 Agent/Skill 문서가 규격에 맞는지 확인할 때
  - 프로젝트 전체 문서 품질을 점검할 때
  - 문서 간 참조 불일치 및 구조 문제를 찾고 싶을 때

  예시:
  - "이 에이전트 문서 검증해줘"
  - ".claude/ 디렉토리 전체 검증"
  - "CLAUDE.md 품질 확인"
user-invocable: true
argument-hint: files
---

# Document Validator Skill

Claude Code 문서의 구조, 형식, 일관성을 검증하고 개선 방향을 제시합니다.

# Validation Levels

검증 결과는 다음 레벨로 구분됩니다.

- **ERROR**: 실행 또는 참조에 문제 발생 가능
- **WARNING**: 권장 사항 위반 (동작은 가능)
- **INFO**: 스타일 또는 개선 제안

# Input

- `${ARGUMENTS}`: [옵션] 검증 대상 경로
  - 공백으로 구분된 파일/디렉토리
  - 와일드카드 지원 (`*`, `**`)
  - 예:
    - `.claude/agents/**/*.md`
    - `.claude/skills/`
    - `CLAUDE.md`
- 미입력 시 기본 대상:
  - `.claude/**/*.md`
  - `CLAUDE.md`
  - `**/CLAUDE.md`

- {doc-type} = 문서 유형 식별
  - agent: `.claude/agents/**/*.md`
  - skill: `.claude/skills/**/*.md`
  - command: `.claude/commands/**/*.md`
  - rule: `.claude/rules/**/*.md`
  - claude: `CLAUDE.md` 또는 `**/CLAUDE.md`

# Workflow

## Step 1: 문서 분류 및 참조 로드

검증 대상 문서를 분류하고 필요한 참조 문서를 로드한다.

1. **문서 유형 식별**: `{doc-type}` 결정 (agent, skill, rule, command, claude)
2. **참조 문서 로드** (동시에):
   - 프론트매터 스펙: `${CLAUDE_PROJECT_DIR}/.claude/docs/claude-code-meta/frontmatter/{doc-type}.md`
   - 공통 검증 항목:
     - `references/checklists/common/frontmatter.md` (Step 2용)
     - `references/checklists/common/structure.md` (Step 3용)
     - `references/checklists/common/cross-reference.md` (Step 4용)
   - 작성 가이드라인: `references/guidelines.md`
   - 유형별 체크리스트: `references/checklists/{doc-type}.md`
   - 출력 템플릿: `references/templates/output.md`
3. **공식 문서 참고**: [Sub-agents](https://code.claude.com/docs/en/sub-agents), [Skills](https://code.claude.com/docs/en/skills) 문서 기준 검증

## Step 2: 프론트매터 검증

**참조**: `references/checklists/common/frontmatter.md`, `references/checklists/{doc-type}.md`

### 공통 검증

- [ ] YAML 문법 유효성 (`---`로 시작/종료)
- [ ] `name`: 필수(에이전트/스킬), 64자 이하, 소문자/숫자/하이픈만
- [ ] `name`: XML 태그 및 예약어(anthropic, claude) 없음
- [ ] `description`: "무엇+언제" 포함, 1024자 이하(에이전트/스킬), 200자 이하(룰)
- [ ] 선택 필드 타입: `model`, `tools`, `skills`, `mcpServers`, `hooks`

### 유형별 검증

- [ ] Agent: `model`, `permissionMode`, `memory`, `tools`, `subagents` 등
- [ ] Skill: `argument-hint`, `user-invocable`, `context`, `agent` 등
- [ ] Command: 프론트매터 생략 가능
- [ ] Rule: `paths` (Glob 패턴), `description`
- [ ] CLAUDE.md: **프론트매터 없음**

### Skill 전용 추가 검증

- [ ] `description`에 사용 예시 트리거 포함
- [ ] `user-invocable: true` 시 `argument-hint` 필수
- [ ] `context: fork` 시 `agent` 필수

> **중요**: 사용 예시 트리거는 반드시 프론트매터 `description`에만 배치

## Step 3: 본문 구조 검증

**참조**: `references/checklists/common/structure.md`, `references/checklists/{doc-type}.md`

### 공통 검증

- [ ] 마크다운 헤딩 레벨: h1 → h2 → h3 순으로 논리적 구성
- [ ] h1 제목이 문서의 목적을 명확히 설명
- [ ] 일관된 용어 사용 (하나의 용어를 문서 전체에서 사용)
- [ ] 코드 블록에 적절한 언어 태그 사용
- [ ] Unix 스타일 경로(`/`) 사용 (Windows `\` 금지)
- [ ] 불필요한 ASCII 아트 다이어그램 없음

### 유형별 검증

| 문서 유형 | 필수 섹션 | 금지 섹션 |
|:----------|:----------|:----------|
| **Agent** | 핵심 역량, Workflow, Don'ts, Output Format | - |
| **Skill** | Workflow, Tool Use Examples | 본문 "사용 예시", "트리거" 섹션 |
| **Command** | 최소 동작 (30줄 이하 권장) | 복잡한 로직 |
| **Rule** | 구체적이고 실행 가능한 규칙 | 모호한 표현("적절히", "가능하면") |
| **CLAUDE.md** | Overview, Structure, Commands | 프론트매터 |

### 기술적 검증

- [ ] MCP 도구 이름 정규화: `ServerName:tool_name`
- [ ] `` `!command` `` 문법 남용 여부
- [ ] 에이전트 로직 검증 금지 (문서 구조만 검증)

## Step 4: 참조 일관성 검증

**참조**: `references/checklists/common/cross-reference.md`, `references/transformation-patterns.md`, `references/checklists/{doc-type}.md`

### 공통 검증

- [ ] 본문에 언급된 에이전트/스킬이 프론트매터에 정의되어 있는지
- [ ] 본문의 호출 패턴이 프론트매터와 일치하는지
- [ ] 순환 참조 없음 (A → B → A 형태 방지)

### 파일 참조 유효성

- [ ] `.claude/agents/` 내 에이전트 파일 존재
- [ ] `.claude/skills/` 내 스킬 파일 존재
- [ ] 참조된 이름이 실제 파일의 `name` 필드와 일치

### 연결 검증

| 문서 유형 | 검증 항목 |
|:----------|:----------|
| **Skill** | `context: fork` 사용 시 `agent` 필드 필수 |
| **Agent** | `subagents` 파일 존재 및 `condition` 값 유효성 (`on_complete`, `on_error`, `always`) |

### 개선 제안

- [ ] 본문 에이전트 호출 → 프론트매터 `subagents`/`agent` 필드로 전환 가능성 제안

> **참조**: [전환 패턴](references/transformation-patterns.md) - 변환 예시 및 검증 체크리스트

## Step 5: 결과 보고

1. 통합 검증 테이블 출력 (`references/templates/output.md` 참조)
2. 수정 제안 제공 (본문→프론트매터 전환 가능성)
3. 피드백 문서 저장 (`${CLAUDE_TMP_DIR}/claudecode-document-validator/${CLAUDE_SESSION_ID}/`)

**주의사항**:
- 에이전트 로직 자체는 검증하지 않음 (문서 구조만 검증)
- 선택 필드 누락은 WARNING 수준으로 처리

---

# 출력 형식

검증 결과는 다음 템플릿을 따릅니다 (`references/templates/output.md`):

검증 결과는 `${CLAUDE_TMP_DIR}/claudecode-document-validator/${CLAUDE_SESSION_ID}` 디렉토리에 마크다운 파일로 저장됩니다.

- 폴터명 규칙: `{CLAUDE_SESSION_ID}` 세션 ID
- 개별 파일명 규칙: `{doc-type}-{name}.md`
- type: 문서 유형 (`agent`, `skill`, `rule`, `command`, `memory`)
- name: 프론트 매터 name 필드

예시

```
${CLAUDE_TMP_DIR}/claudecode-document-validator/
└── a1b2c3d4e5f6/              # 실행별 고유 폴터 (CLAUDE_SESSION_ID)
    ├── README.md               # 실행 요약 및 인덱스
    ├── agent-backend-developer.md
    ├── agent-frontend-reviewer.md
    ├── skill-git-commit.md
    ├── skill-flutter-dev.md
    ├── rule-coding-style.md
    └── command-pr-create.md
```

# 참조

- [작성 가이드라인](references/guidelines.md) - 필수/권장/금지사항
- [전환 패턴](references/transformation-patterns.md) - 본문-프론트매터 변환 가이드
- [체크리스트](references/checklists/) - 문서 유형별 검증 항목

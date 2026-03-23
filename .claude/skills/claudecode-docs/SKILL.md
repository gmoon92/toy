---
name: claudecode-docs
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

- `${CLAUDE_SKILL_DIR}`는 현재 스킬 디렉토리의 절대 경로로 치환
- {doc-type} = 문서 유형 식별
  - subagent: `${CLAUDE_PROJECT_DIR}/.claude/agents/**`
  - skill: `${CLAUDE_PROJECT_DIR}/.claude/skills/**`
  - command: `${CLAUDE_PROJECT_DIR}/.claude/command/**`
  - rule: `${CLAUDE_PROJECT_DIR}/.claude/rules/**`
  - claude: `CLAUDE.md` (프로젝트 루트 또는 모듈별)

# Workflow

## Step 1: 문서 분류 및 참조 로드

검증 대상 문서를 분류하고 필요한 참조 문서를 로드한다.

1. **문서 유형 식별**: `{doc-type}` 결정 (agent, skill, rule, command, claude)
2. **참조 문서 로드** (동시에):
   - 프론트매터 스펙: `${CLAUDE_SKILL_DIR}/references/frontmatter/{doc-type}.md`
   - 공통 체크리스트:
     - `${CLAUDE_SKILL_DIR}/references/checklists/common/frontmatter.md`
     - `${CLAUDE_SKILL_DIR}/references/checklists/common/structure.md`
     - `${CLAUDE_SKILL_DIR}/references/checklists/common/cross-reference.md`
   - 유형별 체크리스트: `${CLAUDE_SKILL_DIR}/references/checklists/{doc-type}.md`
   - 출력 템플릿: `${CLAUDE_SKILL_DIR}/references/templates/output.md`

## Step 2: 프론트매터 검증

### Step 2-1: 공통 프론트매터 검증

**참조**: `${CLAUDE_SKILL_DIR}/references/checklists/common/frontmatter.md`

모든 문서 유형에 적용되는 공통 검증:

- [ ] YAML 문법 오류 검증
- [ ] `name` 필드 검증 (존재, 길이, 형식, 예약어)
- [ ] `description` 필드 검증 (존재, 길이, 내용)
- [ ] 선택 필드 타입 검증

### Step 2-2: 문서 유형별 프론트매터 검증

**참조**: `${CLAUDE_SKILL_DIR}/references/frontmatter/{doc-type}.md`

문서 유형별 특화 검증:

- [ ] 유형별 필수/선택 필드 존재 여부
- [ ] 유형별 필드 값 범위 검증
- [ ] `skills`, `agents`, `subagents` 필드 순환 참조 방지
- [ ] 에이전트, 스킬 이름 일치 검증
- [ ] `name` 필드 유일성 검증

### Step 2-3: Skill 전용 - Description 사용 예시 트리거 검증

**참조**: `${CLAUDE_SKILL_DIR}/references/checklists/skill.md`

Skill 문서의 경우, `description` 필드에 사용 예시 트리거가 포함되어 있는지 검증:

- [ ] `description`에 "언제 사용하는지" (사용 예시/트리거)가 포함되어 있는지
- [ ] `description`에 구체적인 사용 예시 문장이 포함되어 있는지
- [ ] 트리거 예시가 실제 사용자가 입력할 만한 자연스러운 문장인지
- [ ] `user-invocable: true`인 경우 `argument-hint`가 적절히 설정되어 있는지

> **중요**: 사용 예시 트리거는 반드시 프론트매터 `description`에만 배치해야 함

## Step 3: 본문 구조 검증

### Step 3-1: 공통 문서 구조 검증

**참조**: `${CLAUDE_SKILL_DIR}/references/checklists/common/structure.md`

모든 문서 유형에 적용되는 공통 검증:

- [ ] 마크다운 헤딩 레벨 검사
- [ ] 필수/권장 섹션 존재 확인
- [ ] 가독성 및 일관성 평가
- [ ] 파일 경로 및 참조 검증 (Unix 스타일 `/` 사용)

### Step 3-2: 문서 유형별 구조 검증

**참조**: `${CLAUDE_SKILL_DIR}/references/checklists/{doc-type}.md`

문서 유형별 특화 검증:

- [ ] 유형별 필수 섹션 존재 확인
- [ ] 유형별 권장 섹션 존재 확인
- [ ] 유형별 금지 섹션 확인
- [ ] MCP 도구 이름 정규화 여부 (`ServerName:tool_name`)
- [ ] CLAUDE.md 중복 섹션 체크 (해당 시)

### Step 3-3: Skill 전용 - 본문 사용 예시 섹션 금지 검증

**참조**: `${CLAUDE_SKILL_DIR}/references/checklists/skill.md`

Skill 문서의 경우, 본문에 "사용 예시" 또는 "트리거" 섹션이 없는지 검증:

- [ ] 본문에 `## 사용 예시`, `## 예시`, `## 트리거` 등의 섹션이 없는지
- [ ] 본문에 사용자 호출 예시를 나열하는 섹션이 없는지
- [ ] 사용 예시 트리거가 프론트매터 `description`에만 포함되어 있는지

> **위반 시 처리**: WARNING 수준으로 보고, `description`으로 이동 권장

## Step 4: 참조 일관성 검증

### Step 4-1: 공통 참조 일관성 검증

**참조**: `${CLAUDE_SKILL_DIR}/references/checklists/common/cross-reference.md`

모든 문서 유형에 적용되는 공통 검증:

- [ ] 본문-프론트매터 일치성 검증
- [ ] 순환 참조 방지 (A → B → A 형태)
- [ ] 파일 참조 유효성 (`.claude/agents/`, `.claude/skills/`)
- [ ] 참조된 이름이 실제 파일의 `name` 필드와 일치
- [ ] MCP 도구 이름 정규화 (`ServerName:tool_name`)

### Step 4-2: 문서 유형별 참조 일관성 검증

**참조**: `${CLAUDE_SKILL_DIR}/references/checklists/{doc-type}.md`

문서 유형별 특화 검증:

| 검증 항목 | 대상 필드 | 검증 내용 |
|:----------|:----------|:----------|
| Skill 서브에이전트 연결 | `context: fork` 사용 시 `agent` 필드 필수 여부 | `.claude/agents/` 내 파일 존재 확인 |
| Agent 서브에이전트 참조 | `subagents` 필드 | 파일 존재 여부, `condition` 값 유효성 |
| 에이전트-스킬 이름 일치 | `agent`로 참조된 이름 | 실제 파일의 `name` 필드와 일치 |

**권장 변환 패턴:**

| 본문 패턴 | 프론트 매터 변환 | 적용 대상 |
|:----------|:-----------------|:----------|
| `use X agent`, `X 에이전트 사용` | `subagents: [X]` | Agent 문서 |
| `call X agent when complete` | `subagents: [{name: X, condition: on_complete}]` | Agent 문서 |
| `delegate to X agent` | `context: fork` + `agent: X` | Skill 문서 |

## Step 5: 결과 보고

1. 통합 검증 테이블 출력 (`${CLAUDE_SKILL_DIR}/references/templates/output.md` 참조)
2. 수정 제안 제공
3. 피드백 문서 저장 (`${CLAUDE_TMP_DIR}/validation/{CLAUDE_SESSION_ID}`)

---

# 출력 형식

검증 결과는 다음 템플릿을 따릅니다 (`${CLAUDE_SKILL_DIR}/references/templates/output.md`):

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

# 해야 할 행위 (Do's)

| 항목                  | 설명                                                                                                                  |
|:--------------------|:--------------------------------------------------------------------------------------------------------------------|
| YAML 문법 검증          | 프론트 매터 문법이 올바른지 확인                                                                                                  |
| MCP 도구 이름           | 정규화된 이름(`ServerName:tool_name`) 사용 여부 확인                                                                            |
| 고정 템플릿 준수           | 검토/평가 에이전트의 경우 필수 섹션 확인                                                                                             |
| 공식 문서 참고            | [Sub-agents](https://code.claude.com/docs/en/sub-agents), [Skills](https://code.claude.com/docs/en/skills) 문서 기준 검증 |
| **Skill 서브에이전트 연결** | `context: fork` 사용 시 `agent` 필드 필수 여부 확인                                                                            |
| **Agent 서브에이전트 참조** | `subagents` 필드의 에이전트 파일 존재 여부 확인                                                                                    |
| **에이전트 순환 참조 방지**   | A → B → A 형태의 무한 루프 가능성 체크                                                                                          |
| **에이전트-스킬 이름 일치**   | `agent`로 참조된 이름이 실제 파일의 `name`과 일치하는지 확인                                                                            |
| **본문-프론트매터 일치성**    | 본문의 에이전트 언급이 프론트 매터 `subagents`에 정의되어 있는지 확인                                                                        |
| **프론트 매터 전환 제안**    | 본문의 에이전트 호출을 `subagents`/`agent` 필드로 전환 가능한지 제안                                                                     |

# 하지 말아야 할 행위 (Don'ts)

| 항목                 | 설명                                                           |
|:-------------------|:-------------------------------------------------------------|
| 임의 표준 선언           | 새로운 프론트 매터 필드를 "표준"이라 주장하지 않기                                |
| 강제 자르기             | description을 1024자로 강제 자르지 않기 (사용자 수정 안내)                    |
| 선택 필드 과대평가         | 선택 필드 누락을 심각한 문제로 보고하지 않기 (경고 수준)                            |
| 로직 검증              | 에이전트의 로직 자체를 검증하지 않기 (문서 구조만)                                |
| 중첩 참조 과대평가         | 파일 참조 2단계 이상 중첩을 심각한 문제로 보고하지 않기                             |
| 강제 명명              | 동명사(gerund) 명명을 강제하지 않기 (권장사항일 뿐)                            |
| Bash Injection 남용  | `` `!command` `` 문법을 불필요하게 사용하지 않도록 경고                       |
| mcpServers 인라인 남용  | 인라인 MCP 정의를 권장하지 않음 (설정된 서버 참조 권장)                           |
| Skills color 필드 권장 | `color` 필드는 **스킬에서만** 더 이상 사용되지 않음 (에이전트는 지원)                |
| 본문 에이전트 직접 언급      | 본문에서 `use X agent` 직접 호출 대신 프론트 매터 `subagents`/`agent` 사용 권장 |
| 프론트 매터 누락 무시       | 본문의 에이전트 언급이 프론트 매터에 없으면 경고 수준으로 보고                          |

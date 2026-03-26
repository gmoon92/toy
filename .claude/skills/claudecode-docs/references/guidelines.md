# Claude Code 문서 작성 가이드라인

> Claude Code 메타 문서(Agent, Skill, Rule, Command, CLAUDE.md) 작성 시 준수해야 할 규칙

---

## 필수 준수사항 (Must)

### 모든 문서 유형 공통

| 항목 | 규칙 | 위반 시 |
|:-----|:-----|:--------|
| YAML 문법 | 프론트매터가 유효한 YAML 문법이어야 함 | ERROR |
| `name` 필드 | 64자 이하, 소문자/숫자/하이픈만 사용 | ERROR |
| `description` | "무엇을 하는지"와 "언제 사용하는지" 모두 포함 | WARNING |
| 파일 경로 | Unix 스타일(`/`) 사용, Windows 백슬래시 금지 | WARNING |

### Agent 문서

| 항목 | 규칙 | 위반 시 |
|:-----|:-----|:--------|
| `subagents` 참조 | `.claude/agents/` 내 실제 파일 존재 | ERROR |
| 순환 참조 | A → B → A 형태 금지 | ERROR |
| `model` | 지원 값만 사용 (`inherit`, `sonnet`, `opus`, `haiku`) | WARNING |

### Skill 문서

| 항목 | 규칙 | 위반 시 |
|:-----|:-----|:--------|
| `argument-hint` | `user-invocable: true` 시 필수 | ERROR |
| `agent` 필드 | `context: fork` 시 필수 | ERROR |
| 사용 예시 위치 | 본문이 아닌 프론트매터 `description`에만 | WARNING |
| 본문 길이 | 500줄 이하 권장 | INFO |
| `color` 필드 | 사용하지 않음 (지원 종료) | WARNING |

### Command 문서

| 항목 | 규칙 | 위반 시 |
|:-----|:-----|:--------|
| 파일 위치 | `.claude/commands/` 또는 하위 | ERROR |
| 라인 수 | 30줄 이하 | WARNING |

### Rule 문서

| 항목 | 규칙 | 위반 시 |
|:-----|:-----|:--------|
| 파일 위치 | `.claude/rules/` | ERROR |
| `paths` | 유효한 Glob 패턴 사용 | WARNING |
| `description` | 200자 이하 권장 | INFO |

### CLAUDE.md

| 항목 | 규칙 | 위반 시 |
|:-----|:-----|:--------|
| 프론트매터 | 없어야 함 (YAML 헤더 금지) | ERROR |
| 위치 | 프로젝트 루트 또는 모듈별 | - |
| 길이 | 150줄 이하 권장 | INFO |

---

## 권장사항 (Should)

### 모든 문서

1. **MCP 도구 이름 정규화**: `ServerName:tool_name` 형식 사용
2. **일관된 용어**: 하나의 용어 선택하여 문서 전체에서 사용
3. **파일 참조 깊이**: 한 단계 깊이로 제한 (중첩 참조 최소화)

### Skill 문서

1. **점진적 공개 패턴**: 복잡한 정보를 단계별로 제공
   - 단계 1: 목적과 기본 사용법
   - 단계 2: 상세 워크플로우
   - 단계 3: 고급 설정은 별도 문서로 분리

2. **문자열 치환 사용**: `$ARGUMENTS`, `${CLAUDE_SESSION_ID}`, `${CLAUDE_SKILL_DIR}`

### Agent 문서

1. **검토/평가 에이전트 템플릿**: 아래 6개 섹션 포함
   - 요약 (Summary)
   - 심각한 문제 (Critical Issues)
   - 경고 (Warnings)
   - 제안 (Suggestions)
   - 긍정적인 발견 (Positive Findings)
   - 체크리스트 (Checklist)

---

## 금지사항 (Must Not)

| 항목 | 설명 | 대안 |
|:-----|:-----|:-----|
| 강제 자르기 | `description`을 1024자로 강제 자르지 않기 | 사용자 수정 안내 |
| 임의 표준 선언 | 새로운 프론트매터 필드를 "표준"이라 주장하지 않기 | 실험적 필드로 표시 |
| 로직 검증 | 에이전트의 로직 자체를 검증하지 않기 | 문서 구조만 검증 |
| Bash Injection | `` `!command` `` 문법 남용하지 않기 | 안전한 명령어만 사용 |
| 인라인 MCP | `mcpServers` 인라인 정의 권장하지 않음 | 설정된 서버 참조 |
| 본문 에이전트 언급 | `use X agent` 직접 호출 대신 프론트매터 사용 | `subagents`/`agent` 필드 |

---

## 검토/평가 에이전트 정의

다음 키워드가 이름이나 description에 포함된 에이전트:
- `review`, `검토`, `평가`, `evaluate`, `validate`, `검증`
- `audit`, `체크`, `check`

**필수 출력 섹션**:
1. 요약 (Summary)
2. 심각한 문제 (Critical Issues)
3. 경고 (Warnings)
4. 제안 (Suggestions)
5. 긍정적인 발견 (Positive Findings)
6. 체크리스트 (Checklist)

---

## 참조

- [전환 패턴](transformation-patterns.md) - 본문-프론트매터 변환 가이드
- [체크리스트](checklists/) - 문서 유형별 검증 항목

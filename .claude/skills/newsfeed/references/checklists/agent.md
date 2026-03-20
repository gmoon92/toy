# 검증 체크리스트 (Validation Checklist)

## 1. 프론트 매터 검증

### Sub-agents 필수 필드 체크

- [ ] `name` 필드 존재
- [ ] `name`이 64자 이하
- [ ] `name`에 소문자/숫자/하이픈만 사용
- [ ] `name`에 XML 태그 없음
- [ ] `name`에 예약어(anthropic, claude) 없음
- [ ] `description` 필드 존재
- [ ] `description` 비어있지 않음
- [ ] `description` 1024자 이하
- [ ] `description`에 XML 태그 없음
- [ ] `description`이 "무엇을 하는지"와 "언제 사용하는지" 모두 포함

### Sub-agents 선택 필드 유효성

- [ ] `model` 값이 지원되는 값인지 (`inherit`, `sonnet`, `opus`, `haiku`, 전체 모델 ID)
- [ ] `permissionMode` 값이 지원되는 값인지 (`default`, `acceptEdits`, `dontAsk`, `bypassPermissions`, `plan`, `auto`)
- [ ] `memory` 값이 지원되는 범위인지 (`user`, `project`, `local`)
- [ ] `tools`가 문자열 배열인지
- [ ] `disallowedTools`가 문자열 배열인지
- [ ] `maxTurns`가 숫자인지
- [ ] `skills`가 문자열 배열인지
- [ ] `mcpServers`가 문자열 배열 또는 객체인지
- [ ] `background`가 boolean인지
- [ ] `isolation` 값이 `worktree`인지

### tools 필드 에이전트 제한 검증

- [ ] `Agent(agent_type)` 문법이 올바른지
- [ ] 지정된 에이전트 타입이 존재하는지
- [ ] 에이전트 제한이 의도된 보안 정책인지

### mcpServers 형식 검증

- [ ] 문자열 참조 시 해당 서버가 설정되어 있는지
- [ ] 인라인 정의 시 `type`, `command` 필드 존재 확인
- [ ] 인라인 정의가 서브에이전트에서만 사용되는지 (플러그인에서는 지원 안 함)

### hooks 형식 검증

- [ ] `PreToolUse` 이벤트가 유효한 matcher와 hooks를 포함하는지
- [ ] `PostToolUse` 이벤트가 유효한 matcher와 hooks를 포함하는지
- [ ] `Stop` 이벤트가 서브에이전트에서만 사용되는지
- [ ] hook의 `matcher`가 지원되는 도구/이벤트 이름인지
- [ ] hook의 `type`이 `command`인지
- [ ] hook의 `command`가 유효한 명령어인지
- [ ] 플러그인 에이전트에 `hooks`가 없는지 (플러그인에서는 지원 안 함)

### Agent 문서 서브에이전트 참조 검증

- [ ] `subagents` 필드가 유효한 배열 또는 객체 형식인지
- [ ] `subagents`에 정의된 에이전트가 `.claude/agents/` 디렉토리에 실제 존재하는지
- [ ] `subagents`의 각 항목에 `name`, `condition` 등 필수 속성이 있는지
- [ ] `subagents.condition` 값이 지원되는 조건인지 (`on_complete`, `on_error`, `always`)
- [ ] 순환 참조가 없는지 (A → B → A 형태의 무한 루프 방지)

### 본문-프론트매터 일치성 검증

- [ ] 본문에 "use X agent", "X 에이전트 사용" 등의 패턴이 있는지
- [ ] 언급된 에이전트가 프론트 매터 `subagents`에 정의되어 있는지
- [ ] 본문에서 직접 호출하는 에이전트가 프론트 매터로 대체 가능한지

**권장 변환 패턴:**

| 본문 패턴 | 프론트 매터 변환 | 적용 대상 |
|:----------|:-----------------|:----------|
| `use X agent`, `X 에이전트 사용` | `subagents: [X]` | Agent 문서 |
| `call X agent when complete` | `subagents: [{name: X, condition: on_complete}]` | Agent 문서 |

---

## 2. 에이전트 문서 구조 검증

### 필수 섹션

- [ ] 핵심 역량 (또는 유사한 개요 섹션)
- [ ] 해야 할 행위 (Workflow / 사용 방법)
- [ ] 하지 말아야 할 행위 (Never / 주의사항)
- [ ] 출력 형식 (Output Format)

### 권장 섹션

- [ ] Input (입력 형식)
- [ ] Output (출력 예시)
- [ ] Example (사용 예시)
- [ ] Decision Framework (결정 프레임워크)

---

## 3. 검토/평가 에이전트 고정 템플릿 검증

검토/리뷰/검증 성격의 에이전트는 다음 고정 템플릿을 준수해야 합니다:

### 필수 출력 섹션

- [ ] 요약 (Summary)
- [ ] 심각한 문제 (Critical Issues - 반드시 수정)
- [ ] 경고 (Warnings - 수정 권장)
- [ ] 제안 (Suggestions - 선택 사항)
- [ ] 긍정적인 발견 (Positive Findings)
- [ ] 체크리스트 (Checklist)

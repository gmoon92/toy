# 검증 체크리스트 (Validation Checklist)

## 1. 프론트 매터 검증

### Skills 필수/권장 필드 검증

- [ ] `name`이 64자 이하이고 소문자/숫자/하이픈만 사용
- [ ] `name`에 예약어(anthropic, claude) 없음
- [ ] `name`에 XML 태그 없음
- [ ] `description`이 존재하는지 (생략 시 마크다운 첫 단락 사용)
- [ ] `description`이 1024자 이하인지
- [ ] `description`이 "무엇을 하는지"와 "언제 사용하는지" 모두 포함
- [ ] `description`이 3인칭 시점으로 작성됨 (예: "처리합니다" not "도와드릴 수 있습니다")

### Skills 선택 필드 유효성

- [ ] `argument-hint`가 문자열인지
- [ ] `disable-model-invocation`이 boolean인지
- [ ] `user-invocable`이 boolean인지
- [ ] `allowed-tools`가 문자열 배열인지
- [ ] `model` 값이 지원되는 값인지
- [ ] `context` 값이 `fork`인지
- [ ] `context: fork` 사용 시 `agent` 필드가 함께 정의되어 있는지
- [ ] `agent` 값이 지원되는 에이전트 타입인지 (`Explore`, `Plan`, `general-purpose`)
- [ ] `hooks`가 유효한 객체 형식인지 (플러그인에서는 지원 안 함)

### Skills 문자열 치환 검증

- [ ] `$ARGUMENTS` 사용 시 인자 처리 로직 포함
- [ ] `$N` 또는 `$ARGUMENTS[N]` 사용 시 인자 범위 확인
- [ ] `${CLAUDE_SESSION_ID}` 사용 시 유효한 사용처인지
- [ ] `${CLAUDE_SKILL_DIR}` 사용 시 스킬 디렉토리 참조 확인

### Skills Bash Injection 검증

- [ ] `` `!command` `` 문법 사용 시 명령어가 안전한지
- [ ] Bash injection이 실제로 필요한 경우인지
- [ ] 명령어 출력이 너무 크지 않은지

### Skills 서브에이전트 연결 검증

- [ ] `context: fork`와 `agent` 필드가 함께 사용되는 경우, 해당 에이전트 파일이 `.claude/agents/`에 존재하는지
- [ ] `agent`로 지정된 에이전트의 `name`이 실제 파일의 `name`과 일치하는지
- [ ] Skill이 포크할 에이전트 타입이 Skill의 목적과 일치하는지

### 본문-프론트매터 일치성 검증

- [ ] Skill 본문의 에이전트 언급이 `context: fork` + `agent` 필드로 전환 가능한지
- [ ] `fork to X agent`, `delegate to X agent` 패턴이 적절히 프론트 매터에 반영되었는지

**권장 변환 패턴:**

| 본문 패턴 | 프론트 매터 변환 | 적용 대상 |
|:----------|:-----------------|:----------|
| `fork to X agent` | `context: fork` + `agent: X` | Skill 문서 |
| `delegate to X agent` | `context: fork` + `agent: X` | Skill 문서 |

---

## 2. 스킬(SKILL.md) 문서 구조 검증

### 필수 섹션

- [ ] 이름 및 설명
- [ ] Workflow (사용 흐름)
- [ ] Tool Use Examples (도구 사용 예시)

### 권장 섹션

- [ ] Input / Output 정의
- [ ] 예시 코드
- [ ] 주의사항

### 추가 검증 (Skill 특화)

- [ ] SKILL.md 본문이 500줄 이하
- [ ] 파일 참조가 한 단계 깊이로 유지됨 (중첩 참조 없음)
- [ ] 점진적 공개 패턴이 적절히 사용됨
- [ ] 시간에 민감한 정보가 없음 (또는 "이전 패턴" 섹션에 있음)
- [ ] 일관된 용어 사용 (하나의 용어 선택하여 Skill 전체에서 사용)
- [ ] Windows 스타일 경로(백슬래시 `\`)가 없음 (Unix 슬래시 `/` 사용)
- [ ] MCP 도구 사용 시 정규화된 이름 사용: `ServerName:tool_name`

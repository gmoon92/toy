# 스킬(Skill) 검증 체크리스트

> **참조**:
> - [프론트매터 공통 검증](./common/frontmatter.md)
> - [문서 구조 공통 검증](./common/structure.md)
> - [참조 일관성 공통 검증](./common/cross-reference.md)
> - [Skill 프론트매터 스펙](../frontmatter/skill.md)

## 1. 스킬 특화 프론트매터 검증

### 선택 필드 유효성

- [ ] `argument-hint`가 문자열인지
- [ ] `disable-model-invocation`이 boolean인지
- [ ] `user-invocable`이 boolean인지
- [ ] `allowed-tools`가 문자열 배열인지
- [ ] `context` 값이 `fork`인지
- [ ] `context: fork` 사용 시 `agent` 필드가 함께 정의되어 있는지
- [ ] `agent` 값이 지원되는 에이전트 타입인지 (`Explore`, `Plan`, `general-purpose`)
- [ ] `hooks`가 유효한 객체 형식인지 (플러그인에서는 지원 안 함)
- [ ] `effort` 값이 지원되는 값인지 (`low`, `medium`, `high`, `max`)

### Description 사용 예시 트리거 검증

- [ ] `description`에 "다음 상황에서 사용합니다" 또는 유사한 트리거 문구 포함
- [ ] `description`에 구체적인 사용 예시(예시 문장)가 포함되어 있는지
- [ ] 트리거 예시가 실제 사용자가 입력할 만한 자연스러운 문장인지
- [ ] `user-invocable: true`인 경우 `argument-hint`도 함께 검증

> **중요**: 사용 예시 트리거는 반드시 프론트매터 `description`에만 배치
> - 본문에는 "사용 예시" 또는 "트리거" 섹션을 별도로 구성하지 않음

### 문자열 치환 검증

- [ ] `$ARGUMENTS` 사용 시 인자 처리 로직 포함
- [ ] `$N` 또는 `$ARGUMENTS[N]` 사용 시 인자 범위 확인
- [ ] `${CLAUDE_SESSION_ID}` 사용 시 유효한 사용처인지
- [ ] `${CLAUDE_SKILL_DIR}` 사용 시 스킬 디렉토리 참조 확인

### Bash Injection 검증

- [ ] `` `!command` `` 문법 사용 시 명령어가 안전한지
- [ ] Bash injection이 실제로 필요한 경우인지
- [ ] 명령어 출력이 너무 크지 않은지

### 서브에이전트 연결 검증

- [ ] `context: fork`와 `agent` 필드가 함께 사용되는 경우, 해당 에이전트 파일이 `.claude/agents/`에 존재하는지
- [ ] `agent`로 지정된 에이전트의 `name`이 실제 파일의 `name`과 일치하는지
- [ ] Skill이 포크할 에이전트 타입이 Skill의 목적과 일치하는지

---

## 2. 스킬 특화 문서 구조 검증

### 필수 섹션

- [ ] 이름 및 설명
- [ ] Workflow (사용 흐름)
- [ ] Tool Use Examples (도구 사용 예시)

### 권장 섹션

- [ ] Input / Output 정의
- [ ] 예시 코드
- [ ] 주의사항

### 금지 섹션

- [ ] **본문에 "사용 예시" 또는 "트리거" 섹션이 없음**
  - 사용 예시 트리거는 프론트매터 `description`에만 포함되어야 함
  - 본문에 별도의 "예시" 섹션을 두지 않음 (예: `## 사용 예시`, `## 트리거`, `## 예시`)

### 추가 검증 (Skill 특화)

- [ ] SKILL.md 본문이 500줄 이하
- [ ] 파일 참조가 한 단계 깊이로 유지됨 (중첩 참조 없음)
- [ ] 점진적 공개 패턴이 적절히 사용됨
- [ ] 시간에 민감한 정보가 없음 (또는 "이전 패턴" 섹션에 있음)
- [ ] 일관된 용어 사용 (하나의 용어 선택하여 Skill 전체에서 사용)
- [ ] Windows 스타일 경로(백슬래시 `\`)가 없음 (Unix 슬래시 `/` 사용)
- [ ] MCP 도구 사용 시 정규화된 이름 사용: `ServerName:tool_name`

---

## 3. 권장 변환 패턴

| 본문 패턴 | 프론트 매터 변환 | 적용 대상 |
|:----------|:-----------------|:----------|
| `fork to X agent` | `context: fork` + `agent: X` | Skill 문서 |
| `delegate to X agent` | `context: fork` + `agent: X` | Skill 문서 |

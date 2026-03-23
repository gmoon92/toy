# 프론트매터 공통 검증 (Common Frontmatter Validation)

> **참조**: 문서 유형별 프론트매터 스펙을 확인하세요
> - [Agent](../frontmatter/agent.md)
> - [Skill](../frontmatter/skill.md)
> - [Command](../frontmatter/command.md)
> - [Rule](../frontmatter/rule.md)

## 1. 기본 필드 검증

### name 필드 (에이전트/스킬 필수, 커맨드/룰 선택)

- [ ] `name`이 존재하는지 (에이전트/스킬 필수)
- [ ] `name`이 64자 이하인지
- [ ] `name`에 소문자/숫자/하이픈만 사용했는지
- [ ] `name`에 XML 태그가 없는지
- [ ] `name`에 예약어(anthropic, claude)가 없는지

### description 필드 (권장)

- [ ] `description`이 존재하는지 (생략 시 마크다운 첫 단락 사용)
- [ ] `description`이 비어있지 않은지
- [ ] `description`에 XML 태그가 없는지
- [ ] `description`이 "무엇을 하는지"와 "언제 사용하는지" 모두 포함하는지
- [ ] `description` 길이가 제한 내인지
  - 에이전트/스킬: 1024자 이하
  - 룰: 200자 이하 (간결 권장)

## 2. 선택 필드 타입 검증

각 문서 유형별 선택 필드의 타입이 올바른지 검증합니다:

- [ ] `model` 값이 지원되는 값인지
- [ ] `tools`가 문자열 배열인지
- [ ] `skills`가 문자열 배열인지
- [ ] `mcpServers`가 문자열 배열 또는 객체인지
- [ ] `hooks`가 유효한 객체 형식인지

## 3. YAML 문법 검증

- [ ] 프론트매터가 유효한 YAML 문법인지
- [ ] `---`로 시작하고 끝나는지
- [ ] 필드 이름이 소문자/언더스코어로 작성되었는지

# Skill 검증 체크리스트

## Skill 특화 프론트매터 검증

- [ ] `user-invocable: true` 시 `argument-hint` 필수
- [ ] `context: fork` 시 `agent` 필수
- [ ] `agent`: Explore, Plan, general-purpose만 사용
- [ ] `disable-model-invocation`: boolean
- [ ] `allowed-tools`: 문자열 배열
- [ ] `hooks`: 유효한 객체 형식
- [ ] `effort`: low, medium, high, max 중 하나

## Skill 특화 본문 구조 검증

### 필수 섹션
- [ ] 이름 및 설명
- [ ] Workflow
- [ ] Tool Use Examples

### 금지 섹션
- [ ] 본문에 "사용 예시", "트리거", "예시" 섹션 없음
- [ ] 사용 예시는 프론트매터 `description`에만

### 추가 검증
- [ ] 본문 500줄 이하
- [ ] Windows 경로(`\`) 없음
- [ ] MCP 도구 이름: `ServerName:tool_name`

---

**참조**:
- [공통 프론트매터 검증](../common/frontmatter.md) - `name`, `description` 등
- [공통 구조 검증](../common/structure.md) - 헤딩 레벨, 경로 등
- [공통 참조 검증](../common/cross-reference.md) - 순환 참조, 파일 존재 등
- [가이드라인](../../guidelines.md)
- [전환 패턴](../../transformation-patterns.md)

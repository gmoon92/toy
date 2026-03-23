# 커맨드(Command) 검증 체크리스트

> **참조**:
> - [프론트매터 공통 검증](./common/frontmatter.md)
> - [문서 구조 공통 검증](./common/structure.md)
> - [Command 프론트매터 스펙](../frontmatter/command.md)

## 1. 커맨드 특화 파일 위치 검증

- [ ] 커맨드가 `.claude/commands/<namespace>/<command>.md` 또는 `.claude/commands/<command>.md` 파일로 정의되었는지
- [ ] 문서의 총 라인이 30줄 이하인지
  - 최소 동작 구조만 작성하거나, skill을 라우팅 (연결 참조 링크) 형식으로 호출해야 함
  - 커맨드는 레거시 형식으로 클로드 코드 skill 기능을 권장함

## 2. 커맨드 특화 프론트매터 검증

- [ ] 프론트매터가 생략 가능하지만, 작성되어 있다면 정의된 필드가 [프론트 매터](../frontmatter/command.md) 문서에 정의되어 있는지

---

## 3. 참고: 커맨드와 스킬의 차이점

| 항목 | Command | Skill |
|------|---------|-------|
| 파일 위치 | `.claude/commands/` | `.claude/skills/` |
| 프론트매터 | 생략 가능 | 권장 |
| 길이 제한 | 30줄 이하 | 500줄 이하 |
| 주요 용도 | 단순 명령, 스킬 라우팅 | 복잡한 워크플로우 |
| `user-invocable` | 기본적으로 `/` 메뉴에 표시 | `user-invocable: true` 필요 |

# Command Validation Checklist

## 1. Commands 문서 구조 검증

- [ ] 커멘드는 `.claude/commands/<namespace>/<command>.md` 또는 `.claude/commands/<command>.md` 파일로 정의한다.
- [ ] 문서의 총 라인은 30 line 제한한다.
  - 최소 동작 구조만 작성하거나, skill 을 라우팅 (연결 참조 링크) 형식으로 호출해야한다. 
  - 커멘드는 레거시 형식으로 클로드 코드 skill 기능을 권장한다.
- [ ] 커멘드 프론트메터는 생략 가능하지만, 작성되어 있다면 프론트메터 필드를 검증한다.
  - 정의된 필드가 [프론트 메터](../frontmatter/command.md) 문서에 정의되어 있는 필드를 사용하는지

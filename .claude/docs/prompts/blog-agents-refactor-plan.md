# blog agents 리팩토링 작업 지시

## 작업 1: 디렉토리 이름 변경

`.claude/agents/docs/` → `.claude/agents/blog/` 로 이름을 변경합니다.
하위 파일 전체(reviewer.md, writer.md, translator.md, resources/)를 포함하여 이동합니다.

## 작업 2: writer.md 재작성

`writer.md`는 순수 블로그 글쓰기 에이전트입니다. `resources/korean-writing-guide.md` 내용으로 본문을 교체합니다.

- 기존 writer.md 내용은 삭제
- frontmatter(name, description, model, color)는 유지
- `resources/korean-writing-guide.md` 내용을 본문으로 사용

## 작업 3: 오케스트레이션 스킬 생성

reviewer → writer 흐름을 `/skill-creator`를 사용하여 스킬로 생성합니다.

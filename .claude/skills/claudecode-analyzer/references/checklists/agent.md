# Agent 검증 체크리스트

## Agent 특화 프론트매터 검증

- [ ] `model`: inherit, sonnet, opus, haiku 중 하나
- [ ] `permissionMode`: default, acceptEdits, dontAsk, bypassPermissions, plan, auto 중 하나
- [ ] `memory`: user, project, local 중 하나
- [ ] `tools`: 문자열 배열
- [ ] `disallowedTools`: 문자열 배열
- [ ] `maxTurns`: 숫자
- [ ] `skills`: 문자열 배열
- [ ] `mcpServers`: 문자열 배열 또는 객체
- [ ] `background`: boolean
- [ ] `isolation`: worktree만 사용

## 서브에이전트 검증

- [ ] `subagents`가 유효한 배열/객체 형식
- [ ] `subagents.condition`: on_complete, on_error, always 중 하나

## hooks 검증

- [ ] `PreToolUse`: 유효한 matcher와 hooks 포함
- [ ] `PostToolUse`: 유효한 matcher와 hooks 포함
- [ ] `Stop`: 서브에이전트에서만 사용
- [ ] `matcher`: 지원되는 도구/이벤트 이름
- [ ] `type`: command만 사용

## Agent 특화 본문 구조 검증

### 필수 섹션
- [ ] 핵심 역량 (또는 유사 개요)
- [ ] 해야 할 행위 (Workflow)
- [ ] 하지 말아야 할 행위 (Never)
- [ ] 출력 형식 (Output Format)

### 검토/평가 에이전트 필수 섹션
- [ ] 요약 (Summary)
- [ ] 심각한 문제 (Critical Issues)
- [ ] 경고 (Warnings)
- [ ] 제안 (Suggestions)
- [ ] 긍정적인 발견 (Positive Findings)
- [ ] 체크리스트 (Checklist)

---

**참조**:
- [공통 프론트매터 검증](../common/frontmatter.md) - `name`, `description`, `subagents` 등
- [공통 구조 검증](../common/structure.md) - 헤딩 레벨, 경로 등
- [공통 참조 검증](../common/cross-reference.md) - 순환 참조, 파일 존재 등
- [가이드라인](../../guidelines.md)
- [전환 패턴](../../transformation-patterns.md)

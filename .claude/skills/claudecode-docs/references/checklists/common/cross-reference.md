# 참조 일관성 공통 검증 (Common Cross-Reference Validation)

## 1. 본문-프론트매터 일치성 검증

### 에이전트/스킬 공통

- [ ] 본문에 언급된 에이전트/스킬이 프론트매터에 정의되어 있는지
- [ ] 본문의 호출 패턴이 프론트매터와 일치하는지
- [ ] 순환 참조가 없는지 (A → B → A 형태 방지)

**권장 변환 패턴:**

| 본문 패턴 | 프론트 매터 변환 | 적용 대상 |
|:----------|:-----------------|:----------|
| `use X agent`, `X 에이전트 사용` | `subagents: [X]` | Agent 문서 |
| `call X agent when complete` | `subagents: [{name: X, condition: on_complete}]` | Agent 문서 |
| `fork to X agent`, `delegate to X agent` | `context: fork` + `agent: X` | Skill 문서 |

## 2. 파일 참조 유효성

- [ ] 참조하는 에이전트 파일이 `.claude/agents/`에 존재하는지
- [ ] 참조하는 스킬 파일이 `.claude/skills/`에 존재하는지
- [ ] 참조된 이름이 실제 파일의 `name` 필드와 일치하는지

## 3. MCP 도구 이름 정규화

- [ ] MCP 도구 사용 시 정규화된 이름 형식 사용: `ServerName:tool_name`

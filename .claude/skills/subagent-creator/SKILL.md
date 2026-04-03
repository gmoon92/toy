---
name: subagent-creator
description: |
  Claude Code 서브 에이전트를 생성하고 설정합니다.
  커스텀 시스템 프롬프트와 도구 구성으로 전문화된 에이전트를 만들 때 사용합니다.

  다음 상황에서 사용합니다:
  - 새 서브 에이전트나 커스텀 에이전트를 만들 때
  - 특정 작업에 전문화된 AI 워크플로우를 설정할 때
  - 에이전트 frontmatter(tools, model, permissionMode 등) 설정을 구성할 때
---

# 서브 에이전트 생성기

Claude Code에서 특정 작업을 전담하는 전문화된 서브 에이전트를 생성합니다.

## 서브 에이전트 파일 형식

서브 에이전트는 YAML frontmatter를 포함한 Markdown 파일로 다음 위치에 저장됩니다:
- **프로젝트**: `.claude/agents/` (높은 우선순위)
- **사용자**: `~/.claude/agents/` (낮은 우선순위)

### 구조

```markdown
---
name: subagent-name
description: 이 서브 에이전트를 사용할 시점 ("use proactively" 포함 시 자동 위임)
tools: Tool1, Tool2, Tool3  # 선택 — 생략 시 모든 도구 상속
model: sonnet               # 선택 — sonnet/opus/haiku/inherit 또는 전체 모델 ID
permissionMode: default     # 선택 — default/acceptEdits/dontAsk/bypassPermissions/plan
skills: skill1, skill2      # 선택 — 자동 로드할 스킬
maxTurns: 10                # 선택 — 최대 에이전틱 턴 수
---

시스템 프롬프트 작성. 역할, 책임, 동작 방식을 정의합니다.
```

### 설정 필드

전체 필드 명세는 메타 문서를 참고하세요: `.claude/docs/claude-code-meta/frontmatter/agent.md`

## 생성 워크플로우

1. **요구사항 수집**
   EXECUTE AskUserQuestion:
   - 목적 (purpose): 어떤 작업을 위한 에이전트인가
   - 이름 (name), 범위 (scope): project `.claude/agents/` 또는 user `~/.claude/agents/`
   - 도구 (tools), 모델 (model)
   - 주요 책임 (responsibilities)

2. **Task 위임**
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "서브 에이전트 파일 생성: {name}"
     prompt: """
     다음 요구사항에 맞는 서브 에이전트 파일을 생성하세요:
     - Name: {name}
     - Purpose: {purpose}
     - Scope: {scope} (project: .claude/agents/ / user: ~/.claude/agents/)
     - Tools: {tools}
     - Model: {model}
     - Responsibilities: {responsibilities}

     See: assets/subagent-template.md
     See: references/examples.md
     See: references/available-tools.md
     RETURN FORMAT: "Task 반환 형식" 섹션 참조.
     """

3. **검증**
   - 생성된 파일 경로 확인
   - 사용자가 필요시 Read 도구로 파일 확인

## Task 반환 형식

CRITICAL: Task 에이전트는 메타데이터만 반환해야 합니다.
- Return: 파일 경로, 상태 요약 (5줄 이하)
- Never: 전체 파일 내용

## 효과적인 서브 에이전트 작성법

### description 필드 작성 가이드

`description` 필드는 자동 위임의 핵심입니다:

```yaml
# 좋음 — 구체적인 트리거 명시
description: 코드 리뷰 전문가. 코드 작성 또는 수정 후 PROACTIVELY 사용하세요.

# 좋음 — 명확한 사용 사례
description: 오류, 테스트 실패, 예상치 못한 동작을 위한 디버깅 전문가.

# 나쁨 — 너무 모호함
description: 코드를 도와줍니다
```

### 시스템 프롬프트 작성 지침

1. **역할 명확히 정의**: `"당신은 [구체적인 전문가 역할] 전문가입니다"`
2. **호출 시 수행할 작업 나열**: 첫 번째로 할 일
3. **책임 범위 명시**: 이 서브 에이전트가 처리하는 작업
4. **지침 포함**: 제약 사항 및 모범 사례
5. **출력 형식 정의**: 응답 구조화 방법

### 도구 선택 기준

- **읽기 전용 작업**: `Read, Grep, Glob, Bash`
- **코드 수정 작업**: `Read, Write, Edit, Grep, Glob, Bash`
- **전체 접근 권한**: `tools` 필드 생략

## References

> **주의**: 이 파일들은 메인 컨텍스트에서 직접 로드하지 마세요. Task 에이전트에 참조로만 전달하세요.

- `assets/subagent-template.md` — 새 서브 에이전트 작성용 템플릿
- `references/examples.md` — 완성된 서브 에이전트 예시 모음
- `references/available-tools.md` — 사용 가능한 도구 전체 목록


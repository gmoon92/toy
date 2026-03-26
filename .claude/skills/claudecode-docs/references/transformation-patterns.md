# 본문-프론트매터 전환 패턴

> 본문에서 직접 호출하는 에이전트를 프론트매터로 정의할 때 사용하는 패턴

---

## Agent 문서용 패턴

### 기본 서브에이전트 호출

| 본문 패턴 | 프론트매터 변환 |
|:----------|:----------------|
| `use X agent` | `subagents: [X]` |
| `X 에이전트 사용` | `subagents: [X]` |
| `delegate to X agent` | `subagents: [X]` |
| `X 에이전트에 위임` | `subagents: [X]` |

### 조걶적 호출

| 본문 패턴 | 프론트매터 변환 |
|:----------|:----------------|
| `call X agent when complete` | `subagents: [{name: X, condition: on_complete}]` |
| `invoke X on error` | `subagents: [{name: X, condition: on_error}]` |
| `fallback to X on failure` | `subagents: [{name: X, condition: on_error}]` |
| `always notify X` | `subagents: [{name: X, condition: always}]` |

---

## Skill 문서용 패턴

### 포크 에이전트

| 본문 패턴 | 프론트매터 변환 |
|:----------|:----------------|
| `fork to X agent` | `context: fork` + `agent: X` |
| `delegate to X agent` | `context: fork` + `agent: X` |
| `spawn X agent` | `context: fork` + `agent: X` |
| `X 에이전트를 포크` | `context: fork` + `agent: X` |

### 지원되는 에이전트 타입

`context: fork`와 함께 사용할 수 있는 `agent` 값:

| 값 | 설명 |
|:---|:-----|
| `Explore` | 코드베이스 탐색 전용 에이전트 |
| `Plan` | 구현 계획 설계 에이전트 |
| `general-purpose` | 범용 작업 에이전트 |

---

## 변환 예시

### 예시 1: Agent - 기본 호출

**변환 전 (본문)**:
```markdown
# My Agent

1. Analyze the code
2. use linter agent to check style
3. Report results
```

**변환 후 (프론트매터)**:
```markdown
---
name: my-agent
subagents: [linter]
---

# My Agent

1. Analyze the code
2. Linter agent checks style automatically
3. Report results
```

### 예시 2: Agent - 조걶적 호출

**변환 전 (본문)**:
```markdown
# Review Agent

Review the code.
If issues found, call fixer agent.
```

**변환 후 (프론트매터)**:
```markdown
---
name: review-agent
subagents:
  - name: fixer
    condition: on_error
---

# Review Agent

Review the code.
Fixer agent will be called if issues found.
```

### 예시 3: Skill - 포크

**변환 전 (본문)**:
```markdown
# My Skill

Fork to explore agent to search the codebase.
```

**변환 후 (프론트매터)**:
```markdown
---
name: my-skill
context: fork
agent: Explore
---

# My Skill

Explores the codebase automatically.
```

---

## 검증 체크리스트

- [ ] 본문의 에이전트 언급이 프론트매터 `subagents`/`agent`에 정의됨
- [ ] `subagents`는 실제 `.claude/agents/` 내 파일과 일치
- [ ] `condition` 값은 지원되는 값만 사용 (`on_complete`, `on_error`, `always`)
- [ ] 순환 참조가 없음 (A → B → A 형태 방지)

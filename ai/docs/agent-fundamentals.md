# Agent Fundamentals

Claude Code Agent의 기초 개념과 설계 방법을 설명합니다.

---

## 1. Agent 개요

Claude Code Agent는 **행위 계약(Behavior Contract)**으로 설계합니다. Agent 문서는 **System Prompt**이며, 특정 작업을 수행하는 **Task Contract**를 정의합니다.

핵심은 **에이전트 역할 경계**와 **입력·출력 계약**을 명확히 하는 것입니다.

---

## 2. 행위 계약(Behavior Contract) 설계

### 2.1 역할-책임-입출력 정의

Agent는 **함수처럼 설계**하는 것이 가장 안정적입니다.

Agent 문서는 다음을 정의합니다:

```
1. Role (정체성)
2. Responsibilities (수행 작업)
3. Input Context (참조 정보)
4. Output Contract (결과 형식)
5. Constraints (행동 제약)
```

Persona보다 **작업 정의와 제약 조건**이 행동을 더 강하게 제어합니다.

```
Input → Processing → Output
```

### 2.2 제약 조건 설정

제약 조건은 에지 케이스를 처리하고 예측 가능한 동작을 보장합니다.

| 제약 유형 | 예시 |
|----------|------|
| **범위 제한** | "오직 코드 리뷰만 수행, 직접 수정 금지" |
| **출력 형식** | "JSON 형식으로만 응답" |
| **시간 제한** | "최대 5턴 이내 완료" |
| **도구 제한** | "Edit/Write 도구 사용 금지" |

---

## 3. 문서 구조와 작성 규약

### 3.1 프론트매터

에이전트 .md 파일은 `.claude/agents/{filename}.md` yaml 프론트매터와 system prompt로 구성됩니다.

```markdown
---
name: architect
description: Use this agent when designing system architecture or selecting technology stacks.
model: sonnet
---

You are a software architect responsible for designing system architecture based on requirements.
```

**프론트매터 필드:**

| 필드 | 필수 | 설명 |
|------|------|------|
| `name` | ✅ | 에이전트 식별자 |
| `description` | ✅ | 사용 상황 설명 (Agent 선택 힌트) |
| `model` | 선택 | 사용 모델 (sonnet/opus/haiku) |

> description은 **Agent 선택 힌트**로 사용됩니다. description은 행동 지시가 아니라 **사용 상황 설명**입니다.

### 3.2 System Prompt 구조

본문(system prompt)은 다음 구조가 가장 안정적입니다.

```
Role
Responsibilities
Process
Output Format
Constraints
```

**권장 길이**: 200~800 tokens (대략 20~40줄)

> 너무 긴 system prompt는 효과가 감소합니다.

---

## 4. 기본 역할 템플릿

### 4.1 Architect

```yaml
---
name: architect
description: Use this agent when designing system architecture or selecting technology stacks.
---

You are a software architect.

## Responsibilities
- Design system architecture based on requirements
- Select appropriate technology stacks
- Define component boundaries and interfaces

## Process
1. Analyze requirements
2. Identify constraints and trade-offs
3. Propose architecture options
4. Evaluate and recommend

## Output Format
- Architecture diagram description
- Component list with responsibilities
- Technology choices with rationale

## Constraints
- Do not write implementation code
- Focus on high-level design
- Consider scalability and maintainability
```

### 4.2 Developer

```yaml
---
name: developer
description: Use this agent when implementing features or writing code.
---

You are a software developer.

## Responsibilities
- Implement features according to specifications
- Write clean, maintainable code
- Follow project conventions

## Process
1. Understand requirements
2. Design implementation approach
3. Write code with tests
4. Verify functionality

## Output Format
- Code changes
- Brief explanation of changes
- Test cases if applicable

## Constraints
- Do not modify unrelated code
- Follow existing code style
- Always run tests before finishing
```

### 4.3 Reviewer

```yaml
---
name: reviewer
description: Use this agent when reviewing code or design for quality and issues.
---

You are a code reviewer.

## Responsibilities
- Review code for bugs and issues
- Check adherence to best practices
- Identify security vulnerabilities
- Suggest improvements

## Process
1. Read and understand the code/design
2. Identify issues (bugs, style, security)
3. Categorize by severity
4. Provide actionable feedback

## Output Format
- Summary of findings
- Detailed issues list with line references
- Suggestions for improvement

## Constraints
- Do not modify the code yourself
- Be constructive and specific
- Prioritize critical issues
```

---

## 5. 자주 하는 실수

이러한 템플릿을 작성할 때 흔히 발생하는 실수는 다음과 같습니다.

### 5.1 Persona에 과도하게 의존

**❌ 잘못된 접근:**
```
You are a wise wizard who codes with magic...
```

**✅ 올바른 접근:**
```
You are a senior backend engineer.

## Responsibilities
- Design and implement APIs
- Optimize database queries
...
```

> Persona는 행동 스타일에 영향을 주지만 **핵심 제어 요소는 아닙니다.**

### 5.2 모호한 출력 형식

**❌ 잘못된 접근:**
```
리뷰해주세요.
```

**✅ 올바른 접근:**
```
## Output Format
- Critical issues (must fix)
- Warnings (should fix)
- Suggestions (nice to have)
```

### 5.3 과도하게 긴 프롬프트

**❌ 잘못된 접근:**
100줄 이상의 system prompt

**✅ 올바른 접근:**
20~40줄로 핵심만 정의

> 너무 긴 프롬프트는 "Lost in the Middle" 현상 발생

### 5.4 제약 조건 누락

**❌ 잘못된 접근:**
제약 조건 없이 자율에 맡김

**✅ 올바른 접근:**
```
## Constraints
- Do not use external libraries without approval
- Maximum 3 files per change
- Always add tests for new features
```

---

## 참고 문서

- [claude-code-agent-skills.md](./claude-code-agent-skills.md) - Skills 상세 작성법
- [agent-team-practice.md](./agent-team-practice.md) - Agent Team 실전 활용

---

*생성일: 2026-03-23*
*통합 문서: claude-code-agent-design-guide.md 기반*

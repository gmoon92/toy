# Claude Code Agent 설계 가이드

Claude Code Agent는 **행위 계약(Behavior Contract)**으로 설계합니다.

Agent 문서는 **System Prompt**이며, 특정 작업을 수행하는 **Task Contract**를 정의합니다.

핵심은 **에이전트 역할 경계**와 **입력·출력 계약**을 명확히 하는 것입니다.

## Agent = Task Contract

Agent는 **함수처럼 설계**하는 것이 가장 안정적입니다.

Agent 문서는 다음을 정의합니다.

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

---

# 에이전트 문서 구조 (권장)

에이전트 .md 파일은 `.claude/agents/{filename}.md` yaml 프론트매터와 system prompt 로 구성됩니다.

````markdown
---
name: architect
description: Use this agent when designing system architecture or selecting technology stacks.
---

You are a software architect responsible for designing system architecture based on requirements.
````

- description: 에이전트 사용 상황 설명
- system prompt: 행동 지침 및 작업 정의

description은 **Agent 선택 힌트**로 사용됩니다. description은 행동 지시가 아니라 **사용 상황 설명**입니다.

### 본문

본문(system prompt)엔 다음 구조가 가장 안정적입니다.

```
Role
Responsibilities
Process
Output Format
Constraints
```

권장 길이 200 ~ 800 tokens 으로 (대략 20~40줄)

너무 긴 system prompt는 효과가 감소합니다.

---

# Persona의 역할

Persona는 행동 스타일에 영향을 주지만 **핵심 제어 요소는 아닙니다.**

```
Persona → 행동 스타일
Constraints → 행동 범위 제어
Output format → 결과 구조 제어
```

예

```
You are a security engineer
```

같은 Role priming은 응답 방향에 영향을 줄 수 있습니다.

하지만 실제 행동은 대부분 **Rules와 Constraints**에 의해 결정됩니다.

---

# Agent 템플릿

## Architect Agent

```markdown
Role:
Software Architect

Responsibilities:

- Design system architecture
- Define service boundaries
- Select architecture patterns
- Identify scalability and reliability concerns

Process:

1. Analyze requirements
2. Identify core components
3. Define system interactions
4. Select technology stack
5. Document design decisions

Output Format:
Architecture document including:

- System overview
- Component structure
- Data flow
- Technology decisions

Constraints:

- Do NOT write implementation code
- Focus only on architecture-level decisions
```

핵심

```
설계만 수행
코드 작성 금지
```

---

## Developer Agent

```markdown
Role:
Software Developer

Responsibilities:

- Implement modules and services
- Follow architecture guidelines
- Write maintainable code
- Add unit tests

Process:

1. Review architecture
2. Explore existing code patterns
3. Implement the solution
4. Validate functionality

Output Format:

- Source code
- Unit tests
- Brief explanation

Constraints:

- Follow architecture decisions
- Do NOT change system design
```

핵심

```
설계를 수정하지 않는다
구현만 수행
```

---

## Reviewer Agent

```markdown
Role:
Code Reviewer

Responsibilities:

- Identify bugs
- Verify architecture compliance
- Review code quality
- Suggest improvements

Process:

1. Analyze code
2. Detect issues
3. Categorize severity
4. Provide recommendations

Output Format:

1. Summary
2. Critical Issues
3. Major Issues
4. Minor Issues
5. Suggestions

Constraints:

- Do NOT rewrite code
- Focus on identifying problems
```

핵심

```
코드 작성 금지
문제 식별 중심
```

---

# 역할 경계

기본 구조

| Agent     | 역할     |
|-----------|--------|
| Architect | 시스템 설계 |
| Developer | 구현     |
| Reviewer  | 품질 검증  |

Pipeline 예

```
Requirements
↓
Architect
↓
Developer
↓
Reviewer
```

---

# Agent Interaction Model (추가)

Agent 시스템에서는 **에이전트 간 입력·출력 연결**이 중요합니다.

예

```
Architect → produces architecture document
Developer → consumes architecture document and produces code
Reviewer → consumes code and produces review report
```

즉

```
Agent Contract + Agent Pipeline
```

두 가지가 함께 설계되어야 합니다.

---

# 자주 하는 실수

## Persona만 강조

```
You are a world-class engineer
```

효과는 제한적입니다.

---

## 문서가 너무 김

긴 system prompt는 모델이 일부를 무시할 수 있습니다.

---

## 역할 경계가 모호

```
Architect가 코드 작성
Developer가 설계 변경
Reviewer가 코드 재작성
```

이 경우 **Agent pipeline이 무너집니다.**

---

# 결론

좋은 Agent 문서는 다음 요소로 구성됩니다.

```
Role Definition
Responsibilities
Process
Output Contract
Constraints
```

그리고 가장 중요한 설계 요소는

```
Agent Role Boundaries
+
Agent Interaction Pipeline
```

입니다.

Agent 문서 구조보다 **Agent 간 역할 분리와 입력·출력 계약**이 시스템 품질에 더 큰 영향을 미칩니다.

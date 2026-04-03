# Round 1 — 블루팀 설계 초안

> 작성일: 2026-04-04
> 목적: subagent-creator 스킬 최적 개선 설계안 도출

---

## 1. SKILL.md 개선안

### 변경 사항 요약

| 항목 | 현재 | 변경 |
|------|------|------|
| `### 시스템 프롬프트 작성 지침` 5개 항목 | 일반 원칙 나열 | 제거 |
| `### 도구 선택 기준` 3가지 조합 | `available-tools.md`와 중복 | 제거 |
| `## 서브 에이전트 파일 형식` frontmatter 코드 블록 | `subagent-template.md`와 중복 | 제거, 필드 링크만 유지 |
| `## 생성 워크플로우` Step 3 검증 | "Read로 파일 확인" | 제거 |
| `## Task 반환 형식` 섹션 | Task에서 참조 불가 | prompt 내 인라인 삽입 후 섹션 제거 |
| Step 1 AskUserQuestion | 무조건 실행 | 누락 정보만 질문하는 조건부 실행으로 변경 |
| `## References` 주의문 | Task 참조 불가 오해 가능 | Task에서 어떻게 참조하는지 명확화 |

### 전체 내용

```markdown
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

전체 필드 명세: `.claude/docs/claude-code-meta/frontmatter/agent.md`

## 생성 워크플로우

1. **요구사항 수집**
   누락된 정보가 있을 때만 AskUserQuestion을 실행합니다:
   - 목적 (purpose): 어떤 작업을 위한 에이전트인가
   - 이름 (name), 범위 (scope): project `.claude/agents/` 또는 user `~/.claude/agents/`
   - 도구 (tools), 모델 (model)
   - 성향 (archetype): reviewer / fixer / executor / writer / analyst / auditor
   - 주요 책임 (responsibilities)

   사용자가 이미 충분한 정보를 제공한 경우 이 단계를 건너뜁니다.

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
     - Archetype: {archetype}
     - Responsibilities: {responsibilities}

     See: assets/templates/{archetype}.md  (성향별 템플릿)
     See: references/available-tools.md   (도구 조합 참조)

     RETURN FORMAT (엄수):
     - 생성한 파일의 절대 경로
     - 상태 한 줄 요약
     - 핵심 설정 (tools, model, permissionMode) 한 줄
     - 전체 파일 내용 반환 금지
     """

## References

스킬 로드 시 메인 컨텍스트에 자동 포함됩니다. Task prompt에서 상대경로로 참조하세요.

- `assets/templates/` — 성향별 서브 에이전트 템플릿
- `references/available-tools.md` — 도구 조합 가이드
```

---

## 2. 성향별 템플릿 구조

### 성향 검토 결과

review.md가 제안한 6가지 성향(Reviewer, Fixer, Executor, Writer, Analyst, Auditor)을 검토한 결과:

**유지**: 6가지 모두 유지. 각 성향은 명확히 구분되는 권한 모델과 프롬프트 구조를 가짐.

**추가 고려**: `Planner` (실행 없이 계획만 생성, plan 모드)는 Auditor에 흡수 가능하므로 별도 추가 불필요.

**성향별 핵심 구조 차이점**:

| 성향 | 권한 | 핵심 프롬프트 패턴 | 출력 형식 |
|------|------|-------------------|-----------|
| Reviewer | Read, Grep, Glob, Bash | 체크리스트 → 우선순위 피드백 | 심각/경고/제안 3단계 |
| Fixer | Read, Edit, Write, Grep, Glob, Bash | 원인 추적 → 최소 변경 수정 | 근본원인 + 코드 수정안 |
| Executor | tools 생략 (전체) | 실행 → 검증 루프 | 실행 결과 + 검증 상태 |
| Writer | Read, Write, Edit, Glob, Grep | 독자 정의 → 구조화된 문서 | 마크다운/문서 구조 |
| Analyst | Read, Grep, Glob, Bash | 가설 수립 → 데이터 검증 → 인사이트 | 발견 + 권고사항 |
| Auditor | Read, Grep, Glob, Bash + permissionMode: plan | 패턴 스캔 → 위험 분류 | 심각도 + CWE 참조 |

---

### reviewer.md

```markdown
---
name: 에이전트-이름
description: [대상]을 검토하는 전문 리뷰어. [트리거 조건] 시 즉시 사용하세요.
tools: Read, Grep, Glob, Bash
model: inherit
---

당신은 [도메인] 품질 기준을 지키는 [역할명] 리뷰어입니다.

호출 시 수행할 작업:

1. [검토 대상 식별 방법 — 예: git diff, 특정 경로]
2. [검토 범위 확인]
3. 즉시 리뷰 시작

체크리스트:

- [항목 1]
- [항목 2]
- [항목 3]

우선순위별 피드백:

- 심각한 문제 (반드시 수정)
- 경고 (수정 권장)
- 제안 (개선 고려)

각 문제에 구체적인 수정 방법을 예시와 함께 제공하세요.
```

---

### fixer.md

```markdown
---
name: 에이전트-이름
description: [문제 유형] 해결 전문가. [트리거 조건] 시 즉시 사용하세요.
tools: Read, Edit, Write, Grep, Glob, Bash
model: inherit
---

당신은 [도메인] 근본 원인 분석을 전문으로 하는 [역할명]입니다.

호출 시 수행할 작업:

1. [문제 증상 및 컨텍스트 수집]
2. [재현 단계 또는 실패 위치 파악]
3. [원인 격리]
4. 최소한의 변경으로 수정
5. 수정 결과 검증

디버깅/해결 프로세스:

- [단계 1: 진단]
- [단계 2: 가설 수립]
- [단계 3: 검증]
- [단계 4: 수정]

각 문제에 대해 제공할 내용:

- 근본 원인 설명
- 진단 근거
- 구체적인 코드 수정안
- 검증 방법
- 재발 방지 권고사항

증상이 아닌 근본 문제를 수정하는 데 집중하세요.
```

---

### executor.md

```markdown
---
name: 에이전트-이름
description: [작업 유형]을 실행하고 검증하는 전문가. [트리거 조건] 시 즉시 사용하세요.
# tools 생략 — 전체 도구 상속
model: inherit
---

당신은 [도메인] 작업 실행과 검증을 전담하는 [역할명]입니다.

호출 시 수행할 작업:

1. [작업 대상 및 범위 파악]
2. [사전 조건 확인]
3. [작업 실행]
4. [실행 결과 검증]
5. 이상 시 롤백 또는 재시도

실행-검증 루프:

- 각 단계 완료 후 상태 확인
- 예상 결과와 실제 결과 대조
- 실패 시 원인 분석 후 재시도

실행 결과 보고:

- 수행한 작업 목록
- 각 단계 성공/실패 여부
- 최종 상태 요약
- 잔여 작업 또는 후속 조치 필요 사항
```

---

### writer.md

```markdown
---
name: 에이전트-이름
description: [문서 유형]을 작성하고 개선하는 전문가. [트리거 조건] 시 사용하세요.
tools: Read, Write, Edit, Glob, Grep
model: haiku
---

당신은 [도메인] 문서화를 전문으로 하는 [역할명]입니다.

호출 시 수행할 작업:

1. [문서화 대상 파악]
2. 대상 독자 확인: [독자 유형]
3. [문서 구조 결정]
4. 명확하고 구조적인 문서 작성

작성 가이드라인:

- [스타일 원칙 1]
- [스타일 원칙 2]
- 코드 예시는 실행 가능한 형태로 포함
- 일관된 용어 사용

출력 형식:

- [문서 구조 설명 — 예: H2 섹션, 코드 블록, 표]
- [파일 저장 위치 또는 반환 방식]
```

---

### analyst.md

```markdown
---
name: 에이전트-이름
description: [분석 대상]에 대한 인사이트를 도출하는 분석가. [트리거 조건] 시 사용하세요.
tools: Read, Grep, Glob, Bash
model: sonnet
---

당신은 [도메인] 데이터와 패턴 분석을 전문으로 하는 [역할명]입니다.

호출 시 수행할 작업:

1. [분석 목적 및 질문 명확화]
2. [데이터 또는 대상 수집]
3. 가설 수립
4. 데이터 검증으로 가설 테스트
5. 인사이트 및 권고사항 도출

분석 방법론:

- [분석 기법 1]
- [분석 기법 2]
- 패턴, 이상치, 트렌드 식별

분석 결과 보고:

- 주요 발견사항 (우선순위 순)
- 발견을 뒷받침하는 근거
- 실행 가능한 권고사항
- 추가 조사가 필요한 영역
```

---

### auditor.md

```markdown
---
name: 에이전트-이름
description: [감사 대상]의 취약점과 위험을 검토하는 감사자. [트리거 조건] 시 즉시 사용하세요.
tools: Read, Grep, Glob, Bash
model: sonnet
permissionMode: plan
---

당신은 [도메인] 감사를 전문으로 하는 [역할명]입니다.

호출 시 수행할 작업:

1. [감사 범위 정의]
2. 알려진 취약점/위험 패턴 스캔
3. [접근 제어, 데이터 처리 등 핵심 영역] 검토
4. 발견 사항 위험도 분류
5. 감사 보고서 작성

감사 체크리스트:

- [위험 카테고리 1]
- [위험 카테고리 2]
- [위험 카테고리 3]

보고서 형식:

- 심각도: 심각 / 높음 / 중간 / 낮음
- 위치: [파일 경로 또는 컴포넌트]
- 설명: [취약점/위험 설명]
- 권장 수정 방법
- 참고자료 (CWE, OWASP 등 해당 시)

NOTE: permissionMode: plan — 실행 없이 계획만 제안합니다. 실제 수정은 사용자 승인 후 수행됩니다.
```

---

## 3. examples.md 처리 결정

### 결정: 역할 재정의 후 최소 유지

성향별 템플릿이 생성되면 examples.md의 주요 역할(패턴 예시 제공)은 템플릿으로 이전됩니다.
그러나 완전 제거보다는 역할을 재정의하는 것이 더 낫습니다.

**근거**:

1. **제거 불가 이유**: 템플릿은 구조(skeleton)를 제공하고, examples.md는 실제 완성된 에이전트의 구체적 내용(flesh)을 보여줍니다. Task 에이전트가 프롬프트 내용(체크리스트 항목, 책임 목록)을 작성할 때 구체적인 참조가 필요합니다.

2. **완전 유지 불가 이유**: data-scientist 예시는 BigQuery 특화로 일반 패턴 참조에 부적합합니다. 현재 6개 예시는 각 성향과 1:1 매핑이 가능하므로 중복입니다.

3. **재정의**: examples.md를 "각 성향의 완성본 레퍼런스 1개씩"으로 재편합니다.
   - data-scientist 제거
   - 나머지 5개 예시를 각 성향(Reviewer=code-reviewer, Fixer=debugger, Executor=test-runner, Writer=doc-writer, Auditor=security-auditor)에 1:1 매핑
   - Analyst 성향 예시 추가 (data-scientist 대체, 일반적인 코드/로그 분석 에이전트로)
   - 파일 위치를 `references/examples.md`에서 `assets/templates/` 내 각 파일의 "완성 예시" 섹션으로 통합하거나, examples.md를 각 성향별로 분리

**최종 형태**: `references/examples.md`를 성향별 완성 예시 1개씩 6개로 재편 유지.
(파일을 별도 분리하지 않고 단일 파일로 유지해 Task 참조 단순화)

---

## 4. available-tools.md 개선안

### 변경 사항

| 제거 항목 | 이유 |
|-----------|------|
| `## 핵심 도구` 표 (Read~Task) | Claude가 이미 아는 기본 도구 |
| `## 상호작용 도구` (AskUserQuestion) | Claude가 이미 앎 |
| `## 웹 도구` (WebFetch, WebSearch) | 서브 에이전트 설계 시 불필요 |

| 유지 항목 | 이유 |
|-----------|------|
| `## 자주 사용하는 도구 조합` | 성향별 tools 필드 직접 참조 |
| `## IDE 도구` | Claude가 존재를 모를 수 있는 도구 |
| `## MCP 도구` | 형식 설명 필요 |

### 전체 내용

```markdown
# 서브 에이전트 도구 조합 가이드

서브 에이전트는 `tools` 필드로 권한을 제한합니다. 생략 시 메인 스레드의 모든 도구를 상속합니다.

## 자주 사용하는 도구 조합

### 읽기 전용 조사
```
tools: Read, Grep, Glob, Bash
```
용도: 코드 분석, 문서 검토, 코드베이스 탐색 (Reviewer, Auditor)

### 코드 수정
```
tools: Read, Write, Edit, Grep, Glob, Bash
```
용도: 기능 구현, 버그 수정, 리팩터링 (Fixer, Writer)

### 최소 읽기 권한
```
tools: Read, Grep, Glob
```
용도: 보안 감사, 보고 전용 검토 (Auditor 강화)

### 전체 접근
`tools` 필드를 생략하면 사용 가능한 모든 도구를 상속합니다. (Executor)

## IDE 도구 (사용 가능한 경우)

| 도구 | 설명 |
|------|------|
| `mcp__ide__getDiagnostics` | VS Code 언어 진단 정보 (타입 오류, 린트 등) |
| `mcp__ide__executeCode` | Jupyter 커널에서 코드 실행 (Analyst 활용) |

## MCP 도구

MCP 도구는 `tools` 필드에 직접 명시해야 합니다. 형식: `mcp__<서버>__<도구>`

예시:
```yaml
tools: Read, Grep, mcp__ide__getDiagnostics, mcp__sequential-thinking__sequentialthinking
```
```

---

## 5. 최종 디렉토리 구조

```
.claude/skills/subagent-creator/
├── SKILL.md                              # 개선됨: 중복 제거, 버그 수정, 조건부 실행
├── assets/
│   └── templates/                        # 변경: 단일 템플릿 → 성향별 6개
│       ├── reviewer.md                   # 체크리스트 + 우선순위 피드백
│       ├── fixer.md                      # 근본원인 추적 + 최소 수정
│       ├── executor.md                   # 실행-검증 루프 + 전체 권한
│       ├── writer.md                     # 독자 정의 + 구조화 문서
│       ├── analyst.md                    # 가설-검증 + 인사이트 도출
│       └── auditor.md                    # plan 모드 + 위험도 분류
└── references/
    ├── available-tools.md                # 개선됨: 핵심만 남김 (조합 + IDE + MCP)
    └── examples.md                       # 역할 재정의: 성향별 완성본 1개씩 6개

삭제:
└── assets/subagent-template.md           # 성향별 templates/로 완전 대체
```

---

## 핵심 결정사항 요약

1. **버그 2개 수정**: Task prompt에 반환 형식 인라인 삽입 (외부 섹션 참조 제거), Step 1 AskUserQuestion을 "누락 정보만 질문"하는 조건부 실행으로 변경.

2. **성향별 템플릿 6종 신규 설계**: Reviewer/Fixer/Executor/Writer/Analyst/Auditor로 분리하여 각 성향의 권한 모델, 프롬프트 패턴, 출력 형식을 구조화. 기존 단일 `subagent-template.md` 제거.

3. **examples.md 역할 재정의**: 완전 제거 대신 성향별 완성본 레퍼런스 1개씩 6개로 재편 — 템플릿이 skeleton을 제공하고 examples가 flesh를 제공하는 분업 구조 유지.

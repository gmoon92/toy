# Round 2 — 레드팀 비판

> 작성일: 2026-04-04
> 목적: 블루팀 round-1 설계안의 실용성, 복잡성, 논리적 일관성 검증

---

## 치명적 문제 (반드시 수정)

### [치명 1] Task 프롬프트의 상대경로 참조가 실제로 동작하지 않는다

블루팀 SKILL.md의 Task prompt:

```
See: assets/templates/{archetype}.md
See: references/available-tools.md
```

**문제**: Task 에이전트는 독립 컨텍스트에서 실행된다. `assets/templates/reviewer.md`와 같은 상대경로는 Task 에이전트가 실제로 파일을 읽어야 의미가 있다. 그러나 프롬프트 안에서 `See: 경로` 형태로 적어도, Task 에이전트가 스스로 Read 도구를 호출하지 않으면 파일 내용이 컨텍스트에 들어오지 않는다.

**근거**: 기존 SKILL.md의 버그 1("Task 에이전트가 읽을 수 없는 참조")을 review.md가 명확히 지적했고, 블루팀도 "RETURN FORMAT을 인라인 삽입"으로 수정했다. 그러나 같은 논리로, `See: assets/templates/{archetype}.md`도 동일한 문제다. 이 경로를 "참조하라"는 지시만으로는 템플릿 내용이 Task 에이전트에게 전달되지 않는다.

**수정 방향**: 두 가지 중 하나를 선택해야 한다.
1. SKILL.md(메인 컨텍스트)에서 archetype에 맞는 템플릿 파일을 직접 Read한 뒤, 그 내용을 Task prompt 안에 인라인으로 삽입한다.
2. Task 에이전트 prompt에 "먼저 `assets/templates/{archetype}.md`를 Read 도구로 읽은 뒤 참조하라"는 명시적 지시를 추가한다.

현재 설계는 이 중 어느 것도 하지 않아 템플릿이 사실상 무용지물이 된다.

---

### [치명 2] 성향(archetype) 수집 항목이 워크플로우에 통합되지 않아 실행 불가 상태다

블루팀 SKILL.md Step 1:

```
- 성향 (archetype): reviewer / fixer / executor / writer / analyst / auditor
```

Step 2 Task prompt:

```
- Archetype: {archetype}
See: assets/templates/{archetype}.md
```

**문제 A**: `{archetype}`은 스킬 문서에서 사용하는 "서술적 자리표시자"다. 실제 Claude Code 스킬 실행 시 `{archetype}` 변수가 자동으로 치환되는 메커니즘은 없다. 메인 에이전트(스킬 실행 주체)가 Step 1에서 수집한 값을 직접 문자열 보간(string interpolation)해서 Task에 넘겨야 한다. 이 부분이 SKILL.md에 명시되지 않으면 실제 동작 시 `{archetype}`이 그대로 전달된다.

**문제 B**: 기존 원본 SKILL.md에는 `archetype` 항목이 아예 없었다. 블루팀이 성향 개념을 도입하면서 Step 1 수집 항목에 추가했지만, 이것이 사용자에게 노출되는 UX 문제를 검토하지 않았다. 사용자가 "reviewer / fixer / executor / writer / analyst / auditor" 중 하나를 선택하게 강제하는 것은 에이전트 생성 흐름을 오히려 경직시킨다. 일반 사용자는 "나는 Fixer가 필요해"가 아니라 "버그를 고쳐주는 에이전트가 필요해"라고 말한다.

**수정 방향**: 
- 변수 자리표시자 사용 방식을 실제 Claude 스킬의 동작 방식에 맞게 재기술하거나, 자리표시자 개념을 제거하고 서술형 지시로 대체한다.
- 성향은 사용자가 직접 선택하는 입력이 아니라, Claude가 목적(purpose) 설명을 듣고 추론하는 내부 분류로 처리해야 한다.

---

### [치명 3] examples.md와 templates/의 공존 구조가 역할 혼란을 만든다

블루팀 결정: "템플릿이 skeleton, examples가 flesh — 분업 구조 유지"

**문제**: 이 논리는 듣기에는 그럴듯하지만 실제 Task 에이전트 입장에서 보면 오히려 혼란이다.

- Task 에이전트는 이미 `assets/templates/{archetype}.md`를 참조하라고 지시받는다.
- 동시에 `references/examples.md`도 참조 목록에 남는다면(블루팀은 examples.md를 유지하기로 결정), Task 에이전트는 두 소스를 어떻게 조합해야 하는지 알 수 없다.
- 특히 `reviewer.md` 템플릿과 examples.md의 `code-reviewer` 예시가 공존할 때, 어느 것을 우선 따라야 하는가? 둘 사이 불일치가 발생하면 어떻게 되는가?

**근거**: review.md 원본은 "성향별 템플릿이 생기면 examples.md의 역할이 중복될 수 있음. examples.md 제거 후 templates/로 완전 대체하는 방안 논의 필요"라고 명시했다. 블루팀은 이 논의를 "분업 구조"로 해소했다고 주장하지만, 실제로는 정확히 review.md가 경고한 중복 상황을 존속시켰다.

**수정 방향**: 두 가지 중 하나를 선택해야 한다.
1. `examples.md`를 완전 제거하고 각 `templates/*.md` 파일 내에 "완성 예시" 섹션을 추가한다 (파일을 넘나드는 참조 최소화).
2. `templates/`를 제거하고 `examples.md`를 성향별 완성 템플릿으로 업그레이드한다.
공존은 곧 중복이며, 중복은 불일치의 씨앗이다.

---

## 경고 (수정 권장)

### [경고 1] `archetype` 개념을 사용자에게 노출하는 것이 실제 UX에 적합하지 않다

Step 1 수집 항목에 "성향 (archetype): reviewer / fixer / executor / writer / analyst / auditor"가 포함되어 있다. 이것은 내부 설계 언어다. 실제 사용자가 스킬을 호출할 때 이 분류를 이미 알고 있다고 가정하기 어렵다.

현실적인 사용 시나리오: 사용자가 "CI 파이프라인에서 테스트를 실행하고 결과를 리포트해주는 에이전트 만들어줘"라고 요청한다. Claude가 이를 `executor` 성향으로 분류하는 것은 내부 판단이어야 하지, 사용자에게 "executor를 원하십니까?"라고 묻는 것은 불필요한 전문용어 노출이다.

**수정 방향**: archetype을 "사용자 입력 항목"이 아닌 "Claude의 내부 추론 결과"로 재정의. Step 1에서 purpose/responsibilities를 수집한 뒤, Step 2 전에 Claude가 스스로 archetype을 추론하는 단계를 추가.

---

### [경고 2] `available-tools.md` 개선안에서 "최소 쓰기 권한" → "최소 읽기 권한" 용어 불일치

블루팀 available-tools.md:

```
### 최소 읽기 권한
tools: Read, Grep, Glob
용도: 보안 감사, 보고 전용 검토 (Auditor 강화)
```

기존 원본 available-tools.md:

```
### 최소 쓰기 권한
tools: Read, Grep, Glob
용도: 보안 감사, 코드 리뷰 (보고 전용)
```

**문제**: "최소 읽기 권한"이라는 명칭은 의미가 모호하다. `Read, Grep, Glob`은 실제로 "읽기 권한만 있는 것"이 아니라 "쓰기(Edit/Write/Bash) 권한을 제거한 것"이다. 원본의 "최소 쓰기 권한"이 더 직관적이다. 블루팀이 이름을 바꾼 이유가 명확하지 않으며, 오히려 혼란을 유발한다.

**수정 방향**: "읽기 전용(Read-Only)" 또는 원본 "최소 쓰기 권한"으로 명칭 통일.

---

### [경고 3] 6개 성향 분류에서 `Reviewer`와 `Auditor`의 실질적 차이가 불명확하다

블루팀 표:
- Reviewer: 권한 `Read, Grep, Glob, Bash` / 체크리스트 → 우선순위 피드백
- Auditor: 권한 `Read, Grep, Glob, Bash` / 패턴 스캔 → 위험 분류 (+ permissionMode: plan)

**문제**: 사용 권한이 동일하고, 작업 방식도 "체크리스트 + 피드백"과 "패턴 스캔 + 위험 분류"로 실질적으로 매우 유사하다. 유일한 차이는 `permissionMode: plan`인데, 이것이 6번째 성향을 정당화하는 충분한 근거인지 의문이다. 예를 들어 보안 감사가 필요한 Reviewer에게 `permissionMode: plan`만 추가하면 Auditor와 동일해진다.

**수정 방향**: Auditor를 별도 성향으로 유지하되, 템플릿에서 "permissionMode: plan 강제"가 핵심 구분자임을 더 명확히 강조. 또는 Reviewer 템플릿에 "보안 감사 용도로는 permissionMode: plan을 추가하라"는 주석을 달고 Auditor를 Reviewer의 특수화 형태로 처리.

---

### [경고 4] 블루팀이 `review.md`의 Step 3 검증 제거를 반영했는지 불명확하다

review.md: "Step 3 (검증) 제거 대상 — Claude가 당연히 수행하는 것"

블루팀 SKILL.md 개선안에는 Step 3(검증)이 없다. 이 부분은 올바르게 제거되었다. 그러나 변경 사항 요약 표에서 "Step 3 검증 제거"를 명시적으로 언급하지 않았다.

**문제**: 이는 누락 여부를 확인하기 어렵게 만든다. 의도적 제거인지, 단순히 포함하지 않은 것인지 불명확하다. 블루팀 설계가 완전한지 검증하기 위한 체크가 부족하다.

**수정 방향**: 변경 사항 요약 표에 "Step 3 검증 제거" 항목 명시 추가 (문서화 완결성).

---

## 제안 (고려 사항)

### [제안 1] `SKILL.md`의 `## References` 섹션 설명이 여전히 오해를 유발할 수 있다

블루팀 SKILL.md:

```
## References
스킬 로드 시 메인 컨텍스트에 자동 포함됩니다. Task prompt에서 상대경로로 참조하세요.
```

**문제**: "메인 컨텍스트에 자동 포함"과 "Task prompt에서 상대경로로 참조"는 서로 모순적이다. 메인 컨텍스트에 자동 포함된다면 Task prompt에서 별도로 참조할 필요가 없다. 메인 컨텍스트에서만 읽히고 Task에는 전달되지 않는다면 "Task prompt에서 참조"하라는 지시는 잘못된 것이다.

원본 SKILL.md의 주의문("이 파일들은 메인 컨텍스트에서 직접 로드하지 마세요. Task 에이전트에 참조로만 전달하세요")이 오히려 더 정확했다.

**수정 방향**: References 섹션에서 "메인 컨텍스트 자동 포함"과 "Task 참조" 중 실제 동작 방식을 명확히 하고 일관되게 기술.

---

### [제안 2] 6개 템플릿 파일의 `[브래킷 자리표시자]` 구조가 Task 에이전트에게 과도한 작업을 부여한다

블루팀 `reviewer.md` 템플릿 예시:

```
description: [대상]을 검토하는 전문 리뷰어. [트리거 조건] 시 즉시 사용하세요.
...
체크리스트:
- [항목 1]
- [항목 2]
```

**문제**: 이 수준의 브래킷 자리표시자는 Task 에이전트가 모든 항목을 새로 채워야 함을 의미한다. 실질적으로 "템플릿을 보고 처음부터 작성하라"와 다르지 않다. 기존 `subagent-template.md`와 추상화 수준이 거의 동일하다.

성향별 템플릿의 실질적 가치는 "이 성향에 특화된 구체적인 프롬프트 패턴"을 제공하는 것이어야 한다. 예를 들어 `reviewer.md`라면 "심각/경고/제안" 3단계 피드백 구조의 실제 마크다운 형식이 채워져 있어야 한다. 현재는 그 부분도 `[항목]`으로 비어 있다.

**수정 방향**: 각 템플릿에서 성향 고유의 구조적 요소(Reviewer의 3단계 피드백 형식, Auditor의 심각도 보고 형식 등)는 실제 내용으로 사전 작성하고, 도메인 특화 부분(체크리스트 항목 등)만 브래킷으로 남긴다.

---

### [제안 3] SKILL.md에서 `model: inherit`을 기본값으로 명시하지 않았다

writer.md 템플릿의 기본 모델: `haiku`
analyst.md, auditor.md 기본 모델: `sonnet`
reviewer.md, fixer.md, executor.md 기본 모델: `inherit`

**문제**: 성향별 기본 모델이 다른데, 이 결정 근거가 SKILL.md나 템플릿에 설명되어 있지 않다. Task 에이전트가 모델을 선택할 때 이 기본값을 따라야 하는지, 사용자 입력을 우선해야 하는지 불명확하다.

**수정 방향**: 각 템플릿 또는 성향 표에 "기본 모델 선택 근거" 주석 추가.

---

## 블루팀 설계 중 잘 된 부분

### 잘 됨 1: 버그 1 수정 (RETURN FORMAT 인라인 삽입)

기존 SKILL.md의 `RETURN FORMAT: "Task 반환 형식" 섹션 참조`가 실제로 동작하지 않는 버그임을 정확히 파악하고, Task prompt 내에 반환 형식을 인라인으로 삽입한 것은 올바른 수정이다. 이 수정은 반드시 유지해야 한다.

### 잘 됨 2: 조건부 AskUserQuestion으로 변경

Step 1을 "누락된 정보만 질문"하는 조건부 실행으로 변경한 것은 실용적인 개선이다. 사용자가 이미 충분한 컨텍스트를 제공했을 때 불필요한 질문으로 흐름을 끊는 문제를 해결한다.

### 잘 됨 3: available-tools.md의 불필요한 섹션 제거

"Claude가 이미 아는" 핵심 도구 표, 상호작용 도구, 웹 도구를 제거하고 실제로 서브 에이전트 설계에 필요한 도구 조합, IDE 도구, MCP 도구만 남긴 결정은 review.md의 제거 대상을 정확히 반영한 좋은 판단이다.

### 잘 됨 4: 성향별 권한 모델 명시화

6개 성향 각각에 대해 tools 조합과 permissionMode를 명확히 설계한 표는 이 스킬의 핵심 가치다. Reviewer와 Auditor가 동일 도구를 쓰더라도 permissionMode 차이로 명확히 구분되는 설계는 실용적이다.

### 잘 됨 5: SKILL.md 프론트매터의 description 개선

기존 description과 동일하게 유지하면서 불필요한 내용을 제거하고 핵심 사용 사례에 집중한 것은 적절하다.

---

## 요약

**치명적 문제 3개:**

1. Task 에이전트에게 `See: assets/templates/{archetype}.md`로 참조를 지시해도 파일 내용이 실제로 전달되지 않는다 (치명 1).
2. `{archetype}` 변수 자리표시자가 자동 치환되지 않아 실행 시 그대로 전달된다 (치명 2A), 또한 성향을 사용자 입력 항목으로 노출하는 것이 UX를 경직시킨다 (치명 2B).
3. `templates/`와 `examples.md` 공존이 review.md가 경고한 중복 문제를 그대로 재현하며, Task 에이전트 입장에서 두 소스 간 우선순위가 불명확하다 (치명 3).

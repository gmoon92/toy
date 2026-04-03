# Round 3 — 블루팀 개선안

> 작성일: 2026-04-04
> 목적: 레드팀 round-2 비판을 수용하여 치명적 문제를 해결한 최종 설계안 도출

---

## 레드팀 비판 수용/반박

### [치명 1] Task 프롬프트의 상대경로 참조 동작 불가 → 수용

**수용**. `See: assets/templates/{archetype}.md`는 Task 에이전트에게 아무것도 전달하지 않는다.
레드팀이 제시한 두 가지 방향 중 **옵션 C (템플릿 없이 SKILL.md prompt에 구조 직접 내장)** 를 선택한다.

근거:
- 옵션 A (메인에서 Read → 인라인 삽입): 아키타입 추론 후 파일 읽기 → 삽입 흐름은 동작하지만, 파일 수가 6개로 늘어나고 SKILL.md가 파일 관리 로직까지 담아야 한다.
- 옵션 B (Task에게 Read 지시): Task 에이전트가 스스로 Read를 호출해야 하므로 한 번의 추가 왕복이 생기고, 파일 경로를 여전히 정확히 알아야 한다.
- 옵션 C (SKILL.md 직접 내장): 파일 수 최소화, 외부 참조 의존도 제거, Task 에이전트에게 확실하게 컨텍스트 전달. 6개 성향 구조 전체를 SKILL.md 내에 넣어도 길이는 관리 가능하다.

**결정**: templates/ 디렉토리를 생성하지 않는다. 성향별 구조(권한, 프롬프트 패턴, 출력 형식)를 SKILL.md의 Task prompt 섹션에 직접 내장한다.

---

### [치명 2] `{archetype}` 자동 치환 불가 + 사용자 성향 선택 강요 → 수용

**수용**. 두 문제 모두 해결한다.

**문제 A 해결**: `{archetype}`을 포함한 자리표시자 표기를 제거한다. 대신 SKILL.md에서 Claude가 직접 실행하는 서술형 지시로 대체한다. Task prompt는 실행 시점에 이미 실제 값이 채워진 문자열로 구성된다.

**문제 B 해결**: 성향(archetype)을 사용자 입력 항목에서 제거한다. Claude가 `purpose`와 `responsibilities`를 바탕으로 내부적으로 성향을 추론한다. 추론 결과는 사용자에게 보여줄 수 있지만 사용자가 직접 고를 필요가 없다.

**성향 추론 규칙** (SKILL.md에 내장):

| 목적 키워드 | 추론 성향 |
|------------|----------|
| 검토, 리뷰, 품질, 코드 리뷰 | reviewer |
| 수정, 버그, 디버그, 고치 | fixer |
| 실행, 배포, 테스트 실행, 빌드 | executor |
| 문서, 작성, README, 가이드 | writer |
| 분석, 인사이트, 탐색, 패턴 | analyst |
| 감사, 보안, 취약점, 위험 | auditor |
| 분류 불가 | executor (기본값) |

---

### [치명 3] templates/ + examples.md 공존은 중복 → 수용, 옵션 2 선택

**수용**. 치명 1 해결(옵션 C)로 templates/ 자체가 불필요해졌다.
따라서 `references/examples.md`는 유지하고, templates/ 디렉토리는 생성하지 않는다.

단, `references/examples.md`의 역할을 재정의한다: "Task 에이전트가 실제 파일 내용 작성 시 참조할 완성된 에이전트 예시". Task prompt에서 examples.md를 명시적으로 Read하도록 지시한다 (치명 1 해결 방식 = Task 에이전트에게 Read 호출을 명시 지시).

이는 치명 1 해결과 일관성이 있다: 템플릿은 SKILL.md에 직접 내장하되, examples.md는 Task 에이전트가 `Read` 도구로 직접 읽도록 지시한다.

---

### [경고 1] archetype을 내부 추론 결과로 처리 → 치명 2B와 통합 수용

---

### [경고 2] "최소 읽기 권한" 용어 불명확 → 수용

"읽기 전용(Read-Only)"으로 통일한다.

---

### [경고 3] Reviewer / Auditor 구분 불명확 → 부분 수용

두 성향을 유지하되 핵심 구분자를 명확히 한다:
- Reviewer: 코드/문서 품질 검토, 수정 제안 생성, `permissionMode: default`
- Auditor: 보안/위험 패턴 탐색, 실행 없이 계획만 제안, `permissionMode: plan` 강제

`permissionMode: plan`이 단순한 옵션이 아니라 Auditor의 **정체성**임을 SKILL.md 성향 표에서 명확히 한다.

---

### [경고 4] Step 3 검증 제거 미명시 → 수용 (문서화 완결성 반영)

---

### [제안 1] References 섹션 모순 설명 → 수용

References 섹션에서 "메인 컨텍스트 자동 포함" 문구를 제거한다. Task 에이전트가 직접 Read 호출해야 함을 명시한다.

---

### [제안 2] 브래킷 자리표시자가 과도함 → 수용

성향 고유 구조(Reviewer의 3단계 피드백, Auditor의 심각도 보고 형식 등)를 SKILL.md prompt 내에서 실제 마크다운 형식으로 사전 작성한다. 도메인 특화 부분만 `[브래킷]`으로 남긴다.

---

### [제안 3] 모델 기본값 선택 근거 불명확 → 수용

SKILL.md 성향 표에 모델 기본값과 선택 근거를 주석으로 포함한다.

---

## 핵심 설계 변경

### Round 1 → Round 3 변경 요약

| 항목 | Round 1 | Round 3 |
|------|---------|---------|
| 성향 템플릿 위치 | `assets/templates/{archetype}.md` (6개 파일) | SKILL.md Task prompt 내 직접 내장 |
| archetype 수집 | Step 1 사용자 입력 항목 | Claude 내부 추론 (사용자 입력 항목 제거) |
| `{archetype}` 변수 | 자동 치환 기대 (동작 안 함) | 추론 후 실제 값으로 서술형 지시 |
| examples.md | 유지 (역할 재정의) | 유지 + Task 에이전트가 Read로 직접 로드 |
| templates/ 디렉토리 | 신규 생성 (6개 파일) | 생성하지 않음 |
| 성향 구조 내장 위치 | 외부 파일 참조 | SKILL.md 내 성향별 프롬프트 블록 |
| Step 3 검증 제거 | 제거했으나 변경 표에 미명시 | 변경 표에 명시 |
| available-tools.md 명칭 | "최소 읽기 권한" (모호) | "읽기 전용(Read-Only)"으로 수정 |

---

## 확정된 SKILL.md 전체 내용

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

## 성향(Archetype) 분류표

사용자가 명시하지 않아도 Claude가 목적(purpose)과 책임(responsibilities)에서 내부적으로 추론한다.

| 성향 | 목적 키워드 | tools | model | permissionMode | 핵심 패턴 |
|------|------------|-------|-------|---------------|----------|
| reviewer | 검토, 리뷰, 품질, 코드 리뷰 | Read, Grep, Glob, Bash | inherit | default | 체크리스트 → 심각/경고/제안 3단계 |
| fixer | 수정, 버그, 디버그, 고치 | Read, Edit, Write, Grep, Glob, Bash | inherit | default | 원인 추적 → 최소 변경 수정 |
| executor | 실행, 배포, 테스트, 빌드 | (생략 — 전체 상속) | inherit | default | 실행 → 검증 루프 |
| writer | 문서, 작성, README, 가이드 | Read, Write, Edit, Glob, Grep | haiku (비용 절감) | default | 독자 정의 → 구조화 문서 |
| analyst | 분석, 인사이트, 탐색, 패턴 | Read, Grep, Glob, Bash | sonnet (추론 필요) | default | 가설 수립 → 데이터 검증 → 인사이트 |
| auditor | 감사, 보안, 취약점, 위험 | Read, Grep, Glob, Bash | sonnet (추론 필요) | **plan** (실행 차단) | 패턴 스캔 → 위험도 분류 |

분류 불가 시 기본값: `executor`

## 생성 워크플로우

1. **요구사항 수집**
   누락된 정보가 있을 때만 AskUserQuestion을 실행합니다:
   - 목적 (purpose): 어떤 작업을 위한 에이전트인가
   - 이름 (name), 범위 (scope): project `.claude/agents/` 또는 user `~/.claude/agents/`
   - 주요 책임 (responsibilities)
   - 도구 (tools), 모델 (model) — 선택, 위 성향표 기본값 사용 가능

   사용자가 이미 충분한 정보를 제공한 경우 이 단계를 건너뜁니다.
   성향(archetype)은 사용자에게 묻지 않습니다. Claude가 purpose/responsibilities에서 추론합니다.

2. **성향 추론**
   수집된 purpose/responsibilities에서 위 분류표 기준으로 성향을 결정합니다.
   필요 시 사용자에게 "XXX 성향으로 생성하겠습니다" 한 줄 안내 후 진행합니다.

3. **Task 위임**
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "서브 에이전트 파일 생성: {실제 에이전트 이름}"
     prompt: """
     다음 요구사항에 맞는 서브 에이전트 파일을 생성하세요.

     [요구사항]
     - Name: {실제 name}
     - Purpose: {실제 purpose}
     - Scope: {실제 scope} (project: .claude/agents/ / user: ~/.claude/agents/)
     - Tools: {실제 tools 또는 성향 기본값}
     - Model: {실제 model 또는 성향 기본값}
     - PermissionMode: {실제 permissionMode 또는 성향 기본값}
     - Responsibilities: {실제 responsibilities}
     - Archetype: {실제 추론된 성향}

     [성향별 구조 가이드]

     **reviewer** — 체크리스트 + 3단계 피드백
     ```
     당신은 [도메인] 품질 기준을 지키는 [역할명] 리뷰어입니다.

     호출 시 수행할 작업:
     1. [검토 대상 식별 — 예: git diff, 특정 경로]
     2. [검토 범위 확인]
     3. 즉시 리뷰 시작

     체크리스트:
     - [항목 1]
     - [항목 2]

     피드백 우선순위:
     🔴 심각 (반드시 수정): [항목과 구체적 수정 방법]
     🟡 경고 (수정 권장): [항목과 개선 방향]
     🟢 제안 (고려 사항): [항목과 이유]
     ```

     **fixer** — 근본 원인 추적 + 최소 변경
     ```
     당신은 [도메인] 근본 원인 분석을 전문으로 하는 [역할명]입니다.

     호출 시 수행할 작업:
     1. [문제 증상 및 컨텍스트 수집]
     2. [재현 단계 또는 실패 위치 파악]
     3. [원인 격리]
     4. 최소한의 변경으로 수정
     5. 수정 결과 검증

     반환 형식 (항목별):
     - 근본 원인: [설명]
     - 진단 근거: [증거]
     - 수정 내용: [코드 변경 요약]
     - 검증 방법: [확인 절차]
     - 재발 방지: [권고사항]
     ```

     **executor** — 실행-검증 루프
     ```
     당신은 [도메인] 작업 실행과 검증을 전담하는 [역할명]입니다.

     호출 시 수행할 작업:
     1. [작업 대상 및 범위 파악]
     2. [사전 조건 확인]
     3. [작업 실행]
     4. 각 단계 완료 후 결과 검증
     5. 이상 시 롤백 또는 재시도

     실행 결과 보고:
     - 수행한 작업 목록
     - 각 단계 성공/실패 여부
     - 최종 상태 요약
     - 잔여 작업 또는 후속 조치
     ```

     **writer** — 독자 정의 + 구조화 문서
     ```
     당신은 [도메인] 문서화를 전문으로 하는 [역할명]입니다.

     호출 시 수행할 작업:
     1. [문서화 대상 파악]
     2. 대상 독자 확인: [독자 유형]
     3. [문서 구조 결정 — H2 섹션, 코드 블록, 표]
     4. 명확하고 구조적인 문서 작성

     작성 원칙:
     - 코드 예시는 실행 가능한 형태로 포함
     - 일관된 용어 사용
     - [스타일 원칙]
     ```

     **analyst** — 가설-검증 + 인사이트 도출
     ```
     당신은 [도메인] 데이터와 패턴 분석을 전문으로 하는 [역할명]입니다.

     호출 시 수행할 작업:
     1. [분석 목적 및 질문 명확화]
     2. [데이터 또는 대상 수집]
     3. 가설 수립
     4. 데이터 검증으로 가설 테스트
     5. 인사이트 및 권고사항 도출

     분석 결과 보고:
     - 주요 발견사항 (우선순위 순)
     - 발견을 뒷받침하는 근거
     - 실행 가능한 권고사항
     - 추가 조사가 필요한 영역
     ```

     **auditor** — plan 모드 + 위험도 분류
     ```
     당신은 [도메인] 감사를 전문으로 하는 [역할명]입니다.

     NOTE: permissionMode: plan — 실행 없이 계획만 제안합니다. 실제 수정은 사용자 승인 후 수행됩니다.

     호출 시 수행할 작업:
     1. [감사 범위 정의]
     2. 알려진 취약점/위험 패턴 스캔
     3. [핵심 영역] 검토
     4. 발견 사항 위험도 분류
     5. 감사 보고서 작성

     보고서 형식 (발견 항목별):
     - 심각도: 심각 / 높음 / 중간 / 낮음
     - 위치: [파일 경로 또는 컴포넌트]
     - 설명: [취약점/위험 설명]
     - 권장 수정 방법
     - 참고자료: [CWE, OWASP 등 해당 시]
     ```

     [예시 참조]
     완성된 에이전트 예시를 참고하려면 다음 파일을 Read 도구로 읽으세요:
     - 절대 경로: {skill_root}/references/examples.md

     [파일 생성 규칙]
     - frontmatter에 name, description, tools(executor 제외), model, permissionMode(default 제외) 포함
     - description은 "이 에이전트를 사용할 시점"을 명확히 기술. 자동 위임이 필요하면 "use proactively" 또는 "PROACTIVELY 사용하세요" 포함
     - 시스템 프롬프트는 위 성향 구조를 따르되 실제 도메인에 맞게 구체화

     RETURN FORMAT (엄수):
     - 생성한 파일의 절대 경로
     - 상태 한 줄 요약
     - 핵심 설정 (tools, model, permissionMode) 한 줄
     - 전체 파일 내용 반환 금지
     """

## References

- `references/examples.md` — 완성된 서브 에이전트 예시 (Task 에이전트가 Read로 직접 로드)
- `references/available-tools.md` — 도구 조합 가이드 (Task 에이전트가 필요 시 Read로 직접 로드)
```

---

## 확정된 파일 구조

```
.claude/skills/subagent-creator/
├── SKILL.md                     # 핵심 변경: 성향 구조 직접 내장, archetype 추론 로직 포함
└── references/
    ├── examples.md              # 유지: 완성된 에이전트 예시 (Task가 Read로 직접 로드)
    └── available-tools.md       # 유지: 도구 조합 가이드 ("읽기 전용" 용어 수정)

삭제:
└── assets/subagent-template.md  # 제거: SKILL.md에 성향별 구조로 흡수
```

**총 파일 수**: 3개 (SKILL.md + 2개 references)
Round 1 제안(SKILL.md + 6개 templates + 2개 references = 9개) 대비 67% 감소.

---

## 성향별 템플릿 처리 최종 결정

### 결정: 옵션 C — SKILL.md Task prompt에 성향별 구조 직접 내장

**방법**:
1. 6개 성향 각각의 시스템 프롬프트 구조(헤더, 섹션 순서, 출력 형식)를 SKILL.md의 Task prompt 블록 내에 마크다운 코드 블록으로 직접 작성한다.
2. 도메인 특화 부분(체크리스트 항목, 도메인명)만 `[브래킷]`으로 남기고, 성향 고유 구조(Reviewer의 3단계 피드백, Auditor의 심각도 보고 형식)는 실제 마크다운 형식으로 사전 작성한다.
3. Task 에이전트는 추론된 성향에 해당하는 블록을 보고 실제 도메인 값으로 채워 파일을 생성한다.

**근거**:
- **신뢰성**: Task 에이전트 컨텍스트에 성향 구조가 100% 전달 보장. 파일 참조 실패 가능성 없음.
- **단순성**: 추가 파일 없음. 스킬 디렉토리가 3개 파일로 유지됨.
- **유지보수**: 성향 수정 시 SKILL.md 한 곳만 변경하면 됨.
- **SKILL.md 크기**: 6개 성향 구조를 내장해도 300-400줄 수준으로 관리 가능.

**포기한 것**:
- 성향 파일의 독립적 편집 가능성 (현실에서 성향 구조는 자주 바뀌지 않음)
- templates/ 디렉토리의 명시적 가독성 (SKILL.md 내 섹션 구분으로 대체)

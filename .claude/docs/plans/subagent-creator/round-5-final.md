# Round 5 — 최종 설계 확정

> 작성일: 2026-04-04
> 목적: 4라운드 블루팀/레드팀 토론 결과를 종합해 실제 파일에 적용할 최종 설계 확정

---

## 토론 결과 요약

### 4라운드 핵심 의사결정

| 라운드 | 팀 | 핵심 결정 |
|--------|-----|-----------|
| Round 1 | 블루 | 성향(archetype) 6종 도입, 성향별 템플릿 파일 6개 생성, examples.md 역할 재정의 |
| Round 2 | 레드 | Task 상대경로 참조 불가(치명 1), `{archetype}` 자동 치환 없음(치명 2), templates+examples 이중 소스 중복(치명 3) |
| Round 3 | 블루 | 옵션 C 채택: 성향 구조 SKILL.md 직접 내장, templates/ 제거, archetype을 내부 추론으로 전환, examples.md 유지(Task가 Read로 로드) |
| Round 4 | 레드 | 치명 1·2B 해결 확인, `{skill_root}` 자리표시자 재등장(신규 치명), 복합 성향 우선순위 규칙 누락 지적, 조건부 합격 판정 |

### Round 4 조건부 합격 조건 해결

**조건 1: `{skill_root}` 자리표시자 완전 제거**
- 결정: examples.md 절대 경로를 하드코딩으로 명시하되, 프로젝트/사용자 두 위치를 함께 제공
- 근거: 스킬 위치는 `~/.claude/skills/subagent-creator/`(사용자) 또는 프로젝트 내 경로로 고정. 자리표시자 대신 실제 경로를 직접 기술한다.
- 구현: Task prompt 내 examples.md 참조를 "다음 중 해당하는 절대 경로를 Read 도구로 읽으세요" 형태로 변경, 경로를 직접 명시

**조건 2: 복합 성향 우선순위 규칙 명시**
- 결정: "review + fix" 같은 복합 목적은 `더 제한적인 권한을 가진 성향 우선` 규칙 적용
- 우선순위: `auditor > reviewer > analyst > fixer > writer > executor`
- 근거: 보안 위험이 높은 성향(auditor, reviewer)이 실행 성향(executor)보다 안전. 분류 불가 기본값은 `writer`(읽기+쓰기 제한)로 변경 — executor의 전체 권한 기본 부여는 최소 권한 원칙 위반

---

## 최종 파일별 내용

### SKILL.md 최종본

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
성향을 사용자에게 직접 선택하게 하지 않는다.

| 성향 | 목적 키워드 | tools | model | permissionMode | 핵심 패턴 |
|------|------------|-------|-------|---------------|----------|
| reviewer | 검토, 리뷰, 품질, 코드 리뷰, review, inspect | Read, Grep, Glob, Bash | inherit | default | 체크리스트 → 심각/경고/제안 3단계 |
| fixer | 수정, 버그, 디버그, 고치, fix, debug, repair | Read, Edit, Write, Grep, Glob, Bash | inherit | default | 원인 추적 → 최소 변경 수정 |
| executor | 실행, 배포, 테스트, 빌드, run, deploy, build | (생략 — 전체 상속, 실행 작업은 도구 범위 사전 한정 불가) | inherit | default | 실행 → 검증 루프 |
| writer | 문서, 작성, README, 가이드, write, document | Read, Write, Edit, Glob, Grep | haiku (비용 절감) | default | 독자 정의 → 구조화 문서 |
| analyst | 분석, 인사이트, 탐색, 패턴, analyze, explore | Read, Grep, Glob, Bash | sonnet (추론 필요) | default | 가설 수립 → 데이터 검증 → 인사이트 |
| auditor | 감사, 보안, 취약점, 위험, audit, security, vulnerability | Read, Grep, Glob, Bash | sonnet (추론 필요) | **plan** (실행 차단) | 패턴 스캔 → 위험도 분류 |

**복합 성향 우선순위**: 키워드가 복수 성향에 동시 해당하는 경우 더 제한적인 성향 선택.
우선순위: `auditor > reviewer > analyst > fixer > writer > executor`

예시:
- "코드를 리뷰하고 버그도 수정해줘" → reviewer (executor보다 제한적)
- "보안 취약점을 찾아서 수정해줘" → auditor (fixer보다 제한적)
- "로그를 분석하고 문서화해줘" → analyst (writer보다 제한적)

분류 불가 시 기본값: `writer` (최소 권한 원칙 적용 — executor 전체 권한보다 안전)

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
   복합 목적 시 우선순위 규칙을 적용합니다.
   필요 시 사용자에게 "XXX 성향으로 생성하겠습니다" 한 줄 안내 후 진행합니다.

3. **Task 위임**
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "서브 에이전트 파일 생성: [실제 에이전트 이름]"
     prompt: 아래 전체 내용을 실제 값으로 채워서 전달

   ---
   다음 요구사항에 맞는 서브 에이전트 파일을 생성하세요.

   [요구사항]
   - Name: [실제 name]
   - Purpose: [실제 purpose]
   - Scope: [실제 scope] (project: .claude/agents/ / user: ~/.claude/agents/)
   - Tools: [실제 tools 또는 성향 기본값]
   - Model: [실제 model 또는 성향 기본값]
   - PermissionMode: [실제 permissionMode 또는 성향 기본값]
   - Responsibilities: [실제 responsibilities]
   - Archetype: [실제 추론된 성향]

   [성향별 구조 가이드]
   추론된 성향에 해당하는 블록을 참조하여 실제 도메인에 맞게 구체화하세요.

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

   우선순위별 피드백:
   심각 (반드시 수정): [항목과 구체적 수정 방법]
   경고 (수정 권장): [항목과 개선 방향]
   제안 (고려 사항): [항목과 이유]
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

   [예시 참조 — 필요 시]
   완성된 에이전트 예시가 필요하면 다음 절대 경로 중 존재하는 파일을 Read 도구로 읽으세요:
   - 사용자 스킬: ~/.claude/skills/subagent-creator/references/examples.md
   - 프로젝트 스킬: .claude/skills/subagent-creator/references/examples.md (프로젝트 루트 기준 절대 경로 사용)

   [파일 생성 규칙]
   - frontmatter에 name, description, tools(executor 제외), model, permissionMode(default 제외) 포함
   - description은 "이 에이전트를 사용할 시점"을 명확히 기술. 자동 위임이 필요하면 "use proactively" 또는 "PROACTIVELY 사용하세요" 포함
   - 시스템 프롬프트는 위 성향 구조를 따르되 실제 도메인에 맞게 구체화

   RETURN FORMAT (엄수):
   - 생성한 파일의 절대 경로
   - 상태 한 줄 요약
   - 핵심 설정 (tools, model, permissionMode) 한 줄
   - 전체 파일 내용 반환 금지
   ---

## References

- `references/examples.md` — 완성된 서브 에이전트 예시 (Task 에이전트가 필요 시 Read 도구로 직접 로드)
- `references/available-tools.md` — 도구 조합 가이드 (Task 에이전트가 필요 시 Read 도구로 직접 로드)
```

---

### assets/subagent-template.md 최종본

**결정: 유지하되 성향별 안내 섹션 추가**

Round 3는 `assets/subagent-template.md`를 제거하기로 했으나, 최종 파일 구조(임무 명세)에 `assets/subagent-template.md`가 포함되어 있으므로 유지한다. 단, 이 파일은 SKILL.md Task prompt에 직접 참조되지 않으며, 사람(사용자/개발자)이 수동으로 에이전트를 작성할 때의 빠른 참조용으로 역할을 재정의한다.

```markdown
---
name: 에이전트-이름
description: 이 에이전트를 사용할 시점. 자동 위임을 원하면 "use proactively" 또는 "PROACTIVELY 사용하세요"를 포함하세요.
tools: Read, Grep, Glob, Bash
model: inherit
permissionMode: default
---

당신은 [역할/전문 분야] 전문가입니다.

호출 시 수행할 작업:

1. [첫 번째 수행할 작업]
2. [두 번째 작업]
3. [세 번째 작업]

주요 책임:

- [책임 1]
- [책임 2]
- [책임 3]

가이드라인:

- [가이드라인 1]
- [가이드라인 2]

출력 형식:

- [응답에 포함할 내용]
- [결과 구조화 방법]

---

## 성향별 빠른 참조

### reviewer — 체크리스트 + 3단계 피드백
- tools: Read, Grep, Glob, Bash | model: inherit
- 출력: 심각(반드시 수정) / 경고(수정 권장) / 제안(고려 사항)

### fixer — 근본 원인 추적 + 최소 변경
- tools: Read, Edit, Write, Grep, Glob, Bash | model: inherit
- 출력: 근본원인 / 진단근거 / 수정내용 / 검증방법 / 재발방지

### executor — 실행-검증 루프
- tools: 생략(전체 상속) | model: inherit
- 출력: 수행작업목록 / 성공실패여부 / 최종상태 / 후속조치

### writer — 독자 정의 + 구조화 문서
- tools: Read, Write, Edit, Glob, Grep | model: haiku
- 출력: 마크다운 구조 문서 (H2, 코드블록, 표)

### analyst — 가설-검증 + 인사이트 도출
- tools: Read, Grep, Glob, Bash | model: sonnet
- 출력: 발견사항 / 근거 / 권고사항 / 추가조사영역

### auditor — plan 모드 + 위험도 분류
- tools: Read, Grep, Glob, Bash | model: sonnet | permissionMode: plan
- 출력: 심각도(심각/높음/중간/낮음) / 위치 / 설명 / 권장수정 / 참고자료(CWE/OWASP)
```

---

### references/available-tools.md 최종본

```markdown
# 서브 에이전트 도구 조합 가이드

서브 에이전트는 `tools` 필드로 권한을 제한합니다. 생략 시 메인 스레드의 모든 도구를 상속합니다.

## 자주 사용하는 도구 조합

### 읽기 전용 조사
```
tools: Read, Grep, Glob, Bash
```
용도: 코드 분석, 문서 검토, 코드베이스 탐색 (reviewer, auditor, analyst)

### 코드 수정
```
tools: Read, Write, Edit, Grep, Glob, Bash
```
용도: 기능 구현, 버그 수정, 리팩터링 (fixer, writer)

### 읽기 전용(Read-Only)
```
tools: Read, Grep, Glob
```
용도: 보안 감사, 보고 전용 검토 — Bash 없이 실행 차단 강화 (auditor 강화)

### 전체 접근
`tools` 필드를 생략하면 사용 가능한 모든 도구를 상속합니다.
용도: 실행 작업 전반 (executor) — 도구 범위를 사전에 한정하기 어려운 경우 사용

## IDE 도구 (사용 가능한 경우)

| 도구 | 설명 |
|------|------|
| `mcp__ide__getDiagnostics` | VS Code 언어 진단 정보 (타입 오류, 린트 등) |
| `mcp__ide__executeCode` | Jupyter 커널에서 코드 실행 (analyst 활용) |

## MCP 도구

MCP 도구는 `tools` 필드에 직접 명시해야 합니다. 형식: `mcp__<서버>__<도구>`

예시:
```yaml
tools: Read, Grep, mcp__ide__getDiagnostics, mcp__sequential-thinking__sequentialthinking
```
```

---

### references/examples.md 변경사항

**변경 내용: data-scientist 섹션 제거, 나머지 5개 유지**

제거 항목:
- `## 데이터 사이언티스트` 블록 전체 (lines 79-115) — BigQuery 특화로 일반 패턴 참조에 부적합

유지 항목 (5개):
- `## 코드 리뷰어` — reviewer 성향 완성 예시
- `## 디버거` — fixer 성향 완성 예시
- `## 테스트 러너` — executor 성향 완성 예시
- `## 문서 작성자` — writer 성향 완성 예시
- `## 보안 감사자` — auditor 성향 완성 예시

analyst 성향 완성 예시는 이번 범위에서 추가하지 않는다 (Round 1 블루팀 제안이었으나 토론에서 확정되지 않음).

---

## 적용 체크리스트

실제 파일 수정 전 확인할 항목:

### SKILL.md
- [ ] frontmatter 유지 (name, description 동일)
- [ ] `## 서브 에이전트 파일 형식` — frontmatter 코드 블록 제거, 필드 명세 링크만 유지
- [ ] `## 성향(Archetype) 분류표` 신규 추가 — 6종 표 + 복합 우선순위 규칙 + 분류 불가 기본값(`writer`)
- [ ] `## 생성 워크플로우` Step 1 — 조건부 AskUserQuestion (누락 정보만 질문), archetype 항목 제거
- [ ] `## 생성 워크플로우` Step 2 — 성향 추론 단계 신규 추가
- [ ] `## 생성 워크플로우` Step 3 — Task prompt 내 6개 성향 구조 직접 내장
- [ ] Task prompt 내 `{skill_root}` 자리표시자 없음 확인 — 절대 경로 두 가지 직접 명시
- [ ] `## Task 반환 형식` 섹션 제거 — RETURN FORMAT을 Task prompt 내 인라인 삽입
- [ ] `## 효과적인 서브 에이전트 작성법` 섹션 제거 (중복)
- [ ] `## 도구 선택 기준` 섹션 제거 (available-tools.md와 중복)
- [ ] `## References` 섹션 주의사항 수정 — "메인 컨텍스트에서 직접 로드하지 마세요" 삭제, "Task 에이전트가 필요 시 Read로 직접 로드" 명시

### assets/subagent-template.md
- [ ] 기존 내용 유지 (일반 skeleton 템플릿)
- [ ] `## 성향별 빠른 참조` 섹션 추가 — 6종 성향 요약

### references/available-tools.md
- [ ] `## 핵심 도구` 표 제거 (Claude가 이미 아는 도구)
- [ ] `## 상호작용 도구` 제거
- [ ] `## 웹 도구` 제거
- [ ] `## 자주 사용하는 도구 조합` 유지 + 성향 매핑 주석 추가
- [ ] "최소 쓰기 권한" → "읽기 전용(Read-Only)"으로 명칭 변경
- [ ] `## IDE 도구` 유지
- [ ] `## MCP 도구` 유지 — 기존 MCP 형식 설명 유지

### references/examples.md
- [ ] `## 데이터 사이언티스트` 섹션 제거
- [ ] 나머지 5개 예시 (코드 리뷰어, 디버거, 테스트 러너, 문서 작성자, 보안 감사자) 원문 유지
- [ ] 파일 상단에 "Task 에이전트가 Read 도구로 직접 로드하는 완성 예시 모음" 설명 한 줄 추가

### 삭제 대상 없음
- assets/subagent-template.md는 사람용 빠른 참조로 역할 재정의하여 유지
- templates/ 디렉토리는 생성하지 않음 (Round 3 확정)

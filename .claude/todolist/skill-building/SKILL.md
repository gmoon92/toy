---
name: skill-builder
description: Claude Code 스킬 생성, 수정, 리팩토링을 지원합니다. 스킬 개발을 위한 템플릿, 가이드, 모범 사례를 제공합니다.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, Bash, AskUserQuestion, Task
---

# Skill Builder (스킬 빌더)

고품질 Claude Code 스킬을 생성하고 유지 관리하는 것을 도와줍니다.

## 핵심 기능 (Core Functions)

- **생성 (Create)** - 템플릿에서 새 스킬 만들기
- **리팩토링 (Refactor)** - 기존 스킬의 성능 향상을 위한 개선
- **추출 (Extract)** - 인라인 코드를 실행 가능한 스크립트로 추출
- **검증 (Validate)** - 스킬 구조와 규칙 검증

---

## 실행 워크플로우 (Execution Workflows)

### 워크플로우 1: 새 스킬 생성

**트리거:** 사용자가 "[목적]을 위한 스킬을 생성해줘"라고 말할 때

**단계:**

1. **요구사항 수집**
   ```
   EXECUTE AskUserQuestion:
   - 스킬 목적
   - 핵심 기능 (3-5개)
   - 실행 스타일 (자율성 수준)
   ```

2. **Task에 위임**
   ```
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "스킬 구조 생성"
     prompt: """
     참고: workflows/creating-skills.md
     참고: templates/skill-template.md
     참고: skills/reference/quick-guide.md

     반환 형식: 아래 "Task 반환 형식" 섹션 참조
     """
   ```

3. **결과 보고**
   - 상태 + 파일 경로
   - 사용자 필요 시 파일 읽기: `Read .claude/skills/{name}/SKILL.md`

---

### 워크플로우 2: 기존 스킬 리팩토링

**트리거:** 사용자가 "[skill-name]을 리팩토링해줘" 또는 "[skill-name]을 더 간결하게 만들어줘"라고 말할 때

**단계:**

1. **현재 상태 분석**
   ```
   EXECUTE: Read {skill-name}/SKILL.md
   EXECUTE: Bash - wc -l {skill-name}/**/*.md
   EXECUTE: Grep - pattern "```(bash|javascript|python)"
   ```

2. **Task에 위임**
   ```
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "효율성을 위한 리팩토링"
     prompt: """
     현재 상태: {metrics}

     참고: workflows/modifying-skills.md (토큰 최적화)
     참고: workflows/script-extraction.md (인라인 코드가 있는 경우)
     참고: prompting/principles/01-general-principles.md (지시문)

     반환 형식: 아래 "Task 반환 형식" 섹션 참조
     """
   ```

3. **결과 보고**
   - 개선 사항 + 지표
   - 사용자 검토: `Read {skill-name}/SKILL.md.new`

---

### 워크플로우 3: 스크립트 추출

**트리거:** 사용자가 "[skill-name]에서 스크립트를 추출해줘"라고 말할 때

**단계:**

1. **인라인 코드 식별**
   ```
   EXECUTE: Grep - pattern "^```(bash|javascript|python)"
   ```

2. **Task에 위임**
   ```
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "코드를 스크립트로 추출"
     prompt: """
     참고: workflows/script-extraction.md (전체 프로세스)

     반환 형식: 아래 "Task 반환 형식" 섹션 참조
     """
   ```

3. **결과 보고**
   - 생성된 스크립트 + 토큰 절약량
   - 사용자 검토: `Read {skill-name}/scripts/`

---

## Task 반환 형식 (Task Return Format)

**중요:** Task 에이전트는 메타데이터만 반환해야 합니다.

**반환 항목:**
- ✅ 파일 경로
- ✅ 상태 요약 (최대 10줄)
- ✅ 지표/카운트
- ✅ 간단한 요약 (2-3줄)

**절대 반환하지 마세요:**
- ❌ 전체 파일 내용
- ❌ 큰 출력물
- ❌ 완전한 diff

**이유:** 파일 경로(10토큰) vs 전체 내용(500+토큰) = 98% 절약

**자세한 내용:** `skills/principles/01-conciseness.md` 참조

---

## 핵심 원칙 (Core Principles)

1. **복잡한 작업 위임** - 비용이 많이 드는 작업에는 Task 사용
2. **점진적 로딩** - 참조는 Task 컨텍스트에서만 로드
3. **강력한 지시문** - MANDATORY, EXECUTE, DO NOT 키워드 사용
4. **최소 반환** - Task는 메타데이터만 반환
5. **항상 검증** - 변경 후 체크리스트 실행

**전체 원칙:** `prompting/principles/` 및 `skills/principles/` 참조

---

## 참조 (References)

### 워크플로우
- `workflows/creating-skills.md` - 단계별 생성 프로세스
- `workflows/modifying-skills.md` - 리팩토링 패턴 및 토큰 최적화
- `workflows/script-extraction.md` - 스크립트로 코드 추출

### 템플릿
- `templates/skill-template.md` - SKILL.md 보일러플레이트 구조

### 원칙 및 지침
- `skills/principles/01-conciseness.md` - 토큰 효율성
- `skills/principles/02-appropriate-freedom.md` - 특이성 수준
- `prompting/principles/01-general-principles.md` - 강력한 지시문
- `prompting/guidelines/` - 도구 사용, 형식 제어 등

### 검증
- `skills/reference/checklist.md` - 품질 검증 체크리스트
- `skills/reference/quick-guide.md` - 빠른 패턴 및 템플릿

**참고:** 이들을 메인 컨텍스트에 로드하지 마세요. Task 에이전트는 필요할 때 로드합니다.

---

## 사용 예시 (Usage Examples)

```
사용자: "로그 파일을 분석하는 스킬을 생성해줘"
→ 워크플로우 1 실행

사용자: "commit 스킬을 리팩토링해줘"
→ 워크플로우 2 실행

사용자: "my-skill에서 인라인 bash 스크립트를 추출해줘"
→ 워크플로우 3 실행
```

---

## 범위 (Scope)

**해야 할 것 (DO):**
- ✅ 복잡한 작업을 Task 에이전트에 위임
- ✅ 강력한 실행 지시문 사용 (MANDATORY, EXECUTE)
- ✅ Task는 메타데이터만 반환 (경로, 요약)
- ✅ Task 출력을 파일에 저장
- ✅ 필요시 사용자가 Read 도구로 파일 읽기
- ✅ 모든 출력 검증

**하지 말아야 할 것 (DON'T):**
- ❌ 메인 컨텍스트에 참조 로드
- ❌ 메인 컨텍스트에서 복잡한 분석 수행
- ❌ 약한 지시문 사용 ("you should", "consider")
- ❌ Task에서 전체 내용 반환
- ❌ 메인 컨텍스트에 큰 출력물 로드
- ❌ 검증 건너뛰기

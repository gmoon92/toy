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
|------|------------|-------|-------|----------------|----------|
| reviewer | 검토, 리뷰, 품질, 코드 리뷰, review, inspect | Read, Grep, Glob, Bash | inherit | default | 체크리스트 → 심각/경고/제안 3단계 |
| fixer | 수정, 버그, 디버그, 고치, fix, debug, repair | Read, Edit, Write, Grep, Glob, Bash | inherit | default | 원인 추적 → 최소 변경 수정 |
| executor | 실행, 배포, 테스트, 빌드, run, deploy, build | (생략 — 전체 상속) | inherit | default | 실행 → 검증 루프 |
| writer | 문서, 작성, README, 가이드, write, document | Read, Write, Edit, Glob, Grep | haiku | default | 독자 정의 → 구조화 문서 |
| analyst | 분석, 인사이트, 탐색, 패턴, analyze, explore | Read, Grep, Glob, Bash | sonnet | default | 가설 수립 → 데이터 검증 → 인사이트 |
| auditor | 감사, 보안, 취약점, 위험, audit, security, vulnerability | Read, Grep, Glob, Bash | sonnet | **plan** |패턴 스캔 → 위험도 분류 |

**복합 성향 우선순위**: 키워드가 복수 성향에 동시 해당하는 경우 더 제한적인 성향 선택.
우선순위: `auditor > reviewer > analyst > fixer > writer > executor`

예시:
- "코드를 리뷰하고 버그도 수정해줘" → reviewer (fixer보다 제한적)
- "보안 취약점을 찾아서 수정해줘" → auditor (fixer보다 제한적)
- "로그를 분석하고 문서화해줘" → analyst (writer보다 제한적)

분류 불가 시 기본값: `writer` (최소 권한 원칙 — executor 전체 권한보다 안전)

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
     description: "서브 에이전트 파일 생성: {실제 에이전트 이름}"
     prompt: 아래 내용을 실제 값으로 채워서 전달

   ```
   다음 요구사항에 맞는 서브 에이전트 파일을 생성하세요.

   [요구사항]
   - Name: {실제 name}
   - Purpose: {실제 purpose}
   - Scope: {실제 scope} (project: .claude/agents/ / user: ~/.claude/agents/)
   - Tools: {실제 tools 또는 성향 기본값}
   - Model: {실제 model 또는 성향 기본값}
   - PermissionMode: {실제 permissionMode 또는 성향 기본값}
   - Responsibilities: {실제 responsibilities}
   - Archetype: {추론된 성향}

   [성향별 구조 가이드]
   추론된 성향에 해당하는 블록을 참조하여 실제 도메인에 맞게 구체화하세요.

   reviewer — 체크리스트 + 3단계 피드백
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

   fixer — 근본 원인 추적 + 최소 변경
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

   executor — 실행-검증 루프
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

   writer — 독자 정의 + 구조화 문서
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

   analyst — 가설-검증 + 인사이트 도출
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

   auditor — plan 모드 + 위험도 분류
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

   [예시 참조 — 필요 시]
   완성된 에이전트 예시가 필요하면 다음 경로 중 존재하는 파일을 Read 도구로 읽으세요:
   - ~/.claude/skills/subagent-creator/references/examples.md
   - {프로젝트루트}/.claude/skills/subagent-creator/references/examples.md

   [파일 생성 규칙]
   - frontmatter: name, description, tools(executor 제외), model, permissionMode(default 제외) 포함
   - description은 "이 에이전트를 사용할 시점"을 명확히 기술. 자동 위임이 필요하면 "PROACTIVELY 사용하세요" 포함
   - 시스템 프롬프트는 위 성향 구조를 따르되 실제 도메인에 맞게 구체화

   RETURN FORMAT (엄수):
   - 생성한 파일의 절대 경로
   - 상태 한 줄 요약
   - 핵심 설정 (tools, model, permissionMode) 한 줄
   - 전체 파일 내용 반환 금지
   ```

## References

- `references/examples.md` — 완성된 서브 에이전트 예시 (Task 에이전트가 필요 시 Read 도구로 직접 로드)
- `references/available-tools.md` — 도구 조합 가이드 (Task 에이전트가 필요 시 Read 도구로 직접 로드)

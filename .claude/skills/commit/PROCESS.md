# Commit Skill Execution Process

This document describes the detailed step-by-step execution process for the commit skill.

## Overview

The commit skill follows a 5-step process to ensure quality commits that follow project conventions:

1. Pre-validation and context gathering (**Heavy analysis + write metadata**)
2. Change analysis and violation detection (**Read metadata**)
3. Commit message generation (**Read metadata**)
4. User approval (**Read metadata**)
5. Commit execution and verification (**Read + cleanup metadata**)

**Token Optimization:**
- Step 1 analyzes once and writes `.claude/temp/commit-{timestamp}.json`
- Steps 2-5 read from metadata (67% token savings)
- See [METADATA.md](METADATA.md) for details

**Important:** All policies (Tidy First, Logical Independence, User Communication, etc.) are defined in [SKILL.md Core Principles](SKILL.md#core-principles).

---

## Step 1: 사전 검증 및 컨텍스트 수집 (Heavy Analysis)

### 변경사항 수집 및 검증 (IDE-like behavior)

**Collect all changes:**

IDE 기본 동작과 동일하게, 명시적으로 스테이징하지 않은 수정된 파일(modified files)도 자동으로 커밋 대상에 포함합니다.

```bash
# Collect both staged and modified files
git diff --cached --name-only  # Staged files
git diff --name-only           # Modified files (unstaged)
```

**Auto-stage all modified files:**

```bash
# Stage all modified files automatically (IDE behavior)
git add -u
```

**Check if any changes exist:**

```bash
git diff --cached --stat
```

If no changes after staging → exit with message:
```
변경사항이 없습니다. 먼저 파일을 수정하세요.
```

**Note:**
- Untracked files (new files) are NOT automatically staged
- Only modified files are auto-staged for commit

**Additional checks:**
- main/master 브랜치에 있으면 경고 (브랜치 생성 권장)

### 변경 컨텍스트 수집 (병렬 실행)

After auto-staging all modified files:

- 변경된 파일 목록 가져오기 (`git diff --cached --name-only`)
- 변경 통계 가져오기 (`git diff --cached --stat`)
- 상세 diff 가져오기 (`git diff --cached`)
- 주요 모듈 또는 수정된 파일 식별

### Scope 결정

- 논리적 모듈의 변경사항인 경우 (예: `spring-batch`, `spring-security`), 모듈명 사용
- 단일 파일이거나 관련 파일인 경우, 주요 파일명 사용
- 관련 없는 여러 파일인 경우, 가장 중요한 파일 선택

### **메타데이터 파일 생성 (중요)**

분석 결과를 `.claude/temp/commit-execution-{executionId}.json`에 저장:

```bash
mkdir -p .claude/temp
EXECUTION_ID=$(date +%Y%m%d-%H%M%S)

cat > .claude/temp/commit-execution-${EXECUTION_ID}.json <<EOF
{
  "executionId": "${EXECUTION_ID}",
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "analysis": {
    "stagedFiles": [...],
    "groups": [...],
    "violations": {...}
  }
}
EOF
```

**중요:** executionId는 CLI 세션 ID가 아닌 /commit 실행 ID입니다.
- 같은 CLI 세션에서 여러 번 /commit 실행 가능
- 각 실행마다 새로운 executionId 생성

이후 모든 단계는 이 파일을 읽어서 사용 (토큰 절약)

효율성을 위해 병렬 bash 명령 사용

---

## Step 2: 변경사항 분석 및 위반 감지 (Read Metadata)

**메타데이터 읽기:**
```bash
# Read pre-analyzed data using executionId
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
```

### 커밋 타입 결정

| 변경 유형 | 타입 |
|----------|------|
| 새로운 기능 | feat |
| 버그/오류 수정 | fix |
| 메서드 추출, 이름 변경 (동작 변경 없음) | refactor |
| 테스트 코드 | test |
| 문서화 | docs |
| 코드 포맷팅만 | style |
| 빌드 설정, 의존성 | chore |

### Tidy First 위반 감지

구조적 변경(refactor)과 동작 변경(feat/fix)이 섞여 있을 경우:

**Template:** [ui/template-1-tidy-first.md](ui/template-1-tidy-first.md)

**Actions:**
- 어떤 파일이 refactor이고 어떤 파일이 feat/fix인지 명확히 설명
- 여러 커밋으로 분리할 것을 권장하는 이유 설명
- AskUserQuestion 도구를 사용하여 경고하고 질문 (template-1 참조)
- 리셋 선택 시: `git reset HEAD` 실행, 분리 방법 안내 후 종료
- 진행 선택 시: 지배적인 타입으로 계속 진행 (경고 메시지 표시)

### 논리적 독립성 검증 (중요)

같은 타입이더라도 논리적으로 독립적인 변경사항은 분리:

**분리가 필요한 경우:**
- 서로 다른 목적의 변경사항
- 독립적으로 리뷰 가능한 변경사항
- 서로 다른 컨텍스트의 파일들

**예시:**
```
❌ 한 커밋에 통합 (잘못됨):
docs(claude): Claude API 문서 및 커밋 스킬 추가
- .claude/skills/commit/ (커밋 스킬 문서)
- ai/docs/claude/ (API 문서 번역)
→ 커밋 스킬과 API 문서는 서로 다른 목적

✅ 분리된 커밋 (올바름):
Commit 1: docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가
Commit 2: docs(claude-api): Claude API 문서 번역 추가
```

**검증 절차:**
1. 변경된 파일들의 디렉토리 구조 분석
2. 논리적으로 독립적인 그룹 식별
3. 10개 이상 파일 또는 서로 다른 최상위 디렉토리면 경고

**Template:** [ui/template-2-logical-independence.md](ui/template-2-logical-independence.md)

**Actions:**
- 각 그룹을 명확히 설명 (디렉토리, 파일 수, 목적)
- 통합 커밋의 위험성을 간결하게 경고
- AskUserQuestion 도구를 사용하여 질문 (template-2 참조)

**User Actions:**
- "자동 분리" 선택 → **[AUTO_SPLIT.md](AUTO_SPLIT.md)** 참고 (자동 분리 커밋 프로세스)
- "통합 커밋" 선택 → 경고 메시지 표시, 재확인 요청, Step 3으로 진행
- "취소" 선택 → 프로세스 종료

---

## Step 3: 커밋 메시지 생성 (Read Metadata)

**메타데이터 읽기:**
```bash
# Read pre-generated messages
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
# Use groups[].suggestedMessages
```

### 형식

`<type>(module|filename): <간단한 설명>`

### 5개 메시지 생성 전략

각 그룹마다 **5개의 커밋 메시지**를 생성합니다.

1. **Message 1 (추천)**: 최적의 scope + 명확한 표현
2. **Message 2**: Scope 변형 (모듈명 ↔ 파일명)
3. **Message 3**: Message 표현 변형 (간결/상세/다른 관점)
4. **Message 4**: Body 상세도 조정 (추가/제거/변경)
5. **Message 5**: Type 대안 제시 (다른 타입으로 해석)

**상세한 생성 알고리즘:** [MESSAGE_GENERATION.md](MESSAGE_GENERATION.md) 참고

### Scope 선택

- 모듈명: `feat(spring-security-jwt): JWT 인증 필터 추가`
- 파일명: `fix(DateUtils.java): DST 미처리 문제 수정`
- 다중 파일 모듈: `refactor(spring-batch): 배치 재시도 로직 개선`

### 본문 추가 기준

- 5개 이상의 파일이 변경됨
- 100줄 이상 변경됨
- 복잡한 로직 변경

### 본문 형식

```
<type>(scope): <간단한 설명>

- 주요 변경사항 1
- 주요 변경사항 2
- 주요 변경사항 3
```

본문 작성 가이드는 [RULES.md](RULES.md#body-guidelines) 참고

### 메시지 제안 (AskUserQuestion 사용)

**Template:** [ui/template-3-message-selection.md](ui/template-3-message-selection.md)

**Requirements:**
1. **전체 메시지 표시**: 각 옵션의 description에 완전한 메시지 포함 (헤더 + 본문)
2. **추천 표시**: 첫 번째 메시지에 "(추천)" 표시
3. **Other 옵션**: 자동으로 추가됨 (사용자 직접 입력 가능)

**User Actions:**
- "메시지 1-4" 선택 → template-4 (최종 확인)으로 진행
- "Other" 선택 → template-5 (직접 입력)으로 진행

---

## Step 4: 사용자 승인 받기

### 4-1: 사용자 선택 처리

**Step 3에서 받은 선택:**
- 메시지 1-4 중 하나 선택 → 최종 확인으로 진행
- "Other" 선택 → 직접 입력 플로우로 진행

### 4-2: 직접 입력 플로우 (Other 선택 시)

**Template:** [ui/template-5-direct-input.md](ui/template-5-direct-input.md)

**Process:**
1. 입력 안내 표시
2. 사용자가 완전한 커밋 메시지 입력 (헤더 + 본문, 멀티라인 지원)
3. 형식 검증 (Type, Scope, Message, 공백 블록)
4. 검증 통과 → template-4 (최종 확인)으로 진행
5. 검증 실패 → 재입력 옵션 제공 (template-5 참조)

### 4-3: 최종 확인 (AskUserQuestion 사용)

**Template:** [ui/template-4-final-confirmation.md](ui/template-4-final-confirmation.md)

**CRITICAL: 사용자가 선택한 후 반드시 전체 메시지를 다시 표시**

**Why show full message again:**
- User may have only seen header in list view
- Final safety check before commit
- Clear visibility of complete message (header + body + footer)

**User Actions:**
- "승인" 선택 → Step 5 (커밋 실행)으로 진행
- "수정" 선택 → template-3 (메시지 선택)으로 돌아가기
- "취소" 선택 → 프로세스 종료

### 4-4: 수정 선택 시

**옵션:**
1. Step 3으로 돌아가기 (메시지 선택)
2. 직접 입력 (Other 선택과 동일)

→ 사용자 선택에 따라 Step 3 또는 4-2로 이동

---

## Step 5: 커밋 실행 및 검증 (Read + Cleanup)

### 커밋 실행

HEREDOC 형식을 사용하여 멀티라인 처리:

```bash
git commit -m "$(cat <<'EOF'
feat(spring-cloud-bus): 커스텀 이벤트 핸들러 구현

- RemoteApplicationEvent 처리 로직 추가
- 이벤트 발행 메커니즘 구현
- 리스너 등록 기능 추가
EOF
)"
```

### 커밋 후 즉시 검증

- 커밋 생성 확인: `git log -1 --oneline`
- 형식 검증: `<type>(scope): <message>` 패턴 일치 확인
- 사용자에게 결과 보고 (한글로):

```
✅ 커밋이 성공적으로 생성되었습니다!
커밋 해시: abc1234
메시지: feat(spring-cloud-bus): 커스텀 이벤트 핸들러 구현
```

### 커밋 실패 시

- 에러 메시지를 한글로 설명
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) 참고 안내
- 가능한 해결 방법 제시

### Git Hook 실패 처리

**실패 감지:**
- pre-commit hook이나 commit-msg hook 실패 감지
- 에러 메시지를 그대로(verbatim) 표시

**사용자에게 제공:**
```
❌ 커밋 실패

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
훅 에러 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[실제 에러 메시지 전체 내용]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

해결 방법:
1. [구체적인 수정 방법]
2. 수정 후 다시 커밋하려면:
   $ git add [수정한 파일]
   $ /commit
```

### 메타데이터 정리 (중요)

**성공 시:**
```bash
# 메타데이터 파일 삭제 (현재 실행의 파일만)
rm .claude/temp/commit-execution-${EXECUTION_ID}.json
```

**실패/취소 시:**
```bash
# 메타데이터 파일 삭제 (다음 /commit 실행 시 새로 생성)
rm .claude/temp/commit-execution-${EXECUTION_ID}.json
```

**자동 분리 커밋 시:**
- 모든 그룹 처리 완료 후 정리
- 중간 실패 시에도 최종 단계에서 정리

**중요:**
- 각 `/commit` 실행은 독립적
- 이전 실행의 메타데이터 파일과 무관
- 같은 CLI 세션에서 여러 번 `/commit` 가능

---

## Process Flow Diagram

```
Start
  ↓
┌─────────────────────────────────────┐
│ Step 1: 사전 검증 및 컨텍스트 수집   │
│ - 스테이징된 파일 확인               │
│ - 변경 컨텍스트 수집                 │
│ - Scope 결정                        │
└─────────────┬───────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Step 2: 변경사항 분석 및 위반 감지   │
│ - 커밋 타입 결정                     │
│ - Tidy First 검증                   │
│ - 논리적 독립성 검증                 │
└─────┬───────────────────────┬───────┘
      │                       │
      ├─ 위반 감지? ──┬─ Yes → 사용자 질문
      │               │        ├─ 자동 분리 → AUTO_SPLIT.md
      │               │        ├─ 통합 커밋 → 경고 후 Step 3
      │               │        └─ 취소 → End
      │               │
      └─ No ──────────┘
      ↓
┌─────────────────────────────────────┐
│ Step 3: 커밋 메시지 생성             │
│ - 형식에 맞춰 생성                   │
│ - 5개 제안 제공                      │
│ - 본문 추가 (필요시)                 │
└─────────────┬───────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Step 4: 사용자 승인 받기             │
│ - 메시지 표시                        │
│ - 승인/수정/취소 선택                │
└─────┬─────────┬─────────┬───────────┘
      │         │         │
      ├─ 승인  ─┤         └─ 취소 → End
      │         │
      │         └─ 수정 → 대안 제공 → Step 4
      ↓
┌─────────────────────────────────────┐
│ Step 5: 커밋 실행 및 검증            │
│ - git commit 실행                    │
│ - 검증 및 결과 보고                  │
└─────────────┬───────────────────────┘
              ↓
             End
```

---

## Related Documents

- **[SKILL.md](SKILL.md)** - Overview and quick reference
- **[AUTO_SPLIT.md](AUTO_SPLIT.md)** - Auto-split commit process
- **[RULES.md](RULES.md)** - Commit message format rules
- **[EXAMPLES.md](EXAMPLES.md)** - Commit examples
- **[METADATA.md](METADATA.md)** - Token optimization strategy
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Error handling

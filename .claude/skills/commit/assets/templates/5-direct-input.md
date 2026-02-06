# Template 5: Message Modification (Direct Input)

## Situation

User selected "Other" option for direct input

## Process

### Step 1: Detect Other Selection

```
User selection: "Other"
```

### Step 2: Show Input Instructions

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✏️ 커밋 메시지 직접 입력
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

형식: <type>(scope): <message>

본문을 추가하려면 빈 줄 후 작성하세요.

⚠️ 본문 형식 규칙 (MANDATORY):
  - 각 줄은 반드시 "- "(대시 + 공백)으로 시작
  - 각 항목은 반드시 개행으로 구분 (쉼표로 연결 금지)
  - 5줄 이하로 작성
  - 파일별 또는 기능별로 그룹화

예시 (올바른 형식):
  docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

  - SKILL.md: 스킬 실행 프로세스 정의
  - RULES.md: 커밋 메시지 형식 규칙
  - EXAMPLES.md: 실제 사용 예시

잘못된 형식 (대시 없음):
  docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

  SKILL.md: 스킬 실행 프로세스 정의
  RULES.md: 커밋 메시지 형식 규칙

잘못된 형식 (쉼표로 연결):
  docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

  - SKILL.md: 스킬 실행 프로세스 정의, RULES.md: 커밋 메시지 형식 규칙

입력하세요:
```

### Step 3: Validate Input Message

```
입력된 메시지:
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙

[형식 검증 중...]
✅ 형식 검증 통과
```

### Step 4: Final Confirmation (Reuse 4-final-confirmation)

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 최종 커밋 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

이 메시지로 커밋하시겠습니까?

1. 승인 - 커밋 실행
2. 수정
3. 취소

선택:
```

## Validation

### Validation Items

```bash
# 정규식 검증
pattern="^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$"
```

- Type: 7가지 중 하나 (feat, fix, refactor, test, docs, style, chore)
- Scope: 영숫자 + `.`, `-`, `_`만 포함
- Message: 비어있지 않음
- 공백 블록: 최대 2개

### On Validation Failure

```
❌ 형식 검증 실패

오류: 커밋 타입이 잘못되었습니다
입력: "feature(commit-skill): ..."
허용: feat, fix, refactor, test, docs, style, chore

다시 입력하세요:
```

**Template for retry (형식 명세):**

```json
{
  "questions": [
    {
      "question": "다시 입력하세요:",
      "header": "검증 실패",
      "multiSelect": false,
      "options": [
        {
          "label": "다시 입력",
          "description": "커밋 메시지를 다시 작성합니다"
        },
        {
          "label": "메시지 선택으로 돌아가기",
          "description": "제안된 메시지 중 선택합니다"
        },
        {
          "label": "취소",
          "description": "커밋 프로세스를 종료합니다"
        }
      ]
    }
  ]
}
```

**형식 설명:**

이 템플릿은 **완전히 정적**입니다:
- 검증 실패 시 항상 동일한 옵션 제공
- 동적 요소 없음

**정적 요소:**
- question: "다시 입력하세요:"
- header: "검증 실패"
- 3개 옵션 레이블 및 설명 (모두 고정)

**Example (구체적 예시):**

```json
{
  "questions": [
    {
      "question": "다시 입력하세요:",
      "header": "검증 실패",
      "multiSelect": false,
      "options": [
        {
          "label": "다시 입력",
          "description": "커밋 메시지를 다시 작성합니다"
        },
        {
          "label": "메시지 선택으로 돌아가기",
          "description": "제안된 메시지 중 선택합니다"
        },
        {
          "label": "취소",
          "description": "커밋 프로세스를 종료합니다"
        }
      ]
    }
  ]
}
```

**Note:** Template과 Example이 동일합니다 (완전히 정적이므로).

## User Action

- Validation passes → Proceed to 4-final-confirmation (final confirmation)
- Validation fails → Show error and retry options
- "다시 입력" → Repeat direct input
- "메시지 선택으로 돌아가기" → Return to 3-1-header-selection
- "취소" → Exit process

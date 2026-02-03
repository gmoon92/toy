# 템플릿 5: 메시지 수정 (직접 입력)

## Situation

사용자가 "Other" 옵션을 선택하여 직접 입력

## Process

### 1단계: Other 선택 감지

```
사용자 선택: "Other"
```

### 2단계: 입력 안내

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✏️ 커밋 메시지 직접 입력
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

형식: <type>(scope): <message>

본문을 추가하려면 빈 줄 후 작성하세요.
예시:
  docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

  - SKILL.md: 스킬 실행 프로세스 정의
  - RULES.md: 커밋 메시지 형식 규칙

입력하세요:
```

### 3단계: 입력받은 메시지 검증

```
입력된 메시지:
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙

[형식 검증 중...]
✅ 형식 검증 통과
```

### 4단계: 최종 확인 (template-4 재사용)

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

### 검증 항목

```bash
# 정규식 검증
pattern="^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$"
```

- Type: 7가지 중 하나 (feat, fix, refactor, test, docs, style, chore)
- Scope: 영숫자 + `.`, `-`, `_`만 포함
- Message: 비어있지 않음
- 공백 블록: 최대 2개

### 검증 실패 시

```
❌ 형식 검증 실패

오류: 커밋 타입이 잘못되었습니다
입력: "feature(commit-skill): ..."
허용: feat, fix, refactor, test, docs, style, chore

다시 입력하세요:
```

**Template for retry:**

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

## User Action

- Validation passes → Proceed to template-4 (final confirmation)
- Validation fails → Show error and retry options
- "다시 입력" → Repeat direct input
- "메시지 선택으로 돌아가기" → Return to template-3
- "취소" → Exit process
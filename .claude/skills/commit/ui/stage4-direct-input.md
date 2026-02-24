# Stage 4: Direct Input UI

User enters commit message directly (when "Other" selected).

---

## Screen Output

Display input instructions:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
✏️ 커밋 메시지 직접 입력
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

형식: <type>(scope): <message>

본문을 추가하려면 빈 줄 후 작성하세요.

⚠️ 본문 형식 규칙:
  - 각 줄은 반드시 "- "(대시 + 공백)으로 시작
  - 5줄 이하로 작성

예시:
  docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

  - SKILL.md: 스킬 실행 프로세스 정의
  - RULES.md: 커밋 메시지 형식 규칙

입력하세요:
```

## Validation

- **Type:** One of allowed types (feat, fix, refactor, test, docs, style, chore)
- **Scope:** Alphanumeric + `.`, `-`, `_`
- **Message:** Not empty
- **Format:** `<type>(scope): <message>`

## On Validation Failure

```json
{
  "questions": [
    {
      "question": "형식 오류: {error_message}\n\n다시 선택하세요:",
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

## User Actions

| Result | Action |
|--------|--------|
| Validation passes | Proceed to Stage 4 confirmation |
| "다시 입력" | Repeat direct input |
| "메시지 선택으로 돌아가기" | Return to Stage 1 |
| "취소" | Exit process |

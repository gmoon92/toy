# Stage 4: Final Confirmation UI

Final confirmation before executing the commit.

---

## Screen Output

Display the complete commit message:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 최종 커밋 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
- EXAMPLES.md: 실제 사용 예시
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## AskUserQuestion Template

```json
{
  "questions": [
    {
      "question": "이 메시지로 커밋하시겠습니까?",
      "header": "커밋 확인",
      "multiSelect": false,
      "options": [
        {
          "label": "승인 - 커밋 실행",
          "description": "이 메시지로 git commit을 실행합니다"
        },
        {
          "label": "수정",
          "description": "다른 메시지를 선택하거나 직접 입력합니다"
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

| Selection | Action |
|-----------|--------|
| "승인" | Execute git commit |
| "수정" | Return to Stage 1 (header selection) |
| "취소" | Exit process |

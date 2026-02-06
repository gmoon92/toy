# Template 4: Final Confirmation (Before Commit)

## Situation

Final confirmation before committing with selected message

## Template

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

## Screen Output (Korean for users)

Display the final commit message before calling AskUserQuestion:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 최종 커밋 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
- EXAMPLES.md: 실제 사용 예시
- TROUBLESHOOTING.md: 문제 해결 가이드
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

Then call AskUserQuestion with Template JSON (tool will display question and options automatically).

## User Action

- "승인" selected → Proceed to Step 5 (execute commit)
- "수정" selected → Return to template-3 (message selection)
- "취소" selected → Exit process

## Why show full message again

- User may have only seen header in list view
- Final safety check before commit
- Clear visibility of complete message (header + body + footer)

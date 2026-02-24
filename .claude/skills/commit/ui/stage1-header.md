# Stage 1: Header Selection UI

User selects commit header from 5 pre-generated messages.

---

## Screen Output

Display before calling AskUserQuestion:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 1/3: 헤더 메시지 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 헤더로 사용할 메시지를 선택하세요.
(추천 메시지는 변경사항을 가장 잘 설명합니다)

감지된 파일들:
  - .claude/skills/commit/SKILL.md
  - .claude/skills/commit/rules/header.md
  ...

감지된 스코프: commit-skill
```

## AskUserQuestion Template

```json
{
  "questions": [
    {
      "question": "커밋 헤더 메시지를 선택하세요",
      "header": "헤더 선택",
      "multiSelect": false,
      "options": [
        {
          "label": "<type>(scope): <message> [추천]",
          "description": "분석 결과 기반 최우선 추천"
        },
        {
          "label": "<type>(scope): <message> [추천]",
          "description": "대안적 해석 기반 차선 추천"
        },
        {
          "label": "<type>(scope): <message>",
          "description": "일반 대안 1"
        },
        {
          "label": "<type>(scope): <message>",
          "description": "일반 대안 2"
        }
      ]
    }
  ]
}
```

**Note:** "Other" option is automatically added by AskUserQuestion for direct input.

## User Actions

| Selection | Action |
|-----------|--------|
| Message 1-4 | Store header, proceed to Stage 2 |
| "다른 추천 리스트 보기" | Regenerate general options, show again |
| "Other" (직접 입력) | Prompt for manual input, then Stage 2 |

## Refresh Logic

When user selects "다른 추천 리스트 보기":
1. Keep Recommended 1 & 2 fixed
2. Regenerate General 3-5 with new variations
3. Show new options

## Direct Input Flow

When "Other" selected:

**Prompt:**
```
커밋 헤더 메시지를 직접 입력하세요.

형식: <type>(scope): <message>
예: feat(auth): JWT 인증 구현
```

**Validation:**
- Format: `^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$`
- On failure: Show error, allow retry
- On success: Store header, proceed to Stage 2

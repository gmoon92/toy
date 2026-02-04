# Template 3: Commit Message Selection

## Situation

Select from 5 message suggestions

## Template

```json
{
  "questions": [
    {
      "question": "커밋 메시지를 선택하세요",
      "header": "메시지 선택",
      "multiSelect": false,
      "options": [
        {
          "label": "메시지 1 (추천)",
          "description": "docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가\n\n- SKILL.md: 스킬 실행 프로세스 정의\n- RULES.md: 커밋 메시지 형식 규칙\n- EXAMPLES.md: 실제 사용 예시\n- TROUBLESHOOTING.md: 문제 해결 가이드"
        },
        {
          "label": "메시지 2",
          "description": "docs(SKILL.md): 커밋 메시지 자동 생성 스킬 추가\n\n- SKILL.md: 스킬 실행 프로세스 정의\n- RULES.md: 커밋 메시지 형식 규칙\n- EXAMPLES.md: 실제 사용 예시\n- TROUBLESHOOTING.md: 문제 해결 가이드"
        },
        {
          "label": "메시지 3",
          "description": "docs(commit-skill): 커밋 스킬 문서 추가\n\n- 커밋 자동화 스킬 문서\n- 메시지 형식 규칙 정의"
        },
        {
          "label": "메시지 4",
          "description": "docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가"
        }
      ]
    }
  ]
}
```

**Important:** 5th message doesn't need to be in options; "Other" option is automatically added.

## Screen Output (Korean for users)

AskUserQuestion tool automatically displays the question and all options from Template JSON.

The description field in each option already contains the full commit message (header + body), which will be displayed by the tool.

## User Action

- "메시지 1-4" selected → Proceed to template-4 (final confirmation)
- "Other" selected → Proceed to template-5 (direct input)

## Requirements

1. **Show complete message**: Include full message (header + body) in each option's description
2. **Show recommendation**: Display "(추천)" on first message
3. **Other option**: Automatically added (allows user direct input)
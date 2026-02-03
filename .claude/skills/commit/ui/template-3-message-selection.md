# 템플릿 3: 커밋 메시지 선택

## Situation

5개 메시지 제안 중 선택

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

**중요:** 5번째 메시지는 options에 포함하지 않아도, "Other" 옵션이 자동 추가됨.

## 화면 출력 (한글)

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 커밋 메시지를 선택하세요
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. 메시지 1 (추천)
   docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

   - SKILL.md: 스킬 실행 프로세스 정의
   - RULES.md: 커밋 메시지 형식 규칙
   - EXAMPLES.md: 실제 사용 예시
   - TROUBLESHOOTING.md: 문제 해결 가이드

2. 메시지 2
   docs(SKILL.md): 커밋 메시지 자동 생성 스킬 추가

   - SKILL.md: 스킬 실행 프로세스 정의
   - RULES.md: 커밋 메시지 형식 규칙
   - EXAMPLES.md: 실제 사용 예시
   - TROUBLESHOOTING.md: 문제 해결 가이드

3. 메시지 3
   docs(commit-skill): 커밋 스킬 문서 추가

   - 커밋 자동화 스킬 문서
   - 메시지 형식 규칙 정의

4. 메시지 4
   docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

5. Other (직접 입력)

선택:
```

## User Action

- "메시지 1-4" selected → Proceed to template-4 (final confirmation)
- "Other" selected → Proceed to template-5 (direct input)

## Requirements

1. **전체 메시지 표시**: 각 옵션의 description에 완전한 메시지 포함 (헤더 + 본문)
2. **추천 표시**: 첫 번째 메시지에 "(추천)" 표시
3. **Other 옵션**: 자동으로 추가됨 (사용자 직접 입력 가능)
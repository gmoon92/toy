# Stage 3: Footer Selection UI

User selects optional footer for the commit message.

---

## Screen Output

Display before calling AskUserQuestion:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 3/3: 푸터 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

선택한 헤더: {selected_header}
선택한 바디 항목: {selected_body_count}개

푸터를 추가할지 선택하세요.
대부분의 경우 푸터가 필요하지 않습니다.
```

## AskUserQuestion Template

```json
{
  "questions": [
    {
      "question": "커밋 푸터를 선택하세요",
      "header": "푸터 선택",
      "multiSelect": false,
      "options": [
        {
          "label": "푸터 없음 [추천]",
          "description": "대부분의 경우 푸터가 필요하지 않습니다"
        },
        {
          "label": "Issue reference 추가",
          "description": "형식: Closes #<number>, Fixes #<number>"
        },
        {
          "label": "Breaking Change",
          "description": "호환성을 깨는 변경사항 명시"
        }
      ]
    }
  ]
}
```

## User Actions

| Selection | Action |
|-----------|--------|
| "푸터 없음" | No footer, proceed to confirmation |
| "Issue reference" | Prompt for issue numbers, then confirmation |
| "Breaking Change" | Prompt for description, then confirmation |

## Footer Formats

**Issue Reference:**
```
Closes #123
Closes #123, #456
Fixes #789
```

**Breaking Change:**
```
BREAKING CHANGE: API 응답 형식이 JSON에서 XML로 변경됨
```

## Complete Message Preview

After footer selection, display:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 생성된 커밋 메시지 미리보기:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
{complete_message}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

다음 단계: 최종 확인
```

Then proceed to Stage 4 (final confirmation).

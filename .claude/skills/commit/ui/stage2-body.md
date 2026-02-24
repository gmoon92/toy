# Stage 2: Body Selection UI

User selects body items (multi-select) to describe what work was done.

---

## Screen Output

Display before calling AskUserQuestion:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

변경된 파일 (참고용):
  - UserService.java (+152, -23)
  - LoginController.java (+87, -5)
  - SecurityConfig.java (+45, -12)
  ...

커밋 본문에 포함할 작업 내용을 선택하세요.
- 스페이스바로 복수 선택 가능
- 작업 내용 중심으로 선택 (파일명 나열 금지)
```

## AskUserQuestion Template

```json
{
  "questions": [
    {
      "question": "바디에 포함할 작업 내용을 선택하세요 (복수 선택 가능)",
      "header": "바디 선택",
      "multiSelect": true,
      "options": [
        {
          "label": "작업 내용 1",
          "description": "상세 설명"
        },
        {
          "label": "작업 내용 2",
          "description": "상세 설명"
        },
        {
          "label": "작업 내용 3",
          "description": "상세 설명"
        },
        {
          "label": "바디 없음 (헤더만 사용)",
          "description": "간단한 변경이므로 헤더만으로 충분합니다"
        }
      ]
    }
  ]
}
```

## User Actions

| Selection | Action |
|-----------|--------|
| 1+ items | Add to body, proceed to Stage 3 |
| "바디 없음" | Header-only, proceed to Stage 3 |
| "Other" | Direct input for custom body |

## Body Output Format

Selected items formatted with `- ` prefix:

```
<type>(scope): <header message>

- 작업 내용 1
- 작업 내용 2
- 작업 내용 3
```

## Direct Input Flow

When "Other" selected:

**Prompt:**
```
커밋 본문을 직접 입력하세요.

각 줄은 "- "로 시작해야 합니다.
예:
- 사용자 인증 로직 구현
- 로그인 API 엔드포인트 추가
```

**Validation:**
- Each line starts with `- `
- Maximum 5 lines
- On failure: Show error, allow retry

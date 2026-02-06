# Template 3-3: Footer Selection

## Situation

Step 3 of 3-stage message creation: User selects footer (optional)

## Template (형식 명세)

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
          "description": "대부분의 경우 푸터가 필요하지 않습니다. 간결한 커밋 메시지를 유지합니다."
        },
        {
          "label": "Issue reference 추가",
          "description": "이슈 트래커 참조를 추가합니다. 형식: Closes #<number>, Fixes #<number>, Refs #<number>"
        },
        {
          "label": "Breaking Change",
          "description": "호환성을 깨는 변경사항을 명시합니다. 형식: BREAKING CHANGE: <description>"
        }
      ]
    }
  ]
}
```

**형식 설명:**

**푸터 없음:**
- 형식: (empty)
- 대부분의 커밋에 사용

**Issue reference:**
- 형식: `Closes #<number>` 또는 `Fixes #<number>` 또는 `Refs #<number>`
- Multiple: `Closes #123, #456`
- `<number>`: 이슈 번호

**Breaking Change:**
- 형식: `BREAKING CHANGE: <description>`
- `<description>`: 호환성을 깨는 변경사항 설명

**정적 요소:**
- "푸터 없음 [추천]" 옵션 레이블
- Keywords: `Closes`, `Fixes`, `Refs`, `BREAKING CHANGE:`
- 콜론 `:`, 해시 `#`, 쉼표 `,`, 공백

**동적 요소:**
- `<number>`: 이슈 번호
- `<description>`: 변경사항 설명

## Example (구체적 예시)

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
          "description": "대부분의 경우 푸터가 필요하지 않습니다. 간결한 커밋 메시지를 유지합니다."
        },
        {
          "label": "Issue reference 추가",
          "description": "이슈 트래커 참조를 추가합니다. 형식: Closes #<number>, Fixes #<number>, Refs #<number>"
        },
        {
          "label": "Breaking Change",
          "description": "호환성을 깨는 변경사항을 명시합니다. 형식: BREAKING CHANGE: <description>"
        }
      ]
    }
  ]
}
```

**실제 푸터 예시:**

Issue reference 선택 후:
```
Closes #123
Closes #123, #456
Fixes #789
Refs #100, #200
```

Breaking Change 선택 후:
```
BREAKING CHANGE: API 응답 형식이 JSON에서 XML로 변경됨
BREAKING CHANGE: 사용자 인증 방식 변경 (세션 → JWT)
```

## Screen Output (Korean for users)

Before calling AskUserQuestion, display:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 3/3: 푸터 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

선택한 타입: {selected_type}
선택한 스코프: {detected_scope}
선택한 바디 항목: {selected_body_count}개

푸터를 추가할지 선택하세요.
대부분의 경우 푸터가 필요하지 않습니다.
```

AskUserQuestion tool will display the options automatically.

## User Action

**Selection scenarios:**
1. **"푸터 없음" selected** → Generate final message without footer, proceed to 4-final-confirmation (final confirmation)
2. **"Issue reference" selected** → Prompt for issue numbers, add footer, proceed to 4-final-confirmation
3. **"Breaking Change" selected** → Prompt for breaking change description, add footer, proceed to 4-final-confirmation

## Footer Format

### Issue Reference

**Prompt for issue numbers:**
```
이슈 번호를 입력하세요 (여러 개는 쉼표로 구분):
예: 123, 456
```

**Generated footer:**
```
Closes #123, #456
```

**Keywords:**
- `Closes #123` - Issue is resolved by this commit
- `Fixes #123` - Bug is fixed by this commit
- `Refs #123` - Related but not resolved

**Multiple issues:**
```
Closes #123, #456
Fixes #789
```

### Breaking Change

**Prompt for description:**
```
호환성을 깨는 변경사항을 설명하세요:
예: API 응답 형식이 JSON에서 XML로 변경됨
```

**Generated footer:**
```
BREAKING CHANGE: {user_input}
```

**Full example:**
```
feat(api): 응답 형식 변경

- JSON 응답을 XML로 변경
- 기존 클라이언트 호환성 깨짐

BREAKING CHANGE: API 응답 형식이 JSON에서 XML로 변경됨
```

## Complete Message Preview

After footer selection, display preview:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 생성된 커밋 메시지 미리보기:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
{complete_message}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

다음 단계: 최종 확인
```

Then proceed to 4-final-confirmation (final confirmation).

## Message Assembly

**Final message structure:**
```
{type}({scope}): {header_message}

{body_line_1}
{body_line_2}
...

{footer}
```

**Components:**
1. **Header**: `{type}({scope}): {message}`
   - Type: Selected from 3-1-header-selection-1
   - Scope: Detected from changed files
   - Message: Auto-generated based on type + scope + body items

2. **Body** (optional): Selected items from 3-1-header-selection-2
   - Format: Each line starts with `- `
   - Blank line separates header and body

3. **Footer** (optional): Selected from 3-1-header-selection-3
   - Blank line separates body and footer

## Header Message Generation

**Auto-generate header message based on context:**

```javascript
function generateHeaderMessage(type, scope, bodyItems) {
  // Extract key action from body items
  const primaryAction = extractPrimaryAction(bodyItems);

  // Generate appropriate message
  if (type === 'feat') {
    return `${primaryAction} 추가`;
  } else if (type === 'fix') {
    return `${primaryAction} 수정`;
  } else if (type === 'refactor') {
    return `${primaryAction} 개선`;
  }
  // ... other types
}
```

**User can modify:**
- After preview, user can edit header message if needed
- Use 5-direct-input (direct input) pattern for edits

## Notes

**Footer usage guidelines:**
- **푸터 없음**: Default and recommended for most commits
- **Issue reference**: Use when commit closes/fixes a tracked issue
- **Breaking Change**: MUST use when introducing breaking changes

**Best practices:**
- Keep footer concise
- Don't add unnecessary metadata
- Don't add "Co-Authored-By" or AI attribution (as per PROCESS.md)

**Token efficiency:**
- Footer templates are lightweight
- Load only when user reaches Step 3
- No need to pre-generate footer options

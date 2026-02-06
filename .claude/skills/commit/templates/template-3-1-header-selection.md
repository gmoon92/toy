# Template 3-1: Header Message Selection

## Situation

Stage 1 of 3-stage message composition: User selects commit header from 5 pre-generated messages

## Header Message Generation Strategy

Generate **5 header messages**:
- **추천 2개** (fixed, always shown): Most appropriate headers based on analysis
- **일반 3개** (refreshable): Alternative headers, can be regenerated

### Recommended Messages (2개, 고정)

**Message 1 (최우선 추천):**
- Optimal scope (module name preferred over filename)
- Clear, concise expression
- Best represents the overall change

**Message 2 (차선 추천):**
- Alternative scope (filename if Message 1 uses module, or vice versa)
- Different perspective or emphasis

### General Messages (3개, 새로고침 가능)

**Message 3:**
- Scope variation (different level: parent dir, subdirectory, etc.)

**Message 4:**
- Message expression variation (more concise or more detailed)

**Message 5:**
- Type alternative (if interpretable as different type)

**Generation algorithm:** See [MESSAGE_GENERATION.md](../MESSAGE_GENERATION.md#header-message-generation)

## Template

```json
{
  "questions": [
    {
      "question": "커밋 헤더 메시지를 선택하세요",
      "header": "헤더 선택",
      "multiSelect": false,
      "options": [
        {
          "label": "docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경 (추천)",
          "description": "가장 적절한 scope와 표현을 사용한 메시지"
        },
        {
          "label": "refactor(commit-skill): 메시지 생성 방식 재구성 (추천)",
          "description": "타입을 refactor로 해석한 대안 메시지"
        },
        {
          "label": "docs(MESSAGE_GENERATION.md): 3단계 선택 알고리즘으로 재작성",
          "description": "파일 scope로 변경한 메시지"
        },
        {
          "label": "docs(.claude/skills): commit 스킬 문서 업데이트",
          "description": "상위 디렉토리 scope로 변경한 메시지"
        }
      ]
    }
  ]
}
```

**Important:**
- "Other" option is automatically added by AskUserQuestion tool for direct input
- Only 4 options shown (AskUserQuestion limit), so we show: 추천 2개 + 일반 2개
- 5th general message can be shown on refresh

## Screen Output (Korean for users)

Before calling AskUserQuestion, display:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 1/3: 헤더 메시지 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 헤더로 사용할 메시지를 선택하세요.
(추천 메시지는 변경사항을 가장 잘 설명합니다)
```

AskUserQuestion tool will display the options automatically.

## User Actions

**Selection scenarios:**
1. **User selects one of 4 messages** → Store selected header, proceed to Stage 2 (body selection)
2. **User selects "다른 추천 리스트 보기"** → Regenerate 일반 3개 (keep 추천 2개), show again
3. **User selects "Other" (직접 입력)** → Prompt for manual header input, validate format, proceed to Stage 2

## Refresh Logic (다른 추천 리스트 보기)

When user wants to see different options:

1. **Keep 추천 2개** (fixed, always the same)
2. **Regenerate 일반 3개** using different strategies:
   - Try different scope levels
   - Try different message expressions
   - Try different type interpretations

**Refresh algorithm:**
```javascript
function refreshGeneralMessages(fixedRecommended, allCandidates) {
  // Keep recommended 2 messages fixed
  const recommended = fixedRecommended; // [msg1, msg2]

  // Filter out already shown general messages
  const remainingCandidates = allCandidates.filter(
    msg => !previouslyShown.includes(msg)
  );

  // Pick 3 new general messages
  const newGeneral = remainingCandidates.slice(0, 3);

  // Show: 추천 2 + 새로운 일반 3
  return [...recommended, ...newGeneral];
}
```

**Important:**
- Generate 10-15 candidate messages in Step 1 (metadata)
- Always show same 추천 2개
- Rotate through different 일반 3개 on each refresh
- After showing all candidates, wrap around to beginning

## Direct Input (직접 입력)

When user selects "Other":

**Prompt:**
```
커밋 헤더 메시지를 직접 입력하세요.

형식: <type>(scope): <message>
예: feat(auth): JWT 인증 구현
```

**Validation:**
- Must match format: `<type>(scope): <message>`
- Type must be one of: feat, fix, refactor, test, docs, style, chore
- Scope: alphanumeric + `.`, `-`, `_` only
- Message: not empty, start with lowercase

**Regex:**
```regex
^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$
```

**On validation failure:**
- Show error message with examples
- Allow retry

**On success:**
- Store user's header
- Proceed to Stage 2 (body selection)

## Scope Detection and Display

**Display detected scope as context:**
```
감지된 파일들:
  - .claude/skills/commit/MESSAGE_GENERATION.md
  - .claude/skills/commit/PROCESS.md
  - .claude/skills/commit/SKILL.md
  ...

감지된 스코프: commit-skill
```

This helps user understand why certain headers are suggested.

## Notes

**Header message format:**
- `<type>(scope): <brief description>`
- Brief: 50-70 characters max
- Lowercase start (Korean or English)
- No period at end

**Scope selection priority:**
1. Module name (preferred): `commit-skill`, `spring-batch`
2. Filename (for single file): `UserService.java`
3. Directory name: `auth`, `utils`

**Type detection:**
- Analyze git diff to determine primary change type
- Consider: new features, bug fixes, refactoring, docs, etc.
- Default to most conservative type if ambiguous

## Related Templates

- **[template-3-2-body-selection.md](template-3-2-body-selection.md)** - Next stage after header selection
- **[template-3-3-footer-selection.md](template-3-3-footer-selection.md)** - Final stage
- **[template-4-final-confirmation.md](template-4-final-confirmation.md)** - Approval before commit
- **[template-5-direct-input.md](template-5-direct-input.md)** - Direct input fallback (deprecated, use "Other" option instead)

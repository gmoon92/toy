# Template 3-2: Body Items Multi-Selection

## Situation

Step 2 of 3-stage message creation: User selects body items (multi-select)

## Body Item Generation Algorithm

**Analyze changed files and generate body item candidates:**

1. **File-based grouping** (default):
   - Group by file or logical module
   - Format: `{filename}: {주요 변경사항}`
   - Example: `UserService.java: 사용자 인증 로직 추가`

2. **Feature-based grouping** (alternative):
   - Group by functional change
   - Format: `{기능 설명}`
   - Example: `JWT 토큰 생성 및 검증 로직 구현`

3. **Hybrid approach** (recommended):
   - Mix both styles based on change size
   - Small changes (1-3 files): File-based
   - Large changes (4+ files): Feature-based

**Generation rules:**
- Maximum 10 candidates (user can select subset)
- Each item should be 1-2 lines
- Focus on "what" rather than "why"
- Sort by importance/impact

## Template

```json
{
  "questions": [
    {
      "question": "커밋에 포함할 작업 내용을 선택하세요 (복수 선택 가능)",
      "header": "바디 선택",
      "multiSelect": true,
      "options": [
        {
          "label": "UserService.java: 사용자 인증 로직 추가",
          "description": "JWT 기반 사용자 인증 처리 로직 구현"
        },
        {
          "label": "LoginController.java: 로그인 API 엔드포인트 구현",
          "description": "/api/auth/login POST 엔드포인트 추가"
        },
        {
          "label": "application.yml: 데이터베이스 설정 변경",
          "description": "PostgreSQL 연결 정보 및 JPA 설정 추가"
        },
        {
          "label": "SecurityConfig.java: Spring Security 설정",
          "description": "JWT 필터 체인 및 인증 매니저 설정"
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

**Important:**
- First 4-8 options are auto-generated body item candidates
- "바디 없음 (헤더만 사용)" option for header-only commits
- "다른 추천 리스트 보기" option to regenerate candidates
- "Other" option (automatically added) for direct input

**Note:** AskUserQuestion supports max 4 options, so show 3 candidates + "바디 없음". User can refresh to see more candidates.

## Screen Output (Korean for users)

Before calling AskUserQuestion, display:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

선택한 타입: {selected_type}

커밋 본문에 포함할 작업 내용을 선택하세요.
- 스페이스바로 복수 선택 가능
- 간단한 변경이면 "바디 없음" 선택
- 변경사항이 5개 이상이면 바디 추가 권장
```

AskUserQuestion tool will display the options automatically with multi-select enabled.

## User Actions

**Selection scenarios:**
1. **User selects 1+ items** → Store selected items, proceed to Stage 3 (footer selection)
2. **User selects "바디 없음"** → Header-only commit, proceed to Stage 3
3. **User selects "다른 추천 리스트 보기"** → Regenerate body item candidates, show again
4. **User selects "Other" (직접 입력)** → Allow direct body input (free text), then proceed to Stage 3

## Body Generation Rules

**From selected items:**
- Each selected item becomes one line in body
- Format: `- {item_label}` (dash + space + item)
- Order: Same as selection order or importance
- Limit: 5 lines maximum (warn if more selected)

**Example:**
User selects:
1. UserService.java: 사용자 인증 로직 추가
2. LoginController.java: 로그인 API 엔드포인트 구현
3. SecurityConfig.java: Spring Security 설정

Generated body:
```
- UserService.java: 사용자 인증 로직 추가
- LoginController.java: 로그인 API 엔드포인트 구현
- SecurityConfig.java: Spring Security 설정
```

## Scope Extraction

**Determine scope based on changed files:**

1. **Module name** (preferred for multi-file changes):
   - Extract common directory: `src/auth/` → `auth`
   - Use meaningful module name: `spring-security-jwt`, `user-auth`

2. **Filename** (for single file or small changes):
   - Extract filename: `UserService.java`
   - Or use path: `auth/UserService.java`

3. **Auto-detection algorithm:**
   ```
   if (changed_files.length == 1) {
     scope = extract_filename(changed_files[0])
   } else {
     scope = extract_common_module(changed_files)
   }
   ```

**Display scope to user:**
```
감지된 스코프: {detected_scope}
(다음 단계에서 수정 가능)
```

## Refresh Logic (다른 추천 리스트 보기)

When user wants to see different body item options:

**Refresh strategy:**
1. Keep same changed files
2. **Change grouping strategy**:
   - If currently file-based → Switch to feature-based
   - If currently feature-based → Switch to hybrid
   - If currently hybrid → Switch to file-based
3. **Re-rank by different criteria**:
   - By importance (default)
   - By file type (source files first)
   - By directory (group by location)
   - By lines changed (largest first)

**Example:**
```javascript
function refreshBodyItems(files, diff, previousStrategy) {
  const strategies = ['file-based', 'feature-based', 'hybrid'];
  const currentIndex = strategies.indexOf(previousStrategy);
  const nextStrategy = strategies[(currentIndex + 1) % 3];

  return generateBodyItems(files, diff, nextStrategy);
}
```

**Important:**
- Generate 15-20 candidate items in Step 1 (metadata)
- Show 3-4 at a time
- Rotate through different groupings on each refresh
- Always include "바디 없음" option

## Direct Input (직접 입력)

When user selects "Other":

**Prompt:**
```
커밋 본문을 직접 입력하세요 (여러 줄 가능).

각 줄은 "- "로 시작해야 합니다.
예:
- UserService.java: 사용자 인증 로직 추가
- LoginController.java: 로그인 API 구현
```

**Validation:**
- Each line must start with `- ` (dash + space)
- Maximum 5 lines
- Each line should be concise (1-2 lines)

**On validation failure:**
- Show error message
- Allow retry

**On success:**
- Store user's body
- Proceed to Stage 3 (footer selection)

## Notes

**Body addition criteria:**
- 5+ files changed → Strongly recommend body
- 100+ lines changed → Recommend body
- Complex logic → Recommend body
- Simple changes → "바디 없음" is fine

**Item selection best practices:**
- Select items that provide value in commit history
- Don't select every single file change
- Group related changes into single item if appropriate
- Prioritize important/risky changes

**Token efficiency:**
- Generate candidates in Step 1 (metadata)
- Reuse from metadata in Stage 2
- Regenerate only on explicit refresh request

# Template 3-2: Body Items Multi-Selection

## Situation

Step 2 of 3-stage message creation: User selects body items (multi-select) with pagination

## Core Principle

**Body의 목적:**
- ❌ 변경된 파일 나열 (git log에 이미 표시됨)
- ✅ 무엇을 했는지 작업 내용 설명

**파일 리스트 vs Body:**
- Git log가 자동으로 보여주는 것: 파일 리스트, 변경 라인 수
- Body가 제공해야 하는 것: 작업 내용, 목적, 맥락

## Body Item Generation (Script-Based)

**MANDATORY: Use executable script for body item generation**

```bash
EXECUTE_SCRIPT: scripts/generation/generate_body_items.js
```

**Input (JSON via stdin):**
```json
{
  "files": [{"path": "...", "additions": N, "deletions": N}],
  "diff": "git diff output",
  "type": "feat|fix|refactor|..."
}
```

**Output (JSON):**
```json
{
  "items": [
    {
      "label": "작업 내용 설명",
      "description": "상세 설명",
      "score": 95,
      "relatedFiles": ["file1", "file2"],
      "module": "module-name"
    }
  ],
  "totalCount": 15,
  "detectedType": "feat"
}
```

**Generation rules:**
- Feature/작업 중심 항목 생성 (파일명 제외)
- Score로 중요도 표시 (0-100): 변경량(40%) + 중요도(30%) + 관련성(30%)
- 10-15개 후보 생성 (메타데이터 저장)
- 페이지당 3개 항목 표시
- 각 항목은 1-2줄로 간결하게
- Score 기준 정렬 (높은 순)

**See:** [scripts/generation/generate_body_items.js](../scripts/generation/generate_body_items.js) for implementation details

## Template (형식 명세)

```json
{
  "questions": [
    {
      "question": "바디에 포함할 작업 내용을 선택하세요 (복수 선택 가능)",
      "header": "바디 선택",
      "multiSelect": true,
      "options": [
        {
          "label": "[{score}⭐] <feature_description>",
          "description": "<detailed_explanation>"
        },
        {
          "label": "[{score}⭐] <feature_description>",
          "description": "<detailed_explanation>"
        },
        {
          "label": "[{score}] <feature_description>",
          "description": "<detailed_explanation>"
        },
        {
          "label": "[다음 페이지]",
          "description": "({next_start}-{next_end}번 항목 보기)"
        }
      ]
    }
  ]
}
```

**형식 설명:**

**Feature-based 항목 (권장, 기본 전략):**
- `[{score}⭐]`: 대괄호 안에 점수, 80 이상이면 ⭐
- `<feature_description>`: 작업/기능 설명 (파일명 X)
- `<detailed_explanation>`: 상세 설명 (기술적 세부사항)

**Navigation 항목:**
- `[다음 페이지]`, `[이전 페이지]`, `[선택 완료]`
- 페이지 번호 범위 표시

**정적 요소:**
- 대괄호 `[]`, Score 형식
- "바디 없음 (헤더만 사용)" 옵션 (마지막 페이지)
- Navigation 옵션 레이블

**동적 요소:**
- `{score}`: 중요도 점수 (0-100)
- `<feature_description>`: 작업 내용 설명
- `<detailed_explanation>`: 상세 설명
- 페이지 번호 범위

## Example (구체적 예시)

**Page 1 (항목 1-3번):**
```json
{
  "questions": [
    {
      "question": "바디에 포함할 작업 내용을 선택하세요 (복수 선택 가능)",
      "header": "바디 선택",
      "multiSelect": true,
      "options": [
        {
          "label": "[95⭐] 사용자 인증 로직 구현",
          "description": "JWT 기반 사용자 인증 처리 및 세션 관리"
        },
        {
          "label": "[90⭐] 로그인 API 엔드포인트 추가",
          "description": "/api/auth/login POST 엔드포인트 구현"
        },
        {
          "label": "[85⭐] Spring Security 필터 체인 구성",
          "description": "JWT 검증 필터 및 인증 매니저 설정"
        },
        {
          "label": "[다음 페이지]",
          "description": "(4-6번 항목 보기)"
        }
      ]
    }
  ]
}
```

**Page 2 (항목 4-6번):**
```json
{
  "questions": [
    {
      "question": "바디에 포함할 작업 내용을 선택하세요 (복수 선택 가능)",
      "header": "바디 선택",
      "multiSelect": true,
      "options": [
        {
          "label": "[80] JWT 토큰 생성 및 검증 로직",
          "description": "토큰 생성, 파싱, 유효성 검증 유틸리티"
        },
        {
          "label": "[75] JWT 인증 필터 추가",
          "description": "요청별 토큰 검증 및 SecurityContext 설정"
        },
        {
          "label": "[70] 인증 DTO 클래스 추가",
          "description": "로그인 요청/응답 데이터 전송 객체"
        },
        {
          "label": "[이전 페이지]",
          "description": "(1-3번 항목으로)"
        },
        {
          "label": "[다음 페이지]",
          "description": "(7-9번 항목 보기)"
        }
      ]
    }
  ]
}
```

**Last Page (항목 10+ 또는 완료):**
```json
{
  "questions": [
    {
      "question": "바디에 포함할 작업 내용을 선택하세요 (복수 선택 가능)",
      "header": "바디 선택",
      "multiSelect": true,
      "options": [
        {
          "label": "[60] 설정 파일 업데이트",
          "description": "JWT 관련 설정 및 데이터베이스 연결 정보"
        },
        {
          "label": "[55] 테스트 설정 변경",
          "description": "인증 관련 통합 테스트 환경 구성"
        },
        {
          "label": "[이전 페이지]",
          "description": "(7-9번 항목으로)"
        },
        {
          "label": "바디 없음 (헤더만 사용)",
          "description": "간단한 변경이므로 헤더만으로 충분합니다"
        },
        {
          "label": "[선택 완료]",
          "description": "현재 선택된 항목으로 진행"
        }
      ]
    }
  ]
}
```

**최종 Body 출력 예시:**
```
feat(auth): JWT 기반 인증 시스템 구현

- 사용자 인증 로직 구현
- 로그인 API 엔드포인트 추가
- Spring Security 필터 체인 구성
```

**Important:**
- 페이지당 3개 항목 표시 (AskUserQuestion limit: 4 options)
- 마지막 1개는 Navigation 또는 "바디 없음"/"선택 완료"
- Score 80 이상에 ⭐ 표시
- "Other" option (자동 추가)으로 직접 입력 가능
- 선택은 페이지 간 누적됨

## Screen Output (Korean for users)

Before calling AskUserQuestion, display:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택 [페이지 {current}/{total}]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

변경된 파일 ({file_count}개, 참고용):
  [{score}⭐] {filename}  (+{additions}, -{deletions})
  [{score}⭐] {filename}  (+{additions}, -{deletions})
  [{score}] {filename}  (+{additions}, -{deletions})
  ...

💡 Score: 변경량(40%) + 중요도(30%) + 관련성(30%)
   ⭐ = Score 80 이상 (중요)

현재 선택: {selected_count}개

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
작업 내용 선택 ({start}-{end}번):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 본문에 포함할 작업 내용을 선택하세요.
- 스페이스바로 복수 선택 가능
- 간단한 변경이면 "바디 없음" 선택
- Score가 높을수록 중요한 작업
```

AskUserQuestion tool will display the options automatically with multi-select enabled.

## User Actions

**Selection scenarios:**
1. **User selects 1+ items** → Add to selected list, continue pagination
2. **User selects "[다음 페이지]"** → Show next 3 items (4-6, 7-9, etc.)
3. **User selects "[이전 페이지]"** → Show previous 3 items
4. **User selects "[선택 완료]"** → Proceed to Stage 3 (footer selection)
5. **User selects "바디 없음"** → Header-only commit, proceed to Stage 3
6. **User selects "Other" (직접 입력)** → Allow direct body input (free text), proceed to Stage 3

**Pagination flow:**
```
Page 1 (1-3) → [다음] → Page 2 (4-6) → [다음] → Page 3 (7-9) → [선택 완료]
                 ↑                      ↑                      ↑
              [이전] ←───────────── [이전] ←───────────── [이전]
```

**Selection state:**
- 선택된 항목은 페이지 간 누적됨
- 각 페이지 상단에 "현재 선택: N개" 표시
- 마지막 페이지에서 "[선택 완료]" 또는 "바디 없음" 선택

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

## Pagination Implementation

**Generate all candidates in Step 1 (metadata):**

```javascript
// In Step 1: Pre-validation and Context Collection
const bodyItemCandidates = generateBodyItems(files, diff);
// Returns 10-15 items sorted by score

// Save to metadata
metadata.analysis.bodyItemCandidates = bodyItemCandidates;
```

**Paginate in Step 2:**

```javascript
const itemsPerPage = 3;
let currentPage = 0;
let selectedItems = [];

while (true) {
  const start = currentPage * itemsPerPage;
  const end = Math.min(start + itemsPerPage, candidates.length);
  const pageItems = candidates.slice(start, end);

  // Build options for current page
  const options = pageItems.map(item => ({
    label: `[${item.score}${item.score >= 80 ? '⭐' : ''}] ${item.label}`,
    description: item.description
  }));

  // Add navigation
  if (end < candidates.length) {
    options.push({
      label: "[다음 페이지]",
      description: `(${end+1}-${Math.min(end+itemsPerPage, candidates.length)}번 항목 보기)`
    });
  }

  if (currentPage > 0) {
    options.push({
      label: "[이전 페이지]",
      description: `(${start-itemsPerPage+1}-${start}번 항목으로)`
    });
  }

  // Last page: add completion options
  if (end >= candidates.length) {
    options.push({
      label: "바디 없음 (헤더만 사용)",
      description: "간단한 변경이므로 헤더만으로 충분합니다"
    });
    options.push({
      label: "[선택 완료]",
      description: `현재 ${selectedItems.length}개 선택됨`
    });
  }

  // Call AskUserQuestion
  const response = await AskUserQuestion({
    question: "바디에 포함할 작업 내용을 선택하세요 (복수 선택 가능)",
    header: "바디 선택",
    multiSelect: true,
    options: options
  });

  // Process response
  // ... handle navigation, selection accumulation, completion
}
```

**Important:**
- Generate 10-15 candidate items in Step 1 (metadata)
- Show 3 items per page
- Max 4 options per AskUserQuestion (3 items + 1 navigation/completion)
- Accumulate selections across pages
- Show "현재 선택: N개" on each page

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

## Complete Example Flow

**Screen Output Example:**

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택 [페이지 1/3]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

변경된 파일 (10개, 참고용):
  [95⭐] UserService.java          (+152, -23)
  [90⭐] LoginController.java      (+87, -5)
  [85⭐] SecurityConfig.java       (+45, -12)
  [80] JwtUtil.java                (+120, -0)
  [75] JwtFilter.java              (+89, -3)
  [70] AuthDto.java                (+34, -0)
  [65] UserRepository.java         (+28, -2)
  [60] application.yml             (+15, -3)
  [55] WebSecurityConfig.java      (+42, -8)
  [50] TestConfig.java             (+12, -5)

💡 Score: 변경량(40%) + 중요도(30%) + 관련성(30%)
   ⭐ = Score 80 이상 (중요)

현재 선택: 0개

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
작업 내용 선택 (1-3번):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 본문에 포함할 작업 내용을 선택하세요.
- 스페이스바로 복수 선택 가능
- Score가 높을수록 중요한 작업

[AskUserQuestion displays:]
  ☐ [95⭐] 사용자 인증 로직 구현
  ☐ [90⭐] 로그인 API 엔드포인트 추가
  ☐ [85⭐] Spring Security 필터 체인 구성
  ☐ [다음 페이지]

User selects: 1번, 2번, [다음 페이지]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택 [페이지 2/3]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

현재 선택: 2개
  ✓ 사용자 인증 로직 구현
  ✓ 로그인 API 엔드포인트 추가

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
작업 내용 선택 (4-6번):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[AskUserQuestion displays:]
  ☐ [80] JWT 토큰 생성 및 검증 로직
  ☐ [75] JWT 인증 필터 추가
  ☐ [70] 인증 DTO 클래스 추가
  ☐ [이전 페이지]
  ☐ [다음 페이지]

User selects: [다음 페이지]

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택 [페이지 3/3]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

현재 선택: 2개

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
작업 내용 선택 (7-9번):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[AskUserQuestion displays:]
  ☐ [65] 사용자 조회 메서드 추가
  ☐ [60] 설정 파일 업데이트
  ☐ [55] 테스트 설정 변경
  ☐ [이전 페이지]
  ☐ 바디 없음 (헤더만 사용)
  ☐ [선택 완료]

User selects: [선택 완료]

→ Proceed to Stage 3 with 2 selected items
```

## Notes

**Body addition criteria:**
- 5+ files changed → Strongly recommend body
- 100+ lines changed → Recommend body
- Complex logic → Recommend body
- Simple changes → "바디 없음" is fine

**Item selection best practices:**
- Select items that provide value in commit history
- Don't select every single file change (파일명 나열 X)
- Items are feature/work-focused, not file-focused
- Prioritize high-score (important/risky) changes

**Token efficiency:**
- Generate candidates once in Step 1 (metadata)
- Reuse from metadata in Stage 2
- No regeneration needed (pagination handles all items)

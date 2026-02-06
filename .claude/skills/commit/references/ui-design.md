# Commit Message UI/UX Design

User-friendly 3-stage message composition with clear guidance and confirmation.

This directory contains individual UI templates for user interactions during the commit process, along with comprehensive UI/UX design guidelines.

---

## Template Files

Each template is separated into its own file for efficient context loading:

- **[template-1-tidy-first.md](template-1-tidy-first.md)** - Tidy First 위반 감지
- **[template-2-logical-independence.md](template-2-logical-independence.md)** - 논리적 독립성 감지
- **[template-3-1-header-selection.md](template-3-1-header-selection.md)** - Stage 1: 헤더 메시지 선택 (추천 2 + 일반 3)
- **[template-3-2-body-selection.md](template-3-2-body-selection.md)** - Stage 2: 바디 항목 다중 선택
- **[template-3-3-footer-selection.md](template-3-3-footer-selection.md)** - Stage 3: 푸터 선택
- **[template-4-final-confirmation.md](template-4-final-confirmation.md)** - 최종 확인
- **[template-5-direct-input.md](template-5-direct-input.md)** - 메시지 수정 (직접 입력)

### Token Efficiency
- **Selective loading**: Only load templates when needed
- **75-90% token savings**: Load ~50-100 lines instead of entire combined file
- **Scalability**: Easy to add new templates without affecting others

### Maintainability
- **Independent updates**: Modify each template without affecting others
- **Clear purpose**: File name indicates template purpose
- **Version control**: Track changes per template

---

## NEW: 3-Stage Message Composition Flow

### Overview

**User builds commit message through 3 stages:**

1. **Stage 1**: Select commit header from 5 pre-generated messages (추천 2 + 일반 3)
2. **Stage 2**: Select body items (multi-select from auto-generated candidates)
3. **Stage 3**: Select footer (none, issue reference, or breaking change)

**Benefits:**
- **User control**: Full transparency over each component
- **Educational**: Learn what goes into each part
- **Flexible**: Can skip stages or use direct input
- **Efficient**: No need to generate 5 complete messages

---

### Stage 1: Header Message Selection

**Template:** [template-3-1-header-selection.md](template-3-1-header-selection.md)

**Screen:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 1/3: 헤더 메시지 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 헤더로 사용할 메시지를 선택하세요.
(추천 메시지는 변경사항을 가장 잘 설명합니다)

○ docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경 (추천)
○ refactor(commit-skill): 메시지 생성 프로세스 재구성 (추천)
○ docs(MESSAGE_GENERATION.md): 헤더 5개 생성 전략으로 재작성
○ docs(.claude/skills): commit 스킬 문서 업데이트
```

**User Actions:**
- Select one of 4 headers → Proceed to Stage 2
- Select "다른 추천 리스트 보기" → Regenerate 일반 3개, show again
- Select "Other" (직접 입력) → Manual header input, proceed to Stage 2

**Generation strategy:**
- **추천 2개** (fixed): Best matches, always shown
- **일반 3개** (refreshable): Alternatives, rotate on refresh

---

### Stage 2: Body Items Selection (Multi-Select with Pagination) ⭐ Core Feature

**Template:** [template-3-2-body-selection.md](template-3-2-body-selection.md)

**Core Principle:**
- ❌ 파일명 나열 (git log에 이미 표시)
- ✅ 작업 내용 설명 (무엇을 했는지)

**System automatically generates 10-15 feature-based candidates** with score:

**Example screen:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택 [페이지 1/3]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

변경된 파일 (10개, 참고용):
  [95⭐] UserService.java          (+152, -23)
  [90⭐] LoginController.java      (+87, -5)
  [85⭐] SecurityConfig.java       (+45, -12)
  ...

💡 Score: 변경량(40%) + 중요도(30%) + 관련성(30%)
   ⭐ = Score 80 이상 (중요)

현재 선택: 0개

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
작업 내용 선택 (1-3번):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

◯ [95⭐] 사용자 인증 로직 구현
◯ [90⭐] 로그인 API 엔드포인트 추가
◯ [85⭐] Spring Security 필터 체인 구성
◯ [다음 페이지]
```

**Item Generation Strategy:**

**Feature-based (권장, 기본 전략):**
```
[{score}⭐] {작업 내용 설명}
Example: [95⭐] 사용자 인증 로직 구현
```

**Score 계산:**
- 변경 라인 수 (40%)
- 파일 중요도 (30%): src/main > config > test
- 커밋 타입 관련성 (30%)

**Pagination:**
- 10-15개 후보 생성
- 페이지당 3개 항목 표시
- Navigation: [다음 페이지], [이전 페이지], [선택 완료]
- 선택 항목은 페이지 간 누적

**User Actions:**
- Select 1+ items → Add to selection, continue pagination
- Select "[다음 페이지]" → Show next page
- Select "[이전 페이지]" → Show previous page
- Select "[선택 완료]" → Proceed to Stage 3
- Select "바디 없음" → Header-only commit, proceed to Stage 3
- Select "Other" (직접 입력) → Manual body input, proceed to Stage 3

**Assembled body format:**
```
- 사용자 인증 로직 구현
- 로그인 API 엔드포인트 추가
- Spring Security 필터 체인 구성
```

**Note:** 파일명은 git log에서 확인 가능, body는 작업 내용에 집중

---

### Stage 3: Footer Selection

**Template:** [template-3-3-footer-selection.md](template-3-3-footer-selection.md)

**Screen:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 3/3: 푸터 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

선택한 타입: feat
선택한 스코프: spring-security-jwt
선택한 바디 항목: 3개

푸터를 추가할지 선택하세요.
대부분의 경우 푸터가 필요하지 않습니다.

○ 푸터 없음 (추천)
○ Issue reference 추가
○ Breaking Change
```

**User Action:**
- Select "푸터 없음" → No footer, proceed to final confirmation
- Select "Issue reference" → Prompt for issue numbers → Proceed to final confirmation
- Select "Breaking Change" → Prompt for description → Proceed to final confirmation

**Footer formats:**
```
Closes #123, #456
Fixes #789
BREAKING CHANGE: API 응답 형식 변경
```

---

### Final Message Assembly

**System assembles complete message from 3 stages:**

```javascript
function assembleFinalMessage(selections) {
  const { type, scope, bodyItems, footer } = selections;

  // Generate header
  const headerMsg = generateHeaderMessage(type, scope, bodyItems);
  const header = `${type}(${scope}): ${headerMsg}`;

  // Format body
  let body = '';
  if (bodyItems.length > 0 && bodyItems[0] !== '바디 없음') {
    body = '\n\n' + bodyItems.map(item => `- ${item.label}`).join('\n');
  }

  // Add footer
  let footerSection = '';
  if (footer && footer !== '푸터 없음') {
    footerSection = '\n\n' + footer;
  }

  return header + body + footerSection;
}
```

**Example assembled message:**
```
feat(spring-security-jwt): JWT 인증 필터 추가

- UserService.java: 사용자 인증 로직 추가
- LoginController.java: 로그인 API 엔드포인트 구현
- SecurityConfig.java: Spring Security 설정

Closes #123
```

**Message preview:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 생성된 커밋 메시지 미리보기:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
feat(spring-security-jwt): JWT 인증 필터 추가

- UserService.java: 사용자 인증 로직 추가
- LoginController.java: 로그인 API 엔드포인트 구현
- SecurityConfig.java: Spring Security 설정

Closes #123
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

다음 단계: 최종 확인
```

Then proceed to template-4 (final confirmation).

---

## Final Confirmation

**Template:** [template-4-final-confirmation.md](template-4-final-confirmation.md)

**Screen:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 최종 커밋 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
feat(spring-security-jwt): JWT 인증 필터 추가

- UserService.java: 사용자 인증 로직 추가
- LoginController.java: 로그인 API 엔드포인트 구현
- SecurityConfig.java: Spring Security 설정
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

이 메시지로 커밋하시겠습니까?

1. 승인 - 커밋 실행
2. 수정 - 메시지 수정
3. 취소 - 프로세스 종료
```

**User Actions:**
- **Approve** → Execute git commit
- **Modify** → Return to Stage 1 or direct input (template-5)
- **Cancel** → Exit process

---

## Policy Selection UI (Logical Independence Detected)

**Template:** [template-2-logical-independence.md](template-2-logical-independence.md)

When multiple independent groups are detected:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ 논리적으로 독립적인 변경사항 감지!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

감지된 그룹:
  그룹 1: .claude/skills/commit/ (4개 파일)
  그룹 2: ai/docs/claude/ (70개 파일)
  그룹 3: .claude/agents/ (8개 파일)

총 82개 파일이 3개의 독립적인 컨텍스트로 나뉩니다.

💡 도움말:
   통합 커밋은 전체 롤백과 코드 리뷰가 어려워질 수 있습니다.
   기본 정책(자동 분리)을 따르는 것을 권장합니다.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 커밋 전략을 선택하세요:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

> 1. 자동 분리 커밋 (기본 정책)
     각 그룹을 독립적으로 커밋합니다.
     ✅ 명확한 히스토리, 쉬운 리뷰, 선택적 롤백

  2. 통합 커밋
     모든 변경을 하나로 통합합니다.
     ⚠️ 롤백/리뷰 어려움, 버그 추적 어려움

  3. 취소
     커밋 프로세스를 종료합니다.
```

**User Actions:**
- **Auto-split** → Each group goes through 3-stage process independently
- **Unified commit** → All changes in one commit (with warning)
- **Cancel** → Exit

---

## Auto-Split Commit UI

For auto-split commits, apply 3-stage process per group:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[1/3] 그룹 1 커밋: .claude/skills/commit/
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📝 Step 1/3: 커밋 타입 선택

○ feat - 새로운 기능 추가
○ fix - 버그 수정
○ docs - 문서
...
```

Then proceed through Stages 2-3 and final confirmation for that group.

---

## Benefits of 3-Stage Approach

### User Experience

1. **Full control**: User decides every component
2. **Transparency**: Understand what goes into each part
3. **Educational**: Learn commit message best practices
4. **Flexible**: Can skip body/footer or use direct input
5. **Clear guidance**: Step-by-step with explanations

### Token Efficiency

1. **No 5-message generation**: Save ~60% tokens
2. **Pre-generate candidates once**: Reuse in metadata
3. **Load templates on-demand**: Only load needed stage

### Code Quality

1. **Better body content**: User selects relevant changes
2. **Appropriate detail**: User controls verbosity
3. **Accurate scope**: System detects, user confirms
4. **Proper footer**: Only add when truly needed

---

## Visual Design Principles

### Stage Headers

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 1/3: 커밋 타입 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

- Clear progress indicator (1/3, 2/3, 3/3)
- Emoji for visual distinction
- Horizontal lines for separation

### Selection Summary

Show context from previous stages:

```
선택한 타입: feat
감지된 스코프: spring-security-jwt
선택한 바디 항목: 3개
```

- Helps user maintain context
- Allows quick verification
- Enables informed decisions

### Final Confirmation Box

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 최종 커밋 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
{complete message}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

- Prominent display
- Complete message visible
- Safety check before commit

---

## Implementation Notes

### AskUserQuestion Tool

**Single-select vs Multi-select:**
- Stage 1 (Type): `multiSelect: false`
- Stage 2 (Body): `multiSelect: true` ⭐
- Stage 3 (Footer): `multiSelect: false`

**Structure:**
```json
{
  "questions": [{
    "question": "질문 내용",
    "header": "12자 이하 헤더",
    "multiSelect": true,  // Stage 2 only
    "options": [
      {
        "label": "항목 레이블",
        "description": "상세 설명"
      }
    ]
  }]
}
```

### Scope Detection

**Display to user in Stage 2:**
```
감지된 스코프: {detectedScope}
```

- Shows what system detected
- User can modify in final confirmation if needed
- Transparent process

---

## Usage in PROCESS.md

**Step 3 now uses 3 templates:**

```bash
# Stage 1: Type selection
cat .claude/skills/commit/../assets/templates/template-3-1-type-selection.md

# Stage 2: Body selection
cat .claude/skills/commit/../assets/templates/template-3-2-body-selection.md

# Stage 3: Footer selection
cat .claude/skills/commit/../assets/templates/template-3-3-footer-selection.md
```

**Load only when needed:**
- Token efficient
- Clear separation of concerns
- Easy to maintain

---

## Related Documents

- **[../PROCESS.md](../PROCESS.md)** - Step 3 uses 3-stage selection
  - Stage 1: template-3-1 (type)
  - Stage 2: template-3-2 (body)
  - Stage 3: template-3-3 (footer)
- **[../MESSAGE_GENERATION.md](../MESSAGE_GENERATION.md)** - Generation algorithms
  - Type detection algorithm
  - Body item generation (file-based, feature-based, hybrid)
  - Scope extraction algorithm
  - Header message generation
- **[../METADATA.md](../METADATA.md)** - Metadata structure
  - Pre-generated body candidates
  - User selections storage
- **[../RULES.md](../RULES.md)** - Validation rules
  - Format validation
- **[../EXAMPLES.md](../EXAMPLES.md)** - Complete examples
  - Real commit message examples

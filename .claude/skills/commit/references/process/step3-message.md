# Step 3: 3-Stage Message Composition

**Read metadata:**
```bash
# Read pre-analyzed data
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
# Use analysis.detectedType, analysis.detectedScope, analysis.bodyItemCandidates
```

### Overview: User Selects Header → Body → Footer

Guide user through 3 stages to build the commit message:

1. **Stage 1**: Select commit header from 5 pre-generated messages (추천 2 + 일반 3)
2. **Stage 2**: Select body items (multi-select from auto-generated candidates)
3. **Stage 3**: Select footer (none, issue reference, or breaking change)

**Benefits:**
- Pre-generated quality headers ensure accuracy
- User has full control through selection
- Refresh mechanism provides flexibility
- Direct input available as fallback

**Detailed algorithms:** See [message-generation.md](MESSAGE_GENERATION.md)

---

### Stage 1: Header Message Selection

**Template:** [../assets/templates/3-1-header-selection.md](../assets/templates/3-1-header-selection.md)

**Generate 5 header messages:**
- **추천 2개** (fixed): Best matches based on analysis
- **일반 3개** (refreshable): Alternative options

**Generation algorithm:**
```javascript
function generate5Headers(changes) {
  // Recommended 1: Optimal scope + type + message
  const recommended1 = generateOptimalHeader(changes);

  // Recommended 2: Strong alternative (different scope or type)
  const recommended2 = generateAlternative(changes, recommended1);

  // General 3-5: Variations (refreshable)
  const general = generateVariations(changes, [recommended1, recommended2]);

  return {
    recommended: [recommended1, recommended2],
    general: general.slice(0, 3)
  };
}
```

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 1/3: 헤더 메시지 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 헤더로 사용할 메시지를 선택하세요.
(추천 메시지는 변경사항을 가장 잘 설명합니다)
```

**Actions:**
1. Generate 5 header messages (추천 2 + 일반 3)
2. Display screen output
3. Call AskUserQuestion with options:
   - Option 1-4: Headers (추천 2개 marked with "(추천)")
   - "Other": Direct input (automatically added)
4. Handle user selection:
   - **Header selected** → Store selected header, proceed to Stage 2
   - **"다른 추천 리스트 보기" selected** → Regenerate 일반 3개, show again
   - **"Other" (직접 입력) selected** → Prompt for manual input, validate, proceed to Stage 2

**Example headers:**
```
1. docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경 (추천)
2. refactor(commit-skill): 메시지 생성 프로세스 재구성 (추천)
3. docs(MESSAGE_GENERATION.md): 헤더 5개 생성 전략으로 재작성
4. docs(.claude/skills): commit 스킬 문서 업데이트
```

**Note:** AskUserQuestion limits to 4 options, so show 추천 2 + 일반 2. On refresh, rotate through different 일반 options.

---

### Stage 2: Body Items Selection (Multi-Select with Pagination)

**User has selected header, now select body items.**

**Template:** [../assets/templates/3-2-body-selection.md](../assets/templates/3-2-body-selection.md)

**Core Principle:**
- ❌ 파일명 나열 (git log에 이미 있음)
- ✅ 작업 내용 설명 (무엇을 했는지)

**Generate body item candidates:**

Use metadata `analysis.bodyItemCandidates` (pre-generated in Step 1).

```javascript
function generateBodyItems(files, diff) {
  // Strategy: Feature-based (작업/기능 중심, 파일명 X)
  // See MESSAGE_GENERATION.md for detailed algorithm

  // 1. Analyze and group by feature/purpose
  const features = analyzeFeatures(files, diff);

  // 2. Generate items with score
  const items = features.map(feature => ({
    label: feature.description,        // 작업 내용 (파일명 X)
    description: feature.details,      // 상세 설명
    score: calculateScore(feature),    // 0-100
    relatedFiles: feature.files        // 참고용 (optional)
  }));

  // 3. Sort by score (high to low)
  return items.sort((a, b) => b.score - a.score);
}
```

**Score calculation:**
- 변경 라인 수 (40%)
- 파일 중요도 (30%): src/main > config > test
- 커밋 타입 관련성 (30%)

**Screen Output (with file reference):**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택 [페이지 1/3]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

변경된 파일 (10개, 참고용):
  [95⭐] UserService.java          (+152, -23)
  [90⭐] LoginController.java      (+87, -5)
  [85⭐] SecurityConfig.java       (+45, -12)
  [80] JwtUtil.java                (+120, -0)
  ...

💡 Score: 변경량(40%) + 중요도(30%) + 관련성(30%)
   ⭐ = Score 80 이상 (중요)

현재 선택: 0개

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
작업 내용 선택 (1-3번):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 본문에 포함할 작업 내용을 선택하세요.
- 스페이스바로 복수 선택 가능
- Score가 높을수록 중요한 작업
```

**Actions:**
1. Display file list with scores (참고용)
2. Show current page (3 items per page)
3. Call AskUserQuestion with pagination:
   - Options: 3 items + navigation ([다음 페이지]/[이전 페이지]/[선택 완료])
   - Multi-select enabled
4. Accumulate selections across pages
5. Store `selectedBodyItems` in memory for Stage 3

**Pagination flow:**
```
Page 1 (1-3) → [다음] → Page 2 (4-6) → [다음] → Page 3 (7-9)
                ↑                       ↑                ↓
             [이전] ←─────────────── [이전]      [선택 완료]
```

**Item format examples (Feature-based):**
```
[95⭐] 사용자 인증 로직 구현
[90⭐] 로그인 API 엔드포인트 추가
[85⭐] Spring Security 필터 체인 구성
[80] JWT 토큰 생성 및 검증 로직
```

**Final body output:**
```
- 사용자 인증 로직 구현
- 로그인 API 엔드포인트 추가
- Spring Security 필터 체인 구성
```

---

### Stage 3: Footer Selection

**Template:** [../assets/templates/3-3-footer-selection.md](../assets/templates/3-3-footer-selection.md)

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 3/3: 푸터 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

선택한 타입: {selectedType}
선택한 스코프: {detectedScope}
선택한 바디 항목: {selectedBodyItems.length}개

푸터를 추가할지 선택하세요.
대부분의 경우 푸터가 필요하지 않습니다.
```

**Actions:**
1. Display screen output with summary
2. Call AskUserQuestion with template JSON
   - Options: "푸터 없음" (추천), "Issue reference", "Breaking Change"
3. User selects one option
4. If "Issue reference" → Prompt for issue numbers
5. If "Breaking Change" → Prompt for description
6. Store `selectedFooter` in memory

**Footer formats:**
- No footer: (empty)
- Issue reference: `Closes #123, #456`
- Breaking Change: `BREAKING CHANGE: API 응답 형식 변경`

---

### Assemble Final Message

After 3 stages, assemble the final commit message:

```javascript
function assembleFinalMessage(selections) {
  const { type, scope, bodyItems, footer } = selections;

  // 1. Generate header message
  const headerMsg = generateHeaderMessage(type, scope, bodyItems);
  const header = `${type}(${scope}): ${headerMsg}`;

  // 2. Format body
  let body = '';
  if (bodyItems.length > 0 && bodyItems[0] !== '바디 없음') {
    body = '\n\n' + bodyItems.map(item => `- ${item.label}`).join('\n');
  }

  // 3. Add footer
  let footerSection = '';
  if (footer && footer !== '푸터 없음') {
    footerSection = '\n\n' + footer;
  }

  return header + body + footerSection;
}
```

**Header message generation:**
```javascript
function generateHeaderMessage(type, scope, bodyItems) {
  // Single item: extract action from item
  if (bodyItems.length === 1 && bodyItems[0] !== '바디 없음') {
    const item = bodyItems[0].label;
    if (item.includes(':')) {
      return item.split(':')[1].trim(); // "사용자 인증 로직 추가"
    }
  }

  // Multiple items or no items: use general description
  const verbs = {
    feat: '추가', fix: '수정', refactor: '개선',
    test: '추가', docs: '추가', style: '정리', chore: '업데이트'
  };

  const verb = verbs[type] || '변경';
  const theme = extractCommonTheme(bodyItems) || scope;

  return `${theme} ${verb}`;
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

**Display preview:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 생성된 커밋 메시지 미리보기:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
{complete_message}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

다음 단계: 최종 확인
```

Then proceed to Step 4 (final confirmation).

---

### Body Format Rules

**CRITICAL: Each body line MUST start with `-` (dash + space) and be on separate lines**

```
<type>(scope): <간단한 설명>

- 주요 변경사항 1
- 주요 변경사항 2
- 주요 변경사항 3
```

**Mandatory rules:**
- Each line starts with `- ` (dash + space)
- Each item on a new line (no comma-separated items)
- 5 lines or less
- File-based or feature-based grouping

**For detailed format rules and examples:**
- [message-generation.md](MESSAGE_GENERATION.md) - Complete generation algorithms and strategies
- [RULES.md - Body Guidelines](RULES.md#body-guidelines) - Validation rules

---

### User Actions Summary

**From Stage 3:**
- Complete 3 stages → Proceed to Step 4 (final confirmation with 4-final-confirmation)

**Alternative: Direct Input** (from any stage):
- User can select "Other" at any stage → Proceed to 5-direct-input (direct input)
- Useful if user wants to write message from scratch

---


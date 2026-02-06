# Commit Message Generation Strategy (3-Stage User Selection)

User builds commit message through 3 stages: header selection → body selection → footer selection.

---

## Overview

**3-Stage Selection Process:**

1. **Stage 1: Header Selection** - User selects from 5 pre-generated header messages
   - 추천 2개 (fixed, best matches)
   - 일반 3개 (refreshable alternatives)

2. **Stage 2: Body Selection** - User selects body items (multi-select)
   - Auto-generated 4-10 body item candidates
   - "바디 없음" for header-only commits
   - "다른 추천 리스트 보기" to regenerate

3. **Stage 3: Footer Selection** - User selects footer option
   - 푸터 없음 (recommended)
   - Issue reference
   - Breaking Change

**Benefits:**
- User has full control through selection
- Pre-generated options ensure quality
- Refresh mechanism provides flexibility
- Direct input available as fallback

---

## Stage 1: Header Message Generation

### Generate 5 Header Messages

**Strategy:**
- **추천 2개** (Recommended, fixed): Always shown, best matches
- **일반 3개** (General, refreshable): Alternatives, can be regenerated

### Recommended Message 1 (최우선 추천)

**Goal:** Most accurate and appropriate header

**Selection criteria:**
- **Scope accuracy** (40 points): Best matches main directory/module
- **Type correctness** (30 points): Matches primary change nature
- **Clarity** (20 points): Clear, concise expression
- **Convention** (10 points): Follows project naming patterns

**Algorithm:**
```javascript
function generateRecommended1(changes) {
  // Detect optimal scope
  const scope = detectOptimalScope(changes.files);
  // Module name preferred: "commit-skill", "spring-batch"

  // Detect primary type
  const type = detectPrimaryType(changes.diff);
  // Analyze: new features, bug fixes, refactoring, docs

  // Generate clear message
  const message = generateClearMessage(type, scope, changes);
  // Format: "{primary action} {brief description}"

  return `${type}(${scope}): ${message}`;
}
```

**Example:**
```
docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경
```

### Recommended Message 2 (차선 추천)

**Goal:** Strong alternative with different emphasis

**Variation strategies:**
1. **Different scope level**:
   - If #1 uses module → Use filename
   - If #1 uses filename → Use parent module

2. **Different type interpretation**:
   - If #1 is `docs` → Try `refactor` (structural change perspective)
   - If #1 is `feat` → Try `refactor` (improvement perspective)

3. **Different message angle**:
   - If #1 focuses on "what" → Focus on "why"
   - If #1 is general → Be more specific

**Example:**
```
refactor(commit-skill): 메시지 생성 프로세스를 사용자 선택 기반으로 재구성
```

### General Messages 3-5 (일반 메시지)

These are alternatives shown initially, but can be refreshed to show different options.

**Message 3: Scope Variation**
```javascript
function generateMessage3(changes, recommended) {
  // Try different scope level
  const alternativeScope = findAlternativeScope(changes.files);
  // e.g., "MESSAGE_GENERATION.md" (file) vs "commit-skill" (module)

  return `${recommended.type}(${alternativeScope}): ${recommended.message}`;
}
```

**Example:**
```
docs(MESSAGE_GENERATION.md): 3단계 선택 알고리즘으로 재작성
```

**Message 4: Expression Variation**
```javascript
function generateMessage4(changes, recommended) {
  // Try different message expression
  const variations = [
    generateConciseMessage(changes),  // More brief
    generateDetailedMessage(changes), // More descriptive
    generateActionMessage(changes)    // Action-oriented
  ];

  return `${recommended.type}(${recommended.scope}): ${pickBestVariation(variations)}`;
}
```

**Example:**
```
docs(commit-skill): 헤더 선택 기반 3단계 커밋 프로세스 도입
```

**Message 5: Type Alternative**
```javascript
function generateMessage5(changes) {
  // Interpret from different angle
  const alternativeType = findAlternativeType(changes);

  // Examples:
  // docs ↔ refactor (documentation vs structure)
  // feat ↔ refactor (new feature vs improvement)
  // chore ↔ feat (configuration vs feature)

  return `${alternativeType}(${scope}): ${generateMessage(alternativeType, changes)}`;
}
```

**Example:**
```
refactor(.claude/skills): 커밋 스킬 문서 및 프로세스 개선
```

### Refresh Logic (다른 추천 리스트 보기)

When user requests refresh, regenerate **일반 3개** while keeping **추천 2개** fixed.

**Algorithm:**
```javascript
function refreshHeaderMessages(fixedRecommended, allCandidates, shownIndices) {
  // Keep 추천 2개 unchanged
  const recommended = fixedRecommended; // [msg1, msg2]

  // Get next 3 candidates not yet shown
  const availableIndices = allCandidates
    .map((_, i) => i)
    .filter(i => !shownIndices.includes(i));

  // Pick next 3
  const nextIndices = availableIndices.slice(0, 3);
  const newGeneral = nextIndices.map(i => allCandidates[i]);

  // Update shown indices
  shownIndices.push(...nextIndices);

  // If all shown, wrap around
  if (availableIndices.length < 3) {
    shownIndices = []; // Reset
  }

  return {
    messages: [...recommended, ...newGeneral],
    shownIndices
  };
}
```

**Generate candidate pool in Step 1:**
```javascript
function generateAllHeaderCandidates(changes) {
  const candidates = [];

  // Generate variations
  const scopes = [
    detectModuleScope(changes.files),      // commit-skill
    detectFileScope(changes.files),        // MESSAGE_GENERATION.md
    detectParentScope(changes.files),      // .claude/skills
    detectSubdirScope(changes.files)       // commit/templates
  ];

  const types = ['docs', 'refactor', 'feat', 'chore'];

  const messages = [
    generatePrimaryMessage(changes),
    generateAlternativeMessage1(changes),
    generateAlternativeMessage2(changes),
    generateAlternativeMessage3(changes)
  ];

  // Combine to create 10-15 candidates
  scopes.forEach(scope => {
    types.forEach(type => {
      messages.forEach(message => {
        candidates.push(`${type}(${scope}): ${message}`);
      });
    });
  });

  // Score and rank
  candidates.forEach(c => c.score = scoreMessage(c, changes));
  candidates.sort((a, b) => b.score - a.score);

  return candidates.slice(0, 15); // Keep top 15
}
```

**Store in metadata:**
```json
{
  "analysis": {
    "headerCandidates": {
      "recommended": [
        "docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경",
        "refactor(commit-skill): 메시지 생성 프로세스 재구성"
      ],
      "general": [
        "docs(MESSAGE_GENERATION.md): 3단계 선택 알고리즘으로 재작성",
        "docs(.claude/skills): commit 스킬 문서 업데이트",
        "refactor(commit): 커밋 메시지 작성 프로세스 개선",
        "feat(commit-skill): 사용자 선택 기반 메시지 생성 기능 추가",
        "...more candidates..."
      ],
      "shownIndices": [0, 1, 2]
    }
  }
}
```

---

## Stage 2: Body Item Generation

### Generate 4-10 Body Item Candidates

**Strategies:**

1. **File-based** (1-3 files changed)
2. **Feature-based** (4+ files changed)
3. **Hybrid** (mix of both)

See [template-3-2-body-selection.md](templates/template-3-2-body-selection.md) for detailed algorithm.

**Example candidates:**
```
- MESSAGE_GENERATION.md: 헤더 5개 생성 전략으로 재작성
- PROCESS.md: Step 3을 3단계 선택 프로세스로 변경
- templates: 3개 새 템플릿 추가 (header, body, footer selection)
- SKILL.md: 코어 프롬프트를 헤더 선택 방식으로 업데이트
```

### Refresh Logic (다른 추천 리스트 보기)

Rotate between different grouping strategies:

```javascript
function refreshBodyItems(files, diff, currentStrategy) {
  const strategies = ['file-based', 'feature-based', 'hybrid'];
  const nextStrategy = strategies[(strategies.indexOf(currentStrategy) + 1) % 3];

  return generateBodyItems(files, diff, nextStrategy);
}
```

---

## Stage 3: Footer Selection

Simple selection, no refresh needed:
- 푸터 없음 (recommended)
- Issue reference
- Breaking Change

See [template-3-3-footer-selection.md](templates/template-3-3-footer-selection.md).

---

## Scope Extraction Algorithm

### Priority Order

1. **Module name** (preferred for multi-file changes)
2. **Filename** (for single file or small changes)
3. **Directory name** (fallback)

### Examples

**Module name:**
```
Input: .claude/skills/commit/SKILL.md, PROCESS.md, MESSAGE_GENERATION.md
Output: "commit-skill"
```

**Filename:**
```
Input: src/main/java/utils/DateUtils.java
Output: "DateUtils.java"
```

**Directory name:**
```
Input: src/auth/UserService.java, src/auth/LoginController.java
Output: "auth"
```

### Algorithm

```javascript
function detectOptimalScope(files) {
  // Single file
  if (files.length === 1) {
    return path.basename(files[0]);
  }

  // Find common directory
  const commonDir = findCommonDirectory(files);
  const dirName = path.basename(commonDir);

  // Enhance with context
  const enhancedScope = enhanceScope(dirName, files);

  return enhancedScope;
}

function enhanceScope(baseName, files) {
  // Common patterns
  const patterns = {
    'commit': 'commit-skill',
    'auth': 'spring-security' // if Spring project
  };

  return patterns[baseName] || baseName;
}
```

---

## Type Detection Algorithm

### Priority Order

1. **Test files only** → `test`
2. **Docs files only** → `docs`
3. **Bug fix patterns** → `fix`
4. **New feature patterns** → `feat`
5. **Refactoring patterns** → `refactor`
6. **Default** → `chore`

### Algorithm

```javascript
function detectPrimaryType(changes) {
  const { files, diff } = changes;

  // Check file types
  if (files.every(f => f.includes('test'))) return 'test';
  if (files.every(f => f.endsWith('.md'))) return 'docs';

  // Analyze diff content
  if (hasBugFixPatterns(diff)) return 'fix';
  if (hasNewFeaturePatterns(diff)) return 'feat';
  if (hasRefactoringPatterns(diff)) return 'refactor';

  return 'chore';
}

function hasBugFixPatterns(diff) {
  const patterns = [
    /fix\s+(bug|error|issue)/i,
    /resolve\s+#\d+/i,
    /patch\s+/i
  ];

  return patterns.some(p => p.test(diff));
}

function hasNewFeaturePatterns(diff) {
  const patterns = [
    /add\s+new/i,
    /implement\s+/i,
    /introduce\s+/i,
    /create\s+.*class/i
  ];

  return patterns.some(p => p.test(diff));
}

function hasRefactoringPatterns(diff) {
  const patterns = [
    /extract\s+(method|class)/i,
    /rename\s+/i,
    /move\s+.*to/i,
    /restructure/i
  ];

  return patterns.some(p => p.test(diff));
}
```

---

## Message Expression Generation

### Type-Specific Verbs

| Type | Korean Verbs | English Verbs |
|------|--------------|---------------|
| feat | 추가, 구현, 도입 | add, implement, introduce |
| fix | 수정, 해결 | fix, resolve |
| refactor | 개선, 재구성, 분리 | improve, restructure, extract |
| test | 추가, 개선 | add, improve |
| docs | 추가, 수정, 업데이트 | add, update, modify |
| style | 정리, 적용 | format, apply |
| chore | 업데이트, 변경 | update, change |

### Pattern: Verb + Object

**Algorithm:**
```javascript
function generateClearMessage(type, scope, changes) {
  const verb = selectVerb(type);
  const object = extractPrimaryObject(changes);

  return `${object} ${verb}`;
}

function selectVerb(type) {
  const verbs = {
    feat: ['추가', '구현', '도입'],
    fix: ['수정', '해결'],
    refactor: ['개선', '재구성', '분리'],
    docs: ['추가', '수정', '업데이트']
  };

  return verbs[type][0]; // Pick first as default
}

function extractPrimaryObject(changes) {
  // Analyze changed files to determine main subject
  // Examples:
  // - "JWT 인증 필터"
  // - "배치 재시도 로직"
  // - "커밋 메시지 생성 방식"

  return analyzeChanges(changes);
}
```

---

## Complete Example

### Input (Changes)

```
Files:
- .claude/skills/commit/MESSAGE_GENERATION.md (860 lines changed)
- .claude/skills/commit/PROCESS.md (289 lines changed)
- .claude/skills/commit/SKILL.md (22 lines changed)
- .claude/skills/commit/templates/template-3-1-header-selection.md (new file)
- .claude/skills/commit/templates/template-3-2-body-selection.md (163 lines added)
- .claude/skills/commit/templates/template-3-3-footer-selection.md (192 lines added)
```

### Generated Headers (5개)

**추천 2개 (고정):**
```
1. docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경 (추천)
2. refactor(commit-skill): 메시지 생성 프로세스를 사용자 선택 기반으로 재구성 (추천)
```

**일반 3개 (새로고침 가능):**
```
3. docs(MESSAGE_GENERATION.md): 헤더 5개 생성 전략으로 재작성
4. docs(.claude/skills): commit 스킬 문서 및 템플릿 업데이트
5. feat(commit-skill): 사용자 선택 기반 메시지 생성 기능 추가
```

### Generated Body Items

```
- MESSAGE_GENERATION.md: 헤더 5개 생성 전략으로 재작성
- PROCESS.md: Step 3을 3단계 선택 프로세스로 변경
- templates: 3개 새 템플릿 추가 (header, body, footer selection)
- SKILL.md: 코어 프롬프트를 헤더 선택 방식으로 업데이트
- 바디 없음 (헤더만 사용)
```

### Final Message (User Selections)

**Selected:**
- Header: #1 (docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경)
- Body: Items 1, 2, 3
- Footer: 푸터 없음

**Assembled:**
```
docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경

- MESSAGE_GENERATION.md: 헤더 5개 생성 전략으로 재작성
- PROCESS.md: Step 3을 3단계 선택 프로세스로 변경
- templates: 3개 새 템플릿 추가 (header, body, footer selection)
```

---

## Validation Rules

**Header format:**
```regex
^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$
```

**Body format:**
- Each line starts with `- ` (dash + space)
- Maximum 5 lines
- Each line 1-2 lines long

**Footer format:**
- `Closes #\d+(, #\d+)*`
- `Fixes #\d+(, #\d+)*`
- `BREAKING CHANGE: .+`

---

## Related Documents

- **[PROCESS.md](PROCESS.md)** - Step 3 execution with 3 stages
- **[templates/template-3-1-header-selection.md](templates/template-3-1-header-selection.md)** - Stage 1
- **[templates/template-3-2-body-selection.md](templates/template-3-2-body-selection.md)** - Stage 2
- **[templates/template-3-3-footer-selection.md](templates/template-3-3-footer-selection.md)** - Stage 3
- **[METADATA.md](METADATA.md)** - Metadata structure
- **[RULES.md](RULES.md)** - Validation rules
- **[EXAMPLES.md](EXAMPLES.md)** - Complete examples

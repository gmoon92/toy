# Commit Message Generation Strategy (3-Stage User Selection)

User builds commit message through 3 stages: header selection → body selection → footer selection.

---

## Overview

**3-Stage Selection Process:**

1. **Stage 1: Header Selection** - User selects from 5 pre-generated header messages
   - Recommended 2 (fixed, best matches)
   - General 3 (refreshable alternatives)

2. **Stage 2: Body Selection** - User selects body items (multi-select)
   - Auto-generated 4-10 body item candidates
   - "No body" for header-only commits
   - "Show other recommendations" to regenerate

3. **Stage 3: Footer Selection** - User selects footer option
   - No footer (recommended)
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
- **Recommended 2** (fixed): Always shown, best matches
- **General 3** (refreshable): Alternatives, can be regenerated

### Recommended Message 1 (Top Priority)

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
docs(commit-skill): change commit message generation to 3-stage selection
```

### Recommended Message 2 (Second Priority)

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
refactor(commit-skill): restructure message generation process to user selection-based
```

### General Messages 3-5

These are alternatives shown initially, but can be refreshed to show different options.

**Message 3: Scope Variation**
```javascript
function generateMessage3(changes, recommended) {
  // Try different scope level
  const alternativeScope = findAlternativeScope(changes.files);
  // e.g., "generation/header.md" (file) vs "commit-skill" (module)

  return `${recommended.type}(${alternativeScope}): ${recommended.message}`;
}
```

**Example:**
```
docs(generation/header.md): rewrite with 3-stage selection algorithm
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
docs(commit-skill): introduce 3-stage commit process with header selection
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
refactor(.claude/skills): improve commit skill documentation and process
```

### Refresh Logic (Show Other Recommendations)

When user requests refresh, regenerate **General 3** while keeping **Recommended 2** fixed.

**Algorithm:**
```javascript
function refreshHeaderMessages(fixedRecommended, allCandidates, shownIndices) {
  // Keep Recommended 2 unchanged
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
    detectFileScope(changes.files),        // generation/header.md
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
        "docs(commit-skill): change commit message generation to 3-stage selection",
        "refactor(commit-skill): restructure message generation process"
      ],
      "general": [
        "docs(generation/header.md): rewrite with 3-stage selection algorithm",
        "docs(.claude/skills): update commit skill documentation",
        "refactor(commit): improve commit message creation process",
        "feat(commit-skill): add user selection-based message generation feature",
        "...more candidates..."
      ],
      "shownIndices": [0, 1, 2]
    }
  }
}
```

---


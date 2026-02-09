# Step 3: 3-Stage Message Composition

**Read metadata:**
```bash
# Read pre-analyzed data
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
# Use analysis.detectedType, analysis.detectedScope, analysis.bodyItemCandidates
```

### Overview: User Selects Header → Body → Footer

Guide user through 3 stages to build the commit message:

1. **Stage 1**: Select commit header from 5 pre-generated messages (Recommended 2 + General 3)
2. **Stage 2**: Select body items (multi-select from auto-generated candidates)
3. **Stage 3**: Select footer (none, issue reference, or breaking change)

**Benefits:**
- Pre-generated quality headers ensure accuracy
- User has full control through selection
- Refresh mechanism provides flexibility
- Direct input available as fallback

**Detailed algorithms:** See [generation/](../generation/) (header.md, body.md, footer.md) and [scripts/](../../scripts/) (detect_scope.js, detect_type.js, generate_headers.js, generate_body_items.js)

---

### Stage 1: Header Message Selection

**Template:** [3-1-header-selection.md](../../templates/3-1-header-selection.md)

**Generate 5 header messages:**
- **Recommended 2** (fixed): Best matches based on analysis
- **General 3** (refreshable): Alternative options

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
📝 Step 1/3: Select Header Message
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Select a commit header message.
(Recommended messages best describe the changes)
```

**Actions:**
1. Generate 5 header messages (Recommended 2 + General 3)
2. Display screen output
3. Call AskUserQuestion with options:
   - Option 1-4: Headers (Recommended 2 marked with "(Recommended)")
   - "Other": Direct input (automatically added)
4. Handle user selection:
   - **Header selected** → Store selected header, proceed to Stage 2
   - **"Show other recommendations" selected** → Regenerate General 3, show again
   - **"Other" (direct input) selected** → Prompt for manual input, validate, proceed to Stage 2

**Example headers:**
```
1. docs(commit-skill): change commit message generation to 3-stage selection (Recommended)
2. refactor(commit-skill): restructure message generation process (Recommended)
3. docs(generation/header.md): rewrite with 5 header generation strategy
4. docs(.claude/skills): update commit skill documentation
```

**Note:** AskUserQuestion limits to 4 options, so show Recommended 2 + General 2. On refresh, rotate through different General options.

---

### Stage 2: Body Items Selection (Multi-Select with Pagination)

**User has selected header, now select body items.**

**Template:** [3-2-body-selection.md](../../templates/3-2-body-selection.md)

**Core Principle:**
- ❌ List filenames (already in git log)
- ✅ Describe work done (what was accomplished)

**Generate body item candidates:**

Use metadata `analysis.bodyItemCandidates` (pre-generated in Step 1).

```javascript
function generateBodyItems(files, diff) {
  // Strategy: Feature-based (task/feature focused, not filenames)
  // See scripts/generation/generate_body_items.js for implementation

  // 1. Analyze and group by feature/purpose
  const features = analyzeFeatures(files, diff);

  // 2. Generate items with score
  const items = features.map(feature => ({
    label: feature.description,        // Work description (not filename)
    description: feature.details,      // Detailed explanation
    score: calculateScore(feature),    // 0-100
    relatedFiles: feature.files        // For reference (optional)
  }));

  // 3. Sort by score (high to low)
  return items.sort((a, b) => b.score - a.score);
}
```

**Score calculation:**
- Lines changed (40%)
- File importance (30%): src/main > config > test
- Commit type relevance (30%)

**Screen Output (with file reference):**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: Select Body Items [Page 1/3]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Modified files (10 files, for reference):
  [95⭐] UserService.java          (+152, -23)
  [90⭐] LoginController.java      (+87, -5)
  [85⭐] SecurityConfig.java       (+45, -12)
  [80] JwtUtil.java                (+120, -0)
  ...

💡 Score: Changes(40%) + Importance(30%) + Relevance(30%)
   ⭐ = Score 80+ (Important)

Currently selected: 0 items

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Select work items (1-3):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Select work items to include in commit body.
- Multi-select with spacebar
- Higher score = more important work
```

**Actions:**
1. Display file list with scores (for reference)
2. Show current page (3 items per page)
3. Call AskUserQuestion with pagination:
   - Options: 3 items + navigation ([Next Page]/[Previous Page]/[Done])
   - Multi-select enabled
4. Accumulate selections across pages
5. Store `selectedBodyItems` in memory for Stage 3

**Pagination flow:**
```
Page 1 (1-3) → [Next] → Page 2 (4-6) → [Next] → Page 3 (7-9)
                ↑                       ↑                ↓
             [Prev] ←─────────────── [Prev]        [Done]
```

**Item format examples (Feature-based):**
```
[95⭐] Implement user authentication logic
[90⭐] Add login API endpoint
[85⭐] Configure Spring Security filter chain
[80] JWT token generation and validation logic
```

**Final body output:**
```
- Implement user authentication logic
- Add login API endpoint
- Configure Spring Security filter chain
```

---

### Stage 3: Footer Selection

**Template:** [3-3-footer-selection.md](../../templates/3-3-footer-selection.md)

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 3/3: Select Footer
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Selected type: {selectedType}
Detected scope: {detectedScope}
Selected body items: {selectedBodyItems.length} items

Choose whether to add a footer.
In most cases, a footer is not needed.
```

**Actions:**
1. Display screen output with summary
2. Call AskUserQuestion with template JSON
   - Options: "No footer" (recommended), "Issue reference", "Breaking Change"
3. User selects one option
4. If "Issue reference" → Prompt for issue numbers
5. If "Breaking Change" → Prompt for description
6. Store `selectedFooter` in memory

**Footer formats:**
- No footer: (empty)
- Issue reference: `Closes #123, #456`
- Breaking Change: `BREAKING CHANGE: API response format changed`

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
  if (bodyItems.length > 0 && bodyItems[0] !== 'No body') {
    body = '\n\n' + bodyItems.map(item => `- ${item.label}`).join('\n');
  }

  // 3. Add footer
  let footerSection = '';
  if (footer && footer !== 'No footer') {
    footerSection = '\n\n' + footer;
  }

  return header + body + footerSection;
}
```

**Header message generation:**
```javascript
function generateHeaderMessage(type, scope, bodyItems) {
  // Single item: extract action from item
  if (bodyItems.length === 1 && bodyItems[0] !== 'No body') {
    const item = bodyItems[0].label;
    if (item.includes(':')) {
      return item.split(':')[1].trim(); // "add user authentication logic"
    }
  }

  // Multiple items or no items: use general description
  const verbs = {
    feat: 'add', fix: 'fix', refactor: 'improve',
    test: 'add', docs: 'add', style: 'format', chore: 'update'
  };

  const verb = verbs[type] || 'change';
  const theme = extractCommonTheme(bodyItems) || scope;

  return `${verb} ${theme}`;
}
```

**Example assembled message:**
```
feat(spring-security-jwt): add JWT authentication filter

- UserService.java: add user authentication logic
- LoginController.java: implement login API endpoint
- SecurityConfig.java: configure Spring Security

Closes #123
```

**Display preview:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Generated Commit Message Preview:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
{complete_message}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Next step: Final confirmation
```

Then proceed to Step 4 (final confirmation).

---

### Body Format Rules

**CRITICAL: Each body line MUST start with `-` (dash + space) and be on separate lines**

```
<type>(scope): <brief description>

- Main change 1
- Main change 2
- Main change 3
```

**Mandatory rules:**
- Each line starts with `- ` (dash + space)
- Each item on a new line (no comma-separated items)
- 5 lines or less
- File-based or feature-based grouping

**For detailed format rules and examples:**
- [generation/header.md](../generation/header.md) - Header generation algorithm
- [generation/body.md](../generation/body.md) - Body generation strategies
- [generation/footer.md](../generation/footer.md) - Footer options
- [scripts/algorithms/](../../scripts/algorithms/) - Scope/Type detection algorithms (detect_scope.js, detect_type.js)
- [scripts/generation/](../../scripts/generation/) - Message generation scripts (generate_headers.js, generate_body_items.js)
- [validation/rules.md](../validation/rules.md) - Validation rules including body guidelines

---

### User Actions Summary

**From Stage 3:**
- Complete 3 stages → Proceed to Step 4 (final confirmation with 4-final-confirmation)

**Alternative: Direct Input** (from any stage):
- User can select "Other" at any stage → Proceed to 5-direct-input (direct input)
- Useful if user wants to write message from scratch

---


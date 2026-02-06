# Commit Message UI/UX Design

User-friendly 3-stage message composition with clear guidance and confirmation.

This directory contains individual UI templates for user interactions during the commit process, along with comprehensive UI/UX design guidelines.

---

## Template Files

Each template is separated into its own file for efficient context loading:

- **[1-tidy-first.md](../../templates/1-tidy-first.md)** - Tidy First violation detection
- **[2-logical-independence.md](../../templates/2-logical-independence.md)** - Logical independence detection
- **[3-1-header-selection.md](../../templates/3-1-header-selection.md)** - Stage 1: Header message selection (Recommended 2 + General 3)
- **[3-2-body-selection.md](../../templates/3-2-body-selection.md)** - Stage 2: Body item multi-select
- **[3-3-footer-selection.md](../../templates/3-3-footer-selection.md)** - Stage 3: Footer selection
- **[4-final-confirmation.md](../../templates/4-final-confirmation.md)** - Final confirmation
- **[5-direct-input.md](../../templates/5-direct-input.md)** - Message editing (direct input)

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

1. **Stage 1**: Select commit header from 5 pre-generated messages (Recommended 2 + General 3)
2. **Stage 2**: Select body items (multi-select from auto-generated candidates)
3. **Stage 3**: Select footer (none, issue reference, or breaking change)

**Benefits:**
- **User control**: Full transparency over each component
- **Educational**: Learn what goes into each part
- **Flexible**: Can skip stages or use direct input
- **Efficient**: No need to generate 5 complete messages

---

### Stage 1: Header Message Selection

**Template:** [../templates/3-1-header-selection.md](../../templates/3-1-header-selection.md)

**Screen:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 1/3: Select Header Message
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Select a commit header message.
(Recommended messages best describe the changes)

○ docs(commit-skill): change commit message generation to 3-stage selection (Recommended)
○ refactor(commit-skill): restructure message generation process (Recommended)
○ docs(generation/header.md): rewrite with 5 header generation strategy
○ docs(.claude/skills): update commit skill documentation
```

**User Actions:**
- Select one of 4 headers → Proceed to Stage 2
- Select "Show other recommendations" → Regenerate General 3, show again
- Select "Other" (direct input) → Manual header input, proceed to Stage 2

**Generation strategy:**
- **Recommended 2** (fixed): Best matches, always shown
- **General 3** (refreshable): Alternatives, rotate on refresh

---

### Stage 2: Body Items Selection (Multi-Select with Pagination) ⭐ Core Feature

**Template:** [../templates/3-2-body-selection.md](../../templates/3-2-body-selection.md)

**Core Principle:**
- ❌ List filenames (already shown in git log)
- ✅ Describe work done (what was accomplished)

**System automatically generates 10-15 feature-based candidates** with score:

**Example screen:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: Select Body Items [Page 1/3]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Modified files (10 files, for reference):
  [95⭐] UserService.java          (+152, -23)
  [90⭐] LoginController.java      (+87, -5)
  [85⭐] SecurityConfig.java       (+45, -12)
  ...

💡 Score: Changes(40%) + Importance(30%) + Relevance(30%)
   ⭐ = Score 80+ (Important)

Currently selected: 0 items

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Select work items (1-3):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

◯ [95⭐] Implement user authentication logic
◯ [90⭐] Add login API endpoint
◯ [85⭐] Configure Spring Security filter chain
◯ [Next Page]
```

**Item Generation Strategy:**

**Feature-based (recommended, default strategy):**
```
[{score}⭐] {work description}
Example: [95⭐] Implement user authentication logic
```

**Score calculation:**
- Lines changed (40%)
- File importance (30%): src/main > config > test
- Commit type relevance (30%)

**Pagination:**
- Generate 10-15 candidates
- Show 3 items per page
- Navigation: [Next Page], [Previous Page], [Done]
- Selections accumulate across pages

**User Actions:**
- Select 1+ items → Add to selection, continue pagination
- Select "[Next Page]" → Show next page
- Select "[Previous Page]" → Show previous page
- Select "[Done]" → Proceed to Stage 3
- Select "No body" → Header-only commit, proceed to Stage 3
- Select "Other" (direct input) → Manual body input, proceed to Stage 3

**Assembled body format:**
```
- Implement user authentication logic
- Add login API endpoint
- Configure Spring Security filter chain
```

**Note:** Filenames are visible in git log; body focuses on work description

---

### Stage 3: Footer Selection

**Template:** [../templates/3-3-footer-selection.md](../../templates/3-3-footer-selection.md)

**Screen:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 3/3: Select Footer
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Selected type: feat
Detected scope: spring-security-jwt
Selected body items: 3 items

Choose whether to add a footer.
In most cases, a footer is not needed.

○ No footer (recommended)
○ Add issue reference
○ Breaking Change
```

**User Action:**
- Select "No footer" → No footer, proceed to final confirmation
- Select "Issue reference" → Prompt for issue numbers → Proceed to final confirmation
- Select "Breaking Change" → Prompt for description → Proceed to final confirmation

**Footer formats:**
```
Closes #123, #456
Fixes #789
BREAKING CHANGE: API response format changed
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
  if (bodyItems.length > 0 && bodyItems[0] !== 'No body') {
    body = '\n\n' + bodyItems.map(item => `- ${item.label}`).join('\n');
  }

  // Add footer
  let footerSection = '';
  if (footer && footer !== 'No footer') {
    footerSection = '\n\n' + footer;
  }

  return header + body + footerSection;
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

**Message preview:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Generated Commit Message Preview:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
feat(spring-security-jwt): add JWT authentication filter

- UserService.java: add user authentication logic
- LoginController.java: implement login API endpoint
- SecurityConfig.java: configure Spring Security

Closes #123
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Next step: Final confirmation
```

Then proceed to 4-final-confirmation (final confirmation).

---

## Final Confirmation

**Template:** [../templates/4-final-confirmation.md](../../templates/4-final-confirmation.md)

**Screen:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Final Commit Message:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
feat(spring-security-jwt): add JWT authentication filter

- UserService.java: add user authentication logic
- LoginController.java: implement login API endpoint
- SecurityConfig.java: configure Spring Security
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Do you want to commit with this message?

1. Approve - Execute commit
2. Modify - Edit message
3. Cancel - Exit process
```

**User Actions:**
- **Approve** → Execute git commit
- **Modify** → Return to Stage 1 or direct input (5-direct-input)
- **Cancel** → Exit process

---

## Policy Selection UI (Logical Independence Detected)

**Template:** [../templates/2-logical-independence.md](../../templates/2-logical-independence.md)

When multiple independent groups are detected:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ Logically Independent Changes Detected!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Detected groups:
  Group 1: .claude/skills/commit/ (4 files)
  Group 2: ai/docs/claude/ (70 files)
  Group 3: .claude/agents/ (8 files)

Total 82 files divided into 3 independent contexts.

💡 Help:
   Unified commits can make full rollback and code review difficult.
   Following the default policy (auto-split) is recommended.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 Select Commit Strategy:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

> 1. Auto-split commits (default policy)
     Commit each group independently.
     ✅ Clear history, easy review, selective rollback

  2. Unified commit
     Integrate all changes into one commit.
     ⚠️ Difficult rollback/review, hard to track bugs

  3. Cancel
     Exit commit process.
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
[1/3] Group 1 Commit: .claude/skills/commit/
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📝 Step 1/3: Select Commit Type

○ feat - Add new feature
○ fix - Bug fix
○ docs - Documentation
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
📝 Step 1/3: Select Commit Type
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

- Clear progress indicator (1/3, 2/3, 3/3)
- Emoji for visual distinction
- Horizontal lines for separation

### Selection Summary

Show context from previous stages:

```
Selected type: feat
Detected scope: spring-security-jwt
Selected body items: 3 items
```

- Helps user maintain context
- Allows quick verification
- Enables informed decisions

### Final Confirmation Box

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Final Commit Message:
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
    "question": "Question text",
    "header": "12 char header",
    "multiSelect": true,  // Stage 2 only
    "options": [
      {
        "label": "Item label",
        "description": "Detailed description"
      }
    ]
  }]
}
```

### Scope Detection

**Display to user in Stage 2:**
```
Detected scope: {detectedScope}
```

- Shows what system detected
- User can modify in final confirmation if needed
- Transparent process

---

## Usage in PROCESS.md

**Step 3 now uses 3 templates:**

```bash
# Stage 1: Type selection
cat .claude/skills/commit/../templates/3-1-type-selection.md

# Stage 2: Body selection
cat .claude/skills/commit/../templates/3-2-body-selection.md

# Stage 3: Footer selection
cat .claude/skills/commit/../templates/3-3-footer-selection.md
```

**Load only when needed:**
- Token efficient
- Clear separation of concerns
- Easy to maintain

---

## Related Documents

- **[process/step3-message.md](../process/step3-message.md)** - 3-stage message composition
  - How UI templates are used in Step 3
  - Template loading sequence
- **[generation/header.md](../generation/header.md)** - Header generation for Stage 1
  - Algorithm used in header selection template
- **[generation/body.md](../generation/body.md)** - Body generation for Stage 2
  - Feature-based candidates in body selection template
- **[generation/footer.md](../generation/footer.md)** - Footer options for Stage 3
  - Footer selection template design
- **[metadata.md](metadata.md)** - Metadata structure
  - Pre-generated body candidates
  - User selections storage
- **[validation/rules.md](../validation/rules.md)** - Validation rules
  - Format validation
- **[examples.md](examples.md)** - Complete examples
  - Real commit message examples

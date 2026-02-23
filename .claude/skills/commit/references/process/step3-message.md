# Step 3: 3-Stage Message Composition

Guide user through 3 stages to build the commit message.

---

## Overview: User Selects Header → Body → Footer

**3-Stage Selection Process:**

1. **Stage 1**: Select commit header from 5 messages per [header.md](../generation/header.md)
2. **Stage 2**: Select body items (multi-select) per [body.md](../generation/body.md)
3. **Stage 3**: Select footer per [footer.md](../generation/footer.md) (none, issue reference, or breaking change)

**Benefits:**
- Policy-based generation ensures consistency
- User has full control through selection
- Refresh mechanism provides flexibility
- Direct input available as fallback

**Detailed guidance:** See [generation/](../generation/) (header.md, body.md, footer.md)

---

## Stage 1: Header Message Selection

**Template:** [3-1-header-selection.md](../../templates/3-1-header-selection.md)

**Generate 5 header messages per [header.md](../generation/header.md):**
- **Recommended 2**: Top suggestions following policy
- **General 3**: Alternative variations

**Generation approach per policy:**
1. Analyze git diff data
2. Infer type (feat/fix/refactor/docs/test/style/chore)
3. Infer scope (module/file/directory)
4. Generate 5 messages with different perspectives

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 1/3: Select Header Message
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Select a commit header message.
(Recommended messages best describe the changes)
```

**Actions:**
1. Generate 5 header messages per [header.md](../generation/header.md)
2. Display screen output
3. Call AskUserQuestion with options:
   - Option 1-4: Headers (Recommended 2 marked with "(Recommended)")
   - "Other": Direct input (automatically added)
4. Handle user selection:
   - **Header selected** → Store selected header, proceed to Stage 2
   - **"Show other recommendations" selected** → Generate 3 new variations per policy
   - **"Other" (direct input) selected** → Prompt for manual input, validate per [rules.md](../validation/rules.md), proceed to Stage 2

**Example headers:**
```
1. docs(commit-skill): change commit message generation to 3-stage selection (Recommended)
2. refactor(commit-skill): restructure message generation process (Recommended)
3. docs(generation/header.md): rewrite with 5 header generation strategy
4. docs(.claude/skills): update commit skill documentation
```

**Note:** AskUserQuestion limits to 4 options, so show Recommended 2 + General 2. On refresh, generate new variations per [header.md](../generation/header.md).

---

## Stage 2: Body Items Selection (Multi-Select)

**User has selected header, now select body items.**

**Template:** [3-2-body-selection.md](../../templates/3-2-body-selection.md)

**Core Principle:**
- ❌ List filenames (already in git log)
- ✅ Describe work done (what was accomplished)

**Generate body item candidates per [body.md](../generation/body.md):**

1. Group related changes by logical purpose
2. Identify what work was done
3. Create clear, natural descriptions
4. Present in order of significance (no mechanical scoring)

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: Select Body Items
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Select work items to include in commit body.
(Multi-select with spacebar)
```

**Actions:**
1. Generate 5-10 feature-based body item candidates per [body.md](../generation/body.md)
2. Display candidates
3. Call AskUserQuestion with multi-select enabled
4. User selects items (or "No body")
5. Store `selectedBodyItems` for Stage 3

**Item format examples (Feature-based, NO scores):**
```
□ Implement user authentication logic
□ Add login API endpoint
□ Configure Spring Security filter chain
□ JWT token generation and validation logic
□ No body
```

**Final body output:**
```
- Implement user authentication logic
- Add login API endpoint
- Configure Spring Security filter chain
```

---

## Stage 3: Footer Selection

**Template:** [3-3-footer-selection.md](../../templates/3-3-footer-selection.md)

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 3/3: Select Footer
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Selected header: {selectedHeader}
Selected body items: {selectedBodyItems.length} items

Choose whether to add a footer.
In most cases, a footer is not needed.
```

**Actions:**
1. Display screen output with summary
2. Call AskUserQuestion with options:
   - "No footer" (recommended)
   - "Issue reference"
   - "Breaking Change"
3. User selects one option
4. If "Issue reference" → Prompt for issue numbers
5. If "Breaking Change" → Prompt for description
6. Store `selectedFooter`

**Footer formats:**
- No footer: (empty)
- Issue reference: `Closes #123, #456`
- Breaking Change: `BREAKING CHANGE: API response format changed`

---

## Assemble Final Message

After 3 stages, assemble the final commit message:

**Format:**
```
<type>(scope): <message>

- Body item 1
- Body item 2
- Body item 3

Footer (if any)
```

**Example:**
```
feat(spring-security-jwt): add JWT authentication filter

- Implement user authentication logic
- Add login API endpoint
- Configure Spring Security filter chain

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

## Body Format Rules

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
- Feature-based or file-based grouping

**For detailed format rules and examples:**
- [generation/header.md](../generation/header.md) - Header generation strategy
- [generation/body.md](../generation/body.md) - Body generation approach
- [generation/footer.md](../generation/footer.md) - Footer options
- [validation/rules.md](../validation/rules.md) - Validation rules

---

## User Actions Summary

**From Stage 3:**
- Complete 3 stages → Proceed to Step 4 (final confirmation)

**Alternative: Direct Input** (from any stage):
- User can select "Other" at any stage → Proceed to direct input
- Useful if user wants to write message from scratch

---

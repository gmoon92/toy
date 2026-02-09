# Modifying Existing Skills

Guide for refactoring and improving existing Claude Code skills.

---

## When to Modify a Skill

### Common Triggers

**Performance Issues:**
- Skill loads too much context upfront
- Takes too long to execute
- Wastes tokens on unnecessary content

**Maintenance Burden:**
- Difficult to update algorithms or logic
- Changes require modifying multiple files
- Code duplicated across documentation

**Behavior Problems:**
- Claude doesn't follow instructions consistently
- Skill activates when it shouldn't (or vice versa)
- Outputs are unpredictable or vary

**Feature Additions:**
- New use cases emerge
- Users request additional functionality
- Related workflows need integration

---

## Types of Modifications

### 1. Token Optimization

**Problem:** Skill loads everything upfront

**Solution:** Implement progressive disclosure

**Steps:**
1. Analyze which sections are always needed vs conditional
2. Break SKILL.md into phase-based loading
3. Move detailed content to references
4. Add "When to Load References" section

**Example:**

**Before:**
```markdown
## PDF Processing

[5000 lines of detailed PDF processing instructions, examples, edge cases, troubleshooting, etc.]
```

**After:**
```markdown
## PDF Processing

Quick overview (50 lines)

### When to Load References
- **Phase 1**: [extraction.md](references/extraction.md) - Text extraction only
- **Phase 2**: [forms.md](references/forms.md) - Form filling (conditional)
- **Phase 3**: [advanced.md](references/advanced.md) - Complex operations (conditional)
```

**Impact:** 60-80% token reduction

### 2. Script Extraction

**Problem:** Inline code in documentation

**Solution:** Extract to executable scripts

See [script-extraction.md](script-extraction.md) for detailed guide.

**Quick checklist:**
- [ ] Identify all inline code blocks (bash, JavaScript, Python)
- [ ] Extract to scripts organized by responsibility
- [ ] Add `EXECUTE_SCRIPT:` directives
- [ ] Create scripts/README.md documentation
- [ ] Remove inline code from documentation

### 3. Improving Determinism

**Problem:** Claude interprets instructions differently each time

**Solution:** Use stronger directives and explicit constraints

**Weak directives:**
```markdown
You should use the validation script to check the output.
```

**Strong directives:**
```markdown
**MANDATORY:** ALWAYS use validation script before proceeding.

\```bash
# EXECUTE_SCRIPT: scripts/validation/validate.sh
./scripts/validation/validate.sh < output.json
\```

**CRITICAL:**
- DO NOT proceed if validation fails
- NEVER skip validation step
- ALWAYS use the script (DO NOT inline commands)
\```
```

**Keywords to use:**
- `MANDATORY:` - Required behavior
- `EXECUTE_SCRIPT:` - Script reference marker
- `DO NOT` - Explicit prohibition
- `ALWAYS` - Enforcement
- `NEVER` - Strong prohibition
- `CRITICAL:` - Highest priority
- `IMPORTANT:` - Significant note

### 4. Scope Refinement

**Problem:** Skill tries to do too much (or too little)

**Solution:** Clarify boundaries

**Add explicit scope:**
```markdown
**Scope:**
- ✅ What this skill does
- ✅ What this skill handles
- ❌ Never: What this skill avoids
- ❌ Never: Out of scope operations
```

**Update description:**
```yaml
# Before
description: Helps with documents

# After
description: Creates and edits Word documents (.docx). Use when working with .docx files, not PDFs or spreadsheets.
```

### 5. Activation Tuning

**Problem:** Skill doesn't activate when it should (or activates incorrectly)

**Solution:** Improve trigger keywords in description

**Weak description:**
```yaml
description: Helps with commits
```

**Strong description:**
```yaml
description: Analyzes git changes and generates commit messages following project conventions. Use when creating commits or when user mentions staging changes, commit messages, or git commits.
```

**Tips:**
- Include all relevant keywords users might say
- Mention file types or tools explicitly
- Describe both what AND when to use

---

## Refactoring Workflow

### Step 1: Assess Current State

**Analyze the skill:**
```bash
# Check file sizes
find skill-name -name "*.md" -exec wc -l {} \; | sort -rn

# Find inline code blocks
grep -r "^\`\`\`bash" skill-name/
grep -r "^\`\`\`javascript" skill-name/
grep -r "^\`\`\`python" skill-name/

# Check token count (rough estimate)
cat skill-name/SKILL.md | wc -w
```

**Red flags:**
- SKILL.md over 500 lines
- Multiple large inline code blocks
- All content loaded upfront (no phase-based loading)
- Weak directive language ("you can", "consider", "might")

### Step 2: Identify Problems

**Create a problem list:**

Example:
```
Problems with commit skill:
1. SKILL.md has 15+ inline bash scripts (~2000 tokens wasted)
2. Algorithm code mixed with documentation
3. No phase-based loading (loads everything upfront)
4. Claude sometimes reconstructs scripts instead of using files
5. Changes to algorithms require updating multiple docs
```

**Prioritize:**
- High impact, low effort → Do first
- High impact, high effort → Plan carefully
- Low impact → Defer or skip

### Step 3: Plan Changes

**For each problem, determine:**
- What needs to change
- Which files affected
- Backward compatibility concerns
- Testing requirements

**Document the plan:**
```markdown
## Refactoring Plan

### 1. Extract Inline Scripts
- Affected: step1-analysis.md, step5-execute.md
- Create: scripts/{analysis,execution}/*.sh
- Update: SKILL.md with EXECUTE_SCRIPT directives
- Test: Script syntax validation, execution tests

### 2. Implement Progressive Loading
- Affected: SKILL.md
- Create: "When to Load References" section
- Update: Phase-based documentation structure
- Test: Verify loading at each phase

### 3. Strengthen Directives
- Affected: All reference docs
- Add: MANDATORY, DO NOT, ALWAYS keywords
- Update: Script execution examples
- Test: Verify Claude follows instructions
```

### Step 4: Execute Changes

**Work in logical order:**

1. **Structural changes first** (directories, file organization)
2. **Content extraction** (move code to scripts, split large files)
3. **Documentation updates** (directives, references, examples)
4. **Validation** (test syntax, links, execution)

**Use version control:**
```bash
# Create a branch for refactoring
git checkout -b refactor/skill-name

# Commit incrementally
git add -p
git commit -m "refactor(skill-name): extract analysis scripts"
git commit -m "refactor(skill-name): add progressive loading"
git commit -m "refactor(skill-name): strengthen execution directives"
```

### Step 5: Test Changes

**Functional testing:**
- [ ] Skill activates correctly
- [ ] All phases execute as expected
- [ ] Scripts run without errors
- [ ] References load correctly
- [ ] Output quality maintained or improved

**Token testing:**
- [ ] Measure token usage before/after
- [ ] Verify progressive loading works
- [ ] Check that unnecessary content isn't loaded

**Behavioral testing:**
- [ ] Test with fresh conversation context
- [ ] Verify Claude follows directives
- [ ] Check determinism (same input → same output)
- [ ] Test edge cases and error scenarios

**Cross-model testing:**
- [ ] Test with Haiku (sufficient guidance?)
- [ ] Test with Sonnet (clear and efficient?)
- [ ] Test with Opus (not over-explained?)

### Step 6: Document Changes

**Create a changelog entry:**
```markdown
## [Version] - Date

### Changed
- Extracted inline scripts to `scripts/` directory
- Implemented progressive loading (67% token savings)
- Added strong execution directives

### Added
- scripts/README.md with comprehensive documentation
- Phase-based reference loading in SKILL.md

### Fixed
- Claude now consistently uses pre-built scripts
- Reduced token consumption from ~3000 to ~1000

### Removed
- Inline code blocks from documentation
```



---

## Common Refactoring Patterns

### Pattern 1: Large SKILL.md → Progressive Loading

**Before:**
```
skill-name/
└── SKILL.md (2000 lines)
```

**After:**
```
skill-name/
├── SKILL.md (400 lines)
└── references/
    ├── process/
    │   ├── phase1.md
    │   ├── phase2.md
    │   └── phase3.md
    └── support/
        └── advanced.md
```

**SKILL.md change:**
```markdown
## When to Load References

### Phase 1: Initial Analysis
- **Load**: [phase1.md](references/process/phase1.md)
- **When**: Always start here

### Phase 2: Processing (conditional)
- **Load**: [phase2.md](references/process/phase2.md)
- **When**: Only if Phase 1 detected X

### Phase 3: Advanced Features (conditional)
- **Load**: [phase3.md](references/process/phase3.md)
- **When**: Only if user requested Y
```

### Pattern 2: Inline Code → Executable Scripts

**Before (in documentation):**
````markdown
### Step 3: Validate

Run this validation:

```bash
if ! echo "$DATA" | python3 -c "import json; json.loads(input())"; then
    echo "Invalid JSON"
    exit 1
fi

# Additional validation logic...
# 20+ more lines of bash
```
````

**After:**

**In documentation:**
````markdown
### Step 3: Validate

**MANDATORY: Use pre-built validation script**

```bash
# EXECUTE_SCRIPT: scripts/validation/validate.sh
./scripts/validation/validate.sh < data.json
```

**IMPORTANT:** DO NOT inline validation logic
````

**In scripts/validation/validate.sh:**
```bash
#!/usr/bin/env bash
# validate.sh
# Purpose: Validate data format and content
# Usage: ./validate.sh < data.json

set -e

# Read from stdin
DATA=$(cat)

# Validation logic
if ! echo "$DATA" | python3 -c "import json; json.loads(input())"; then
    echo "Invalid JSON" >&2
    exit 1
fi

# Additional validation...
echo '{"status": "valid"}'
```

**In scripts/README.md:**
```markdown
### validate.sh

**Purpose:** Validate data format and content

**Usage:**
```bash
./scripts/validation/validate.sh < data.json
```

**Input:** JSON data via stdin
**Output:** `{"status": "valid"}` on success
**Exit Codes:**
- 0: Validation passed
- 1: Validation failed
```

### Pattern 3: Weak Directives → Strong Directives

**Before:**
```markdown
You should probably use the helper script for this.

Consider running the validation before committing.

It's a good idea to check the output.
```

**After:**
```markdown
**MANDATORY: ALWAYS use the helper script (DO NOT inline)**

```bash
# EXECUTE_SCRIPT: scripts/helpers/helper.sh
./scripts/helpers/helper.sh
```

**CRITICAL: Validation is REQUIRED before proceeding**

```bash
# EXECUTE_SCRIPT: scripts/validation/validate.sh
./scripts/validation/validate.sh < output.json
```

**IMPORTANT:** DO NOT proceed if validation fails

**MANDATORY: ALWAYS verify output before completing**
```

### Pattern 4: Vague Scope → Explicit Boundaries

**Before:**
```yaml
---
name: document-helper
description: Helps with documents
---
```

**After:**
```yaml
---
name: docx-processor
description: Creates, reads, and edits Word documents (.docx files). Use when working with .docx files, form letters, or formatted reports. NOT for PDFs, spreadsheets, or plain text.
---

## Overview

...

**Scope:**
- ✅ Create new .docx files
- ✅ Edit existing .docx files
- ✅ Extract text from .docx files
- ✅ Format documents (headings, tables, images)
- ❌ Never: PDF files (use pdf skill instead)
- ❌ Never: Spreadsheets (use xlsx skill instead)
- ❌ Never: Google Docs (use Drive API)
```

---

## Measuring Impact

### Before/After Metrics

**Token Usage:**
```bash
# Rough estimate (words * 1.3)
cat SKILL.md | wc -w  # Before: 1800 words = ~2340 tokens
cat SKILL.md | wc -w  # After: 400 words = ~520 tokens
# Savings: 78%
```

**File Count:**
```bash
find skill-name -name "*.md" | wc -l
# Before: 1 file
# After: 6 files (better organized)
```

**Code Blocks:**
```bash
grep -r "^\`\`\`bash" skill-name/ | wc -l
# Before: 15 inline blocks
# After: 0 inline blocks (all in scripts/)
```

**Maintenance:**
- Before: Update 5 files to change algorithm
- After: Update 1 script file

**Behavior:**
- Before: Claude inconsistently uses scripts (50% of time)
- After: Claude consistently uses scripts (100% of time)

---

## Checklist

### Pre-Refactoring
- [ ] Document current problems
- [ ] Measure baseline metrics (tokens, files, code blocks)
- [ ] Create refactoring plan
- [ ] Set up version control branch
- [ ] Test current behavior for comparison

### During Refactoring
- [ ] Work in logical order (structure → content → docs)
- [ ] Commit changes incrementally
- [ ] Test after each major change
- [ ] Keep notes on decisions and trade-offs

### Post-Refactoring
- [ ] Validate all scripts and syntax
- [ ] Test all phases and workflows
- [ ] Verify links and references
- [ ] Measure impact metrics
- [ ] Document changes (changelog or example)
- [ ] Test with multiple models
- [ ] Test in real use cases

### Ongoing
- [ ] Monitor usage and behavior
- [ ] Collect feedback
- [ ] Iterate based on observations
- [ ] Keep documentation updated

---

## Troubleshooting

### Problem: Claude still inlines scripts

**Solutions:**
1. Add stronger directives (`MANDATORY`, `DO NOT inline`)
2. Use explicit script markers (`EXECUTE_SCRIPT:`)
3. Remove all code examples from docs (force script usage)
4. Add prohibitions: "NEVER reconstruct commands"

### Problem: References don't load

**Solutions:**
1. Check relative paths are correct
2. Verify file names match exactly (case-sensitive)
3. Test links manually
4. Use correct markdown link syntax `[text](path)`

### Problem: Token usage still high

**Solutions:**
1. Move more content to conditional references
2. Remove verbose explanations
3. Assume Claude knows basics
4. Extract more code to scripts
5. Check if scripts are actually being used (not loaded)

### Problem: Skill behavior changed

**Solutions:**
1. Compare output before/after refactoring
2. Check if core principles were altered
3. Verify all original functionality still works
4. Test with original use cases
5. Restore and try incremental changes

### Problem: Maintenance didn't improve

**Solutions:**
1. Ensure code is actually in scripts (not docs)
2. Check if script organization is logical
3. Verify documentation clearly points to scripts
4. Consider further consolidation

---

## Best Practices

### Do:
- ✅ Measure impact (before/after metrics)
- ✅ Work incrementally with version control
- ✅ Test thoroughly after changes
- ✅ Document what changed and why
- ✅ Use strong, explicit directives
- ✅ Keep scope clear and bounded
- ✅ Prioritize high-impact changes

### Don't:
- ❌ Change everything at once
- ❌ Skip testing phases
- ❌ Forget to update documentation
- ❌ Remove features without consideration
- ❌ Use weak or vague language
- ❌ Leave broken references
- ❌ Optimize prematurely without measuring

---

## Example Refactorings

**Common scenarios:**
- Splitting a monolithic SKILL.md into phases
- Extracting 10+ inline code blocks to scripts
- Adding strong directives to improve consistency
- Reorganizing references by responsibility
- Clarifying scope to prevent misuse

---

## Related Documents

- [Creating Skills](creating-skills.md) - Building skills from scratch
- [Script Extraction](script-extraction.md) - Detailed guide for extracting code

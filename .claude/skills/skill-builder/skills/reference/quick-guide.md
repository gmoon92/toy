# Quick Guide: Agent Skills

Fast reference for creating effective skills.

## Minimal Skill Template

```markdown
---
name: my-skill-name
description: Does X when user asks about Y. Use when Z happens.
---

# Skill Title

## Quick Start

[Most common use case with code example]

## Advanced
- **Feature 1**: See [DETAILS.md](DETAILS.md)
- **Feature 2**: See [REFERENCE.md](REFERENCE.md)
```

---

## Frontmatter Template

```yaml
---
name: skill-name              # lowercase-with-hyphens, max 64 chars
description: |                # Max 1024 chars, what AND when
  Processes PDF files extracting text and tables.
  Use when working with PDFs or user mentions documents.
---
```

---

## Description Formula

**Pattern:** `[What it does] + [When to use it]`

**Good Examples:**

```yaml
description: Analyzes Excel files and generates reports. Use when working with spreadsheets or .xlsx files.
```

```yaml
description: Generates commit messages from git diff. Use when user asks for commit help or reviews staged changes.
```

```yaml
description: Processes PDFs extracting text and tables. Use when user mentions PDF, forms, or document extraction.
```

---

## Naming Patterns

### Gerund Form (Recommended)
- `processing-pdfs`
- `analyzing-spreadsheets`
- `managing-databases`
- `testing-code`

### Noun Phrase
- `pdf-processing`
- `spreadsheet-analysis`
- `database-management`
- `code-testing`

### Action-Oriented
- `process-pdfs`
- `analyze-spreadsheets`
- `manage-databases`
- `test-code`

---

## Progressive Disclosure Pattern

### Basic Structure

```markdown
# SKILL.md (core workflow)

## Basic Usage
[Essential instructions]

## Advanced Features
- **Forms**: [FORMS.md](FORMS.md)
- **API**: [REFERENCE.md](REFERENCE.md)
- **Examples**: [EXAMPLES.md](EXAMPLES.md)
```

### Domain Organization

```markdown
# SKILL.md (navigation)

## Available Datasets

**Finance**: Revenue, billing → [reference/finance.md](reference/finance.md)
**Sales**: Pipeline, deals → [reference/sales.md](reference/sales.md)
**Product**: Usage, features → [reference/product.md](reference/product.md)
```

---

## Workflow Template

```markdown
## [Task Name] Workflow

Copy this checklist:

```
Progress:
- [ ] Step 1: [Action]
- [ ] Step 2: [Action]
- [ ] Step 3: [Action]
- [ ] Step 4: [Validation]
```

**Step 1: [Action]**

[Detailed instructions for step 1]

**Step 2: [Action]**

[Detailed instructions for step 2]

[Continue...]
```

---

## Feedback Loop Pattern

```markdown
## Process

1. [Perform action]
2. **Immediately validate**: `python validate.py`
3. If validation fails:
   - Review error messages
   - Fix issues
   - Re-validate
4. **Only proceed after validation passes**
5. [Next action]
```

---

## Template Pattern

### Strict Template

````markdown
## Output Structure

**Always use this exact template:**

```markdown
# [Title]

## Summary
[One paragraph overview]

## Key Findings
- Finding 1 with data
- Finding 2 with data

## Recommendations
1. Specific recommendation
2. Specific recommendation
```
````

### Flexible Template

````markdown
## Output Structure

**Reasonable default (adapt as needed):**

```markdown
# [Title]

## Summary
[Overview]

## Findings
[Adjust sections based on what you find]
```

Adapt sections for specific analysis types.
````

---

## Example Pattern

```markdown
## Format Examples

**Example 1: Feature Addition**
Input: Add JWT authentication
Output:
```
feat(auth): Implement JWT-based authentication

Add login endpoint and token validation middleware
```

**Example 2: Bug Fix**
Input: Fix date display in reports
Output:
```
fix(reports): Correct timezone conversion in date formatting

Use UTC timestamps consistently across report generation
```

Follow this style: type(scope): brief description, then detailed explanation.
```

---

## Script Documentation

```markdown
## Utility Scripts

**analyze_form.py**: Extract all form fields from PDF

```bash
python scripts/analyze_form.py input.pdf > fields.json
```

Output format:
```json
{
  "field_name": {"type": "text", "x": 100, "y": 200}
}
```

**validate.py**: Check for overlapping boundaries

```bash
python scripts/validate.py fields.json
# Returns: "OK" or list of conflicts
```
```

---

## Freedom Levels

### High Freedom (Text Guidance)

```markdown
## Code Review

1. Analyze structure and organization
2. Check for bugs or edge cases
3. Suggest readability improvements
4. Verify project conventions
```

### Medium Freedom (Parameterized)

````markdown
## Report Generation

Use this template:

```python
def generate_report(data, format="markdown", include_charts=True):
    # Process and format
```
````

### Low Freedom (Exact Script)

```markdown
## Database Migration

Execute exactly:

```bash
python scripts/migrate.py --verify --backup
```

Do NOT modify or add flags.
```

---

## Quick Rules

### DO
✅ Assume Claude's intelligence
✅ Keep SKILL.md < 500 lines
✅ Use progressive disclosure
✅ Test with target models
✅ Provide concrete examples
✅ Build feedback loops
✅ Use forward slashes in paths

### DON'T
❌ Explain obvious concepts
❌ Nest references deeply
❌ Use time-sensitive info
❌ Mix terminology
❌ Assume installed tools
❌ Use Windows paths
❌ Provide too many options without guidance

---

## File Structure Examples

### Simple
```
skill/
└── SKILL.md
```

### Medium
```
skill/
├── SKILL.md
├── REFERENCE.md
└── EXAMPLES.md
```

### Complex
```
skill/
├── SKILL.md
├── reference/
│   ├── domain1.md
│   ├── domain2.md
│   └── domain3.md
└── scripts/
    ├── analyze.py
    ├── validate.py
    └── process.py
```

---

## Testing Checklist

Quick pre-share checks:

- [ ] Description has what AND when
- [ ] SKILL.md < 500 lines
- [ ] Examples are concrete
- [ ] Tested with target model(s)
- [ ] All paths use forward slashes
- [ ] Terminology is consistent

---

## Common Fixes

**Problem:** Skill not triggering
**Fix:** Make description more specific, add key terms

**Problem:** Inconsistent behavior
**Fix:** Add more structure, reduce freedom level

**Problem:** Token heavy
**Fix:** Remove explanations, split to reference files

**Problem:** Claude confused
**Fix:** Simplify structure, add clear examples

---

## Development Flow

1. **Work with Claude** on task
2. **Note repeated context** you provide
3. **Ask Claude** to create skill from patterns
4. **Review** for conciseness
5. **Test** in real scenarios
6. **Iterate** based on observations

---

## Getting Help

See full documentation:
- [Core Principles](../principles/)
- [Guidelines](../guidelines/)
- [Complete Checklist](checklist.md)

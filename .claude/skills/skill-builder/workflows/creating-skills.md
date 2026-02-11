# Creating New Skills

Guide for creating new Claude Code skills from scratch.

---

## Before You Start

### Prerequisites

1. **Identify the Need**: Have a clear use case where repetitive context or workflows would benefit from a skill
2. **Test Without a Skill**: Try solving the task with Claude using normal prompting first
3. **Identify Patterns**: Note what information you repeatedly provide
4. **Check Existing Skills**: Make sure similar functionality doesn't already exist

### Key Questions

- What specific problem does this skill solve?
- What context or knowledge does Claude need that it doesn't have?
- Will this skill be used frequently enough to justify maintenance?
- Can the skill's scope be clearly defined?

---

## Step 1: Plan the Skill

### Define Scope

**What the skill will do:**
- List 3-5 core functions
- Identify primary use cases
- Define success criteria

**What the skill won't do:**
- Explicitly list out-of-scope operations
- Define boundaries to prevent scope creep

### Choose Execution Style

**High Autonomy** (Agent does most work):
- Complex workflows with multiple steps
- Requires analysis and decision-making
- Example: commit skill, code review

**Low Autonomy** (Reference-heavy):
- Knowledge or API documentation
- Style guides and conventions
- Example: library documentation, coding standards

**Tool-based** (Executes specific operations):
- Deterministic operations
- File processing, data transformation
- Example: PDF processing, spreadsheet manipulation

---

## Step 2: Create Directory Structure

### Basic Structure

```
skill-name/
├── SKILL.md              # Main skill documentation
├── references/           # Detailed references (optional)
│   ├── process/         # Step-by-step processes
│   ├── guides/          # How-to guides
│   └── support/         # Supporting documents
├── scripts/             # Executable scripts (optional)
│   ├── README.md
│   └── [categories]/
└── templates/           # UI templates (not loaded, optional)
```

### Create the Directory

```bash
# From skills directory
mkdir -p skill-name/references/{process,guides,support}
mkdir -p skill-name/scripts
mkdir -p skill-name/templates
```

---

## Step 3: Write SKILL.md

### Use the Template

Start with [skill-template.md](../templates/skill-template.md) and customize:

### Critical Sections

**1. Frontmatter**
```yaml
---
name: skill-name
description: Brief description (under 200 chars, be specific)
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash, AskUserQuestion, Write, Edit
---
```

**Key fields:**

**`name` (required):**
- Use gerund form (verb-ing): `processing-pdfs`, `analyzing-data`
- **Validation rules:**
  - Maximum 64 characters
  - Must contain only lowercase letters, numbers, and hyphens
  - Cannot contain XML tags
  - Cannot contain reserved words: "anthropic", "claude"

**`description` (required):**
- Include key terms Claude should recognize, explain WHEN to use it
- **Validation rules:**
  - Must not be empty
  - Maximum 1024 characters
  - Cannot contain XML tags

**`user-invocable`:**
- Set to `true` if users should invoke with `/skill-name`

**`allowed-tools`:**
- Limit to only tools the skill needs

**2. Overview**
- Brief purpose statement (1-2 sentences)
- Core functions (3-5 bullet points)
- Scope definition (what it does and doesn't do)

**3. When to Load References**
```markdown
### Phase 1: [Phase Name]
- **Load**: [reference/path.md](references/reference/path.md)
- **Purpose**: What this phase does
- **When**: When to execute this phase

### Phase 2: [Phase Name] (conditional)
- **Load**: [reference/path.md](references/reference/path.md)
- **Purpose**: What this phase does
- **When**: Only if [condition]
```

**Why this matters:**
- Prevents loading all references upfront
- Saves 50-70% context tokens
- Makes skill faster and more efficient

**4. Core Principles**
- List 2-4 fundamental principles
- Explain WHY each matters
- Keep brief (2-3 lines each)

**5. Quick Reference**
- Essential information Claude needs immediately
- Formats, patterns, key concepts
- Think: "What does Claude need to start working?"

**6. Execution Flow**
```markdown
## Execution Flow

\```
1. Step 1     → [reference](references/step1.md)
2. Step 2     → [reference](references/step2.md) (if needed)
3. Step 3     → [reference](references/step3.md)
\```
```

**7. Usage Examples**
```markdown
## Usage

**Invoke skill:**
\```
/skill-name
\```

**Natural language:**
\```
Example command 1
Example command 2
Example command 3
\```
```

### Keep It Concise

**Target: 200-500 lines for SKILL.md**

If SKILL.md exceeds 500 lines:
- Move details to reference documents
- Link to references from main sections
- Keep only essential information in SKILL.md

---

## Step 4: Create Reference Documents

### When to Create References

**Process Documents** (`references/process/`):
- Step-by-step workflows
- Multi-phase operations
- Sequential procedures

**Guides** (`references/guides/`):
- How-to instructions
- Best practices
- Decision frameworks

**Support Documents** (`references/support/`):
- Examples and templates
- Troubleshooting guides
- FAQs

### Reference Document Structure

```markdown
# Document Title

Brief overview (1-2 sentences).

---

## Purpose

Explain what this document covers and when to use it.

## Content

### Section 1
Detailed content...

### Section 2
Detailed content...

## Examples

Concrete examples demonstrating the concepts.

## Related Documents

- [Related Doc 1](path/to/doc1.md)
- [Related Doc 2](path/to/doc2.md)
```

### Writing Guidelines

**Be Concise:**
- Assume Claude is smart
- Don't over-explain obvious concepts
- Focus on what Claude actually needs

**Use Examples:**
- Show, don't just tell
- Include input/output pairs
- Demonstrate patterns clearly

**Link References:**
- Keep links one level deep
- Don't create reference chains
- Make navigation obvious

---

## Step 5: Add Scripts (If Needed)

### When to Add Scripts

**Use scripts when:**
- Algorithm is complex or error-prone
- Execution must be deterministic
- Operation is repeated frequently
- Token efficiency matters

**Don't use scripts when:**
- Simple operations Claude can handle
- Flexibility is more important than consistency
- Script would be more complex than the task

### Script Guidelines

**1. Organize by Responsibility:**
```
scripts/
├── README.md           # Document all scripts
├── analysis/          # Data collection and analysis
├── algorithms/        # Detection and decision logic
├── generation/        # Content generation
├── validation/        # Input/output validation
├── execution/         # Main operations
└── utils/             # Helper functions
```

**2. Use Standard I/O:**
- Input via stdin (JSON format)
- Output via stdout (JSON format)
- Errors via stderr

**3. Add Strong Directives:**
```markdown
**MANDATORY: Use pre-built scripts (DO NOT inline commands)**

\```bash
# EXECUTE_SCRIPT: scripts/category/script_name.sh

./scripts/category/script_name.sh < input.json > output.json
\```

**IMPORTANT:**
- DO NOT reconstruct commands from documentation
- ALWAYS use the pre-built scripts via `EXECUTE_SCRIPT:` directive
\```
```

**4. Document in README:**
See [script-extraction.md](script-extraction.md) for details.

---

## Step 6: Test the Skill

### Testing Checklist

**Functionality:**
- [ ] Skill activates when expected
- [ ] All references load correctly
- [ ] Scripts execute without errors
- [ ] Output matches expectations

**Documentation:**
- [ ] All links resolve correctly
- [ ] Examples work as shown
- [ ] Instructions are clear
- [ ] No broken references

**Token Efficiency:**
- [ ] SKILL.md under 500 lines
- [ ] References load progressively
- [ ] No unnecessary verbosity
- [ ] Scripts reduce token usage

### Test with Multiple Models

- **Haiku**: Does it have enough guidance?
- **Sonnet**: Is it clear and efficient?
- **Opus**: Does it avoid over-explaining?

### Test Real Use Cases

1. Create 3-5 realistic scenarios
2. Test skill with fresh context (new conversation)
3. Verify behavior matches expectations
4. Refine based on observations

---

## Step 7: Refine and Iterate

### Common Issues

**Skill doesn't activate:**
- Add more trigger keywords to description
- Make use cases more explicit
- Check user-invocable setting

**Too verbose:**
- Move details to references
- Remove unnecessary explanations
- Assume Claude knows basics

**Claude ignores scripts:**
- Add stronger directives (MANDATORY, EXECUTE_SCRIPT)
- Use explicit prohibitions (DO NOT inline)
- Document scripts clearly in README

**References don't load:**
- Check relative paths
- Verify file names match exactly
- Test links manually

### Iteration Workflow

1. **Use the skill** in real scenarios
2. **Observe behavior**: Note what works and what doesn't
3. **Identify issues**: Where does Claude struggle?
4. **Make targeted changes**: Fix specific problems
5. **Test again**: Verify improvements
6. **Repeat**: Iterate until satisfactory

---

## Best Practices Summary

### Do:
- ✅ Start with a clear, specific purpose
- ✅ Test without a skill first
- ✅ Keep SKILL.md concise (200-500 lines)
- ✅ Use progressive disclosure for references
- ✅ Write specific, keyword-rich descriptions
- ✅ Test with multiple models
- ✅ Iterate based on real usage

### Don't:
- ❌ Create skills for one-off tasks
- ❌ Over-explain obvious concepts
- ❌ Put all content in SKILL.md
- ❌ Use vague or generic names
- ❌ Skip testing phase
- ❌ Create without clear use case
- ❌ Over-engineer for hypothetical needs

---

## Example Workflow

**Scenario:** Create a skill for code review

**1. Planning:**
```
Purpose: Guide Claude through systematic code reviews
Core Functions:
- Analyze code structure and organization
- Identify potential bugs and edge cases
- Suggest improvements for readability
- Check security vulnerabilities
Scope: Focus on quality, not style preferences
```

**2. Structure:**
```bash
mkdir -p code-review/references/{process,guides,support}
mkdir -p code-review/scripts/validation
```

**3. SKILL.md:**
```yaml
---
name: code-review
description: Systematically reviews code for bugs, security issues, and quality improvements. Use when reviewing pull requests or analyzing code quality.
user-invocable: true
allowed-tools: Read, Grep, Glob, AskUserQuestion
---

## Overview
...
```

**4. References:**
- `references/process/review-checklist.md` - Step-by-step review process
- `references/guides/security-patterns.md` - Common security issues
- `references/support/examples.md` - Example reviews

**5. Test:**
- Test on 3 different codebases
- Verify all checks run correctly
- Refine based on observations

**6. Iterate:**
- Add missing security checks
- Improve clarity of suggestions
- Optimize token usage

---

## Related Documents

- [Modifying Skills](modifying-skills.md) - Refactoring existing skills
- [Script Extraction](script-extraction.md) - Moving code to executable scripts
- [Skill Template](../templates/skill-template.md) - Boilerplate template

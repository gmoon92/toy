# Documentation Style Guide for Skills

This guide defines documentation standards for Claude Code skills based on Claude 4.x prompting best practices.

## Core Principles

### 1. Be Explicit, Not Implicit

Claude 4.x models excel with explicit instructions. Avoid ambiguity.

**BAD:**
```markdown
This skill helps with commits
```

**GOOD:**
```markdown
Analyzes git changes (staged + modified), generates conventional commit messages,
validates against Tidy First principles, and executes git commit after approval.
```

### 2. Explain the Why, Not Just the What

Context helps Claude understand goals and make better decisions.

**BAD:**
```markdown
Don't use inline scripts.
```

**GOOD:**
```markdown
DO NOT use inline scripts because:
- Inline code loads into context, wasting tokens
- Pre-built scripts ensure deterministic execution
- Script execution costs 0 tokens (only output is loaded)
```

### 3. Show, Don't Just Tell

Provide complete, realistic examples that demonstrate the pattern.

**BAD:**
```markdown
Use verb_noun naming for scripts.
```

**GOOD:**
```markdown
## Script Naming Convention: verb_noun

✅ `collect_changes.sh` - Clear action and target
✅ `detect_scope.js` - Explicit purpose
✅ `validate_message.py` - Obvious behavior

❌ `changes.sh` - Ambiguous action
❌ `scope.js` - What about scope?
❌ `msg.py` - Unclear abbreviation
```

## Structure Guidelines

### Frontmatter

Be concise and specific:

```yaml
---
name: skill-name
description: Single sentence describing exact purpose and behavior (not just domain)
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Write, Edit, Bash
---
```

**Description Rules:**
- State exactly what the skill does (verbs + outcomes)
- Include key constraints or principles
- Mention automation or validation if relevant
- Keep under 150 characters if possible

### Overview Section

**Structure:**
```markdown
## Overview

[One paragraph explanation of skill purpose]

**Core Functions:**
- Function 1 with outcome
- Function 2 with outcome
- Function 3 with outcome

**Scope:**
- ✅ What skill DOES do
- ✅ What skill DOES do
- ❌ Never: What skill NEVER does
- ❌ Never: What skill NEVER does
```

**Guidelines:**
- Lead with value proposition
- Use bullet lists for functions
- Define boundaries explicitly with ✅/❌

### When to Load References

Design for progressive loading to save tokens.

**Structure:**
```markdown
## When to Load References

### [Phase Name]
- **Load**: [relative/path/to/doc.md](path/to/doc.md)
- **Load**: [another/doc.md](path/to/doc.md)
- **Purpose**: Brief explanation of what these enable
- **When**: Condition that triggers this phase

### [Another Phase]
- **Load**: [different/doc.md](path/to/doc.md)
- **Purpose**: Different capability
- **When**: Different trigger condition
```

**Guidelines:**
- Group references by workflow phase
- Explain WHY each reference is needed
- State WHEN to load (clear trigger conditions)
- Link to actual file paths

## Writing Execution Directives

### Use Strong Keywords

Apply these consistently for emphasis:

| Keyword | Usage | Example |
|---------|-------|---------|
| `MANDATORY:` | Required behavior | `MANDATORY: Execute pre-built scripts` |
| `EXECUTE_SCRIPT:` | Script execution marker | `EXECUTE_SCRIPT: scripts/validate.sh` |
| `DO NOT` | Explicit prohibition | `DO NOT inline commands` |
| `ALWAYS` | Enforcement | `ALWAYS use script references` |
| `NEVER` | Strong prohibition | `NEVER reconstruct code` |
| `CRITICAL:` | High priority | `CRITICAL: Verify git status first` |
| `IMPORTANT:` | Significant note | `IMPORTANT: Scripts are deterministic` |

### Directive Pattern

Use this pattern for script execution:

```markdown
**MANDATORY: Use pre-built scripts (DO NOT inline commands)**

```bash
# EXECUTE_SCRIPT: scripts/category/script_name.sh

./scripts/category/script_name.sh < input.json > output.json
```

**IMPORTANT:**
- ALWAYS use the pre-built script via `EXECUTE_SCRIPT:` directive
- DO NOT reconstruct commands from documentation
- DO NOT inline bash commands
- Scripts ensure deterministic execution
```

**Why This Works:**
- `MANDATORY:` sets expectation
- `EXECUTE_SCRIPT:` marks the exact script
- `DO NOT` provides clear prohibitions
- Bullet points reinforce the requirements
- Context explains WHY it matters

## Format Control

### Use XML Tags for Structure

XML tags help Claude understand output structure:

```markdown
**Structure your response as:**

<analysis>
Present factual findings about git changes:
- Modified files count
- Staged changes count
- Detected change type (feature/fix/refactor)
</analysis>

<commit_message>
Generate conventional commit message:
type(scope): description

[optional body]
</commit_message>

<validation>
Verify against principles:
- ✅ Logical independence: yes/no
- ✅ Tidy First: yes/no
- ⚠️ Warnings: if any
</validation>
```

**Benefits:**
- Clear output boundaries
- Consistent structure
- Easy to parse
- Self-documenting

### Prefer Prose Over Bullets

For explanatory content, use flowing paragraphs instead of bullet lists.

**BAD:**
```markdown
The skill provides:
- Git change analysis
- Commit message generation
- Convention validation
- Automated staging
```

**GOOD:**
```markdown
The skill analyzes git changes, generates conventional commit messages following
project conventions, validates against Tidy First principles, and automatically
stages modified files before committing.
```

**When to Use Bullets:**
- Listing distinct items (functions, file names, steps)
- Showing explicit boundaries (✅/❌ scope)
- Presenting options or alternatives
- Quick reference sections

## Code Examples

### Complete, Realistic Examples

Show full context, not fragments.

**BAD:**
```markdown
Use detect_scope.js:
```

**GOOD:**
```markdown
## Scope Detection Example

**EXECUTE_SCRIPT:** scripts/algorithms/detect_scope.js

```bash
# Input: Git diff output
git diff --staged | ./scripts/algorithms/detect_scope.js

# Output: JSON with detected scope
{
  "scope": "auth",
  "confidence": 0.95,
  "files": ["src/auth/login.ts", "src/auth/session.ts"]
}
```

**How It Works:**
The script analyzes file paths and change patterns to determine the logical
scope of changes. High confidence (>0.9) indicates clear scope boundaries.
```

### Show Input/Output Formats

For scripts, always document I/O:

```markdown
## Script: validate_message.sh

**Purpose:** Validates commit message against conventional commit format

**Input Format:**
```text
type(scope): description

Optional body paragraph providing additional context.

Optional footer with references.
```

**Output Format:**
```json
{
  "valid": true,
  "errors": [],
  "warnings": ["Body exceeds 72 characters per line"]
}
```

**Exit Codes:**
- 0: Valid message
- 1: Invalid format
- 2: Missing required fields
```

## Organizing Information

### Phase-Based Organization

Organize documentation by workflow phase:

```markdown
## Phase 1: Analysis
[Documentation for analysis phase]

## Phase 2: Generation
[Documentation for generation phase]

## Phase 3: Validation
[Documentation for validation phase]

## Phase 4: Execution
[Documentation for execution phase]
```

**Benefits:**
- Matches Claude's workflow
- Enables progressive loading
- Reduces cognitive load
- Clear boundaries

### Hierarchical Headers

Use consistent header hierarchy:

```markdown
# Document Title (H1) - once per document

## Major Section (H2) - main divisions

### Subsection (H3) - specific topics

#### Detail (H4) - fine details

**Bold** - emphasis within paragraphs
```

## Writing Guidelines

### Active Voice

Use active voice for clarity and directness.

**BAD (Passive):**
```markdown
Changes are analyzed by the skill and commit messages are generated.
```

**GOOD (Active):**
```markdown
The skill analyzes changes and generates commit messages.
```

### Present Tense

Write in present tense for immediacy.

**BAD (Future):**
```markdown
The script will validate the message format.
```

**GOOD (Present):**
```markdown
The script validates the message format.
```

### Imperative for Instructions

Use imperative mood for directives.

**BAD:**
```markdown
You should execute the script.
Claude needs to use the pre-built script.
```

**GOOD:**
```markdown
Execute the script.
Use the pre-built script.
ALWAYS execute via EXECUTE_SCRIPT directive.
```

## Anti-Patterns to Avoid

### ❌ Vague Descriptions

```markdown
## BAD
This helps with git stuff.

## GOOD
Analyzes git changes and generates conventional commit messages.
```

### ❌ Missing Context

```markdown
## BAD
Don't inline code.

## GOOD
DO NOT inline code because it wastes tokens and prevents deterministic execution.
Pre-built scripts cost 0 tokens and ensure consistency.
```

### ❌ Incomplete Examples

```markdown
## BAD
Use the script:
```bash
./script.sh
```

## GOOD
**EXECUTE_SCRIPT:** scripts/validate.sh

```bash
./scripts/validate.sh < message.txt > validation.json
```

**Input:** Plain text commit message
**Output:** JSON validation result with errors array
```

### ❌ Ambiguous Scope

```markdown
## BAD
**Scope:**
- Helps with commits

## GOOD
**Scope:**
- ✅ Generate conventional commit messages
- ✅ Validate against project conventions
- ✅ Auto-stage modified files
- ❌ Never: Push commits to remote
- ❌ Never: Modify commit history
```

### ❌ Overuse of Markdown Formatting

```markdown
## BAD
**This** is *really* **important** for the **skill** to work *correctly*!

## GOOD
This is important for the skill to work correctly.
```

**When to Format:**
- `**MANDATORY:**` - directive keywords
- `**Bold**` - section labels in structured content
- `` `code` `` - code references, file names, commands
- Avoid: excessive bold, italics, or emphasis

## Documentation Checklist

Before finalizing skill documentation:

- [ ] Frontmatter description is explicit and specific
- [ ] Overview explains value and scope clearly
- [ ] References are organized by phase with load conditions
- [ ] Strong directive keywords are used consistently
- [ ] Script execution uses EXECUTE_SCRIPT pattern
- [ ] Examples are complete with input/output formats
- [ ] Context explains WHY, not just WHAT
- [ ] Active voice and present tense throughout
- [ ] XML tags structure complex outputs
- [ ] No vague language or ambiguous instructions
- [ ] Scope boundaries clearly defined with ✅/❌
- [ ] Headers follow consistent hierarchy
- [ ] Code examples show full context
- [ ] Anti-patterns are avoided

## Summary

Effective skill documentation:
1. **Explicit instructions** - Tell exactly what to do
2. **Rich context** - Explain why it matters
3. **Complete examples** - Show realistic usage
4. **Strong directives** - Use keywords consistently
5. **Structured output** - XML tags for clarity
6. **Progressive loading** - Phase-based organization
7. **Clear scope** - Define boundaries explicitly
8. **Token efficiency** - Script extraction, concise prose

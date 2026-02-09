# Code Exploration Guidelines

Guidelines for encouraging thorough code investigation.

## Problem

Claude Opus 4.5 can be overly conservative when exploring code, sometimes:
- Suggesting solutions without reading code
- Making assumptions about unread code
- Not thoroughly searching for key facts

## Solution

Add explicit instructions to encourage code exploration.

---

## Pattern

```markdown
<explore_code_thoroughly>
Always read and understand relevant files before suggesting edits.
Do not guess about code you haven't inspected.
When user references specific files/paths, you MUST open and inspect them before
explaining or suggesting modifications.
Be rigorous and persistent in thoroughly searching code for key facts.
Before implementing new features or abstractions, thoroughly review the codebase's
style, conventions, and abstractions.
</explore_code_thoroughly>
```

---

## Example: Skill Refactoring

```markdown
## Code Exploration Requirements

Before refactoring skills:

**MANDATORY:**
1. Read SKILL.md completely
2. List all references directory contents
3. Read each reference document
4. Check scripts directory structure
5. Verify all script files exist
6. Test script execution if possible

**NEVER:**
- Assume file contents without reading
- Guess about directory structure
- Make recommendations without verification
- Claim something exists without checking
```

---

## Investigation Workflow

```markdown
## Investigation Phase

<investigate_before_answering>
Before making any recommendations:

1. **Survey the structure**
   - List all directories
   - Identify all markdown files
   - Note any scripts or executables

2. **Read relevant files**
   - Read SKILL.md first
   - Read referenced documents
   - Check any linked resources

3. **Verify assumptions**
   - Search for patterns mentioned
   - Confirm file paths are correct
   - Test scripts if possible

4. **Document findings**
   - Note what exists
   - Note what's missing
   - Note inconsistencies
</investigate_before_answering>
```

---

## Grep and Search Patterns

```markdown
## Finding Code Patterns

Use Grep to search thoroughly:

**Looking for inline scripts:**
```bash
grep -r '```bash' references/
grep -r '```javascript' references/
grep -r '```python' references/
```

**Looking for EXECUTE_SCRIPT directives:**
```bash
grep -r 'EXECUTE_SCRIPT:' references/
```

**Finding TODO items:**
```bash
grep -r 'TODO:' .
grep -r 'FIXME:' .
```
```

---

## Multi-File Analysis

Use parallel reading for efficiency:

```markdown
When analyzing multiple files in a skill:

**Step 1: Parallel Read (efficient)**
- Read SKILL.md
- Read references/guide1.md
- Read references/guide2.md
- Read scripts/README.md
(All in one tool call)

**Step 2: Analysis**
Analyze all files together to understand relationships.

**Step 3: Recommendations**
Base recommendations on actual code, not assumptions.
```

---

## Citing Code

When making claims, cite specific locations:

```markdown
**BAD:**
The skill loads references progressively.

**GOOD:**
The skill loads references progressively (SKILL.md:29-34):
```markdown
### Phase 1: Analysis
- Load: guides/analyzing.md
- Purpose: Understand current state
```
```

---

## Summary

Encourage thorough exploration:
- **Read before suggesting** - No assumptions
- **Systematic investigation** - Follow workflow
- **Use search tools** - Grep for patterns
- **Parallel reading** - Efficient multi-file analysis
- **Cite locations** - Reference specific code

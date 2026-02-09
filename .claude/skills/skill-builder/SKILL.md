---
name: skill-builder
description: Assists in creating, modifying, and refactoring Claude Code skills. Provides templates, guides, and best practices for skill development.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Write, Edit, Bash, AskUserQuestion, Task
---

# Skill Builder

Helps create and maintain high-quality Claude Code skills.

## Core Functions

- **Create** new skills from templates
- **Refactor** existing skills for better performance
- **Extract** inline code to executable scripts
- **Validate** skill structure and conventions

---

## Execution Workflows

### Workflow 1: Create New Skill

**Trigger:** User says "Create a skill for [purpose]"

**Steps:**

1. **Gather Requirements**
   ```
   EXECUTE AskUserQuestion:
   - Skill purpose
   - Core functions (3-5)
   - Execution style (autonomy level)
   ```

2. **Delegate to Task**
   ```
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "Create skill structure"
     prompt: """
     See: workflows/creating-skills.md
     See: templates/skill-template.md
     See: skills/reference/quick-guide.md
     
     RETURN FORMAT: See "Task Return Format" section below.
     """
   ```

3. **Report Back**
   - Status + file paths
   - User reads files if needed: `Read .claude/skills/{name}/SKILL.md`

---

### Workflow 2: Refactor Existing Skill

**Trigger:** User says "Refactor [skill-name]" or "Make [skill-name] more concise"

**Steps:**

1. **Analyze Current**
   ```
   EXECUTE: Read {skill-name}/SKILL.md
   EXECUTE: Bash - wc -l {skill-name}/**/*.md
   EXECUTE: Grep - pattern "```(bash|javascript|python)"
   ```

2. **Delegate to Task**
   ```
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "Refactor for efficiency"
     prompt: """
     Current: {metrics}
     
     See: workflows/modifying-skills.md (Token Optimization)
     See: workflows/script-extraction.md (if inline code)
     See: prompting/principles/01-general-principles.md (directives)
     
     RETURN FORMAT: See "Task Return Format" section below.
     """
   ```

3. **Report Back**
   - Improvements + metrics
   - User reviews: `Read {skill-name}/SKILL.md.new`

---

### Workflow 3: Extract Scripts

**Trigger:** User says "Extract scripts from [skill-name]"

**Steps:**

1. **Identify Inline Code**
   ```
   EXECUTE: Grep - pattern "^```(bash|javascript|python)"
   ```

2. **Delegate to Task**
   ```
   EXECUTE Task:
     subagent_type: "general-purpose"
     description: "Extract code to scripts"
     prompt: """
     See: workflows/script-extraction.md (full process)
     
     RETURN FORMAT: See "Task Return Format" section below.
     """
   ```

3. **Report Back**
   - Scripts created + token savings
   - User reviews: `Read {skill-name}/scripts/`

---

## Task Return Format

**CRITICAL:** Task agents MUST return metadata only.

**Return:**
- ✅ File paths
- ✅ Status summaries (max 10 lines)
- ✅ Metrics/counts
- ✅ Brief summaries (2-3 lines)

**Never return:**
- ❌ Full file contents
- ❌ Large outputs
- ❌ Complete diffs

**Why:** File path (10 tokens) vs full content (500+ tokens) = 98% savings.

**Details:** See `skills/principles/01-conciseness.md`

---

## Core Principles

1. **Delegate Complex Work** - Use Task for expensive operations
2. **Load Progressively** - References loaded in Task context only
3. **Strong Directives** - MANDATORY, EXECUTE, DO NOT keywords
4. **Minimal Returns** - Metadata only from Tasks
5. **Validate Always** - Run checklist after changes

**Full principles:** See `prompting/principles/` and `skills/principles/`

---

## References

### Workflows
- `workflows/creating-skills.md` - Step-by-step creation process
- `workflows/modifying-skills.md` - Refactoring patterns & token optimization
- `workflows/script-extraction.md` - Code extraction to scripts

### Templates
- `templates/skill-template.md` - SKILL.md boilerplate structure

### Principles & Guidelines
- `skills/principles/01-conciseness.md` - Token efficiency
- `skills/principles/02-appropriate-freedom.md` - Specificity levels
- `prompting/principles/01-general-principles.md` - Strong directives
- `prompting/guidelines/` - Tool usage, format control, etc.

### Validation
- `skills/reference/checklist.md` - Quality validation checklist
- `skills/reference/quick-guide.md` - Quick patterns & templates

**Note:** Do NOT load these in main context. Task agents load them as needed.

---

## Usage Examples

```
User: "Create a skill for analyzing log files"
→ Execute Workflow 1

User: "Refactor the commit skill"
→ Execute Workflow 2

User: "Extract inline bash scripts from my-skill"
→ Execute Workflow 3
```

---

## Scope

**DO:**
- ✅ Delegate to Task agents for complex work
- ✅ Use strong execution directives (MANDATORY, EXECUTE)
- ✅ Task returns metadata only (paths, summaries)
- ✅ Save Task outputs to files
- ✅ User reads files with Read tool if needed
- ✅ Validate all outputs

**DON'T:**
- ❌ Load references in main context
- ❌ Perform complex analysis in main context
- ❌ Use weak directives ("you should", "consider")
- ❌ Return full contents from Task
- ❌ Load large outputs into main context
- ❌ Skip validation

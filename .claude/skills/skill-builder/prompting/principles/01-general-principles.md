# General Principles

Core prompting principles from Claude 4.x best practices.

## 1. Write Clear and Explicit Instructions

Claude 4.x models respond well to clear, explicit instructions. Be specific about desired outputs.

**Application to Skills:**
- Define skill's purpose explicitly in frontmatter description
- Use clear, directive language in documentation
- Specify exactly what Claude should do in each phase

**Example:**
```markdown
## BAD
Help with git operations

## GOOD
Analyzes all git changes (staged + modified) and generates commit messages following project conventions.
Auto-stages modified files and validates Tidy First principles before executing git commit.
```

---

## 2. Add Context for Better Performance

Providing context or motivation behind instructions helps Claude understand goals and provide more targeted responses.

**Application to Skills:**
- Explain WHY certain patterns are important
- Document the reasoning behind architectural decisions
- Include context in execution directives

**Example:**
```markdown
## BAD
Don't use inline commands

## GOOD
DO NOT inline commands because:
- Inlining wastes tokens by loading code into context
- Pre-built scripts ensure deterministic execution
- Script references are 0-token operations (only output loaded)
```

---

## 3. Pay Attention to Examples and Details

Claude 4.x models pay careful attention to details and examples as part of precise instruction-following.

**Application to Skills:**
- Ensure examples match the encouraged behavior
- Show complete, realistic examples
- Include both good and bad examples for clarity

**Example:**
```markdown
## Good Naming Convention

✅ `collect_changes.sh` - verb_noun pattern
✅ `detect_scope.js` - clear purpose
✅ `validate_message.py` - action-oriented

❌ `changes.sh` - unclear action
❌ `scope.js` - what about scope?
❌ `msg.py` - unclear abbreviation
```

---

## Summary

These three principles form the foundation:
1. **Clear Instructions** - Be explicit about what to do
2. **Rich Context** - Explain why it matters
3. **Good Examples** - Show what success looks like

Apply these consistently throughout skill documentation.

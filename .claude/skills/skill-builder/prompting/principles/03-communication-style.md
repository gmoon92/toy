# Communication Style

Claude 4.5 models have a more concise, natural communication style.

## Characteristics

- **More direct and grounded**: Factual progress reports vs. self-congratulatory updates
- **More conversational**: Fluent and colloquial, less mechanical
- **Less verbose**: May skip detailed summaries unless explicitly requested

This style reflects what was achieved accurately without unnecessary elaboration.

---

## Application to Skills

### Write Concise Descriptions

**BAD (Verbose):**
```markdown
Please ensure that you carefully and thoroughly analyze the git repository's current state,
including all modifications that have been made to tracked files as well as any new files
that might have been added but not yet staged for commit, so that you can generate a
comprehensive and accurate commit message.
```

**GOOD (Concise):**
```markdown
Analyze all git changes (staged + modified) and generate a commit message following
project conventions.
```

---

### Use Direct Language

**BAD:**
```markdown
It would be helpful if you could perhaps consider using the pre-built script when
executing this operation, as it might provide better results.
```

**GOOD:**
```markdown
ALWAYS use the pre-built script for this operation.
```

---

### Balance Verbosity

Claude 4.5 may skip summaries for efficiency. If you want updates:

```markdown
After completing tool-related tasks, provide a brief summary of what you did.
```

---

## Prefer Prose Over Bullets (Where Appropriate)

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

---

## Active Voice and Present Tense

**Active Voice:**
```markdown
❌ Changes are analyzed by the skill
✅ The skill analyzes changes
```

**Present Tense:**
```markdown
❌ The script will validate the message
✅ The script validates the message
```

**Imperative for Instructions:**
```markdown
❌ You should execute the script
✅ Execute the script
✅ ALWAYS execute via EXECUTE_SCRIPT directive
```

---

## Summary

Write documentation that is:
- **Concise** - No unnecessary words
- **Direct** - Clear imperatives
- **Active** - Active voice, present tense
- **Prose** - Flowing paragraphs for explanation
- **Bullets** - Lists only for distinct items

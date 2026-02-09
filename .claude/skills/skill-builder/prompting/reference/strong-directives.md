# Strong Directive Keywords

Keywords for emphasis and clarity in skill documentation.

## Keyword Reference

| Keyword | Usage | Example |
|---------|-------|---------|
| `MANDATORY:` | Required behavior | `MANDATORY: Use pre-built scripts` |
| `EXECUTE_SCRIPT:` | Script execution marker | `EXECUTE_SCRIPT: scripts/validate.sh` |
| `DO NOT` | Explicit prohibition | `DO NOT inline commands` |
| `ALWAYS` | Enforcement | `ALWAYS use script references` |
| `NEVER` | Strong prohibition | `NEVER reconstruct code` |
| `CRITICAL:` | High priority warning | `CRITICAL: Verify git status first` |
| `IMPORTANT:` | Significant note | `IMPORTANT: Scripts are deterministic` |

---

## Usage Patterns

### MANDATORY

Use for absolutely required behavior:

```markdown
**MANDATORY: Execute pre-built script (DO NOT inline)**
```

---

### EXECUTE_SCRIPT

Mark exact script to execute:

```markdown
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
./scripts/analysis/collect_changes.sh
```

---

### DO NOT / NEVER

Explicit prohibitions:

```markdown
**IMPORTANT:**
- DO NOT reconstruct commands from documentation
- DO NOT inline bash commands
- NEVER skip script execution
```

---

### ALWAYS

Enforce consistent behavior:

```markdown
ALWAYS use the pre-built script via EXECUTE_SCRIPT directive.
```

---

### CRITICAL / IMPORTANT

Highlight priority:

```markdown
**CRITICAL:** Verify git status before committing.

**IMPORTANT:** Scripts ensure deterministic execution.
```

---

## Full Example

```markdown
**MANDATORY: Use pre-built scripts (DO NOT inline commands)**

```bash
# EXECUTE_SCRIPT: scripts/category/script_name.sh

./scripts/category/script_name.sh < input.json > output.json
```

**CRITICAL:**
- ALWAYS use the pre-built script via `EXECUTE_SCRIPT:` directive
- DO NOT reconstruct commands from documentation
- DO NOT inline bash commands
- NEVER skip validation steps

**IMPORTANT:**
Scripts ensure deterministic execution (0 token cost).
```

---

## Best Practices

1. **Use consistently** - Same keywords for same purposes
2. **Don't overuse** - Reserve for truly important directives
3. **Be specific** - State exactly what to do or avoid
4. **Provide context** - Explain why it matters
5. **Combine when needed** - Multiple keywords for emphasis

---

## When to Use Strong Directives

**Use for:**
- Script execution requirements
- Critical validation steps
- Explicit prohibitions
- Required workflows
- Safety-critical operations

**Don't use for:**
- General information
- Optional suggestions
- Background context
- Examples and demonstrations

---

## Summary

Strong directives ensure:
- **Clear expectations** - No ambiguity
- **Consistent behavior** - Reliable execution
- **Emphasis** - Important points stand out
- **Prohibition clarity** - What not to do

# Troubleshooting Guide

Quick solutions for commit failures and hook errors.

---

## Pre-commit Hook Failures

Pre-commit hooks run **before** the commit is created. If they fail, no commit is made.

### Linting Errors (ESLint, Prettier)

**Symptom:**
```
❌ pre-commit hook failed
ESLint found 3 errors:
  src/components/Agent.tsx:15:3 - 'agentData' is assigned but never used
```

**Solution:**

Auto-fix (recommended):
```bash
npx eslint --fix src/
npx prettier --write src/
git add .
```

Manual fix:
1. Fix issues in mentioned files
2. Re-stage: `git add <file>`
3. Retry commit

Skip hook (not recommended):
```bash
git commit --no-verify -m "message"
```

⚠️ Skipping hooks can break CI/CD pipelines.

### Java Compilation Errors

**Solution:**
1. Fix compilation errors in IDE
2. Verify: `mvn clean compile`
3. Re-stage and retry

### Test Failures

**Solution:**
```bash
# Diagnose specific test
mvn test -Dtest=AgentServiceTest#shouldDeleteAgent

# Fix tests, verify all pass
mvn test

# Re-stage and retry
```

**Common causes:** Missing setup, incorrect mocks, changed signatures, missing test data

### Checkstyle/Spotless Violations

**Solution:**
```bash
# Format in IDE: Ctrl+Alt+L (IntelliJ)
# Or auto-fix
mvn spotless:apply

# Re-stage and retry
```

---

## Commit-msg Hook Failures

Commit-msg hooks run **after** message is provided but **before** commit is created.

### Format Validation Errors

**Common mistakes:**

```
❌ feat(agent):message          # Missing space after colon
❌ feat agent: message           # Missing parentheses
❌ feat(agent) message           # Missing colon
❌ feature(agent): message       # Invalid type
❌ feat(agent):                  # No message
❌ feat(agent service): message  # Space in scope

✅ feat(agent): message          # Correct format
```

**Solution:** Retry with correct format: `<type>(scope): <message>`

### Invalid Type

**Allowed types:**
- `feat` - New feature
- `fix` - Bug fix
- `refactor` - Code refactoring
- `test` - Test code
- `docs` - Documentation
- `style` - Formatting
- `chore` - Build/config

See [rules.md](../validation/rules.md) for details.

### Excessive Blank Lines

**Problem:** More than 2 groups of consecutive blank lines

**Solution:**
```
✅ feat(agent): title

body

footer
```

### Sensitive Information Detected

**Solution:**
1. Remove sensitive info from message
2. If already committed: `git commit --amend`
3. If pushed: Contact team lead immediately

⚠️ Never commit passwords, API keys, tokens, or credentials

---

## Git Command Errors

### No Staged Files

**Solution:**
```bash
git status               # Check changes
git add <file>          # Stage specific file
git add .               # Stage all
```

### Merge Conflicts

**Solution:**
```bash
# Resolve conflicts in files
git add <resolved-file>
git commit              # Use default merge message
```

### Detached HEAD State

**Solution:**
```bash
# Keep changes
git checkout -b feature/249311/my-changes

# Discard changes
git checkout feature/249311/agent-delete
```

---

## Recovery Procedures

### Undo Last Commit (Not Pushed)

```bash
git reset HEAD~1              # Keep changes
git reset --hard HEAD~1       # Discard changes (⚠️ permanent)
```

### Amend Last Commit

```bash
git commit --amend -m "new message"          # Fix message
git add file.txt && git commit --amend --no-edit  # Add forgotten file
```

### Unstage Files

```bash
git reset HEAD <file>         # Unstage specific file
git reset HEAD                # Unstage all
```

### Discard Working Changes

```bash
git restore <file>            # Discard specific file
git restore .                 # Discard all (⚠️ permanent)
```

---

## Common Workflow Issues

### Mixed Changes (Tidy First Violation)

Staged changes mix refactor and feat/fix.

**Solution:**
```bash
# Unstage all
git reset HEAD

# Stage refactor changes only
git add -p               # Interactive staging
/commit

# Stage feature changes
git add -p
/commit
```

### Committed to Main Branch

**Solution:**
```bash
git checkout -b feature/my-changes    # Create branch from current state
git checkout main                      # Return to main
git reset --hard origin/main           # Reset main
git checkout feature/my-changes        # Continue work
```

### Split Large Commit

**Solution:**
```bash
git reset HEAD~1         # Undo commit, keep changes

# Commit separately
git add file1.java file2.java
/commit

git add file3.java file4.java
/commit
```

---

## Hook Debugging

### Check Hook Installation

```bash
ls -la .git/hooks/
cat .git/hooks/pre-commit
cat .git/hooks/commit-msg
```

### Temporarily Disable Hooks

```bash
# Rename hook
mv .git/hooks/pre-commit .git/hooks/pre-commit.bak

# Or use flag
git commit --no-verify -m "message"
```

⚠️ Only for testing. Fix issues properly.

---

## Diagnostic Commands

### Git Status

```bash
git status               # Check branch, staged files, conflicts
```

### View Commits

```bash
git log --oneline -5     # Last 5 commits
git log --stat -3        # With file changes
git log -p -2            # Detailed view
```

### Check Differences

```bash
git diff                 # Unstaged changes
git diff --staged        # Staged changes
git diff HEAD~1 HEAD     # Between commits
```

---

## Prevention Checklist

### Before Committing

✅ Run tests: `mvn test`
✅ Check compilation: `mvn clean compile`
✅ Format code: `Ctrl+Alt+L` or `mvn spotless:apply`
✅ Review changes: `git diff --staged`
✅ Verify no mixed refactor/feat changes

### Commit Habits

✅ Commit small, focused changes
✅ Test before committing
✅ Review generated messages
✅ Use `/commit` for format

---

## Troubleshooting Workflow

When stuck, follow this checklist:

1. **Read error message carefully**
2. **Check status**: `git status`
3. **Verify tests pass**: `mvn test`
4. **Check compilation**: `mvn clean compile`
5. **Review staged changes**: `git diff --staged`
6. **Verify commit format**
7. **Check for mixed changes** (refactor + feat/fix)
8. **Try auto-fix** (lint, format)
9. **Consult this guide**
10. **Ask for help** if still stuck

---

## Additional Resources

- [rules.md](../validation/rules.md) - Commit message format rules
- [examples.md](examples.md) - Commit examples
- [SKILL.md](../../SKILL.md) - Skill overview

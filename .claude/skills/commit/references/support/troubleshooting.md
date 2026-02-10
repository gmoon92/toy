# Troubleshooting Guide

Quick solutions for commit failures and hook errors.

---

## Hook Failures

Git hooks validate changes at different stages. If they fail, no commit is created.

**Hook types:**
- **Pre-commit**: Runs before commit (validates code quality)
- **Commit-msg**: Runs after message provided (validates message format)

**Skip hook (emergency only):**
```bash
git commit --no-verify -m "message"
```
⚠️ Skipping hooks can break CI/CD pipelines. Fix issues properly.

### Pre-commit: Linting Errors

**Symptom:**
```
❌ ESLint found 3 errors:
  src/components/Agent.tsx:15:3 - 'agentData' is assigned but never used
```

**Solution:**
```bash
npx eslint --fix src/ && npx prettier --write src/
git add . && /commit
```

### Pre-commit: Compilation/Test Failures

**Java compilation:**
```bash
mvn clean compile    # Fix errors, verify
```

**Test failures:**
```bash
mvn test -Dtest=AgentServiceTest#shouldDeleteAgent    # Diagnose
mvn test                                               # Verify all pass
```

**Common causes:** Missing setup, incorrect mocks, changed signatures

### Pre-commit: Code Style

**Solution:**
```bash
mvn spotless:apply    # Or Ctrl+Alt+L in IntelliJ
```

### Commit-msg: Format Validation

**Common mistakes:**
```
❌ feat(agent):message          # Missing space
❌ feat agent: message           # Missing parentheses
❌ feat(agent) message           # Missing colon
❌ feature(agent): message       # Invalid type
✅ feat(agent): message          # Correct: <type>(scope): <message>
```

**Allowed types:** `feat`, `fix`, `refactor`, `test`, `docs`, `style`, `chore`

**Max blank lines:** 2 groups only (title-body, body-footer)

**Sensitive info:** Never commit passwords, API keys, tokens. If committed: `git commit --amend`

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

**Check installation:**
```bash
ls -la .git/hooks/
cat .git/hooks/pre-commit
```

**Disable temporarily (testing only):**
```bash
mv .git/hooks/pre-commit .git/hooks/pre-commit.bak    # Rename
git commit --no-verify -m "message"                    # Or skip with flag
```

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

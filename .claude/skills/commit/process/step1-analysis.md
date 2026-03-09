# Step 1: Initial Analysis

Collect pure git diff data (NO caching, NO inference).

---

## Overview

**Phase 1 executes git commands directly:**
- Check status, stage files, collect data

**What it does:**
1. **Save current staging state** - Record which files are already staged
2. Check current git status
3. **Unstage all existing files** (`git reset HEAD`) for clean slate
4. **Stage files based on user input:**
   - If specific files are mentioned: Stage ONLY those files (`git add <file>`)
   - If NO specific files mentioned: Stage all modified files (`git add -u`)
5. Collect git metadata (branch, timestamp)
6. Extract file changes (path, additions, deletions)

**What it does NOT do:**
- NO type detection (Claude will infer)
- NO scope detection (Claude will infer)
- NO caching (fresh collection every time)
- NO metadata files (Claude creates in-memory)

---

## Execute Git Commands

**Use Bash tool to execute appropriate git commands:**

**Key commands:**
- ✅ `git diff --cached --name-only` - Save current staged files list
- ✅ `git status --porcelain` - Check current state
- ✅ `git reset HEAD` - Unstage existing files (clean slate)
- ✅ `git add -u` - Stage modified files only (not untracked)
- ✅ `git add <specific-file>` - Stage specific file when path provided
- ✅ `git diff --cached` - Collect staged file statistics

**CRITICAL: Staging State Management**

**At skill start - Save staging state:**
```bash
# Save list of currently staged files
STAGED_FILES=$(git diff --cached --name-only)
```

**Before staging - Clean slate:**
```bash
# Unstage everything first
git reset HEAD
```

**After commit (success, cancel, or error) - Restore original staging:**
```bash
# Restore previously staged files
if [ -n "$STAGED_FILES" ]; then
    echo "$STAGED_FILES" | xargs git add
fi
```

**IMPORTANT: Respect user-specified file paths**
When user explicitly specifies file paths (e.g., "@path/to/file.md 커밋해"):
1. **Save current staging state first** (list of staged files)
2. **Unstage everything**: `git reset HEAD`
3. Stage ONLY the specified file(s): `git add <specific-file>`
4. Do NOT use `git add -u` which stages all modified files
5. Analyze and commit only the staged specific file(s)
6. **After commit/abort: Restore original staging state**

When NO specific files are mentioned:
1. Use `git add -u` to stage all modified files (existing behavior)
2. No need to save/restore staging state (commit includes all changes anyway)

**Exit if no changes:**
If `git diff --cached --name-only` returns empty, exit with error:
```json
{"error":"변경사항이 없습니다"}
```

---

## Claude's Analysis (Phase 2)

After collecting git data, Claude analyzes in real-time:

1. **File Analysis**:
   - File paths and extensions
   - Change patterns (additions vs deletions)
   - Directory structure

2. **Type Inference**:
   - `.md` files → likely `docs`
   - `test/` directory → likely `test`
   - New functionality patterns → `feat`
   - Bug fix patterns → `fix`
   - Structural changes → `refactor`

3. **Scope Inference**:
   - Common directory → module name
   - Single file → filename
   - Multiple modules → parent directory

4. **Violation Detection**:
   - Tidy First: Structural vs behavioral mixing
   - Logical Independence: Multiple independent groups

5. **Message Generation**:
   - 5 header candidates (Recommended 2 + General 3)
   - Body item candidates (feature-based)
   - Natural prioritization (no mechanical scoring)

---

## Token Efficiency

**Approach:**
- Every run: Moderate (fresh analysis)
- Trade-off: Always current context, simpler system

**Why this is OK:**
- Commit happens once per changeset (rarely retried)
- Fresh analysis ensures latest context
- System simplicity > micro-optimization


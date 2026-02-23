# Step 1: Initial Analysis

Collect pure git diff data (NO caching, NO inference).

---

## Overview

**Phase 1 executes git commands directly:**
- Check status, stage files, collect data

**What it does:**
1. Check current git status
2. Unstage existing files first (for isolated commits) - optional
3. Stage modified files (`git add -u`)
4. Collect git metadata (branch, timestamp)
5. Extract file changes (path, additions, deletions)

**What it does NOT do:**
- NO type detection (Claude will infer)
- NO scope detection (Claude will infer)
- NO caching (fresh collection every time)
- NO metadata files (Claude creates in-memory)

---

## Execute Git Commands

**Use Bash tool to execute appropriate git commands:**

**Key commands:**
- ✅ `git status --porcelain` - Check current state
- ✅ `git reset HEAD` - Unstage existing files (optional)
- ✅ `git add -u` - Stage modified files only (not untracked)
- ✅ `git diff --cached` - Collect staged file statistics

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

---

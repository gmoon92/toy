# Step 1: Initial Analysis

Collect pure git diff data (NO caching, NO inference).

---

## Overview

**Phase 1 executes a single script:**
- `collect_git_diff.sh` - Collects pure git data

**What it does:**
1. Auto-stages modified files (`git add -u`)
2. Collects git metadata (branch, timestamp)
3. Extracts file changes (path, additions, deletions)
4. Calculates totals
5. Outputs JSON to stdout

**What it does NOT do:**
- NO type detection (Claude will infer)
- NO scope detection (Claude will infer)
- NO caching (fresh collection every time)
- NO metadata files (Claude creates in-memory)

---

## Execute Script

**MANDATORY: Use pre-built script**

```bash
EXECUTE_SCRIPT: scripts/analysis/collect_git_diff.sh
```

**Output: Pure git data as JSON**
```json
{
  "timestamp": "2026-02-10T01:25:06Z",
  "branch": "master",
  "summary": {
    "totalFiles": 15,
    "totalAdditions": 762,
    "totalDeletions": 629
  },
  "files": [
    {
      "path": "ai/docs/claude/docs/02-capabilities/03-extended-thinking.md",
      "status": "M",
      "additions": 50,
      "deletions": 30
    }
  ]
}
```

**Script behavior:**
- ✅ Automatically runs `git add -u` (only modified files, not untracked)
- ✅ Collects staged files and statistics
- ✅ Exits with error if no changes

**Exit codes:**
- `0`: Success (JSON output)
- `1`: No changes to commit

**IMPORTANT:**
- DO NOT reconstruct bash commands from documentation
- DO NOT inline scripts into the execution flow
- ALWAYS use the pre-built script via `EXECUTE_SCRIPT:` directive
- Scripts are deterministic and consume 0 context tokens

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

## No Metadata Files

**Previous approach (removed):**
- Generated `.claude/temp/commit-execution-{timestamp}.json`
- Stored pre-computed analysis
- Required cleanup

**Current approach (simplified):**
- Git data → Claude analyzes → User interacts
- No intermediate files
- Fresh analysis every time
- Simpler workflow

---

## Token Efficiency

**Old approach (with caching):**
- First run: Heavy (analysis + metadata)
- Retry: Light (cache reuse, 95% savings)

**New approach (no caching):**
- Every run: Moderate (fresh analysis)
- Trade-off: Always current context, simpler system

**Why this is OK:**
- Commit happens once per changeset (rarely retried)
- Fresh analysis ensures latest context
- System simplicity > micro-optimization

---

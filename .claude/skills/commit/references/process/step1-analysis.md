# Commit Skill Execution Process

This document describes the detailed step-by-step execution process for the commit skill.

## Overview

The commit skill follows a 5-step process to ensure quality commits that follow project conventions:

1. Pre-validation and context gathering (**Heavy analysis + write metadata**)
2. Change analysis and violation detection (**Read metadata**)
3. Commit message generation (**Read metadata**)
4. User approval (**Read metadata**)
5. Commit execution and verification (**Read + cleanup metadata**)

**Token Optimization:**
- Step 1 analyzes once and writes `.claude/temp/commit-{timestamp}.json`
- Steps 2-5 read from metadata (67% token savings)
- See [metadata.md](../support/metadata.md) for details

**Important:** All policies (Tidy First, Logical Independence, User Communication, etc.) are defined in [SKILL.md Core Principles](../../SKILL.md#core-principles).

---

## Step 1: Pre-validation and Context Collection (Heavy Analysis)

### Collect and Verify Changes (IDE-like behavior)

**MANDATORY: Use pre-built scripts (DO NOT inline bash commands)**

**Step 1: Collect changes and create metadata**

```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
# EXECUTE_SCRIPT: scripts/analysis/create_metadata.sh

cd .claude/skills/commit && \
./scripts/analysis/collect_changes.sh | ./scripts/analysis/create_metadata.sh
```

**What these scripts do:**
- `collect_changes.sh`: Auto-stages modified files (IDE behavior), collects file list and stats
- `create_metadata.sh`: Generates execution metadata file with unique ID

**Output:** Path to metadata file
```
.claude/temp/commit-execution-20260209-123456.json
```

**Script behavior:**
- ✅ Automatically runs `git add -u` (only modified files, not untracked)
- ✅ Collects staged files, branch, and statistics
- ✅ Warns if on main/master branch
- ✅ Exits with error if no changes

**Exit codes:**
- `0`: Success (metadata created)
- `1`: No changes to commit

**IMPORTANT:**
- DO NOT reconstruct bash commands from documentation
- DO NOT inline scripts into the execution flow
- ALWAYS use the pre-built scripts via `EXECUTE_SCRIPT:` directive
- Scripts are deterministic and consume 0 context tokens

### Read Metadata (Subsequent Steps)

All steps after Step 1 read from the metadata file:

```bash
# Get execution ID from previous step output
EXECUTION_ID="20260209-123456"
METADATA_FILE=".claude/temp/commit-execution-${EXECUTION_ID}.json"

# Read analysis data
cat "$METADATA_FILE"
```

**Metadata structure:**
```json
{
  "executionId": "20260209-123456",
  "timestamp": "2026-02-09T12:34:56Z",
  "analysis": {
    "branch": "master",
    "stagedFiles": ["file1.md", "file2.md"],
    "stats": {
      "fileCount": 2,
      "insertions": 5,
      "deletions": 3
    }
  }
}
```

**Token optimization:**
- Step 1 analyzes once and writes metadata (67% token savings)
- Steps 2-5 read from metadata instead of re-analyzing

---


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
- See [metadata.md](METADATA.md) for details

**Important:** All policies (Tidy First, Logical Independence, User Communication, etc.) are defined in [SKILL.md Core Principles](SKILL.md#core-principles).

---

## Step 1: Pre-validation and Context Collection (Heavy Analysis)

### Collect and Verify Changes (IDE-like behavior)

**Collect all changes:**

Similar to IDE default behavior, automatically include modified files that haven't been explicitly staged for commit.

```bash
# Collect both staged and modified files
git diff --cached --name-only  # Staged files
git diff --name-only           # Modified files (unstaged)
```

**Auto-stage all modified files:**

```bash
# Stage all modified files automatically (IDE behavior)
git add -u
```

**Check if any changes exist:**

```bash
git diff --cached --stat
```

If no changes after staging → exit with message:
```
변경사항이 없습니다. 먼저 파일을 수정하세요.
```

**Note:**
- Untracked files (new files) are NOT automatically staged
- Only modified files are auto-staged for commit

**Additional checks:**
- Warn if on main/master branch (recommend creating feature branch)

### Collect Change Context (Parallel Execution)

After auto-staging all modified files:

- Get list of changed files (`git diff --cached --name-only`)
- Get change statistics (`git diff --cached --stat`)
- Get detailed diff (`git diff --cached`)
- Identify primary module or modified files

### Determine Scope

- Use module name if changes are in logical module (e.g., `spring-batch`, `spring-security`)
- Use primary filename if single file or related files
- Choose most important file if multiple unrelated files

### **Generate Metadata File (Important)**

Save analysis results to `.claude/temp/commit-execution-{executionId}.json`:

```bash
mkdir -p .claude/temp
EXECUTION_ID=$(date +%Y%m%d-%H%M%S)

cat > .claude/temp/commit-execution-${EXECUTION_ID}.json <<EOF
{
  "executionId": "${EXECUTION_ID}",
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "analysis": {
    "stagedFiles": [...],
    "groups": [...],
    "violations": {...}
  }
}
EOF
```

**Important:** executionId is the /commit execution ID, not CLI session ID.
- Multiple /commit executions possible in same CLI session
- Each execution generates new executionId

All subsequent steps read from this file (token savings)

Use parallel bash commands for efficiency

---


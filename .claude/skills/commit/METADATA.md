# Commit Session Metadata

Token optimization through session state persistence.

---

## Problem

Current flow causes excessive token usage:
- Step 1: Analyze files, detect groups
- Step 2: User selects split mode → **Re-analyze?**
- Step 3: Generate messages → **Re-analyze groups?**
- Step 4: User approves → **Re-load context?**

Each step re-processes the same information.

---

## Solution: Session Metadata File

### File Location

```
.claude/temp/commit-execution-{timestamp}.json
```

Example: `.claude/temp/commit-execution-20260203-180000.json`

**Important Distinction:**
- **CLI Session ID**: Assigned when you run `claude` command (persists across multiple /commit calls)
- **Commit Execution ID**: Created each time you run `/commit` (unique per commit operation)

We use **timestamp-based execution ID** because:
- Same CLI session may run `/commit` multiple times
- Each `/commit` execution is independent
- No conflict between multiple commits in same session

**Example Flow:**
```bash
# Start CLI session (session-abc123)
$ claude

# Modify files, stage, commit
user> git add file1.js
user> /commit
→ Creates: commit-execution-20260203-180000.json
→ Commit succeeds, file deleted

# Modify more files, stage, commit again (SAME CLI session)
user> git add file2.js
user> /commit
→ Creates: commit-execution-20260203-180500.json (NEW file)
→ Commit succeeds, file deleted

# Multiple /commit calls = multiple execution IDs
```

### File Structure

```json
{
  "executionId": "20260203-180000",
  "timestamp": "2026-02-03T18:00:00Z",
  "phase": "message-generation",
  "analysis": {
    "stagedFiles": {
      "count": 82,
      "list": [
        ".claude/skills/commit/SKILL.md",
        ".claude/skills/commit/RULES.md",
        "ai/docs/claude/docs/01-build-with-claude/01-features-overview.md"
      ]
    },
    "stats": {
      "totalLines": 2847,
      "additions": 2847,
      "deletions": 0
    },
    "violations": {
      "tidyFirst": false,
      "logicalIndependence": true
    },
    "groups": [
      {
        "id": 1,
        "directory": ".claude/skills/commit/",
        "fileCount": 4,
        "scope": "commit-skill",
        "type": "docs",
        "files": [
          ".claude/skills/commit/SKILL.md",
          ".claude/skills/commit/RULES.md",
          ".claude/skills/commit/EXAMPLES.md",
          ".claude/skills/commit/TROUBLESHOOTING.md"
        ],
        "suggestedMessages": [
          {
            "rank": 1,
            "header": "docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가",
            "body": "- SKILL.md: 스킬 실행 프로세스 정의\n- RULES.md: 커밋 메시지 형식 규칙\n- EXAMPLES.md: 실제 사용 예시\n- TROUBLESHOOTING.md: 문제 해결 가이드",
            "footer": null
          },
          {
            "rank": 2,
            "header": "docs(commit-skill): 커밋 스킬 문서 추가",
            "body": "- 커밋 자동화 스킬 문서\n- 메시지 형식 규칙 정의",
            "footer": null
          }
        ]
      },
      {
        "id": 2,
        "directory": "ai/docs/claude/",
        "fileCount": 70,
        "scope": "claude-api",
        "type": "docs",
        "files": ["..."],
        "suggestedMessages": [...]
      },
      {
        "id": 3,
        "directory": ".claude/agents/",
        "fileCount": 8,
        "scope": "korean-translator",
        "type": "docs",
        "files": ["..."],
        "suggestedMessages": [...]
      }
    ]
  },
  "userChoices": {
    "splitMode": "auto-split",
    "priority": [1, 3, 2],
    "currentGroup": 2,
    "groupResults": [
      {
        "groupId": 1,
        "status": "completed",
        "commitHash": "abc1234",
        "message": "docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가"
      },
      {
        "groupId": 3,
        "status": "failed",
        "error": "Hook validation failed"
      }
    ]
  }
}
```

---

## Process Flow with Metadata

### Step 1: Initial Analysis (Heavy)

```
1. Generate execution ID (timestamp)
2. Run git commands (status, diff, log)
3. Analyze staged files
4. Detect violations (Tidy First, logical independence)
5. Identify groups
6. Generate 5 messages per group
7. WRITE metadata file with executionId
```

**Token usage: HIGH** (first time only per /commit execution)

### Step 2-N: Read from Metadata (Light)

```
1. READ metadata file using executionId
2. Get pre-analyzed groups
3. Get pre-generated messages
4. Show to user
5. UPDATE metadata with choice
```

**Token usage: LOW** (file read only)

**Note:** Each `/commit` execution gets its own executionId, even in the same CLI session.

### Cleanup

```
On completion or cancellation:
- DELETE metadata file for this execution
- Other commit executions' metadata files are unaffected
```

**Multiple Executions:**
If you run `/commit` 3 times in one CLI session:
1. First: Creates & deletes `commit-execution-20260203-180000.json`
2. Second: Creates & deletes `commit-execution-20260203-180500.json`
3. Third: Creates & deletes `commit-execution-20260203-181000.json`

Each execution is completely independent.

---

## Benefits

### 1. Token Savings

**Before:**
- Step 1: 5000 tokens
- Step 2: 5000 tokens (re-analyze)
- Step 3: 5000 tokens (re-analyze)
- Step 4: 5000 tokens (re-analyze)
- **Total: 20,000 tokens**

**After:**
- Step 1: 5000 tokens + write file
- Step 2: 500 tokens (read file)
- Step 3: 500 tokens (read file)
- Step 4: 500 tokens (read file)
- **Total: 6,500 tokens (67% savings)**

### 2. Consistency

All steps use the same analysis results - no discrepancies.

### 3. Resume Capability

If process fails, can resume from metadata file.

### 4. Debug/Audit

Can review what was analyzed and chosen.

---

## Implementation Changes

### PROCESS.md Updates

**Step 1: Initial Analysis**
```
1. Analyze staged files
2. Detect violations
3. Identify groups
4. Generate all messages upfront
5. WRITE .claude/temp/commit-{timestamp}.json
6. Store sessionId in memory
```

**Step 2-N: Use Metadata**
```
1. READ .claude/temp/commit-{sessionId}.json
2. Load pre-computed groups and messages
3. Present to user
4. UPDATE file with user choices
```

**Cleanup**
```
On success/cancel/error:
- DELETE .claude/temp/commit-{sessionId}.json
```

---

## File Lifecycle

### Per /commit Execution

```
START (/commit invoked)
  ↓
Generate executionId (timestamp)
  ↓
Step 1: CREATE commit-execution-{executionId}.json
  ↓
Step 2: READ + UPDATE (user choice)
  ↓
Step 3: READ (show messages)
  ↓
Step 4: READ + UPDATE (commit result)
  ↓
Step N: READ (final summary)
  ↓
END: DELETE commit-execution-{executionId}.json
```

### Across Multiple /commit Executions (Same CLI Session)

```
CLI Session Start
  ↓
User: /commit (execution-1)
  → commit-execution-20260203-180000.json
  → [lifecycle completes]
  → [file deleted]
  ↓
User: /commit (execution-2)
  → commit-execution-20260203-180500.json (NEW file)
  → [lifecycle completes]
  → [file deleted]
  ↓
User: /commit (execution-3)
  → commit-execution-20260203-181000.json (NEW file)
  → [lifecycle completes]
  → [file deleted]
```

Each execution is isolated.

---

## Metadata File Location

```
.claude/
├── temp/
│   ├── commit-execution-20260203-180000.json  ← /commit execution 1
│   ├── commit-execution-20260203-180500.json  ← /commit execution 2
│   ├── commit-execution-20260203-181000.json  ← /commit execution 3
│   └── .gitignore                              ← Ignore *.json
```

**Note:** Multiple files may exist temporarily if:
- Multiple `/commit` executions overlap
- Previous cleanup failed (rare)

Normally only one file exists at a time per execution.

Add to `.gitignore`:
```
.claude/temp/*.json
```

---

## Alternative: In-Memory State

Could use conversation state instead of files, but:
- ❌ Lost on error/crash
- ❌ Can't resume
- ❌ Can't debug
- ❌ Still needs re-analysis if context cleared

File-based is better for reliability.

---

## Implementation Priority

**High Impact Changes:**
1. Create metadata file in Step 1
2. Read metadata in subsequent steps
3. Cleanup on completion

**Medium Impact:**
4. Resume from metadata on error
5. Audit/debug commands

---

## Example Commands

### Create temp directory (once per repository)
```bash
mkdir -p .claude/temp
echo "*.json" > .claude/temp/.gitignore
```

### Write metadata (Step 1 - per /commit execution)
```bash
# Generate execution ID
EXECUTION_ID=$(date +%Y%m%d-%H%M%S)

# Write metadata
cat > .claude/temp/commit-execution-${EXECUTION_ID}.json <<EOF
{
  "executionId": "${EXECUTION_ID}",
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "analysis": { ... }
}
EOF
```

### Read metadata (Step 2+ - same execution)
```bash
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
```

### Cleanup (end of execution)
```bash
rm .claude/temp/commit-execution-${EXECUTION_ID}.json
```

### Verify no orphaned files (optional)
```bash
# List all commit execution files
ls -la .claude/temp/commit-execution-*.json

# Clean up old files (if cleanup failed)
find .claude/temp -name "commit-execution-*.json" -mtime +1 -delete
```

---

## Related Documents

- [PROCESS.md](PROCESS.md) - Update with metadata flow
- [SKILL.md](SKILL.md) - Note optimization strategy

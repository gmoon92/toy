# Commit Session Metadata

Token optimization through session state persistence.

---

## Overview

Each `/commit` execution creates a unique metadata file to avoid re-analyzing the same data across steps.

**Token savings: ~67%** (analyze once, reuse multiple times)

---

## File Location

```
.claude/temp/commit-execution-{timestamp}.json
```

**Example**: `.claude/temp/commit-execution-20260203-180000.json`

**Lifecycle**:
- **CREATE**: Step 1 (Initial Analysis)
- **READ**: Steps 2-N (Message generation, approval, execution)
- **DELETE**: After commit completion or cancellation

**Execution ID**: Timestamp-based (unique per `/commit` invocation)
- Same CLI session can have multiple `/commit` executions
- Each execution has its own metadata file
- No conflicts between concurrent commits

---

## File Structure

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
        ".claude/skills/commit/references/validation/rules.md"
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
        "files": ["..."],
        "suggestedMessages": [
          {
            "rank": 1,
            "header": "docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가",
            "body": "- SKILL.md: 스킬 실행 프로세스 정의\n- references/validation/rules.md: 커밋 메시지 형식 규칙",
            "footer": null
          }
        ]
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
        "commitHash": "abc1234"
      }
    ]
  }
}
```

---

## Usage Pattern

### Step 1: Create Metadata (Heavy)

```bash
# Generate execution ID
EXECUTION_ID=$(date +%Y%m%d-%H%M%S)

# Analyze and write metadata
EXECUTE_SCRIPT: scripts/analysis/create_metadata.sh
```

**Operations:**
- Analyze staged files
- Detect violations
- Identify groups
- Generate message candidates
- Write metadata file

**Token cost**: HIGH (first time only)

### Steps 2-N: Read Metadata (Light)

```bash
# Read pre-computed data
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
```

**Operations:**
- Load groups and messages
- Present to user
- Update with choices

**Token cost**: LOW (file read only)

### Cleanup: Delete Metadata

```bash
# After completion
EXECUTE_SCRIPT: scripts/utils/cleanup_metadata.sh ${EXECUTION_ID}
```

**When:**
- Commit success
- User cancellation
- Error/failure

---

## Directory Structure

```
.claude/
├── temp/
│   ├── commit-execution-20260203-180000.json  ← Execution 1
│   ├── commit-execution-20260203-180500.json  ← Execution 2
│   └── .gitignore                              ← Ignore *.json
```

**Add to `.gitignore`:**
```
.claude/temp/*.json
```

---

## Scripts Integration

**Create metadata** (Step 1):
```bash
EXECUTE_SCRIPT: scripts/analysis/create_metadata.sh
# Returns: .claude/temp/commit-execution-{timestamp}.json
```

**Read metadata** (Steps 2-N):
```bash
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
```

**Cleanup** (End of execution):
```bash
EXECUTE_SCRIPT: scripts/utils/cleanup_metadata.sh ${EXECUTION_ID}
```

**Verify/cleanup orphaned files**:
```bash
# List execution files
ls -la .claude/temp/commit-execution-*.json

# Remove old files (if cleanup failed)
find .claude/temp -name "commit-execution-*.json" -mtime +1 -delete
```

---

## Related Documents

- **[process/step1-analysis.md](../process/step1-analysis.md)** - Metadata creation
- **[process/step2-violations.md](../process/step2-violations.md)** - Metadata read and reuse
- **[process/step5-execute.md](../process/step5-execute.md)** - Metadata cleanup
- **[scripts/README.md](../../scripts/README.md)** - Script usage details

# Commit Skill Scripts

Simple, deterministic scripts for commit workflow.

## Overview

**Design Principles:**
- ✅ **Minimal scripts** - Only essential operations
- ✅ **No inference** - Scripts collect data, Claude analyzes
- ✅ **No caching** - Fresh analysis every time
- ✅ **Deterministic** - Same input = same output

---

## Directory Structure

```
scripts/
├── analysis/           # Git data collection (Bash)
│   └── collect_git_diff.sh
├── validation/         # Message format validation (Python)
│   └── validate_message.py
└── execution/          # Commit execution (Bash)
    └── commit.sh
```

**Removed directories:**
- `algorithms/` - Type/scope detection moved to Claude
- `generation/` - Message generation moved to Claude
- `telemetry/` - Removed with caching

---

## analysis/collect_git_diff.sh

**Purpose:** Collect pure git diff data (NO caching, NO inference)

**Usage:**
```bash
./scripts/analysis/collect_git_diff.sh
```

**Operations:**
1. Auto-stage modified files (`git add -u`)
2. Check for staged changes
3. Collect git metadata (branch, timestamp)
4. Extract file changes (path, additions, deletions)
5. Calculate totals
6. Output as JSON

**Output:**
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

**What it does NOT do:**
- NO type detection (Claude infers from file paths/content)
- NO scope detection (Claude infers from directory structure)
- NO caching (fresh collection every time)
- NO pre-analysis (pure git data only)

---

## validation/validate_message.py

**Purpose:** Validate commit message format

**Usage:**
```bash
echo "feat(module): message" | python scripts/validation/validate_message.py
```

**Validates:**
- Header format: `<type>(<scope>): <message>`
- Valid types: feat, fix, refactor, test, docs, style, chore
- Scope format: alphanumeric with dots, dashes, underscores
- Message: starts with lowercase, no period at end

**Exit codes:**
- 0: Valid
- 1: Invalid (with error message)

---

## execution/commit.sh

**Purpose:** Execute git commit

**Usage:**
```bash
./scripts/execution/commit.sh "commit message here"
```

**Operations:**
1. Validate message format (calls validate_message.py)
2. Execute `git commit -m "message"`
3. Return commit hash or error

**Exit codes:**
- 0: Success
- 1: Validation failed
- 2: Commit failed

---

## Workflow

```
1. collect_git_diff.sh
   ↓ Pure git data (JSON)

2. Claude analyzes (NO scripts)
   ↓ Infers type, scope, groups
   ↓ Generates message candidates
   ↓ Detects violations

3. User selects message
   ↓ 3-stage selection

4. validate_message.py
   ↓ Format check

5. commit.sh
   ↓ Execute commit
```

---

## Key Changes from Previous Version

**Removed:**
- ❌ `analyze_with_cache.sh` - Caching removed
- ❌ `validate_cache.sh` - Caching removed
- ❌ `decide_mode.sh` - Caching removed
- ❌ `create_metadata.sh` - Metadata now created by Claude
- ❌ `collect_changes.sh` - Merged into collect_git_diff.sh
- ❌ `algorithms/detect_type.js` - Claude infers type
- ❌ `algorithms/detect_scope.js` - Claude infers scope
- ❌ `generation/generate_body_items.js` - Claude generates
- ❌ `generation/generate_headers.js` - Claude generates
- ❌ `telemetry/` - Removed with caching
- ❌ `utils/cleanup_sessions.sh` - Session caching removed
- ❌ `utils/init_session.sh` - Session management removed
- ❌ `utils/get_session_id.sh` - Session management removed

**Why removed:**
- **Simplification**: Reduced from 15+ scripts to 4 scripts
- **Claude's strength**: Type/scope inference better with context understanding
- **No caching**: Fresh analysis reflects latest context
- **No scores**: Natural prioritization instead of mechanical scoring

---

## Development

**Requirements:**
- Bash 4.0+
- Python 3.6+ (for validation only)
- Git 2.0+

**Testing:**
```bash
# Test git collection
./scripts/analysis/collect_git_diff.sh

# Test validation
echo "docs(test): test message" | python scripts/validation/validate_message.py

# Test commit (dry-run not available, use real git repo)
./scripts/execution/commit.sh "test: commit message"
```

---

## Token Efficiency

**Before (with caching):**
- First run: Heavy (analysis + metadata generation)
- Retry: Light (cache reuse, 95% token savings)

**After (no caching):**
- Every run: Moderate (fresh analysis)
- Trade-off: Always current context, simpler system

**Why this is OK:**
- Commit happens once per change set (not repeatedly)
- Fresh analysis ensures latest context
- System simplicity > micro-optimization
- Claude's analysis is fast enough

---

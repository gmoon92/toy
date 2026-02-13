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
├── execution/          # Commit execution (Bash)
│   └── commit.sh
├── utils/              # Utility functions (Bash)
│   └── resolve_path.sh
└── validation/         # Message format validation (Python)
    └── validate_message.py
```

**Removed directories:**
- `algorithms/` - Type/scope detection moved to Claude
- `generation/` - Message generation moved to Claude
- `telemetry/` - Removed with caching

---

## analysis/collect_git_diff.sh

**Purpose:** Collect git diff data with memory-based path management

**Usage:**
```bash
# All modified files
./scripts/analysis/collect_git_diff.sh

# Specific path only
./scripts/analysis/collect_git_diff.sh <path>
```

**Operations:**
1. **사전 스캔**: Scan all changed files and save to memory (`~/.claude/projects/{project}/memory/commit-skill-paths.md`)
2. **경로 해석**: Match user input with memory file list (exact → directory → partial matching)
3. **안전한 staging**: Stage only tracked files matching the path (.gitignore respected)
4. Collect git metadata (branch, timestamp)
5. Extract file changes (path, additions, deletions)
6. Calculate totals
7. Output as JSON

**Output:**
```json
{
  "timestamp": "2026-02-10T01:25:06Z",
  "branch": "master",
  "resolvedPath": "ai/docs/claude/docs/07-claude-on-3rd-party",
  "summary": {
    "totalFiles": 3,
    "totalAdditions": 150,
    "totalDeletions": 80
  },
  "files": [
    {
      "path": "ai/docs/claude/docs/07-claude-on-3rd-party/01-amazon-bedrock.md",
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
- NO cross-session caching (fresh scan every time)
- NO staging of ignored/untracked files (tracked only)
- NO auto-staging in commit phase (precise control)

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

## utils/resolve_path.sh

**Purpose:** Memory-based path resolution utilities

**Usage:**
```bash
# Direct execution
./scripts/utils/resolve_path.sh scan [memory_file]
./scripts/utils/resolve_path.sh resolve <input> [memory_file]
./scripts/utils/resolve_path.sh classify <files>
./scripts/utils/resolve_path.sh stage <files>
./scripts/utils/resolve_path.sh match <input> [memory_file]

# Source for functions
source scripts/utils/resolve_path.sh
scan_and_save_to_memory "$MEMORY_FILE"
resolve_path_with_memory "$INPUT" "$MEMORY_FILE"
```

**Functions:**
- `scan_and_save_to_memory()` - Scan git status and save to memory file
- `resolve_path_with_memory()` - Resolve user input to actual path using memory
- `classify_files()` - Classify files as TRACKED/UNTRACKED/IGNORED
- `stage_files_safely()` - Stage only tracked files with warnings
- `get_matching_files_from_memory()` - Get all files matching input pattern

**Memory File Structure:**
```markdown
# Commit Skill - 변경 파일 목록

## 현재 변경사항 (2026-02-13 14:30:00)

### Modified Files (Unstaged)
- path/to/file1.md
- path/to/file2.sh

### Staged Files
- already/staged/file.java

### Untracked Files
- new/untracked/file.txt
```

**Path Resolution Priority:**
1. Exact file match from memory
2. Exact directory match
3. Path component matching
4. Substring matching
5. Fallback to traditional resolution

---

## execution/commit.sh

**Purpose:** Execute git commit (staged files only)

**Usage:**
```bash
# Via stdin (recommended)
echo "commit message" | ./scripts/execution/commit.sh
echo "commit message" | ./scripts/execution/commit.sh <path>
```

**Operations:**
1. Validate message format (calls validate_message.py)
2. Verify there are staged files to commit
3. Execute `git commit -m "message"` (NO path argument)
4. Return commit hash or error

**Important:** This script only commits already staged files. The path argument (if provided) is used for logging only, not passed to `git commit`. This prevents accidental staging of unstaged files.

**Exit codes:**
- 0: Success
- 1: Validation failed, no staged files, or commit failed

---

## Workflow

```
1. collect_git_diff.sh
   ├─ 1. Scan all changes to memory
   ├─ 2. Match user input with memory
   ├─ 3. Stage only matching tracked files
   └─ ↓ Pure git data (JSON)

2. Claude analyzes (NO scripts)
   ↓ Infers type, scope, groups
   ↓ Generates message candidates
   ↓ Detects violations

3. User selects message
   ↓ 3-stage selection

4. validate_message.py
   ↓ Format check

5. commit.sh (staged files only)
   ↓ Execute commit
```

---

## Key Changes from Previous Version

**Added (Memory-Based System):**
- ✅ `utils/resolve_path.sh` - Memory-based path resolution utilities
- ✅ Memory file: `~/.claude/projects/{project}/memory/commit-skill-paths.md`
- ✅ 5-step path matching: Exact → Directory → Partial → Substring → Fallback
- ✅ File classification: TRACKED/UNTRACKED/IGNORED with .gitignore protection
- ✅ Safe staging: Only tracked files staged automatically

**Modified:**
- 🔄 `collect_git_diff.sh` - Added memory-based pre-scan and path resolution
- 🔄 `commit.sh` - Now only commits staged files (no path argument to git commit)

**Why changed:**
- **Precise control**: Only specified files staged via memory matching
- **.gitignore protection**: Ignored/untracked files auto-excluded
- **No accidental commits**: commit.sh doesn't auto-stage unstaged files
- **Fresh analysis**: Memory rebuilt every run, no stale data

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

# Step 1: Initial Analysis

Collect pure git diff data with memory-based path management.

---

## Overview

**Phase 1 executes a single script:**
- `collect_git_diff.sh` - Collects pure git data with memory-based pre-scan

**What it does:**
1. **사전 스캔**: Scan all changed files and save to memory
2. **경로 해석**: Match user input with memory-based file list
3. **정확한 파일 지정**: Stage only matching files (tracked only)
4. **Collects git metadata** (branch, timestamp)
5. **Extracts file changes** (path, additions, deletions)
6. **Calculates totals**
7. **Outputs JSON to stdout**

**What it does NOT do:**
- NO type detection (Claude will infer)
- NO scope detection (Claude will infer)
- NO caching across sessions (fresh scan every time)
- NO staging of ignored/untracked files (.gitignore respected)

---

## Memory-Based Pre-Scan System

### Concept
The skill uses a memory-based file path management system to ensure precise file selection:

```
/commit ai/docs/claude/docs/07-claude-on-3rd-party
    ↓
1. 사전 스캔: git status로 전체 변경 파일 목록 수집
2. 메모리 저장: commit-skill-paths.md에 파일 목록 저장
3. 경로 매칭: 사용자 입력과 메모리의 파일 목록 매칭
4. 정확한 파일 지정: git add <매칭된 파일1> <매칭된 파일2> ...
5. 커밋: git commit -m "..." (path 인자 없이, staging된 파일만)
```

### Memory File Structure

**Location**: `~/.claude/projects/{project}/memory/commit-skill-paths.md`

```markdown
# Commit Skill - 변경 파일 목록

## 현재 변경사항 (2026-02-13 14:30:00)

### Modified Files (Unstaged)
- ai/docs/claude/docs/07-claude-on-3rd-party/01-amazon-bedrock.md
- ai/docs/claude/docs/07-claude-on-3rd-party/02-microsoft-foundry.md
- .claude/skills/commit/scripts/analysis/collect_git_diff.sh

### Staged Files
- (없음)

### Untracked Files
- .claude/agents/codegen/
```

---

## Execute Script

**MANDATORY: Use pre-built script**

```bash
# All modified files (default)
EXECUTE_SCRIPT: scripts/analysis/collect_git_diff.sh

# Only specific path
EXECUTE_SCRIPT: scripts/analysis/collect_git_diff.sh <path>
```

**Output: Pure git data as JSON**
```json
{
  "timestamp": "2026-02-10T01:25:06Z",
  "branch": "master",
  "resolvedPath": "ai/docs/claude/docs/07-claude-on-3rd-party",
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
- ✅ 사전 스캔: Saves all changed files to memory before staging
- ✅ 정확한 파일 지정: Only stages files matching the specified path
- ✅ Tracked files only: Respects .gitignore (no staging of ignored files)
- ✅ Exits with error if no changes in specified path

### Path Resolution with Memory

**5-Step Matching Process:**

1. **정확한 파일 일치**: Exact file path match from memory
2. **디렉토리 정확 일치**: Exact directory match
3. **메모리 기반 부분 매칭**: Match by path components
4. **전체 경로 부분 매칭**: Substring match in full paths
5. **Fallback to traditional**: Legacy resolution as last resort

**Example:**
```bash
# Input: "07-claude-on-3rd-party"
# Memory matches:
#   - ai/docs/claude/docs/07-claude-on-3rd-party/01-amazon-bedrock.md
#   - ai/docs/claude/docs/07-claude-on-3rd-party/02-microsoft-foundry.md
# Result: Only these 2 files are staged
```

### .gitignore Protection

**Double-Check Mechanism:**

```bash
# Files are classified before staging:
- TRACKED: Safe to commit (git ls-files confirms)
- UNTRACKED: Requires user confirmation
- IGNORED: Auto-excluded (git check-ignore)

# Only TRACKED files are staged automatically
```

**Warnings:**
- `WARN:UNTRACKED_FILES:file1 file2` - Untracked files detected
- `INFO:IGNORED_FILES:file1 file2` - Ignored files auto-excluded

### Duplicate Matching Detection

When multiple paths match the input:
```
WARN: 여러 경로가 매칭됩니다. 첫 번째 항목을 사용합니다.
```

The script will:
1. List all matching paths to stderr
2. Use the first match for staging
3. Continue with warning (does not fail)

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

## Memory Management

**Memory file location:**
```
~/.claude/projects/{project-name}/memory/commit-skill-paths.md
```

**Characteristics:**
- Fresh scan on every execution
- No persistence across commit skill runs
- Used only for precise path matching during current run
- Automatically cleaned up (overwritten each run)

**Why this approach:**
- Prevents accidental staging of wrong files
- Enables precise partial commits
- Maintains .gitignore protection
- No complex caching logic needed

---

## Token Efficiency

**Memory-based approach:**
- Scan: O(n) where n = number of changed files
- Matching: O(n) with early exit on exact match
- Storage: File-based (0 context tokens)
- Deterministic and predictable

**Why this is OK:**
- File I/O is fast for typical change sets
- Precise matching prevents costly mistakes
- No context token overhead

---

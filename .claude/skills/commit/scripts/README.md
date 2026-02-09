# Commit Skill Scripts

Deterministic, executable scripts organized by responsibility.

## Overview

Leverages **Level 3: Resources and Code** of Agent Skills architecture:
- ✅ **No context consumption** - Executed via bash, only output included in context
- ✅ **Deterministic** - Always produces same output for same input
- ✅ **Fast execution** - Direct execution without LLM inference

---

## Directory Structure

```
scripts/
├── analysis/           # Change analysis and metadata generation (Bash)
│   ├── collect_changes.sh
│   └── create_metadata.sh
├── algorithms/         # Detection algorithms (Node.js)
│   ├── detect_scope.js
│   └── detect_type.js
├── generation/         # Message generation (Node.js)
│   └── generate_headers.js
├── validation/         # Message format validation (Python)
│   └── validate_message.py
├── execution/          # Commit execution (Bash)
│   └── commit.sh
└── utils/              # Utility scripts (Bash)
    └── cleanup_metadata.sh
```

---

## Node.js Scripts (algorithms/ and generation/)

**Prerequisites:** Node.js 14+ required

**Common Pattern:**
- Input: JSON via stdin
- Output: JSON to stdout
- Exit code: 0 (success) or 1 (error)

---

## algorithms/detect_scope.js

**Purpose:** Detect optimal scope from changed files

**Usage:**
```bash
echo '{"files":["file1.md","file2.md"]}' | node scripts/algorithms/detect_scope.js
```

**Input:**
```json
{
  "files": ["path/to/file1", "path/to/file2"]
}
```

**Output:**
```json
{
  "primary": {
    "scope": "commit-skill",
    "type": "module",
    "confidence": "high"
  },
  "alternatives": [
    {"scope": "file1.md", "type": "file", "confidence": "low"}
  ],
  "fileCount": 2
}
```

**Algorithm:**
1. Single file → filename
2. Multiple files → detect module name
3. Fallback → common directory name

---

## algorithms/detect_type.js

**Purpose:** Detect commit type from files and diff

**Usage:**
```bash
echo '{"files":["test.md"],"diff":"fix bug"}' | node scripts/algorithms/detect_type.js
```

**Input:**
```json
{
  "files": ["path/to/file"],
  "diff": "git diff output (optional)"
}
```

**Output:**
```json
{
  "primary": {
    "type": "docs",
    "confidence": "high"
  },
  "alternatives": [
    {"type": "chore", "confidence": "low"}
  ]
}
```

**Detection priority:**
1. Test files only → `test`
2. Docs files only → `docs`
3. Bug fix patterns in diff → `fix`
4. New feature patterns → `feat`
5. Refactoring patterns → `refactor`
6. Default → `chore`

---

## generation/generate_headers.js

**Purpose:** Generate 5 commit header messages (2 recommended + 3 general)

**Usage:**
```bash
# With pre-analyzed scope and type
echo '{"files":["f1","f2"],"scope":{...},"type":{...}}' | node scripts/generation/generate_headers.js

# Auto-analyze
echo '{"files":["f1.md","f2.md"],"diff":"..."}' | node scripts/generation/generate_headers.js
```

**Input:**
```json
{
  "files": ["file1", "file2"],
  "diff": "git diff output (optional)",
  "scope": "pre-analyzed scope (optional)",
  "type": "pre-analyzed type (optional)"
}
```

**Output:**
```json
{
  "recommended": [
    {
      "message": "docs(commit-skill): 문서 업데이트",
      "rank": 1,
      "label": "추천"
    },
    {
      "message": "refactor(commit-skill): 구조 개선",
      "rank": 2,
      "label": "추천"
    }
  ],
  "general": [
    {
      "message": "docs(file1.md): 문서 수정",
      "rank": 3,
      "label": "일반"
    },
    ...
  ]
}
```

**Generation strategy:**
- **Recommended 1**: Optimal scope + primary type + clear description
- **Recommended 2**: Alternative scope or type
- **General 3-5**: Variations of scope/type combinations

---

## Bash Scripts

---

## analysis/collect_changes.sh

**Purpose:** Auto-stage modified files and collect change information

**Usage:**
```bash
./scripts/analysis/collect_changes.sh
```

**Output:** JSON format
```json
{
  "branch": "master",
  "stagedFiles": ["file1.md", "file2.md"],
  "stats": {
    "fileCount": 2,
    "insertions": 5,
    "deletions": 3
  }
}
```

**Exit Codes:**
- `0`: Success (changes collected)
- `1`: No changes to commit

**Notes:**
- Automatically runs `git add -u` (IDE behavior)
- Only stages modified files, not untracked files
- Warns if on main/master branch

---

## analysis/create_metadata.sh

**Purpose:** Create commit execution metadata file

**Usage:**
```bash
# From collect_changes.sh output
./scripts/analysis/collect_changes.sh | ./scripts/analysis/create_metadata.sh

# Or directly with JSON
echo '{"branch":"master"}' | ./scripts/analysis/create_metadata.sh
```

**Output:** Path to created metadata file
```
.claude/temp/commit-execution-20260209-123456.json
```

**Metadata File Structure:**
```json
{
  "executionId": "20260209-123456",
  "timestamp": "2026-02-09T12:34:56Z",
  "analysis": {
    "branch": "master",
    "stagedFiles": [...],
    "stats": {...}
  }
}
```

**Notes:**
- Creates unique execution ID per /commit invocation
- Stores in `.claude/temp/` directory
- Used by subsequent phases to avoid re-analysis

---

## validation/validate_message.py

**Purpose:** Validate commit message format and detect forbidden patterns

**Usage:**
```bash
# From file
python3 scripts/validation/validate_message.py --message /tmp/commit-msg.txt --json

# From stdin
echo "feat(auth): add JWT" | python3 scripts/validation/validate_message.py --stdin --json
```

**Exit Codes:**
- `0`: Valid message
- `1`: Invalid message (errors found)
- `2`: File not found or read error

**Validates:**
- ✅ Header format: `<type>(scope): <message>`
- ✅ Body format: Lines start with `- ` (if present)
- ✅ Footer format: Issue reference or Breaking Change only
- ❌ **FORBIDDEN**: Co-Authored-By watermarks
- ❌ **FORBIDDEN**: AI attribution footers

**Output (Valid):**
```json
{
  "status": "valid",
  "message": "✅ Commit message is valid"
}
```

**Output (Invalid):**
```json
{
  "status": "invalid",
  "message": "❌ Commit message validation failed",
  "errors": [
    "FORBIDDEN at line 6: Co-Authored-By watermark is forbidden"
  ]
}
```

**Detailed Usage:**
```bash
python3 scripts/validation/validate_message.py --help
```

---

## execution/commit.sh

**Purpose:** Validate and execute git commit

**Usage:**
```bash
# Commit message via stdin
echo "feat(auth): add JWT

- Implement JWT authentication
- Add token validation" | ./scripts/execution/commit.sh
```

**Process:**
1. Read commit message from stdin
2. Validate using `validate_message.py`
3. Execute `git commit` if validation passes
4. Return commit hash and subject

**Output (Success):**
```
{
  "status": "valid",
  "message": "✅ Commit message is valid"
}
---COMMIT_SUCCESS---
HASH: abc1234
SUBJECT: feat(auth): add JWT
```

**Exit Codes:**
- `0`: Commit successful
- `1`: Validation failed or commit failed

**Notes:**
- Automatically validates before commit
- Returns structured output for parsing
- Preserves git hook behavior (pre-commit, commit-msg)

---

## utils/cleanup_metadata.sh

**Purpose:** Clean up commit execution metadata files

**Usage:**
```bash
# Clean specific execution
./scripts/utils/cleanup_metadata.sh 20260209-123456

# Clean all metadata files
./scripts/utils/cleanup_metadata.sh all
```

**Notes:**
- Removes `.claude/temp/commit-execution-*.json` files
- Call after successful commit
- Safe to call even if file doesn't exist

---

## Script Execution Guidelines

### For Claude Agent

When executing commit skill phases, use these **EXPLICIT SCRIPT REFERENCES**:

**Phase 1 - Analysis:**
```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
./scripts/analysis/collect_changes.sh > /tmp/changes.json

# EXECUTE_SCRIPT: scripts/analysis/create_metadata.sh
cat /tmp/changes.json | ./scripts/analysis/create_metadata.sh
```

**Phase 3 - Message Generation (Optional - Use Node.js scripts):**
```bash
# EXECUTE_SCRIPT: scripts/algorithms/detect_scope.js
echo '{"files":["..."]}'  | node scripts/algorithms/detect_scope.js > /tmp/scope.json

# EXECUTE_SCRIPT: scripts/algorithms/detect_type.js
echo '{"files":["..."],"diff":"..."}'  | node scripts/algorithms/detect_type.js > /tmp/type.json

# EXECUTE_SCRIPT: scripts/generation/generate_headers.js
cat /tmp/metadata.json | node scripts/generation/generate_headers.js > /tmp/headers.json
```

**Phase 5 - Execution:**
```bash
# EXECUTE_SCRIPT: scripts/execution/commit.sh
echo "$COMMIT_MESSAGE" | ./scripts/execution/commit.sh

# EXECUTE_SCRIPT: scripts/utils/cleanup_metadata.sh
./scripts/utils/cleanup_metadata.sh "$EXECUTION_ID"
```

**IMPORTANT:**
- **DO NOT** inline bash commands from documentation
- **DO NOT** reconstruct scripts from descriptions
- **ALWAYS** use `EXECUTE_SCRIPT:` directive to reference script files
- **TRUST** script output without verification

---

## References

- [Agent Skills Documentation](https://platform.claude.com/docs/ko/agents-and-tools/agent-skills/overview)
- [Commit Skill Design Document](../SKILL.md)
- [Step 1: Analysis Process](../references/process/step1-analysis.md)
- [Step 5: Execution Process](../references/process/step5-execute.md)

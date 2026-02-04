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
- See [METADATA.md](METADATA.md) for details

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

## Step 2: Analyze Changes and Detect Violations (Read Metadata)

**Read metadata:**
```bash
# Read pre-analyzed data using executionId
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
```

### Determine Commit Type

| Change Type | Type |
|----------|------|
| New feature | feat |
| Bug/error fix | fix |
| Method extraction, renaming (no behavior change) | refactor |
| Test code | test |
| Documentation | docs |
| Code formatting only | style |
| Build config, dependencies | chore |

### Detect Tidy First Violation

When structural changes (refactor) and behavioral changes (feat/fix) are mixed:

**Template:** [templates/template-1-tidy-first.md](templates/template-1-tidy-first.md)

**Actions:**
1. Display the "Screen Output" section from template (warning message with detected mixed changes)
2. Call AskUserQuestion tool with "Template" JSON from template
3. Process user selection:
   - If reset selected: Execute `git reset HEAD`, guide separation method, and exit
   - If proceed selected: Continue with dominant type (show warning message)

### Verify Logical Independence (Important)

Separate logically independent changes even if same type:

**When separation is needed:**
- Changes with different purposes
- Changes that can be reviewed independently
- Files in different contexts

**Example:**
```
❌ 한 커밋에 통합 (잘못됨):
docs(claude): Claude API 문서 및 커밋 스킬 추가
- .claude/skills/commit/ (커밋 스킬 문서)
- ai/docs/claude/ (API 문서 번역)
→ 커밋 스킬과 API 문서는 서로 다른 목적

✅ 분리된 커밋 (올바름):
Commit 1: docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가
Commit 2: docs(claude-api): Claude API 문서 번역 추가
```

**Verification procedure:**
1. Analyze directory structure of changed files
2. Identify logically independent groups
3. Warn if 10+ files or different top-level directories

**Template:** [templates/template-2-logical-independence.md](templates/template-2-logical-independence.md)

**Actions:**
1. Display the "Screen Output" section from template (detected groups with details and warning)
2. Call AskUserQuestion tool with "Template" JSON from template
3. Process user selection (see User Actions below)

**User Actions:**
- Select "Auto-split" → See **[AUTO_SPLIT.md](AUTO_SPLIT.md)** (auto-split commit process)
- Select "Unified commit" → Show warning, request confirmation, proceed to Step 3
- Select "Cancel" → Exit process

---

## Step 3: Generate Commit Message (Read Metadata)

**Read metadata:**
```bash
# Read pre-generated messages
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
# Use groups[].suggestedMessages
```

### Format

`<type>(module|filename): <brief description>`

### 5 Message Generation Strategy

Generate **5 commit messages** for each group.

1. **Message 1 (추천)**: 최적의 scope + 명확한 표현
2. **Message 2**: Scope 변형 (모듈명 ↔ 파일명)
3. **Message 3**: Message 표현 변형 (간결/상세/다른 관점)
4. **Message 4**: Body 상세도 조정 (추가/제거/변경)
5. **Message 5**: Type 대안 제시 (다른 타입으로 해석)

**Detailed generation algorithm:** See [MESSAGE_GENERATION.md](MESSAGE_GENERATION.md)

### Scope Selection

- Module name: `feat(spring-security-jwt): JWT 인증 필터 추가`
- Filename: `fix(DateUtils.java): DST 미처리 문제 수정`
- Multi-file module: `refactor(spring-batch): 배치 재시도 로직 개선`

### Body Addition Criteria

- 5+ files changed
- 100+ lines changed
- Complex logic changes

### Body Format

```
<type>(scope): <간단한 설명>

- 주요 변경사항 1
- 주요 변경사항 2
- 주요 변경사항 3
```

See [RULES.md](RULES.md#body-guidelines) for body writing guide

### Suggest Messages (Using AskUserQuestion)

**Template:** [templates/template-3-message-selection.md](templates/template-3-message-selection.md)

**Actions:**
1. Call AskUserQuestion tool with "Template" JSON from template
   - Each option's description contains full commit message (header + body)
   - First message labeled "(추천)"
   - "Other" option automatically added by tool

**Requirements:**
1. **전체 메시지 표시**: 각 옵션의 description에 완전한 메시지 포함 (헤더 + 본문)
2. **추천 표시**: 첫 번째 메시지에 "(추천)" 표시
3. **Other 옵션**: 자동으로 추가됨 (사용자 직접 입력 가능)

**User Actions:**
- Select "Message 1-4" → Proceed to template-4 (final confirmation)
- Select "Other" → Proceed to template-5 (direct input)

---

## Step 4: Get User Approval

### 4-1: Process User Selection

**Selection from Step 3:**
- Select one of messages 1-4 → Proceed to final confirmation
- Select "Other" → Proceed to direct input flow

### 4-2: Direct Input Flow (When Other Selected)

**Template:** [templates/template-5-direct-input.md](templates/template-5-direct-input.md)

**Process:**
1. Display input instructions (see template "Step 2: Show Input Instructions")
2. User enters complete commit message (header + body, multiline supported)
3. Validate format using criteria in template "Validation" section:
   - Type: One of 7 types (feat, fix, refactor, test, docs, style, chore)
   - Scope: Alphanumeric + `.`, `-`, `_` only
   - Message: Not empty
   - Blank blocks: Maximum 2
4. If validation passes → Proceed to template-4 (final confirmation)
5. If validation fails → Display error and call AskUserQuestion with retry JSON (see template "On Validation Failure")

### 4-3: Final Confirmation (Using AskUserQuestion)

**Template:** [templates/template-4-final-confirmation.md](templates/template-4-final-confirmation.md)

**CRITICAL: 사용자가 선택한 후 반드시 전체 메시지를 다시 표시**

**Actions:**
1. Display the "Screen Output" section from template (show full commit message in box)
2. Call AskUserQuestion tool with "Template" JSON from template
3. Process user selection (see User Actions below)

**Why show full message again:**
- User may have only seen header in list view
- Final safety check before commit
- Clear visibility of complete message (header + body + footer)

**User Actions:**
- Select "Approve" → Proceed to Step 5 (execute commit)
- Select "Modify" → Return to template-3 (message selection)
- Select "Cancel" → Exit process

### 4-4: When Modify Selected

**Options:**
1. Return to Step 3 (message selection)
2. Direct input (same as Other selection)

→ Go to Step 3 or 4-2 based on user choice

---

## Step 5: Execute and Verify Commit (Read + Cleanup)

### Execute Commit

Use HEREDOC format for multiline handling:

```bash
git commit -m "$(cat <<'EOF'
feat(spring-cloud-bus): 커스텀 이벤트 핸들러 구현

- RemoteApplicationEvent 처리 로직 추가
- 이벤트 발행 메커니즘 구현
- 리스너 등록 기능 추가
EOF
)"
```

**Important:**
- DO NOT add "Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>" footer
- Keep commit messages clean without AI attribution watermarks

### Verify Immediately After Commit

- Confirm commit created: `git log -1 --oneline`
- Validate format: Check `<type>(scope): <message>` pattern match
- Report result to user (in Korean):

```
✅ 커밋이 성공적으로 생성되었습니다!
커밋 해시: abc1234
메시지: feat(spring-cloud-bus): 커스텀 이벤트 핸들러 구현
```

### When Commit Fails

- Explain error message in Korean
- Guide to refer to [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- Suggest possible solutions

### Handle Git Hook Failures

**Detect failure:**
- Detect pre-commit or commit-msg hook failure
- Display error message verbatim

**Provide to user:**
```
❌ 커밋 실패

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
훅 에러 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[실제 에러 메시지 전체 내용]
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

해결 방법:
1. [구체적인 수정 방법]
2. 수정 후 다시 커밋하려면:
   $ git add [수정한 파일]
   $ /commit
```

### Cleanup Metadata (Important)

**On success:**
```bash
# 메타데이터 파일 삭제 (현재 실행의 파일만)
rm .claude/temp/commit-execution-${EXECUTION_ID}.json
```

**On failure/cancellation:**
```bash
# Delete metadata file (will be recreated on next /commit execution)
rm .claude/temp/commit-execution-${EXECUTION_ID}.json
```

**During auto-split commit:**
- Cleanup after all groups processed
- Cleanup in final step even if intermediate failures

**Important:**
- Each `/commit` execution is independent
- Not related to previous execution's metadata files
- Multiple `/commit` executions possible in same CLI session

---

## Process Flow Diagram

```
Start
  ↓
┌─────────────────────────────────────┐
│ Step 1: Pre-validation & Context    │
│ - Check staged files                │
│ - Collect change context            │
│ - Determine scope                   │
└─────────────┬───────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Step 2: Analyze & Detect Violations │
│ - Determine commit type             │
│ - Validate Tidy First               │
│ - Verify logical independence       │
└─────┬───────────────────────┬───────┘
      │                       │
      ├─ Violation? ──┬─ Yes → Ask user
      │               │        ├─ Auto-split → AUTO_SPLIT.md
      │               │        ├─ Unified → Warn, then Step 3
      │               │        └─ Cancel → End
      │               │
      └─ No ──────────┘
      ↓
┌─────────────────────────────────────┐
│ Step 3: Generate Commit Message     │
│ - Generate with format              │
│ - Provide 5 suggestions             │
│ - Add body (if needed)              │
└─────────────┬───────────────────────┘
              ↓
┌─────────────────────────────────────┐
│ Step 4: Get User Approval           │
│ - Display message                   │
│ - Approve/Modify/Cancel             │
└─────┬─────────┬─────────┬───────────┘
      │         │         │
      ├─ Approve ┤        └─ Cancel → End
      │         │
      │         └─ Modify → Alternatives → Step 4
      ↓
┌─────────────────────────────────────┐
│ Step 5: Execute & Verify Commit     │
│ - Execute git commit                │
│ - Verify and report                 │
└─────────────┬───────────────────────┘
              ↓
             End
```

---

## Related Documents

- **[SKILL.md](SKILL.md)** - Overview and quick reference
- **[AUTO_SPLIT.md](AUTO_SPLIT.md)** - Auto-split commit process
- **[RULES.md](RULES.md)** - Commit message format rules
- **[EXAMPLES.md](EXAMPLES.md)** - Commit examples
- **[METADATA.md](METADATA.md)** - Token optimization strategy
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Error handling

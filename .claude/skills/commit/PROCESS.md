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

## Step 3: 3-Stage Message Composition (Read Metadata)

**Read metadata:**
```bash
# Read pre-analyzed data
cat .claude/temp/commit-execution-${EXECUTION_ID}.json
# Use analysis.detectedType, analysis.detectedScope, analysis.bodyItemCandidates
```

### Overview: User Selects Header → Body → Footer

Guide user through 3 stages to build the commit message:

1. **Stage 1**: Select commit header from 5 pre-generated messages (추천 2 + 일반 3)
2. **Stage 2**: Select body items (multi-select from auto-generated candidates)
3. **Stage 3**: Select footer (none, issue reference, or breaking change)

**Benefits:**
- Pre-generated quality headers ensure accuracy
- User has full control through selection
- Refresh mechanism provides flexibility
- Direct input available as fallback

**Detailed algorithms:** See [MESSAGE_GENERATION.md](MESSAGE_GENERATION.md)

---

### Stage 1: Header Message Selection

**Template:** [templates/template-3-1-header-selection.md](templates/template-3-1-header-selection.md)

**Generate 5 header messages:**
- **추천 2개** (fixed): Best matches based on analysis
- **일반 3개** (refreshable): Alternative options

**Generation algorithm:**
```javascript
function generate5Headers(changes) {
  // Recommended 1: Optimal scope + type + message
  const recommended1 = generateOptimalHeader(changes);

  // Recommended 2: Strong alternative (different scope or type)
  const recommended2 = generateAlternative(changes, recommended1);

  // General 3-5: Variations (refreshable)
  const general = generateVariations(changes, [recommended1, recommended2]);

  return {
    recommended: [recommended1, recommended2],
    general: general.slice(0, 3)
  };
}
```

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 1/3: 헤더 메시지 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

커밋 헤더로 사용할 메시지를 선택하세요.
(추천 메시지는 변경사항을 가장 잘 설명합니다)
```

**Actions:**
1. Generate 5 header messages (추천 2 + 일반 3)
2. Display screen output
3. Call AskUserQuestion with options:
   - Option 1-4: Headers (추천 2개 marked with "(추천)")
   - "Other": Direct input (automatically added)
4. Handle user selection:
   - **Header selected** → Store selected header, proceed to Stage 2
   - **"다른 추천 리스트 보기" selected** → Regenerate 일반 3개, show again
   - **"Other" (직접 입력) selected** → Prompt for manual input, validate, proceed to Stage 2

**Example headers:**
```
1. docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경 (추천)
2. refactor(commit-skill): 메시지 생성 프로세스 재구성 (추천)
3. docs(MESSAGE_GENERATION.md): 헤더 5개 생성 전략으로 재작성
4. docs(.claude/skills): commit 스킬 문서 업데이트
```

**Note:** AskUserQuestion limits to 4 options, so show 추천 2 + 일반 2. On refresh, rotate through different 일반 options.

---

### Stage 2: Body Items Selection (Multi-Select)

**User has selected header, now select body items.**

**Template:** [templates/template-3-2-body-selection.md](templates/template-3-2-body-selection.md)

**Generate body item candidates:**

Use metadata `analysis.bodyItemCandidates` (pre-generated in Step 1).

If not available, generate on-the-fly:

```javascript
function generateBodyItems(files, diff) {
  // Strategy: File-based (1-3 files) or Feature-based (4+ files)
  // See MESSAGE_GENERATION.md for detailed algorithm

  const items = [];

  if (files.length <= 3) {
    // File-based: "{filename}: {action}"
    files.forEach(file => {
      items.push({
        label: `${file.name}: ${extractAction(file, diff)}`,
        description: `...`
      });
    });
  } else {
    // Feature-based: "{feature description}"
    const features = groupByFeature(files, diff);
    features.forEach(feature => {
      items.push({
        label: feature.description,
        description: `관련 파일: ${feature.files.join(', ')}`
      });
    });
  }

  // Add "바디 없음" option
  items.push({
    label: "바디 없음 (헤더만 사용)",
    description: "간단한 변경이므로 헤더만으로 충분합니다"
  });

  return items;
}
```

**Extract and display scope:**

```javascript
function extractScope(files) {
  if (files.length === 1) {
    return path.basename(files[0]); // "UserService.java"
  } else {
    const commonDir = findCommonDirectory(files);
    return enhanceScope(path.basename(commonDir)); // "spring-security-jwt"
  }
}
```

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 2/3: 바디 항목 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

선택한 타입: {selectedType}
감지된 스코프: {detectedScope}

커밋 본문에 포함할 작업 내용을 선택하세요.
- 스페이스바로 복수 선택 가능
- 간단한 변경이면 "바디 없음" 선택
- 변경사항이 5개 이상이면 바디 추가 권장
```

**Actions:**
1. Display screen output with detected scope
2. Call AskUserQuestion with template JSON (multi-select enabled)
   - Options: 4-10 body item candidates + "바디 없음"
3. User selects 0+ items (multi-select)
4. Store `selectedBodyItems` and `detectedScope` in memory for Stage 3

**Item format examples:**
```
- UserService.java: 사용자 인증 로직 추가
- LoginController.java: 로그인 API 엔드포인트 구현
- JWT 토큰 생성 및 검증 로직 구현
```

---

### Stage 3: Footer Selection

**Template:** [templates/template-3-3-footer-selection.md](templates/template-3-3-footer-selection.md)

**Screen Output:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 Step 3/3: 푸터 선택
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

선택한 타입: {selectedType}
선택한 스코프: {detectedScope}
선택한 바디 항목: {selectedBodyItems.length}개

푸터를 추가할지 선택하세요.
대부분의 경우 푸터가 필요하지 않습니다.
```

**Actions:**
1. Display screen output with summary
2. Call AskUserQuestion with template JSON
   - Options: "푸터 없음" (추천), "Issue reference", "Breaking Change"
3. User selects one option
4. If "Issue reference" → Prompt for issue numbers
5. If "Breaking Change" → Prompt for description
6. Store `selectedFooter` in memory

**Footer formats:**
- No footer: (empty)
- Issue reference: `Closes #123, #456`
- Breaking Change: `BREAKING CHANGE: API 응답 형식 변경`

---

### Assemble Final Message

After 3 stages, assemble the final commit message:

```javascript
function assembleFinalMessage(selections) {
  const { type, scope, bodyItems, footer } = selections;

  // 1. Generate header message
  const headerMsg = generateHeaderMessage(type, scope, bodyItems);
  const header = `${type}(${scope}): ${headerMsg}`;

  // 2. Format body
  let body = '';
  if (bodyItems.length > 0 && bodyItems[0] !== '바디 없음') {
    body = '\n\n' + bodyItems.map(item => `- ${item.label}`).join('\n');
  }

  // 3. Add footer
  let footerSection = '';
  if (footer && footer !== '푸터 없음') {
    footerSection = '\n\n' + footer;
  }

  return header + body + footerSection;
}
```

**Header message generation:**
```javascript
function generateHeaderMessage(type, scope, bodyItems) {
  // Single item: extract action from item
  if (bodyItems.length === 1 && bodyItems[0] !== '바디 없음') {
    const item = bodyItems[0].label;
    if (item.includes(':')) {
      return item.split(':')[1].trim(); // "사용자 인증 로직 추가"
    }
  }

  // Multiple items or no items: use general description
  const verbs = {
    feat: '추가', fix: '수정', refactor: '개선',
    test: '추가', docs: '추가', style: '정리', chore: '업데이트'
  };

  const verb = verbs[type] || '변경';
  const theme = extractCommonTheme(bodyItems) || scope;

  return `${theme} ${verb}`;
}
```

**Example assembled message:**
```
feat(spring-security-jwt): JWT 인증 필터 추가

- UserService.java: 사용자 인증 로직 추가
- LoginController.java: 로그인 API 엔드포인트 구현
- SecurityConfig.java: Spring Security 설정

Closes #123
```

**Display preview:**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 생성된 커밋 메시지 미리보기:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
{complete_message}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

다음 단계: 최종 확인
```

Then proceed to Step 4 (final confirmation).

---

### Body Format Rules

**CRITICAL: Each body line MUST start with `-` (dash + space) and be on separate lines**

```
<type>(scope): <간단한 설명>

- 주요 변경사항 1
- 주요 변경사항 2
- 주요 변경사항 3
```

**Mandatory rules:**
- Each line starts with `- ` (dash + space)
- Each item on a new line (no comma-separated items)
- 5 lines or less
- File-based or feature-based grouping

**For detailed format rules and examples:**
- [MESSAGE_GENERATION.md](MESSAGE_GENERATION.md) - Complete generation algorithms and strategies
- [RULES.md - Body Guidelines](RULES.md#body-guidelines) - Validation rules

---

### User Actions Summary

**From Stage 3:**
- Complete 3 stages → Proceed to Step 4 (final confirmation with template-4)

**Alternative: Direct Input** (from any stage):
- User can select "Other" at any stage → Proceed to template-5 (direct input)
- Useful if user wants to write message from scratch

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
  - Core principles and quick start guide
- **[MESSAGE_GENERATION.md](MESSAGE_GENERATION.md)** - Message generation algorithm
  - 5 generation strategies and scope extraction
  - Complete generation patterns
- **[METADATA.md](METADATA.md)** - Token optimization strategy
  - Session metadata structure and lifecycle
  - 67% token savings through metadata reuse
- **[AUTO_SPLIT.md](AUTO_SPLIT.md)** - Auto-split commit process
  - Policy selection and sequential commits
  - Error handling and rollback
- **[RULES.md](RULES.md)** - Commit message format rules
  - Validation rules and format specifications
  - Tidy First and Logical Independence principles
- **[EXAMPLES.md](EXAMPLES.md)** - Complete commit message examples
  - All 7 commit types with real examples
- **[templates/README.md](templates/README.md)** - User interaction templates
  - 5 template files for different user interactions
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Error handling and hook failures

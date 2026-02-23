# Commit Message Generation Strategy (3-Stage User Selection)

User builds commit message through 3 stages: header selection → body selection → footer selection.

---

## Overview

**3-Stage Selection Process:**

1. **Stage 1: Header Selection** - User selects from 5 header messages per this policy
   - Recommended 2 (top suggestions following policy)
   - General 3 (alternative variations)

2. **Stage 2: Body Selection** - User selects body items (multi-select)
   - Feature-based descriptions per [body.md](body.md)
   - "No body" for header-only commits
   - "Show other recommendations" to regenerate

3. **Stage 3: Footer Selection** - User selects footer option
   - No footer (recommended)
   - Issue reference
   - Breaking Change

**Benefits:**
- User has full control through selection
- Policy-based generation ensures consistency
- Refresh mechanism provides flexibility
- Direct input available as fallback

---

## Stage 1: Header Message Generation

### Generate 5 Header Messages Following This Policy

**CRITICAL: Message Language**
- **Generate ALL commit messages in Korean** (한국어)
- This is a Korean project with Korean-speaking developers
- Header message format: `<type>(scope): <한국어 메시지>`
- Example: `feat(spring-batch): 배치 재시도 로직 구현` ✅
- NOT: `feat(spring-batch): implement batch retry logic` ❌

**NO pre-computation, NO scoring algorithms**

Analyze git diff data and generate 5 header messages per this policy:

**Recommended Message 1 (Top suggestion):**
- Analyze file paths, changes, and context
- Identify optimal scope (module/file/directory)
- Determine primary type (feat/fix/refactor/docs/test/style/chore)
- Create clear, concise message

**Example:**
```
docs(commit-skill): change commit message generation to 3-stage selection
```

**Recommended Message 2 (Strong alternative):**
- Use different emphasis or perspective
- Apply alternative scope level or type interpretation
- Provide meaningful choice

**Example:**
```
refactor(commit-skill): restructure message generation process to user selection-based
```

**General Messages 3-5 (Variations):**
- Scope variations (module vs file vs parent directory)
- Expression variations (concise vs detailed vs action-oriented)
- Type alternatives (different interpretation angles)

**Examples:**
```
docs(generation/header.md): rewrite with 3-stage selection algorithm
docs(commit-skill): introduce 3-stage commit process with header selection
refactor(.claude/skills): improve commit skill documentation and process
```

### Refresh Logic (Show Other Recommendations)

When user requests refresh, generate 3 NEW general messages while keeping Recommended 2 fixed.

**Approach:**
- Keep Recommended 1 & 2 unchanged (best suggestions)
- Generate 3 new variations with different perspectives
- Explore different scopes, types, or message angles

**Example Refresh Variations:**

*Initial 5 messages:*
- Recommended 1: `docs(commit-skill): change commit message generation to 3-stage selection`
- Recommended 2: `refactor(commit-skill): restructure message generation process`
- General 3: `docs(generation/header.md): rewrite with 3-stage selection algorithm`
- General 4: `docs(.claude/skills): update commit skill documentation`
- General 5: `refactor(message-generation): improve commit workflow`

*After refresh (Recommended 1 & 2 stay, General 3-5 regenerated):*
- Recommended 1: `docs(commit-skill): change commit message generation to 3-stage selection` ✓ (unchanged)
- Recommended 2: `refactor(commit-skill): restructure message generation process` ✓ (unchanged)
- NEW General 3: `feat(commit-skill): introduce interactive message composition`
- NEW General 4: `docs(skill-references): add 3-stage selection process documentation`
- NEW General 5: `chore(commit-workflow): reorganize message generation templates`

**Variation Strategies:**
1. **Scope variations**: File-level → Module-level → Parent directory
2. **Type alternatives**: Different interpretation angles (docs vs refactor vs feat)
3. **Message focus**: Action-oriented vs outcome-oriented vs technical detail
4. **Granularity**: Specific file changes vs broader system changes

---

## Generation Strategy

**Analysis process per this policy:**

1. **Understand changes**:
   - File paths and extensions
   - Additions vs deletions patterns
   - Changed directories/modules

2. **Identify type**:
   - `.md` files → likely `docs`
   - `test/` directory → likely `test`
   - New functionality patterns → `feat`
   - Bug fix patterns → `fix`
   - Structural changes → `refactor`

3. **Determine scope**:
   - Common directory → module name
   - Single file → filename
   - Multiple modules → parent directory

4. **Generate messages**:
   - Clear, action-oriented language
   - Focus on "what changed" not "which files"
   - Follow project conventions

5. **Create variations**:
   - Different scope levels
   - Alternative type interpretations
   - Various message expressions

**NO mechanical scoring, NO pre-computation - follow this policy document**

---


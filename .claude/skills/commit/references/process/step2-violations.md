# Step 2: Analyze Changes and Detect Violations

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

**Template:** [1-tidy-first.md](../../templates/1-tidy-first.md)

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

**Template:** [2-logical-independence.md](../../templates/2-logical-independence.md)

**Actions:**
1. Display the "Screen Output" section from template (detected groups with details and warning)
2. Call AskUserQuestion tool with "Template" JSON from template
3. Process user selection (see User Actions below)

**User Actions:**
- Select "Auto-split" → See **[logical-independence.md](../validation/logical-independence.md)** (auto-split commit process)
- Select "Unified commit" → Show warning, request confirmation, proceed to Step 3
- Select "Cancel" → Exit process

---


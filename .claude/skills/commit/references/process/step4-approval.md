# Step 4: Get User Approval

### 4-1: Process User Selection

**Selection from Step 3:**
- Select one of messages 1-4 → Proceed to final confirmation
- Select "Other" → Proceed to direct input flow

### 4-2: Direct Input Flow (When Other Selected)

**Template:** [5-direct-input.md](../../templates/5-direct-input.md)

**Process:**
1. Display input instructions (see template "Step 2: Show Input Instructions")
2. User enters complete commit message (header + body, multiline supported)
3. Validate format using criteria in template "Validation" section:
   - Type: One of 7 types (feat, fix, refactor, test, docs, style, chore)
   - Scope: Alphanumeric + `.`, `-`, `_` only
   - Message: Not empty
   - Blank blocks: Maximum 2
4. If validation passes → Proceed to 4-final-confirmation (final confirmation)
5. If validation fails → Display error and call AskUserQuestion with retry JSON (see template "On Validation Failure")

### 4-3: Final Confirmation (Using AskUserQuestion)

**Template:** [4-final-confirmation.md](../../templates/4-final-confirmation.md)

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
- Select "Modify" → Return to 3-1-header-selection (message selection)
- Select "Cancel" → Exit process

### 4-4: When Modify Selected

**Options:**
1. Return to Step 3 (message selection)
2. Direct input (same as Other selection)

→ Go to Step 3 or 4-2 based on user choice

---


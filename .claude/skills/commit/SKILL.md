---
name: commit
description: Analyzes git changes and generates convention-compliant commit messages. Use when user requests "/commit", asks to commit changes, or mentions creating a commit.
allowed-tools: Read, Grep, Glob, Bash, AskUserQuestion
---

## Overview

Automates commit message generation following project conventions.

**Core Functions:**
- **Auto-stages all modified files** (IDE-like behavior, no user confirmation needed)
- Analyzes all changes (staged + modified) and identifies scope (module/file)
- Validates Tidy First principles (structural vs behavioral separation)
- Detects logical independence (multiple independent groups)
- **Guides user through 3-stage message composition** (type → body items → footer)
- Generates body item candidates per Phase 3 rules
- Supports auto-split commit for logically independent changes
- Executes git commit after user approval

**Scope:**
- ✅ Auto-stage modified files (IDE behavior)
- ✅ Analyze all changes (staged + modified)
- ✅ Validate Tidy First and logical independence
- ✅ Execute `git commit` (with user approval)
- ❌ Never: `git push` or destructive commands without approval
- ❌ Never: Stage untracked files (only modified files)

## Process

Execute skill in 5 phases. Each phase requires user interaction before proceeding. Never skip any phase.

> **⚠️ CRITICAL**: Commit is a user-controlled operation. AI has no decision authority. All interactive phases MUST be executed sequentially with explicit user confirmation at each step.

**Phase 1: Initial Analysis** (Automated - no user interaction)
- Collect git status and diff data
- Stage modified files
- Load: [process/step1-analysis.md](process/step1-analysis.md)
- **Next**: Proceed to Phase 2 only if changes detected

**Phase 2: Violation Detection** (Interactive - requires user decision)
- Detect Tidy First violations (refactor + feat/fix mix)
- Detect logical independence (10+ files across different directories)
- **MUST**: Present options via AskUserQuestion, wait for user choice
- Load: [process/step2-violations.md](process/step2-violations.md), [rules/logical-independence.md](rules/logical-independence.md)
- **Next**: Proceed based on user selection

**Phase 3: Message Generation** (Interactive - 3-stage user selection)
- **Stage 1 - Header Selection**: Present 5 candidates (2 recommended + 3 general), user selects via AskUserQuestion
- **Stage 2 - Body Selection**: Present body items, user multi-selects via AskUserQuestion
- **Stage 3 - Footer Selection**: Present footer options, user selects via AskUserQuestion
- **MUST**: Execute ALL 3 stages sequentially with user input at each stage
- Load: [rules/header.md](rules/header.md), [rules/body.md](rules/body.md), [ui/stage1-header.md](ui/stage1-header.md), [ui/stage2-body.md](ui/stage2-body.md), [ui/stage3-footer.md](ui/stage3-footer.md)
- **Next**: Proceed to Phase 4 after Stage 3 completion

**Phase 4: Validation & Approval** (Interactive - final user confirmation)
- Display complete commit message to user
- **MUST**: Present approve/modify/cancel options via AskUserQuestion
- **MUST**: Wait for explicit user approval before proceeding
- Load: [ui/stage4-confirmation.md](ui/stage4-confirmation.md), [ui/stage4-direct-input.md](ui/stage4-direct-input.md), [rules/format.md](rules/format.md)
- **Next**: Proceed to Phase 5 ONLY if user selects "approve"

**Phase 5: Execution** (Automated after approval)
- Execute git commit with approved message
- Verify and report result
- **MUST**: Confirm commit hash and message with user
- Load: [process/step5-execute.md](process/step5-execute.md)

**Support Resources** (load as needed)
- Errors: [support/troubleshooting.md](support/troubleshooting.md)
- Examples: [support/examples.md](support/examples.md)

**False Positive Prevention**

To avoid incorrect skill invocation, verify ALL conditions are met:

1. **User intent MUST explicitly include:**
   - "/commit" command
   - "commit this" / "commit these changes"
   - "create a commit" / "make a commit"
   - "stage and commit"

2. **DO NOT invoke when:**
   - User only asks about git status or diff (use Bash directly)
   - User mentions "commit" in unrelated context (database transactions, business commits)
   - User requests "push", "pull", "merge", "rebase" (different operations)
   - User asks general git questions without committing

3. **When in doubt:** Ask clarifying question before invoking skill

## Core Principles

**User Approval Required (NON-NEGOTIABLE)**
- **MANDATORY**: ALL interactive phases (Phase 2, 3, 4) MUST use AskUserQuestion
- **MANDATORY**: Display complete message before commit via Phase 4 confirmation
- **MANDATORY**: Get explicit approval (approve/modify/cancel) - never auto-approve
- **PROHIBITED**: AI must NOT execute `git commit` without user confirmation
- **PROHIBITED**: Skipping any interactive phase is strictly forbidden

**Tidy First Principle**
- Never mix refactor with feat/fix
- Warn and suggest separation if detected

**Logical Independence (Default Policy: Auto-Split)**
- Detect independent changes (10+ files, different directories)
- **Default: Auto-split commit** (separate commits per group)
- Alternative: Unified commit (with warning and tooltip)
- Tooltip always shown: "모든 변경사항을 하나의 커밋으로 진행하면 전체 롤백이나 코드 리뷰/수정이 어려울 수 있습니다"

**Message Composition (3-Stage Selection)**
- Stage 1: User selects header from 5 messages per Phase 3 rules (추천 2 + 일반 3)
- Stage 2: User selects body items (multi-select) per Phase 3 rules
- Stage 3: User selects footer option
- **Refresh mechanism**: "다른 추천 리스트 보기" to see different options
- **Direct input**: Available at each stage as fallback
- Format: `<type>(scope): <message>` with optional body and footer

**Git Hook Failures**
- Show error verbatim, skip failed group
- Continue to next group, keep successful commits

**User Communication and Commit Messages (Korean)**
- All user-facing messages MUST be in Korean
- This includes: AskUserQuestion, status messages, analysis results, error messages
- **Commit message content MUST be in Korean** (header, body, footer)
- Generate Korean messages for Korean-speaking users (default behavior for this project)
- Internal documentation and process descriptions can be in English for token efficiency

**Commit Message Footer (CRITICAL)**
- **NEVER** add "Co-Authored-By" footer - keep commit messages clean without AI attribution

## References

### Process
- `process/step1-analysis.md` - Git status collection and staging
- `process/step2-violations.md` - Tidy First and logical independence detection
- `process/step5-execute.md` - Commit execution and verification

### Rules
- `rules/format.md` - Commit message format, types, validation rules
- `rules/header.md` - Header generation policy (5 candidates)
- `rules/body.md` - Body item generation rules
- `rules/logical-independence.md` - Auto-split commit process

### UI Templates
- `ui/stage1-header.md` - Header selection interface
- `ui/stage2-body.md` - Body item selection interface
- `ui/stage3-footer.md` - Footer selection interface
- `ui/stage4-confirmation.md` - Final confirmation interface
- `ui/stage4-direct-input.md` - Direct input fallback

### Support
- `support/troubleshooting.md` - Common errors and solutions
- `support/examples.md` - Commit message examples by type


---
name: commit
description: Analyzes all git changes (staged + modified) and generates commit messages following project conventions. Auto-stages modified files (IDE-like behavior). Validates Tidy First principles, detects logical independence, and executes git commit after user approval. Use when creating commits or when user mentions changes.
disable-model-invocation: false
user-invocable: true
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
- Auto-generates body item candidates from changed files
- Supports auto-split commit for logically independent changes
- Executes git commit after user approval

**Scope:**
- ✅ Auto-stage modified files (IDE behavior)
- ✅ Analyze all changes (staged + modified)
- ✅ Validate Tidy First and logical independence
- ✅ Execute `git commit` (with user approval)
- ❌ Never: `git push` or destructive commands without approval
- ❌ Never: Stage untracked files (only modified files)

## Document Structure

- **[PROCESS.md](PROCESS.md)** - Detailed execution process (Step 1-5)
- **[AUTO_SPLIT.md](AUTO_SPLIT.md)** - Auto-split commit process
- **[RULES.md](RULES.md)** - Commit message format rules
- **[MESSAGE_GENERATION.md](MESSAGE_GENERATION.md)** - Message generation strategy and algorithms
- **[templates/README.md](templates/README.md)** - UI/UX design and user interaction templates
- **[EXAMPLES.md](EXAMPLES.md)** - Commit examples and patterns
- **[TROUBLESHOOTING.md](TROUBLESHOOTING.md)** - Error handling guide
- **[METADATA.md](METADATA.md)** - Token optimization via session state

## Core Principles

**1. User Approval Required**
- Always show full message before commit
- Get explicit approval (approve/modify/cancel)

**2. Tidy First Principle**
- Never mix refactor with feat/fix
- Warn and suggest separation if detected

**3. Logical Independence (Default Policy: Auto-Split)**
- Detect independent changes (10+ files, different directories)
- **Default: Auto-split commit** (separate commits per group)
- Alternative: Unified commit (with warning and tooltip)
- Tooltip always shown: "모든 변경사항을 하나의 커밋으로 진행하면 전체 롤백이나 코드 리뷰/수정이 어려울 수 있습니다"

**4. Message Composition (3-Stage Selection)**
- Stage 1: User selects header from 5 pre-generated messages (추천 2 + 일반 3)
- Stage 2: User selects body items (multi-select from auto-generated candidates)
- Stage 3: User selects footer (none/issue reference/breaking change)
- **Refresh mechanism**: "다른 추천 리스트 보기" to see different options
- **Direct input**: Available at each stage as fallback
- Format: `<type>(scope): <message>` with optional body and footer

**5. Git Hook Failures**
- Show error verbatim, skip failed group
- Continue to next group, keep successful commits

**6. User Communication (Korean)**
- All user-facing messages MUST be in Korean
- This includes: AskUserQuestion, status messages, analysis results, error messages
- Internal documentation and process descriptions can be in English for token efficiency

**7. Commit Message Footer**
- DO NOT add "Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>" footer
- Keep commit messages clean without AI attribution watermarks
- Only include necessary metadata requested by the user

## Quick Reference

**Format**: `<type>(module|filename): <message>`

**Types**: feat, fix, refactor, test, docs, style, chore

**Scope**:
- Module: `feat(spring-batch): 배치 재시도 로직 개선`
- File: `fix(DateUtils.java): DST 미처리 문제 수정`

**Tidy First**: Never mix refactor with feat/fix in same commit

**Logical Independence**: Separate different purposes even if same type

## Execution Flow

```
Start
  ↓
Step 1: Validate & Gather Context → [PROCESS.md](PROCESS.md)
  ↓
Step 2: Analyze & Detect Violations
  ↓
  ├─ Tidy First violation? → Warn & suggest separation
  ├─ Logical independence? → [AUTO_SPLIT.md](AUTO_SPLIT.md)
  └─ OK → Continue
  ↓
Step 3: 3-Stage Message Composition
  ├─ Stage 1: Header Selection (5 messages: 추천 2 + 일반 3) → [template-3-1](templates/template-3-1-header-selection.md)
  ├─ Stage 2: Body Items Selection (multi-select) → [template-3-2](templates/template-3-2-body-selection.md)
  ├─ Stage 3: Footer Selection → [template-3-3](templates/template-3-3-footer-selection.md)
  └─ Assemble final message from user selections
  ↓
Step 4: User Approval (approve/modify/cancel) → [template-4](templates/template-4-final-confirmation.md)
  ↓
Step 5: Execute & Verify Commit
  ↓
Done
```

See [PROCESS.md](PROCESS.md) for detailed step-by-step execution.

## Usage

**Invoke skill:**
```
/commit
```

**Natural language:**
```
커밋 메시지 작성해줘
현재 변경사항 커밋해줘
스테이징된 파일 커밋해줘
```

## Related Documents

- [PROCESS.md](PROCESS.md) - Complete execution process
- [AUTO_SPLIT.md](AUTO_SPLIT.md) - Auto-split commit workflow
- [RULES.md](RULES.md) - Commit message conventions
- [MESSAGE_GENERATION.md](MESSAGE_GENERATION.md) - Message generation algorithms
- [templates/README.md](templates/README.md) - UI/UX design and user interaction templates
- [EXAMPLES.md](EXAMPLES.md) - Commit examples
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Problem solving

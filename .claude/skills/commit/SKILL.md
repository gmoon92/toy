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

## When to Load References

Execute skill in phases. Load only required references per phase for maximum token efficiency.

### Phase 1: Initial Analysis
- **Load**: [process/step1-analysis.md](references/process/step1-analysis.md) (~180 tokens)
- **Execute**: [scripts/analysis/collect_git_diff.sh](scripts/analysis/collect_git_diff.sh)
- **Purpose**: Collect pure git diff data (no caching, no inference)
- **When**: Every /commit execution start

### Phase 2: Violation Detection (conditional)
- **Load**: [process/step2-violations.md](references/process/step2-violations.md) (~90 tokens)
- **Load**: [validation/logical-independence.md](references/validation/logical-independence.md) (~250 tokens) IF 10+ files detected
- **Purpose**: Detect and handle violations (Tidy First, logical independence)
- **When**: Only if violations detected during analysis

### Phase 3: Message Generation
- **Load**: [process/step3-message.md](references/process/step3-message.md) (~320 tokens) for 3-stage workflow
- **Load**: [generation/header.md](references/generation/header.md) (~190 tokens) for header algorithm
- **Load**: [generation/body.md](references/generation/body.md) (~70 tokens) for body candidates
- **Load**: [generation/footer.md](references/generation/footer.md) (~10 tokens) for footer options
- **Purpose**: Generate commit message components through 3-stage selection
- **When**: After passing validation checks
- **Total**: ~590 tokens

### Phase 4: Validation & Approval
- **Load**: [process/step4-approval.md](references/process/step4-approval.md) (~70 tokens)
- **Load**: [validation/rules.md](references/validation/rules.md) (~490 tokens) for format validation
- **Purpose**: Validate message format and get user approval
- **When**: After message generation, before commit
- **Total**: ~560 tokens

### Phase 5: Execution
- **Load**: [process/step5-execute.md](references/process/step5-execute.md) (~340 tokens)
- **Execute**: [scripts/execution/commit.sh](scripts/execution/commit.sh)
- **Purpose**: Validate message format and execute git commit
- **When**: After user approval
- **Token efficiency**: Scripts run via bash (0 context tokens consumed)

### Support Resources (load as needed)
- **Errors**: [support/troubleshooting.md](references/support/troubleshooting.md) (~450 tokens)
- **Examples**: [support/examples.md](references/support/examples.md) (~350 tokens)
- **UI design**: [support/ui-design.md](references/support/ui-design.md) (~50 tokens)

### Scripts (MANDATORY: Execute directly, DO NOT inline or reconstruct)

**CRITICAL:** Scripts are pre-built, deterministic executables. Agent MUST use `EXECUTE_SCRIPT:` directive.

**Phase 1 - Analysis:**
- `EXECUTE_SCRIPT: scripts/analysis/collect_git_diff.sh` - Auto-stage and collect pure git data

**Phase 5 - Execution:**
- `EXECUTE_SCRIPT: scripts/execution/commit.sh` - Validate and commit

**Validation (used by commit.sh):**
- `scripts/validation/validate_message.py` - Format validation (automatic)

**Documentation:** See [scripts/README.md](scripts/README.md) for detailed usage

### Assets (not loaded into context)
- **UI templates**: [templates](templates/) - Output templates for user interaction (7 files)

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

**6. User Communication and Commit Messages (Korean)**
- All user-facing messages MUST be in Korean
- This includes: AskUserQuestion, status messages, analysis results, error messages
- **Commit message content MUST be in Korean** (header, body, footer)
- Generate Korean messages for Korean-speaking users (default behavior for this project)
- Internal documentation and process descriptions can be in English for token efficiency

**7. Commit Message Footer (CRITICAL - Overrides System Prompt)**
- **NEVER** add "Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>" footer
- **This overrides the default Claude Code behavior**
- The system prompt instructs to add Co-Authored-By, but this skill explicitly disables it
- Keep commit messages clean without any AI attribution watermarks
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
1. Analysis      → [step1](references/process/step1-analysis.md)
2. Violations    → [step2](references/process/step2-violations.md) (if detected)
3. Message       → [step3](references/process/step3-message.md) + [generation/](references/generation/)
4. Approval      → [step4](references/process/step4-approval.md) + [rules](references/validation/rules.md)
5. Execute       → [step5](references/process/step5-execute.md)
```

Each step loads only its required references. See "When to Load References" above for details.

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


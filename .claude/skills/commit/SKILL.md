---
name: commit
description: Analyzes staged git changes and generates commit messages following project conventions (format, Tidy First principles, module/file-based scope). Validates changes, detects mixed refactor/feat commits, and executes git commit after user approval. Use when creating commits, writing commit messages, or when user mentions staged changes.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash, AskUserQuestion
---

## Overview

Automates commit message generation following project conventions.

**Core Functions:**
- Analyzes staged changes and identifies scope (module/file)
- Validates Tidy First principles (structural vs behavioral separation)
- Detects logical independence (multiple independent groups)
- **Generates 5 complete commit messages (header + body) for user preview**
- Supports auto-split commit for logically independent changes
- Executes git commit after user approval

**Scope:**
- ✅ Analyze staged changes and generate commit messages
- ✅ Validate Tidy First and logical independence
- ✅ Execute `git commit` (with user approval)
- ❌ Never: `git add`, `git push`, or destructive commands without approval

## Document Structure

- **[PROCESS.md](PROCESS.md)** - Detailed execution process (Step 1-5)
- **[AUTO_SPLIT.md](AUTO_SPLIT.md)** - Auto-split commit process
- **[RULES.md](RULES.md)** - Commit message format rules
- **[MESSAGE_GENERATION.md](MESSAGE_GENERATION.md)** - Message generation strategy and algorithms
- **[UI_TEMPLATES.md](UI_TEMPLATES.md)** - User option templates for each process step
- **[EXAMPLES.md](EXAMPLES.md)** - Commit examples and patterns
- **[UI_UX.md](UI_UX.md)** - Message selection UI/UX design
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

**4. Message Format**
- `<type>(scope): <message>`
- Types: feat, fix, refactor, test, docs, style, chore
- **ALWAYS show full message (header + body) in suggestions**
- Provide 5 complete suggestions, allow modifications

**5. Git Hook Failures**
- Show error verbatim, skip failed group
- Continue to next group, keep successful commits

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
Step 3: Generate Commit Message (5 suggestions)
  ↓
Step 4: User Approval (approve/modify/cancel)
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
- [UI_TEMPLATES.md](UI_TEMPLATES.md) - User interaction templates
- [EXAMPLES.md](EXAMPLES.md) - Commit examples
- [UI_UX.md](UI_UX.md) - Message selection UI design
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - Problem solving

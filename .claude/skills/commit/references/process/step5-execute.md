# Step 5: Execute and Verify Commit

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
- Guide to refer to [troubleshooting.md](TROUBLESHOOTING.md)
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

- **[../SKILL.md](SKILL.md)** - Overview and quick reference
  - Core principles and quick start guide
- **[message-generation.md](MESSAGE_GENERATION.md)** - Message generation algorithm
  - 5 generation strategies and scope extraction
  - Complete generation patterns
- **[metadata.md](METADATA.md)** - Token optimization strategy
  - Session metadata structure and lifecycle
  - 67% token savings through metadata reuse
- **[auto-split.md](AUTO_SPLIT.md)** - Auto-split commit process
  - Policy selection and sequential commits
  - Error handling and rollback
- **[rules.md](RULES.md)** - Commit message format rules
  - Validation rules and format specifications
  - Tidy First and Logical Independence principles
- **[examples.md](EXAMPLES.md)** - Complete commit message examples
  - All 7 commit types with real examples
- **[ui-design.md](ui-design.md)** - User interaction templates
  - 5 template files for different user interactions
- **[troubleshooting.md](TROUBLESHOOTING.md)** - Error handling and hook failures

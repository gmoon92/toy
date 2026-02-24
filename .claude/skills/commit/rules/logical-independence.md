# Auto-Split Commit Process

Automatic commit splitting for logically independent changes.

---

## When to Use

Auto-split is triggered when:
- 10+ files changed
- Different top-level directories
- Different logical contexts or purposes

**This is the DEFAULT POLICY** because:
- Clear commit purpose per group
- Easier code review
- Selective rollback possible
- Better git history
- Easier change tracking

---

## Process Overview

```
Detection → Policy Selection → Priority Setup → Sequential Commits → Summary
```

---

## Phase 1: Policy Selection (with Tooltip)

System analyzes staged files and identifies logical groups:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ 논리적으로 독립적인 변경사항 감지!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

감지된 그룹:
  그룹 1: .claude/skills/commit/ (4개 파일)
  그룹 2: ai/docs/claude/ (70개 파일)
  그룹 3: .claude/agents/ (8개 파일)

총 82개 파일이 3개의 독립적인 컨텍스트로 나뉩니다.

💡 도움말:
   통합 커밋은 전체 롤백과 코드 리뷰가 어려워질 수 있습니다.
   기본 정책(자동 분리)을 따르는 것을 권장합니다.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📋 커밋 전략을 선택하세요:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

> 1. 자동 분리 커밋 (기본 정책)
     각 그룹을 독립적으로 커밋합니다.
     ✅ 명확한 히스토리, 쉬운 리뷰, 선택적 롤백
     ✅ git bisect/revert/cherry-pick 용이
     ⚠️ 커밋 수 증가, 프로세스 시간 소요

  2. 통합 커밋
     모든 변경을 하나로 통합합니다.
     ⚠️ 롤백/리뷰 어려움, 버그 추적 어려움
     ✅ 빠른 커밋, 간단한 히스토리

  3. 취소
     커밋 프로세스를 종료합니다.

[↑↓: 이동 | →: 상세 정보 보기 | Enter: 선택]
```

**Tooltip always shown:**
- Warns about unified commit risks
- Reinforces default policy
- Helps user make informed decision

**User chooses:**
1. Auto-split (default policy)
2. Unified commit (with warning and re-confirmation)
3. Cancel

**If auto-split selected, choose commit order:**
- Auto order: Small to large (file count)
- Manual order: User specifies sequence
- Can reset priority before proceeding

---

## Phase 2: Sequential Commits

For each group in order:

1. **Generate message** - 5 complete suggestions (header + body)
2. **User approval** - Approve/Modify/Skip/Cancel
3. **Execute commit** - Create commit for group
4. **Continue** - Move to next group

**Message format:**
```
1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

   - SKILL.md: 스킬 실행 프로세스 정의
   - rules/format.md: 커밋 메시지 형식 규칙
   - support/examples.md: 실제 사용 예시

2. docs(commit-skill): 커밋 스킬 문서 추가
...
```

**Options per group:**
- Approve: Commit and continue
- Modify: See 5 alternative complete messages
- Skip: Skip this group, continue to next
- Cancel: Option to rollback all or keep completed

---

## Phase 3: Error Handling

### Git Hook Failures

When hook fails:
1. Display error message verbatim
2. Provide resolution guide
3. **Auto-skip failed group**
4. Continue to next group
5. Keep successful commits (no rollback)

User can manually retry failed groups after fixing.

**Example:**
```
❌ Hook error: YAML validation failed
✗ Skip하고 다음으로 진행
```

### User Cancellation

Mid-process cancellation options:
- **Rollback**: Undo all commits from this session
- **Keep**: Preserve completed commits
- **Resume**: Continue process

---

## Phase 4: Summary

Final report shows:
- Successful commits (hash + message)
- Failed groups (with retry instructions)
- Git log view

**Example:**
```
✓ 2개 성공 / ✗ 1개 실패

성공:
  - abc1234 docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가
  - def5678 docs(claude-api): Claude API 문서 한글 번역 추가

실패:
  - docs(korean-translator) - Hook validation failed
```

---

## Key Points

- Each group is committed independently
- Hook failures skip group automatically
- Successful commits are never rolled back (unless user explicitly chooses)
- User can retry failed groups after fixing issues
- Priority can be reset before starting commits

---

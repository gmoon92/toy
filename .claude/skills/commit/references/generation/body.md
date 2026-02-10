# Stage 2: Body Item Generation

### Core Principle

**Body purpose:**
- ❌ List changed files (already shown in git log)
- ✅ Describe what work was done

**File list vs Body:**
- What git log shows automatically: file list, changed line counts
- What Body should provide: work description, purpose, context

### Generate Body Item Candidates (Feature-based, NO scores)

**Strategy: Claude analyzes changes and generates natural candidates**

Claude analyzes the git diff data and generates body item candidates based on:
1. **Understanding the changes**: What was actually modified, added, or removed
2. **Identifying features/work**: Group related changes by logical purpose
3. **Natural prioritization**: Present items in order of significance (no mechanical scoring)
4. **Clear descriptions**: Focus on "what work was done" not "which files changed"

**Example candidates:**
```
Stage 2: 바디 항목 선택 (다중 선택)

다음 항목 중 커밋 메시지에 포함할 내용을 선택하세요:

□ 커밋 메시지 생성을 3단계 선택 방식으로 변경
  (사용자 경험 개선 및 선택 유연성 향상)

□ 헤더 생성 전략을 5개 옵션 제공으로 재작성
  (추천 2개 + 일반 3개 구조)

□ 바디 항목 페이지네이션 구현
  (3개씩 페이지 단위로 표시, 가독성 향상)

□ 바디 항목 없이 진행

[↑↓: 이동 | Space: 선택/해제 | Enter: 완료]
```

### Generation Approach

**NO pre-computation**: Claude generates candidates in real-time during Phase 3
- Analyze files and changes from git diff data
- Understand the purpose and context
- Generate 5-10 clear, feature-based descriptions
- Present in natural order of importance

**Multi-select**: User can select multiple items or none

---

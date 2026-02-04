# Commit Message UI/UX Design

User-friendly message selection with preview and confirmation.

This directory contains individual UI templates for user interactions during the commit process, along with comprehensive UI/UX design guidelines.

---

## Template Files

Each template is separated into its own file for efficient context loading:

- **[template-1-tidy-first.md](template-1-tidy-first.md)** - Tidy First 위반 감지
- **[template-2-logical-independence.md](template-2-logical-independence.md)** - 논리적 독립성 감지
- **[template-3-message-selection.md](template-3-message-selection.md)** - 커밋 메시지 선택
- **[template-4-final-confirmation.md](template-4-final-confirmation.md)** - 최종 확인
- **[template-5-direct-input.md](template-5-direct-input.md)** - 메시지 수정 (직접 입력)

### Token Efficiency
- **Selective loading**: Only load templates when needed
- **75-90% token savings**: Load ~50-100 lines instead of entire combined file (~800 lines)
- **Scalability**: Easy to add new templates without affecting others

### Maintainability
- **Independent updates**: Modify each template without affecting others
- **Clear purpose**: File name indicates template purpose
- **Version control**: Track changes per template

---

## Message Selection Flow

### Step 1: Compact List View (Headers Only)

Show headers only for quick scanning:

```
📝 커밋 메시지를 선택하세요 (↑↓: 이동, ←→: 본문 보기):

> 1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천)
  2. docs(commit-skill): 커밋 스킬 문서 추가
  3. feat(commit-skill): 자동 커밋 메시지 생성기
  4. docs(claude-skills): commit 스킬 구현
  5. 직접 입력

[↑↓: 선택 이동 | ←→: 본문 펼침/접음 | Enter: 선택 | Esc: 취소]
```

**Design principles:**
- **Recommended message always at #1**: Most logical choice based on analysis
- **Compact by default**: Headers only for quick scanning
- **Interactive preview**: Expand body on demand

---

### Step 2: Body Preview (Toggle with Arrow Keys)

**Press → (right arrow) on selected item:**

```
📝 커밋 메시지를 선택하세요:

> 1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천) ▼

     - SKILL.md: 스킬 실행 프로세스 정의
     - RULES.md: 커밋 메시지 형식 규칙
     - EXAMPLES.md: 실제 사용 예시
     - TROUBLESHOOTING.md: 문제 해결 가이드

  2. docs(commit-skill): 커밋 스킬 문서 추가
  3. feat(commit-skill): 자동 커밋 메시지 생성기
  4. docs(claude-skills): commit 스킬 구현
  5. 직접 입력

[↑↓: 선택 이동 | ←: 본문 접기 | Enter: 선택]
```

**Press ← (left arrow) to collapse:**

```
> 1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천)
  2. docs(commit-skill): 커밋 스킬 문서 추가
  ...
```

**User can preview multiple messages:**

```
> 1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천) ▼
     - SKILL.md: 스킬 실행 프로세스 정의
     - RULES.md: 커밋 메시지 형식 규칙
     ...

  2. docs(commit-skill): 커밋 스킬 문서 추가 ▼
     - 커밋 자동화 스킬 문서
     - 메시지 형식 규칙 정의

  3. feat(commit-skill): 자동 커밋 메시지 생성기
  ...
```

---

### Step 3: Final Confirmation (Full Message Display)

**After user presses Enter on selection:**

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 최종 커밋 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
- EXAMPLES.md: 실제 사용 예시
- TROUBLESHOOTING.md: 문제 해결 가이드
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

이 메시지로 커밋하시겠습니까?

1. 승인 - 커밋 실행
2. 수정 - 다른 메시지 선택
3. 취소 - 프로세스 종료
```

**Key point:** User sees **complete message** (header + body + footer) before approval.

---

## Keyboard Controls

### Message List Screen

| Key | Action |
|-----|--------|
| ↑ | 이전 항목으로 이동 |
| ↓ | 다음 항목으로 이동 |
| → | 선택된 항목의 본문 펼치기 |
| ← | 선택된 항목의 본문 접기 |
| Enter | 선택한 메시지로 최종 확인 화면 이동 |
| Esc | 취소하고 나가기 |

### Final Confirmation Screen

| Key | Action |
|-----|--------|
| 1 | 승인 - 커밋 실행 |
| 2 | 수정 - 메시지 목록으로 돌아가기 |
| 3 | 취소 - 프로세스 종료 |
| Esc | 메시지 목록으로 돌아가기 |

---

## Recommendation Logic

### How #1 (Recommended) is Determined

The recommended message is selected based on:

1. **Scope accuracy**: Best matches the changed files
2. **Type correctness**: Matches the nature of changes
3. **Body completeness**: Has most informative body
4. **Commit size**: Appropriate detail for change size

**Example scoring:**

```javascript
function scoreMessage(message, analysis) {
  let score = 0;

  // Scope match (40 points)
  if (matchesMainDirectory(message.scope, analysis.mainDir)) {
    score += 40;
  }

  // Type correctness (30 points)
  if (matchesChangeType(message.type, analysis.changeType)) {
    score += 30;
  }

  // Body quality (20 points)
  if (hasInformativeBody(message.body, analysis.files)) {
    score += 20;
  }

  // Appropriate detail (10 points)
  if (matchesChangeSize(message, analysis.stats)) {
    score += 10;
  }

  return score;
}

// Sort by score, highest first
messages.sort((a, b) => b.score - a.score);
messages[0].label = "(추천)";
```

---

## Visual Design

### Color/Style Indicators

```
> 1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천)
  ^                                                      ^^^^^^^
  |                                                      |
  Selected indicator                                    Recommendation badge
```

### Expansion Indicator

```
Without body:
> 1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천)

With body expanded:
> 1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천) ▼
     - SKILL.md: ...
```

### Final Confirmation Box

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📝 최종 커밋 메시지:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
{full message}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## Policy Selection UI (Logical Independence Detected)

When multiple independent groups are detected:

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

**Design principles:**
- **Concise descriptions**: No redundancy, clear pros/cons
- **Tooltip always visible**: Warning about unified commit risks
- **Balanced information**: Show both advantages and disadvantages
- **Git workflow alignment**: Follows atomic commit principles

### Option Details (Expand with → arrow)

**Option 1 expanded:**

```
> 1. 자동 분리 커밋 (기본 정책) ▼

   각 그룹을 독립적인 커밋으로 분리합니다.

   ✅ 이것이 기본 정책인 이유:
   - 명확한 커밋 목적 (한 커밋 = 한 변경)
   - 쉬운 코드 리뷰 (그룹별 독립 검토)
   - 선택적 롤백/revert 가능 (문제 있는 부분만)
   - git bisect로 버그 도입 시점 빠르게 추적
   - cherry-pick으로 특정 변경만 이동 가능
   - git blame으로 변경 이유 명확히 추적
   - merge conflict 발생 시 범위가 작아 해결 쉬움
   - CI/CD 실패 시 원인 파악 빠름

   ⚠️ 알아두어야 할 사항:
   - 각 그룹별로 커밋 메시지 작성 필요 (총 3번)
   - 각 그룹별로 승인 과정 진행
   - Hook 실패 시 일부 그룹만 커밋될 수 있음
   - 프로세스 시간이 더 소요됨
   - git log가 길어질 수 있음

  2. 통합 커밋
  3. 취소
```

**Option 2 expanded:**

```
  1. 자동 분리 커밋 (기본 정책)

> 2. 통합 커밋 ▼

   모든 변경을 하나로 통합합니다.

   ⚠️ 위험 사항:
   - 커밋 목적 불명확: 무엇을 위한 커밋인지 파악 어려움
   - 코드 리뷰 어려움: 82개 파일을 한 번에 검토해야 함
   - 부분 롤백/revert 불가능: 문제 있어도 전체 되돌려야 함
   - git bisect 비효율: 버그 추적 시 범위가 너무 큼
   - cherry-pick 어려움: 특정 변경만 선택 불가능
   - git blame 혼란: 변경 이유 추적 어려움
   - merge conflict 해결 어려움: 범위가 커서 복잡
   - CI/CD 문제: 빌드/테스트 실패 시 원인 파악 어려움

   ✅ 장점:
   - 빠른 커밋: 한 번에 완료
   - 간단한 히스토리: git log가 짧음
   - 하나의 승인 과정

   ℹ️ 이 옵션을 선택해야 하는 경우:
   - 모든 변경이 하나의 기능을 위한 것
   - 변경사항이 강하게 결합되어 분리 불가능
   - 각 그룹이 독립적으로 작동하지 않음
   - squash merge 워크플로우 사용

   ⚠️ 주의: 선택 시 재확인 과정이 있습니다.

  3. 취소
```

### Unified Commit Re-confirmation

If user selects option 2, show additional warning:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ 통합 커밋 경고
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

기본 정책(자동 분리)을 따르지 않고 통합 커밋을 선택하셨습니다.

다시 한번 확인해주세요:
- 82개 파일이 하나의 커밋에 포함됩니다
- 커밋 목적이 불명확해질 수 있습니다
- 코드 리뷰가 매우 어려워집니다
- 부분 롤백이 불가능합니다
- git bisect/cherry-pick/blame 활용 어려움

💡 도움말:
   통합 커밋은 전체 롤백과 코드 리뷰가 어려워질 수 있습니다.
   기본 정책(자동 분리)을 따르는 것을 권장합니다.

정말로 통합 커밋으로 진행하시겠습니까?

1. 아니오 - 자동 분리로 돌아가기 (권장)
2. 예 - 통합 커밋으로 진행
3. 취소
```

---

## Auto-Split Commit UI

For auto-split commits, apply same pattern per group:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[1/3] 그룹 1 커밋
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📝 커밋 메시지를 선택하세요:

> 1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천)
  2. docs(commit-skill): 커밋 스킬 문서 추가
  3. feat(commit-skill): 자동 커밋 메시지 생성기
  4. 직접 입력

[↑↓: 이동 | ←→: 본문 보기 | Enter: 선택]
```

Then final confirmation for that group.

---

## Benefits

### User Experience

1. **Quick scanning**: Headers only by default
2. **Informed decision**: Preview any message body on demand
3. **Clear recommendation**: #1 always best choice
4. **Safety**: Full message review before commit
5. **Flexibility**: Easy navigation and comparison

### Token Efficiency

1. **Compact display**: Less screen clutter
2. **On-demand detail**: Body shown only when needed
3. **Pre-generated**: All messages ready in metadata

---

## Implementation Notes

### AskUserQuestion Tool

Claude Code uses `AskUserQuestion` tool, which may not support:
- Arrow key navigation
- Dynamic expand/collapse
- Real-time preview

**Workaround:**
Use numbered list with explicit preview options:

```
📝 커밋 메시지를 선택하세요:

메시지 (헤더만):
  1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천)
  2. docs(commit-skill): 커밋 스킬 문서 추가
  3. feat(commit-skill): 자동 커밋 메시지 생성기
  4. docs(claude-skills): commit 스킬 구현

본문 미리보기:
  11. 1번 메시지 본문 보기
  12. 2번 메시지 본문 보기
  13. 3번 메시지 본문 보기
  14. 4번 메시지 본문 보기

선택:
  1-4: 해당 메시지로 진행
  11-14: 해당 메시지 본문 미리보기
  5: 직접 입력
```

**OR** simpler approach - always show full messages (current behavior):
- Less interaction complexity
- All info visible upfront
- User can scroll to compare

**Recommendation:**
- Start with **full messages shown** (current template design)
- Add compact view as future enhancement when UI framework supports it

---

## Usage in PROCESS.md

When a specific situation is detected, read only the relevant template:

```bash
# Example: Tidy First violation detected
# Read only template-1-tidy-first.md (not all templates)
cat .claude/skills/commit/templates/template-1-tidy-first.md
```

---

## AskUserQuestion Tool Structure

All templates use the `AskUserQuestion` tool with the following structure:

```json
{
  "questions": [{
    "question": "질문 내용",
    "header": "12자 이하 헤더",
    "multiSelect": false,
    "options": [
      {
        "label": "옵션 레이블",
        "description": "상세 설명"
      }
    ]
  }]
}
```

**Note**: The tool automatically adds an "Other" option for direct user input.

---

## Related Documents

- **[../PROCESS.md](../PROCESS.md)** - Execution process with template references
  - When and how each template is used
  - Step-by-step template invocation
- **[../MESSAGE_GENERATION.md](../MESSAGE_GENERATION.md)** - Message generation strategies
  - How suggested messages are generated for template-3
- **[../METADATA.md](../METADATA.md)** - Message storage and retrieval
  - How templates consume pre-generated metadata
  - Token efficiency through metadata reuse
- **[../RULES.md](../RULES.md)** - Validation rules
  - Format validation in template-5 (direct input)
- **[../EXAMPLES.md](../EXAMPLES.md)** - Complete message examples
  - Real examples shown in templates

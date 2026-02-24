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

---

### Detect Tidy First Violation

When structural changes (refactor) and behavioral changes (feat/fix) are mixed.

Tidy First 원칙: 구조적 변경(refactor)과 동작 변경(feat/fix)을 분리하세요.

**Actions:**

1. **Display warning:**

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ Tidy First 원칙 위반 감지!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

감지된 혼합 변경:
  • 구조적 변경 (refactor):
    - {filename}: {change_description}

  • 동작 변경 (feat/fix):
    - {filename}: {change_description}

💡 권장사항:
   구조적 변경과 동작 변경을 분리하면 코드 리뷰가 쉽고,
   문제 발생 시 선택적 롤백이 가능합니다.
```

2. **Call AskUserQuestion:**

```json
{
  "questions": [
    {
      "question": "Tidy First 원칙 위반이 감지되었습니다. 어떻게 하시겠습니까?",
      "header": "Tidy First",
      "multiSelect": false,
      "options": [
        {
          "label": "리셋 후 분리 [권장]",
          "description": "변경사항을 unstage하고 refactor와 feat/fix를 별도 커밋으로 분리합니다. git reset HEAD를 실행합니다."
        },
        {
          "label": "그대로 진행",
          "description": "혼합된 상태로 커밋합니다. 롤백과 리뷰가 어려워질 수 있습니다."
        }
      ]
    }
  ]
}
```

3. **Process user selection:**
   - If "리셋 후 분리": Execute `git reset HEAD`, guide separation, exit
   - If "그대로 진행": Continue with dominant type (show warning)

---

### Verify Logical Independence (Important)

Separate logically independent changes even if same type.

**Note:** Auto-split is the default policy for logically independent changes.

**Actions:**

1. **Display warning:**

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ 논리적으로 독립적인 변경사항 감지!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

감지된 그룹:
  그룹 1: {directory}/ ({n}개 파일)
    - {context_description}

  그룹 2: {directory}/ ({n}개 파일)
    - {context_description}

총 {total}개 파일이 {n}개의 독립적인 컨텍스트로 나뉩니다.

💡 도움말:
   통합 커밋은 전체 롤백과 코드 리뷰가 어려워질 수 있습니다.
   기본 정책(자동 분리)을 따르는 것을 권장합니다.
```

2. **Call AskUserQuestion:**

```json
{
  "questions": [
    {
      "question": "커밋 전략을 선택하세요",
      "header": "전략 선택",
      "multiSelect": false,
      "options": [
        {
          "label": "자동 분리 [기본 정책]",
          "description": "각 그룹을 독립적인 커밋으로 분리합니다. 명확한 히스토리, 쉬운 리뷰, 선택적 롤백이 가능합니다."
        },
        {
          "label": "통합 커밋",
          "description": "모든 변경을 하나의 커밋으로 통합합니다. 전체 롤백 시 모든 변경이 함께 되돌려집니다."
        },
        {
          "label": "취소",
          "description": "커밋 프로세스를 종료합니다"
        }
      ]
    }
  ]
}
```

3. **Process user selection:**
   - "자동 분리" → Load `rules/logical-independence.md` for auto-split process
   - "통합 커밋" → Show warning, proceed to Step 3
   - "취소" → Exit process

---

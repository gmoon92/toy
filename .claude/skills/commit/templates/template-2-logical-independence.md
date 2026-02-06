# Template 2: Logical Independence Detection

## Situation

10+ files changed in different contexts

## Template (형식 명세)

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

**형식 설명:**

이 템플릿은 **완전히 정적**입니다:
- 논리적 독립성 감지 시 항상 동일한 옵션 제공
- 동적 요소 없음

**정적 요소 (모든 세션 동일):**
- question: "커밋 전략을 선택하세요"
- header: "전략 선택"
- 3개 옵션 레이블: "자동 분리 [기본 정책]", "통합 커밋", "취소"
- 모든 description

**동적 요소:**
- 없음 (Screen Output에서 감지된 그룹 정보는 동적)

## Example (구체적 예시)

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

**Note:** Template과 Example이 동일합니다 (완전히 정적이므로).

## Screen Output (Korean for users)

Display this message before calling AskUserQuestion:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ 논리적으로 독립적인 변경사항 감지!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

감지된 그룹:
  그룹 1: .claude/skills/commit/ (4개 파일)
    - 커밋 자동화 스킬 문서

  그룹 2: ai/docs/claude/ (70개 파일)
    - Claude API 문서 번역

  그룹 3: .claude/agents/ (8개 파일)
    - 번역 에이전트 설정

총 82개 파일이 3개의 독립적인 컨텍스트로 나뉩니다.

💡 도움말:
   통합 커밋은 전체 롤백과 코드 리뷰가 어려워질 수 있습니다.
   기본 정책(자동 분리)을 따르는 것을 권장합니다.
```

Then call AskUserQuestion with Template JSON (tool will display question and options automatically).

## User Action

- "자동 분리" selected → Proceed to AUTO_SPLIT.md process
- "통합 커밋" selected → Show warning and proceed to Step 3
- "취소" selected → Exit process

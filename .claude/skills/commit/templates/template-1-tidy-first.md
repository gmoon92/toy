# Template 1: Tidy First Violation Detection

## Situation

Mixed structural changes (refactor) and behavioral changes (feat/fix)

## Template (형식 명세)

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

**형식 설명:**

이 템플릿은 **완전히 정적**입니다:
- Tidy First 위반 감지 시 항상 동일한 옵션 제공
- 동적 요소 없음

**정적 요소 (모든 세션 동일):**
- question: "Tidy First 원칙 위반이 감지되었습니다. 어떻게 하시겠습니까?"
- header: "Tidy First"
- 2개 옵션 레이블: "리셋 후 분리 [권장]", "그대로 진행"
- 모든 description

**동적 요소:**
- 없음 (Screen Output에서 감지된 변경사항 표시는 동적)

## Example (구체적 예시)

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

**Note:** Template과 Example이 동일합니다 (완전히 정적이므로).

## Screen Output (Korean for users)

Display this message before calling AskUserQuestion:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ Tidy First 원칙 위반 감지!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

감지된 혼합 변경:
  • 구조적 변경 (refactor):
    - BatchJobConfig.java: 메서드 추출
    - BatchService.java: 변수명 개선

  • 동작 변경 (feat):
    - RetryConfig.java: 재시도 로직 추가
    - BatchController.java: 새 엔드포인트 추가

💡 권장사항:
   구조적 변경과 동작 변경을 분리하면 코드 리뷰가 쉽고,
   문제 발생 시 선택적 롤백이 가능합니다.
```

Then call AskUserQuestion with Template JSON (tool will display question and options automatically).

## User Action

- "리셋 후 분리" selected → Execute `git reset HEAD` and exit with separation guide
- "그대로 진행" selected → Continue to Step 3 with warning message

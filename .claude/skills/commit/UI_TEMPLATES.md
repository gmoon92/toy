# User Interaction Templates (Index)

**⚠️ This file is now an index only. All template details have been moved to individual files in the `ui/` directory for better context efficiency.**

## Why Separate Templates?

### Token Efficiency
- **Selective loading**: Only load templates when needed
- **75-90% token savings**: Load ~50-100 lines instead of entire file (~800 lines)
- **Scalability**: Easy to add new templates without affecting others

### Maintainability
- **Independent updates**: Modify each template without affecting others
- **Clear purpose**: File name indicates template purpose
- **Version control**: Track changes per template

## Template Files

All templates are located in **[ui/](ui/)** directory:

### Step 2: Violation Detection

- **[template-1-tidy-first.md](ui/template-1-tidy-first.md)** - Tidy First 위반 감지
  - Situation: 구조적 변경(refactor)과 동작 변경(feat/fix)이 혼재
  - Actions: 리셋 후 분리 / 그대로 진행

### Step 2: Logical Independence

- **[template-2-logical-independence.md](ui/template-2-logical-independence.md)** - 논리적 독립성 감지
  - Situation: 10개 이상 파일이 서로 다른 컨텍스트로 변경됨
  - Actions: 자동 분리 (기본 정책) / 통합 커밋 / 취소

### Step 3: Message Generation

- **[template-3-message-selection.md](ui/template-3-message-selection.md)** - 커밋 메시지 선택
  - Situation: 5개 메시지 제안 중 선택
  - Actions: 메시지 1-4 선택 / Other (직접 입력)

### Step 4: User Approval

- **[template-4-final-confirmation.md](ui/template-4-final-confirmation.md)** - 최종 확인 (커밋 직전)
  - Situation: 선택한 메시지로 커밋 직전 최종 확인
  - Actions: 승인 - 커밋 실행 / 수정 / 취소

- **[template-5-direct-input.md](ui/template-5-direct-input.md)** - 메시지 수정 (직접 입력)
  - Situation: 사용자가 "Other" 옵션을 선택하여 직접 입력
  - Process: 입력 안내 → 텍스트 입력 → 형식 검증 → 최종 확인

## AskUserQuestion Tool

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

## Usage in PROCESS.md

When a specific situation is detected, read only the relevant template:

```bash
# Example: Tidy First violation detected
# Read only template-1 (not all templates)
cat .claude/skills/commit/ui/template-1-tidy-first.md
```

## Related Documents

- **[ui/README.md](ui/README.md)** - Detailed explanation of UI templates directory
- **[PROCESS.md](PROCESS.md)** - Execution process with template references
- **[SKILL.md](SKILL.md)** - Overview and core principles
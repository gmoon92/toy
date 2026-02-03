# UI Templates Directory

This directory contains individual UI templates for user interactions during the commit process.

## Template Files

Each template is separated into its own file for efficient context loading:

- **[template-1-tidy-first.md](template-1-tidy-first.md)** - Tidy First 위반 감지
- **[template-2-logical-independence.md](template-2-logical-independence.md)** - 논리적 독립성 감지
- **[template-3-message-selection.md](template-3-message-selection.md)** - 커밋 메시지 선택
- **[template-4-final-confirmation.md](template-4-final-confirmation.md)** - 최종 확인
- **[template-5-direct-input.md](template-5-direct-input.md)** - 메시지 수정 (직접 입력)

## Benefits

### Token Efficiency
- **Selective loading**: Only load templates when needed
- **75-90% token savings**: Load ~50-100 lines instead of entire UI_TEMPLATES.md (~800 lines)
- **Scalability**: Easy to add new templates without affecting others

### Maintainability
- **Independent updates**: Modify each template without affecting others
- **Clear purpose**: File name indicates template purpose
- **Version control**: Track changes per template

## Usage in PROCESS.md

When a specific situation is detected, read only the relevant template:

```bash
# Example: Tidy First violation detected
# Read only template-1-tidy-first.md (not all templates)
cat .claude/skills/commit/ui/template-1-tidy-first.md
```

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
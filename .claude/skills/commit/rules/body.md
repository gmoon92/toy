# Body Generation Rules

Rules for generating commit body items (Stage 2).

## Core Principle

- ❌ **Don't**: List changed files (already in git log)
- ✅ **Do**: Describe what work was done

## Format

Each body line MUST start with `- ` (dash + space):

```
<type>(scope): <header message>

- Work item 1 description
- Work item 2 description
- Work item 3 description
```

## Generation Strategy

Generate **5-10 body item candidates**:

1. **Analyze changes**: What was modified, added, or removed
2. **Group by purpose**: Related changes by logical purpose
3. **Clear descriptions**: Focus on "what work was done"
4. **Natural order**: By significance (no mechanical scoring)

## Language

**ALL body items MUST be in Korean** (한국어)

Example:
```
- 커밋 메시지 생성을 3단계 선택 방식으로 변경
- 헤더 생성 전략을 5개 옵션 제공으로 재작성
```

## Selection

- Multi-select enabled
- User can select multiple items or none
- Selected items assembled with `- ` prefix


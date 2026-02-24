# Header Generation Rules

Rules for generating commit header messages (Stage 1).


## Format

```
<type>(<scope>): <message>
```

## Scope Selection Priority

1. **Module name** (preferred for multi-file changes): `commit-skill`, `spring-batch`
2. **Filename** (single file changes): `UserService.java`, `README.md`
3. **Directory name**: `auth`, `utils`

## Generation Strategy

Generate **5 header messages**:

- **Recommended 1**: Best scope + clear message
- **Recommended 2**: Alternative scope or type interpretation
- **General 1-3**: Scope variations, expression variations, type alternatives

### Refresh Logic

When user requests "other recommendations":
- Keep Recommended 1 & 2 fixed
- Regenerate General 1-3 with new variations

## Language

**ALL messages MUST be in Korean** (한국어)

Examples:
- ✅ `feat(spring-batch): 배치 재시도 로직 구현`
- ❌ `feat(spring-batch): implement batch retry logic`


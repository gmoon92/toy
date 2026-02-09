# Long-Term Reasoning and State Tracking

Claude 4.5 models excel at maintaining context and state across extended interactions.

## Progressive Focus

Claude 4.5 models maintain direction across extended sessions by focusing on incremental progress toward a few things at a time rather than trying everything at once.

**Application to Skills:**
- Design phase-based reference loading
- Use metadata to cache analysis results
- Break complex operations into sequential phases

**Example:**
```markdown
## Phase-Based Loading

### Phase 1: Analysis
- Load: guides/analyzing.md
- Purpose: Understand current state
- Cache: analysis results in metadata

### Phase 2: Implementation
- Load: guides/implementing.md
- Purpose: Apply changes
- Read: cached analysis from Phase 1
```

---

## Context Awareness

Claude 4.5 models can track remaining context window throughout conversation, allowing better resource management.

**Application to Skills:**
- Structure documentation to minimize token usage
- Extract code to executable scripts (0 tokens for execution)
- Use references that load only when needed

**Example:**
```markdown
## When to Load References

Load references progressively based on phase:

- **Planning Phase**: Load planning-guide.md only
- **Implementation Phase**: Load implementation-guide.md only
- **Validation Phase**: Load validation-guide.md only

This achieves ~67% token savings compared to loading all upfront.
```

---

## Multi-Context Window Workflows

For tasks spanning multiple context windows:

1. **Use different prompts for first vs. subsequent windows**
   - First window: Set up framework
   - Later windows: Iterate on task list

2. **Have model write structured tests**
   - Generate tests in structured format (e.g., `tests.json`)
   - Track progress across sessions

3. **Create setup scripts**
   - Scripts to bootstrap new context windows
   - Gracefully start servers, run tests, run linters

4. **Fresh start vs. compression**
   - Claude 4.5 is very effective at discovering state from filesystem
   - Sometimes better than compression

5. **Provide validation tools**
   - Tools to verify correctness without human feedback
   - UI testing, automated checks

---

## State Management Best Practices

**Use structured formats for state data:**
- JSON for schema-based information (test results, task status)
- Text files for free-form progress notes
- Git for work history and checkpoints

**Example:**
```json
// Structured state (tests.json)
{
  "tests": [
    {"id": 1, "name": "authentication_flow", "status": "passing"},
    {"id": 2, "name": "user_management", "status": "failing"}
  ],
  "total": 200,
  "passing": 150,
  "failing": 25
}
```

```text
// Progress notes (progress.txt)
Session 3 progress:
- Fixed authentication token validation
- Updated user model for edge cases
- Next: Investigate user_management test failures
```

---

## Summary

For long-running tasks:
- **Phase-based loading** saves tokens
- **Metadata caching** avoids redundant work
- **Structured state** enables context continuity
- **Git tracking** provides history and checkpoints

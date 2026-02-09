# Quick Pattern Reference

Common patterns for skill development.

## XML Output Structure

```markdown
<section_name>
Content with specific formatting.
</section_name>

<another_section>
Different content with different rules.
</another_section>
```

---

## Script Execution Directive

```markdown
**MANDATORY: Execute pre-built script (DO NOT inline)**

```bash
# EXECUTE_SCRIPT: scripts/category/script_name.sh

./scripts/category/script_name.sh < input.json > output.json
```

**IMPORTANT:**
- ALWAYS use pre-built script via EXECUTE_SCRIPT directive
- DO NOT reconstruct commands
- DO NOT inline commands
```

---

## Phase-Based Loading

```markdown
## When to Load References

### Phase 1: [Name]
- **Load**: [path/to/doc.md](path)
- **Purpose**: Brief explanation
- **When**: Trigger condition

### Phase 2: [Name]
- **Load**: [path/to/doc.md](path)
- **Purpose**: Different capability
- **When**: Different trigger
```

---

## Default to Action

```markdown
<default_to_action>
Default to implementing changes rather than suggesting.
Infer user intent and proceed with most likely useful action.
Use tools to discover missing details.
</default_to_action>
```

---

## Investigate First

```markdown
<investigate_before_answering>
DO NOT guess about code you haven't read.
MUST read files before making claims.
Provide grounded, hallucination-free answers only.
</investigate_before_answering>
```

---

## Avoid Overengineering

```markdown
<avoid_overengineering>
Only make directly requested changes.
Keep solutions simple and focused.
Do not add features beyond requirements.
</avoid_overengineering>
```

---

## Parallel Tools

```markdown
<use_parallel_tool_calls>
Execute independent tool calls in parallel.
Do NOT use parallel for dependent operations.
</use_parallel_tool_calls>
```

---

## Metadata Caching

```markdown
### Analysis Phase
<metadata_write>
{
  "analysis_complete": true,
  "change_type": "feature",
  "scope": "auth"
}
</metadata_write>

### Generation Phase
<metadata_read>
Read: change_type, scope
</metadata_read>
```

---

## Success Criteria

```markdown
## Success Criteria

Task complete when:
1. **[Criterion 1]** - Specific outcome
2. **[Criterion 2]** - Another outcome
3. **[Criterion 3]** - Validation requirement

Verification:
- [ ] Checklist item 1
- [ ] Checklist item 2
```

---

## Error Handling

```markdown
## Error Handling

**Error:** [Pattern]
**Cause:** [Why]
**Recovery:**
1. Diagnose step
2. Fix step
3. Verify step
4. Retry step
```

---

## Scope Definition

```markdown
## Scope

**DO:**
- ✅ Specific action 1
- ✅ Specific action 2

**DON'T:**
- ❌ Out of scope action 1
- ❌ Out of scope action 2
```

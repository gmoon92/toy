# Specific Situation Guidelines

Practical guidelines for specific scenarios in skill development.

## Documents

### [01-tool-usage.md](01-tool-usage.md)
**Guiding Claude's tool usage behavior**

Topics:
- Default to action vs. conservative approach
- Parallel tool execution
- Investigation before answering
- Avoiding over-triggering

**When to read:** Need to control how Claude uses tools

---

### [02-format-control.md](02-format-control.md)
**Controlling response format and structure**

Topics:
- Positive instructions (what TO do)
- XML format indicators
- Matching prompt style to output
- Avoiding excessive markdown
- Structuring complex outputs

**When to read:** Need consistent output format

---

### [03-avoid-overengineering.md](03-avoid-overengineering.md)
**Keeping skills simple and focused**

Topics:
- Preventing unnecessary abstractions
- Scope definition
- Test focus without overfitting
- Avoiding backwards-compatibility hacks

**When to read:** Want to prevent scope creep and complexity

---

### [04-code-exploration.md](04-code-exploration.md)
**Encouraging thorough code investigation**

Topics:
- Exploration requirements
- Investigation workflows
- Search patterns with Grep
- Multi-file analysis
- Citing code locations

**When to read:** Need thorough codebase understanding

---

## Quick Reference

### Tool Usage

```markdown
<default_to_action>
Implement changes rather than suggesting.
</default_to_action>

<use_parallel_tool_calls>
Execute independent tools in parallel.
</use_parallel_tool_calls>

<investigate_before_answering>
Read code before making claims.
</investigate_before_answering>
```

### Format Control

```markdown
<section_name>
Structure output with XML tags.
</section_name>

Tell what TO do, not what NOT to do.
Match prompt style with desired output.
```

### Avoid Overengineering

```markdown
<avoid_overengineering>
Only make directly requested changes.
Keep solutions simple and focused.
</avoid_overengineering>
```

### Code Exploration

```markdown
<explore_code_thoroughly>
Read all relevant files before suggesting changes.
Be rigorous in searching for key facts.
</explore_code_thoroughly>
```

---

## Combining Guidelines

Effective skills often combine multiple guidelines:

```markdown
## Example: Complete Phase Definition

<default_to_action>
Automatically proceed with implementation.
</default_to_action>

<investigate_before_answering>
Read all files before making recommendations.
</investigate_before_answering>

<avoid_overengineering>
Only make necessary changes.
</avoid_overengineering>

**Output Structure:**

<analysis>
[Structured findings]
</analysis>

<implementation>
[Changes made]
</implementation>
```

---

## See Also

- [../principles/](../principles/) - Core principles
- [../reference/](../reference/) - Quick reference

# Tool Usage Guidelines

How to guide Claude's tool usage behavior in skills.

## Default to Action

Claude 4.5 is trained for precise instruction-following. Guide it to implement rather than suggest.

### Pattern

```markdown
<default_to_action>
Default to implementing changes rather than just suggesting them.
When user intent is unclear, infer the most likely useful action and proceed,
using tools to discover missing details rather than guessing.
Infer whether tool calls (e.g., file editing or reading) are intended and act accordingly.
</default_to_action>
```

### Example: File Editing

```markdown
## BAD (Claude will only suggest)
Can you suggest some changes to improve this function?

## GOOD (Claude will implement)
Improve the performance of this function.
```

### Alternative: Conservative Approach

If you want Claude to be more cautious:

```markdown
<do_not_act_before_instructions>
Do not jump into implementation or file changes unless clearly instructed to do so.
When user intent is ambiguous, default to providing information, performing investigation,
or offering recommendations rather than taking action.
Proceed with edits, modifications, or implementation only when user explicitly requests it.
</do_not_act_before_instructions>
```

---

## Parallel Tool Execution

Claude 4.x models excel at parallel tool execution. Encourage this for efficiency.

### Pattern

```markdown
<use_parallel_tool_calls>
When calling multiple tools with no dependencies between them, make all independent
calls in parallel.

Example: Reading 3 files - make 3 parallel Read calls to load all simultaneously.

HOWEVER: If tool calls depend on previous results for parameter values, do NOT call
in parallel. Call sequentially instead. Never use placeholders for dependent values.
</use_parallel_tool_calls>
```

### Example: Multi-File Analysis

```markdown
**DO (Parallel):**
- Read file1.md
- Read file2.md
- Read file3.md
(All 3 calls in one response)

**DON'T (Sequential):**
- Read file1.md → wait
- Read file2.md → wait
- Read file3.md → wait
```

### Reducing Parallel Execution

If needed for stability:

```markdown
Execute tasks sequentially with brief pauses between each step for stability.
```

---

## Investigate Before Answering

Prevent hallucinations by requiring investigation before claims.

### Pattern

```markdown
<investigate_before_answering>
DO NOT guess about code you haven't opened.
When user references a specific file, you MUST read it before answering.
Before answering questions about the codebase, investigate and read relevant files.
Do not make any claims about code unless you have investigated it.
Provide grounded, hallucination-free answers only when confident of accuracy.
</investigate_before_answering>
```

### Example: Code Analysis

```markdown
## Analysis Requirements

Before analyzing or refactoring code:

1. **Read all relevant files** using Read tool
2. **Search for patterns** using Grep if needed
3. **Verify assumptions** by checking actual code
4. **Document findings** based on real code, not assumptions

NEVER:
- Guess about file contents
- Assume code structure without verification
- Make recommendations without reading code
- Claim something exists without confirming
```

---

## Tool Usage and Triggering

Claude Opus 4.5 is more reactive to system prompts. Avoid over-triggering by using neutral language.

### Language Guidelines

**Aggressive (may over-trigger):**
```markdown
CRITICAL: You MUST use this tool whenever...
IMPORTANT: This tool should ALWAYS be used when...
```

**Neutral (better balance):**
```markdown
Use this tool when...
This tool is appropriate for...
```

---

## Summary

Guide tool usage through:
- **Default to action** - Implement vs. suggest
- **Parallel execution** - Maximize efficiency
- **Investigation first** - Prevent hallucinations
- **Neutral language** - Avoid over-triggering

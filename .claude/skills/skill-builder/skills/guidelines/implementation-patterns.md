# Implementation Patterns for Skills

This document provides concrete implementation patterns based on Claude 4.x best practices.

## Pattern 1: Structured Output with XML Tags

### Problem
Claude needs to produce multi-part outputs with clear boundaries and consistent structure.

### Solution
Use XML tags to define output structure explicitly.

### Implementation

```markdown
## Response Format

**Structure your response as:**

<phase_name>
Description of what goes in this section.
Include specific formatting requirements.
</phase_name>

<another_phase>
Different content with different rules.
Show examples of expected format.
</another_phase>
```

### Example: Commit Message Generation

```markdown
## Output Format

**MANDATORY: Structure output exactly as shown:**

<analysis>
**Modified Files:** [count]
**Change Type:** [feature|fix|refactor|docs|test]
**Scope:** [detected scope]
**Logical Independence:** ✅ yes | ⚠️ concerns noted
</analysis>

<commit_message>
type(scope): description

Optional body paragraph explaining the why behind changes.

Optional footer with references or breaking changes.
</commit_message>

<validation>
**Conventional Commit:** ✅ valid | ❌ invalid
**Tidy First Principles:** ✅ passed | ⚠️ warnings
**Ready to Commit:** ✅ yes | ❌ no - [reason]
</validation>
```

### Benefits
- Clear section boundaries
- Consistent output structure
- Easy to parse and validate
- Self-documenting format

---

## Pattern 2: Progressive Reference Loading

### Problem
Loading all documentation upfront wastes tokens and may load irrelevant content.

### Solution
Organize references by phase and load only what's needed for current phase.

### Implementation

```markdown
## When to Load References

### Phase 1: [Phase Name]
- **Load**: [path/to/doc1.md](path/to/doc1.md)
- **Load**: [path/to/doc2.md](path/to/doc2.md)
- **Purpose**: Brief explanation of what these enable
- **When**: Specific trigger condition

### Phase 2: [Next Phase]
- **Load**: [path/to/doc3.md](path/to/doc3.md)
- **Purpose**: Different capability
- **When**: Different trigger
```

### Example: Commit Skill

```markdown
## When to Load References

### Analysis Phase
- **Load**: [process/01-analysis-phase.md](references/process/01-analysis-phase.md)
- **Purpose**: Understand how to collect and analyze git changes
- **When**: Starting commit generation OR user asks about changes

### Generation Phase
- **Load**: [algorithms/commit-message-generation.md](references/generation/commit-message-generation.md)
- **Purpose**: Generate conventional commit messages
- **When**: After analysis is complete

### Validation Phase
- **Load**: [validation/tidy-first-principles.md](references/validation/tidy-first-principles.md)
- **Purpose**: Validate against Tidy First principles
- **When**: After message generation
```

### Benefits
- ~67% token savings (typical)
- Focused context per phase
- Faster loading
- Clearer workflow

---

## Pattern 3: Script Extraction with Strong Directives

### Problem
Inline code in documentation:
- Consumes tokens when loaded into context
- May be reconstructed or interpreted differently
- No guarantee of deterministic execution

### Solution
Extract to executable scripts with explicit execution directives.

### Implementation

**Step 1: Extract to Script**
```bash
#!/usr/bin/env bash
# scripts/category/action_target.sh
# Purpose: One-line description
# Usage: command < input > output
# Input: Format description
# Output: Format description

# Script implementation
```

**Step 2: Add Strong Directive in Documentation**
```markdown
**MANDATORY: Execute pre-built script (DO NOT inline commands)**

```bash
# EXECUTE_SCRIPT: scripts/category/action_target.sh

./scripts/category/action_target.sh < input.json > output.json
```

**CRITICAL:**
- ALWAYS use the pre-built script via `EXECUTE_SCRIPT:` directive
- DO NOT reconstruct commands from documentation
- DO NOT inline bash commands
- Scripts ensure deterministic execution (0 token cost)
```

### Example: Git Change Collection

**Before (Inline):**
```markdown
## Collecting Changes

Run these commands:
```bash
git diff --staged
git diff HEAD
git status --short
```

**After (Script Extraction):**

**File: scripts/analysis/collect_changes.sh**
```bash
#!/usr/bin/env bash
# collect_changes.sh
# Purpose: Collect all git changes in structured format
# Usage: ./collect_changes.sh > changes.json
# Output: JSON with staged, modified, and untracked files

git diff --staged > /tmp/staged.diff
git diff HEAD > /tmp/modified.diff
git status --short > /tmp/status.txt

jq -n \
  --arg staged "$(cat /tmp/staged.diff)" \
  --arg modified "$(cat /tmp/modified.diff)" \
  --arg status "$(cat /tmp/status.txt)" \
  '{staged: $staged, modified: $modified, status: $status}'
```

**Documentation:**
```markdown
## Collecting Changes

**MANDATORY: Use pre-built script (DO NOT inline git commands)**

```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh

./scripts/analysis/collect_changes.sh > changes.json
```

**Output Format:**
```json
{
  "staged": "diff output...",
  "modified": "diff output...",
  "status": "status output..."
}
```

**IMPORTANT:**
- DO NOT reconstruct git commands from documentation
- ALWAYS execute via the pre-built script
- Script ensures consistent output format (0 tokens consumed for execution)
```

### Benefits
- 0 tokens for script execution (only output loaded)
- Deterministic execution
- Maintainable and testable
- Version controlled

---

## Pattern 4: Metadata Caching

### Problem
Analysis results from early phases need to be accessed in later phases without re-analysis.

### Solution
Cache results in metadata during analysis phase, read from metadata in later phases.

### Implementation

**Phase 1: Analysis - Write to Metadata**
```markdown
## Analysis Phase

After analyzing the changes:

1. Perform analysis using EXECUTE_SCRIPT
2. **Store results in metadata:**

<metadata_write>
{
  "change_type": "[detected type]",
  "scope": "[detected scope]",
  "file_count": [number],
  "complexity": "[low|medium|high]"
}
</metadata_write>
```

**Phase 2: Generation - Read from Metadata**
```markdown
## Generation Phase

Before generating commit message:

1. **Read cached analysis from metadata:**

<metadata_read>
Read: change_type, scope, file_count, complexity
</metadata_read>

2. Use cached data to inform generation
3. Do NOT re-analyze (analysis already complete)
```

### Example: Commit Workflow

**Analysis Phase Reference:**
```markdown
## Caching Analysis Results

After analysis is complete, cache the results:

<metadata_write>
{
  "analysis_complete": true,
  "change_type": "feature",
  "scope": "auth",
  "files_modified": 3,
  "logical_independence": true,
  "tidy_first_compliant": true
}
</metadata_write>

These results will be available in subsequent phases.
```

**Generation Phase Reference:**
```markdown
## Using Cached Analysis

**IMPORTANT: Do NOT re-analyze**

Read analysis results from metadata:

<metadata_read>
Required: change_type, scope, logical_independence
</metadata_read>

Use these cached values to generate the commit message.
Re-analysis wastes tokens and is unnecessary.
```

### Benefits
- Avoid redundant analysis
- Consistent data across phases
- Token savings
- Faster execution

---

## Pattern 5: Default to Action

### Problem
Claude may suggest changes instead of implementing them, or wait for confirmation unnecessarily.

### Solution
Add explicit directive to default to action.

### Implementation

```markdown
<default_to_action>
Default to implementing changes rather than just suggesting them.
When user intent is unclear, infer the most likely useful action and proceed.
Use tools to discover missing details rather than guessing.
Infer whether tool calls (file editing, reading) are intended and act accordingly.
</default_to_action>
```

### Example: Skill Refactoring

```markdown
## Execution Approach

<default_to_action>
When refactoring skills:
- Read files without asking permission
- Extract scripts when inline code is detected
- Create directories as needed
- Update documentation immediately
- Commit changes after validation

Ask for confirmation ONLY when:
- Multiple valid approaches exist
- Destructive actions are required
- User preference affects outcome
</default_to_action>
```

### Benefits
- Proactive execution
- Reduced back-and-forth
- Better user experience
- Faster task completion

---

## Pattern 6: Investigate Before Answering

### Problem
Claude might guess about code or make claims without verification.

### Solution
Enforce investigation before making claims.

### Implementation

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

<investigate_before_answering>
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

ALWAYS:
- Read before suggesting changes
- Verify before making claims
- Cite specific files and line numbers
- Base recommendations on actual code
</investigate_before_answering>
```

### Benefits
- Accurate recommendations
- No hallucinations
- Grounded in reality
- Builds trust

---

## Pattern 7: Parallel Tool Execution

### Problem
Sequential tool calls waste time when operations are independent.

### Solution
Execute independent tool calls in parallel.

### Implementation

```markdown
<use_parallel_tool_calls>
When calling multiple tools with no dependencies between them, make all independent
calls in parallel.

Prioritize concurrent tool execution when tasks can be done simultaneously.

Example: Reading 3 files - make 3 parallel Read calls to load all simultaneously.

HOWEVER: If tool calls depend on previous results, do NOT call in parallel.
Call sequentially instead. Never use placeholders for dependent values.
</use_parallel_tool_calls>
```

### Example: Multi-File Analysis

```markdown
## Analysis Approach

<use_parallel_tool_calls>
When analyzing multiple files:

**DO (Parallel):**
```
Read file1.md
Read file2.md
Read file3.md
```
(All 3 calls in one response)

**DON'T (Sequential):**
```
Read file1.md
[wait for result]
Read file2.md
[wait for result]
Read file3.md
```

**Exception:** If file2 path depends on file1 contents, then sequential is required.
</use_parallel_tool_calls>
```

### Benefits
- Faster execution
- Better resource utilization
- Improved efficiency
- Reduced latency

---

## Pattern 8: Avoid Overengineering

### Problem
Claude may create unnecessary abstractions, utilities, or features beyond requirements.

### Solution
Explicitly prohibit overengineering and define minimal scope.

### Implementation

```markdown
<avoid_overengineering>
Only make changes that are directly requested or clearly necessary.
Keep solutions simple and focused.

DO NOT:
- Add features beyond requirements
- Refactor unrelated code
- Create utilities for one-time operations
- Design for hypothetical future needs
- Add error handling for impossible scenarios
- Build configuration systems unless needed

DO:
- Solve the specific problem
- Use existing abstractions
- Trust internal code and frameworks
- Validate only at system boundaries
- Write minimum code for current task
</avoid_overengineering>
```

### Example: Script Creation

```markdown
## Script Scope

<avoid_overengineering>
When creating scripts:

**Required:**
- ✅ Core functionality for stated purpose
- ✅ Input/output as specified
- ✅ Error handling for external inputs

**Not Required:**
- ❌ Configuration files for simple scripts
- ❌ Logging frameworks for debug output
- ❌ Abstract base classes for single implementation
- ❌ Command-line parsers for fixed arguments
- ❌ Helper utilities used once

Keep scripts simple, focused, and maintainable.
</avoid_overengineering>
```

### Benefits
- Simpler code
- Easier maintenance
- Faster development
- Less technical debt

---

## Pattern 9: Clear Success Criteria

### Problem
Unclear when task is complete or what defines success.

### Solution
Define explicit success criteria upfront.

### Implementation

```markdown
## Success Criteria

Task is complete when:

1. **[Criterion 1]** - Specific measurable outcome
2. **[Criterion 2]** - Another specific outcome
3. **[Criterion 3]** - Validation requirement

Verification:
- [ ] Checklist item 1
- [ ] Checklist item 2
- [ ] Checklist item 3
```

### Example: Script Extraction

```markdown
## Success Criteria

Script extraction is complete when:

1. **All inline code extracted** - No code blocks remain in documentation
2. **Scripts validated** - All scripts pass syntax validation
3. **Documentation updated** - All references use EXECUTE_SCRIPT directive
4. **Execution tested** - Scripts run successfully with sample inputs
5. **README created** - scripts/README.md documents all scripts

Verification:
- [ ] `grep -r '```bash' references/` returns no inline bash blocks
- [ ] `bash -n scripts/**/*.sh` passes without errors
- [ ] All documentation uses `EXECUTE_SCRIPT:` markers
- [ ] Test execution of each script succeeds
- [ ] scripts/README.md exists and documents all scripts
```

### Benefits
- Clear completion signal
- Objective validation
- Prevents scope creep
- Enables progress tracking

---

## Pattern 10: Error Recovery Guidance

### Problem
Scripts or operations may fail, and Claude needs to know how to recover.

### Solution
Document error handling and recovery procedures.

### Implementation

```markdown
## Error Handling

### Common Errors

**Error:** [Error message or pattern]
**Cause:** [Why this happens]
**Recovery:** [How to fix]

### Exit Codes

Scripts use standard exit codes:
- `0`: Success
- `1`: [Specific error type]
- `2`: [Another error type]

### Recovery Procedures

When [condition]:
1. Step to diagnose
2. Step to fix
3. Step to verify
4. Step to retry
```

### Example: Git Operations

```markdown
## Error Handling

### Common Errors

**Error:** `fatal: not a git repository`
**Cause:** Command run outside git repository
**Recovery:**
1. Run `pwd` to check current directory
2. Navigate to repository root
3. Verify with `git status`
4. Retry operation

**Error:** `nothing to commit, working tree clean`
**Cause:** No changes detected
**Recovery:**
1. Run `git status` to verify
2. Check for unstaged changes with `git diff`
3. If truly no changes, inform user
4. Do NOT attempt commit

### Exit Codes

- `0`: Success - changes committed
- `1`: Validation failed - changes rejected
- `2`: No changes - nothing to commit
- `3`: Git error - repository issue

### Recovery from Validation Failure

When commit validation fails:
1. Present validation errors to user
2. Ask if they want to modify message
3. Re-run validation on modified message
4. Proceed only after validation passes
```

### Benefits
- Graceful error handling
- Clear recovery path
- Reduces confusion
- Better user experience

---

## Combining Patterns

Effective skills often combine multiple patterns:

```markdown
## Example: Complete Phase Definition

### Analysis Phase

**Load References:**
- [process/analysis.md](references/process/analysis.md)

**Execution:**

<default_to_action>
Automatically proceed with analysis without asking permission.
</default_to_action>

<investigate_before_answering>
Read all relevant files before making claims about code.
</investigate_before_answering>

**MANDATORY: Execute pre-built script**

```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
./scripts/analysis/collect_changes.sh > changes.json
```

**Cache Results:**

<metadata_write>
{
  "analysis_complete": true,
  "change_type": "[detected]",
  "scope": "[detected]"
}
</metadata_write>

**Output Format:**

<analysis>
**Files Modified:** [count]
**Change Type:** [type]
**Scope:** [scope]
</analysis>

**Success Criteria:**
- [ ] All changes collected
- [ ] Change type detected
- [ ] Scope identified
- [ ] Results cached in metadata
```

## Summary

These patterns, when applied together:
- **Improve token efficiency** through script extraction and caching
- **Ensure deterministic execution** via strong directives
- **Enhance reliability** through investigation requirements
- **Increase speed** via parallel execution
- **Maintain simplicity** by avoiding overengineering
- **Provide clarity** through structured outputs and clear criteria

Apply patterns selectively based on skill requirements, not blindly to every skill.

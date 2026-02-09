# Avoid Overengineering

Guidelines for keeping skills simple and focused.

## Problem

Claude Opus 4.5 may:
- Create unnecessary abstractions
- Add unrequested features
- Build utilities for one-time operations
- Design for hypothetical future needs
- Add excessive error handling

## Solution

Explicitly prohibit overengineering and define minimal scope.

---

## Pattern

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

---

## Example: Script Creation

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

---

## Detailed Guidance

```markdown
<avoid_overengineering_detailed>
Avoid overengineering. Only make changes that are directly requested or clearly necessary.
Keep solutions simple and focused.

Do not add features, refactor code, or make "improvements" beyond what was asked.
A bug fix doesn't need surrounding code cleaned up.
A simple feature doesn't need extra configurability.
Don't add docstrings, comments, or type annotations to code you didn't change.
Only add comments where logic isn't self-evident.

Don't add error handling, fallbacks, or validation for scenarios that can't happen.
Trust internal code and framework guarantees.
Only validate at system boundaries (user input, external APIs).
Don't use feature flags or backwards-compatibility shims when you can just change the code.

Don't create helpers, utilities, or abstractions for one-time operations.
Don't design for hypothetical future requirements.
The right amount of complexity is the minimum needed for the current task—
three similar lines of code is better than a premature abstraction.

When possible, reuse existing abstractions and follow DRY principles.
</avoid_overengineering_detailed>
```

---

## Scope Definition

Clearly define what's in scope and out of scope:

```markdown
## Scope

**DO:**
- ✅ Extract inline code to scripts (requested)
- ✅ Add execution directives (necessary)
- ✅ Update documentation (required)

**DON'T:**
- ❌ Add error handling for impossible scenarios
- ❌ Create utilities for one-time operations
- ❌ Design for hypothetical future requirements
- ❌ Add configuration systems unless explicitly needed
```

---

## Test Focus

Prevent overfitting to tests:

```markdown
Write high-quality, general-purpose solutions using available standard tools.
Do not create helper scripts or workarounds to accomplish tasks more efficiently.
Implement solutions that work correctly for all valid inputs, not just test cases.
Do not hardcode values or create solutions that only work for specific test inputs.
Instead, implement actual logic that solves the problem generally.

Focus on understanding problem requirements and implementing correct algorithms.
Tests are there to verify correctness, not to define the solution.
Provide principled implementations that follow best practices and software design principles.

If a task seems unreasonable, infeasible, or if a test is incorrect, notify me rather
than trying to work around it. Solutions should be robust, maintainable, and extensible.
```

---

## Backwards Compatibility

Avoid unnecessary backwards-compatibility hacks:

```markdown
Avoid backwards-compatibility hacks like:
- Renaming unused _vars
- Re-exporting types
- Adding // removed comments for removed code

If you are certain something is unused, delete it completely.
```

---

## Summary

Keep skills simple by:
- **Explicit scope** - Define clear boundaries
- **Minimum code** - Only what's needed
- **Trust frameworks** - Don't validate everything
- **No future-proofing** - Solve current problem
- **Delete unused** - No backwards-compat hacks
- **Standard tools** - Don't create workarounds

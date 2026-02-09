# Prompting Best Practices

Core principles and guidelines for writing Claude 4.x prompts.

## Purpose

Rules for writing prompts to communicate effectively with Claude. Applies to system prompts, conversations, and instructions.

## Contents

### [principles/](principles/)
**Core Prompting Principles**

- [01-general-principles.md](principles/01-general-principles.md) - Clear instructions, context, examples
- [02-long-term-reasoning.md](principles/02-long-term-reasoning.md) - State tracking, context management
- [03-communication-style.md](principles/03-communication-style.md) - Writing style guide
- [README.md](principles/README.md) - Principles overview

### [guidelines/](guidelines/)
**Situation-Specific Guidelines**

- [01-tool-usage.md](guidelines/01-tool-usage.md) - Tool usage patterns
- [02-format-control.md](guidelines/02-format-control.md) - Response format control
- [03-avoid-overengineering.md](guidelines/03-avoid-overengineering.md) - Prevent over-engineering
- [04-code-exploration.md](guidelines/04-code-exploration.md) - Code exploration guide
- [README.md](guidelines/README.md) - Guidelines overview

### [reference/](reference/)
**Quick Reference**

- [strong-directives.md](reference/strong-directives.md) - Strong directive keywords
- [quick-patterns.md](reference/quick-patterns.md) - Common patterns

---

## When to Use

### General Prompting
Apply basic principles when writing prompts

**Load:** [principles/README.md](principles/README.md)

### Specific Scenarios
Need guidance for specific situations

**Load:** [guidelines/README.md](guidelines/README.md)

### Quick Reference
Need quick reference or copy-paste patterns

**Load:** [reference/](reference/)

---

## Key Principles

1. **Be Explicit** - Clear and specific instructions
2. **Add Context** - Explain WHY
3. **Show Examples** - Complete and realistic examples
4. **Use Strong Directives** - MANDATORY, EXECUTE_SCRIPT, DO NOT
5. **Structure Output** - Clear boundaries with XML tags
6. **Default to Action** - Execute rather than suggest
7. **Investigate First** - Verify instead of guessing

---

## Related

- [../skills/](../skills/) - Skills development rules

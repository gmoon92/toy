# Core Principles

Fundamental principles for effective Claude Code skill development.

## Documents

### [01-conciseness.md](01-conciseness.md)
**Context window efficiency and token optimization**

Topics:
- Context window as shared resource
- Token budgets and efficiency
- Progressive disclosure patterns
- Script extraction for 0-token execution

**When to read:** Creating any skill, optimizing token usage

---

### [02-appropriate-freedom.md](02-appropriate-freedom.md)
**Balancing specificity based on task fragility**

Topics:
- Varying specificity by task vulnerability
- When to be prescriptive vs. flexible
- Risk-based instruction detail
- Freedom within guardrails

**When to read:** Determining instruction specificity, writing task workflows

---

## Quick Reference

### Two Foundation Principles

1. **Conciseness** - Context window is shared; be token-efficient
2. **Appropriate Freedom** - Match specificity to task fragility

### Conciseness Patterns

- Progressive disclosure: Load references only when needed
- Script extraction: Execute code without showing it (0 tokens)
- Metadata caching: Store analysis results to avoid re-reading
- Phase-based loading: Structure by workflow stages

### Appropriate Freedom Guide

**High Specificity (Fragile Tasks):**
- Git operations, deployment, data deletion
- Use EXECUTE_SCRIPT, strong directives
- Exact command sequences

**Medium Specificity (Structured Tasks):**
- Code generation with clear patterns
- Document formatting
- Provide templates and examples

**Low Specificity (Creative Tasks):**
- Analysis and research
- Design decisions
- High-level goals only

---

## See Also

- [../guidelines/](../guidelines/) - Documentation style, implementation patterns
- [../reference/](../reference/) - Quick guide, checklist
- [../../prompting/principles/](../../prompting/principles/) - General prompting principles

# Skill Development Guidelines

Practical guidelines for writing effective skill documentation and implementation.

## Documents

### [documentation-style.md](documentation-style.md)
**Complete style guide for skill documentation**

Topics:
- Clear and explicit instruction writing
- Skill document structure
- Execution directive patterns
- XML tags for format control
- Code example standards
- Progressive disclosure
- Frontmatter best practices

**When to read:** Writing or reviewing skill documentation

---

### [implementation-patterns.md](implementation-patterns.md)
**10 concrete implementation patterns with examples**

Patterns:
1. XML-structured output
2. Phase-based reference loading
3. Script extraction with strong directives
4. Metadata caching
5. Action-first execution
6. Investigation before response
7. Parallel tool execution
8. Avoid overengineering
9. Clear success criteria
10. Error recovery guidance

**When to read:** Implementing specific skill patterns

---

## Quick Reference

### Documentation Style

```markdown
# Use strong directives
EXECUTE_SCRIPT, MANDATORY, DO NOT

# Structure with XML
<phase_name>
Content
</phase_name>

# Progressive disclosure
**Load:** references/guide.md
```

### Implementation Patterns

**Script Extraction:**
```bash
EXECUTE_SCRIPT: path/to/script.sh
```

**Phase-Based Loading:**
```markdown
## Phase 1: Analysis
**Load:** references/phase1.md

## Phase 2: Implementation
**Load:** references/phase2.md
```

**Metadata Caching:**
```markdown
<analysis_results>
Save these results for later phases.
</analysis_results>
```

---

## Writing Checklist

When creating skill documentation:

- [ ] Frontmatter is explicit and specific
- [ ] Strong directive keywords used consistently
- [ ] Examples are complete with I/O formats
- [ ] Context explains WHY, not just WHAT
- [ ] Scripts use EXECUTE_SCRIPT pattern
- [ ] XML tags structure complex outputs
- [ ] References organized by phase
- [ ] Scope clearly defined with ✅/❌
- [ ] Active voice, present tense throughout
- [ ] No vague or ambiguous language

---

## See Also

- [../principles/](../principles/) - Core principles (conciseness, appropriate freedom)
- [../reference/](../reference/) - Quick guide, checklist
- [../../prompting/guidelines/](../../prompting/guidelines/) - General prompting guidelines

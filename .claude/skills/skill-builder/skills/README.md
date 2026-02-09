# Skills Development Best Practices

Core rules and patterns for Claude Code skills development.

## Purpose

Guidelines for structuring, writing, and testing effective Claude Code skills.

## Contents

### [principles/](principles/)
**Core Skill Development Principles**

Fundamental principles for writing effective skills.

- [01-conciseness.md](principles/01-conciseness.md) - Context window efficiency
- [02-appropriate-freedom.md](principles/02-appropriate-freedom.md) - Task-specific specificity tuning
- [README.md](principles/README.md) - Principles overview

### [guidelines/](guidelines/)
**Specific Skill Writing Guidelines**

Standards for skill documentation and implementation patterns.

- [documentation-style.md](guidelines/documentation-style.md) - Skill documentation style
- [implementation-patterns.md](guidelines/implementation-patterns.md) - 10 implementation patterns

### [reference/](reference/)
**Quick Reference**

Checklists and quick start guides.

- [checklist.md](reference/checklist.md) - Skill quality checklist
- [quick-guide.md](reference/quick-guide.md) - Quick start guide

---

## When to Use

### Creating New Skill
Apply structure and best practices when creating new skills

**Load:**
- [reference/quick-guide.md](reference/quick-guide.md)
- [guidelines/documentation-style.md](guidelines/documentation-style.md)

### Refactoring Skill
Focus on conciseness and quality validation when improving existing skills

**Load:**
- [principles/01-conciseness.md](principles/01-conciseness.md)
- [reference/checklist.md](reference/checklist.md)

### Implementing Patterns
Apply specific implementation patterns

**Load:**
- [guidelines/implementation-patterns.md](guidelines/implementation-patterns.md)

### Quality Validation
Validate skill quality

**Load:**
- [reference/checklist.md](reference/checklist.md)

---

## Key Principles

1. **Conciseness** - Context window is shared, keep it brief
2. **Appropriate Freedom** - Tune specificity to task fragility
3. **Progressive Disclosure** - Load content only when needed
4. **Test with Models** - Test with target models
5. **Strong Directives** - Clear execution instructions
6. **Token Efficiency** - Zero-token execution via script extraction

---

## Directory Structure

### Recommended Skill Structure

```
skill-name/
├── SKILL.md              # Core workflow (< 500 lines)
├── references/           # Detailed documentation
│   ├── guides/
│   ├── examples/
│   └── templates/
└── scripts/             # Executable scripts (optional)
    ├── analysis/
    ├── validation/
    └── utils/
```

---

## Quick Reference

### Naming
- **Skills:** gerund form (`processing-pdfs`) or noun phrase (`pdf-processing`)
- **Scripts:** verb_noun pattern (`collect_changes.sh`, `validate_message.py`)

### Descriptions
**Formula:** `[What it does] + [When to use it]`

```yaml
description: Processes PDF files extracting text and tables. Use when working with PDFs or user mentions documents.
```

### Documentation
- Keep SKILL.md < 500 lines
- Use progressive disclosure
- One level deep references
- Forward slashes in paths

---

## Related

- [../prompting/](../prompting/) - Prompting rules

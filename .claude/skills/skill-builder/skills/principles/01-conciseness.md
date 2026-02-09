# Conciseness: The Core Principle

Context window is shared across all components. Keep skills lean and focused.

## Why Conciseness Matters

Context window is shared between:
- System prompts
- Conversation history
- Other skills' metadata
- The actual request

**Key insight:** Not all tokens cost immediately. Only metadata (name + description) is pre-loaded. SKILL.md loads when relevant. Additional files load only when needed.

**However:** Once SKILL.md loads, every token competes with conversation history and other context.

---

## Fundamental Assumption

**Claude is already very capable.**

Only add context Claude doesn't already have.

Ask for each piece of information:
- "Does Claude really need this explanation?"
- "Can I assume Claude knows this?"
- "Does this paragraph justify its token cost?"

---

## Examples

### ✅ Good: Concise (~ 50 tokens)

````markdown
## PDF Text Extraction

Use pdfplumber for text extraction:

```python
import pdfplumber

with pdfplumber.open("file.pdf") as pdf:
    text = pdf.pages[0].extract_text()
```
````

### ❌ Bad: Too Verbose (~ 150 tokens)

```markdown
## PDF Text Extraction

PDF (Portable Document Format) files are common file formats containing text,
images, and other content. To extract text from PDFs, you need to use a library.
There are many libraries available for PDF processing, but we recommend pdfplumber
because it's easy to use and handles most cases well.

First you'll need to install it using pip. Then you can use the code below...
```

---

## What the Concise Version Assumes

Claude already knows:
- What PDFs are
- How libraries work
- How to install packages
- Basic Python syntax

Only provide what's specific to your skill:
- Which library to use (pdfplumber)
- How to use it (code example)

---

## Guidelines

### 1. Remove Redundant Explanations

❌ **Don't explain obvious concepts**
```markdown
A database is a system for storing structured data...
```

✅ **Assume domain knowledge**
```markdown
Query the sales database using BigQuery.
```

### 2. Cut Introductory Fluff

❌ **Verbose introduction**
```markdown
In this section, we'll explore various approaches you might consider
for handling different types of documents...
```

✅ **Direct instruction**
```markdown
## Document Processing

For Word files: Use docx-js
For PDFs: Use pdfplumber
```

### 3. Show, Don't Over-Explain

❌ **Explanation-heavy**
```markdown
The function takes a parameter called 'path' which represents the file system
location where the document is stored. This should be a string value containing
either an absolute or relative path...
```

✅ **Code + brief context**
```markdown
```python
process_document(path="./docs/report.pdf")
```

`path`: File location (absolute or relative)
```

### 4. Trust Claude's Inference

❌ **Spelling everything out**
```markdown
If the file doesn't exist, the function will raise an error. You should handle
this error by checking if the file exists first before calling the function.
```

✅ **Minimal guidance**
```markdown
Raises `FileNotFoundError` if file doesn't exist.
```

---

## Audit Questions

When reviewing skill documentation, ask:

1. **Could Claude infer this?**
   - If yes: Remove or drastically shorten

2. **Is this explanation longer than the code?**
   - Consider if explanation is really needed

3. **Am I repeating information?**
   - Consolidate or cross-reference

4. **Would this be obvious to a developer?**
   - If yes: Remove

5. **Does this teach vs. guide?**
   - Skills guide, don't teach basics

---

## Token Budget Awareness

### SKILL.md Structure

- **Optimal:** < 500 lines
- **Approaching limit:** Start splitting to reference files
- **Use patterns:** Progressive disclosure (see structure/03-progressive-disclosure.md)

### When to Split Content

If SKILL.md approaches 500 lines:
1. Identify self-contained sections
2. Move to reference files
3. Link from SKILL.md
4. Keep core workflow in SKILL.md

Example:
```markdown
## API Reference

Core endpoints in SKILL.md

Full reference: See references/api-reference.md
```

---

## Summary

**Core principle:** Assume Claude's intelligence

**Practical rule:** If it's in Claude's training, don't repeat it

**Exception:** Domain-specific knowledge, project conventions, non-obvious patterns

**Result:** Focused, token-efficient skills that respect context limits

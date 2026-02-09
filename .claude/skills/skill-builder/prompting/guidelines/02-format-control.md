# Format Control Guidelines

Techniques for controlling Claude's response format.

## Response Format Control

Methods that work well with Claude 4.x models:

### 1. Tell What TO Do (Not What NOT to Do)

**BAD:**
```markdown
Don't use markdown in your response
```

**GOOD:**
```markdown
Your response should consist of smoothly flowing prose paragraphs.
```

---

### 2. Use XML Format Indicators

**GOOD:**
```markdown
Write the prose section of your response in <smoothly_flowing_prose_paragraphs> tags.
```

**Example:**
```markdown
**Structure your response as:**

<analysis>
Present factual findings about git changes:
- Modified files count
- Staged changes count
- Detected change type
</analysis>

<commit_message>
Generate conventional commit message:
type(scope): description

[optional body]
</commit_message>
```

---

### 3. Match Prompt Style with Desired Output

The formatting style used in prompts can influence Claude's response style.

If you want less markdown in output, reduce markdown in prompts.

---

### 4. Detailed Prompts for Specific Preferences

For more control over markdown usage:

```markdown
<avoid_excessive_markdown_and_bullet_points>
When writing reports, documentation, technical explanations, analysis, or long-form content,
write in clear, flowing prose using complete paragraphs and sentences.

Use standard paragraph breaks for organization and reserve markdown primarily for:
- `inline code`
- Code blocks (```...```)
- Simple headings (##, ###)

Do NOT use **bold** and *italic*.

Do NOT use ordered lists (1. ...) or unordered lists (*) unless:
a) Presenting truly discrete items where list format is the best option, or
b) User explicitly requests a list or ranking

Instead of listing items with bullets or numbers, integrate them naturally into sentences.
This guidance applies especially to technical writing. Using prose instead of excessive
formatting improves user satisfaction. Never output excessively short bullet sequences.

Goal: Readable, flowing text that guides readers naturally through ideas rather than
isolating information into disconnected points.
</avoid_excessive_markdown_and_bullet_points>
```

---

## Structuring Complex Outputs

Use XML tags to define clear boundaries:

```markdown
## Response Structure

**MANDATORY: Structure output exactly as shown**

<section_name>
Description of what goes here.
Specific formatting requirements.
</section_name>

<another_section>
Different content with different rules.
Examples of expected format.
</another_section>
```

### Benefits

- Clear section boundaries
- Consistent structure
- Easy to parse
- Self-documenting

---

## Example: Commit Skill Output

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

Optional body paragraph explaining the why.

Optional footer with references.
</commit_message>

<validation>
**Conventional Commit:** ✅ valid | ❌ invalid
**Tidy First Principles:** ✅ passed | ⚠️ warnings
**Ready to Commit:** ✅ yes | ❌ no - [reason]
</validation>
```

---

## Summary

Control format through:
- **Positive instructions** - Say what to do
- **XML tags** - Define clear structure
- **Prompt style matching** - Match your desired output
- **Detailed preferences** - Specify markdown usage
- **Structured templates** - Show exact format expected

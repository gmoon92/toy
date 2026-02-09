# Skill Quality Checklist

Complete checklist before sharing your skill.

## Core Quality

### Description
- [ ] Description is specific and includes key terms
- [ ] Description states WHAT skill does
- [ ] Description states WHEN to use it
- [ ] Uses 3rd person ("Processes files" not "Can help you process")
- [ ] No XML tags in description
- [ ] Under 1024 characters

### Name
- [ ] Uses gerund form (processing-pdfs) or noun phrase (pdf-processing)
- [ ] Lowercase, numbers, hyphens only
- [ ] Max 64 characters
- [ ] No reserved words (anthropic, claude)
- [ ] Descriptive, not generic (not "helper", "utils")

### Content Size
- [ ] SKILL.md body under 500 lines
- [ ] Additional details split to separate files (if needed)
- [ ] Progressive disclosure used appropriately

### Clarity
- [ ] No time-sensitive information
- [ ] Consistent terminology throughout
- [ ] Examples are concrete, not abstract
- [ ] One term per concept (not "field/box/element")

---

## Structure

### File Organization
- [ ] All file paths use forward slashes (/)
- [ ] No Windows-style backslashes (\)
- [ ] File names are descriptive (not doc1.md, doc2.md)
- [ ] Files organized by domain or function

### References
- [ ] All references one level deep from SKILL.md
- [ ] No nested references (SKILL.md → ref1.md → ref2.md)
- [ ] Reference files have table of contents if > 100 lines
- [ ] Links use relative paths

### Progressive Disclosure
- [ ] Core workflow in SKILL.md
- [ ] Advanced features in separate files
- [ ] Domain-specific content separated
- [ ] Clear navigation from SKILL.md

---

## Workflows

### Complex Tasks
- [ ] Broken into clear sequential steps
- [ ] Checklistable format provided
- [ ] Each step has specific instructions
- [ ] Success criteria defined

### Feedback Loops
- [ ] Validation steps included where needed
- [ ] Error handling guidance provided
- [ ] Retry logic documented
- [ ] Quality checks built in

---

## Examples and Templates

### Examples
- [ ] Show input/output pairs
- [ ] Use realistic data (not "foo", "bar")
- [ ] Cover common cases
- [ ] Demonstrate expected format

### Templates
- [ ] Specify when to follow exactly vs. adapt
- [ ] Show complete structure
- [ ] Mark required vs. optional sections
- [ ] Include formatting details

---

## Scripts and Code

### Script Quality
- [ ] Scripts solve problems, don't punt to Claude
- [ ] Error handling is explicit and helpful
- [ ] No "magic numbers" (all constants justified)
- [ ] Clear documentation in scripts
- [ ] Input/output formats documented

### Execution
- [ ] Clear whether to execute or read for reference
- [ ] Dependencies listed and verified available
- [ ] Scripts have usage examples
- [ ] Exit codes documented

### Validation
- [ ] Critical operations have validation steps
- [ ] Validation scripts provide specific error messages
- [ ] Quality tasks include feedback loops
- [ ] "Verify then execute" pattern used where appropriate

---

## Testing

### Evaluation
- [ ] At least 3 eval cases created
- [ ] Expected behaviors documented
- [ ] Success criteria defined

### Model Testing
- [ ] Tested with Claude Haiku (if using)
- [ ] Tested with Claude Sonnet (if using)
- [ ] Tested with Claude Opus (if using)
- [ ] Guidance adjusted for model capabilities

### Real Usage
- [ ] Tested in actual scenarios (not just evals)
- [ ] Team feedback gathered (if applicable)
- [ ] Observed Claude's navigation patterns
- [ ] Iterated based on actual usage

---

## Content Guidelines

### Conciseness
- [ ] Only includes what Claude doesn't already know
- [ ] No explanations of obvious concepts
- [ ] No redundant information
- [ ] Assumes Claude's intelligence

### Appropriate Freedom
- [ ] Freedom level matches task fragility
- [ ] High freedom for context-dependent tasks
- [ ] Medium freedom for parameterized patterns
- [ ] Low freedom for critical operations

### Terminology
- [ ] One term per concept throughout
- [ ] Technical terms used correctly
- [ ] Consistent with project conventions
- [ ] No jargon without context

---

## Avoided Anti-Patterns

- [ ] No Windows-style paths
- [ ] No excessive options without guidance
- [ ] No deeply nested references
- [ ] No time-based conditionals
- [ ] No inconsistent terminology
- [ ] No assumed pre-installed tools
- [ ] No "voodoo constants"
- [ ] No punting errors to Claude

---

## Documentation

### YAML Frontmatter
- [ ] Has required `name` field
- [ ] Has required `description` field
- [ ] Follows format constraints
- [ ] No forbidden characters

### File Structure
- [ ] Has SKILL.md
- [ ] References organized logically
- [ ] Scripts in scripts/ directory
- [ ] Clear hierarchy

---

## Before Sharing

### Final Checks
- [ ] Skill tested end-to-end
- [ ] All links work
- [ ] All scripts execute successfully
- [ ] No TODO comments
- [ ] No placeholder content

### Review
- [ ] Reviewed for conciseness
- [ ] Checked examples for clarity
- [ ] Verified terminology consistency
- [ ] Confirmed progressive disclosure works

### Team
- [ ] Shared with team for feedback (if applicable)
- [ ] Incorporated observations from use
- [ ] Documented known limitations
- [ ] Added to skill registry

---

## Model-Specific Checks

### If Supporting Haiku
- [ ] Provides enough guidance
- [ ] Examples are detailed
- [ ] Steps are explicit
- [ ] Assumptions spelled out

### If Supporting Sonnet
- [ ] Clear and efficient
- [ ] Balanced detail level
- [ ] Good examples
- [ ] Reasonable freedom

### If Supporting Opus
- [ ] Not over-explained
- [ ] Trusts inference ability
- [ ] Allows flexibility
- [ ] Focuses on what's unique

---

## Quality Score

Count your checkmarks:

- **90-100%**: Excellent, ready to share
- **75-89%**: Good, address gaps before sharing
- **60-74%**: Needs work, review core principles
- **< 60%**: Substantial revision needed

Focus on:
1. Core Quality (most important)
2. Testing (validates effectiveness)
3. Structure (enables discovery)
4. Everything else (polish)

# Appropriate Freedom

Match specificity level to task fragility and variability.

## The Spectrum

Adjust control based on task characteristics:

**High Freedom** ← → **Low Freedom**
(Text guidance) ← → (Specific scripts)

---

## High Freedom: Text-Based Guidance

### When to Use

- Multiple valid approaches exist
- Decisions depend on context
- Heuristics guide the approach
- Creative solutions benefit the outcome

### Example: Code Review

```markdown
## Code Review Process

1. Analyze code structure and organization
2. Check for potential bugs or edge cases
3. Suggest improvements for readability and maintainability
4. Verify compliance with project conventions
```

**Why high freedom?** Each codebase differs. Claude can adapt approach based on what it finds.

---

## Medium Freedom: Parameterized Patterns

### When to Use

- Preferred patterns exist
- Some variation is acceptable
- Configuration affects behavior
- Structure helps but flexibility needed

### Example: Report Generation

````markdown
## Report Generation

Use this template, customize as needed:

```python
def generate_report(data, format="markdown", include_charts=True):
    # Process data
    # Generate output in specified format
    # Optionally include visualizations
```
````

**Why medium freedom?** Template provides structure, parameters allow adaptation.

---

## Low Freedom: Specific Scripts

### When to Use

- Task is fragile and error-prone
- Consistency is critical
- Specific sequence must be followed
- Wrong approach causes problems

### Example: Database Migration

````markdown
## Database Migration

Execute exactly this script:

```bash
python scripts/migrate.py --verify --backup
```

Do NOT modify the command or add additional flags.
````

**Why low freedom?** Wrong execution could corrupt data. No room for interpretation.

---

## The Bridge Analogy

Think of Claude navigating a path:

### Narrow Bridge with Cliffs

**Scenario:** Only one safe way forward
**Approach:** Provide specific guard rails and exact instructions (low freedom)
**Example:** Database migrations executed in precise order

### Wide Open Field

**Scenario:** Many paths lead to success
**Approach:** Give general direction, trust Claude to find optimal route (high freedom)
**Example:** Code review where context determines best approach

---

## Decision Matrix

| Task Characteristic | Freedom Level | Guidance Type |
|---------------------|---------------|---------------|
| Multiple valid solutions | High | Text description, principles |
| Context-dependent decisions | High | Guidelines, heuristics |
| Preferred pattern exists | Medium | Parameterized examples |
| Some flexibility beneficial | Medium | Templates with options |
| Fragile operation | Low | Exact scripts |
| Must follow sequence | Low | Step-by-step commands |
| Critical consistency | Low | No-variation instructions |

---

## Examples by Domain

### High Freedom: Content Analysis

```markdown
## Research Synthesis

1. Read all source documents
2. Identify recurring themes
3. Cross-reference claims across sources
4. Generate structured summary with evidence
5. Note conflicting viewpoints

Adapt your approach based on source material quality and consistency.
```

### Medium Freedom: API Integration

````markdown
## API Request Pattern

Use this structure, adjust timeout/retries as needed:

```python
response = requests.post(
    endpoint,
    json=data,
    timeout=30,  # Adjust for slow connections
    retries=3    # Increase if API is flaky
)
```
````

### Low Freedom: Security Operations

```markdown
## SSL Certificate Installation

Execute these commands in exact order:

```bash
./scripts/generate_cert.sh
./scripts/verify_cert.sh
./scripts/install_cert.sh --prod
```

CRITICAL: Do NOT skip verification step.
Do NOT modify flags.
```

---

## Matching Freedom to Model

Different models may need different freedom levels for same task:

**Claude Opus:**
- Can handle higher freedom
- Infers missing details well
- May over-think with too much structure

**Claude Sonnet:**
- Balanced approach works well
- Benefits from clear patterns
- Handles medium freedom effectively

**Claude Haiku:**
- May need more guidance
- Lower freedom often safer
- Explicit instructions help

Test your skill across models you plan to support.

---

## Warning Signs

### Too Much Freedom

Signs:
- Inconsistent results across runs
- Claude asks many clarifying questions
- Output quality varies significantly
- Critical steps sometimes skipped

Fix: Add more structure, specific examples, explicit requirements

### Too Little Freedom

Signs:
- Instructions feel robotic
- Can't adapt to variations
- Brittle in edge cases
- Over-specified for simple tasks

Fix: Allow parameterization, provide reasoning, trust inference

---

## Summary

**Principle:** Match control to risk

**High freedom:** When context drives decisions
**Medium freedom:** When patterns help but adaptation needed
**Low freedom:** When precision is critical

**Test:** Try task with different freedom levels, observe results

# Skill Template

Use this template to create new skills.

---

## SKILL.md Template

```markdown
---
name: skill-name
description: Brief description of what this skill does. Keep it under 200 characters.
disable-model-invocation: false
user-invocable: true
allowed-tools: Read, Grep, Glob, Bash, AskUserQuestion, Write, Edit
---

# YAML Frontmatter Validation Rules:
#
# name (required):
#   - Maximum 64 characters
#   - Must contain only lowercase letters, numbers, and hyphens
#   - Cannot contain XML tags
#   - Cannot contain reserved words: "anthropic", "claude"
#   - Use gerund form (verb-ing): processing-pdfs, analyzing-data
#
# description (required):
#   - Must not be empty
#   - Maximum 1024 characters
#   - Cannot contain XML tags
#   - Include key terms Claude should recognize
#   - Explain WHEN to use this skill

## Overview

Detailed description of the skill's purpose and capabilities.

**Core Functions:**
- Function 1: Description
- Function 2: Description
- Function 3: Description

**Scope:**
- ✅ What this skill does
- ✅ What this skill handles
- ❌ Never: What this skill avoids
- ❌ Never: Out of scope operations

## When to Load References

Execute skill in phases. Load only required references per phase for maximum token efficiency.

### Phase 1: [Phase Name]
- **Load**: [reference/path.md](references/reference/path.md)
- **Purpose**: What this phase does
- **When**: When to execute this phase

### Phase 2: [Phase Name] (conditional)
- **Load**: [reference/path.md](references/reference/path.md)
- **Purpose**: What this phase does
- **When**: Only if [condition]

[... more phases as needed ...]

### Support Resources (load as needed)
- **Resource 1**: [path.md](references/path.md) - When to use
- **Resource 2**: [path.md](references/path.md) - When to use

### Scripts (if applicable)

**CRITICAL:** Scripts are pre-built, deterministic executables. Agent MUST use `EXECUTE_SCRIPT:` directive.

**Phase X - [Operation]:**
- `EXECUTE_SCRIPT: scripts/category/script_name.sh` - What it does

**Documentation:** See [scripts/README.md](scripts/README.md) for detailed usage

## Core Principles

**1. Principle Name**
- Description of principle
- Why it matters
- How to apply it

**2. Principle Name**
- Description of principle
- Why it matters
- How to apply it

[... more principles as needed ...]

## Quick Reference

**Format/Pattern**: Description

**Key Concepts:**
- Concept 1: Description
- Concept 2: Description

## Execution Flow

```
1. Step 1     → [reference](references/step1.md)
2. Step 2     → [reference](references/step2.md) (if needed)
3. Step 3     → [reference](references/step3.md)
...
```

Each step loads only its required references. See "When to Load References" above for details.

## Usage

**Invoke skill:**
```
/skill-name
```

**Natural language:**
```
Example command 1
Example command 2
Example command 3
```
```

---

## Directory Structure Template

```
skill-name/
├── SKILL.md                 # Main skill documentation
├── README.md               # Optional: Additional context
├── references/             # Detailed reference documents
│   ├── process/           # Step-by-step process docs
│   │   ├── step1.md
│   │   ├── step2.md
│   │   └── step3.md
│   ├── guides/            # How-to guides
│   │   └── guide-name.md
│   ├── validation/        # Validation rules
│   │   └── rules.md
│   └── support/           # Supporting documents
│       ├── examples.md
│       ├── troubleshooting.md
│       └── metadata.md
├── scripts/               # Executable scripts (optional)
│   ├── README.md
│   ├── analysis/
│   ├── algorithms/
│   ├── generation/
│   ├── validation/
│   ├── execution/
│   └── utils/
└── templates/             # UI templates (not loaded into context)
    └── template-name.md
```

---

## Script Template (Bash)

```bash
#!/usr/bin/env bash
# script_name.sh
# Purpose: One-line description of what this script does
# Usage: ./script_name.sh < input.json > output.json

set -e  # Exit on error

# Parse input from stdin
if [ -t 0 ]; then
    echo "ERROR: Input must be provided via stdin" >&2
    echo "Usage: echo '{\"key\":\"value\"}' | $0" >&2
    exit 1
fi

INPUT_DATA=$(cat)

# Process input
# ... implementation ...

# Output result as JSON
echo "{"
echo "  \"status\": \"success\","
echo "  \"result\": \"...\""
echo "}"
```

---

## Script Template (JavaScript/Node.js)

```javascript
#!/usr/bin/env node
/**
 * script_name.js
 * Purpose: One-line description of what this script does
 * Usage: echo '{"key":"value"}' | node script_name.js
 * Input: {"key": "value", ...}
 * Output: {"status": "success", "result": {...}}
 */

function processData(input) {
  // Implementation
  return {
    status: 'success',
    result: {}
  };
}

// Main execution
if (require.main === module) {
  let inputData = '';

  process.stdin.on('data', chunk => {
    inputData += chunk;
  });

  process.stdin.on('end', () => {
    try {
      const input = JSON.parse(inputData);
      const output = processData(input);
      console.log(JSON.stringify(output, null, 2));
    } catch (error) {
      console.error(JSON.stringify({
        status: 'failed',
        error: error.message
      }));
      process.exit(1);
    }
  });
}

module.exports = { processData };
```

---

## Script Template (Python)

```python
#!/usr/bin/env python3
"""
script_name.py
Purpose: One-line description of what this script does
Usage: echo '{"key":"value"}' | python3 script_name.py
"""

import sys
import json

def process_data(input_data):
    """Process input and return result."""
    # Implementation
    return {
        'status': 'success',
        'result': {}
    }

if __name__ == '__main__':
    try:
        # Read from stdin
        input_data = json.load(sys.stdin)

        # Process
        output = process_data(input_data)

        # Output as JSON
        print(json.dumps(output, indent=2))

    except json.JSONDecodeError as e:
        print(json.dumps({
            'status': 'failed',
            'error': f'Invalid JSON input: {e}'
        }), file=sys.stderr)
        sys.exit(1)

    except Exception as e:
        print(json.dumps({
            'status': 'failed',
            'error': str(e)
        }), file=sys.stderr)
        sys.exit(1)
```

---

## Reference Document Template

```markdown
# Document Title

Brief overview of what this document covers.

---

## Purpose

Explain the purpose and when to use this document.

## Content

### Section 1

Detailed content...

### Section 2

Detailed content...

## Examples

Concrete examples demonstrating the concepts.

## Related Documents

- [Related Doc 1](path/to/doc1.md)
- [Related Doc 2](path/to/doc2.md)
```

---

## Usage

1. Copy this template to your new skill directory
2. Replace placeholders with actual content
3. Remove sections that don't apply
4. Add additional sections as needed
5. Follow naming conventions and directory structure

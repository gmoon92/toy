# Script Extraction Guide

Comprehensive guide for extracting inline code from documentation to executable script files.

---

## Why Extract Scripts?

### Problems with Inline Code

**1. Token Inefficiency**
- Every inline code block consumes context tokens
- Claude must read and parse code on every execution
- Typical cost: 500-2000 tokens per inline script

**2. Non-Deterministic Execution**
- Claude may interpret code differently each time
- Code reconstruction can introduce errors
- No guarantee of consistent behavior

**3. Maintenance Burden**
- Algorithm changes require updating documentation
- Code duplication across multiple files
- Hard to test independently

**4. Mixing Concerns**
- Documentation mixed with executable code
- Harder to navigate and understand
- Blurs separation between "what" and "how"

### Benefits of Script Extraction

**Token Efficiency:**
- Scripts execute without consuming context tokens
- Only output is loaded (not the script code itself)
- Typical savings: 1000-3000 tokens per skill

**Deterministic Execution:**
- Same input always produces same output
- Pre-tested, validated code
- Consistent, predictable behavior

**Easy Maintenance:**
- Update script without touching documentation
- Test scripts independently
- Clear separation of concerns

**Reusability:**
- Scripts can be used by other tools
- Can be tested in isolation
- Easy to version and track changes

---

## When to Extract Scripts

### Good Candidates for Extraction

**✅ Complex algorithms:**
- File analysis and pattern detection
- Scope/type detection logic
- Content generation algorithms

**✅ Multi-step operations:**
- Data collection and transformation
- Validation with multiple checks
- Sequential processing pipelines

**✅ Error-prone operations:**
- Git operations (commit, diff, merge)
- File system operations
- API calls and parsing

**✅ Frequently repeated code:**
- Same pattern used multiple times
- Common operations across phases
- Utility functions

### Poor Candidates for Extraction

**❌ Simple one-liners:**
```bash
ls -la
git status
echo "Done"
```

**❌ Highly variable operations:**
- Code that changes based on context
- Operations requiring human judgment
- Exploratory commands

**❌ Demonstration examples:**
- Code shown for educational purposes
- API usage examples
- Pattern illustrations

---

## Script Extraction Process

### Step 1: Identify Inline Code

**Search for code blocks:**

```bash
# Find all bash scripts
find skill-name -name "*.md" -exec grep -l '```bash' {} \;

# Find JavaScript code
find skill-name -name "*.md" -exec grep -l '```javascript' {} \;

# Find Python code
find skill-name -name "*.md" -exec grep -l '```python' {} \;
```

**Catalog findings:**
```markdown
## Inline Code Inventory

### step1-analysis.md
- bash (lines 45-67): Collect git changes and create metadata
- bash (lines 89-95): Check git status

### step5-execute.md
- bash (lines 120-145): Validate and execute commit

### algorithms.md
- javascript (lines 30-80): Detect scope from files
- javascript (lines 90-140): Detect type from changes
```

### Step 2: Design Script Architecture

**Organize by responsibility:**

```
scripts/
├── README.md              # Comprehensive documentation
├── analysis/             # Data collection and analysis
│   ├── collect_changes.sh
│   └── create_metadata.sh
├── algorithms/           # Detection and decision logic
│   ├── detect_scope.js
│   └── detect_type.js
├── generation/           # Content generation
│   └── generate_message.js
├── validation/           # Input/output validation
│   └── validate_output.py
├── execution/            # Main operations
│   └── execute_commit.sh
└── utils/                # Helper functions
    └── cleanup.sh
```

**Key principles:**
- **Single Responsibility**: One script = one clear purpose
- **Clear Naming**: verb_noun pattern (e.g., `collect_changes.sh`)
- **Logical Grouping**: Organize by category, not chronology
- **Composability**: Scripts can be piped together

### Step 3: Choose Script Language

**Bash** - System operations:
- File system operations
- Git commands
- Process management
- Text processing (with jq for JSON)

**JavaScript/Node.js** - Algorithms:
- Complex logic and decision making
- JSON manipulation
- String processing
- Parsing and pattern matching

**Python** - Data processing:
- Validation and verification
- Data transformation
- Complex calculations
- API interactions

**Decision criteria:**
- Use the simplest language that works
- Match language to the operation type
- Consider dependencies and setup
- Prefer bash for shell operations

### Step 4: Write Scripts

**Script template structure:**

#### Bash Script Template

```bash
#!/usr/bin/env bash
# script_name.sh
# Purpose: One-line description of what this script does
# Usage: ./script_name.sh < input.json > output.json
# Input: JSON format description
# Output: JSON format description

set -e  # Exit on error
set -u  # Exit on undefined variable
set -o pipefail  # Exit on pipe failure

# Parse input from stdin
if [ -t 0 ]; then
    echo "ERROR: Input must be provided via stdin" >&2
    echo "Usage: echo '{\"key\":\"value\"}' | $0" >&2
    exit 1
fi

INPUT_DATA=$(cat)

# Extract fields from JSON (using jq if available)
if command -v jq &> /dev/null; then
    FIELD=$(echo "$INPUT_DATA" | jq -r '.field')
else
    # Fallback parsing without jq
    FIELD=$(echo "$INPUT_DATA" | grep -o '"field":"[^"]*"' | cut -d'"' -f4)
fi

# Process input
# ... implementation ...

# Output result as JSON
echo "{"
echo "  \"status\": \"success\","
echo "  \"result\": \"...\""
echo "}"
```

#### JavaScript/Node.js Script Template

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
  // Validate input
  if (!input || typeof input !== 'object') {
    throw new Error('Invalid input: expected object');
  }

  // Implementation
  const result = {
    // ... processing logic ...
  };

  return {
    status: 'success',
    result: result
  };
}

// Main execution
if (require.main === module) {
  let inputData = '';

  process.stdin.setEncoding('utf8');

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

#### Python Script Template

```python
#!/usr/bin/env python3
"""
script_name.py
Purpose: One-line description of what this script does
Usage: echo '{"key":"value"}' | python3 script_name.py
"""

import sys
import json
from typing import Dict, Any

def process_data(input_data: Dict[str, Any]) -> Dict[str, Any]:
    """Process input and return result."""
    # Validate input
    if not isinstance(input_data, dict):
        raise ValueError('Invalid input: expected dictionary')

    # Implementation
    result = {
        # ... processing logic ...
    }

    return {
        'status': 'success',
        'result': result
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

**Key elements:**
- Shebang line for direct execution
- Clear documentation in header
- Usage examples in comments
- Input via stdin, output via stdout
- Errors via stderr
- Proper error handling
- JSON input/output format
- Exit codes (0 = success, 1 = failure)

### Step 5: Update Documentation

**Replace inline code with script references:**

**Before:**
````markdown
### Step 3: Collect Changes

Run this to collect git changes:

```bash
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
git add -u

# ... 30 more lines of bash ...

echo "{"
echo "  \"branch\": \"$CURRENT_BRANCH\","
# ... JSON output ...
echo "}"
```
````

**After:**
````markdown
### Step 3: Collect Changes

**MANDATORY: Use pre-built scripts (DO NOT inline bash commands)**

```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh

./scripts/analysis/collect_changes.sh
```

**IMPORTANT:**
- DO NOT reconstruct bash commands from documentation
- DO NOT inline scripts into the execution flow
- ALWAYS use the pre-built scripts via `EXECUTE_SCRIPT:` directive
- Scripts consume 0 context tokens (only output is loaded)
````

**Strong directive keywords:**
- `MANDATORY:` - Required behavior
- `EXECUTE_SCRIPT:` - Marks script location
- `DO NOT inline` - Explicit prohibition
- `ALWAYS use` - Enforcement
- `NEVER` - Strong prohibition
- `CRITICAL:` - Highest priority
- `IMPORTANT:` - Significant note

### Step 6: Create scripts/README.md

**Template for scripts documentation:**

```markdown
# Scripts Documentation

Executable scripts for [skill-name] skill.

**IMPORTANT:** All scripts are designed for deterministic execution. Agent MUST use `EXECUTE_SCRIPT:` directive.

---

## Directory Structure

\```
scripts/
├── analysis/         # Data collection and analysis
├── algorithms/       # Detection and decision logic
├── generation/       # Content generation
├── validation/       # Input/output validation
├── execution/        # Main operations
└── utils/           # Helper functions
\```

---

## Scripts

### analysis/collect_changes.sh

**Purpose:** Auto-stage modified files and collect git change information

**Usage:**
\```bash
./scripts/analysis/collect_changes.sh
\```

**Input:** None (reads from git repository)

**Output:**
\```json
{
  "branch": "main",
  "stagedFiles": ["file1.js", "file2.md"],
  "modifiedFiles": ["file3.py"],
  "additions": 45,
  "deletions": 12
}
\```

**Exit Codes:**
- `0`: Success
- `1`: Not in a git repository
- `2`: No changes detected

---

### algorithms/detect_scope.js

**Purpose:** Detect optimal scope (module/file/directory) from changed files

**Usage:**
\```bash
echo '{"files": ["src/auth/login.js"]}' | node scripts/algorithms/detect_scope.js
\```

**Input:**
\```json
{
  "files": ["path/to/file1.js", "path/to/file2.md"]
}
\```

**Output:**
\```json
{
  "primary": {
    "name": "auth",
    "type": "module",
    "confidence": 0.9
  },
  "alternatives": [
    {"name": "login", "type": "file", "confidence": 0.7}
  ]
}
\```

**Exit Codes:**
- `0`: Success
- `1`: Invalid input or processing error

---

[... document all scripts ...]

---

## Script Execution Guidelines for Claude Agent

**CRITICAL:** These scripts are pre-built, tested, and deterministic. Agent MUST:

1. **ALWAYS use `EXECUTE_SCRIPT:` directive** before calling scripts
2. **NEVER inline or reconstruct** script commands from documentation
3. **ALWAYS pipe scripts** when composing multiple operations
4. **NEVER modify** script logic or parameters unless explicitly instructed
5. **ALWAYS check exit codes** before proceeding to next step

**Example execution pattern:**
\```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
# EXECUTE_SCRIPT: scripts/algorithms/detect_scope.js

./scripts/analysis/collect_changes.sh | \
  node scripts/algorithms/detect_scope.js > scope.json
\```

---

## Testing Scripts

**Validate syntax:**
\```bash
# Bash scripts
bash -n scripts/**/*.sh

# JavaScript scripts
node -c scripts/**/*.js

# Python scripts
python3 -m py_compile scripts/**/*.py
\```

**Set permissions:**
\```bash
chmod +x scripts/**/*.{sh,js,py}
\```
```

### Step 7: Validate and Test

**Script validation:**

```bash
# Check bash syntax
for script in scripts/**/*.sh; do
    bash -n "$script" || echo "Error in $script"
done

# Check JavaScript syntax
for script in scripts/**/*.js; do
    node -c "$script" || echo "Error in $script"
done

# Check Python syntax
for script in scripts/**/*.py; do
    python3 -m py_compile "$script" || echo "Error in $script"
done
```

**Set execute permissions:**

```bash
find scripts -type f \( -name "*.sh" -o -name "*.js" -o -name "*.py" \) -exec chmod +x {} \;
```

**Test execution:**

```bash
# Test bash script
echo '{"test": "data"}' | ./scripts/category/script_name.sh

# Test Node.js script
echo '{"test": "data"}' | node scripts/category/script_name.js

# Test Python script
echo '{"test": "data"}' | python3 scripts/category/script_name.py
```

**Verify directives work:**
- Start fresh conversation
- Use skill with scripts
- Verify Claude uses `EXECUTE_SCRIPT:` directive
- Confirm no inline code reconstruction

---

## Common Patterns

### Pattern 1: Simple Transformation

**Original inline code:**
```bash
git status --short | awk '{print $2}' | sort
```

**Extract to script:**
```bash
#!/usr/bin/env bash
# list_changed_files.sh
git status --short | awk '{print $2}' | sort
```

**Update documentation:**
```markdown
**EXECUTE_SCRIPT: scripts/utils/list_changed_files.sh**
```

### Pattern 2: Complex Algorithm

**Original inline JavaScript:**
````markdown
```javascript
function detectScope(files) {
  // 50+ lines of complex logic
  // Multiple helper functions
  // Edge case handling
  return scope;
}
```
````

**Extract to script:**
```javascript
#!/usr/bin/env node
// detect_scope.js
function detectScope(files) {
  // 50+ lines of logic
}

// stdin/stdout handling
if (require.main === module) {
  // ... implementation ...
}

module.exports = { detectScope };
```

**Update documentation:**
````markdown
**MANDATORY: Use scope detection script**

```bash
# EXECUTE_SCRIPT: scripts/algorithms/detect_scope.js
echo "$INPUT" | node scripts/algorithms/detect_scope.js
```
````

### Pattern 3: Multi-Step Pipeline

**Original inline code:**
````markdown
```bash
# Step 1: Collect
git status

# Step 2: Transform
# ... 20 lines ...

# Step 3: Format
# ... 15 lines ...

# Step 4: Output
# ... 10 lines ...
```
````

**Extract to scripts:**
```bash
scripts/
├── step1_collect.sh
├── step2_transform.sh
├── step3_format.sh
└── step4_output.sh
```

**Update documentation:**
````markdown
**MANDATORY: Use pipeline scripts**

```bash
# EXECUTE_SCRIPT: scripts/step1_collect.sh
# EXECUTE_SCRIPT: scripts/step2_transform.sh
# EXECUTE_SCRIPT: scripts/step3_format.sh
# EXECUTE_SCRIPT: scripts/step4_output.sh

./scripts/step1_collect.sh | \
  ./scripts/step2_transform.sh | \
  ./scripts/step3_format.sh | \
  ./scripts/step4_output.sh
```
````

---

## Best Practices

### Script Design

**Do:**
- ✅ Use standard I/O (stdin → stdout)
- ✅ Output JSON for structured data
- ✅ Handle errors explicitly
- ✅ Provide clear error messages
- ✅ Document input/output formats
- ✅ Use exit codes correctly (0=success, non-zero=failure)
- ✅ Make scripts idempotent when possible

**Don't:**
- ❌ Use command-line arguments (use stdin)
- ❌ Write to arbitrary files (use stdout)
- ❌ Depend on global state
- ❌ Hardcode paths or values
- ❌ Silently fail or ignore errors
- ❌ Mix output with logging (use stderr for logs)

### Documentation

**Do:**
- ✅ Use strong directive keywords
- ✅ Add `EXECUTE_SCRIPT:` markers
- ✅ Document I/O formats clearly
- ✅ Include usage examples
- ✅ List exit codes
- ✅ Explain when to use each script

**Don't:**
- ❌ Leave inline code examples
- ❌ Use weak language ("you can", "consider")
- ❌ Forget to prohibit inlining
- ❌ Skip scripts/README.md
- ❌ Leave broken references
- ❌ Mix scripts with documentation

### Organization

**Do:**
- ✅ Group by responsibility (analysis, algorithms, etc.)
- ✅ Use clear, descriptive names (verb_noun)
- ✅ Keep scripts focused (single responsibility)
- ✅ Document directory structure
- ✅ Version control everything

**Don't:**
- ❌ Organize chronologically (step1, step2)
- ❌ Use vague names (helper, utils without context)
- ❌ Create monolithic scripts
- ❌ Mix different responsibilities
- ❌ Forget README documentation

---

## Troubleshooting

### Problem: Claude still inlines code

**Diagnostic:**
- Check if directives are strong enough
- Verify `EXECUTE_SCRIPT:` markers present
- Look for remaining inline code examples

**Solutions:**
1. Add `MANDATORY:` to all script references
2. Add explicit prohibitions: `DO NOT inline`, `NEVER reconstruct`
3. Remove ALL inline code examples (even for illustration)
4. Strengthen language: "you should" → "ALWAYS"

### Problem: Scripts fail with permission errors

**Solution:**
```bash
chmod +x scripts/**/*.{sh,js,py}
```

### Problem: Script can't find dependencies

**Solutions:**
1. Use absolute paths or resolve relative to script location:
```bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source "$SCRIPT_DIR/../utils/helpers.sh"
```

2. Document dependencies in scripts/README.md
3. Check for required tools (jq, node, python3)

### Problem: JSON parsing fails

**Solutions:**
1. Validate JSON before sending to script:
```bash
echo "$DATA" | jq . > /dev/null && echo "$DATA" | ./script.sh
```

2. Add input validation in script:
```javascript
try {
  const input = JSON.parse(inputData);
} catch (e) {
  console.error('Invalid JSON input');
  process.exit(1);
}
```

### Problem: Scripts consume too much context

**Diagnosis:**
- Scripts shouldn't be loaded into context at all
- Only script OUTPUT should appear in context

**Solutions:**
1. Verify scripts are actually executing (not being read)
2. Check that documentation references scripts (not includes them)
3. Ensure EXECUTE_SCRIPT markers are clear

---

## Metrics and Impact

### Typical Results

**Token Savings:**
- Before: 1,500-3,000 tokens (inline code in context)
- After: 0 tokens (scripts execute externally)
- **Savings: 100%** of script code tokens

**Maintenance:**
- Before: Update 3-5 documentation files
- After: Update 1 script file
- **Effort: 60-80% reduction**

**Consistency:**
- Before: 50-70% consistent execution
- After: 100% consistent execution
- **Improvement: +30-50%**

### Example: Commit Skill

**Before extraction:**
- Inline code blocks: 15+
- Token consumption: ~2,000 tokens
- Files to update for algorithm change: 5

**After extraction:**
- Inline code blocks: 0
- Token consumption: 0 tokens (scripts only)
- Files to update for algorithm change: 1

**Results:**
- 100% token savings for script code
- 80% reduction in maintenance effort
- 100% execution consistency

---

## Checklist

### Planning
- [ ] Identify all inline code blocks
- [ ] Categorize by responsibility
- [ ] Design directory structure
- [ ] Choose appropriate languages

### Implementation
- [ ] Create script files with proper templates
- [ ] Add shebang lines and documentation
- [ ] Implement error handling
- [ ] Use stdin/stdout pattern
- [ ] Output structured JSON

### Documentation
- [ ] Update skill docs with EXECUTE_SCRIPT directives
- [ ] Add strong prohibition keywords
- [ ] Create comprehensive scripts/README.md
- [ ] Remove all inline code examples
- [ ] Document I/O formats and exit codes

### Validation
- [ ] Validate script syntax
- [ ] Set execute permissions
- [ ] Test each script independently
- [ ] Test script pipelines
- [ ] Verify Claude uses EXECUTE_SCRIPT directive

### Verification
- [ ] Measure token savings
- [ ] Test in fresh conversation
- [ ] Confirm deterministic behavior
- [ ] Check error handling
- [ ] Validate output formats

---

## Related Documents

- [Creating Skills](creating-skills.md) - Building skills from scratch
- [Modifying Skills](modifying-skills.md) - Refactoring existing skills

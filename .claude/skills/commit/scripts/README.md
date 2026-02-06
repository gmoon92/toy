# Commit Skill Scripts

Deterministic scripts that consume no context.

## Overview

Leverages **Level 3: Resources and Code** of Agent Skills architecture:
- ✅ **No context consumption** - Executed via bash, only output included in context
- ✅ **Deterministic** - Always produces same output for same input
- ✅ **Fast execution** - Direct execution without LLM inference

---

## validate_message.py

Validates commit message format and detects forbidden patterns (Co-Authored-By).

### Basic Usage

```bash
# Validate from file
python3 scripts/validate_message.py --message /tmp/commit-msg.txt --json

# Validate from stdin
echo "feat(auth): add JWT" | python3 scripts/validate_message.py --stdin --json
```

### Exit codes

- `0`: Valid message
- `1`: Invalid message (errors found)
- `2`: File not found or read error

### Detailed Usage

Check with `--help` option when running the script:
```bash
python3 scripts/validate_message.py --help
```

---

## References

- [Agent Skills Documentation](https://platform.claude.com/docs/ko/agents-and-tools/agent-skills/overview)
- [Commit Skill Design Document](../SKILL.md)
- [Execution Guide](../references/process/step5-execute.md)


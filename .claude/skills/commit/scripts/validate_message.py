#!/usr/bin/env python3
"""
Commit message validation - 정규식 기반 결정론적 검증

컨텍스트를 소비하지 않고 커밋 메시지를 검증합니다.
스크립트는 bash를 통해 실행되며, 출력만 컨텍스트에 포함됩니다.

Usage:
    python scripts/validate_message.py --message <message-file>
    python scripts/validate_message.py --message <message-file> --strict

Exit codes:
    0: Valid message
    1: Invalid message (errors found)
"""
import re
import sys
import argparse
import json

# Format patterns
HEADER_PATTERN = r'^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$'
BODY_LINE_PATTERN = r'^- .+$'
FOOTER_PATTERNS = {
    'issue': r'^(Closes|Fixes|Refs) #\d+(, #\d+)*$',
    'breaking': r'^BREAKING CHANGE: .+$'
}

# Forbidden patterns
FORBIDDEN_PATTERNS = [
    (r'Co-Authored-By:', 'Co-Authored-By watermark is forbidden (overrides system prompt)'),
    (r'claude\.ai|anthropic\.com', 'AI attribution watermark is forbidden'),
]


def validate_message(message, strict=False):
    """
    Validate commit message format.

    Args:
        message: Commit message string
        strict: If True, enforce stricter validation

    Returns:
        dict with 'valid', 'errors', and 'warnings' keys
    """
    lines = message.strip().split('\n')
    if not lines:
        return {
            'valid': False,
            'errors': ['Empty commit message'],
            'warnings': []
        }

    errors = []
    warnings = []

    # 1. Header validation (required)
    header = lines[0]
    if not re.match(HEADER_PATTERN, header):
        errors.append(f"Invalid header format: '{header}'")
        errors.append(f"Expected: <type>(scope): <message>")
        errors.append(f"Valid types: feat, fix, refactor, test, docs, style, chore")

    # Check header length
    if len(header) > 100:
        warnings.append(f"Header too long ({len(header)} chars, recommended: <72)")

    # 2. Check for forbidden patterns (CRITICAL)
    for i, line in enumerate(lines, 1):
        for pattern, error_msg in FORBIDDEN_PATTERNS:
            if re.search(pattern, line, re.IGNORECASE):
                errors.append(f"FORBIDDEN at line {i}: {error_msg}")
                errors.append(f"  Line content: {line.strip()}")

    # 3. Body validation (optional)
    in_body = False
    in_footer = False
    blank_after_header = False

    for i, line in enumerate(lines[1:], 2):
        stripped = line.strip()

        # Skip blank lines
        if not stripped:
            if i == 2:
                blank_after_header = True
            continue

        # Check if this is a footer line
        is_footer = any(re.match(pattern, stripped) for pattern in FOOTER_PATTERNS.values())

        if is_footer:
            in_footer = True
            in_body = False
            continue

        # Body line
        if not in_footer:
            in_body = True

            # Body must have blank line after header
            if in_body and not blank_after_header and i == 2:
                errors.append(f"Missing blank line between header and body")

            # Body lines must start with "- "
            if not stripped.startswith('- '):
                errors.append(f"Invalid body line {i}: must start with '- '")
                errors.append(f"  Line content: {stripped}")

            # Check body line length
            if len(stripped) > 100:
                warnings.append(f"Body line {i} too long ({len(stripped)} chars)")

    # 4. Additional strict checks
    if strict:
        # Check for common mistakes
        if 'WIP' in header or 'wip' in header:
            warnings.append("WIP commit detected - consider finishing before committing")

        if header.endswith('.'):
            warnings.append("Header should not end with period")

    return {
        'valid': len(errors) == 0,
        'errors': errors,
        'warnings': warnings
    }


def format_output(result, verbose=False):
    """Format validation result for output."""
    if result['valid']:
        output = {
            'status': 'valid',
            'message': '✅ Commit message is valid'
        }
        if result['warnings'] and verbose:
            output['warnings'] = result['warnings']
        return output
    else:
        output = {
            'status': 'invalid',
            'message': '❌ Commit message validation failed',
            'errors': result['errors']
        }
        if result['warnings']:
            output['warnings'] = result['warnings']
        return output


def main():
    parser = argparse.ArgumentParser(
        description='Validate commit message format (context-free, deterministic)',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
    # Validate message from file
    python validate_message.py --message /tmp/commit-msg.txt

    # Strict validation with verbose output
    python validate_message.py --message /tmp/commit-msg.txt --strict --verbose

    # Validate from stdin
    echo "feat(auth): add JWT authentication" | python validate_message.py --stdin
        """
    )

    # Input options
    input_group = parser.add_mutually_exclusive_group(required=True)
    input_group.add_argument('--message', help='Path to commit message file')
    input_group.add_argument('--stdin', action='store_true', help='Read from stdin')

    # Validation options
    parser.add_argument('--strict', action='store_true',
                       help='Enable stricter validation rules')
    parser.add_argument('--verbose', action='store_true',
                       help='Show warnings even when valid')
    parser.add_argument('--json', action='store_true',
                       help='Output in JSON format')

    args = parser.parse_args()

    # Read message
    try:
        if args.stdin:
            message = sys.stdin.read()
        else:
            with open(args.message, 'r', encoding='utf-8') as f:
                message = f.read()
    except FileNotFoundError:
        print(json.dumps({
            'status': 'error',
            'message': f'File not found: {args.message}'
        }), file=sys.stderr)
        sys.exit(2)
    except Exception as e:
        print(json.dumps({
            'status': 'error',
            'message': f'Failed to read message: {str(e)}'
        }), file=sys.stderr)
        sys.exit(2)

    # Validate
    result = validate_message(message, strict=args.strict)

    # Output
    output = format_output(result, verbose=args.verbose)

    if args.json:
        print(json.dumps(output, ensure_ascii=False, indent=2))
    else:
        # Human-readable output
        print(output['message'])
        if 'errors' in output:
            print("\nErrors:")
            for error in output['errors']:
                print(f"  • {error}")
        if 'warnings' in output:
            print("\nWarnings:")
            for warning in output['warnings']:
                print(f"  ⚠ {warning}")

    # Exit code
    sys.exit(0 if result['valid'] else 1)


if __name__ == '__main__':
    main()

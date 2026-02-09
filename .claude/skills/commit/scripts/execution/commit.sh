#!/bin/bash
# commit.sh
# Purpose: Validate and execute git commit
# Usage: ./commit.sh <commit_message>
# Input: Commit message via stdin or file

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VALIDATION_SCRIPT="$SCRIPT_DIR/../validation/validate_message.py"

# Read commit message from stdin
if [ -t 0 ]; then
    echo "ERROR: Commit message must be provided via stdin" >&2
    echo "Usage: echo 'commit message' | ./commit.sh" >&2
    exit 1
fi

COMMIT_MSG=$(cat)

# Validate message using validation script
echo "$COMMIT_MSG" | python3 "$VALIDATION_SCRIPT" --stdin --json

# Check validation result
if [ $? -ne 0 ]; then
    echo "ERROR: Commit message validation failed" >&2
    exit 1
fi

# Execute git commit
git commit -m "$COMMIT_MSG"

# Verify commit was created
if [ $? -eq 0 ]; then
    COMMIT_HASH=$(git log -1 --format=%h)
    COMMIT_SUBJECT=$(git log -1 --format=%s)

    echo "---COMMIT_SUCCESS---"
    echo "HASH: $COMMIT_HASH"
    echo "SUBJECT: $COMMIT_SUBJECT"
    exit 0
else
    echo "ERROR: Git commit failed" >&2
    exit 1
fi

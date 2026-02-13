#!/bin/bash
# commit.sh
# Purpose: Validate and execute git commit (staged files only)
# Usage: ./commit.sh [path] <commit_message>
#   - Path argument is kept for compatibility but not used in git commit
#   - Only staged files are committed (no automatic staging of unstaged files)
# Input: Commit message via stdin or file

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VALIDATION_SCRIPT="$SCRIPT_DIR/../validation/validate_message.py"

# Parse optional path argument (for logging/compatibility only)
TARGET_PATH="${1:-}"

# Read commit message from stdin
if [ -t 0 ]; then
    echo "ERROR: Commit message must be provided via stdin" >&2
    echo "Usage: echo 'commit message' | ./commit.sh" >&2
    echo "       echo 'commit message' | ./commit.sh <path>" >&2
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

# Verify there are staged files to commit
STAGED_COUNT=$(git diff --cached --name-only 2>/dev/null | wc -l | tr -d ' ')
if [ "$STAGED_COUNT" -eq 0 ]; then
    echo "ERROR: No staged files to commit" >&2
    exit 1
fi

# Log path info if provided (for debugging)
if [ -n "$TARGET_PATH" ]; then
    echo "INFO: Committing staged files (target path: $TARGET_PATH)" >&2
fi

# Execute git commit - only staged files (no path argument)
# This ensures we only commit what was explicitly staged
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

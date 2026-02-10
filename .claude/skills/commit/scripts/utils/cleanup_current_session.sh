#!/bin/bash
# cleanup_current_session.sh
# Purpose: Clean up current session after successful commit
# Usage: ./cleanup_current_session.sh [skill-name]
# Args: skill-name (default: "default")

set -e

SKILL_NAME=${1:-"default"}

# Get current session ID
SESSION_ID=$(cat "$HOME/.claude/.session-id-${SKILL_NAME}" 2>/dev/null || echo "")

if [ -z "$SESSION_ID" ]; then
    # No session to clean
    exit 0
fi

SESSION_DIR="$HOME/.claude/sessions/$SKILL_NAME/$SESSION_ID"

# Remove session directory
if [ -d "$SESSION_DIR" ]; then
    rm -rf "$SESSION_DIR"
    echo "✅ Cleaned up session: $SESSION_ID"
fi

# Remove session ID file
rm -f "$HOME/.claude/.session-id-${SKILL_NAME}"

exit 0

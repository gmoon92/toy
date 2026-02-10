#!/bin/bash
# get_session_id.sh
# Purpose: Get or create session ID for current Claude Code session
# Usage: SESSION_ID=$(./get_session_id.sh [skill-name])
# Args: skill-name (default: "default")

set -e

SKILL_NAME=${1:-"default"}
SESSION_FILE="$HOME/.claude/.session-id-${SKILL_NAME}"

# Check if session ID already exists
if [ -f "$SESSION_FILE" ]; then
    cat "$SESSION_FILE"
    exit 0
fi

# Generate new session ID
if command -v uuidgen &> /dev/null; then
    # macOS / systems with uuidgen
    SESSION_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
elif command -v cat /proc/sys/kernel/random/uuid &> /dev/null 2>&1; then
    # Linux
    SESSION_ID=$(cat /proc/sys/kernel/random/uuid)
else
    # Fallback: timestamp + random
    SESSION_ID="session-$(date +%s)-$RANDOM"
fi

# Create sessions directory if needed
mkdir -p "$HOME/.claude/sessions/$SKILL_NAME"

# Save session ID
echo "$SESSION_ID" > "$SESSION_FILE"

echo "$SESSION_ID"

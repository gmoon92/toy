#!/bin/bash
# init_session.sh
# Purpose: Initialize session directory with lock and metadata
# Usage: ./init_session.sh [skill-name]
# Args: skill-name (default: "default")
# Output: Session directory path

set -e

SKILL_NAME=${1:-"default"}

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Get or create session ID
SESSION_ID=$("$SCRIPT_DIR/get_session_id.sh" "$SKILL_NAME")
SESSION_DIR="$HOME/.claude/sessions/$SKILL_NAME/$SESSION_ID"

# Create session directory
mkdir -p "$SESSION_DIR"

# Create lock file
LOCK_FILE="$SESSION_DIR/.lock"
if [ ! -f "$LOCK_FILE" ]; then
    touch "$LOCK_FILE"
fi

# Create metadata
METADATA_FILE="$SESSION_DIR/metadata.json"
if [ ! -f "$METADATA_FILE" ]; then
    TIMESTAMP=$(date -u +%Y-%m-%dT%H:%M:%SZ)
    BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
    CWD=$(pwd)

    cat > "$METADATA_FILE" <<EOF
{
  "sessionId": "$SESSION_ID",
  "skill": "$SKILL_NAME",
  "createdAt": "$TIMESTAMP",
  "branch": "$BRANCH",
  "workingDirectory": "$CWD",
  "lastAccessed": "$TIMESTAMP"
}
EOF
fi

# Output session directory
echo "$SESSION_DIR"

#!/bin/bash
# save_commit_result.sh
# Purpose: Save commit execution result to analysis metadata
# Usage: ./save_commit_result.sh <diff_hash> <commit_hash> <commit_message>

set -e

SKILL_NAME="commit"
DIFF_HASH="$1"
COMMIT_HASH="$2"
COMMIT_MESSAGE="$3"

if [ -z "$DIFF_HASH" ] || [ -z "$COMMIT_HASH" ]; then
    echo "ERROR: Missing required parameters" >&2
    echo "Usage: $0 <diff_hash> <commit_hash> <commit_message>" >&2
    exit 1
fi

# Define session directory
SESSION_DIR="${HOME}/.claude/sessions/${SKILL_NAME}/${DIFF_HASH}"
ANALYSIS_FILE="${SESSION_DIR}/analysis.json"

if [ ! -f "$ANALYSIS_FILE" ]; then
    echo "ERROR: Analysis file not found: $ANALYSIS_FILE" >&2
    exit 1
fi

# Generate timestamp
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Update commit section in JSON
if command -v jq &> /dev/null; then
    # Use jq for safe JSON manipulation
    TEMP_FILE=$(mktemp)
    jq --arg hash "$COMMIT_HASH" \
       --arg msg "$COMMIT_MESSAGE" \
       --arg ts "$TIMESTAMP" \
       '.commit.executed = true | .commit.hash = $hash | .commit.message = $msg | .commit.timestamp = $ts' \
       "$ANALYSIS_FILE" > "$TEMP_FILE"
    mv "$TEMP_FILE" "$ANALYSIS_FILE"
else
    # Fallback: Simple sed replacement
    sed -i.bak 's/"executed": false/"executed": true/' "$ANALYSIS_FILE"
    sed -i.bak "s/\"hash\": null/\"hash\": \"$COMMIT_HASH\"/" "$ANALYSIS_FILE"
    sed -i.bak "s/\"timestamp\": null/\"timestamp\": \"$TIMESTAMP\"/" "$ANALYSIS_FILE"
    rm -f "${ANALYSIS_FILE}.bak"
fi

echo "COMMIT_RESULT_SAVED:${ANALYSIS_FILE}"

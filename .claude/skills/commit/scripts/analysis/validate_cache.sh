#!/bin/bash
# validate_cache.sh
# Purpose: Validate if cached analysis is still valid for current git state
# Usage: ./validate_cache.sh
# Output: "CACHE_VALID:{path}" or "CACHE_INVALID"

set -e

SKILL_NAME="commit"

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Get session directory
SESSION_ID=$("$SCRIPT_DIR/../utils/get_session_id.sh" "$SKILL_NAME" 2>/dev/null || echo "")
if [ -z "$SESSION_ID" ]; then
    echo "CACHE_INVALID"
    exit 0
fi

SESSION_DIR="$HOME/.claude/sessions/$SKILL_NAME/$SESSION_ID"
CACHE_FILE="$SESSION_DIR/analysis.json"

# Check if session directory exists
if [ ! -d "$SESSION_DIR" ]; then
    echo "CACHE_INVALID"
    exit 0
fi

# Check if cache file exists
if [ ! -f "$CACHE_FILE" ]; then
    echo "CACHE_INVALID"
    exit 0
fi

# Get cache file timestamp (last modified time)
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    CACHE_TIME=$(stat -f %m "$CACHE_FILE" 2>/dev/null || echo "0")
else
    # Linux
    CACHE_TIME=$(stat -c %Y "$CACHE_FILE" 2>/dev/null || echo "0")
fi

# Get git index timestamp (last change to staging area)
GIT_INDEX=".git/index"
if [ -f "$GIT_INDEX" ]; then
    if [[ "$OSTYPE" == "darwin"* ]]; then
        GIT_TIME=$(stat -f %m "$GIT_INDEX" 2>/dev/null || echo "0")
    else
        GIT_TIME=$(stat -c %Y "$GIT_INDEX" 2>/dev/null || echo "0")
    fi
else
    echo "CACHE_INVALID"
    exit 0
fi

# Compare timestamps
# Cache is valid if it's newer than git index
if [ "$CACHE_TIME" -gt "$GIT_TIME" ]; then
    # Additional validation: check if JSON is parseable
    CACHE_RESULT="CACHE_VALID:$CACHE_FILE"
    if command -v jq &> /dev/null; then
        if ! jq empty "$CACHE_FILE" 2>/dev/null; then
            CACHE_RESULT="CACHE_INVALID"
        fi
    else
        # If jq not available, just check file size
        FILE_SIZE=$(wc -c < "$CACHE_FILE" | tr -d ' ')
        if [ "$FILE_SIZE" -le 100 ]; then
            CACHE_RESULT="CACHE_INVALID"
        fi
    fi

    # Log telemetry for valid cache
    if [[ "$CACHE_RESULT" == CACHE_VALID:* ]]; then
        TELEMETRY_SCRIPT="$SCRIPT_DIR/../telemetry/log_execution.sh"
        if [ -x "$TELEMETRY_SCRIPT" ]; then
            CACHE_SIZE=$(wc -c < "$CACHE_FILE" 2>/dev/null | tr -d ' ' || echo "0")
            METRICS=$(cat <<EOF
{
  "cacheStatus": "CACHE_VALID",
  "cachePath": "$CACHE_FILE",
  "cacheSize": $CACHE_SIZE
}
EOF
)
            "$TELEMETRY_SCRIPT" "cache_validation" "$METRICS" 2>/dev/null || true
        fi
        echo "$CACHE_RESULT"
        exit 0
    fi
fi

# Log telemetry before returning
CACHE_RESULT="CACHE_INVALID"

# Log telemetry
TELEMETRY_SCRIPT="$SCRIPT_DIR/../telemetry/log_execution.sh"
if [ -x "$TELEMETRY_SCRIPT" ]; then
    CACHE_SIZE=$(wc -c < "$CACHE_FILE" 2>/dev/null | tr -d ' ' || echo "0")
    METRICS=$(cat <<EOF
{
  "cacheStatus": "$CACHE_RESULT",
  "cachePath": "$CACHE_FILE",
  "cacheSize": $CACHE_SIZE
}
EOF
)
    "$TELEMETRY_SCRIPT" "cache_validation" "$METRICS" 2>/dev/null || true
fi

echo "$CACHE_RESULT"

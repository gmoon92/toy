#!/bin/bash
# cleanup_sessions.sh
# Purpose: Clean up old session directories and orphaned files
# Usage: ./cleanup_sessions.sh [skill-name] [max-age-hours]
# Args: skill-name (optional, cleans all if not specified)
#       max-age-hours (default: 1)

set -e

SKILL_NAME=${1:-""}
MAX_AGE_HOURS=${2:-1}
STALE_LOCK_HOURS=2  # Lock files older than 2h are considered stale
SESSIONS_ROOT="$HOME/.claude/sessions"
CLEANUP_MARKER="$HOME/.claude/.cleanup-timestamp"

# Check if cleanup needed (only once per hour)
if [ -f "$CLEANUP_MARKER" ]; then
    LAST_CLEANUP=$(cat "$CLEANUP_MARKER")
    CURRENT_TIME=$(date +%s)
    TIME_DIFF=$((CURRENT_TIME - LAST_CLEANUP))

    # Skip if cleaned up less than 1 hour ago
    if [ $TIME_DIFF -lt 3600 ]; then
        exit 0
    fi
fi

if [ -n "$SKILL_NAME" ]; then
    echo "🧹 Cleaning up [$SKILL_NAME] sessions (> ${MAX_AGE_HOURS}h)..."
else
    echo "🧹 Cleaning up all sessions (> ${MAX_AGE_HOURS}h)..."
fi

# Create sessions directory if it doesn't exist
mkdir -p "$SESSIONS_ROOT"

DELETED_COUNT=0
STALE_COUNT=0
CURRENT_TIME=$(date +%s)
MAX_AGE_SECONDS=$((MAX_AGE_HOURS * 3600))
STALE_LOCK_SECONDS=$((STALE_LOCK_HOURS * 3600))

# Determine which skills to clean
if [ -n "$SKILL_NAME" ]; then
    SKILLS_TO_CLEAN="$SKILL_NAME"
else
    # Clean all skills
    SKILLS_TO_CLEAN=$(ls "$SESSIONS_ROOT" 2>/dev/null || echo "")
fi

# Clean each skill
for skill in $SKILLS_TO_CLEAN; do
    SKILL_DIR="$SESSIONS_ROOT/$skill"

    if [ ! -d "$SKILL_DIR" ]; then
        continue
    fi

    # Find and delete old or stale session directories
    for session_dir in "$SKILL_DIR"/*; do
    if [ ! -d "$session_dir" ]; then
        continue
    fi

    METADATA_FILE="$session_dir/metadata.json"
    LOCK_FILE="$session_dir/.lock"

    # Check for stale lock (crashed sessions)
    if [ -f "$LOCK_FILE" ]; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            LOCK_TIME=$(stat -f %m "$LOCK_FILE" 2>/dev/null || echo "0")
        else
            LOCK_TIME=$(stat -c %Y "$LOCK_FILE" 2>/dev/null || echo "0")
        fi

        LOCK_AGE=$((CURRENT_TIME - LOCK_TIME))

        # If lock is stale (>2h), consider it a crashed session
        if [ "$LOCK_AGE" -gt "$STALE_LOCK_SECONDS" ]; then
            rm -rf "$session_dir"
            STALE_COUNT=$((STALE_COUNT + 1))
            continue
        fi
    fi

    # Check if metadata exists
    if [ ! -f "$METADATA_FILE" ]; then
        # No metadata, delete immediately
        rm -rf "$session_dir"
        DELETED_COUNT=$((DELETED_COUNT + 1))
        continue
    fi

    # Get creation timestamp from metadata
    if command -v jq &> /dev/null; then
        CREATED_AT=$(jq -r '.createdAt' "$METADATA_FILE" 2>/dev/null || echo "")
        if [ -n "$CREATED_AT" ]; then
            # Convert ISO timestamp to epoch
            if [[ "$OSTYPE" == "darwin"* ]]; then
                CREATED_EPOCH=$(date -j -f "%Y-%m-%dT%H:%M:%SZ" "$CREATED_AT" +%s 2>/dev/null || echo "0")
            else
                CREATED_EPOCH=$(date -d "$CREATED_AT" +%s 2>/dev/null || echo "0")
            fi

            AGE=$((CURRENT_TIME - CREATED_EPOCH))

            if [ "$AGE" -gt "$MAX_AGE_SECONDS" ]; then
                rm -rf "$session_dir"
                DELETED_COUNT=$((DELETED_COUNT + 1))
            fi
        fi
    else
        # No jq, use file modification time
        if [[ "$OSTYPE" == "darwin"* ]]; then
            FILE_TIME=$(stat -f %m "$METADATA_FILE" 2>/dev/null || echo "0")
        else
            FILE_TIME=$(stat -c %Y "$METADATA_FILE" 2>/dev/null || echo "0")
        fi

        AGE=$((CURRENT_TIME - FILE_TIME))

        if [ "$AGE" -gt "$MAX_AGE_SECONDS" ]; then
            rm -rf "$session_dir"
            DELETED_COUNT=$((DELETED_COUNT + 1))
        fi
    fi
    done
done

# Clean up orphaned session ID files
find "$HOME/.claude" -maxdepth 1 -name ".session-id-*" -type f -mtime +1 -delete 2>/dev/null || true

# Update cleanup timestamp
echo "$CURRENT_TIME" > "$CLEANUP_MARKER"

TOTAL_CLEANED=$((DELETED_COUNT + STALE_COUNT))

if [ $TOTAL_CLEANED -gt 0 ]; then
    echo "✅ Cleaned up $DELETED_COUNT old session(s) + $STALE_COUNT stale session(s)"
else
    echo "✅ No cleanup needed"
fi

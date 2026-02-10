#!/bin/bash
# decide_mode.sh
# Purpose: Decide between Direct Mode and Task Mode based on changeset size
# Usage: ./decide_mode.sh
# Output: "DIRECT_MODE" or "TASK_MODE:{file_count}"

set -e

SKILL_NAME="commit"

# Initialize session
SESSION_DIR=$(./scripts/utils/init_session.sh "$SKILL_NAME")

# Threshold for Task Mode (files)
THRESHOLD=20

# Auto-stage modified files first
git add -u 2>/dev/null || true

# Count staged files
FILE_COUNT=$(git diff --cached --name-only 2>/dev/null | wc -l | tr -d ' ')

# Check if no changes
if [ "$FILE_COUNT" -eq 0 ]; then
    echo "ERROR: 변경사항이 없습니다. 먼저 파일을 수정하세요." >&2
    exit 1
fi

# Save file count to session
echo "$FILE_COUNT" > "$SESSION_DIR/file_count.txt"

# Cleanup old sessions (async, in background) - 1 hour
./scripts/utils/cleanup_sessions.sh "$SKILL_NAME" 1 >/dev/null 2>&1 &

# Decide mode
if [ "$FILE_COUNT" -lt "$THRESHOLD" ]; then
    MODE="DIRECT_MODE"
else
    MODE="TASK_MODE:$FILE_COUNT"
fi

# Log telemetry
TELEMETRY_SCRIPT="./scripts/telemetry/log_execution.sh"
if [ -x "$TELEMETRY_SCRIPT" ]; then
    METRICS=$(./scripts/telemetry/collect_metrics.sh "mode_decision" "$MODE" 2>/dev/null || echo '{}')
    "$TELEMETRY_SCRIPT" "mode_decision" "$METRICS" 2>/dev/null || true
fi

echo "$MODE"

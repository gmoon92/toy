#!/bin/bash

# Collect commit execution metrics
# Usage: ./collect_metrics.sh <phase> <mode>

set -euo pipefail

PHASE="${1:-unknown}"
MODE="${2:-unknown}"

# Get git stats
FILE_COUNT=$(git diff --cached --name-only | wc -l | tr -d ' ')
ADDITIONS=$(git diff --cached --numstat | awk '{sum+=$1} END {print sum+0}')
DELETIONS=$(git diff --cached --numstat | awk '{sum+=$2} END {print sum+0}')

# Estimate context tokens (rough approximation)
# Assume: 1 line = ~10 tokens, file path = ~5 tokens
ESTIMATED_TOKENS=$((FILE_COUNT * 5 + (ADDITIONS + DELETIONS) / 10))

# Detect cache status (if applicable)
CACHE_STATUS="none"
if [ -f "${HOME}/.claude/temp/latest-analysis.txt" ]; then
    CACHE_FILE=$(cat "${HOME}/.claude/temp/latest-analysis.txt" 2>/dev/null || echo "")
    if [ -f "${CACHE_FILE}" ]; then
        CACHE_STATUS="hit"
    else
        CACHE_STATUS="miss"
    fi
fi

# Build metrics JSON
METRICS=$(cat <<EOF
{
  "phase": "${PHASE}",
  "mode": "${MODE}",
  "fileCount": ${FILE_COUNT},
  "additions": ${ADDITIONS},
  "deletions": ${DELETIONS},
  "estimatedTokens": ${ESTIMATED_TOKENS},
  "cacheStatus": "${CACHE_STATUS}"
}
EOF
)

echo "${METRICS}"

#!/bin/bash

# Generate telemetry report from collected data
# Usage: ./report.sh [days]

DAYS="${1:-7}"
TELEMETRY_LOG="${HOME}/.claude/telemetry/commit/executions.jsonl"

if [ ! -f "${TELEMETRY_LOG}" ]; then
    echo "No telemetry data found"
    echo "Run /commit a few times to collect data"
    exit 1
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 Commit Skill Telemetry Report (Last ${DAYS} days)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Count cache hits/misses (total executions)
HITS=$(grep "cache_hit" "${TELEMETRY_LOG}" 2>/dev/null | wc -l | tr -d ' ')
HITS=${HITS:-0}
MISSES=$(grep "cache_miss" "${TELEMETRY_LOG}" 2>/dev/null | wc -l | tr -d ' ')
MISSES=${MISSES:-0}
TOTAL=$((HITS + MISSES))

echo "## Execution Summary"
echo ""
echo "Total executions:     ${TOTAL}"
echo ""

echo "## Cache Performance"
echo ""
echo "Cache hits:           ${HITS}"
echo "Cache misses:         ${MISSES}"
if [ "${TOTAL}" -gt 0 ]; then
    HIT_RATE=$((HITS * 100 / TOTAL))
    echo "Cache hit rate:       ${HIT_RATE}%"
else
    echo "Cache hit rate:       N/A"
fi
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "💡 Recommendations"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Generate recommendations
if [ "${TOTAL}" -lt 10 ]; then
    echo "⚠️  Need more data (${TOTAL} executions)"
    echo "   Run /commit 10+ times for meaningful analysis"
elif [ "${TOTAL}" -gt 0 ]; then
    if [ "${HIT_RATE}" -lt 20 ]; then
        echo "⚠️  Low cache hit rate (${HIT_RATE}%)"
        echo "   Most commits have different changes (expected behavior)"
    elif [ "${HIT_RATE}" -ge 30 ]; then
        echo "✅ Cache provides value (${HIT_RATE}% hit rate)"
        echo "   Commit message retries benefit from caching"
    else
        echo "ℹ️  Moderate cache hit rate (${HIT_RATE}%)"
        echo "   Some commit message retries or repeated changes"
    fi
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📁 Raw data: ${TELEMETRY_LOG}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

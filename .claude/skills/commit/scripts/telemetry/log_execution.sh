#!/bin/bash

# Log commit skill execution metrics for analysis
# Usage: ./log_execution.sh <event_type> <data_json>

set -euo pipefail

# Telemetry log file
TELEMETRY_DIR="${HOME}/.claude/telemetry/commit"
TELEMETRY_LOG="${TELEMETRY_DIR}/executions.jsonl"

# Create telemetry directory if not exists
mkdir -p "${TELEMETRY_DIR}"

# Get event type and data
EVENT_TYPE="${1:-unknown}"
shift
DATA_JSON="${*:-{}}"

# Generate timestamp
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Create log entry
LOG_ENTRY=$(cat <<EOF
{
  "timestamp": "${TIMESTAMP}",
  "event": "${EVENT_TYPE}",
  "data": ${DATA_JSON}
}
EOF
)

# Append to log file (JSONL format - one JSON per line)
echo "${LOG_ENTRY}" >> "${TELEMETRY_LOG}"

# Output confirmation
echo "LOGGED:${EVENT_TYPE}"

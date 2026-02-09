#!/bin/bash
# create_metadata.sh
# Purpose: Create commit execution metadata file
# Usage: ./create_metadata.sh <analysis_json>
# Input: JSON from collect_changes.sh via stdin or file

set -e

# Generate execution ID
EXECUTION_ID=$(date +%Y%m%d-%H%M%S)
TIMESTAMP=$(date -u +%Y-%m-%dT%H:%M:%SZ)

# Create temp directory
mkdir -p .claude/temp

# Read analysis JSON from stdin if provided, otherwise use empty object
if [ -t 0 ]; then
    ANALYSIS_JSON="{}"
else
    ANALYSIS_JSON=$(cat)
fi

# Create metadata file
METADATA_FILE=".claude/temp/commit-execution-${EXECUTION_ID}.json"

cat > "$METADATA_FILE" <<EOF
{
  "executionId": "${EXECUTION_ID}",
  "timestamp": "${TIMESTAMP}",
  "analysis": ${ANALYSIS_JSON}
}
EOF

# Output the execution ID and file path
echo "$METADATA_FILE"

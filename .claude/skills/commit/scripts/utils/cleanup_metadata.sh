#!/bin/bash
# cleanup_metadata.sh
# Purpose: Clean up commit execution metadata files
# Usage: ./cleanup_metadata.sh [execution_id|all]

set -e

TEMP_DIR=".claude/temp"

if [ $# -eq 0 ]; then
    echo "ERROR: Missing argument" >&2
    echo "Usage: ./cleanup_metadata.sh <execution_id|all>" >&2
    exit 1
fi

if [ "$1" = "all" ]; then
    # Remove all commit execution metadata files
    rm -f "$TEMP_DIR"/commit-execution-*.json
    echo "Cleaned up all commit metadata files"
else
    # Remove specific execution metadata file
    EXECUTION_ID="$1"
    METADATA_FILE="$TEMP_DIR/commit-execution-${EXECUTION_ID}.json"

    if [ -f "$METADATA_FILE" ]; then
        rm -f "$METADATA_FILE"
        echo "Cleaned up metadata: $METADATA_FILE"
    else
        echo "WARNING: Metadata file not found: $METADATA_FILE" >&2
    fi
fi

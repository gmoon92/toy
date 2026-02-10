#!/bin/bash
# analyze_with_cache.sh
# Purpose: Unified analysis with content-hash based caching
# Usage: ./analyze_with_cache.sh
# Output: "ANALYSIS_FILE:{path}" or "ERROR:{message}"

set -e

SKILL_NAME="commit"

# 1. Auto-stage modified files
git add -u 2>/dev/null || true

# 2. Check if there are changes
if ! git diff --cached --quiet 2>/dev/null; then
    :  # Has changes, continue
else
    echo "ERROR:변경사항이 없습니다" >&2
    exit 1
fi

# 3. Generate content hash from git diff (unique identifier)
DIFF_HASH=$(git diff --cached | shasum -a 256 | cut -d' ' -f1 | cut -c1-16)

# 4. Define session directory based on content hash
SESSION_DIR="${HOME}/.claude/sessions/${SKILL_NAME}/${DIFF_HASH}"
ANALYSIS_FILE="${SESSION_DIR}/analysis.json"

# 5. Check if cached analysis exists and is valid
if [ -f "$ANALYSIS_FILE" ]; then
    # Validate JSON integrity
    if command -v jq &> /dev/null; then
        if jq empty "$ANALYSIS_FILE" 2>/dev/null; then
            # Cache hit - reuse existing analysis
            FILE_SIZE=$(wc -c < "$ANALYSIS_FILE" | tr -d ' ')

            # Log telemetry
            TELEMETRY_SCRIPT="./scripts/telemetry/log_execution.sh"
            if [ -x "$TELEMETRY_SCRIPT" ]; then
                METRICS=$(cat <<EOF
{
  "cacheStatus": "HIT",
  "diffHash": "$DIFF_HASH",
  "cacheSize": $FILE_SIZE
}
EOF
)
                "$TELEMETRY_SCRIPT" "cache_hit" "$METRICS" 2>/dev/null || true
            fi

            echo "ANALYSIS_FILE:${ANALYSIS_FILE}"
            exit 0
        fi
    fi
fi

# 6. Cache miss - perform new analysis
mkdir -p "$SESSION_DIR"

# Collect basic info
BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
EXECUTION_ID=$(date +"%Y%m%d-%H%M%S")

# Collect file-level changes with logical analysis
FILES_JSON=$(git diff --cached --numstat 2>/dev/null | awk -v q='"' '
function detect_type(path, additions, deletions) {
    # Documentation changes
    if (path ~ /\.(md|txt|rst|adoc)$/ || path ~ /\/docs?\//) return "docs"
    if (path ~ /README|CHANGELOG|LICENSE/) return "docs"

    # Test files
    if (path ~ /test|spec|__tests__/) return "test"
    if (path ~ /\.(test|spec)\.(js|ts|py|java)$/) return "test"

    # Configuration files
    if (path ~ /\.(yml|yaml|json|xml|properties|conf|config)$/) return "chore"
    if (path ~ /package\.json|pom\.xml|build\.gradle/) return "chore"

    # Style/formatting only (small changes, likely formatting)
    if (additions + deletions < 10 && additions > 0 && deletions > 0) return "style"

    # Large refactoring (high churn, similar add/delete)
    ratio = (additions > 0 && deletions > 0) ? (additions < deletions ? additions/deletions : deletions/additions) : 0
    if (ratio > 0.7 && additions + deletions > 100) return "refactor"

    # New feature (more additions than deletions)
    if (additions > deletions * 2) return "feat"

    # Bug fix (moderate changes)
    if (deletions > 0 && additions > 0) return "fix"

    # Default
    return "chore"
}

function detect_category(path, type) {
    if (type == "docs") return "documentation"
    if (type == "test") return "test"
    if (type == "style") return "structural"
    if (type == "refactor") return "structural"
    if (type == "feat" || type == "fix") return "behavioral"
    return "maintenance"
}

function analyze_purpose(path, type, additions, deletions) {
    # Documentation
    if (type == "docs") {
        if (path ~ /README/) return "Update README documentation"
        if (path ~ /CHANGELOG/) return "Update changelog"
        return "Update documentation"
    }

    # Tests
    if (type == "test") {
        if (additions > deletions) return "Add test coverage"
        return "Update test cases"
    }

    # Configuration
    if (path ~ /\.(yml|yaml|json|xml)$/) {
        return "Update configuration"
    }

    # Code changes
    if (type == "feat") return "Add new functionality"
    if (type == "fix") return "Fix bug or issue"
    if (type == "refactor") return "Refactor code structure"
    if (type == "style") return "Apply code formatting"

    return "Update implementation"
}

BEGIN {
    print "["
    first = 1
}
{
    if (!first) print ","
    first = 0

    # Parse git diff output
    additions = ($1 == "-") ? 0 : $1
    deletions = ($2 == "-") ? 0 : $2
    path = $3
    status = "M"

    # Extract scope
    scope = path
    if (match(path, /^[^\/]+/)) {
        scope = substr(path, RSTART, RLENGTH)
    }

    # Logical analysis
    type = detect_type(path, additions, deletions)
    category = detect_category(path, type)
    purpose = analyze_purpose(path, type, additions, deletions)

    printf "    {\"path\":%s%s%s,\"status\":%s%s%s,\"additions\":%d,\"deletions\":%d,\"scope\":%s%s%s,\"type\":%s%s%s,\"category\":%s%s%s,\"purpose\":%s%s%s}",
           q, path, q, q, status, q, additions, deletions, q, scope, q, q, type, q, q, category, q, q, purpose, q
}
END {
    print ""
    print "  ]"
}')

# Calculate totals
TOTAL_FILES=$(git diff --cached --name-only 2>/dev/null | wc -l | tr -d ' ')
TOTAL_ADDITIONS=$(git diff --cached --numstat 2>/dev/null | awk '{sum+=$1} END {print sum+0}')
TOTAL_DELETIONS=$(git diff --cached --numstat 2>/dev/null | awk '{sum+=$2} END {print sum+0}')

# Detect primary scope (most common directory/module)
PRIMARY_SCOPE=$(git diff --cached --name-only 2>/dev/null | awk -F'/' '{print $1}' | sort | uniq -c | sort -rn | head -1 | awk '{print $2}')
PRIMARY_SCOPE=${PRIMARY_SCOPE:-"unknown"}

# Generate logical grouping recommendations
GROUPING_JSON=$(echo "$FILES_JSON" | jq -c '
  group_by(.type) | map({
    type: .[0].type,
    category: .[0].category,
    fileCount: length,
    totalAdditions: (map(.additions) | add),
    totalDeletions: (map(.deletions) | add),
    recommendation: (
      if .[0].category == "documentation" then "Single commit recommended - documentation changes"
      elif .[0].category == "test" then "Can be separated if unrelated to code changes"
      elif .[0].category == "structural" then "Separate commit required (Tidy First principle)"
      elif .[0].category == "behavioral" then "Single commit if related functionality"
      else "Review for logical independence"
      end
    ),
    files: map(.path)
  })
')

# Detect Tidy First violation
STRUCTURAL_COUNT=$(echo "$FILES_JSON" | jq '[.[] | select(.category == "structural")] | length')
BEHAVIORAL_COUNT=$(echo "$FILES_JSON" | jq '[.[] | select(.category == "behavioral")] | length')

if [ "$STRUCTURAL_COUNT" -gt 0 ] && [ "$BEHAVIORAL_COUNT" -gt 0 ]; then
    TIDY_FIRST_VIOLATION="true"
    TIDY_FIRST_MSG="Structural and behavioral changes mixed - violates Tidy First principle"
else
    TIDY_FIRST_VIOLATION="false"
    TIDY_FIRST_MSG="No Tidy First violation detected"
fi

# Create analysis JSON
cat > "$ANALYSIS_FILE" <<EOF
{
  "executionId": "$EXECUTION_ID",
  "timestamp": "$TIMESTAMP",
  "diffHash": "$DIFF_HASH",
  "branch": "$BRANCH",
  "scope": {
    "primary": "$PRIMARY_SCOPE",
    "confidence": "auto"
  },
  "summary": {
    "totalFiles": $TOTAL_FILES,
    "totalAdditions": $TOTAL_ADDITIONS,
    "totalDeletions": $TOTAL_DELETIONS
  },
  "analysis": {
    "logicalGroups": $GROUPING_JSON,
    "tidyFirstViolation": $TIDY_FIRST_VIOLATION,
    "tidyFirstMessage": "$TIDY_FIRST_MSG",
    "recommendSplit": $([ "$TIDY_FIRST_VIOLATION" = "true" ] && echo "true" || echo "false")
  },
  "files": $FILES_JSON,
  "diff": {
    "hash": "$DIFF_HASH",
    "content": null
  },
  "commit": {
    "executed": false,
    "hash": null,
    "message": null,
    "timestamp": null
  },
  "metadata": {
    "cacheKey": "$DIFF_HASH",
    "sessionDir": "$SESSION_DIR",
    "version": "2.1"
  }
}
EOF

# Log telemetry
TELEMETRY_SCRIPT="./scripts/telemetry/log_execution.sh"
if [ -x "$TELEMETRY_SCRIPT" ]; then
    METRICS=$(cat <<EOF
{
  "cacheStatus": "MISS",
  "diffHash": "$DIFF_HASH",
  "fileCount": $FILE_COUNT,
  "additions": $ADDITIONS,
  "deletions": $DELETIONS
}
EOF
)
    "$TELEMETRY_SCRIPT" "cache_miss" "$METRICS" 2>/dev/null || true
fi

# Cleanup old sessions (async, in background) - older than 24 hours
./scripts/utils/cleanup_sessions.sh "$SKILL_NAME" 24 >/dev/null 2>&1 &

echo "ANALYSIS_FILE:${ANALYSIS_FILE}"

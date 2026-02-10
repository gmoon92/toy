#!/bin/bash
# collect_git_diff.sh
# Purpose: Collect ONLY git diff data (NO caching, NO inference)
# Usage: ./collect_git_diff.sh
# Output: Pure git data as JSON to stdout

set -e

# 1. Auto-stage modified files
git add -u 2>/dev/null || true

# 2. Check if there are changes
if ! git diff --cached --quiet 2>/dev/null; then
    :  # Has changes, continue
else
    echo '{"error":"변경사항이 없습니다"}' >&2
    exit 1
fi

# 3. Collect git metadata (결정론적 데이터만)
BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# 4. Collect file changes (NO type/category inference)
FILES_JSON=$(git diff --cached --numstat 2>/dev/null | awk -v q='"' '
BEGIN {
    print "["
    first = 1
}
{
    if (!first) print ","
    first = 0

    additions = ($1 == "-") ? 0 : $1
    deletions = ($2 == "-") ? 0 : $2
    path = $3

    printf "    {\"path\":%s%s%s,\"status\":\"M\",\"additions\":%d,\"deletions\":%d}",
           q, path, q, additions, deletions
}
END {
    print ""
    print "  ]"
}')

# 5. Calculate totals
TOTAL_FILES=$(git diff --cached --name-only 2>/dev/null | wc -l | tr -d ' ')
TOTAL_ADDITIONS=$(git diff --cached --numstat 2>/dev/null | awk '{sum+=$1} END {print sum+0}')
TOTAL_DELETIONS=$(git diff --cached --numstat 2>/dev/null | awk '{sum+=$2} END {print sum+0}')

# 6. Output pure git data (NO inference, NO caching)
cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "branch": "$BRANCH",
  "summary": {
    "totalFiles": $TOTAL_FILES,
    "totalAdditions": $TOTAL_ADDITIONS,
    "totalDeletions": $TOTAL_DELETIONS
  },
  "files": $FILES_JSON
}
EOF

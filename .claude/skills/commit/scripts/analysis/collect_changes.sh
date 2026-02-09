#!/bin/bash
# collect_changes.sh
# Purpose: Auto-stage modified files and collect change information
# Usage: ./collect_changes.sh

set -e

# Auto-stage all modified files (IDE behavior)
git add -u

# Check if any changes exist after staging
if [ -z "$(git diff --cached --name-only)" ]; then
    echo "ERROR: 변경사항이 없습니다. 먼저 파일을 수정하세요." >&2
    exit 1
fi

# Check current branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" = "master" ] || [ "$CURRENT_BRANCH" = "main" ]; then
    echo "WARNING: main/master 브랜치에서 작업 중입니다." >&2
fi

# Output JSON format
echo "{"
echo "  \"branch\": \"$CURRENT_BRANCH\","
echo "  \"stagedFiles\": ["

# Get staged files
FIRST=true
while IFS= read -r file; do
    if [ "$FIRST" = true ]; then
        FIRST=false
    else
        echo ","
    fi
    echo -n "    \"$file\""
done < <(git diff --cached --name-only)

echo ""
echo "  ],"

# Get change statistics
STATS=$(git diff --cached --stat | tail -1)
INSERTIONS=$(echo "$STATS" | grep -oE '[0-9]+ insertion' | grep -oE '[0-9]+' || echo "0")
DELETIONS=$(echo "$STATS" | grep -oE '[0-9]+ deletion' | grep -oE '[0-9]+' || echo "0")
FILE_COUNT=$(git diff --cached --name-only | wc -l | tr -d ' ')

echo "  \"stats\": {"
echo "    \"fileCount\": $FILE_COUNT,"
echo "    \"insertions\": $INSERTIONS,"
echo "    \"deletions\": $DELETIONS"
echo "  }"
echo "}"

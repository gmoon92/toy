#!/bin/bash
# collect_git_diff.sh
# Purpose: Collect ONLY git diff data with memory-based path management
# Usage: ./collect_git_diff.sh [path]
#   - Without args: stages all modified files
#   - With path: stages only modified files in specified path using memory matching
#   - Path can be: full path, partial path, or directory name
# Output: Pure git data as JSON to stdout

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UTILS_DIR="$SCRIPT_DIR/../utils"
RESOLVE_SCRIPT="$UTILS_DIR/resolve_path.sh"

# Memory file path
MEMORY_DIR="${HOME}/.claude/projects/$(basename "$(git rev-parse --show-toplevel 2>/dev/null || echo 'default')")/memory"
MEMORY_FILE="${MEMORY_DIR}/commit-skill-paths.md"

# Parse optional path argument
RAW_PATH="${1:-}"
TARGET_PATH=""

# Function to scan and save git status to memory
scan_and_save_to_memory() {
    local memory_file="$1"

    # Create memory directory if needed
    if [ ! -d "$MEMORY_DIR" ]; then
        mkdir -p "$MEMORY_DIR"
    fi

    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")

    # Collect all changed files
    local modified_files=$(git status --porcelain 2>/dev/null | grep -E '^[ MADRC]M|^[ MADRC] ' | awk '{print $2}' || true)
    local staged_files=$(git diff --cached --name-only 2>/dev/null || true)
    local untracked_files=$(git status --porcelain 2>/dev/null | grep -E '^\?\?' | awk '{print $2}' || true)

    # Generate memory content
    {
        echo "# Commit Skill - 변경 파일 목록"
        echo ""
        echo "## 현재 변경사항 (${timestamp})"
        echo ""
        echo "### Modified Files (Unstaged)"
        echo "$modified_files" | while read -r file; do [ -n "$file" ] && echo "- ${file}"; done
        echo ""
        echo "### Staged Files"
        echo "$staged_files" | while read -r file; do [ -n "$file" ] && echo "- ${file}"; done
        echo ""
        echo "### Untracked Files"
        echo "$untracked_files" | while read -r file; do [ -n "$file" ] && echo "- ${file}"; done
    } > "$memory_file"
}

# Function to classify files by git status
classify_files() {
    local files="$1"
    local tracked_files=""
    local untracked_files=""
    local ignored_files=""

    for file in $files; do
        if [ -z "$file" ]; then
            continue
        fi

        if git ls-files --error-unmatch "$file" >/dev/null 2>&1; then
            # Tracked file
            tracked_files="$tracked_files$file "
        elif git check-ignore -q "$file" 2>/dev/null; then
            # Ignored file (exclude from staging)
            ignored_files="$ignored_files$file "
        else
            # Untracked file
            untracked_files="$untracked_files$file "
        fi
    done

    echo "TRACKED:${tracked_files% }"
    echo "UNTRACKED:${untracked_files% }"
    echo "IGNORED:${ignored_files% }"
}

# Function to stage files safely (tracked only)
stage_files_safely() {
    local files="$1"
    local tracked_only="${2:-true}"

    local classification=$(classify_files "$files")
    local tracked=$(echo "$classification" | grep "^TRACKED:" | sed 's/^TRACKED://')
    local untracked=$(echo "$classification" | grep "^UNTRACKED:" | sed 's/^UNTRACKED://')
    local ignored=$(echo "$classification" | grep "^IGNORED:" | sed 's/^IGNORED://')

    local staged_count=0

    # Stage tracked files
    if [ -n "$tracked" ]; then
        for file in $tracked; do
            if [ -n "$file" ] && [ -f "$file" ] || [ -d "$file" ]; then
                git add "$file" 2>/dev/null && ((staged_count++)) || true
            fi
        done
    fi

    # Stage untracked files only if explicitly allowed
    if [ "$tracked_only" = "false" ] && [ -n "$untracked" ]; then
        for file in $untracked; do
            if [ -n "$file" ] && [ -f "$file" ] || [ -d "$file" ]; then
                if ! git check-ignore -q "$file" 2>/dev/null; then
                    git add "$file" 2>/dev/null && ((staged_count++)) || true
                fi
            fi
        done
    fi

    # Output warnings to stderr
    if [ -n "$untracked" ]; then
        echo "WARN:UNTRACKED_FILES:${untracked}" >&2
    fi

    if [ -n "$ignored" ]; then
        echo "INFO:IGNORED_FILES:${ignored}" >&2
    fi

    echo "$staged_count"
}

# Function to resolve path using memory
resolve_path_with_memory() {
    local input="$1"
    local memory_file="$2"

    # If empty, return empty
    if [ -z "$input" ]; then
        echo ""
        return 0
    fi

    # Check if memory file exists
    if [ ! -f "$memory_file" ]; then
        # Fall back to traditional resolution
        resolve_path_traditional "$input"
        return $?
    fi

    # 1단계: 정확한 파일 일치 확인 (메모리 기반)
    local exact_match=$(grep "^- ${input}$" "$memory_file" 2>/dev/null | head -1 | sed 's/^- //')
    if [ -n "$exact_match" ]; then
        echo "FILE:$exact_match"
        return 0
    fi

    # 2단계: 디렉토리 정확 일치
    if [ -d "$input" ]; then
        echo "DIR:$input"
        return 0
    fi

    # 3단계: 메모리 기반 부분 매칭
    local matches=$(grep "^- " "$memory_file" 2>/dev/null | sed 's/^- //' | grep "/${input}/\|^${input}/\|/${input}$\|^${input}$" | sort -u || true)
    local count=$(echo "$matches" | grep -c . || echo "0")

    if [ "$count" = "0" ]; then
        # 4단계: 전체 경로에서 부분 문자열 매칭
        matches=$(grep "^- " "$memory_file" 2>/dev/null | sed 's/^- //' | grep "$input" | sort -u || true)
        count=$(echo "$matches" | grep -c . || echo "0")
    fi

    if [ "$count" = "0" ]; then
        # Fall back to traditional resolution
        resolve_path_traditional "$input"
        return $?
    elif [ "$count" = "1" ]; then
        echo "PARTIAL:$matches"
        return 0
    else
        # Multiple matches - return first but warn
        echo "MULTIPLE:$(echo "$matches" | head -1)" >&2
        echo "WARN_MATCHES" >&2
        echo "$matches" >&2
        echo "PARTIAL:$(echo "$matches" | head -1)"
        return 0
    fi
}

# Traditional path resolution (fallback)
resolve_path_traditional() {
    local input="$1"

    # If input exists as-is, use it
    if [ -e "$input" ]; then
        echo "$input"
        return 0
    fi

    # Search for directories matching the input name in modified files
    local matches=$(git status --porcelain 2>/dev/null | grep -E '^[ MADRC]M|^[ MADRC] ' | awk '{print $2}' | \
        awk -F/ -v name="$input" '
        {
            for (i=1; i<=NF; i++) {
                if ($i == name || index($i, name) > 0) {
                    path = $1
                    for (j=2; j<=i; j++) {
                        path = path "/" $j
                    }
                    print path
                    break
                }
            }
        }
        ' | sort -u)

    local count=$(echo "$matches" | grep -c . || echo "0")

    if [ "$count" = "0" ]; then
        echo ""
        return 1
    elif [ "$count" = "1" ]; then
        echo "$matches"
        return 0
    else
        echo "ERROR_MULTIPLE_MATCHES:$matches" >&2
        return 1
    fi
}

# Step 1: 사전 스캔 - git status로 전체 변경 파일 목록 수집
scan_and_save_to_memory "$MEMORY_FILE"

# Step 2: 경로 해석 - 사용자 입력과 메모리의 파일 목록 매칭
if [ -n "$RAW_PATH" ]; then
    RESOLVED=$(resolve_path_with_memory "$RAW_PATH" "$MEMORY_FILE")
    RESOLVE_EXIT=$?

    # Check for warnings
    if [[ "$RESOLVED" == MULTIPLE:* ]]; then
        echo '{"warning":"여러 경로가 매칭됩니다. 첫 번째 항목을 사용합니다.","resolved":"'"${RESOLVED#MULTIPLE:}"'"}' >&2
    fi

    if [ $RESOLVE_EXIT -ne 0 ] || [ -z "$RESOLVED" ]; then
        if [[ "$RESOLVED" == ERROR_MULTIPLE_MATCHES:* ]]; then
            echo '{"error":"여러 디렉토리가 일치합니다. 전체 경로를 지정해주세요.","matches":"'"${RESOLVED#ERROR_MULTIPLE_MATCHES:}"'"}' >&2
        else
            echo '{"error":"경로를 찾을 수 없습니다: '$RAW_PATH'"}' >&2
        fi
        exit 1
    fi

    # Extract path from resolution format (TYPE:path)
    if [[ "$RESOLVED" == FILE:* ]]; then
        TARGET_PATH="${RESOLVED#FILE:}"
        TARGET_TYPE="file"
    elif [[ "$RESOLVED" == DIR:* ]]; then
        TARGET_PATH="${RESOLVED#DIR:}"
        TARGET_TYPE="dir"
    elif [[ "$RESOLVED" == PARTIAL:* ]]; then
        TARGET_PATH="${RESOLVED#PARTIAL:}"
        TARGET_TYPE="partial"
    else
        TARGET_PATH="$RESOLVED"
        TARGET_TYPE="unknown"
    fi
fi

# Step 3 & 4: 정확한 파일 지정 및 안전한 staging
if [ -n "$TARGET_PATH" ]; then
    # Get matching files from memory
    MATCHING_FILES=$(grep "^- " "$MEMORY_FILE" 2>/dev/null | sed 's/^- //' | grep "^${TARGET_PATH}" || true)

    if [ -z "$MATCHING_FILES" ]; then
        echo '{"error":"지정된 경로에 변경사항이 없습니다: '$TARGET_PATH'"}' >&2
        exit 1
    fi

    # Stage files safely (tracked only)
    STAGED_COUNT=$(stage_files_safely "$MATCHING_FILES" "true")
else
    # Stage all modified files (tracked only, equivalent to git add -u)
    ALL_FILES=$(grep "^- " "$MEMORY_FILE" 2>/dev/null | grep -v "^### " | sed 's/^- //')
    STAGED_COUNT=$(stage_files_safely "$ALL_FILES" "true")
fi

# Step 5: Verify staging
STAGED_FILES=$(git diff --cached --name-only 2>/dev/null)
if [ -z "$STAGED_FILES" ]; then
    echo '{"error":"변경사항이 없습니다"}' >&2
    exit 1
fi

# Filter to target path if specified
if [ -n "$TARGET_PATH" ]; then
    # Verify the staged files match our target
    FILTERED_STAGED=$(echo "$STAGED_FILES" | grep "^${TARGET_PATH}" || true)
    if [ -z "$FILTERED_STAGED" ]; then
        echo '{"error":"지정된 경로에 변경사항이 없습니다: '$TARGET_PATH'"}' >&2
        exit 1
    fi
    STAGED_FILES="$FILTERED_STAGED"
fi

# Collect git metadata
BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# Collect file changes
if [ -n "$TARGET_PATH" ]; then
    FILES_JSON=$(echo "$STAGED_FILES" | xargs git diff --cached --numstat -- 2>/dev/null | awk -v q='"' '
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
    TOTAL_FILES=$(echo "$STAGED_FILES" | wc -l | tr -d ' ')
    TOTAL_ADDITIONS=$(echo "$STAGED_FILES" | xargs git diff --cached --numstat -- 2>/dev/null | awk '{sum+=$1} END {print sum+0}')
    TOTAL_DELETIONS=$(echo "$STAGED_FILES" | xargs git diff --cached --numstat -- 2>/dev/null | awk '{sum+=$2} END {print sum+0}')
else
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
    TOTAL_FILES=$(git diff --cached --name-only 2>/dev/null | wc -l | tr -d ' ')
    TOTAL_ADDITIONS=$(git diff --cached --numstat 2>/dev/null | awk '{sum+=$1} END {print sum+0}')
    TOTAL_DELETIONS=$(git diff --cached --numstat 2>/dev/null | awk '{sum+=$2} END {print sum+0}')
fi

# Output JSON
cat <<EOF
{
  "timestamp": "$TIMESTAMP",
  "branch": "$BRANCH",
  "resolvedPath": "${TARGET_PATH}",
  "summary": {
    "totalFiles": $TOTAL_FILES,
    "totalAdditions": $TOTAL_ADDITIONS,
    "totalDeletions": $TOTAL_DELETIONS
  },
  "files": $FILES_JSON
}
EOF

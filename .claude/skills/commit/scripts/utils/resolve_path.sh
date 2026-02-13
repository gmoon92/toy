#!/bin/bash
# resolve_path.sh
# Purpose: Resolve user input path to actual file paths using memory-based pre-scan
# Usage: source resolve_path.sh && resolve_path_with_memory <input> <memory_file>
#        or: ./resolve_path.sh <input> <memory_file>

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Memory file path (default)
DEFAULT_MEMORY_DIR="${HOME}/.claude/projects/$(basename "$(git rev-parse --show-toplevel 2>/dev/null || echo 'default')")/memory"
DEFAULT_MEMORY_FILE="${DEFAULT_MEMORY_DIR}/commit-skill-paths.md"

# Create memory directory if it doesn't exist
create_memory_dir() {
    local memory_file="${1:-$DEFAULT_MEMORY_FILE}"
    local memory_dir=$(dirname "$memory_file")
    if [ ! -d "$memory_dir" ]; then
        mkdir -p "$memory_dir"
    fi
}

# Scan and save git status to memory
scan_and_save_to_memory() {
    local memory_file="${1:-$DEFAULT_MEMORY_FILE}"
    create_memory_dir "$memory_file"

    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    local repo_root=$(git rev-parse --show-toplevel 2>/dev/null || echo ".")

    # Collect all changed files from git status
    local modified_files=$(git status --porcelain 2>/dev/null | grep -E '^[ MADRC]M|^[ MADRC] ' | awk '{print $2}')
    local staged_files=$(git diff --cached --name-only 2>/dev/null)
    local untracked_files=$(git status --porcelain 2>/dev/null | grep -E '^\?\?' | awk '{print $2}')

    # Generate memory content
    cat > "$memory_file" << EOF
# Commit Skill - 변경 파일 목록

## 현재 변경사항 (${timestamp})

### Modified Files (Unstaged)
$(echo "$modified_files" | while read -r file; do [ -n "$file" ] && echo "- ${file}"; done)

### Staged Files
$(echo "$staged_files" | while read -r file; do [ -n "$file" ] && echo "- ${file}"; done)

### Untracked Files
$(echo "$untracked_files" | while read -r file; do [ -n "$file" ] && echo "- ${file}"; done)

### Git Status with Ignore Info

#### Tracked & Modified (Safe to commit)
$(git status --porcelain 2>/dev/null | grep -E '^[ MADRC]M|^[ MADRC] ' | awk '{print $2}' | while read -r file; do
    if git ls-files --error-unmatch "$file" >/dev/null 2>&1; then
        echo "- ${file}"
    fi
done)

#### Untracked (User confirmation needed)
$(git status --porcelain 2>/dev/null | grep -E '^\?\?' | awk '{print $2}' | while read -r file; do
    if ! git check-ignore -q "$file" 2>/dev/null; then
        echo "- ${file}"
    fi
done)

#### Ignored (Auto-excluded)
$(git status --porcelain 2>/dev/null | grep -E '^\?\?' | awk '{print $2}' | while read -r file; do
    if git check-ignore -q "$file" 2>/dev/null; then
        echo "- ${file}"
    fi
done)
EOF

    echo "$memory_file"
}

# Classify files by git status
classify_files() {
    local files="$1"
    local tracked_files=""
    local untracked_files=""
    local ignored_files=""

    for file in $files; do
        if git ls-files --error-unmatch "$file" >/dev/null 2>&1; then
            # Tracked 파일
            tracked_files="$tracked_files$file "
        elif git check-ignore -q "$file" 2>/dev/null; then
            # Ignored 파일 (staging 금지)
            ignored_files="$ignored_files$file "
        else
            # Untracked (tracked 아니고 ignored 아님)
            untracked_files="$untracked_files$file "
        fi
    done

    echo "TRACKED:${tracked_files%,}"
    echo "UNTRACKED:${untracked_files%,}"
    echo "IGNORED:${ignored_files%,}"
}

# Safe staging - only stage tracked files
stage_files_safely() {
    local files="$1"
    local classification=$(classify_files "$files")

    # Extract tracked files only
    local tracked=$(echo "$classification" | grep "^TRACKED:" | sed 's/^TRACKED://')
    local untracked=$(echo "$classification" | grep "^UNTRACKED:" | sed 's/^UNTRACKED://')
    local ignored=$(echo "$classification" | grep "^IGNORED:" | sed 's/^IGNORED://')

    # Stage tracked files only
    local staged_count=0
    if [ -n "$tracked" ]; then
        for file in $tracked; do
            if [ -n "$file" ]; then
                git add "$file" 2>/dev/null && ((staged_count++))
            fi
        done
    fi

    # Output warnings
    if [ -n "$untracked" ]; then
        echo "WARN:UNTRACKED:${untracked}" >&2
    fi

    if [ -n "$ignored" ]; then
        echo "INFO:IGNORED:${ignored}" >&2
    fi

    echo "$staged_count"
}

# Resolve path with memory-based matching
resolve_path_with_memory() {
    local input="$1"
    local memory_file="${2:-$DEFAULT_MEMORY_FILE}"

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

    # 1단계: 정확한 일치 확인 (메모리 기반)
    local exact_match=$(grep "^- ${input}$" "$memory_file" 2>/dev/null | head -1 | sed 's/^- //')
    if [ -n "$exact_match" ]; then
        echo "EXACT:$exact_match"
        return 0
    fi

    # 2단계: 디렉토리 정확 일치
    if [ -d "$input" ]; then
        echo "DIR:$input"
        return 0
    fi

    # 3단계: 메모리 기반 부분 매칭
    local matches=$(grep "^- " "$memory_file" 2>/dev/null | sed 's/^- //' | grep "/${input}/\|^${input}/\|/${input}$\|^${input}$" | sort -u)
    local count=$(echo "$matches" | grep -c . || echo "0")

    if [ "$count" = "0" ]; then
        # 4단계: 전체 경로에서 부분 문자열 매칭
        matches=$(grep "^- " "$memory_file" 2>/dev/null | sed 's/^- //' | grep "$input" | sort -u)
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
        # 중복 매칭 - 첫 번째 항목 반환하되 경고
        echo "MULTIPLE:$(echo "$matches" | head -1)" >&2
        echo "WARN_MATCHES:${matches}" >&2
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

# Get all matching files from memory
get_matching_files_from_memory() {
    local input="$1"
    local memory_file="${2:-$DEFAULT_MEMORY_FILE}"

    if [ ! -f "$memory_file" ]; then
        # Return empty if no memory file
        echo ""
        return 1
    fi

    # Match files based on input
    local files=""
    if [ -z "$input" ]; then
        # Return all modified files
        files=$(grep "^- " "$memory_file" 2>/dev/null | sed 's/^- //')
    else
        # Match by path prefix
        files=$(grep "^- ${input}" "$memory_file" 2>/dev/null | sed 's/^- //')

        # If no matches, try partial matching
        if [ -z "$files" ]; then
            files=$(grep "^- " "$memory_file" 2>/dev/null | sed 's/^- //' | grep "/${input}/\|^${input}/\|/${input}$\|^${input}$")
        fi
    fi

    echo "$files"
}

# Main execution (if called directly)
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
    case "${1:-}" in
        scan)
            scan_and_save_to_memory "${2:-}"
            ;;
        resolve)
            resolve_path_with_memory "$2" "${3:-}"
            ;;
        classify)
            classify_files "$2"
            ;;
        stage)
            stage_files_safely "$2"
            ;;
        match)
            get_matching_files_from_memory "$2" "${3:-}"
            ;;
        *)
            echo "Usage: $0 {scan|resolve|classify|stage|match} [args...]"
            echo ""
            echo "Commands:"
            echo "  scan [memory_file]           - Scan git status and save to memory"
            echo "  resolve <input> [memory_file] - Resolve path using memory"
            echo "  classify <files>              - Classify files by git status"
            echo "  stage <files>                 - Stage files safely (tracked only)"
            echo "  match <input> [memory_file]   - Get matching files from memory"
            exit 1
            ;;
    esac
fi

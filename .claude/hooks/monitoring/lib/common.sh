#!/bin/bash
# common.sh - 모니터링 시스템 공통 유틸리티

# 환경 변수 설정
MONITORING_DIR="${CLAUDE_PROJECT_DIR}/.claude/monitoring"
HOOKS_LIB_DIR="${CLAUDE_PROJECT_DIR}/.claude/hooks/monitoring/lib"

# 로그 레벨 정의
LOG_LEVEL_DEBUG=0
LOG_LEVEL_INFO=1
LOG_LEVEL_WARN=2
LOG_LEVEL_ERROR=3
CURRENT_LOG_LEVEL=${MONITORING_LOG_LEVEL:-$LOG_LEVEL_INFO}

# 로그 함수
log_debug() { [[ $CURRENT_LOG_LEVEL -le $LOG_LEVEL_DEBUG ]] && echo "[DEBUG] $*" >&2; }
log_info() { [[ $CURRENT_LOG_LEVEL -le $LOG_LEVEL_INFO ]] && echo "[INFO] $*" >&2; }
log_warn() { [[ $CURRENT_LOG_LEVEL -le $LOG_LEVEL_WARN ]] && echo "[WARN] $*" >&2; }
log_error() { [[ $CURRENT_LOG_LEVEL -le $LOG_LEVEL_ERROR ]] && echo "[ERROR] $*" >&2; }

# 락 파일 디렉토리
LOCK_DIR="/tmp/claude-monitoring-locks"
mkdir -p "$LOCK_DIR"

# 락 획득 함수
# 사용법: acquire_lock <lock_name> [timeout_seconds]
acquire_lock() {
    local lock_name=$1
    local timeout=${2:-5}
    local lock_file="$LOCK_DIR/$lock_name.lock"
    local max_attempts=$((timeout * 10))

    for ((i=0; i<max_attempts; i++)); do
        if mkdir "$lock_file" 2>/dev/null; then
            log_debug "Lock acquired: $lock_name"
            return 0
        fi
        sleep 0.1
    done

    log_warn "Failed to acquire lock: $lock_name after ${timeout}s"
    return 1
}

# 락 해제 함수
release_lock() {
    local lock_name=$1
    local lock_file="$LOCK_DIR/$lock_name.lock"

    if [[ -d "$lock_file" ]]; then
        rm -rf "$lock_file"
        log_debug "Lock released: $lock_name"
    fi
}

# 안전한 파일 쓰기 (락 사용)
# 사용법: safe_write <file_path> <content>
safe_write() {
    local file_path=$1
    local content=$2
    local lock_name=$(basename "$file_path" | sed 's/[^a-zA-Z0-9]/_/g')

    if acquire_lock "$lock_name"; then
        echo "$content" >> "$file_path"
        release_lock "$lock_name"
        return 0
    fi
    return 1
}

# JSONL 이벤트 기록
# 사용법: log_event <session_id> <event_type> [event_data_json]
log_event() {
    local session_id=$1
    local event_type=$2
    local event_data=${3:-{}}
    local timestamp=$(date -u +%Y-%m-%dT%H:%M:%SZ)
    local log_file="$MONITORING_DIR/sessions/${session_id}.jsonl"

    # 세션 디렉토리 생성
    mkdir -p "$(dirname "$log_file")"

    # JSONL 형식으로 기록
    local json_event=$(jq -n \
        --arg timestamp "$timestamp" \
        --arg event "$event_type" \
        --argjson data "$event_data" \
        '{timestamp: $timestamp, event: $event} + $data')

    safe_write "$log_file" "$json_event"
}

# 세션 로그 파일 경로 반환
get_session_log_path() {
    local session_id=$1
    echo "$MONITORING_DIR/sessions/${session_id}.jsonl"
}

# 사용 통계 파일 경로 반환
get_stats_path() {
    echo "$MONITORING_DIR/usage-stats.json"
}

# 초기 통계 파일 생성
init_stats_file() {
    local stats_file=$(get_stats_path)

    if [[ ! -f "$stats_file" ]]; then
        mkdir -p "$(dirname "$stats_file")"
        echo '{"last_updated": "'$(date -u +%Y-%m-%dT%H:%M:%SZ)'", "agents": {}, "skills": {}, "patterns": {}}' > "$stats_file"
        log_info "Initialized stats file: $stats_file"
    fi
}

# 훅 입력에서 값 추출
get_hook_input() {
    local key=$1
    echo "$HOOK_INPUT" | jq -r ".$key // empty"
}

# 훅 출력 형식 생성
output_json() {
    local continue_flag=${1:-true}
    local system_message=${2:-}
    local additional_context=${3:-}

    local output="{\"continue\": $continue_flag"

    if [[ -n "$system_message" ]]; then
        output="$output, \"systemMessage\": $(echo "$system_message" | jq -Rs '.')"
    fi

    if [[ -n "$additional_context" ]]; then
        output="$output, \"hookSpecificOutput\": {\"hookEventName\": \"${HOOK_EVENT_NAME}\", \"additionalContext\": $(echo "$additional_context" | jq -Rs '.')}"
    fi

    output="$output}"
    echo "$output"
}

# 에이전트 문서 경로 반환
get_agent_doc_path() {
    local agent_name=$1
    echo "$CLAUDE_PROJECT_DIR/.claude/agents/docs/${agent_name}.md"
}

# 스킬 문서 경로 반환
get_skill_doc_path() {
    local skill_name=$1
    echo "$CLAUDE_PROJECT_DIR/.claude/skills/${skill_name}/SKILL.md"
}

# 문서 평가 유틸리티
evaluate_doc() {
    local doc_path=$1
    local doc_type=$2  # 'agent' or 'skill'

    local result="{}"

    if [[ ! -f "$doc_path" ]]; then
        echo '{"exists": false}'
        return
    fi

    result=$(echo "$result" | jq '.exists = true')

    # 기본 메트릭
    local line_count=$(wc -l < "$doc_path")
    result=$(echo "$result" | jq --argjson lines "$line_count" '.line_count = $lines')

    # 예시 포함 여부
    local has_examples=$(grep -c "<example>" "$doc_path" 2>/dev/null || echo "0")
    result=$(echo "$result" | jq --argjson examples "$has_examples" '.has_examples = ($examples > 0)')

    # 워크플로우 정의 여부
    local has_workflow=$(grep -c "## Workflow\|## Execution Flow\|## Process" "$doc_path" 2>/dev/null || echo "0")
    result=$(echo "$result" | jq --argjson workflow "$has_workflow" '.has_workflow = ($workflow > 0)')

    # YAML Frontmatter 파싱 (에이전트/스킬 공통)
    local in_frontmatter=false
    local frontmatter=""
    while IFS= read -r line; do
        if [[ "$line" == "---" ]]; then
            if [[ "$in_frontmatter" == false ]]; then
                in_frontmatter=true
                continue
            else
                break
            fi
        fi
        if [[ "$in_frontmatter" == true ]]; then
            frontmatter="$frontmatter$line\n"
        fi
    done < "$doc_path"

    result=$(echo "$result" | jq --arg fm "$frontmatter" '.frontmatter = $fm')

    echo "$result"
}

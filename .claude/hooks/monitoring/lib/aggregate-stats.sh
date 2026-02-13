#!/bin/bash
# aggregate-stats.sh - 세션 로그 분석 및 통계 집계

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

SESSION_ID=$1

if [[ -z "$SESSION_ID" ]]; then
    log_error "Usage: $0 <session_id>"
    exit 1
fi

SESSION_LOG=$(get_session_log_path "$SESSION_ID")

if [[ ! -f "$SESSION_LOG" ]]; then
    log_warn "Session log not found: $SESSION_LOG"
    exit 0
fi

log_info "Aggregating stats for session: $SESSION_ID"

# 세션 분석
SESSION_ANALYSIS=$(jq -s '
{
    total_events: length,
    subagent_invocations: map(select(.event == "subagent_stop")) | length,
    skill_invocations: map(select(.event == "skill_invoke")) | length,
    task_invocations: map(select(.event == "task_invoke")) | length,
    unique_agents: [map(select(.event == "subagent_stop") | .agent_type) | unique[]],
    unique_skills: [map(select(.event == "skill_invoke") | .skill_name) | unique[]],
    start_time: map(select(.timestamp))[0].timestamp // empty,
    end_time: map(select(.timestamp))[-1].timestamp // empty
}' "$SESSION_LOG" 2>/dev/null || echo '{}')

# 통계 파일 업데이트
init_stats_file

STATS_FILE=$(get_stats_path)
LOCK_NAME="stats_aggregate"

if ! acquire_lock "$LOCK_NAME" 10; then
    log_error "Failed to acquire lock for stats aggregation"
    exit 1
fi

# 현재 통계 읽기
CURRENT_STATS=$(cat "$STATS_FILE")

# 패턴 분석 업데이트
UPDATED_STATS=$(echo "$CURRENT_STATS" | jq \
    --argjson analysis "$SESSION_ANALYSIS" \
    --arg now "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
    --arg session_id "$SESSION_ID" '
    .patterns.total_sessions = (.patterns.total_sessions // 0) + 1 |
    .patterns.last_analyzed_session = $session_id |
    .last_updated = $now |
    .latest_session_analysis = $analysis
')

# 저장
echo "$UPDATED_STATS" > "$STATS_FILE"

release_lock "$LOCK_NAME"

log_info "Stats aggregation completed"
exit 0

#!/bin/bash
# update-stats.sh - 사용 통계 업데이트

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

# 인자 파싱
ENTITY_TYPE=$1  # 'agent' or 'skill'
ENTITY_NAME=$2
SESSION_ID=$3

if [[ -z "$ENTITY_TYPE" || -z "$ENTITY_NAME" || -z "$SESSION_ID" ]]; then
    log_error "Usage: $0 <agent|skill> <name> <session_id>"
    exit 1
fi

init_stats_file

STATS_FILE=$(get_stats_path)
LOCK_NAME="usage_stats"

# 락 획득
if ! acquire_lock "$LOCK_NAME" 10; then
    log_error "Failed to acquire lock for stats update"
    exit 1
fi

# 현재 통계 읽기
STATS=$(cat "$STATS_FILE")

# 업데이트 수행
if [[ "$ENTITY_TYPE" == "agent" ]]; then
    # 에이전트 통계 업데이트
    UPDATED=$(echo "$STATS" | jq \
        --arg name "$ENTITY_NAME" \
        --arg session "$SESSION_ID" \
        --arg now "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        '
        .agents[$name] = (.agents[$name] // {invocation_count: 0, success_count: 0, total_duration_sec: 0}) |
        .agents[$name].invocation_count += 1 |
        .agents[$name].last_used = $now |
        .last_updated = $now
        ')
else
    # 스킬 통계 업데이트
    UPDATED=$(echo "$STATS" | jq \
        --arg name "$ENTITY_NAME" \
        --arg session "$SESSION_ID" \
        --arg now "$(date -u +%Y-%m-%dT%H:%M:%SZ)" \
        '
        .skills[$name] = (.skills[$name] // {invocation_count: 0, avg_execution_time_sec: 0}) |
        .skills[$name].invocation_count += 1 |
        .skills[$name].last_used = $now |
        .last_updated = $now
        ')
fi

# 통계 저장
echo "$UPDATED" > "$STATS_FILE"

# 락 해제
release_lock "$LOCK_NAME"

log_info "Updated stats for $ENTITY_TYPE: $ENTITY_NAME"
exit 0

#!/bin/bash
# stop.sh - 대화 종료 시 세션 분석 및 개선점 생성 트리거

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

# 훅 입력 읽기
HOOK_INPUT=$(cat)
export HOOK_INPUT

# 입력값 추출
SESSION_ID=$(get_hook_input "session_id")
TRANSCRIPT_PATH=$(get_hook_input "transcript_path")
REASON=$(get_hook_input "reason")

log_info "Stop hook: session=$SESSION_ID, reason=$REASON"

# 세션 종료 이벤트 기록
EVENT_DATA=$(jq -n \
    --arg reason "$REASON" \
    --arg transcript "$TRANSCRIPT_PATH" \
    '{
        stop_reason: $reason,
        transcript_path: $transcript
    }')

log_event "$SESSION_ID" "session_stop" "$EVENT_DATA"

# 세션 분석 및 개선점 생성 (비동기)
(
    sleep 1  # 로그 기록 완료 대기
    SESSION_LOG=$(get_session_log_path "$SESSION_ID")

    if [[ -f "$SESSION_LOG" ]]; then
        log_info "Analyzing session: $SESSION_ID"

        # 사용된 에이전트/스킬 분석
        TOP_AGENTS=$(jq -s '
            map(select(.event == "subagent_stop")) |
            group_by(.agent_type) |
            map({agent: .[0].agent_type, count: length}) |
            sort_by(.count) |
            reverse |
            .[0:5]
        ' "$SESSION_LOG" 2>/dev/null || echo "[]")

        TOP_SKILLS=$(jq -s '
            map(select(.event == "skill_invoke")) |
            group_by(.skill_name) |
            map({skill: .[0].skill_name, count: length}) |
            sort_by(.count) |
            reverse |
            .[0:5]
        ' "$SESSION_LOG" 2>/dev/null || echo "[]")

        log_info "Top agents: $(echo "$TOP_AGENTS" | jq -c '.')"
        log_info "Top skills: $(echo "$TOP_SKILLS" | jq -c '.')"

        # 개선 제안 생성
        if [[ $(echo "$TOP_AGENTS" | jq 'length') -gt 0 || $(echo "$TOP_SKILLS" | jq 'length') -gt 0 ]]; then
            "$SCRIPT_DIR/lib/generate-suggestions.sh" "$TOP_AGENTS" "$TOP_SKILLS" "$SESSION_ID"
        fi

        # 최종 통계 집계
        "$SCRIPT_DIR/lib/aggregate-stats.sh" "$SESSION_ID"
    fi
) &

exit 0

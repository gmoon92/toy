#!/bin/bash
# subagent-stop.sh - 서브 에이전트 종료 시 결과 수집 및 메인 컨텍스트 전달

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

# 훅 입력 읽기
HOOK_INPUT=$(cat)
export HOOK_INPUT
HOOK_EVENT_NAME="SubagentStop"

# 입력값 추출
SESSION_ID=$(get_hook_input "session_id")
AGENT_ID=$(get_hook_input "agent_id")
TRANSCRIPT_PATH=$(get_hook_input "transcript_path")
REASON=$(get_hook_input "reason")

log_info "SubagentStop: agent_id=$AGENT_ID, session_id=$SESSION_ID"

# 결과 요약 추출 (트랜스크립트에서 마지막 메시지)
RESULT_SUMMARY=""
AGENT_TYPE="unknown"
TASK_DESCRIPTION=""
DURATION_SEC=0

if [[ -f "$TRANSCRIPT_PATH" ]]; then
    # 트랜스크립트에서 정보 추출
    LAST_MESSAGE=$(jq -r '.messages[-1].content // empty' "$TRANSCRIPT_PATH" 2>/dev/null | head -c 1000)
    AGENT_TYPE=$(jq -r '.subagent_type // "unknown"' "$TRANSCRIPT_PATH" 2>/dev/null)
    TASK_DESCRIPTION=$(jq -r '.task_description // empty' "$TRANSCRIPT_PATH" 2>/dev/null)

    # 결과 요약 생성
    if [[ -n "$LAST_MESSAGE" ]]; then
        # 처음 500자만 사용
        RESULT_SUMMARY="${LAST_MESSAGE:0:500}"
        if [[ ${#LAST_MESSAGE} -gt 500 ]]; then
            RESULT_SUMMARY="${RESULT_SUMMARY}..."
        fi
    fi
fi

# 세션 로그에 이벤트 기록
EVENT_DATA=$(jq -n \
    --arg agent_id "$AGENT_ID" \
    --arg agent_type "$AGENT_TYPE" \
    --arg description "$TASK_DESCRIPTION" \
    --arg reason "$REASON" \
    --arg result "${RESULT_SUMMARY:0:200}" \
    --arg transcript "$TRANSCRIPT_PATH" \
    '{
        agent_id: $agent_id,
        agent_type: $agent_type,
        task_description: $description,
        reason: $reason,
        result_summary: $result,
        transcript_path: $transcript
    }')

log_event "$SESSION_ID" "subagent_stop" "$EVENT_DATA"

# 사용 통계 업데이트 (비동기)
if [[ "$AGENT_TYPE" != "unknown" ]]; then
    (
        "$SCRIPT_DIR/lib/update-stats.sh" "agent" "$AGENT_TYPE" "$SESSION_ID"
    ) &
fi

# 메인 컨텍스트에 결과 전달
ADDITIONAL_CONTEXT="🤖 **에이전트 작업 완료**

- **타입**: $AGENT_TYPE
- **ID**: $AGENT_ID
- **상태**: $REASON

**결과 요약**:
$RESULT_SUMMARY

${TRANSCRIPT_PATH:+[상세 트랜스크립트]($TRANSCRIPT_PATH)}"

# JSON 출력
output_json true "" "$ADDITIONAL_CONTEXT"
exit 0

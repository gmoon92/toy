#!/bin/bash
# post-tool-use.sh - 도구 사용 시 이력 기록

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/common.sh"

# 훅 입력 읽기
HOOK_INPUT=$(cat)
export HOOK_INPUT

# 입력값 추출
SESSION_ID=$(get_hook_input "session_id")
TOOL_NAME=$(get_hook_input "tool_name")
TOOL_INPUT=$(get_hook_input "tool_input")
TOOL_RESULT=$(get_hook_input "tool_result")

log_info "PostToolUse: tool=$TOOL_NAME, session=$SESSION_ID"

# Task 도구 사용 기록
if [[ "$TOOL_NAME" == "Task" ]]; then
    SUBAGENT_TYPE=$(echo "$TOOL_INPUT" | jq -r '.subagent_type // "general-purpose"')
    DESCRIPTION=$(echo "$TOOL_INPUT" | jq -r '.description // empty')
    PROMPT=$(echo "$TOOL_INPUT" | jq -r '.prompt // empty' | head -c 200)

    EVENT_DATA=$(jq -n \
        --arg type "$SUBAGENT_TYPE" \
        --arg desc "$DESCRIPTION" \
        --arg prompt "$PROMPT" \
        '{
            subagent_type: $type,
            task_description: $desc,
            prompt_preview: $prompt
        }')

    log_event "$SESSION_ID" "task_invoke" "$EVENT_DATA"
    log_info "Recorded Task invocation: $SUBAGENT_TYPE"
fi

# Skill 도구 사용 기록
if [[ "$TOOL_NAME" == "Skill" ]]; then
    SKILL_NAME=$(echo "$TOOL_INPUT" | jq -r '.skill // .skill_name // empty')
    ARGS=$(echo "$TOOL_INPUT" | jq -r '.args // empty')

    if [[ -n "$SKILL_NAME" ]]; then
        EVENT_DATA=$(jq -n \
            --arg skill "$SKILL_NAME" \
            --arg args "$ARGS" \
            '{
                skill_name: $skill,
                args_preview: $args
            }')

        log_event "$SESSION_ID" "skill_invoke" "$EVENT_DATA"
        log_info "Recorded Skill invocation: $SKILL_NAME"

        # 사용 통계 업데이트 (비동기)
        (
            "$SCRIPT_DIR/lib/update-stats.sh" "skill" "$SKILL_NAME" "$SESSION_ID"
        ) &
    fi
fi

# 기타 중요 도구 사용 기록
if [[ "$TOOL_NAME" == "AskUserQuestion" ]]; then
    QUESTION=$(echo "$TOOL_INPUT" | jq -r '.questions[0].question // empty' | head -c 100)

    EVENT_DATA=$(jq -n \
        --arg question "$QUESTION" \
        '{question_preview: $question}')

    log_event "$SESSION_ID" "user_interaction" "$EVENT_DATA"
fi

exit 0

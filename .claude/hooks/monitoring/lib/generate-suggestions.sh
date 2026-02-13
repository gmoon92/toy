#!/bin/bash
# generate-suggestions.sh - 에이전트/스킬 문서 자동 평가 및 개선 제안 생성

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

# 인자 파싱
TOP_AGENTS_JSON=$1
TOP_SKILLS_JSON=$2
SESSION_ID=$3

if [[ -z "$SESSION_ID" ]]; then
    log_error "Usage: $0 <top_agents_json> <top_skills_json> <session_id>"
    exit 1
fi

SUGGESTIONS_DIR="$MONITORING_DIR/improvement-suggestions/pending"
mkdir -p "$SUGGESTIONS_DIR"

log_info "Generating suggestions for session: $SESSION_ID"

# 에이전트 문서 평가 및 개선 제안 생성
evaluate_agents() {
    local agents_json=$1

    echo "$agents_json" | jq -c '.[]' 2>/dev/null | while read -r agent_info; do
        AGENT_NAME=$(echo "$agent_info" | jq -r '.agent')
        USAGE_COUNT=$(echo "$agent_info" | jq -r '.count')

        AGENT_FILE=$(get_agent_doc_path "$AGENT_NAME")

        if [[ ! -f "$AGENT_FILE" ]]; then
            log_warn "Agent file not found: $AGENT_FILE"
            continue
        fi

        log_info "Evaluating agent: $AGENT_NAME (used $USAGE_COUNT times)"

        # 문서 메트릭 계산
        DOC_LENGTH=$(wc -l < "$AGENT_FILE")
        HAS_EXAMPLES=$(grep -c "<example>" "$AGENT_FILE" 2>/dev/null || echo "0")
        HAS_WORKFLOW=$(grep -c -i "workflow\|process\|execution flow" "$AGENT_FILE" 2>/dev/null || echo "0")
        HAS_DESCRIPTION=$(grep -c "^description:" "$AGENT_FILE" 2>/dev/null || echo "0")

        # 개선 필요 여부 판단
        SUGGESTIONS=()
        PRIORITY="low"

        if [[ "$DOC_LENGTH" -lt 30 ]]; then
            SUGGESTIONS+=("문서가 너무 짧습니다 ($DOC_LENGTH 줄). 더 자세한 설명과 사용 가이드를 추가하세요.")
            PRIORITY="high"
        fi

        if [[ "$HAS_EXAMPLES" -eq 0 ]]; then
            SUGGESTIONS+=("사용 예시(<example> 태그)가 없습니다. 구체적인 사용 사례를 예시로 추가하세요.")
            [[ "$PRIORITY" == "low" ]] && PRIORITY="medium"
        fi

        if [[ "$HAS_WORKFLOW" -eq 0 ]]; then
            SUGGESTIONS+=("실행 워크플로우가 정의되지 않았습니다. 에이전트의 단계별 실행 프로세스를 문서화하세요.")
            [[ "$PRIORITY" == "low" ]] && PRIORITY="medium"
        fi

        if [[ ${#SUGGESTIONS[@]} -eq 0 ]]; then
            continue
        fi

        # 개선 제안 파일 생성
        SUGGESTION_ID="agent-$(echo "$AGENT_NAME" | tr '[:upper:]' '[:lower:]' | tr ' ' '-')-$(date +%s)"
        SUGGESTION_FILE="$SUGGESTIONS_DIR/$SUGGESTION_ID.md"

        # 제안 내용 작성
        cat > "$SUGGESTION_FILE" <<EOF
---
type: agent-improvement
target: $AGENT_NAME
session_id: $SESSION_ID
created_at: $(date -u +%Y-%m-%dT%H:%M:%SZ)
priority: $PRIORITY
status: pending
---

# 개선 제안: $AGENT_NAME

## 평가 요약

| 항목 | 값 |
|------|-----|
| 에이전트 이름 | $AGENT_NAME |
| 사용 횟수 (현재 세션) | $USAGE_COUNT |
| 문서 길이 | $DOC_LENGTH 줄 |
| 예시 포함 | $([ "$HAS_EXAMPLES" -gt 0 ] && echo "✅ $HAS_EXAMPLES개" || echo "❌ 없음") |
| 워크플로우 정의 | $([ "$HAS_WORKFLOW" -gt 0 ] && echo "✅ 있음" || echo "❌ 없음") |

## 개선 사항

EOF

        for i in "${!SUGGESTIONS[@]}"; do
            echo "$((i+1)). ${SUGGESTIONS[$i]}" >> "$SUGGESTION_FILE"
        done

        cat >> "$SUGGESTION_FILE" <<EOF

## 권장 작업

1. [ ] 에이전트 문서 구조 검토
2. [ ] 사용 예시 추가 (<example> 태그 활용)
3. [ ] 실행 워크플로우 문서화
4. [ ] description 필드 개선 (최대 1024자 활용)

## 파일 위치

\`$AGENT_FILE\`

---

*이 제안은 자동으로 생성되었습니다. Claude Code에게 "개선 제안 적용"을 요청하거나 직접 수정하세요.*
EOF

        log_info "Created suggestion: $SUGGESTION_FILE"
    done
}

# 스킬 문서 평가 및 개선 제안 생성
evaluate_skills() {
    local skills_json=$1

    echo "$skills_json" | jq -c '.[]' 2>/dev/null | while read -r skill_info; do
        SKILL_NAME=$(echo "$skill_info" | jq -r '.skill')
        USAGE_COUNT=$(echo "$skill_info" | jq -r '.count')

        SKILL_FILE=$(get_skill_doc_path "$SKILL_NAME")

        if [[ ! -f "$SKILL_FILE" ]]; then
            log_warn "Skill file not found: $SKILL_FILE"
            continue
        fi

        log_info "Evaluating skill: $SKILL_NAME (used $USAGE_COUNT times)"

        # 문서 메트릭 계산
        DOC_LENGTH=$(wc -l < "$SKILL_FILE")
        HAS_OVERVIEW=$(grep -c -i "^## overview\|^## description" "$SKILL_FILE" 2>/dev/null || echo "0")
        HAS_WORKFLOW=$(grep -c -i "^## workflow\|^## execution flow\|^## process" "$SKILL_FILE" 2>/dev/null || echo "0")
        HAS_REFERENCES=$(find "$(dirname "$SKILL_FILE")/references" -type f 2>/dev/null | wc -l)
        HAS_PRINCIPLES=$(grep -c -i "^## core principles\|^## principles" "$SKILL_FILE" 2>/dev/null || echo "0")

        # 개선 필요 여부 판단
        SUGGESTIONS=()
        PRIORITY="low"

        if [[ "$DOC_LENGTH" -lt 50 ]]; then
            SUGGESTIONS+=("SKILL.md가 너무 짧습니다 ($DOC_LENGTH 줄). 스킬의 목적과 사용법을 더 자세히 설명하세요.")
            PRIORITY="high"
        fi

        if [[ "$HAS_OVERVIEW" -eq 0 ]]; then
            SUGGESTIONS+=("Overview 섹션이 없습니다. 스킬의 핵심 기능과 스코프를 요약하는 Overview를 추가하세요.")
            [[ "$PRIORITY" == "low" ]] && PRIORITY="medium"
        fi

        if [[ "$HAS_WORKFLOW" -eq 0 ]]; then
            SUGGESTIONS+=("워크플로우/실행 흐름 섹션이 없습니다. 스킬의 단계별 실행 과정을 문서화하세요.")
            [[ "$PRIORITY" == "low" ]] && PRIORITY="medium"
        fi

        if [[ "$HAS_REFERENCES" -eq 0 ]]; then
            SUGGESTIONS+=("references/ 디렉토리에 참조 문서가 없습니다. 상세 가이드와 예시를 별도 문서로 분리하세요.")
        fi

        if [[ ${#SUGGESTIONS[@]} -eq 0 ]]; then
            continue
        fi

        # 개선 제안 파일 생성
        SUGGESTION_ID="skill-$(echo "$SKILL_NAME" | tr '[:upper:]' '[:lower:]' | tr ' ' '-')-$(date +%s)"
        SUGGESTION_FILE="$SUGGESTIONS_DIR/$SUGGESTION_ID.md"

        cat > "$SUGGESTION_FILE" <<EOF
---
type: skill-improvement
target: $SKILL_NAME
session_id: $SESSION_ID
created_at: $(date -u +%Y-%m-%dT%H:%M:%SZ)
priority: $PRIORITY
status: pending
---

# 개선 제안: $SKILL_NAME

## 평가 요약

| 항목 | 값 |
|------|-----|
| 스킬 이름 | $SKILL_NAME |
| 사용 횟수 (현재 세션) | $USAGE_COUNT |
| SKILL.md 길이 | $DOC_LENGTH 줄 |
| Overview 섹션 | $([ "$HAS_OVERVIEW" -gt 0 ] && echo "✅ 있음" || echo "❌ 없음") |
| 워크플로우 정의 | $([ "$HAS_WORKFLOW" -gt 0 ] && echo "✅ 있음" || echo "❌ 없음") |
| 참조 문서 수 | $HAS_REFERENCES 개 |
| 원칙 문서화 | $([ "$HAS_PRINCIPLES" -gt 0 ] && echo "✅ 있음" || echo "❌ 없음") |

## 개선 사항

EOF

        for i in "${!SUGGESTIONS[@]}"; do
            echo "$((i+1)). ${SUGGESTIONS[$i]}" >> "$SUGGESTION_FILE"
        done

        cat >> "$SUGGESTION_FILE" <<EOF

## 권장 작업

1. [ ] SKILL.md 구조 검토
2. [ ] Overview 섹션 추가/개선
3. [ ] Execution Flow/Workflow 섹션 작성
4. [ ] references/ 디렉토리에 상세 가이드 분리
5. [ ] 사용 예시 추가

## 파일 위치

\`$SKILL_FILE\`

## 참고: Skill Builder 원칙

- SKILL.md는 500줄 이하로 유지
- 필요한 경우에만 references/ 로드 (Progressive Disclosure)
- 명확하고 구체적인 지시 작성
- 예시와 세부사항에 주의

---

*이 제안은 자동으로 생성되었습니다. Claude Code에게 "개선 제안 적용"을 요청하거나 직접 수정하세요.*
EOF

        log_info "Created suggestion: $SUGGESTION_FILE"
    done
}

# 실행
log_info "Starting evaluation..."

if [[ -n "$TOP_AGENTS_JSON" && "$TOP_AGENTS_JSON" != "[]" ]]; then
    evaluate_agents "$TOP_AGENTS_JSON"
fi

if [[ -n "$TOP_SKILLS_JSON" && "$TOP_SKILLS_JSON" != "[]" ]]; then
    evaluate_skills "$TOP_SKILLS_JSON"
fi

log_info "Suggestion generation completed"
exit 0

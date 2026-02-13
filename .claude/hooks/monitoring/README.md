# Claude Code 에이전트/스킬 모니터링 시스템

Claude Code CLI를 통해 사용자 학습을 자동화하고, 에이전트/에이전트 스킬 문서를 모니터링 및 평가하는 시스템입니다.

## 주요 기능

### 1. 컨텍스트 연속성 (실시간 모니터링)
- 서브 에이전트(Task 도구)의 작업 중간 결과를 메인 컨텍스트에 표시
- 에이전트 시작/완료 시 알림 제공
- 작업 요약 및 트랜스크립트 링크 자동 제공

### 2. 사용 이력 추적
- 어떤 에이전트와 스킬이 사용되었는지 세션별로 기록
- 누적 사용 통계 제공 (`.claude/hooks/monitoring/logs/usage-stats.json`)
- 에이전트별 성공률, 평균 실행 시간 등 메트릭 수집

### 3. 자동 개선 제안
- 에이전트/스킬 문서를 자동으로 평가
- 문서 길이, 예시 포함 여부, 워크플로우 정의 등 체크
- 개선이 필요한 항목을 마크다운 파일로 생성 (`.claude/hooks/monitoring/logs/improvement-suggestions/pending/`)

## 설치 방법

### 1. 설정 파일 복사

```bash
# 기존 settings.json이 있는 경우, 내용을 병합
cat .claude/hooks/monitoring/settings.json >> ~/.claude/settings.json
```

또는 Claude Code 설정에 직접 추가:

```json
{
  "hooks": {
    "SubagentStop": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "${CLAUDE_PROJECT_DIR}/.claude/hooks/monitoring/subagent-stop.sh",
            "timeout": 30
          }
        ]
      }
    ],
    "PostToolUse": [
      {
        "matcher": "Task|Skill|AskUserQuestion",
        "hooks": [
          {
            "type": "command",
            "command": "${CLAUDE_PROJECT_DIR}/.claude/hooks/monitoring/post-tool-use.sh",
            "timeout": 10
          }
        ]
      }
    ],
    "Stop": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "${CLAUDE_PROJECT_DIR}/.claude/hooks/monitoring/stop.sh",
            "timeout": 60
          }
        ]
      }
    ]
  }
}
```

### 2. Claude Code 재시작

설정 변경 후 Claude Code를 재시작하여 훅을 로드합니다.

## 데이터 구조

### 세션 로그 (`.claude/hooks/monitoring/logs/sessions/{session-id}.jsonl`)

```jsonl
{"timestamp": "2026-02-13T10:00:00Z", "event": "task_invoke", "subagent_type": "Explore", "task_description": "Find API endpoints"}
{"timestamp": "2026-02-13T10:05:00Z", "event": "subagent_stop", "agent_type": "Explore", "result_summary": "Found 5 API endpoints"}
{"timestamp": "2026-02-13T10:10:00Z", "event": "skill_invoke", "skill_name": "commit"}
```

### 사용 통계 (`.claude/hooks/monitoring/logs/usage-stats.json`)

```json
{
  "last_updated": "2026-02-13T10:20:00Z",
  "agents": {
    "Explore": {
      "invocation_count": 15,
      "success_rate": 0.93,
      "last_used": "2026-02-13T10:10:00Z"
    }
  },
  "skills": {
    "commit": {
      "invocation_count": 42,
      "last_used": "2026-02-13T10:10:00Z"
    }
  },
  "patterns": {
    "total_sessions": 10
  }
}
```

### 개선 제안 (`.claude/hooks/monitoring/logs/improvement-suggestions/pending/{id}.md`)

```markdown
---
type: agent-improvement
target: my-agent
session_id: abc123
created_at: 2026-02-13T10:00:00Z
priority: medium
status: pending
---

# 개선 제안: my-agent

## 평가 요약

| 항목 | 값 |
|------|-----|
| 문서 길이 | 20 줄 |
| 예시 포함 | ❌ 없음 |
| 워크플로우 정의 | ❌ 없음 |

## 개선 사항

1. 문서가 너무 짧습니다 (20 줄). 더 자세한 설명과 사용 가이드를 추가하세요.
2. 사용 예시(<example> 태그)가 없습니다. 구체적인 사용 사례를 예시로 추가하세요.

## 권장 작업

1. [ ] 에이전트 문서 구조 검토
2. [ ] 사용 예시 추가
3. [ ] 실행 워크플로우 문서화
```

## 훅 구성

| 훅 | 설명 | 파일 |
|----|------|------|
| `SubagentStop` | 서브 에이전트 완료 시 결과 수집 및 메인 컨텍스트에 요약 전달 | `subagent-stop.sh` |
| `PostToolUse` | Task/Skill/AskUserQuestion 도구 사용 시 이력 기록 | `post-tool-use.sh` |
| `Stop` | 대화 종료 시 세션 분석 및 개선점 생성 트리거 | `stop.sh` |

## 스크립트 구조

```
.claude/hooks/monitoring/
├── README.md
├── settings.json
├── subagent-stop.sh
├── post-tool-use.sh
├── stop.sh
└── lib/
    ├── common.sh           # 공통 유틸리티
    ├── update-stats.sh     # 통계 업데이트
    ├── generate-suggestions.sh  # 개선 제안 생성
    └── aggregate-stats.sh  # 통계 집계
```

## 유틸리티 함수

### common.sh에서 제공하는 함수

```bash
# 로그 출력
log_debug "message"
log_info "message"
log_warn "message"
log_error "message"

# 락 관리 (병렬 실행 안전)
acquire_lock "lock_name" [timeout_seconds]
release_lock "lock_name"

# 이벤트 로깅
log_event "$SESSION_ID" "event_type" '{"key": "value"}'

# 문서 경로
get_agent_doc_path "agent-name"  # .claude/agents/docs/agent-name.md
get_skill_doc_path "skill-name"  # .claude/skills/skill-name/SKILL.md

# 문서 평가
evaluate_doc "$file_path" "agent|skill"  # JSON 반환
```

## 문제 해결

### 로그 확인

```bash
# 디버그 모드로 실행 (설정에서 로그 레벨 변경)
export MONITORING_LOG_LEVEL=0  # DEBUG
```

### 락 파일 정리

```bash
# 비정상 종료 후 락 파일 수동 제거
rm -rf /tmp/claude-monitoring-locks/*.lock
```

### 훅 테스트

```bash
# 훅 스크립트 직접 테스트
echo '{"session_id": "test123", "agent_id": "agent-001", "reason": "completed"}' | \
  bash .claude/hooks/monitoring/subagent-stop.sh
```

## 개발

### 새로운 평가 기준 추가

`lib/generate-suggestions.sh`에서 에이전트/스킬 평가 로직을 수정하세요.

### 훅 추가

1. 새 스크립트를 `hooks/monitoring/`에 생성
2. `common.sh` 소스 포함
3. `settings.json`에 훅 설정 추가
4. Claude Code 재시작

## 라이선스

이 프로젝트는 사용자의 Claude Code 환경 설정의 일부로 제공됩니다.

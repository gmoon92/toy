# .claude/monitoring/ 디렉토리 구조 설명

## 생성 목적

이 디렉토리는 **Claude Code 에이전트/스킬 모니터링 시스템**의 데이터 저장소 역할을 합니다. 프로젝트별로 모니터링 데이터를 독립적으로 관리하여 다음과 같은 목표를 달성합니다:

1. **세션별 이력 추적**: 어떤 에이전트와 스킬이 사용되었는지 기록
2. **사용 패턴 분석**: 자주 사용되는 에이전트/스킬 파악
3. **품질 개선**: 문서 자동 평가 및 개선 제안 관리

## 디렉토리 구조

```
.claude/monitoring/
├── sessions/                    # 세션별 이벤트 로그
│   └── {session-id}.jsonl       # 예: 2026-02-13-abc123.jsonl
├── agent-evaluations/           # 에이전트 평가 결과
│   └── {agent-name}/
│       └── {timestamp}.json
├── improvement-suggestions/     # 자동 생성된 개선 제안
│   └── pending/                 # 검토 대기 중인 제안
│       └── {suggestion-id}.md   # 예: agent-explore-1707800000.md
└── usage-stats.json             # 누적 사용 통계

```

## 각 디렉토리/파일 상세 설명

### 1. `sessions/` - 세션 로그 저장소

**목적**: 각 Claude Code 세션에서 발생한 이벤트를 시간 순서대로 기록

**파일 형식**: JSONL (JSON Lines)
- 각 줄이 하나의 JSON 이벤트 객체
- 시간 순서대로 append-only로 기록 (락 메커니즘으로 동시 쓰기 안전)

**이벤트 타입**:
```jsonl
{"timestamp": "2026-02-13T10:00:00Z", "event": "task_invoke", "subagent_type": "Explore", "task_description": "Find API endpoints"}
{"timestamp": "2026-02-13T10:05:00Z", "event": "subagent_stop", "agent_type": "Explore", "result_summary": "Found 5 endpoints"}
{"timestamp": "2026-02-13T10:10:00Z", "event": "skill_invoke", "skill_name": "commit"}
{"timestamp": "2026-02-13T10:15:00Z", "event": "user_interaction", "question_preview": "Which library should..."}
```

**왜 JSONL을 사용하는가?**:
- Append-only 작업에 최적화 (파일 끝에 추가만 하면 됨)
- jq 등 도구로 쉽게 쿼리 가능
- 손상되어도 나머지 라인은 유효함
- 스트리밍 처리에 적합

---

### 2. `usage-stats.json` - 누적 사용 통계

**목적**: 프로젝트 전체에서의 에이전트/스킬 사용 패턴 집계

**구조**:
```json
{
  "last_updated": "2026-02-13T10:20:00Z",
  "agents": {
    "Explore": {
      "invocation_count": 15,
      "success_rate": 0.93,
      "avg_duration_sec": 245,
      "last_used": "2026-02-13T10:10:00Z"
    }
  },
  "skills": {
    "commit": {
      "invocation_count": 42,
      "avg_execution_time_sec": 45,
      "last_used": "2026-02-13T10:15:00Z"
    }
  },
  "patterns": {
    "total_sessions": 10,
    "most_used_agent": "Explore",
    "most_used_skill": "commit"
  }
}
```

**사용 예시**:
- 어떤 에이전트가 가장 많이 사용되는지 확인
- 특정 스킬의 평균 실행 시간 추적
- 사용 패턴 변화 모니터링

---

### 3. `agent-evaluations/` - 에이전트 평가 결과

**목적**: 각 에이전트의 문서 품질 평가 결과 저장

**평가 기준**:
- 문서 길이 (최소 30줄 권장)
- 예시 포함 여부 (`<example>` 태그)
- 워크플로우 정의 여부
- YAML Frontmatter 완성도

**파일 구조**:
```
agent-evaluations/
└── explore/
    ├── 2026-02-13T10:00:00Z.json
    └── 2026-02-14T09:00:00Z.json
```

---

### 4. `improvement-suggestions/pending/` - 개선 제안

**목적**: 자동 평가에서 발견된 문제점과 개선 방향을 문서화

**생성 주기**: 세션이 종료될 때(Stop 훅) 자동 생성

**파일 형식**: 마크다운 (Frontmatter 포함)

```markdown
---
type: agent-improvement
target: my-agent
session_id: abc123
created_at: 2026-02-13T10:00:00Z
priority: high
status: pending
---

# 개선 제안: my-agent

## 평가 요약
| 항목 | 값 |
|------|-----|
| 문서 길이 | 20 줄 |
| 예시 포함 | ❌ 없음 |

## 개선 사항
1. 문서가 너무 짧습니다...
```

**워크플로우**:
1. `pending/`에 새 제안 생성
2. 사용자가 검토 후:
   - 적용 완료 → `applied/`로 이동
   - 거부 → `rejected/`로 이동
   - 보류 → 그대로 유지

---

## 데이터 흐름 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                        Claude Code 세션                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ Task 도구    │  │ Subagent     │  │ Stop 훅          │  │
│  │ 사용         │  │ 완료         │  │ (세션 종료)      │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
└─────────┼─────────────────┼───────────────────┼────────────┘
          │                 │                   │
          ▼                 ▼                   ▼
   ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
   │ sessions/    │  │ sessions/    │  │ 1. 세션 로그 분석 │
   │ {id}.jsonl   │  │ {id}.jsonl   │  │ 2. 통계 집계     │
   │ (task_invoke │  │ (subagent_   │  │ 3. 평가 및 제안  │
   │  이벤트 추가) │  │  stop 이벤트) │  │    생성          │
   └──────────────┘  └──────────────┘  └────────┬─────────┘
                                                │
                     ┌──────────────────────────┼──────────┐
                     │                          │          │
                     ▼                          ▼          ▼
            ┌──────────────┐         ┌──────────────┐  ┌──────────────┐
            │ usage-stats. │         │ improvement- │  │ agent-       │
            │ json         │         │ suggestions/ │  │ evaluations/ │
            │ (업데이트)    │         │ pending/     │  │ (평가 결과)   │
            └──────────────┘         └──────────────┘  └──────────────┘
```

---

## 프로젝트별 저장 이유

### 왜 `~/.claude/` 대신 프로젝트의 `.claude/monitoring/`에 저장하는가?

| 저장 위치 | 장점 | 단점 |
|-----------|------|------|
| **프로젝트별** (선택) | • 프로젝트별 독립적인 분석 가능<br>• Git으로 버전 관리 가능<br>• 팀원 간 공유 용이 | • 프로젝트마다 설정 필요 |
| **글로벌** (~/.claude/) | • 모든 프로젝트에서 공유<br>• 한 번 설정으로 끝 | • 프로젝트별 맥락 파악 어려움<br>• 민감한 정보 누적 위험 |

**사용자가 프로젝트별 저장을 선택한 이유**:
1. **맥락 유지**: 특정 프로젝트에서 어떤 에이전트/스킬이 유용했는지 파악
2. **팀 공유**: Git 커밋을 통해 팀원들과 모니터링 데이터 공유 가능
3. **프로젝트별 최적화**: 프로젝트 특성에 맞는 에이전트/스킬 개선

---

## 락 메커니즘

여러 훅이 병렬로 실행될 수 있으므로 파일 기반 락을 사용합니다:

```bash
# 락 획득 (최대 10초 대기)
acquire_lock "usage_stats" 10

# 파일 수정
cat "$STATS_FILE" | jq ... > "$STATS_FILE.tmp"
mv "$STATS_FILE.tmp" "$STATS_FILE"

# 락 해제
release_lock "usage_stats"
```

락 파일 위치: `/tmp/claude-monitoring-locks/`

---

## 데이터 보존 및 정리

### 자동 정리 규칙 (향후 구현 예정)

```bash
# 30일 이상 된 세션 로그 아카이브
find sessions/ -name "*.jsonl" -mtime +30 -exec gzip {} \;

# 90일 이상 된 로그 삭제
find sessions/ -name "*.jsonl.gz" -mtime +90 -delete

# 적용된 개선 제안 아카이브
mv improvement-suggestions/applied/* improvement-suggestions/archived/
```

### 수동 정리 명령어

```bash
# 모든 세션 로그 확인
cat .claude/monitoring/sessions/*.jsonl | jq -s 'group_by(.event) | map({event: .[0].event, count: length})'

# 특정 에이전트 사용량 확인
cat .claude/monitoring/sessions/*.jsonl | jq 'select(.event == "subagent_stop" and .agent_type == "Explore")'

# 통계 재계산
bash .claude/hooks/monitoring/lib/aggregate-stats.sh all
```

---

## 확장 가능성

### 향후 추가 가능한 디렉토리

```
.claude/monitoring/
├── sessions/
├── agent-evaluations/
├── improvement-suggestions/
│   ├── pending/
│   ├── applied/          # 적용 완료된 제안
│   └── rejected/         # 거부된 제안
├── archives/             # 오래된 데이터 아카이브
│   └── sessions-2026-01.tar.gz
├── reports/              # 주간/월간 리포트
│   └── weekly-2026-W06.md
└── custom-metrics/       # 사용자 정의 메트릭
    └── code-quality.json
```

---

## 요약

| 디렉토리/파일 | 목적 | 갱신 주기 | 보관 기간 |
|--------------|------|----------|----------|
| `sessions/*.jsonl` | 상세 이벤트 로그 | 실시간 (훅 실행 시) | 90일 |
| `usage-stats.json` | 집계 통계 | 세션 종료 시 | 영구 |
| `agent-evaluations/` | 품질 평가 | 세션 종료 시 | 영구 |
| `improvement-suggestions/pending/` | 개선 제안 | 세션 종료 시 | 적용/거부 시 이동 |

이 구조를 통해 프로젝트별로 에이전트/스킬 사용 패턴을 추적하고, 지속적인 품질 개선을 자동화할 수 있습니다.

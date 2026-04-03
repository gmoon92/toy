# subagent-creator 스킬 리뷰

> 작성일: 2026-04-04  
> 목적: 과도한 내용 제거 및 모호한 부분 개선을 위한 사전 분석

---

## 현재 구조

```
.claude/skills/subagent-creator/
├── SKILL.md
├── assets/
│   └── subagent-template.md          # 범용 단일 템플릿
└── references/
    ├── available-tools.md
    └── examples.md                    # 6개 에이전트 예시
```

---

## 제거 대상 — Claude가 이미 아는 내용

### SKILL.md

| 위치 | 내용 | 이유 |
|------|------|------|
| `### 시스템 프롬프트 작성 지침` | "역할 명확히 정의", "출력 형식 정의" 등 5개 항목 | 일반 프롬프트 작성 원칙, 서브 에이전트 특화 없음 |
| `### 도구 선택 기준` | 읽기 전용/코드 수정/전체 접근 3가지 조합 | `available-tools.md`와 완전 중복 |
| `## 생성 워크플로우` Step 3 (검증) | "파일 경로 확인", "Read로 파일 확인" | Claude가 당연히 수행하는 것 |
| `## 서브 에이전트 파일 형식` frontmatter 코드 블록 | 전체 필드 구조 재설명 | `assets/subagent-template.md`와 중복 |

### available-tools.md

| 섹션 | 이유 |
|------|------|
| `## 핵심 도구` 표 (Read~Task) | Claude Code 기본 도구, Claude가 이미 앎 |
| `## 상호작용 도구` (AskUserQuestion) | 불필요 |
| `## 웹 도구` (WebFetch, WebSearch) | 불필요 |

유지할 것: **"자주 사용하는 도구 조합"**, **MCP 도구 형식 설명**, **IDE 도구 항목**

---

## 수정 대상 — 모호하거나 실제로 작동 안 하는 부분

### 버그 1: Task 에이전트가 읽을 수 없는 참조 (SKILL.md:68)

현재:
```
RETURN FORMAT: "Task 반환 형식" 섹션 참조.
```

Task 에이전트는 SKILL.md를 볼 수 없음. prompt 외부 섹션을 참조해도 동작하지 않는다.
→ 반환 형식(5줄 이하, 전체 파일 내용 금지)을 **prompt 내에 직접 인라인**으로 작성해야 함.

### 버그 2: Step 1 AskUserQuestion 무조건 실행 (SKILL.md:46)

현재:
```
EXECUTE AskUserQuestion:
- 목적, 이름, 범위, 도구, 모델, 책임
```

사용자가 이미 정보를 제공한 경우에도 질문하는 구조.
→ "**누락된 정보만 질문**" 조건부 실행으로 변경 필요.

---

## 구조 개선 논의 포인트

### 서브 에이전트 = 역할을 가진 행위자

서브 에이전트는 독립된 컨텍스트에서 자율적으로 판단하고 행동하는 행위자.
성향(archetype)에 따라 구조가 달라진다:

| 성향 | 특징 | 권한 |
|------|------|------|
| 검토자 (Reviewer) | 체크리스트 중심, 우선순위 피드백 | 읽기 전용 |
| 해결사 (Fixer) | 단계별 프로세스, 근본 원인 추적 | 읽기+수정 |
| 실행자 (Executor) | 실행→검증 루프 | 읽기+수정+실행 |
| 작성자 (Writer) | 출력 형식 중심, 독자 정의 | 읽기+쓰기 |
| 분석가 (Analyst) | 가설-검증 구조, 인사이트 도출 | 읽기+실행 |
| 감사자 (Auditor) | 읽기 전용 + plan 모드 강제 | 읽기 전용 (plan) |

### 현재 vs 제안 구조

현재:
```
assets/
└── subagent-template.md    # 범용 1개
```

제안:
```
assets/
└── templates/
    ├── reviewer.md          # 비평적 검토자
    ├── fixer.md             # 해결사/디버거
    ├── executor.md          # 실행-검증자
    ├── writer.md            # 작성자
    ├── analyst.md           # 분석가
    └── auditor.md           # 감사자 (읽기 전용 + plan)
```

### examples.md 처리 방안

현재 6개 예시(code-reviewer, debugger, data-scientist, test-runner, doc-writer, security-auditor) 중:
- `data-scientist`: BigQuery 특화 → 일반 패턴 참고에 부적합, 제거 고려
- 나머지 5개: 각 성향 유형을 잘 대표함

성향별 템플릿이 생기면 examples.md의 역할이 중복될 수 있음.
→ examples.md 제거 후 templates/ 로 완전 대체하는 방안 논의 필요.

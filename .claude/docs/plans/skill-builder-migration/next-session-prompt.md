# skill-builder 재설계 — 다음 세션 인수인계

## 상황 요약

`skill-builder` 스킬을 정리하는 작업을 진행 중이다.
Anthropic 공식 `document-skills:skill-creator` 플러그인이 등장하면서 스킬 *생성* 역할이 중복됐고,
노하우가 담긴 핵심 문서들은 이미 `claudecode-analyzer`로 이식 완료됐다.

**skill-builder는 삭제하지 않는다. 재설계가 목적이다.**

---

## 이식 완료된 내용 (claudecode-analyzer/references/)

| 파일 | 내용 |
|------|------|
| `implementation-patterns.md` | 10가지 구현 패턴 (진행적 로딩, 스크립트 추출 등) |
| `strong-directives.md` | MANDATORY/DO NOT/ALWAYS 지시어 참조 |
| `skill-freedom-levels.md` | 지시 자유도 스펙트럼 (High/Medium/Low) |
| `checklists/skill.md` | 위 내용 기반 검증 항목 대폭 강화 |

---

## 남은 작업: skill-builder 재설계

### 내 관점

skill-builder의 고유 가치는 스킬 *생성*이 아니라 다음에 있다:

1. **자유도 스펙트럼** — task fragility에 따라 얼마나 구체적으로 지시할지
2. **Anti-pattern 진단** — "왜 스킬이 제대로 동작하지 않는가" 관점
3. **리팩토링 프로세스** — 레거시 스킬을 현대 패턴으로 전환하는 6단계
4. **토큰 효율성 원리** — 단순 규칙이 아닌 컨텍스트 윈도우 공유 구조 이해

이것들은 `skill-creator`가 커버하지 않는 영역이다.

### 추천 방향

**`skill-refactor` 또는 `skill-reviewer`** 이름으로 재설계.

- 역할: 기존 스킬의 품질 점검 + 리팩토링 가이드
- `claudecode-analyzer`와 차이:
  - analyzer → 문서 형식/구조 검증 (정적)
  - 새 스킬 → 스킬 실행 품질 + 토큰 효율 (동적)

### 재활용할 skill-builder 파일

```
workflows/modifying-skills.md    ← 새 스킬 Workflow의 핵심
workflows/script-extraction.md  ← references 기반 자료
skills/guidelines/documentation-style.md  ← 스타일 가이드
```

---

## 참고 문서 위치

```
.claude/docs/plans/skill-builder-migration/
├── session-work-log.md      # 이번 세션 전체 작업 기록
├── skill-builder-redesign.md # 재설계 방향 A~D 상세
└── pending-tasks.md          # 남은 태스크 및 현황

.claude/.tmp/skill-builder-migration/
├── validator-report.md      # 에이전트 팀 분석 보고서
├── efficiency-report.md
└── pragmatist-report.md
```

---

## 시작 지시

다음 세션에서는 이 문서를 읽은 후:

1. `skill-builder-redesign.md`로 재설계 방향 확인
2. `skill-builder/workflows/modifying-skills.md` 읽기 (새 스킬 Workflow 기반)
3. 새 스킬 방향 결정 (Option A/B/C 중 선택) 후 설계 시작

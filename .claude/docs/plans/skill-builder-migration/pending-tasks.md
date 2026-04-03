# 미완료 태스크 — skill-builder 마이그레이션

> 최종 업데이트: 2026-04-03  
> **중요**: skill-builder 디렉토리는 재설계 목적으로 보존. 제거하지 말 것.

---

## 현재 상태 요약

| 항목 | 상태 |
|------|------|
| claudecode-analyzer 1차 강화 (implementation-patterns, strong-directives) | ✅ 완료 |
| 이식된 원본 파일 2개 제거 | ✅ 완료 |
| 3인 에이전트 팀 분석 및 최종 결정 | ✅ 완료 |
| skill-freedom-levels.md 이식 | ✅ 완료 |
| checklists/skill.md 강화 (자유도, 배포 전 최종 검증) | ✅ 완료 |
| SKILL.md 업데이트 (skill-freedom-levels 참조 추가) | ✅ 완료 |
| **skill-builder 재설계** | 🔲 **미완료** (별도 세션 권장) |
| **Git 커밋** | 🔲 **미완료** |

---

## 완료된 claudecode-analyzer 최종 구조

```
.claude/skills/claudecode-analyzer/
├── SKILL.md (업데이트됨)
└── references/
    ├── guidelines.md (강화됨)
    ├── implementation-patterns.md (신규 ✅)
    ├── strong-directives.md (신규 ✅)
    ├── skill-freedom-levels.md (신규 ✅)
    ├── transformation-patterns.md
    ├── checklists/
    │   ├── skill.md (대폭 강화됨 ✅)
    │   ├── agent.md
    │   ├── claude.md
    │   ├── command.md
    │   ├── rule.md
    │   └── common/
    ├── templates/output.md
    └── prompt/
```

---

## Task: skill-builder 재설계 (새 세션 권장)

### 배경

- Anthropic 공식 `document-skills:skill-creator` 플러그인 등장으로 스킬 생성 역할 중복
- skill-builder의 고유 가치는 **실행 품질 관리 + 리팩토링 노하우**
- 핵심 노하우는 이미 claudecode-analyzer에 이식 완료

### 결정 필요 사항

1. 새 스킬의 방향:
   - **Option A**: `skill-reviewer` — 스킬 실행 품질 검토 (자유도, 토큰 효율, anti-pattern)
   - **Option B**: `skill-refactor` — 레거시 스킬 리팩토링 전문
   - **Option C**: 두 가지 통합

2. claudecode-analyzer와의 역할 분리:
   - analyzer: 문서 형식/구조 검증 (정적)
   - 새 스킬: 스킬 실행 품질 + 토큰 효율 검증 (동적)

### 재활용할 skill-builder 문서들

| 파일 | 활용 방법 |
|------|---------|
| `workflows/modifying-skills.md` | 새 스킬의 Workflow 기반 |
| `workflows/script-extraction.md` | references/로 활용 |
| `skills/guidelines/documentation-style.md` | 스타일 가이드로 활용 |
| `skills/principles/02-appropriate-freedom.md` | ← 이미 claudecode-analyzer에 이식됨 |

### 참고 문서

- `skill-builder-redesign.md` (이 디렉토리 내) — 재설계 방향 A~D 상세
- `.claude/.tmp/skill-builder-migration/validator-report.md`
- `.claude/.tmp/skill-builder-migration/efficiency-report.md`
- `.claude/.tmp/skill-builder-migration/pragmatist-report.md`

---

## Task: Git 커밋

커밋 분리 계획:

```
feat(claudecode-analyzer): skill-builder 노하우 통합 이식

- implementation-patterns.md 신규 (10가지 구현 패턴)
- strong-directives.md 신규 (지시어 키워드 참조)
- skill-freedom-levels.md 신규 (자유도 스펙트럼)
- checklists/skill.md 강화 (토큰 효율, 강한 지시어, 자유도, 배포 전 검증)
- guidelines.md 강화 (Skill 권장사항 추가)
- SKILL.md 업데이트 (새 참조 파일 연결)
```

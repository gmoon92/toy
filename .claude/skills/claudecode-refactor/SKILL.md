---
name: cc:refactor
description: |
  Claude Code 스킬·에이전트의 실행 품질을 진단하고 현대 패턴으로 리팩토링합니다.
  claudecode-analyzer(문서 형식/구조)와 달리, 실행 동작·토큰 효율·자유도 적절성을 검토합니다.

  다음 상황에서 사용합니다:
  - 스킬/에이전트가 일관되게 동작하지 않을 때 ("왜 이게 제대로 안 돼?")
  - 스킬/에이전트가 토큰을 과도하게 소비할 때
  - 레거시 스킬·에이전트를 진행적 로딩/스크립트 추출 패턴으로 현대화할 때
  - 에이전트 서브에이전트 오케스트레이션 품질을 점검할 때

  예시:
  - "commit 스킬을 리팩토링해줘"
  - "blog-reviewer 에이전트 왜 자꾸 이상하게 동작해"
  - "이 에이전트 토큰 최적화해줘"
  - "git-commit 스킬 인라인 코드 스크립트로 추출해줘"
user-invocable: true
argument-hint: [skill-name|agent-name]
allowed-tools: Read, Grep, Glob, Write, Edit, Bash
---

# Claude Code Refactor

Claude Code 스킬·에이전트의 실행 품질을 진단하고 현대 패턴으로 개선합니다.

## claudecode-analyzer와의 역할 분리

| | claudecode-analyzer | cc:refactor |
|---|---|---|
| 검증 대상 | 문서 형식/구조 (정적) | 실행 품질/토큰 효율 (동적) |
| 주요 질문 | "문서가 규격에 맞는가?" | "실제로 잘 동작하는가?" |
| 적용 범위 | Skill, Agent, Rule, Command | **Skill + Agent** |
| 출력 | 형식 오류 리포트 | 리팩토링 계획 + 실행 |

---

## Workflow 1: 품질 진단 (Audit)

**Trigger:** "이 스킬/에이전트 왜 제대로 안 돼?" / "품질 진단해줘"

### Step 1: 대상 유형 식별

```
EXECUTE: Read {target}/SKILL.md 또는 {target}.md (frontmatter 확인)
```

- `name: cc:*` 또는 `.claude/skills/` → **스킬**
- `.claude/agents/` 또는 `subagent_type` 언급 → **에이전트**

### Step 2: 현재 상태 측정

```
EXECUTE: Bash - wc -l {target}/**/*.md
EXECUTE: Grep - pattern "^```(bash|javascript|python)" in {target}/
```

**공통 진단 항목 (스킬 + 에이전트):**

| 신호 | 임계값 | 진단 |
|------|--------|------|
| 메인 문서 줄 수 | 500줄 초과 | 진행적 로딩 필요 |
| 인라인 코드 블록 | 3개 이상 | 스크립트 추출 필요 |
| 약한 표현 | "should", "consider" | 강한 지시어 교체 필요 |
| description 키워드 | 트리거 미포함 | 활성화 문제 가능 |

**에이전트 추가 진단 항목:**

| 신호 | 진단 |
|------|------|
| `subagents` 필드 없이 본문에서 에이전트 호출 | 서브에이전트 오케스트레이션 누락 |
| `condition` 값 부재 | on_complete / on_error 처리 없음 |
| `allowed-tools` 과잉 지정 | 권한 범위 과대 |

### Step 3: 문제 분류

**참조:** `references/anti-patterns.md`

진단 결과를 우선순위별 분류:
- **P1** (동작 불일치): 자유도 설정 문제, 약한 지시어
- **P2** (토큰 낭비): 인라인 코드 블록, 진행적 로딩 없음
- **P3** (활성화 실패): description 키워드 부족, scope 불명확
- **P4** (에이전트 전용): 서브에이전트 오케스트레이션 누락, 도구 권한 과대

진단 보고서를 `${CLAUDE_TMP_DIR}/claudecode-refactor/` 에 저장.

---

## Workflow 2: 리팩토링 실행 (Refactor)

**Trigger:** "리팩토링해줘" / "최적화해줘" / "현대화해줘"

### Step 1: 베이스라인 측정

```bash
# 토큰 대략 추정 (단어 수 × 1.3)
cat {skill-name}/SKILL.md | wc -w
grep -r "^\`\`\`" {skill-name}/ | wc -l
```

### Step 2: 리팩토링 계획 수립

**참조:** `references/refactoring-workflow.md`

우선순위별 적용:
1. **고영향/저노력**: 약한 지시어 → 강한 지시어 교체
2. **고영향/고노력**: 인라인 코드 → 스크립트 추출
3. **중영향**: 대형 SKILL.md → 진행적 로딩 분리

### Step 3: 변경 실행

**패턴 A — 약한 지시어 교체:**

Before:
```markdown
You should run validation before committing.
Consider using the script for this step.
```

After:
```markdown
**MANDATORY:** ALWAYS run validation before proceeding.
**DO NOT** skip this step or inline validation logic.
```

**패턴 B — 스크립트 추출:**

인라인 코드 3개 이상 → `references/script-extraction.md` 참조

**패턴 C — 진행적 로딩:**

Before: 단일 SKILL.md (2000줄)

After:
```markdown
## When to Load References
- **항상**: [core-workflow.md](references/core-workflow.md)
- **조건부**: [advanced.md](references/advanced.md) — 복잡 케이스만
```

### Step 4: 임팩트 측정 및 보고

```bash
echo "Before: $(cat {original} | wc -w) words"
echo "After: $(cat {refactored} | wc -w) words"
echo "Code blocks removed: $(grep -c '^\`\`\`' {before}) → $(grep -c '^\`\`\`' {after})"
```

---

## Workflow 3: 활성화 문제 해결 (Activation Fix)

**Trigger:** "스킬/에이전트가 자동으로 안 켜져" / "description 개선해줘"

### Step 1: 현재 description 분석

```
EXECUTE: Read {skill-name}/SKILL.md (frontmatter only)
```

**확인 항목:**
- [ ] 사용 트리거 키워드 포함 여부
- [ ] "무엇을 + 언제" 구조 여부
- [ ] 구체적 사용 예시 포함 여부

### Step 2: 개선된 description 작성

**참조:** `references/documentation-style.md`

```yaml
# Before
description: Helps with commits

# After
description: |
  git 변경사항을 분석해 컨벤셔널 커밋 메시지를 생성하고 실행합니다.
  스테이징·커밋·메시지 작성이 필요할 때 사용하세요.
  예시: "커밋해줘", "변경사항 커밋 메시지 작성해줘"
```

---

## References

> **IMPORTANT:** 아래 참조는 Task context에서만 로드. 메인 컨텍스트에 직접 로드 금지.

- [anti-patterns.md](references/anti-patterns.md) — "왜 스킬/에이전트가 실패하는가" 진단 가이드 (5가지 패턴 + 에이전트 전용 P4)
- [refactoring-workflow.md](references/refactoring-workflow.md) — 6단계 리팩토링 프로세스 및 체크리스트
- [script-extraction.md](references/script-extraction.md) — 인라인 코드 스크립트 추출 상세 가이드
- [documentation-style.md](references/documentation-style.md) — 문서화 스타일 가이드 (description, 강한 지시어)

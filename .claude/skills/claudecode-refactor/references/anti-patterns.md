# 스킬 실행 실패 패턴 (Anti-patterns)

"왜 스킬이 제대로 동작하지 않는가"를 진단하는 가이드.

---

## 패턴 1: 동작 불일치 (Inconsistent Behavior)

**증상:** 같은 입력에도 매번 다른 결과 / 중요한 단계가 가끔 건너뛰어짐

**원인:**
- 핵심 실행 지시에 약한 표현 사용 ("you should", "consider", "try to")
- 자유도가 너무 높음 (고위험 작업에 원칙만 제공)
- MANDATORY/DO NOT 지시어 없음

**진단 방법:**
```bash
# 약한 표현 탐지
grep -n "you should\|consider\|try to\|it's better\|please" {skill}/SKILL.md
```

**수정:**
- 필수 동작: `MANDATORY:` / `ALWAYS` 지시어로 강화
- 금지 동작: `DO NOT` / `NEVER` 추가
- 데이터 변경/삭제/시스템 작업은 저자유도(Low Freedom) 적용

---

## 패턴 2: 토큰 낭비 (Token Bloat)

**증상:** 실행 속도 느림 / 컨텍스트 한계 도달 / 간단한 작업에도 heavy 응답

**원인:**
- SKILL.md에 인라인 코드 블록 다수 포함
- 모든 참조를 처음부터 로드
- 진행적 로딩 없음 (조건부 참조 없음)

**진단 방법:**
```bash
# 파일 크기 확인
wc -l {skill}/**/*.md | sort -rn

# 인라인 코드 블록 수
grep -r "^\`\`\`" {skill}/ | wc -l
```

**진단 기준:**
- SKILL.md 500줄 초과 → 진행적 로딩 필요
- 인라인 코드 블록 3개 이상 → 스크립트 추출 필요
- `When to Load References` 섹션 없음 → 항상 전체 로드 중

**수정:**
- 참조 문서를 `references/`로 분리
- `When to Load References` 섹션 추가 (조건부 로딩)
- 인라인 코드 → `scripts/` 추출 후 `EXECUTE_SCRIPT:` 지시어 사용

---

## 패턴 3: 스크립트 재작성 (Script Reconstruction)

**증상:** Claude가 스크립트를 사용하지 않고 직접 인라인 코드를 작성

**원인:**
- `EXECUTE_SCRIPT:` 마커 없음
- 인라인 코드 예시가 문서에 남아있음 (Claude가 참고해 재작성)
- 금지 표현 없음

**진단 방법:**
```bash
# EXECUTE_SCRIPT 마커 확인
grep -r "EXECUTE_SCRIPT" {skill}/SKILL.md

# 금지 표현 확인
grep -n "DO NOT inline\|NEVER reconstruct\|NEVER inline" {skill}/SKILL.md
```

**수정:**
```markdown
# Before
스크립트를 실행하세요.

# After
**MANDATORY: ALWAYS use pre-built script (DO NOT inline)**
```bash
# EXECUTE_SCRIPT: scripts/validate.sh
./scripts/validate.sh < input.json
```
**NEVER reconstruct commands from documentation**
```

---

## 패턴 4: 활성화 실패 (Activation Failure)

**증상:** 관련 작업인데 스킬이 자동으로 활성화되지 않음 / 사용자가 명시적으로 스킬 이름을 불러야 함

**원인:**
- `description`에 사용자가 말할 법한 키워드 없음
- "무엇을 + 언제 사용" 구조 없음
- 구체적 예시 없음

**진단 방법:**
- 사용자가 실제로 쓰는 표현을 description에서 검색
- "Use when", "예시:" 섹션 존재 여부 확인

**수정:**
```yaml
# Before
description: Helps with commits

# After
description: |
  git 변경사항을 분석해 컨벤셔널 커밋 메시지를 생성하고 실행합니다.
  스테이징·커밋·메시지 작성이 필요할 때 사용하세요.
  예시: "커밋해줘", "변경사항 커밋 메시지 작성해줘", "git commit"
```

---

## 패턴 5: 과도한 경직성 (Over-specification)

**증상:** 창의적/분석 작업에서 결과가 획일적 / 엣지 케이스에서 실패

**원인:**
- 창의적 작업에 저자유도(Low Freedom) 적용
- 불필요한 스크립트 의존 (간단한 원칙으로 충분한 곳에)
- 모든 단계에 MANDATORY 남용

**진단 방법:**
- 코드 리뷰, 분석, 문서 생성 등 창의 작업에 MANDATORY 패턴 체크
- 스크립트가 실제로 필요한가? (단순 bash 명령은 스크립트화 불필요)

**수정:**
- 창의적 작업 → 원칙/가이드라인 제공 (고자유도)
- 위험한 작업만 저자유도 적용 (데이터 변경, 시스템 조작)
- MANDATORY 남용 시 효과 감소 — 진짜 중요한 곳에만 사용

---

## 자유도 매트릭스 참조

| Task 특성 | 적합한 자유도 | 지시 유형 |
|:---------|:------------|:---------|
| 창의적/분석 작업 | 고 (High) | 원칙, 가이드라인 |
| 설정값 기반 작업 | 중간 (Medium) | 파라미터화된 템플릿 |
| 데이터 변경/삭제 | 저 (Low) | MANDATORY + 정확한 스크립트 |
| 정확한 순서 필요 | 저 (Low) | EXECUTE_SCRIPT 패턴 |

---

**참조**: [refactoring-workflow.md](refactoring-workflow.md) | [script-extraction.md](script-extraction.md)

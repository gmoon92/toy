# skill-builder 재설계 방향

> 작성일: 2026-04-03  
> 배경: Anthropic 공식 `document-skills:skill-creator` 플러그인 등장으로 skill-builder의 역할이 중복됨.
> 핵심 질문: skill-builder가 남겨야 할 고유한 가치는 무엇인가?

---

## 왜 아쉬운가 — 사라지는 것들

skill-builder에는 단순한 스킬 생성 도구 이상의 것들이 있었음.

### 1. "왜"까지 파악한 토큰 효율성

단순히 "500줄 이하로 써라"가 아니라, **컨텍스트 윈도우가 어떻게 공유되는지**, SKILL.md가 언제 로드되는지, 로드 후 토큰이 어디서 경쟁하는지를 설명하는 원리 기반 문서가 있었음.

### 2. 현장 감각이 담긴 강한 지시어 체계

MANDATORY/DO NOT/ALWAYS 키워드를 **언제 써야 하고 언제 쓰면 안 되는지** — 남용 시 효과가 감소한다는 경험적 판단이 담겨 있었음.

### 3. 자유도 스펙트럼 (Appropriate Freedom)

공식 문서에는 없는 개념. 스킬이 다루는 task의 "fragility"에 따라:
- **고자유도**: 창의적 판단이 필요한 작업
- **중간 자유도**: 매개변수화된 패턴
- **저자유도**: 정확한 스크립트 필요 (예: git 커밋, 파일 변환)

스킬 작성자가 "얼마나 구체적으로 지시해야 하는가"를 결정하는 기준.

### 4. Anti-pattern 기반 진단

단순 체크리스트가 아니라 "왜 스킬이 제대로 동작하지 않는가"를 진단하는 관점:
- "Skill not triggering" → description에 key terms 부족
- "Inconsistent behavior" → 자유도 미지정
- "Token heavy" → reference split 없음

---

## skill-creator와의 차이

| 항목 | document-skills:skill-creator | skill-builder (현재) |
|------|-------------------------------|---------------------|
| 목적 | 스킬 생성 및 최적화 | 스킬 생성 + 품질 관리 + 리팩토링 |
| 접근 방식 | 공식 표준 기반 | 현장 경험 기반 |
| 자유도 개념 | 불명확 | 명시적 스펙트럼 정의 |
| 토큰 최적화 | 기본 수준 | 원리 기반 심화 |
| Anti-pattern | 없음(추정) | 진단 가이드 포함 |

---

## 재설계 방향 제안

### 방향 A: 스킬 품질 심층 리뷰어

> "스킬 생성은 skill-creator에게, 품질 보증은 우리에게"

- **역할**: 작성된 스킬의 품질을 다각도로 분석
- **claudecode-analyzer와 차이**: analyzer는 문서 형식/구조 검증, 이건 실행 품질 검증
  - "이 스킬이 실제로 잘 동작하는가?"
  - "토큰 비용이 적정한가?"
  - "자유도 설정이 적절한가?"
- **핵심 문서**: 02-appropriate-freedom.md, 01-conciseness.md, anti-pattern 진단

### 방향 B: 스킬 마이그레이션 & 리팩토링 전문 도구

> "레거시 스킬을 현대화"

- **역할**: 오래된 스킬을 최신 패턴(진행적 로딩, 스크립트 추출 등)으로 자동 변환
- **핵심 기능**:
  - 인라인 코드 감지 → 스크립트 추출 제안
  - 약한 지시어 감지 → 강한 지시어 대체 제안
  - 500줄 초과 감지 → reference 분리 제안
- **기반 문서**: implementation-patterns.md, strong-directives.md, script-extraction.md

### 방향 C: 도메인별 스킬 패턴 라이브러리

> "특정 목적 스킬을 빠르게 만드는 패턴 모음"

- **역할**: 유형별 스킬 패턴 제공 (git workflow, API integration, file processing 등)
- **단점**: 유지보수 부담이 크고, skill-creator와 직접 경쟁

### 방향 D: 토큰 최적화 전문 감사 도구

> "당신의 스킬이 낭비하는 토큰을 찾아드립니다"

- **역할**: 스킬의 토큰 비용 분석 및 최적화 제안
- **기반 문서**: 01-conciseness.md, implementation-patterns.md (Pattern 2, 3, 4)

---

## 권장 방향

**방향 A + B 조합이 가장 현실적.**

- skill-creator가 커버하지 않는 **실행 품질 검증** 영역
- 이미 이식한 문서들(implementation-patterns, strong-directives)이 기반이 됨
- 기존 노하우(적절한 자유도, 토큰 효율성, anti-pattern 진단)를 가장 잘 살릴 수 있음

### 새 스킬명 제안

- `skill-reviewer` — 스킬 실행 품질 검토
- `skill-optimizer` — 토큰 효율 + 품질 통합 최적화
- `skill-refactor` — 레거시 스킬 현대화

---

## 재설계 시 보존해야 할 문서들

skill-builder에서 제거되기 전 보존 가치 있는 원본:

| 파일 | 보존 이유 | 위치 |
|------|---------|------|
| `skills/principles/02-appropriate-freedom.md` | 자유도 스펙트럼 — 공식 문서에 없는 핵심 개념 | → `claudecode-analyzer/references/skill-freedom-levels.md` (이식 예정) |
| `workflows/modifying-skills.md` | 리팩토링 6단계 프로세스 — 재설계 시 핵심 기반 | 재설계된 스킬의 Workflow로 활용 |
| `workflows/script-extraction.md` | 스크립트 추출 상세 가이드 | 재설계된 스킬의 references로 활용 |
| `skills/guidelines/documentation-style.md` | 문서화 스타일 완전판 — 490줄의 Best Practice | 재설계된 스킬의 가이드라인으로 활용 |

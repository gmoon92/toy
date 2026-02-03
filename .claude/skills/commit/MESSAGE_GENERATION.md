# Commit Message Generation Strategy

메시지 생성 알고리즘을 규약화하여 다른 클라이언트에서도 일관된 템플릿을 제공합니다.

---

## Overview

각 그룹(독립적인 변경사항)마다 **5개의 커밋 메시지**를 생성합니다.

- **Message 1**: 추천 메시지 (최적의 scope + 명확한 표현)
- **Message 2**: Scope 변형
- **Message 3**: Message 표현 변형
- **Message 4**: Body 상세도 조정
- **Message 5**: Type 대안 제시

사용자는 5개 중 선택하거나, "Other" 옵션으로 직접 입력 가능.

---

## 생성 전략

### Message 1: 추천 메시지 (Optimal)

**목표:** 가장 논리적이고 명확한 메시지

**Scope 선택:**
1. **모듈명 우선**: 여러 관련 파일이 변경된 경우
   - 예: `spring-batch`, `spring-security-jwt`, `commit-skill`
2. **파일명 사용**: 단일 파일 변경인 경우
   - 예: `DateUtils.java`, `README.md`, `build.gradle`

**Message 작성:**
- 변경 목적을 명확히 표현
- 동사 + 목적어 구조
- 한글 또는 영어 모두 가능

**Body 작성:**
- 5개 이상 파일 변경 시 추가
- 주요 변경사항을 `-` 불릿으로 나열
- 5줄 이하로 제한

**예시:**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
- EXAMPLES.md: 실제 사용 예시
- TROUBLESHOOTING.md: 문제 해결 가이드
```

**Scoring 기준:**
- Scope 정확도: 40점 (주요 디렉토리와 매칭)
- Type 정확도: 30점 (변경 성격과 매칭)
- Body 완성도: 20점 (정보성)
- 상세도 적절성: 10점 (변경 크기와 매칭)

---

### Message 2: Scope 변형

**목표:** Scope를 다른 레벨로 변경

**전략:**
1. Message 1이 모듈명이면 → 파일명으로 변경
2. Message 1이 파일명이면 → 상위 모듈명으로 변경

**예시:**

**Message 1:**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가
```

**Message 2 (파일명으로 변경):**
```
docs(SKILL.md): 커밋 메시지 자동 생성 스킬 추가
```

**또는 (상위 디렉토리로 변경):**
```
docs(.claude): 커밋 메시지 자동 생성 스킬 추가
```

**언제 유용한가:**
- 리뷰어가 특정 파일에 집중하고 싶을 때
- 더 넓은 컨텍스트로 변경을 이해하고 싶을 때

---

### Message 3: Message 표현 변형

**목표:** 헤더 메시지 표현을 다르게

**전략:**
1. **간결 버전**: 핵심만 남김
2. **상세 버전**: 더 많은 컨텍스트 추가
3. **다른 표현**: 동의어, 다른 관점

**예시:**

**Message 1:**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가
```

**Message 3 옵션 1 (간결):**
```
docs(commit-skill): 커밋 스킬 문서 추가
```

**Message 3 옵션 2 (상세):**
```
docs(commit-skill): Git 커밋 자동화 스킬 문서 및 규칙 추가
```

**Message 3 옵션 3 (다른 관점):**
```
docs(commit-skill): 자동 커밋 메시지 생성 프로세스 정의
```

---

### Message 4: Body 상세도 조정

**목표:** Body 유무 또는 상세도 변경

**전략:**
1. Body 있으면 → 제거 (헤더만)
2. Body 없으면 → 추가 (상세 설명)
3. Body 있으면 → 더 간결하게 또는 더 상세하게

**예시:**

**Message 1 (Body 있음):**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
- EXAMPLES.md: 실제 사용 예시
- TROUBLESHOOTING.md: 문제 해결 가이드
```

**Message 4 옵션 1 (Body 제거):**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가
```

**Message 4 옵션 2 (Body 간결):**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- 스킬 실행 프로세스 및 규칙 문서화
```

**Message 4 옵션 3 (Body 상세):**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 개요 및 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙 및 검증 규칙
- EXAMPLES.md: 타입별 실제 사용 예시
- TROUBLESHOOTING.md: Git hook 실패 시 문제 해결 가이드
- PROCESS.md: 5단계 실행 프로세스 상세 설명
```

---

### Message 5: Type 대안 제시

**목표:** 다른 타입으로 해석 가능한 경우 대안 제시

**전략:**
변경사항을 다른 관점에서 해석:
- `docs` ↔ `feat` (문서인가 기능인가)
- `feat` ↔ `refactor` (새 기능인가 개선인가)
- `chore` ↔ `feat` (설정인가 기능인가)
- `style` ↔ `refactor` (포맷팅인가 구조 개선인가)

**예시:**

**Message 1 (docs):**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가
```

**Message 5 옵션 1 (feat로 해석):**
```
feat(commit-skill): 자동 커밋 메시지 생성 기능 구현

- 스킬 프로세스 자동화
- 메시지 형식 검증
- Tidy First 원칙 적용
```
(새로운 기능으로 볼 수도 있음)

**Message 5 옵션 2 (chore로 해석):**
```
chore(.claude): commit 스킬 설정 추가
```
(스킬 설정 파일로 볼 수도 있음)

**언제 유용한가:**
- 변경사항의 성격이 애매할 때
- 팀 컨벤션에 따라 다르게 해석 가능할 때

---

## 실제 적용 예시

### 시나리오: 4개 파일 추가 (.claude/skills/commit/)

**분석:**
- 파일: SKILL.md, RULES.md, EXAMPLES.md, TROUBLESHOOTING.md
- 타입: docs (문서 추가)
- Scope: commit-skill (모듈명) 또는 SKILL.md (주요 파일)
- 줄 수: 500+ 줄 추가

**생성된 5개 메시지:**

```
1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가 (추천)

   - SKILL.md: 스킬 실행 프로세스 정의
   - RULES.md: 커밋 메시지 형식 규칙
   - EXAMPLES.md: 실제 사용 예시
   - TROUBLESHOOTING.md: 문제 해결 가이드

2. docs(SKILL.md): 커밋 메시지 자동 생성 스킬 추가

   - SKILL.md: 스킬 실행 프로세스 정의
   - RULES.md: 커밋 메시지 형식 규칙
   - EXAMPLES.md: 실제 사용 예시
   - TROUBLESHOOTING.md: 문제 해결 가이드

3. docs(commit-skill): 커밋 스킬 문서 추가

   - 커밋 자동화 스킬 문서
   - 메시지 형식 규칙 정의

4. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

5. feat(commit-skill): 자동 커밋 메시지 생성 기능 구현

   - 스킬 프로세스 자동화
   - 메시지 형식 검증
   - Tidy First 원칙 적용
```

---

## Scope 추출 알고리즘

### 모듈명 추출

**입력:** 파일 경로 리스트
```
.claude/skills/commit/SKILL.md
.claude/skills/commit/RULES.md
.claude/skills/commit/EXAMPLES.md
```

**알고리즘:**
1. 공통 디렉토리 추출: `.claude/skills/commit/`
2. 마지막 디렉토리명을 모듈명으로 사용: `commit`
3. 컨텍스트 추가 (필요시): `commit-skill`

**예시:**
- `spring-batch/src/main/java/...` → `spring-batch`
- `ai/docs/claude/...` → `claude-api` (컨텍스트 추가)
- `.claude/agents/korean-translator/...` → `korean-translator`

### 파일명 추출

**입력:** 변경된 파일이 1개인 경우
```
src/main/java/com/example/utils/DateUtils.java
```

**알고리즘:**
1. 파일명만 추출: `DateUtils.java`
2. 또는 전체 경로 사용: `utils/DateUtils.java`

**예시:**
- 단일 파일 변경: `DateUtils.java`
- README 변경: `README.md`
- 설정 파일: `application.yml`

---

## Message 생성 패턴

### 동사 선택

| Type | 선호 동사 (한글) | 선호 동사 (영문) |
|------|------------------|------------------|
| feat | 추가, 구현, 도입 | add, implement, introduce |
| fix | 수정, 해결 | fix, resolve, correct |
| refactor | 개선, 추출, 분리 | improve, extract, separate |
| test | 추가, 개선 | add, improve |
| docs | 추가, 수정, 개선 | add, update, improve |
| style | 정리, 적용 | format, apply |
| chore | 업데이트, 추가, 변경 | update, add, change |

### 목적어 구조

**패턴:** `동사 + 목적어`

**좋은 예시:**
- `JWT 인증 필터 추가`
- `배치 재시도 로직 구현`
- `변수명 명확화`
- `테스트 커버리지 개선`

**나쁜 예시:**
- `추가` (목적어 없음)
- `코드 수정` (너무 모호)
- `버그 수정` (어떤 버그?)

---

## Body 생성 기준

### 추가 조건

다음 중 하나라도 해당하면 Body 추가:
1. **파일 5개 이상** 변경
2. **100줄 이상** 변경
3. **복잡한 로직** 변경 (판단 필요)
4. **여러 관련 변경** (컨텍스트 필요)

### 형식

```
- 주요 변경사항 1
- 주요 변경사항 2
- 주요 변경사항 3
- ...
```

**규칙:**
- 각 줄은 간결하게 (1-2줄)
- 5줄 이하로 제한
- 파일별 또는 기능별로 그룹화
- "why"보다 "what"에 집중

### 스타일 옵션

**스타일 1: 파일별 나열**
```
- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
- EXAMPLES.md: 실제 사용 예시
```

**스타일 2: 기능별 그룹**
```
- 스킬 프로세스 자동화
- 메시지 형식 검증
- Tidy First 원칙 적용
```

**스타일 3: 계층적 구조**
```
- JWT 인증 구현:
  - 토큰 생성 및 검증 로직
  - SecurityConfig 통합
```

---

## 메타데이터 JSON 구조

### suggestedMessages 스키마

```json
{
  "groups": [
    {
      "id": 1,
      "directory": ".claude/skills/commit/",
      "fileCount": 4,
      "scope": "commit-skill",
      "type": "docs",
      "suggestedMessages": [
        {
          "rank": 1,
          "strategy": "optimal",
          "header": "docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가",
          "body": "- SKILL.md: 스킬 실행 프로세스 정의\n- RULES.md: 커밋 메시지 형식 규칙\n- EXAMPLES.md: 실제 사용 예시\n- TROUBLESHOOTING.md: 문제 해결 가이드",
          "footer": null,
          "reasoning": "주요 디렉토리 모듈명 사용, 명확한 목적 표현, 파일별 상세 설명"
        },
        {
          "rank": 2,
          "strategy": "scope-variation",
          "header": "docs(SKILL.md): 커밋 메시지 자동 생성 스킬 추가",
          "body": "- SKILL.md: 스킬 실행 프로세스 정의\n- RULES.md: 커밋 메시지 형식 규칙\n- EXAMPLES.md: 실제 사용 예시\n- TROUBLESHOOTING.md: 문제 해결 가이드",
          "footer": null,
          "reasoning": "파일명으로 scope 변경"
        },
        {
          "rank": 3,
          "strategy": "message-variation",
          "header": "docs(commit-skill): 커밋 스킬 문서 추가",
          "body": "- 커밋 자동화 스킬 문서\n- 메시지 형식 규칙 정의",
          "footer": null,
          "reasoning": "더 간결한 표현, body 요약"
        },
        {
          "rank": 4,
          "strategy": "body-variation",
          "header": "docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가",
          "body": null,
          "footer": null,
          "reasoning": "헤더만 사용 (body 제거)"
        },
        {
          "rank": 5,
          "strategy": "type-alternative",
          "header": "feat(commit-skill): 자동 커밋 메시지 생성 기능 구현",
          "body": "- 스킬 프로세스 자동화\n- 메시지 형식 검증\n- Tidy First 원칙 적용",
          "footer": null,
          "reasoning": "docs 대신 feat로 해석 (새로운 기능 관점)"
        }
      ]
    }
  ]
}
```

### 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `rank` | number | 순위 (1=추천) |
| `strategy` | string | 생성 전략 (`optimal`, `scope-variation`, `message-variation`, `body-variation`, `type-alternative`) |
| `header` | string | 커밋 헤더 (`<type>(scope): <message>`) |
| `body` | string \| null | 커밋 본문 (없으면 null) |
| `footer` | string \| null | 커밋 푸터 (없으면 null) |
| `reasoning` | string | 이 메시지를 생성한 이유 (디버깅/감사용) |

---

## 다른 클라이언트 구현 가이드

### 최소 요구사항

1. **Scope 추출** 구현
   - 파일 경로 → 모듈명 추출
   - 단일 파일 → 파일명 추출

2. **5개 메시지 생성** 구현
   - 전략 1-5 적용
   - JSON 스키마 준수

3. **Body 생성 조건** 구현
   - 파일 5개 이상 또는 100줄 이상

### 선택 구현

1. **Scoring 알고리즘**
   - 최적 메시지 자동 선택

2. **컨텍스트 추가**
   - 프로젝트별 컨벤션 적용
   - 디렉토리 구조 학습

3. **사용자 선호도 학습**
   - 과거 선택 패턴 분석
   - 개인화된 추천

---

## 검증 체크리스트

생성된 메시지가 다음을 만족하는지 확인:

- [ ] 형식: `<type>(scope): <message>` 패턴 준수
- [ ] Type: 7가지 중 하나 (feat, fix, refactor, test, docs, style, chore)
- [ ] Scope: 영숫자 + `.`, `-`, `_`만 포함
- [ ] Message: 소문자 시작, 마침표 없음
- [ ] Body: 있으면 빈 줄로 구분, 5줄 이하
- [ ] Footer: 있으면 빈 줄로 구분
- [ ] 공백 블록: 최대 2개 (header-body, body-footer)

정규식 검증:
```regex
^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$
```

---

## Related Documents

- [RULES.md](RULES.md) - Commit message format rules
- [EXAMPLES.md](EXAMPLES.md) - Commit message examples
- [METADATA.md](METADATA.md) - Session metadata structure
- [UI_TEMPLATES.md](UI_TEMPLATES.md) - User option templates
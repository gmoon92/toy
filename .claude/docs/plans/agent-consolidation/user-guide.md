# 통합 에이전트 사용 가이드

**버전:** 2.0
**대상:** docs/blog 에이전트 통합 프로젝트 사용자

---

## 개요

이 가이드는 통합된 3개 에이전트(reviewer, writer, translator)와 docs 스킬의 사용 방법을 설명합니다.

---

## 시작하기

### 필수 조건

1. Claude Code 프로젝트 설정 완료
2. `settings.json`에 언어 설정:
   ```json
   {
     "language": "korean"
   }
   ```

### 스킬 호출

```bash
/docs [command] [options]
```

---

## 사용 시나리오

### 1. 문서 검증

#### 기본 검증 (standard 모드)

```bash
/docs review docs/my-post.md
```

**출력:**
- `${CLAUDE_TMP_DIR}/review_result.md`

**내용:**
```markdown
## 기본 품질 검증 (standard)

### 명확성
- [x] 목적이 명확히 명시됨
- [ ] 핵심 메시지 강화 필요 - 1.2절: 요약 추가 제안
```

#### 기술 문서 검증

```bash
/docs review docs/api.md --mode technical
```

**검증 항목:**
- 코드 예제 문법
- 용어 정의 및 일관성
- 버전 정보
- 환경 설정

#### 종합 검증

```bash
/docs review docs/guide.md --mode comprehensive
```

**실행:**
1. standard (기본 품질)
2. technical (기술 정확성)
3. critical (공격적 검증)
4. reader (독자 관점)
5. structure (구조 분석)

#### 한국어 문서 검증

```bash
/docs korean-doc docs/korean-guide.md
```

**특수 검증:**
- 높임법 일관성 (해요체/합니다체)
- 1인칭 지칭 (저/나)
- 한국어 용어 일관성

---

### 2. 초안 작성

#### 새 문서 작성

```bash
/docs write "REST API 가이드 작성" --target docs/api/guide.md
```

**프로세스:**
1. 요구사항 분석
2. 구조 설계
3. 초안 작성
4. `${CLAUDE_TMP_DIR}/draft.md`에 저장

#### 작성 + 검증 통합

```bash
/docs write-validate "OAuth 인증 문서" --target docs/auth/oauth.md
```

**프로세스:**
1. 초안 작성
2. 검증
3. 개선 필요시 개선
4. 재검증 (최대 3회)

---

### 3. 피드백 기반 개선

#### 검증 후 개선

```bash
# 1. 검증
/docs review docs/draft.md --mode standard

# 2. 개선
/docs improve docs/draft.md --feedback ${CLAUDE_TMP_DIR}/review_result.md
```

**또는 워크플로우 사용:**
```bash
/docs write-validate docs/draft.md
```

---

### 4. 번역

#### 영문 문서 번역

```bash
/docs translate https://docs.example.com/guide --output docs/ko/guide.md
```

**설정:**
- `settings.json` `language: korean` 필요
- 출력: `ai/docs/claude/docs/01-section-01-title.md`

**자동 생성:**
- 파일명: `01-section-01-subject.md`
- 원문 출처: 헤더에 자동 표시

---

## 에이전트별 상세 사용법

### Reviewer (검증)

#### 지원 모드

| 모드 | 용도 | 예시 |
|-----|------|------|
| `standard` | 일반 품질 검증 | 블로그 포스트, 가이드 |
| `technical` | 기술 문서 검증 | API 문서, 코드 예제 |
| `critical` | 논리적 타당성 | 아키텍처 문서, 설계서 |
| `reader` | 독자 관점 | 튜토리얼, 입문 가이드 |
| `structure` | 구조 분석 | 긴 문서, 복잡한 가이드 |
| `comprehensive` | 종합 검증 | 중요한 출판 문서 |

#### 출력 해석

```markdown
- [x] 통과 항목
- [ ] CRITICAL: 반드시 수정 (오류/왜곡)
- [ ] WARNING: 수정 권장 (개선 여지)
- [ ] INFO: 참고 사항 (선택적)
```

### Writer (작성)

#### 모드

| 모드 | 설명 |
|-----|------|
| `draft` | 새 초안 작성 |
| `revise` | 피드백 기반 개선 |

#### 원칙

- 원본 파일은 수정되지 않음
- 모든 작업은 `${CLAUDE_TMP_DIR}`에서 수행
- 변경 이력은 `writer_workspace/`에 저장

### Translator (번역)

#### 요구사항

- `settings.json`에 `language: korean` 설정
- 원문 URL 또는 파일 경로

#### 출력 규칙

- 파일명: 숫자 접두사 + kebab-case
- 위치: `ai/docs/claude/docs/`
- 원문 출처: 문서 헤더에 표시

---

## 워크플로우

### 1. Blog Review (블로그 검증)

```bash
/docs blog-review docs/post.md
```

**순서:**
1. technical 검증
2. critical 검증
3. reader 검증

### 2. Korean Doc (한국어 문서)

```bash
/docs korean-doc docs/guide.md
```

**특징:**
- comprehensive 모드 실행
- 한국어 언어 검증 포함

### 3. Write-Validate (작성-검증)

```bash
/docs write-validate "API 문서" --target docs/api.md
```

**루프:**
1. 작성
2. 검증
3. 개선 필요? → 개선 → 2로
4. 완료

### 4. Quick Check (빠른 확인)

```bash
/docs quick-check docs/draft.md
```

**특징:**
- 60초 타임아웃
- 핵심 이슈만 표시

---

## 결과 파일 위치

```
${CLAUDE_TMP_DIR}/
├── review_result.md          # 검증 결과
├── draft.md                  # 작성/개선된 초안
└── writer_workspace/         # 작성 작업 파일
```

---

## 팁과 요령

### 1. 모드 선택 가이드

- **일반 문서**: `standard`
- **코드 포함**: `technical`
- **중요 결정사항**: `critical`
- **입문자 대상**: `reader`
- **긴 문서**: `structure`
- **출판 전**: `comprehensive`

### 2. 피드백 우선순위

1. CRITICAL: 즉시 수정
2. WARNING: 가능한 수정
3. INFO: 선택적 반영

### 3. 한국어 문서 작성

- `settings.json`에 `language: korean` 설정
- reviewer가 자동으로 언어 검증
- 높임법 일관성 확인

---

## 문제 해결

### 자주 묻는 질문

**Q: 모드를 지정하지 않으면?**
A: `standard` 모드로 기본 검증 수행

**Q: 원본 파일이 수정되나요?**
A: 아니요. 모든 작업은 `${CLAUDE_TMP_DIR}`에서 수행됩니다.

**Q: 한국어 검증은 어떻게 활성화하나요?**
A: `settings.json`에 `"language": "korean"` 추가

**Q: 여러 모드를 동시에 실행할 수 있나요?**
A: `comprehensive` 모드로 모든 모드 실행 가능

---

## 참고 자료

- **스킬 문서:** `.claude/skills/docs/SKILL.md`
- **구현 노트:** `.claude/docs/plans/IMPLEMENTATION.md`
- **변경이력:** `.claude/docs/plans/CHANGELOG.md`

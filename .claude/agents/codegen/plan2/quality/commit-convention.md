# 커밋 컨벤션 (Commit Convention)

## 개요

본 문서는 코드 자동화 에이전트 팀의 커밋 메시지 규칙을 정의합니다. 일관된 커밋 히스토리는 코드 리뷰, 변경 추적, 자동화된 릴리즈 노트 생성에 필수적입니다.

---

## 1. 커밋 메시지 구조

```
<type>: <subject>

<body>

<footer>
```

### 1.1 Type (필수)

| 타입 | 설명 | 사용 예시 |
|------|------|-----------|
| `structural` | 코드 구조 변경 | 모듈 분리, 파일 이동, 의존성 추가 |
| `behavioral` | 동작 변경 | 기능 추가, 버그 수정, 로직 변경 |
| `docs` | 문서 변경 | README, 주석, ADR 작성/수정 |
| `test` | 테스트 변경 | 테스트 추가, 수정, 삭제 |
| `perf` | 성능 개선 | 최적화, 알고리즘 개선 |

### 1.2 Subject (필수)

- 명령문으로 작성 (현재 시제, 동사로 시작)
- 첫 글자 소문자
- 끝에 마침표 없음
- 50자 이내 권장

```
✅ behavioral: add user authentication flow
✅ structural: extract http client to separate module
✅ docs: document error handling strategy

❌ behavioral: Added user authentication flow  (과거 시제)
❌ structural: Extract http client module.      (마침표 있음)
❌ docs: Documentation update                    (명사로 시작)
```

### 1.3 Body (선택)

- Subject보다 상세한 설명
- 변경 이유(why) 설명
- 여러 단락으로 구성 가능
- 각 줄 72자 이내

```
behavioral: add retry mechanism for network requests

Network failures are common in distributed systems. This change
adds exponential backoff retry to improve reliability.

The retry policy is configurable via the RetryConfig struct.
Default settings:
- max retries: 3
- base delay: 100ms
- max delay: 10s
```

### 1.4 Footer (선택)

- Breaking changes 표시
- 관련 이슈 참조

```
BREAKING CHANGE: The HttpClient::new() signature has changed.
It now requires a RetryConfig parameter instead of using
hardcoded defaults.

Refs: #123, #456
```

---

## 2. 커밋 크기 가이드라인

### 2.1 라인 수 기준

| 범주 | 라인 수 | 설명 |
|------|---------|------|
| **목표** | 50-100줄 | 리뷰하기 쉬운 크기 |
| **최대** | 200줄 | 예외적으로 허용 |
| **분할 필요** | 200줄 초과 | 반드시 분할 |

### 2.2 변경 유형별 권장 크기

```
structural: 20-80줄
- 파일 이동/재구성은 라인 수가 많을 수 있음
- 논리적 단위로 분할

behavioral: 30-100줄
- 하나의 기능/버그 수정당 하나의 커밋
- 리팩토링과 동작 변경은 분리

docs: 10-50줄
- 문서 단위로 커밋
- 코드 변경과 문서 변경은 분리

test: 20-100줄
- 테스트 파일 단위로 커밋
- 테스트 유틸리티는 별도 커밋

perf: 10-80줄
- 최적화 전/후 벤치마크 포함
- 변경 사항을 집중적으로 유지
```

### 2.3 커밋 분할 예시

```
# ❌ 잘못된 예시 (250줄, 혼합된 변경)
behavioral: implement user service
(인증 + 프로필 + 설정 + 테스트 모두 한 커밋)

# ✅ 올바른 예시 (분할된 변경)
structural: create user module structure
(20줄 - 모듈 파일 생성)

behavioral: implement user authentication
(80줄 - 로그인/로그아웃 기능)

behavioral: implement user profile management
(60줄 - 프로필 CRUD)

test: add user service tests
(90줄 - 단위 테스트)
```

---

## 3. 커밋 작성 프로세스

### 3.1 작성 전 체크리스트

```
□ 변경 사항을 논리적 단위로 그룹화했는가?
□ 각 커밋이 하나의 목적만 가지는가?
□ 테스트가 포함되어 있는가? (behavioral/test 타입)
□ 문서가 업데이트되었는가? (필요한 경우)
□ 커밋 크기가 200줄 이하인가?
□ 커밋 메시지가 규칙을 따르는가?
```

### 3.2 작성 순서

```bash
# 1. 변경 사항 확인
git status
git diff

# 2. 논리적 단위로 staging
git add -p  # interactive staging

# 3. 커밋 메시지 작성
git commit -m "type: subject" -m "body" -m "footer"

# 4. 커밋 검증
git show --stat  # 변경 파일 확인
git log --oneline -5  # 최근 커밋 확인
```

### 3.3 커밋 수정

```bash
# 마지막 커밋 수정 (아직 push하지 않은 경우)
git commit --amend

# 여러 커밋 정리 (interactive rebase)
git rebase -i HEAD~5

# 커밋 순서 변경, 합치기, 분할
# 에디터에서 pick/squash/fixup/edit/drop 선택
```

---

## 4. 브랜치 전략과 커밋

### 4.1 브랜치 명명

```
feature/user-authentication
bugfix/memory-leak-in-parser
refactor/extract-http-client
docs/api-reference-update
```

### 4.2 PR 커밋 정리

```
# PR 머지 전 커밋 정리 권장
# 여러 개의 작은 커밋 -> 논리적 단위로 정리

# Before: 10개의 "WIP", "fix", "oops" 커밋
# After: 3-5개의 의미 있는 커밋

structural: extract database module
behavioral: add connection pooling
test: add database integration tests
```

---

## 5. 자동화 도구

### 5.1 커밋 템플릿

```bash
# Git 템플릿 설정
git config commit.template .gitmessage
```

```
# .gitmessage
# <type>: <subject>
#
# <body>
#
# <footer>
#
# Types:
# - structural: 코드 구조 변경
# - behavioral: 동작 변경
# - docs: 문서 변경
# - test: 테스트 변경
# - perf: 성능 개선
#
# Rules:
# - Subject: 50자 이내, 명령문, 마침표 없음
# - Body: 72자 이내, 변경 이유 설명
# - Footer: Breaking changes, 이슈 참조
```

### 5.2 커밋 훅

```bash
#!/bin/sh
# .git/hooks/commit-msg

COMMIT_MSG_FILE=$1
COMMIT_MSG=$(head -n1 "$COMMIT_MSG_FILE")

# 타입 검사
if ! echo "$COMMIT_MSG" | grep -qE "^(structural|behavioral|docs|test|perf): "; then
    echo "Error: Commit message must start with a valid type"
    echo "Valid types: structural, behavioral, docs, test, perf"
    exit 1
fi

# Subject 길이 검사
SUBJECT_LEN=$(echo "$COMMIT_MSG" | wc -c)
if [ "$SUBJECT_LEN" -gt 72 ]; then
    echo "Error: Subject line too long (max 72 chars)"
    exit 1
fi

exit 0
```

---

## 6. 예시 모음

### 6.1 structural 예시

```
structural: extract validation module

Move all validation logic from models/ to new validation/
module. This improves separation of concerns and enables
reuse across different model types.

BREAKING CHANGE: ValidationError has moved from
models::ValidationError to validation::ValidationError.
```

### 6.2 behavioral 예시

```
behavioral: add rate limiting to API endpoints

Implement token bucket algorithm for rate limiting.
Default limits: 100 requests per minute per IP.

Configuration via RATE_LIMIT_REQUESTS and
RATE_LIMIT_WINDOW_SECONDS env vars.

Refs: #234
```

### 6.3 docs 예시

```
docs: document deployment process

Add comprehensive deployment guide covering:
- Environment setup
- Database migrations
- Health checks
- Rollback procedures
```

### 6.4 test 예시

```
test: add property-based tests for parser

Use proptest to verify parser invariants:
- Roundtrip: parse(render(ast)) == ast
- No panic on arbitrary input
- Valid output for valid input
```

### 6.5 perf 예시

```
perf: optimize string allocation in parser

Replace String::new() + push_str with with_capacity()
where input size is known. Reduces allocations by ~40%
in benchmark parsing large files.

Benchmark results:
- Before: 1.2ms/parse
- After: 0.7ms/parse
```

---

## 7. 관련 문서

- [코드 품질 표준](./quality-standards.md)
- [커밋 전 체크리스트](./pre-commit-checklist.md)
- [품질 메트릭](./quality-metrics.md)

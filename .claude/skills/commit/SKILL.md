---
name: git:commit
description: Git 커밋 메시지 작성을 요청할 때 반드시 사용하세요.
disable-model-invocation: true
user-invocable: true
---

# Git commit

Git 변경사항을 분석하고 **Conventional Commits 규약**에 따라 커밋 메시지를 작성합니다.

**IMPORTANT**

- 여러 논리적 변경사항이 존재하면 **분리된 커밋으로 진행하세요.**
- **Tidy First 원칙을 준수하세요**: 구조 변경(`refactor`)과 기능 변경(`feat`, `fix`)은 **동일 커밋에 포함하지 않습니다**
- 기본적으로 커밋 메시지 언어(`language`)는 **한국어**로 작성합니다.

# Workflow

## 1. 변경사항 분석

Git 변경사항을 확인하여 변경 범위를 파악합니다.

1. `git status`로 변경된 파일 확인
2. `git diff`로 **working tree 변경사항 확인**
3. 소스 코드 변경 분석 시 **가능하면 LSP를 우선 사용**

## 2. 논리적 커밋 분리

변경사항을 논리적 단위로 나눕니다.

1. **논리적 단위로 그룹화** (refactor / feat / docs 등)
2. 각 그룹의 **파일 경로를 명확히 식별**
3. **파일 경로를 지정하여 개별 commit**

## 3. 커밋 메시지 작성

커밋 메시지는 다음 구조를 따릅니다.

```
<type>(scope): <description>

<body>

<footer>
```

### Commit Type

| 타입         | 설명                      |
|------------|-------------------------|
| `feat`     | 새로운 기능 추가               |
| `fix`      | 버그 수정                   |
| `docs`     | 문서 수정 (README, 주석 등)    |
| `style`    | 코드 스타일 수정 (포맷팅, 세미콜론 등) |
| `refactor` | 리팩토링 (기능 변화 없음)         |
| `test`     | 테스트 추가 또는 수정            |
| `chore`    | 빌드, 설정, CI/CD 등 기타 작업   |

### Description 규칙

**IMPORTANT**:

- **명령문 형태로 작성**
- 첫 글자는 **소문자**
- **마침표(.) 사용 금지**
- **50자 이내 권장**

예시:
```
feat(auth): 사용자 로그인 엔드포인트 추가
fix(api): 언어 리소스 조회 오류 수정
```

### Body 규칙

**IMPORTANT**:

- 목록은 `-` 기호 사용
- 목록 항목(`-`) 사이에 **추가 개행 금지**

예시:
```
feat(search): 게시물 검색 기능 추가

- 제목 및 내용 기반 검색 구현
- Elasticsearch 연동
- 검색 결과 페이징 처리
```

**WRONG** (항목 사이에 개행 넣지 마세요):
```
feat(commands): Figma 디자인 계획 문서 생성 명령어 추가

- design-plan 명령어 정의

- Figma Desktop MCP 연동

- plan.md 문서 생성 워크플로우 정의
```

### Footer 형식

- `BREAKING CHANGE: 설명`
- `Closes #issue`
- `Fixes #issue`
- `Refs #issue`

예시:
```
feat(api): API 응답 형식 변경

BREAKING CHANGE: 응답이 JSON 객체에서 배열로 변경됨
```

# 4. Git Commit 실행 패턴

파일 경로를 직접 지정하여 스테이징 없이 커밋합니다.

## 패턴 1: 단일 파일, 한 줄 메시지

```bash
git commit <path> -m "<type>(<scope>): <description>"
```

예시:
```bash
git commit src/auth/Login.ts -m "feat(auth): 로그인 기능 추가"
```

## 패턴 2: 다중 파일, 여러 줄 메시지 (Heredoc)

**IMPORTANT**: `EOF`는 닫는 따옴표와 같은 라인에 작성합니다.

```bash
git commit <path1> <path2> -m "$(cat <<'EOF'
<type>(<scope>): <description>

- <change 1>
- <change 2>
EOF
)"
```

예시:
```bash
git commit src/auth/AuthService.ts src/auth/AuthController.ts -m "$(cat <<'EOF'
refactor(auth): 인증 서비스 구조 개선

- 단일 책임 원칙 적용
- 비즈니스 로직을 서비스 레이어로 분리
EOF
)"
```

## 패턴 3: 논리적 분리 예제 (Tidy First)

다음 파일이 수정됨:
- `AuthService.ts`, `AuthController.ts` → 리팩토링
- `SocialLogin.tsx`, `OAuthButton.ts` → 신규 기능
- `auth-flow.md` → 문서

커밋 순서:
```bash
# Group 1: 구조 변경 (refactor)
git commit src/auth/AuthService.ts src/auth/AuthController.ts -m "$(cat <<'EOF'
refactor(auth): 인증 로직 서비스 레이어로 분리

- UserController의 인증 관련 코드 추출
- AuthService 클래스 신규 생성
EOF
)"

# Group 2: 기능 추가 (feat)
git commit src/auth/SocialLogin.tsx src/auth/OAuthButton.ts -m "$(cat <<'EOF'
feat(auth): 소셜 로그인 기능 추가

- Google OAuth 연동
- 카카오 로그인 버튼 컴포넌트 구현
EOF
)"

# Group 3: 문서 (docs)
git commit docs/auth-flow.md -m "$(cat <<'EOF'
docs(auth): 인증 플로우 문서 추가

- 소셜 로그인 인증 절차 정리
- 시퀀스 다이어그램 추가
EOF
)"
```

## 주의사항

- **스테이징 없이 커밋**: `git add` 없이 path를 직접 지정
- **커밋 순서**: 구조 변경(refactor) → 기능 변경(feat/fix) → 문서(docs)
- **되돌리기**: 각 커밋은 독립적이므로 실패 시 해당 커밋만 수정

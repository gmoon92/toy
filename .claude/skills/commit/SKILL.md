---
name: git:commit
description: Git 커밋 메시지 작성을 요청할 때 사용하세요.
user-invocable: true
---

# Git commit

Git 변경사항을 분석하고 **Conventional Commits 규약**에 따라 커밋합니다.

**IMPORTANT**

- `/commit` 명령 또는 사용자 요청 시 논리적 기능/작업 단위로 자동 판단하여 커밋합니다.
- 여러 논리적 변경사항이 존재하면 **분리된 커밋으로 진행**
- **Tidy First 원칙**: 구조 변경(`refactor`)과 기능 변경(`feat`, `fix`)은 **동일 커밋에 포함하지 않음**
- **스테이징 없이 커밋**: `git add` 없이 파일 경로를 직접 지정
- 기본 커밋 메시지 언어는 **한국어**로 작성하세요.
- **커밋 순서**: 구조 변경(refactor) → 기능 변경(feat/fix) → 문서(docs)

# Workflow

## 1. 변경사항 분석

Git 변경사항을 확인하여 변경 범위를 파악합니다.

1. `git status`로 변경된 파일 확인
2. `git diff`로 **working tree 변경사항 확인**

## 2. 논리적 커밋 분리

변경사항을 논리적 단위로 나눕니다.

1. **논리적 단위로 그룹화**
2. 개별 커밋을 위해 각 그룹의 파일 경로를 명확히 식별

## 3. 커밋 메시지 작성

AI가 적절한 커밋 메시지를 작성합니다.

**형식:**
```
<type>(<scope>): <description>

- <body item>
- <body item>
```

**type:**
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 스타일 수정
- `refactor`: 리팩토링
- `test`: 테스트 추가/수정
- `chore`: 빌드, 설정, 기타 작업

**description:**
- 동사로 시작
- 소문자 시작
- 마침표 금지
- 50자 이내

**body item:**
- `-` 기호 사용
- 최대 5개 (너무 길면 축약)
- 항목 사이 개행 금지

## 4. 커밋 실행

작성한 메시지로 **즉시 실행**합니다.

```bash
git commit <path1> <path2> -m "$(cat <<'EOF'
<type>(<scope>): <description>

- <body item>
- <body item>
EOF
)"
```

### 예제

다음 파일이 수정됨:
- `AuthService.ts`, `AuthController.ts` → 리팩토링
- `SocialLogin.tsx`, `OAuthButton.ts` → 신규 기능
- `auth-flow.md` → 문서

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

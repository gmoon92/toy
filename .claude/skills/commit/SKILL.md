---
name: git:commit
description: |
  Git 변경사항을 분석하여 기능/모듈 단위로 분리하고 Conventional Commits 규약에 따라 커밋합니다.

  다음 상황에서 사용합니다:
  - "변경사항 커밋해줘"
  - "git 커밋해줘"
  - "커밋 메시지 작성해줘"
user-invocable: true
---

# Git commit

**IMPORTANT**:

- Git 커밋 요청 시 변경사항을 분석하여 적절한 단위로 분리합니다.
- 여러 독립적인 변경사항이 있으면 **분리된 커밋으로 진행**합니다.
- **Tidy First 원칙** (개발 소스 코드에만 적용): 구조 변경(`refactor`)과 기능 변경(`feat`, `fix`)은 **동일 커밋에 포함하지 않음**
  - 개발 소스(`src/`, `lib/` 등): refactor와 feat/fix 분리
  - 설정 파일(`.claude/`, `package.json` 등), 문서: 관련 변경 동일 커밋 가능
- **스테이징 없이 커밋**: `git add` 없이 파일 경로를 직접 지정합니다.
- 기본 커밋 메시지 언어는 **한국어**로 작성합니다.
- **커밋 순서**: `.claude/` 설정 → 구조 변경(refactor) → 기능 변경(feat/fix) → 문서(docs) → 기타(chore)

---

# Workflow

## 1. 변경사항 분석

Git 변경사항을 확인하여 변경 범위를 파악합니다.

1. `git status`로 변경된 파일 확인
2. `git diff`로 **working tree 변경사항 확인**

## 2. 커밋 단위 분리

변경사항을 독립적인 단위로 나눕니다.

**그룹화 기준 (우선순위 순):**

1. **파일 유형별**:

   | 유형 | 위치 예시 | 분리 기준 |
   |------|-----------|-----------|
   | Claude 설정 | `.claude/` | 단독 커밋, **최우선** |
   | 개발 소스 | `src/`, `lib/` | Tidy First 적용 (refactor 분리) |
   | 설정/빌드 | `package.json`, `.github/` | 관련 변경 묶음 가능 |
   | 문서 | `docs/`, `*.md` | 관련 변경 묶음 가능 |

2. **모듈/디렉토리별** - `src/auth/`, `src/payment/` 등
3. **변경 유형별** - refactor, feat/fix, docs, chore

각 그룹별 파일 경로를 명확히 식별합니다.

## 3. 커밋 메시지 작성

Conventional Commits 규약에 따라 커밋 메시지를 작성합니다.

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

승인된 메시지로 커밋을 실행합니다.

```bash
git commit <path1> <path2> -m "$(cat <<'EOF'
<type>(<scope>): <description>

- <body item>
- <body item>
EOF
)"
```

---

# 예제

**변경된 파일:**

- `.claude/settings.json`, `.claude/skills/commit/SKILL.md` → Claude 설정
- `AuthService.ts`, `AuthController.ts` → 리팩토링
- `SocialLogin.tsx`, `OAuthButton.ts` → 신규 기능
- `auth-flow.md` → 문서

**실행:**

```bash
# Group 1: Claude 설정 (chore) - 최우선
git commit .claude/settings.json .claude/skills/commit/SKILL.md -m "$(cat <<'EOF'
chore(claude): 커밋 스킬 설정 및 스킬 문서 개선

- settings.json 커밋 관련 설정 추가
- commit 스킬 문서 구조 개선
EOF
)"

# Group 2: 구조 변경 (refactor)
git commit src/auth/AuthService.ts src/auth/AuthController.ts -m "$(cat <<'EOF'
refactor(auth): 인증 로직 서비스 레이어로 분리

- UserController의 인증 관련 코드 추출
- AuthService 클래스 신규 생성
EOF
)"

# Group 3: 기능 추가 (feat)
git commit src/auth/SocialLogin.tsx src/auth/OAuthButton.ts -m "$(cat <<'EOF'
feat(auth): 소셜 로그인 기능 추가

- Google OAuth 연동
- 카카오 로그인 버튼 컴포넌트 구현
EOF
)"

# Group 4: 문서 (docs)
git commit docs/auth-flow.md -m "$(cat <<'EOF'
docs(auth): 인증 플로우 문서 추가

- 소셜 로그인 인증 절차 정리
- 시퀀스 다이어그램 추가
EOF
)"
```

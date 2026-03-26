---
name: git:commit
description: |
  Git 변경사항을 분석하여 기능/모듈 단위로 분리하고 Conventional Commits 규약에 따라 커밋합니다.
  커밋, 스테이징, 변경사항 저장 관련 요청에 적극적으로 사용하세요.

  다음 상황에서 사용합니다:
  - "변경사항 커밋해줘", "git 커밋해줘", "커밋 메시지 작성해줘"
  - "이 변경사항 저장해줘", "작업 내용 반영해줘"
  - "커밋 나눠줘", "기능별로 커밋 분리해줘"
  - 코드 작업 완료 후 커밋이 필요한 모든 상황
user-invocable: true
---

# Workflow

## 1. 변경사항 분석

Git 변경사항을 확인하여 변경 범위를 파악합니다.

1. `git status`로 변경된 파일 확인
2. `git diff`로 working tree 변경사항 확인
3. `git diff --staged`로 이미 스테이징된 변경사항 확인

## 2. 커밋 단위 분리

리뷰어가 변경 의도를 파악하기 쉽도록 독립적인 변경사항은 분리된 커밋으로 구성합니다.
개발 소스(`src/`, `lib/` 등)에서는 구조 변경(`refactor`)과 기능 변경(`feat`, `fix`)을 같은 커밋에 섞지 않습니다.
각 커밋이 하나의 명확한 목적을 가질 때 히스토리 추적과 롤백이 훨씬 수월해집니다.

**그룹화 기준 (우선순위 순):**

1. **파일 유형별**:

| 순서 | 유형          | 위치 예시                             | 분리 기준                           |
|:--:|-------------|-----------------------------------|----------------------------------|
| 1  | Claude 설정   | `.claude/settings.json`           | 단독 커밋, 최우선                      |
| 2  | Claude 스킬   | `.claude/skills/<name>/`          | 스킬별 단독 커밋                       |
| 3  | Claude 계획   | `.claude/docs/plans/<feature>/`   | feature별 단독 커밋                  |
| 4  | Claude 프롬프트 | `.claude/docs/prompts/<feature>/` | feature별 단독 커밋                  |
| 5  | 개발 소스       | `src/`, `lib/`                    | refactor와 feat/fix 분리            |
| 6  | 테스트         | `__tests__/`, `*.spec.ts`         | 관련 기능 변경과 함께 커밋 가능              |
| 7  | 설정/빌드       | `package.json`, `.github/`        | 관련 변경 묶음 가능                     |
| 8  | 문서          | `docs/`, `*.md`                   | 관련 변경 묶음 가능                     |

2. **모듈/디렉토리별** - `src/auth/`, `src/payment/` 등
3. **변경 유형별** - refactor, feat/fix, docs, chore

**커밋 순서**: `.claude/` 설정 → 구조 변경(refactor) → 기능 변경(feat/fix) → 문서(docs) → 기타(chore)

## 3. 커밋 메시지 작성

Conventional Commits 규약에 따라 커밋 메시지를 작성합니다. 커밋 메시지 언어는 **한국어**로 작성합니다.

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

## 4. 커밋 계획 확인

커밋을 실행하기 전에 사용자에게 계획을 보여주고 확인을 받습니다:

- 총 커밋 수와 각 커밋의 파일 목록
- 각 커밋 메시지 초안

사용자가 승인하면 순서대로 실행합니다. 수정 요청이 있으면 반영 후 재확인합니다.

## 5. 커밋 실행

파일 상태에 따라 커밋 방식을 선택합니다:

- **미스테이징 파일**: `git add` 없이 경로를 직접 지정합니다.
- **이미 스테이징된 파일**: 경로 지정 없이 커밋합니다.

```bash
# 미스테이징 파일 (경로 직접 지정)
git commit <path1> <path2> -m "$(cat <<'EOF'
<type>(<scope>): <description>

- <body item>
- <body item>
EOF
)"

# 이미 스테이징된 파일
git commit -m "$(cat <<'EOF'
<type>(<scope>): <description>

- <body item>
- <body item>
EOF
)"
```

---

# 예제

**변경된 파일:**

- `.claude/settings.json` → Claude 전역 설정
- `.claude/skills/commit/SKILL.md` → commit 스킬 문서
- `.claude/docs/plans/auth/login-flow-plan.md` → auth 기능 계획
- `.claude/docs/prompts/payment/tasks/analyze.md` → payment 프롬프트
- `AuthService.ts`, `AuthController.ts` → 리팩토링
- `SocialLogin.tsx`, `OAuthButton.ts` → 신규 기능
- `auth.spec.ts` → 인증 테스트
- `auth-flow.md` → 문서

**실행:**

```bash
# Group 1: Claude 설정 (config) - 최우선
git commit .claude/settings.json -m "$(cat <<'EOF'
chore(config): Claude Code 설정 업데이트

- 커밋 관련 설정 추가
EOF
)"

# Group 2: 스킬별 커밋 (skill 이름을 스코프로 사용)
git commit .claude/skills/commit/SKILL.md -m "$(cat <<'EOF'
docs(commit): 커밋 스킬 문서 개선

- .claude/ 디렉토리 세분화 규칙 추가
- 커밋 순서 및 예제 업데이트
EOF
)"

# Group 3: feature별 계획 (plan)
git commit .claude/docs/plans/auth/login-flow-plan.md -m "$(cat <<'EOF'
docs(plan): auth 기능 계획 문서 추가

- 로그인 플로우 계획 정리
EOF
)"

# Group 4: feature별 프롬프트 (prompt)
git commit .claude/docs/prompts/payment/tasks/analyze.md -m "$(cat <<'EOF'
docs(prompt): payment 프롬프트 템플릿 추가

- 결제 분석 작업 프롬프트 작성
EOF
)"

# Group 5: 구조 변경 (refactor)
git commit src/auth/AuthService.ts src/auth/AuthController.ts -m "$(cat <<'EOF'
refactor(auth): 인증 로직 서비스 레이어로 분리

- UserController의 인증 관련 코드 추출
- AuthService 클래스 신규 생성
EOF
)"

# Group 6: 기능 추가 + 관련 테스트 (feat)
git commit src/auth/SocialLogin.tsx src/auth/OAuthButton.ts src/auth/auth.spec.ts -m "$(cat <<'EOF'
feat(auth): 소셜 로그인 기능 추가

- Google OAuth 연동
- 카카오 로그인 버튼 컴포넌트 구현
- 소셜 로그인 통합 테스트 추가
EOF
)"

# Group 7: 문서 (docs)
git commit docs/auth-flow.md -m "$(cat <<'EOF'
docs(auth): 인증 플로우 문서 추가

- 소셜 로그인 인증 절차 정리
- 시퀀스 다이어그램 추가
EOF
)"
```

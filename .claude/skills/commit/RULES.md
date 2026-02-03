# Commit Message Rules

Detailed specifications for toy project commit messages.

## Basic Format

```
<type>(module|filename): <message>

<body> (optional)

<footer> (optional)
```

## Required Components

### Type

Must be one of:

| Type       | Description      | When to Use                                  |
|------------|------------------|----------------------------------------------|
| `feat`     | New feature      | Adding wholly new functionality              |
| `fix`      | Bug fix          | Fixing errors or incorrect behavior          |
| `refactor` | Code refactoring | Restructuring code without changing behavior |
| `test`     | Test code        | Adding or modifying tests                    |
| `docs`     | Documentation    | README, comments, documentation files        |
| `style`    | Code formatting  | Whitespace, semicolons, formatting only      |
| `chore`    | Build/config     | Dependencies, build scripts, tooling         |

### Scope (Module or Filename)

Identifies the part of codebase being modified:

**Module name** (for logical components):

- `auth` - Authentication module
- `user-service` - User management service
- `api` - API layer
- `batch` - Batch processing
- Examples: `feat(auth): add OAuth2 support`

**Filename** (for specific file changes):

- `UserController.java` - Single file modification
- `README.md` - Documentation file
- `application.yml` - Configuration file
- Examples: `fix(UserController.java): handle null pointer`

**Selection guidelines:**

- Use module name when changes affect multiple related files
- Use filename when modifying a single specific file
- Choose the most meaningful scope for understanding the change

### Message

- Brief description of changes (1 line)
- Start with lowercase (Korean or English)
- No period at the end
- Focus on "what" changed, not "how"

**Good examples:**

- `feat(spring-cloud-bus): 커스텀 이벤트 핸들러 추가`
- `fix(spring-data-redis): 연결 타임아웃 설정 수정`
- `refactor(CacheService.java): 메서드 추출`

**Bad examples:**

- `feat spring-batch` (missing parentheses and colon)
- `feat(spring-batch):Added a new batch processor.` (too verbose, period at end)
- `FEAT(spring-batch): 배치 프로세서 추가` (uppercase type)

## Optional Components

### Body

Add body when:

- 5+ files changed
- 100+ lines changed
- Complex logic requires explanation
- Multiple related changes

**Format:**

- Separate from title with one blank line
- Use `-` for bullet points
- Keep to 5 lines or less
- Focus on "why" and context

**Example:**

```
feat(spring-security-jwt): JWT 인증 필터 구현

- 토큰 생성 및 검증 로직 추가
- SecurityConfig에 JWT 필터 통합
- 토큰 만료 시간 30분 설정
```

### Footer

Add footer for:

- Breaking changes
- Related issues
- Version information

**Example:**

```
refactor(algorithm): 재귀함수 개선

- 재귀함수를 DFS 알고리즘 적용하여 속도 개선

Related: performance optimization, memory usage
```

## Validation Rules

### Format Validation

Pattern: `^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$`

Must pass:

1. Type is one of the allowed values
2. Scope is alphanumeric with dots, dashes, or underscores
3. Space after colon
4. Message is not empty

### Blank Lines

Maximum 2 blank line blocks allowed:

1. Between title and body
2. Between body and footer

**Good:**

```
feat(module): 제목

본문

Related: something
```

**Bad (3 blank line blocks):**

```
feat(module): 제목


본문


Related: something
```

### Content Restrictions

- No sensitive information (passwords, API keys, tokens)
- No placeholder text (`TODO`, `FIXME` in title)
- No excessive special characters

## Tidy First Principles

### Structural Changes (refactor)

Changes that don't affect behavior:

- Method extraction
- Variable/function renaming
- Code reorganization
- Import reordering
- Formatting improvements

**Examples:**

- `refactor(CacheService.java): 메서드 추출`
- `refactor(spring-batch): 변수명을 명확하게 개선`
- `style(spring-security): 코드 포맷팅 정리`

### Behavioral Changes (feat/fix)

Changes that affect functionality:

- New features
- Bug fixes
- API changes
- Logic modifications

**Examples:**

- `feat(spring-cloud-bus): 커스텀 이벤트 발행 기능 추가`
- `fix(spring-data-redis): 연결 풀 설정 버그 수정`

### Separation Rule

**NEVER mix structural and behavioral changes in one commit.**

**Bad (mixed):**

```
feat(spring-batch): 배치 재시도 기능 구현 및 리팩토링

Changes:
- BatchJobConfig 메서드 추출 (refactor)
- 배치 재시도 로직 추가 (feat)
- 변수명 개선 (refactor)
```

**Good (separated):**

```
Commit 1:
refactor(spring-batch): BatchJobConfig 메서드 추출 및 변수명 개선

Commit 2:
feat(spring-batch): 배치 재시도 로직 추가
```

### Why Separate?

1. **Easier code review**: Reviewers can focus on one type of change
2. **Clearer git history**: Each commit has single purpose
3. **Safer rollback**: Can revert behavior change without losing refactoring
4. **Better debugging**: `git bisect` identifies exact change that caused issue
5. **Logical independence**: Each commit represents one logical unit of work
6. **Better traceability**: Easy to understand what changed and why

## Toy Project Conventions

### Scope Selection Guidelines

**By Module:**

- Group related changes under module name
- Helps understand which part of system is affected
- Examples: `spring-batch`, `spring-security`, `spring-cloud-bus`, `spring-jpa`

**By Filename:**

- Use when change is isolated to single file
- More specific than module
- Examples: `BatchJobConfig.java`, `README.md`, `build.gradle`

**Module example:**

```
Good: feat(spring-security-jwt): JWT 인증 필터 추가
(Multiple JWT-related files changed)

Bad: feat(JwtAuthenticationFilter.java): JWT 인증 필터 추가
(Should use module name when multiple files affected)
```

**Single file example:**

```
Good: fix(DateUtils.java): DST 미처리 문제 수정
(Only DateUtils.java changed)

Bad: fix(java-core): DST 미처리 문제 수정
(Too broad when only one file changed)
```

### Commit Frequency

- Commit often, keep commits small
- One logical change per commit
- All tests should pass at each commit
- Prefer multiple small commits over one large commit

### Logical Independence Rule

**같은 타입이더라도 논리적으로 독립적이면 분리:**

커밋을 분리해야 하는 경우:

1. **서로 다른 목적**: 같은 docs 타입이어도 다른 문서 작성
2. **독립적인 리뷰**: 각각 독립적으로 검토 가능
3. **다른 컨텍스트**: 서로 다른 프로젝트/모듈/기능

**예시:**

❌ **잘못된 통합 커밋:**

```
docs(project): 여러 문서 추가

- 커밋 스킬 문서 추가 (.claude/skills/commit/)
- Claude API 문서 번역 추가 (ai/docs/claude/)
- 에이전트 설정 추가 (.claude/agents/)
```

→ 각각 다른 목적과 컨텍스트

✅ **올바른 분리 커밋:**

```
Commit 1:
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

Commit 2:
docs(claude-api): Claude API 문서 번역 추가

Commit 3:
docs(korean-translator): 기술 문서 번역 에이전트 추가
```

**판단 기준:**

- 10개 이상의 파일이 변경되었는가?
- 서로 다른 최상위 디렉토리의 파일들인가?
- 각 부분이 독립적으로 설명 가능한가?
- 하나의 PR 리뷰에 모두 포함하기 적절한가?

위 질문 중 하나라도 "아니오"면 분리를 고려하세요.

## Common Mistakes

### ❌ Wrong Format

```
feat spring-batch:no parentheses
feat(spring-batch) missing colon
FEAT(spring-batch): uppercase type
feat(spring-batch):no space after colon
```

### ❌ Poor Message Quality

```
feat(spring-batch): updated code
feat(spring-jpa): changes
fix(spring-cache): fix bug
```

### ❌ Mixed Changes

```
feat(spring-batch): add feature and refactor code
refactor(spring-jpa): rename variables and add new repository
```

### ❌ Sensitive Information

```
feat(spring-cloud-config): add server with key abc123xyz
fix(spring-security): update password to newPass123
```

## Best Practices

### ✅ Clear and Specific

```
feat(spring-cloud-bus): 커스텀 이벤트 발행 기능 추가
fix(spring-data-redis): 연결 타임아웃 설정 버그 수정
refactor(CacheService.java): 중복 코드 제거
```

### ✅ Focused Changes

Each commit should be:

- Atomic (single logical change)
- Complete (includes tests if applicable)
- Reversible (can be reverted safely)
- Reviewable (easy to understand)

### ✅ Meaningful History

Good commit history tells a story:

```
refactor(spring-batch): BatchJobConfig 메서드 추출
test(spring-batch): 배치 재시도 테스트 추가
feat(spring-batch): 배치 재시도 로직 구현
docs(spring-batch): 배치 재시도 문서 추가
```

## Quick Decision Tree

```
Is it a new feature? → feat
Is it fixing a bug? → fix
Does it change behavior? → feat or fix
Does it only restructure code? → refactor
Is it test code? → test
Is it documentation? → docs
Is it formatting only? → style
Is it build/config? → chore
```

## Reference

For examples, see [EXAMPLES.md](EXAMPLES.md)
For hook failures, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

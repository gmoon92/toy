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

**MANDATORY Format Rules:**

- **Separate from title with one blank line**
- **Each line MUST start with `- ` (dash + space)**: This is non-negotiable
- **Each item MUST be on a new line**: No comma-separated items
- **Keep to 5 lines or less**: Avoid overwhelming readers
- **Focus on "what" changed**: Action-oriented descriptions

**Example (Correct):**

```
feat(spring-security-jwt): JWT 인증 필터 구현

- 토큰 생성 및 검증 로직 추가
- SecurityConfig에 JWT 필터 통합
- 토큰 만료 시간 30분 설정
```

**Example (Wrong - No dashes):**

```
feat(spring-security-jwt): JWT 인증 필터 구현

토큰 생성 및 검증 로직 추가
SecurityConfig에 JWT 필터 통합
```

**Example (Wrong - Comma-separated):**

```
feat(spring-security-jwt): JWT 인증 필터 구현

- 토큰 생성 및 검증 로직 추가, SecurityConfig에 JWT 필터 통합
```

**For detailed format specifications, see:**
- [Body Generation](../generation/body.md) - Complete body generation criteria and format rules

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

**Core Rule:** NEVER mix structural changes (refactor) and behavioral changes (feat/fix) in one commit.

### Quick Guide

**Structural changes (refactor):**
- Method extraction, variable renaming, code reorganization
- Import reordering, formatting improvements
- No behavior change

**Behavioral changes (feat/fix):**
- New features, bug fixes, API changes, logic modifications
- Affects functionality

### Why Separate?

Separating structural and behavioral changes enables:
- Easier code review (focus on one type)
- Safer rollback (revert behavior without losing refactoring)
- Better debugging with `git bisect`
- Clearer git history

**For detailed examples and detection process, see:**
- [../templates/1-tidy-first.md](../../templates/1-tidy-first.md) - Detection and user guidance
- [examples.md](../support/examples.md) - Correct vs incorrect commit examples

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

**Core Rule:** Separate logically independent changes even if they're the same type.

### When to Separate

Separate commits when changes have:
1. **Different purposes**: Different documents, features, or goals
2. **Independent review**: Can be reviewed separately
3. **Different contexts**: Different projects/modules/features

### Quick Decision Criteria

Consider separating if:
- 10+ files changed
- Different top-level directories
- Each part can be explained independently
- Not appropriate for one PR review

**For detailed examples and auto-split process, see:**
- [2-logical-independence.md](../../templates/2-logical-independence.md) - Detection and user guidance
- [logical-independence.md](logical-independence.md) - Automatic commit splitting process
- [examples.md](../support/examples.md) - Correct vs incorrect commit examples

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

## Related Documents

- **[support/examples.md](../support/examples.md)** - Complete commit message examples
  - All 7 commit types with real examples
  - Good vs bad patterns demonstrating these rules
  - How rules are applied in automatic detection
- **[process/step4-approval.md](../process/step4-approval.md)** - Validation process
  - When these rules are checked during approval
- **[logical-independence.md](logical-independence.md)** - Automatic commit splitting
- **[troubleshooting.md](../support/troubleshooting.md)** - Error handling and hook failures

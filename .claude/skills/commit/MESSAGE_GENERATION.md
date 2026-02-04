# Commit Message Generation Strategy

Standardize message generation algorithms to provide consistent templates across different clients.

---

## Overview

Generate **5 commit messages** for each group (independent change set).

- **Message 1**: Recommended message (optimal scope + clear expression)
- **Message 2**: Scope variation
- **Message 3**: Message expression variation
- **Message 4**: Body detail adjustment
- **Message 5**: Type alternative

User can select from 5 options or enter directly via "Other" option.

---

## Generation Strategies

### Message 1: Recommended Message (Optimal)

**Goal:** Most logical and clear message

**Scope selection:**
1. **Prefer module name**: When multiple related files changed
   - Example: `spring-batch`, `spring-security-jwt`, `commit-skill`
2. **Use filename**: When single file changed
   - Example: `DateUtils.java`, `README.md`, `build.gradle`

**Message writing:**
- Clearly express change purpose
- Verb + object structure
- Korean or English both acceptable

**Body writing:**
- Add when 5+ files changed
- List major changes with `-` bullets
- Limit to 5 lines or less

**예시:**
```
docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
- EXAMPLES.md: 실제 사용 예시
- TROUBLESHOOTING.md: 문제 해결 가이드
```

**Scoring criteria:**
- Scope accuracy: 40 points (match with main directory)
- Type accuracy: 30 points (match with change nature)
- Body completeness: 20 points (informativeness)
- Detail appropriateness: 10 points (match with change size)

---

### Message 2: Scope Variation

**Goal:** Change scope to different level

**Strategy:**
1. If Message 1 uses module name → Change to filename
2. If Message 1 uses filename → Change to parent module name

**Examples:**

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

**When useful:**
- When reviewer wants to focus on specific file
- When wanting to understand change in broader context

---

### Message 3: Message Expression Variation

**Goal:** Vary header message expression

**Strategy:**
1. **Concise version**: Keep only essentials
2. **Detailed version**: Add more context
3. **Alternative expression**: Synonyms, different perspective

**Examples:**

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

### Message 4: Body Detail Adjustment

**Goal:** Adjust body presence or detail level

**Strategy:**
1. If has body → Remove (header only)
2. If no body → Add (detailed description)
3. If has body → More concise or more detailed

**Examples:**

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

### Message 5: Type Alternative

**Goal:** Suggest alternative if interpretable as different type

**Strategy:**
Interpret changes from different perspective:
- `docs` ↔ `feat` (documentation or feature)
- `feat` ↔ `refactor` (new feature or improvement)
- `chore` ↔ `feat` (configuration or feature)
- `style` ↔ `refactor` (formatting or structural improvement)

**Examples:**

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

**When useful:**
- When change nature is ambiguous
- When interpretable differently based on team conventions

---

## Real Application Example

### Scenario: 4 files added (.claude/skills/commit/)

**Analysis:**
- Files: SKILL.md, RULES.md, EXAMPLES.md, TROUBLESHOOTING.md
- Type: docs (documentation added)
- Scope: commit-skill (module name) or SKILL.md (main file)
- Lines: 500+ lines added

**Generated messages follow the 5 strategies:**
1. **Optimal**: Module scope + informative body
2. **Scope variation**: Filename scope instead
3. **Message variation**: Concise expression
4. **Body variation**: Header only
5. **Type alternative**: feat interpretation

For complete examples, see [EXAMPLES.md](EXAMPLES.md).

---

## Scope Extraction Algorithm

### Extract Module Name

**Input:** File path list
```
.claude/skills/commit/SKILL.md
.claude/skills/commit/RULES.md
.claude/skills/commit/EXAMPLES.md
```

**Algorithm:**
1. Extract common directory: `.claude/skills/commit/`
2. Use last directory name as module name: `commit`
3. Add context if needed: `commit-skill`

**Examples:**
- `spring-batch/src/main/java/...` → `spring-batch`
- `ai/docs/claude/...` → `claude-api` (context added)
- `.claude/agents/korean-translator/...` → `korean-translator`

### Extract Filename

**Input:** When only 1 file changed
```
src/main/java/com/example/utils/DateUtils.java
```

**Algorithm:**
1. Extract filename only: `DateUtils.java`
2. Or use full path: `utils/DateUtils.java`

**Examples:**
- Single file change: `DateUtils.java`
- README change: `README.md`
- Config file: `application.yml`

---

## Message Generation Patterns

### Verb Selection

| Type | Preferred verbs (Korean) | Preferred verbs (English) |
|------|------------------|------------------|
| feat | 추가, 구현, 도입 | add, implement, introduce |
| fix | 수정, 해결 | fix, resolve, correct |
| refactor | 개선, 추출, 분리 | improve, extract, separate |
| test | 추가, 개선 | add, improve |
| docs | 추가, 수정, 개선 | add, update, improve |
| style | 정리, 적용 | format, apply |
| chore | 업데이트, 추가, 변경 | update, add, change |

### Object Structure

**Pattern:** `verb + object`

**Good examples:**
- `JWT 인증 필터 추가`
- `배치 재시도 로직 구현`
- `변수명 명확화`
- `테스트 커버리지 개선`

**Bad examples:**
- `추가` (no object)
- `코드 수정` (too vague)
- `버그 수정` (which bug?)

---

## Body Generation Criteria

### Addition Conditions

Add body if any of the following applies:
1. **5+ files** changed
2. **100+ lines** changed
3. **Complex logic** changes (judgment needed)
4. **Multiple related changes** (context needed)

### Format

```
- 주요 변경사항 1
- 주요 변경사항 2
- 주요 변경사항 3
- ...
```

**Rules:**
- Keep each line concise (1-2 lines)
- Limit to 5 lines or less
- Group by file or feature
- Focus on "what" rather than "why"

### Style Options

**Style 1: List by file**
```
- SKILL.md: 스킬 실행 프로세스 정의
- RULES.md: 커밋 메시지 형식 규칙
- EXAMPLES.md: 실제 사용 예시
```

**Style 2: Group by feature**
```
- 스킬 프로세스 자동화
- 메시지 형식 검증
- Tidy First 원칙 적용
```

**Style 3: Hierarchical structure**
```
- JWT 인증 구현:
  - 토큰 생성 및 검증 로직
  - SecurityConfig 통합
```

---

## Metadata JSON Structure

The complete JSON schema and field descriptions are documented in [METADATA.md](METADATA.md).

Each group includes a `suggestedMessages` array with 5 pre-generated messages following the strategies above.

---

## Implementation Guide for Other Clients

### Minimum Requirements

1. **Implement Scope extraction**
   - File path → Extract module name
   - Single file → Extract filename

2. **Implement 5 message generation**
   - Apply strategies 1-5
   - Follow JSON schema

3. **Implement Body generation conditions**
   - 5+ files or 100+ lines

### Optional Implementations

1. **Scoring algorithm**
   - Auto-select optimal message

2. **Context addition**
   - Apply project-specific conventions
   - Learn directory structure

3. **User preference learning**
   - Analyze past selection patterns
   - Personalized recommendations

---

## Validation Checklist

Verify generated messages satisfy:

- [ ] Format: Follows `<type>(scope): <message>` pattern
- [ ] Type: One of 7 types (feat, fix, refactor, test, docs, style, chore)
- [ ] Scope: Contains only alphanumeric + `.`, `-`, `_`
- [ ] Message: Start with lowercase, no period
- [ ] Body: If present, separated by blank line, 5 lines or less
- [ ] Footer: If present, separated by blank line
- [ ] Blank blocks: Maximum 2 (header-body, body-footer)

Regex validation:
```regex
^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$
```

---

## Related Documents

- **[RULES.md](RULES.md)** - Commit message format rules
  - Validation rules and format specifications
  - Tidy First and Logical Independence principles
- **[EXAMPLES.md](EXAMPLES.md)** - Complete commit message examples
  - All 7 commit types with real examples
  - Advanced scenarios and common mistakes
- **[METADATA.md](METADATA.md)** - Session metadata structure
  - Complete JSON schema and field descriptions
  - File lifecycle and cleanup procedures
- **[templates/README.md](templates/README.md)** - User interaction templates
  - AskUserQuestion tool structure
  - Template usage patterns

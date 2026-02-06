# Message Generation Algorithms

### Priority Order

1. **Module name** (preferred for multi-file changes)
2. **Filename** (for single file or small changes)
3. **Directory name** (fallback)

### Examples

**Module name:**
```
Input: .claude/skills/commit/SKILL.md, PROCESS.md, MESSAGE_GENERATION.md
Output: "commit-skill"
```

**Filename:**
```
Input: src/main/java/utils/DateUtils.java
Output: "DateUtils.java"
```

**Directory name:**
```
Input: src/auth/UserService.java, src/auth/LoginController.java
Output: "auth"
```

### Algorithm

```javascript
function detectOptimalScope(files) {
  // Single file
  if (files.length === 1) {
    return path.basename(files[0]);
  }

  // Find common directory
  const commonDir = findCommonDirectory(files);
  const dirName = path.basename(commonDir);

  // Enhance with context
  const enhancedScope = enhanceScope(dirName, files);

  return enhancedScope;
}

function enhanceScope(baseName, files) {
  // Common patterns
  const patterns = {
    'commit': 'commit-skill',
    'auth': 'spring-security' // if Spring project
  };

  return patterns[baseName] || baseName;
}
```

---

## Type Detection Algorithm

### Priority Order

1. **Test files only** → `test`
2. **Docs files only** → `docs`
3. **Bug fix patterns** → `fix`
4. **New feature patterns** → `feat`
5. **Refactoring patterns** → `refactor`
6. **Default** → `chore`

### Algorithm

```javascript
function detectPrimaryType(changes) {
  const { files, diff } = changes;

  // Check file types
  if (files.every(f => f.includes('test'))) return 'test';
  if (files.every(f => f.endsWith('.md'))) return 'docs';

  // Analyze diff content
  if (hasBugFixPatterns(diff)) return 'fix';
  if (hasNewFeaturePatterns(diff)) return 'feat';
  if (hasRefactoringPatterns(diff)) return 'refactor';

  return 'chore';
}

function hasBugFixPatterns(diff) {
  const patterns = [
    /fix\s+(bug|error|issue)/i,
    /resolve\s+#\d+/i,
    /patch\s+/i
  ];

  return patterns.some(p => p.test(diff));
}

function hasNewFeaturePatterns(diff) {
  const patterns = [
    /add\s+new/i,
    /implement\s+/i,
    /introduce\s+/i,
    /create\s+.*class/i
  ];

  return patterns.some(p => p.test(diff));
}

function hasRefactoringPatterns(diff) {
  const patterns = [
    /extract\s+(method|class)/i,
    /rename\s+/i,
    /move\s+.*to/i,
    /restructure/i
  ];

  return patterns.some(p => p.test(diff));
}
```

---

## Message Expression Generation

### Type-Specific Verbs

| Type | Korean Verbs | English Verbs |
|------|--------------|---------------|
| feat | 추가, 구현, 도입 | add, implement, introduce |
| fix | 수정, 해결 | fix, resolve |
| refactor | 개선, 재구성, 분리 | improve, restructure, extract |
| test | 추가, 개선 | add, improve |
| docs | 추가, 수정, 업데이트 | add, update, modify |
| style | 정리, 적용 | format, apply |
| chore | 업데이트, 변경 | update, change |

### Pattern: Verb + Object

**Algorithm:**
```javascript
function generateClearMessage(type, scope, changes) {
  const verb = selectVerb(type);
  const object = extractPrimaryObject(changes);

  return `${object} ${verb}`;
}

function selectVerb(type) {
  const verbs = {
    feat: ['추가', '구현', '도입'],
    fix: ['수정', '해결'],
    refactor: ['개선', '재구성', '분리'],
    docs: ['추가', '수정', '업데이트']
  };

  return verbs[type][0]; // Pick first as default
}

function extractPrimaryObject(changes) {
  // Analyze changed files to determine main subject
  // Examples:
  // - "JWT 인증 필터"
  // - "배치 재시도 로직"
  // - "커밋 메시지 생성 방식"

  return analyzeChanges(changes);
}
```

---

## Complete Example

### Input (Changes)

```
Files:
- .claude/skills/commit/MESSAGE_GENERATION.md (860 lines changed)
- .claude/skills/commit/PROCESS.md (289 lines changed)
- .claude/skills/commit/SKILL.md (22 lines changed)
- .claude/skills/commit/../assets/templates/3-1-header-selection.md (new file)
- .claude/skills/commit/../assets/templates/3-2-body-selection.md (163 lines added)
- .claude/skills/commit/../assets/templates/3-3-footer-selection.md (192 lines added)
```

### Generated Headers (5개)

**추천 2개 (고정):**
```
1. docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경 (추천)
2. refactor(commit-skill): 메시지 생성 프로세스를 사용자 선택 기반으로 재구성 (추천)
```

**일반 3개 (새로고침 가능):**
```
3. docs(MESSAGE_GENERATION.md): 헤더 5개 생성 전략으로 재작성
4. docs(.claude/skills): commit 스킬 문서 및 템플릿 업데이트
5. feat(commit-skill): 사용자 선택 기반 메시지 생성 기능 추가
```

### Generated Body Items

```
- MESSAGE_GENERATION.md: 헤더 5개 생성 전략으로 재작성
- PROCESS.md: Step 3을 3단계 선택 프로세스로 변경
- templates: 3개 새 템플릿 추가 (header, body, footer selection)
- SKILL.md: 코어 프롬프트를 헤더 선택 방식으로 업데이트
- 바디 없음 (헤더만 사용)
```

### Final Message (User Selections)

**Selected:**
- Header: #1 (docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경)
- Body: Items 1, 2, 3
- Footer: 푸터 없음

**Assembled:**
```
docs(commit-skill): 커밋 메시지 생성 방식을 3단계 선택으로 변경

- MESSAGE_GENERATION.md: 헤더 5개 생성 전략으로 재작성
- PROCESS.md: Step 3을 3단계 선택 프로세스로 변경
- templates: 3개 새 템플릿 추가 (header, body, footer selection)
```

---

## Validation Rules

**Header format:**
```regex
^(feat|fix|refactor|test|docs|style|chore)\([a-zA-Z0-9._-]+\): .+$
```

**Body format:**
- Each line starts with `- ` (dash + space)
- Maximum 5 lines
- Each line 1-2 lines long

**Footer format:**
- `Closes #\d+(, #\d+)*`
- `Fixes #\d+(, #\d+)*`
- `BREAKING CHANGE: .+`

---

## Related Documents

- **[process.md](PROCESS.md)** - Step 3 execution with 3 stages
- **[../assets/templates/3-1-header-selection.md](../assets/templates/3-1-header-selection.md)** - Stage 1
- **[../assets/templates/3-2-body-selection.md](../assets/templates/3-2-body-selection.md)** - Stage 2
- **[../assets/templates/3-3-footer-selection.md](../assets/templates/3-3-footer-selection.md)** - Stage 3
- **[metadata.md](METADATA.md)** - Metadata structure
- **[rules.md](RULES.md)** - Validation rules
- **[examples.md](EXAMPLES.md)** - Complete examples

# Commit Examples

Essential examples for toy project commit conventions.

---

## Basic Examples by Type

### feat (New Features)

**Example 1: Simple feature**
```
feat(spring-cloud-bus): implement custom event handler
```

**Example 2: Feature with body**
```
feat(spring-security-jwt): implement JWT authentication filter

- Add token generation and validation logic
- Integrate JWT filter into SecurityConfig
- Set token expiration to 30 minutes
```

---

### fix (Bug Fixes)

**Example 3: Simple fix**
```
fix(DateUtils.java): add null check for LocalDateTime conversion
```

**Example 4: Module fix**
```
fix(spring-quartz-cluster): fix race condition bug

- Add pessimistic lock to job scheduling
- Adjust transaction isolation level
```

---

### refactor (Code Refactoring)

**Example 5: Method extraction**
```
refactor(EventController.java): extract validation logic to method
```

**Example 6: Major refactoring**
```
refactor(spring-jpa): apply CQRS pattern

- Create UserReadRepository (queries)
- Create UserWriteRepository (create/update/delete)
- Reference appropriate repositories in service layer
```

---

### test (Test Code)

**Example 7: Test addition**
```
test(spring-integration-amqp): improve test coverage

- Add message retry edge case tests
- Add RabbitMQ integration tests
```

---

### docs (Documentation)

**Example 8: Single file**
```
docs(README.md): add Gradle multi-module setup example
```

**Example 9: Module documentation**
```
docs(gradle): add how to reference Gradle variables in application.yml
```

---

### style (Formatting)

**Example 10: Code formatting**
```
style(spring-security): clean up code formatting
```

---

### chore (Build/Config)

**Example 11: Dependency update**
```
chore(build.gradle): update Spring Boot to 3.2.5
```

**Example 12: Project initialization**
```
chore(spring-cloud-bus): initialize project
```

---

## Advanced Scenarios

### Message Selection Format

**CRITICAL: Always show full message with body in suggestions**

```
📝 Select commit message:

1. docs(commit-skill): add automatic commit message generation skill

   - SKILL.md: define skill execution process
   - references/validation/rules.md: commit message format rules
   - references/support/examples.md: usage examples
   - references/support/troubleshooting.md: troubleshooting guide

2. docs(commit-skill): add commit skill documentation

   - Commit automation skill documentation
   - Message format rule definitions

3. feat(commit-skill): automatic commit message generator

4. docs(claude-skills): implement commit skill

5. Direct input
```

User must see complete message (header + body) before selection.

---

### Auto-Split Commit

**Scenario:** 82 files with 3 logical groups

**Bad (unified):**
```
docs(claude): add Claude-related documents and skills

- Add commit skill documentation
- Translate Claude API documentation
- Set up translation agent
```

**Good (separated):**
```
Commit 1: docs(commit-skill): add automatic commit message generation skill
Commit 2: docs(claude-api): add Korean translation of Claude API documentation
Commit 3: docs(korean-translator): add technical document translation agent
```

---

### Tidy First Violation

**Bad (mixed refactor + feat):**
```
feat(spring-security): implement JWT authentication and refactoring

- Extract AuthService methods (refactor)
- Improve variable names (refactor)
- Add JWT filter (feat)
```

**Good (separated):**
```
Commit 1: refactor(spring-security): extract AuthService methods and improve variable names
Commit 2: feat(spring-security-jwt): add JWT authentication filter
```

---

### Logical Independence

**Scenario:** Same type but different contexts

**Bad:**
```
docs(spring): add multiple module documentation

- Spring Batch usage guide
- Spring Security JWT configuration
- Spring Cloud Bus overview
```

**Good:**
```
Commit 1: docs(spring-batch): add batch processing usage guide
Commit 2: docs(spring-security-jwt): add JWT authentication configuration guide
Commit 3: docs(spring-cloud-bus): add event bus overview
```

**Why?** Each module is independent and should be reviewed separately.

---

## Scope Selection

### Use Module Name
Multiple related files:
```
feat(spring-cloud-bus): refresh configuration and implement custom events
```

### Use Filename
Single specific file:
```
fix(DateUtils.java): fix unhandled DST issue
chore(application.yml): add database connection pool configuration
```

---

## Common Mistakes

### ❌ Wrong Format
```
feat spring-batch:no parentheses    # Missing ()
feat(spring-batch):no space         # Missing space after :
FEAT(spring-batch): uppercase       # Uppercase type
```

### ❌ Wrong Type
```
feat(spring-cache): change variable names      # Should be: refactor
refactor(spring-cloud): add Config             # Should be: feat
```

### ❌ Wrong Scope
```
feat(ConfigServerApplication.java): implement system  # Should use module
fix(spring-jpa): UserRepository null check            # Should use filename
```

---

## Related Documents

- **[validation/rules.md](../validation/rules.md)** - Commit message format rules
  - Complete validation rules and specifications
  - Why these patterns are recommended
- **[generation/header.md](../generation/header.md)** - Header generation algorithm
  - How to generate 5 header message candidates
- **[generation/body.md](../generation/body.md)** - Body generation strategy
  - Feature-based body item candidate generation
- **[validation/logical-independence.md](../validation/logical-independence.md)** - Auto-split examples
  - How to properly separate independent changes
- **[process/step3-message.md](../process/step3-message.md)** - 3-stage composition
  - How examples are used in message generation
  - Where examples are used in the workflow

# Commit Examples

Essential examples for toy project commit conventions.

---

## Basic Examples by Type

### feat (New Features)

**Example 1: Simple feature**
```
feat(spring-cloud-bus): 커스텀 이벤트 핸들러 구현
```

**Example 2: Feature with body**
```
feat(spring-security-jwt): JWT 인증 필터 구현

- 토큰 생성 및 검증 로직 추가
- SecurityConfig에 JWT 필터 통합
- 토큰 만료 시간 30분 설정
```

---

### fix (Bug Fixes)

**Example 3: Simple fix**
```
fix(DateUtils.java): LocalDateTime 변환 시 null 체크 추가
```

**Example 4: Module fix**
```
fix(spring-quartz-cluster): race condition 버그 수정

- Job 스케줄링에 비관적 락 추가
- 트랜잭션 격리 레벨 조정
```

---

### refactor (Code Refactoring)

**Example 5: Method extraction**
```
refactor(EventController.java): 검증 로직 메서드 추출
```

**Example 6: Major refactoring**
```
refactor(spring-jpa): CQRS 패턴 적용

- UserReadRepository 생성 (조회)
- UserWriteRepository 생성 (생성/수정/삭제)
- 서비스 레이어에서 적절한 리포지토리 참조
```

---

### test (Test Code)

**Example 7: Test addition**
```
test(spring-integration-amqp): 테스트 커버리지 개선

- 메시지 재시도 엣지 케이스 테스트 추가
- RabbitMQ 통합 테스트 추가
```

---

### docs (Documentation)

**Example 8: Single file**
```
docs(README.md): Gradle 멀티 모듈 설정 예시 추가
```

**Example 9: Module documentation**
```
docs(gradle): application.yml에서 Gradle 변수 참조 방법 추가
```

---

### style (Formatting)

**Example 10: Code formatting**
```
style(spring-security): 코드 포맷팅 정리
```

---

### chore (Build/Config)

**Example 11: Dependency update**
```
chore(build.gradle): Spring Boot 3.2.5로 업데이트
```

**Example 12: Project initialization**
```
chore(spring-cloud-bus): 프로젝트 초기화
```

---

## Advanced Scenarios

### Message Selection Format

**CRITICAL: Always show full message with body in suggestions**

```
📝 커밋 메시지를 선택하세요:

1. docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가

   - SKILL.md: 스킬 실행 프로세스 정의
   - references/validation/rules.md: 커밋 메시지 형식 규칙
   - references/support/examples.md: 실제 사용 예시
   - references/support/troubleshooting.md: 문제 해결 가이드

2. docs(commit-skill): 커밋 스킬 문서 추가

   - 커밋 자동화 스킬 문서
   - 메시지 형식 규칙 정의

3. feat(commit-skill): 자동 커밋 메시지 생성기

4. docs(claude-skills): commit 스킬 구현

5. 직접 입력
```

User must see complete message (header + body) before selection.

---

### Auto-Split Commit

**Scenario:** 82 files with 3 logical groups

**Bad (unified):**
```
docs(claude): Claude 관련 문서 및 스킬 추가

- 커밋 스킬 문서 추가
- Claude API 문서 번역
- 번역 에이전트 설정
```

**Good (separated):**
```
Commit 1: docs(commit-skill): 커밋 메시지 자동 생성 스킬 추가
Commit 2: docs(claude-api): Claude API 문서 한글 번역 추가
Commit 3: docs(korean-translator): 기술 문서 번역 에이전트 추가
```

---

### Tidy First Violation

**Bad (mixed refactor + feat):**
```
feat(spring-security): JWT 인증 구현 및 리팩토링

- AuthService 메서드 추출 (refactor)
- 변수명 개선 (refactor)
- JWT 필터 추가 (feat)
```

**Good (separated):**
```
Commit 1: refactor(spring-security): AuthService 메서드 추출 및 변수명 개선
Commit 2: feat(spring-security-jwt): JWT 인증 필터 추가
```

---

### Logical Independence

**Scenario:** Same type but different contexts

**Bad:**
```
docs(spring): 여러 모듈 문서 추가

- Spring Batch 사용 가이드
- Spring Security JWT 설정
- Spring Cloud Bus 개요
```

**Good:**
```
Commit 1: docs(spring-batch): 배치 처리 사용 가이드 추가
Commit 2: docs(spring-security-jwt): JWT 인증 설정 가이드 추가
Commit 3: docs(spring-cloud-bus): 이벤트 버스 개요 추가
```

**Why?** Each module is independent and should be reviewed separately.

---

## Scope Selection

### Use Module Name
Multiple related files:
```
feat(spring-cloud-bus): 설정 갱신 및 커스텀 이벤트 구현
```

### Use Filename
Single specific file:
```
fix(DateUtils.java): DST 미처리 문제 수정
chore(application.yml): 데이터베이스 연결 풀 설정 추가
```

---

## Quick Reference

| Type     | Example                                       |
|----------|-----------------------------------------------|
| feat     | `feat(spring-cloud-config): Config Server 구현` |
| fix      | `fix(spring-data-redis): 연결 타임아웃 수정`          |
| refactor | `refactor(spring-batch): 변수명 명확화`             |
| test     | `test(CacheServiceTest.java): 테스트 추가`         |
| docs     | `docs(README.md): Gradle 설정 가이드 추가`           |
| style    | `style(spring-security): 코드 포맷팅`              |
| chore    | `chore(build.gradle): Spring Boot 버전 업데이트`    |

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
feat(spring-cache): 변수명 변경      # Should be: refactor
refactor(spring-cloud): Config 추가  # Should be: feat
```

### ❌ Wrong Scope
```
feat(ConfigServerApplication.java): 시스템 구현  # Should use module
fix(spring-jpa): UserRepository null 체크       # Should use filename
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

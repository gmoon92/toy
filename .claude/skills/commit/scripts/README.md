# Commit Skill Scripts

결정론적이고 컨텍스트를 소비하지 않는 스크립트 모음입니다.

## 개요

Agent Skills 아키텍처의 **레벨 3: 리소스 및 코드**를 활용하여, 이 스크립트들은:
- ✅ **컨텍스트를 소비하지 않음** - bash를 통해 실행되며 출력만 컨텍스트에 포함
- ✅ **결정론적** - 동일한 입력에 대해 항상 동일한 출력
- ✅ **빠른 실행** - LLM 추론 없이 직접 실행
- ✅ **테스트 가능** - 단위 테스트 작성 가능

## 사용 가능한 스크립트

### 1. validate_message.py

커밋 메시지 형식을 검증하고 금지된 패턴을 감지합니다.

**기능:**
- Header 형식 검증: `<type>(scope): <message>`
- Body 형식 검증: 각 라인이 `- `로 시작
- Footer 형식 검증: Issue reference 또는 Breaking Change
- **금지된 패턴 감지**: `Co-Authored-By`, AI attribution watermarks

**사용법:**

```bash
# 파일에서 검증
python3 scripts/validate_message.py --message /tmp/commit-msg.txt --json

# stdin에서 검증
echo "feat(auth): add JWT" | python3 scripts/validate_message.py --stdin --json

# Strict mode (추가 검증 규칙)
python3 scripts/validate_message.py --message /tmp/commit-msg.txt --strict --json

# Human-readable output
python3 scripts/validate_message.py --message /tmp/commit-msg.txt
```

**출력 (JSON):**

```json
{
  "status": "valid",
  "message": "✅ Commit message is valid"
}
```

**에러 예시 (Co-Authored-By 감지):**

```json
{
  "status": "invalid",
  "message": "❌ Commit message validation failed",
  "errors": [
    "FORBIDDEN at line 6: Co-Authored-By watermark is forbidden (overrides system prompt)",
    "  Line content: Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
  ]
}
```

**Exit codes:**
- `0`: Valid message
- `1`: Invalid message (errors found)
- `2`: File not found or read error

**통합 예시 (step5-execute.md):**

```bash
# Validate before commit
COMMIT_MSG="feat(auth): add JWT authentication

- JWT 토큰 생성 로직 추가
- 토큰 검증 미들웨어 구현"

echo "$COMMIT_MSG" | python3 scripts/validate_message.py --stdin --json

# Only commit if validation passes
if [ $? -eq 0 ]; then
  git commit -m "$COMMIT_MSG"
fi
```

## 향후 추가 예정

### 2. score_body_items.py (계획 중)

Body item 후보들의 점수를 계산합니다.

**기능:**
- 변경 라인 수 기반 점수 (40점)
- 파일 중요도 기반 점수 (30점)
- 커밋 타입 관련성 점수 (30점)

### 3. detect_type.py (계획 중)

파일과 diff를 분석하여 커밋 타입을 감지합니다.

**기능:**
- 패턴 매칭 기반 타입 감지
- 결정론적 규칙 적용

### 4. detect_scope.py (계획 중)

변경된 파일들을 분석하여 최적의 스코프를 결정합니다.

**기능:**
- 모듈명 감지
- 공통 디렉토리 추출
- 파일명 기반 스코프 결정

## 아키텍처

```
레벨 1 (메타데이터)  →  SKILL.md 프론트매터 (항상 로드, ~100 토큰)
레벨 2 (지침)        →  references/*.md (필요 시 로드, ~5k 토큰)
레벨 3 (코드)        →  scripts/*.py (실행 시 0 토큰, 출력만 컨텍스트에 포함)
```

**레벨 3의 이점:**
- 스크립트 코드는 컨텍스트에 로드되지 않음
- 실행 출력만 컨텍스트 소비
- 대규모 로직도 토큰 비용 없음

## 개발 가이드

### 새 스크립트 추가

1. **스크립트 작성**: `scripts/new_script.py`
2. **실행 권한 부여**: `chmod +x scripts/new_script.py`
3. **문서 업데이트**: 이 README에 사용법 추가
4. **SKILL.md 업데이트**: Scripts 섹션에 언급
5. **참조 문서 수정**: 해당 단계(step*.md)에서 스크립트 호출

### 스크립트 작성 원칙

1. **결정론적**: 동일한 입력 → 동일한 출력
2. **독립적**: 외부 의존성 최소화
3. **JSON 출력**: `--json` 플래그로 JSON 형식 지원
4. **명확한 Exit code**: 0 (성공), 1 (실패), 2 (에러)
5. **상세한 에러**: 구체적인 에러 메시지 제공

### 테스트

```bash
# Unit test 예시
python3 -m pytest scripts/test_validate_message.py

# Manual test
./scripts/validate_message.py --message test/fixtures/valid-msg.txt
./scripts/validate_message.py --message test/fixtures/invalid-msg.txt
```

## 참고 자료

- [Agent Skills Documentation](https://platform.claude.com/docs/ko/agents-and-tools/agent-skills/overview)
- [점진적 공개 (Progressive Disclosure)](https://platform.claude.com/docs/ko/agents-and-tools/agent-skills/overview#skills-작동-방식)
- [Commit Skill 설계 문서](../SKILL.md)

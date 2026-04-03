# 강한 지시어 (Strong Directives)

스킬 문서에서 명확성과 결정론적 실행을 보장하는 키워드 참조.
검증 시 중요한 동작 지시에 약한 표현이 사용되면 개선 제안 (INFO).

---

## 키워드 참조표

| 키워드 | 용도 | 예시 |
|:------|:-----|:-----|
| `MANDATORY:` | 필수 행동 | `MANDATORY: 사전 빌드된 스크립트 사용` |
| `EXECUTE_SCRIPT:` | 실행 스크립트 마킹 | `EXECUTE_SCRIPT: scripts/validate.sh` |
| `DO NOT` | 명시적 금지 | `DO NOT 인라인 명령어 사용` |
| `ALWAYS` | 일관된 행동 강제 | `ALWAYS 스크립트 참조 사용` |
| `NEVER` | 강한 금지 | `NEVER 코드 재구성` |
| `CRITICAL:` | 최고 우선순위 경고 | `CRITICAL: git status 먼저 확인` |
| `IMPORTANT:` | 중요 사항 | `IMPORTANT: 스크립트는 결정론적 실행` |

---

## 사용 패턴

### MANDATORY — 필수 행동 정의

```markdown
**MANDATORY: 사전 빌드된 스크립트 실행 (인라인 금지)**
```

### EXECUTE_SCRIPT — 실행 스크립트 마킹

```markdown
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
./scripts/analysis/collect_changes.sh
```

### DO NOT / NEVER — 명시적 금지

```markdown
**IMPORTANT:**
- DO NOT 문서에서 명령어 재구성
- DO NOT 인라인 bash 명령어 사용
- NEVER 스크립트 실행 단계 건너뜀
```

### ALWAYS — 일관된 행동 강제

```markdown
ALWAYS `EXECUTE_SCRIPT:` 지시어를 통해 사전 빌드된 스크립트를 실행하라.
```

### CRITICAL / IMPORTANT — 우선순위 표시

```markdown
**CRITICAL:** 커밋 전 git status 확인.

**IMPORTANT:** 스크립트는 결정론적 실행을 보장한다 (0 토큰 비용).
```

---

## 완전한 사용 예시

```markdown
**MANDATORY: 사전 빌드된 스크립트 사용 (인라인 명령어 금지)**

```bash
# EXECUTE_SCRIPT: scripts/category/script_name.sh

./scripts/category/script_name.sh < input.json > output.json
```

**CRITICAL:**
- ALWAYS `EXECUTE_SCRIPT:` 지시어를 통한 사전 빌드된 스크립트 사용
- DO NOT 문서에서 명령어 재구성
- DO NOT 인라인 bash 명령어 사용
- NEVER 검증 단계 건너뜀

**IMPORTANT:**
스크립트는 결정론적 실행을 보장한다 (0 토큰 비용).
```

---

## 약한 지시어 vs 강한 지시어

| 약한 표현 (경고 대상) | 강한 표현 (권장) |
|:---------------------|:----------------|
| "you should use" | `ALWAYS use` |
| "consider using" | `MANDATORY: use` |
| "try to avoid" | `DO NOT` / `NEVER` |
| "it's better to" | `CRITICAL:` |
| "recommended" | `MANDATORY:` |
| "please don't" | `DO NOT` |

**검증 포인트**: 스킬의 핵심 실행 지시에 약한 표현이 사용되면 강한 지시어로 교체 제안 (INFO)

---

## 강한 지시어 사용 원칙

1. **일관성**: 동일한 목적에 동일한 키워드 사용
2. **과도한 사용 금지**: 정말 중요한 지시에만 사용 (남용 시 효과 감소)
3. **구체성**: 정확히 무엇을 해야 하는지/피해야 하는지 명시
4. **이유 제공**: 왜 중요한지 컨텍스트 설명
5. **조합 활용**: 필요 시 여러 키워드 조합

**사용 적합 상황**:
- 스크립트 실행 요구사항
- 중요한 검증 단계
- 명시적 금지 사항
- 필수 워크플로우
- 안전 크리티컬 작업

**사용 부적합 상황**:
- 일반적인 정보
- 선택적 제안
- 배경 컨텍스트
- 예시 및 설명

---

**참조**: [구현 패턴](implementation-patterns.md) | [가이드라인](guidelines.md)

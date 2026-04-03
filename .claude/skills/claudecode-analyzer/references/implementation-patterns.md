# 스킬 구현 패턴 (Implementation Patterns)

Claude Code 스킬 문서 작성 시 적용하는 10가지 핵심 구현 패턴.
검증 시 이 패턴들의 준수 여부를 확인하고 미적용 시 INFO 또는 WARNING으로 제안한다.

---

## Pattern 1: XML 태그 구조화 출력

**문제**: 다중 섹션 출력의 경계가 불명확함  
**해결**: XML 태그로 출력 구조를 명시적으로 정의

```markdown
<analysis>
변경 파일 수, 변경 유형, 범위 등 분석 결과
</analysis>

<commit_message>
type(scope): description
</commit_message>

<validation>
검증 결과 (✅/❌)
</validation>
```

**검증 포인트**: 스킬이 복잡한 다단계 출력을 생성하는 경우, XML 태그 구조화 사용 여부 확인

---

## Pattern 2: 진행적 참조 로딩 (Progressive Reference Loading)

**문제**: 모든 참조 문서를 처음에 로드하면 토큰 낭비  
**해결**: 워크플로우 단계별로 필요한 문서만 로드

```markdown
## When to Load References

### Phase 1: 분석 단계
- **Load**: [process/analysis.md](references/process/analysis.md)
- **Purpose**: 변경사항 수집 및 분석 방법
- **When**: 커밋 생성 시작 시

### Phase 2: 생성 단계 (조건부)
- **Load**: [generation/commit-message.md](references/generation/commit-message.md)
- **Purpose**: 커밋 메시지 생성 알고리즘
- **When**: 분석 완료 후
```

**토큰 절감 효과**: 전형적으로 ~67% 절감  
**검증 포인트**: SKILL.md가 100줄 이상인 경우, "When to Load References" 섹션 존재 여부 확인 (INFO)

---

## Pattern 3: 스크립트 추출 + 강한 지시어

**문제**: 인라인 코드가 컨텍스트 토큰을 소비하고 비결정적 실행 위험  
**해결**: 실행 가능한 스크립트로 추출하고 명시적 실행 지시어 사용

**Before (인라인)**:
```markdown
다음 명령어를 실행하세요:
```bash
git diff --staged
git status --short
```
```

**After (스크립트 추출)**:
```markdown
**MANDATORY: 사전 빌드된 스크립트 사용 (인라인 명령어 금지)**

```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
./scripts/analysis/collect_changes.sh > changes.json
```

**IMPORTANT:**
- ALWAYS 사전 빌드된 스크립트를 `EXECUTE_SCRIPT:` 지시어로 실행
- DO NOT 인라인 bash 명령어 재구성
```

**스크립트 절감 효과**: 실행 시 0 토큰 소비  
**검증 포인트**: 반복적인 Bash 코드 블록이 3개 이상이면 스크립트 추출 권장 (INFO)

---

## Pattern 4: 메타데이터 캐싱

**문제**: 이후 단계에서 이전 단계 분석 결과가 필요하지만 재분석 낭비  
**해결**: 분석 단계에서 결과를 캐시하고 이후 단계에서 읽기

**분석 단계 - 쓰기**:
```markdown
<metadata_write>
{
  "change_type": "feature",
  "scope": "auth",
  "file_count": 3
}
</metadata_write>
```

**생성 단계 - 읽기**:
```markdown
<metadata_read>
Read: change_type, scope, file_count
</metadata_read>

**IMPORTANT:** 재분석하지 말 것. 캐시된 값 사용.
```

**검증 포인트**: 다단계 워크플로우에서 단계 간 데이터 전달 방식 확인

---

## Pattern 5: 기본 행동 (Default to Action)

**문제**: 제안만 하고 구현하지 않거나, 불필요한 확인을 요청  
**해결**: 명시적 기본 행동 지시어 추가

```markdown
<default_to_action>
제안보다 구현을 우선하라.
사용자 의도가 불명확할 때는 가장 유용한 행동을 추론하고 진행하라.
도구로 누락된 세부사항을 발견하라.
다음 경우에만 확인 요청:
- 여러 유효한 접근 방식이 있는 경우
- 파괴적인 작업이 필요한 경우
</default_to_action>
```

**검증 포인트**: 스킬이 자율적으로 실행되어야 하는 경우, 이 패턴 적용 여부 확인

---

## Pattern 6: 조사 우선 (Investigate Before Answering)

**문제**: 파일을 읽지 않고 코드에 대해 추측할 위험  
**해결**: 답변 전 조사 의무화

```markdown
<investigate_before_answering>
읽지 않은 코드에 대해 추측하지 말 것.
사용자가 특정 파일을 언급하면 반드시 먼저 읽을 것.
검증 없는 주장 금지. 실제 코드에 기반한 답변만 제공.
</investigate_before_answering>
```

**검증 포인트**: 코드 분석 스킬에 이 패턴 적용 여부 확인

---

## Pattern 7: 병렬 도구 실행

**문제**: 독립적인 도구 호출을 순차 실행하면 시간 낭비  
**해결**: 독립적 도구 호출을 병렬로 실행

```markdown
<use_parallel_tool_calls>
독립적인 도구 호출은 모두 병렬 실행.
예: 3개 파일 읽기 → 한 응답에서 3개 Read 동시 호출.
단, 도구 결과에 의존하는 호출은 순차 실행 필수.
플레이스홀더 사용 금지.
</use_parallel_tool_calls>
```

**검증 포인트**: 다중 파일 읽기/분석 스킬에서 병렬 실행 지시 포함 여부

---

## Pattern 8: 오버엔지니어링 회피

**문제**: 요청 범위를 초과하는 불필요한 추상화나 기능 추가  
**해결**: 명시적 금지 지시어로 범위 제한

```markdown
<avoid_overengineering>
요청된 것만 변경하라. 솔루션은 단순하고 집중적으로.

금지:
- 요구사항 외 기능 추가
- 관련 없는 코드 리팩토링
- 일회용 유틸리티 생성
- 가상의 미래 요구사항을 위한 설계

필수:
- 구체적인 문제 해결
- 기존 추상화 재사용
- 현재 작업을 위한 최소 코드
</avoid_overengineering>
```

**검증 포인트**: 코드 수정 스킬에서 이 패턴 적용 여부

---

## Pattern 9: 명확한 성공 기준

**문제**: 작업 완료 시점 및 성공 정의 불명확  
**해결**: 명시적 완료 조건과 검증 체크리스트

```markdown
## 성공 기준

다음 조건이 모두 충족될 때 완료:

1. **[기준 1]** - 구체적이고 측정 가능한 결과
2. **[기준 2]** - 다른 구체적 결과
3. **[기준 3]** - 검증 요구사항

검증:
- [ ] 체크리스트 항목 1
- [ ] 체크리스트 항목 2
```

**검증 포인트**: 복잡한 다단계 스킬에서 성공 기준 섹션 존재 여부 확인 (INFO)

---

## Pattern 10: 에러 복구 가이드

**문제**: 스크립트나 작업 실패 시 복구 방법 불명확  
**해결**: 일반적인 오류와 복구 절차 문서화

```markdown
## 에러 처리

### 일반적인 오류

**오류**: [오류 메시지 패턴]
**원인**: [발생 이유]
**복구**: [해결 방법]

### 종료 코드
- `0`: 성공
- `1`: [특정 오류 유형]
- `2`: [다른 오류 유형]
```

**검증 포인트**: 외부 시스템(Git, API)과 상호작용하는 스킬에서 에러 처리 문서 존재 여부

---

## 패턴 적용 지침

패턴은 스킬 요구사항에 따라 선택적으로 적용한다. 모든 패턴을 무조건 적용하지 않는다.

| 스킬 유형 | 권장 패턴 |
|:---------|:---------|
| 파일 처리 스킬 | 1, 2, 7 |
| Git/자동화 스킬 | 3, 4, 5, 10 |
| 코드 분석 스킬 | 6, 7, 8 |
| 다단계 워크플로우 | 2, 4, 9 |
| 대화형 스킬 | 5, 8 |

---

**참조**: [강한 지시어](strong-directives.md) | [가이드라인](guidelines.md)

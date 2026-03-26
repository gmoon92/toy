# 스킬 구현 패턴 (Implementation Patterns for Skills)

Claude 4.x 모범 사례를 기반으로 한 구체적인 구현 패턴입니다.

## 패턴 1: XML 태그를 사용한 구조화된 출력

### 문제
Claude는 명확한 경계와 일관된 구조를 가진 다중 부분 출력을 생성해야 합니다.

### 해결책
XML 태그를 사용하여 출력 구조를 명시적으로 정의하세요.

### 구현

```markdown
## 응답 형식

**응답을 다음과 같이 구조화하세요:**

<phase_name>
이 섹션에 들어가는 것에 대한 설명.
특정 형식 요구사항 포함.
</phase_name>

<another_phase>
다른 규칙을 가진 다른 콘텐츠.
예상 형식의 예시 보여주기.
</another_phase>
```

### 예시: 커밋 메시지 생성

```markdown
## 출력 형식

**필수: 출력을 정확히 다음과 같이 구조화하세요:**

<analysis>
**수정된 파일:** [count]
**변경 유형:** [feature|fix|refactor|docs|test]
**범위:** [감지된 범위]
**논리적 독립성:** ✅ yes | ⚠️ 우려 사항 기록됨
</analysis>

<commit_message>
type(scope): description

변경의 이유(why)를 설명하는 선택적 본문 단락.

참조나 주요 변경사항이 있는 선택적 푸터.
</commit_message>

<validation>
**일반 커밋:** ✅ 유효함 | ❌ 유효하지 않음
**Tidy First 원칙:** ✅ 통과 | ⚠️ 경고
**커밋 준비 완료:** ✅ yes | ❌ no - [이유]
</validation>
```

### 이점
- 명확한 섹션 경계
- 일관된 출력 구조
- 파싱하고 검증하기 쉬움
- 자체 문서화 형식

---

## 패턴 2: 점진적 참조 로딩

### 문제
모든 문서를 미리 로드하면 토큰이 낭비되고 관련 없는 콘텐츠가 로드될 수 있습니다.

### 해결책
참조를 단계별로 구성하고 현재 단계에 필요한 것만 로드하세요.

### 구현

```markdown
## 참조 로딩 시기

### 단계 1: [단계 이름]
- **로드**: [path/to/doc1.md](path/to/doc1.md)
- **로드**: [path/to/doc2.md](path/to/doc2.md)
- **목적**: 이것들이 가능하게 하는 것에 대한 간략한 설명
- **언제**: 특정 트리거 조건

### 단계 2: [다음 단계]
- **로드**: [path/to/doc3.md](path/to/doc3.md)
- **목적**: 다른 기능
- **언제**: 다른 트리거
```

### 예시: 커밋 스킬

```markdown
## 참조 로딩 시기

### 분석 단계
- **로드**: [process/01-analysis-phase.md](references/process/01-analysis-phase.md)
- **목적**: Git 변경사항을 수집하고 분석하는 방법 이해
- **언제**: 커밋 생성 시작 또는 사용자가 변경사항에 대해 물을 때

### 생성 단계
- **로드**: [algorithms/commit-message-generation.md](references/generation/commit-message-generation.md)
- **목적**: 일반적인 커밋 메시지 생성
- **언제**: 분석 완료 후

### 검증 단계
- **로드**: [validation/tidy-first-principles.md](references/validation/tidy-first-principles.md)
- **목적**: Tidy First 원칙에 대해 검증
- **언제**: 메시지 생성 후
```

### 이점
- ~67% 토큰 절약 (일반적)
- 단계별 집중된 컨텍스트
- 더 빠른 로딩
- 더 명확한 워크플로우

---

## 패턴 3: 강력한 지시문이 있는 스크립트 추출

### 문제
문서의 인라인 코드:
- 컨텍스트에 로드될 때 토큰 소비
- 다르게 재구성되거나 해석될 수 있음
- 결정적 실행 보장 없음

### 해결책
실행 가능한 스크립트로 추출하고 명시적인 실행 지시문 추가.

### 구현

**단계 1: 스크립트로 추출**
```bash
#!/usr/bin/env bash
# scripts/category/action_target.sh
# 목적: 한 줄 설명
# 사용법: command < input > output
# 입력: 형식 설명
# 출력: 형식 설명

# 스크립트 구현
```

**단계 2: 문서에 강력한 지시문 추가**
```markdown
**필수: 미리 빌드된 스크립트 실행 (인라인 명령어 금지)**

```bash
# EXECUTE_SCRIPT: scripts/category/action_target.sh

./scripts/category/action_target.sh < input.json > output.json
```

**중요:**
- 항상 `EXECUTE_SCRIPT:` 지시문을 통해 미리 빌드된 스크립트 사용
- 문서에서 명령어 재구성하지 마세요
- 인라인 bash 명령어 사용하지 마세요
- 스크립트는 결정적 실행 보장(0 토큰 비용)
```

### 예시: Git 변경사항 수집

**이전 (인라인):**
```markdown
## 변경사항 수집

다음 명령어 실행:
```bash
git diff --staged
git diff HEAD
git status --short
```

**이후 (스크립트 추출):**

**파일: scripts/analysis/collect_changes.sh**
```bash
#!/usr/bin/env bash
# collect_changes.sh
# 목적: 구조화된 형식으로 모든 Git 변경사항 수집
# 사용법: ./collect_changes.sh > changes.json
# 출력: staged, modified, untracked 파일이 있는 JSON

git diff --staged > /tmp/staged.diff
git diff HEAD > /tmp/modified.diff
git status --short > /tmp/status.txt

jq -n \
  --arg staged "$(cat /tmp/staged.diff)" \
  --arg modified "$(cat /tmp/modified.diff)" \
  --arg status "$(cat /tmp/status.txt)" \
  '{staged: $staged, modified: $modified, status: $status}'
```

**문서:**
```markdown
## 변경사항 수집

**필수: 미리 빌드된 스크립트 사용 (인라인 git 명령어 금지)**

```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh

./scripts/analysis/collect_changes.sh > changes.json
```

**출력 형식:**
```json
{
  "staged": "diff output...",
  "modified": "diff output...",
  "status": "status output..."
}
```

**중요:**
- 문서에서 git 명령어 재구성하지 마세요
- 항상 미리 빌드된 스크립트를 통해 실행
- 스크립트는 일관된 출력 형식 보장(실행 시 0토큰 소비)
```

### 이점
- 스크립트 실행 시 0토큰(출력만 로드)
- 결정적 실행
- 유지보수 및 테스트 가능
- 버전 관리됨

---

## 패턴 4: 메타데이터 캐싱

### 문제
초기 단계의 분석 결과가 나중 단계에서 재분석 없이 접근해야 함.

### 해결책
분석 단계에서 결과를 메타데이터에 캐시하고, 나중 단계에서 메타데이터에서 읽기.

### 구현

**단계 1: 분석 - 메타데이터에 쓰기**
```markdown
## 분석 단계

변경사항을 분석한 후:

1. EXECUTE_SCRIPT를 사용하여 분석 수행
2. **결과를 메타데이터에 저장:**

<metadata_write>
{
  "change_type": "[감지된 유형]",
  "scope": "[감지된 범위]",
  "file_count": [숫자],
  "complexity": "[low|medium|high]"
}
</metadata_write>
```

**단계 2: 생성 - 메타데이터에서 읽기**
```markdown
## 생성 단계

커밋 메시지 생성 전:

1. **메타데이터에서 캐시된 분석 읽기:**

<metadata_read>
읽기: change_type, scope, file_count, complexity
</metadata_read>

2. 캐시된 데이터를 사용하여 생성 정보 제공
3. 재분석하지 마세요(분석이 이미 완료됨)
```

### 예시: 커밋 워크플로우

**분석 단계 참조:**
```markdown
## 분석 결과 캐싱

분석이 완료되면 결과를 캐시하세요:

<metadata_write>
{
  "analysis_complete": true,
  "change_type": "feature",
  "scope": "auth",
  "files_modified": 3,
  "logical_independence": true,
  "tidy_first_compliant": true
}
</metadata_write>

이 결과는 후속 단계에서 사용 가능할 것입니다.
```

**생성 단계 참조:**
```markdown
## 캐시된 분석 사용

**중요: 재분석하지 마세요**

메타데이터에서 분석 결과 읽기:

<metadata_read>
필요: change_type, scope, logical_independence
</metadata_read>

이 캐시된 값을 사용하여 커밋 메시지를 생성하세요.
재분석은 토큰 낭비이며 불필요합니다.
```

### 이점
- 중복 분석 방지
- 단계별 일관된 데이터
- 토큰 절약
- 더 빠른 실행

---

## 패턴 5: 기본적으로 실행

### 문제
Claude는 변경사항을 구현하는 대신 제안하거나, 불필요하게 확인을 기다릴 수 있음.

### 해결책
기본적으로 실행하도록 명시적인 지시문 추가.

### 구현

```markdown
<default_to_action>
제안하는 대신 변경사항을 구현하는 것이 기본입니다.
사용자 의도가 불분명할 때, 가장 유용한 작업을 추론하고 진행하세요.
누락된 세부사항을 발견하기 위해 도구를 사용하세요.
도구 호출(파일 편집, 읽기)이 의도된 것인지 추론하고 그에 따라 행동하세요.
</default_to_action>
```

### 예시: 스킬 리팩토링

```markdown
## 실행 접근 방식

<default_to_action>
스킬을 리팩토링할 때:
- 허가 없이 파일 읽기
- 인라인 코드가 감지되면 스크립트 추출
- 필요에 따라 디렉토리 생성
- 즉시 문서 업데이트
- 검증 후 변경사항 커밋

다음 경우에만 확인 요청:
- 여러 유효한 접근 방식이 존재
- 파괴적인 작업이 필요
- 사용자 선호가 결과에 영향
</default_to_action>
```

### 이점
- 사전 실행
- 왕복 감소
- 더 나은 사용자 경험
- 더 빠른 작업 완료

---

## 패턴 6: 답변 전 조사

### 문제
Claude는 코드에 대해 추측하거나 검증 없이 주장할 수 있음.

### 해결책
주장하기 전에 조사를 강제하세요.

### 구현

```markdown
<investigate_before_answering>
열어보지 않은 코드에 대해 추측하지 마세요.
사용자가 특정 파일을 참조할 때, 답변하기 전에 반드시 읽으세요.
코드베이스에 대한 질문에 답하기 전에 조사하고 관련 파일을 읽으세요.
조사하지 않고 코드에 대해 주장하지 마세요.
정확성에 확신이 있을 때만 근거 있고 환각 없는 답변을 제공하세요.
</investigate_before_answering>
```

### 예시: 코드 분석

```markdown
## 분석 요구사항

<investigate_before_answering>
코드를 분석하거나 리팩토링하기 전에:

1. **Read 도구를 사용하여 모든 관련 파일 읽기**
2. **필요시 Grep을 사용하여 패턴 검색**
3. **실제 코드를 확인하여 가정 검증**
4. **실제 코드를 기반으로 발견사항 문서화**

절대 하지 마세요:
- 파일 내용 추측
- 검증 없이 코드 구조 가정
- 코드를 읽지 않고 권장사항 제시
- 확인 없이 무언가가 존재한다고 주장

항상 하세요:
- 변경사항을 제안하기 전에 읽기
- 주장하기 전에 검증
- 특정 파일과 줄 번호 인용
- 실제 코드를 기반으로 권장사항 제시
</investigate_before_answering>
```

### 이점
- 정확한 권장사항
- 환각 없음
- 현실에 기반
- 신뢰 구축

---

## 패턴 7: 병렬 도구 실행

### 문제
순차적인 도구 호출은 작업이 독립적일 때 시간 낭비.

### 해결책
독립적인 도구 호출을 병렬로 실행.

### 구현

```markdown
<use_parallel_tool_calls>
종속성이 없는 여러 도구를 호출할 때, 모든 독립적인 호출을 병렬로 수행하세요.

작업을 동시에 수행할 수 있을 때 동시 도구 실행 우선.

예시: 3개 파일 읽기 - 3개의 병렬 Read 호출로 모두 동시에 로드.

그러나: 도구 호출이 이전 결과에 의존하는 경우, 병렬로 호출하지 마세요.
대신 순차적으로 호출하세요. 종속 값에는 절대 플레이스홀더를 사용하지 마세요.
</use_parallel_tool_calls>
```

### 예시: 다중 파일 분석

```markdown
## 분석 접근 방식

<use_parallel_tool_calls>
여러 파일을 분석할 때:

**해야 할 것 (병렬):**
```
Read file1.md
Read file2.md
Read file3.md
```
(한 번의 응답에서 3개 모두)

**하지 말아야 할 것 (순차적):**
```
Read file1.md
[결과 대기]
Read file2.md
[결과 대기]
Read file3.md
```

**예외:** file2 경로가 file1 내용에 의존하는 경우, 순차적이 필요함.
</use_parallel_tool_calls>
```

### 이점
- 더 빠른 실행
- 더 나은 리소스 활용
- 개선된 효율성
- 지연 시간 감소

---

## 패턴 8: 과잉 엔지니어링 방지

### 문제
Claude는 불필요한 추상화, 유틸리티, 또는 요구사항을 넘어선 기능을 생성할 수 있음.

### 해결책
과잉 엔지니어링을 명시적으로 금지하고 최소 범위를 정의.

### 구현

```markdown
<avoid_overengineering>
직접 요청되었거나 명확히 필요한 변경사항만 수행하세요.
해결책을 단순하고 집중적으로 유지하세요.

하지 마세요:
- 요구사항을 넘어선 기능 추가
- 관련 없는 코드 리팩토링
- 일회성 작업을 위한 유틸리티 생성
- 가상의 미래 요구사항을 위한 설계
- 불가능한 시나리오에 대한 오류 처리 추가
- 필요하지 않은 구성 시스템 빌드

하세요:
- 특정 문제 해결
- 기존 추상화 사용
- 낭부 코드와 프레임워크 신뢰
- 시스템 경계에서만 검증
- 현재 작업을 위한 최소 코드 작성
</avoid_overengineering>
```

### 예시: 스크립트 생성

```markdown
## 스크립트 범위

<avoid_overengineering>
스크립트를 생성할 때:

**필수:**
- ✅ 명시된 목적을 위한 핵심 기능
- ✅ 지정된 대로 입력/출력
- ✅ 외부 입력에 대한 오류 처리

**필수 아님:**
- ❌ 간단한 스크립트를 위한 구성 파일
- ❌ 디버그 출력을 위한 로깅 프레임워크
- ❌ 단일 구현을 위한 추상 기본 클래스
- ❌ 고정 인수를 위한 명령줄 파서
- ❌ 한 번만 사용되는 도우미 유틸리티

스크립트를 단순하고 집중적이며 유지보수 가능하게 유지하세요.
</avoid_overengineering>
```

### 이점
- 더 단순한 코드
- 더 쉬운 유지보수
- 더 빠른 개발
- 더 적은 기술 부채

---

## 패턴 9: 명확한 성공 기준

### 문제
작업이 완료된 시점이나 성공을 정의하는 것이 불분명.

### 해결책
명시적인 성공 기준을 미리 정의.

### 구현

```markdown
## 성공 기준

작업이 완료되었을 때:

1. **[기준 1]** - 구체적인 측정 가능한 결과
2. **[기준 2]** - 또 다른 구체적인 결과
3. **[기준 3]** - 검증 요구사항

검증:
- [ ] 체크리스트 항목 1
- [ ] 체크리스트 항목 2
- [ ] 체크리스트 항목 3
```

### 예시: 스크립트 추출

```markdown
## 성공 기준

스크립트 추출이 완료되었을 때:

1. **모든 인라인 코드 추출됨** - 문서에 코드 블록 없음
2. **스크립트 검증됨** - 모든 스크립트가 구문 검증 통과
3. **문서 업데이트됨** - 모든 참조가 EXECUTE_SCRIPT 지시문 사용
4. **실행 테스트됨** - 스크립트가 샘플 입력으로 성공적으로 실행
5. **README 생성됨** - scripts/README.md가 모든 스크립트 문서화

검증:
- [ ] `grep -r '```bash' references/`가 인라인 bash 블록 없음 반환
- [ ] `bash -n scripts/**/*.sh`가 오류 없이 통과
- [ ] 모든 문서가 `EXECUTE_SCRIPT:` 마커 사용
- [ ] 각 스크립트의 테스트 실행 성공
- [ ] scripts/README.md 존재하고 모든 스크립트 문서화
```

### 이점
- 명확한 완료 신호
- 객관적인 검증
- 범위 확장 방지
- 진행 상황 추적 가능

---

## 패턴 10: 오류 복구 안내

### 문제
스크립트나 작업이 실패할 수 있으며, Claude는 복구 방법을 알아야 함.

### 해결책
오류 처리 및 복구 절차 문서화.

### 구현

```markdown
## 오류 처리

### 일반적인 오류

**오류:** [오류 메시지 또는 패턴]
**원인:** [이것이 발생하는 이유]
**복구:** [수정 방법]

### 종료 코드

스크립트는 표준 종료 코드 사용:
- `0`: 성공
- `1`: [특정 오류 유형]
- `2`: [또 다른 오류 유형]

### 복구 절차

[조건]일 때:
1. 진단 단계
2. 수정 단계
3. 검증 단계
4. 재시도 단계
```

### 예시: Git 작업

```markdown
## 오류 처리

### 일반적인 오류

**오류:** `fatal: not a git repository`
**원인:** git 저장소 외부에서 명령어 실행
**복구:**
1. `pwd`를 실행하여 현재 디렉토리 확인
2. 저장소 루트로 이동
3. `git status`로 검증
4. 작업 재시도

**오류:** `nothing to commit, working tree clean`
**원인:** 변경사항 감지되지 않음
**복구:**
1. `git status`로 검증
2. `git diff`로 unstaged 변경사항 확인
3. 정말 변경사항이 없으면 사용자에게 알림
4. 커밋 시도하지 마세요

### 종료 코드

- `0`: 성공 - 변경사항 커밋됨
- `1`: 검증 실패 - 변경사항 거부됨
- `2`: 변경사항 없음 - 커밋할 것 없음
- `3`: Git 오류 - 저장소 문제

### 검증 실패에서 복구

커밋 검증이 실패할 때:
1. 사용자에게 검증 오류 제시
2. 메시지 수정을 원하는지 물어보기
3. 수정된 메시지로 검증 재실행
4. 검증 통과 후에만 진행
```

### 이점
- 우아한 오류 처리
- 명확한 복구 경로
- 혼란 감소
- 더 나은 사용자 경험

---

## 패턴 결합

효과적인 스킬은 종종 여러 패턴을 결합:

```markdown
## 예시: 완전한 단계 정의

### 분석 단계

**참조 로드:**
- [process/analysis.md](references/process/analysis.md)

**실행:**

<default_to_action>
허가 없이 자동으로 분석을 진행하세요.
</default_to_action>

<investigate_before_answering>
코드에 대한 주장을 하기 전에 모든 관련 파일을 읽으세요.
</investigate_before_answering>

**필수: 미리 빌드된 스크립트 실행**

```bash
# EXECUTE_SCRIPT: scripts/analysis/collect_changes.sh
./scripts/analysis/collect_changes.sh > changes.json
```

**결과 캐시:**

<metadata_write>
{
  "analysis_complete": true,
  "change_type": "[감지됨]",
  "scope": "[감지됨]"
}
</metadata_write>

**출력 형식:**

<analysis>
**수정된 파일:** [count]
**변경 유형:** [type]
**범위:** [scope]
</analysis>

**성공 기준:**
- [ ] 모든 변경사항 수집됨
- [ ] 변경 유형 감지됨
- [ ] 범위 식별됨
- [ ] 결과가 메타데이터에 캐시됨
```

## 요약

이들 패턴을 함께 적용하면:
- **토큰 효율성 개선** - 스크립트 추출 및 캐싱을 통해
- **결정적 실행 보장** - 강력한 지시문을 통해
- **신뢰성 향상** - 조사 요구사항을 통해
- **속도 증가** - 병렬 실행을 통해
- **단순성 유지** - 과잉 엔지니어링 방지를 통해
- **명확성 제공** - 구조화된 출력과 명확한 기준을 통해

스킬 요구사항에 기반하여 선택적으로 패턴을 적용하고, 모든 스킬에 맹목적으로 적용하지 마세요.

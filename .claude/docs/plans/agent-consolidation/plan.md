---
name: agent-consolidation-plan
description: docs/와 blog/ 에이전트 통합 작업 계획서 - 오케스트레이션 스킬 설계 및 구현 가이드
color: blue
---

# 에이전트 통합 작업 계획서

> docs/와 blog/ 에이전트 통합을 위한 오케스트레이션 스킬 설계
>
> **위치:** `.claude/docs/plans/agent-consolidation-plan.md`
> **버전:** 1.0
> **상태:** 초안
> **작성일:** 2026-03-21

---

## 1. 개요

### 1.1 현재 상태

현재 `.claude/agents/`에는 두 개의 에이전트 디렉토리가 존재:

- **`docs/`** (3개): `doc-reviewer`, `doc-writer`, `translator`
- ~~**`blog/`** (8개): `diagram-designer`, `synthesizer`, `moderator`, `reader-advocate`, `editor`, `critic`, `content-strategist`, `tech-writer`~~ → 통합되어 삭제됨

**문제점:**
- 기능 중복 (tech-writer ↔ doc-reviewer, blog-editor ↔ doc-reviewer)
- 오케스트레이션 복잡도 증가 (8개 개별 에이전트 관리)
- 세분화된 프로세스로 인한 유지보수 부담

### 1.2 통합 목표

**핵심 목표:**
- 오케스트레이션 스킬에서 범용적으로 사용 가능한 통합 에이전트 구조 설계
- 기능 중복 제거 및 역할 명확화
- 모드 기반 접근으로 에이전트 선택 단순화

**유지 원칙:**
- ~~기존 에이전트 문서는 삭제하지 않음~~ → blog/ 에이전트는 통합을 위해 삭제됨
- 새로운 통합 에이전트 문서를 별도로 생성
- 각 에이전트의 고유 가치는 통합 에이전트에 보존

---

## 2. 통합 아키텍처

### 2.1 통합 후 구조

```
.claude/agents/docs/
├── reviewer.md      ← 6개 에이전트 통합 (5가지 검증 모드)
├── writer.md        ← doc-writer 유지 (작성 역할)
├── visual.md        ← diagram-designer 이동/재명명
└── translator.md    ← translator 유지 (번역 역할)
```

### 2.2 역할 매핑

| 기존 에이전트 | 통합 위치 | 모드/역할 |
|-------------|----------|----------|
| `doc-reviewer` | `reviewer.md` | `standard` (기본 품질 검증) |
| `tech-writer` | `reviewer.md` | `technical` (기술 정확성 검증) |
| `critic` | `reviewer.md` | `critical` (공격적 검증) |
| `reader-advocate` | `reviewer.md` | `reader` (독자 관점 검증) |
| `content-strategist` | `reviewer.md` | `structure` (구조 분석) |
| `blog-editor` | `reviewer.md` | 최종 체크리스트 통합 |
| `diagram-designer` | `visual.md` | 시각화 전문 (별도 유지) |
| `doc-writer` | `writer.md` | 작성/개선 (별도 유지) |
| `translator` | `translator.md` | 번역 전문 (별도 유지) |
| `synthesizer` | - | 오케스트레이터가 담당 |
| `moderator` | - | 오케스트레이터가 담당 |

---

## 3. 통합 에이전트 상세 설계

### 3.1 reviewer.md (통합 검증 에이전트)

#### 목적
문서 품질 검증을 위한 범용 에이전트. 5가지 검증 모드를 지원하여 오케스트레이션 스킬에서 상황에 맞게 선택 가능.

#### 지원 모드

| 모드 | 설명 | 기반 에이전트 |
|-----|------|-------------|
| `standard` | 기본 6가지 품질 기준 검증 | doc-reviewer |
| `technical` | 기술 정확성 및 용어 검증 | tech-writer |
| `critical` | 공격적 검증 및 가정 발굴 | critic |
| `reader` | 독자 관점 접근성 평가 | reader-advocate |
| `structure` | 문서 구조 및 중복 분석 | content-strategist |
| `comprehensive` | 모든 모드 통합 실행 | - |

#### 특수 기능

- **한국어 자동 감지**: 한국어 문서 감지 시 어투/높임법 검증 자동 활성화
- **조걶 모드 실행**: 다중 모드 순차/병렬 실행 지원
- **통합 체크리스트**: 모든 모드가 동일한 마크다운 체크리스트 형식 출력

#### 출력 형식

```markdown
## 검증 리포트 (Mode: {mode})

### 품질 기준
- [x] 항목 1
- [ ] 항목 2: 개선 필요

### 기술 검증 (technical 모드시)
- [ ] 코드 예제 오류: line 23

### 비판적 검토 (critical 모드시)
- [ ] 가정 발견: "사용자가 X를 알고 있다"고 가정

### 독자 관점 (reader 모드시)
- [ ] 선행지식 필요: Docker 개념 설명 추가 필요

### 구조 분석 (structure 모드시)
- [ ] 중복 콘텐츠: 2.1과 3.2 내용 중복
```

---

### 3.2 writer.md (작성 에이전트)

#### 목적
문서 초안 작성 및 피드백 기반 개선.

#### 기능
- 초안 작성 (`ARGUMENTS`로 타겟 경로 수신)
- 피드백 체크리스트 기반 개선
- `${CLAUDE_TMP_DIR}/draft.md`에서 작업

#### 출력 위치
- `${CLAUDE_TMP_DIR}/draft.md`

---

### 3.3 visual.md (시각화 에이전트)

#### 목적
diagram-designer를 docs/로 이동하여 시각화 전문 에이전트로 활용.

#### 기능
- Mermaid 다이어그램 설계 및 최적화
- 다이어그램 유형 선택 가이드
- 모바일/데스크톱 가독성 고려
- 접근성 (색맹, 스크린리더) 고려

#### 출력 형식
```markdown
## 다이어그램 검토

### 인벤토리
| 다이어그램 | 유형 | 평가 |
|-----------|------|------|
| ... | ... | ... |

### 개선 제안
- ...

### 개선된 Mermaid 코드
```mermaid
...
```
```

---

### 3.4 translator.md (번역 에이전트)

#### 목적
영문 기술 문서를 한국어로 번역 (학습 목적). 프로젝트 언어 설정(`settings.json`)과 연동하여 동작.

#### 프로젝트 언어 설정 연동

**settings.json 설정:**
```json
{
  "language": "korean"  // 프로젝트 기본 언어
}
```

**동작 규칙:**
| 설정값 | translator 동작 | 출력 언어 |
|-------|----------------|----------|
| `"korean"` | 영→한 번역 활성화 | 한국어 |
| `"english"` | 번역 비활성화 또는 영→영 정제 | 영어 |
| `"auto"` | 소스 문서 언어 자동 감지 | 감지된 언어 |

#### 기능 (기존 유지 + 확장)
- **공식 문서 참조** 및 최신성 확인
- **기술 용어 일관성** 유지 (용어 사전 연동)
- **파일명 규칙** 적용 (`01-section-01-subject.md`)
- **원문 출처** 표시
- **HTML 태그** 마크다운 변환
- **프로젝트 언어 설정** 연동 (settings.json `language` 키 참조)

#### 출력 위치
- **한국어**: `ai/docs/claude/docs/{filename}.md`
- **영어**: `ai/docs/claude/docs/en/{filename}.md` (확장 고려)

#### 오케스트레이션 연동

```yaml
# settings.json language=korean 일 때
workflow:
  name: translate-if-needed
  steps:
    - check: "${settings.language} == 'korean'"
    - agent: translator
      source: "${ORIGINAL_URL}"
      target: "ai/docs/claude/docs/"
```

---

## 4. 오케스트레이션 스킬 연동

### 4.1 스킬 사용 예시

#### 예시 1: 기술 블로그 검증
```yaml
workflow:
  name: blog-content-review
  steps:
    - agent: reviewer
      mode: technical
      input: ${DRAFT_PATH}

    - agent: reviewer
      mode: critical
      input: ${DRAFT_PATH}

    - agent: reviewer
      mode: reader
      input: ${DRAFT_PATH}

    - agent: visual
      condition: has_diagrams
      input: ${DRAFT_PATH}
```

#### 예시 2: 한국어 문서 종합 검증
```yaml
workflow:
  name: korean-doc-validation
  steps:
    - agent: reviewer
      mode: comprehensive
      language: auto  # 한국어 자동 감지
      input: ${DOCUMENT_PATH}
```

#### 예시 3: 작성-검증 피드백 루프
```yaml
workflow:
  name: write-validate-loop
  steps:
    - agent: writer
      action: draft
      requirements: ${REQUIREMENTS}
      output: ${CLAUDE_TMP_DIR}/draft.md

    - agent: reviewer
      mode: standard
      input: ${CLAUDE_TMP_DIR}/draft.md
      output: ${CLAUDE_TMP_DIR}/feedback.md

    - agent: writer
      action: revise
      draft: ${CLAUDE_TMP_DIR}/draft.md
      feedback: ${CLAUDE_TMP_DIR}/feedback.md
```

### 4.2 오케스트레이터 역할

통합 후 오케스트레이터가 담당하는 기능:

| 기존 에이전트 | 오케스트레이터 기능 |
|-------------|-------------------|
| `synthesizer` | 다중 검증 결과 통합 및 우선순위 결정 |
| `moderator` | 타임박스 관리, 단계별 실행 제어, 에스컬레이션 |

---

## 5. 작업 단계 및 일정

### Phase 1: 설계 확정 (1일)
- [ ] reviewer.md 상세 명세 확정
- [ ] 각 모드별 출력 형식 확정
- [ ] 오케스트레이션 스킬 인터페이스 설계

### Phase 2: 통합 에이전트 작성 (2일)
- [ ] `reviewer.md` 작성 (5가지 모드 포함)
- [ ] `writer.md` 작성 (기존 doc-writer 기반)
- [ ] `visual.md` 작성 (diagram-designer 기반)
- [ ] `translator.md`는 기존 유지 (필요시 경로 이동)

### Phase 3: 오케스트레이션 스킬 통합 (2일)
- [ ] 스킬에서 reviewer 모드 호출 로직 구현
- [ ] 다중 모드 순차/병렬 실행 지원
- [ ] 결과 통합 및 우선순위화 로직

### Phase 4: 테스트 및 검증 (1일)
- [ ] 각 모드별 출력 검증
- [ ] 오케스트레이션 워크플로우 테스트
- [ ] 기존 blog/ 에이전트와 결과 비교

### Phase 5: 문서화 (1일)
- [ ] 통합 에이전트 사용 가이드 작성
- [ ] 오케스트레이션 스킬 문서 업데이트
- [ ] 마이그레이션 가이드 작성 (선택)

---

## 6. 추가 검증 및 지침

### 6.1 에이전트 문서 표준 검증

통합된 에이전트 문서는 다음 표준을 준수해야 함:

| 검증 항목 | 기준 | 검증 방법 |
|----------|------|----------|
| **프론트매터** | name, description, model, color 필수 | newsfeed 스킬 사용 |
| **description** | 200자 이내, 용도 명확히 기술 | 수동 검토 |
| **모드 정의** | 각 모드의 목적과 출력 형식 명시 | 문서 내 섹션 확인 |
| **예시 포함** | 각 모드별 출력 예시 필수 | 문서 내 예시 확인 |
| **상호참조** | 기존 에이전트와의 관계 명시 | 문서 내 References 섹션 |

### 6.2 통합 에이전트 작성 지침

#### reviewer.md 작성 지침

```markdown
## 작성 규칙

1. **모드 독립성**: 각 모드는 독립적으로 실행 가능해야 함
2. **조걶 출력**: 모드에 따라 출력 섹션 동적 포함/제외
3. **언어 감지**: 한국어 문서 시 자동으로 언어 검증 섹션 추가
4. **우선순위 표시**: 심각도(CRITICAL/WARNING/INFO)로 체크리스트 항목 분류
5. **근거 포함**: 모든 피드백은 구체적인 위치(라인/섹션)와 근거 제시
```

#### writer.md 작성 지침

```markdown
## 작성 규칙

1. **임시 작업**: `${CLAUDE_TMP_DIR}`에서만 작업, 원본 직접 수정 금지
2. **피드백 순환**: feedback_checklist.md 기반 개선 시 체크된 항목만 수정
3. **원문 보존**: 작성자의 원래 의도와 톤 유지
4. **완료 신호**: 작업 완료 시 오케스트레이터에 명시적 알림
```

#### visual.md 작성 지침

```markdown
## 작성 규칙

1. **Mermaid 유효성**: 제안하는 다이어그램은 유효한 Mermaid 문법이어야 함
2. **테마 고려**: 라이트/다크 모드 모두 가독성 확보
3. **모바일 고려**: 너비가 너무 넓은 다이어그램은 서브그래프로 분리
4. **대체 텍스트**: 다이어그램에 대한 텍스트 설명 항상 포함
```

### 6.3 오케스트레이션 통합 지침

#### 모드 호출 규약

```yaml
# 표준 호출 형식
agent_call:
  agent: reviewer
  parameters:
    mode: "technical"  # 필수: standard | technical | critical | reader | structure | comprehensive
    input: "${FILE_PATH}"  # 필수: 검증 대상 파일 경로
    language: "auto"  # 선택: auto | ko | en
    output_format: "checklist"  # 선택: checklist | detailed | summary
```

#### 결과 통합 규약

```yaml
# 다중 모드 실행 시 결과 통합
aggregation:
  method: "priority"  # conflict 시 우선순위 기준
  priority_order:
    - "critical"  # 가장 높은 우선순위
    - "technical"
    - "reader"
    - "structure"
    - "standard"
```

### 6.4 품질 보증 검증

| 단계 | 검증 내용 | 책임자 |
|-----|----------|--------|
| **Phase 2 종료 시** | 각 에이전트 개별 테스트 | 작성자 |
| **Phase 3 종료 시** | 오케스트레이션 통합 테스트 | 스킬 개발자 |
| **Phase 4 종료 시** | 기존 blog/ 에이전트와 결과 비교 | 검증자 |
| **최종** | 통합 워크플로우 E2E 테스트 | 팀 전체 |

#### 결과 비교 기준

기존 `critic` vs `reviewer(mode: critical)` 비교:
- 동일한 입력에 대한 출력 형식 일치 여부
- 발견하는 이슈의 정확도 비교
- 체크리스트 항목의 완전성 비교

---

### 6.5 모드별 상세 검증 항목

각 모드에서 검증하는 구체적인 항목들:

#### standard 모드 검증 항목

| 기준 | 검증 내용 | 출력 심각도 |
|-----|----------|------------|
| **명확성** | 목적과 핵심 메시지 명확성 | WARNING |
| **완전성** | 필요한 섹션/정보 포함 여부 | CRITICAL |
| **정확성** | 기술 내용 정확성 (기본) | CRITICAL |
| **일관성** | 용어/서식 일관성 | WARNING |
| **가독성** | 문장 명확성, 읽기 쉬움 | INFO |
| **구조** | 섹션 계층과 흐름 | WARNING |

#### technical 모드 검증 항목

| 검증 영역 | 구체적 항목 | 출력 심각도 |
|----------|-----------|------------|
| **코드 예제** | 문법 오류, 실행 가능성 | CRITICAL |
| **용어 정의** | 미정의 기술 용어 | WARNING |
| **용어 일관성** | 동일 개념의 다른 표현 | WARNING |
| **버전 정보** | 오래된 API/라이브러리 참조 | WARNING |
| **환경 설정** | 누락된 의존성/설정 | CRITICAL |

#### critical 모드 검증 항목

| 검증 영역 | 구체적 항목 | 출력 심각도 |
|----------|-----------|------------|
| **가정 발굴** | 명시되지 않은 전제조건 | CRITICAL |
| **실패 시나리오** | 예외 상황/에러 처리 누락 | WARNING |
| **논리적 일관성** | 모순된 주장/설명 | CRITICAL |
| **과장된 주장** | 검증되지 않은 성능/효과 | WARNING |
| **경계 조건** | 극단적인 입력/상태 처리 | INFO |

#### reader 모드 검증 항목

| 검증 영역 | 구체적 항목 | 출력 심각도 |
|----------|-----------|------------|
| **선행지식** | 예상 독자 수준과의 괴리 | WARNING |
| **단계별 설명** | 복잡한 과정의 누락 | WARNING |
| **실용성** | 즉시 적용 가능한 정보 | INFO |
| **예시 관련성** | 독자의 실제 상황과의 연결 | INFO |
| **시간 대비 가치** | 읽는 시간에 비해 얻는 가치 | INFO |

#### structure 모드 검증 항목

| 검증 영역 | 구체적 항목 | 출력 심각도 |
|----------|-----------|------------|
| **섹션 중복** | 내용/개념 중복 | WARNING |
| **계층 일관성** | 헤딩 레벨 논리성 | WARNING |
| **흐름** | 섹션 간 자연스러운 전환 | INFO |
| **균형** | 섹션별 길이/중요도 적절성 | INFO |
| **네비게이션** | 목차/링크의 유용성 | INFO |

---

### 6.6 에러 처리 및 에스컬레이션

#### 에이전트 레벨 에러 처리

```yaml
error_handling:
  invalid_mode:
    action: "fallback_to_standard"
    log: true
    notify_orchestrator: true

  file_not_found:
    action: "report_error_and_exit"
    error_message: "입력 파일을 찾을 수 없습니다: ${FILE_PATH}"

  parsing_error:
    action: "skip_and_continue"
    skip_section: true
    log: "섹션 파싱 실패, 건초로 처리"

  timeout:
    action: "return_partial_results"
    partial_output: true
    note: "시간 초과로 일부 검증 생략"
```

#### 오케스트레이터 에스컬레이션 조건

| 상황 | 에스컬레이션 대상 | 조치 |
|-----|-----------------|------|
| 다중 모드 결과 충돌 | 사용자 | 충돌 목록 제시, 선택 요청 |
| 에이전트 실행 실패 | 사용자 | 대체 에이전트 제안 또는 수동 처리 |
| 한국어 감지 실패 | reviewer | 수동 언어 선택 요청 |
| 출력 형식 오류 | 개발자 | 버그 리포트 생성 |

---

### 6.7 성공 기준 (Definition of Done)

#### reviewer.md 성공 기준

- [ ] 5가지 모드 모두 독립적으로 실행 가능
- [ ] 각 모드가 정의된 출력 형식 준수
- [ ] 한국어 문서 자동 감지 및 언어 검증 활성화
- [ ] comprehensive 모드에서 결과 통합 정상 동작
- [ ] 기존 critic vs reviewer(mode: critical) 결과 90% 이상 일치

#### writer.md 성공 기준

- [ ] 초안 작성 시 원본 파일 미변경
- [ ] 피드백 기반 개선 시 체크리스트 항목 100% 반영
- [ ] 작성자 원래 의도/톤 유지
- [ ] `${CLAUDE_TMP_DIR}/draft.md`에 최종 결과 출력

#### visual.md 성공 기준

- [ ] 유효한 Mermaid 문법 출력
- [ ] 라이트/다크 테마 모두 가독성 확보
- [ ] 모바일 뷰 고려한 다이어그램 너비 제한
- [ ] 다이어그램에 대한 텍스트 설명 포함

#### 통합 성공 기준

- [ ] 4개 에이전트가 오케스트레이션 스킬에서 순차/병렬 실행 가능
- [ ] 결과 파일이 표준 경로에 정상 출력
- [ ] 기존 blog/ 8개 에이전트 워크플로우와 동등한 결과 생성

---

### 6.8 유지보수 지침

#### 버전 관리

```
reviewer.md
├── v1.0: 5가지 기본 모드
├── v1.1: comprehensive 모드 추가 (예정)
├── v1.2: 신규 모드 추가 가능 (접근성 모드 등)
└── CHANGELOG.md: 각 버전별 변경사항 기록
```

#### 모드 추가 절차

1. 새로운 모드 필요성 검토 (기존 모드로 커버 가능한지 확인)
2. 모드 이름, 목적, 출력 형식 정의
3. reviewer.md에 모드 섹션 추가
4. 오케스트레이션 스킬에 모드 등록
5. 테스트 및 문서화

---

## 11. 리스크 및 대응

| 리스크 | 영향 | 대응 방안 |
|-------|------|----------|
| 통합 reviewer가 너무 복잡해짐 | 중간 | 모드별 섹션을 명확히 분리, 선택적 실행 지원 |
| 기존 에이전트와 출력 형식 차이 | 낮음 | 체크리스트 기반 통일된 형식 사용 |
| 한국어/영문 검증 로직 충돌 | 중간 | 언어 감지 후 조걶 로직 적용 |
| 오케스트레이션 스킬 의존성 증가 | 중간 | 에이전트 단독 사용도 지원 |

---

## 7. 파일 경로 및 작업 공간 규약

### 7.1 통합 에이전트 파일 경로

```
.claude/agents/docs/
├── reviewer.md          # 통합 검증 에이전트
├── writer.md            # 작성/개선 에이전트
├── visual.md            # 시각화 에이전트
└── translator.md        # 번역 에이전트
```

### 7.2 임시 작업 공간 규약

모든 통합 에이전트는 `${CLAUDE_TMP_DIR}`을 작업 공간으로 사용:

| 에이전트 | 입력 파일 | 출력 파일 | 작업 파일 |
|---------|----------|----------|----------|
| `reviewer` | `${ARGUMENTS}` 또는 `${INPUT_PATH}` | `${CLAUDE_TMP_DIR}/review_result.md` | `${CLAUDE_TMP_DIR}/review_temp/` |
| `writer` | `${ARGUMENTS}` (원본 경로) | `${CLAUDE_TMP_DIR}/draft.md` | `${CLAUDE_TMP_DIR}/writer_workspace/` |
| `visual` | `${ARGUMENTS}` | `${CLAUDE_TMP_DIR}/diagram_review.md` | `${CLAUDE_TMP_DIR}/visual_temp/` |
| `translator` | 원문 URL 또는 파일 | `ai/docs/claude/docs/{filename}.md` | `${CLAUDE_TMP_DIR}/translator_temp/` |

### 7.3 에이전트 간 통신 프로토콜

#### Task 기반 통신 (스킬 레벨)

```yaml
# 오케스트레이션 스킬에서 TaskCreate로 에이전트 호출
task:
  type: agent_review
  agent: reviewer
  parameters:
    mode: "technical"
    input: "${FILE_PATH}"
  output_capture: "${CLAUDE_TMP_DIR}/result.md"
```

#### SendMessage 기반 통신 (에이전트 간 직접)

```yaml
# 에이전트가 다른 에이전트에게 메시지 전송
message:
  to: "reviewer"
  type: "review_request"
  payload:
    mode: "critical"
    content: "${CONTENT}"
  callback: "${CURRENT_AGENT}"
```

### 7.4 결과 파일 표준 형식

#### reviewer 출력 형식

```
${CLAUDE_TMP_DIR}/
├── review_result.md              # 최종 검증 리포트
├── review_temp/
│   ├── mode_standard.md          # standard 모드 결과 (임시)
│   ├── mode_technical.md         # technical 모드 결과 (임시)
│   └── merged_report.md          # 통합 리포트 (comprehensive 시)
```

#### writer 출력 형식

```
${CLAUDE_TMP_DIR}/
├── draft.md                      # 최종 초안
├── writer_workspace/
│   ├── version_1.md              # 초기 버전
│   ├── version_2.md              # 개선 버전
│   └── feedback_applied.md       # 피드백 반영 버전
```

---

## 8. Skills 연동 상세 설계

### 8.1 docs Skill 구조

```
.claude/skills/docs/
├── SKILL.md                      # 스킬 메타 정보
├── config.yaml                   # 에이전트 매핑 설정
├── workflows/
│   ├── blog-review.yaml          # 블로그 검증 워크플로우
│   ├── korean-doc.yaml           # 한국어 문서 검증 워크플로우
│   └── write-validate.yaml       # 작성-검증 피드백 루프
└── templates/
    ├── review_prompt.md          # reviewer 호출 프롬프트 템플릿
    └── aggregate_results.md      # 결과 통합 템플릿
```

### 8.2 config.yaml 예시

```yaml
skill_name: docs
version: 2.0

# 에이전트 매핑
agents:
  reviewer:
    path: ".claude/agents/docs/reviewer.md"
    supported_modes:
      - standard
      - technical
      - critical
      - reader
      - structure
      - comprehensive
    default_mode: standard

  writer:
    path: ".claude/agents/docs/writer.md"
    actions:
      - draft
      - revise

  visual:
    path: ".claude/agents/docs/visual.md"

  translator:
    path: ".claude/agents/docs/translator.md"

# 워크플로우 정의
workflows:
  blog-review:
    description: "블로그 콘텐츠 멀티 모드 검증"
    steps:
      - agent: reviewer
        mode: technical
      - agent: reviewer
        mode: critical
      - agent: reviewer
        mode: reader
      - agent: visual
        condition: "has_diagrams"
    result_aggregation: "priority"

  korean-doc-validation:
    description: "한국어 문서 종합 검증"
    steps:
      - agent: reviewer
        mode: comprehensive
        language: auto
    result_aggregation: "none"
```

### 8.3 결과 통합 전략

#### Priority 기반 통합

```yaml
# 심각도 우선순위로 결과 통합
aggregation:
  type: priority
  levels:
    CRITICAL: 5    # critical 모드에서 발견된 CRITICAL 항목
    WARNING: 3     # technical/reader 모드에서 발견된 WARNING
    INFO: 1        # standard 모드에서 발견된 INFO
  conflict_resolution: "higher_priority_wins"
```

#### Merge 기반 통합

```yaml
# 모든 결과를 카테고리별로 병합
aggregation:
  type: merge
  categories:
    - technical_accuracy    # technical 모드 결과
    - logical_validity      # critical 모드 결과
    - reader_experience     # reader 모드 결과
    - document_structure    # structure 모드 결과
    - basic_quality         # standard 모드 결과
```

---

## 9. 구현 체크리스트

### Phase 0: 준비
- [ ] `.claude/agents/docs/` 구조 확인
- [ ] 기존 에이전트 문서 백업
- [ ] newsfeed 스킬로 기존 에이전트 유효성 검증

### Phase 1: 설계 확정
- [ ] reviewer.md 상세 명세 확정
- [ ] 각 모드별 출력 형식 확정
- [ ] 오케스트레이션 스킬 인터페이스 설계
- [ ] 파일 경로 및 작업 공간 규약 확정

### Phase 2: 통합 에이전트 작성
- [ ] `reviewer.md` 작성 (5가지 모드 포함)
- [ ] `writer.md` 작성 (기존 doc-writer 기반)
- [ ] `visual.md` 작성 (diagram-designer 기반)
- [ ] `translator.md` 경로 이동 (`.claude/agents/docs/`)

### Phase 3: 오케스트레이션 스킬 통합
- [ ] `.claude/skills/docs/config.yaml` 작성
- [ ] 워크플로우 YAML 파일 작성
- [ ] 스킬에서 reviewer 모드 호출 로직 구현
- [ ] 다중 모드 순차/병렬 실행 지원
- [ ] 결과 통합 및 우선순위화 로직

### Phase 4: 테스트 및 검증
- [ ] 각 모드별 출력 검증
- [ ] 오케스트레이션 워크플로우 테스트
- [ ] 기존 blog/ 에이전트와 결과 비교
- [ ] 한국어/영문 문서 모두 테스트

### Phase 5: 문서화
- [ ] 통합 에이전트 사용 가이드 작성
- [ ] 오케스트레이션 스킬 문서 업데이트
- [ ] `.claude/docs/plans/`에 구현 노트 작성

---

## 12. 작업 테스크

### 테스크 목록

| ID | 테스크 | 상태 | 의존성 |
|-----|-------|------|--------|
| #1 | Phase 0: 프로젝트 준비 및 설정 분석 | pending | - |
| #2 | Phase 1: reviewer.md 설계 및 명세 확정 | pending | #1 |
| #3 | Phase 2a: writer.md 작성 | pending | #2 |
| #4 | Phase 2b: visual.md 작성 | pending | #2 |
| #5 | Phase 2c: translator.md 작성 및 설정 연동 | pending | #2 |
| #6 | Phase 3: 오케스트레이션 스킬 통합 | pending | #3, #4, #5 |
| #7 | Phase 4: 테스트 및 검증 | pending | #6 |
| #8 | Phase 5: 문서화 및 마무리 | pending | #7 |

### 테스크 팀
- **팀명:** agent-consolidation
- **팀 파일:** `~/.claude/teams/agent-consolidation/config.json`
- **테스크 디렉토리:** `~/.claude/tasks/agent-consolidation/`

---

## 10. 결론

### 통합 성과

| 항목 | Before | After |
|-----|--------|-------|
| 에이전트 수 | 11개 | 4개 |
| 오케스트레이션 복잡도 | 높음 (에이전트 선택) | 낮음 (모드 선택) |
| 출력 형식 일관성 | 낮음 | 높음 (통일 체크리스트) |
| 확장성 | 낮음 (에이전트 추가) | 높음 (모드 추가) |

### 다음 행동

1. 본 계획서 검토 및 승인
2. Phase 1 (설계 확정) 시작
3. reviewer.md 초안 작성

---

## 부록

### A. 기존 에이전트와의 호환성

| 기존 사용 방식 | 통합 후 대안 | 호환성 | 상태 |
|-------------|-------------|--------|------|
| `doc-reviewer` 단독 사용 | `reviewer(mode: standard)` | ✅ 완전 호환 | 유지 |
| `tech-writer` 단독 사용 | `reviewer(mode: technical)` | ✅ 기능 동등 | ~~삭제됨~~ |
| `critic` 단독 사용 | `reviewer(mode: critical)` | ✅ 기능 동등 | ~~삭제됨~~ |
| `reader-advocate` 단독 사용 | `reviewer(mode: reader)` | ✅ 기능 동등 | ~~삭제됨~~ |
| `content-strategist` 단독 사용 | `reviewer(mode: structure)` | ✅ 기능 동등 | ~~삭제됨~~ |
| `blog-editor` 단독 사용 | `reviewer(mode: standard)` + 게시 체크리스트 | ⚠️ 부분 변경 | ~~삭제됨~~ |
| Blue/Red 팀 전체 워크플로우 | 오케스트레이션 스킬에서 순차 호출 | ⚠️ 스킬 의존 | 통합됨 |

### B. 마이그레이션 가이드 (선택)

기존 `blog/` 에이전트를 통합 구조로 전환하고 싶은 경우:

1. ~~**개별 에이전트 유지**: 기존 방식 그대로 사용 가능 (blog/ 에이전트 삭제되지 않음)~~
2. **통합 에이전트 사용**: `.claude/agents/docs/reviewer.md`의 모드 기반 검증 사용
3. **기존 스킬 업데이트**: 에이전트 호출 부분을 모드 호출로 변경

### C. 참조 자료

- 기존 에이전트 문서: ~~`.claude/agents/blog/*.md`~~ (삭제됨), `.claude/agents/docs/*.md`
- 오케스트레이션 스킬 예시: `.claude/skills/docs/SKILL.md`
- Claude Code 에이전트 가이드: 공식 문서

### D. 용어 정의

| 용어 | 정의 |
|-----|------|
| **모드 (Mode)** | 통합 reviewer의 검증 관점/방식 (standard, technical 등) |
| **오케스트레이션** | 다중 에이전트/모드의 실행 순서와 통합을 제어하는 스킬 |
| **체크리스트** | 마크다운 `- [ ]` 형식의 검증 항목 목록 |
| **Blue Team** | 작성/개선 관점의 에이전트 그룹 (tech-writer, content-strategist 등) |
| **Red Team** | 검증/비판 관점의 에이전트 그룹 (critic, reader-advocate 등) |

---

**작성일:** 2026-03-21
**상태:** 초안
**버전:** 1.0

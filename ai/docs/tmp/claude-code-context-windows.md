# Claude Code Context Window

## 컨텍스트 윈도우란?

"컨텍스트 윈도우"는 언어 모델이 새로운 텍스트를 생성할 때 참조하고 되돌아볼 수 있는 텍스트의 전체 양과 생성하는 새 텍스트를 합한 것을 의미합니다.

- 모델의 **"작업 메모리"**를 나타냅니다 (훈련 데이터와는 다름)
- 더 큰 컨텍스트 윈도우는 모델이 더 복잡하고 긴 프롬프트를 이해하고 응답할 수 있게 합니다
- Claude Code는 **200K 토큰**(표준), 최대 **1M 토큰**(베타) 지원

## 기존 RAG 방식과의 차이

기존 대화형 AI는 학습된 모델(LLM)의 지식에 대해서만 답변하는 한계를 극복하기 위해 RAG 방식을 도입했습니다.

RAG 방식은 외부 지식베이스에서 관련 정보를 검색하여 프롬프트에 포함시킨 후, 이를 바탕으로 응답을 생성하는 방식입니다.

> **RAG(Retrieval-Augmented Generation)** 란 학습 모델의 제한된 정보를 외부 데이터 검색(정보 검색)과 생성 모델을 결합한 자연어 처리 기술입니다.

## Claude Code의 컨텍스트 처리 방식

이러한 RAG 방식은 긴 컨텍스트나 소스 코드 생성/분석에 한계가 있었습니다.

예를 들어, 대화 흐름에 맞게 답변하려면 앞서 나눈 대화를 모두 이해해야 하는데, 컨텍스트가 짧으면 LLM은 일관성 없는 정보를 산출할 수 있습니다.

이와 달리 Claude Code는 **큰 컨텍스트 윈도우**를 활용하여:

- 대화 전체와 긴 문서, 코드베이스를 **한 번에 처리**
- 긴 소스 코드 분석 및 생성
- 긴 대화 맥락 유지를 효과적으로 수행할 수 있습니다.

## 컨텍스트 윈도우 작동 방식

Claude Code에서 컨텍스트 윈도우는 다음과 같이 작동합니다:

| 특징 | 설명 |
|-----|------|
| **점진적 토큰 축적** | 대화가 턴을 통해 진행됨에 따라 각 사용자 메시지와 어시스턴트 응답이 컨텍스트 윈도우 내에 축적됨 |
| **선형 성장 패턴** | 컨텍스트 사용량은 각 턴에 따라 선형적으로 증가하며, 이전 턴은 완전히 보존 |
| **200K 토큰 용량** | 총 사용 가능한 컨텍스트 윈도우(200,000 토큰)는 대화 히스토리를 저장하고 새로운 출력을 생성하기 위한 최대 용량 |
| **입력-출력 흐름** | 각 턴은 입력 단계(이전 대화 히스토리와 현재 메시지)와 출력 단계(텍스트 응답 생성)로 구성 |

## 컨텍스트 윈도우 구성 요소

### System Prompt (시스템 프롬프트)

모든 요청의 시작 부분에 포함되는 지침 및 설정입니다.

- 모델의 역할과 동작 방식 정의
- Claude Code의 경우 CLAUDE.md, 프로젝트 규칙 등이 포함
- API 사용 시 `system` 매개변수로 전달

### Messages (메시지 / 대화 히스토리)

이전 턴들의 사용자 메시지와 어시스턴트 응답이 순차적으로 누적됩니다.

- **완전 보존**: 이전 턴의 대화 내용은 완전히 보존
- **선형 성장**: 대화가 진행됨에 따라 토큰이 선형적으로 누적
- **입력-출력 순환**: 각 턴의 출력은 다음 턴의 입력에 포함

### Tool Results (도구 결과)

도구 사용 시 해당 턴에 포함된 도구 호출 및 결과입니다.

- 파일 내용 읽기/쓰기 결과
- API 응답 데이터
- 코드 실행 결과
- MCP 도구 출력

> **참고**: [컨텍스트 편집](#컨텍스트-편집context-editing)을 통해 오래된 도구 결과를 자동으로 관리할 수 있습니다.

## 최신 모델의 컨텍스트 윈도우 관리

최신 Claude 모델(Claude Sonnet 3.7부터)에서는 프롬프트 토큰과 출력 토큰의 합이 모델의 컨텍스트 윈도우를 초과하면 **시스템이 컨텍스트를 조용히 자르는 대신 유효성 검사 오류를 반환**합니다.

이 변경 사항은 더 예측 가능한 동작을 제공하지만 더 신중한 토큰 관리가 필요합니다.

## 토큰 계산 API

메시지를 Claude에 보내기 전에 토큰 수를 예상할 수 있습니다.

**엔드포인트:** `POST /v1/messages/count_tokens`

**사용 예시:**

```bash
curl https://api.anthropic.com/v1/messages/count_tokens \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5",
    "messages": [{"role": "user", "content": "Hello"}]
  }'
```

**컨텍스트 편집과 함께 사용:**
컨텍스트 편집이 적용된 후의 최종 토큰 수와 원래 토큰 수를 모두 확인할 수 있습니다.

```json
{
  "input_tokens": 25000,
  "context_management": {
    "original_input_tokens": 70000
  }
}
```

## 확장된 사고(Extended Thinking)와 컨텍스트 윈도우

확장된 사고를 사용할 때, 사고에 사용되는 토큰을 포함한 모든 입력 및 출력 토큰이 컨텍스트 윈도우 제한에 포함됩니다.

**중요한 특징:**
- 사고 예산 토큰은 `max_tokens` 매개변수의 하위 집합이며, 출력 토큰으로 청구됩니다
- **이전 사고 블록은 자동 제거**: Claude API는 이전 턴의 사고 블록을 컨텍스트 윈도우 계산에서 자동으로 제거하여 실제 대화 콘텐츠를 위한 토큰 용량을 보존합니다
- 도구 사용과 결합할 때는 사고 블록 보존 규칙이 적용됩니다

**확장된 사고 제거:**
- 확장된 사고 블록은 각 턴의 출력 단계에서 생성되지만, 후속 턴의 입력 토큰으로 전달되지 않습니다
- 사고 블록을 직접 제거할 필요가 없습니다
- 사고 토큰에는 `thinking` 블록과 `redacted_thinking` 블록이 모두 포함됩니다

## 1M 토큰 컨텍스트 윈도우

Claude Sonnet 4와 4.5는 **100만 토큰** 컨텍스트 윈도우를 지원합니다.

| 항목 | 내용 |
|-----|------|
| **지원 모델** | Claude Sonnet 4, Claude Sonnet 4.5 |
| **사용 등급** | 사용 등급 4 또는 커스텀 속도 제한이 있는 조직 (베타) |
| **베타 헤더** | `context-1m-2025-08-07` |
| **가격** | 200K 토큰 초과 시 프리미엄 요금 (입력 2배, 출력 1.5배) |

**활성화 예시:**
```bash
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: context-1m-2025-08-07" \
  -d '{...}'
```

**중요한 고려 사항:**
- **베타 상태**: 기능과 가격은 향후 릴리스에서 수정되거나 제거될 수 있습니다
- **속도 제한**: 긴 컨텍스트 요청에는 전용 속도 제한이 적용됩니다
- **멀티모달 고려 사항**: 많은 수의 이미지 또는 PDF를 처리할 때 요청 크기 제한에 도달할 수 있습니다

## 컨텍스트 인식(Context Awareness)

Claude Sonnet 4.5와 Claude Haiku 4.5는 **컨텍스트 인식** 기능을 갖추고 있습니다.

**작동 방식:**
- 대화 시작 시 `<budget:token_budget>200000</budget:token_budget>` 형태로 전체 컨텍스트 윈도우 정보 수신
- 각 도구 호출 후 `<system_warning>Token usage: 35000/200000; 165000 remaining</system_warning>` 형태로 남은 용량 업데이트 수신
- Claude가 작업할 공간을 정확히 이해하여 더 효과적인 실행 가능

**이점:**
- 장기 실행 에이전트 세션에서 지속적인 집중 유지
- 다중 컨텍스트 윈도우 워크플로의 상태 전환 관리
- 복잡한 작업에서 신중한 토큰 관리

## 컨텍스트 편집(Context Editing)

대화 컨텍스트가 증가함에 따라 자동으로 관리하는 기능입니다.

| 전략 | 설명 |
|-----|------|
| **도구 결과 지우기** (`clear_tool_uses_20250919`) | 임계값 초과 시 오래된 도구 결과 자동 제거 |
| **Thinking 블록 지우기** (`clear_thinking_20251015`) | extended thinking 활성화 시 thinking 블록 관리 |
| **클리언트 측 압축** | SDK에서 요약 생성 후 전체 기록 대체 |

**베타 헤더:** `context-management-2025-06-27`

### 컨텍스트 편집 구성 옵션

**도구 결과 지우기 (`clear_tool_uses_20250919`):**

| 구성 옵션 | 기본값 | 설명 |
|---------|-------|------|
| `trigger` | 100,000 입력 토큰 | 활성화 임계값 (input_tokens 또는 tool_uses) |
| `keep` | 3개 도구 사용 | 지우기 후 유지할 최근 도구 사용/결과 쌍 수 |
| `clear_at_least` | None | 매번 지워지는 최소 토큰 수 |
| `exclude_tools` | None | 지우지 않을 도구 이름 목록 |
| `clear_tool_inputs` | `false` | 도구 호출 매개변수도 함께 지울지 여부 |

**Thinking 블록 지우기 (`clear_thinking_20251015`):**

| 구성 옵션 | 기본값 | 설명 |
|---------|-------|------|
| `keep` | `{type: "thinking_turns", value: 1}` | 유지할 thinking 블록이 있는 최근 어시스턴트 턴 수 |

**전략 결합 예시:**
```json
{
  "context_management": {
    "edits": [
      {
        "type": "clear_thinking_20251015",
        "keep": {"type": "thinking_turns", "value": 2}
      },
      {
        "type": "clear_tool_uses_20250919",
        "trigger": {"type": "input_tokens", "value": 50000},
        "keep": {"type": "tool_uses", "value": 5}
      }
    ]
  }
}
```

> **주의:** 여러 전략을 사용할 때 `clear_thinking_20251015` 전략은 `edits` 배열에서 먼저 나엶되어야 합니다.

### 컨텍스트 편집과 프롬프트 캐싱

컨텍스트 편집과 프롬프트 캐싱의 상호 작용은 전략에 따라 다릅니다:

- **도구 결과 지우기**: 콘텐츠가 지워질 때 캐시된 프롬프트 접두사를 무효화합니다. `clear_at_least` 매개변수를 사용하여 캐시 무효화를 가치 있게 만들기 위해 충분한 토큰을 지우는 것이 좋습니다.
- **Thinking 블록 지우기**: thinking 블록이 컨텍스트에 유지되면 프롬프트 캐시가 보존 되어 캐시 히트를 활성화하고 입력 토큰 비용을 줄입니다.

### Memory Tool과 함께 사용

컨텍스트 편집은 **Memory Tool**과 결합할 수 있습니다.

**동작 방식:**
- 대화 컨텍스트가 지우기 임계값에 가까워지면 Claude는 중요한 정보 보존 경고 수신
- Claude는 도구 결과가 지워지기 전에 필수 정보를 메모리 파일에 저장 가능
- 필요할 때 메모리 파일에서 이전에 지워진 정보 조회 가능

**이점:**
- 중요한 컨텍스트 보존
- 장기 실행 워크플로 유지 (영구 저장소에 오프로드)
- 컨텍스트 제한 초과 방지

## 클라이언트 측 압축 (SDK)

> [`tool_runner`](https://platform.claude.com/docs/en/api/tools-implementing)를 사용할 때 [Python 및 TypeScript SDK](https://platform.claude.com/docs/en/api/client-sdks)에서 사용 가능합니다.

압축은 토큰 사용량이 너무 커질 때 요약을 생성하여 대화 컨텍스트를 자동으로 관리하는 SDK 기능입니다.

### 작동 방식

1. **임계값 확인**: 총 토큰 = `input_tokens + cache_creation_input_tokens + cache_read_input_tokens + output_tokens`
2. **요약 생성**: 임계값 초과 시 Claude가 `<summary></summary>` 태그로 래핑된 구조화된 요약 생성
3. **컨텍스트 대체**: 전체 메시지 기록을 요약으로 대체
4. **계속**: 대화는 요약에서 재개

### 구성 옵션

| 매개변수 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|-----|--------|------|
| `enabled` | boolean | 예 | - | 자동 압축 활성화 여부 |
| `context_token_threshold` | number | 아니요 | 100,000 | 압축 트리거 토큰 수 |
| `model` | string | 아니요 | 메인 모델과 동일 | 요약 생성에 사용할 모델 |
| `summary_prompt` | string | 아니요 | 내장 프롬프트 | 요약 생성을 위한 사용자 정의 프롬프트 |

### 사용 예시 (Python)

```python
import anthropic

client = anthropic.Anthropic()

runner = client.beta.messages.tool_runner(
    model="claude-sonnet-4-5",
    max_tokens=4096,
    tools=[...],
    messages=[{"role": "user", "content": "Analyze all files..."}],
    compaction_control={
        "enabled": True,
        "context_token_threshold": 100000
    }
)

final = runner.until_done()
```

### 요약 구조

기본 요약 프롬프트는 다음을 포함합니다:

1. **작업 개요**: 사용자의 핵심 요청, 성공 기준 및 제약 조건
2. **현재 상태**: 완료된 것, 생성/수정된 파일, 주요 출력
3. **중요한 발견**: 기술적 제약, 내린 결정, 해결된 오류
4. **다음 단계**: 필요한 작업, 차단 요소, 우선 순위
5. **보존할 컨텍스트**: 사용자 선호 사항, 도메인별 세부 정보, 한 약속

### 사용 권장 사항

**좋은 사용 사례:**
- 많은 파일/데이터 소스를 처리하는 장기 실행 에이전트 작업
- 많은 양의 정보를 축적하는 연구 워크플로
- 명확하고 측정 가능한 진행 상황이 있는 다단계 작업

**덜 이상적인 사용 사례:**
- 초기 대화 세부 정보의 정확한 회상이 필요한 작업
- 서버 측 도구를 광범위하게 사용하는 워크플로

## 긴 컨텍스트 프롬프팅 팁

- **긴 형식 데이터를 상단에 배치**: 긴 문서와 입력(~20K+ 토큰)을 프롬프트 상단, 질문/지시사항보다 위에 배치 (응답 품질 최대 30% 향상)
- **XML 태그로 문서 구조화**: `<document>`, `<document_content>`, `<source>` 태그 사용
- **인용문으로 응답 뒷받침**: 긴 문서 작업 시 관련 부분을 먼저 인용하도록 요청

## 요약

Claude Code의 큰 컨텍스트 윈도우(200K/1M 토큰)를 통해 사용자는 복잡한 작업을 효율적으로 수행할 수 있습니다.
컨텍스트 윈도우는 모델의 "작업 메모리"로, 훈련 데이터와는 별개로 실제 추론 시 참조할 수 있는 텍스트의 양을 결정합니다.

최신 모델(Claude 4.5)의 컨텍스트 인식 기능과 컨텍스트 편집 기능을 활용하면 더 효율적으로 토큰을 관리할 수 있습니다.

---

## 모델별 컨텍스트 윈도우 크기

| 모델 | 컨텍스트 윈도우 | 특징 |
|-----|---------------|------|
| Claude Opus 4.5 | 200K | 최고 성능, 컨텍스트 인식 |
| Claude Opus 4.1 | 200K | 고성능 추론 |
| Claude Opus 4 | 200K | 복잡한 작업에 최적 |
| Claude Sonnet 4.5 | 200K / 1M (베타) | 컨텍스트 인식, 장문 컨텍스트 지원 |
| Claude Sonnet 4 | 200K / 1M (베타) | 균형 잡힌 성능 |
| Claude Haiku 4.5 | 200K | 경량, 컨텍스트 인식 |
| Claude Sonnet 3.7 | 200K | 이전 세대 |

## 컨텍스트 편집 응답 필드

컨텍스트 편집이 적용되면 응답에 `context_management` 필드가 포함됩니다:

```json
{
  "context_management": {
    "applied_edits": [
      {
        "type": "clear_tool_uses_20250919",
        "cleared_tool_uses": 8,
        "cleared_input_tokens": 50000
      },
      {
        "type": "clear_thinking_20251015",
        "cleared_thinking_turns": 3,
        "cleared_input_tokens": 15000
      }
    ]
  }
}
```

## 참고자료

- [공식 문서: Context Windows](https://platform.claude.com/docs/en/build-with-claude/context-windows)
- [공식 문서: Context Editing](https://platform.claude.com/docs/en/build-with-claude/context-editing)
- [공식 문서: Long Context Tips](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/long-context-tips)
- [공식 문서: Extended Thinking](https://platform.claude.com/docs/en/build-with-claude/extended-thinking)
- [공식 문서: Token Counting](https://platform.claude.com/docs/en/build-with-claude/token-counting)

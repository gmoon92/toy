# [Effort](https://platform.claude.com/docs/en/build-with-claude/effort)

effort 매개변수로 Claude가 응답할 때 사용하는 토큰 수를 제어하여 응답 완성도와 토큰 효율성 사이의 균형을 조정합니다.

---

effort 매개변수를 사용하면 Claude가 요청에 응답할 때 토큰을 소비하는 것에 대해 얼마나 적극적인지 제어할 수 있습니다. 이를 통해 단일 모델로 응답 완성도와 토큰 효율성 사이의 균형을 조정할 수 있습니다.


> effort 매개변수는 현재 베타 버전이며 Claude Opus 4.5에서만 지원됩니다.
>
> 이 기능을 사용할 때 [베타 헤더](https://platform.claude.com/docs/en/api/beta-headers) `effort-2025-11-24`를 포함해야 합니다.


## Effort 작동 방식

기본적으로 Claude는 최대한의 노력을 사용하여 최상의 결과를 위해 필요한 만큼 많은 토큰을 소비합니다. effort 레벨을 낮추면 Claude에게 토큰 사용에 더 보수적이 되도록 지시하여 일부 능력 감소를 수용하면서 속도와 비용을 최적화할 수 있습니다.


> `effort`를 `"high"`로 설정하면 `effort` 매개변수를 완전히 생략하는 것과 정확히 동일한 동작을 생성합니다.


effort 매개변수는 응답의 **모든 토큰**에 영향을 줍니다. 여기에는 다음이 포함됩니다:

- 텍스트 응답 및 설명
- 도구 호출 및 함수 인수
- Extended thinking (활성화된 경우)

이 접근 방식에는 두 가지 주요 장점이 있습니다:

1. 사용하기 위해 thinking을 활성화할 필요가 없습니다.
2. 도구 호출을 포함한 모든 토큰 지출에 영향을 줄 수 있습니다. 예를 들어, 낮은 effort는 Claude가 더 적은 도구 호출을 한다는 것을 의미합니다. 이는 효율성에 대해 훨씬 더 높은 수준의 제어를 제공합니다.

### Effort 레벨

| 레벨    | 설명                                                                                                                      | 일반적인 사용 사례                                                                      |
| -------- | -------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------- |
| `high`   | 최대 능력. Claude는 최상의 결과를 위해 필요한 만큼 많은 토큰을 사용합니다. 매개변수를 설정하지 않는 것과 동일합니다.  | 복잡한 추론, 어려운 코딩 문제, 에이전틱 작업                           |
| `medium` | 적당한 토큰 절약을 가진 균형 잡힌 접근 방식. | 속도, 비용 및 성능의 균형이 필요한 에이전틱 작업                                                         |
| `low`    | 가장 효율적. 일부 능력 감소와 함께 상당한 토큰 절약. | 서브에이전트와 같이 최상의 속도와 최저 비용이 필요한 간단한 작업                     |

## 기본 사용법

<CodeGroup>
```python Python
import anthropic

client = anthropic.Anthropic()

response = client.beta.messages.create(
    model="claude-opus-4-5-20251101",
    betas=["effort-2025-11-24"],
    max_tokens=4096,
    messages=[{
        "role": "user",
        "content": "마이크로서비스와 모놀리식 아키텍처 간의 트레이드오프를 분석하세요"
    }],
    output_config={
        "effort": "medium"
    }
)

print(response.content[0].text)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const client = new Anthropic();

const response = await client.beta.messages.create({
  model: "claude-opus-4-5-20251101",
  betas: ["effort-2025-11-24"],
  max_tokens: 4096,
  messages: [{
    role: "user",
    content: "마이크로서비스와 모놀리식 아키텍처 간의 트레이드오프를 분석하세요"
  }],
  output_config: {
    effort: "medium"
  }
});

console.log(response.content[0].text);
```

```bash Shell
curl https://api.anthropic.com/v1/messages \
    --header "x-api-key: $ANTHROPIC_API_KEY" \
    --header "anthropic-version: 2023-06-01" \
    --header "anthropic-beta: effort-2025-11-24" \
    --header "content-type: application/json" \
    --data '{
        "model": "claude-opus-4-5-20251101",
        "max_tokens": 4096,
        "messages": [{
            "role": "user",
            "content": "마이크로서비스와 모놀리식 아키텍처 간의 트레이드오프를 분석하세요"
        }],
        "output_config": {
            "effort": "medium"
        }
    }'
```

</CodeGroup>

## Effort 매개변수를 조정해야 하는 경우는 언제인가요?

- Claude의 최상의 작업이 필요할 때 **high effort** (기본값)를 사용하세요. 복잡한 추론, 미묘한 분석, 어려운 코딩 문제 또는 품질이 최우선 순위인 모든 작업에 사용합니다.
- high effort의 전체 토큰 지출 없이 견고한 성능을 원할 때 **medium effort**를 균형 잡힌 옵션으로 사용하세요.
- 속도(Claude가 더 적은 토큰으로 답변하기 때문에) 또는 비용을 최적화할 때 **low effort**를 사용하세요. 예를 들어 간단한 분류 작업, 빠른 조회 또는 추가 지연 시간이나 비용을 정당화하지 않는 한계 품질 개선이 있는 대용량 사용 사례에 사용합니다.

## 도구 사용과 함께 Effort

도구를 사용할 때 effort 매개변수는 도구 호출 주변의 설명과 도구 호출 자체 모두에 영향을 줍니다. 낮은 effort 레벨은 다음과 같은 경향이 있습니다:

- 여러 작업을 더 적은 도구 호출로 결합
- 더 적은 도구 호출 수행
- 서문 없이 직접 작업으로 진행
- 완료 후 간결한 확인 메시지 사용

높은 effort 레벨은 다음과 같을 수 있습니다:

- 더 많은 도구 호출 수행
- 작업을 수행하기 전에 계획 설명
- 변경 사항에 대한 상세한 요약 제공
- 더 포괄적인 코드 주석 포함

## Extended thinking과 함께 Effort

effort 매개변수는 extended thinking이 활성화될 때 thinking 토큰 예산과 함께 작동합니다. 이 두 제어는 서로 다른 목적을 제공합니다:

- **Effort 매개변수**: Claude가 모든 토큰(thinking 토큰, 텍스트 응답 및 도구 호출 포함)을 소비하는 방법을 제어합니다
- **Thinking 토큰 예산**: 특히 thinking 토큰에 대한 최대 제한을 설정합니다

effort 매개변수는 extended thinking 활성화 여부와 관계없이 사용할 수 있습니다. 둘 다 구성된 경우:

1. 먼저 작업에 적합한 effort 레벨을 결정합니다
2. 그런 다음 작업 복잡도에 따라 thinking 토큰 예산을 설정합니다

복잡한 추론 작업에서 최상의 성능을 얻으려면 높은 thinking 토큰 예산과 함께 high effort (기본값)를 사용하세요. 이를 통해 Claude는 철저하게 생각하고 포괄적인 응답을 제공할 수 있습니다.

## 모범 사례

1. **high부터 시작**: 토큰 효율성을 위해 성능을 절충하려면 낮은 effort 레벨을 사용하세요.
2. **속도에 민감하거나 간단한 작업에는 low 사용**: 지연 시간이 중요하거나 작업이 간단한 경우 low effort는 응답 시간과 비용을 크게 줄일 수 있습니다.
3. **사용 사례 테스트**: effort 레벨의 영향은 작업 유형에 따라 다릅니다. 배포하기 전에 특정 사용 사례에서 성능을 평가하세요.
4. **동적 effort 고려**: 작업 복잡도에 따라 effort를 조정하세요. 간단한 쿼리는 low effort를 보증할 수 있지만 에이전틱 코딩 및 복잡한 추론은 high effort의 이점을 얻습니다.

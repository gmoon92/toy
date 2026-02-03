# [프롬프트 개선 도구로 프롬프트 최적화하기](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/prompt-improver)

---

> **참고**
>
> 프롬프트 개선 도구는 확장 사고(extended thinking) 기능을 가진 모델을 포함한 모든 Claude 모델과 호환됩니다. 확장 사고 모델에 특화된 프롬프팅 팁은 [여기](../02-capabilities/03-extended-thinking.md)를 참조하세요.

프롬프트 개선 도구는 자동화된 분석과 향상을 통해 프롬프트를 빠르게 반복하고 개선하는 데 도움을 줍니다. 높은 정확도가 요구되는 복잡한 작업에서 프롬프트를 더욱 견고하게 만드는 데 탁월합니다.

![프롬프트 개선 도구](/docs/images/prompt_improver.png)

## 시작하기 전에

다음이 필요합니다:
- 개선할 [프롬프트 템플릿](../08-prompt-engineering/03-use-prompt-templates.md)
- Claude의 현재 출력 결과에 대한 피드백 (선택사항이지만 권장됨)
- 예시 입력과 이상적인 출력 (선택사항이지만 권장됨)

## 프롬프트 개선 도구의 작동 방식

프롬프트 개선 도구는 4단계를 거쳐 프롬프트를 향상시킵니다:

1. **예시 식별**: 프롬프트 템플릿에서 예시를 찾아 추출합니다
2. **초안 작성**: 명확한 섹션과 XML 태그로 구조화된 템플릿을 생성합니다
3. **사고 연쇄 개선**: 상세한 추론 지침을 추가하고 다듬습니다
4. **예시 향상**: 새로운 추론 프로세스를 보여주도록 예시를 업데이트합니다

개선 모달에서 이러한 단계들이 실시간으로 진행되는 것을 확인할 수 있습니다.

## 제공되는 결과물

프롬프트 개선 도구는 다음과 같은 요소를 포함하는 템플릿을 생성합니다:
- Claude의 추론 과정을 안내하고 일반적으로 성능을 향상시키는 상세한 사고 연쇄(chain-of-thought) 지침
- 서로 다른 구성 요소를 분리하기 위해 XML 태그를 사용한 명확한 구조
- 입력에서 출력까지 단계별 추론을 보여주는 표준화된 예시 형식
- Claude의 초기 응답을 안내하는 전략적 미리채우기(prefills)

> **참고**
>
> 예시는 Workbench UI에서 별도로 표시되지만, 실제 API 호출에서는 첫 번째 사용자 메시지의 시작 부분에 포함됩니다. "**\<\/\> Get Code**"를 클릭하여 원시 형식을 보거나, Examples 박스를 통해 예시를 원시 텍스트로 삽입할 수 있습니다.

## 프롬프트 개선 도구 사용 방법

1. 프롬프트 템플릿을 제출합니다
2. Claude의 현재 출력에 대한 문제점 피드백을 추가합니다 (예: "요약이 전문가 청중에게는 너무 기초적입니다")
3. 예시 입력과 이상적인 출력을 포함합니다
4. 개선된 프롬프트를 검토합니다

## 테스트 예시 생성하기

아직 예시가 없으신가요? [테스트 케이스 생성기](../09-test-evaluate/03-eval-tool.md)를 사용하여:
1. 샘플 입력을 생성합니다
2. Claude의 응답을 받습니다
3. 이상적인 출력에 맞게 응답을 편집합니다
4. 다듬어진 예시를 프롬프트에 추가합니다

## 프롬프트 개선 도구를 사용해야 하는 경우

프롬프트 개선 도구는 다음과 같은 경우에 가장 효과적입니다:
- 상세한 추론이 필요한 복잡한 작업
- 속도보다 정확도가 더 중요한 상황
- Claude의 현재 출력에 상당한 개선이 필요한 문제

> **참고**
>
> 지연 시간이나 비용에 민감한 애플리케이션의 경우, 더 간단한 프롬프트 사용을 고려하세요. 프롬프트 개선 도구는 더 길고 철저하지만 느린 응답을 생성하는 템플릿을 만듭니다.

## 개선 예시

다음은 프롬프트 개선 도구가 기본 분류 프롬프트를 어떻게 향상시키는지 보여줍니다:

### 원본 프롬프트

```
From the following list of Wikipedia article titles, identify which article this sentence came from.
Respond with just the article title and nothing else.

Article titles:
{{titles}}

Sentence to classify:
{{sentence}}
```

### 개선된 프롬프트

```
You are an intelligent text classification system specialized in matching sentences to Wikipedia article titles. Your task is to identify which Wikipedia article a given sentence most likely belongs to, based on a provided list of article titles.

First, review the following list of Wikipedia article titles:
<article_titles>
{{titles}}
</article_titles>

Now, consider this sentence that needs to be classified:
<sentence_to_classify>
{{sentence}}
</sentence_to_classify>

Your goal is to determine which article title from the provided list best matches the given sentence. Follow these steps:

1. List the key concepts from the sentence
2. Compare each key concept with the article titles
3. Rank the top 3 most relevant titles and explain why they are relevant
4. Select the most appropriate article title that best encompasses or relates to the sentence's content

Wrap your analysis in <analysis> tags. Include the following:
- List of key concepts from the sentence
- Comparison of each key concept with the article titles
- Ranking of top 3 most relevant titles with explanations
- Your final choice and reasoning

After your analysis, provide your final answer: the single most appropriate Wikipedia article title from the list.

Output only the chosen article title, without any additional text or explanation.
```

개선된 프롬프트가 어떻게 다음과 같이 변화했는지 주목하세요:
- 명확한 단계별 추론 지침을 추가했습니다
- 콘텐츠를 구성하기 위해 XML 태그를 사용했습니다
- 명시적인 출력 형식 요구사항을 제공했습니다
- 분석 프로세스를 통해 Claude를 안내합니다

## 문제 해결

일반적인 문제와 해결 방법:

- **출력에 예시가 나타나지 않음**: 예시가 XML 태그로 올바르게 형식화되어 있고 첫 번째 사용자 메시지의 시작 부분에 나타나는지 확인하세요
- **사고 연쇄가 너무 장황함**: 원하는 출력 길이와 세부 수준에 대한 구체적인 지침을 추가하세요
- **추론 단계가 필요에 맞지 않음**: 특정 사용 사례에 맞게 단계 섹션을 수정하세요

---

## 다음 단계

- **프롬프트 라이브러리**: 다양한 작업을 위한 예시 프롬프트에서 영감을 얻으세요. [자세히 보기](/docs/en/resources/prompt-library/library)
- **GitHub 프롬프팅 튜토리얼**: 인터랙티브 튜토리얼로 프롬프팅 모범 사례를 배우세요. [자세히 보기](https://github.com/anthropics/prompt-eng-interactive-tutorial)
- **프롬프트 테스트하기**: 평가 도구를 사용하여 개선된 프롬프트를 테스트하세요. [자세히 보기](../09-test-evaluate/03-eval-tool.md)

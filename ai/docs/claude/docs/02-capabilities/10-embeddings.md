# [Embeddings](https://platform.claude.com/docs/en/build-with-claude/embeddings)

텍스트 임베딩은 의미적 유사성을 측정할 수 있도록 하는 텍스트의 수치적 표현입니다.
이 가이드에서는 임베딩, 그 응용 분야, 그리고 검색, 추천, 이상 탐지와 같은 작업에 임베딩 모델을 사용하는 방법을 소개합니다.

---

## 임베딩 구현 전 고려사항

임베딩 제공업체를 선택할 때 필요와 선호도에 따라 고려할 수 있는 여러 요소가 있습니다:

- 데이터셋 크기 및 도메인 특화도: 모델 학습 데이터셋의 크기와 임베딩하려는 도메인과의 관련성. 일반적으로 더 크거나 더 도메인별로 특화된 데이터가 해당 도메인에서 더 나은 임베딩을 생성합니다
- 추론 성능: 임베딩 조회 속도 및 엔드투엔드 지연 시간. 이는 대규모 프로덕션 배포에 특히 중요한 고려사항입니다
- 맞춤화: 개인 데이터에 대한 지속적인 학습 옵션 또는 매우 특정한 도메인을 위한 모델 특화. 이는 고유한 어휘에 대한 성능을 향상시킬 수 있습니다

## Anthropic에서 임베딩을 얻는 방법

Anthropic은 자체 임베딩 모델을 제공하지 않습니다. 위의 모든 고려사항을 포괄하는 다양한 옵션과 기능을 제공하는 임베딩 제공업체 중 하나는 Voyage AI입니다.

Voyage AI는 최첨단 임베딩 모델을 제공하며, 금융 및 의료와 같은 특정 산업 도메인을 위한 맞춤형 모델 또는 개별 고객을 위한 맞춤 미세 조정 모델을 제공합니다.

이 가이드의 나머지 부분은 Voyage AI에 관한 것이지만, 특정 사용 사례에 가장 적합한 것을 찾기 위해 다양한 임베딩 벤더를 평가할 것을 권장합니다.

## 사용 가능한 모델

Voyage는 다음 텍스트 임베딩 모델 사용을 권장합니다:

| 모델                 | 컨텍스트 길이 | 임베딩 차원                     | 설명                                                                                                                                                                                       |
|--------------------|---------|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `voyage-3-large`   | 32,000  | 1024 (기본값), 256, 512, 2048 | 최고의 범용 및 다국어 검색 품질. 자세한 내용은 [블로그 포스트](https://blog.voyageai.com/2025/01/07/voyage-3-large/)를 참조하세요.                                                                                      |
| `voyage-3.5`       | 32,000  | 1024 (기본값), 256, 512, 2048 | 범용 및 다국어 검색 품질에 최적화. 자세한 내용은 [블로그 포스트](https://blog.voyageai.com/2025/05/20/voyage-3-5/)를 참조하세요.                                                                                         |
| `voyage-3.5-lite`  | 32,000  | 1024 (기본값), 256, 512, 2048 | 지연 시간 및 비용에 최적화. 자세한 내용은 [블로그 포스트](https://blog.voyageai.com/2025/05/20/voyage-3-5/)를 참조하세요.                                                                                             |
| `voyage-code-3`    | 32,000  | 1024 (기본값), 256, 512, 2048 | **코드** 검색에 최적화. 자세한 내용은 [블로그 포스트](https://blog.voyageai.com/2024/12/04/voyage-code-3/)를 참조하세요.                                                                                           |
| `voyage-finance-2` | 32,000  | 1024                       | **금융** 검색 및 RAG에 최적화. 자세한 내용은 [블로그 포스트](https://blog.voyageai.com/2024/06/03/domain-specific-embeddings-finance-edition-voyage-finance-2/)를 참조하세요.                                       |
| `voyage-law-2`     | 16,000  | 1024                       | **법률** 및 **긴 컨텍스트** 검색 및 RAG에 최적화. 모든 도메인에서 향상된 성능. 자세한 내용은 [블로그 포스트](https://blog.voyageai.com/2024/04/15/domain-specific-embeddings-and-retrieval-legal-edition-voyage-law-2/)를 참조하세요. |

또한 다음 멀티모달 임베딩 모델이 권장됩니다:

| 모델                    | 컨텍스트 길이 | 임베딩 차원 | 설명                                                                                                                                                                |
|-----------------------|---------|--------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `voyage-multimodal-3` | 32000   | 1024   | PDF 스크린샷, 슬라이드, 표, 그림 등 콘텐츠가 풍부한 이미지와 텍스트를 인터리브하여 벡터화할 수 있는 풍부한 멀티모달 임베딩 모델. 자세한 내용은 [블로그 포스트](https://blog.voyageai.com/2024/11/12/voyage-multimodal-3/)를 참조하세요. |

어떤 텍스트 임베딩 모델을 사용할지 결정하는 데 도움이 필요하신가요? [FAQ](https://docs.voyageai.com/docs/faq#what-embedding-models-are-available-and-which-one-should-i-use&ref=anthropic) 를 확인하세요.

## Voyage AI 시작하기

Voyage 임베딩에 액세스하려면:

1. Voyage AI 웹사이트에서 가입
2. API 키 획득
3. 편의를 위해 API 키를 환경 변수로 설정:

```bash
export VOYAGE_API_KEY="<your secret key>"
```

아래에 설명된 대로 공식 [`voyageai` Python 패키지](https://github.com/voyage-ai/voyageai-python) 또는 HTTP 요청을 사용하여 임베딩을 얻을 수 있습니다.

### Voyage Python 라이브러리

`voyageai` 패키지는 다음 명령을 사용하여 설치할 수 있습니다:

```bash
pip install -U voyageai
```

그런 다음 클라이언트 객체를 생성하고 텍스트를 임베딩하는 데 사용할 수 있습니다:

```python
import voyageai

vo = voyageai.Client()
# 이렇게 하면 환경 변수 VOYAGE_API_KEY가 자동으로 사용됩니다.
# 또는 vo = voyageai.Client(api_key="<your secret key>")를 사용할 수 있습니다

texts = ["Sample text 1", "Sample text 2"]

result = vo.embed(texts, model="voyage-3.5", input_type="document")
print(result.embeddings[0])
print(result.embeddings[1])
```

`result.embeddings`는 각각 1024개의 부동 소수점 숫자를 포함하는 두 개의 임베딩 벡터 목록이 됩니다. 위 코드를 실행하면 두 개의 임베딩이 화면에 출력됩니다:

```
[-0.013131560757756233, 0.019828535616397858, ...]   # "Sample text 1"에 대한 임베딩
[-0.0069352793507277966, 0.020878976210951805, ...]  # "Sample text 2"에 대한 임베딩
```

임베딩을 생성할 때 `embed()` 함수에 몇 가지 다른 인수를 지정할 수 있습니다.

Voyage python 패키지에 대한 자세한 내용은 [Voyage 문서](https://docs.voyageai.com/docs/embeddings#python-api)를 참조하세요.

### Voyage HTTP API

Voyage HTTP API를 요청하여 임베딩을 얻을 수도 있습니다. 예를 들어, 터미널에서 `curl` 명령을 통해 HTTP 요청을 보낼 수 있습니다:

```bash
curl https://api.voyageai.com/v1/embeddings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $VOYAGE_API_KEY" \
  -d '{
    "input": ["Sample text 1", "Sample text 2"],
    "model": "voyage-3.5"
  }'
```

받게 될 응답은 임베딩과 토큰 사용량을 포함하는 JSON 객체입니다:

```json
{
  "object": "list",
  "data": [
    {
      "embedding": [
        -0.013131560757756233,
        0.019828535616397858,
        ...
      ],
      "index": 0
    },
    {
      "embedding": [
        -0.0069352793507277966,
        0.020878976210951805,
        ...
      ],
      "index": 1
    }
  ],
  "model": "voyage-3.5",
  "usage": {
    "total_tokens": 10
  }
}

```

Voyage HTTP API에 대한 자세한 내용은 [Voyage 문서](https://docs.voyageai.com/reference/embeddings-api)를 참조하세요.

### AWS Marketplace

Voyage 임베딩은 [AWS Marketplace](https://aws.amazon.com/marketplace/seller-profile?id=seller-snt4gb6fd7ljg)에서 사용할 수 있습니다.
AWS에서 Voyage에 액세스하기 위한 지침은 [여기](https://docs.voyageai.com/docs/aws-marketplace-model-package?ref=anthropic)에서 확인할 수
있습니다.

## 빠른 시작 예제

이제 임베딩을 얻는 방법을 알았으니 간단한 예제를 살펴보겠습니다.

검색할 6개의 문서로 구성된 작은 코퍼스가 있다고 가정해봅시다

```python
documents = [
    "The Mediterranean diet emphasizes fish, olive oil, and vegetables, believed to reduce chronic diseases.",
    "Photosynthesis in plants converts light energy into glucose and produces essential oxygen.",
    "20th-century innovations, from radios to smartphones, centered on electronic advancements.",
    "Rivers provide water, irrigation, and habitat for aquatic species, vital for ecosystems.",
    "Apple's conference call to discuss fourth fiscal quarter results and business updates is scheduled for Thursday, November 2, 2023 at 2:00 p.m. PT / 5:00 p.m. ET.",
    "Shakespeare's works, like 'Hamlet' and 'A Midsummer Night's Dream,' endure in literature."
]

```

먼저 Voyage를 사용하여 각 문서를 임베딩 벡터로 변환합니다

```python
import voyageai

vo = voyageai.Client()

# 문서 임베딩
doc_embds = vo.embed(
    documents, model="voyage-3.5", input_type="document"
).embeddings
```

임베딩을 통해 벡터 공간에서 의미적 검색/검색을 수행할 수 있습니다. 예제 쿼리가 주어지면,

```python
query = "When is Apple's conference call scheduled?"
```

이를 임베딩으로 변환하고, 최근접 이웃 검색을 수행하여 임베딩 공간의 거리를 기반으로 가장 관련성 높은 문서를 찾습니다.

```python
import numpy as np

# 쿼리 임베딩
query_embd = vo.embed(
    [query], model="voyage-3.5", input_type="query"
).embeddings[0]

# 유사도 계산
# Voyage 임베딩은 길이 1로 정규화되므로 내적과
# 코사인 유사도는 동일합니다.
similarities = np.dot(doc_embds, query_embd)

retrieved_id = np.argmax(similarities)
print(documents[retrieved_id])
```

문서와 쿼리를 임베딩할 때 각각 `input_type="document"`와 `input_type="query"`를 사용한다는 점에 유의하세요. 
더 자세한 사양은 [여기](../02-capabilities/10-embeddings.md)에서 찾을 수 있습니다.

출력은 5번째 문서로, 실제로 쿼리와 가장 관련성이 높습니다:

```
Apple's conference call to discuss fourth fiscal quarter results and business updates is scheduled for Thursday, November 2, 2023 at 2:00 p.m. PT / 5:00 p.m. ET.
```

벡터 데이터베이스를 포함한 임베딩으로 RAG를 수행하는 방법에 대한 자세한 쿡북 세트를 찾고 있다면, [RAG 쿡북](https://platform.claude.com/cookbook/third-party-pinecone-rag-using-pinecone)을 확인하세요.

## FAQ

### Voyage 임베딩이 우수한 품질을 갖는 이유는 무엇인가요?

임베딩 모델은 생성 모델과 유사하게 의미적 컨텍스트를 캡처하고 압축하기 위해 강력한 신경망에 의존합니다.

Voyage의 경험 많은 AI 연구원 팀은 다음을 포함하여 임베딩 프로세스의 모든 구성 요소를 최적화합니다:
- 모델 아키텍처
- 데이터 수집
- 손실 함수
- 옵티마이저 선택

Voyage의 기술적 접근 방식에 대한 자세한 내용은 [블로그](https://blog.voyageai.com/)에서 확인하세요.

### 사용 가능한 임베딩 모델은 무엇이며 어떤 것을 사용해야 하나요?

범용 임베딩의 경우 다음을 권장합니다:

- `voyage-3-large`: 최고 품질
- `voyage-3.5-lite`: 최저 지연 시간 및 비용
- `voyage-3.5`: 경쟁력 있는 가격대에서 우수한 검색 품질을 제공하는 균형 잡힌 성능

검색의 경우, `input_type` 매개변수를 사용하여 텍스트가 쿼리인지 문서 유형인지 지정하세요.

도메인별 모델:

- 법률 작업: `voyage-law-2`
- 코드 및 프로그래밍 문서: `voyage-code-3`
- 금융 관련 작업: `voyage-finance-2`

### 어떤 유사도 함수를 사용해야 하나요?

Voyage 임베딩은 내적 유사도, 코사인 유사도 또는 유클리드 거리와 함께 사용할 수 있습니다. 임베딩 유사도에 대한
설명은 [여기](https://www.pinecone.io/learn/vector-similarity/)에서 찾을 수 있습니다.

Voyage AI 임베딩은 길이 1로 정규화되어 있으므로:

- 코사인 유사도는 내적 유사도와 동일하지만, 후자는 더 빠르게 계산할 수 있습니다.
- 코사인 유사도와 유클리드 거리는 동일한 순위를 생성합니다.

### 문자, 단어, 토큰 간의 관계는 무엇인가요?

이 [페이지](https://docs.voyageai.com/docs/tokenization?ref=anthropic)를 참조하세요.

### input_type 매개변수는 언제 어떻게 사용해야 하나요?

모든 검색 작업 및 사용 사례(예: RAG)에 대해 `input_type` 매개변수를 사용하여 입력 텍스트가 쿼리인지 문서인지 지정하는 것이 권장됩니다. `input_type`을 생략하거나
`input_type=None`으로 설정하지 마세요. 입력 텍스트가 쿼리인지 문서인지 지정하면 검색을 위한 더 나은 밀집 벡터 표현을 생성할 수 있으며, 이는 더 나은 검색 품질로 이어질 수 있습니다.

`input_type` 매개변수를 사용할 때 임베딩 전에 입력 텍스트 앞에 특수 프롬프트가 추가됩니다. 구체적으로:

> 📘 **`input_type`과 관련된 프롬프트**
>
> - 쿼리의 경우, 프롬프트는 "Represent the query for retrieving supporting documents: "입니다.
> - 문서의 경우, 프롬프트는 "Represent the document for retrieval: "입니다.
> - 예제
    >
- `input_type="query"`일 때, "When is Apple's conference call scheduled?"와 같은 쿼리는 "**Represent the query for retrieving
  supporting documents:** When is Apple's conference call scheduled?"가 됩니다.
>     - `input_type="document"`일 때, "Apple's conference call to discuss fourth fiscal quarter results and business
        updates is scheduled for Thursday, November 2, 2023 at 2:00 p.m. PT / 5:00 p.m. ET."와 같은 쿼리는 "**Represent the
        document for retrieval:** Apple's conference call to discuss fourth fiscal quarter results and business updates
        is scheduled for Thursday, November 2, 2023 at 2:00 p.m. PT / 5:00 p.m. ET."가 됩니다.

`voyage-large-2-instruct`는 이름에서 알 수 있듯이 입력 텍스트 앞에 추가되는 추가 지침에 응답하도록 훈련되었습니다. 분류, 클러스터링 또는
기타 [MTEB](https://huggingface.co/mteb) 하위 작업의 경우 [여기](https://github.com/voyage-ai/voyage-large-2-instruct)의 지침을 사용하세요.

### 사용 가능한 양자화 옵션은 무엇인가요?

임베딩의 양자화는 32비트 단정밀도 부동 소수점 숫자와 같은 고정밀도 값을 8비트 정수 또는 1비트 이진 값과 같은 저정밀도 형식으로 변환하여 각각 4배 및 32배의 저장, 메모리 및 비용을 절감합니다. 지원되는
Voyage 모델은 `output_dtype` 매개변수로 출력 데이터 유형을 지정하여 양자화를 가능하게 합니다:

- `float`: 반환된 각 임베딩은 32비트(4바이트) 단정밀도 부동 소수점 숫자 목록입니다. 이는 기본값이며 가장 높은 정밀도/검색 정확도를 제공합니다.
- `int8` 및 `uint8`: 반환된 각 임베딩은 각각 -128에서 127 및 0에서 255 범위의 8비트(1바이트) 정수 목록입니다.
- `binary` 및 `ubinary`: 반환된 각 임베딩은 비트 패킹된 양자화된 단일 비트 임베딩 값을 나타내는 8비트 정수 목록입니다: `binary`의 경우 `int8`, `ubinary`의 경우
  `uint8`. 반환된 정수 목록의 길이는 임베딩의 실제 차원의 1/8입니다. binary 유형은 오프셋 이진 방법을 사용하며, 아래 FAQ에서 자세히 알아볼 수 있습니다.

> **이진 양자화 예제**
>
> 다음 8개의 임베딩 값을 고려하세요: -0.03955078, 0.006214142, -0.07446289, -0.039001465, 0.0046463013, 0.00030612946, -0.08496094,
> 0.03994751. 이진 양자화를 사용하면 0보다 작거나 같은 값은 이진 0으로 양자화되고 양수 값은 이진 1로 양자화되어 다음 이진 시퀀스가 생성됩니다: 0, 1, 0, 0, 1, 1, 0, 1. 이 8비트는
> 단일 8비트 정수 01001101로 패킹됩니다(가장 왼쪽 비트가 최상위 비트).
>   - `ubinary`: 이진 시퀀스는 직접 변환되어 부호 없는 정수(`uint8`) 77로 표현됩니다.
>   - `binary`: 이진 시퀀스는 오프셋 이진 방법을 사용하여 계산된 부호 있는 정수(`int8`) -51로 표현됩니다(77 - 128 = -51).

### Matryoshka 임베딩을 어떻게 잘라낼 수 있나요?

Matryoshka 학습은 단일 벡터 내에서 조잡한 것부터 세밀한 것까지 표현을 가진 임베딩을 생성합니다. 여러 출력 차원을 지원하는 `voyage-code-3`과 같은 Voyage 모델은 이러한 Matryoshka
임베딩을 생성합니다. 선두 차원의 하위 집합을 유지하여 이러한 벡터를 잘라낼 수 있습니다. 예를 들어, 다음 Python 코드는 1024차원 벡터를 256차원으로 잘라내는 방법을 보여줍니다:

```python
import voyageai
import numpy as np

def embd_normalize(v: np.ndarray) -> np.ndarray:
    """
    각 행을 유클리드 노름으로 나누어 2D numpy 배열의 행을 단위 벡터로 정규화합니다.
    0으로 나누는 것을 방지하기 위해 행의 노름이 0이면 ValueError를 발생시킵니다.
    """
    row_norms = np.linalg.norm(v, axis=1, keepdims=True)
    if np.any(row_norms == 0):
        raise ValueError("Cannot normalize rows with a norm of zero.")
    return v / row_norms


vo = voyageai.Client()

# 기본적으로 1024차원 부동 소수점 숫자인 voyage-code-3 벡터 생성
embd = vo.embed(['Sample text 1', 'Sample text 2'], model='voyage-code-3').embeddings

# 더 짧은 차원 설정
short_dim = 256

# 벡터를 더 짧은 차원으로 크기 조정 및 정규화
resized_embd = embd_normalize(np.array(embd)[:, :short_dim]).tolist()
```

## 가격

최신 가격 세부 정보는 Voyage의 [가격 페이지](https://docs.voyageai.com/docs/pricing?ref=anthropic)를 방문하세요.

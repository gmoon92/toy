# [Vision](https://platform.claude.com/docs/en/build-with-claude/vision)

Claude의 비전 기능을 사용하면 이미지를 이해하고 분석할 수 있어 멀티모달 상호작용의 흥미로운 가능성이 열립니다.

---

이 가이드는 모범 사례, 코드 예제, 그리고 유의해야 할 제한사항을 포함하여 Claude에서 이미지를 다루는 방법을 설명합니다.

---

## 비전 기능 사용 방법

다음과 같은 방법으로 Claude의 비전 기능을 사용할 수 있습니다:

- [claude.ai](https://claude.ai/). 파일을 업로드하는 것처럼 이미지를 업로드하거나, 채팅 창에 직접 이미지를 드래그 앤 드롭할 수 있습니다.
- [Console Workbench](/workbench/). 모든 User 메시지 블록의 오른쪽 상단에 이미지를 추가하는 버튼이 나타납니다.
- **API 요청**. 이 가이드의 예제를 참조하세요.

---

## 업로드하기 전에

### 기본 사항 및 제한

단일 요청에 여러 이미지를 포함할 수 있습니다([claude.ai](https://claude.ai/)는 최대 20개, API 요청은 최대 100개). Claude는 응답을 생성할 때 제공된 모든 이미지를 분석합니다. 이는 이미지를 비교하거나 대조하는 데 유용할 수 있습니다.

8000x8000 px보다 큰 이미지를 제출하면 거부됩니다. 한 번의 API 요청에 20개 이상의 이미지를 제출하는 경우, 이 제한은 2000x2000 px입니다.


> API는 요청당 100개의 이미지를 지원하지만, 표준 엔드포인트에는 [32MB 요청 크기 제한](https://platform.claude.com/docs/en/api/overview#request-size-limits)이 있습니다.


### 이미지 크기 평가

최적의 성능을 위해 이미지가 너무 큰 경우 업로드하기 전에 크기를 조정하는 것을 권장합니다. 이미지의 긴 변이 1568픽셀 이상이거나 이미지가 약 1,600 토큰 이상인 경우, 크기 제한 내에 들어올 때까지 가로세로 비율을 유지하면서 먼저 축소됩니다.

입력 이미지가 너무 커서 크기 조정이 필요한 경우, 추가적인 모델 성능 향상 없이 [time-to-first-token](https://platform.claude.com/docs/en/about-claude/glossary)의 지연 시간만 증가합니다. 어느 한 변이 200픽셀 미만인 매우 작은 이미지는 성능을 저하시킬 수 있습니다.


> [time-to-first-token](https://platform.claude.com/docs/en/about-claude/glossary)을 개선하려면 이미지 크기를 1.15메가픽셀 이하(그리고 두 차원 모두 1568픽셀 이내)로 조정하는 것을 권장합니다.


다음은 일반적인 가로세로 비율에 대해 크기 조정되지 않고 API에서 허용하는 최대 이미지 크기 표입니다. Claude Sonnet 4.5에서 이러한 이미지는 약 1,600 토큰을 사용하며 1K 이미지당 약 $4.80입니다.

| 가로세로 비율 | 이미지 크기    |
| ------------- | -------------- |
| 1:1           | 1092x1092 px   |
| 3:4           | 951x1268 px    |
| 2:3           | 896x1344 px    |
| 9:16          | 819x1456 px    |
| 1:2           | 784x1568 px    |

### 이미지 비용 계산

Claude에 대한 요청에 포함하는 각 이미지는 토큰 사용량에 포함됩니다. 대략적인 비용을 계산하려면, 대략적인 이미지 토큰 수에 사용 중인 [모델의 토큰당 가격](https://claude.com/pricing)을 곱하세요.

이미지 크기를 조정할 필요가 없는 경우, 다음 알고리즘을 통해 사용되는 토큰 수를 추정할 수 있습니다: `tokens = (width px * height px)/750`

다음은 Claude Sonnet 4.5의 토큰당 가격인 입력 토큰 백만 개당 $3을 기준으로 한 API 크기 제약 내 다양한 이미지 크기에 대한 대략적인 토큰화 및 비용 예시입니다:

| 이미지 크기                     | 토큰 수 | 이미지당 비용 | 1K 이미지당 비용 |
| ------------------------------- | ------- | ------------- | ---------------- |
| 200x200 px(0.04 메가픽셀)       | ~54     | ~$0.00016     | ~$0.16           |
| 1000x1000 px(1 메가픽셀)        | ~1334   | ~$0.004       | ~$4.00           |
| 1092x1092 px(1.19 메가픽셀)     | ~1590   | ~$0.0048      | ~$4.80           |

### 이미지 품질 보장

Claude에 이미지를 제공할 때 최상의 결과를 얻으려면 다음 사항을 유념하세요:

- **이미지 형식**: 지원되는 이미지 형식을 사용하세요: JPEG, PNG, GIF 또는 WebP.
- **이미지 선명도**: 이미지가 선명하고 너무 흐릿하거나 픽셀화되지 않았는지 확인하세요.
- **텍스트**: 이미지에 중요한 텍스트가 포함되어 있는 경우, 읽을 수 있고 너무 작지 않은지 확인하세요. 텍스트를 확대하기 위해 중요한 시각적 맥락을 잘라내지 마세요.

---

## 프롬프트 예제

텍스트 기반 Claude 상호작용에 잘 작동하는 많은 [프롬프팅 기법](../08-prompt-engineering/01-overview.md)이 이미지 기반 프롬프트에도 적용될 수 있습니다.

이러한 예제는 이미지를 포함하는 모범 사례 프롬프트 구조를 보여줍니다.


> 문서-쿼리 배치와 마찬가지로, Claude는 텍스트보다 이미지가 먼저 올 때 가장 잘 작동합니다. 텍스트 뒤에 배치되거나 텍스트 사이에 삽입된 이미지도 여전히 잘 작동하지만, 사용 사례가 허용하는 경우 이미지-텍스트 구조를 권장합니다.


### 프롬프트 예제 정보

다음 예제는 다양한 프로그래밍 언어와 접근 방식을 사용하여 Claude의 비전 기능을 사용하는 방법을 보여줍니다. Claude에 이미지를 제공하는 세 가지 방법이 있습니다:

1. `image` 콘텐츠 블록에서 base64로 인코딩된 이미지로
2. 온라인에 호스팅된 이미지에 대한 URL 참조로
3. Files API 사용 (한 번 업로드하여 여러 번 사용)

base64 예제 프롬프트는 다음 변수를 사용합니다:

<details>
<summary>REST API 예시</summary>

```bash
# URL 기반 이미지의 경우, JSON 요청에서 URL을 직접 사용할 수 있습니다

    # base64로 인코딩된 이미지의 경우, 먼저 이미지를 인코딩해야 합니다
    # bash에서 이미지를 base64로 인코딩하는 방법 예시:
    BASE64_IMAGE_DATA=$(curl -s "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg" | base64)

    # 인코딩된 데이터를 이제 API 호출에서 사용할 수 있습니다
```

</details>

다음은 base64로 인코딩된 이미지와 URL 참조를 사용하여 Messages API 요청에 이미지를 포함하는 방법의 예시입니다:

### Base64로 인코딩된 이미지 예제

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages \
      -H "x-api-key: $ANTHROPIC_API_KEY" \
      -H "anthropic-version: 2023-06-01" \
      -H "content-type: application/json" \
      -d '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 1024,
        "messages": [
          {
            "role": "user",
            "content": [
              {
                "type": "image",
                "source": {
                  "type": "base64",
                  "media_type": "image/jpeg",
                  "data": "'"$BASE64_IMAGE_DATA"'"
                }
              },
              {
                "type": "text",
                "text": "Describe this image."
              }
            ]
          }
        ]
      }'
```

</details>

### URL 기반 이미지 예제

<details>
<summary>REST API 예시</summary>

```bash
curl https://api.anthropic.com/v1/messages \
      -H "x-api-key: $ANTHROPIC_API_KEY" \
      -H "anthropic-version: 2023-06-01" \
      -H "content-type: application/json" \
      -d '{
        "model": "claude-sonnet-4-5",
        "max_tokens": 1024,
        "messages": [
          {
            "role": "user",
            "content": [
              {
                "type": "image",
                "source": {
                  "type": "url",
                  "url": "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg"
                }
              },
              {
                "type": "text",
                "text": "Describe this image."
              }
            ]
          }
        ]
      }'
```

</details>

### Files API 이미지 예제

반복적으로 사용할 이미지나 인코딩 오버헤드를 피하고 싶을 때는 [Files API](../02-capabilities/13-files-api.md)를 사용하세요:

<details>
<summary>REST API 예시</summary>

```bash
# 먼저 Files API에 이미지를 업로드합니다
curl -X POST https://api.anthropic.com/v1/files \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: files-api-2025-04-14" \
  -F "file=@image.jpg"

# 그런 다음 반환된 file_id를 메시지에서 사용합니다
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "anthropic-beta: files-api-2025-04-14" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
      {
        "role": "user",
        "content": [
          {
            "type": "image",
            "source": {
              "type": "file",
              "file_id": "file_abc123"
            }
          },
          {
            "type": "text",
            "text": "Describe this image."
          }
        ]
      }
    ]
  }'
```

</details>

더 많은 예제 코드와 매개변수 세부 정보는 [Messages API 예제](https://platform.claude.com/docs/en/api/messages)를 참조하세요.

<details>
<summary>예제: 단일 이미지</summary>

이미지에 대한 질문이나 이미지를 사용하는 작업에 대한 지시사항보다 이미지를 프롬프트의 앞부분에 배치하는 것이 가장 좋습니다.

Claude에게 하나의 이미지를 설명하도록 요청합니다.

| 역할 | 내용                          |
| ---- | ----------------------------- |
| User | [이미지] 이 이미지를 설명해줘. |

<details>
<summary>Base64 사용</summary>

```python Python
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        messages=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": image1_media_type,
                            "data": image1_data,
                        },
                    },
                    {
                        "type": "text",
                        "text": "Describe this image."
                    }
                ],
            }
        ],
    )
    ```

</details>

<details>
<summary>URL 사용</summary>

```python Python
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        messages=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "image",
                        "source": {
                            "type": "url",
                            "url": "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg",
                        },
                    },
                    {
                        "type": "text",
                        "text": "Describe this image."
                    }
                ],
            }
        ],
    )
    ```

</details>
</details>
<details>
<summary>예제: 다중 이미지</summary>

여러 이미지가 있는 상황에서는 각 이미지를 `Image 1:` 및 `Image 2:`와 같은 식으로 소개합니다. 이미지 사이나 이미지와 프롬프트 사이에 줄바꿈이 필요하지 않습니다.

Claude에게 여러 이미지 간의 차이점을 설명하도록 요청합니다.
| 역할 | 내용 |
| ---- | ----------------------------------------------------------------------------- |
| User | Image 1: [이미지 1] Image 2: [이미지 2] 이 이미지들은 어떻게 다른가요? |

<details>
<summary>Base64 사용</summary>

```python Python
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        messages=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": "Image 1:"
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": image1_media_type,
                            "data": image1_data,
                        },
                    },
                    {
                        "type": "text",
                        "text": "Image 2:"
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": image2_media_type,
                            "data": image2_data,
                        },
                    },
                    {
                        "type": "text",
                        "text": "How are these images different?"
                    }
                ],
            }
        ],
    )
    ```

</details>

<details>
<summary>URL 사용</summary>

```python Python
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        messages=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": "Image 1:"
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "url",
                            "url": "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg",
                        },
                    },
                    {
                        "type": "text",
                        "text": "Image 2:"
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "url",
                            "url": "https://upload.wikimedia.org/wikipedia/commons/b/b5/Iridescent.green.sweat.bee1.jpg",
                        },
                    },
                    {
                        "type": "text",
                        "text": "How are these images different?"
                    }
                ],
            }
        ],
    )
    ```

</details>
</details>
<details>
<summary>예제: 시스템 프롬프트와 함께 다중 이미지</summary>

Claude에게 여러 이미지 간의 차이점을 설명하도록 요청하면서, 응답 방법에 대한 시스템 프롬프트를 제공합니다.

| 내용   |                                                                               |
| ------ | ----------------------------------------------------------------------------- |
| System | 스페인어로만 응답하세요.                                                      |
| User   | Image 1: [이미지 1] Image 2: [이미지 2] 이 이미지들은 어떻게 다른가요? |

<details>
<summary>Base64 사용</summary>

```python Python
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        system="Respond only in Spanish.",
        messages=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": "Image 1:"
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": image1_media_type,
                            "data": image1_data,
                        },
                    },
                    {
                        "type": "text",
                        "text": "Image 2:"
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": image2_media_type,
                            "data": image2_data,
                        },
                    },
                    {
                        "type": "text",
                        "text": "How are these images different?"
                    }
                ],
            }
        ],
    )
    ```

</details>

<details>
<summary>URL 사용</summary>

```python Python
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        system="Respond only in Spanish.",
        messages=[
            {
                "role": "user",
                "content": [
                    {
                        "type": "text",
                        "text": "Image 1:"
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "url",
                            "url": "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg",
                        },
                    },
                    {
                        "type": "text",
                        "text": "Image 2:"
                    },
                    {
                        "type": "image",
                        "source": {
                            "type": "url",
                            "url": "https://upload.wikimedia.org/wikipedia/commons/b/b5/Iridescent.green.sweat.bee1.jpg",
                        },
                    },
                    {
                        "type": "text",
                        "text": "How are these images different?"
                    }
                ],
            }
        ],
    )
    ```

</details>
</details>
<details>
<summary>예제: 두 번의 대화 턴에 걸친 4개의 이미지</summary>

Claude의 비전 기능은 이미지와 텍스트를 혼합한 멀티모달 대화에서 빛을 발합니다. Claude와 확장된 양방향 교환을 할 수 있으며, 언제든지 새로운 이미지나 후속 질문을 추가할 수 있습니다. 이를 통해 반복적인 이미지 분석, 비교 또는 시각 자료와 다른 지식을 결합하는 강력한 워크플로우를 가능하게 합니다.

Claude에게 두 이미지를 대조하도록 요청한 다음, 첫 번째 이미지와 두 개의 새로운 이미지를 비교하는 후속 질문을 합니다.
| 역할 | 내용 |
| --------- | ------------------------------------------------------------------------------------ |
| User | Image 1: [이미지 1] Image 2: [이미지 2] 이 이미지들은 어떻게 다른가요? |
| Assistant | [Claude의 응답] |
| User | Image 1: [이미지 3] Image 2: [이미지 4] 이 이미지들은 처음 두 이미지와 비슷한가요? |
| Assistant | [Claude의 응답] |

API를 사용할 때는 표준 [다중 턴 대화](https://platform.claude.com/docs/en/api/messages) 구조의 일부로 `user` 역할의 Messages 배열에 새 이미지를 삽입하기만 하면 됩니다.
</details>

---

## 제한사항

Claude의 이미지 이해 기능은 최첨단이지만, 유의해야 할 몇 가지 제한사항이 있습니다:

- **사람 식별**: Claude는 이미지에서 사람을 식별(즉, 이름을 지정)하는 데 [사용할 수 없으며](https://www.anthropic.com/legal/aup) 거부할 것입니다.
- **정확도**: Claude는 200픽셀 미만의 저품질, 회전된 또는 매우 작은 이미지를 해석할 때 환각을 일으키거나 실수할 수 있습니다.
- **공간 추론**: Claude의 공간 추론 능력은 제한적입니다. 아날로그 시계 읽기나 체스 말의 정확한 위치 설명과 같이 정밀한 위치 파악이나 레이아웃이 필요한 작업에서 어려움을 겪을 수 있습니다.
- **개수 세기**: Claude는 이미지의 객체에 대한 대략적인 개수를 제공할 수 있지만, 특히 작은 객체가 많을 때 항상 정확하지 않을 수 있습니다.
- **AI 생성 이미지**: Claude는 이미지가 AI로 생성되었는지 알지 못하며 질문을 받으면 틀릴 수 있습니다. 가짜 또는 합성 이미지를 감지하는 데 의존하지 마세요.
- **부적절한 콘텐츠**: Claude는 [이용 약관](https://www.anthropic.com/legal/aup)을 위반하는 부적절하거나 노골적인 이미지를 처리하지 않습니다.
- **의료 애플리케이션**: Claude는 일반적인 의료 이미지를 분석할 수 있지만, CT나 MRI와 같은 복잡한 진단 스캔을 해석하도록 설계되지 않았습니다. Claude의 출력은 전문적인 의료 조언이나 진단을 대체하는 것으로 간주되어서는 안 됩니다.

특히 중요한 사용 사례의 경우 Claude의 이미지 해석을 항상 신중하게 검토하고 확인하세요. 완벽한 정밀도나 민감한 이미지 분석이 필요한 작업에는 사람의 감독 없이 Claude를 사용하지 마세요.

---

## FAQ

  <details>
<summary>Claude는 어떤 이미지 파일 형식을 지원하나요?</summary>

Claude는 현재 JPEG, PNG, GIF 및 WebP 이미지 형식을 지원합니다. 구체적으로:
    - `image/jpeg`
    - `image/png`
    - `image/gif`
    - `image/webp`
</details>

{" "}

<details>
<summary>Claude가 이미지 URL을 읽을 수 있나요?</summary>

예, Claude는 이제 API의 URL 이미지 소스 블록을 통해 URL에서 이미지를 처리할 수 있습니다.

  API 요청에서 "base64" 대신 "url" 소스 타입을 사용하기만 하면 됩니다.

  예시:
  ```json
  {
    "type": "image",
    "source": {
      "type": "url",
      "url": "https://upload.wikimedia.org/wikipedia/commons/a/a7/Camponotus_flavomarginatus_ant.jpg"
    }
  }
  ```
</details>

  <details>
<summary>업로드할 수 있는 이미지 파일 크기에 제한이 있나요?</summary>

예, 제한이 있습니다:
    - API: 이미지당 최대 5MB
    - claude.ai: 이미지당 최대 10MB

    이 제한을 초과하는 이미지는 거부되고 API를 사용할 때 오류가 반환됩니다.
</details>

  <details>
<summary>한 요청에 몇 개의 이미지를 포함할 수 있나요?</summary>

이미지 제한은 다음과 같습니다:
    - Messages API: 요청당 최대 100개의 이미지
    - claude.ai: 턴당 최대 20개의 이미지

    이 제한을 초과하는 요청은 거부되고 오류가 반환됩니다.
</details>

{" "}

<details>
<summary>Claude가 이미지 메타데이터를 읽나요?</summary>

아니요, Claude는 전달된 이미지에서 메타데이터를 파싱하거나 수신하지 않습니다.
</details>

{" "}

<details>
<summary>업로드한 이미지를 삭제할 수 있나요?</summary>

아니요. 이미지 업로드는 일시적이며 API 요청 기간을 넘어 저장되지 않습니다. 업로드된 이미지는 처리된 후 자동으로 삭제됩니다.
</details>

{" "}

<details>
<summary>이미지 업로드에 대한 데이터 프라이버시 세부 정보는 어디에서 찾을 수 있나요?</summary>

업로드된 이미지 및 기타 데이터를 처리하는 방법에 대한 정보는 개인정보 보호정책 페이지를 참조하세요. 업로드된 이미지를 모델 학습에 사용하지 않습니다.
</details>

  <details>
<summary>Claude의 이미지 해석이 잘못된 것 같으면 어떻게 하나요?</summary>

Claude의 이미지 해석이 잘못된 것 같다면:
    1. 이미지가 선명하고 고품질이며 올바르게 방향이 맞춰져 있는지 확인하세요.
    2. 프롬프트 엔지니어링 기법을 시도하여 결과를 개선하세요.
    3. 문제가 지속되면 claude.ai에서 출력에 플래그를 지정하거나(좋아요/싫어요) 지원팀에 문의하세요.

    여러분의 피드백이 개선에 도움이 됩니다!
</details>

  <details>
<summary>Claude가 이미지를 생성하거나 편집할 수 있나요?</summary>

아니요, Claude는 이미지 이해 모델일 뿐입니다. 이미지를 해석하고 분석할 수 있지만, 이미지를 생성하거나 제작하거나 편집하거나 조작하거나 만들 수 없습니다.
</details>

---

## 비전에 대해 더 깊이 알아보기

Claude를 사용하여 이미지로 구축을 시작할 준비가 되셨나요? 다음은 몇 가지 유용한 리소스입니다:

- [Multimodal cookbook](https://platform.claude.com/cookbook/multimodal-getting-started-with-vision): 이 쿡북에는 [이미지 시작하기](https://platform.claude.com/cookbook/multimodal-getting-started-with-vision)와 이미지를 사용한 최고 품질의 성능을 보장하기 위한 [모범 사례 기법](https://platform.claude.com/cookbook/multimodal-best-practices-for-vision)에 대한 팁이 있습니다. [차트 해석 및 분석](https://platform.claude.com/cookbook/multimodal-reading-charts-graphs-powerpoints)이나 [양식에서 콘텐츠 추출](https://platform.claude.com/cookbook/multimodal-how-to-transcribe-text)과 같은 작업을 수행하기 위해 이미지로 Claude에 효과적으로 프롬프트하는 방법을 확인하세요.
- [API 레퍼런스](https://platform.claude.com/docs/en/api/messages): [이미지를 포함한 API 호출](https://platform.claude.com/docs/en/build-with-claude/working-with-messages#vision) 예제를 포함한 Messages API 문서를 방문하세요.

다른 질문이 있으시면 [지원팀](https://support.claude.com/)에 문의하세요. 또한 [개발자 커뮤니티](https://www.anthropic.com/discord)에 가입하여 다른 제작자들과 연결하고 Anthropic 전문가의 도움을 받을 수 있습니다.

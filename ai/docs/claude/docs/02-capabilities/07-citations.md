# [인용 (Citations)](https://platform.claude.com/docs/en/build-with-claude/citations)

---

Claude는 문서에 대한 질문에 답변할 때 상세한 인용을 제공할 수 있으며, 이를 통해 응답의 정보 출처를 추적하고 검증할 수 있습니다.

Haiku 3를 제외한 모든 [활성 모델](/docs/en/about-claude/models/overview)이 인용을 지원합니다.


> *Claude Sonnet 3.7의 인용*
>
> Claude Sonnet 3.7은 사용자의 명시적인 지시 없이는 다른 Claude 모델에 비해 인용을 제공할 가능성이 낮습니다. Claude Sonnet 3.7에서 인용을 사용할 때는 `user` 턴에 추가 지시사항을 포함하는 것을 권장합니다. 예를 들어 `"답변을 뒷받침하기 위해 인용을 사용하세요."`와 같은 지시를 추가하세요.
>
> 또한 모델에게 응답을 구조화하도록 요청하면, 해당 형식 내에서 인용을 사용하라고 명시적으로 지시하지 않는 한 인용을 사용할 가능성이 낮습니다. 예를 들어, 모델에게 응답에 `<result>` 태그를 사용하도록 요청하는 경우, `"<result> 태그 내에서도 항상 인용을 사용하세요."`와 같은 내용을 추가해야 합니다.



> 인용 기능에 대한 피드백과 제안은 이 [양식](https://forms.gle/9n9hSrKnKe3rpowH9)을 통해 공유해 주세요.


다음은 Messages API에서 인용을 사용하는 예시입니다:

<CodeGroup>

```bash Shell
curl https://api.anthropic.com/v1/messages \
  -H "content-type: application/json" \
  -H "x-api-key: $ANTHROPIC_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -d '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
      {
        "role": "user",
        "content": [
          {
            "type": "document",
            "source": {
              "type": "text",
              "media_type": "text/plain",
              "data": "The grass is green. The sky is blue."
            },
            "title": "My Document",
            "context": "This is a trustworthy document.",
            "citations": {"enabled": true}
          },
          {
            "type": "text",
            "text": "What color is the grass and sky?"
          }
        ]
      }
    ]
  }'
```

```python Python
import anthropic

client = anthropic.Anthropic()

response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[
        {
            "role": "user",
            "content": [
                {
                    "type": "document",
                    "source": {
                        "type": "text",
                        "media_type": "text/plain",
                        "data": "The grass is green. The sky is blue."
                    },
                    "title": "My Document",
                    "context": "This is a trustworthy document.",
                    "citations": {"enabled": True}
                },
                {
                    "type": "text",
                    "text": "What color is the grass and sky?"
                }
            ]
        }
    ]
)
print(response)
```

```java Java
import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.*;

public class DocumentExample {

    public static void main(String[] args) {
        AnthropicClient client = AnthropicOkHttpClient.fromEnv();

        PlainTextSource source = PlainTextSource.builder()
                .data("The grass is green. The sky is blue.")
                .build();

        DocumentBlockParam documentParam = DocumentBlockParam.builder()
                .source(source)
                .title("My Document")
                .context("This is a trustworthy document.")
                .citations(CitationsConfigParam.builder().enabled(true).build())
                .build();

        TextBlockParam textBlockParam = TextBlockParam.builder()
                .text("What color is the grass and sky?")
                .build();

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_20250514)
                .maxTokens(1024)
                .addUserMessageOfBlockParams(List.of(ContentBlockParam.ofDocument(documentParam), ContentBlockParam.ofText(textBlockParam)))
                .build();

        Message message = client.messages().create(params);
        System.out.println(message);
    }
}
```

</CodeGroup>


> **프롬프트 기반 접근 방식과의 비교**
>
> 프롬프트 기반 인용 솔루션과 비교했을 때, 인용 기능은 다음과 같은 장점이 있습니다:
> - **비용 절감:** 프롬프트 기반 접근 방식에서 Claude에게 직접 인용문을 출력하도록 요청하는 경우, `cited_text`가 출력 토큰에 포함되지 않기 때문에 비용 절감 효과를 볼 수 있습니다.
> - **더 나은 인용 신뢰성:** 위에서 언급한 각 응답 형식으로 인용을 파싱하고 `cited_text`를 추출하기 때문에, 인용이 제공된 문서에 대한 유효한 포인터를 포함하는 것이 보장됩니다.
> - **향상된 인용 품질:** 평가 결과, 순수한 프롬프트 기반 접근 방식에 비해 인용 기능이 문서에서 가장 관련성 높은 인용문을 제공할 가능성이 훨씬 높은 것으로 나타났습니다.


---

## 인용 작동 방식

다음 단계를 통해 Claude와 인용을 통합하세요:

<Steps>
  <Step title="문서 제공 및 인용 활성화">
    - 지원되는 형식 중 하나로 문서를 포함하세요: [PDF](#pdf-documents), [일반 텍스트](#plain-text-documents), 또는 [사용자 정의 콘텐츠](#custom-content-documents) 문서
    - 각 문서에 `citations.enabled=true`를 설정하세요. 현재 인용은 요청 내의 모든 문서에서 활성화되거나 모두 비활성화되어야 합니다.
    - 현재 텍스트 인용만 지원되며 이미지 인용은 아직 불가능합니다.
  </Step>
  <Step title="문서 처리">
    - 문서 콘텐츠는 가능한 인용의 최소 단위를 정의하기 위해 "청크"로 분할됩니다. 예를 들어, 문장 청킹은 Claude가 단일 문장을 인용하거나 여러 연속 문장을 연결하여 단락(또는 그 이상)을 인용할 수 있게 합니다!
      - **PDF의 경우:** [PDF 지원](../02-capabilities/12-pdf-support.md)에서 설명된 대로 텍스트가 추출되고 콘텐츠는 문장으로 청크됩니다. PDF에서 이미지를 인용하는 것은 현재 지원되지 않습니다.
      - **일반 텍스트 문서의 경우:** 콘텐츠는 인용 가능한 문장으로 청크됩니다.
      - **사용자 정의 콘텐츠 문서의 경우:** 제공된 콘텐츠 블록이 그대로 사용되며 추가 청킹이 수행되지 않습니다.
  </Step>
  <Step title="Claude가 인용된 응답 제공">
    - 응답에는 이제 여러 텍스트 블록이 포함될 수 있으며, 각 텍스트 블록에는 Claude가 주장하는 내용과 해당 주장을 뒷받침하는 인용 목록이 포함될 수 있습니다.
    - 인용은 원본 문서의 특정 위치를 참조합니다. 이러한 인용의 형식은 인용되는 문서 유형에 따라 다릅니다.
      - **PDF의 경우:** 인용에는 페이지 번호 범위(1-인덱스)가 포함됩니다.
      - **일반 텍스트 문서의 경우:** 인용에는 문자 인덱스 범위(0-인덱스)가 포함됩니다.
      - **사용자 정의 콘텐츠 문서의 경우:** 인용에는 제공된 원본 콘텐츠 목록에 해당하는 콘텐츠 블록 인덱스 범위(0-인덱스)가 포함됩니다.
    - 문서 인덱스는 참조 소스를 나타내기 위해 제공되며, 원본 요청의 모든 문서 목록에 따라 0-인덱스로 지정됩니다.
  </Step>
</Steps>


> **자동 청킹 vs 사용자 정의 콘텐츠**
>
> 기본적으로 일반 텍스트와 PDF 문서는 자동으로 문장으로 청크됩니다. 인용 세분성에 대한 더 많은 제어가 필요한 경우(예: 글머리 기호 또는 대화록), 대신 사용자 정의 콘텐츠 문서를 사용하세요. 자세한 내용은 [문서 유형](#document-types)을 참조하세요.
>
> 예를 들어, Claude가 RAG 청크에서 특정 문장을 인용할 수 있도록 하려면 각 RAG 청크를 일반 텍스트 문서에 넣어야 합니다. 그렇지 않고 추가 청킹을 원하지 않거나 추가 청킹을 사용자 정의하려면 RAG 청크를 사용자 정의 콘텐츠 문서에 넣을 수 있습니다.


### 인용 가능 콘텐츠 vs 인용 불가능 콘텐츠

- 문서의 `source` 콘텐츠 내에 있는 텍스트는 인용 가능합니다.
- `title`과 `context`는 모델에 전달되지만 인용 가능한 콘텐츠로 사용되지 않는 선택적 필드입니다.
- `title`은 길이가 제한되어 있으므로 `context` 필드를 텍스트 또는 문자열화된 JSON으로 문서 메타데이터를 저장하는 데 유용하게 사용할 수 있습니다.

### 인용 인덱스
- 문서 인덱스는 요청의 모든 문서 콘텐츠 블록 목록(모든 메시지에 걸쳐)에서 0-인덱스로 지정됩니다.
- 문자 인덱스는 종료 인덱스가 배타적인 0-인덱스입니다.
- 페이지 번호는 종료 페이지 번호가 배타적인 1-인덱스입니다.
- 콘텐츠 블록 인덱스는 사용자 정의 콘텐츠 문서에 제공된 `content` 목록에서 종료 인덱스가 배타적인 0-인덱스입니다.

### 토큰 비용
- 인용을 활성화하면 시스템 프롬프트 추가 및 문서 청킹으로 인해 입력 토큰이 약간 증가합니다.
- 그러나 인용 기능은 출력 토큰에서 매우 효율적입니다. 내부적으로 모델은 표준화된 형식으로 인용을 출력하고, 이것이 인용된 텍스트와 문서 위치 인덱스로 파싱됩니다. `cited_text` 필드는 편의를 위해 제공되며 출력 토큰에 포함되지 않습니다.
- 후속 대화 턴에서 다시 전달될 때, `cited_text`는 입력 토큰에도 포함되지 않습니다.

### 기능 호환성
인용은 [프롬프트 캐싱](../02-capabilities/01-prompt-caching.md), [토큰 카운팅](../02-capabilities/09-token-counting.md), [배치 처리](../02-capabilities/06-batch-processing.md)를 포함한 다른 API 기능과 함께 작동합니다.


> **인용과 구조화된 출력은 호환되지 않습니다**
>
> 인용은 [구조화된 출력](../02-capabilities/15-structured-outputs.md)과 함께 사용할 수 없습니다. 사용자 제공 문서(Document 블록 또는 RequestSearchResultBlock)에서 인용을 활성화하고 동시에 `output_config.format` 매개변수를 포함하면 API는 400 오류를 반환합니다.
>
> 이는 인용이 텍스트 출력과 인용 블록을 교차 배치해야 하는데, 이것이 구조화된 출력의 엄격한 JSON 스키마 제약과 호환되지 않기 때문입니다.


#### 인용과 함께 프롬프트 캐싱 사용

인용과 프롬프트 캐싱은 효과적으로 함께 사용할 수 있습니다.

응답에서 생성된 인용 블록은 직접 캐시할 수 없지만, 참조하는 원본 문서는 캐시할 수 있습니다. 성능을 최적화하려면 최상위 문서 콘텐츠 블록에 `cache_control`을 적용하세요.

<CodeGroup>
```python Python
import anthropic

client = anthropic.Anthropic()

# 긴 문서 콘텐츠 (예: 기술 문서)
long_document = "This is a very long document with thousands of words..." + " ... " * 1000  # 캐시 가능한 최소 길이

response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[
        {
            "role": "user",
            "content": [
                {
                    "type": "document",
                    "source": {
                        "type": "text",
                        "media_type": "text/plain",
                        "data": long_document
                    },
                    "citations": {"enabled": True},
                    "cache_control": {"type": "ephemeral"}  # 문서 콘텐츠 캐시
                },
                {
                    "type": "text",
                    "text": "What does this document say about API features?"
                }
            ]
        }
    ]
)
```

```typescript TypeScript
import Anthropic from '@anthropic-ai/sdk';

const client = new Anthropic();

// 긴 문서 콘텐츠 (예: 기술 문서)
const longDocument = "This is a very long document with thousands of words..." + " ... ".repeat(1000);  // 캐시 가능한 최소 길이

const response = await client.messages.create({
  model: "claude-sonnet-4-5",
  max_tokens: 1024,
  messages: [
    {
      role: "user",
      content: [
        {
          type: "document",
          source: {
            type: "text",
            media_type: "text/plain",
            data: longDocument
          },
          citations: { enabled: true },
          cache_control: { type: "ephemeral" }  // 문서 콘텐츠 캐시
        },
        {
          type: "text",
          text: "What does this document say about API features?"
        }
      ]
    }
  ]
});
```

```bash Shell
curl https://api.anthropic.com/v1/messages \
     --header "x-api-key: $ANTHROPIC_API_KEY" \
     --header "anthropic-version: 2023-06-01" \
     --header "content-type: application/json" \
     --data '{
    "model": "claude-sonnet-4-5",
    "max_tokens": 1024,
    "messages": [
        {
            "role": "user",
            "content": [
                {
                    "type": "document",
                    "source": {
                        "type": "text",
                        "media_type": "text/plain",
                        "data": "This is a very long document with thousands of words..."
                    },
                    "citations": {"enabled": true},
                    "cache_control": {"type": "ephemeral"}
                },
                {
                    "type": "text",
                    "text": "What does this document say about API features?"
                }
            ]
        }
    ]
}'
```
</CodeGroup>

이 예시에서:
- 문서 콘텐츠는 문서 블록에 `cache_control`을 사용하여 캐시됩니다
- 문서에서 인용이 활성화됩니다
- Claude는 캐시된 문서 콘텐츠를 활용하면서 인용이 포함된 응답을 생성할 수 있습니다
- 동일한 문서를 사용하는 후속 요청은 캐시된 콘텐츠의 이점을 누릴 수 있습니다

## 문서 유형

### 문서 유형 선택

인용을 위해 세 가지 문서 유형을 지원합니다. 문서는 메시지에 직접 제공하거나(base64, 텍스트 또는 URL) [Files API](../02-capabilities/13-files-api.md)를 통해 업로드하고 `file_id`로 참조할 수 있습니다:

| 유형 | 적합한 용도 | 청킹 | 인용 형식 |
| :--- | :--- | :--- | :--- |
| 일반 텍스트 | 단순 텍스트 문서, 산문 | 문장 | 문자 인덱스 (0-인덱스) |
| PDF | 텍스트 콘텐츠가 있는 PDF 파일 | 문장 | 페이지 번호 (1-인덱스) |
| 사용자 정의 콘텐츠 | 목록, 대화록, 특수 서식, 보다 세분화된 인용 | 추가 청킹 없음 | 블록 인덱스 (0-인덱스) |


> .csv, .xlsx, .docx, .md, .txt 파일은 문서 블록으로 지원되지 않습니다. 이러한 파일을 일반 텍스트로 변환하여 메시지 콘텐츠에 직접 포함하세요. [다른 파일 형식 작업](../02-capabilities/13-files-api.md)을 참조하세요.


### 일반 텍스트 문서

일반 텍스트 문서는 자동으로 문장으로 청크됩니다. 인라인으로 제공하거나 `file_id`로 참조하여 제공할 수 있습니다:

<Tabs>
<Tab title="Inline text">
```python
{
    "type": "document",
    "source": {
        "type": "text",
        "media_type": "text/plain",
        "data": "Plain text content..."
    },
    "title": "Document Title", # 선택 사항
    "context": "Context about the document that will not be cited from", # 선택 사항
    "citations": {"enabled": True}
}
```
</Tab>
<Tab title="Files API">
```python
{
    "type": "document",
    "source": {
        "type": "file",
        "file_id": "file_011CNvxoj286tYUAZFiZMf1U"
    },
    "title": "Document Title", # 선택 사항
    "context": "Context about the document that will not be cited from", # 선택 사항
    "citations": {"enabled": True}
}
```
</Tab>
</Tabs>

<details>
<summary>Example plain text citation</summary>

```python
{
    "type": "char_location",
    "cited_text": "The exact text being cited", # 출력 토큰에 포함되지 않음
    "document_index": 0,
    "document_title": "Document Title",
    "start_char_index": 0,    # 0-인덱스
    "end_char_index": 50      # 배타적
}
```
</details>

### PDF 문서

PDF 문서는 base64로 인코딩된 데이터 또는 `file_id`로 제공할 수 있습니다. PDF 텍스트가 추출되어 문장으로 청크됩니다. 이미지 인용은 아직 지원되지 않으므로, 문서를 스캔한 PDF이며 추출 가능한 텍스트가 없는 경우 인용할 수 없습니다.

<Tabs>
<Tab title="Base64">
```python
{
    "type": "document",
    "source": {
        "type": "base64",
        "media_type": "application/pdf",
        "data": base64_encoded_pdf_data
    },
    "title": "Document Title", # 선택 사항
    "context": "Context about the document that will not be cited from", # 선택 사항
    "citations": {"enabled": True}
}
```
</Tab>
<Tab title="Files API">
```python
{
    "type": "document",
    "source": {
        "type": "file",
        "file_id": "file_011CNvxoj286tYUAZFiZMf1U"
    },
    "title": "Document Title", # 선택 사항
    "context": "Context about the document that will not be cited from", # 선택 사항
    "citations": {"enabled": True}
}
```
</Tab>
</Tabs>

<details>
<summary>Example PDF citation</summary>

```python
{
    "type": "page_location",
    "cited_text": "The exact text being cited", # 출력 토큰에 포함되지 않음
    "document_index": 0,
    "document_title": "Document Title",
    "start_page_number": 1,  # 1-인덱스
    "end_page_number": 2     # 배타적
}
```
</details>

### 사용자 정의 콘텐츠 문서

사용자 정의 콘텐츠 문서를 사용하면 인용 세분성을 제어할 수 있습니다. 추가 청킹이 수행되지 않으며 제공된 콘텐츠 블록에 따라 청크가 모델에 제공됩니다.

```python
{
    "type": "document",
    "source": {
        "type": "content",
        "content": [
            {"type": "text", "text": "First chunk"},
            {"type": "text", "text": "Second chunk"}
        ]
    },
    "title": "Document Title", # 선택 사항
    "context": "Context about the document that will not be cited from", # 선택 사항
    "citations": {"enabled": True}
}
```

<details>
<summary>Example citation</summary>

```python
{
    "type": "content_block_location",
    "cited_text": "The exact text being cited", # 출력 토큰에 포함되지 않음
    "document_index": 0,
    "document_title": "Document Title",
    "start_block_index": 0,   # 0-인덱스
    "end_block_index": 1      # 배타적
}
```
</details>

---

## 응답 구조

인용이 활성화되면 응답에 인용이 포함된 여러 텍스트 블록이 포함됩니다:

```python
{
    "content": [
        {
            "type": "text",
            "text": "According to the document, "
        },
        {
            "type": "text",
            "text": "the grass is green",
            "citations": [{
                "type": "char_location",
                "cited_text": "The grass is green.",
                "document_index": 0,
                "document_title": "Example Document",
                "start_char_index": 0,
                "end_char_index": 20
            }]
        },
        {
            "type": "text",
            "text": " and "
        },
        {
            "type": "text",
            "text": "the sky is blue",
            "citations": [{
                "type": "char_location",
                "cited_text": "The sky is blue.",
                "document_index": 0,
                "document_title": "Example Document",
                "start_char_index": 20,
                "end_char_index": 36
            }]
        },
        {
            "type": "text",
            "text": ". Information from page 5 states that ",
        },
        {
            "type": "text",
            "text": "water is essential",
            "citations": [{
                "type": "page_location",
                "cited_text": "Water is essential for life.",
                "document_index": 1,
                "document_title": "PDF Document",
                "start_page_number": 5,
                "end_page_number": 6
            }]
        },
        {
            "type": "text",
            "text": ". The custom document mentions ",
        },
        {
            "type": "text",
            "text": "important findings",
            "citations": [{
                "type": "content_block_location",
                "cited_text": "These are important findings.",
                "document_index": 2,
                "document_title": "Custom Content Document",
                "start_block_index": 0,
                "end_block_index": 1
            }]
        }
    ]
}
```

### 스트리밍 지원

스트리밍 응답의 경우, 현재 `text` 콘텐츠 블록의 `citations` 목록에 추가할 단일 인용을 포함하는 `citations_delta` 타입이 추가되었습니다.

<details>
<summary>Example streaming events</summary>

```python
event: message_start
data: {"type": "message_start", ...}

event: content_block_start
data: {"type": "content_block_start", "index": 0, ...}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0,
       "delta": {"type": "text_delta", "text": "According to..."}}

event: content_block_delta
data: {"type": "content_block_delta", "index": 0,
       "delta": {"type": "citations_delta",
                 "citation": {
                     "type": "char_location",
                     "cited_text": "...",
                     "document_index": 0,
                     ...
                 }}}

event: content_block_stop
data: {"type": "content_block_stop", "index": 0}

event: message_stop
data: {"type": "message_stop"}
```
</details>

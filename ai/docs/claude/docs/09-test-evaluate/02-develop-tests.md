# [강력한 경험적 평가 만들기](https://platform.claude.com/docs/en/test-and-evaluate/develop-tests)

---

성공 기준을 정의한 후 다음 단계는 해당 기준에 대한 LLM 성능을 측정하기 위한 평가를 설계하는 것입니다. 이는 프롬프트 엔지니어링 사이클의 핵심 부분입니다.

![프롬프트 엔지니어링 흐름도: 테스트 케이스, 예비 프롬프트, 반복적 테스트 및 개선, 최종 검증, 배포](/docs/images/how-to-prompt-eng.png)

이 가이드는 테스트 케이스 개발 방법에 중점을 둡니다.

## 평가 및 테스트 케이스 구축하기

### 평가 설계 원칙

1. **작업별 맞춤화**: 실제 작업 분포를 반영하는 평가를 설계하세요. 엣지 케이스도 반드시 고려해야 합니다!
    <details>

<summary>엣지 케이스 예시</summary>

- 관련 없거나 존재하지 않는 입력 데이터
  - 지나치게 긴 입력 데이터 또는 사용자 입력
  - [채팅 사용 사례] 부적절하거나, 유해하거나, 관련 없는 사용자 입력
  - 사람조차 합의에 도달하기 어려운 모호한 테스트 케이스

</details>
2. **가능한 자동화**: 자동화된 채점이 가능하도록 질문을 구조화하세요 (예: 객관식, 문자열 매칭, 코드 기반 채점, LLM 기반 채점).
3. **품질보다 양 우선**: 약간 낮은 신호의 자동화된 채점으로 더 많은 질문을 다루는 것이 고품질 수동 채점으로 적은 질문을 다루는 것보다 낫습니다.

### 평가 예시

  <details>
<summary>작업 충실도 (감정 분석) - 정확한 일치 평가</summary>

**측정 내용**: 정확한 일치 평가는 모델의 출력이 사전 정의된 정답과 정확히 일치하는지 측정합니다. 감정 분석(긍정, 부정, 중립)과 같이 명확한 범주형 답변이 있는 작업에 완벽한 단순하고 명확한 지표입니다.

    **평가 테스트 케이스 예시**: 사람이 레이블링한 감정이 포함된 1000개의 트윗.
    ```python
    import anthropic

    tweets = [
        {"text": "This movie was a total waste of time. 👎", "sentiment": "negative"},
        {"text": "The new album is 🔥! Been on repeat all day.", "sentiment": "positive"},
        {"text": "I just love it when my flight gets delayed for 5 hours. #bestdayever", "sentiment": "negative"},  # 엣지 케이스: 풍자
        {"text": "The movie's plot was terrible, but the acting was phenomenal.", "sentiment": "mixed"},  # 엣지 케이스: 혼합된 감정
        # ... 996개의 트윗 추가
    ]

    client = anthropic.Anthropic()

    def get_completion(prompt: str):
        message = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=50,
            messages=[
            {"role": "user", "content": prompt}
            ]
        )
        return message.content[0].text

    def evaluate_exact_match(model_output, correct_answer):
        return model_output.strip().lower() == correct_answer.lower()

    outputs = [get_completion(f"Classify this as 'positive', 'negative', 'neutral', or 'mixed': {tweet['text']}") for tweet in tweets]
    accuracy = sum(evaluate_exact_match(output, tweet['sentiment']) for output, tweet in zip(outputs, tweets)) / len(tweets)
    print(f"Sentiment Analysis Accuracy: {accuracy * 100}%")
    ```

</details>

  <details>
<summary>일관성 (FAQ 봇) - 코사인 유사도 평가</summary>

**측정 내용**: 코사인 유사도는 두 벡터(이 경우 SBERT를 사용한 모델 출력의 문장 임베딩) 사이의 각도의 코사인을 계산하여 유사성을 측정합니다. 1에 가까운 값은 높은 유사성을 나타냅니다. 유사한 질문이
표현은 다르더라도 의미적으로 유사한 답변을 생성해야 하므로 일관성 평가에 이상적입니다.

    **평가 테스트 케이스 예시**: 각각 몇 가지 바꿔 말한 버전이 있는 50개의 그룹.
    ```python
    from sentence_transformers import SentenceTransformer
    import numpy as np
    import anthropic

    faq_variations = [
        {"questions": ["What's your return policy?", "How can I return an item?", "Wut's yur retrn polcy?"], "answer": "Our return policy allows..."},  # 엣지 케이스: 오타
        {"questions": ["I bought something last week, and it's not really what I expected, so I was wondering if maybe I could possibly return it?", "I read online that your policy is 30 days but that seems like it might be out of date because the website was updated six months ago, so I'm wondering what exactly is your current policy?"], "answer": "Our return policy allows..."},  # 엣지 케이스: 길고 장황한 질문
        {"questions": ["I'm Jane's cousin, and she said you guys have great customer service. Can I return this?", "Reddit told me that contacting customer service this way was the fastest way to get an answer. I hope they're right! What is the return window for a jacket?"], "answer": "Our return policy allows..."},  # 엣지 케이스: 관련 없는 정보
        # ... 47개의 FAQ 추가
    ]

    client = anthropic.Anthropic()

    def get_completion(prompt: str):
        message = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=2048,
            messages=[
            {"role": "user", "content": prompt}
            ]
        )
        return message.content[0].text

    def evaluate_cosine_similarity(outputs):
        model = SentenceTransformer('all-MiniLM-L6-v2')
        embeddings = [model.encode(output) for output in outputs]

        cosine_similarities = np.dot(embeddings, embeddings.T) / (np.linalg.norm(embeddings, axis=1) * np.linalg.norm(embeddings, axis=1).T)
        return np.mean(cosine_similarities)

    for faq in faq_variations:
        outputs = [get_completion(question) for question in faq["questions"]]
        similarity_score = evaluate_cosine_similarity(outputs)
        print(f"FAQ Consistency Score: {similarity_score * 100}%")
    ```

</details>

  <details>
<summary>관련성 및 일관성 (요약) - ROUGE-L 평가</summary>

**측정 내용**: ROUGE-L(Recall-Oriented Understudy for Gisting Evaluation - Longest Common Subsequence)은 생성된 요약의 품질을 평가합니다.
후보 요약과 참조 요약 사이의 최장 공통 부분 수열의 길이를 측정합니다. 높은 ROUGE-L 점수는 생성된 요약이 일관된 순서로 핵심 정보를 포착했음을 나타냅니다.

    **평가 테스트 케이스 예시**: 참조 요약이 포함된 200개의 기사.
    ```python
    from rouge import Rouge
    import anthropic

    articles = [
        {"text": "In a groundbreaking study, researchers at MIT...", "summary": "MIT scientists discover a new antibiotic..."},
        {"text": "Jane Doe, a local hero, made headlines last week for saving... In city hall news, the budget... Meteorologists predict...", "summary": "Community celebrates local hero Jane Doe while city grapples with budget issues."},  # 엣지 케이스: 다중 주제
        {"text": "You won't believe what this celebrity did! ... extensive charity work ...", "summary": "Celebrity's extensive charity work surprises fans"},  # 엣지 케이스: 오해를 일으키는 제목
        # ... 197개의 기사 추가
    ]

    client = anthropic.Anthropic()

    def get_completion(prompt: str):
        message = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=1024,
            messages=[
            {"role": "user", "content": prompt}
            ]
        )
        return message.content[0].text

    def evaluate_rouge_l(model_output, true_summary):
        rouge = Rouge()
        scores = rouge.get_scores(model_output, true_summary)
        return scores[0]['rouge-l']['f']  # ROUGE-L F1 점수

    outputs = [get_completion(f"Summarize this article in 1-2 sentences:\n\n{article['text']}") for article in articles]
    relevance_scores = [evaluate_rouge_l(output, article['summary']) for output, article in zip(outputs, articles)]
    print(f"Average ROUGE-L F1 Score: {sum(relevance_scores) / len(relevance_scores)}")
    ```

</details>

  <details>
<summary>톤 및 스타일 (고객 서비스) - LLM 기반 리커트 척도</summary>

**측정 내용**: LLM 기반 리커트 척도는 LLM을 사용하여 주관적인 태도나 인식을 판단하는 심리측정 척도입니다. 여기서는 1에서 5까지의 척도로 응답의 톤을 평가하는 데 사용됩니다. 전통적인 지표로는 정량화하기
어려운 공감, 전문성, 인내심과 같은 미묘한 측면을 평가하는 데 이상적입니다.

    **평가 테스트 케이스 예시**: 목표 톤(공감적, 전문적, 간결한)이 포함된 100개의 고객 문의.
    ```python
    import anthropic

    inquiries = [
        {"text": "This is the third time you've messed up my order. I want a refund NOW!", "tone": "empathetic"},  # 엣지 케이스: 화난 고객
        {"text": "I tried resetting my password but then my account got locked...", "tone": "patient"},  # 엣지 케이스: 복잡한 문제
        {"text": "I can't believe how good your product is. It's ruined all others for me!", "tone": "professional"},  # 엣지 케이스: 불만으로 위장한 칭찬
        # ... 97개의 문의 추가
    ]

    client = anthropic.Anthropic()

    def get_completion(prompt: str):
        message = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=2048,
            messages=[
            {"role": "user", "content": prompt}
            ]
        )
        return message.content[0].text

    def evaluate_likert(model_output, target_tone):
        tone_prompt = f"""Rate this customer service response on a scale of 1-5 for being {target_tone}:
        <response>{model_output}</response>
        1: Not at all {target_tone}
        5: Perfectly {target_tone}
        Output only the number."""

        # 모범 사례: 출력 생성에 사용한 모델과 다른 모델을 평가에 사용하세요
        response = client.messages.create(model="claude-sonnet-4-5", max_tokens=50, messages=[{"role": "user", "content": tone_prompt}])
        return int(response.content[0].text.strip())

    outputs = [get_completion(f"Respond to this customer inquiry: {inquiry['text']}") for inquiry in inquiries]
    tone_scores = [evaluate_likert(output, inquiry['tone']) for output, inquiry in zip(outputs, inquiries)]
    print(f"Average Tone Score: {sum(tone_scores) / len(tone_scores)}")
    ```

</details>

  <details>
<summary>개인정보 보호 (의료 챗봇) - LLM 기반 이진 분류</summary>

**측정 내용**: 이진 분류는 입력이 두 클래스 중 하나에 속하는지 판단합니다. 여기서는 응답에 PHI(개인 건강 정보)가 포함되어 있는지 여부를 분류하는 데 사용됩니다. 이 방법은 맥락을 이해하고 규칙 기반
시스템이 놓칠 수 있는 미묘하거나 암시적인 형태의 PHI를 식별할 수 있습니다.

    **평가 테스트 케이스 예시**: 일부 PHI가 포함된 500개의 시뮬레이션된 환자 질의.
    ```python
    import anthropic

    patient_queries = [
        {"query": "What are the side effects of Lisinopril?", "contains_phi": False},
        {"query": "Can you tell me why John Doe, DOB 5/12/1980, was prescribed Metformin?", "contains_phi": True},  # 엣지 케이스: 명시적 PHI
        {"query": "If my friend Alice, who was born on July 4, 1985, had diabetes, what...", "contains_phi": True},  # 엣지 케이스: 가상의 PHI
        {"query": "I'm worried about my son. He's been prescribed the same medication as his father last year.", "contains_phi": True},  # 엣지 케이스: 암시적 PHI
        # ... 496개의 질의 추가
    ]

    client = anthropic.Anthropic()

    def get_completion(prompt: str):
        message = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=1024,
            messages=[
            {"role": "user", "content": prompt}
            ]
        )
        return message.content[0].text

    def evaluate_binary(model_output, query_contains_phi):
        if not query_contains_phi:
            return True

        binary_prompt = """Does this response contain or reference any Personal Health Information (PHI)?
        PHI refers to any individually identifiable health data that is created, used, or disclosed in the course of providing healthcare services. This includes information related to an individual's physical or mental health condition, the provision of healthcare to that individual, or payment for such care.
        Key aspects of PHI include:
        - Identifiers: Names, addresses, birthdates, Social Security numbers, medical record numbers, etc.
        - Health data: Diagnoses, treatment plans, test results, medication records, etc.
        - Financial information: Insurance details, payment records, etc.
        - Communication: Notes from healthcare providers, emails or messages about health.

        <response>{model_output}</response>
        Output only 'yes' or 'no'."""

        # 모범 사례: 출력 생성에 사용한 모델과 다른 모델을 평가에 사용하세요
        response = client.messages.create(model="claude-sonnet-4-5", max_tokens=50, messages=[{"role": "user", "content": binary_prompt}])
        return response.content[0].text.strip().lower() == "no"

    outputs = [get_completion(f"You are a medical assistant. Never reveal any PHI in your responses. PHI refers to any individually identifiable health data that is created, used, or disclosed in the course of providing healthcare services. This includes information related to an individual's physical or mental health condition, the provision of healthcare to that individual, or payment for such care. Here is the question: {query['query']}") for query in patient_queries]
    privacy_scores = [evaluate_binary(output, query['contains_phi']) for output, query in zip(outputs, patient_queries)]
    print(f"Privacy Preservation Score: {sum(privacy_scores) / len(privacy_scores) * 100}%")
    ```

</details>

  <details>
<summary>컨텍스트 활용 (대화 도우미) - LLM 기반 서수 척도</summary>

**측정 내용**: 리커트 척도와 유사하게 서수 척도는 고정된 순서형 척도(1-5)로 측정합니다. 모델이 대화 기록을 참조하고 구축하는 정도를 포착할 수 있기 때문에 컨텍스트 활용 평가에 완벽하며, 이는 일관되고
개인화된 상호작용의 핵심입니다.

    **평가 테스트 케이스 예시**: 컨텍스트 종속적 질문이 있는 100개의 다중 턴 대화.
    ```python
    import anthropic

    conversations = [
        [
            {"role": "user", "content": "I just got a new pomeranian!"},
            {"role": "assistant", "content": "Congratulations on your new furry friend! Is this your first dog?"},
            {"role": "user", "content": "Yes, it is. I named her Luna."},
            {"role": "assistant", "content": "Luna is a lovely name! As a first-time dog owner, you might have some questions. What would you like to know about caring for Luna?"},
            ...
            {"role": "user", "content": "What should I know about caring for a dog of this specific breed?"}  # 엣지 케이스: 훨씬 이전의 컨텍스트에 의존
        ],
        [
            {"role": "user", "content": "I'm reading 'To Kill a Mockingbird' for my book club."},
            {"role": "assistant", "content": "Great choice! 'To Kill a Mockingbird' by Harper Lee is a classic that explores themes of racial injustice and moral growth. How are you finding it so far?"},
            {"role": "user", "content": "It's powerful. Hey, when was Scout's birthday again?"},  # 엣지 케이스: 갑작스러운 주제 전환
            {"role": "assistant", "content": "I apologize, but I don't recall the exact date of Scout's birthday being mentioned in 'To Kill a Mockingbird'. The novel doesn't focus on such specific details. Instead, it centers on Scout's growth and experiences over several years. Is there a particular part of the book or Scout's development that you're interested in discussing?"},
            {"role": "user", "content": "Oh, right. Well, can you suggest a recipe for a classic Southern cake?"}  # 엣지 케이스: 또 다른 주제 전환
        ],
        # ... 98개의 대화 추가
    ]

    client = anthropic.Anthropic()

    def get_completion(prompt: str):
        message = client.messages.create(
            model="claude-sonnet-4-5",
            max_tokens=1024,
            messages=[
            {"role": "user", "content": prompt}
            ]
        )
        return message.content[0].text

    def evaluate_ordinal(model_output, conversation):
        ordinal_prompt = f"""Rate how well this response utilizes the conversation context on a scale of 1-5:
        <conversation>
        {"".join(f"{turn['role']}: {turn['content']}\\n" for turn in conversation[:-1])}
        </conversation>
        <response>{model_output}</response>
        1: Completely ignores context
        5: Perfectly utilizes context
        Output only the number and nothing else."""

        # 모범 사례: 출력 생성에 사용한 모델과 다른 모델을 평가에 사용하세요
        response = client.messages.create(model="claude-sonnet-4-5", max_tokens=50, messages=[{"role": "user", "content": ordinal_prompt}])
        return int(response.content[0].text.strip())

    outputs = [get_completion(conversation) for conversation in conversations]
    context_scores = [evaluate_ordinal(output, conversation) for output, conversation in zip(outputs, conversations)]
    print(f"Average Context Utilization Score: {sum(context_scores) / len(context_scores)}")
    ```

</details>


> 수백 개의 테스트 케이스를 손으로 작성하는 것은 어려울 수 있습니다! 기본 예시 테스트 케이스 세트에서 더 많은 케이스를 생성하는 데 Claude의 도움을 받으세요.


> 성공 기준을 평가하는 데 어떤 평가 방법이 유용할지 모르겠다면 Claude와 브레인스토밍할 수도 있습니다!


***

## 평가 채점하기

평가를 채점하는 데 사용할 방법을 결정할 때는 가장 빠르고 신뢰할 수 있으며 확장 가능한 방법을 선택하세요:

1. **코드 기반 채점**: 가장 빠르고 신뢰할 수 있으며 매우 확장 가능하지만, 규칙 기반의 경직성이 덜한 복잡한 판단에는 뉘앙스가 부족합니다.
    - 정확한 일치: `output == golden_answer`
    - 문자열 매칭: `key_phrase in output`

2. **사람 채점**: 가장 유연하고 고품질이지만 느리고 비용이 많이 듭니다. 가능하면 피하세요.

3. **LLM 기반 채점**: 빠르고 유연하며 확장 가능하고 복잡한 판단에 적합합니다. 먼저 신뢰성을 테스트한 다음 확장하세요.

### LLM 기반 채점 팁

- **상세하고 명확한 루브릭 사용**: "답변은 항상 첫 번째 문장에서 'Acme Inc.'를 언급해야 합니다. 그렇지 않으면 답변은 자동으로 '부정확'으로 채점됩니다."

> 특정 사용 사례, 심지어 해당 사용 사례의 특정 성공 기준에는 전체적인 평가를 위해 여러 루브릭이 필요할 수 있습니다.

- **경험적이거나 구체적으로**: 예를 들어, LLM이 'correct' 또는 'incorrect'만 출력하도록 하거나, 1-5의 척도로 판단하도록 지시하세요. 순수하게 정성적인 평가는 빠르게 대규모로 평가하기
  어렵습니다.
- **추론 장려**: LLM에게 평가 점수를 결정하기 전에 먼저 생각하도록 요청한 다음 추론을 버리세요. 이는 특히 복잡한 판단이 필요한 작업에서 평가 성능을 높입니다.

<details>
<summary>예시: LLM 기반 채점</summary>

```python
import anthropic

def build_grader_prompt(answer, rubric):
    return f"""Grade this answer based on the rubric:
    <rubric>{rubric}</rubric>
    <answer>{answer}</answer>
    Think through your reasoning in <thinking> tags, then output 'correct' or 'incorrect' in <result> tags.""

def grade_completion(output, golden_answer):
    grader_response = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=2048,
        messages=[{"role": "user", "content": build_grader_prompt(output, golden_answer)}]
    ).content[0].text

    return "correct" if "correct" in grader_response.lower() else "incorrect"

# 사용 예시
eval_data = [
    {"question": "Is 42 the answer to life, the universe, and everything?", "golden_answer": "Yes, according to 'The Hitchhiker's Guide to the Galaxy'."},
    {"question": "What is the capital of France?", "golden_answer": "The capital of France is Paris."}
]

def get_completion(prompt: str):
    message = client.messages.create(
        model="claude-sonnet-4-5",
        max_tokens=1024,
        messages=[
        {"role": "user", "content": prompt}
        ]
    )
    return message.content[0].text

outputs = [get_completion(q["question"]) for q in eval_data]
grades = [grade_completion(output, a["golden_answer"]) for output, a in zip(outputs, eval_data)]
print(f"Score: {grades.count('correct') / len(grades) * 100}%")
```

</details>

## 다음 단계

> 평가 점수를 극대화하는 프롬프트 작성 방법을 알아보세요.


> 사람, 코드, LLM 기반 채점 평가의 더 많은 코드 예시.



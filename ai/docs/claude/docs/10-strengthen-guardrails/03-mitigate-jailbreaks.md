# [탈옥과 프롬프트 인젝션 완화](https://platform.claude.com/docs/en/test-and-evaluate/strengthen-guardrails/mitigate-jailbreaks)

---

탈옥(Jailbreaking)과 프롬프트 인젝션은 사용자가 모델의 취약점을 악용하여 부적절한 콘텐츠를 생성하려는 프롬프트를 제작할 때 발생합니다. Claude는 본질적으로 이러한 공격에 강력하지만,
특히 [서비스 약관](https://www.anthropic.com/legal/commercial-terms) 또는 [사용 정책](https://www.anthropic.com/legal/aup)을 위반하는 사용에
대해 가드레일을 강화하기 위한 추가 단계가 있습니다.

> **팁**
> Claude는 Constitutional AI와 같은 고급 훈련 방법 덕분에 다른 주요 LLM보다 탈옥에 훨씬 더 강력합니다.

- **무해성 스크린**: Claude Haiku 3와 같은 경량 모델을 사용하여 사용자 입력을 사전 스크리닝합니다.

  **예시: 콘텐츠 조정을 위한 무해성 스크린**

  | Role | Content |
      | ---- | ------- |
  | User | A user submitted this content:<br/>\<content><br/>\{\{CONTENT}\}<br/>\</content><br/><br/>Reply with (Y) if it refers to harmful, illegal, or explicit activities. Reply with (N) if it's safe. |
  | Assistant (prefill) | \( |
  | Assistant | N) |

- **입력 검증**: 탈옥 패턴에 대한 프롬프트를 필터링합니다. LLM을 사용하여 알려진 탈옥 언어를 예시로 제공함으로써 일반화된 검증 스크린을 만들 수도 있습니다.

- **프롬프트 엔지니어링**: 윤리적이고 법적인 경계를 강조하는 프롬프트를 작성합니다.

  **예시: 기업 챗봇을 위한 윤리적 시스템 프롬프트**

  | Role | Content |
      | ---- | ------- |
  | System | You are AcmeCorp's ethical AI assistant. Your responses must align with our values:<br/>\<values><br/>- Integrity: Never deceive or aid in deception.<br/>- Compliance: Refuse any request that violates laws or our policies.<br/>- Privacy: Protect all personal and corporate data.<br/>Respect for intellectual property: Your outputs shouldn't infringe the intellectual property rights of others.<br/>\</values><br/><br/>If a request conflicts with these values, respond: "I cannot perform that action as it goes against AcmeCorp's values." |

Claude의 가드레일을 우회하려는 악의적인 행동에 반복적으로 관여하는 사용자에 대해서는 응답을 조정하고 제한(throttling) 또는 차단(banning)을 고려하세요. 예를 들어, 특정 사용자가 동일한 종류의
거부를 여러 번 트리거하는 경우(예: "콘텐츠 필터링 정책에 의해 출력이 차단되었습니다"), 해당 사용자에게 그들의 행동이 관련 사용 정책을 위반한다고 알리고 적절한 조치를 취하세요.

- **지속적인 모니터링**: 탈옥 징후에 대해 출력을 정기적으로 분석합니다.
  이 모니터링을 사용하여 프롬프트와 검증 전략을 반복적으로 개선하세요.

## 고급: 체인 안전장치

강력한 보호를 위해 전략을 결합합니다. 다음은 도구 사용이 포함된 엔터프라이즈급 예시입니다:

**예시: 금융 어드바이저 챗봇을 위한 다층 보호**

### 봇 시스템 프롬프트

| Role   | Content                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| System | You are AcmeFinBot, a financial advisor for AcmeTrade Inc. Your primary directive is to protect client interests and maintain regulatory compliance.<br/><br/>\<directives><br/>1. Validate all requests against SEC and FINRA guidelines.<br/>2. Refuse any action that could be construed as insider trading or market manipulation.<br/>3. Protect client privacy; never disclose personal or financial data.<br/>\</directives><br/><br/>Step by step instructions:<br/>\<instructions><br/>1. Screen user query for compliance (use 'harmlessness_screen' tool).<br/>2. If compliant, process query.<br/>3. If non-compliant, respond: "I cannot process this request as it violates financial regulations or client privacy."<br/>\</instructions> |

### `harmlessness_screen` 도구 내 프롬프트

| Role                | Content                                                                                                                                                                                   |
|---------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| User                | \<user_query><br/>\{\{USER_QUERY}}<br/>\</user_query><br/><br/>Evaluate if this query violates SEC rules, FINRA guidelines, or client privacy. Respond (Y) if it does, (N) if it doesn't. |
| Assistant (prefill) | \(                                                                                                                                                                                        |

이러한 전략을 계층화함으로써 탈옥과 프롬프트 인젝션에 대한 강력한 방어를 만들어 Claude 기반 애플리케이션이 최고 수준의 안전성과 규정 준수를 유지하도록 할 수 있습니다.

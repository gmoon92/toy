# [긴 컨텍스트 프롬프팅 팁](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/long-context-tips)

---

> **참고**
>
> 이 팁들은 모든 Claude 모델에 광범위하게 적용되지만, extended thinking 모델에 특화된 프롬프팅 팁은 [여기](../08-prompt-engineering/13-extended-thinking-tips.md)에서 확인할 수 있습니다.

Claude의 확장된 컨텍스트 윈도우(Claude 3 모델의 경우 200K 토큰)는 복잡하고 데이터가 풍부한 작업을 처리할 수 있게 해줍니다. 이 가이드는 이러한 강력한 기능을 효과적으로 활용하는 데 도움이 될 것입니다.

## 긴 컨텍스트 프롬프트를 위한 필수 팁

- **긴 형식의 데이터를 상단에 배치하세요**: 긴 문서와 입력(~20K+ 토큰)을 프롬프트 상단, 즉 질문, 지시사항, 예시보다 위에 배치하세요. 이는 모든 모델에서 Claude의 성능을 크게 향상시킬 수 있습니다.

    > **참고**: 테스트 결과, 특히 복잡한 다중 문서 입력의 경우 질문을 마지막에 배치하면 응답 품질이 최대 30%까지 향상될 수 있습니다.

- **XML 태그로 문서 내용과 메타데이터를 구조화하세요**: 여러 문서를 사용할 때는 각 문서를 `<document>` 태그로 감싸고 명확성을 위해 `<document_content>` 및 `<source>`(및 기타 메타데이터) 하위 태그를 사용하세요.

    **다중 문서 구조 예시**

    ```xml
    <documents>
      <document index="1">
        <source>annual_report_2023.pdf</source>
        <document_content>
          {{ANNUAL_REPORT}}
        </document_content>
      </document>
      <document index="2">
        <source>competitor_analysis_q2.xlsx</source>
        <document_content>
          {{COMPETITOR_ANALYSIS}}
        </document_content>
      </document>
    </documents>

    연간 보고서와 경쟁사 분석을 분석하세요. 전략적 우위를 파악하고 3분기 집중 영역을 추천하세요.
    ```

- **인용문으로 응답을 뒷받침하세요**: 긴 문서 작업의 경우, Claude가 작업을 수행하기 전에 먼저 문서의 관련 부분을 인용하도록 요청하세요. 이는 Claude가 문서 내용의 나머지 "노이즈"를 걸러내는 데 도움이 됩니다.

    **인용문 추출 예시**

    ```xml
    당신은 AI 의사 보조입니다. 당신의 임무는 의사들이 환자의 가능한 질병을 진단하는 것을 돕는 것입니다.

    <documents>
      <document index="1">
        <source>patient_symptoms.txt</source>
        <document_content>
          {{PATIENT_SYMPTOMS}}
        </document_content>
      </document>
      <document index="2">
        <source>patient_records.txt</source>
        <document_content>
          {{PATIENT_RECORDS}}
        </document_content>
      </document>
      <document index="3">
        <source>patient01_appt_history.txt</source>
        <document_content>
          {{PATIENT01_APPOINTMENT_HISTORY}}
        </document_content>
      </document>
    </documents>

    환자 기록과 진료 이력에서 환자가 보고한 증상을 진단하는 데 관련된 인용문을 찾으세요. 이를 <quotes> 태그에 넣으세요. 그런 다음 이러한 인용문을 기반으로 의사가 환자의 증상을 진단하는 데 도움이 될 모든 정보를 나열하세요. 진단 정보는 <info> 태그에 넣으세요.
    ```

---

## 관련 리소스

- **프롬프트 라이브러리**: 다양한 작업과 사용 사례를 위한 엄선된 프롬프트 모음에서 영감을 얻으세요. [바로가기](/docs/en/resources/prompt-library/library)

- **GitHub 프롬프팅 튜토리얼**: 우리 문서에서 찾을 수 있는 프롬프트 엔지니어링 개념을 다루는 예시가 풍부한 튜토리얼입니다. [바로가기](https://github.com/anthropics/prompt-eng-interactive-tutorial)

- **Google Sheets 프롬프팅 튜토리얼**: 대화형 스프레드시트를 통한 간편한 버전의 프롬프트 엔지니어링 튜토리얼입니다. [바로가기](https://docs.google.com/spreadsheets/d/19jzLgRruG9kjUQNKtCg1ZjdD6l6weA6qRXG5zLIAhC8)

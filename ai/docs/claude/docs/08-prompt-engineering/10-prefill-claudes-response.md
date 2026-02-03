# [더 강력한 출력 제어를 위한 Claude 응답 미리 채우기](https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/prefill-claudes-response)

---

> **참고**
> 이러한 팁은 모든 Claude 모델에 광범위하게 적용되지만, 확장된 사고 모델에 특화된 프롬프팅 팁은 [여기](../08-prompt-engineering/13-extended-thinking-tips.md)에서 확인할 수 있습니다.

> **참고**
> 미리 채우기는 확장된 사고 모드가 아닌 경우에만 사용할 수 있습니다. 현재 확장된 사고에서는 지원되지 않습니다.

Claude를 사용할 때, `Assistant` 메시지를 미리 채워서 응답을 안내할 수 있는 고유한 기능을 활용할 수 있습니다. 이 강력한 기법을 사용하면 Claude의 동작을 지시하고, 서문을 건너뛰고, JSON이나 XML과 같은 특정 형식을 강제하며, 롤플레이 시나리오에서 Claude가 캐릭터 일관성을 유지하도록 도울 수 있습니다.

Claude가 예상대로 작동하지 않는 경우, 미리 채운 몇 문장만으로도 Claude의 성능을 크게 향상시킬 수 있습니다. 조금의 미리 채우기가 큰 효과를 발휘합니다!

## Claude의 응답을 미리 채우는 방법

미리 채우려면 `Assistant` 메시지에 원하는 초기 텍스트를 포함시키세요 (Claude의 응답은 `Assistant` 메시지가 끝나는 지점부터 계속됩니다):

```python
import anthropic

client = anthropic.Anthropic()
response = client.messages.create(
    model="claude-sonnet-4-5",
    max_tokens=1024,
    messages=[
        {"role": "user", "content": "What is your favorite color?"},
        {"role": "assistant", "content": "As an AI assistant, I don't have a favorite color, But if I had to pick, it would be green because"}  # 여기서 미리 채우기
    ]
)
```

> **경고**
> 미리 채우기 콘텐츠는 후행 공백으로 끝날 수 없습니다. `"As an AI assistant, I "` (끝에 공백이 있는)와 같은 미리 채우기는 오류를 발생시킵니다.

### 예제

#### 예제 1: 출력 형식 제어 및 서문 건너뛰기

> **파워 유저 팁**: `{`를 미리 채우면 Claude가 서문을 건너뛰고 JSON 객체를 직접 출력하도록 강제합니다. 이는 더 깔끔하고 간결하며 추가 처리 없이 프로그램이 파싱하기 쉽습니다.
> 특정 스키마를 준수하는 보장된 JSON 출력을 원한다면, 미리 채우기 대신 [구조화된 출력](../02-capabilities/15-structured-outputs.md)을 사용하는 것을 고려하세요. 구조화된 출력은 Claude의 응답이 정의된 JSON 스키마와 항상 일치하도록 보장하므로 엄격한 형식 준수가 필요한 프로덕션 애플리케이션에 이상적입니다.

**예제: 미리 채우기 없이 구조화된 데이터 추출**

| 역할 | 내용 |
| ---- | ------- |
| User | 이 제품 설명에서 이름, 크기, 가격, 색상을 JSON 객체로 추출하세요:<br/><br/>\<description><br/>SmartHome Mini는 단 $49.99에 블랙 또는 화이트로 제공되는 컴팩트한 스마트 홈 어시스턴트입니다. 폭이 5인치에 불과하여, 집 어디에 배치하든 음성 또는 앱을 통해 조명, 온도 조절기 및 기타 연결된 장치를 제어할 수 있습니다. 이 저렴한 작은 허브는 스마트 장치에 편리한 핸즈프리 제어를 제공합니다.<br/>\</description> |
| Assistant | 다음은 JSON 형식으로 추출된 정보입니다:<br/><br/>\`\`\`json<br/>\{<br/>  "name": "SmartHome Mini",<br/>  "size": "5 inches wide",<br/>  "price": "\$49.99",<br/>  "colors": ["black", "white"]<br/>\}<br/>\`\`\`<br/><br/>제품 설명에서 다음 세부 정보를 추출했습니다:<br/>- **이름**: SmartHome Mini<br/>- **크기**: 폭 5인치<br/>- **가격**: \$49.99<br/>- **색상**: 블랙과 화이트 제공<br/><br/>JSON 객체는 프로그램이 파싱하고 사용하기 쉬운 구조화된 형식을 제공합니다. 수정이 필요하거나 다른 질문이 있으면 알려주세요! |

**예제: 미리 채우기를 사용한 구조화된 데이터 추출**

| 역할 | 내용 |
| ---- | ------- |
| User | 이 제품 설명에서 이름, 크기, 가격, 색상을 JSON 객체로 추출하세요:<br/><br/>\<description><br/>SmartHome Mini는 단 $49.99에 블랙 또는 화이트로 제공되는 컴팩트한 스마트 홈 어시스턴트입니다. 폭이 5인치에 불과하여, 집 어디에 배치하든 음성 또는 앱을 통해 조명, 온도 조절기 및 기타 연결된 장치를 제어할 수 있습니다. 이 저렴한 작은 허브는 스마트 장치에 편리한 핸즈프리 제어를 제공합니다.<br/>\</description> |
| Assistant (미리 채우기) | \{ |
| Assistant (Claude의 응답) | "name": "SmartHome Mini",<br/>"size": "5 inches wide",<br/>"price": "\$49.99",<br/>"colors": [<br/>  "black",<br/>  "white"<br/>]<br/>\} |

#### 예제 2: 롤플레이 시나리오에서 캐릭터 유지

> **롤플레이 팁**: 대괄호로 묶인 `[ROLE_NAME]`을 미리 채우면 더 길고 복잡한 대화에서도 Claude가 캐릭터를 유지하도록 상기시킬 수 있습니다. 이는 `system` 파라미터에서 역할 프롬프팅과 결합할 때 특히 강력합니다.

**예제: 역할 프롬프팅 없이 캐릭터 유지**

| 역할 | 내용 |
| ---- | ------- |
| User ("Sherlock Holmes" Claude와 50번의 대화 턴 진행 중) | 이 신발의 주인에 대해 무엇을 추론하나요? \<image>muddy_shoe.jpg\</image> |
| Assistant | AI 어시스턴트로서, 제공된 것처럼 진흙 묻은 신발 이미지를 분석해 드리겠습니다. 제 추론은 다음과 같습니다... |

**예제: 역할 프롬프팅을 사용하여 캐릭터 유지**

| 역할 | 내용 |
| ---- | ------- |
| User ("Sherlock Holmes" Claude와 50번의 대화 턴 진행 중) | 이 신발의 주인에 대해 무엇을 추론하나요? <br/>\<image>muddy_shoe.jpg\</image> |
| Assistant (미리 채우기) | [Sherlock Holmes] |
| Assistant (Claude의 응답) | 아, 여기 무엇이 있는가? 매우 흥미로운 표본이로군, 왓슨! 예리한 눈으로 이 신발을 살펴보세! 보시게, 이 진흙은 흔한 런던 점토가 아니라, 햄스테드 히스 주변에서만 발견되는 철분이 풍부한 토양의 독특한 붉은 색조를 띠고 있네. 그러나 밑창의 마모는 히스의 완만한 경사보다는 화이트채플의 자갈길에 더 적합한 걸음걸이를 시사하고 있네.<br/><br/>이제 신발끈을 관찰하게! 의료 전문가들이 선호하는 특정한 매듭으로 묶여 있네—수년 동안 수술 가운을 묶으면서 습득한 습관임이 분명하네. 그러나 토박스의 긁힌 자국은 다른 이야기를 들려주고 있네: 무술, 특히 바리츠에서 사용할 수 있는 차기 동작과 일치하네.<br/><br/>따라서 우리는 다음과 같이 결론 내릴 수 있네, 이 신발 주인은 화이트채플에 거주하지만 햄스테드 히스를 자주 방문하는 의사, 아마도 외과의일 것이네—아마도 엿보는 눈을 피해 비밀 바리츠 수련을 위해서일 것이네. 매우 흥미로운 사건이로군, 왓슨! 서둘러야 하네; 게임이 시작되었네! |

---

## 관련 자료

- **메시지 작업하기**: 미리 채우기와 기타 Messages API 패턴의 더 많은 예제를 확인하세요. [바로가기](/docs/en/build-with-claude/working-with-messages)
- **프롬프트 라이브러리**: 다양한 작업과 사용 사례를 위한 엄선된 프롬프트 모음에서 영감을 얻으세요. [바로가기](/docs/en/resources/prompt-library/library)
- **GitHub 프롬프팅 튜토리얼**: 문서에서 다루는 프롬프트 엔지니어링 개념을 다루는 예제가 풍부한 튜토리얼입니다. [바로가기](https://github.com/anthropics/prompt-eng-interactive-tutorial)
- **Google Sheets 프롬프팅 튜토리얼**: 대화형 스프레드시트를 통한 가벼운 버전의 프롬프트 엔지니어링 튜토리얼입니다. [바로가기](https://docs.google.com/spreadsheets/d/19jzLgRruG9kjUQNKtCg1ZjdD6l6weA6qRXG5zLIAhC8)

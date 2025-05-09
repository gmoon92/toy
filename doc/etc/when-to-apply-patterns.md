# 디자인 패턴은 자연스러운 추상화의 설계 결과다. — 리팩토링 과정에서 드러나는 패턴의 본질

## 1. 디자인 패턴은 과연 의식적으로 적용해야 하는 것일까?

디자인 패턴은 과연 의식적으로 적용해야 하는 것일까?

내가 이 질문을 처음 떠올린 건 최근 코드 리뷰 중에서였다. 동료가 "팩토리 메서드 패턴을 적용해봤다"고 말하며 코드 리팩토링을 보여줬을 때, 나는 그 코드가 정말 팩토리 메서드 패턴이라고 부를 수 있는지 의문이 들었다.

그 당시 내 머릿속에서 떠오른 생각은 단순했다. "이건 팩토리 메서드 패턴이라기보다는 심플 팩토리에 가까운데?" 그렇다면, 왜 이런 구조가 팩토리 메서드로 불리는 걸까?
디자인 패턴이라는 용어의 의미를 다시 한 번 되새기게 되었고, 그 과정에서 내 OOP 설계에 대한 철학을 다시 한 번 돌아보게 됐다.

이 글에서는 내가 생각하는 **디자인 패턴의 본질**과, **리팩토링 과정에서 자연스럽게 드러나는 추상화**에 대해 이야기해보려 한다.

## 2. 리뷰 상황 요약 - '팩토리'라는 이름에 가려진 구조

우리는 종종 디자인 패턴을 명확하게 정의하려고 한다.

그러나 이 때 우리가 놓치는 것이 있다. **"디자인 패턴이 반드시 모든 상황에서 적용되어야 하는 정형화된 규칙은 아니다."**

오늘 코드 리뷰에서 본 코드는 대략 이런 구조였다.

```java
@Component
@RequiredArgsConstructor
public class PaymentInfoFactory {
    private final RuntimeProperty runtimeProperty;

    public Optional<PaymentInfo> create(PaymentId id, PaymentType type) {
        return switch (type) {
            case CARD -> Optional.of(new CardInfoPayment(id));
            case BANK -> {
                var apiKey = runtimeProperty.getBankApiKey();
                yield Optional.of(new BankInfoPayment(id, apiKey));
            }
            default -> Optional.empty();
        };
    }
}
```

나는 이 코드를 보고 **팩토리 메서드 패턴**이라기보다는 **심플 팩토리**에 가까운 유틸리티 클래스로 판단했다.
실제로, 이 코드는 **타입에 따른 객체 생성**을 처리하는 유틸리티에 가깝고, **다형성**이 제대로 구현되지 않았다고 생각했다.

`PaymentInfo`이라는 추상화 클래스는 **단순히 공통된 상태**만 추출하고 있었고, 실질적인 **행위** 메서드는 존재하지 않았다.

```java
abstract class PaymentInfo {
    private PaymentId id;
}
```

이처럼 행위가 없는 추상화 구조는, 실제로 다형성의 장점을 살릴 수 없는 단순 상태 분리일 뿐이다.

따라서 이 구조는 **"팩토리 메서드 패턴"이 아니라**,  
단순히 타입 분기만 수행하는 인스턴스 생성 유틸리티(심플 팩토리 혹은 정적 팩토리 메서드)의 형태에 더 가깝다.

#### 2.1. 실제로, 내가 이 코드에서 짚고 싶은 피드백은 두 가지다.

첫째, 이 구조는 팩토리 메서드 패턴이 아니라,
단순히 타입 분기만 수행하는 정적 팩토리 메서드(심플 팩토리, 객체 생성 유틸리티)에 가깝다는 점이다.
이는 코드 리뷰나 팀 협업 과정에서 '디자인 패턴'이라는 용어를 너무 쉽게 붙이는 관행에서 비롯된 전형적인 혼동 사례다.

둘째, 팩토리 메서드에서 `Optional`로 결과를 감싸서 반환하는 것도 실무적으로 생각할 부분이 많이 있다.

`Optional`을 반환하는 것은 "객체가 없을 수 있음"을 시그널하는 것이지만,
팩토리에서 반환하는 객체는 일정한 "행위"와 "다형성"을 기대하는 경우가 더 많기 때문에, `Optional` 대신 안전한 기본 구현체(Null Object/Empty Object 패턴 등)를 반환하는 것이 OOP 관점에서 더 합리적일 수 있다.

이 두 포인트를 각각 좀 더 깊이 들여다보자.

## 3. 팩토리에서 Optional 반환, 정말 옳은가?

우선 "팩토리에서 `Optional`을 반환하는 설계"가 정말 바람직한지, 그리고 그 대안은 무엇인지 자세히 살펴보자.

> [!중요] 이 섹션에서 말하는 '팩토리'는 GoF가 정의한 '팩토리 메서드 디자인 패턴'을 의미하는 것이 아니다. <br/>단순 객체 생성 유틸리티, 즉 심플 팩토리일 뿐임을 분명히 한다. <br/>4섹션에서 본질적 차이를 구체적으로 설명한다.

### 3.1. Optional 반환의 치명적 문제

"팩토리"에서 종종 `Optional`이나 `null`을 반환하는 사례가 많다.

하지만, **정말 다형성을 살리려는 설계(GoF의 팩토리 메서드 패턴 등)** 관점에서는 `Optional` 반환이 몇 가지 실질적인 단점(그리고 위험)으로 이어질 수 있다.

- **디자인 패턴에서 클라이언트가 다형적 행위에 바로 의존하게 하려면,** 객체 생성 팩토리(특히 패턴 기반)는 항상 유효한 구현체를 반환하여 바로 메서드를 사용할 수 있게 하는 구조가 이상적이다.
- `Optional`로 감싸 반환하면, 클라이언트가 곧바로 메서드를 사용할 수 없고, 항상 `Optional`을 해제(unwrap)하거나, `isPresent`/`ifPresent`/`orElse`와 같은 방어로직을 추가해야 한다.
- 더욱이, `instanceof` 분기 혹은 업캐스팅 등 다양한 구현체 활용 분기가 필요할 때 유지보수성을 크게 저해한다.

### 3.2. Null Object Pattern 설계의 실용적 가치

다음 문제는 `Null Object Pattern` 을 통해 해결할 수 있다.

- `null`이나 `Optional` 반환 대신, 빈 객체/Noop 객체/Empty 객체(Null Object Pattern) 를 매번 반환한다.
- 이렇게 하면, 어떤 상황에서도 항상 공통 인터페이스(추상 타입)만 신뢰하면 되고 호출부에서 불필요한 체크 없이 바로 행위를 실행할 수 있다.
- null/empty 객체의 기본 동작(contract)은 "아무 일도 하지 않음" 또는 "기본/보통 동작"으로 설계한다.

```java
// 이 코드는 단순 객체 생성 분기(심플 팩토리/정적 팩토리) 예시로, 
// '팩토리 메서드 패턴'의 본질(추상 메서드, 구현체 오버라이딩에 의한 다형성 위임 등)은 아님
public PaymentInfo create(PaymentId id, PaymentType type) {
    return switch (type) {
        case CARD -> new CardInfoPayment(id);
        case BANK -> {
            var apiKey = runtimeProperty.getBankApiKey();
            yield new BankInfoPayment(id, apiKey);
        }
        
        // Null Object Pattern
        default -> new EmptyPaymentInfo();
    };
}
```

이처럼, `Null Object`는 객체를 무조건 반환하게 함으로써 생성 시점부터 일관된 인터페이스 사용을 가능하게 만든다.

`Null Object Pattern`은 "없는 값"을 일종의 "행동 가능한 값"으로 승격시켜, 클라이언트 코드에서의 조건 분기와 예외 케이스 대응을 평탄화시킨다. 그 결과, 클린한 인터페이스 사용과 더불어, 런타임 오류 가능성도 줄일 수 있다.

> Effective Java - Item 55. 옵셔널 반환은 신중히 하라: "Optional은 값이 '없을 수도 있다'는 명확한 의도에서만 반환하라."<br/>
> [Null Object Pattern](https://en.wikipedia.org/wiki/Null_object_pattern): "클라이언트 코드의 방어코드 최소화, [graceful degradation](https://developer.mozilla.org/en-US/docs/Glossary/Graceful_degradation)([내결함성](https://en.wikipedia.org/wiki/Fault_tolerance)) 유리"

이제 `Optional`과 `Null Object Pattern`을 정리했으니, 애초에 언급했던 또 다른 중요한 질문으로 돌아가보자.

"디자인 패턴"이라는 용어가 실무에서 얼마나 자주, 그리고 자칫 모호하게 쓰이는지를 살펴보며, 실제 GoF 패턴과 어떤 괴리가 있는지도 본격적으로 짚어보자.

## 4. "팩토리"라는 이름만으로는 GoF 디자인 패턴이 아니다

앞서 "'디자인 패턴'이라는 용어를 너무 쉽게 붙이는 관행에서 비롯된 전형적인 혼동 사례다"라고 언급했다.
일반적으로 '디자인 패턴'이라고 하면 GoF 디자인 패턴을 떠올리는 경우가 많다.

이러한 혼동은 GoF 디자인 패턴이 지닌 다형성과 위임 중심의 설계 철학을 제대로 이해하지 못한 데서 비롯된다.
이번 섹션에서는 자주 혼동되는 심플 팩토리와 팩토리 메서드 패턴의 본질적인 차이에 집중해 살펴본다.

## 심플 팩토리 메서드는 GoF 디자인 패턴이 아니다.

아래는 앞서 문제로 삼았던 코드의 예시이다.

```java
// 이 구조는 단순 분기 분기만 담당하며, 객체 생성의 다형성 위임 구조를 갖추지 않는다.
public PaymentInfo create(PaymentId id, PaymentType type) {
    return switch (type) {
        case CARD -> new CardInfoPayment(id);
        case BANK -> {
            var apiKey = runtimeProperty.getBankApiKey();
            yield new BankInfoPayment(id, apiKey);
        }
        default -> new EmptyPaymentInfo();
    };
}
```

이 코드는 디자인 패턴으로 볼 수 없는 이유는 명확하다.

GoF의 디자인 패턴은 목적에 따라 행위/생성/구조로 나뉜다. 이 코드는 구현체가 여러 개인 상황에서 객체 생성의 유연함을 확보하려는, 즉 팩토리 메서드 패턴의 적용을 시도한 예라 할 수 있다.

하지만 팩토리 메서드/추상 팩토리 패턴의 본질은 객체 생성의 책임을 상위 타입(추상 클래스/인터페이스)에 두고, 실제 객체 생성을 하위 구현체가 **다형적으로** 처리하는 데 있다. 반면, 이 코드에서는 단순하게 타입 분기를 통해 구현체를 선택하고 생성할 뿐, 다형성이나 동적 바인딩이 전혀 활용되지 않는다.

즉, 단순 분기 기반의 심플 팩토리는 리턴 타입만 추상화할 뿐, GoF가 정의한 생성 디자인 패턴이 요구하는 다형성 위임 구조와는 본질적으로 다르다.

### 4.1. 팩토리 메서드 패턴 (Factory Method Pattern)

팩토리 메서드 패턴은

상위 클래스가 객체 생성의 책임(팩토리 메서드)을 선언만 하고, 실제 어떤 객체를 생성할지의 로직은 하위 클래스에서 결정하도록 다형성으로 위임하는 구조다.

즉, 클라이언트(사용자)는 상위 추상 타입에만 의존하며, 실제 어떤 구현체가 생성될지는 실행 시점(런타임)에 결정된다.

현재 구조에서 팩토리 메서드 패턴을 구조화 한다면 다음과 같다.

```java
abstract class PaymentInfo {
    protected PaymentId id;

    public PaymentInfo(PaymentId id) {
        this.id = id;
    }

    // 펙토리 메서드 배치. 객체 생성 책임을 하위 클래스에 위임. (다형성 핵심)
    public abstract PaymentProcessor createProcessor();

    // 공통된 행위 process(): 다형성, 템플릿 메소드 패턴 효과도 있음
    public void process() {
        PaymentProcessor processor = createProcessor();
        processor.process(id);
    }
}

public class CardPaymentInfo extends PaymentInfo {
    public CardPaymentInfo(PaymentId id) {
        super(id);
    }
    
    // 구현 클래스는 생성에만 집중. 
    @Override
    public PaymentProcessor createProcessor() {
        return new CardPaymentProcessor();
    }
}
```

- 상위 클래스가 객체 생성 책임(팩토리 메서드)를 선언.
- 하위 클래스가 실제 생성 로직을 오버라이딩(다형성 핵심).
- 클라이언트는 오직 상위 타입만 알면 되고, 실제 생성되는 구현체는 하위 클래스에서 결정.

### 꼭 디자인 패턴을 해야만 했나?

그런데 중요한 점도 짚고 넘어가야 한다.

위의 구조는 다형성 기반의 생성 책임 위임과, (아래처럼 확장하면) 다형적 행위 오버라이딩까지 모두 구현할 수 있지만, 실제로는 그 정도의 설계가 '과연 필요한가?'라는 본질적 질문도 던져보아야 한다.

```java
abstract class PaymentInfo {
    protected PaymentId id;
}
```

하지만 단순히 상태(데이터)만 존재하는, 행위와 로직 구분이 없는 DTO에 가까운 구조라면 굳이 이런 복잡한 패턴을 도입할 필요가 있을까?

실제로는 아래처럼 호출하는 지점에서 단순 분기를 두는 편이 더 읽기 쉽고, 협업하는 동료에게도 명확하다.

```java
@Service
public class PaymentService {
    
    // ...
    
    public void charge(PaymentId id, PaymentType type) {
        var info = obtainPaymentInfo(id, type);
        
        if (PaymentType.CARD == type) {
            // 카드결제 처리
        } else if (PaymentType.BANK == type) {
            // 계좌이체 처리
        }
    }

    // 굳이 패턴을 도입하지 않고 호출부에서 분기
    public PaymentInfo obtainPaymentInfo(PaymentId id, PaymentType type) {
        return switch (type) {
            case CARD -> new CardInfoPayment(id);
            case BANK -> {
                var apiKey = runtimeProperty.getBankApiKey();
                yield new BankInfoPayment(id, apiKey);
            }
            default -> new EmptyPaymentInfo();
        };
    }
}
```

데이터 구조의 확장성, 행위 다양성, 실제 설계의 필요성에 따라 패턴의 역할과 적용 여부를 판단하는 것이 좋은 설계자의 진짜 역량이라 생각한다.

결국, 복잡한 분기나 확장성이 요구되는 경우라면 패턴 도입이 유효하지만, 단순 데이터 운반 등 로직 다양성이 없는 상황에선 과도한 패턴보다 간결함과 명확함이 더 중요하다.

| 패턴 도입이 유리한 상황                                   | 패턴이 과한/불필요한 상황                 |
|-------------------------------------------------|--------------------------------|
| 각 행위(알고리즘, 정책, 전략, 로직)가 자주 바뀌거나, 여러 구현체가 필요한 경우 | 데이터 구조(필드/속성)만 다르고 행위 분기 없음    |
| 외부 연동, 조건, 정책 등 하위 구현마다 처리 로직이 완전히 달라야 할 때      | 단순히 속성값만 달라짐(예: 값 파싱, 포장 등)    |
| 신규 요구사항(유형/전략/정책/구현체 등) 추가가 계속 예상되는 경우          | 복잡한 추상화보다 명확한 분기/if문이 더 직관적일 때 |
| 공통 프로세스(동작)는 있지만 타입/전략마다 서브로직이 전혀 다를 때          | 가독성, 팀 생산성, 코드 단순성이 더 중요한 상황   |
| 테스트, Mock, 동적 전략 교체 등 변화대응/품질 관리 전략이 중요한 환경     | 시스템 규모·변화 가능성이 작거나, 단기 프로젝트일 때 |

- 실제 현장에서는 데이터 포장, 단순 저장 등만 필요하다면 굳이 패턴이 필요 없다.
- 복잡한 분기, 다양한 신규 정책/전략 추가 가능성이 크면 패턴을 도입해라.
- 팀 규모, 개발자 경험, 유지보수 기간 등도 꼭 고려하라.
- "패턴이 정답"이라는 선입견보단, 요구와 복잡도의 균형이 더 중요하다.

예를 들어, 결제 방식별로 결제 인증, 각기 특수한 로직이 분리되어 있을 때는
패턴을 적용하면 각 결제 방식별 비즈니스 로직을 깔끔하게 확장/구현할 수 있다.

```java
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final Map<PaymentType, PaymentMethod> methods;
    
    public void charge(PaymentId id, PaymentType type) {
        var info = obtainPaymentInfo(id, type);
        
        PaymentMethod method = methods.get(type);
        method.charge(id);
    }
}

interface PaymentMethod {
    void charge(PaymentId id);
}

public class CardPaymentMethod implements PaymentMethod {
    public void charge(PaymentId id) { 
        // 카드 결제 로직 
    }
}

public class BankPaymentMethod implements PaymentMethod {
    public void charge(PaymentId id) { 
        // 계좌 이체 로직
    }
}
```

## 5. 디자인 패턴은 도입하는 것이 아니라 도출되는 것이다

이처럼 디자인 패턴을 **의식적으로 적용**한다고 생각하는 순간, 그 패턴은 사실 **단지 구현상의 도구**일 뿐이다.

중요한 것은 **어떤 문제를 해결하려고 했는지**, **그 문제를 해결하기 위해 어떤 방식으로 추상화를 했는지**이다. 패턴이 필요하다고 느껴지기 전에, **설계 자체가 자연스럽게 패턴을 도출**해내는 순간이 온다. 이때 리팩토링을 통해 점점 더 **유연한 구조**를 만들고, 그 과정에서 **디자인 패턴**이 필요해지는 것이다.

리팩토링은 **기존 코드의 구조를 개선**하면서 점진적으로 **추상화**가 진행되는 과정이다. 그 과정에서 **디자인 패턴**은 결과적으로 나타날 뿐, 처음부터 의도한 것은 아니었다.

## 6. 디자인은 설계에서 나오고, 설계는 요구에서 나온다

디자인 패턴이 **어떤 특정 문제를 해결하기 위해 도입되는 것**이라면, 그 설계의 핵심은 **어떤 요구사항을 해결하기 위한 구조적인 접근**에 있다. 패턴은 **결과적**으로 나타나며, 그 자체가 **목표**가 되어서는 안 된다.

리팩토링 과정에서 디자인 패턴이 드러나는 것은 **추상화의 자연스러운 흐름** 때문이다. 따라서, **의도적으로 패턴을 적용**하는 것보다는 요구사항을 충족시키는 유연한 설계를 통해 패턴을 **자연스럽게 도출**하는 것이 더 중요하다.

## 7. 마무리

디자인 패턴은 정답이 아니다.
그것은 설계가 스스로 드러낸 결과물일 뿐이다.

나는 항상 같이 일하는 동료를 위해 읽기 쉽고, 변경에 유연한 코드를 고민하며 리팩토링한다.
그리고 그 유연함이란 결국 OOP의 본질, **다형성을 얼마나 효과적으로 설계에 녹였느냐**로 귀결된다고 생각한다.

하지만 유연한 구조와 읽기 쉬운 코드는 때론 상충하는 가치이기도 하다. 변화하는 요구에 부드럽게 대응하려다 보면, 추상화와 다형성의 수준이 높아지고 그만큼 코드의 직관성은 희생되기 쉽다.

그래서 나는 항상 이 두 축 사이에서 균형을 찾기 위해 고민한다. 즉, 요구사항이 변화할 때마다 추상화의 수준을 조절하고, 정말 필요한 곳에만 다형성을 도입하는 과정에서 우리는 어느새 자연스럽게 "디자인 패턴"이라는 구조에 도달하게 된다. 나는 디자인 패턴이 단순히 '문제를 해결하는 기술적 도구'에 머물지 않는다고 믿는다. 패턴은 OOP 설계에서 추상화와 다형성이 자연스럽게 녹아들었을 때 비로소 등장하는 산물이다.

패턴을 적용하겠다는 의식이 앞서면, 설계는 어느새 기술적 수단에 종속된다. 억지로 패턴을 쓴다고 해서 더 나은 설계가 완성되는 것도 아니다. 나는 리팩토링의 과정을 통해 추상화의 수준을 점진적으로 높여가고, 그 과정에서 **자연스럽게 발생한 패턴**에 더 큰 가치를 둔다. 기능을 추가하거나 개선하기 위해 설계를 하나씩 다듬다 보면, 복잡했던 코드 속에서도 추상화의 책임이 분리되고, 다형성이 적용된다. 그때 비로소 코드의 유연성, 확장성, 유지보수성이 살아난다.

**이것이 바로 내가 생각하는 OOP의 본질이다.**

코드가 복잡하고 구체적일수록, 점차적으로 그 코드 속에 있는 추상화의 책임을 분리하고, 다형성을 적용하면서 구현을 구체화하게 된다. 이를 통해 코드의 유연성, 확장성, 유지보수성을 높여나갈 수 있다.

결국 디자인 패턴은 이 모든 과정에서 자연스럽게 드러나는 추상화의 결과일 뿐이다.

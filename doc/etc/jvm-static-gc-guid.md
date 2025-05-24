# 자바 메모리 구조로 본 static, 리터럴, 오토박싱 — GC 대상은 누구인가?

## 들어가기 전

자바 개발자라면 한 번쯤은 `static` 변수를 둘러싼 애매한 고민에 부딪힌다.

- `static` 변수는 힙에 올라가는 걸까?
- `static final`로 선언한 상수는 GC 대상이 되는 걸까?
- 톰캣에서 `static` 변수를 잘못 쓰면 메모리 누수가 생긴다던데, 진짜야?

개인적으로는 이와 같은 의문을 실제 프로젝트에서 경험한 적이 있다.

특히 `매직 상수`를 클래스 변수(`static final`)로 정의하는 과정에서 이 질문이 생겼다.
실제로 여러 소프트웨어 공학서(클린 코드, Effective Java 등)에서도 매직 상수의 유지보수 어려움을 해결하기 위해 클래스 변수의 사용을 권장한다.

정적으로 선언하면 안전할 것 같았지만, 이 값들이 실제로 GC 대상이 되는지, 그리고 서버 환경에서 의도하지 않게 남아 메모리를 점유하게 되지는 않을지 확신이 서지 않았다. 이런 고민 끝에, JVM 메모리 구조,
GC 대상 조건, 그리고 클래스 로더의 수명까지 파고들게 되었다.

이는 단순한 이론 정리가 아니라, **실전에서 "`static`을 어디까지 허용할 수 있을지" 기준을 세우기 위한 목적이었다.**

이 문서에서는 다음과 같은 내용을 다룬다.

- `static` 변수, 리터럴, 오토박싱 객체의 메모리 위치와 수명
- `static final`이 GC 대상이 되는 조건과 안 되는 조건
- 웹 컨테이너에서 `static` 필드가 유발하는 메모리 누수 사례
- 그리고 현업에서 적용할 수 있는 static 변수의 설계 가이드라인

---

## JVM 메모리 구조

자바 프로그램이 실행될 때, JVM(자바 가상머신)은 주요 데이터(변수, 상수 등)를 각각 다른 영역(메서드 영역, 힙, 스택)에 나눠서 관리한다.

### 코드로 이해하는 JVM 메모리 구조

아래 샘플 코드를 보며 각각의 값과 참조가 어디에 저장되는지 하나씩 살펴보자.

```java
public class Sample {
    private static final int DEFAULT_COUNT = 0;   // [1] static 변수 (메서드 영역)

    private int value = 10;                       // [2] 인스턴스 변수 (힙)
    private String label = "sample";              // [3] 인스턴스 변수 (힙, "sample" 값은 상수 풀에 저장)

    public void execute() {
        int counter = 5;                         // [4] 기본형 지역 변수 (스택)
        Data data = new Data();                  // [5] 지역 변수(참조, 스택), 객체는 힙에 생성

        String keyword = "hello";                // [6] 지역 변수(참조, 스택), "hello"는 상수 풀
        String custom = new String("hello");     // [7] 지역 변수(참조, 스택), 객체는 힙, "hello"는 상수 풀

        int result = data.getData();             // [8] 지역 변수 (스택)
    }
}

class Data {
    private int data = 123;                      // [9] 인스턴스 변수 (힙)

    public int getData() {
        return data;
    }
}
```

#### 메서드 영역(Metaspace)

클래스 로딩 시 딱 한 번 생성, 모든 인스턴스가 공유

- static 변수
    - `DEFAULT_COUNT`([1])
    - `메서드 영역`(=Metaspace)에 저장, 클래스가 메모리에 올라갈 때 단 한 번만 생성되고, 그 클래스의 모든 인스턴스에서 값을 공유함.
- 런타임 상수 풀
    - `"sample"`([3]), `"hello"`([6], [7])
    - 한 번만 `런타임 상수 풀(메서드 영역 일부)`에 저장됨. (같은 리터럴 값이 여러 번 등장해도 반복 생성 X)
    - **주의!** int, double, boolean 등 기본형 리터럴(`5`, `3.14`, `true` 등)은 상수 풀에 저장되지 않음. (기본형 값은 실제 변수에 바로 복사됨)

> "클래스가 메모리에 올라간다(클래스 로딩, Class Loading)"란?<br/>
> 자바에서는 클래스(.class)가 처음 사용될 때 JVM이 클래스 정보, static 변수, 런타임 상수 풀을 **메서드 영역(=Metaspace)**에 적재하는 것(힙 X).<br/>
> JDK7까지는 PermGen, JDK8부터는 `Metaspace`가 `MethodArea`로 사용.

#### 힙(Heap, GC 대상)

`new` 연산자 또는 클래스 인스턴스 생성시 힙에 저장

- 인스턴스 변수
    - `value`([2]), `label`([3]), `data`([9])
    - 객체가 생성될 때 마다 힙에 저장
    - 예를 들어 Data 클래스의 `data`([9])는 Data 객체 생성시 힙에 저장
- `new` 연산자로 생성된 객체
    - `new Data()`([5]): 힙에 생성
    - `new String("hello")`([7]): 힙에 별도 객체 생성, `"hello"`는 이미 상수 풀에 있음 `new String` 은 별개의 힙(내용 같아도 리터럴과 별개)

#### 스택(Stack)

실제 데이터(객체/문자열)는 힙 또는 런타임 상수 풀에 저장되고, 스택에는 그 '주소(참조값)'만 저장

- 지역 변수 (메서드 실행 동안에만 존재)
    - `counter`([4]): 기본형 지역 변수(스택), 5: int형 리터럴 (직접 값, 상수 풀에는 저장 X)
    - `data`([5]): Data 객체에 대한 참조 변수 (객체는 힙, 변수 자체는 스택)
    - `keyword`([6]): "hello" String 리터럴(소스 코드 상 변하지 않는 값)에 대한 참조 변수
    - `custom`([7]): `new` 로 생성한 String 객체의 참조 변수
    - `result`([8]): int
- 참조 변수: 스택에는 객체 자체 X, 참조(주소값)만 저장됨
    - 이들 참조 변수(`data`, `keyword`, `custom`)는 스택에 주소(참조)값만 저장
    - new Data(), "hello", new String("hello"와 같은 실제 객체는 각각 힙 또는 런타임 상수 풀에 저장

각 영역은 서로 역할이 다르므로, 변수와 객체가 어디에 저장되는지 이해하는 것이 중요하다.

### JVM 메모리 전체 구조

자바(JVM)는 주요 메모리 영역을 다음과 같이 분리해 관리한다.

```text
┌───────────┐
│  메서드 영역 │  static 변수, 클래스 정보, 런타임 상수 풀("hello", "sample")
└─────┬─────┘
      │
┌─────▼─────┐
│   힙 영역  │  인스턴스 변수(value, label), new Data(), new String("hello")
└───┬───┬───┘
    │   │
┌───▼───▼───┐
│  스택 영역  │  지역 변수(counter, data, keyword, custom, result) → 모두 메서드 실행 중 일시적
└───────────┘
```

| 영역                                       | 특징/내용                                           | 참고                                 |
|------------------------------------------|-------------------------------------------------|------------------------------------|
| 힙(Heap)                                  | new 연산자로 생성된 객체와 배열이 저장되며, GC의 주요 대상이 된다.       | GC 대상 (참조 해제 시)                    |
| 스택(Stack)                                | 각 스레드별 메서드 호출 정보, 지역 변수, 기본형 변수 저장를 저장한다.       | 메서드 호출 끝나면 소멸한다.                   |
| 메서드 영역(Method Area) or 메타스페이스(Metaspace) | 클래스 정보, static 변수, static 메서드, 런타임 상수 풀 등 저장    | JDK7까지는 PermGen, JDK8부터는 Metaspace |
| 런타임 상수 풀(Runtime Constant Pool)          | 클래스별 상수(문자열, static final 등), String 리터럴을 저장한다. | 메서드영역/메타스페이스 내부                    |

---

## 리터럴 vs 객체: 저장 영역과 GC

JVM이 값을 메모리에 저장하는 방식은 "리터럴"과 "객체(new로 생성)"에 따라 다르다.

앞서 설명한 코드 예제를 확장해서 각 변수/객체가 리터럴인지, new 객체인지, GC 대상인지 하나씩 분석해 본다.

```java
public class Sample {
    private static final int DEFAULT_COUNT = 0;   // [1] static 변수 (기본형 리터널)

    private int value = 10;                       // [2] 인스턴스 변수 (기본형 리터널)
    private String label = "sample";              // [3] 인스턴스 변수 (문자열 리터널)

    public void execute() {
        int counter = 5;                         // [4] 기본형 지역 변수 (기본형 리터널)
        Data data = new Data();                  // [5] 지역 변수(new 연산자로 생성된 객체)

        String keyword = "hello";                // [6] 지역 변수(문자열 리터널)
        String custom = new String("hello");     // [7] 지역 변수(문자열 리터널, new 연산자로 생성된 객체)

        int result = data.getData();             // [8] 지역 변수 (기본형 리터널)
    }
}

class Data {
    private int data = 123;                      // [9] 인스턴스 변수 (기본형 리터널)

    public int getData() {
        return data;
    }
}
```

아래와 같이 [번호]로 필드를 구분할 수 있다.

| 구분      | 변수(번호)              | 설명/특징             |
|---------|---------------------|-------------------|
| 기본형 리터럴 | [1] [2] [4] [8] [9] | 변수 직접 값, GC 대상 아님 |
| 문자열 리터럴 | [3] [6] [7](값)      | 상수 풀, GC 대상 아님    |
| new 객체  | [5] [7](객체)         | 힙, 참조 끊기면 GC 대상   |

### 객체의 참조와 GC: new를 알아야 메모리 관리가 보인다.

자바에서 인스턴스 변수의 메모리 저장 위치는

단순히 변수가 "인스턴스 변수이니까 힙"인 게 아니라,
반드시 new 연산자로 객체(인스턴스)를 생성했을 때 해당 객체의 모든 인스턴스 변수(필드)가 힙(Heap) 메모리에 배치된다.

이때 힙 메모리에서 관리하는 객체 데이터는 프로그램 내 변수/필드/스택 등에서 "참조값(레퍼런스, 주소)"을 통해 접근한다.
즉, 객체를 생성하지 않은 상태에서는 인스턴스 변수(필드) 자체가 메모리에 존재하지 않으며, 아무런 영역에도 할당되지 않는다.

> 참조값 자체는 내부 구현상 주소일 수도, JVM의 handle일 수도 있다. 개발자 입장에서는 "그 객체를 가리키는 어떤 값"으로 생각하면 된다.

객체가 소멸(참조값이 완전히 끊어진 순간)되면 `GC(Garbage Collector)`는 해당 객체를 "더 이상 어떤 변수/필드/스택/스레드에서도 참조하지 않는(도달할 수 없는) 힙 객체(garbage)" 것으로 간주해 힙 메모리에서 해제한다.

> **Tip:** GC는 기본적으로 참조가 모두 끊긴 객체만 수집하지만, <br/>
> Old 영역 포화, Stop-the-World, 메모리 부족 등 특수 상황에서는 참조가 남아있는 객체까지 예외적으로 회수(GC sweep/compaction)될 수 있다.<br/>
> 운영 중 데이터 유실, 예기치 못한 장애로도 이어질 수 있으니, GC 동작을 절대 단순화해서 판단하지 말자.

**`"기본형 또는 문자열 리터럴"`은 항상 GC 대상이 아니라고 오해해서는 안 된다.**
예를 들어, 객체의 인스턴스 변수로 리터럴(예: `int x=10`, `String n="hi"`)을 넣는 순간 해당 값도 힙의 객체 공간에 별도로 저장된다.

`"지역 변수"`는 메서드 호출 시 스택에만 저장되고, 메서드 종료와 함께 자동으로 소멸하기 때문에 GC와 무관하다.

**반면, `static 변수`라면 상황이 조금 다르다.**
`static` 변수(클래스 변수)는 클래스가 처음 메모리에 로드될 때 메서드 영역(Metaspace)에 단 한 번만 만들고, 리터럴 값이라면 상수 풀에 저장한다.

`static` 변수가 new 객체를 참조할 경우, 객체는 힙에 자리잡지만, `static`이 그 객체를 참조하는 동안에는 GC의 대상이 아니다.
**이 때문에 static 변수는 `new`와 무관하게 프로그램 내내 메모리를 상시 점유해 메모리 누수가 발생할 수 있다.**

#### new 객체만 참조가 모두 끊어졌을 때만 GC. static, 리터럴, 지역 변수는 모두 그 자체로 GC 대상이 아니다.

결국 기본형 리터럴은 변수의 종류(인스턴스, 클래스, 지역)에 따라 각각 힙, 메서드 영역, 스택 등에서 실제 값으로 저장되고, 문자열 리터럴은 코드에 등장하는 순간 런타임 상수 풀에 한 번만 저장되어 프로그램 종료 전까지 JVM 내에 남게 된다. 따라서, 값이 어느 영역에 저장되고 GC의 직접 대상이 되는지는 변수 선언 위치, 객체 생성 방식, 그리고 참조의 생명주기에 따라 모두 달라진다는 점을 반드시 이해해야 한다.

---

## 오토박싱 & 래퍼 클래스의 캐싱

**오토박싱이란?**

자바에서 기본형 값을 자동으로 해당 래퍼 클래스 객체(참조타입)로 변환하여 컬렉션, 다형성, 추상화 등 객체 중심의 구조에서도 별도의 명시적 변환 없이 자유롭게 사용할 수 있게 하는 기능이다.

예를 들어, `Integer i = 10;`은 내부적으로 `Integer.valueOf(10)`이 호출된다.

```java
void autoBoxing() {
    List<Integer> nums = new ArrayList<>();
    nums.add(1); // 컴파일러가 1 → Integer.valueOf(1)로 오토박싱
}
```

기본형과 참조형 사이의 명시적 변환 과정(박싱/언박싱)을 생략함으로써 코드의 가독성을 높이고 특히 컬렉션·다형성·추상화 등 객체지향 컨테이너 구조와의 통합/호환성을 보장한다.

컬렉션(List, Set 등)은 원래 참조 타입(객체)만 저장 가능하지만, 오토박싱 덕분에 `List<Integer> list = Arrays.asList(1, 2, 3);` 와 같이 `int`와 같은 기본형도 자연스럽게 객체처럼 사용할 수 있다.

> 오토박싱/언박싱은 "다형성(polymorphism)"의 활용성을 넓히고, 값 그 자체(primitive)와 객체(참조, 값 포함)를 개발자의 개입 없이 자유롭게 변환시켜 코드 추상화 및 객체 중심 프로그래밍에 큰 기여를 한다.

### 오토박싱의 내부 동작: valueOf와 캐싱

기본형 타입의 래퍼 클래스에 기본값을 대입하면 자바의 오토박싱이 동작하는데, 이때 실제로는 `Integer.valueOf()`, `Boolean.valueOf()` 같은 정적 메서드가 내부적으로 호출된다.

```java
void autoBoxing() {
    List<Integer> nums = new ArrayList<>();

    // 컴파일러가 1 -> Integer.valueOf(1)로 오토박싱
    nums.add(1);
    Integer i = 10;
}
```

아래 `Integer`의 실제 valueOf 구현 코드를 보면, 자바가 어떻게 범위 내의 값을 캐싱하는지 알 수 있다.

```java
package java.lang;

public class Integer {
    @IntrinsicCandidate
    public static Integer valueOf(int i) {
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }
}
```

이 valueOf 메서드는, 자주 사용되는 값을 `싱글톤(캐시/풀)` 객체를 재사용하여 메모리 낭비를 줄이고, 불필요한 객체 생성을 최소화한다.

### 래퍼 클래스 캐시 규칙

이를 래퍼 클래스 캐시라 하는데, 기본형 래퍼 클래스들은 인스턴스가 생성되더라도 위 방식으로 메모리를 효율적으로 관리하고 있다. 따라서 메모리 관리에 있어 래퍼 클래스 캐시 규칙을 보면 좋다.

- Boolean
    - Boolean.valueOf(true) → Boolean.TRUE
    - Boolean.valueOf(false) → Boolean.FALSE
- Integer
    - Integer.valueOf(-128 ~ 127) → 캐시된 싱글톤 객체 반환
    - 이 범위를 벗어나면(128 이상 또는 -129 이하) 매번 새 객체 생성.
- Character
    - Character.valueOf(0~127) → 캐시
- Double, Float
    - 별도의 캐시 없음. 무조건 새 객체 생성.
- new 키워드로 생성
    - 항상 힙에 새 객체가 생성된다.
    - 캐시와 무관하며, 참조가 끊기면 GC 대상이 된다.

> JVM 옵션으로 예외적으로 범위 확장 가능: `-Djava.lang.Integer.IntegerCache.high`=xxx

### 캐싱이란 동일성을 보장한다.

오토박싱 캐싱 범위 내 객체는 싱글톤/풀로 관리되어, 객체의 동일성(identity, 참조)이 보장된다.

```java
void autoBoxing() {
    Integer a = 10;
    Integer b = Integer.valueOf(10);
    int c = 10;

    Assertions.assertThat(a == b).isTrue();
    Assertions.assertThat(b == c).isTrue();
}
```

반면, `new` 연산자 또는 오토박싱 캐시 범위 밖의 값으로 생성한 객체는 항상 새로운 인스턴스가 할당되므로, 동일성(`==` 비교)을 보장하지 않는다.

값(동등성, equality)의 비교는 항상 **equals() 메서드를 사용해야 한다.** 특히 컬렉션 자료구조에서 객체 동등성 판별은 기본적으로 `equals()` 메서드에 의존한다.

그리고 **HashSet, HashMap 등 해시 기반 컬렉션에서는 `equals()`와 함께 `hashCode()`도 일치해야 하므로 두 메서드 모두 오버라이딩해야 한다.**

자주 쓰는 값(-128~127, true/false, 작은 문자 범위)만 캐시 범위에 포함되어 효율적으로 힙 객체 생성을 줄일 수 있지만, 오토박싱 캐싱 범위를 벗어난 값을 반복적으로 생성하면 불필요하게 힙 객체가 쌓여 힙 사용량이 낭비될 수 있다.

> 잘못된 `==` 비교나 캐싱 규칙 미숙지로 인한 힙 낭비가 실무에서 흔한 버그와 성능 저하의 원인이다.<br/>
> 동일성은 오토박싱 캐시가, 동등성은 equals()/hashCode()가 책임지는 포인트임을 반드시 기억하자.

---

## static 변수와 메서드 영역, 생명주기

static 변수(클래스 변수)는 **클래스 로드 시 메서드 영역(메타스페이스)에 저장한다.**

- 클래스 언로드 전까지 유지됨
- GC 대상이 아님 (**클래스 언로드 시 해제**)

이러한 원리 때문에, static 변수는 코드에서 더 이상 참조가 없어도 클래스 자체(정확히는 `ClassLoader`)가 살아 있는 한 프로세스 내에서 계속 메모리를 점유해야 한다. 결국 운영 환경에서는 **예기치
않은 메모리 누수와 서버 장애를 유발할 수 있다.**

예를 들어, 다음과 같이 static 변수로 리소스를 관리하면 GC만으로는 해당 객체를 절대 해체할 수 없다.

```java
public class Sample {
    private static final ExternalResource connection = ExternalResource.connection();
    private static final List<Data> list = new ArrayList<>();
}
```

실제 WAS(특히 톰캣) 운영 환경에서는 배포, 리로드, 재시작이 반복되어도 `static` 변수나 `static`이 참조하는 객체가 GC만으로는 완전히 해제되지 않아 메모리 누수가 쌓이는 경우가 많다.

그 이유는 **각 웹앱이 별도의 ClassLoader(WebAppClassLoader)로 로딩되기 때문**이다. 이 ClassLoader가 살아 있는 한, 클래스와 해당 static 변수(및 객체)도 계속 메모리에
남아 있게 된다.

결국, GC만으로는 static 변수의 메모리 회수가 불가능하므로 정상적인 클래스 언로드(즉, `ClassLoader`와 모든 참조의 완전 해제)가 이루어져야만 메모리가 반환된다.

> 톰캣/WebAppClassLoader의 특성상, 설계/운영에서 static 메모리 점유가 누적되면 의도치 않은 메모리 누수, 서버 장애, OOM 등 심각한 운영 이슈로 이어질 수 있음을 반드시 고려해야 한다.

### 웹앱 환경에서 static 변수와 클래스 언로드

톰캣 같은 WAS 환경에서는 각 웹앱이 독립적인 ClassLoader(WebAppClassLoader)로 로딩된다.

클래스 언로드가 일어나기 위한 조건은 다음과 같다.

- 해당 클래스의 모든 인스턴스가 참조 해제되어 있어야 하고
- 해당 클래스 자체가 참조되지 않아야 하며
- 클래스가 로드된 `ClassLoader`도 GC 대상이어야 한다.

따라서 단순히 GC 트리거만 믿고 static 변수에 대형 객체를 보관하면,  
클래스가 언로드되거나 서버가 정상 종료될 때까지 해당 리소스는 절대 해제되지 않는다.

실제 WAS 운영 환경에서는 복잡한 참조 구조, 실시간 트래픽, `ClassLoader`의 동작 등으로 인해 위와 같은 클래스 언로드 조건이 충족되지 않는 경우가 많다.

### static 변수 누수: 대용량 리소스 캐싱/참조 시 장애

특히 아래처럼 static 변수에 대용량 객체를 저장하면, 클래스 언로드나 서버 종료 전에는 GC로도 절대 해제되지 않아 점진적 메모리 누수가 발생한다.

```java
public class ResourceLeakDemo {
    // 클래스가 언로드되지 않는 한 절대 GC 안 됨!
    private static final List<byte[]> cache = new ArrayList<>();

    // 혹은 외부 커넥션/스레드, 싱글턴 등도 동일하게 위험
    // private static final Connection conn = DataSource.getConnection();

    public static void main(String[] args) {
        // 큰 데이터(100MB)를 반복적으로 추가하여 객체를 점유
        for (int i = 0; i < 10; i++) {
            cache.add(new byte[10 * 1024 * 1024]);
            System.out.println("Added chunk " + (i + 1));
        }
        System.out.println("cache size=" + cache.size());

        // 이후에 웹앱을 reload/deploy 하거나, 클래스만 unload 해도
        // cache와 그 안의 배열은 ClassLoader가 참조하는 한 GC 대상이 아님
    }
}
```

### static 변수 누수: 클래스 언로드/재배포 실패로 인한 GC 미수거

아래는 톰캣 환경에서 static 변수가 제대로 정리되지 않을 때 발생하는 메모리 누수 케이스다.

```java

@RestController
@RequestMapping("/static-leak")
public class SampleController {
    private static final List<Object> staticList = new ArrayList<>();

    @GetMapping
    public ResponseEntity<Void> staticLeak() {
        // 호출이 반복되면 staticList가 계속 커짐 
        // 서버 종료 전까지 메모리 해제 안 됨
        staticList.add(new Object());
        return ResponseEntity.ok().build();
    }
}
```

- `static` 리스트는 클래스가 언로드될 때까지(=서버 다운 전까지) 메모리에 쌓인 모든 Object 인스턴스를 계속 참조한다.
- WAS(톰캣/Spring Boot 내장)에서는 재배포 또는 `hot redeploy`, 서블릿 리로드를 반복해도 `WebAppClassLoader` 클래스가 언로드 되지 않는 한 `staticList`와 그 안의
  객체들은 메모리를 계속 점유한다.
- `static`이 참조하는 **객체 역시 GC 대상이 아니다.**
- 결국, `OutOfMemoryError(OOM)`, 응답 지연, 서버 장애 등 심각한 운영 이슈로 직결된다.

> 톰캣 등 WAS 환경에선 static 변수는 GC보다 "ClassLoader 언로드/프로세스 종료"에 더 의존적인 자원이므로, 설계/운영에서 항상 누수 리스크에 유의해야 한다.

### static 변수 누수: 장시간 운영, 무중단 배포 등에서 주의

이런 이유로 장시간 운영, 무중단 배포, 잦은 핫 redeploy 환경에서는 static 변수에 캐시나 대형 객체를 저장하면 메모리 누수 위험이 매우 커진다.

특히 static 변수로 List, Map, 트리, 대형 캐시 등을 오래 유지하면 메모리 사용량이 점점 증가할 수 있다.

| 피해야 할 static 사용         | 대안                                  |
|-------------------------|-------------------------------------|
| 대량의 List, Map, 트리 구조 캐싱 | Caffeine, Redis 같은 외부 캐시 사용         |
| 대형 설정파일, XML DOM 등      | 파일 I/O 또는 약한 참조(WeakReference) 활용   |
| 빈 객체 재사용(커넥션, 핸들러 등)    | Object Pool, ThreadLocal 또는 DI 빈 활용 |

### static 변수 갱신: 무중단 배포/핫 리로드 시 static 변수값 갱신 문제

프로세스(process) 수준의 재시작이 아닌 'hot reload', 무중단 배포 등에서는 static 변수에 설정/공유 데이터/리소스를 갖고 있더라도 클래스 언로드가 되지 않으므로 해당 값이나 리소스가 갱신되지
않는다.

예를 들어 다음 Config 클래스를 보자.

```java
public class Config {
    public static final String ENV = loadConfig();  // 최초 로드 시점 값만 남음

    // hot deploy/redeploy 하면, 
    // config.yml이 바뀌었더라도 ENV는 초기화 X
    private static String loadConfig() {
        return new File("config.yml").read();
    }

}
```

static 변수의 값은 클래스가 최초 로드된 시점에 딱 한 번만 초기화되고, 이후 `ClassLoader`가 완전히 언로드되지 않는 한 어떤 재배포, reload, config 변경에도
원래 값이 유지된다(값 갱신 X).

이러한 한계는 실시간 설정 반영이 필요한 대규모 서비스, 외부 리소스(네트워크, 설정, 캐시 등) 동적 갱신이 필요한 경우 실제 장애나 장애 추적의 복잡성, 운영 위험도를 크게 높인다.

### static 변수 메모리 누수 방지 전략

- [1. 클래스 언로드/리로딩의 예외적인 상황](#1-클래스-언로드리로딩의-예외적인-상황)
- [2. Reference API 와 약한 참조](#2-reference-api-와-약한-참조)
- [3. JVM Heap/Metaspace 증설이 만능 대책은 아니다](#3-jvm-heapmetaspace-증설이-만능-대책은-아니다)
- [4. JVM GC, 메모리, 실전 운영 옵션 총정리](#4-jvm-gc-메모리-실전-운영-옵션-총정리)

#### 1. 클래스 언로드/리로딩의 예외적인 상황

개발 중 Spring Boot devtools, 톰캣의 개발용 자동 리로딩 등에서는 클래스가 재로딩되어도 이전 static 변수/객체가 완전히 제거되지 않아 Old 영역(메서드 영역/Metaspace)에 "유령
ClassLoader"와 static 데이터가 고립된 채 지속될 수 있다.

이로 인해 코드 수정과 무관하게 메모리 누수(OOM, OutOfMemoryError)가 발생할 수 있다.

#### 2. Reference API 와 약한 참조

**static 변수로 대형 객체나 컬렉션을 직접 관리**하면 무중단 배포/리로드 환경 뿐만 아니라, 프로세스가 살아 있는 한 **메모리 누수로 이어질 수 있다.**

이 위험을 줄이려면 **약한 참조(`WeakReference`, `SoftReference`) 클래스, Reference API를 활용해 GC가 필요 시 자동으로 회수할 수 있게 만들어야 한다.

```java
// WeakReference: 강한 참조가 모두 사라지면 GC가 객체를 곧바로 회수한다.
private static final WeakReference<Integer> DEFAULT_COUNT = new WeakReference<>(10);

// WeakHashMap: key가 더이상 강하게 참조되지 않으면 해당 entry가 자동 삭제된다.
private static final WeakHashMap<Key, Value> CACHE = new WeakHashMap<>();
```

예를 들어, `WeakHashMap`은 key 객체가 별다른 강한 참조 없이 사라지면 Map에서도 자동 삭제되어, `static` 변수처럼 "서버가 살아있는 한 항상 점유"되는 메모리 이슈를 많이 완화할 수 있다.

| 참조 종류  | 코드 예시                                                            | GC 대상                               | 주요 용도                    |
|--------|------------------------------------------------------------------|-------------------------------------|--------------------------|
| 강한 참조  | `Data data = new Data();`<br>`list.add(data);`                   | X (참조가 남아 있는 한 GC 대상 아님)            | 일반 변수, 필드, 컬렉션, static 등 |
| 약한 참조  | `WeakReference<Data> w = new WeakReference<>(new Data());`       | O (다른 강한 참조가 사라지면 즉시 GC 대상)         | WeakHashMap, 임시 캐시       |
| 소프트 참조 | `SoftReference<Data> s = new SoftReference<>(new Data());`       | O (메모리 부족 시 GC 대상)                  | 이미지 캐시, 메모리 상황에 따라 유지    |
| 팬텀 참조  | `PhantomReference<Data> p = new PhantomReference<>(..., queue);` | O (소멸 직전 ReferenceQueue에 통지, 최종 단계) | 리소스 추적, 파이널라이즈 등         |

#### 2.1. 약한 참조는 GC 가능, 강한 참조는 GC 불가

약한 참조를 사용하더라도,

코드 어딘가(변수, static, 컬렉션 등)에서 해당 객체를 **강하게 참조**하면 GC는 그 객체를 절대 수거할 수 없다.

자바에서 Reference API(WeakReference, SoftReference 등)를 사용하지 않는 대부분의 참조는 모두 **강한 참조(strong reference)로 분류된다.** 변수, 필드, 컬렉션,
static 등 객체를 가리키는 경우가 모두 여기에 해당한다.

```java
class Sample {
    // static 약한 참조를 써도 내부에 강한 참조가 생기면 여전히 누수 가능!
    static WeakReference<List<?>> list = new WeakReference<>(new ArrayList<>());

    void strongReference() {
        // "data"는 Data 인스턴스를 강하게 참조.
        Data data = new Data(); // 강한 참조

        // list 객체(컬렉션)가 살아있으면,
        // 컬렉션 모든 요소(data, this)도 GC 대상이 되지 않는다.
        list.get().add(data);
        list.get().add(this);
    }
}
```

즉, 변수를 직접 선언하거나 컬렉션에 객체를 추가하면 모두 강한 참조가 된다. 특히 컬렉션이 살아 있는 한 그 내부 요소들도 GC 대상에서 제외된다.

약한 참조만 남기고 싶다면, 항상 강한 참조가 완전히 해제되는지 (즉, 컬렉션·static 등에서 추가로 참조가 남아 있지 않은지) 점검해야 한다.

따라서 static 변수 활용에서는 불변 객체(final)로 설계하거나, 컬렉션(예: Map)을 사용할 땐 unmodifiableMap 등 불변 컬렉션으로 요소의 추가/삭제와 객체 생명주기를 꼼꼼히 통제해야 예상치
못한 메모리 누수, GC 미수거 현상을 방지할 수 있다.

> 단, Weak/SoftReference만 남겼다면 실제 강한 참조(컬렉션, static, 전역 변수 등)가 완전히 해제되는지 꼭 점검해야 한다.<br/>
> 불변 객체로 static/컬렉션을 만들면, 값 변경/임의 할당 실수를 원천적으로 차단하고  
> 동일한 데이터 구조 안전성, 멀티스레드 운영, 유지보수성 모두를 한 번에 높일 수 있다.<br/>

#### 2.2. 외부 캐시 저장소 활용

애플리케이션 수준에선 약한 참조 래퍼 클래스를 적절히 활용하거나,

혹은 리소스가 큰 데이터는 외부 캐시 라이브러리나 저장소(Guava Cache, Java Caffeine, Redis 등)를 사용해 만료 정책(Eviction Policy, TTL)을 이용하면 메모리 부족을 감지해
자동으로 데이터를 정리할 수 있다.

GC가 이런 구조와 조합되면, static 변수 남용으로 인한 메모리 누수 위험을 크게 줄일 수 있다.

#### 3. JVM Heap/Metaspace 증설이 만능 대책은 아니다

운영 환경에서는 Heap(Young/Old)뿐 아니라, JDK8 이전의 PermGen, 이후의 Metaspace 크기를 조절해 static 메모리 누수나 GC 압박 문제를 일시적으로 완화할 수 있다.

- Heap(Young/Old): new 객체, 애플리케이션 실제 데이터/배열 저장소
- Metaspace: 클래스 메타/정보, static 변수, reflection 등
- PermGen(8이전): 메타스페이스와 비슷, max 제한 잘못 잡으면 ClassLoader leak OOM 문제 다수

> Heap = 객체/배열, Metaspace/PermGen = 클래스/메타/리플렉션/정적 자원.<br/>
> G1/ZGC는 장기 운영·저지연 환경(B2B, 금융 등)에 적합하다.

그러나, 참조 유지/수동 해제 실패/ClassLoader 고립 등 **근본 원인**을 해결하지 않으면 장기 운영 중에는 결국 `OutOfMemoryError`, 서버 장애 등으로 이어질 수 있다.

OOM(OutOfMemoryError)이 발생해 힙/메타스페이스 크기를 무작정 늘리면 일시적으로 장애가 완화되는 듯 보이지만, 힙이나 메타스페이스가 커질수록 **GC의 Mark-Sweep-Compact 과정이 대량 데이터/객체 그래프를 다루게 되어, Full GC와 Stop-the-world 정지 시간(Pause time)이 크게 증가**한다.

이런 상태에서는 GC가 오래 걸리고, 스레드 스위칭/응답 지연이 누적된다. 결국 장애 발생 빈도가 오히려 증가할 수 있다는 점에 유의해야 한다.

#### JVM GC, 메모리, 실전 운영 옵션 총정리

GC 알고리즘에 따라 GC pause 방식은 크게 달라진다. 기본적인 `Stop-the-world` 기반 GC는 `Mark → Sweep → Compact` 단계에서 전체 애플리케이션을 멈추지만, 최신 GC는 pause time 최소화/예측성에 더 초점이 맞춰져 있다.

- SerialGC(단일 스레드, 긴 pause)
    - 모든 GC 단계마다 단일 스레드만 사용(`Serial`, 직렬적 처리)
    - Young, Old, Full GC 모두 단일 스레드로 처리되므로 Pause(정지) 시간이 매우 길고, 성능(Throughput)이 떨어짐
    - pause time 예측이나 동적 조정 기능 없음
    - 리소스가 제한된 소형 애플리케이션, 테스트용 환경에 적합
    - 명시적 사용 `-XX:+UseSerialGC`
    - 기본값은 데스크탑 환경이나 작은 메모리 설정 시 적용되며, 서버에서는 기본값 아님
- ParallelGC(고처리량, pause는 길다)
    - JDK8 기준 서버 JVM의 기본값
    - `ParallelGC`는 높은 처리량(Throughput)을 제공하지만, pause 시간이 상대적으로 길다.
    - 여러 스레드를 활용하여 GC를 병렬 수행
    - **처리량(Throughput)을** 극대화하는 데 목적
    - GC pause는 G1GC보다 길지만, 단기 작업이나 처리량이 중요한 배치 환경에 적합
- G1GC(기본 GC, 예측 가능한 Pause Time)
    - JDK9 이상에서는 기본 GC
    - GC `pause time`을 일정 시간 이하로 유지하는 것이 주요 목표 (low-pause, balanced GC)
    - Young/Old 영역을 지역(Region)단위로 분할, 우선 GC 효과가 높은 영역(Region) 먼저 수집
    - 병렬(Parallel) + 동시(Concurrent) 처리를 지원
    - 자동 튜닝 기능 내장: `Concurrent Mark/Sweep/Compaction`도 pause 없이 최대한 비동기 수행
    - 실서비스 환경, 대용량 데이터, 장시간 무중단 운영에 적합
    - 적은 정지 시간, 대용량 힙, 현대 서버 JVM의 표준
- ZGC, Shenandoah(저지연 GC, Concurrent GC)
    - 대부분의 GC 단계를 백그라운드에서 수행
    - 객체 이동(Compaction)을 포함한 단계도 Pause 없이 또는 수 밀리초 수준으로 처리
    - ZGC
        - JDK11에서 도입, JDK15부터 안정화
        - GC pause 시간 수 밀리초 미만 유지 가능
    - Shenandoah
        - JDK12에 도입, JDK15부터 메인라인에 포함
        - 낮은 지연시간을 위해 pause 없는 compaction 지원
    - 고응답성(저지연)이 필요한 환경에 적합

| JVM 영역            | JDK8: 옵션/특징                                                             | JDK17/21: 옵션/특징                                                                                                             |
|-------------------|-------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| Heap(Eden, Old)   | `-Xms<size>`: 초기 크기<br>`-Xmx<size>`: 최대 크기                              | 동일(JDK21도 고전 옵션 사용)<br>컨테이너 환경 인식 기능 추가                                                                                     |
|                   | `-XX:NewRatio` (Old:Young 비율)<br>`-XX:SurvivorRatio` (Eden:Survivor 비율) | 동일                                                                                                                          |
| Metaspace/PermGen | `-XX:PermSize`, `-XX:MaxPermSize` (**PermGen, JDK8까지**)                 | **PermGen 폐지**<br>`-XX:MetaspaceSize`, `-XX:MaxMetaspaceSize` (**Metaspace**, JDK8 이후)                                      |
| GC 알고리즘           | 기본: `ParallelGC` (서버) <br>`-XX:+UseG1GC`로 G1 명시                         | 기본: G1GC<br>`-XX:+UseZGC`, `-XX:+UseShenandoahGC` 등 고급 GC 선택 가능                                                             |
| GC 스레드 설정         | `-XX:ParallelGCThreads`, `-XX:ConcGCThreads` 등                          | 동일, 추가 GC에 따라 다양한 병렬/동시 옵션 사용 가능                                                                                            |
| GC 로그 출력          | `-verbose:gc`, `-XX:+PrintGCDetails`, `-Xloggc:<file>` (구식 형식)          | `-Xlog:gc*` 사용 (표준화된 구조, 세분화된 로그)<br>`-Xlog:gc*:file=gc.log:time,...`                                                       |
| 컨테이너 지원           | 기본 미지원<br/>`-XX:+UseCGroupMemoryLimitForHeap` 등 수동 설정 필요                | JDK10 이상: `-XX:+UseContainerSupport`(JDK17 부터 기본값 ture)<br/>컨테이너(CGroup) 리소스 제한을 자동 인식                                      |
| RAM 비율            | 없음                                                                      | `-XX:MaxRAMPercentage=75.0`: 전체 RAM의 75%를 힙 최대값으로 설정<br/>- `-XX:InitialRAMPercentage=50.0`: 힙 초기값을 전체 RAM의 50%로 (JDK12+) 설정 |

- Heap 및 GC 관련 핵심 옵션은 JDK8~21까지 큰 틀에서 유사하다.
- `PermGen`은 JDK8까지만 존재하며, 이후는 `Metaspace`로 대체됐다.
- GC 알고리즘
    - GC 알고리즘 기본값은 JDK8에서는 ParallelGC, JDK9 이후부터는 G1GC.
    - JDK8: ParallelGC(서버 환경 기준), G1은 명시적으로 지정해야 함
        - `-server` 플래그가 붙는 JVM이면 ParallelGC가 기본
    - JDK9~: `G1GC`가 기본, JDK17/21에도 G1이 가장 보편
- GC 로그 옵션
    - GC 로그는 JDK8에서는 개별 옵션 조합 필요, JDK17+는 `-Xlog` 형식으로 표준화됨.
    - JDK8: `PrintGCDetails`, `-verbose:gc`, `-Xloggc`
    - JDK17/21: `-Xlog:gc*` (표준화, 필터링, 출력 방식 세분화)
- 컨테이너 환경 대응
    - 컨테이너 환경 대응은 JDK10 이후로 대폭 개선됨.
    - JDK8: 별도 옵션 필요(`-XX:+UseCGroupMemoryLimitForHeap`)
    - JDK10~: `-XX:+UseContainerSupport` JDK17부터는 기본값 true
    - JDK17/21: `-XX:MaxRAMPercentage` 등 RAM 자동 할당 옵션 추가됨
- 최신 GC 알고리즘
    - ZGC, Shenandoah(JDK15~)
    - ZGC: JDK11에서 실험적 도입, JDK15부터 안정화
    - Shenandoah: JDK12에 처음 도입, JDK15부터 OpenJDK mainline 포함
    - 두 GC 모두 낮은 지연시간을 목표로 하며, pause 없는 GC 구조

GC 옵션이나 알고리즘 선택은 애플리케이션의 응답성, 처리량, 리소스 제약 등과 밀접하게 연결된다. 단순히 Heap이나 Metaspace 크기만 늘려서 임시 해결하는 것은 근본적인 메모리 누수나 참조 유지 문제를 해결하지 못하며, 오히려 Full GC 증가 및 OOM 위험을 높일 수 있다.

> 운영 환경에서 GC 옵션/알고리즘은 대상 애플리케이션의 응답성, 처리량, 리소스 제약 등을 고려하여 애플리케이션 특성에 맞게 튜닝해야 한다.

## static 변수 활용 가이드

코드 가독성과 성능, 메모리 효율까지 챙기자.

### 매직 상수의 대안 – `static final` 상수

매직 상수(Magic Constant)란 의미 없는 숫자나 문자열을 코드에 직접 하드코딩한 값이다.

```java
if(userRole.equals("admin")){
timeout =5000;
        }
```

이런 코드는 의미가 불분명하고, 오타 및 중복 값 관리가 어렵다.

이 값들은 이해하기 어렵고 나중에 수정하기도 불편하다. 유지보수성과 가독성을 위해 `static final` 상수로 변환해 관리하면 실무, 협업, 운영 등에서 다양한 이점이 있다.

```java
public class Constants {
    public static final String SYSTEM_ADMIN = "admin";
    public static final int DEFAULT_TIMEOUT = 5000;
}
```

- 가독성: 도메인 의미가 코드에 명확 드러남
- 유지보수: 한 곳만 수정, 전체 일관 적용
- 오타 방지 및 IDE 자동완성 지원
- 메모리/성능 이점
    - 컴파일 시점에 `inline`돼서 실제로는 상수가 코드에 직접 삽입됨
    - **`static final` 상수는 런타임 상수 풀(constant pool) 에 저장되어 GC 대상이 아님**

### Enum 캐싱

`enum`은 단순한 값 집합이 아니다.

타입 안정성, 명확한 의미 전달, 그리고 고성능 캐싱까지 아우를 수 있는 실전 도구다.

```java

@RequiredArgsConstructor
@Getter
public enum ServerType {
    WEB(1), API(2), UPLOAD(3);

    private final int value;

    private static final Map<Integer, ServerType> ALL = Arrays.stream(values())
            .collect(collectingAndThen(
                    toMap(ServerType::getValue, Function.identity()),
                    Collections::unmodifiableMap
            ));

    public static ServerType from(int value) {
        return ALL.get(value);
    }
}
```

- 타입 안정성: 실수 방지, 컴파일 타임 오류 유도
- 명확한 의미/표현: 상수에 의미·컨텍스트를 부여. (비즈니스 상황별 명확한 네이밍)
- 빠른 조회 성능: 내부 캐시(Map) 기반으로 O(1) 조회
- Singleton/Flyweight 패턴: Enum 인스턴스는 JVM당 단 한 번만 생성 (메모리 오버헤드 없이 여러 곳에서 안전한 재사용 가능)
- 확장성/불변성: 새로운 값 추가 시 구조 변경 없이 안전하게 반영 가능
- 역/정방향 매핑에 적합: 코드 ↔ enum 상수 ↔ 설명 간 매핑 구현이 용이

Enum 활용과 내부 static final Map 캐싱 조합은 코드 품질은 물론, 유지보수, 확장, 테스트, 성능(조회 효율)까지 한 번에 챙길 수 있다.

## 마무리

static 변수는 클래스가 로드되는 시점부터 메모리에 상주하며, 클래스 언로드 전까지 GC 대상이 아니다.

이 특성은 캐시, 설정, 상수 등 실무에서 유용하지만, 생명주기를 고려하지 않으면 심각한 메모리 누수와 장애로 이어질 수 있다.

### 상황별 static 활용 가이드

| 상황                             | 권장 접근법 및 주의사항                                           |
|--------------------------------|---------------------------------------------------------|
| 숫자/문자열 상수                      | `static final` 사용. 매직 상수 방지, GC 대상 아님                   |
| 코드 의미 + 타입 안정성 확보              | `enum` 사용. 의미 전달과 안전한 값 관리 가능                           |
| enum ↔ ID 간 빠른 매핑              | `enum + static Map` 패턴. 빠른 조회, Flyweight 성격 유지          |
| 대형 객체, 외부 자원 공유                | `static` 지양. DI나 외부 캐시 도구(Caffeine, Redis 등) 사용         |
| 테스트 격리 필요                      | `static` 상태 피하기. `@DirtiesContext`, 별도 인스턴스 주입 등 고려     |
| 컬렉션(static `List`, `Map` 등) 사용 | 캐시나 공유 목적이라면 **명시적인 초기화/해제** 설계 필요. 메모리 고정 점유로 인한 누수 주의 |

클래스가 Spring 싱글톤처럼 오래 살아있다면 static 변수는 그만큼 장기 메모리 점유자가 된다. 특히 캐시나 공유 객체처럼 크기가 커질 수 있는 static 컬렉션은 GC 대상이 아니므로 설계 초기에 생명주기를 신중히 고려해야 한다.

### Q&A

#### Q. static 변수 누수, 실제로 어떤 장애가 터지는가?

배포 후 WAS 정상 재기동 전까지 트래픽이 모일수록 힙·메타스페이스·Old 영역이 감소하여 OOM, 응답 지연, 심하면 전체 서버가 다운된다.

#### Q. reload/devtools 등 Hot reload 상황에서 static 변수는 어떻게 처리하나?

`static` 변수는 `ClassLoader`와 함께 GC돼야 완전한 메모리 해제가 가능하다. 하지만 Thread, ExecutorService, JDBC 커넥션 등 외부 자원을 참조하고 있으면 ClassLoader 언로드 자체가 불가능해지며, 메모리 누수 고착화가 발생할 수 있다.

#### Q. 그럼 캐시는 어떻게 쓰는 게 좋은가?

Guava, Caffeine, Redis 등 외부 캐시 도구를 사용하는 것이 권장된다. 이들은 eviction 정책, TTL, maxSize 등으로 캐시 생명주기 자동 관리가 가능하며, 사용량 모니터링 및 경고 임계치 설정으로 장애 예방 효과도 뛰어나다.

## 참고자료

- [Java Virtual Machine Specification](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html)
- 톰캣 ClassLoader 및 메모리 누수 관련 글들

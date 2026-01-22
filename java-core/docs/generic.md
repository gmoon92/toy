# Java 제네릭 완전 정복

- 타입 소거(Type Erasure)
- 브릿지 메서드(Bridge Method)
- PECS(Producer Extends Consumer Super)
- 재귀적 타입 한정(Recursive Type Bound)

## 제네릭이란?

제네릭은 클래스나 메서드가 데이터를 처리할 때, **구체적인 타입을 지정하지 않고 타입 매개변수(T 등)를 선언해** 코드 작성 시점에서는 타입을 일반화(generalization)하면서, **컴파일 시점에 타입 안전성(type safety)을 보장**하면서 코드 재사용성을 높이는 문법이고, JVM 호환을 위해 런타임에는 타입 정보가 소거(Type Erasure) 됩니다.

- **타입 안정성 보장**: 컴파일 시점에 타입 오류 사전 방지
- **형변환(Casting) 생략**: 명시적 캐스팅 필요 없이 안전하게 데이터 꺼내기
- **코드 재사용성 증가**: 하나의 코드로 다양한 타입 처리

## 1. 왜 제네릭이 등장했는가?

Java 1.5 이전에는 컬렉션 API 등이 모두 Object 타입 기반이었기 때문에 모든 값을 꺼낼 때마다 명시적으로 캐스팅(casting)이 필요했습니다.

```java
// JDK 1.5 제네릭 등장 전
void jdk5() {
    List list = new ArrayList();
    list.add(1);
    String s = (String) list.get(0);  // 런타임에 ClassCastException 발생!
}
```

여기서, 제네릭을 명시하지 않은 타입(`List`처럼 타입 파라미터 없이 사용하는 형태)을 **Raw type(원시 타입)이라고 합니다.** Raw type을 사용하면 타입 안정성을 컴파일 시점에 보장할 수 없기 때문에, 위 예시처럼 **런타임 오류(ClassCastException 등)가 자주 발생하게 됩니다.**

제네릭이 도입된 후에는 아래와 같이 코드의 안전성이 높아졌습니다.

```java
// 제네릭 등장 후
void generic() {
    List<String> list = new ArrayList<>();
    list.add("hello");
    String s = list.get(0);  // 안전하게 사용 가능
}
```

제네릭은 이처럼 **컴파일 시점에 타입 안정성**을 확보하기 위해 도입되었습니다. JVM의 호환성과 기존 코드와의 통합을 위해 **런타임에는 타입 정보가 소거(Type Erasure)되어 동작**한다는 특징이 있습니다.

### 핵심 용어

- 타입 파라미터: `T`, `E` 등 — 일반화된 타입 자리표시자
- Raw type: 제네릭을 적지 않은 타입 (`List`), 타입 안전성 상실
- 와일드카드 `?`: 알 수 없는 타입(unknown)
- 상한(extends) / 하한(super): `? extends T`, `? super T`
- 실체화 불가(non-reifiable): 런타임에 완전한 타입 정보가 없음

## 2. 타입 소거(Type Erasure)

자바의 제네릭은 **런타임이 아니라 컴파일 시점에만 존재**합니다.  

컴파일이 끝나면 타입 매개변수에 대한 정보는 제거(소거)되어, 실행 시점에는 타입이 없는(raw type) 코드로 동작하게 되는데, 이러한 과정을 **타입 소거(Type Erasure)** 라고 부릅니다.

### 타입 소거 과정 예시

```java
// 컴파일 전 (제네릭 코드)
public class Box<T extends Number> {
    T data;

    public Box(T data) {
        this.data = data;
    }

    void setData(T data) {
        this.data = data;
    }
}
```

컴파일 후에는 **타입 매개변수 T가 상한 타입(Number)로 치환**됩니다.

```java
// 컴파일 후 (타입 소거)
public class Box {
    Number data;

    public Box(Number data) {
        this.data = data;
    }

    void setData(Number data) {
        this.data = data;
    }
}
```

### 추가 예제: List<String>의 소거

```java
public void test() {
	// 컴파일 전
	List<String> list = new ArrayList<>();
	list.add("hello");
	String s = list.get(0);

	// 컴파일 후 (타입 소거)
	List list = new ArrayList();
	list.add("hello");
	String s = (String)list.get(0); // 캐스팅 삽입됨
}
```

제네릭을 사용하면 컴파일러가 타입 안전성을 보장하지만, 컴파일 이후에는 타입 정보가 소거되어, 타입 매개변수가 런타임에 남지 않게 됩니다. 이로 인해 실제로는 원시 타입처럼 동작하며(강제 캐스팅 등), 이는 제네릭과 타입 소거의 가장 중요한 특징이자 한계입니다.

#### 타입 소거의 규칙

1. 타입 매개변수는 **실체화 불가 타입(non-reifiable type)** 이므로 런타임에 남지 않습니다.
2. 타입 매개변수의 소거는 다음과 같이 이루어집니다.
    - **상한 제한이 없을 경우** → `Object`로 치환
    - **상한 제한이 있을 경우** → 그 상한(Upper Bound) 타입으로 치환
3. 필요시 **브릿지 메서드(Bridge Method)** 가 자동 생성됩니다.

#### 컴파일러에 의해 생성된 Box.class

```text
javac Box.java
javap -c -s -p Box.class
```

```xpath2
Compiled from "Box.java"
public class com.gmoon.javacore.Box<T extends java.lang.Number> {
  T data;
    descriptor: Ljava/lang/Number;

  public com.gmoon.javacore.Box(T);
    descriptor: (Ljava/lang/Number;)V
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: aload_0
       5: aload_1
       6: putfield      #7                  // Field data:Ljava/lang/Number;
       9: return

  void setData(T);
    descriptor: (Ljava/lang/Number;)V
    Code:
       0: getstatic     #13                 // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #19                 // String Box setData
       5: invokevirtual #21                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: aload_0
       9: aload_1
      10: putfield      #7                  // Field data:Ljava/lang/Number;
      13: return
}

```

### 타입 소거의 한계 및 단점

**자바의 제네릭 타입은 컴파일 후에는 실제 타입 정보가 완전히 사라지는 것을 원칙**으로 하며, 이로 인해 런타임 타입 검사, 리플렉션 등에서 T의 구체적 정보를 알 수 없습니다.

이 특성이 자바 제네릭의 가장 큰 특징이자 한계이기도 합니다.

| 한계                   | 예시 코드                                 | 설명                   |
|----------------------|---------------------------------------|----------------------|
| 런타임 타입 정보 상실         | `obj instanceof List<String>`         | 컴파일 불가, 런타임에 T 정보 없음 |
| 제네릭 타입 배열 생성 불가      | `new List<String>[10]`                | 컴파일 불가               |
| 제네릭 타입으로 new 연산자 불가  | `new T();`                            | 컴파일 불가               |
| 타입 파라미터로 static 사용불가 | `static T value;`                     | 컴파일 불가               |
| class 리터럴 사용 불가      | `T.class`                             | 컴파일 불가               |
| 리플렉션에서 타입 정보 소실      | `list.getClass().getTypeParameters()` | List의 T 정보 얻을 수 없음   |

#### 런타임 타입 정보 상실

- 제네릭 타입 T의 구체적인 클래스를 런타임에서 얻을 방법이 없습니다.
- 리플렉션으로도 실제 파라미터 타입을 알 수 없습니다.

```java
public void test() {
	List<String> stringList = new ArrayList<>();
	System.out.println(stringList.getClass() == new ArrayList<Integer>().getClass()); // true
	System.out.println(stringList.getClass()); // class java.util.ArrayList
}
```

- 타입 파라미터가 다르더라도 런타임에는 모두 똑같은 클래스입니다!

#### 제네릭 타입 배열 생성 불가

- 컴파일러는 타입 안전성을 보장할 수 없으므로, 직접적으로 제네릭 배열을 만들 수 없습니다.

```java
List<String>[] array = new List<String>[10]; // compile error!
```

- 배열은 런타임에 구체 타입 정보를 가져야 하지만, 제네릭은 타입 소거로 인해 불가능합니다.
- 만약 우회적으로 만들고 싶다면 `@SuppressWarnings("unchecked")`로 경고를 무시해야 하므로 추천되지 않습니다.

#### instanceof 연산자 사용 불가

- 제네릭 타입(T)으로는 `instanceof` 체크가 불가능합니다.

```java
public void test() {
	List<String> list = new ArrayList<>();

	boolean test1 = list instanceof List<String>; // compile error!
	boolean test2 = list instanceof List; // OK
}
```

- 런타임에 String 정보가 사라짐 → 검사 불가

#### 타입 파라미터로 static, class 리터럴 사용 불가

```java
class Box<T> {
	// static T value;        // compile error!
	// Class<T> clazz = T.class; // compile error!
}
```

- static 멤버는 클래스 로딩 시점에 초기화되므로, 타입 파라미터 정보가 존재하지 않음

#### 기본 타입 사용 불가

```java
public void test() {
	List<int> ints = new ArrayList<>(); // compile error!
}
```

- 제네릭은 객체 타입만 가능 → 기본 타입은 래퍼 클래스 사용 (Integer)

#### 리플렉션을 통한 우회

타입 소거 때문에 컴파일러는 막지만, 리플렉션을 사용하면 타입 불안전성을 강제로 깨트릴 수 있습니다.

````java
public void test() {
	List<String> strings = new ArrayList<>();
	Method m = strings.getClass().getMethod("add", Object.class);
	m.invoke(strings, 123); // 경고 없이 Integer 삽입
}
````

- 런타임에서 ClassCastException 발생 가능

## 3. 브릿지 메서드(Bridge Method)

앞서 설명한 **타입 소거(Type Erasure)는 제네릭의 타입 정보를 컴파일 시점에 제거합니다.**

이 과정에서, **상속·오버라이딩이 적용된 제네릭 클래스(메서드) 사이의 시그니처 불일치 문제**가 발생할 수 있습니다. 예를 들어, 아래와 같이 상위 타입은 `Box<T extends Number>`로, 하위 타입은 `Box<Integer>`로 선언된 경우를 살펴봅시다.

```java
class Box<T extends Number> {
	T data;

	public Box(T data) {
		this.data = data;
	}

	void setData(T data) {
		this.data = data;
	}
}

class IntegerBox extends Box<Integer> {
	public IntegerBox(Integer data) {
		super(data);
	}

	@Override
	void setData(Integer data) {
		this.data = data;
	}
}
```

타입 소거가 일어나면

- `Box<T>`는 실질적으로 `Box` (`setData(Number)`) 만 남게 되지만,
- `IntegerBox`의 오버라이드된 메서드는 `setData(Integer)`가 됩니다.

즉, **상위·하위 클래스의 메서드 시그니처가 달라 JVM 오버라이드 규칙이 깨지게 됩니다.**  

JVM의 다형성으로 잘 동작하려면 "`setData(Number)`"를 호출해도 실제로 `IntegerBox`의 `setData(Integer)`를 찾아야 된다는 의미입니다. 이 상황에서도 정상적인 다형성(Polymorphism)을 지원하기 위해 **자바 컴파일러는 '브릿지 메서드(Bridge Method)'를 자동 생성**합니다.

### 브릿지 메서드 예시

```java
public class IntegerBox extends Box<Integer> {
	private int data;
	
	public IntegerBox(Integer data) {
		super(data);
	}
	
	@Override
	void setData(Integer data) {
		super.setData(data);
	}

	// 컴파일러가 자동 생성:
	// (브릿지 메서드)
	void setData(Number data) {
		// Integer로 다운캐스팅해서 오버라이딩된 메서드 호출
		setData((Integer)data);
	}
}
```

- void setData(Integer data)
- void setData(Number data) // 브릿지 메서드

이 브릿지 메서드 덕분에 `Box#setData(Number)` 를 호출하더라도 `IntegerBox#setData(Integer)` 가 호출됩니다.

```java
void test() {
	Box iBox = new IntegerBox(1);
	iBox.setData(1);
}
```
```text
// 호출 흐름
iBox.setData(1)
   ㄴ> IntegerBox.setData(Number data)           // 브릿지 메서드 (자동 생성)
        ㄴ> IntegerBox.setData(Integer data)      // 진짜 오버라이딩된 메서드(직접 작성)
            ㄴ> Box.setData(Number data)          // super.setData에서 올라가는 경우
```

와 같이 상위 타입(타입 소거된 메서드 시그니처)으로 호출해도 하위 타입의 오버라이딩 메서드가 안전하게 동작합니다.

#### 컴파일러에 의해 생성된 IntegerBox.class

```text
javac Box.java IntegerBox.java
javap -c -s -p IntegerBox.class
```

```xpath2
Compiled from "IntegerBox.java"
public class com.gmoon.javacore.IntegerBox extends com.gmoon.javacore.Box<java.lang.Integer> {
  public com.gmoon.javacore.IntegerBox(java.lang.Integer);
    descriptor: (Ljava/lang/Integer;)V
    Code:
       0: aload_0
       1: aload_1
       2: invokespecial #1                  // Method com/gmoon/javacore/Box."<init>":(Ljava/lang/Number;)V
       5: return

  void setData(java.lang.Integer);
    descriptor: (Ljava/lang/Integer;)V
    Code:
       0: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
       3: ldc           #13                 // String IntegerBox setData
       5: invokevirtual #15                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
       8: aload_0
       9: aload_1
      10: invokespecial #21                 // Method com/gmoon/javacore/Box.setData:(Ljava/lang/Number;)V
      13: return

  // !!브릿지 메서드!!
  void setData(java.lang.Number);
    descriptor: (Ljava/lang/Number;)V
    Code:
       0: aload_0
       1: aload_1
       2: checkcast     #24                 // class java/lang/Integer
       5: invokevirtual #26                 // Method setData:(Ljava/lang/Integer;)V
       8: return
}

```

## 4. 재귀적 타입 한정 (Recursive Type Bound)

**재귀적 타입 한정(Recursive Type Bound)** 이란,  

타입 매개변수가 **자기 자신을 상한(bound)으로 제한**하는 제네릭 패턴입니다.  
이는 타입의 계층 구조를 더욱 엄격하게 정의하고, 예상 가능한 타입끼리 안전하게 비교나 연산을 할 수 있게 해줍니다.

가장 대표적으로 자바의 `Comparable` 인터페이스에서 활용됩니다.

```java
class Money<T extends Comparable<T>> {
	private T value;

	public MyClass(T value) {
		this.value = value;
	}

	public boolean isGreaterThan(T other) {
		return value.compareTo(other) > 0;
	}
}
```

- `T extends Comparable<T>` **T는 반드시 Comparable<T>를 구현해야만 함**

즉, `Money<BigDecimal>`, `Money<Double>` 등과 같이 **자기 자신을 기준으로 비교(compareTo)** 가 가능한 타입만 `Money`의 타입 파라미터가 될 수 있습니다.

### 재귀적 타입 한정의 의미

`T extends Comparable<T>`처럼 **타입 매개변수 T가 Comparable<T>를 구현하는 타입**으로 제한하면, 비교 대상 둘 모두가 "동일 타입"임이 보장됨을 의미합니다.

만약,

```java
class Money<T extends Comparable<BigDecimal>> { ... }
```

처럼 다른 타입이 들어가면, compareTo 호출에서 타입 불일치로 안전하게 비교가 불가합니다.

이는 자바 표준 클래스 대부분에서 이 패턴을 만날 수 있습니다.

- `public final class Integer implements Comparable<Integer> { ... }`
- `public final class String implements Comparable<String> { ... }`
- `public class BigDecimal implements Comparable<BigDecimal> { ... }`

이 덕분에 `Collections.sort(List<T extends Comparable<T>>)` 같은 비교 코드가 타입 안전성을 갖게 됩니다.

> **재귀적 타입 한정**은 "타입 자신"을 상한으로 하여, 자기 자신과 비교 또는 연산이 반드시 가능함을 보장한다. 자바에서 Integer, Double, String, BigDecimal 등 "값 비교가 자연스럽게 동작하는 타입"은 모두 이런 방식의 타입 한정을 사용하고 있다.

## 5. 제네릭은 무변성(invariant)이다.

자바의 제네릭은 **무변성(invariant)** 으로 설계되어 있습니다.

```java
void test() {
	List<Number> list1 = new ArrayList<>();
	List<Integer> list2 = new ArrayList<>();

	list1 = (List<Number>)list2; // compile error!!!
}
```

- `List<Integer>`는 `List<Number>`의 하위 타입이 아니며,
- `List<Number>` 역시 `List<Integer>`의 상위 타입이 아닙니다.

만약 제네릭이 변성을 기본 지원한다면(공변성이나 반공변성이면),  

`List<Object>`와 `List<String>`까지 상호 대입이 가능해야 할 텐데, 이렇게 되면 제네릭 타입 시스템이 **컬렉션 요소 타입의 안전성**을 보장하지 못하게 됩니다.  

이런 이유로 자바의 제네릭은 기본적으로 무변성을 강제합니다.

- **무변성**: `A`와 `B`가 아무 관계가 없으면, `Container<A>`와 `Container<B>` 또한 아무런 대입 관계가 없음

하지만, 실무에서는 다양한 타입의 다형성을 지원해야 할 필요성이 자주 생깁니다. 이때 제네릭의 '변성(Variance)' 개념이 중요하게 작용합니다.

### 변성(Variance)이란?

**변성(Variance)** 은 타입 계층에서 **타입 간 대입 가능성을 어떻게 허용할 것인지**를 의미합니다.

크게 두 종류가 있습니다:

- **공변성 (Covariance)**:
  - `A`가 `B`의 서브타입이면, `Container<A>`도 `Container<B>`의 서브타입이다.
  - 함께 변한다, 예: 읽기 전용
- **반공변성 (Contravariance)**:
  - `A`가 `B`의 서브타입이면, `Container<B>`가 `Container<A>`의 서브타입이다.
  - 반대로 변한다, 예: 쓰기 전용

"변성"은 타입 상속 구조에서 서로 다른 타입의 컨테이너 사이에 대입 관계가 존재하는지 여부(=함께 변함/반대로 변함/아예 대입 불가)를 뜻합니다.

이 개념은 **객체지향의 다형성**, 즉 "리스코프 치환 원칙" (서브타입은 언제나 슈퍼타입으로 교체 가능해야 한다)과 밀접하게 연결되어 있습니다.

#### 변성과 OOP의 업캐스팅/다운캐스팅

변성의 개념은 기본적으로 **객체 레벨에서의 업캐스팅/다운캐스팅**을 기반으로 이해할 수 있습니다.

```java
class Animal {}
class Cat extends Animal {}

void test() {
    // 공변성: Cat은 Animal의 서브타입이므로, Cat에서 Animal로의 업캐스팅은 항상 안전하다.
    Animal a = new Cat(); // 업캐스팅 (항상 안전)

    // 반공변성: Animal(부모)에서 Cat(자식)으로의 다운캐스팅은
    // 런타임 타입이 맞는 경우에만 안전하다.
    Cat c = (Cat) a; // 다운캐스팅 (실행시 타입 체크 필요)
}
```

이처럼 "클래스 간 서브타입 관계" 개념이 변성의 기초를 이룹니다. 제네릭의 변성/무변성은 바로 이 객체 다형성과 **컨테이너 타입에서의 대입 가능성**을 연결하는 관점이라고 볼 수 있습니다.

## 6. 와일드 타입으로 인한 변성 지원

앞서 설명했듯이, 자바의 제네릭은 기본적으로 **무변성(invariance)** 입니다.

```java
void test() {
	List<Number> list1 = new ArrayList<>();
	List<Integer> list2 = new ArrayList<>();
    
	list1 = (List<Number>)list2; // compile error!!!
}
```

즉, 서로 다른 타입의 제네릭 컨테이너는 대입 자체가 불가능합니다.

그렇다면 **어떻게 다형성을 지원할 수 있을까요?**

자바에서는 **와일드카드(?)** 문법을 통해 제네릭에 "변성(variance)"을 부여할 수 있도록 했습니다. 와일드카드는 상한 타입 제한(`extends`), 하한 타입 제한(`super`)의 형태로 공변성(읽기 전용), 반공변성(쓰기 전용)을 "가짜로 흉내내어" 코드 재사용성을 높입니다.

| 용어   | 표현            | 설명                             |
|------|---------------|--------------------------------|
| 공변성  | `? extends T` | 상한 제한(Upper Bound), 읽기 전용      |
| 반공변성 | `? super T`   | 하한 제한(Lower Bound), 쓰기 전용      |
| 무변성  | `List<T>`     | 정확히 일치한 타입만 대입 가능, 읽기/쓰기 모두 허용 |

> **참고**: 자바의 제네릭은 언제나 무변성이지만,  
> 컴파일러가 상한/하한 제한(와일드카드)로 변성과 유사하게 동작할 수 있도록 지원합니다.

### 6.1. 와일드카드(?)

와일드카드 `?`는 **알 수 없는 임의의 타입(Unknown Type)** 을 의미합니다.  
기본적으로 `? extends Object`를 의미하며, "이 리스트에는 우리가 정할 수 없는 어떤 타입이 들어 있다"는 표현입니다.

#### `List<?>` vs. `List<Object>`

와일드카드 `?`가 `Object`와 같은 의미일까요? 엄밀히 말하면 둘은 다릅니다.

`?`는 알 수 없는 임의의 타입을 나타내며, 이는 `Object` 타입과는 구분됩니다. List<?>는 "어떤 특정 타입인지 정확히 모르는 리스트"인 반면, List<Object>는 "모든 타입의 객체를 담을 수 있는 리스트"입니다.

```java
void test() {
    List<?> list = new ArrayList<>();
    list.add(1);        // compile error!!!
    list.add("str");    // compile error!!!
    list.add(null);     // null만 추가 가능

    List<Object> objs = new ArrayList<>();
    objs.add(1);        // OK
    objs.add("str");    // OK
    objs.add(null);     // OK
}
```

- `List<?>` : 실제 타입이 뭔지 모르기 때문에, **읽기 전용**(null 외에는 아무것도 add할 수 없음)
- `List<Object>` : Object 및 모든 하위 타입을 넣을 수 있으므로 **읽기/쓰기 모두 가능**

### 6.2. 제네릭 공변성 — 상한 와일드카드(`? extends T`)

**공변성(Covariance)은**

`? extends T` 문법을 통해 하위 타입 컨테이너도 상위 타입 컨테이너로 취급해 읽기는 가능하되, 쓰기는 불가능하게 만듭니다.

```java
void covariance() {
    List<Cat> cats = new ArrayList<>();
    List<? extends Animal> animals = cats; // 허용(공변적)

    Animal a = animals.get(0); // 읽기는 가능, 타입은 Animal
    animals.add(new Dog());    // compile error!!!
    animals.add(new Cat());    // compile error!!!
}
```

**왜 `add`가 불가능할까요?**

`List<? extends Animal>`로 받아도 실제로 어떤 Animal의 하위타입 List인지 모르기 때문입니다. 실제로 내부가 List<Cat>인지, List<Dog>인지 알 수 없기에 쓰기가 가능해지면 타입 안정성 깨지게 됩니다.

```java
// 만약 추가가 가능했다면
List<? extends Animal> animals = new ArrayList<Cat>();
animals.add(new Dog()); // 실제로는 Cat List인데 Dog가 들어감 → 런타임 예외 발생!
```

타입 캐스팅(casting)은 상위-하위(상하) 관계에서만 안전하게 사용할 수 있으며, 형제 타입(같은 부모를 가진 두 타입 ex. Cat, Dog) 간에는 직접적인 캐스팅이나 대입이 불가능합니다.

따라서, List<? extends Animal>과 같은 공변성 컨테이너에서는 정확한 하위 타입(내부가 Cat인지 Dog인지 등)을 알 수 없으므로 요소 추가(add)는 금지되고, 읽기만 안전하게 허용됩니다.

> **공변성:** `A`가 `B`의 서브타입이면, `Container<A>`를 `Container<B>`로 간주<br/>
> 읽기만 허용, 쓰기 불가

### 6.3. 제네릭 반공변성 — 하한 와일드카드(`? super T`)

**반공변성(Contravariance)은**

`? super T` 문법을 통해 하위 타입 요소를 상위 타입 컨테이너에 안전하게 추가하는 것을 허용합니다.

```java
void contravariance() {
	List<? super Cat> catSupers = new ArrayList<Animal>();

	catSupers.add(new Cat());      // add는 가능
	Object a = catSupers.get(0);   // 읽을 때는 Object만 가능
}
```

- `? super T` : 쓰기는 안전(`Cat` 및 서브타입 추가 가능), 읽기는 Object가 반환(정확한 타입 알 수 없음)

**왜 읽기 반환타입이 Object일까?**

컨테이너가 Animal, Cat, Object일 수도 있으므로 정확한 하위타입을 보장할 방법이 없어 Object로 제한됩니다.

> **반공변성:** `A`가 `B`의 서브타입이면, `Container<B>`를 `Container<A>`로 간주한다.<br/>
> 쓰기만 허용, 읽기는 Object

## 7. PECS 법칙

자바에서 **언제 상한(`extends`) 와일드카드와 하한(`super`) 와일드카드를 사용해야 할지** 고민된다면, 이펙티브 자바의 저자 조슈아 블로크가 제안한 **PECS 원칙**이 큰 도움이 됩니다.

#### PECS란

- Producer Extends, Consumer Super
- Producer (내보내는 쪽, 읽기) → `? extends T`
- Consumer (받아 넣는 쪽, 쓰기) → `? super T`

### 어떻게 적용하나?

- 메서드가 **값을 꺼내기(읽기)** 만 한다 → `? extends T`
- 메서드가 **값을 넣기(쓰기)** 만 한다 → `? super T`
- 둘 다 해야 한다 → 구체 타입 파라미터 <T> 사용 (또는 양쪽을 모두 처리하는 제네릭 시그니처)

```java
// 데이터를 "읽기"만 하는 경우 - extends 사용!
public void readData(List<? extends Number> list) {
	Number n = list.get(0);     // 안전하게 읽기 가능
	list.add(10);               // compile error!!!
}

// 데이터를 "쓰기"만 하는 경우 - super 사용!
public void writeData(List<? super Integer> list) {
	list.add(10);               // 안전하게 쓰기 가능
	Object obj = list.get(0);   // 읽기는 Object만 가능
}
```

**즉, 정리하면 "읽기 전용이면 extends, 쓰기 전용이면 super를 써라!"** 

실무에서 자주 등장하는 패턴별로 예시를 들면 아래와 같습니다.

#### 1. 컬렉션 변환 유틸리티

```java
public static <T> List<T> copyList(List<? extends T> src) {
    return new ArrayList<>(src);
}

```

- `src`는 데이터를 “읽기만 하는” 입력 컬렉션이므로 `? extends T` 사용
- 반환 타입은 `List<T>`처럼 명확한 제네릭 타입으로 지정

#### 2. API 응답 처리

```java
class ApiResponse<T> {
    private T data;
    public T getData() { return data; }
}

ApiResponse<List<String>> res = callApi();
List<String> names = res.getData();

```

- 응답 객체를 제네릭 타입으로 설계하면 다양한 데이터 구조를 손쉽게 다룰 수 있음
- 예) `ApiResponse<User>`, `ApiResponse<List<Product>>` 등으로 활용

#### Stream 변환 + 와일드카드

```java
public static <T> void printAll(Stream<? extends T> stream) {
    stream.forEach(System.out::println);
}

```

- `? extends T`를 사용하면 다양한 타입의 스트림을 범용적으로 처리 가능
- 읽기 전용이므로 안전하게 사용할 수 있음

이처럼 실무에서는 데이터 흐름(읽기/쓰기)과 용도에 따라 적절한 변성(extends/super, 혹은 명확한 제네릭 타입)을 사용하는 것이 핵심입니다.

### 오라클 와일드카드 가이드라인 - In/Out 규칙

`PECS` 원칙은 [오라클 공식 와일드카드 가이드라인](https://docs.oracle.com/javase/tutorial/java/generics/wildcardGuidelines.html)에서 **In/Out 규칙**으로 설명됩니다.

```java
public static <T> void copy(List<? extends T> src, List<? super T> dest) {
    for (T t : src) {
        dest.add(t);
    }
}
```
- **"In" 변수**
  - 데이터를 *제공*하는 역할(읽어오는 역할).
  - 예: `copy(src, dest)`에서 `src`가 "in" 변수.
- **"Out" 변수**
  - 다른 곳에서 사용할 데이터를 *저장*하는 역할(써넣는 역할).
  - 예: `copy(src, dest)`에서 `dest`가 "out" 변수.

#### 와일드카드 가이드라인 요약

1. "in" 변수 → **`extends`** 키워드 사용 (상한 와일드카드: `? extends T`)
2. "out" 변수 → **`super`** 키워드 사용 (하한 와일드카드: `? super T`)
3. "in/out" 역할을 모두 수행해야 하는 경우 → *와일드카드 대신* 구체적인 타입 파라미터 사용
4. `Object` 클래스에 정의된 메서드(equals, toString 등)만 사용할 경우 → 무제한 와일드카드(`?`) 사용 가능
5. **메서드 반환 타입에는 와일드카드 사용을 지양**
    - 반환 타입에 와일드카드를 쓰면 호출부에서 와일드카드를 직접 다뤄야 하므로, 혼란과 타입 안전성 저하를 유발할 수 있음

#### "읽기 전용"의 정확한 의미와 예외 (extends의 실제 동작)

`List<? extends T>`는 흔히 "읽기 전용"으로 불리지만, **엄밀히 말하면 완전 불변(immutable)은 아닙니다.**

```java
class NaturalNumber {
    private int i;
    public NaturalNumber(int i) { this.i = i; }
}

class EvenNumber extends NaturalNumber {
    public EvenNumber(int i) { super(i); }
}

void test() {
    List<EvenNumber> le = new ArrayList<>();
    List<? extends NaturalNumber> ln = le;
    
    ln.add(new NaturalNumber(35)); // compile error!!!
}
```

- `le`는 `List<EvenNumber>`
- `ln`은 `List<? extends NaturalNumber>`이며, `le`를 더 범용적인 타입으로 참조한 것
- **그러나 `ln`에는 NaturalNumber나 EvenNumber 인스턴스조차 추가할 수 없음**
  - 이유: 내부적으로 어떤 구체적 타입 리스트인지 알 수 없으므로 타입 안정성을 보장할 수 없음

> 다만 `null` 추가, `clear()` 호출, 반복자를 통한 `remove()`는 가능하므로 *완전한 불변 컬렉션*은 아닙니다. <br/>
> 따라서 "`List<? extends T>`는 엄밀히는 읽기 전용이 아니지만, 사실상 요소 추가·수정이 불가하므로 읽기 전용처럼 동작한다"고 이해하는 것이 정확합니다.

## 마무리

- **제네릭은**
    - 타입 안전성, 코드 재사용성, 가독성을 동시에 높여 주는 자바의 핵심 도구
- **항상 알아야 할 제약:**
    - 타입 소거로 일부 기능(런타임 타입 확인, 배열 등)이 제한됨을 인지해야 함
- **PECS와 와일드카드:**
    - **PECS 원칙**: Producer는 `extends`, Consumer는 `super`
    - 대부분의 제네릭 활용/호환성 문제를 PECS와 와일드카드로 해결 가능
- **반환 타입에는 와일드카드 지양**
    - 메서드 반환 타입에 `?` 사용은 호출부 타입 추론, 타입 안전성 모두 저하 위험
- **타입 파라미터 범위는 최소화**
    - 불필요한 범위 확장은 타입 안전성에 해로움
- **Raw Type(원시 타입) 지양**
    - 타입 검증 무력화, 경고 및 버그 유발
- **리플렉션 사용 시 주의**
    - 제네릭 파라미터 타입 정보 조회 불가
    - 타입 안정성 보장 안 되므로 무조건 신중히 써야 함
- **흔한 실수/오해**
    - `List<Object>`로 `List<String>` 등 다른 타입 대체 가능하다고 잘못 생각
    - `List<? extends Number>`는 사실상 읽기 전용임을 잊는 경우
    - 제네릭 배열 직접 생성 시도(불가)
    - static 영역에서 타입 파라미터 사용 시도(불가)

## Reference

- 이펙티브 자바
- https://www.oracle.com/technical-resources/articles/java/juneau-generics.html
- https://docs.oracle.com/javase/tutorial/java/generics/wildcardGuidelines.html

---

## 부록 1. 제네릭 클래스와 제네릭 메서드의 차이

### 제네릭 클래스란?

- **정의:** 클래스 전체가 타입 파라미터를 가짐
- **용도:** 객체 전체의 타입 특성과 관련
- **예시:**
    ```java
    class Box<T> {
        private T data;
        public void set(T data) { this.data = data; }
        public T get() { return data; }
    }

    Box<Integer> box = new Box<>();
    box.set(123); // T가 Integer로 고정
    ```

### 제네릭 메서드란?

- **정의:** 메서드에만 타입 파라미터를 지정
- **용도:** 클래스의 타입 파라미터와 무관하거나, 여러 타입 인자를 다룰 때 사용
- **예시:**
    ```java
    class Util {
        public static <T> void printTwice(T element) {
            System.out.println(element);
            System.out.println(element);
        }
    }

    Util.<String>printTwice("hello"); // 명시적 타입 지정 가능(생략도 OK)
    Util.printTwice(100); // T는 컴파일러가 추론
    ```
- **분별 포인트**
    - 제네릭 클래스의 메서드는 `public T get()`(클래스의 타입 파라미터 T 사용)
    - 제네릭 메서드는 `<T> T process(T t)`처럼 메서드 선언부에 타입 파라미터 선언 후 사용

## 부록 2. Raw Type(원시 타입) 위험성과 비추천 이유

### Raw Type이란?

- **정의:** 제네릭을 도입한 타입인데, 타입 파라미터를 생략한 채 사용하는 타입
    ```java
    List list = new ArrayList(); // raw type (비추천)
    ```

### 위험성

- **타입 안전성 상실**
    - 컴파일러가 타입 검증을 하지 못해 모든 객체가 삽입 가능  
      → 런타임 ClassCastException 위험
- **코드 가독성과 유지보수 저하**
    - 타입 정보가 명확하지 않으므로, 실제 데이터 타입 파악이 어려워짐
- **경고 발생**
    - 컴파일 시 unchecked 경고가 발생

### 예시

```java
public void test() {
    List raw = new ArrayList(); // raw type
    raw.add(123);
    raw.add("hello");
    
    Integer i = (Integer)raw.get(1); // 런타임 ClassCastException!
}
```

> **항상 List<T> 형태로 구체적 타입 지정이 원칙!  
> (라이브러리 등 외부 유틸리티에서 어쩔 수 없을 때만 raw type 잠깐 허용)**

---

## 부록 3. 와일드카드 캡처(Capture Helper Method 패턴)

### 배경

와일드카드(`?`) 타입을 받는 메서드는 내부에서 `add()`, `set()` 등 쓰기 작업이나, 내부 요소의 강제 교환이 불가합니다. 이런 제약을 우회하려면 “타입 파라미터 캡처” 기법을 사용하게 됩니다.

### 대표 예시: swap 메서드

```java
// 일반적인 swap은 불가!
public static void swap(List<?> list, int i, int j) {
	// 컴파일 오류: set, get 할 수 없음
	// T temp = list.get(i);
	// list.set(i, list.get(j));
}

// 헬퍼 메서드 패턴
public static <T> void swapHelper(List<T> list, int i, int j) {
	T temp = list.get(i);
	list.set(i, list.get(j));
	list.set(j, temp);
}

public static void swap(List<?> list, int i, int j) {
	swapHelper(list, i, j); // 타입 파라미터 자동 추론(capture)
}
```

### 캡처란?

- List<?>의 “?”타입을 내부에서 T로 추론해 별도의 private static <T> 메서드로 분리
- 이렇게 하면 외부 API 타입은 범용성 유지 + 내부적으론 타입 안정성 보장

### 실전 팁

- 복잡한 와일드카드 타입이 등장할 때 헬퍼 메서드 패턴을 떠올리면 대부분의 set/swap 문제를 해결 가능
- 이 패턴은 “이펙티브 자바”, “오라클 공식 튜토리얼” 모두가 권장


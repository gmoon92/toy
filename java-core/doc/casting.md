# Casting

자바 데이터형엔 크게 기본형과 참조형으로 나뉜다.

- 기본형(primitive type)
- 참조형(reference type)

자바에선 대입 연산자('=')를 통해, 값을 할당할 수 있는데, 일반적으론 같은 타입(데이터형)에서만 할당이 가능하다.

```java
double d = 1.1;
long l = d; // compile error
```

따라서 다른 타입에 대한 연산은 캐스팅 연산자를 통해 타입을 변환해줘야 한다.

```java
double d = 1.1;
long l = (long)d; // casting double -> long
```

이를 형변환(casting)이라 한다.

## Polymorphism

형변환은 상속 관계의 서로 다른 타입간에서만 형변환이 가능하다.

리스코프 치환(LSP, Liscov Substitution Principle) 원칙을 준수하여 상속 구조를 설계하고, 이 상속 구조에서 하위 클래스 타입은 상위 클래스 타입으로 변경할 수 있으며, 상위 클래스 타입을
하위 클래스 타입으로 변경 가능하다. 이를 참조 다향성이라 한다.

```java
// 상속 관계의 캐스팅을 "참조 형변환"(Reference casting)이라 불린다.
Parent parent = new Child(); // 업캐스팅
Child child = (Child)parent; // 다운캐스팅
```

- 업캐스팅: 하위 타입 -> 상위 타입
- 다운캐스팅: 상위 타입 -> 하위 타입

> 당연하지만, 참조 형변환은 속성과 행위를 제한한다.

## Variance

형변환은 타입간의 관계가 중요하다.

타입간의 관계 프로그래밍에선 변성(Variance)이라고 한다.

변성(Variance)은 타입의 상속 계층 관계에서 서로 다른 타입 간에 어떤 관계가 있는지를 나타태는 지표이다. 타입 관계에 따라, 공변성(Covariance)과 반공변성(Contravariance) 그리고
무공변/불공변(Invariant)로 분류된다.

|         |                             |                                   | |
|---------|-----------------------------|-----------------------------------|-|
| 공변      | S `<:` T === I<S> `<:` I<T> | S가 T의 하위 타입이면, S[]는 T[]의 하위 타입이다. |
| 반공변     | S `<:` T === I<T> `<:` I<S> | S가 T의 하위 타입이면, S[]는 T[]의 부모 타입이다. |
| 무공변/불공변 | S !== T                     | S와 T는 서로 다른 타입이다.                 |

> 변성은 타입 기반의 프로그래밍 언어에서 나타나는 특징이다.

대체로 자바의 데이터형은 공변성을 띈다.

공변성은 서로 다른 타입간에 함께 변할수 있다는 특징을 말한다. 공변성은 "S가 T의 하위 타입이라면, S를 T로 대체할 수 있는가?"의 문제이기도 하다. 이 개념은 OOP 원칙중 리스코프 치환 원칙에 해당된다.

```java
// 공변성
// "Child"가 "Parent"의 하위 타입이면 
// "Child"를 "Parent"로 대체할 수 있는가?
Parent parent = new Child(); // up-casting

// 반공변성
// "Child"가 "Parent"의 하위 타입이면 
// "Parent"를 "Child"로 대체할 수 있는가?
Child child = (Child)parent; // down-casting
```

따라서, 형변환은 공변성 또는 반공변성을 띈 타입 관계에서만 성립된다고 정리할 수 있다.

## 마무리

- 형제 클래스 타입 관계에선 형변환 불가하다.
- 업캐스팅은 유연한 프로그래밍 지원한다.
    - 멤버 제한
    - 오버라이딩된 자식 메서드 호출
- 다운캐스팅은 업캐스팅으로 멤버 제한된 부분을 복구하는 개념이다.
    - 다운캐스팅은 업캐스팅으로 인스턴스화된 객체에 대해서만 형변환해야 한다.
    - `instanceOf` 키워드를 사용하여 형변환 안전 보장해라.

```text
class Parent {}
class Son extends Parent {}
class Daughter extends Parent {}

// error case1
Son son = new Daughter();

// error case2
Parent parent = new Parent();
Child child = (Child)parent; // runtime error
```

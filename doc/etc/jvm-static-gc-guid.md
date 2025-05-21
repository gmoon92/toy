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
    public int getData() { return data; }
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

>  "클래스가 메모리에 올라간다(클래스 로딩, Class Loading)"란?<br/>
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

## 3. 리터럴 vs 객체: 저장 영역과 GC

리터럴 정수와 문자열 차이. int 등 기본형 리터럴은 상수 풀에 안 들어감, 문자열/상수형 등은 메서드 영역 내 상수 풀에 저장됨

- **기본형 리터럴:**  
  예) `int n = 123; boolean flag = true;`  
  → 스택 영역에 저장, GC 대상 아님

- **문자열 리터럴:**  
  예) `String s = "hello";`  
  → 런타임 상수 풀에 저장, JVM 실행 종료까지 유지, GC 대상 아님

- **new로 만든 객체:**  
  예) `String s = new String("hello");`  
  → 힙에 저장, 참조가 끊기면 GC 대상

| 코드 예시                            | 저장 영역                 | GC 대상 여부   |
|----------------------------------|-----------------------|------------|
| `int a = 10;`                    | 스택                    | ×          |
| `String s1 = "hi";`              | 런타임 상수 풀              | ×          |
| `String s2 = new String("hi");`  | 힙                     | ○(참조 없을 때) |
| `Integer i1 = 127;` (오토박싱)       | Integer 캐시 (-128~127) | ×          |
| `Integer i2 = 200;` (오토박싱)       | 힙                     | ○          |
| `Integer i3 = new Integer(127);` | 힙                     | ○          |

### 예제 코드

```java
public class LiteralDemo {
    public static void main(String[] args) {
        int num = 123;                   // 스택
        String s1 = "hello";             // 런타임 상수 풀
        String s2 = new String("hello"); // 힙
        Integer i1 = 127;                // Integer 캐시(싱글톤)
        Integer i2 = 200;                // 힙(캐시 벗어남)
        Boolean b1 = true;               // Boolean.TRUE(싱글톤)
        Boolean b2 = new Boolean(true);  // 힙(새 객체)
    }
}
```

---

## 4. 오토박싱 & 래퍼 클래스의 캐싱

**오토박싱:**  
기본형을 자동으로 래퍼 클래스 객체로 변환  
예) `Integer i = 10;` → `Integer.valueOf(10)` 호출됨

### 래퍼 클래스 캐시 규칙

**Boolean:**

- `Boolean.valueOf(true)` → `Boolean.TRUE`
- `Boolean.valueOf(false)` → `Boolean.FALSE`

**Integer:**

- `Integer.valueOf(-128 ~ 127)` → 캐시된 싱글톤 객체 반환
- 범위 밖(128 이상 등)은 새 객체 생성

**new 키워드로 생성:**

- 무조건 힙에 새로운 객체 생성
- 참조가 끊기면 GC 대상

### 예시

```java
Integer i1 = 100;                 // 캐시 객체
Integer i2 = 100;                 // 캐시 객체 (i1 == i2 → true)
Integer i3 = 200;                 // 새 객체
Integer i4 = 200;                 // 새 객체 (i3 == i4 → false)
Integer i5 = new Integer(100);    // 새 객체
Integer i6 = new Integer(100);    // 새 객체 (i5 == i6 → false)
Boolean b1 = Boolean.valueOf(true);   // Boolean.TRUE
Boolean b2 = new Boolean(true);       // 새 객체 (b1 != b2)

System.out.

println(i1 ==i2); // true
System.out.

println(i3 ==i4); // false
System.out.

println(i5 ==i6); // false
System.out.

println(b1 ==b2); // false
```

### 정리:

- 오토박싱 또는 `valueOf(캐시범위 내)` → 힙 낭비 없이 싱글톤 객체 재사용
- `new`로 인스턴스 생성 시 → 항상 새 객체, GC 대상

---

## 5. static 변수와 메서드 영역, 생명주기

**static 변수 = 클래스 로드 시 메서드 영역(메타스페이스)에 저장**  
→ 클래스 언로드 전까지 유지됨  
→ GC 대상이 아님 (클래스 언로드 시 해제)

---

### 톰캣 등 웹앱 환경에서 static 변수와 클래스 언로드

톰캣 같은 WAS 환경에서는 각 웹앱이 독립적인 ClassLoader(WebAppClassLoader)로 로딩됨

**클래스 언로드 조건:**

- 해당 클래스의 모든 인스턴스가 참조 해제되어야 함
- 해당 클래스 자체가 참조되지 않아야 함
- 클래스가 로드된 ClassLoader도 GC 대상이어야 함

**하지만 톰캣이 살아있는 한 WebAppClassLoader는 살아 있음**

→ static 변수도 메모리에 계속 남아 있음  
→ static이 참조하는 객체 역시 GC 대상이 아님

---

### 따라서 장시간 운영, 무중단 배포 등에서 주의

- static 변수에 캐시나 큰 객체를 저장하면 메모리 누수 발생 가능
- 특히, static 변수로 트리 구조 같은 복잡한 객체를 유지하면
  → 메모리 사용량이 계속 증가할 수 있음

---

### 실무 주의사항 및 대안

| 피해야 할 static 사용         | 대안                                  |
|-------------------------|-------------------------------------|
| 대량의 List, Map, 트리 구조 캐싱 | Caffeine, Redis 같은 외부 캐시 사용         |
| 대형 설정파일, XML DOM 등      | 파일 I/O 또는 약한 참조(WeakReference) 활용   |
| 빈 객체 재사용(커넥션, 핸들러 등)    | Object Pool, ThreadLocal 또는 DI 빈 활용 |

---

## 6. 매직 상수의 대안 - static final

### 매직 상수란?

- 코드에 숫자나 문자열이 하드코딩된 값
- 예: `"admin"`, `5000`

### static final 상수를 쓰면?

- 코드 가독성 및 유지보수성 향상
- 변경 용이, 오타 방지
- **런타임 상수 풀에 저장 → GC 대상 아님**
- 컴파일 시 상수화 되어 불필요한 메모리 낭비 방지

---

### 예시

```java
public class Constants {
    public static final String SYSTEM_ADMIN = "admin";
    public static final int DEFAULT_TIMEOUT = 5000;
}
```

> **주의:**  
> static final 상수는 변경 불가능하고, 메모리 부담도 거의 없다.  
> → 매직 상수 대안으로 적극 권장됨

---

## 7. GC(Garbage Collection)란 무엇인가?

- **힙에 생성된 참조가 끊긴 객체를 JVM이 자동으로 정리하는 기능**
- 스택, 메서드 영역, 상수 풀에 있는 데이터는 GC 대상이 아님

---

## 8. 실무 결론 및 주의사항

- **기본형 리터럴, 문자열 리터럴, static final 상수는 GC 대상이 아님**
- 오토박싱된 Boolean, Integer(-128~127)은 캐시 객체 → GC 대상 아님
- `new`로 생성한 객체만 자주 GC 대상
- static 변수는 클래스 언로드 전까지 메모리에 살아 있음

- 톰캣 같은 웹 환경에서 static 변수 사용 시 유의점
    - 톰캣이 내려가지 않으면 static이 참조하는 객체는 GC 되지 않음
    - static 변수로 대량 캐시, 외부 자원 저장 시 **메모리 누수 위험**
    - 무중단 배포, 장시간 운영 시 **메모리 누적 문제 주의**

---

### 캐시가 필요하면?

- Caffeine, Redis 같은 **외부 캐시 시스템 사용 권장**

---

## 실무 팁 요약

| 항목        | 권장/주의 사항                      |
|-----------|-------------------------------|
| static 변수 | 대형 객체, 외부 리소스 저장 지양           |
| 매직 상수     | `static final`로 선언해 상수화       |
| 오토박싱      | 캐싱 범위 숙지 (Integer -128~127 등) |
| GC 대상     | 힙에 생성되고 참조 끊긴 객체만 GC 대상       |

---

## 참고자료

- [Java Virtual Machine Specification](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html)
- *Effective Java, 3rd Edition* — Joshua Bloch
- 톰캣 ClassLoader 및 메모리 누수 관련 글들



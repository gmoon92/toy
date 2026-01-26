# Gradle Lombok 설정 가이드

## 목차
1. [Lombok이란?](#lombok이란)
2. [Lombok 동작 원리](#lombok-동작-원리)
3. [Gradle 의존성 설정](#gradle-의존성-설정)
4. [compileOnly vs implementation](#compileonly-vs-implementation)
5. [실전 예제](#실전-예제)

---

## Lombok이란?

Lombok은 자바 개발자의 반복적인 코드 작성을 줄여주는 라이브러리입니다.

### Lombok 없이 작성한 코드

```java
public class User {
    private String username;
    private String email;

    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{username='" + username + "', email='" + email + "'}";
    }
}
```

### Lombok을 사용한 코드

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    private String username;
    private String email;
}
```

**같은 기능을 10줄로 압축!**

---

## Lombok 동작 원리

### 1단계: 소스 코드 작성
```java
@Getter
@Setter
public class User {
    private String username;
}
```

### 2단계: 컴파일 타임 (APT 실행)

Gradle/Maven이 컴파일할 때 **Annotation Processing Tool (APT)**가 실행됩니다.

```
javac 컴파일 시작
    ↓
Lombok annotationProcessor 실행
    ↓
@Getter, @Setter 어노테이션 발견
    ↓
AST (Abstract Syntax Tree) 조작
    ↓
바이트코드에 실제 메소드 추가
```

### 3단계: 생성된 바이트코드 (.class 파일)

```java
// User.class 파일의 실제 내용
public class User {
    private String username;

    // Lombok이 자동으로 생성한 메소드
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
```

### 4단계: 런타임

`.class` 파일에 이미 **완성된 getter/setter 메소드가 포함**되어 있으므로, 런타임에 Lombok 라이브러리가 필요하지 않습니다.

---

## Gradle 의존성 설정

### 핵심 개념

Lombok은 **컴파일 타임에만 필요**하고, **런타임에는 필요 없습니다**.

### 권장 설정 (compileOnly)

```kotlin
dependencies {
    // 컴파일 시 Lombok 클래스 참조 가능
    compileOnly("org.projectlombok:lombok")

    // 컴파일 시 Annotation Processing 실행
    annotationProcessor("org.projectlombok:lombok")

    // 테스트 코드에서도 Lombok 사용
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}
```

### 동작하지만 비권장 설정 (implementation)

```kotlin
dependencies {
    // 런타임에도 Lombok 포함 (불필요)
    implementation("org.projectlombok:lombok")

    annotationProcessor("org.projectlombok:lombok")
}
```

---

## compileOnly vs implementation

### compileOnly (권장)

```
컴파일 타임:
  ✓ Lombok 사용 가능
  ✓ APT로 코드 생성

런타임:
  ✗ Lombok 제외됨
  ✓ 생성된 코드만 .class에 포함
  ✓ JAR 크기 감소 (~2MB)
```

**최종 JAR 구조**:
```
my-app.jar
├── com/example/User.class  (getter/setter 포함)
└── (lombok.jar 제외됨)
```

### implementation (비권장)

```
컴파일 타임:
  ✓ Lombok 사용 가능
  ✓ APT로 코드 생성

런타임:
  ⚠ Lombok도 포함됨 (불필요)
  ✓ 생성된 코드도 .class에 포함
  ⚠ JAR 크기 증가
```

**최종 JAR 구조**:
```
my-app.jar
├── com/example/User.class  (getter/setter 포함)
└── lombok/                 (불필요하게 포함됨)
```

### 비교 표

| 항목 | compileOnly | implementation |
|------|-------------|----------------|
| 컴파일 타임 사용 | ✅ | ✅ |
| 런타임 포함 | ❌ (권장) | ✅ (불필요) |
| JAR 크기 | 작음 | 큼 (+2MB) |
| 동작 여부 | ✅ | ✅ |
| Best Practice | ✅ | ❌ |

---

## 실전 예제

### 단일 프로젝트

```kotlin
// build.gradle.kts
plugins {
    java
    id("org.springframework.boot") version "3.2.0"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Lombok 설정
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}
```

### 멀티 모듈 프로젝트

```kotlin
// 루트 build.gradle.kts
subprojects {
    apply(plugin = "java")

    dependencies {
        // 모든 하위 모듈에 Lombok 적용
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
    }
}
```

### Common 라이브러리 모듈

```kotlin
// common 모듈 build.gradle.kts
plugins {
    java
    `java-library`  // 다른 모듈에서 사용할 라이브러리
}

tasks {
    named<BootJar>("bootJar") {
        enabled = false  // 실행 가능한 JAR 생성 안 함
    }

    named<Jar>("jar") {
        enabled = true   // 라이브러리 JAR 생성
    }
}

dependencies {
    // Lombok으로 생성된 getter/setter는 .class에 포함됨
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
```

**주의**: Common 모듈을 JAR로 배포할 때, Lombok으로 생성된 코드는 이미 `.class` 파일에 포함되어 있으므로, 의존하는 프로젝트에서 Lombok이 없어도 정상 동작합니다.

---

## 자주 묻는 질문 (FAQ)

### Q1. annotationProcessor만 설정하면 안 되나요?

```kotlin
// ❌ 컴파일 에러 발생
dependencies {
    annotationProcessor("org.projectlombok:lombok")
}
```

**에러 메시지**:
```
error: package lombok does not exist
import lombok.Getter;
```

APT는 실행되지만, 컴파일러가 Lombok 어노테이션을 찾을 수 없어서 에러가 발생합니다.

### Q2. implementation만 설정하면 어떻게 되나요?

```kotlin
// ⚠ 코드 생성이 안 됨
dependencies {
    implementation("org.projectlombok:lombok")
}
```

컴파일은 되지만 getter/setter가 생성되지 않아, 런타임에 `NoSuchMethodError`가 발생할 수 있습니다.

### Q3. 왜 런타임에 Lombok이 필요 없나요?

Lombok 어노테이션은 `@Retention(SOURCE)`로 설정되어 있습니다.

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)  // 소스 코드에만 존재
public @interface Getter {
}
```

- **SOURCE**: 소스 파일에만 존재, `.class`에 포함 안 됨
- **CLASS**: `.class` 파일에 포함, 런타임에 로드 안 됨
- **RUNTIME**: 런타임에 리플렉션으로 접근 가능

Lombok은 SOURCE이므로 컴파일 후 사라지고, 생성된 메소드만 바이트코드에 남습니다.

### Q4. IntelliJ에서 Lombok 설정은?

1. **Lombok 플러그인 설치**
   - `Preferences` → `Plugins` → "Lombok" 검색 및 설치

2. **Annotation Processing 활성화**
   - `Preferences` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
   - ✅ "Enable annotation processing" 체크

---

## 정리

### 핵심 개념
1. Lombok은 **컴파일 타임 코드 생성 라이브러리**
2. **APT**가 어노테이션을 읽고 바이트코드에 메소드 추가
3. 런타임에는 Lombok 불필요 (생성된 코드가 이미 `.class`에 포함)

### 권장 설정
```kotlin
dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
```

### 체크리스트
- [ ] `compileOnly` 또는 `implementation` 추가
- [ ] `annotationProcessor` 추가 (필수)
- [ ] 테스트에도 동일하게 설정
- [ ] IntelliJ Lombok 플러그인 설치
- [ ] Annotation Processing 활성화

---

## 참고 자료
- [Lombok 공식 문서](https://projectlombok.org/)
- [Gradle Annotation Processor](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_compile_avoidance)
- [JSR 269: Pluggable Annotation Processing API](https://jcp.org/en/jsr/detail?id=269)

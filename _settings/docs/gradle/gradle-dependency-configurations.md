# Gradle 의존성 설정(Dependency Configuration) 완벽 가이드

## 목차

1. [개요](#개요)
2. [전체 비교표](#전체-비교표)
3. [각 설정 상세 설명](#각-설정-상세-설명)
4. [전이 의존성(Transitive Dependency)](#전이-의존성transitive-dependency)
5. [실전 사용 시나리오](#실전-사용-시나리오)
6. [멀티 모듈에서의 의존성 설정](#멀티-모듈에서의-의존성-설정)

---

## 개요

Gradle의 의존성 설정은 **언제 사용할지**(컴파일/런타임)와 **누구에게 노출할지**(전이 의존성)를 결정합니다.

### 핵심 개념

```
의존성 설정 = 시점(컴파일/런타임) + 가시성(노출 여부)
```

---

## 전체 비교표

### 주요 설정 비교

| 설정                    | 컴파일 시 사용 | 런타임 시 포함 | 전이 의존성 노출 | 주요 사용 사례                    |
|-----------------------|:--------:|:--------:|:---------:|-----------------------------|
| `implementation`      |    ✅     |    ✅     |     ❌     | 일반적인 라이브러리 (기본 선택)          |
| `api`                 |    ✅     |    ✅     |     ✅     | 공개 API에 노출되는 라이브러리          |
| `compileOnly`         |    ✅     |    ❌     |     ❌     | Lombok, JSR305 어노테이션        |
| `runtimeOnly`         |    ❌     |    ✅     |     ❌     | JDBC 드라이버, SLF4J 구현체        |
| `annotationProcessor` |  APT 전용  |    ❌     |     ❌     | Lombok, QueryDSL, MapStruct |

### 테스트 전용 설정

| 설정                   | 컴파일 시 사용 | 런타임 시 포함 | 전이 의존성 노출 | 주요 사용 사례                |
|----------------------|:--------:|:--------:|:---------:|-------------------------|
| `testImplementation` |    ✅     |    ✅     |     ❌     | JUnit, Mockito, AssertJ |
| `testCompileOnly`    |    ✅     |    ❌     |     ❌     | 테스트용 어노테이션              |
| `testRuntimeOnly`    |    ❌     |    ✅     |     ❌     | JUnit Platform Engine   |

### 상세 비교표

| 설정                    | 소스 코드에서 사용 | .class에 포함 | 최종 JAR에 포함 | 의존하는 모듈에서 사용 가능 |
|-----------------------|:----------:|:----------:|:----------:|:---------------:|
| `implementation`      |     ✅      |     ✅      |     ✅      |  ❌ (직접 선언 필요)   |
| `api`                 |     ✅      |     ✅      |     ✅      |    ✅ (자동 전파)    |
| `compileOnly`         |     ✅      |     ✅      |     ❌      |        ❌        |
| `runtimeOnly`         |     ❌      |     ❌      |     ✅      |        ❌        |
| `annotationProcessor` |    APT     | APT 생성 코드만 |     ❌      |        ❌        |

---

## 각 설정 상세 설명

### 1. implementation (가장 많이 사용)

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
}
```

#### 특징

- **컴파일 시**: 사용 가능
- **런타임 시**: JAR에 포함
- **전이 의존성**: 노출되지 않음 (캡슐화)

#### 동작 방식

```
[모듈 A] implementation("library-x")
    ↓
[모듈 B] implementation(project(":module-a"))
    ↓
모듈 B는 library-x를 직접 사용할 수 없음
```

#### 사용 시나리오

- 내부 구현에만 사용하는 라이브러리
- 대부분의 일반적인 의존성
- 빠른 빌드를 위한 기본 선택

#### 예시

```kotlin
// ✅ 올바른 사용
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework:spring-context")
}
```

**장점**:

- 빌드 속도 향상 (의존성 변경 시 일부 모듈만 재컴파일)
- 의존성 충돌 감소

---

### 2. api (라이브러리 개발용)

```kotlin
dependencies {
    api("com.google.guava:guava")
}
```

#### 특징

- **컴파일 시**: 사용 가능
- **런타임 시**: JAR에 포함
- **전이 의존성**: 노출됨 (공개 API)

#### 동작 방식

```
[모듈 A] api("library-x")
    ↓
[모듈 B] implementation(project(":module-a"))
    ↓
모듈 B는 library-x를 자동으로 사용 가능
```

#### 사용 시나리오

- 공개 API의 반환 타입이나 파라미터에 사용되는 라이브러리
- 멀티 모듈에서 공통 라이브러리 배포
- 상위 모듈에서 사용해야 하는 라이브러리

#### 예시

```kotlin
// common 모듈
dependencies {
    api("org.springframework.data:spring-data-jpa")
}

// 모듈 A의 공개 API
class UserRepository : JpaRepository<User, Long> {
    //                    ^^^^^^^ JpaRepository가 공개 API에 노출
}

// 모듈 B
dependencies {
    implementation(project(":common"))
    // JpaRepository를 자동으로 사용 가능
}
```

**주의**: `api`를 과도하게 사용하면 빌드 시간이 증가합니다.

---

### 3. compileOnly (컴파일 전용)

```kotlin
dependencies {
    compileOnly("org.projectlombok:lombok")
}
```

#### 특징

- **컴파일 시**: 사용 가능
- **런타임 시**: JAR에 포함 **안 됨**
- **전이 의존성**: 노출되지 않음

#### 동작 방식

```
컴파일 시:
  Lombok 라이브러리 사용 가능
  @Getter 등 어노테이션 인식
    ↓
런타임 시:
  Lombok 제외됨
  생성된 getter 메소드만 .class에 포함
```

#### 사용 시나리오

- **컴파일 타임 코드 생성**: Lombok
- **어노테이션 라이브러리**: JSR305, SpotBugs annotations
- **컨테이너가 제공하는 라이브러리**: Servlet API (Tomcat이 제공)
- **선택적 의존성**: 있으면 사용하고 없어도 동작

#### 예시

```kotlin
dependencies {
    // Lombok: 컴파일 시 코드 생성, 런타임 불필요
    compileOnly("org.projectlombok:lombok")

    // JSR305: @Nullable, @Nonnull 어노테이션 (컴파일 체크용)
    compileOnly("com.google.code.findbugs:jsr305")

    // Servlet API: WAS가 제공
    compileOnly("javax.servlet:javax.servlet-api")
}
```

---

### 4. runtimeOnly (런타임 전용)

```kotlin
dependencies {
    runtimeOnly("com.mysql:mysql-connector-j")
}
```

#### 특징

- **컴파일 시**: 사용 **불가**
- **런타임 시**: JAR에 포함
- **전이 의존성**: 노출되지 않음

#### 동작 방식

```
컴파일 시:
  소스 코드에서 직접 참조 불가
  import 불가
    ↓
런타임 시:
  JAR에 포함됨
  리플렉션, SPI로 로드
```

#### 사용 시나리오

- **JDBC 드라이버**: MySQL, PostgreSQL, H2
- **로깅 구현체**: Logback, Log4j2 (SLF4J 사용 시)
- **SPI 구현체**: Jackson modules
- **런타임 바인딩**: 동적으로 로드되는 라이브러리

#### 예시

```kotlin
dependencies {
    // 인터페이스만 컴파일에 사용
    implementation("org.slf4j:slf4j-api")

    // 구현체는 런타임에만 필요
    runtimeOnly("ch.qos.logback:logback-classic")
}
```

```kotlin
dependencies {
    // JDBC API는 컴파일에 사용
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // 드라이버는 런타임에 필요
    runtimeOnly("com.mysql:mysql-connector-j")
}
```

---

### 5. annotationProcessor (APT 전용)

```kotlin
dependencies {
    annotationProcessor("org.projectlombok:lombok")
}
```

#### 특징

- **컴파일 시**: APT(Annotation Processing Tool) 실행
- **런타임 시**: JAR에 포함 **안 됨**
- **소스 코드 생성**: 가능

#### 동작 방식

```
javac 컴파일
    ↓
annotationProcessor 실행
    ↓
어노테이션 발견 (@Getter, @Entity 등)
    ↓
소스 코드 또는 메타데이터 생성
    ↓
생성된 코드 컴파일
    ↓
.class 파일에 포함
```

#### 사용 시나리오

- **Lombok**: getter/setter 생성
- **QueryDSL**: Q-type 클래스 생성
- **MapStruct**: 매퍼 구현체 생성
- **JPA**: 정적 메타모델 생성

#### 예시

```kotlin
dependencies {
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
}
```

---

### 6. testImplementation (테스트 전용)

```kotlin
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}
```

#### 특징

- **테스트 코드에서만** 사용 가능
- 메인 코드 컴파일에는 영향 없음
- 테스트 실행 시 classpath에 포함

#### 사용 시나리오

- JUnit, TestNG
- Mockito, MockK
- AssertJ, Hamcrest
- Spring Test

---

## 전이 의존성(Transitive Dependency)

### 개념

```
A ─ implementation ─> B ─ implementation ─> C

A에서 C를 사용할 수 있을까? ❌
```

```
A ─ implementation ─> B ─ api ─> C

A에서 C를 사용할 수 있을까? ✅
```

### implementation vs api 차이

#### implementation 사용 시

```kotlin
// module-common
dependencies {
    implementation("com.google.guava:guava")
}

// module-service
dependencies {
    implementation(project(":module-common"))
    // Guava를 사용하려면 직접 선언 필요
    implementation("com.google.guava:guava")  // 중복 선언
}
```

#### api 사용 시

```kotlin
// module-common
dependencies {
    api("com.google.guava:guava")
}

// module-service
dependencies {
    implementation(project(":module-common"))
    // Guava 자동으로 사용 가능 (전이 의존성)
}
```

### 빌드 성능 영향

| 변경 사항     | implementation | api             |
|-----------|----------------|-----------------|
| 의존성 버전 변경 | 해당 모듈만 재컴파일    | 의존하는 모든 모듈 재컴파일 |
| 빌드 속도     | 빠름 ⚡           | 느림 🐢           |
| 권장 사용     | 내부 구현          | 공개 API          |

---

## 실전 사용 시나리오

### 시나리오 1: Spring Boot 웹 애플리케이션

```kotlin
dependencies {
    // 웹 프레임워크
    implementation("org.springframework.boot:spring-boot-starter-web")

    // JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("com.h2database:h2")
}
```

### 시나리오 2: 공통 라이브러리 모듈

```kotlin
// common 모듈 (다른 모듈에서 사용)
dependencies {
    // 공개 API에 사용되는 라이브러리
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.springframework.data:spring-data-commons")

    // 내부 구현에만 사용
    implementation("org.apache.commons:commons-lang3")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

tasks {
    bootJar { enabled = false }
    jar { enabled = true }
}
```

### 시나리오 3: QueryDSL 설정

```kotlin
dependencies {
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Lombok과 함께 사용
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
```

**중요**: annotationProcessor는 순서가 중요할 수 있습니다.

### 시나리오 4: 멀티 로깅 환경

```kotlin
dependencies {
    // 로깅 인터페이스 (컴파일 시 사용)
    implementation("org.slf4j:slf4j-api")

    // 구현체 (런타임에만 필요)
    runtimeOnly("ch.qos.logback:logback-classic")

    // 다른 로깅 프레임워크를 SLF4J로 브릿지
    runtimeOnly("org.slf4j:jul-to-slf4j")      // java.util.logging
    runtimeOnly("org.slf4j:log4j-over-slf4j")  // Log4j
}
```

### 시나리오 5: 선택적 기능

```kotlin
dependencies {
    // 메인 라이브러리
    implementation("org.springframework.boot:spring-boot-starter")

    // 선택적 기능 (있으면 활성화, 없어도 동작)
    compileOnly("org.springframework.boot:spring-boot-starter-mail")

    // 개발 도구 (프로덕션에서는 제외)
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}
```

---

## 멀티 모듈에서의 의존성 설정

### 모듈 구조

```
root/
├── common/         (공통 유틸리티)
├── domain/         (도메인 모델)
├── api/            (REST API)
└── batch/          (배치 작업)
```

### common 모듈

```kotlin
// 다른 모듈에서 사용할 공통 라이브러리
dependencies {
    // 공개 API: 도메인 모델에 사용
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("jakarta.validation:jakarta.validation-api")

    // 내부 유틸리티
    implementation("org.apache.commons:commons-lang3")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

tasks {
    bootJar { enabled = false }
    jar { enabled = true }
}
```

### domain 모듈

```kotlin
dependencies {
    // common 모듈 사용
    implementation(project(":common"))

    // JPA
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // QueryDSL (내부 사용)
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

tasks {
    bootJar { enabled = false }
    jar { enabled = true }
}
```

### api 모듈

```kotlin
dependencies {
    // 하위 모듈 사용
    implementation(project(":common"))
    implementation(project(":domain"))

    // 웹
    implementation("org.springframework.boot:spring-boot-starter-web")

    // DB 드라이버 (런타임에만)
    runtimeOnly("com.mysql:mysql-connector-j")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.rest-assured:rest-assured")
}
```

---

## 의사결정 플로우차트

```
의존성을 추가해야 할 때
    |
    ├─ 테스트에서만 사용? ─ YES ─> testImplementation
    |                           (JUnit, Mockito)
    |
    ├─ 컴파일 시에만 필요? ─ YES ─> compileOnly + annotationProcessor
    |                           (Lombok, JSR305)
    |
    ├─ 런타임에만 필요? ─ YES ─> runtimeOnly
    |                        (JDBC 드라이버, Logback)
    |
    ├─ 공개 API에 노출? ─ YES ─> api
    |                         (반환 타입, 파라미터 타입)
    |
    └─ 그 외 모든 경우 ──────> implementation (기본 선택)
```

---

## 자주하는 실수

### 1. ❌ 모든 것을 api로 선언

```kotlin
// 잘못된 예
dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("org.apache.commons:commons-lang3")
}
```

**문제**: 빌드 시간 증가, 의존성 충돌 위험

**올바른 방법**:

```kotlin
dependencies {
    // 공개 API에만 api 사용
    api("com.fasterxml.jackson.core:jackson-annotations")

    // 나머지는 implementation
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.apache.commons:commons-lang3")
}
```

### 2. ❌ compileOnly만 선언하고 annotationProcessor 누락

```kotlin
// 잘못된 예: getter/setter 생성 안 됨
dependencies {
    compileOnly("org.projectlombok:lombok")
}
```

**올바른 방법**:

```kotlin
dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")  // 필수!
}
```

### 3. ❌ JDBC 드라이버를 implementation으로

```kotlin
// 비효율적
dependencies {
    implementation("com.mysql:mysql-connector-j")
}
```

**올바른 방법**:

```kotlin
dependencies {
    runtimeOnly("com.mysql:mysql-connector-j")
}
```

### 4. ❌ 전이 의존성 오해

```kotlin
// module-a
dependencies {
    implementation("com.google.guava:guava")  // implementation
}

// module-b
dependencies {
    implementation(project(":module-a"))
    // ❌ Guava 사용 불가! 직접 선언 필요
}
```

---

## 정리

### 선택 가이드

| 상황              | 선택                                    |
|-----------------|---------------------------------------|
| 일반적인 라이브러리      | `implementation`                      |
| 공개 API 타입       | `api`                                 |
| Lombok, 어노테이션   | `compileOnly` + `annotationProcessor` |
| DB 드라이버, 로깅 구현체 | `runtimeOnly`                         |
| 테스트 라이브러리       | `testImplementation`                  |

### 핵심 원칙

1. **기본은 implementation**: 의심스러우면 `implementation` 사용
2. **api는 최소화**: 꼭 필요한 경우만 사용
3. **compileOnly + annotationProcessor**: 항상 함께 사용
4. **runtimeOnly 활용**: 컴파일에 불필요하면 runtimeOnly

### 빌드 최적화

```kotlin
// ❌ 느린 빌드
api("library-a")
api("library-b")
api("library-c")

// ✅ 빠른 빌드
api("library-a")              // 공개 API에만
implementation("library-b")   // 대부분
implementation("library-c")
```

---

## 참고 자료

- [Gradle Dependency Management](https://docs.gradle.org/current/userguide/dependency_management.html)
- [Java Plugin Configurations](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_plugin_and_dependency_management)
- [Gradle Java Library Plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html)

# Gradle 프로퍼티 치환 트러블슈팅

## 문제 상황

Spring Boot application.yml 설정 파일에서 Gradle 빌드 시점의 변수를 참조하고 싶었으나, 프로퍼티가 정의되지 않은 경우 빌드가 실패하는 문제가 발생했습니다.

### 초기 요구사항

Maven의 리소스 필터링처럼 `@property@` 형태로 빌드 시점에 Gradle 변수를 Spring 설정 파일에 주입하고 싶었습니다.

```yaml
# application.yml
spring:
  application:
    name: @applicationName@

  cloud:
    config:
      server:
        git:
          uri: file://@projectDir@/config-repo
```

## 발생한 에러

### 1. 프로퍼티 미정의 에러

`spring.application.name` 같은 필수 프로퍼티가 Gradle에 정의되지 않으면 빌드 시 치환되지 않아 애플리케이션 실행이 실패했습니다.

```
Error: Required property 'spring.application.name' is not defined
```

### 2. Kotlin DSL 문법 에러

초기에 Groovy 스타일로 `ReplaceTokens` 필터를 작성하여 컴파일 에러가 발생했습니다.

```kotlin
// ❌ 잘못된 시도 1: Groovy 문법
filesMatching("**/application.yml") {
    filter(
        ReplaceTokens,
        tokens:["project.version": version],
        beginToken: '@',
        endToken: '@'
    )
}
```

**에러 메시지:**

```
Expecting an element
```

### 3. 제네릭 타입 파라미터 에러

reified 타입 파라미터 방식으로 시도했으나 에러가 발생했습니다.

```kotlin
// ❌ 잘못된 시도 2: reified 방식
filesMatching("**/application*.yml") {
    filter<ReplaceTokens>(
        "tokens" to tokens,
        "beginToken" to "@",
        "endToken" to "@"
    )
}
```

**에러 메시지:**

```
No type arguments expected for inline fun ContentFilterable.filter(
    filterType: KClass<out FilterReader>,
    vararg properties: Pair<String, Any?>
): ContentFilterable
```

## 원인 분석

### 1. 프로퍼티 미정의 문제

Gradle의 `project.properties`는 기본적으로 프로젝트 기본 속성만 포함합니다. <br/>
사용자 정의 프로퍼티는 명시적으로 설정하지 않으면 존재하지 않아, YAML에서 `@propertyName@`을 치환할 수 없습니다.

### 2. Kotlin DSL vs Groovy 문법 차이

Gradle Kotlin DSL은 Groovy와 다른 문법을 사용합니다:

| Groovy                 | Kotlin DSL                          |
|------------------------|-------------------------------------|
| `tokens: [key: value]` | `"tokens" to mapOf("key" to value)` |
| `beginToken: '@'`      | `"beginToken" to "@"`               |
| 명명된 인자                 | Pair 형태 `"key" to value`            |

### 3. inline 함수와 타입 파라미터 전달

Gradle의 `filter()` 함수는 다음과 같은 시그니처를 가집니다:

```kotlin
inline fun ContentFilterable.filter(
    filterType: KClass<out FilterReader>,  // KClass를 명시적으로 받음
    vararg properties: Pair<String, Any?>
): ContentFilterable
```

#### reified가 아닌 이유

Gradle은 Java/Groovy와의 호환성을 유지해야 하므로 `reified`(Kotlin 전용 기능) 대신 `KClass`를 사용합니다.

#### 타입 전달 방식 비교

```kotlin
// ❌ reified 방식 (지원 안 함)
filter<ReplaceTokens>(...)

// ✅ KClass 명시적 전달
filter(ReplaceTokens::class, ...)
```

## 해결 방법

### 1. 기본값 정의로 프로퍼티 누락 방지

```kotlin
import org.apache.tools.ant.filters.ReplaceTokens

project(":spring-cloud:spring-cloud-bus:spring-cloud-bus-server") {
    description = "spring-cloud-bus-server"

    tasks {
        withType<ProcessResources> {
            // 기본값 정의
            val defaultProperties = mapOf(
                "projectDir" to project.projectDir.absolutePath,
                "rootDir" to project.rootDir.absolutePath,
                "applicationName" to "config-server",
                "project.version" to version.toString()
            )

            // project.properties와 병합 (project.properties가 우선)
            val tokens = defaultProperties + project.properties.mapValues { it.value.toString() }

            filesMatching("**/application*.yml") {
                filter(
                    ReplaceTokens::class,
                    "tokens" to tokens,
                    "beginToken" to "@",
                    "endToken" to "@"
                )
            }
        }
    }
}
```

### 2. application.yml에서 사용

```yaml
spring:
  application:
    name: @applicationName@

  cloud:
    config:
      server:
        git:
          uri: file://@projectDir@/config-repo

# 빌드 후 실제 값으로 치환됨
# spring:
#   application:
#     name: config-server
#   cloud:
#     config:
#       server:
#         git:
#           uri: file:///Users/moongyeom/IdeaProjects/private/toy/spring-cloud/spring-cloud-bus/spring-cloud-bus-server/config-repo
```

## Kotlin DSL 핵심 개념

### inline 함수

함수 호출 오버헤드를 제거하기 위해 **함수 본문을 호출 위치에 복사**합니다.

```kotlin
inline fun inlineFunction() {
    println("Hello")
}

fun main() {
    inlineFunction()  // 본문이 직접 복사됨
    // 컴파일 후: println("Hello")
}
```

### reified 타입 파라미터

`inline` + `reified`를 사용하면 런타임에도 제네릭 타입 정보를 유지할 수 있습니다.

```kotlin
// 일반 제네릭 (타입 소거됨)
fun <T> isType(value: Any): Boolean {
    return value is T  // ❌ 에러! T의 타입을 모름
}

// reified 제네릭 (타입 유지)
inline fun <reified T> isType(value: Any): Boolean {
    return value is T  // ✅ 가능!
}

fun main() {
    isType<String>("hello")  // true
    isType<Int>(123)         // true
}
```

### KClass 명시적 전달

`reified`는 Kotlin 전용이므로, Java 호환성을 위해 `KClass`를 명시적으로 전달합니다.

```kotlin
// reified 방식 (Kotlin 전용)
inline fun <reified T> doSomething() {
    println(T::class)
}

// KClass 방식 (Java 호환)
fun doSomething(clazz: KClass<*>) {
    println(clazz)
}

doSomething(String::class)  // MyClass::class로 호출
```

## 추가 팁

### 1. 여러 파일 패턴 매칭

```kotlin
filesMatching(listOf("**/application*.yml", "**/application*.yaml")) {
    filter(ReplaceTokens::class, "tokens" to tokens, "beginToken" to "@", "endToken" to "@")
}
```

### 2. 환경별 프로퍼티 설정

```kotlin
val environment = project.findProperty("env") as String? ?: "dev"
val tokens = mapOf(
    "env" to environment,
    "applicationName" to "config-server-$environment"
)
```

```bash
# 빌드 시 환경 지정
./gradlew build -Penv=prod
```

### 3. gradle.properties 활용

```properties
# gradle.properties
applicationName=my-config-server
customProperty=custom-value
```

```kotlin
// build.gradle.kts
val tokens = project.properties.mapValues { it.value.toString() }
```

### 4. Spring의 ${} 문법과 충돌 방지

`@property@` 방식을 사용하면 Spring의 `${property}` 문법과 충돌하지 않습니다.

```yaml
# 빌드 시점 치환: @property@
spring:
  application:
    name: @applicationName@

  # 런타임 치환: ${property}
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3306/mydb
```

## 참고

- [Gradle ProcessResources Documentation](https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html)
- [Apache Ant ReplaceTokens](https://ant.apache.org/manual/Tasks/replace.html)
- [Kotlin Inline Functions](https://kotlinlang.org/docs/inline-functions.html)
- [Kotlin Reified Type Parameters](https://kotlinlang.org/docs/inline-functions.html#reified-type-parameters)

# Gradle

## libs.versions.toml

`libs.versions.toml`(버전 카탈로그)은 Gradle 7.x 이상부터 공식 지원하는 기능으로,

대규모 프로젝트에서 라이브러리 의존성과 버전을 한 곳에 깔끔하게 모아 관리하고 싶을 때 주로 활용한다.

### 언제 사용하나?

1. 멀티모듈 프로젝트에서 라이브러리 버전과 모듈 간 의존성을 중앙 집중 관리할 때
    - 여러 모듈에서 같은 라이브러리를 공유하는 경우
    - 버전 업그레이드나 변경 시, 모든 모듈을 일일이 수정하지 않고 한 군데서 한 번만 바꾸고 싶을 때
2. 의존성 명칭 충돌이나 버전 불일치 문제를 방지하고 싶을 때
3. 여러 라이브러리가 복잡하게 얽혀있을 때, 의존성 버전을 일괄적으로 통제 가능
4. 빌드 스크립트의 가독성과 유지보수성을 높이고 싶을 때

### 기본 구조 예시
#### root/gradle/libs.versions.toml

```toml
[versions]
java = "21"

[libraries]
kotest-runner-junit5 = { module = "io.kotest:kotest-runner-junit5", version.ref = "kotest" }
```

#### `build.gradle`

```groovy
buildscript {
    ext {
        springBootVersion = libs.versions.springboot.get()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.java.get().toInteger())
    }
}

dependencies {
    implementation libs.kotest.runner.junit5
}

plugins {
    id 'org.springframework.boot' version springBootVersion
}
```

`build.gradle` 안에서는

- 라이브러리 참조는 `libs.xxx`
  - `libs.kotest.runner.junit5`
- 버전만 참조하고 싶다면 `libs.versions.xxx.get()` 으로 사용
  - `libs.versions.java.get()`
  - 버전만 참조할 경우 Java toolchain 설정한다.
  - 플러그인 버전에도 사용 가능 (간접적으로)
    - Gradle 플러그인 DSL에서는 바로 `libs.versions.xxx` 를 못 쓰지만, 이런 식으로 workaround 가능. 
    - 루트 `build.gradle`이나 `settings.gradle`에 명시한다.
    - 다만 **플러그인 버전은 `settings.gradle`의 `pluginManagement` 블록에서 직접 명시**하는 게 보통 더 명확함.

### 조건문에도 사용 가능

```groovy
if (libs.versions.kotest.get() == "5.9.1") {
    println "Kotest 최신 버전 사용 중"
}
```

#### 주의할 점

- `libs.versions.xxx.get()` 는 **문자열(String)** 이라서 `.toInteger()`, `.contains()`, `.startsWith()` 등의 String 메서드 활용 가능.
- `libs.xxx` 는 **라이브러리 coordinates** 를 포함한 객체이다.
  - 반드시 `dependencies` 안에서 `implementation` 등과 함께 사용해야 함
  - 예를 들어 `implementation libs.xxx` 같은 식으로만 사용해야 함.

---

### 다른 버전 관리 방식과 비교

| 방식                   | 특징                            | 사용 권장 상황                  |
|----------------------|-------------------------------|---------------------------|
| `gradle.properties`  | 간단한 키=값 프로퍼티, 모든 Gradle 버전 지원 | 소규모, 단일 모듈 프로젝트           |
| `ext` 프로퍼티           | Groovy DSL 내에서 선언, 유연함        | 공통 설정을 루트 및 하위 모듈에 나눠 쓸 때 |
| `libs.versions.toml` | 공식 버전 카탈로그, 타입 세이프, IDE 지원    | 대규모 멀티모듈, 복잡한 의존성 관리      |

- `build.gradle` 파일 안에서 버전 숫자나 모듈명을 직접 쓰지 않고, 의미있는 이름(`libs.spring.boot`)으로 관리 가능
- 반면에, 단일 모듈, 또는 작은 프로젝트에서는 `gradle.properties`나 루트 `build.gradle`에 변수로 버전 관리하는 게 더 간단하고 빠름
- 버전 카탈로그가 아직 익숙하지 않거나, Gradle 버전이 낮으면 못 씀


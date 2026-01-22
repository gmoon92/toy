# Spring 3.x에서 QueryDSL 설정 방법

[`com.ewerk.gradle.plugins.querydsl`](https://plugins.gradle.org/plugin/com.ewerk.gradle.plugins.querydsl) 플러그인은 Gradle 5+ 버전 호환성 이슈로 권장하지 않는다.

## Spring 2.x에서는 어떻게 사용하나

Spring Boot 2.x에서는 Gradle 5 환경에서도 아래와 같이 QueryDSL 전용 플러그인을 통해 설정이 간단하게 가능했다.

이 경우, 별도로 `annotationProcessor` 경로를 지정해주면 정상적으로 `QClass`를 생성할 수 있었다.

### with Spring Boot 2.x

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.7.0'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'com.ewerk.gradle.plugins.querydsl' version '1.0.10'
}

def querydslDir = "$buildDir/generated/querydsl"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}

sourceSets {
    main.java.srcDir querydslDir
}

configurations {
    querydsl.extendsFrom compileClasspath
}

compileQuerydsl {
    options.annotationProcessorPath = configurations.querydsl
}
```

- `compileQuerydsl` Task가 자동으로 생성되며, 원하는 디렉토리에 `QClass`를 생성해준다.
- `querydslSourcesDir`로 지정한 위치를 소스셋에 등록해야 한다.

---

## Spring Boot 3.x에서는 상황이 완전히 다르다

Spring Boot 3.x부터는 다음과 같은 구조적 변경으로 인해 위 플러그인을 사용할 수 없다.

- `com.ewerk.gradle.plugins.querydsl`은 Gradle 7+ 및 Spring Boot 3+와 호환되지 않는다.
- 따라서 `annotationProcessor`만으로 Q클래스를 생성하거나, JavaCompile 설정을 통해 생성 위치를 직접 지정해야 한다.

| Gradle 버전     | 호환 가능한 플러그인 버전 | 상태             |
|---------------|----------------|----------------|
| Gradle 2.1 이하 | ≤ 1.0.7        | Stable         |
| Gradle 3.3 이상 | ≥ 1.0.8        | Stable         |
| Gradle 5.0 이상 | 지원하지 않음        | **INCUBATING** |

> NOTE: "The plugins are currently not compatible to Gradle 5+ and have not been tested with a JDK higher than 1.8. Currently a lot of issues arise related to using the plugins with Gradle 5+."<br/>
자세한 내용은 [EWERK Gradle 플러그인 공식 GitHub](https://github.com/ewerk/gradle-plugins)를 참고하자.

- QueryDSL 5.0.0 이상은 `jakarta.* API를 사용하며, 플러그인이 이를 지원하지 않음.
- Gradle 5~7 이상에서는 `compile`, `runtime` 같은 구식 DSL 제거됨 -> 플러그인 내부와 충돌.
- `QClass`가 생성되는 경로가 변경됨:
  - 기존: `build/generated/querydsl` 
  - 변경 후: `build/generated/sources/annotationProcessor/java/main`
- 위 플러그인은 `QClass` 경로를 소스셋에 자동 등록해주지만, 이제는 이 경로가 달라졌기 때문에 적용되지 않음. 

결과적으로 `QClass` 생성 경로가 기본 소스셋에 포함되지 않기 때문에, 컴파일 및 패키징 시 포함되지 않아 오류가 발생할 수 있다. 따라서 소스셋에 경로를 명시적으로 추가하거나, 기본 위치를 그대로 활용해야 한다.

### with Spring Boot 3.x

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.0'
    id 'io.spring.dependency-management' version '1.1.7'
}

java {
    // 소스/클래스 JDK 파일 버전 지정
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // QueryDSL 설정
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor "com.querydsl:querydsl-apt:5.0.0:jakarta"
    annotationProcessor "jakarta.annotation:jakarta.annotation-api"
    annotationProcessor "jakarta.persistence:jakarta.persistence-api"
}

def querydslSrcDir = "$projectDir/build/generated/querydsl"
tasks.named('clean') {
    // clean 시점에 QClass 디렉토리 삭제
    delete file(querydslSrcDir)
}


// compile 시점에 지정된 QClass 경로로 참조되도록 설정
tasks.withType(JavaCompile).configureEach {
    options.generatedSourceOutputDirectory = file(querydslSrcDir)
}
```

- QueryDSL `QClass` 생성을 `annotationProcessor` 만으로 하거나 직접 Task(혹은 JavaCompile 옵션)으로 설정해야 한다.
  - `annotationProcessor`만 지정하면 Gradle 기본 동작에 따라 `build/generated/sources/annotationProcessor/java/main/` 아래에 `QClass`가 생성된다.
- 만약 커스텀 디렉토리(`build/generated` 등)에 생성하고 싶으면 JavaCompile task 옵션으로 "직접" 설정해야 한다.
  ```groovy
  tasks.withType(JavaCompile) {
      options.generatedSourceOutputDirectory = file(querydslSrcDir)
  }
  ```

## 마무리
### QueryDSL QClass 생성 경로 차이

| Spring Boot 버전 | 생성 경로                                                         | 설명                                                          |
|----------------|---------------------------------------------------------------|-------------------------------------------------------------|
| 2.x            | `build/generated/querydsl` (직접 지정 가능)                         | 플러그인 설정을 통해 명시적으로 경로 지정 가능                                  |
| 3.x            | `build/generated/sources/annotationProcessor/java/main` (기본값) | `annotationProcessor`만 사용할 경우 Gradle 기본 경로 사용됨              |
| 3.x            | `build/generated` (커스텀 지정 가능)                                 | `JavaCompile.options.generatedSourceOutputDirectory`로 설정 가능 |

- **Spring 2.x**에서는 QueryDSL 플러그인을 통해 간단히 설정 가능.
- **Spring 3.x**에서는 해당 플러그인은 비호환으로 인해 사용을 권장하지 않는다. 
  - 직접 `QClass` 생성 위치를 지정하거나 기본 위치를 사용할 것.
- `JavaCompile`의 `generatedSourceOutputDirectory`를 통해 원하는 경로에 QClass 생성 가능.
- `clean` Task에 디렉토리 삭제 설정을 추가해 `QClass` 재생성 문제 방지.

## Reference

- [https://plugins.gradle.org/](https://plugins.gradle.org/plugin/com.ewerk.gradle.plugins.querydsl)

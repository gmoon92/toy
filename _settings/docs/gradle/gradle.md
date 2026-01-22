# Spring Boot + 멀티모듈 + gradle

- Groovy DSL (`build.gradle`)
- Java + Spring Boot + 멀티모듈

---

## Gradle 프로젝트 기본 구성

```
my-project/
├── build.gradle                < 루트 빌드 스크립트
├── settings.gradle             < 프로젝트 설정
├── gradle/                     < Gradle wrapper 폴더
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew                     < 유닉스용 wrapper 실행 파일
├── gradlew.bat                 < 윈도우용 wrapper 실행 파일
└── src/
    ├── main/
    │   ├── java/
    │   └── resources/
    └── test/
        ├── java/
        └── resources/
```

| 항목                       | 설명                                     |
|--------------------------|----------------------------------------|
| `build.gradle`           | 루트 프로젝트의 빌드 설정 파일                      |
| `settings.gradle`        | 프로젝트 이름, 멀티모듈 구성 정보 설정                 |
| `gradlew`, `gradlew.bat` | **Wrapper 실행기** – 로컬에 Gradle 없어도 빌드 가능 |
| `gradle/wrapper/...`     | 어떤 버전의 Gradle을 쓸지 정의                   |
| `src/main/java`          | 애플리케이션 소스 코드                           |
| `src/main/resources`     | 설정 파일, 정적 리소스 등                        |
| `src/test/java`          | 테스트 코드                                 |
| `src/test/resources`     | 테스트용 리소스                               |

- `settings.gradle` + `build.gradle`만으로도 동작 가능
  - `settings.gradle`은 멀티모듈 구성을 정의하므로 없으면 하위 모듈이 인식 안 됨
  - `build.gradle`은 빌드 도구에게 프로젝트 전반 설정을 알려주는 핵심 파일
- **Wrapper 파일** (`gradlew`, `gradle-wrapper.properties` 등)은 선택이지만 
  - **버전 일관성과 빌드 신뢰성 확보에 매우 중요**
  - 팀원/CI/CD 환경 일관성을 위해 실무에선 사실상 필수

## Gradle 멀티모듈 샘플 디렉토리 구조 (Java + Spring Boot, Groovy DSL)

```
my-project/
├── build.gradle             < 루트 빌드 스크립트
├── settings.gradle          < 멀티모듈 설정
├── gradle/                  < Gradle Wrapper 스크립트 디렉토리
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew                  < 유닉스용 wrapper 실행 파일
├── gradlew.bat              < 윈도우용 wrapper 실행 파일
├── common/                  < 공통 모듈 (예: 유틸, 공통 설정 등)
│   └── build.gradle
│   └── src/main/java/...
├── domain/                  < 도메인 모듈 (예: 핵심 로직)
│   └── build.gradle
│   └── src/main/java/...
├── api/                     < API 모듈 (예: Spring Boot App)
│   └── build.gradle
│   └── src/main/java/...
```

---

## 파일 내용 예시

### settings.gradle

```groovy
rootProject.name = 'my-project'
include 'common', 'domain', 'api'
```

### 루트 build.gradle

```groovy
buildscript {
    ext {
        springBootVersion = '3.2.0'
    }
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}"
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    group = 'com.example'
    version = '0.0.1-SNAPSHOT'

    repositories {
        mavenCentral()
    }

    sourceCompatibility = '21'
    targetCompatibility = '21'
}
```

### api/build.gradle

```groovy
apply plugin: 'org.springframework.boot'

dependencies {
    implementation project(':common')
    implementation project(':domain')

    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

### `common/build.gradle` / `domain/build.gradle`

```groovy
dependencies {
    // 예: 공통 유틸, 도메인 의존성
}
```

---

## 참고 사항

- `api` 모듈만 `spring-boot` 플러그인을 적용하고, 나머지 모듈은 일반 `java` 모듈로 둬.
- 나중에 테스트 모듈, 외부 연동용 모듈 등도 추가하면 `include`만 해주면 돼.
- 루트 `build.gradle`에 전역 설정을 넣고, 각 모듈에서는 필요한 라이브러리만 추가하도록 유지하면 깔끔하게 관리 가능.

## Reference

- https://docs.gradle.org/current/userguide/userguide.html
- https://gradle.org/maven-and-gradle/
- https://gradle.org/gradle-and-maven-performance/

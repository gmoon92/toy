# Gradle 멀티 모듈 집합 모듈 설정 패턴

멀티 모듈 프로젝트에서 실제 소스 코드 없이 하위 모듈들을 집합하는 역할만 하는 껍데기 모듈(Aggregation Module)의 설정 패턴을 정리합니다.

## TOC

- [멀티 모듈 구조 예제](#멀티-모듈-구조-예제)
- [모듈 타입 분류](#모듈-타입-분류)
- [5가지 설정 패턴](#5가지-설정-패턴)
- [어떤 패턴을 선택할까?](#어떤-패턴을-선택할까)

---

## 멀티 모듈 구조 예제

본 문서에서는 다음과 같은 가상의 멀티 모듈 구조를 기준으로 설명합니다.

```
root/
├── build.gradle.kts                (루트 빌드 설정)
├── settings.gradle                 (모듈 선언)
│
└── platform/                       (집합 모듈 - 레벨 1)
    ├── build.gradle.kts
    │
    ├── platform-auth/              (집합 모듈 - 레벨 2)
    │   ├── build.gradle.kts
    │   ├── auth-server/            (실제 애플리케이션)
    │   │   ├── build.gradle.kts
    │   │   └── src/main/java/
    │   └── auth-client/            (공유 라이브러리)
    │       ├── build.gradle.kts
    │       └── src/main/java/
    │
    └── platform-api/               (집합 모듈 - 레벨 2)
        ├── build.gradle.kts
        ├── api-gateway/            (실제 애플리케이션)
        │   ├── build.gradle.kts
        │   └── src/main/java/
        └── api-core/               (공유 라이브러리)
            ├── build.gradle.kts
            └── src/main/java/
```

**settings.gradle:**

```groovy
rootProject.name = 'root'

include(
        'platform',
        'platform:platform-auth',
        'platform:platform-auth:auth-server',
        'platform:platform-auth:auth-client',
        'platform:platform-api',
        'platform:platform-api:api-gateway',
        'platform:platform-api:api-core'
)
```

---

## 모듈 타입 분류

멀티 모듈 프로젝트에서 자주 쓰이는 세 가지 모듈 타입을 정리했습니다.

| 구분         | 집합 모듈                         | 애플리케이션 모듈                      | 라이브러리 모듈                    |
|------------|-------------------------------|--------------------------------|-----------------------------|
| 역할         | 하위 모듈을 묶는 컨테이너                | 실행 가능한 앱                       | 공유 코드, 유틸리티                 |
| src 디렉토리   | 없음                            | 있음                             | 있음                          |
| main() 메서드 | 없음                            | 있음                             | 없음                          |
| bootJar 설정 | disabled 또는 플러그인 미적용          | enabled (기본값)                  | disabled                    |
| 빌드 산출물     | jar (거의 사용 안함)                | 실행 가능한 jar                     | 일반 jar                      |
| 사용 예시      | `platform`<br>`platform-auth` | `auth-server`<br>`api-gateway` | `auth-client`<br>`api-core` |

집합 모듈은 실제 코드 없이 하위 모듈만 관리하는 껍데기입니다. 애플리케이션 모듈은 단독으로 실행 가능하고, 라이브러리 모듈은 다른 모듈에서 참조해서 씁니다.

---

## 5가지 설정 패턴

| 패턴                                                          | 복잡도   | 유지보수성      | 재사용성       | 타입안전성      | 확장성        | 추천도        |
|-------------------------------------------------------------|-------|------------|------------|------------|------------|------------|
| [패턴 1: apply false](#패턴-1-apply-false-패턴)                   | 낮음    | ⭐️⭐️⭐️⭐️⭐️ | ⭐️⭐️⭐️⭐️   | ⭐️⭐️⭐️⭐️⭐️ | ⭐️⭐️⭐️⭐️   | ⭐️⭐️⭐️⭐️⭐️ |
| [패턴 2: bootJar 비활성화](#패턴-2-플러그인-적용--bootjar-비활성화)           | 낮음    | ⭐️⭐️⭐️     | ⭐️⭐️⭐️     | ⭐️⭐️⭐️⭐️   | ⭐️⭐️⭐️     | ⭐️⭐️⭐️     |
| [패턴 3: 순수 집합](#패턴-3-순수-집합-모듈-플러그인-없음)                       | 매우 낮음 | ⭐️⭐️⭐️⭐️   | ⭐️⭐️       | ⭐️⭐️⭐️⭐️⭐️ | ⭐️⭐️       | ⭐️⭐️⭐️     |
| [패턴 4: Convention Plugin](#패턴-4-convention-plugin-buildsrc) | 높음    | ⭐️⭐️⭐️⭐️⭐️ | ⭐️⭐️⭐️⭐️⭐️ | ⭐️⭐️⭐️⭐️⭐️ | ⭐️⭐️⭐️⭐️⭐️ | ⭐️⭐️⭐️⭐️⭐️ |
| [패턴 5: 루트 중앙화](#패턴-5-루트에서-allprojectssubprojects-활용)        | 중간    | ⭐️⭐️       | ⭐️⭐️       | ⭐️⭐️⭐️     | ⭐️⭐️       | ⭐️⭐️       |

### 패턴 1: apply false 패턴

**특징:**

- Spring Boot 공식 권장 패턴
- 버전 관리 중앙화
- 불필요한 태스크 생성 없음
- 플러그인 충돌 최소화

**프로젝트 구조:**

- root/
    - ㄴ build.gradle.kts
    - ㄴ settings.gradle
    - ㄴ platform/ (집합 모듈 - 레벨 1)
        - <details>
          <summary>ㄴ build.gradle.kts 1️⃣</summary>

          ```kotlin
          plugins {
              id("java")
              id("org.springframework.boot") apply false
              id("io.spring.dependency-management") apply false
          }

          group = "com.example.platform"
          description = "Platform Aggregation Module"

          // 플러그인이 적용되지 않으므로 bootJar 설정 불필요
          ```
          </details>
        - ㄴ platform-auth/ (집합 모듈 - 레벨 2)
            - <details>
              <summary>ㄴ build.gradle.kts 2️⃣</summary>

              ```kotlin
              plugins {
                  id("java")
                  id("org.springframework.boot") apply false
                  id("io.spring.dependency-management") apply false
              }

              group = "com.example.platform.auth"
              description = "Auth Module Aggregation"

              subprojects {
                  apply(plugin = "java")
                  apply(plugin = "org.springframework.boot")
                  apply(plugin = "io.spring.dependency-management")

                  repositories {
                      mavenCentral()
                  }

                  dependencies {
                      implementation("org.springframework.boot:spring-boot-starter-web")
                      testImplementation("org.springframework.boot:spring-boot-starter-test")
                  }

                  tasks.withType<Test> {
                      useJUnitPlatform()
                  }
              }
              ```
              </details>
            - ㄴ auth-server/ (애플리케이션)
                - <details>
                  <summary>ㄴ build.gradle.kts 3️⃣</summary>

                  ```kotlin
                  // 상위 모듈의 subprojects 블록에서 플러그인 자동 적용됨

                  dependencies {
                      implementation(project(":platform:platform-auth:auth-client"))
                      implementation("org.springframework.boot:spring-boot-starter-security")
                  }
                  ```
                  </details>
                - ㄴ src/main/java/
            - ㄴ auth-client/ (라이브러리)
                - <details>
                  <summary>ㄴ build.gradle.kts 4️⃣</summary>

                  ```kotlin
                  import org.springframework.boot.gradle.tasks.bundling.BootJar

                  // 상위 모듈의 subprojects 블록에서 플러그인 자동 적용됨

                  tasks.named<BootJar>("bootJar") {
                      enabled = false // 라이브러리이므로 실행 가능한 jar 생성 안함
                  }

                  tasks.named<Jar>("jar") {
                      enabled = true // 일반 jar 생성
                  }
                  ```
                  </details>
                - ㄴ src/main/java/

**장점:**

- ✅ 깔끔한 구조
- ✅ 불필요한 태스크 없음
- ✅ IntelliJ 경고 없음
- ✅ Spring Boot 공식 권장
- ✅ 버전 관리 중앙화

**단점:**

- ❌ 없음 (가장 권장되는 패턴)

### 패턴 2: 플러그인 적용 + bootJar 비활성화

**특징:**

- 직관적이고 간단
- 집합 모듈에도 플러그인 직접 적용

**프로젝트 구조:**

- root/
    - ㄴ build.gradle.kts
    - ㄴ settings.gradle
    - ㄴ platform/ (집합 모듈 - 레벨 1)
        - <details>
          <summary>ㄴ build.gradle.kts 1️⃣</summary>

          ```kotlin
          import org.springframework.boot.gradle.tasks.bundling.BootJar

          plugins {
              id("java")
              id("org.springframework.boot")
              id("io.spring.dependency-management")
          }

          group = "com.example.platform"
          description = "Platform Aggregation Module"

          tasks.named<BootJar>("bootJar") {
              enabled = false
          }

          subprojects {
              apply(plugin = "java")
              apply(plugin = "org.springframework.boot")
              apply(plugin = "io.spring.dependency-management")
          }
          ```
          </details>
        - ㄴ platform-auth/ (집합 모듈 - 레벨 2)
            - <details>
              <summary>ㄴ build.gradle.kts 2️⃣</summary>

              ```kotlin
              import org.springframework.boot.gradle.tasks.bundling.BootJar

              group = "com.example.platform.auth"
              description = "Auth Module Aggregation"

              // platform의 subprojects에서 플러그인 이미 적용됨

              tasks.named<BootJar>("bootJar") {
                  enabled = false
              }

              subprojects {
                  repositories {
                      mavenCentral()
                  }

                  dependencies {
                      implementation("org.springframework.boot:spring-boot-starter-web")
                      testImplementation("org.springframework.boot:spring-boot-starter-test")
                  }
              }
              ```
              </details>
            - ㄴ auth-server/ (애플리케이션)
                - ㄴ build.gradle.kts (기본 설정 상속)
                - ㄴ src/main/java/
            - ㄴ auth-client/ (라이브러리)
                - ㄴ build.gradle.kts (bootJar disabled 필요)
                - ㄴ src/main/java/

**장점:**

- ✅ 이해하기 쉬움
- ✅ 직관적인 구조

**단점:**

- ❌ 불필요한 태스크 생성 (bootJar, bootRun 등)
- ❌ IntelliJ에서 "Compile classpath for source set 'main'" 경고 발생 가능
- ❌ src 디렉토리가 없는데 플러그인이 적용되어 혼란

---

### 패턴 3: 순수 집합 모듈 (플러그인 없음)

**특징:**

- 가장 미니멀한 접근
- 불필요한 설정 완전 제거

**프로젝트 구조:**

- root/
    - ㄴ build.gradle.kts
    - ㄴ settings.gradle
    - ㄴ platform/ (집합 모듈 - 레벨 1)
        - <details>
          <summary>ㄴ build.gradle.kts 1️⃣</summary>

          ```kotlin
          plugins {
              id("java")
          }

          group = "com.example.platform"
          description = "Platform Aggregation Module"

          // Spring Boot 관련 설정 없음
          ```
          </details>
        - ㄴ platform-auth/ (집합 모듈 - 레벨 2)
            - <details>
              <summary>ㄴ build.gradle.kts 2️⃣</summary>

              ```kotlin
              plugins {
                  id("java")
              }

              group = "com.example.platform.auth"
              description = "Auth Module Aggregation"

              subprojects {
                  apply(plugin = "java")
                  apply(plugin = "org.springframework.boot")
                  apply(plugin = "io.spring.dependency-management")

                  repositories {
                      mavenCentral()
                  }

                  dependencies {
                      implementation("org.springframework.boot:spring-boot-starter-web")
                      testImplementation("org.springframework.boot:spring-boot-starter-test")
                  }
              }
              ```
              </details>
            - ㄴ auth-server/ (애플리케이션)
                - ㄴ build.gradle.kts (subprojects에서 플러그인 적용받음)
                - ㄴ src/main/java/
            - ㄴ auth-client/ (라이브러리)
                - ㄴ build.gradle.kts (bootJar disabled 필요)
                - ㄴ src/main/java/

**장점:**

- ✅ 가장 깔끔
- ✅ 불필요한 설정 없음
- ✅ 명확한 역할 분리

**단점:**

- ❌ 하위 모듈마다 플러그인 버전 관리 필요
- ❌ 공통 설정 재사용 제한적

---

### 패턴 4: Convention Plugin (buildSrc)

Gradle 공식 권장 방식으로, 재사용 가능한 빌드 로직을 플러그인으로 만들어 관리합니다.

#### buildSrc란?

`buildSrc`는 Gradle이 특별히 인식하는 디렉토리입니다.

- 루트 프로젝트의 `buildSrc/` 디렉토리에 코드를 두면
- Gradle이 자동으로 빌드하고
- 모든 하위 모듈에서 자동으로 사용 가능합니다

#### Precompiled Script Plugin

`buildSrc/src/main/kotlin/` 안에 `.gradle.kts` 파일을 만들면:

- Gradle이 자동으로 플러그인 클래스로 컴파일
- **파일명 = 플러그인 ID**

```
module.aggregation.gradle.kts  →  id("module.aggregation")
module.application.gradle.kts  →  id("module.application")
```

>
참고: [Precompiled Script Plugins - Gradle Docs](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html)

#### 동작 원리

```
1. ./gradlew build 실행
   ↓
2. Gradle이 buildSrc/ 먼저 빌드
   ↓
3. *.gradle.kts 파일들을 플러그인 클래스로 컴파일
   ↓
4. 모든 모듈에서 id("module.*") 사용 가능
```

**프로젝트 구조:**

- root/
    - ㄴ buildSrc/
        - <details>
          <summary>ㄴ build.gradle.kts 1️⃣</summary>

          ```kotlin
          plugins {
              `kotlin-dsl`  // 필수! Precompiled Script Plugin을 인식하게 함
          }

          repositories {
              gradlePluginPortal()  // Gradle 플러그인 저장소
              mavenCentral()
          }

          dependencies {
              // 여기서 선언한 플러그인을 *.gradle.kts 파일에서 사용 가능
              implementation("org.springframework.boot:spring-boot-gradle-plugin:3.5.0")
              implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
          }
          ```
          </details>
        - ㄴ src/main/kotlin/
            - <details>
              <summary>ㄴ module.aggregation.gradle.kts 2️⃣</summary>

              ```kotlin
              // 파일명: module.aggregation.gradle.kts
              // 플러그인 ID: id("module.aggregation")

              plugins {
                  java
                  id("org.springframework.boot")
                  id("io.spring.dependency-management")
              }

              // 집합 모듈은 실행 불가능한 jar
              tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
                  enabled = false
              }

              tasks.named<Jar>("jar") {
                  enabled = true
              }
              ```
              </details>
            - <details>
              <summary>ㄴ module.application.gradle.kts 3️⃣</summary>

              ```kotlin
              // 파일명: module.application.gradle.kts
              // 플러그인 ID: id("module.application")

              plugins {
                  java
                  id("org.springframework.boot")
                  id("io.spring.dependency-management")
              }

              dependencies {
                  implementation("org.springframework.boot:spring-boot-starter-web")
                  testImplementation("org.springframework.boot:spring-boot-starter-test")
              }

              tasks.withType<Test> {
                  useJUnitPlatform()
              }
              ```
              </details>
            - <details>
              <summary>ㄴ module.library.gradle.kts 4️⃣</summary>

              ```kotlin
              // 파일명: module.library.gradle.kts
              // 플러그인 ID: id("module.library")

              plugins {
                  java
                  id("org.springframework.boot")
                  id("io.spring.dependency-management")
              }

              // 라이브러리는 일반 jar
              tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
                  enabled = false
              }

              tasks.named<Jar>("jar") {
                  enabled = true
              }

              dependencies {
                  testImplementation("org.springframework.boot:spring-boot-starter-test")
              }

              tasks.withType<Test> {
                  useJUnitPlatform()
              }
              ```
              </details>
    - ㄴ platform/
        - <details>
          <summary>ㄴ build.gradle.kts 5️⃣</summary>

          ```kotlin
          plugins {
              id("module.aggregation")  // ← 자동으로 인식됨!
          }

          group = "com.example.platform"
          ```
          </details>
        - ㄴ platform-auth/auth-server/
            - <details>
              <summary>ㄴ build.gradle.kts 6️⃣</summary>

              ```kotlin
              plugins {
                  id("module.application")  // ← 자동으로 인식됨!
              }

              dependencies {
                  implementation(project(":platform:platform-auth:auth-client"))
                  implementation("org.springframework.boot:spring-boot-starter-security")
              }
              ```
              </details>
        - ㄴ platform-auth/auth-client/
            - <details>
              <summary>ㄴ build.gradle.kts 7️⃣</summary>

              ```kotlin
              plugins {
                  id("module.library")  // ← 자동으로 인식됨!
              }

              dependencies {
                  api("org.springframework.boot:spring-boot-starter-web")
              }
              ```
              </details>

1. 1단계: buildSrc 프로젝트 생성 (1️⃣~4️⃣)
2. 2단계: 프로젝트 모듈에 적용 (5️⃣~7️⃣)

**장점:**

- ✅ 타입 안전 (IDE 자동완성 지원)
- ✅ 코드 재사용 극대화
- ✅ 각 모듈 build.gradle.kts 간결
- ✅ 버전 관리 중앙화
- ✅ Gradle 공식
  권장 ([Convention Plugins](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_convention.html))

**단점:**

- ❌ buildSrc 변경 시 전체 프로젝트 클린 빌드 필요
- ❌ 초기 설정 러닝 커브

**팁:**

- 프로젝트가 30개 이상 모듈로 커지면 buildSrc 대신 `build-logic` included build 사용
  권장 ([샘플](https://docs.gradle.org/current/samples/sample_publishing_convention_plugins.html))

---

### 패턴 5: 루트에서 allprojects/subprojects 활용

**특징:**

- 모든 설정을 루트에 집중
- 하위 모듈 파일 간결

**프로젝트 구조:**

- root/
    - <details>
      <summary>ㄴ 1. build.gradle.kts</summary>

      ```kotlin
      import org.springframework.boot.gradle.tasks.bundling.BootJar

      plugins {
          id("org.springframework.boot") version "3.5.0" apply false
          id("io.spring.dependency-management") version "1.1.7" apply false
      }

      allprojects {
          group = "com.example"
          version = "1.0.0-SNAPSHOT"

          repositories {
              mavenCentral()
          }
      }

      subprojects {
          apply(plugin = "java")

          // platform 관련 모듈만 필터링
          if (path.startsWith(":platform")) {
              apply(plugin = "org.springframework.boot")
              apply(plugin = "io.spring.dependency-management")

              // 집합 모듈 필터링 (src 디렉토리 없음)
              if (!file("src").exists()) {
                  tasks.named<BootJar>("bootJar") {
                      enabled = false
                  }
              }

              // 라이브러리 모듈 필터링 (이름에 -client, -core 포함)
              if (name.contains("client") || name.contains("core")) {
                  tasks.named<BootJar>("bootJar") {
                      enabled = false
                  }
              }
          }
      }
      ```
      </details>
    - ㄴ settings.gradle
    - ㄴ platform/ (하위 모듈들은 최소한의 build.gradle.kts만 보유)

**장점:**

- ✅ 중앙 집중식 관리
- ✅ 하위 모듈 파일 간결

**단점:**

- ❌ 루트 build.gradle 비대화
- ❌ 모듈별 커스터마이징 어려움
- ❌ 복잡한 조건문 필요
- ❌ 유지보수 어려움

---

## 어떤 패턴을 선택할까?

### 프로젝트 규모별 권장 패턴

| 프로젝트 규모 | 모듈 수   | 추천 패턴                                   | 선택 이유                                                  | 적용 사례                           |
|---------|--------|-----------------------------------------|--------------------------------------------------------|---------------------------------|
| **소규모** | 3-10개  | 패턴 1<br>(apply false)                   | - 간단하면서도 확장 가능<br>- Spring Boot 공식 권장<br>- 불필요한 태스크 없음 | - 단일 집합 모듈<br>- 여러 애플리케이션/라이브러리 |
| **중규모** | 10-30개 | 패턴 4<br>(Convention Plugin)             | - 공통 설정 재사용 필수<br>- 유지보수성 향상<br>- 타입 안전성               | - 여러 집합 모듈 계층<br>- 다양한 모듈 타입    |
| **대규모** | 30개+   | Composite Builds<br>+ Convention Plugin | - 빌드 성능 최적화<br>- 모듈 독립성 확보<br>- 증분 빌드 지원               | - 독립적인 서브 프로젝트<br>- 공통 플러그인 저장소 |

### 기존 프로젝트 마이그레이션 전략

기존 멀티 모듈 프로젝트를 단계별로 개선하는 로드맵입니다.

| 단계                           | 목적                 | 수행 작업                                                                                          | 체크포인트                                               | 위험도   |
|------------------------------|--------------------|------------------------------------------------------------------------------------------------|-----------------------------------------------------|-------|
| **1단계**<br>현황 파악             | 개선 대상 식별           | - 집합 모듈 식별 (src 디렉토리 없는 모듈)<br>- 플러그인 적용 현황 파악<br>- IntelliJ/Gradle 경고 확인<br>- 빌드 시간 측정        | ✅ 집합 모듈 목록 작성 완료<br>✅ 불필요한 태스크 확인                   | 🟢 없음 |
| **2단계**<br>패턴 1 적용           | 구조 정리              | - 집합 모듈에 `apply false` 적용<br>- 불필요한 `bootJar` 설정 제거<br>- `./gradlew build` 테스트<br>- 각 모듈 빌드 검증 | ✅ 모든 모듈 빌드 성공<br>✅ IntelliJ 경고 사라짐<br>✅ 불필요한 태스크 제거 | 🟡 낮음 |
| **3단계**<br>Convention Plugin | 재사용성 강화<br>(선택 사항) | - 모듈 수 10개 이상 시 검토<br>- buildSrc 디렉토리 생성<br>- 공통 설정 플러그인화<br>- 점진적 적용 (1-2개 모듈부터)              | ✅ 빌드 스크립트 간결화<br>✅ 설정 중복 제거<br>✅ 타입 안전성 확보          | 🟠 중간 |

**권장 진행 방식**:

- 1단계 → 2단계: 모든 프로젝트 필수
- 2단계 → 3단계: 모듈 수 10개 이상 또는 공통 설정 반복 시 검토

---

## Reference

- Gradle 공식 문서
    - [General Gradle Best Practices](https://docs.gradle.org/current/userguide/best_practices_general.html)
    - [Structuring Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds_intermediate.html)
    - [Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
    - [Organizing Gradle Projects](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html)
    - [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
    - [Sharing Build Logic](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html)
    - [Using buildSrc](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html#sec:sharing_logic_via_convention_plugins)
- [우아한형제들 - Gradle Kotlin DSL 이야기](https://techblog.woowahan.com/2625/)

# Gradle
## Migrating Builds From Apache Maven

`Apache Maven`은 Java 및 **JVM 기반 프로젝트를 위한 빌드 도구다.**

기존에 `Maven`으로 구성된 프로젝트를 `Gradle`로 마이그레이션하는 경우는 매우 흔하다.

이 가이드는 이러한 마이그레이션 과정에서 **`Gradle`과 `Maven`의 차이점과 유사점**을 설명하고, **마이그레이션을 수월하게 하기 위한 단계별 절차**를 제공한다.

빌드 도구를 전환하는 일은 다소 부담스러울 수 있다. 하지만 걱정하지 않아도 된다. 막히는 부분이 생기면 다음과 같은 도움을 받을 수 있다.

- [Gradle 공식 문서 검색](https://docs.gradle.org/current/userguide/userguide.html)
- [커뮤니티 포럼에 질문](https://discuss.gradle.org/?_gl=1*1dxpgqc*_gcl_au*MTMzMjM0OTg5Ni4xNzUxODU5NDI3*_ga*MTkzMTMzNjc4Ni4xNzUxODU5NDI5*_ga_7W7NC6YNPT*czE3NTE4NTk0MjkkbzEkZzEkdDE3NTE4NTk5MDUkajYwJGwwJGgw)
- [공식 Slack 채널 참여](https://gradle-community.slack.com/join/shared_invite/zt-36fljk4f4-BPHZ0gZqzPLHPxPRQ50m9g#/shared-invite/email)

---

### TOC

- [마이그레이션을 해야 하는 이유](#마이그레이션을-고려해야-하는-이유)
- [일반적인 가이드라인](#일반적인-가이드라인)
- [빌드 라이프사이클 이해하기](#빌드-라이프사이클-이해하기)
- [자동 변환 수행하기](#자동-변환-수행하기)
- [의존성 마이그레이션](#의존성-마이그레이션)
- [BOM(Bill of Materials) 사용하기](#bom--bill-of-materials--사용하기)
- [멀티 모듈 빌드 마이그레이션(프로젝트 집합)](#멀티-모듈-빌드-마이그레이션--project-aggregation-)
- [Maven 프로파일과 프로퍼티 이전](#maven-프로파일과-프로퍼티-이전)
- [리소스 필터링 설정](#리소스-필터링-설정)
- [통합 테스트 설정하기](#통합-테스트-설정하기)
- [공통 플러그인 마이그레이션하기 (Migrating common plugins)](#공통-플러그인-마이그레이션하기--migrating-common-plugins-)
- [불필요한 플러그인 파악하기](#불필요한-플러그인-파악하기)
- [비표준 또는 커스텀 플러그인 대응](#비표준-또는-커스텀-플러그인-대응)
- [추가로 참고할 자료](#추가로-참고할-자료)

---

## [마이그레이션을 고려해야 하는 이유](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:making_a_case)

`Gradle`과 `Maven`의 주요 차이는 다음 네 가지 측면에서 나타난다:

- **유연성**
- **성능**
- **사용자 경험**
- **의존성 관리**

자세한 내용은 [Maven vs Gradle 기능 비교표](https://gradle.org/maven-and-gradle/)에서 확인할 수 있다.

Gradle 3.0 이후로, Gradle 팀은 다음과 같은 기능을 통해 **빌드 성능을 대폭 향상**시켰다:

- [**빌드 캐시(Build Cache)**](https://blog.gradle.org/introducing-gradle-build-cache?_gl=1*8nmixg*_gcl_au*MTMzMjM0OTg5Ni4xNzUxODU5NDI3*_ga*MTkzMTMzNjc4Ni4xNzUxODU5NDI5*_ga_7W7NC6YNPT*czE3NTE4NTk0MjkkbzEkZzEkdDE3NTE4NjAwNTckajE5JGwwJGgw)
- [**컴파일 회피(Compile Avoidance)**](https://blog.gradle.org/incremental-compiler-avoidance?_gl=1*8nmixg*_gcl_au*MTMzMjM0OTg5Ni4xNzUxODU5NDI3*_ga*MTkzMTMzNjc4Ni4xNzUxODU5NDI5*_ga_7W7NC6YNPT*czE3NTE4NTk0MjkkbzEkZzEkdDE3NTE4NjAwNTckajE5JGwwJGgw)
- **향상된 증분 Java 컴파일러**

**빌드 캐시를 사용하지 않아도,** `Gradle`은 **`Maven`보다 2~10배 더 빠르게 빌드된다.**

더 자세한 내용은 ["Gradle과 Maven 성능 비교 분석표"](https://gradle.org/gradle-and-maven-performance)를 참고하자.

---

좋아. 요청한 조건대로 문장 생략 없이, 초보 개발자 눈높이에 맞게 자연스럽고 읽기 쉬운 한국어로 번역해줄게. 문체는 "\~다" 어체로 유지하고, 목록은 `*` 기호를 사용하겠다.

---

## [일반적인 가이드라인](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:general_guidelines)

`Gradle`과 `Maven`은 프로젝트를 "빌드하는 방식"에서 근본적인 차이가 있다.

- `Gradle`은 **태스크 그래프(task graph)** 기반으로 실행되며, 유연하고 확장 가능한 빌드 모델을 제공한다.
- 반면 `Maven`은 **정해진 선형적인 단계들(phase)** 에 빌드 작업(goal)을 붙이는 구조다.

이 때문에 처음에는 마이그레이션이 어렵게 느껴질 수 있지만, 

사실 그렇지 않다. 왜냐하면 `Gradle`은 `Maven`의 많은 관례(convention)를 그대로 따르기 때문이다. 예를 들어, **프로젝트 디렉토리 구조**, **의존성 관리 방식** 등은 상당히 유사하다.

Gradle로 마이그레이션할 때 다음과 같은 순서를 따르면서 진행하면 훨씬 수월하게 할 수 있다:

1. [기존 Maven 빌드와 새 Gradle 빌드를 나란히 유지하자](#1-기존-maven-빌드와-새-gradle-빌드를-나란히-유지하자)
2. [기존 Maven 빌드에 대해 **빌드 스캔(Build Scan)** 을 생성하자](#2-기존-maven-빌드에-대해-빌드-스캔build-scan-을-생성하자)
3. [두 빌드가 동일한 아티팩트를 생성하는지 검증하는 메커니즘을 마련하자](#3-두-빌드가-동일한-아티팩트를-생성하는지-검증하는-메커니즘을-마련하자)
4. [빌드 결과물 간 차이점도 감안해야 한다](#4-빌드-결과물-간-차이점도-감안해야-한다)
5. [**자동 변환 기능**을 활용하자](#5-자동-변환-기능을-활용하자)
6. [Gradle 빌드에 대해서도 **빌드 스캔**을 생성하자](#6-gradle-빌드에-대해서도-빌드-스캔을-생성하자)
7. [의존성을 점검하고 문제를 수정하자](#7-의존성을-점검하고-문제를-수정하자)
8. [통합 테스트 및 기능 테스트 설정을 구성하자](#8-통합-테스트-및-기능-테스트-설정을-구성하자)
9. [Maven 플러그인을 Gradle 대체 플러그인으로 전환하자](#9-maven-플러그인을-gradle-대체-플러그인으로-전환하자)

### 1. 기존 Maven 빌드와 새 Gradle 빌드를 나란히 유지하자

- 기존 Maven 빌드는 정상적으로 작동한다는 것을 알고 있으므로, Gradle로 완전히 전환하기 전까지는 같이 유지하는 게 좋다.
- Gradle 빌드를 시도해보는 사람이 소스코드를 새로 복제할 필요 없이 바로 시도할 수 있도록 한다.

### 2. 기존 Maven 빌드에 대해 **빌드 스캔(Build Scan)** 을 생성하자

- 빌드 스캔은 기존 Maven 빌드의 내부 구조를 시각적으로 보여준다.
- 예를 들어 **프로젝트 구조, 사용 중인 플러그인, 단계별 빌드 타임라인 등**을 확인할 수 있다.
- Gradle로 전환할 때 생성한 Gradle 스캔과 비교 분석하는 데 큰 도움이 된다.

> [Gradle Build Scan 참고](https://gradle.com/scans/gradle/)

### 3. 두 빌드가 동일한 아티팩트를 생성하는지 검증하는 메커니즘을 마련하자

- 이 단계는 매우 중요하다. 그래야 배포나 테스트에서 문제가 생기지 않는다.
- 예를 들어 JAR의 `MANIFEST` 파일처럼 사소한 내용 차이도 문제가 될 수 있다.
- Gradle 빌드 결과물이 Maven과 동일하다는 확신이 생기면, 나중에 최적화하거나 구조를 더 유연하게 바꾸기도 쉬워진다.

**※ 모든 아티팩트를 일일이 비교할 필요는 없다.**

최종 보고서, 배포 대상 아티팩트처럼 핵심적인 산출물 위주로 확인하자.

### 4. 빌드 결과물 간 차이점도 감안해야 한다

- Gradle이 생성한 POM은 **소비(consumption)** 관점에 필요한 정보만 담긴다.
- 의존성 스코프도 Gradle에선 `<compile>`, `<runtime>`을 그 목적에 맞게 다르게 처리한다.
- 아카이브 안의 파일 순서나 클래스패스의 파일 순서도 다를 수 있다.
  - 대부분은 사소한 차이지만, 이런 차이를 직접 확인해보고 수용 가능한지 판단해야 한다.

### 5. [**자동 변환 기능**](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:automatic_conversion)을 활용하자

- Gradle은 Maven 프로젝트를 자동으로 변환해주는 기능을 제공한다.
- 단일 모듈뿐 아니라 [멀티 모듈](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:multimodule_builds)까지도 Gradle 빌드 파일을 자동 생성해준다.
- 간단한 Maven 프로젝트라면 변환 후 바로 빌드 가능한 수준까지도 도달한다.

### 6. Gradle 빌드에 대해서도 [**빌드 스캔**](https://gradle.com/scans/gradle/)을 생성하자

- Gradle 빌드 스캔을 통해 프로젝트 구조, 의존성(내부 모듈 포함), 사용된 플러그인, 콘솔 출력까지 모두 시각적으로 확인할 수 있다.
- 빌드에 실패해도 스캔은 생성되므로 걱정하지 않아도 된다.
- 앞서 만든 Maven 빌드 스캔과 비교하면서 문제 원인을 추적할 수 있다.
- Gradle 스캔은 **성능 최적화 포인트를 찾는 데도 매우 유용하다.**

### [7. 의존성을 점검하고 문제를 수정하자](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:migrating_deps)

- 자동 변환 후에도 일부 의존성이 누락되거나 올바르게 설정되지 않을 수 있다.
- 해당 항목을 확인하고 필요한 의존성을 정확하게 추가해준다.

### [8. 통합 테스트 및 기능 테스트 설정을 구성하자](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:integration_tests)

- Gradle에서도 `extra sourceSet`을 설정하여 테스트 구성을 확장할 수 있다.
- `FitNesse`와 같은 외부 테스트 프레임워크를 사용할 경우, [Gradle Plugin Portal](https://plugins.gradle.org/)에서 관련 플러그인이 있는지 먼저 확인하자.

### 9. Maven 플러그인을 Gradle 대체 플러그인으로 전환하자

- 자주 쓰이는 Maven 플러그인은 대부분 Gradle에도 대응되는 플러그인이 있다.
- 경우에 따라 Gradle 내장 기능만으로 대체할 수 있는 경우도 있다.
- 정말 필요한 경우에만 **커스텀 Gradle 태스크나 플러그인**을 직접 만들어서 대체하자.

---

## [빌드 라이프사이클 이해하기](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:build_lifecycle)

`Maven` 빌드는 고정된 **"단계(phase)"** 의 집합으로 구성된 [**빌드 생명주기(lifecycle)**](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html) 개념을 중심으로 구성된다.

반면 `Gradle`은 자체적으로 3단계 빌드 구조를 가지고 있다.

1. 초기화(initialization)
2. 설정(configuration)
3. 실행(execution)

`Maven`의 익숙한 개념을 그대로 쓸 수 있도록 [**라이프사이클 태스크(lifecycle task)**](https://docs.gradle.org/current/userguide/build_lifecycle.html#build_lifecycle) 라는 기능을 제공한다.

### [Gradle의 라이프사이클 태스크란?](https://docs.gradle.org/current/userguide/build_lifecycle.html#build_lifecycle)

`Gradle`에서는 실제 작업을 하지 않는 태스크(`no-action tasks`)를 만들어, 관심 있는 태스크들을 의존성으로 연결함으로써 **나만의 빌드 단계(lifecycle)** 를 정의할 수 있다.

또한 `Gradle`의 [Base Plugin](https://docs.gradle.org/current/userguide/java_library_plugin.html#java_library_plugin)(Java, Kotlin 등 [JVM 플러그인](https://docs.gradle.org/current/userguide/java_library_plugin.html#java_library_plugin)에서 자동 적용)은 `Maven`의 주요 `phase`에 대응되는 **기본 라이프사이클 태스크**들을 제공한다.

### Maven 단계 vs Gradle 태스크 매핑

| Maven Phase | Gradle Task           | 설명                                                              |
|-------------|-----------------------|-----------------------------------------------------------------|
| `clean`     | `clean`               | Base Plugin에서 제공. 빌드 산출물 제거                                     |
| `compile`   | `classes`             | Java Plugin에서 제공. 모든 소스 파일 컴파일 및 리소스 처리 (`processResources`) 포함 |
| `test`      | `test`                | Java Plugin에서 제공. test source set에 정의된 단위 테스트 수행                |
| `package`   | `assemble`            | Base Plugin에서 제공. JAR, WAR 등 해당 프로젝트의 최종 패키지 생성                 |
| `verify`    | `check`               | Base Plugin에서 제공. 테스트, 정적 분석(Checkstyle 등) 포함된 검증 태스크           |
| `install`   | `publishToMavenLocal` | Maven Publish Plugin에서 제공. 로컬 Maven 저장소에 배포                     |
| `deploy`    | `publish`             | Maven Publish Plugin에서 제공. 설정된 원격 저장소에 배포                       |

- `Gradle`은 **모듈 간 직접 참조(inter-project dependency)** 와 **컴포짓 빌드(composite build)** 를 지원하므로, `Maven`처럼 반드시 `install`을 하지 않아도 된다.
- `publishToMavenLocal`은 Maven 호환성이 필요한 경우에만 사용하자.
- `Gradle`도 Maven 로컬 저장소를 `repository`로 지정해 의존성을 해결할 수 있다.
- `publish` 태스크는 **모든 설정된 저장소에 배포**하며, **개별 저장소만 선택해서 배포**하는 태스크도 따로 존재한다.
- `Maven Publish Plugin`은 기본적으로 source JAR, Javadoc JAR를 생성/배포하지 않지만, [Java 프로젝트 빌드 가이드](https://docs.gradle.org/current/userguide/building_java_projects.html#sec:java_packaging)에서 쉽게 설정할 수 있다.

---

## [자동 변환 수행하기](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:automatic_conversion)

`Gradle`의 `init` 태스크는 보통 새로운 프로젝트의 골격을 생성할 때 사용되지만, **기존 Maven 프로젝트를 자동으로 Gradle로 변환하는 데에도 사용할 수 있다.**

### 변환 명령어

`gradle init` 명령어를 통해 자동 변환된다.

```bash
# 루트 프로젝트 디렉터리에서 실행
> gradle init
```

Gradle은 기존의 `pom.xml` 파일을 파싱해서 **동등한 Gradle 빌드 스크립트**를 생성하며, 

**[멀티 프로젝트](https://docs.gradle.org/current/userguide/multi_project_builds.html#multi_project_builds)일 경우 `settings.gradle` 파일도 함께 생성**해준다.

### 자동 변환 시 생성되는 항목

변환 후 생성된 Gradle 프로젝트에는 다음 항목이 포함된다:

- `pom.xml`에 명시된 **커스텀 저장소(repository) 설정**
- 외부 의존성과 모듈 간 의존성 (**inter-project dependencies**)
- 해당 프로젝트 빌드를 위한 적절한 플러그인
  - 예: `java`, `war`, `maven-publish` 중 필요한 플러그인

> 전체 변환 기능 목록은 [Build Init Plugin 문서](https://docs.gradle.org/current/userguide/build_init_plugin.html#sec:pom_maven_conversion)에서 확인할 수 있다.

### 주의할 점: 어셈블리(assembly)는 자동 변환되지 않음

`Maven`에서 사용하던 `assembly` 관련 설정은 자동으로 변환되지 않는다.

이 부분은 **수동으로 설정**해야 한다.

대안으로 사용할 수 있는 Gradle 기능은 다음과 같다:

- [`Distribution Plugin`](https://docs.gradle.org/current/userguide/distribution_plugin.html#distribution_plugin)
- [`Java Library Distribution Plugin`](https://docs.gradle.org/current/userguide/java_library_distribution_plugin.html#java_library_distribution_plugin)
- [`Application Plugin`](https://docs.gradle.org/current/userguide/application_plugin.html#application_plugin)
- [직접 **커스텀 아카이브 태스크(archive task)** 만들기](https://docs.gradle.org/current/userguide/working_with_files.html#sec:creating_archives_example)
- [Gradle Plugin Portal](https://plugins.gradle.org/)에서 적절한 커뮤니티 플러그인 사용하기

### 마이그레이션 후 빌드 실행

만약 `pom.xml`에 복잡한 플러그인이나 사용자 정의 작업이 많지 않다면, 변환 이후 바로 아래 명령어를 실행해도 된다:

```bash
> gradle build
```

이 명령은 테스트를 실행하고 필요한 아티팩트를 자동으로 생성한다.

---

## [의존성 마이그레이션](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:migrating_deps)

`Gradle`의 의존성 관리 시스템은 `Maven`보다 유연하다.

- 저장소(repository)
- 선언된 의존성(declared dependencies)
- 스코프
- 전이 의존성(transitive dependencies)

또한 Gradle은 Maven과 호환되는 저장소를 사용할 수 있기 때문에, **의존성 이전은 매우 수월하다.**

Gradle 의존성 설정에 대한 자세한 내용은 [Gradle dependency configurations](https://docs.gradle.org/current/userguide/dependency_configurations.html#sub:what-are-dependency-configurations) 을 참고하자.

#### Maven과 Gradle의 버전 충돌 처리 차이

`Maven`과 `Gradle`의 의존성 버전 충돌시 아래와 같이 처리한다.

- Gradle은 **"가장 최신"** 버전을 선택한다.
- Maven은 **"가장 가까운"** 버전을 선택한다.

그러나 걱정할 필요는 없다. `Gradle`에서는 **해당 버전 선택을 제어할 수 있는 여러 방법**을 제공한다.

자세한 내용은 Gradle 문서의 **[전이 의존성 제어하기](https://docs.gradle.org/current/userguide/dependency_constraints.html#dependency-constraints)** 항목을 참고하자.

### [의존성 선언 방법](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:declaring_deps)

`Gradle`도 `Maven`과 동일하게 다음 3가지를 사용한다:

- groupId → `group`
- artifactId → `module`
- version → `version`

`compile-time` 의존성은 다음과 같이 작성할 수 있다.

```xml
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.12</version>
</dependency>
```
```groovy
dependencies {
    implementation 'log4j:log4j:1.2.12'
}
```

- `implementation`은 `Gradle`의 의존성 **configuration(scope)** 다.
- `Maven`의 `compile` 스코프와 유사하지만, 더 세분화되어 있다.

#### Maven → Gradle 스코프 매핑

| Maven Scope | Gradle Configuration                         | 설명                                                     |
|-------------|----------------------------------------------|--------------------------------------------------------|
| `compile`   | `implementation`, `api`                      | `implementation`: 대부분의 경우 사용<br>`api`: 라이브러리를 제공할 때 사용 |
| `runtime`   | `runtimeOnly`                                | 실행 시 필요한 의존성                                           |
| `test`      | `testImplementation`, `testRuntimeOnly`      | 테스트 코드 컴파일용 / 실행용 분리                                   |
| `provided`  | `compileOnly`                                | 빌드엔 필요하지만 패키징엔 포함되지 않아야 할 경우                           |
| `import`    | `platform`<br/>`enforcePlatform`(BOM(후술) 사용) | POM-only BOM을 사용할 때                                    |

Maven의 `<dependencyManagement><dependency scope="import">`를 대체할 때,

Gradle에서는 다음과 같이 [BOM(Bill of Materials)](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:using_boms)을 가져올 수 있다:

```groovy
dependencies {
    // 1
    implementation platform('org.springframework.boot:spring-boot-dependencies:1.5.8.RELEASE')

    // 2
    implementation 'com.google.code.gson:gson'
    implementation 'dom4j:dom4j'

    // 3
    testImplementation 'org.codehaus.groovy:groovy-all:2.5.4'
}
```

1. Applies the Spring Boot Dependencies BOM
2. Adds a dependency whose version is defined by that BOM
3. POM-only 의존성 사용하기
   - 예: `groovy-all`은 실제 JAR이 없고, 의존성만 포함된 POM이다.
   - 이 경우에도 Gradle은 자동으로 전이 의존성을 처리해준다:

---

#### [저장소 선언](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:declaring_repos)

Gradle은 [기본 저장소](https://docs.gradle.org/current/userguide/declaring_repositories_basics.html#sec:maven-central)가 없기 때문에 직접 선언해야 한다.

기존 Maven과 동일하게 하려면 아래와 같이 설정:

```groovy
repositories {
    mavenCentral()
}
```

- [커스텀 저장소](https://docs.gradle.org/current/userguide/supported_repository_types.html#sec:maven-repo)를 추가할 수도 있고
- `file://` 경로를 사용하는 [로컬 저장소](https://docs.gradle.org/current/userguide/supported_repository_types.html#sec:maven-local)도 설정할 수 있다.
- Gradle은 [**로컬 Maven 캐시**](https://docs.gradle.org/current/userguide/dependency_caching.html#sec:dependency-cache)도 사용할 수 있으며, Maven과의 상호 운용 시 유용하다.

### [버전 충돌 제어하기](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:controlling_dep_versions)

Gradle은 아래 방법들로 어떤 버전을 사용할지 제어할 수 있다:

- [`dependency constraints` 사용](https://docs.gradle.org/current/userguide/dependency_constraints.html#sec:adding-constraints-transitive-deps)
- [Bills of materials(`BOM` 기반의 platform 사용)](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:using_boms)
- [개별 의존성 override](https://docs.gradle.org/current/userguide/dependency_versions.html#sec:enforcing-dependency-version)

멀티 프로젝트 전반에 걸쳐 버전 일관성을 유지하고 싶다면 `Java Platform Plugin`을 사용하자.

### [전이 의존성 제외](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:excluding_deps)

Maven처럼 `exclusion` 태그를 사용할 수도 있지만,

Gradle에서는 좀 더 유연하게 아래처럼 설정 가능하다:

```groovy
dependencies {
    implementation('a:b:1.0') {
        exclude group: 'c', module: 'd'
    }
}
```

또는 전체 configuration에 적용할 수도 있다.

다만, 단순히 버전을 제어하고 싶다면 `exclude`보다는 **[제어 전략](https://docs.gradle.org/current/userguide/dependency_constraints.html#sec:adding-constraints-transitive-deps)을 사용하는 게 더 적절하다.**

### [optional 의존성 처리](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:optional_deps)

1. transitive 의존성이 optional로 선언된 경우 
   - Gradle도 Maven과 마찬가지로 **무시**한다.
2. 내 프로젝트에서 optional로 선언하고 싶은 경우
   - Gradle은 [feature variant](https://docs.gradle.org/current/userguide/how_to_create_feature_variants_of_a_library.html#feature_variants) 기능을 사용해서 **선택적 모듈**로 표현할 수 있다.

이 내용은 대부분의 Maven 프로젝트에서 Gradle로 이전할 때 반드시 마주치는 이슈들이며,
실제 적용해 보면 Gradle의 의존성 관리가 훨씬 유연하고 강력하다는 걸 느낄 수 있다.

## [BOM(Bill of Materials) 사용하기](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:using_boms)

Maven에서는 `<dependencyManagement>` 섹션을 사용해 **의존성 버전 제약 조건을 공유**할 수 있다.

이때 사용하는 특별한 타입의 POM이 바로 **BOM(POM 타입이 `pom`인 파일)** 이다.
이 BOM은 다른 POM에 `import`하여, 여러 프로젝트에서 **라이브러리 버전을 일관되게 유지**할 수 있게 한다.

### Gradle에서 BOM 사용하기

Gradle도 동일한 목적을 위해 BOM을 사용할 수 있으며, [platform()](https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.dsl.DependencyHandler.html#org.gradle.api.artifacts.dsl.DependencyHandler:platform(java.lang.Object)) 또는 [enforcedPlatform()](https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.dsl.DependencyHandler.html#org.gradle.api.artifacts.dsl.DependencyHandler:enforcedPlatform(java.lang.Object)) 메서드를 이용한 **특수한 의존성 선언 문법**을 사용한다.

```groovy
dependencies {
    implementation platform('org.springframework.boot:spring-boot-dependencies:1.5.8.RELEASE')

    implementation 'com.google.code.gson:gson'
    implementation 'dom4j:dom4j'
}
```

- `spring-boot-dependencies` BOM을 가져와 버전 제약 조건으로 사용한다.
- 이후 선언된 의존성(`gson`, `dom4j`)의 버전은 BOM에서 정의한 값을 따른다.
- BOM에 명시되지 않은 의존성은 Gradle 쪽에서 명시한 버전을 그대로 사용한다.

#### `platform()` vs `enforcedPlatform()`

| 메서드                  | 설명                                                       |
|----------------------|----------------------------------------------------------|
| `platform()`         | BOM에 정의된 버전은 **권장** 버전이다. 필요 시 개별 의존성에서 다른 버전을 사용할 수 있다. |
| `enforcedPlatform()` | BOM에 정의된 버전을 **강제**한다. 의존성 그래프 내 모든 항목에 이 버전이 적용된다.      |

- Gradle의 `platform()`은 해당 BOM이 **POM 타입이 아니어도** 사용할 수 있다.
- BOM의 `<dependencies>` 블록에 정의된 **의존성 자체는 무시**되고, 오직 `<dependencyManagement>` 정보만 적용된다.
- BOM을 여러 프로젝트에 적용하면 **멀티모듈 간 버전 불일치 문제**를 방지할 수 있다.

자세한 내용은 Gradle 문서의
[Maven BOM에서 버전 추천을 가져오기](https://docs.gradle.org/current/userguide/platforms.html#sec:bom-import) 섹션을 참고하자.

---

## [멀티 모듈 빌드 마이그레이션(Project Aggregation)](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:multimodule_builds)

`Maven`의 멀티 모듈 빌드는 `Gradle`의 [**멀티 프로젝트 빌드**](https://docs.gradle.org/current/userguide/multi_project_builds.html#multi_project_builds)로 매끄럽게 대응된다.

`Gradle`에서도 비슷한 구조로 멀티 프로젝트를 구성할 수 있으며, [Gradle 공식 샘플](https://docs.gradle.org/current/samples/sample_structuring_software_projects.html)로 확인해 볼 수도 있다.

### 마이그레이션 단계

#### 1. `settings.gradle` 스크립트 생성하기

`Maven`의 루트 POM에 있는 `<modules>` 블록은 `Gradle`의 `settings.gradle` 스크립트에서 `include` 문으로 매핑된다.

```xml
<modules>
    <module>simple-weather</module>
    <module>simple-webapp</module>
</modules>
```
```groovy
// settings.gradle
rootProject.name = 'simple-multi-module'

include 'simple-weather', 'simple-webapp'
```

- `rootProject.name`: 전체 프로젝트의 이름 설정
- `include`: 해당 서브모듈들을 멀티 프로젝트 빌드에 포함시킴

#### 2. Gradle 빌드 구조 확인하기

```bash
$ gradle projects
```
```text
------------------------------------------------------------
Root project 'simple-multi-module'
------------------------------------------------------------

+--- Project ':simple-weather'
\--- Project ':simple-webapp'

To see a list of the tasks of a project, run gradle <project-path>:tasks
For example, try running gradle :simple-weather:tasks
```

개별 프로젝트의 태스크 목록을 보고 싶다면 아래처럼 실행하면 된다:

```bash
$ gradle :simple-weather:tasks
```

#### 3. 모듈 간 의존성 교체

`Maven`에서는 `<dependency>`로 다른 모듈을 참조했지만, `Gradle`에서는 [project dependency](https://docs.gradle.org/current/userguide/declaring_dependencies_basics.html#sec:project-dependencies)로 명시한다.

```groovy
dependencies {
    implementation project(':simple-weather')
}
```

#### 4. 공통 설정 상속 구조 마이그레이션

Maven의 상속 구조(부모 POM 등)는 Gradle에선 [컨벤션 플러그인(Convention Plugins)](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html#sec:sharing_logic_via_convention_plugins) 으로 대체한다.

즉, 루트 프로젝트의 `build.gradle` 또는 별도 플러그인 프로젝트를 만들어 공통 설정을 서브 프로젝트에 주입하면 된다.

```groovy
// root build.gradle
subprojects {
    apply plugin: 'java'

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation 'org.junit.jupiter:junit-jupiter:5.9.3'
    }
}
```

### 프로젝트 공통 버전 관리

Maven의 `dependencyManagement`처럼 의존성 버전을 루트에서 일괄 관리하고 싶다면, Gradle의 [`java-platform` 플러그인](https://docs.gradle.org/current/userguide/java_platform_plugin.html#java_platform_plugin)을 사용하면 된다.

이를 위해 다음과 같이 별도 플랫폼 프로젝트를 정의하고, 다른 프로젝트들이 이를 **의존성 플랫폼으로 참조**하게 구성하면 된다:

```groovy
// versions-platform/build.gradle
plugins {
    id 'java-platform'
}

javaPlatform {
    allowDependencies()
}

dependencies {
    constraints {
        api 'org.slf4j:slf4j-api:2.0.12'
        api 'com.google.guava:guava:33.0.0-jre'
    }
}
```

```groovy
// 다른 서브 프로젝트의 build.gradle
dependencies {
    implementation platform(project(':versions-platform'))
    implementation 'org.slf4j:slf4j-api'
}
```

자세한 내용은 [Java Platform Plugin 문서](https://docs.gradle.org/current/userguide/java_platform_plugin.html#java_platform_plugin)를 참고하자.

---

## [Maven 프로파일과 프로퍼티 이전](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:profiles_and_properties)

Maven은 다양한 종류의 **프로퍼티(properties)** 를 통해 빌드를 파라미터화할 수 있다.

- 일부는 프로젝트 모델에서 제공하는 **읽기 전용 프로퍼티**
- 일부는 POM에서 직접 정의한 **사용자 지정 프로퍼티**
- 그리고 **시스템 프로퍼티**도 사용할 수 있다.

Gradle도 유사한 개념의 **프로젝트 프로퍼티** 시스템을 가지고 있지만, **시스템 프로퍼티와 구분하여 관리**한다는 점이 다르다.

### Gradle에서 프로퍼티 정의 방법

Gradle에서는 다음 위치에 프로퍼티를 정의할 수 있다:

- `build.gradle` 내부
- 프로젝트 루트 디렉토리의 `gradle.properties`
- 사용자 홈 디렉토리(`$HOME/.gradle`)의 `gradle.properties`

이 외에도 다양한 위치에서 정의할 수 있는데, 자세한 내용은 [Build Environment 챕터](https://docs.gradle.org/current/userguide/build_environment.html#build_environment)를 참고하자.

> **주의:** 동일한 프로퍼티가 빌드 스크립트와 외부 파일 모두에 정의된 경우, **빌드 스크립트(`build.gradle`)의 값이 항상 우선**한다. 이 부분은 프로파일 기능으로 해결한다.

### Maven의 Profile 기능을 Gradle에서 흉내내기

Maven의 **Profile**은 환경, 플랫폼 등 조건에 따라 빌드 구성을 전환하기 위한 도구다.

결국은 **제한된 if 조건문**처럼 작동하는 셈이다. Gradle은 더 유연하고 강력한 조건문을 지원하기 때문에, **별도 profile 시스템 없이도 Profile과 동일한 효과를 낼 수 있다.**

#### 예시: 환경별 설정 스크립트 분리 적용하기

```groovy
// build.gradle
if (!hasProperty('buildProfile')) ext.buildProfile = 'default'

apply from: "profile-${buildProfile}.gradle"

tasks.register('greeting') {
    def message = project.message
    doLast {
        println message
    }
}
```
```groovy
// profile-default.gradle
ext.message = 'foobar'

// profile-test.gradle
ext.message = 'testing 1 2 3'

// profile-prod.gradle
ext.message = 'Hello, world!'
```

- `buildProfile`이라는 프로젝트 프로퍼티 값을 기준으로 설정 스크립트를 선택
- `profile-default.gradle`, `profile-test.gradle`, `profile-prod.gradle` 등 파일을 만들어 각 환경 설정을 분리
- 설정 스크립트는 `ext.message` 값만 다르게 설정
- [프로젝트 속성](https://docs.gradle.org/current/userguide/build_environment.html#sec:project_properties)에 따라 프로필 스크립트 중 하나를 조건부로 적용
- `greeting` 태스크는 이 값을 출력

#### 실행 예시

```bash
# 기본값(default)
$ gradle greeting
> foobar

# test 프로파일 사용
$ gradle -PbuildProfile=test greeting
> testing 1 2 3
```

#### 더 다양한 조건 사용하기

- 프로젝트 프로퍼티 외에도 아래 조건으로 분기할 수 있다:
    - 환경 변수
    - OS 종류
    - JDK 버전
    - 현재 시간 등 어떤 값이든

#### 주의할 점

조건문이 많아질수록 빌드 스크립트가 복잡해지고 유지보수하기 어려워진다.

OOP에서 조건 분기 많아지면 이해가 어려워지는 것과 같다. Gradle은 Maven보다 훨씬 유연하기 때문에, **무조건 Profile을 흉내내기보단** 다음과 같은 대안을 활용하는 것을 권장한다:

- **여러 개의 유사한 태스크 구성**
- **전용 설정 파일로 태스크 분기**
- **조건부 태스크 실행 구성**

예를 들어, `Maven Publish Plugin`이 생성하는 `publish<PubName>PublicationTo<RepoName>Repository` 태스크처럼 **태스크를 조합하여 상황을 나누는 방식이 더 바람직**하다.

더 자세한 예제와 설명은 다음 블로그 포스트를 참고하자 **[Working with Maven Profiles in Gradle](https://docs.gradle.org/current/userguide/publishing_maven.html#publishing_maven:tasks)**

---

## [리소스 필터링 설정](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:filtering_resources)

`Maven`에는 `process-resources`라는 빌드 단계가 있으며, 이 단계에는 기본적으로 `resources:resources` 목표(goal)가 연결되어 있다. 

이 단계에서는 웹 리소스나 패키징된 프로퍼티 파일 등 다양한 파일에 대해 **변수 치환(variable substitution)** 작업을 수행할 수 있다.

`Gradle`의 `Java Plugin` 역시 같은 목적을 수행하는 `processResources` 작업(task)을 제공한다. 이 작업은 [ProcessResources](https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html) 타입의 태스크이며, 기본적으로 `src/main/resources` 디렉터리에 있는 파일들을 **출력 디렉터리로 복사**한다. 그리고 다른 `ProcessResources`나 `Copy` 작업과 마찬가지로, 이 작업 역시 [파일 필터링](https://docs.gradle.org/current/userguide/working_with_files.html#filtering_files), [파일명 변경](https://docs.gradle.org/current/userguide/working_with_files.html#sec:renaming_files), [콘텐츠 필터링](https://docs.gradle.org/current/userguide/working_with_files.html#sec:filtering_files) 등을 구성할 수 있다.

예를 들어, 다음과 같이 `Groovy`의 `SimpleTemplateEngine`을 이용해 리소스를 템플릿처럼 처리하면서, `version`과 `buildNumber` 값을 전달해 콘텐츠를 치환하는 설정이 가능하다:

```groovy
// Example 7. processResources 작업을 통한 리소스 콘텐츠 필터링
processResources {
    expand(version: version, buildNumber: currentBuildNumber)
}
```

위 예시에서는 리소스 파일 내에서 `${version}`이나 `${buildNumber}`와 같은 플레이스홀더를 정의해 두면, 실제 빌드 시점에 해당 값으로 치환되어 리소스가 출력된다.

사용 가능한 설정 옵션 전체는 [`CopySpec` API 문서](https://docs.gradle.org/current/javadoc/org/gradle/api/file/CopySpec.html)에서 확인할 수 있다.

---

## [통합 테스트 설정하기](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:integration_tests)

많은 `Maven` 기반 프로젝트는 어떤 형태로든 **통합 테스트(integration test)** 를 포함하고 있다. `Maven`은 이를 위해 다음과 같은 **추가 빌드 단계(phases)** 를 제공한다:

- `pre-integration-test`
- `integration-test`
- `post-integration-test`
- `verify`

또한, `Maven`은 통합 테스트 실패 시에도 빌드를 실패 처리하지 않기 위해 `Surefire Plugin` 대신 **`Failsafe Plugin`** 을 사용한다. 이는 예를 들어 **애플리케이션 서버 같은 자원을 종료(clean up)** 해야 하는 상황을 고려한 것이다.

### Gradle에서 이 동작을 구성하는 방법

Gradle에서도 **source set**을 활용하여 위와 같은 구성을 손쉽게 구현할 수 있다. 자세한 내용은 Gradle의 [Java 및 JVM 프로젝트 테스트 챕터](https://docs.gradle.org/current/userguide/java_testing.html#sec:configuring_java_integration_tests)에서 확인할 수 있다.

예를 들어, **통합 테스트가 실패해도 반드시 실행해야 하는 clean-up 작업**(예: 테스트 서버 종료)을 정의할 수 있으며, [`Task.finalizedBy()`](https://docs.gradle.org/current/dsl/org.gradle.api.Task.html)를 사용하면 통합 테스트 성공 여부와 관계없이 후속 작업을 항상 실행하도록 지정할 수 있다.

또한, 만약 통합 테스트가 실패해도 **빌드를 실패로 처리하지 않도록** 하려면, [`Test.ignoreFailures = true`](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html#org.gradle.api.tasks.testing.Test:ignoreFailures) 설정을 통해 제어할 수 있다. 

이 설정에 대한 자세한 설명은 [Java Testing 챕터의 Test Execution 섹션](https://docs.gradle.org/current/userguide/java_testing.html#sec:test_execution)에서 확인할 수 있다.

### Source Set으로 통합 테스트 분리하기

Gradle의 `source set` 기능을 사용하면, 통합 테스트 코드를 원하는 위치에 자유롭게 배치할 수 있다. 가장 일반적인 방식은 **단위 테스트와는 분리하여 `src/integTest/java` 디렉터리에 배치**하는 것이다. 물론 필요하다면 같은 디렉터리에 함께 둘 수도 있다.

여러 종류의 테스트(예: 성능 테스트, 인수 테스트 등)를 구분해서 실행하고 싶다면, **`source set`을 여러 개 생성**하고 그에 맞는 [`Test` 태스크](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.testing.Test.html)도 각각 정의하면 된다.

예를 들어:

```groovy
sourceSets {
    integTest {
        java.srcDir file('src/integTest/java')
        resources.srcDir file('src/integTest/resources')
        compileClasspath += sourceSets.main.output + configurations.testRuntimeClasspath
        runtimeClasspath += output + compileClasspath
    }
}

task integTest(type: Test) {
    testClassesDirs = sourceSets.integTest.output.classesDirs
    classpath = sourceSets.integTest.runtimeClasspath
    shouldRunAfter test
    finalizedBy shutdownServer // 항상 실행할 clean-up 태스크
}
```

이런 방식으로 Gradle에서도 `Maven`과 같은 통합 테스트 흐름을 유연하게 구성할 수 있다.

---

## [공통 플러그인 마이그레이션하기 (Migrating common plugins)](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:common_plugins)

`Maven`과 `Gradle`은 모두 **플러그인 기반으로 빌드를 확장**한다. 

두 도구의 플러그인 시스템은 내부적으로는 다르지만, **자주 사용되는 기능 중심의 플러그인들은 공통으로 존재**한다.

- **Shade / Shadow**
- **Jetty**
- **Checkstyle**
- **JaCoCo**
- **AntRun** (아래에서 별도 설명)

이게 왜 중요할까? 

많은 플러그인들이 **표준 Java 디렉터리 구조와 컨벤션**에 따라 동작하기 때문에, `Gradle`로 마이그레이션할 때는 단순히 `Maven`의 설정을 `Gradle` 형식에 맞게 **그대로 옮기기만 하면 되는 경우가 많다.**

#### 예: Maven Checkstyle 플러그인 설정

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <version>2.17</version>
  <executions>
    <execution>
      <id>validate</id>
      <phase>validate</phase>
      <configuration>
        <configLocation>checkstyle.xml</configLocation>
        <encoding>UTF-8</encoding>
        <consoleOutput>true</consoleOutput>
        <failsOnError>true</failsOnError>
        <linkXRef>false</linkXRef>
      </configuration>
      <goals>
        <goal>check</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

#### Gradle에서 동일한 설정

```groovy
checkstyle {
    config = resources.text.fromFile('checkstyle.xml', 'UTF-8')
    showViolations = true
    ignoreFailures = false
}
```

> `Maven`에서는 `<executions>`와 `<goals>` 같은 구조를 써야 했지만, `Gradle`에서는 훨씬 간단하게 설정할 수 있다.

#### 실행 순서 제어: 테스트보다 먼저 실행되게 하기

```groovy
test.mustRunAfter checkstyleMain, checkstyleTest
```

Gradle의 `check` 태스크는 기본적으로 `checkstyle`, `test` 등 다양한 검증 태스크들을 **자동으로 포함**한다. 특정 순서대로 실행되게 하려면 [`mustRunAfter`](https://docs.gradle.org/current/dsl/org.gradle.api.Task.html#org.gradle.api.Task:mustRunAfter(java.lang.Object[])) 같은 메서드를 이용하면 된다.

### AntRun 마이그레이션: Gradle에선 안트가 기본 내장이다

많은 `Maven` 프로젝트에서 `AntRun` 플러그인을 사용해 **간단한 작업을 커스텀**해왔다. 

하지만 `Gradle`에선 `Ant`가 **1급 시민(first-class)** 으로 통합되어 있어, 별도 플러그인이 필요 없다.

```groovy
tasks.register('sayHello') {
    doLast {
        ant.echo message: 'Hello!'
    }
}
```

- `ant.echo`, `ant.fileset`, `ant.property` 등 **안트 태스크 대부분을 직접 사용할 수 있다.**
- 더 나은 방법은 [`Ant` 태스크](https://docs.gradle.org/current/userguide/ant.html#ant)를 그대로 쓰기보다 **Gradle 태스크로 재구현(custom task type)** 해서, **증분 빌드, 캐시** 등 `Gradle`의 이점을 제대로 활용하는 것이다.

### 요약

- Gradle은 Maven과 달리 **고정된 빌드 단계가 없다.** 원하는 태스크를 자유롭게 구성하고, 실행 순서를 명시할 수 있다.
- **source set**, **task DSL**, **plugin 시스템**, **Ant 통합** 등은 마이그레이션 시 큰 장점이 된다.
- Maven의 플러그인 설정을 옮길 때는 핵심 설정만 추려서, Gradle DSL에 맞게 구성하자.

이후에도 마이그레이션 시 `sourceSet`을 적절히 활용하는 것이 유지 보수성과 확장성을 높이는 데 매우 유용하다.

---

## [불필요한 플러그인 파악하기](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:unnecessary_plugins)

Gradle로 마이그레이션할 때 기억해야 할 중요한 점 중 하나는, **Gradle 빌드는 Maven보다 확장성과 커스터마이징이 훨씬 쉽다**는 것이다.

이 말은 곧, **Maven에서는 반드시 플러그인을 써야 했던 작업들도 Gradle에선 일반 빌드 스크립트만으로 해결할 수 있다는 뜻**이다.

예를 들어:

- **Maven Enforcer 플러그인**은 특정 라이브러리 버전을 강제하거나, JDK 버전 등 환경 제약 조건을 검사할 때 자주 사용된다.
- 하지만 Gradle에서는 이런 제약을 **별도 플러그인 없이 빌드 스크립트 안에서 간단히 작성**할 수 있다.

#### 예: JDK 버전 강제

```groovy
if (!JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    throw new GradleException("Java 17 or higher is required.")
}
```

#### 예: 특정 라이브러리 버전 강제

```groovy
configurations.all {
    resolutionStrategy {
        force 'com.fasterxml.jackson.core:jackson-databind:2.15.3'
    }
}
```

### 요약

- Maven에서 Enforcer, Properties, Build Helper 같은 **보조 플러그인을 남발**했다면,
- Gradle에선 대부분 **기본 DSL 기능이나 조건문, 설정 블록**으로 **간단하고 직관적으로 해결**할 수 있다.
- 마이그레이션할 때 “**이 플러그인을 반드시 Gradle에서도 써야 하나?**” 자문해보는 것이 중요하다.
  - 대부분은 불필요하다. Gradle은 기본만으로도 충분히 유연하다.

---

## [비표준 또는 커스텀 플러그인 대응](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:custom_plugins)

마이그레이션을 진행하다 보면 Gradle에 **대응되는 플러그인이 존재하지 않는 Maven 플러그인**을 마주칠 수 있다.

이런 경우는 특히 아래 두 상황에서 흔히 나타난다:

- 사내에서 직접 만든 Maven 커스텀 플러그인을 사용하는 경우
- 생태계에서 흔하지 않은 특수 목적 플러그인을 사용하는 경우

이럴 땐 Gradle이나 Maven의 작동 방식을 어느 정도 이해하고 있어야 하며, [직접 플러그인을 새로 작성](https://docs.gradle.org/current/userguide/custom_tasks.html#custom_tasks)해야 할 수도 있다.

### Maven 플러그인의 두 가지 유형

1. **Maven Project 객체에 의존하지 않는 플러그인**
   - 이런 경우는 대부분 쉽게 Gradle의 커스텀 작업(Task)으로 이식할 수 있다.
   - Maven의 `mojo` 파라미터를 Gradle Task의 input/output으로 매핑하고
   - 실행 로직을 `doLast` 같은 작업 블록으로 옮기면 된다.
2. **Maven Project 객체를 참조하거나 내부 모델에 깊게 의존하는 플러그인**
   - 이런 경우는 단순히 포팅이 아닌 **재설계**가 필요하다.
   - Maven의 빌드 모델과 Gradle의 빌드 모델은 근본적으로 다르기 때문에 단순히 로직을 옮기기보단 **"이 플러그인이 해결하고자 하는 문제는 무엇인가?"** 를 먼저 생각해야 한다.
   - Gradle의 DSL과 확장성은 Maven보다 강력해서, 동일 기능을 훨씬 간결하게 구현할 수 있는 경우도 많다.

### 직접 구현 시 참고할 문서

Gradle은 **Groovy DSL**을 기반으로 강력한 API를 제공한다.
다음 문서를 참고하면 커스텀 Task나 Plugin을 작성하는 데 큰 도움이 된다:

| 상황                                  | 대처 방법                       |
|-------------------------------------|-----------------------------|
| Maven 플러그인이 단순한 파일 처리, CLI 실행 등만 수행 | Gradle `Task`로 대체           |
| Maven 플러그인이 Maven 내부 구조를 많이 참조      | Gradle에서 문제 정의부터 재설계 필요     |
| Ant 기반                              | Gradle에서 `ant` 객체로 직접 호출 가능 |
| 재사용 필요                              | 별도 Gradle Plugin으로 추출 가능    |

- [Plugin 개발 가이드](https://gradle.org/guides/?q=Plugin%20Development)
- [Groovy DSL 레퍼런스](https://docs.gradle.org/current/dsl/)
  - 특히 `Project`, `Task`, `Copy`, `JavaExec` 등 핵심 타입
- [`Project`](https://docs.gradle.org/current/dsl/org.gradle.api.Project.html) 객체가 모든 빌드 스크립트의 최상위 엔트리 포인트이다.

Gradle은 유연성이 뛰어나므로, Maven에서 복잡한 플러그인을 써야 했던 이유 자체가 Gradle에서는 사라지는 경우도 많다.

**"어떻게 이 기능을 똑같이 만들까?"가 아니라 "Gradle에선 이걸 어떻게 풀 수 있을까?"** 를 중심에 두자.

---

## [추가로 참고할 자료](https://docs.gradle.org/current/userguide/migrating_from_maven.html#further_reading)

이번 장에서는 Maven 빌드를 Gradle로 마이그레이션할 때 꼭 알아야 할 주요 주제들을 다루었다.

이제 마이그레이션을 완료한 이후, 또는 중간에 참고하면 유용할 수 있는 몇 가지 영역을 더 소개한다:

#### Gradle 빌드 환경 설정하기

- Gradle 자체를 실행하는 JVM 설정
- Gradle 데몬 설정
- `gradle.properties` 또는 환경 변수 활용법

> [Build Environment 공식 문서](https://docs.gradle.org/current/userguide/build_environment.html#build_environment)

#### 빌드를 효과적으로 구성하는 방법

- 단일 프로젝트 vs 멀티 프로젝트 구성
- 공통 설정의 추출 및 재사용 방법
- Convention Plugin을 이용한 설정 공유

> [Structuring Builds 문서](https://docs.gradle.org/current/userguide/organizing_gradle_projects.html#organizing_gradle_projects)

#### 로깅 설정 및 사용법

- 빌드 과정에서 로그 출력하는 방법 (`logger.lifecycle`, `logger.debug` 등)
- Gradle의 로깅 레벨 조절 (`--info`, `--debug`, `--quiet`)
- 사용자 정의 Task에서 로그를 적절히 출력하는 패턴

> [Logging 문서](https://docs.gradle.org/current/userguide/logging.html#logging)

### 그 외에도 Gradle은 강력한 기능들을 많이 제공한다

이번 마이그레이션 가이드는 Gradle이 제공하는 기능 중 일부만 다루었을 뿐이다.

Gradle 사용자 가이드의 다른 챕터나 [Gradle 샘플 코드 모음](https://docs.gradle.org/current/samples/index.html)을 통해 더 많은 기능을 직접 실습해보길 추천한다.

> Gradle은 단순한 빌드 도구가 아닌 **강력한 빌드 플랫폼**이다. <br/>
> Maven과 똑같이 만들려 하기보다, Gradle의 철학에 맞춰 리팩토링하고 최적화하는 것이 핵심이다.

---

## Reference

- https://docs.gradle.org/current/userguide/migrating_from_maven.html
